package org.kramerlab.cfpminer.experiments.validation;

import java.awt.Dimension;
import java.util.HashMap;

import org.jfree.chart.ChartPanel;
import org.kramerlab.cfpminer.weka.eval2.CFPFeatureProvider;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.datamining.ResultSetBoxPlot;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.FileUtil;
import org.mg.javalib.util.ListUtil;
import org.mg.javalib.util.SwingUtil;
import org.mg.wekalib.eval2.model.FeatureModel;
import org.mg.wekalib.eval2.model.Model;
import org.mg.wekalib.evaluation.PredictionUtil;
import org.mg.wekalib.evaluation.PredictionUtil.ClassificationMeasure;

public class InnerValidationResults extends PaperValidationResults
{
	public static void plotValidationResult(ResultSet rs, String pngFile) throws Exception
	{
		ResultSetBoxPlot plot = new ResultSetBoxPlot(rs, null, null, null, ArrayUtil
				.toList(ArrayUtil.toStringArray(PredictionUtil.ClassificationMeasure.SELECTION)));
		plot.setHideMean(true);
		plot.setPrintMeanAndStdev(true);
		plot.printNumResultsPerPlot(false);
		Dimension dim = new Dimension(450, 200);
		plot.setFontSize(12);

		if (pngFile != null)
			plot.ToPNGFile(pngFile, dim);
		else
		{
			ChartPanel p = plot.getChart();
			p.setPreferredSize(dim);
			SwingUtil.showInFrame(p);
		}
	}

	public ResultSet getValidationResults(String dataset) throws Exception
	{
		CFPCrossValidation cv = CFPCrossValidation.paramOptimize();
		cv.datasets = ListUtil.createList(dataset);
		ResultSet rs = cv.validateModelResultsPerRepetition(ClassificationMeasure.SELECTION);
		return rs;
	}

	public void plotValidationResult(String dataset, String pngFile) throws Exception
	{
		plotValidationResult(getValidationResults(dataset), pngFile);
	}

	public void printSelectedAndValidatedAlgorithms() throws Exception
	{
		printSelectedAndValidatedAlgorithms((String) null);
	}

	public void printSelectedAndValidatedAlgorithms(String... dataset) throws Exception
	{
		CFPCrossValidation cv = CFPCrossValidation.paramOptimize();
		if (dataset != null && dataset[0] != null)
			cv.datasets = ListUtil.createList(dataset);

		HashMap<String, FeatureModel> selectedAlgs = cv.selectBestModel();

		ResultSet rs = cv.validateModelResults(ClassificationMeasure.SELECTION);
		int idx = 0;
		for (String data : selectedAlgs.keySet())
		{
			FeatureModel model = selectedAlgs.get(data);
			Model classifierModel = (Model) model.getModel().cloneJob();
			CFPFeatureProvider featProv = (CFPFeatureProvider) model.getFeatureProvider();

			if (!rs.getResultValue(idx, "Dataset").equals(data))
				throw new IllegalStateException();
			rs.setResultValue(idx, "Dataset", data.replaceAll("_", " "));
			rs.setResultValue(idx, "Algorithm", classifierModel.getAlgorithmShortName());
			rs.setResultValue(idx, "AlgParams", classifierModel.getAlgorithmParamsNice());
			//			rs.setResultValue(idx, "FeatureSelection",
			//					featProv.getFeatureSelection().toNiceString());
			rs.setResultValue(idx, "HashfoldSize", featProv.getHashfoldSize());
			//			rs.setResultValue(idx, "Type", featProv.getType());

			idx++;
		}
		rs.sortProperties(new String[] { "Dataset", "Algorithm", "AlgParams", //"FeatureSelection",
				"HashfoldSize"//, "Type"
		});

		rs.setNumDecimalPlaces(3);
		System.out.println(rs.toNiceString());

		if (WRITE_FILES)
		{
			String f = DEST_FOLDER + "/table_nested.tex";
			System.err.println("printed latex-table to " + f);
			rs.setNicePropery("Accuracy", "Accur.");
			rs.setNicePropery("Sensitivity", "Sensit.");
			rs.setNicePropery("Specificity", "Specif.");
			rs.setNicePropery("HashfoldSize", "\\#Frags");
			FileUtil.writeStringToFile(f, rs.toLatexTable());
		}

		System.out.println(rs.getResultValues("Algorithm"));
		System.out.println(rs.getResultValues("HashfoldSize"));
	}

	public FeatureModel getSelectedModel(String dataset) throws Exception
	{
		CFPCrossValidation cv = CFPCrossValidation.paramOptimize();
		cv.datasets = ListUtil.createList(dataset);
		HashMap<String, FeatureModel> selectedAlgs = cv.selectBestModel();
		return selectedAlgs.get(dataset);
	}

	public static void main(String[] args) throws Exception
	{
		//		CFPCrossValidation cv = CFPCrossValidation.paramOptimize();
		//		cv.datasets = ListUtil.createList("NCTRER");
		//		System.out.println(cv.selectModelResults(ClassificationMeasure.values(), false));
		//		FeatureModel fm = cv.selectBestModel().get(0);
		//		System.out.println(fm.getFeatureProvider().getName());
		//		System.out.println(fm.getModel().getName());
		//		System.out.println(cv.selectModelResults(ClassificationMeasure.values(), false));
		//		System.out.println(cv.validateModelResults(ClassificationMeasure.values()));

		//		plotValidationResult("NCTRER", null);

		WRITE_FILES = false;
		//new InnerValidationResults().printSelectedAndValidatedAlgorithms();
		//		System.out.println(new InnerValidationResults().getValidationResults("NCTRER"));
		System.out.println(new InnerValidationResults().getSelectedModel("NCTRER"));
		System.exit(0);
	}
}
