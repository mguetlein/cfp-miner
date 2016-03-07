package org.kramerlab.cfpminer.experiments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.kramerlab.cfpminer.experiments.validation.PaperValidationResults;
import org.kramerlab.cfpminer.weka.eval2.CFPtoArff;
import org.mg.cdklib.CDKConverter;
import org.mg.cdklib.cfp.CFPFragment;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.CDKDataset;
import org.mg.cdklib.data.DataLoader;
import org.mg.javalib.util.ListUtil;
import org.mg.javalib.util.StopWatchUtil;
import org.mg.wekalib.eval2.model.AbstractModel;
import org.mg.wekalib.eval2.model.Model;
import org.mg.wekalib.eval2.model.ModelProvider;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

@SuppressWarnings("unchecked")
public class ModelBuildingRuntimes extends PaperResults
{

	private static String getRuntimeKey(String datasetName, CFPType type,
			FeatureSelection featureSelection, int hashfoldsize, Model classifier, boolean build)
	{
		return datasetName + "#" + type.toString() + "#" + featureSelection.toString() + "#"
				+ hashfoldsize + "#" + classifier.getKeyContent() + "#" + build;
	}

	private static HashMap<String, Double> runtimes = new HashMap<String, Double>();
	private static String runtimesFile = RESULTS + "/runtimes/runtimes.hash";

	static
	{
		try
		{
			if (new File(runtimesFile).exists())
			{
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(runtimesFile));
				runtimes = (HashMap<String, Double>) ois.readObject();
				ois.close();
				System.out.println("runtime file loaded with " + runtimes.size() + " entries");
			}
			else
				runtimes = new HashMap<>();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static double getRuntime(String datasetName, CFPType type,
			FeatureSelection featureSelection, int hashfoldsize, Model classifier, boolean build)
	{
		String k = getRuntimeKey(datasetName, type, featureSelection, hashfoldsize, classifier,
				build);
		if (runtimes.containsKey(k))
			return runtimes.get(k);
		else
		{
			System.err.println("runtime not found " + k);
			return 0;
		}
	}

	private static void estimateSingleRuntime(String datasetName, CFPType type,
			FeatureSelection featureSelection, int hashfoldsize, Model classifier) throws Exception
	{
		System.out.println("start " + datasetName + " " + type + " " + featureSelection + " "
				+ hashfoldsize + " " + classifier);
				//StopWatchUtil.start("all");

		//StopWatchUtil.start("all > load data");

		CDKDataset dataset = DataLoader.INSTANCE.getDataset(datasetName);
		List<String> list = dataset.getSmiles();
		List<String> endpointValues = dataset.getEndpoints();
		ListUtil.scramble(new Random(1), list, endpointValues);

		//StopWatchUtil.stop("all > load data");
		//StopWatchUtil.start("all > create");

		long start = StopWatchUtil.getCpuTime();// StopWatchUtil.getCpuTime();
		//StopWatchUtil.start("all > create > mine");

		CFPMiner cfps = new CFPMiner(ListUtil.cast(String.class, endpointValues));
		cfps.setType(type);
		cfps.setFeatureSelection(featureSelection);
		cfps.setHashfoldsize(hashfoldsize);
		cfps.mine(list);

		//StopWatchUtil.stop("all > create > mine");

		//StopWatchUtil.start("all > create > filter");
		if (featureSelection == FeatureSelection.filt)
			cfps.applyFilter();
		//StopWatchUtil.stop("all > create > filter");

		//StopWatchUtil.start("all > create > build model");

		//StopWatchUtil.start("all > create > build model > create instances");
		Instances inst = CFPtoArff.getTrainingDataset(cfps, datasetName);
		inst.setClassIndex(inst.numAttributes() - 1);

		if (!(inst.get(0) instanceof SparseInstance))
			throw new IllegalStateException();
		//		if (classi instanceof SMO)
		//		{
		//			NonSparseToSparse f = new NonSparseToSparse();
		//			f.setInputFormat(inst);
		//			inst = NonSparseToSparse.useFilter(inst, f);
		//		}

		//StopWatchUtil.stop("all > create > build model > create instances");
		//StopWatchUtil.start("all > create > build model > train");
		Classifier classi = ((AbstractModel) classifier).getWekaClassifer();
		classi.buildClassifier(inst);
		//StopWatchUtil.stop("all > create > build model > train");
		//StopWatchUtil.stop("all > create > build model");

		//StopWatchUtil.stop("all > create");
		long create = StopWatchUtil.getCpuTime() - start;

		start = StopWatchUtil.getCpuTime();// StopWatchUtil.getCpuTime();

		//		StopWatchUtil.start("all > predict");
		for (String smi : list)
		{
			//			StopWatchUtil.start("all > predict > create instance");
			double vals[] = new double[cfps.getNumFragments() + 1];
			HashSet<CFPFragment> set = cfps
					.getFragmentsForTestCompound(CDKConverter.parseSmiles(smi));
			for (int i = 0; i < vals.length - 1; i++)
				vals[i] = set.contains(cfps.getFragmentViaIdx(i)) ? 1.0 : 0.0;
			Instance testInst;
			if (classi instanceof SMO)
				testInst = new SparseInstance(1.0, vals);
			else
				testInst = new DenseInstance(1.0, vals);
			testInst.setDataset(inst);
			//			StopWatchUtil.stop("all > predict > create instance");
			//			StopWatchUtil.start("all > predict > classify");
			//			double d[] = 
			classi.distributionForInstance(testInst);
			//			StopWatchUtil.stop("all > predict > classify");
			//			System.out.println(ArrayUtil.toString(d));
		}
		//		StopWatchUtil.stop("all > predict");
		long predict = StopWatchUtil.getCpuTime() - start;
		//StopWatchUtil.stop("all");
		//		StopWatchUtil.print();

		String key = getRuntimeKey(datasetName, type, featureSelection, hashfoldsize, classifier,
				true);
		System.out.println(key + " " + create / 1000.0);
		//		System.out.println(runtimes.get(k));
		runtimes.put(key, create / 1000.0);

		key = getRuntimeKey(datasetName, type, featureSelection, hashfoldsize, classifier, false);
		System.out.println(key + " " + predict / 1000.0);
		//		System.out.println(runtimes.get(k));
		runtimes.put(key, predict / 1000.0);

		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(runtimesFile));
		oos.writeObject(runtimes);
		oos.flush();
		oos.close();
	}

