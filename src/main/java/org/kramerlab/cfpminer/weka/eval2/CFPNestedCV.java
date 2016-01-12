package org.kramerlab.cfpminer.weka.eval2;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.DataLoader;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.datamining.ResultSetBoxPlot;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.FileUtil;
import org.mg.javalib.util.ListUtil;
import org.mg.javalib.util.SwingUtil;
import org.mg.wekalib.eval2.CVEvalModel;
import org.mg.wekalib.eval2.CVEvaluator;
import org.mg.wekalib.eval2.MultiDatasetRunner;
import org.mg.wekalib.eval2.job.FeatureProvider;
import org.mg.wekalib.eval2.job.Printer;
import org.mg.wekalib.eval2.model.FeatureModel;
import org.mg.wekalib.eval2.model.Model;
import org.mg.wekalib.eval2.model.NaiveBayesModel;
import org.mg.wekalib.eval2.model.RandomForestModel;
import org.mg.wekalib.eval2.model.SupportVectorMachineModel;
import org.mg.wekalib.evaluation.PredictionUtil;
import org.mg.wekautil.Predictions;

import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.functions.supportVector.RBFKernel;

public class CFPNestedCV
{
	int numFolds;
	int numRepetitions;
	List<Integer> sizes;
	List<Model> classifiers;
	List<String> datasets;

	private DataLoader loader = new DataLoader("data");
	private List<Model> featModels;
	private List<CDKDataSet> data;

	public CFPNestedCV()
	{
		numFolds = 10;
		numRepetitions = 3;
		sizes = ArrayUtil.toList(new Integer[] { 1024, 2048, 4096, 8192 });

		classifiers = new ArrayList<Model>();
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

		datasets = ArrayUtil.toList(loader.allDatasets());
	}

