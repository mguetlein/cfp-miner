package org.kramerlab.cfpminer.weka.eval2;

import java.util.ArrayList;
import java.util.List;

import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.CDKDataset;
import org.mg.cdklib.data.DataLoader;
import org.mg.javalib.util.ListUtil;
import org.mg.wekalib.eval2.CV;
import org.mg.wekalib.eval2.CVEvaluator;
import org.mg.wekalib.eval2.job.FeatureProvider;
import org.mg.wekalib.eval2.job.JobOwner;
import org.mg.wekalib.eval2.model.FeatureModel;
import org.mg.wekalib.eval2.model.Model;
import org.mg.wekalib.eval2.model.NaiveBayesModel;
import org.mg.wekalib.eval2.model.RandomForestModel;
import org.mg.wekalib.eval2.persistance.DB;

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

		CFPFeatureProvider feat = new CFPFeatureProvider(1024, FeatureSelection.filt,
				CFPType.ecfp4);

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

	private static String getHost(String pw)
	{
		//return pw + "@134.93.175.97";
		return "127.0.0.1";
	}

	public static void clear(String pw) throws Exception
	{
		RedisClient redisClient = new RedisClient(
				RedisURI.create("redis://" + getHost(pw) + ":6379"));
		DB.init(new RedisResultProvider(redisClient.connect(new ByteArrayCodec())),
				new RedisBlocker(redisClient.connect()));

		DB.getBlocker().clear();
		System.out.println("cleared blocker!");
		DB.getResultProvider().clear();
		System.out.println("cleared result db!");

		System.exit(1);
	}

	public static void main(String[] args) throws Exception
	{
		//		StopWatchUtil.setUseCpuTime(false);
		//		DB.setResultProvider(new ResultProviderTime(new ResultProviderImpl()));
		//clear(args.length > 0 ? args[0] : "");
		//		RedisClient redisClient = new RedisClient(RedisURI.create("redis://" + getHost(args[0]) + ":6379"));
		//		DB.setBlocker(new BlockerTime(new RedisBlocker(redisClient.connect())));
		//		DB.setResultProvider(new ResultProviderTime(new RedisResultProvider(redisClient.connect(new ByteArrayCodec()))));

		cvEval().runSequentially();

		//		StopWatchUtil.print();

		System.exit(0);
	}
}
