package org.kramerlab.cfpminer.weka;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.jfree.chart.ChartPanel;
import org.kramerlab.cfpminer.CFPDataLoader;
import org.kramerlab.cfpminer.CFPMiner.FeatureSelection;
import org.mg.javalib.datamining.Result;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.datamining.ResultSetBoxPlot;
import org.mg.javalib.datamining.ResultSetFilter;
import org.mg.javalib.datamining.ResultSetIO;
import org.mg.javalib.datamining.ResultSetLinePlot;
import org.mg.javalib.io.ExternalTool;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.DoubleKeyHashMap;
import org.mg.javalib.util.FileUtil;
import org.mg.javalib.util.ListUtil;
import org.mg.javalib.util.StringUtil;
import org.mg.javalib.util.SwingUtil;
import org.mg.javalib.weka.MergeArffFiles;

public class CreatePaperResults
{
	static double SIG_LEVEL = 0.05;
	static Double TEST_CORRECTION = 1 / 9.0;
	static String[] SIZES = { "1024", "2048", "4096", "8192" };
	static String[] ALGORITHMS = new String[] { "RaF", "SMO" };

	static HashMap<String, String> niceValues = new HashMap<>();
	static
	{
		niceValues.put("hashfoldSize", "Num bits");
		niceValues.put("RaF", "Random forest");
		for (FeatureSelection f : FeatureSelection.values())
			if (!f.toString().equals(f.toNiceString()))
				niceValues.put(f.toString(), f.toNiceString());

		//ValidationResultsProvider.setPerformanceMeasures(new String[] { "Accuracy" });
	}
	static String destFolder = "/home/martin/documents/ecfps/latex/results/";

	String alg;
	String params;
	String paramsStr;
	//ValidationResultsProvider res;
	ResultSet results;
	boolean write = false;
	boolean showCharts = true;

	public void read(String alg, String params) throws Exception
	{
		this.alg = alg;
		this.params = params;
		paramsStr = params.length() > 0 ? ("_" + params) : "";
		String name = alg + paramsStr;
		System.out.println("reading " + name);
		ValidationResultsProvider valRes = new ValidationResultsProvider("/home/martin/workspace/CFPMiner/results/"
				+ name + ".arff");
		System.out.println("done");
		results = valRes.results;
	}

	public void renameMethods()
	{
		renameMethods(results);
	}

	public void renameDatasets(ResultSet results)
	{
		for (int i = 0; i < results.getNumResults(); i++)
		{
			String d = results.getResultValue(i, "Dataset").toString();
			results.setResultValue(i, "Dataset", d.replaceAll("_", " "));
		}
	}

	public void renameMethods(ResultSet results)
	{
		Boolean stripSize = oneSize(results);
		for (int i = 0; i < results.getNumResults(); i++)
		{
			String method = results.getResultValue(i, "Method").toString();
			results.setResultValue(i, "Method", renameMethod(method, stripSize));
		}
	}

	public void sortDatasets(ResultSet r)
	{
		r.sortResults("Dataset", CFPDataLoader.CFPDataComparator);
	}

	public String renameMethod(String method, boolean stripSize)
	{
		if (alg != null)
		{
			method = method.replace("RandomForest ", "");
			method = method.replace("SMO ", "");
		}
		if (params != null)
		{
			for (String p : params.split("_"))
				if (!p.equals("ecfp") && !p.equals("fcfp"))
					method = method.replace(p, "");
		}
		if (stripSize)
		{
			for (String size : SIZES)
			{
				method = method.replace(size, "");
			}
		}
		for (String k : niceValues.keySet())
		{
			method = method.replace(k, niceValues.get(k));
		}
		method = method.replace("__", "_");
		method = method.replace("_", " ");
		return method.trim();
	}

	public void debug() throws Exception
	{
		ranking("AMES");

		for (String alg : ALGORITHMS)
		{
			//			read(alg, "ecfp4_filt");
			//
			//			ResultSet r = results.join(new String[] { "Method", "Dataset" }, new String[] { "Run", "Fold" }, null);
			//			System.out.println(r.toNiceString());

			//			results = results.filter(new ResultSetFilter()
			//			{
			//				@Override
			//				public boolean accept(Result result)
			//				{
			//					return result.getValue("Method").toString().contains("ecfp2")
			//							|| result.getValue("Method").toString().contains("ecfp4");
			//				}
			//			});

			//tableWinLoss("FiltVsFold", WinLossCompareAgainst.sameSize, "filt", "fold");

			//			lineChart(2048);

			//			tableWinLossFiltering();
			//			//			tableDetailedFiltering();
			//lineChart(2048);
			//tableWinLossSizes();
			//tableWinLoss("ecfp4", "fcfp4");
			//			tableWinLoss("ecfp4", "ecfp2");
			//			tableWinLoss("ecfp4", "ecfp0");

			//			tableWinLossSize("filt");
			//			//			tableDetailedSize();
			//			//			chartSize("fold");
			//			tableDetailed("FiltVsFold", new String[] { "fold", "filt" }, sizes);
			//			tableDetailed("Size", sizes, new String[] { "fold", "filt" });
			//
			//			results = valRes.results.filter(new ResultSetFilter()
			//			{
			//				@Override
			//				public boolean accept(Result result)
			//				{
			//					return result.getValue("Method").toString().contains("ecfp")
			//							&& result.getValue("Method").toString().contains("fold");
			//				}
			//			});
			//tableDetailed("ECFP-Diameter", sizes, new String[] { "ecfp6", "ecfp4", "ecfp2", "ecfp0" });

			//			break;
		}

	}

