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
import org.mg.cdklib.data.DataProvider;
import org.mg.cdklib.data.DataProvider.DataID;
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
 * - if different algorithms are tested, than plain cross-validation is only model selection without model assessment
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
	public List<DataID> datasets;

	private List<Model> featModels;
	private List<CDKDataSet> data;

	public static CFPCrossValidation randomForest()
	{
		CFPCrossValidation cv = new CFPCrossValidation();
		cv.numCVFolds = 10;
		cv.numCVRepetitions = 3;
		cv.stratified = true;
		cv.sizes = ArrayUtil.toList(new Integer[] { 2048 });
		cv.types = ListUtil.createList(CFPType.ecfp4);
		cv.featureSelections = ListUtil.createList(FeatureSelection.filt);
		cv.classifiers = ListUtil.create(Model.class, ModelProvider.RANDOM_FOREST);
		cv.datasets = ArrayUtil.toList(DataProvider.cfpDatasetsSorted());
		return cv;
	}

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
		cv.datasets = ArrayUtil.toList(DataProvider.cfpDatasetsSorted());
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
		cv.datasets = ArrayUtil.toList(DataProvider.cfpDatasetsSorted());
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
		cv.datasets = ArrayUtil.toList(DataProvider.cfpDatasetsSorted());
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
		for (DataID d : datasets)
			data.add(new CDKDataSet(d.toString(), DataProvider.getDataset(d)));
	}

	private ValidationEval innerValidation()
	{
		ValidationEval innerValidation = new ValidationEval();
		innerValidation.setModels(ListUtil.toArray(featModels));
		Validation val;
		innerValidation.setRepetitions(numCVRepetitions);
		val = new CV();
		((CV) val).setNumFolds(numCVFolds);
		val.setStratified(stratified);
		innerValidation.setValidation(val);
		innerValidation.setEvalCriterion(
				new LowNumFeaturesEvalCriterion(PredictionUtil.ClassificationMeasure.AUPRC));
		return innerValidation;
	}

	public MultiDatasetRunner<String> jobSelectModel() throws Exception
	{
		init();
		MultiDatasetRunner<String> d = new MultiDatasetRunner<>();
		d.setSameJobForAllDatasets(innerValidation());
		d.setDataSets(ListUtil.toArray(data));
		return d;
	}

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
					runtime = ModelBuildingRuntimes.getRuntime(DataID.valueOf(d.getName()),
							featProv.getType(), featProv.getFeatureSelection(),
							featProv.getHashfoldSize(), classifierModel, true);
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
			}
		}
		return res;
	}

	public MultiDatasetRunner<String> jobValidateModel() throws Exception
	{
		init();

		Model selectedModel;
		if (featModels.size() > 1)
		{
			// default case with inner validation
			selectedModel = new ValidatedModel();
			((ValidatedModel) selectedModel).setValidationEvaluator(innerValidation());
		}
		else
		{
			// only 1 model, no need for inner validation
			selectedModel = featModels.get(0);
		}

		ValidationEval outerVal = new ValidationEval();
		outerVal.setModels(selectedModel);
		outerVal.setRepetitions(numCVRepetitions);
		Validation val = new CV();
		((CV) val).setNumFolds(numCVFolds);
		val.setStratified(stratified);
		outerVal.setValidation(val);

		MultiDatasetRunner<String> d = new MultiDatasetRunner<>();
		d.setSameJobForAllDatasets(outerVal);
		d.setDataSets(ListUtil.toArray(data));
		return d;
	}

}