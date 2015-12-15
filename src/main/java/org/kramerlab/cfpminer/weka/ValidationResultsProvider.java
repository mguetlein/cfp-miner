package org.kramerlab.cfpminer.weka;

import java.awt.Dimension;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JScrollPane;

import org.jfree.chart.ChartPanel;
import org.kramerlab.cfpminer.CFPMiner;
import org.kramerlab.cfpminer.CFPMiner.CFPType;
import org.kramerlab.cfpminer.CFPMiner.FeatureSelection;
import org.mg.javalib.datamining.Result;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.datamining.ResultSetBoxPlot;
import org.mg.javalib.datamining.ResultSetFilter;
import org.mg.javalib.datamining.ResultSetLinePlot;
import org.mg.javalib.datamining.ResultSetLinePlot.XLabelsRotation;
import org.mg.javalib.datamining.WekaResultSetUtil;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.SwingUtil;
import org.mg.wekalib.data.MergeArffFiles;

import weka.core.Instances;
import weka.gui.experiment.ResultsPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class ValidationResultsProvider
{
	//	public static final String RESULTS_FOLDER = "/home/martin/workspace/CFPMiner/results_r5_all/";
	//	public static final String RESULTS_MERGED_FOLDER = "/home/martin/workspace/CFPMiner/results_r5_all_merged/";

	//	public static final String RESULTS_FOLDER = "/home/martin/workspace/CFPMiner/results_nb/";
	//	public static final String RESULTS_MERGED_FOLDER = "/home/martin/workspace/CFPMiner/results_nb_merged/";

	public static final String RESULTS_FOLDER = "/home/martin/workspace/CFPMiner/results_rnf/";
	public static final String RESULTS_MERGED_FOLDER = "/home/martin/workspace/CFPMiner/results_rnf_merged/";

	//	public static final String RESULTS_FOLDER = "/home/martin/workspace/CFPMiner/results_small/";
	//	public static final String RESULTS_MERGED_FOLDER = "/home/martin/workspace/CFPMiner/results_small_merged/";

	//    public static final String RESULTS_FOLDER = "/home/martin/workspace/CFPMiner/results_r5_best_no_resample/";
	//    public static final String RESULTS_MERGED_FOLDER = "/home/martin/workspace/CFPMiner/results_r5_best_no_resample_merged/";

	File arff;
	ResultSet allResults;
	ResultSet results;

	public static final String[] ORIG_PERFORMANCE_MEASURES = new String[] { "Accuracy", "AUC", "AUPRC", "Sensitivity",
			"Selectivity" };

	static String performanceMeasures[];
	static HashMap<String, String> wekaAttributes = new LinkedHashMap<>();
	static
	{
		setPerformanceMeasures(ORIG_PERFORMANCE_MEASURES);
	}

	public static void setPerformanceMeasures(String perf[])
	{
		performanceMeasures = perf;

		wekaAttributes.clear();
		//		wekaAttributes.put("Key_Scheme_options", "Method");
		wekaAttributes.put("Key_Dataset", "Dataset");
		wekaAttributes.put("Key_Run", "Run");
		wekaAttributes.put("Key_Fold", "Fold");
		wekaAttributes.put("measureNumAttributesSelected", "Features");

		if (ArrayUtil.indexOf(perf, "AUC") != -1)
			wekaAttributes.put("Area_under_ROC", "AUC");
		if (ArrayUtil.indexOf(perf, "Accuracy") != -1)
			wekaAttributes.put("Percent_correct", "Accuracy");
		if (ArrayUtil.indexOf(perf, "Sensitivity") != -1)
			wekaAttributes.put("True_positive_rate", "Sensitivity");
		if (ArrayUtil.indexOf(perf, "Selectivity") != -1)
			wekaAttributes.put("IR_precision", "Selectivity");
		if (ArrayUtil.indexOf(perf, "Specificity") != -1)
			wekaAttributes.put("True_negative_rate", "Specificity");
		if (ArrayUtil.indexOf(perf, "FMeasure") != -1)
			wekaAttributes.put("F_measure", "FMeasure");
		if (ArrayUtil.indexOf(perf, "TP") != -1)
			wekaAttributes.put("Num_true_positives", "TP");
		if (ArrayUtil.indexOf(perf, "TN") != -1)
			wekaAttributes.put("Num_true_negatives", "TN");
		if (ArrayUtil.indexOf(perf, "FP") != -1)
			wekaAttributes.put("Num_false_positives", "FP");
		if (ArrayUtil.indexOf(perf, "FN") != -1)
			wekaAttributes.put("Num_false_negatives", "FN");
		if (ArrayUtil.indexOf(perf, "AUPRC") != -1)
			wekaAttributes.put("Area_under_PRC", "AUPRC");

	}

	public ValidationResultsProvider(String... arffResultFiles) throws Exception
	{
		this(null, arffResultFiles);
	}

	public ValidationResultsProvider(ResultSetFilter filter, String... arffResultFiles) throws Exception
	{
		if (arffResultFiles.length > 1)
		{
			arff = File.createTempFile("result", "arff");
			arff.deleteOnExit();
			MergeArffFiles.merge(arffResultFiles, arff.getAbsolutePath());
		}
		else
			arff = new File(arffResultFiles[0]);
		allResults = WekaResultSetUtil.fromWekaDataset(new Instances(new FileReader(arff)));
		results = new ResultSet();
		for (int i = 0; i < allResults.getNumResults(); i++)
		{
			int idx = results.addResult();

			Integer version = (int) Double
					.parseDouble(allResults.getResultValue(i, "Key_Scheme_version_ID").toString());
			String method = allResults.getResultValue(i, "Key_Scheme_options").toString();
			results.setResultValue(idx, "Algorithm", algFromMethod(method, version));
			results.setResultValue(idx, "CFPType", fpFromMethod(method));
			FeatureSelection sel = featFromMethod(method);
			results.setResultValue(idx, "FeatureSelection", sel);
			results.setResultValue(idx, "hashfoldSize", sel == FeatureSelection.none ? null : sizeFromMethod(method));

			for (String wp : wekaAttributes.keySet())
				results.setResultValue(idx, wekaAttributes.get(wp), allResults.getResultValue(i, wp));

			if (filter != null)
				results.filterLastResult(filter);
		}
		if (wekaAttributes.containsKey("Percent_correct"))
			for (int i = 0; i < results.getNumResults(); i++)
				results.setResultValue(i, wekaAttributes.get("Percent_correct"),
						((Double) results.getResultValue(i, wekaAttributes.get("Percent_correct"))) * 0.01);
	}

	private static String algFromMethod(String m, int version)
	{
		if (m.contains("RandomForest"))
		{
			if (version >= 4)
				return "RnF";
			else
				return "RaF";
		}
		else if (m.contains("SMO"))
			return "SMO";
		else if (m.contains("NaiveBayes"))
			return "NBy";
		throw new IllegalArgumentException(m);
	}

	private static CFPType fpFromMethod(String m)
	{
		for (CFPType t : CFPType.values())
			if (m.contains(t.toString()))
				return t;
		throw new IllegalArgumentException(m);
	}

	private static FeatureSelection featFromMethod(String m)
	{
		for (FeatureSelection t : FeatureSelection.values())
			if (m.contains(t.toString()))
				return t;
		throw new IllegalArgumentException(m);
	}

	private static Integer sizeFromMethod(String m)
	{
		for (int s : new int[] { 1024, 2048, 4096, 8192 })
			if (m.contains(s + ""))
				return s;
		throw new IllegalArgumentException(m);
	}

	public static ValidationResultsProvider readResults(String dir, String includePattern) throws Exception
	{
		return readResults(dir, includePattern, null);
	}

	public static ValidationResultsProvider readResults(String dir, String includePattern, String excludePattern)
			throws Exception
	{
		List<String> files = new ArrayList<String>();
		for (String n : new File(dir).list())
		{
			String s = dir + File.separator + n;
			if (!s.endsWith(".arff"))
				continue;
			//			System.out.println(s);
			//			System.out.println(".*(" + includePattern + ").*");
			//			System.out.println(s.matches(".*(" + includePattern + ").*"));
			//			System.out.println(".*(" + excludePattern + ").*");
			//			System.out.println(s.matches(".*(" + excludePattern + ").*"));
			//			System.out.println();
			if (!s.matches(".*(" + includePattern + ").*"))
				continue;
			if (excludePattern != null && s.matches(".*(" + excludePattern + ").*"))
				continue;
			files.add(s);
		}
		Collections.sort(files, new Comparator<String>()
		{
			@Override
			public int compare(String o1, String o2)
			{
				if (o1.contains("filt") && o2.contains("fold"))
					return 1;
				if (o2.contains("filt") && o1.contains("fold"))
					return -1;
				return o1.compareTo(o2);
			}
		});
		return new ValidationResultsProvider(ArrayUtil.toArray(files));
	}

	public void plot(String valPng)
	{
		ResultSetBoxPlot plot = new ResultSetBoxPlot(results, "", "", null, ArrayUtil.toList(performanceMeasures));
		plot.setHideMean(true);
		plot.printNumResultsPerPlot(false);
		plot.setPrintMeanAndStdev(true);
		if (valPng != null)
			plot.ToPNGFile(valPng, new Dimension(450, 150));
		else
			SwingUtil.showInDialog(plot.getChart());
	}

	public void plots(String compareProp, String compareProp2, boolean box)
	{
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("fill:pref:grow"));
		for (String p : performanceMeasures)
		{
			ChartPanel c;
			if (box)
			{
				ResultSetBoxPlot plot = new ResultSetBoxPlot(results, "", p, compareProp, compareProp2, p);
				plot.setHideMean(true);
				plot.setRotateXLabels(true);
				//			plot.printResultsPerPlot(false);

				c = plot.getChart();
			}
			else
			{
				ResultSet r = results.join(new String[] { compareProp, compareProp2 }, new String[] { "Run", "Fold" },
						null);
				//System.out.println(r.toNiceString());
				ResultSetLinePlot plot = new ResultSetLinePlot(r, p, compareProp, compareProp2);
				plot.setTitle(null);
				plot.setXAxisLabel(null);
				plot.setYAxisLabel(p);
				plot.setRotateXLabels(XLabelsRotation.vertical);
				c = plot.getChartPanel();
			}

			c.setMaximumDrawWidth(10000);
			c.setMaximumDrawHeight(5000);
			c.setPreferredSize(new Dimension(1750, 800));

			//			c.setBounds(new Rectangle(0, 0, 1000, 200));
			builder.append(c);
		}
		SwingUtil.showInDialog(new JScrollPane(builder.getPanel()), new Dimension(1850, 1100));
	}

	public void significanceTest()
	{
		try
		{
			class MyResultsPanel extends ResultsPanel
			{
				public void performTestPublic()
				{
					m_CompareModel.setSelectedItem("AUC");
					m_SigTex.setText("1");
					performTest();
					System.out.println(m_OutText.getText());
				}
			}
			MyResultsPanel p = new MyResultsPanel();
			Instances data = new Instances(new FileReader(arff));
			p.setInstances(data);
			p.performTestPublic();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	final static String SIZES[] = new String[] { "1024", "2048", "4096", "8192" };

	public static void plotResults() throws Exception
	{
		for (String alg : new String[] { "RaF", "SMO" })
		{
			ValidationResultsProvider val = readResults("/home/martin/workspace/CFPMiner/results", alg,
					"2048|4096|8192");
			val.plots("Method", "Dataset", false);
		}
		System.exit(0);
	}

	public static void testResults() throws Exception
	{
		for (String alg : new String[] { "RaF", "SMO" })
		{

			{
				ResultSet results = readResults("/home/martin/workspace/CFPMiner/results_no_closed", alg, null).results;
				results.sortResults("Method");
				//				{
				//					System.out.println("\n\nprint auc\n");
				//					ResultSet r = results.join(new String[] { "Method", "Dataset" }, new String[] { "Run", "Fold" },
				//							null);
				//					r.sortResults("Method");
				//					System.out.println(r.toNiceString());
				//					r.sortResults("Dataset");
				//					r = r.filter(new ResultSetFilter()
				//					{
				//
				//						@Override
				//						public boolean accept(Result result)
				//						{
				//							return result.getValue("Method").toString().contains("1024");
				//						}
				//					});
				//					r = r.diff("Method", ArrayUtil.toList(new String[] { "Dataset" }), null);
				//					System.out.println(r.toNiceString());
				//				}
				{
					System.out.println("\n\ncompare filtering to folding\n");
					System.out.println(ttest(results, "AUC", "filt", true).toNiceString());
				}
				{
					System.out.println("\n\ncompare different sizes when folding\n");
					ResultSet r = retain(results, "Method", "fold");
					System.out.println(ttest(r, "AUC", "1024", false).toNiceString());
				}
				{
					System.out.println("\n\ncompare different sizes when filtering\n");
					ResultSet r = retain(results, "Method", "filt");
					System.out.println(ttest(r, "AUC", "1024", false).toNiceString());
				}
			}
		}
	}

	private static ResultSet retain(ResultSet set, final String prop, final String match)
	{
		return set.filter(new ResultSetFilter()
		{
			@Override
			public boolean accept(Result result)
			{
				return result.getValue(prop).toString().contains(match);
			}
		});
	}

	public static ResultSet ttest(ResultSet set, String measure, final String leftSide, final boolean numberEqual)
	{
		double prob = 0.05;
		ResultSet test = set.pairedTTestWinLoss("Method", new String[] { "Run", "Fold" }, measure, prob, 1 / 9.0,
				new String[] { "Dataset" }, true);
		test = test.filter(new ResultSetFilter()
		{
			@Override
			public boolean accept(Result result)
			{
				boolean keep = result.getValue("Method_1").toString().contains(leftSide);
				if (!keep)
					return false;
				if (numberEqual)
				{
					for (String size : SIZES)
					{
						if (result.getValue("Method_1").toString().contains(size)
								&& result.getValue("Method_2").toString().contains(size))
							return true;
					}
					return false;
				}
				else
					return true;

			}
		});
		return test;
	}

	public static boolean resultsExist(String datasetName, CFPMiner miner, String classifier)
	{
		return new File(RESULTS_FOLDER
				+ CFPValidate.resultFileName(1, miner.getCFPType(), miner.getFeatureSelection(),
						miner.getHashfoldsize(), classifier, datasetName)).exists();
	}

	public static String getResultsFile(String datasetName, CFPMiner miner, String classifier)
	{
		final String resultFileName = CFPValidate.resultFileName(miner.getCFPType(), miner.getFeatureSelection(),
				miner.getHashfoldsize(), classifier, datasetName);
		String dest = RESULTS_MERGED_FOLDER + resultFileName;
		if (!new File(dest).exists())
		{
			try
			{
				MergeArffFiles.merge(RESULTS_FOLDER, new FilenameFilter()
				{
					@Override
					public boolean accept(File dir, String name)
					{
						return name.contains(resultFileName);
					}
				}, dest);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		return dest;
	}

	public static void main(String[] args) throws Exception
	{
		//plotResults();
		testResults();

		//		ValidationResultsProvider res = new ValidationResultsProvider(
		//				"/home/martin/workspace/CFPService/persistance/model/CPDBAS_Mutagenicity.arff");
		//		res.plot(null);

		System.exit(0);
	}

}