	public static void merge(final String alg, final String p, final String p2) throws FileNotFoundException,
			IOException
	{
		//		final boolean orP2 = false;

		//csvToArff("/home/martin/data/arffs/nctrer.csv", "/home/martin/data/arffs/nctrer.arff");
		String dir = "/home/martin/workspace/CFPMiner/results_r4_no_smo_ames";
		String dest = "/home/martin/workspace/CFPMiner/results/" + alg + (p == null ? "" : ("_" + p))
				+ (p2 == null ? "" : ("_" + p2)) + ".arff";
		if (!new File(dest).exists())
			MergeArffFiles.merge(dir, new FilenameFilter()
			{

				@Override
				public boolean accept(File dir, String name)
				{
					if (!name.contains(alg))
						return false;
					//				if (orP2)
					//					return ((p == null || name.contains(p)) || (p2 == null || name.contains(p2)));
					//				else
					return ((p == null || name.contains(p)) && (p2 == null || name.contains(p2)));
				}
			}, dest);
	}

	public void merge() throws Exception
	{
		for (String alg : new String[] { "RaF", "SMO" })
		{
			for (String dataset : new String[] { "AMES" })
			{
				merge(alg, dataset, null);
			}

			for (String type : new String[] { "ecfp", "fcfp" })
			{
				for (String typeSize : new String[] { "6", "4", "2", "0" })
				{
					String p = type + typeSize;
					String p2 = null;
					merge(alg, p, p2);
				}
			}
			for (String type : new String[] { "ecfp", "fcfp" })
			{
				String p = type;
				for (String size : SIZES)
				{
					String p2 = "fold_" + size;
					merge(alg, p, p2);
				}
			}
		}
	}

	public void paper() throws Exception
	{
		write = true;
		showCharts = true;

		//		tableCollisions(false);
		for (final String alg : ALGORITHMS)
		{
			//			read(alg, "ecfp4_1024");
			//			//lineChart();
			//			diffChart();

			//read(alg, "ecfp4");
			//tableWinLoss("FiltVsFold", WinLossCompareAgainst.sameSize, "filt", "fold");

			//			for (final String type : new String[] { "ecfp", "fcfp" })
			//				for (final String typeSize : new String[] { "6", "4", "2", "0" })
			//				{
			//					read(alg, type + typeSize);
			//					tableDetailed("FiltVsFold", SIZES, new String[] { "fold", "filt" });
			//					//tableDetailed("Size", new String[] { "fold", "filt" }, sizes);
			//				}

			//			read(alg, "ecfp4_fold");
			//			tableWinLoss("Sizes", WinLossCompareAgainst.lastProp, new String[] { "2048", "4096", "8192", "1024" });
			//			read(alg, "ecfp4_filt");
			//			tableWinLoss("Sizes", WinLossCompareAgainst.lastProp, new String[] { "2048", "4096", "8192", "1024" });
			//

			//			for (String size : SIZES)
			//			{
			//				read(alg, "ecfp_fold_" + size);
			//				tableWinLoss("Diameters", WinLossCompareAgainst.all, "ecfp0", "ecfp2", "ecfp4", "ecfp6");
			//			}

			//			read(alg, "fold_1024");
			//			tableWinLoss("FCFPvsECFP", WinLossCompareAgainst.sameDiameter, new String[] { "fcfp", "ecfp" });
			//			read(alg, "filt_1024");
			//			tableWinLoss("FCFPvsECFP", WinLossCompareAgainst.sameDiameter, new String[] { "fcfp", "ecfp" });

			//			ValidationResultsProvider.setPerformanceMeasures(new String[] { "AUC" });
			//			read(alg, "filt");
			//			paramsStr = "_filt_best2";
			//			results = results.filter(new ResultSetFilter()
			//			{
			//				@Override
			//				public boolean accept(Result result)
			//				{
			//					String m = result.getValue("Method").toString();
			//					String s = sizeFromMethod(m);
			//					String t = fpFromMethod(m);
			//					if (alg.equals("RaF"))
			//						return ((s.equals("1024") && t.equals("fcfp4")) || (s.equals("2048") && t.equals("ecfp4")));
			//					else
			//						return ((s.equals("4096") && t.equals("fcfp4")) || (s.equals("8192") && t.equals("ecfp4")));
			//
			//				}
			//			});
			//			lineChart();

			//top 3 smo
			//fcp4 4096
			//ecfp4 8192
			//fcfp4 all

			//			break;
		}

		//ranking("");

		datasets();
	}

	// for ecfp0 fcfp0 fcfp2
	//  delete "filt <fp> 2048/4096/8192"
	//  rename "filt <fp> 1024" to "all <fp> ''"
	// delete "filt ecfp2 2048/4096"
	// rename "filt ecfp2 8192" to "all ecfp1 ''"
	// rename "filt fcfp4 8192" to "all fcfp4"
	public void convert_low_filt()
	{
		results = results.filter(new ResultSetFilter()
		{
			@Override
			public boolean accept(Result result)
			{
				String m = result.getValue("Method").toString();
				if (!m.contains("filt"))
					return true;
				if (m.contains("ecfp0") || m.contains("fcfp0") || m.contains("fcfp2"))
					return m.contains("1024");
				if (m.contains("ecfp2"))
					return m.contains("1024") || m.contains("8192");
				return true;
			}
		});
		for (int i = 0; i < results.getNumResults(); i++)
		{
			String m = results.getResultValue(i, "Method").toString();
			if (m.contains("ecfp0_filt_1024"))
				results.setResultValue(i, "Method", m.replace("ecfp0_filt_1024", "ecfp0_all"));
			if (m.contains("fcfp0_filt_1024"))
				results.setResultValue(i, "Method", m.replace("fcfp0_filt_1024", "fcfp0_all"));
			if (m.contains("fcfp2_filt_1024"))
				results.setResultValue(i, "Method", m.replace("fcfp2_filt_1024", "fcfp2_all"));
			if (m.contains("ecfp2_filt_8192"))
				results.setResultValue(i, "Method", m.replace("ecfp2_filt_8192", "ecfp2_all"));
			if (m.contains("fcfp4_filt_8192"))
				results.setResultValue(i, "Method", m.replace("fcfp4_filt_8192", "fcfp4_all"));
		}
	}

