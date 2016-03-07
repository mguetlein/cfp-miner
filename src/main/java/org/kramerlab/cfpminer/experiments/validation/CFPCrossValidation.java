package org.kramerlab.cfpminer.experiments.validation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.kramerlab.cfpminer.experiments.ModelBuildingRuntimes;
import org.kramerlab.cfpminer.weka.eval2.CDKDataSet;
import org.kramerlab.cfpminer.weka.eval2.CFPFeatureProvider;
import org.kramerlab.cfpminer.weka.eval2.LowNumFeaturesEvalCriterion;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.DataLoader;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.ListUtil;
import org.mg.javalib.util.SwingUtil;
import org.mg.wekalib.eval2.CV;
import org.mg.wekalib.eval2.MultiDatasetRunner;
import org.mg.wekalib.eval2.ValidatedModel;
import org.mg.wekalib.eval2.Validation;
import org.mg.wekalib.eval2.ValidationEval;
import org.mg.wekalib.eval2.data.DataSet;
import org.mg.wekalib.eval2.job.DataSetJobOwner;
import org.mg.wekalib.eval2.job.DefaultJobOwner;
import org.mg.wekalib.eval2.job.FeatureProvider;
import org.mg.wekalib.eval2.job.JobOwner;
import org.mg.wekalib.eval2.model.FeatureModel;
import org.mg.wekalib.eval2.model.Model;
import org.mg.wekalib.eval2.model.ModelProvider;
import org.mg.wekalib.eval2.model.NaiveBayesModel;
import org.mg.wekalib.eval2.model.RandomForestModel;
import org.mg.wekalib.eval2.persistance.DB;
import org.mg.wekalib.eval2.persistance.ResultProviderImpl;
import org.mg.wekalib.evaluation.PredictionUtil;
import org.mg.wekalib.evaluation.PredictionUtil.ClassificationMeasure;
import org.mg.wekalib.evaluation.PredictionUtilPlots;
import org.mg.wekalib.evaluation.Predictions;

/**
 * main class for applying cross-validation to cfp datasets 
 * 
 * contains two modes
 * - plain repeated cross-validation (e.g. for comparing different fp types)
 * - double/nested cross-validation
 *   * inner loop: model selection
 *   * outer loop: model predictivity assessment
 *   
 * - plain cross-validation is only model selection without model assessment
 * - to find out which model to build as final model, use plain-cv
 * - to find out how good this model is, use nested cv 
 * (i.e., nested cv does not contain info on which model is actually selected on entire data)
 * -> names for two modes: jobSelectModel() for plain cv, jobValidateModel() for nested cv
 * 
 * this is a per dataset thing, and run separated for each dataset (multi-dataset-runner)
 * 
 * structure for jobSelectModel() (= plain-cv)
 * 
 * multi-dataset-runner
 * - dataset
 * - validation-eval
 *   - num-repetitions
 *   - CV + settings 
 * 	 - list-of-models 
 * 
 * structure for jobValidateModel() (= nested-cv)
 * 
 * multi-dataset-runner
 * - dataset
 * - validation-eval
 *   - num-repetitions
 *   - CV + settings
 *   - validated-model
 *   	- validation-eval
 *        - num-repetitions
 *        - CV + settings
 *        - list-of-models
 */
public class CFPCrossValidation
{
	public int numCVFolds;
	public int numCVRepetitions;
	public boolean stratified;
	public List<CFPType> types;
	public List<FeatureSelection> featureSelections;
	public List<Integer> sizes;
	public List<Model> classifiers;
	public List<String> datasets;

	private static DataLoader loader = DataLoader.INSTANCE;
	private List<Model> featModels;
	private List<CDKDataSet> data;

	public static CFPCrossValidation paramOptimize()
	{
		CFPCrossValidation cv = new CFPCrossValidation();
		cv.numCVFolds = 10;
		cv.numCVRepetitions = 3;
		cv.stratified = true;
		cv.sizes = ArrayUtil.toList(new Integer[] { 1024, 2048, 4096, 8192 });
		cv.types = ListUtil.createList(CFPType.ecfp4);
		cv.featureSelections = ListUtil.createList(FeatureSelection.filt);
		cv.classifiers = ArrayUtil.toList(ModelProvider.ALL_MODELS_PARAM_OPTIMIZE);
		cv.datasets = ArrayUtil.toList(loader.allDatasets());
		return cv;
	}