	public void init()
	{
		List<CFPFeatureProvider> feats = new ArrayList<CFPFeatureProvider>();
		for (Integer size : sizes)
			feats.add(new CFPFeatureProvider(size, FeatureSelection.filt, CFPType.ecfp4));
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

	public MultiDatasetRunner<String> jobSelectModel() throws Exception
	{
		init();

		CVEvaluator innerCV = new CVEvaluator();
		innerCV.setModels(ListUtil.toArray(featModels));
		innerCV.setNumFolds(numFolds);
		innerCV.setRepetitions(numRepetitions);

		MultiDatasetRunner<String> d = new MultiDatasetRunner<>();
		d.setJob(innerCV);
		d.setDataSets(ListUtil.toArray(data));

		return d;
	}

	public FeatureModel selectModel(String dataset) throws Exception
	{
		System.out.println(dataset);
		MultiDatasetRunner<String> d = jobSelectModel();
		if (!d.isDone())
			throw new IllegalArgumentException("not yet done");
		int idx = datasets.indexOf(dataset);
		CVEvaluator cvEval = (CVEvaluator) d.jobs().get(idx);
		return (FeatureModel) cvEval.getBestModel();
	}

	public Model selectModelClassifier(String dataset) throws Exception
	{
		return selectModel(dataset).getModel();
	}

	public CFPFeatureProvider selectModelFeatures(String dataset) throws Exception
	{
		return (CFPFeatureProvider) selectModel(dataset).getFeatureProvider();
	}

	public ResultSet selectModelOverview() throws Exception
	{
		ResultSet rs = new ResultSet();
		for (String dataset : datasets)
		{
			int idx = rs.addResult();
			rs.setResultValue(idx, "Dataset", dataset.replaceAll("_", " "));
			Model m = selectModelClassifier(dataset);
			rs.setResultValue(idx, "Alg", m.getAlgorithmShortName());
			rs.setResultValue(idx, "Params", m.getAlgorithmParamsNice());
			CFPFeatureProvider f = selectModelFeatures(dataset);
			rs.setResultValue(idx, "#Frags", f.getHashfoldSize());

			ResultSet rsV = validateModel(dataset);
			rsV = rsV.join("Dataset");
			if (rsV.getNumResults() != 1)
				throw new IllegalStateException();
			for (PredictionUtil.ClassificationMeasure measure : PredictionUtil.ClassificationMeasure
					.values())
				rs.setResultValue(idx, measure.shortName(),
						rsV.getResultValue(0, measure.toString()));
		}

		System.out.println(rs.getResultValues("Alg"));
		System.out.println(rs.getResultValues("#Frags"));

		return rs;
	}

	public MultiDatasetRunner<String> jobValidateModel() throws Exception
	{
		init();

		CVEvaluator innerCV = new CVEvaluator();
		innerCV.setModels(ListUtil.toArray(featModels));
		innerCV.setNumFolds(numFolds);
		innerCV.setRepetitions(numRepetitions);

		CVEvalModel cvm = new CVEvalModel();
		cvm.setCvEvaluator(innerCV);

		CVEvaluator outerCV = new CVEvaluator();
		outerCV.setModels(cvm);
		outerCV.setNumFolds(numFolds);
		outerCV.setRepetitions(numRepetitions);

		MultiDatasetRunner<String> d = new MultiDatasetRunner<>();
		d.setJob(outerCV);
		d.setDataSets(ListUtil.toArray(data));

		return d;
	}

	public ResultSet validateModel(String dataset) throws Exception
	{
		MultiDatasetRunner<String> d = jobValidateModel();
		if (!d.isDone())
			d.runSequentially();
		int idx = datasets.indexOf(dataset);
		CVEvaluator cvEval = (CVEvaluator) d.jobs().get(idx);
		List<Predictions> pred = cvEval.getPredictions();

		ResultSet rs = new ResultSet();
		int seed = 0;
		for (Predictions predictions : pred)
		{
			for (Predictions p : PredictionUtil.perFold(predictions))
			{
				int rIdx = rs.addResult();
				rs.setResultValue(rIdx, "Dataset", dataset);
				rs.setResultValue(rIdx, "Fold", p.fold[0]);
				rs.setResultValue(rIdx, "Seed", seed);
				for (PredictionUtil.ClassificationMeasure m : PredictionUtil.ClassificationMeasure
						.values())
					rs.setResultValue(rIdx, m.toString(),
							PredictionUtil.getClassificationMeasure(p, m));
			}
			seed++;
		}
		return rs;
	}

	public void plotValidationResult(String dataset, String pngFile) throws Exception
	{
		plotValidationResult(validateModel(dataset), pngFile);
	}

	public static void plotValidationResult(ResultSet rs, String pngFile) throws Exception
	{
		ResultSetBoxPlot plot = new ResultSetBoxPlot(rs, null, null, null, ArrayUtil
				.toList(ArrayUtil.toStringArray(PredictionUtil.ClassificationMeasure.values())));
		plot.setHideMean(true);
		plot.printNumResultsPerPlot(false);

		if (pngFile != null)
			plot.ToPNGFile(pngFile, new Dimension(450, 150));
		else
			SwingUtil.showInFrame(plot.getChart());
	}

	public static void run(boolean validate, String[] datasets, Integer sizes[],
			Integer classifiers[]) throws Exception
	{
		CFPNestedCV cv = new CFPNestedCV();
		if (sizes != null)
			cv.sizes = ArrayUtil.toList(sizes);
		if (datasets != null)
			cv.datasets = ArrayUtil.toList(datasets);
		if (classifiers != null)
		{
			List<Model> selectedClassifiers = new ArrayList<>();
			for (Integer c : classifiers)
				selectedClassifiers.add(cv.classifiers.get(c));
			cv.classifiers = selectedClassifiers;
		}
		if (validate)
			cv.jobValidateModel().runSequentially();
		else
			cv.jobSelectModel().runSequentially();
	}

	public static void printSelectedAlgorithms() throws Exception
	{
		Printer.PRINT_TO_SYSTEM_OUT = true;

		CFPNestedCV cv = new CFPNestedCV();

		//cv.datasets = ListUtil.createList("ChEMBL_51");
		cv.datasets.remove("AMES");
		cv.jobSelectModel().runSequentially();

		ResultSet rs = cv.selectModelOverview();
		rs.sortResults("Dataset", DataLoader.CFPDataComparator);

		System.out.println(rs.toNiceString());
		String f = System.getProperty("user.home")
				+ "/documents/ecfps/latex/results/table_nested.tex";
		System.err.println("printed latex-table to " + f);
		rs.setNicePropery("#Frags", "\\#Frags");
		FileUtil.writeStringToFile(f, rs.toLatexTable());
	}

	public static void debug() throws Exception
	{
		Printer.PRINT_TO_SYSTEM_OUT = true;
		//runModelBuildJob("1024,CPDBAS_Dog_Primates,");

		//run(true, new String[] { "CPDBAS_Rat" }, null);

		//		CFPNestedCV cv = new CFPNestedCV();
		//		cv.datasets.remove("AMES");
		//		cv.jobValidateModel().runSequentially();

		printSelectedAlgorithms();

		//		{
		//			CFPNestedCV cv = new CFPNestedCV();
		//			cv.datasets.remove("AMES");
		//			//cv.datasets = ListUtil.createList("ChEMBL_51");
		//			cv.jobSelectModel().runSequentially();
		//			System.out.println(cv.selectModelOverview().toNiceString());
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

		//		{
		//			String dataset = "CPDBAS_Mouse";
		//
		//			CFPNestedCV cv = new CFPNestedCV();
		//			//			cv.numFolds = 4;
		//			//			cv.numRepetitions = 5;
		//			//			cv.sizes = ListUtil.createList(1024, 2048);
		//			cv.datasets = ListUtil.createList(dataset);
		//			//			cv.classifiers = ListUtil.create(Model.class, new NaiveBayesModel(),
		//			//					new RandomForestModel());
		//
		//			//cv.jobSelectModel().runSequentially();
		//			//			System.out.println(cv.selectModelClassifier(dataset).getName());
		//			//			System.out.println(cv.selectModelFeatures(dataset).getName());
		//			//
		//			cv.jobValidateModel().runSequentially();
		//			//			System.out.println(cv.validateModel(dataset).toNiceString());
		//			//			cv.plotValidationResult(dataset, null);
		//			//		cv.jobValidateModel().runSequentially();
		//		}
	}

	public static void main(String[] args) throws Exception
	{
		if (args.length > 0 && args[0].equals("debug"))
			debug();
		else
		{
			Printer.setOutfileSuffix(
					ArrayUtil.toString(args, "_", "", "", "").replaceAll(",", "#"));
			Options opt = new Options();
			opt.addOption("s", true, "sizes, comma seperated");
			opt.addOption("d", true, "datasets, comma seperated");
			opt.addOption("c", true, "classifier index 0-12, comma seperated");
			opt.addOption("v", false, "v given: validate, v not given: select");
			CommandLine cmd = new DefaultParser().parse(opt, args);
			Integer sizes[] = null;
			String datasets[] = null;
			Integer classifiers[] = null;
			boolean validate = false;
			if (cmd.hasOption("s"))
				sizes = ArrayUtil.parseIntegers(cmd.getOptionValue("s").split(","));
			if (cmd.hasOption("d"))
				datasets = cmd.getOptionValue("d").split(",");
			if (cmd.hasOption("c"))
				classifiers = ArrayUtil.parseIntegers(cmd.getOptionValue("c").split(","));
			if (cmd.hasOption("v"))
				validate = true;
			run(validate, datasets, sizes, classifiers);
		}
		System.exit(0);
	}
}