	public void supplementary() throws Exception
	{

	}

	public CreatePaperResults(boolean debug) throws Exception
	{
		Locale.setDefault(Locale.US);
		merge();
		if (debug)
			debug();
		else
			paper();
		//
	}

	public void datasets()
	{
		CFPDataLoader d = new CFPDataLoader("data");
		String datasets[] = d.allDatasets();
		Arrays.sort(datasets, 0, datasets.length, CFPDataLoader.CFPDataComparator);

		//		{
		//			ResultSet r = d.getInfo(datasets);
		//			System.out.println(r.toNiceString());
		//			if (write)
		//			{
		//				String dest = destFolder + "datasets.tex";
		//				System.out.println("write table to " + dest);
		//				FileUtil.writeStringToFile(dest, r.toLatexTable());
		//			}
		//			System.out.println();
		//		}

		{
			ResultSet r = d.getCategoryInfo(datasets);
			System.out.println(r.toNiceString());
			if (write)
			{
				String dest = destFolder + "datasets_overview.tex";
				System.out.println("write table to " + dest);
				FileUtil.writeStringToFile(dest, r.toLatexTable(null, null, null));
			}
		}
	}

	public void ranking(String params) throws Exception
	{
		ResultSet combined = new ResultSet();
		for (String alg : new String[] { "RaF", "SMO" })
		{
			ResultSet r;

			this.alg = alg;
			this.params = params;
			paramsStr = params.length() > 0 ? ("_" + params) : "";
			String cache = "/home/martin/workspace/CFPMiner/results/" + alg + paramsStr + ".ranking";
			if (!new File(cache).exists())
			{
				read(alg, params);
				convert_low_filt();
				r = results.join(new String[] { "Method", "Dataset" }, new String[] { "Run", "Fold" }, null);
				ResultSetIO.printToFile(new File(cache), r, true);
			}
			else
				r = ResultSetIO.parseFromFile(new File(cache));

			r = r.rank("AUC", new String[] { "Dataset" });
			r.clearMergeCountAndVariance();
			int numDatasets = r.getResultValues("Dataset").getNumValues();
			//			System.err.println(numDatasets);
			r = r.join("Method");
			renameMethods(r);
			r.removePropery("Dataset");
			for (String p : ValidationResultsProvider.performanceMeasures)
				r.removePropery(p);
			//			r.sortResults("AUC" + ResultSet.RANK_SUFFIX, true, true, -1);
			System.out.println(r.toNiceString());

			for (int i = 0; i < r.getNumResults(); i++)
			{
				if (alg.equals("RaF"))
					combined.addResult();
				else if (!combined.getResultValue(i, "Method").equals(r.getResultValue(i, "Method")))
					throw new IllegalStateException();
				combined.setResultValue(i, "Method", r.getResultValue(i, "Method"));
				combined.setResultValue(i, "Features", Math.round((Double) r.getResultValue(i, "Features")));
				combined.setResultValue(i, "Collision rate", "to be overwritten");
				combined.setResultValue(i, alg + " AUC Rank", r.getResultValue(i, "AUC" + ResultSet.RANK_SUFFIX));
				combined.setResultValue(i, alg + " AUC Best",
						((Double) r.getResultValue(i, "AUC" + ResultSet.RANK_BEST_SUFFIX)) * numDatasets);
				combined.setNicePropery(alg + " AUC Rank", "Rank");
				combined.setNicePropery(alg + " AUC Best", "Best");
			}

			r.sortResults("AUC" + ResultSet.RANK_SUFFIX, true, true, -1);
			System.out.println(r.toNiceString());
		}
		tableCollisions(true); // to load collisions
		for (int i = 0; i < combined.getNumResults(); i++)
		{
			String method = combined.getResultValue(i, "Method").toString();
			Double rate;
			if (method.contains(niceValues.get("fold")))
				rate = collisionRate.get(fpFromMethod(method), sizeFromMethod(method));
			else
				rate = 0.0;
			combined.setResultValue(i, "Collision rate", rate);
			combined.setResultValue(i, "Combined Rank",
					((Double) combined.getResultValue(i, "RaF AUC Rank") + (Double) combined.getResultValue(i,
							"SMO AUC Rank")) / 2.0);
		}
		combined.sortResults("Combined Rank", true, true, -1);
		System.out.println(combined.toNiceString());
		System.out.println(combined.getNumResults());
		System.out.println(9 / (double) combined.getNumResults());
		System.out.println(27 / (double) combined.getNumResults());

		if (write)
		{
			String dest = destFolder + "ranking.tex";
			System.out.println("write table to " + dest);

			String preProp = "& & ";
			for (String alg : new String[] { "RaF", "SMO" })
				preProp += "& \\multicolumn{2}{c}{" + alg + " AUC} ";
			preProp += "&\\\\\n";
			FileUtil.writeStringToFile(dest, combined.toLatexTable(null, null, preProp));
		}
	}

	DoubleKeyHashMap<String, String, Double> collisionRate = new DoubleKeyHashMap<>();