	public static CFPCrossValidation compareCFPs()
	{
		CFPCrossValidation cv = new CFPCrossValidation();
		cv.numCVFolds = 10;
		cv.numCVRepetitions = 3;
		cv.stratified = true;
		cv.sizes = ArrayUtil.toList(new Integer[] { 1024, 2048, 4096, 8192 });
		cv.types = ListUtil.createList(CFPType.ecfp4);
		cv.featureSelections = ArrayUtil.toList(FeatureSelection.values());
		cv.classifiers = ArrayUtil.toList(ModelProvider.ALL_MODELS_PARAM_DEFAULT);
		cv.datasets = ArrayUtil.toList(loader.allDatasets());
		return cv;
	}

	public static CFPCrossValidation ridgeEval()
	{
		CFPCrossValidation cv = new CFPCrossValidation();
		cv.numCVFolds = 10;
		cv.numCVRepetitions = 1;
		cv.stratified = true;
		cv.sizes = ArrayUtil.toList(new Integer[] { 1024, 2048, 4096, 8192 });
		cv.types = ListUtil.createList(CFPType.ecfp4);
		cv.featureSelections = ArrayUtil.toList(FeatureSelection.values());
		cv.classifiers = ArrayUtil
				.toList(ArrayUtil.cast(Model.class, ModelProvider.SVMS_RIDGE_EVAL));
		cv.datasets = ArrayUtil.toList(loader.reducedDatasets());
		return cv;
	}

	private CFPCrossValidation()
	{
	}

	public void init()
	{
		List<CFPFeatureProvider> feats = new ArrayList<CFPFeatureProvider>();
		for (CFPType type : types)
		{
			for (FeatureSelection feat : featureSelections)
			{
				if (feat == FeatureSelection.none)
					feats.add(new CFPFeatureProvider(0, feat, type));
				else
					for (Integer size : sizes)
						feats.add(new CFPFeatureProvider(size, feat, type));
			}
		}

		featModels = new ArrayList<>();
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

		data = new ArrayList<>();
		for (String datasetName : datasets)
			data.add(new CDKDataSet(datasetName, loader.getDataset(datasetName)));
	}

	private ValidationEval innerValidation()//boolean performCV)
	{
		ValidationEval innerValidation = new ValidationEval();
		innerValidation.setModels(ListUtil.toArray(featModels));
		Validation val;
		//		if (performCV)
		//		{
		innerValidation.setRepetitions(numCVRepetitions);
		val = new CV();
		((CV) val).setNumFolds(numCVFolds);
		//		}
		//		else
		//		{
		//			innerValidation.setRepetitions(numSplitRepetitions);
		//			val = new Holdout();
		//			((Holdout) val).setSplitRatio(splitRatio);
		//		}
		val.setStratified(stratified);
		innerValidation.setValidation(val);
		//		innerCV.setEvalCriterion(
		//				new CVEvaluator.DefaultEvalCriterion(PredictionUtil.ClassificationMeasure.AUPRC));
		innerValidation.setEvalCriterion(
				new LowNumFeaturesEvalCriterion(PredictionUtil.ClassificationMeasure.AUPRC));
		return innerValidation;
	}

	public MultiDatasetRunner<String> jobSelectModel() throws Exception
	{
		init();

		MultiDatasetRunner<String> d = new MultiDatasetRunner<>();
		//		ValidationEval innerValidations[] = new ValidationEval[data.size()];
		//		for (int i = 0; i < innerValidations.length; i++)
		//		{
		//			boolean vs = loader.isVirtualScreeningDataset(data.get(i).getName());
		//			innerValidations[i] = innerValidation(!vs);
		//		}
		d.setSameJobForAllDatasets(innerValidation());
		d.setDataSets(ListUtil.toArray(data));

		return d;
	}

	//	public static Model selectModelClassifier(String dataset) throws Exception
	//	{
	//		return selectModel(dataset).getModel();
	//	}
	//
	//	public static CFPFeatureProvider selectModelFeatures(String dataset) throws Exception
	//	{
	//		return (CFPFeatureProvider) selectModel(dataset).getFeatureProvider();
	//	}

