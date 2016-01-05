package org.kramerlab.cfpminer.weka.eval2;

import java.util.ArrayList;
import java.util.List;

import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.CDKDataset;
import org.mg.cdklib.data.DataLoader;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.ListUtil;
import org.mg.wekalib.eval2.CV;
import org.mg.wekalib.eval2.CVEvalModel;
import org.mg.wekalib.eval2.CVEvaluator;
import org.mg.wekalib.eval2.MultiDatasetRunner;
import org.mg.wekalib.eval2.job.DataSetJobOwner;
import org.mg.wekalib.eval2.job.FeatureProvider;
import org.mg.wekalib.eval2.job.JobOwner;
import org.mg.wekalib.eval2.job.Printer;
import org.mg.wekalib.eval2.model.FeatureModel;
import org.mg.wekalib.eval2.model.Model;
import org.mg.wekalib.eval2.model.NaiveBayesModel;
import org.mg.wekalib.eval2.model.RandomForestModel;
import org.mg.wekalib.eval2.model.SupportVectorMachineModel;
import org.mg.wekalib.eval2.persistance.DB;

import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.functions.supportVector.RBFKernel;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.codec.ByteArrayCodec;

public class CFPValidate2
{
	public static JobOwner<?> singleCV() throws Exception
	{
		String datasetName = "CPDBAS_MultiCellCall";
		CDKDataset dataset = new DataLoader("data").getDataset(datasetName);
		CDKDataSet data = new CDKDataSet(datasetName, dataset);

		CFPFeatureProvider feat = new CFPFeatureProvider(1024, FeatureSelection.filt, CFPType.ecfp4);

		Model basicModel = new RandomForestModel();

		FeatureModel fm = new FeatureModel();
		fm.setFeatureProvider(feat);
		fm.setModel(basicModel);

		CV cv = new CV();
		cv.setDataSet(data);
		cv.setModel(fm);
		cv.setNumFolds(10);
		cv.setRandomSeed(4);

		return cv;
	}

	public static JobOwner<?> cvEval() throws Exception
	{
		String datasetName = "CPDBAS_MultiCellCall";
		CDKDataset dataset = new DataLoader("data").getDataset(datasetName);
		CDKDataSet data = new CDKDataSet(datasetName, dataset);

		CFPFeatureProvider feats[] = new CFPFeatureProvider[] {
				new CFPFeatureProvider(1024, FeatureSelection.filt, CFPType.ecfp4),
				new CFPFeatureProvider(1024, FeatureSelection.fold, CFPType.ecfp4) };

		Model basicModels[] = new Model[] { new RandomForestModel(), new NaiveBayesModel() };

		List<Model> featModels = new ArrayList<>();
		for (Model model : basicModels)
		{
			for (FeatureProvider feat : feats)
			{
				FeatureModel fm = new FeatureModel();
				fm.setFeatureProvider(feat);
				fm.setModel(model);
				featModels.add(fm);
			}
		}

		CVEvaluator cv = new CVEvaluator();
		cv.setDataSet(data);
		cv.setModels(ListUtil.toArray(featModels));
		cv.setNumFolds(10);
		cv.setRepetitions(1);

		//		CVEvalModel cvm = new CVEvalModel();
		//		cvm.setCvEvaluator(cv);
		//		cvm.setTrainingDataset(data);
		//		cvm.setTestDataset(data);

		return cv;
	}