	public void tableCollisions(boolean noOutput)
	{
		String preProp = "& ";
		for (String size : SIZES)
			preProp += "& \\multicolumn{2}{c}{" + size + "} ";
		preProp += "\\\\\n";

		for (final String type : new String[] { "ecfp", "fcfp" })
		{
			ResultSet res = ResultSetIO.parseFromFile(new File("data_collisions/collisions_" + type + ".result"));
			if (!noOutput)
				System.out.println(res.toNiceString());
			sortDatasets(res);

			for (final String typeSize : new String[] { "6", "4", "2", "0" })
			{
				ResultSet r = res.copy().filter(new ResultSetFilter()
				{
					@Override
					public boolean accept(Result result)
					{
						return result.getValue("Type").toString().equals(type + typeSize);
					}
				});
				r.removePropery("Type");
				for (String size : SIZES)
				{
					r.setNicePropery(size + " bit-load", "bit-load");
					r.setNicePropery(size + " collisions", "rate");
				}

				if (!noOutput)
				{
					System.out.println("\n");
					String datasetPreProp = "& " + preProp;
					System.out.println(datasetPreProp);
					System.out.println(r.toNiceString());

					if (write)
					{
						String dest = destFolder + "collisions_" + type + typeSize + ".tex";
						System.out.println("write table to " + dest);
						FileUtil.writeStringToFile(dest, r.toLatexTable(null, null, datasetPreProp));
					}
				}
			}

			ResultSet joined = res.join("Type");
			joined.removePropery("Dataset");
			joined.removePropery("Compounds");

			for (String size : SIZES)
			{
				joined.setNicePropery(size + " bit-load", "bit-load");
				joined.setNicePropery(size + " collisions", "rate");
			}

			for (int i = 0; i < joined.getNumResults(); i++)
			{
				String t = joined.getResultValue(i, "Type").toString();
				for (int s = 0; s < SIZES.length; s++)
				{
					Double rate = (Double) joined.getResultValue(i, SIZES[s] + " collisions");
					//					System.out.println(t + " " + sizes[s] + " " + rate);
					collisionRate.put(t, SIZES[s], rate);
				}
			}

			if (!noOutput)
			{
				System.out.println("\n");
				System.out.println(preProp);
				System.out.println(joined.toNiceString());

				if (write)
				{
					String dest = destFolder + "collisions_" + type + ".tex";
					System.out.println("write table to " + dest);
					FileUtil.writeStringToFile(dest, joined.toLatexTable(null, null, preProp));
				}
			}
		}
	}

	public void tableWinLossFiltering()
	{
		results.sortResults("Method");
		ResultSet t1 = new ResultSet();
		for (String s : ValidationResultsProvider.performanceMeasures)
		{
			ResultSet set = ValidationResultsProvider.ttest(results, s, "filt", true);
			for (int i = 0; i < set.getNumResults(); i++)
			{
				String method = set.getResultValue(i, "Method_1").toString();
				if (!method.contains("filt"))
					throw new IllegalStateException();
				if (t1.getNumResults() <= i)
					t1.addResult();
				t1.setResultValue(i, "hashfoldSize", method.substring(method.lastIndexOf("_") + 1));
				t1.setResultValue(i, s, set.getResultValue(i, s));
			}
		}
		System.out.println("Filtering vs Folding");
		for (String p : niceValues.keySet())
			t1.setNicePropery(p, niceValues.get(p));
		System.out.println(t1.toNiceString());

		FileUtil.writeStringToFile(destFolder + "Table_WinLoss_FiltVsFold_" + alg + ".tex", t1.toLatexTable());
		//		System.out.println(t1.toLatexTable());
	}

	private boolean oneSize(ResultSet res)
	{
		String size = sizeFromMethod(res.getResultValue(0, "Method").toString());
		if (size == null)
			return false;
		for (Object s : res.getResultValues("Method").values())
		{
			String size2 = sizeFromMethod(s.toString());
			if (!size.equals(size2))
				return false;
		}
		return true;
	}

	public void lineChart()
	{
		ResultSet r = results.join(new String[] { "Method", "Dataset" }, new String[] { "Run", "Fold" }, null);
		sortDatasets(r);
		r.sortResults("Method", false);
		renameMethods(r);
		renameDatasets(r);

		//System.out.println(r.toNiceString());
		ResultSetLinePlot plot = new ResultSetLinePlot(r, ValidationResultsProvider.performanceMeasures, "Method",
				"Dataset");
		plot.setTitle(null);
		plot.setXAxisLabel(null);
		plot.setRotateXLabels(ResultSetLinePlot.XLabelsRotation.vertical);
		if (ValidationResultsProvider.performanceMeasures.length > 1)
			plot.setYAxisRange(0.0, 1.0);
		else if (ValidationResultsProvider.performanceMeasures.length == 1
				&& ValidationResultsProvider.performanceMeasures[0].equals("AUC"))
			plot.setYAxisRange(0.5, 1.0);
		for (String p : ValidationResultsProvider.performanceMeasures)
		{
			//			if (p.equals("Accuracy"))
			//			{
			plot.addMarker(p, "AMES", CFPDataLoader.BALANCED_DATASETS);
			plot.addMarker(p, "ChEMBL 100", "ChEMBL");
			plot.addMarker(p, "DUD cdk2", "DUD");
			plot.addMarker(p, "MUV 466", "MUV");
			//			}
			//			else
			//			{
			//				plot.addMarker(p, "AMES", "");
			//				plot.addMarker(p, "ChEMBL_100", "");
			//				plot.addMarker(p, "DUD_cdk2", "");
			//				plot.addMarker(p, "MUV_466", "");
			//			}
		}

		{
			ResultSet rTest = results.copy();
			renameMethods(rTest);
			for (String p : ValidationResultsProvider.performanceMeasures)
			{
				ResultSet test = rTest.pairedTTest("Method", ArrayUtil.toList(new String[] { "Run", "Fold" }), p,
						SIG_LEVEL, TEST_CORRECTION, "Dataset");
				List<String> methods = ListUtil.toStringList(r.getResultValues("Method").values());
				for (String m1 : methods)
				{
					for (String m2 : methods)
					{
						if (m1 == m2)
							continue;
						for (Object datasetWins : ResultSet.listSeriesWins(test, "Method", p, "Dataset", m1, m2))
							plot.setDrawShape(p, datasetWins.toString(), m1);
					}
				}
				System.out.println(test.toNiceString());
			}
			//			for (Object datasetWins : ResultSet.listSeriesWins(test, "Method", "AUC", "Dataset",
			//					"RandomForest ecfp_filt_1024", "RandomForest ecfp_fold_1024"))
			//				plot.setDrawShape("AUC", datasetWins.toString(), "RandomForest ecfp_filt_1024");
			//			for (Object datasetWins : ResultSet.listSeriesWins(test, "Method", "AUC", "Dataset",
			//					"RandomForest ecfp_fold_1024", "RandomForest ecfp_filt_1024"))
			//				plot.setDrawShape("AUC", datasetWins.toString(), "RandomForest ecfp_fold_1024");

		}

		ChartPanel c = plot.getChartPanel();
		c.setMaximumDrawWidth(10000);
		c.setMaximumDrawHeight(5000);
		//c.setPreferredSize(new Dimension(800, 600));

		if (showCharts)
			SwingUtil.showInFrame(c, new Dimension(1000, 800));

		if (write)
		{
			plot.toSVGFile(destFolder + "Chart_" + alg + paramsStr + ".svg", new Dimension(800,
					ValidationResultsProvider.performanceMeasures.length > 1 ? 600 : 400));
			toPDF(destFolder + "Chart_" + alg + paramsStr);
		}
	}

