package org.kramerlab.cfpminer.weka.eval2;

import java.util.ArrayList;
import java.util.List;

import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.CDKDataset;
import org.mg.cdklib.data.DataLoader;
import org.mg.javalib.util.ListUtil;
import org.mg.wekalib.eval2.CV;
import org.mg.wekalib.eval2.CVEvalModel;
import org.mg.wekalib.eval2.CVEvaluator;
import org.mg.wekalib.eval2.MultiDatasetRunner;
import org.mg.wekalib.eval2.job.FeatureProvider;
import org.mg.wekalib.eval2.job.JobOwner;
import org.mg.wekalib.eval2.model.FeatureModel;
import org.mg.wekalib.eval2.model.Model;
import org.mg.wekalib.eval2.model.NaiveBayesModel;
import org.mg.wekalib.eval2.model.RandomForestModel;

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

	public static JobOwner<?> nestedCV() throws Exception
	{
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

		CVEvaluator innerCV = new CVEvaluator();
		innerCV.setModels(ListUtil.toArray(featModels));
		innerCV.setNumFolds(10);
		innerCV.setRepetitions(3);

		CVEvalModel cvm = new CVEvalModel();
		cvm.setCvEvaluator(innerCV);

		CVEvaluator outerCV = new CVEvaluator();
		outerCV.setModels(cvm);
		outerCV.setNumFolds(10);
		outerCV.setRepetitions(3);

		List<CDKDataSet> data = new ArrayList<>();
		for (String datasetName : new String[] { "CPDBAS_Mutagenicity", "DUD_vegfr2" })
		{
			CDKDataset dataset = new DataLoader("data").getDataset(datasetName);
			data.add(new CDKDataSet(datasetName, dataset));
		}
		//outerCV.setDataSet(data.get(0));

		MultiDatasetRunner<String> d = new MultiDatasetRunner<>();
		d.setJob(outerCV);
		d.setDataSets(ListUtil.toArray(data));

		return d;
	}

	public static void main(String[] args) throws Exception
	{
		nestedCV().runSequentially();
	}
}