	public class ValidationToMeasures
			extends DefaultJobOwner<HashMap<Integer, HashMap<ClassificationMeasure, Double>>>
	{
		Validation val;
		boolean perFold;
		ClassificationMeasure measures[];

		public ValidationToMeasures(Validation val, boolean perFold,
				ClassificationMeasure[] measures)
		{
			this.val = val;
			this.perFold = perFold;
			this.measures = measures;
		}

		@Override
		public String getName()
		{
			return this.getClass().getSimpleName();
		}

		@Override
		public JobOwner<HashMap<Integer, HashMap<ClassificationMeasure, Double>>> cloneJob()
		{
			throw new NotImplementedException("");
		}

		@Override
		public Runnable nextJob() throws Exception
		{
			return new Runnable()
			{

				@Override
				public void run()
				{
					Predictions predictions = val.getResult();
					List<Predictions> pList;
					if (perFold)
						pList = PredictionUtil.perFold(predictions);
					else
						pList = ListUtil.createList(predictions);

					HashMap<Integer, HashMap<ClassificationMeasure, Double>> map = new HashMap<>();

					for (Predictions p : pList)
						map.put(perFold ? p.fold[0] : 0,
								new HashMap<ClassificationMeasure, Double>());

					int pos = val.getDataSet().getPositiveClass();

					for (ClassificationMeasure m : measures)
						for (Predictions p : pList)
							map.get(perFold ? p.fold[0] : 0).put(m,
									PredictionUtil.getClassificationMeasure(p, m, pos));

					setResult(map);
				}
			};
		}

		@Override
		public String getKeyContent()
		{
			return getKeyContent(val, perFold, measures);
		}

		@Override
		public String getKeyPrefix()
		{
			return "ValidationToMeasures" + File.separator + val.getKeyPrefix();
		}

	}

	public ResultSet validateModelResults(ClassificationMeasure measures[]) throws Exception
	{
		ResultSet res = validateModelResultsPerRepetition(measures);
		return res.join(new String[] { "Dataset" }, new String[] { "Fold", "Seed" }, null);
	}

	public ResultSet validateModelResultsPerRepetition(ClassificationMeasure measures[])
			throws Exception
	{
		ResultSet res = new ResultSet();

		MultiDatasetRunner<String> multiDataset = jobValidateModel();
		multiDataset.runSequentially();

		for (DataSetJobOwner<String> validationEval : multiDataset.jobs())
		{
			ValidationEval valEval = (ValidationEval) validationEval;
			DataSet d = valEval.getDataset();

			List<Predictions> pred = valEval.getPredictions();

			int seed = 0;
			for (Predictions predictions : pred)
			{
				for (Predictions p : PredictionUtil.perFold(predictions))
				{
					int rIdx = res.addResult();
					res.setResultValue(rIdx, "Dataset", d.getName());
					res.setResultValue(rIdx, "Fold", p.fold[0]);
					res.setResultValue(rIdx, "Seed", seed);

					for (PredictionUtil.ClassificationMeasure m : measures)
						res.setResultValue(rIdx, m.toString(), PredictionUtil
								.getClassificationMeasure(p, m, d.getPositiveClass()));
				}
				seed++;
			}
		}

		return res;
	}

	public HashMap<String, FeatureModel> selectBestModel() throws Exception
	{
		HashMap<String, FeatureModel> bestModels = new LinkedHashMap<>();

		MultiDatasetRunner<String> multiDataset = jobSelectModel();
		multiDataset.runSequentially();

		for (DataSetJobOwner<String> validationEval : multiDataset.jobs())
		{
			ValidationEval valEval = (ValidationEval) validationEval;
			bestModels.put(valEval.getDataset().getName(), (FeatureModel) valEval.getBestModel());
		}

		return bestModels;
	}

	public ResultSet selectModelResults(ClassificationMeasure measures[], boolean addRuntime)
			throws Exception
	{
		ResultSet res = selectModelResultsPerRepetition(measures, addRuntime, false);
		return res.join(new String[] { "Dataset", "Algorithm", "AlgParams", "FeatureSelection",
				"HashfoldSize", "Type" }, new String[] { "RandomSeed" }, null);
	}