	public void diffChart()
	{
		ResultSet r = results.join(new String[] { "Method", "Dataset" }, new String[] { "Run", "Fold" }, null);
		sortDatasets(r);
		r.sortResults("Method", true);
		renameMethods(r);
		System.out.println(r.toNiceString());
		r.clearMergeCountAndVariance();
		r = r.diff("Method", ArrayUtil.toList(new String[] { "Dataset" }), null);
		System.out.println(r.toNiceString());

		ResultSetBoxPlot plot = new ResultSetBoxPlot(r, "", r.getUniqueValue("Method").toString(), "Method",
				ArrayUtil.toList(ValidationResultsProvider.performanceMeasures));
		plot.setHideMean(true);
		plot.setRotateXLabels(false);
		plot.printResultsPerPlot(false);
		plot.setPrintMeanAndStdev(true);

		ChartPanel c = plot.getChart();
		c.setMaximumDrawWidth(10000);
		c.setMaximumDrawHeight(5000);
		c.setMinimumDrawHeight(200);
		Dimension size = new Dimension(450, 250);
		c.setPreferredSize(size);

		if (showCharts)
			SwingUtil.showInFrame(c);

		if (write)
		{
			plot.boxPlotToSVGFile(destFolder + "Chart_Diff_" + alg + paramsStr + ".svg", size);
			toPDF(destFolder + "Chart_Diff_" + alg + paramsStr);
		}
	}

	public void chartFiltering()
	{
		ResultSet r = results.join(new String[] { "Method", "Dataset" }, new String[] { "Run", "Fold" }, null);
		r = r.filter(new ResultSetFilter()
		{
			@Override
			public boolean accept(Result result)
			{
				return result.getValue("Method").toString().contains("1024");
			}
		});
		//System.out.println(r.toNiceString());
		ResultSetLinePlot plot = new ResultSetLinePlot(r, new String[] { "AUC", "Accuracy", "Sensitivity",
				"Selectivity" }, "Method", "Dataset");
		plot.setTitle(null);
		//plot.setRotateXLabels(true);
		plot.setYAxisRange(0.0, 1.0);

		//				{
		//					ResultSet test = results.pairedTTest("Method",
		//							ArrayUtil.toList(new String[] { "Run", "Fold" }), "AUC", SIG, "Dataset");
		//					for (Object datasetWins : ResultSet.listSeriesWins(test, "Method", "AUC", "Dataset",
		//							"RandomForest ecfp_filt_1024", "RandomForest ecfp_fold_1024"))
		//						plot.setDrawShape("AUC", datasetWins.toString(), "RandomForest ecfp_filt_1024");
		//					for (Object datasetWins : ResultSet.listSeriesWins(test, "Method", "AUC", "Dataset",
		//							"RandomForest ecfp_fold_1024", "RandomForest ecfp_filt_1024"))
		//						plot.setDrawShape("AUC", datasetWins.toString(), "RandomForest ecfp_fold_1024");
		//					System.out.println(test.toNiceString());
		//				}

		ChartPanel c = plot.getChartPanel();
		//				c.setMaximumDrawWidth(10000);
		//				c.setMaximumDrawHeight(5000);
		//c.setPreferredSize(new Dimension(800, 600));
		SwingUtil.showInDialog(c);
		if (write)
		{
			plot.toSVGFile(destFolder + "Chart_FiltVsFold_1024_" + alg + ".svg", new Dimension(800, 600));
			toPDF(destFolder + "Chart_FiltVsFold_1024_" + alg);
		}
	}

