package org.kramerlab.cfpminer.experiments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
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
import org.mg.cdklib.data.DataProvider;
import org.mg.cdklib.data.DataProvider.DataID;
import org.mg.javalib.util.DoubleArraySummary;
import org.mg.javalib.util.ListUtil;
import org.mg.javalib.util.StopWatchUtil;
import org.mg.wekalib.eval2.model.AbstractModel;
import org.mg.wekalib.eval2.model.Model;
import org.mg.wekalib.eval2.model.ModelProvider;
import org.mg.wekalib.eval2.model.RandomForestModel;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO_ridgeAdjustable;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

@SuppressWarnings("unchecked")
public class ModelBuildingRuntimes extends PaperResults
{

	private static String getRuntimeKey(DataID dataset, CFPType type,
			FeatureSelection featureSelection, int hashfoldsize, Model classifier, boolean build)
	{
		return dataset.toString() + "#" + type.toString() + "#" + featureSelection.toString() + "#"
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
				System.out.println("runtime file loaded with " + runtimes.size() + " entries "
						+ new File(runtimesFile).getAbsolutePath());
			}
			else
				runtimes = new HashMap<>();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static double getRuntime(DataID d, CFPType type, FeatureSelection featureSelection,
			int hashfoldsize, Model classifier, boolean build)
	{
		String k = getRuntimeKey(d, type, featureSelection, hashfoldsize, classifier, build);
		if (runtimes.containsKey(k))
			return runtimes.get(k);
		else
		{
			System.err.println("runtime not found " + k);
			return 0;
		}
	}

	private static void estimateSingleRuntime(DataID d, CFPType type,
			FeatureSelection featureSelection, int hashfoldsize, Model classifier) throws Exception
	{
		System.out.println("start " + d + " " + type + " " + featureSelection + " " + hashfoldsize
				+ " " + classifier);
		//StopWatchUtil.start("all");

		//StopWatchUtil.start("all > load data");

		CDKDataset dataset = DataProvider.getDataset(d);
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

		//		long startFilter = StopWatchUtil.getCpuTime();
		//		long mineTime = startFilter - start;
		//		System.err.println("mine " + mineTime / 1000.0);

		//StopWatchUtil.stop("all > create > mine");

		//StopWatchUtil.start("all > create > filter");
		if (featureSelection == FeatureSelection.filt)
			cfps.applyFilter();

		//		long filterTime = StopWatchUtil.getCpuTime() - startFilter;
		//		System.err.println("filter " + filterTime / 1000.0);

		//StopWatchUtil.stop("all > create > filter");

		//StopWatchUtil.start("all > create > build model");

		//StopWatchUtil.start("all > create > build model > create instances");
		Instances inst = CFPtoArff.getTrainingDataset(cfps, d.toString());
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
			if (classi instanceof SMO_ridgeAdjustable)
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

		String key = getRuntimeKey(d, type, featureSelection, hashfoldsize, classifier, true);
		System.out.println(key + " " + create / 1000.0);
		//		System.out.println(runtimes.get(k));
		runtimes.put(key, create / 1000.0);

		key = getRuntimeKey(d, type, featureSelection, hashfoldsize, classifier, false);
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
		for (Integer size : PaperValidationResults.SIZES)
		{
			for (DataID dataset : DATASETS)
			{
				CFPType type = CFPType.ecfp4;
				{
					if (size >= frags.getNumFeatures(dataset, type))
						continue;

					for (Model classifier : ModelProvider.ALL_MODELS_PARAM_DEFAULT)
					{
						for (FeatureSelection featureSelection : FeatureSelection.values())
						{
							int hashfoldsize = (featureSelection == FeatureSelection.none) ? 0
									: size;
							{
								String k = getRuntimeKey(dataset, type, featureSelection,
										hashfoldsize, classifier, true);
								if (!runtimes.containsKey(k))
								{
									estimateSingleRuntime(dataset, type, featureSelection,
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

		//estimateNextRuntime();

		//		//		//demo();
		List<Double> filtBuildTime = new ArrayList<Double>();
		List<Double> foldBuildTime = new ArrayList<Double>();
		List<Double> filterTime = new ArrayList<Double>();
		//		String datasetName = "AMES";
		for (DataID dataset : DATASETS)
		{
			System.out.println(dataset);
			CFPType type = CFPType.ecfp4;
			int hashfoldsize = 2048;
			Model classifier = new RandomForestModel();
			double t1 = getRuntime(dataset, type, FeatureSelection.filt, hashfoldsize, classifier,
					true);
			double t2 = getRuntime(dataset, type, FeatureSelection.fold, hashfoldsize, classifier,
					true);
			filtBuildTime.add(t1);
			foldBuildTime.add(t2);
			filterTime.add(t1 - t2);
		}
		System.out.println(DoubleArraySummary.create(filtBuildTime));
		System.out.println(DoubleArraySummary.create(foldBuildTime));
		System.out.println(DoubleArraySummary.create(filterTime));

		//		estimateRuntime(datasetName, type, featureSelection, hashfoldsize, classifier);
	}
}