	public ResultSet selectModelResultsPerRepetition(ClassificationMeasure measures[],
			boolean addRuntime, boolean perFold) throws Exception
	{
		ResultSet res = new ResultSet();

		MultiDatasetRunner<String> multiDataset = jobSelectModel();
		multiDataset.runSequentially();

		for (DataSetJobOwner<String> validationEval : multiDataset.jobs())
		{
			ValidationEval valEval = (ValidationEval) validationEval;
			DataSet d = valEval.getDataset();
			System.out.println(d.getName());

			for (Validation val : valEval.getValidations())
			{
				FeatureModel model = (FeatureModel) val.getModel();
				Model classifierModel = (Model) model.getModel().cloneJob();
				CFPFeatureProvider featProv = (CFPFeatureProvider) model.getFeatureProvider();

				double runtime = 0;
				if (addRuntime)
				{
					classifierModel.setTrainingDataset(null);
					classifierModel.setTestDataset(null);
					runtime = ModelBuildingRuntimes.getRuntime(d.getName(), featProv.getType(),
							featProv.getFeatureSelection(), featProv.getHashfoldSize(),
							classifierModel, true);
				}

				int inspectSeed = -1;
				int inspectFoldIdx = -1;
				if (val.getRandomSeed() == inspectSeed)
				{
					System.out.println(featProv.getFeatureSelection());

					Predictions p = (Predictions) val.getResult();

					Predictions pFold = PredictionUtil.perFold(p).get(inspectFoldIdx);
					//					for (int i = 0; i < 2; i++)
					//					{
					System.out.println("AUC " + PredictionUtil.AUC(pFold));
					System.out
							.println("AUPRC " + PredictionUtil.AUPRC(pFold, d.getPositiveClass()));
					System.out.println("BEDROC20 " + PredictionUtil.getClassificationMeasure(pFold,
							ClassificationMeasure.BEDROC20, d.getPositiveClass()));
					System.out.println("ER5 " + PredictionUtil.getClassificationMeasure(pFold,
							ClassificationMeasure.EnrichmentFactor5, d.getPositiveClass()));
					PredictionUtil.printPredictionsWithWEKAProbability(pFold, d.getPositiveClass());
					//						double probs[] = PredictionUtil.getProbabilitiesToBePositive(pFold,
					//								d.getPositiveClass());
					//						for (int j = 0; j < pFold.actual.length; j++)
					//							if (probs[j] >= 0.13)
					//								pFold.predicted[j] = d.getPositiveClass();
					//					}

					//					for (double a : new double[] { 0.001, 0.01, 0.02, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5,
					//							1.0, 10.0, 20.0, 50.0 })
					//					{
					//						double alpha = a;
					//						System.out
					//								.println(
					//										alpha + " "
					//												+ StringUtil.formatDouble(PredictionUtil
					//														.toEnrichmentAssessment(pFold,
					//																d.getPositiveClass())
					//														.bedroc(alpha, true), 4));
					//					}
					System.exit(1);

					SwingUtil.showInFrame(
							PredictionUtilPlots.getROCPlot(d.getPositiveClass(), pFold),
							"AUC " + featProv.getFeatureSelection().toString() + " "
									+ PredictionUtil.AUC(pFold),
							false);
					SwingUtil
							.showInFrame(PredictionUtilPlots.getPRPlot(d.getPositiveClass(), pFold),
									"AUPRC " + featProv.getFeatureSelection().toString() + " "
											+ PredictionUtil.AUPRC(pFold, d.getPositiveClass()),
							false);
				}

				ValidationToMeasures valToMeasures = new ValidationToMeasures(val, perFold,
						ClassificationMeasure.values());
				valToMeasures.runSequentially(true);
				HashMap<Integer, HashMap<ClassificationMeasure, Double>> vRes = valToMeasures
						.getResult();
				for (Integer fold : vRes.keySet())
				{
					int rIdx = res.addResult();
					res.setResultValue(rIdx, "Dataset", d.getName().replace("_", " "));
					res.setResultValue(rIdx, "RandomSeed", val.getRandomSeed());
					res.setResultValue(rIdx, "Algorithm", classifierModel.getAlgorithmShortName());
					res.setResultValue(rIdx, "AlgParams", classifierModel.getName());
					res.setResultValue(rIdx, "FeatureSelection",
							featProv.getFeatureSelection().toNiceString());
					res.setResultValue(rIdx, "HashfoldSize", featProv.getHashfoldSize());
					res.setResultValue(rIdx, "Type", featProv.getType());

					if (perFold)
						res.setResultValue(rIdx, "Fold", fold);
					for (ClassificationMeasure m : vRes.get(fold).keySet())
						res.setResultValue(rIdx, m.toString(), vRes.get(fold).get(m));

					if (addRuntime)
						res.setResultValue(rIdx, PaperValidationResults.RUNTIME, runtime);
				}

				//				Predictions predictions = val.getResult();
				//				List<Predictions> pList;
				//				if (perFold)
				//					pList = PredictionUtil.perFold(predictions);
				//				else
				//					pList = ListUtil.createList(predictions);

				//				for (ClassificationMeasure m : measures)
				//				{
				//					for (Predictions p : pList)
				//					{
				//						if (perFold)
				//							res.setResultValue(rIdx, "Fold", p.fold[0]);
				//						res.setResultValue(rIdx, m.toString(), PredictionUtil
				//								.getClassificationMeasure(p, m, d.getPositiveClass()));
				//					}
				//				}

			}
		}

		return res;
	}