	public void tableWinLossSizes()
	{
		if (results.getResultValues("Method").getNumValues() != SIZES.length)
			throw new Error("Methods differ in more aspects but sizes " + results.getResultValues("Method").values());

		//reverse sort to have 1024 at the bottom, and therefore on the right side (method_2)
		results.sortResults("Method", false, false, -1);
		String compareAgainstMethod = results.getResultValue(results.getNumResults() - 1, "Method").toString();

		ResultSet t1 = new ResultSet();
		for (String s : ValidationResultsProvider.performanceMeasures)
		{
			ResultSet set = results.pairedTTestWinLoss("Method", ArrayUtil.toList(new String[] { "Run", "Fold" }), s,
					SIG_LEVEL, TEST_CORRECTION, "Dataset", true);
			set.sortResults("Method_1");
			int resCount = 0;
			for (int i = 0; i < set.getNumResults(); i++)
			{
				String method = set.getResultValue(i, "Method_1").toString();
				String method2 = set.getResultValue(i, "Method_2").toString();
				if (!method2.equals(compareAgainstMethod))
					continue;
				if (t1.getNumResults() - 1 < resCount)
					t1.addResult();
				t1.setResultValue(resCount, "Num bits", renameMethod(method, false));//method.substring(method.lastIndexOf("_") + 1));
				t1.setResultValue(resCount, "vs num bits", renameMethod(method2, false));//method.substring(method.lastIndexOf("_") + 1));
				t1.setResultValue(resCount, s, set.getResultValue(i, s));
				resCount++;
			}
		}
		System.out.println(alg + " " + params);
		for (String p : niceValues.keySet())
			t1.setNicePropery(p, niceValues.get(p));
		System.out.println(t1.toNiceString());

		if (write)
		{
			String dest = destFolder + "Table_WinLoss_Size_" + alg + paramsStr + ".tex";
			System.out.println("write table to " + dest);
			FileUtil.writeStringToFile(dest, t1.toLatexTable());
			//			System.out.println(t1.toLatexTable());
		}
	}

	public String fpFromMethod(String m)
	{
		for (String t : new String[] { "e", "f" })
			for (String s : new String[] { "0", "2", "4", "6" })
			{
				String type = t + "cfp" + s;
				if (m.contains(type))
					return type;
				if (params.contains(type))
					return type;
			}
		throw new IllegalArgumentException(m);
	}

	public static String sizeFromMethod(String m)
	{
		for (String s : SIZES)
			if (m.contains(s))
				return s;
		if (m.contains("all"))
			return null;
		throw new IllegalArgumentException(m);
	}

	enum WinLossCompareAgainst
	{
		sameSize, sameDiameter, lastProp, all
	}

	public void tableWinLoss(String name, WinLossCompareAgainst cmp, final String... props)
	{
		// sort to get last element in props on the right side of the win loss table
		results.sortResults("Method", new Comparator<Object>()
		{
			@Override
			public int compare(Object o1, Object o2)
			{
				String s1 = o1.toString();
				String s2 = o2.toString();
				Integer i1 = ArrayUtil.containsAt(s1, props);
				Integer i2 = ArrayUtil.containsAt(s2, props);
				return i1.compareTo(i2);
			}
		});

		ResultSet t1 = new ResultSet();
		for (String s : ValidationResultsProvider.performanceMeasures)
		{
			ResultSet set = results.pairedTTestWinLoss("Method", ArrayUtil.toList(new String[] { "Run", "Fold" }), s,
					SIG_LEVEL, TEST_CORRECTION, "Dataset", true);
			//			set.sortResults("Method_1");

			int resCount = 0;
			for (int i = 0; i < set.getNumResults(); i++)
			{
				String method = set.getResultValue(i, "Method_1").toString();
				String size = sizeFromMethod(method);
				String type = fpFromMethod(method);
				String method2 = set.getResultValue(i, "Method_2").toString();
				String size2 = sizeFromMethod(method2);
				String type2 = fpFromMethod(method2);
				if (ArrayUtil.containsAt(method, props) == -1 || ArrayUtil.containsAt(method2, props) == -1)
					continue;
				if (cmp == WinLossCompareAgainst.sameDiameter
						&& StringUtil.lastChar(type) != StringUtil.lastChar(type2))
					continue;
				if (cmp == WinLossCompareAgainst.lastProp && !method2.contains(ArrayUtil.last(props)))
					continue;
				if (cmp == WinLossCompareAgainst.sameSize && !size.equals(size2))
					continue;
				if (t1.getNumResults() - 1 < resCount)
					t1.addResult();
				t1.setResultValue(resCount, "Method", renameMethod(method, cmp == WinLossCompareAgainst.sameSize));
				t1.setResultValue(resCount, "vs Method", renameMethod(method2, cmp == WinLossCompareAgainst.sameSize));
				if (cmp == WinLossCompareAgainst.sameSize)
					t1.setResultValue(resCount, "hashfoldSize", size);
				t1.setResultValue(resCount, s, set.getResultValue(i, s));
				resCount++;
			}
		}
		System.out.println();
		System.out.println(name + " " + alg + " " + params);
		for (String p : niceValues.keySet())
			t1.setNicePropery(p, niceValues.get(p));
		System.out.println(t1.toNiceString());

		if (write)
		{
			String dest = destFolder + "Table_WinLoss_" + name + "_" + alg + paramsStr + ".tex";
			System.out.println("write table to " + dest);
			FileUtil.writeStringToFile(dest, t1.toLatexTable());
		}
	}

