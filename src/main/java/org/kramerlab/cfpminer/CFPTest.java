package org.kramerlab.cfpminer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.kramerlab.cfpminer.weka.CFPValidate;
import org.mg.cdklib.CDKConverter;
import org.mg.cdklib.cfp.CFPFragment;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.CDKDataset;
import org.mg.cdklib.data.DataLoader;
import org.mg.javalib.util.ListUtil;
import org.mg.javalib.util.StopWatchUtil;
import org.mg.wekalib.attribute_evaluation.AttributeCrossvalidator;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.RandomForest;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.filters.unsupervised.instance.NonSparseToSparse;

@SuppressWarnings("unchecked")
public class CFPTest
{

	public static void amesRuntimeTest() throws Exception
	{
		AttributeCrossvalidator.RUNTIME_DEBUG = true;

		String datasetName = "AMES";
		int run = 1;
		CFPType type = CFPType.ecfp6;
		FeatureSelection featureSelection = FeatureSelection.filt;
		int hashfoldsize = 1024;

		CDKDataset dataset = new DataLoader("data").getDataset(datasetName, run);
		List<String> list = dataset.getSmiles();
		List<String> endpointValues = dataset.getEndpoints();
		ListUtil.scramble(new Random(1), list, endpointValues);

		CFPMiner cfps = new CFPMiner(ListUtil.cast(String.class, endpointValues));
		cfps.setType(type);
		cfps.setFeatureSelection(featureSelection);
		cfps.setHashfoldsize(hashfoldsize);
		cfps.mine(list);

		String classifier = "SMO";
		for (int i : new int[] { 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192 })
		{
			cfps.setHashfoldsize(i);
			CFPValidate.validate(datasetName, run, "/dev/null", new String[] { classifier }, endpointValues,
					new CFPMiner[] { cfps });
		}

		System.exit(0);
	}

	public static void demo() throws Exception
	{
		String datasetName = "CPDBAS_MultiCellCall";
		int run = 1;
		CFPType type = CFPType.fcfp4;
		FeatureSelection featureSelection = FeatureSelection.filt;
		//		int hashfoldsize = 1024;

		CDKDataset dataset = new DataLoader("data").getDataset(datasetName, run);
		List<String> list = dataset.getSmiles();
		List<String> endpointValues = dataset.getEndpoints();
		ListUtil.scramble(new Random(1), list, endpointValues);

		CFPMiner cfps = new CFPMiner(ListUtil.cast(String.class, endpointValues));
		cfps.setType(type);
		cfps.setFeatureSelection(featureSelection);
		//		cfps.hashfoldsize = hashfoldsize;
		cfps.mine(list);

		for (String classifier : new String[] { "SMO", "RaF" })
		{
			for (int i : new int[] { classifier.equals("RaF") ? 1024 : 1024 })
			{
				Boolean b = null;
				//				for (Boolean b : new boolean[] { true, false })
				//				{
				//					AttributeCrossvalidator.FORCE_SPARSE = b;
				cfps.setHashfoldsize(i);
				CFPValidate.validate(datasetName, run, "/tmp/" + classifier + "_" + b + "_mult.arff",
						new String[] { classifier }, endpointValues, new CFPMiner[] { cfps });
				//				}
			}
		}

		System.exit(0);
	}

	private static String getRuntimeKey(String datasetName, CFPType type, FeatureSelection featureSelection,
			int hashfoldsize, String classifier, boolean build)
	{
		return datasetName + "#" + type.toString() + "#" + featureSelection.toString() + "#" + hashfoldsize + "#"
				+ classifier + "#" + build;
	}

	private static HashMap<String, Double> runtimes = new HashMap<String, Double>();
	private static String runtimesFile = "/home/martin/tmp/runtimes";

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

	public static double getRuntime(String datasetName, CFPType type, FeatureSelection featureSelection,
			int hashfoldsize, String classifier, boolean build)
	{
		String k = getRuntimeKey(datasetName, type, featureSelection, hashfoldsize, classifier, build);
		if (runtimes.containsKey(k))
			return runtimes.get(k);
		else
			return 0;
	}

	private static void estimateRuntime(String datasetName, CFPType type, FeatureSelection featureSelection,
			int hashfoldsize, String classifier) throws Exception
	{
		//StopWatchUtil.start("all");

		int run = 1;

		//StopWatchUtil.start("all > load data");

		DataLoader loader = new DataLoader("data");
		CDKDataset dataset = loader.getDataset(datasetName, run);
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

		Classifier classi;
		if (classifier.equals("RnF"))
			classi = new RandomForest();
		else if (classifier.equals("SMO"))
			classi = new SMO();
		else if (classifier.equals("NBy"))
			classi = new NaiveBayes();
		else
			throw new IllegalStateException();
		//StopWatchUtil.start("all > create > build model");

		//StopWatchUtil.start("all > create > build model > create instances");
		Instances inst = CFPtoArff.getTrainingDataset(cfps, datasetName);
		inst.setClassIndex(inst.numAttributes() - 1);
		if (classi instanceof SMO)
		{
			NonSparseToSparse f = new NonSparseToSparse();
			f.setInputFormat(inst);
			inst = NonSparseToSparse.useFilter(inst, f);
		}
		//StopWatchUtil.stop("all > create > build model > create instances");
		//StopWatchUtil.start("all > create > build model > train");
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
			HashSet<CFPFragment> set = cfps.getFragmentsForTestCompound(CDKConverter.parseSmiles(smi));
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

		String k = getRuntimeKey(datasetName, type, featureSelection, hashfoldsize, classifier, true);
		System.out.println(k + " " + create / 1000.0);
		//		System.out.println(runtimes.get(k));
		runtimes.put(k, create / 1000.0);

		k = getRuntimeKey(datasetName, type, featureSelection, hashfoldsize, classifier, false);
		System.out.println(k + " " + predict / 1000.0);
		//		System.out.println(runtimes.get(k));
		runtimes.put(k, predict / 1000.0);

		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(runtimesFile));
		oos.writeObject(runtimes);
		oos.flush();
		oos.close();
	}

	public static void measureRuntimes() throws Exception
	{
		String datasets[] = new DataLoader("data").allDatasets();
		Arrays.sort(datasets, DataLoader.CFPDataComparator);
		for (String datasetName : datasets)
		{
			CFPType type = CFPType.ecfp4;
			{
				for (String classifier : new String[] { "RnF", "SMO", "NBy" })
				{
					for (FeatureSelection featureSelection : FeatureSelection.values())
					{
						int hashfoldsize = (featureSelection == FeatureSelection.none) ? 0 : 2048;
						{
							String k = getRuntimeKey(datasetName, type, featureSelection, hashfoldsize, classifier,
									true);
							if (!runtimes.containsKey(k))
							{
								estimateRuntime(datasetName, type, featureSelection, hashfoldsize, classifier);
								System.exit(0);
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
		demo();

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
