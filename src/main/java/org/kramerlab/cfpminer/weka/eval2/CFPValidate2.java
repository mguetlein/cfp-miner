package org.kramerlab.cfpminer.weka.eval2;

import java.util.ArrayList;
import java.util.List;

import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.CDKDataset;
import org.mg.cdklib.data.DataLoader;
import org.mg.javalib.util.ListUtil;
import org.mg.wekalib.eval2.CVEvalModel;
import org.mg.wekalib.eval2.CVEvaluator;
import org.mg.wekalib.eval2.FeatureModel;
import org.mg.wekalib.eval2.FeatureProvider;
import org.mg.wekalib.eval2.JobOwner;
import org.mg.wekalib.eval2.Model;
import org.mg.wekalib.eval2.NaiveBayesModel;
import org.mg.wekalib.eval2.RandomForestModel;

public class CFPValidate2
{
	public static void demo() throws Exception
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
		//cv.setDataSet(data);
		cv.setModels(ListUtil.toArray(featModels));
		cv.setNumFolds(10);
		cv.setRepetitions(1);

		CVEvalModel cvm = new CVEvalModel();
		cvm.setCvEvaluator(cv);
		cvm.setTrainingDataset(data);
		cvm.setTestDataset(data);

		JobOwner<?> job = cvm;
		if (!job.isDone())
		{
			Runnable r = job.nextJob();
			while (r != null)
			{
				r.run();
				if (!job.isDone())
					r = job.nextJob();
				else
					r = null;
			}
		}
		else
			System.out.println("done " + job.hashCode());

		System.exit(0);
	}

	public static void nestedCV() throws Exception
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

		CVEvaluator innerCV = new CVEvaluator();
		//cv.setDataSet(data);
		innerCV.setModels(ListUtil.toArray(featModels));
		innerCV.setNumFolds(10);
		innerCV.setRepetitions(1);

		CVEvalModel cvm = new CVEvalModel();
		cvm.setCvEvaluator(innerCV);
		//		cvm.setTrainingDataset(data);
		//		cvm.setTestDataset(data);

		CVEvaluator outerCV = new CVEvaluator();
		outerCV.setDataSet(data);
		outerCV.setModels(cvm);
		outerCV.setNumFolds(10);
		outerCV.setRepetitions(1);

		JobOwner<?> job = outerCV;
		if (!job.isDone())
		{
			Runnable r = job.nextJob();
			while (r != null)
			{
				r.run();
				if (!job.isDone())
					r = job.nextJob();
				else
					r = null;
			}
		}
		else
			System.out.println("done " + job.hashCode());

		System.exit(0);
	}

	public static void main(String[] args) throws Exception
	{
		nestedCV();
	}
}