	//	public void tableWinLossSize(final String featureSelection)
	//	{
	//		ResultSet set2 = results.filter(new ResultSetFilter()
	//		{
	//			@Override
	//			public boolean accept(Result result)
	//			{
	//				return result.getValue("Method").toString().contains(featureSelection);
	//			}
	//		});
	//		//reverse sort to have 1024 at the bottom, and therefore on the right side (method_2)
	//		set2.sortResults("Method", false, false, -1);
	//
	//		ResultSet t1 = new ResultSet();
	//		for (String s : ValidationResultsProvider.performanceMeasures)
	//		{
	//			ResultSet set = ValidationResultsProvider.ttest(set2, s, featureSelection, false);
	//			set = set.filter(new ResultSetFilter()
	//			{
	//				@Override
	//				public boolean accept(Result result)
	//				{
	//					return result.getValue("Method_2").toString().contains("1024");
	//				}
	//			});
	//			set.sortResults("Method_1");
	//
	//			for (int i = 0; i < set.getNumResults(); i++)
	//			{
	//				String method = set.getResultValue(i, "Method_1").toString();
	//				if (!method.contains(featureSelection))
	//					throw new IllegalStateException();
	//				if (t1.getNumResults() <= i)
	//					t1.addResult();
	//				t1.setResultValue(i, "hashfoldSize", method.substring(method.lastIndexOf("_") + 1));
	//				t1.setResultValue(i, s, set.getResultValue(i, s));
	//			}
	//		}
	//		System.out.println(featureSelection + ": <Size> vs 1024 " + alg);
	//		for (String p : niceValues.keySet())
	//			t1.setNicePropery(p, niceValues.get(p));
	//		System.out.println(t1.toNiceString());
	//		//
	//		FileUtil.writeStringToFile(destFolder + "Table_WinLoss_Size_" + featureSelection + "_" + alg + ".tex",
	//				t1.toLatexTable());
	//		//		System.out.println(t1.toLatexTable());
	//	}

	public void tableDetailed(String name, String[] paramKeys1, String[] paramKeys2)
	{
		ResultSet r = results.join(new String[] { "Method", "Dataset" }, new String[] { "Run", "Fold" },
				ValidationResultsProvider.performanceMeasures);
		//r.sortResults("Method");
		//System.out.println(r.toNiceString());

		List<String> datasets = ListUtil.cast(String.class, r.getResultValues("Dataset").values());
		for (String m : ValidationResultsProvider.performanceMeasures)
		{
			ResultSet test = results.pairedTTest("Method", ArrayUtil.toList(new String[] { "Run", "Fold" }), m,
					SIG_LEVEL, TEST_CORRECTION, "Dataset");

			System.out.println(m);

			ResultSet t2 = new ResultSet();
			String preProp = " ";
			//			List<Boolean> centerColumn = new ArrayList<>();
			List<Boolean> hlineLeadingColumn = new ArrayList<>();
			for (String d : datasets)
				t2.setResultValue(t2.addResult(), "Dataset", d);
			//			centerColumn.add(false);
			hlineLeadingColumn.add(false);

			for (String k1 : paramKeys1)
			{
				preProp += "& \\multicolumn{" + paramKeys2.length + "}{c}{"
						+ (niceValues.containsKey(k1) ? niceValues.get(k1) : k1) + "} ";

				for (String k2 : paramKeys2)
				{
					String p = k1 + " " + k2;

					for (int i = 0; i < r.getNumResults(); i++)
					{
						String method = r.getResultValue(i, "Method").toString();
						String dataset = r.getResultValue(i, "Dataset").toString();
						Double val = (Double) r.getResultValue(i, m);
						Double var = (Double) r.getVariance(i, m);
						if (method.contains(k1) && method.contains(k2))
						{
							if (!t2.hasProperty(p))
							{
								//								centerColumn.add(true);
								hlineLeadingColumn.add(k2.equals(paramKeys2[0]));
							}

							String value = Math.round((Double) val * 100) + "+-" + Math.round((Double) var * 100);

							if (!k2.equals(paramKeys2[0]))
							{
								String method2 = method.replaceAll(k2, paramKeys2[0]);
								Boolean win = ResultSet.isWinOrLoss(test, "Method", method, method2, m, "Dataset",
										dataset);
								if (win != null)
								{
									if (win)
										value += Character.toString((char) 0x2022);
									else
										value += Character.toString((char) 0x25E6);
								}
							}

							t2.setResultValue(datasets.indexOf(dataset), p, value);
							t2.setNicePropery(p, niceValues.containsKey(k2) ? niceValues.get(k2) : k2);
						}
					}
				}
			}
			preProp += "\\\\";
			System.out.println(preProp);
			System.out.println(t2.toNiceString());

			if (write)
			{
				String dest = destFolder + "Table_" + name + "_" + alg + paramsStr + "_" + m + ".tex";
				System.out.println("write table to " + dest);
				FileUtil.writeStringToFile(dest, t2.toLatexTable(null, hlineLeadingColumn, preProp));
			}
		}
	}