	/*
	 * nested CV has two modes:
	 * 
	 * evaluation run:
	 * - outer and inner cv
	 * - evalutes the predictivity of selecting the best model with cv
	 * 
	 * model building run:
	 * - only inner cv on entire dataset (hence, its actually no inner cv anymore)
	 * - selects the final model
	 */
	public static JobOwner<?> nestedCV(boolean evaluationRun, String filter) throws Exception
	{
		CFPFeatureProvider feats[] = new CFPFeatureProvider[0];

		for (Integer size : new Integer[] { 1024, 2048, 4096, 8192 })
			if (filter == null || filter.contains(size.toString()))
				feats = ArrayUtil.push(CFPFeatureProvider.class, feats, new CFPFeatureProvider(
						size, FeatureSelection.filt, CFPType.ecfp4));

		List<Model> classifiers = new ArrayList<Model>();
		classifiers.add(new NaiveBayesModel());
		classifiers.add(new RandomForestModel());
		Double cs[] = new Double[] { 1.0, 10.0, 100.0 };
		for (Double g : new Double[] { 0.001, 0.01, 0.1 })
		{
			for (Double c : cs)
			{
				if (c == 1.0 && g == 0.001) // does not work well
					continue;
				SupportVectorMachineModel svm = new SupportVectorMachineModel();
				svm.setC(c);
				svm.setKernel(new RBFKernel());
				svm.setGamma(g);
				classifiers.add(svm);
			}
		}
		for (Double e : new Double[] { 1.0 }) // exponent optimizing not needed , 2.0, 3.0
		{
			for (Double c : cs)
			{
				SupportVectorMachineModel svm = new SupportVectorMachineModel();
				svm.setC(c);
				svm.setKernel(new PolyKernel());
				svm.setExp(e);
				classifiers.add(svm);
			}
		}

		List<Model> featModels = new ArrayList<>();
		for (Model model : classifiers)
		{
			for (FeatureProvider feat : feats)
			{
				FeatureModel fm = new FeatureModel();
				fm.setFeatureProvider(feat);
				fm.setModel(model);
				featModels.add(fm);
			}
		}

		CVEvaluator innerCV = new CVEvaluator();
		innerCV.setModels(ListUtil.toArray(featModels));
		innerCV.setNumFolds(10);
		innerCV.setRepetitions(3);

		DataSetJobOwner<String> job;
		if (evaluationRun)
		{
			CVEvalModel cvm = new CVEvalModel();
			cvm.setCvEvaluator(innerCV);

			CVEvaluator outerCV = new CVEvaluator();
			outerCV.setModels(cvm);
			outerCV.setNumFolds(10);
			outerCV.setRepetitions(3);
			job = outerCV;
		}
		else
			job = innerCV;

		List<CDKDataSet> data = new ArrayList<>();
		DataLoader loader = new DataLoader("data");
		for (String datasetName : loader.allDatasets())
		{
			if (filter == null || filter.contains(datasetName + ","))
			{
				CDKDataset dataset = loader.getDataset(datasetName);
				data.add(new CDKDataSet(datasetName, dataset));
			}
		}
		//outerCV.setDataSet(data.get(0));

		MultiDatasetRunner<String> d = new MultiDatasetRunner<>();
		d.setJob(job);
		d.setDataSets(ListUtil.toArray(data));

		if (d.isDone())
			if (evaluationRun)
			{
				//TODO
			}
			else
			{
				for (DataSetJobOwner<String> j : d.jobs())
				{
					System.out.println(((CVEvaluator) j).getBestModel().getName());
				}
			}

		return d;
	}

	private static String getHost(String pw)
	{
		//return pw + "@134.93.175.97";
		return "127.0.0.1";
	}

	public static void clear(String pw) throws Exception
	{
		RedisClient redisClient = new RedisClient(RedisURI.create("redis://" + getHost(pw)
				+ ":6379"));
		DB.setBlocker(new RedisBlocker(redisClient.connect()));
		DB.getBlocker().clear();
		System.out.println("cleared blocker!");

		DB.setResultProvider(new RedisResultProvider(redisClient.connect(new ByteArrayCodec())));
		DB.getResultProvider().clear();
		System.out.println("cleared result db!");

		System.exit(1);
	}

	public static void main(String[] args) throws Exception
	{
		Printer.PRINT_TO_SYSTEM_OUT = true;
		//args = new String[] { "1024,2048,4096,8192,CPDBAS_Hamster,DUD_vegfr2" };
		//		args = new String[] { "1024,2048,4096,8192,CPDBAS_Hamster,DUD_vegfr2" };
		//args = new String[] { "1024,2048,4096,8192,CPDBAS_Hamster" };
		args = new String[] { "1024,2048,4096,8192,ChEMBL_51," };

		//		StopWatchUtil.setUseCpuTime(false);
		//		DB.setResultProvider(new ResultProviderTime(new ResultProviderImpl()));
		//clear(args.length > 0 ? args[0] : "");
		//		RedisClient redisClient = new RedisClient(RedisURI.create("redis://" + getHost(args[0]) + ":6379"));
		//		DB.setBlocker(new BlockerTime(new RedisBlocker(redisClient.connect())));
		//		DB.setResultProvider(new ResultProviderTime(new RedisResultProvider(redisClient.connect(new ByteArrayCodec()))));

		nestedCV(false, args.length > 0 ? args[0] : null).runSequentially();

		//		StopWatchUtil.print();

		System.exit(0);
	}
}