	public MultiDatasetRunner<String> jobValidateModel() throws Exception
	{
		init();

		MultiDatasetRunner<String> d = new MultiDatasetRunner<>();

		//		ValidationEval outerValidations[] = new ValidationEval[data.size()];
		//		for (int i = 0; i < outerValidations.length; i++)
		//		{
		//			boolean vs = loader.isVirtualScreeningDataset(data.get(i).getName());

		//			ValidationEval innerVal = innerValidation(!vs);
		ValidatedModel vModel = new ValidatedModel();
		vModel.setValidationEvaluator(innerValidation());
		ValidationEval outerVal = new ValidationEval();
		outerVal.setModels(vModel);
		Validation val;
		//			if (!vs)
		//			{
		outerVal.setRepetitions(numCVRepetitions);
		val = new CV();
		((CV) val).setNumFolds(numCVFolds);
		//			}
		//			else
		//			{
		//				outerVal.setRepetitions(numSplitRepetitions);
		//				val = new Holdout();
		//				((Holdout) val).setSplitRatio(splitRatio);
		//			}
		val.setStratified(stratified);
		outerVal.setValidation(val);

		//			outerValidations[i] = outerVal;
		//		}
		d.setSameJobForAllDatasets(outerVal);

		d.setDataSets(ListUtil.toArray(data));

		return d;
	}

	public static void debug() throws Exception
	{
		DB.init(new ResultProviderImpl("/home/martin/tmp/jobs/store", "/home/martin/tmp/jobs/tmp"),
				null);

		//runModelBuildJob("1024,CPDBAS_Dog_Primates,");

		//run(true, new String[] { "CPDBAS_Rat" }, null);

		//		CFPNestedCV cv = new CFPNestedCV();
		//		cv.datasets.remove("AMES");
		//		cv.jobValidateModel().runSequentially();

		//		printSelectedAlgorithms();

		{
			//			CFPNestedCV cv = new CFPNestedCV();
			//			//cv.datasets.remove("AMES");
			//			cv.datasets = ListUtil.createList("AMES");
			//			cv.jobSelectModel().runSequentially();
			//System.out.println(selectModelOverview().toNiceString());
			//			//			cv.sizes = ListUtil.createList(8192);
			//			//			cv.datasets = ListUtil.createList("ChEMBL_51");
			//			cv.jobSelectModel().runSequentially();
			//			for (String dataset : cv.datasets)
			//			{
			//				System.out.println(dataset);
			//				System.out.println(cv.selectModelClassifier(dataset).getName());
			//				System.out.println(cv.selectModelFeatures(dataset).getName());
			//			}
		}

		{
			//String dataset = "CPDBAS_Dog_Primates";
			//String dataset = "DUD_vegfr2";
			String dataset = "MUV_733";
			//String dataset = "ChEMBL_61";
			//String dataset = "AMES";

			CFPCrossValidation cv = new CFPCrossValidation();

			//			cv.numFolds = 4;
			//			cv.numRepetitions = 5;
			cv.sizes = ListUtil.createList(0);
			cv.featureSelections = ListUtil.createList(FeatureSelection.none);
			cv.datasets = ListUtil.createList(dataset);
			cv.classifiers = ListUtil.create(Model.class, new NaiveBayesModel(),
					new RandomForestModel());

			//			cv.numSplitRepetitions = 5;
			//			cv.splitRatio = 0.9;

			cv.jobSelectModel().runSequentially();

			//			ValidationEval val = (ValidationEval) cv.jobSelectModel().jobs().get(0);
			//			System.out.println(val.getBestModel());

			//			selectModelOverview();
			//			System.out.println(cv.selectModelClassifier(dataset).getName());
			//			System.out.println(cv.selectModelFeatures(dataset).getName());
			//
			//cv.jobValidateModel().runSequentially();
			//			System.out.println(cv.validateModel(dataset).toNiceString());
			//			cv.plotValidationResult(dataset, null);
			//		cv.jobValidateModel().runSequentially();
		}
	}

	public static void main(String[] args) throws Exception
	{
		//		Printer.PRINT_TO_SYSTEM_OUT = true;
		//args = "-d DUD_vegfr2 -s 1024 -c 0,1".split(" ");
		debug();
	}
}