	static NumFragmentsAndCollisions frags = new NumFragmentsAndCollisions();

	public static void estimateNextRuntime() throws Exception
	{
		String datasets[] = DataLoader.INSTANCE.allDatasetsSorted();
		for (Integer size : PaperValidationResults.SIZES)
		{
			for (String datasetName : datasets)
			{
				CFPType type = CFPType.ecfp4;
				{
					if (size >= frags.getNumFeatures(datasetName, type))
						continue;

					for (Model classifier : ModelProvider.ALL_MODELS_PARAM_DEFAULT)
					{
						for (FeatureSelection featureSelection : FeatureSelection.values())
						{
							int hashfoldsize = (featureSelection == FeatureSelection.none) ? 0
									: size;
							{
								String k = getRuntimeKey(datasetName, type, featureSelection,
										hashfoldsize, classifier, true);
								if (!runtimes.containsKey(k))
								{
									estimateSingleRuntime(datasetName, type, featureSelection,
											hashfoldsize, classifier);
									System.exit(0);
								}
							}
						}
					}
				}
			}
		}
		System.exit(1);
	}

	public static void main(String[] args) throws Exception
	{
		//		for (String key : new HashSet<>(runtimes.keySet()))
		//		{
		//			if (key.contains("MUV_692"))
		//			{
		//				System.out.println(key);
		//				runtimes.remove(key);
		//			}
		//		}
		//		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(runtimesFile));
		//		oos.writeObject(runtimes);
		//		oos.flush();
		//		oos.close();

		estimateNextRuntime();

		//		//		//demo();
		//		String datasetName = "AMES";
		//		CFPType type = CFPType.ecfp4;
		//		FeatureSelection featureSelection = FeatureSelection.none;
		//		int hashfoldsize = 1024;
		//		String classifier = "RaF";
		//		//
		//		estimateRuntime(datasetName, type, featureSelection, hashfoldsize, classifier);
	}
}
