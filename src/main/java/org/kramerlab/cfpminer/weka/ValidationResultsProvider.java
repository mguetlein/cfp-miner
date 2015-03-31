package org.kramerlab.cfpminer.weka;

import java.awt.Dimension;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JScrollPane;

import org.jfree.chart.ChartPanel;
import org.mg.javalib.datamining.Result;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.datamining.ResultSetBoxPlot;
import org.mg.javalib.datamining.ResultSetFilter;
import org.mg.javalib.datamining.ResultSetLinePlot;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.SwingUtil;
import org.mg.javalib.weka.MergeArffFiles;

import weka.core.Instances;
import weka.gui.experiment.ResultsPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class ValidationResultsProvider
{
	File arff;
	ResultSet allResults;
	ResultSet results;

	static String performanceMeasures[];
	static HashMap<String, String> wekaAttributes = new LinkedHashMap<>();
	static
	{
		setPerformanceMeasures(new String[] { "AUC", "Accuracy", "Sensitivity", "Selectivity" }); //"Specificity"
	}

	public static void setPerformanceMeasures(String perf[])
	{
		performanceMeasures = perf;

		wekaAttributes.clear();
		wekaAttributes.put("Key_Scheme_options", "Method");
		wekaAttributes.put("Key_Dataset", "Dataset");
		wekaAttributes.put("Key_Run", "Run");
		wekaAttributes.put("Key_Fold", "Fold");
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
	}

	public ValidationResultsProvider(String... arffResultFiles) throws Exception
	{
		if (arffResultFiles.length > 1)
		{
			arff = File.createTempFile("result", "arff");
			arff.deleteOnExit();
			MergeArffFiles.merge(arffResultFiles, arff.getAbsolutePath());
		}
		else
			arff = new File(arffResultFiles[0]);

		allResults = ResultSet.fromWekaDataset(new Instances(new FileReader(arff)));
		results = new ResultSet();
		for (int i = 0; i < allResults.getNumResults(); i++)
		{
			int idx = results.addResult();
			for (String wp : wekaAttributes.keySet())
				results.setResultValue(idx, wekaAttributes.get(wp), allResults.getResultValue(idx, wp));
		}
		if (wekaAttributes.containsKey("Percent_correct"))
			for (int i = 0; i < results.getNumResults(); i++)
				results.setResultValue(i, wekaAttributes.get("Percent_correct"),
						((Double) results.getResultValue(i, wekaAttributes.get("Percent_correct"))) * 0.01);
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
		plot.printResultsPerPlot(false);
		if (valPng != null)
			plot.ToPNGFile(valPng, new Dimension(500, 150));
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
				plot.setRotateXLabels(true);
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
		ResultSet test = set.pairedTTestWinLoss("Method", ArrayUtil.toList(new String[] { "Run", "Fold" }), measure,
				prob, "Dataset", true);
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
