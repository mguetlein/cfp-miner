package org.kramerlab.cfpminer.weka.eval2;

public class CFPNestedValidation
{
	//	int numCVFolds;
	//	int numCVRepetitions;
	//	double splitRatio;
	//	int numSplitRepetitions;
	//	boolean stratified;
	//	List<CFPType> types;
	//	List<FeatureSelection> featureSelections;
	//	List<Integer> sizes;
	//	List<Model> classifiers;
	//	List<String> datasets;
	//
	//	private static DataLoader loader = new DataLoader("data");
	//
	//	private List<Model> featModels;
	//	private List<CDKDataSet> data;
	//
	//	public CFPNestedValidation()
	//	{
	//		numCVFolds = 10;
	//		numCVRepetitions = 3;
	//		splitRatio = 0.2;
	//		numSplitRepetitions = 30;
	//		stratified = true;
	//		sizes = ArrayUtil.toList(new Integer[] { 1024, 2048, 4096, 8192 });
	//		types = ListUtil.createList(CFPType.ecfp4);
	//		featureSelections = ListUtil.createList(FeatureSelection.filt);
	//
	//		classifiers = new ArrayList<Model>();
	//		classifiers.add(new NaiveBayesModel());
	//		classifiers.add(new RandomForestModel());
	//		Double cs[] = new Double[] { 1.0, 10.0, 100.0 };
	//		for (Double g : new Double[] { 0.001, 0.01, 0.1 })
	//		{
	//			for (Double c : cs)
	//			{
	//				if (c == 1.0 && g == 0.001) // does not work well
	//					continue;
	//				SupportVectorMachineModel svm = new SupportVectorMachineModel();
	//				svm.setC(c);
	//				svm.setKernel(new RBFKernel());
	//				svm.setGamma(g);
	//				classifiers.add(svm);
	//			}
	//		}
	//		for (Double e : new Double[] { 1.0 }) // exponent optimizing not needed , 2.0, 3.0
	//		{
	//			for (Double c : cs)
	//			{
	//				SupportVectorMachineModel svm = new SupportVectorMachineModel();
	//				svm.setC(c);
	//				svm.setKernel(new PolyKernel());
	//				svm.setExp(e);
	//				classifiers.add(svm);
	//			}
	//		}
	//
	//		datasets = ArrayUtil.toList(loader.allDatasets());
	//	}
	//
	//	public void init()
	//	{
	//		List<CFPFeatureProvider> feats = new ArrayList<CFPFeatureProvider>();
	//		for (Integer size : sizes)
	//			for (CFPType type : types)
	//				for (FeatureSelection feat : featureSelections)
	//					feats.add(new CFPFeatureProvider(size, feat, type));
	//
	//		featModels = new ArrayList<>();
	//		for (Model model : classifiers)
	//		{
	//			for (FeatureProvider feat : feats)
	//			{
	//				FeatureModel fm = new FeatureModel();
	//				fm.setFeatureProvider(feat);
	//				fm.setModel(model);
	//				featModels.add(fm);
	//			}
	//		}
	//
	//		data = new ArrayList<>();
	//		for (String datasetName : datasets)
	//			data.add(new CDKDataSet(datasetName, loader.getDataset(datasetName)));
	//	}
	//
	//	private ValidationEval innerValidation(boolean performCV)
	//	{
	//		ValidationEval innerValidation = new ValidationEval();
	//		innerValidation.setModels(ListUtil.toArray(featModels));
	//		Validation val;
	//		if (performCV)
	//		{
	//			innerValidation.setRepetitions(numCVRepetitions);
	//			val = new CV();
	//			((CV) val).setNumFolds(numCVFolds);
	//		}
	//		else
	//		{
	//			innerValidation.setRepetitions(numSplitRepetitions);
	//			val = new Holdout();
	//			((Holdout) val).setSplitRatio(splitRatio);
	//		}
	//		val.setStratified(stratified);
	//		innerValidation.setValidation(val);
	//		//		innerCV.setEvalCriterion(
	//		//				new CVEvaluator.DefaultEvalCriterion(PredictionUtil.ClassificationMeasure.AUPRC));
	//		innerValidation.setEvalCriterion(
	//				new LowNumFeaturesEvalCriterion(PredictionUtil.ClassificationMeasure.AUPRC));
	//		return innerValidation;
	//	}
	//
	//	public MultiDatasetRunner<String> jobSelectModel() throws Exception
	//	{
	//		init();
	//
	//		MultiDatasetRunner<String> d = new MultiDatasetRunner<>();
	//		ValidationEval innerValidations[] = new ValidationEval[data.size()];
	//		for (int i = 0; i < innerValidations.length; i++)
	//		{
	//			boolean vs = loader.isVirtualScreeningDataset(data.get(i).getName());
	//			innerValidations[i] = innerValidation(!vs);
	//		}
	//		d.setJobs(innerValidations);
	//		d.setDataSets(ListUtil.toArray(data));
	//
	//		return d;
	//	}
	//
	//	public static FeatureModel selectModel(String dataset) throws Exception
	//	{
	//		CFPNestedValidation cv = new CFPNestedValidation();
	//		cv.datasets = ListUtil.createList(dataset);
	//		MultiDatasetRunner<String> d = cv.jobSelectModel();
	//		if (!d.isDone())
	//			d.runSequentially();
	//		if (d.jobs().size() != 1)
	//			throw new IllegalStateException();
	//		ValidationEval cvEval = (ValidationEval) d.jobs().get(0);
	//		return (FeatureModel) cvEval.getBestModel();
	//	}
	//
	//	//	public static Model selectModelClassifier(String dataset) throws Exception
	//	//	{
	//	//		return selectModel(dataset).getModel();
	//	//	}
	//	//
	//	//	public static CFPFeatureProvider selectModelFeatures(String dataset) throws Exception
	//	//	{
	//	//		return (CFPFeatureProvider) selectModel(dataset).getFeatureProvider();
	//	//	}
	//
	//	public static ResultSet selectModelOverview() throws Exception
	//	{
	//		ResultSet rs = new ResultSet();
	//		for (String dataset : ArrayUtil.toList(loader.allDatasetsSorted()))
	//		{
	//			if (!dataset.equals("ChEMBL_61"))
	//				continue;
	//			//			if (!dataset.equals("CPDBAS_Mutagenicity"))
	//			//				continue;
	//			System.out.println(dataset);
	//			int idx = rs.addResult();
	//			rs.setResultValue(idx, "Dataset", dataset.replaceAll("_", " "));
	//			FeatureModel featureModel = selectModel(dataset);
	//			Model m = featureModel.getModel();
	//			rs.setResultValue(idx, "Alg", m.getAlgorithmShortName());
	//			rs.setResultValue(idx, "Params", m.getAlgorithmParamsNice());
	//			CFPFeatureProvider f = (CFPFeatureProvider) featureModel.getFeatureProvider();
	//			rs.setResultValue(idx, "#Frags", f.getHashfoldSize());
	//
	//			ResultSet rsV = validateModel(dataset);
	//			rsV = rsV.join("Dataset");
	//			if (rsV.getNumResults() != 1)
	//				throw new IllegalStateException();
	//			for (PredictionUtil.ClassificationMeasure measure : PredictionUtil.ClassificationMeasure
	//					.values())
	//				rs.setResultValue(idx, measure.shortName(),
	//						rsV.getResultValue(0, measure.toString()));
	//		}
	//
	//		System.out.println(rs.getResultValues("Alg"));
	//		System.out.println(rs.getResultValues("#Frags"));
	//
	//		return rs;
	//	}
	//
	//	public MultiDatasetRunner<String> jobValidateModel() throws Exception
	//	{
	//		init();
	//
	//		MultiDatasetRunner<String> d = new MultiDatasetRunner<>();
	//
	//		ValidationEval outerValidations[] = new ValidationEval[data.size()];
	//		for (int i = 0; i < outerValidations.length; i++)
	//		{
	//			boolean vs = loader.isVirtualScreeningDataset(data.get(i).getName());
	//
	//			ValidationEval innerVal = innerValidation(!vs);
	//			ValidatedModel vModel = new ValidatedModel();
	//			vModel.setValidationEvaluator(innerVal);
	//			ValidationEval outerVal = new ValidationEval();
	//			outerVal.setModels(vModel);
	//			Validation val;
	//			if (!vs)
	//			{
	//				outerVal.setRepetitions(numCVRepetitions);
	//				val = new CV();
	//				((CV) val).setNumFolds(numCVFolds);
	//			}
	//			else
	//			{
	//				outerVal.setRepetitions(numSplitRepetitions);
	//				val = new Holdout();
	//				((Holdout) val).setSplitRatio(splitRatio);
	//			}
	//			val.setStratified(stratified);
	//			outerVal.setValidation(val);
	//
	//			outerValidations[i] = outerVal;
	//		}
	//		d.setJobs(outerValidations);
	//
	//		d.setDataSets(ListUtil.toArray(data));
	//
	//		return d;
	//	}
	//
	//	public static ResultSet validateModel(String dataset) throws Exception
	//	{
	//		CFPNestedValidation cv = new CFPNestedValidation();
	//		cv.datasets = ListUtil.createList(dataset);
	//		MultiDatasetRunner<String> d = cv.jobValidateModel();
	//		if (!d.isDone())
	//			d.runSequentially();
	//		if (d.jobs().size() != 1)
	//			throw new IllegalStateException();
	//		ValidationEval cvEval = (ValidationEval) d.jobs().get(0);
	//		List<Predictions> pred = cvEval.getPredictions();
	//
	//		ResultSet rs = new ResultSet();
	//		int seed = 0;
	//		for (Predictions predictions : pred)
	//		{
	//			for (Predictions p : PredictionUtil.perFold(predictions))
	//			{
	//				int rIdx = rs.addResult();
	//				rs.setResultValue(rIdx, "Dataset", dataset);
	//				rs.setResultValue(rIdx, "Fold", p.fold[0]);
	//				rs.setResultValue(rIdx, "Seed", seed);
	//				for (PredictionUtil.ClassificationMeasure m : PredictionUtil.ClassificationMeasure
	//						.values())
	//					rs.setResultValue(rIdx, m.toString(), PredictionUtil.getClassificationMeasure(p,
	//							m, cvEval.getDataset().getPositiveClass()));
	//			}
	//			seed++;
	//		}
	//		return rs;
	//	}
	//
	//	public static void plotValidationResult(String dataset, String pngFile) throws Exception
	//	{
	//		plotValidationResult(validateModel(dataset), pngFile);
	//	}
	//
	//	public static void plotValidationResult(ResultSet rs, String pngFile) throws Exception
	//	{
	//		ResultSetBoxPlot plot = new ResultSetBoxPlot(rs, null, null, null, ArrayUtil
	//				.toList(ArrayUtil.toStringArray(PredictionUtil.ClassificationMeasure.values())));
	//		plot.setHideMean(true);
	//		plot.setPrintMeanAndStdev(true);
	//		plot.printNumResultsPerPlot(false);
	//		Dimension dim = new Dimension(450, 200);
	//		plot.setFontSize(12);
	//
	//		if (pngFile != null)
	//			plot.ToPNGFile(pngFile, dim);
	//		else
	//		{
	//			ChartPanel p = plot.getChart();
	//			p.setPreferredSize(dim);
	//			SwingUtil.showInFrame(p);
	//		}
	//	}
	//
	//	public static void run(boolean validate, String[] datasets, Integer sizes[],
	//			Integer classifiers[]) throws Exception
	//	{
	//		CFPNestedValidation cv = new CFPNestedValidation();
	//		if (sizes != null)
	//			cv.sizes = ArrayUtil.toList(sizes);
	//		if (datasets != null)
	//			cv.datasets = ArrayUtil.toList(datasets);
	//		if (classifiers != null)
	//		{
	//			List<Model> selectedClassifiers = new ArrayList<>();
	//			for (Integer c : classifiers)
	//				selectedClassifiers.add(cv.classifiers.get(c));
	//			cv.classifiers = selectedClassifiers;
	//		}
	//		if (validate)
	//			cv.jobValidateModel().runSequentially();
	//		else
	//			cv.jobSelectModel().runSequentially();
	//	}
	//
	//	public static void printSelectedAlgorithms() throws Exception
	//	{
	//		ResultSet rs = selectModelOverview();
	//		rs.sortResults("Dataset", DataLoader.CFPDataComparator);
	//
	//		rs.setNumDecimalPlaces(3);
	//		System.out.println(rs.toNiceString());
	//		String f = System.getProperty("user.home")
	//				+ "/documents/ecfps/latex/results/table_nested.tex";
	//		System.err.println("printed latex-table to " + f);
	//		rs.setNicePropery("#Frags", "\\#Frags");
	//		FileUtil.writeStringToFile(f, rs.toLatexTable());
	//	}
	//
	//	public static void debug() throws Exception
	//	{
	//		DB.init(new ResultProviderImpl("/home/martin/tmp/jobs/store", "/home/martin/tmp/jobs/tmp"),
	//				null);
	//
	//		//runModelBuildJob("1024,CPDBAS_Dog_Primates,");
	//
	//		//run(true, new String[] { "CPDBAS_Rat" }, null);
	//
	//		//		CFPNestedCV cv = new CFPNestedCV();
	//		//		cv.datasets.remove("AMES");
	//		//		cv.jobValidateModel().runSequentially();
	//
	//		//		printSelectedAlgorithms();
	//
	//		{
	//			//			CFPNestedCV cv = new CFPNestedCV();
	//			//			//cv.datasets.remove("AMES");
	//			//			cv.datasets = ListUtil.createList("AMES");
	//			//			cv.jobSelectModel().runSequentially();
	//			//System.out.println(selectModelOverview().toNiceString());
	//			//			//			cv.sizes = ListUtil.createList(8192);
	//			//			//			cv.datasets = ListUtil.createList("ChEMBL_51");
	//			//			cv.jobSelectModel().runSequentially();
	//			//			for (String dataset : cv.datasets)
	//			//			{
	//			//				System.out.println(dataset);
	//			//				System.out.println(cv.selectModelClassifier(dataset).getName());
	//			//				System.out.println(cv.selectModelFeatures(dataset).getName());
	//			//			}
	//		}
	//
	//		{
	//			//String dataset = "CPDBAS_Dog_Primates";
	//			//String dataset = "DUD_vegfr2";
	//			String dataset = "MUV_733";
	//			//String dataset = "ChEMBL_61";
	//			//String dataset = "AMES";
	//
	//			CFPNestedValidation cv = new CFPNestedValidation();
	//
	//			//			cv.numFolds = 4;
	//			//			cv.numRepetitions = 5;
	//			cv.sizes = ListUtil.createList(0);
	//			cv.featureSelections = ListUtil.createList(FeatureSelection.none);
	//			cv.datasets = ListUtil.createList(dataset);
	//			cv.classifiers = ListUtil.create(Model.class, new NaiveBayesModel(),
	//					new RandomForestModel());
	//			cv.numSplitRepetitions = 5;
	//
	//			cv.splitRatio = 0.9;
	//
	//			cv.jobSelectModel().runSequentially();
	//
	//			//			ValidationEval val = (ValidationEval) cv.jobSelectModel().jobs().get(0);
	//			//			System.out.println(val.getBestModel());
	//
	//			//			selectModelOverview();
	//			//			System.out.println(cv.selectModelClassifier(dataset).getName());
	//			//			System.out.println(cv.selectModelFeatures(dataset).getName());
	//			//
	//			//cv.jobValidateModel().runSequentially();
	//			//			System.out.println(cv.validateModel(dataset).toNiceString());
	//			//			cv.plotValidationResult(dataset, null);
	//			//		cv.jobValidateModel().runSequentially();
	//		}
	//	}
	//
	//	public static void main(String[] args) throws Exception
	//	{
	//		//		Printer.PRINT_TO_SYSTEM_OUT = true;
	//		//args = "-d DUD_vegfr2 -s 1024 -c 0,1".split(" ");
	//
	//		if (args.length > 0 && args[0].equals("debug"))
	//		{
	//			debug();
	//		}
	//		else
	//		{
	//			DB.init(new ResultProviderImpl("jobs/store", "jobs/tmp"),
	//					new BlockerImpl("jobs/block"));
	//			Printer.setOutfile("jobs/out",
	//					ArrayUtil.toString(args, "_", "", "", "").replaceAll(",", "#"));
	//			Options opt = new Options();
	//			opt.addOption("s", true, "sizes, comma seperated");
	//			opt.addOption("d", true, "datasets, comma seperated");
	//			opt.addOption("c", true, "classifier index 0-12, comma seperated");
	//			opt.addOption("v", false, "v given: validate, v not given: select");
	//			CommandLine cmd = new DefaultParser().parse(opt, args);
	//			Integer sizes[] = null;
	//			String datasets[] = null;
	//			Integer classifiers[] = null;
	//			boolean validate = false;
	//			if (cmd.hasOption("s"))
	//				sizes = ArrayUtil.parseIntegers(cmd.getOptionValue("s").split(","));
	//			if (cmd.hasOption("d"))
	//				datasets = cmd.getOptionValue("d").split(",");
	//			if (cmd.hasOption("c"))
	//				classifiers = ArrayUtil.parseIntegers(cmd.getOptionValue("c").split(","));
	//			if (cmd.hasOption("v"))
	//				validate = true;
	//			run(validate, datasets, sizes, classifiers);
	//		}
	//		System.exit(0);
	//	}
}
