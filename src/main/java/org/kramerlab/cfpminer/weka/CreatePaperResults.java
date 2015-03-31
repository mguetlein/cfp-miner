package org.kramerlab.cfpminer.weka;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.jfree.chart.ChartPanel;
import org.mg.javalib.datamining.Result;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.datamining.ResultSetFilter;
import org.mg.javalib.datamining.ResultSetLinePlot;
import org.mg.javalib.io.ExternalTool;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.FileUtil;
import org.mg.javalib.util.ListUtil;
import org.mg.javalib.util.SwingUtil;

public class CreatePaperResults
{
	static double SIG_LEVEL = 0.05;
	static String[] sizes = { "1024", "2048", "4096", "8192" };
	static HashMap<String, String> niceValues = new HashMap<>();
	static
	{
		niceValues.put("hashfoldSize", "Num bits");
		niceValues.put("RaF", "Random forest");
		ValidationResultsProvider.setPerformanceMeasures(new String[] { "AUC" });
	}
	static String destFolder = "/home/martin/documents/ecfps/latex/results/";

	String alg;
	//ValidationResultsProvider res;
	ResultSet results;

	public CreatePaperResults() throws Exception
	{
		Locale.setDefault(Locale.US);

		for (String alg : new String[] { "RaF", "SMO" })
		{
			this.alg = alg;
			ValidationResultsProvider valRes = new ValidationResultsProvider(
					"/home/martin/workspace/CFPMiner/results_subset_r2/" + alg + ".arff");
			results = valRes.results.filter(new ResultSetFilter()
			{
				@Override
				public boolean accept(Result result)
				{
					return result.getValue("Method").toString().contains("ecfp6");
					//return true;
				}
			});

			//			ResultSet r = results.join(new String[] { "Method", "Dataset" }, new String[] { "Run", "Fold" },
			//					ValidationResultsProvider.performanceMeasures);
			//			r = r.rank("AUC", new String[] { "Dataset" });
			//			r.clearMergeCountAndVariance();
			//			r = r.join("Method");
			//			r.removePropery("Dataset");
			//			r.sortResults("AUC" + ResultSet.RANK_SUFFIX, true, true, -1);
			//			System.out.println(r.toNiceString());

			tableWinLossFiltering();
			//			tableDetailedFiltering();
			//			chartFiltering();
			tableWinLossSize("fold");
			tableWinLossSize("filt");
			//			tableDetailedSize();
			//			chartSize("fold");
			tableDetailed("FiltVsFold", new String[] { "fold", "filt" }, sizes);
			tableDetailed("Size", sizes, new String[] { "fold", "filt" });
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
			//			tableDetailed("ECFP-Diameter", sizes, new String[] { "ecfp6", "ecfp4", "ecfp2" });

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
		plot.setRotateXLabels(true);
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

		plot.toSVGFile(destFolder + "Chart_FiltVsFold_1024_" + alg + ".svg", new Dimension(800, 600));
		toPDF(destFolder + "Chart_FiltVsFold_1024_" + alg);

	}

	public void tableWinLossSize(final String featureSelection)
	{
		ResultSet set2 = results.filter(new ResultSetFilter()
		{
			@Override
			public boolean accept(Result result)
			{
				return result.getValue("Method").toString().contains(featureSelection);
			}
		});
		//reverse sort to have 1024 at the bottom, and therefore on the right side (method_2)
		set2.sortResults("Method", false, false, -1);

		ResultSet t1 = new ResultSet();
		for (String s : ValidationResultsProvider.performanceMeasures)
		{
			ResultSet set = ValidationResultsProvider.ttest(set2, s, featureSelection, false);
			set = set.filter(new ResultSetFilter()
			{
				@Override
				public boolean accept(Result result)
				{
					return result.getValue("Method_2").toString().contains("1024");
				}
			});
			set.sortResults("Method_1");

			for (int i = 0; i < set.getNumResults(); i++)
			{
				String method = set.getResultValue(i, "Method_1").toString();
				if (!method.contains(featureSelection))
					throw new IllegalStateException();
				if (t1.getNumResults() <= i)
					t1.addResult();
				t1.setResultValue(i, "hashfoldSize", method.substring(method.lastIndexOf("_") + 1));
				t1.setResultValue(i, s, set.getResultValue(i, s));
			}
		}
		System.out.println(featureSelection + ": <Size> vs 1024 " + alg);
		for (String p : niceValues.keySet())
			t1.setNicePropery(p, niceValues.get(p));
		System.out.println(t1.toNiceString());
		//
		FileUtil.writeStringToFile(destFolder + "Table_WinLoss_Size_" + featureSelection + "_" + alg + ".tex",
				t1.toLatexTable());
		//		System.out.println(t1.toLatexTable());
	}

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
					SIG_LEVEL, "Dataset");

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
				preProp += "& \\multicolumn{" + paramKeys2.length + "}{c}{" + k1 + "} ";

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
							t2.setNicePropery(p, k2);
						}
					}
				}
			}
			preProp += "\\\\";
			System.out.println(preProp);
			System.out.println(t2.toNiceString());
			FileUtil.writeStringToFile(destFolder + "Table_" + name + "_" + alg + "_" + m + ".tex",
					t2.toLatexTable(null, hlineLeadingColumn, preProp));
			break;
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
		new CreatePaperResults();
		System.exit(0);
	}
}