	//	public void tableDetailedFiltering()
	//	{
	//		ResultSet r = results.join(new String[] { "Method", "Dataset" }, new String[] { "Run", "Fold" },
	//				ValidationResultsProvider.performanceMeasures);
	//		//r.sortResults("Method");
	//		//System.out.println(r.toNiceString());
	//
	//		List<String> datasets = ListUtil.cast(String.class, r.getResultValues("Dataset").values());
	//		for (String m : ValidationResultsProvider.performanceMeasures)
	//		{
	//			ResultSet test = results.pairedTTest("Method", ArrayUtil.toList(new String[] { "Run", "Fold" }), m, SIG,
	//					"Dataset");
	//
	//			System.out.println(m);
	//
	//			ResultSet t2 = new ResultSet();
	//			String preProp = " ";
	//			//			List<Boolean> centerColumn = new ArrayList<>();
	//			List<Boolean> hlineLeadingColumn = new ArrayList<>();
	//			for (String d : datasets)
	//				t2.setResultValue(t2.addResult(), "Dataset", d);
	//			//			centerColumn.add(false);
	//			hlineLeadingColumn.add(false);
	//
	//			for (int s : sizes)
	//			{
	//				preProp += "& \\multicolumn{2}{c}{" + s + "} ";
	//
	//				for (String f : new String[] { "fold", "filt" })
	//				{
	//					for (int i = 0; i < r.getNumResults(); i++)
	//					{
	//						String method = r.getResultValue(i, "Method").toString();
	//						String dataset = r.getResultValue(i, "Dataset").toString();
	//						Double val = (Double) r.getResultValue(i, m);
	//						Double var = (Double) r.getVariance(i, m);
	//						if (method.contains(s + "") && method.contains(f + ""))
	//						{
	//							if (!t2.hasProperty(f + " " + s))
	//							{
	//								//								centerColumn.add(true);
	//								hlineLeadingColumn.add(f.equals("fold"));
	//							}
	//
	//							String value = Math.round((Double) val * 100) + "+-" + Math.round((Double) var * 100);
	//
	//							if (f.equals("filt"))
	//							{
	//								String method2 = method.replaceAll("filt", "fold");
	//								Boolean win = ResultSet.isWinOrLoss(test, "Method", method, method2, m, "Dataset",
	//										dataset);
	//								if (win != null)
	//								{
	//									if (win)
	//										value += Character.toString((char) 0x2022);
	//									else
	//										value += Character.toString((char) 0x25E6);
	//								}
	//								//								if (!t2.hasProperty("w " + s))
	//								//								{
	//								//									centerColumn.add(true);
	//								//									hlineLeadingColumn.add(false);
	//								//								}
	//								//								t2.setResultValue(datasets.indexOf(dataset), "w " + s, winStr);
	//								//								t2.setNicePropery("w " + s, "w");
	//							}
	//
	//							t2.setResultValue(datasets.indexOf(dataset), f + " " + s, value);
	//							t2.setNicePropery(f + " " + s, f);
	//
	//						}
	//					}
	//				}
	//			}
	//			preProp += "\\\\";
	//			System.out.println(t2.toNiceString());
	//			FileUtil.writeStringToFile(destFolder + "Table_FiltVsFold_" + alg + "_" + m + ".tex",
	//					t2.toLatexTable(null, hlineLeadingColumn, preProp));
	//		}
	//	}
	//
	//	public void tableDetailedSize()
	//	{
	//		ResultSet r = results.join(new String[] { "Method", "Dataset" }, new String[] { "Run", "Fold" },
	//				ValidationResultsProvider.performanceMeasures);
	//		//r.sortResults("Method");
	//		//System.out.println(r.toNiceString());
	//
	//		List<String> datasets = ListUtil.cast(String.class, r.getResultValues("Dataset").values());
	//		for (String m : ValidationResultsProvider.performanceMeasures)
	//		{
	//			ResultSet test = results.pairedTTest("Method", ArrayUtil.toList(new String[] { "Run", "Fold" }), m, SIG,
	//					"Dataset");
	//
	//			System.out.println(m);
	//
	//			ResultSet t2 = new ResultSet();
	//			String preProp = " ";
	//			//			List<Boolean> centerColumn = new ArrayList<>();
	//			List<Boolean> hlineLeadingColumn = new ArrayList<>();
	//			for (String d : datasets)
	//				t2.setResultValue(t2.addResult(), "Dataset", d);
	//			//			centerColumn.add(false);
	//			hlineLeadingColumn.add(false);
	//
	//			for (String f : new String[] { "fold", "filt" })
	//			{
	//				preProp += "& \\multicolumn{4}{c}{" + f + "} ";
	//				for (int s : sizes)
	//				{
	//
	//					for (int i = 0; i < r.getNumResults(); i++)
	//					{
	//						String method = r.getResultValue(i, "Method").toString();
	//						String dataset = r.getResultValue(i, "Dataset").toString();
	//						Double val = (Double) r.getResultValue(i, m);
	//						Double var = (Double) r.getVariance(i, m);
	//						if (method.contains(s + "") && method.contains(f + ""))
	//						{
	//							if (!t2.hasProperty(f + " " + s))
	//							{
	//								//								centerColumn.add(true);
	//								hlineLeadingColumn.add(s == 1024);
	//							}
	//
	//							String value = Math.round((Double) val * 100) + "+-" + Math.round((Double) var * 100);
	//
	//							if (s > 1024)
	//							{
	//								String method2 = method.replaceAll(s + "", "1024");
	//								Boolean win = ResultSet.isWinOrLoss(test, "Method", method, method2, m, "Dataset",
	//										dataset);
	//								if (win != null)
	//								{
	//									if (win)
	//										value += Character.toString((char) 0x2022);
	//									else
	//										value += Character.toString((char) 0x25E6);
	//								}
	//								//								if (!t2.hasProperty("w " + s))
	//								//								{
	//								//									centerColumn.add(true);
	//								//									hlineLeadingColumn.add(false);
	//								//								}
	//								//								t2.setResultValue(datasets.indexOf(dataset), "w " + s, winStr);
	//								//								t2.setNicePropery("w " + s, "w");
	//							}
	//
	//							t2.setResultValue(datasets.indexOf(dataset), f + " " + s, value);
	//							t2.setNicePropery(f + " " + s, s + "");
	//
	//						}
	//					}
	//				}
	//			}
	//			preProp += "\\\\";
	//			System.out.println(preProp);
	//			System.out.println(t2.toNiceString());
	//
	//			FileUtil.writeStringToFile(destFolder + "Table_Size_" + alg + "_" + m + ".tex",
	//					t2.toLatexTable(null, hlineLeadingColumn, preProp));
	//		}
	//	}

	private static void toPDF(String svgFileWithoutExtension)
	{
		new ExternalTool(null).run("to-pdf", ("rsvg-convert -f pdf -o " + svgFileWithoutExtension + ".pdf "
				+ svgFileWithoutExtension + ".svg").split(" "));
	}

	public static void main(String[] args) throws Exception
	{
		new CreatePaperResults(args.length > 0 && args[0].equals("debug"));
		System.exit(0);
	}
}
