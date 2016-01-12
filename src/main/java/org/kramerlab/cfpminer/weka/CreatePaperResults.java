package org.kramerlab.cfpminer.weka;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.kramerlab.cfpminer.CFPTest;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.DataLoader;
import org.mg.javalib.datamining.Result;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.datamining.ResultSetBoxPlot;
import org.mg.javalib.datamining.ResultSetFilter;
import org.mg.javalib.datamining.ResultSetIO;
import org.mg.javalib.datamining.ResultSetLinePlot;
import org.mg.javalib.datamining.WekaResultSetUtil;
import org.mg.javalib.freechart.WinLossBarChart;
import org.mg.javalib.io.ExternalTool;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.DoubleKeyHashMap;
import org.mg.javalib.util.FileUtil;
import org.mg.javalib.util.ListUtil;
import org.mg.javalib.util.ObjectUtil;
import org.mg.javalib.util.StringUtil;
import org.mg.javalib.util.SwingUtil;
import org.mg.wekalib.data.MergeArffFiles;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class CreatePaperResults
{
	int defaultSize = 1024;

	public void paper() throws Exception
	{
		ResultSet.T_TESTER = WekaResultSetUtil.T_TESTER;

		//		DATASETS = ArrayUtil.toArray(CFPDataLoader.listCategoryDatasets(CFPDataLoader.BALANCED_DATASETS));
		//		DATASETS = ArrayUtil.toArray(CFPDataLoader.listCategoryDatasets(CFPDataLoader.VS_DATASETS));
		//		DATASETS = ArrayUtil.toArray(CFPDataLoader.listSubCategoryDatasets("ChEMBL"));
		//		ArrayUtil.scramble(DATASETS, new Random(3));

		//		ALGORITHMS = new String[] { "NBy" };
		write = true;
		showCharts = false;
		boolean onlyPaper = false;

		//		tableCollisions(false);
		//ranking("", "AUP");
		datasets();

		//		read("ecfp4");
		//		filter("AllOr1024", getFilter(null, null, 1024), getFilter(null, FeatureSelection.none, null));
		//		addRuntimes();
		//		lineChart();
		//		ValidationResultsProvider.setPerformanceMeasures(new String[] { "AUP" });
		//		setParamsStr();
		//		lineChart();

		// ValidationResultsProvider.setPerformanceMeasures(new String[] { "AUC", "AUP" });
		//		Integer size = 1024;
		//		read("ecfp4");
		//		filter("AllOrFold" + size, getFilter(null, FeatureSelection.fold, size),
		//				getFilter(null, FeatureSelection.none, null));
		//		diffChart();
		//		read("ecfp4");
		//		filter("AllOrFold" + size, getFilter(null, FeatureSelection.filt, size),
		//				getFilter(null, FeatureSelection.none, null));
		//		diffChart();
		//		read("ecfp4_" + size);
		//		diffChart();

		{
			//			// LINE - PLOTS
			//			ValidationResultsProvider
			//					.setPerformanceMeasures(new String[] { "Accuracy", "AUC", "AUPRC" });
			//			read("ecfp4_" + defaultSize + "orAll", RemoveObsoleteSizes.no,
			//					getFilter(CFPType.ecfp4, null, null, defaultSize),
			//					getFilter(CFPType.ecfp4, null, FeatureSelection.none, null));
			//			addRuntimes();
			//			lineChartFeatureSelection();
			//			ValidationResultsProvider.setPerformanceMeasures(new String[] { "AUPRC" });
			//			lineChartFeatureSelection();
		}

		{
			//			//			// DIFF - CHARTS
			//			//Integer chartSize = 2048;
			//			for (Integer chartSize : SIZES)
			//			{
			//				Map<FeatureSelection[], Map<String, ChartPanel>> charts = new LinkedHashMap<>();
			//				//int chartSize = 4096;
			//
			//				ValidationResultsProvider.setPerformanceMeasures(new String[] { "AUC", "AUPRC" });
			//				read("ecfp4_" + chartSize, RemoveObsoleteSizes.no,
			//						getFilter(CFPType.ecfp4, null, null, chartSize));
			//				addRuntimes();
			//				charts.put(new FeatureSelection[] { FeatureSelection.filt, FeatureSelection.fold },
			//						diffChart());
			//
			//				ValidationResultsProvider.setPerformanceMeasures(new String[] { "AUC", "AUPRC" });
			//				read("ecfp4_filt" + chartSize + "orAll", RemoveObsoleteSizes.yes_remove_none,
			//						getFilter(CFPType.ecfp4, null, FeatureSelection.filt, chartSize),
			//						getFilter(CFPType.ecfp4, null, FeatureSelection.none, null));
			//				addRuntimes();
			//				charts.put(new FeatureSelection[] { FeatureSelection.filt, FeatureSelection.none },
			//						diffChart());
			//
			//				ValidationResultsProvider.setPerformanceMeasures(new String[] { "AUC", "AUPRC" });
			//				read("ecfp4_fold" + chartSize + "orAll", RemoveObsoleteSizes.yes_remove_none,
			//						getFilter(CFPType.ecfp4, null, FeatureSelection.fold, chartSize),
			//						getFilter(CFPType.ecfp4, null, FeatureSelection.none, null));
			//				addRuntimes();
			//				charts.put(new FeatureSelection[] { FeatureSelection.fold, FeatureSelection.none },
			//						diffChart());
			//
			//				composeDiffCharts("diffCharts_" + chartSize, charts);
			//			}
		}

		{
			// WIN - LOSS - TABLES
			//						 FeatureSelection - ECFP4 1024			
			//			if (onlyPaper)
			//			{
			//				ValidationResultsProvider.setPerformanceMeasures(new String[] { "AUPRC" });
			//				plotWinLossSizes();
			//			}
			//			else
			//			{
			//				ValidationResultsProvider.setPerformanceMeasures(new String[] { "AUC" });
			//				plotWinLossSizes();
			//			}
			//			for (Integer size : SIZES)
			//			{
			//				read("ecfp4_" + size + "orAll", RemoveObsoleteSizes.no, getFilter(CFPType.ecfp4, null, null, size),
			//						getFilter(CFPType.ecfp4, null, FeatureSelection.none, null));
			//				tableWinLoss("FiltVsFoldVsAll", WinLossOption.compareAll, "FeatureSelection", "Algorithm");
			//			}

			// Diameters - ECFP - all & 1024
			//			if (onlyPaper)
			//			{
			//				ValidationResultsProvider.setPerformanceMeasures(new String[] { "AUPRC" });
			//				plotWinLossECFPDiameter(true, true);
			//			}
			//			else
			//			{
			//				ValidationResultsProvider.setPerformanceMeasures(new String[] { "AUPRC", "AUC" });
			//				plotWinLossECFPDiameter(false, true);
			//				plotWinLossECFPDiameter(false, false);
			//			}

			//			read("ecfpAll", RemoveObsoleteSizes.no, getFilter(null, true, FeatureSelection.none, null));
			//			tableWinLoss("Diameters", WinLossOption.compareToDiameter4, "CFPType", "Algorithm");
			//			read("ecfpFilt" + defaultSize, RemoveObsoleteSizes.yes_remove_none,
			//					getFilter(null, true, FeatureSelection.filt, defaultSize));
			//			tableWinLoss("Diameters", WinLossOption.compareToDiameter4, "CFPType", "Algorithm");

			// ECFP vs FCFP - all & 1024
			//			read("all", RemoveObsoleteSizes.no, getFilter(null, null, FeatureSelection.none, null));
			//			tableWinLoss("Type", WinLossOption.compareSameDiameter, "CFPType", "Algorithm");
			//			read("filt" + defaultSize, RemoveObsoleteSizes.yes_remove_none,
			//					getFilter(null, null, FeatureSelection.filt, defaultSize));
			//			tableWinLoss("Type", WinLossOption.compareSameDiameter, "CFPType", "Algorithm");
			//plotWinLossECFPvsFCFP();
			//			if (onlyPaper)
			//			{
			//				ValidationResultsProvider.setPerformanceMeasures(new String[] { "AUPRC" });
			//				plotWinLossECFPvsFCFP(true, true);
			//			}
			//			else
			//			{
			//				ValidationResultsProvider.setPerformanceMeasures(new String[] { "AUPRC", "AUC" });
			//				plotWinLossECFPvsFCFP(false, true);
			//				plotWinLossECFPvsFCFP(false, false);
			//			}

			// FeatureSelection - All sizes, all diameters, ecfp + fcfp
			//			for (Integer size : SIZES)
			//			{
			//				read(size + "orAll", RemoveObsoleteSizes.yes_remove_none, getFilter(null, null, null, size),
			//						getFilter(null, null, FeatureSelection.none, null));
			//				tableWinLoss("FiltVsFoldVsAll", WinLossOption.compareAll, "FeatureSelection", "Algorithm");
			//			}

			// FeatureSelection - All sizes, all diameters, ecfp
			//			for (Integer size : SIZES)
			//			{
			//				read("ecfp" + size + "orAll", RemoveObsoleteSizes.yes_remove_none, getFilter(null, true, null, size),
			//						getFilter(null, true, FeatureSelection.none, null));
			//				tableWinLoss("FiltVsFoldVsAll", WinLossOption.compareAll, "FeatureSelection", "Algorithm");
			//			}

			//			// Algorithm Comparison - Using best param setting per dataset
			//			ValidationResultsProvider.setPerformanceMeasures(ValidationResultsProvider.ORIG_PERFORMANCE_MEASURES);
			//			estimateBestPerAlg("AUP");
			//			ValidationResultsProvider.setPerformanceMeasures(new String[] { "AUC", "AUP" });
			//			tableWinLoss("RF-SMO-NB", WinLossOption.compareAll, "Algorithm");
			//			ValidationResultsProvider.setPerformanceMeasures(new String[] { "AUP" });
			//			lineChartAlgorithm();
		}

		{
			//			estimateBestOverall("AUP");
		}

		//		estimateBest2("AUP");
		//		renameMethods(ALL_ALGS);
		//		tableWinLoss("RF-SMO-RF", WinLossCompareAgainst.all, "RnF", "SMO", "NBy");
		//		lineChart();

		//		for (final String type : new String[] { "ecfp", "fcfp" })
		//			for (final String typeSize : new String[] { "6", "4", "2", "0" })
		//			{
		//				read(type + typeSize);
		//				tableDetailed("FiltVsFold", SIZES, new String[] { "fold", "filt" });
		//				///////tableDetailed("Size", new String[] { "fold", "filt" }, sizes);
		//			}

		//		read("ecfp4_fold");
		//		tableWinLoss("Sizes", WinLossCompareAgainst.lastProp, new String[] { "2048", "4096", "8192", "1024" });
		//		read("ecfp4_filt");
		//		tableWinLoss("Sizes", WinLossCompareAgainst.lastProp, new String[] { "2048", "4096", "8192", "1024" });

		//		for (String size : SIZES)
		//		//		String size = "1024";
		//		{
		//			read("ecfp_fold_" + size);
		//			tableWinLoss("Diameters", WinLossCompareAgainst.all, "ecfp0", "ecfp2", "ecfp4", "ecfp6");
		//		}

		//		read("fold_1024");
		//		tableWinLoss("FCFPvsECFP", WinLossCompareAgainst.sameDiameter, new String[] { "fcfp", "ecfp" });
		//		read("filt_1024");
		//		tableWinLoss("FCFPvsECFP", WinLossCompareAgainst.sameDiameter, new String[] { "fcfp", "ecfp" });

		//		read("1024");
		//		tableWinLoss("ECFPvsFCFP", WinLossCompareAgainst.sameDiameterAndFeat, new String[] { "ecfp", "fcfp" });

	}

	static double SIG_LEVEL = 0.05;
	static Double TEST_CORRECTION = 1 / 9.0;
	static double SIG_LEVEL_RELAXED = 0.1;
	static Double TEST_CORRECTION_RELAXED = null;
	static Integer[] SIZES = { 1024, 2048, 4096, 8192 };
	static String ALL_ALGS = "AllAlgs";
	//	static final HashMap<String, String> WEKA_ALGORITHM_NAMES = new HashMap<>();
	//	{
	//		WEKA_ALGORITHM_NAMES.put("RandomForest", "RaF");
	//		WEKA_ALGORITHM_NAMES.put("RandomFores2", "RnF");
	//		WEKA_ALGORITHM_NAMES.put("SMO", "SMO");
	//		WEKA_ALGORITHM_NAMES.put("NaiveBayes", "NBy");
	//		WEKA_ALGORITHM_NAMES.put("Vote", "Ens");
	//	}

	// all there is
	static String[] ORIG_ALGORITHMS = new String[] { "RnF", "SMO", "NBy" }; //, "Ens" };
	static String[] ORIG_ALGORITHMS_NICE = new String[ORIG_ALGORITHMS.length];
	// selected for this run
	static String[] ALGORITHMS = ORIG_ALGORITHMS;
	static String[] ALL_DATASETS = new DataLoader("data").allDatasets();
	static String[] DATASETS = ALL_DATASETS;
	static String[] ALL_PERFORMANCE_MEASURES = ValidationResultsProvider.performanceMeasures;

	static HashMap<String, String> niceValues = new HashMap<>();
	static HashMap<String, String> niceValuesShort = new HashMap<>();

	static
	{
		niceValues.put("hashfoldSize", "Num bits");
		niceValues.put("FeatureSelection", "Feature selection");
		niceValues.put("CFPType", "Fingerprint");
		niceValues.put("RaF", "Random forests");
		niceValues.put("RnF", "Random forests");
		niceValues.put("SMO", "Support vector machines");
		niceValues.put("NBy", "Naive Bayes");
		for (FeatureSelection f : FeatureSelection.values())
			if (!f.toString().equals(f.toNiceString()))
				niceValues.put(f.toString(), f.toNiceString());
		for (CFPType t : CFPType.values())
			niceValues.put(t.toString(), t.toNiceString());

		for (String k : niceValues.keySet())
			niceValuesShort.put(k, niceValues.get(k));
		for (FeatureSelection f : FeatureSelection.values())
			niceValuesShort.put(f.toString(), f.toNiceShortString());
		niceValuesShort.put("RaF", "RF");
		niceValuesShort.put("RnF", "RF");
		niceValuesShort.put("SMO", "SMV");
		niceValuesShort.put("NBy", "NB");
		niceValuesShort.put("CFPType", "FP");
		niceValuesShort.put("hashfoldSize", "Size");
		niceValuesShort.put("FeatureSelection", "FS");

		for (int i = 0; i < ORIG_ALGORITHMS.length; i++)
			ORIG_ALGORITHMS_NICE[i] = niceValues.get(ORIG_ALGORITHMS[i]);
		//ValidationResultsProvider.setPerformanceMeasures(new String[] { "Accuracy" });
	}

	static String destFolder = "/home/martin/documents/ecfps/latex/results/";

	//	String params = "";
	//	String paramsStr = "";
	//	String filterName = "";
	//ValidationResultsProvider res;
	String setName;

	LinkedHashMap<String, ResultSet> algResults = new LinkedHashMap<>();
	boolean write = false;
	boolean showCharts = true;

	public void addRuntimes()
	{
		if (ArrayUtil.indexOf(ValidationResultsProvider.performanceMeasures, "Time") == -1)
			ValidationResultsProvider.setPerformanceMeasures(
					ArrayUtil.push(ValidationResultsProvider.performanceMeasures, "Time"));
		for (String alg : algResults.keySet())
		{
			String algo = null;
			if (!alg.equals(ALL_ALGS))
				algo = alg;
			ResultSet r = algResults.get(alg);
			for (int i = 0; i < r.getNumResults(); i++)
			{
				String a = algo != null ? algo : r.getResultValue(i, "Algorithm").toString();
				String d = r.getResultValue(i, "Dataset").toString();
				CFPType t = type(r, i);
				FeatureSelection f = feat(r, i);
				Integer s = size(r, i);
				double run = CFPTest.getRuntime(d, t, f, s == null ? 0 : s, a, true);
				r.setResultValue(i, "Time", run);// 5 + new Random().nextDouble() * 20.0);
			}
		}
	}

	public void read(String setName, RemoveObsoleteSizes rem, ResFilter... filter) throws Exception
	{
		this.setName = setName;
		algResults.clear();

		read(ALGORITHMS, rem, filter);

		System.out.print("merging algs .. ");
		for (String alg : ALGORITHMS)
		{
			if (algResults.containsKey(ALL_ALGS))
				algResults.get(ALL_ALGS).concat(algResults.get(alg));
			else
				algResults.put(ALL_ALGS, algResults.get(alg).copy());
		}
		System.out.println("done");
	}

	//	private void setParamsStr()
	//	{
	//		paramsStr = params.isEmpty() ? "" : ("_" + params);
	//		if (DATASETS.length < ALL_DATASETS.length)
	//			paramsStr += "_d" + DATASETS.length;
	//		if (ValidationResultsProvider.performanceMeasures.length < ALL_PERFORMANCE_MEASURES.length)
	//			paramsStr += "_m" + ValidationResultsProvider.performanceMeasures.length;
	//	}

	enum RemoveObsoleteSizes
	{
		yes_remove_none, yes_retain_none, no
	}

	public void read(String algs[], final RemoveObsoleteSizes rem, ResFilter... filter)
			throws Exception
	{
		if (filter != null && filter.length > 0 && filter[0] instanceof CompFilter)
			throw new IllegalArgumentException();
		CompFilter filt = null;
		String fKey = "";
		if (filter != null && filter.length > 0)
		{
			filt = new CompFilter(filter);
			fKey = "." + filt.getKey();
		}

		for (String alg : algs)
		{
			//			this.params = params;
			//			setParamsStr();
			//			filterName = "";
			//			String name = alg + (params.isEmpty() ? "" : ("_" + params));
			System.out.print("reading " + alg + " .. ");

			File f = new File(ValidationResultsProvider.RESULTS_MERGED_FOLDER + alg + fKey + "."
					+ ArrayUtil.toString(ValidationResultsProvider.performanceMeasures, "-", "", "",
							"")
					+ ".res-cache");
			ResultSet res;
			if (f.exists())
			{
				System.out.print("cached " + fKey + " .. ");
				res = ResultSetIO.readFromFile(f);
			}
			else
			{
				ValidationResultsProvider valRes = new ValidationResultsProvider(filt,
						ValidationResultsProvider.RESULTS_MERGED_FOLDER + alg + ".arff");
				res = valRes.results;
				ResultSetIO.writeToFile(f, res);
			}

			if (res.getNumResults() == 0)
				throw new IllegalStateException("nothing there");

			res = res.filter(new ResultSetFilter()
			{
				@Override
				public boolean accept(Result result)
				{
					return ArrayUtil.indexOf(DATASETS, result.getValue("Dataset").toString()) != -1;
				}
			});

			if (rem != RemoveObsoleteSizes.no)
				res = res.filter(new ResultSetFilter()
				{
					@Override
					public boolean accept(Result result)
					{
						FeatureSelection feat = feat(result);
						if (feat == FeatureSelection.none)
						{
							if (rem == RemoveObsoleteSizes.yes_retain_none)
								return true;
							else
								return !skipFiltFoldMethod(type(result), 1024);
						}
						else
							return !skipFiltFoldMethod(type(result), size(result));
					}
				});

			if (res.getNumResults() == 0)
				throw new IllegalStateException("nothing left");

			int num = res.getNumResults()
					/ (DATASETS.length * res.getResultValues("Fold").getNumValues()
							* res.getResultValues("Run").getNumValues());

			System.out.println("done (" + num + " vals per dataset+fold+run)");

			algResults.put(alg, res);
		}
	}

	public boolean skipFiltFoldMethod(CFPType type, Integer size)
	{
		if (type == CFPType.ecfp0 || type == CFPType.fcfp0 || type == CFPType.fcfp2)
			return true;
		if (type == CFPType.ecfp2)
			return size > 1024;
		if (type == CFPType.fcfp4)
			return size > 4096;
		return false;
	}

	public void renameResultValues(ResultSet results)
	{
		renameResultValues(results, false);
	}

	public void renameResultValues(ResultSet results, boolean shortValues)
	{
		for (int i = 0; i < results.getNumResults(); i++)
		{
			if (results.getResultValue(i, "Dataset") != null)
			{
				String d = results.getResultValue(i, "Dataset").toString();
				results.setResultValue(i, "Dataset", d.replaceAll("_", " "));
			}

			for (String s : new String[] { "Algorithm", "CFPType", "FeatureSelection",
					"FeatureSelection_1", "FeatureSelection_2" })
			{
				if (results.hasProperty(s))
				{
					String v = results.getResultValue(i, s).toString();
					if (shortValues)
						results.setResultValue(i, s, niceValuesShort.get(v));
					else
						results.setResultValue(i, s, niceValues.get(v));
				}
			}
		}
	}

	public void sortDatasets(ResultSet r)
	{
		r.sortResults("Dataset", DataLoader.CFPDataComparator);
	}

	//	public void renameMethods(String alg)
	//	{
	//		renameMethods(alg, algResults.get(alg));
	//	}
	//
	//	public void renameMethods(String alg, ResultSet results)
	//	{
	//		Boolean stripSize = oneSize(results);
	//		for (int i = 0; i < results.getNumResults(); i++)
	//		{
	//			String method = results.getResultValue(i, "Method").toString();
	//			results.setResultValue(i, "Method", renameMethod(alg, method, stripSize, false));
	//		}
	//	}
	//
	//	public String renameMethod(String alg, String method)
	//	{
	//		return renameMethod(alg, method, false, false);
	//	}
	//
	//	public String renameMethod(String alg, String method, boolean stripSize, boolean stripFeatureSelection)
	//	{
	//		if (alg != null)
	//		{
	//			if (alg.equals(ALL_ALGS))
	//				return algFromMethod(method);
	//			//				return niceValues.get(algFromMethod(method));
	//			for (String w : WEKA_ALGORITHM_NAMES.keySet())
	//				method = method.replace(w + " ", "");
	//		}
	//		if (params != null)
	//		{
	//			for (String p : params.split("_"))
	//				if (!p.equals("ecfp") && !p.equals("fcfp"))
	//					method = method.replace(p, "");
	//		}
	//		if (stripFeatureSelection)
	//		{
	//			for (FeatureSelection s : FeatureSelection.values())
	//			{
	//				method = method.replace(s.toString(), "");
	//			}
	//		}
	//		if (stripSize)
	//		{
	//			for (String size : SIZES)
	//			{
	//				method = method.replace(size, "");
	//			}
	//		}
	//		for (String k : niceValues.keySet())
	//		{
	//			method = method.replace(k, niceValues.get(k));
	//		}
	//		method = method.replace("__", "_");
	//		method = method.replace("_", " ");
	//		return method.trim();
	//	}

	public static void merge(final String alg, final String p, final String p2)
			throws FileNotFoundException, IOException
	{
		//		final boolean orP2 = false;

		//csvToArff("/home/martin/data/arffs/nctrer.csv", "/home/martin/data/arffs/nctrer.arff");
		String dir = ValidationResultsProvider.RESULTS_FOLDER;
		String dest = ValidationResultsProvider.RESULTS_MERGED_FOLDER + alg
				+ (p == null ? "" : ("_" + p)) + (p2 == null ? "" : ("_" + p2)) + ".arff";

		if (!new File(dest).exists())
			MergeArffFiles.merge(dir, new FilenameFilter()
			{

				@Override
				public boolean accept(File dir, String name)
				{
					if (!name.contains(alg))
						return false;

					if (p != null && (p.equals(DataLoader.BALANCED_DATASETS) || p.equals("MUV")
							|| p.equals("ChEMBL") || p.equals("DUD")))
					{
						final Set<String> cat = DataLoader.listCategoryDatasets(p);
						if (p2 != null)
							throw new IllegalArgumentException();
						for (String d : cat)
							if (name.contains(d))
								return true;
					}

					//				if (orP2)
					//					return ((p == null || name.contains(p)) || (p2 == null || name.contains(p2)));
					//				else
					return ((p == null || name.contains(p)) && (p2 == null || name.contains(p2)));
				}
			}, dest);
	}

	public void merge() throws Exception
	{
		for (String alg : ALGORITHMS)
		{
			merge(alg, null, null);

			//			merge(alg, "CPDBAS_Rat", null);
			//
			//			merge(alg, "1024", null);
			//			merge(alg, "ecfp", null);
			//			merge(alg, "ecfp", "none");
			//			merge(alg, "ecfp4", "1024");
			//			merge(alg, "ecfp4", "2048");
			//			merge(alg, "ecfp", "1024");
			//			merge(alg, "ecfp", "2048");
			//
			//			merge(alg, "fcfp", "none");
			//			merge(alg, "fcfp", "1024");
			//			merge(alg, "fcfp", "2048");
			//
			//			merge(alg, CFPDataLoader.BALANCED_DATASETS, null);
			//			merge(alg, "MUV", null);
			//			merge(alg, "DUD", null);
			//			merge(alg, "ChEMBL", null);
			//
			//			for (String feat : new String[] { "fold", "filt" })
			//			{
			//				merge(alg, feat, null);
			//				merge(alg, "ecfp", feat);
			//				merge(alg, "ecfp4", feat);
			//				merge(alg, feat, "1024");
			//			}
			//
			//			for (String dataset : new String[] { "AMES" })
			//			{
			//				merge(alg, dataset, null);
			//			}
			//
			//			for (String type : new String[] { "ecfp", "fcfp" })
			//			{
			//
			//				for (String typeSize : new String[] { "6", "4", "2", "0" })
			//				{
			//					String p = type + typeSize;
			//					String p2 = null;
			//					merge(alg, p, p2);
			//				}
			//			}
			//			for (String type : new String[] { "ecfp", "fcfp" })
			//			{
			//				String p = type;
			//				for (Integer size : SIZES)
			//				{
			//					String p2 = "fold_" + size;
			//					merge(alg, p, p2);
			//				}
			//			}
		}
	}

	//	// delete ecfp0/fcfp0/fcfp2 1024/2048/4096/8192
	//	// delete ecfp2 2048/4096/8192
	//	// delete fcfp4 8192
	//	public void remove_obs_sizes(String alg, final boolean retainAll)
	//	{
	//		ResultSet results = algResults.get(alg);
	//		results = results.filter(new ResultSetFilter()
	//		{
	//			@Override
	//			public boolean accept(Result result)
	//			{
	//				FeatureSelection feat = feat(result);
	//				if (feat == FeatureSelection.none)
	//				{
	//					if (retainAll)
	//						return true;
	//					else
	//						return !skipFiltFoldMethod(type(result), 1024);
	//				}
	//				else
	//					return !skipFiltFoldMethod(type(result), size(result));
	//			}
	//		});
	//		//		for (int i = 0; i < results.getNumResults(); i++)
	//		//		{
	//		//			String m = results.getResultValue(i, "Method").toString();
	//		//			if (m.contains("ecfp0_filt_1024"))
	//		//				results.setResultValue(i, "Method", m.replace("ecfp0_filt_1024", "ecfp0_" + FeatureSelection.none));
	//		//			if (m.contains("fcfp0_filt_1024"))
	//		//				results.setResultValue(i, "Method", m.replace("fcfp0_filt_1024", "fcfp0_" + FeatureSelection.none));
	//		//			if (m.contains("fcfp2_filt_1024"))
	//		//				results.setResultValue(i, "Method", m.replace("fcfp2_filt_1024", "fcfp2_" + FeatureSelection.none));
	//		//			if (m.contains("ecfp2_filt_8192"))
	//		//				results.setResultValue(i, "Method", m.replace("ecfp2_filt_8192", "ecfp2_" + FeatureSelection.none));
	//		//			if (m.contains("fcfp4_filt_8192"))
	//		//				results.setResultValue(i, "Method", m.replace("fcfp4_filt_8192", "fcfp4_" + FeatureSelection.none));
	//		//		}
	//		if (results.getNumResults() == 0)
	//			throw new IllegalStateException("nothing left");
	//		algResults.put(alg, results);
	//	}

	public CreatePaperResults(boolean debug) throws Exception
	{
		Locale.setDefault(Locale.US);
		merge();
		paper();
	}

	public void datasets()
	{
		DataLoader d = new DataLoader("data");
		String datasets[] = d.allDatasets();
		Arrays.sort(datasets, 0, datasets.length, DataLoader.CFPDataComparator);

		{
			ResultSet r = d.getInfo(datasets);
			for (int idx = 0; idx < r.getNumResults(); idx++)
				r.setResultValue(idx, "name",
						r.getResultValue(idx, "name").toString().replaceAll("_", " "));
			r.removePropery("source");
			System.out.println(r.toNiceString());
			if (write)
			{
				String dest = destFolder + "datasets.tex";
				System.out.println("write table to " + dest);
				FileUtil.writeStringToFile(dest, r.toLatexTable());
			}
			System.out.println();
		}

		//		{
		//			ResultSet r = d.getCategoryInfo(datasets);
		//			System.out.println(r.toNiceString());
		//			if (write)
		//			{
		//				String dest = destFolder + "datasets_overview.tex";
		//				System.out.println("write table to " + dest);
		//				FileUtil.writeStringToFile(dest, r.toLatexTable());
		//			}
		//		}
	}

	/** leaves only a single method per algorithm and dataset in algResutls */
	public void estimateBestPerAlg(final String measure) throws Exception
	{
		setName = "best" + measure;
		algResults.remove(ALL_ALGS);

		int algCount = 1;
		for (String alg : ALGORITHMS)
		{
			System.out.println(alg);

			File f = new File(ValidationResultsProvider.RESULTS_MERGED_FOLDER + alg + "." + measure
					+ ".d" + DATASETS.length + ".s" + false + ".m"
					+ ValidationResultsProvider.performanceMeasures.length + ".best");
			if (f.exists())
			{
				//				this.params = measure;
				//				setParamsStr();
				System.err.println("read cached best from " + f);
				algResults.put(alg, ResultSetIO.parseFromTxtFile(f));
			}
			else
			{
				read(new String[] { alg }, RemoveObsoleteSizes.yes_retain_none);

				//				System.out.println(algResults.get(alg).getProperties());
				//				System.exit(1);

				for (final String dataset : DATASETS)
				{
					System.out.println(dataset);

					ResultSet r = algResults.get(alg).filter(new ResultSetFilter()
					{
						@Override
						public boolean accept(Result result)
						{
							return result.getValue("Dataset").equals(dataset);
						}
					});
					if (r.getNumResults() == 0)
						continue;

					//System.err.println(test.toNiceString());

					r = r.join(
							new String[] { "Algorithm", "CFPType", "FeatureSelection",
									"hashfoldSize", "Dataset" },
							new String[] { "Run", "Fold" }, null);

					//					System.err.println();

					r.sortResults("Features", true, true, -1);
					r.sortResults(measure, false);

					System.err.println(r.toNiceString());

					//					System.err.println();
					//					System.err.println(r_non_filt.toNiceString());
					//					System.exit(1);

					//final String bestMethod = r.getResultValue(0, "Method").toString();
					final ResultSet finalR = r;

					algResults.put(alg, algResults.get(alg).filter(new ResultSetFilter()
					{
						@Override
						public boolean accept(Result result)
						{
							if (result.getValue("Dataset").toString().equals(dataset))
							{
								boolean match = true;
								for (String p : new String[] { "Algorithm", "CFPType",
										"FeatureSelection", "hashfoldSize" })
									if (!ObjectUtil.equals(result.getValue(p),
											finalR.getResultValue(0, p)))
										match = false;
								return match;
							}
							else
								return true;
						}
					}));
				}
				ResultSetIO.printToTxtFile(f, algResults.get(alg), true);
			}
			//			System.out.println(algResults.get(alg).toNiceString());

			if (algResults.containsKey(ALL_ALGS))
				algResults.get(ALL_ALGS).concat(algResults.get(alg));
			else
				algResults.put(ALL_ALGS, algResults.get(alg));

			System.out.println(DATASETS.length * algCount++ * 5 * 10);
			System.out.println(algResults.get(ALL_ALGS).getNumResults());
		}

		System.out.println(algResults
				.get(ALL_ALGS).join(new String[] { "Algorithm", "CFPType", "FeatureSelection",
						"hashfoldSize", "Dataset" }, new String[] { "Run", "Fold" }, null)
				.toNiceString());

		algResults.get(ALL_ALGS).removePropery("CFPType");
		algResults.get(ALL_ALGS).removePropery("FeatureSelection");
		algResults.get(ALL_ALGS).removePropery("hashfoldSize");
	}

	/** 
	 * goal is to get best overall method with few interpretable features
	 * 
	 * * remove all non-fold
	 * * remove too many features (unfiltered with diameter > 2)
	 * per dataset:
	 * * make signi-test with relaxed sig-level and remove loosers & non-winners
	 * * sort by measure
	 * * sort by num-features (ascending)
	 * 
	 * @param measure
	 * @throws Exception
	 */
	public void estimateBestOverall(final String measure) throws Exception
	{
		setName = "best" + measure;

		read("best" + measure, RemoveObsoleteSizes.yes_retain_none,
				getFilter(null, null, FeatureSelection.filt, null),
				getFilter(CFPType.ecfp0, null, FeatureSelection.none, null),
				getFilter(CFPType.fcfp0, null, FeatureSelection.none, null),
				getFilter(CFPType.ecfp2, null, FeatureSelection.none, null),
				getFilter(CFPType.fcfp2, null, FeatureSelection.none, null));

		ResultSet all = algResults.get(ALL_ALGS);

		String sortedDatasets[] = DATASETS;
		Arrays.sort(sortedDatasets, DataLoader.CFPDataComparator);
		double maxDiff = 0;
		double maxFeatures = 0;

		for (final String dataset : DATASETS)
		{
			System.err.println(dataset);

			ResultSet r = all.filter(new ResultSetFilter()
			{
				@Override
				public boolean accept(Result result)
				{
					return result.getValue("Dataset").equals(dataset);
				}
			});
			if (r.getNumResults() == 0)
				continue;

			for (int i = 0; i < r.getNumResults(); i++)
				r.setResultValue(i, "Method",
						alg(r, i) + "#" + type(r, i) + "#" + feat(r, i) + "#" + size(r, i));
			final ResultSet test = r.pairedTTest_All("Method",
					ArrayUtil.toList(new String[] { "Run", "Fold" }), measure, SIG_LEVEL_RELAXED,
					TEST_CORRECTION_RELAXED);
			//			System.err.println(test.toNiceString());
			//			System.err.println();
			r = r.join(new String[] { "Algorithm", "CFPType", "FeatureSelection", "hashfoldSize",
					"Dataset", "Method" }, new String[] { "Run", "Fold" }, null);

			//			System.err.println(r.toNiceString());

			ResultSet winners = r.filter(new ResultSetFilter()
			{
				@Override
				public boolean accept(Result result)
				{
					Boolean w = ResultSet.isWinOrLoss(test, "Method",
							result.getValueToString("Method"), measure);
					return (w != null && w);
				}
			});
			if (winners.getNumResults() > 0)
			{
				System.err.println("skip " + (r.getNumResults() - winners.getNumResults())
						+ " loosers, left: " + winners.getNumResults());
				r = winners;
			}
			else
				System.err.println("no differences");

			r.removePropery("Method");
			r.sortResults(measure, false);
			double best = (Double) r.getResultValue(0, measure);
			r.sortResults("Features", true, true, -1);
			double smallest = (Double) r.getResultValue(0, measure);

			//			System.err.println();
			System.err.println("diff to best: " + (best - smallest));
			maxDiff = Math.max(maxDiff, best - smallest);
			maxFeatures = Math.max(maxFeatures, (Double) r.getResultValue(0, "Features"));
			System.err.println(r.toNiceString());

			//					System.err.println();
			//					System.err.println(r_non_filt.toNiceString());
			//					System.exit(1);

			//final String bestMethod = r.getResultValue(0, "Method").toString();
			final ResultSet finalR = r;

			all = all.filter(new ResultSetFilter()
			{
				@Override
				public boolean accept(Result result)
				{
					if (result.getValue("Dataset").toString().equals(dataset))
					{
						boolean match = true;
						for (String p : new String[] { "Algorithm", "CFPType", "FeatureSelection",
								"hashfoldSize" })
							if (!ObjectUtil.equals(result.getValue(p), finalR.getResultValue(0, p)))
								match = false;
						return match;
					}
					else
						return true;
				}
			});
		}

		System.out
				.println(all
						.join(new String[] { "Algorithm", "CFPType", "FeatureSelection",
								"hashfoldSize", "Dataset" }, new String[] { "Run", "Fold" }, null)
						.toNiceString());
		System.out.println("max perf lost: " + maxDiff);
		System.out.println("max features: " + maxFeatures);

		String dest = ValidationResultsProvider.RESULTS_MERGED_FOLDER + measure + ".best";
		System.out.println("\nprint to " + dest);
		ResultSetIO.printToTxtFile(new File(dest), all, true);
	}

	//	/** leaves only a single method per algorithm and dataset in algResutls */
	//	public void estimateBest(String measure) throws Exception
	//	{
	//		int algCount = 1;
	//		for (String alg : ALGORITHMS)
	//		{
	//			System.out.println(alg);
	//
	//			File f = new File(ValidationResultsProvider.RESULTS_MERGED_FOLDER + alg + "." + measure + ".d"
	//					+ DATASETS.length + ".best");
	//			if (f.exists())
	//			{
	//				//				this.params = measure;
	//				//				setParamsStr();
	//				algResults.put(alg, ResultSetIO.parseFromTxtFile(f));
	//			}
	//			else
	//			{
	//				read(new String[] { alg }, RemoveObsoleteSizes.yes_retain_none);
	//
	//				for (final String dataset : DATASETS)
	//				{
	//					ResultSet r = algResults.get(alg).filter(new ResultSetFilter()
	//					{
	//						@Override
	//						public boolean accept(Result result)
	//						{
	//							return result.getValue("Dataset").equals(dataset);
	//						}
	//					});
	//					if (r.getNumResults() == 0)
	//						continue;
	//					r = r.join(new String[] { "Method", "Dataset" }, new String[] { "Run", "Fold" }, null);
	//					//					r.sortResults("Method", new Comparator<Object>()
	//					//					{
	//					//						@Override
	//					//						public int compare(Object o1, Object o2)
	//					//						{
	//					//							String method = o1.toString();
	//					//							String method2 = o2.toString();
	//					//							String size = sizeFromMethod(method);
	//					//							String size2 = sizeFromMethod(method2);
	//					//							if (size == null)
	//					//								size = Integer.MAX_VALUE + "";
	//					//							if (size2 == null)
	//					//								size2 = Integer.MAX_VALUE + "";
	//					//							if (!size.equals(size2))
	//					//								return new Integer(size).compareTo(new Integer(size2));
	//					//							int diameter = fpFromMethod(method).getDiameter();
	//					//							int diameter2 = fpFromMethod(method2).getDiameter();
	//					//							if (diameter != diameter2)
	//					//								return new Integer(diameter).compareTo(new Integer(diameter2));
	//					//							FeatureSelection feat = featFromMethod(method);
	//					//							FeatureSelection feat2 = featFromMethod(method2);
	//					//							if (feat != feat2)
	//					//							{
	//					//								if (feat == FeatureSelection.filt)
	//					//									return -1;
	//					//								else if (feat2 == FeatureSelection.filt)
	//					//									return 1;
	//					//								if (feat == FeatureSelection.none)
	//					//									return -1;
	//					//								else if (feat2 == FeatureSelection.none)
	//					//									return 1;
	//					//							}
	//					//							return 0;
	//					//							//                                CFPType type = fpFromMethod(method);
	//					//							//                                CFPType type2 = fpFromMethod(method2);
	//					//							//                                if (type.getDiameter() != type2.getDiameter())
	//					//							//                                    return new Integer(type.getDiameter()).compareTo(new Integer(type.getDiameter()));
	//					//							//                                if (type != type2)
	//					//						}
	//					//					});
	//					r.sortResults("Features"); //, !dataset.equals("AMES"));
	//					r.sortResults(measure, false);
	//					final String bestMethod = r.getResultValue(0, "Method").toString();
	//
	//					//					if (r.getResultValue(0, measure).equals(r.getResultValue(1, measure))
	//					//							&& r.getResultValue(0, "Features").equals(r.getResultValue(1, "Features"))
	//					//							&& sizeFromMethod(bestMethod).equals(
	//					//									sizeFromMethod(r.getResultValue(1, "Method").toString()))
	//					//							&& featFromMethod(bestMethod).equals(
	//					//									featFromMethod(r.getResultValue(1, "Method").toString()))
	//					//							&& fpFromMethod(bestMethod).getDiameter() == fpFromMethod(
	//					//									r.getResultValue(1, "Method").toString()).getDiameter())
	//					//						throw new Error("equal: auc && #features && size && diam && feat-sel:\n" + r.toNiceString());
	//
	//					algResults.put(alg, algResults.get(alg).filter(new ResultSetFilter()
	//					{
	//						@Override
	//						public boolean accept(Result result)
	//						{
	//							if (result.getValue("Dataset").toString().equals(dataset))
	//								return result.getValue("Method").equals(bestMethod);
	//							else
	//								return true;
	//						}
	//					}));
	//				}
	//				ResultSetIO.printToTxtFile(f, algResults.get(alg), true);
	//			}
	//			//			System.out.println(algResults.get(alg).toNiceString());
	//
	//			if (algResults.containsKey(ALL_ALGS))
	//				algResults.get(ALL_ALGS).concat(algResults.get(alg));
	//			else
	//				algResults.put(ALL_ALGS, algResults.get(alg));
	//
	//			System.out.println(DATASETS.length * algCount++ * 5 * 10);
	//			System.out.println(algResults.get(ALL_ALGS).getNumResults());
	//		}
	//
	//		System.out.println(algResults.get(ALL_ALGS)
	//				.join(new String[] { "Method", "Dataset" }, new String[] { "Run", "Fold" }, null).toNiceString());
	//	}
	//
	//	public void best(String measure) throws Exception
	//	{
	//		ResultSet cmp = new ResultSet();
	//		ResultSet cmp2 = new ResultSet();
	//		File f = new File("/tmp/best.out");
	//		if (f.exists())
	//		{
	//			cmp = ResultSetIO.parseFromTxtFile(f);
	//		}
	//		else
	//		{
	//			boolean scriptAndCopy = false;
	//
	//			StringBuffer rerunScript = new StringBuffer();
	//			rerunScript.append("#!/bin/bash\n");
	//			rerunScript.append("module load Java/jdk1.8.0_25\n");
	//
	//			for (String alg : ALGORITHMS)
	//			{
	//				algResults.clear();
	//				read(new String[] { alg }, RemoveObsoleteSizes.yes_retain_none);
	//
	//				ResultSet best = new ResultSet();
	//				//        System.out.println(algResults.get(alg).toNiceString());
	//
	//				int datasetIdx = 0;
	//				for (final String dataset : new CFPDataLoader("data").allDatasets())
	//				{
	//					ResultSet r = algResults.get(alg).filter(new ResultSetFilter()
	//					{
	//						@Override
	//						public boolean accept(Result result)
	//						{
	//							return result.getValue("Dataset").equals(dataset);
	//						}
	//					});
	//					if (r.getNumResults() == 0)
	//						continue;
	//					r = r.join(new String[] { "Method", "Dataset" }, new String[] { "Run", "Fold" }, null);
	//
	//					//					r.sortResults("Method", new Comparator<Object>()
	//					//					{
	//					//						@Override
	//					//						public int compare(Object o1, Object o2)
	//					//						{
	//					//							String method = o1.toString();
	//					//							String method2 = o2.toString();
	//					//							String size = sizeFromMethod(method);
	//					//							String size2 = sizeFromMethod(method2);
	//					//							if (size == null)
	//					//								size = Integer.MAX_VALUE + "";
	//					//							if (size2 == null)
	//					//								size2 = Integer.MAX_VALUE + "";
	//					//							if (!size.equals(size2))
	//					//								return new Integer(size).compareTo(new Integer(size2));
	//					//							int diameter = fpFromMethod(method).getDiameter();
	//					//							int diameter2 = fpFromMethod(method2).getDiameter();
	//					//							if (diameter != diameter2)
	//					//								return new Integer(diameter).compareTo(new Integer(diameter2));
	//					//							FeatureSelection feat = featFromMethod(method);
	//					//							FeatureSelection feat2 = featFromMethod(method2);
	//					//							if (feat != feat2)
	//					//							{
	//					//								if (feat == FeatureSelection.filt)
	//					//									return -1;
	//					//								else if (feat2 == FeatureSelection.filt)
	//					//									return 1;
	//					//								if (feat == FeatureSelection.none)
	//					//									return -1;
	//					//								else if (feat2 == FeatureSelection.none)
	//					//									return 1;
	//					//							}
	//					//							return 0;
	//					//							//                                CFPType type = fpFromMethod(method);
	//					//							//                                CFPType type2 = fpFromMethod(method2);
	//					//							//                                if (type.getDiameter() != type2.getDiameter())
	//					//							//                                    return new Integer(type.getDiameter()).compareTo(new Integer(type.getDiameter()));
	//					//							//                                if (type != type2)
	//					//						}
	//					//					});
	//					r.sortResults("Features", !dataset.equals("AMES"));
	//					r.sortResults(measure, false);
	//
	//					//					if (r.getResultValue(0, measure).equals(r.getResultValue(1, measure))
	//					//							&& r.getResultValue(0, "Features").equals(r.getResultValue(1, "Features"))
	//					//							&& sizeFromMethod(r.getResultValue(0, "Method").toString()).equals(
	//					//									sizeFromMethod(r.getResultValue(1, "Method").toString()))
	//					//							&& featFromMethod(r.getResultValue(0, "Method").toString()).equals(
	//					//									featFromMethod(r.getResultValue(1, "Method").toString()))
	//					//							&& fpFromMethod(r.getResultValue(0, "Method").toString()).getDiameter() == fpFromMethod(
	//					//									r.getResultValue(1, "Method").toString()).getDiameter())
	//					//						throw new Error("equal: auc && #features && size && diam && feat-sel:\n" + r.toNiceString());
	//
	//					//			System.out.println(dataset);
	//					//			System.out.println(r.getResultValue(0, "Method"));
	//					//			//			System.out.println(r.toNiceString());
	//					//			System.out.println();
	//
	//					int idx = best.addResult();
	//					best.setResultValue(idx, "Dataset", dataset);
	//					best.setResultValue(idx, "type", type(r, 0));
	//					best.setResultValue(idx, "feat", feat(r, 0));
	//					best.setResultValue(idx, "hashfoldsize", size(r, 0));
	//
	//					idx = cmp.addResult();
	//					cmp.setResultValue(idx, "Dataset", dataset);
	//					cmp.setResultValue(idx, "Algorithm", alg);
	//					cmp.setResultValue(idx, measure, r.getResultValue(0, measure));
	//					if (datasetIdx >= cmp2.getNumResults())
	//					{
	//						cmp2.addResult();
	//						cmp2.setResultValue(datasetIdx, "Dataset", dataset);
	//					}
	//					cmp2.setResultValue(datasetIdx, alg, r.getResultValue(0, measure));
	//
	//					//					if (scriptAndCopy)
	//					//					{
	//					//						if (dataset.startsWith("ChEMBL") || dataset.startsWith("MUV"))
	//					//						{
	//					//							for (int run = 1; run <= 5; run++)
	//					//							{
	//					//								String resFile = CFPMiner.resultFileName(run, type, feat, size, alg, dataset);
	//					//								String outFile = resFile.replace(".arff", ".output");
	//					//								String hashfoldStr = "";
	//					//								if (feat != FeatureSelection.none)
	//					//									hashfoldStr = "--hashfoldsize " + size;
	//					//								rerunScript.append("bsub -q short -W 300 -n 1 -app Reserve5G -o output/" + outFile
	//					//										+ "  java -jar -Xmx2G cfpminer.jar --datasetName " + dataset + " --run " + run
	//					//										+ " --classifier " + alg + " --type " + type + " --featureSelection " + feat
	//					//										+ " " + hashfoldStr + " -x\n");
	//					//								rerunScript.append("sleep 0.5\n");
	//					//							}
	//					//						}
	//					//						else
	//					//						{
	//					//							//				String src = "/home/martin/workspace/CFPMiner/results_r5_all/";
	//					//							//				String dest = "/home/martin/workspace/CFPMiner/results_r5_best_no_resample/";
	//					//							//				for (int run = 1; run <= 5; run++)
	//					//							//				{
	//					//							//					String resFile = CFPMiner.resultFileName(run, type, FeatureSelection.filt, size, alg, dataset);
	//					//							//					FileUtil.copy(src + resFile, dest + resFile);
	//					//							//				}
	//					//						}
	//					//					}
	//					datasetIdx++;
	//				}
	//				System.out.println(best.toNiceString());
	//				if (scriptAndCopy)
	//				{
	//					String dest = ValidationResultsProvider.RESULTS_MERGED_FOLDER + alg + setName + ".best";
	//					ResultSetIO.printToTxtFile(new File(dest), best, true);
	//					System.out.println("written best methods to " + dest);
	//
	//					FileUtil.writeStringToFile("/tmp/killme", rerunScript.toString());
	//					System.out.println();
	//					System.out.flush();
	//					System.err.println(rerunScript);
	//					break;
	//				}
	//			}
	//			ResultSetIO.printToTxtFile(f, cmp, true);
	//		}
	//		System.out.println(cmp.toNiceString());
	//		ResultSet diff = cmp.diff("Algorithm", ArrayUtil.toList(new String[] { "Dataset" }), null);
	//		System.out.println(diff.toNiceString());
	//		ResultSet winLoss = diff.winLoss(ArrayUtil.toList(new String[] { "Algorithm" }), null);
	//		System.out.println("\nwin loss (uses diff as input)\n");
	//		System.out.println(winLoss.toNiceString());
	//
	//		System.out.println(cmp2.toNiceString());
	//	}

	public void ranking(ResFilter filter, String measure) throws Exception
	{
		ResultSet combined = new ResultSet();
		for (String alg : ALGORITHMS)
		{
			System.out.println("\n\n\n" + alg + "\n--------------\n");
			ResultSet r;

			//			this.params = params;
			//			setParamsStr();
			String cache = ValidationResultsProvider.RESULTS_MERGED_FOLDER + alg + filter.getKey()
					+ ".joined";
			if (!new File(cache).exists())
			{
				read(new String[] { alg }, RemoveObsoleteSizes.yes_retain_none, filter);
				r = algResults.get(alg).join(new String[] { "Method", "Dataset" },
						new String[] { "Run", "Fold" }, null);
				ResultSetIO.printToTxtFile(new File(cache), r, true);
			}
			else
			{
				System.err.println("read cached from: " + cache);
				r = ResultSetIO.parseFromTxtFile(new File(cache));
			}
			//			r = r.filter(new ResultSetFilter()
			//			{
			//				@Override
			//				public boolean accept(Result result)
			//				{
			//					String size = sizeFromMethod(result.getValue("Method").toString());
			//					return size == null || size.equals("1024") || size.equals("8192");
			//				}
			//			});

			r = r.rank(measure, new String[] { "Dataset" });
			r.clearMergeCountAndVariance();
			int numDatasets = r.getResultValues("Dataset").getNumValues();
			System.err.println("num datasets " + numDatasets);
			r = r.join("Method");
			//			renameMethods(alg, r);
			r.removePropery("Dataset");
			for (String p : ValidationResultsProvider.performanceMeasures)
				r.removePropery(p);
			r.removePropery("Sensitivity");
			r.removePropery("Selectivity");

			r.sortResults(measure + ResultSet.RANK_SUFFIX, true, true, -1);

			System.out.println(r.toNiceString());
			//			for (String p : new String[] { "Features", "folding-enabled" })
			//				System.out.println("correlation to " + p + " " + r.spearmanCorrelation(measure + "_rank", p));

			//			System.exit(1);

			int count = 0;
			int winFold = 0;
			int winAll = 0;

			double diffFold = 0.0;
			double diffAll = 0.0;
			for (CFPType t : CFPType.values())
			{
				for (Integer size : SIZES)
				{
					//					System.out.println(t + " " + size);
					int iFilt = getResultIdx(r, t, size, FeatureSelection.filt);
					if (iFilt == -1)
						continue;
					Double rFilt = (Double) r.getResultValue(iFilt,
							measure + ResultSet.RANK_SUFFIX);
					Double rFold = (Double) r.getResultValue(
							getResultIdx(r, t, size, FeatureSelection.fold),
							measure + ResultSet.RANK_SUFFIX);
					//					System.out.println(rFilt + " vs " + rFold);
					if (rFilt < rFold)
						winFold += 1;
					else
						System.out.println("fold loss: " + t + " " + size);
					diffFold += rFold - rFilt;

					Double rAll = (Double) r.getResultValue(
							getResultIdx(r, t, null, FeatureSelection.none),
							measure + ResultSet.RANK_SUFFIX);
					if (rFilt < rAll)
						winAll += 1;
					diffAll += rAll - rFilt;

					count += 1;
				}
			}
			System.out.println("filt vs fold - < " + winFold + "/" + count + " - diff "
					+ (diffFold / (double) count));
			System.out.println("filt vs all - < " + winAll + "/" + count + " - diff "
					+ (diffAll / (double) count));

			int sumBest = 0;
			for (int i = 0; i < r.getNumResults(); i++)
			{
				if (alg.equals(ALGORITHMS[0]))
					combined.addResult();
				//				else if (!combined.getResultValue(i, "Method").equals(r.getResultValue(i, "Method")))
				//					throw new IllegalStateException();
				combined.setResultValue(i, alg + " CFPType", type(r, i));
				combined.setResultValue(i, alg + " FeatureSelection",
						feat(r, i).toNiceShortString());
				combined.setResultValue(i, alg + " hashfoldSize", size(r, i));

				//				combined.setResultValue(i, "Coll.", "to be overwritten");
				//				combined.setResultValue(i, alg + " Top3", "");
				combined.setResultValue(i, alg + " " + measure + " Rank", StringUtil.formatDouble(
						((Double) r.getResultValue(i, measure + ResultSet.RANK_SUFFIX)), 1));
				double best = ((Number) r.getResultValue(i, measure + ResultSet.RANK_BEST_SUFFIX))
						.doubleValue() * numDatasets;
				sumBest += best;
				combined.setResultValue(i, alg + " " + measure + " Best", (int) best);
				//				combined.setNicePropery(alg + " Top3", "Top3");
				combined.setNicePropery(alg + " " + measure + " Rank", "Rank");

				combined.setNicePropery(alg + " CFPType", niceValuesShort.get("CFPType"));
				combined.setNicePropery(alg + " FeatureSelection",
						niceValuesShort.get("FeatureSelection"));
				combined.setNicePropery(alg + " hashfoldSize", niceValuesShort.get("hashfoldSize"));
				combined.setNicePropery(alg + " " + measure + " Best", "Best");
			}
			System.err.println("num best sum " + sumBest);

		}

		System.out.println("\n\n\ncombined\n--------------\n");

		System.out.println(combined.toNiceString());

		//		System.out.println(9 / (double) combined.getNumResults());
		//		System.out.println(27 / (double) combined.getNumResults());

		if (write)
		{
			String dest = destFolder + "ranking.tex";
			System.out.println("write table to " + dest);

			String preProp = "";
			Integer hlines[] = {};
			for (String alg : ALGORITHMS)
			{
				if (!preProp.isEmpty())
					preProp += "& ";
				preProp += "\\multicolumn{3}{c}{" + niceValues.get(alg) + "} & \\multicolumn{2}{c}{"
						+ measure + "} ";
				Integer h[] = new Integer[] { hlines.length > 0 ? 2 : 0, 0, 0, 1, 0 };
				if (hlines.length == 0)
					hlines = h;
				else
					hlines = ArrayUtil.concat(hlines, h);
			}
			//hlines = ArrayUtil.concat(hlines, new Boolean[] { true });
			preProp += "\\\\\n";

			FileUtil.writeStringToFile(dest, combined.toLatexTable(null, hlines, preProp));
		}
	}

	//	public void rankingOld(ResFilter filter, String measure) throws Exception
	//	{
	//		ResultSet combined = new ResultSet();
	//		for (String alg : ALGORITHMS)
	//		{
	//			ResultSet r;
	//
	//			//			this.params = params;
	//			//			setParamsStr();
	//			String cache = ValidationResultsProvider.RESULTS_MERGED_FOLDER + alg + filter.getKey() + ".joined";
	//			if (!new File(cache).exists())
	//			{
	//				read(new String[] { alg }, RemoveObsoleteSizes.yes_retain_none, filter);
	//				//				remove_obs_sizes(alg, true);
	//				r = algResults.get(alg)
	//						.join(new String[] { "Method", "Dataset" }, new String[] { "Run", "Fold" }, null);
	//				ResultSetIO.printToTxtFile(new File(cache), r, true);
	//			}
	//			else
	//			{
	//				System.err.println("read cached from: " + cache);
	//				r = ResultSetIO.parseFromTxtFile(new File(cache));
	//			}
	//			//			r = r.filter(new ResultSetFilter()
	//			//			{
	//			//				@Override
	//			//				public boolean accept(Result result)
	//			//				{
	//			//					String size = sizeFromMethod(result.getValue("Method").toString());
	//			//					return size == null || size.equals("1024") || size.equals("8192");
	//			//				}
	//			//			});
	//
	//			//			if (alg.equals("SMO") && r.getResultValues("Dataset").getNumValues() > 1)
	//			//				r = r.filter(new ResultSetFilter()
	//			//				{
	//			//					@Override
	//			//					public boolean accept(Result result)
	//			//					{
	//			//						return !result.getValue("Dataset").equals("AMES");
	//			//					}
	//			//				});
	//
	//			r = r.rank(measure, new String[] { "Dataset" });
	//			r.clearMergeCountAndVariance();
	//			int numDatasets = r.getResultValues("Dataset").getNumValues();
	//			System.err.println("num datasets " + numDatasets);
	//			r = r.join("Method");
	//			//			renameMethods(alg, r);
	//			r.removePropery("Dataset");
	//			for (String p : ValidationResultsProvider.performanceMeasures)
	//				r.removePropery(p);
	//			r.removePropery("Sensitivity");
	//			r.removePropery("Selectivity");
	//			//			r.sortResults("AUC" + ResultSet.RANK_SUFFIX, true, true, -1);
	//			//			System.out.println(r.toNiceString());
	//
	//			int sumBest = 0;
	//			for (int i = 0; i < r.getNumResults(); i++)
	//			{
	//				if (alg.equals(ALGORITHMS[0]))
	//					combined.addResult();
	//				else if (!combined.getResultValue(i, "Method").equals(r.getResultValue(i, "Method")))
	//					throw new IllegalStateException();
	//				combined.setResultValue(i, "Method", r.getResultValue(i, "Method"));
	//				combined.setResultValue(i, "Features", Math.round((Double) r.getResultValue(i, "Features")));
	//				combined.setResultValue(i, "Coll.", "to be overwritten");
	//				combined.setResultValue(i, alg + " Top3", "");
	//				combined.setResultValue(i, alg + " " + measure + " Rank",
	//						r.getResultValue(i, measure + ResultSet.RANK_SUFFIX));
	//				double best = ((Number) r.getResultValue(i, measure + ResultSet.RANK_BEST_SUFFIX)).doubleValue()
	//						* numDatasets;
	//				sumBest += best;
	//				combined.setResultValue(i, alg + " " + measure + " Best", (int) best);
	//				combined.setNicePropery(alg + " Top3", "Top3");
	//				combined.setNicePropery(alg + " " + measure + " Rank", "Rank");
	//				combined.setNicePropery(alg + " " + measure + " Best", "Best");
	//			}
	//			System.err.println("num best sum " + sumBest);
	//
	//			r.sortResults(measure + ResultSet.RANK_SUFFIX, true, true, -1);
	//
	//			for (int i = 0; i < r.getNumResults(); i++)
	//			{
	//				r.setResultValue(i, "folding-enabled", feat(r, i) == FeatureSelection.fold);
	//			}
	//			System.out.println(r.toNiceString());
	//			for (String p : new String[] { "Features", "folding-enabled" })
	//				System.out.println("correlation to " + p + " " + r.spearmanCorrelation(measure + "_rank", p));
	//
	//			//			System.exit(1);
	//
	//			for (int i = 0; i < 3; i++)
	//			{
	//				double rank = (Double) r.getResultValue(i, measure + ResultSet.RANK_SUFFIX);
	//				for (int c = 0; c < combined.getNumResults(); c++)
	//				{
	//					double rankC = (Double) combined.getResultValue(c, alg + " " + measure + " Rank");
	//					if (rankC == rank)
	//						combined.setResultValue(c, alg + " Top3", "*");
	//				}
	//
	//			}
	//		}
	//		tableCollisions(true); // to load collisions
	//
	//		int addP = 0;
	//		combined.getProperties().add(addP++, "idx");
	//		combined.getProperties().add(addP++, niceValuesShort.get("CFPType"));
	//		combined.getProperties().add(addP++, "FeatureSelection");
	//		combined.getProperties().add(addP++, "hashfoldSize");
	//
	//		for (int i = 0; i < combined.getNumResults(); i++)
	//		{
	//			combined.setResultValue(i, niceValuesShort.get("CFPType"), type(combined, i).toNiceString());
	//			combined.setResultValue(i, "FeatureSelection", feat(combined, i).toNiceString());
	//			combined.setResultValue(i, "hashfoldSize", size(combined, i));
	//
	//			Double rate;
	//			if (feat(combined, i) == FeatureSelection.fold)
	//				rate = collisionRate.get(type(combined, i), size(combined, i));
	//			else
	//				rate = 0.0;
	//			combined.setResultValue(i, "Coll.", rate);
	//			double rankSum = 0;
	//			for (String alg : ALGORITHMS)
	//				rankSum += ((Number) combined.getResultValue(i, alg + " " + measure + " Rank")).doubleValue();
	//			combined.setResultValue(i, "Combined Rank", rankSum / (double) ALGORITHMS.length);
	//		}
	//		combined.sortResults("Combined Rank", true, true, -1);
	//
	//		//combined.sortResults("RaF "+measure+" Rank", true, true, -1);
	//		//combined.removePropery("Combined Rank");
	//
	//		for (String p : niceValues.keySet())
	//			combined.setNicePropery(p, niceValues.get(p));
	//
	//		combined.removePropery("Method");
	//		for (String alg : ALGORITHMS)
	//			combined.removePropery(alg + " Top3");
	//
	//		for (int i = 0; i < combined.getNumResults(); i++)
	//			combined.setResultValue(i, "idx", i + 1);
	//
	//		System.out.println(combined.toNiceString());
	//		System.out.println(combined.getNumResults());
	//
	//		//		System.out.println(9 / (double) combined.getNumResults());
	//		//		System.out.println(27 / (double) combined.getNumResults());
	//
	//		if (write)
	//		{
	//			String dest = destFolder + "ranking.tex";
	//			System.out.println("write table to " + dest);
	//
	//			String preProp = " \\multicolumn{" + addP + "}{c}{Selected features} & & ";
	//			Boolean hlines[] = { false, true, false, false, true, false };
	//			for (String alg : ALGORITHMS)
	//			{
	//				preProp += "& \\multicolumn{2}{c}{" + niceValuesShort.get(alg) + " " + measure + "} ";
	//				hlines = ArrayUtil.concat(hlines, new Boolean[] { true, false });
	//			}
	//			hlines = ArrayUtil.concat(hlines, new Boolean[] { true });
	//			preProp += "&\\\\\n";
	//
	//			FileUtil.writeStringToFile(dest, combined.toLatexTable(null, hlines, preProp));
	//		}
	//	}

	DoubleKeyHashMap<CFPType, Integer, Double> collisionRate = new DoubleKeyHashMap<>();

	public void tableCollisions(boolean noOutput)
	{
		String preProp = "& ";
		for (Integer size : SIZES)
			preProp += "& \\multicolumn{2}{c}{" + size + "} ";
		preProp += "\\\\\n";

		for (final String type : new String[] { "ecfp", "fcfp" })
		{
			ResultSet res = ResultSetIO
					.parseFromTxtFile(new File("data_collisions/collisions_" + type + ".result"));
			if (!noOutput)
				System.out.println(res.toNiceString());
			sortDatasets(res);

			for (final String typeSize : new String[] { "6", "4", "2", "0" })
			{
				CFPType t = CFPType.valueOf(type + typeSize);

				ResultSet r = res.copy().filter(new ResultSetFilter()
				{
					@Override
					public boolean accept(Result result)
					{
						return result.getValue("Type").toString().equals(type + typeSize);
					}
				});
				r.removePropery("Type");
				for (Integer size : SIZES)
				{
					if (skipFiltFoldMethod(t, size))
					{
						System.err.println("skip " + t + " " + size);
						for (int i = 0; i < r.getNumResults(); i++)
						{
							r.setResultValue(i, size + " bit-load", null);
							r.setResultValue(i, size + " collisions", null);
						}
					}
					else
					{
						r.setNicePropery(size + " bit-load", "bit-load");
						r.setNicePropery(size + " collisions", "rate");
					}
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
						FileUtil.writeStringToFile(dest,
								r.toLatexTable(null, (Integer[]) null, datasetPreProp));
					}
				}
			}

			ResultSet joined = res.join("Type");
			for (int i = 0; i < joined.getNumResults(); i++)
			{
				CFPType t = CFPType.valueOf(joined.getResultValue(i, "Type").toString());
				for (Integer size : SIZES)
				{
					if (skipFiltFoldMethod(t, size))
					{
						joined.setResultValue(i, size + " bit-load", null);
						joined.setResultValue(i, size + " collisions", null);
					}
				}
			}

			joined.removePropery("Dataset");
			joined.removePropery("Compounds");

			for (Integer size : SIZES)
			{
				joined.setNicePropery(size + " bit-load", "bit-load");
				joined.setNicePropery(size + " collisions", "rate");
			}

			for (int i = 0; i < joined.getNumResults(); i++)
			{
				CFPType t = CFPType.valueOf(joined.getResultValue(i, "Type").toString());
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
					FileUtil.writeStringToFile(dest,
							joined.toLatexTable(null, (Integer[]) null, preProp));
				}
			}
		}
	}

	//	public void tableWinLossFiltering()
	//	{
	//		for (String alg : ALGORITHMS)
	//		{
	//			algResults.get(alg).sortResults("Method");
	//			ResultSet t1 = new ResultSet();
	//			for (String s : ValidationResultsProvider.performanceMeasures)
	//			{
	//				ResultSet set = ValidationResultsProvider.ttest(algResults.get(alg), s, "filt", true);
	//				for (int i = 0; i < set.getNumResults(); i++)
	//				{
	//					String method = set.getResultValue(i, "Method_1").toString();
	//					if (!method.contains("filt"))
	//						throw new IllegalStateException();
	//					if (t1.getNumResults() <= i)
	//						t1.addResult();
	//					t1.setResultValue(i, "hashfoldSize", method.substring(method.lastIndexOf("_") + 1));
	//					t1.setResultValue(i, s, set.getResultValue(i, s));
	//				}
	//			}
	//			System.out.println("Filtering vs Folding");
	//			for (String p : niceValues.keySet())
	//				t1.setNicePropery(p, niceValues.get(p));
	//			System.out.println(t1.toNiceString());
	//
	//			FileUtil.writeStringToFile(destFolder + "Table_WinLoss_FiltVsFold_" + alg + ".tex", t1.toLatexTable());
	//			//		System.out.println(t1.toLatexTable());
	//		}
	//	}

	public void lineChartAlgorithm()
	{
		lineChart(new String[] { ALL_ALGS }, "Algorithm");
	}

	public void lineChartFeatureSelection()
	{
		lineChart(ALGORITHMS, "FeatureSelection");
	}

	public void lineChart(String algs[], String property)
	{
		for (String alg : algs)
		{
			System.out.println("plot " + alg);
			ResultSet res = algResults.get(alg).copy();
			checkUniqueProps(res, property);

			ResultSet r = res.join(new String[] { property, "Dataset" },
					new String[] { "Run", "Fold" }, null);
			sortDatasets(r);
			//			if (alg.equals(ALL_ALGS))
			//			{
			//				r.sortResults("Method", new Comparator<Object>()
			//				{
			//					@Override
			//					public int compare(Object o1, Object o2)
			//					{
			//						String m1 = o1.toString();
			//						String m2 = o2.toString();
			//						return algFromMethod(m1).compareTo(algFromMethod(m2));
			//					}
			//				});
			//			}
			//			else
			//			{
			//			renameMethods(alg, r);
			if (property.equals("Algorithm"))
				r.sortResults(property, getAlgComparator());
			else
				r.sortResults(property, false);

			//			}
			renameResultValues(r);

			//			System.out.println(r.toNiceString());
			ResultSetLinePlot plot = new ResultSetLinePlot(r,
					ValidationResultsProvider.performanceMeasures, property, "Dataset");
			plot.setTitle(null);
			plot.setXAxisLabel(null);
			plot.setRotateXLabels(ResultSetLinePlot.XLabelsRotation.vertical);
			plot.setYAxisLabelSuffix(" (" + niceValuesShort.get(alg) + ")");
			if (ValidationResultsProvider.performanceMeasures.length > 1)
			{
				for (String p : ValidationResultsProvider.performanceMeasures)
					if (p.equals("AUC") || p.equals("Accuracy"))
					{
						plot.setYAxisRange(p, 0.5, 1.0);
						plot.setYAxisTickUnits(p, 0.125);
					}
					else if (p.equals("FMeasure") || p.equals("Sensitivity")
							|| p.equals("Selectivity") || p.equals("AUPRC"))
					{
						plot.setYAxisRange(p, 0.0, 1.0);
						plot.setYAxisTickUnits(p, 0.2);
					}
					else if (p.equals("TN"))
					{
						plot.setYAxisRange(p, 197, 201);
					}
					else if (p.equals("Time"))
					{
						plot.setYAxisRange(p, 0, 50);
					}
			}
			else if (ValidationResultsProvider.performanceMeasures.length == 1
					&& ValidationResultsProvider.performanceMeasures[0].equals("AUC"))
				plot.setYAxisRange(0.5, 1.0);
			else if (ValidationResultsProvider.performanceMeasures.length == 1
					&& ValidationResultsProvider.performanceMeasures[0].equals("AUPRC"))
			{
				plot.setYAxisRange(0.0, 1.0);
				plot.setYAxisTickUnits("AUPRC", 0.2);
			}

			for (String p : ValidationResultsProvider.performanceMeasures)
			{
				//			if (p.equals("Accuracy"))
				//			{
				//				if (r.getResultValues("Dataset").contains("AMES"))
				plot.addMarker(p, "AMES", DataLoader.BALANCED_DATASETS);
				//				else
				//					plot.addMarker(p, "CPDBAS Dog Primates", CFPDataLoader.BALANCED_DATASETS);
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

			//			{
			//				ResultSet rTest = algResults.get(alg).copy();
			//				renameMethods(alg, rTest);
			//				for (String p : ValidationResultsProvider.performanceMeasures)
			//				{
			//					ResultSet test = rTest.pairedTTest("Method", ArrayUtil.toList(new String[] { "Run", "Fold" }), p,
			//							SIG_LEVEL, TEST_CORRECTION, "Dataset");
			//					List<String> methods = ListUtil.toStringList(r.getResultValues("Method").values());
			//					for (String m1 : methods)
			//					{
			//						for (String m2 : methods)
			//						{
			//							if (m1 == m2)
			//								continue;
			//							for (Object datasetWins : ResultSet.listSeriesWins(test, "Method", p, "Dataset", m1, m2))
			//								plot.setDrawShape(p, datasetWins.toString(), m1);
			//						}
			//					}
			//					System.out.println(test.toNiceString());
			//				}
			//				//			for (Object datasetWins : ResultSet.listSeriesWins(test, "Method", "AUC, "Dataset",
			//				//					"RandomForest ecfp_filt_1024", "RandomForest ecfp_fold_1024"))
			//				//				plot.setDrawShape("AUC", datasetWins.toString(), "RandomForest ecfp_filt_1024");
			//				//			for (Object datasetWins : ResultSet.listSeriesWins(test, "Method", "AUC", "Dataset",
			//				//					"RandomForest ecfp_fold_1024", "RandomForest ecfp_filt_1024"))
			//				//				plot.setDrawShape("AUC", datasetWins.toString(), "RandomForest ecfp_fold_1024");
			//
			//			}

			int height;
			int chartH = 145;
			int legendH = 135;

			if (ValidationResultsProvider.performanceMeasures.length == 1)
			{
				plot.setShowLegend(false);
				plot.setShowDomainAxis(false);
				height = chartH;
			}
			else
			{
				height = legendH + ValidationResultsProvider.performanceMeasures.length * chartH;
			}

			ChartPanel c = plot.getChartPanel();
			c.setMaximumDrawWidth(10000);
			c.setMaximumDrawHeight(5000);
			//c.setPreferredSize(new Dimension(800, 600));

			String name = "Chart_" + alg + "_" + setName + "_m"
					+ ValidationResultsProvider.performanceMeasures.length;

			if (showCharts)
				SwingUtil.showInFrame(c, destFolder + name, false,
						new Dimension(1000, 50 + height));

			if (write)
			{
				plot.toSVGFile(destFolder + name + ".svg", new Dimension(800, height));
				toPDF(destFolder + name);
			}
		}
	}

	public Map<String, ChartPanel> diffChart()
	{
		Map<String, ChartPanel> charts = new LinkedHashMap<>();
		for (String measure : ValidationResultsProvider.performanceMeasures)
		{
			//			checkUniqueProps(algResults.get(ALL_ALGS), "FeatureSelection");

			ResultSet r = algResults.get(ALL_ALGS).join(
					new String[] { "FeatureSelection", "Dataset", "Algorithm" },
					new String[] { "Run", "Fold" }, null);

			sortDatasets(r);

			//			renameMethods(alg, r);
			//			System.out.println(r.toNiceString());
			r.clearMergeCountAndVariance();

			List<String> diff = new ArrayList<>();
			List<String> ratio = new ArrayList<>();
			if (measure.equals("Time"))
			{
				r.sortResults("FeatureSelection", false);
				ratio.add(measure);
			}
			else
			{
				r.sortResults("FeatureSelection", true);
				diff.add(measure);
			}

			renameResultValues(r, true);

			r = r.diff("FeatureSelection",
					ArrayUtil.toList(new String[] { "Algorithm", "Dataset" }), diff, ratio);
			r.sortResults("Algorithm", getAlgComparator());
			System.out.println(r.toNiceString());

			String title = null; //measure;
			String label = null;//r.getUniqueValue("FeatureSelection").toString();
			String series1 = "FeatureSelection";
			String series2 = "Algorithm";
			String category = measure;
			//			List<String> categories = ArrayUtil.toList(new String[] { measure });

			ResultSetBoxPlot plot = new ResultSetBoxPlot(r, title, label, series1, series2,
					category);

			plot.setHideMean(true);
			plot.setRotateXLabels(false);
			plot.printNumResultsPerPlot(false);
			plot.setPrintMeanAndStdev(true);

			if (measure.equals("Time"))
			{
				plot.setYTickUnit(1.0);
				plot.setYRange(-0.5, 5.5);
			}
			else
			{
				plot.setYTickUnit(0.05);
				plot.setYRange(-0.15, 0.15);
			}

			ChartPanel c = plot.getChart();
			c.setMaximumDrawWidth(10000);
			c.setMaximumDrawHeight(5000);
			c.setMinimumDrawWidth(200);
			c.setMinimumDrawHeight(200);
			Dimension size = new Dimension(220, 220);
			c.setPreferredSize(size);
			c.setOpaque(false);

			charts.put(measure + " (" + (measure.equals("Time") ? "factor" : "\u0394") + ")", c);

			String name = "Chart_Diff_" + measure + "_" + setName + "_m"
					+ ValidationResultsProvider.performanceMeasures.length;
			if (showCharts)
				SwingUtil.showInFrame(c, name, false);

			if (write)
			{
				plot.boxPlotToSVGFile(destFolder + name + ".svg", size);
				toPDF(destFolder + name);
			}
		}
		return charts;
	}

	public void composeDiffCharts(String name,
			Map<FeatureSelection[], Map<String, ChartPanel>> charts)
	{
		DefaultFormBuilder builder = new DefaultFormBuilder(
				new FormLayout("p,3dlu,p,3dlu,p,3dlu,p"));
		builder.append("");
		Font f = null;

		for (FeatureSelection[] vs : charts.keySet())
		{
			Map<String, ChartPanel> map = charts.get(vs);
			if (f == null)
			{
				JFreeChart p = map.values().iterator().next().getChart();
				p.setTitle("bla");
				f = p.getTitle().getFont().deriveFont(p.getTitle().getFont().getSize() - 4.0f);
				p.setTitle("");
				for (String s : map.keySet())
				{
					JLabel l = new JLabel(s);
					l.setHorizontalAlignment(SwingConstants.CENTER);
					l.setFont(f);
					builder.append(l);
				}
				builder.nextLine();
			}

			JLabel l = new JLabel("<html><div style='text-align: center;'>" + vs[0].toNiceString()
					+ "<br>vs<br>" + vs[1].toNiceString() + "</div></html>");
			l.setHorizontalAlignment(SwingConstants.CENTER);
			l.setFont(f);
			builder.append(l);
			for (ChartPanel c : map.values())
				builder.append(c);
			builder.nextLine();
		}
		builder.getPanel().setBackground(Color.WHITE);
		if (showCharts)
			SwingUtil.showInFrame(builder.getPanel());
		if (write)
		{
			System.err.println("write to " + destFolder + name + ".png");
			SwingUtil.toFile(destFolder + name + ".png", builder.getPanel(),
					builder.getPanel().getPreferredSize());
		}
	}

	public void diffChartOld()
	{
		for (String alg : ALGORITHMS)
		{
			checkUniqueProps(algResults.get(alg), "FeatureSelection");

			ResultSet r = algResults.get(alg).join(new String[] { "FeatureSelection", "Dataset" },
					new String[] { "Run", "Fold" }, null);
			sortDatasets(r);
			r.sortResults("FeatureSelection", true);
			//			renameMethods(alg, r);
			System.out.println(r.toNiceString());
			r.clearMergeCountAndVariance();

			List<String> diff = ArrayUtil.toList(ValidationResultsProvider.performanceMeasures);
			List<String> ratio = new ArrayList<>();
			if (diff.contains("Time"))
			{
				ratio.add("Time");
				diff.remove("Time");
			}
			r = r.diff("FeatureSelection", ArrayUtil.toList(new String[] { "Dataset" }), diff,
					ratio);
			System.out.println(r.toNiceString());

			ResultSetBoxPlot plot = new ResultSetBoxPlot(r, niceValues.get(alg),
					r.getUniqueValue("FeatureSelection").toString(), "FeatureSelection",
					ArrayUtil.toList(ValidationResultsProvider.performanceMeasures));
			plot.setHideMean(true);
			plot.setRotateXLabels(false);
			plot.printNumResultsPerPlot(false);
			plot.setPrintMeanAndStdev(true);

			plot.setYTickUnit(0.05);
			plot.setYRange(-0.15, 0.15);

			ChartPanel c = plot.getChart();
			c.setMaximumDrawWidth(10000);
			c.setMaximumDrawHeight(5000);
			c.setMinimumDrawWidth(200);
			c.setMinimumDrawHeight(200);
			Dimension size = new Dimension(250, 250);
			c.setPreferredSize(size);

			String name = "Chart_Diff_" + alg + "_" + setName + "_m"
					+ ValidationResultsProvider.performanceMeasures.length;
			if (showCharts)
				SwingUtil.showInFrame(c, name, false);

			if (write)
			{
				plot.boxPlotToSVGFile(destFolder + name + ".svg", size);
				toPDF(destFolder + name);
			}
		}
	}

	//	public void chartFiltering()
	//	{
	//		for (String alg : ALGORITHMS)
	//		{
	//			ResultSet r = algResults.get(alg).join(new String[] { "Method", "Dataset" },
	//					new String[] { "Run", "Fold" }, null);
	//			r = r.filter(new ResultSetFilter()
	//			{
	//				@Override
	//				public boolean accept(Result result)
	//				{
	//					return result.getValue("Method").toString().contains("1024");
	//				}
	//			});
	//			//System.out.println(r.toNiceString());
	//			ResultSetLinePlot plot = new ResultSetLinePlot(r, new String[] { "AUC", "Accuracy", "Sensitivity",
	//					"Selectivity" }, "Method", "Dataset");
	//			plot.setTitle(null);
	//			//plot.setRotateXLabels(true);
	//			plot.setYAxisRange(0.0, 1.0);
	//
	//			//				{
	//			//					ResultSet test = results.pairedTTest("Method",
	//			//							ArrayUtil.toList(new String[] { "Run", "Fold" }), "AUC", SIG, "Dataset");
	//			//					for (Object datasetWins : ResultSet.listSeriesWins(test, "Method", "AUC", "Dataset",
	//			//							"RandomForest ecfp_filt_1024", "RandomForest ecfp_fold_1024"))
	//			//						plot.setDrawShape("AUC", datasetWins.toString(), "RandomForest ecfp_filt_1024");
	//			//					for (Object datasetWins : ResultSet.listSeriesWins(test, "Method", "AUC", "Dataset",
	//			//							"RandomForest ecfp_fold_1024", "RandomForest ecfp_filt_1024"))
	//			//						plot.setDrawShape("AUC", datasetWins.toString(), "RandomForest ecfp_fold_1024");
	//			//					System.out.println(test.toNiceString());
	//			//				}
	//
	//			ChartPanel c = plot.getChartPanel();
	//			//				c.setMaximumDrawWidth(10000);
	//			//				c.setMaximumDrawHeight(5000);
	//			//c.setPreferredSize(new Dimension(800, 600));
	//			SwingUtil.showInDialog(c);
	//			if (write)
	//			{
	//				plot.toSVGFile(destFolder + "Chart_FiltVsFold_1024_" + alg + ".svg", new Dimension(800, 600));
	//				toPDF(destFolder + "Chart_FiltVsFold_1024_" + alg);
	//			}
	//		}
	//	}

	//	public void tableWinLossSizes()
	//	{
	//		for (String alg : ALGORITHMS)
	//		{
	//			if (algResults.get(alg).getResultValues("Method").getNumValues() != SIZES.length)
	//				throw new Error("Methods differ in more aspects but sizes "
	//						+ algResults.get(alg).getResultValues("Method").values());
	//
	//			//reverse sort to have 1024 at the bottom, and therefore on the right side (method_2)
	//			algResults.get(alg).sortResults("Method", false, false, -1);
	//			String compareAgainstMethod = algResults.get(alg)
	//					.getResultValue(algResults.get(alg).getNumResults() - 1, "Method").toString();
	//
	//			ResultSet t1 = new ResultSet();
	//			for (String s : ValidationResultsProvider.performanceMeasures)
	//			{
	//				ResultSet set = algResults.get(alg).pairedTTestWinLoss("Method",
	//						ArrayUtil.toList(new String[] { "Run", "Fold" }), s, SIG_LEVEL, TEST_CORRECTION, "Dataset",
	//						true);
	//				set.sortResults("Method_1");
	//				int resCount = 0;
	//				for (int i = 0; i < set.getNumResults(); i++)
	//				{
	//					String method = set.getResultValue(i, "Method_1").toString();
	//					String method2 = set.getResultValue(i, "Method_2").toString();
	//					if (!method2.equals(compareAgainstMethod))
	//						continue;
	//					if (t1.getNumResults() - 1 < resCount)
	//						t1.addResult();
	//					t1.setResultValue(resCount, "Num bits", renameMethod(alg, method));//method.substring(method.lastIndexOf("_") + 1));
	//					t1.setResultValue(resCount, "vs num bits", renameMethod(alg, method2));//method.substring(method.lastIndexOf("_") + 1));
	//					t1.setResultValue(resCount, s, set.getResultValue(i, s));
	//					resCount++;
	//				}
	//			}
	//			System.out.println(alg + " " + params);
	//			for (String p : niceValues.keySet())
	//				t1.setNicePropery(p, niceValues.get(p));
	//			System.out.println(t1.toNiceString());
	//
	//			if (write)
	//			{
	//				String dest = destFolder + "Table_WinLoss_Size_" + alg + paramsStr + ".tex";
	//				System.out.println("write table to " + dest);
	//				FileUtil.writeStringToFile(dest, t1.toLatexTable());
	//				//			System.out.println(t1.toLatexTable());
	//			}
	//		}
	//	}

	//	public String algFromMethod(String m)
	//	{
	//		for (String s : WEKA_ALGORITHM_NAMES.keySet())
	//			if (m.contains(s))
	//				return WEKA_ALGORITHM_NAMES.get(s);
	//		for (String s : ORIG_ALGORITHMS)
	//			if (m.equals(s))
	//				return s;
	//		throw new IllegalArgumentException(m);
	//	}
	//
	//	public CFPType fpFromMethod(String m)
	//	{
	//		for (CFPType t : CFPType.values())
	//		{
	//			if (m.contains(t.toString()))
	//				return t;
	//			if (m.contains(t.toNiceString()))
	//				return t;
	//			if (params.contains(t.toString()))
	//				return t;
	//		}
	//		if (ArrayUtil.indexOf(ORIG_ALGORITHMS, m) != -1 || ArrayUtil.indexOf(ORIG_ALGORITHMS_NICE, m) != -1)
	//			return null;
	//		throw new IllegalArgumentException(m);
	//	}
	//
	//	public FeatureSelection featFromMethod(String m)
	//	{
	//		for (FeatureSelection t : FeatureSelection.values())
	//		{
	//			if (m.contains(t.toString()))
	//				return t;
	//			if (m.contains(t.toNiceString()))
	//				return t;
	//			if (params.contains(t.toString()))
	//				return t;
	//		}
	//		if (ArrayUtil.indexOf(ORIG_ALGORITHMS, m) != -1 || ArrayUtil.indexOf(ORIG_ALGORITHMS_NICE, m) != -1)
	//			return null;
	//		throw new IllegalArgumentException(m);
	//	}
	//
	//	public static String sizeFromMethod(String m)
	//	{
	//		for (String s : SIZES)
	//			if (m.contains(s))
	//				return s;
	//		if (m.contains(FeatureSelection.none.toString()) || m.contains(FeatureSelection.none.toNiceString()))
	//			return null;
	//		if (ArrayUtil.indexOf(ORIG_ALGORITHMS, m) != -1 || ArrayUtil.indexOf(ORIG_ALGORITHMS_NICE, m) != -1)
	//			return null;
	//		throw new IllegalArgumentException(m);
	//	}

	enum WinLossOption
	{
		compareAll, compareToLast, compareSameDiameter, compareToDiameter4
	}

	public void tableWinLoss(String name, WinLossOption opt, String property)
	{
		tableWinLoss(name, opt, property, null);
	}

	static interface SplitIterator<T> extends Iterator<T>
	{
		public Object getSplitValue();
	}

	private static Comparator<Object> getAlgComparator()
	{
		return new Comparator<Object>()
		{
			@Override
			public int compare(Object o1, Object o2)
			{
				Integer i1 = ArrayUtil.indexOf(ORIG_ALGORITHMS, (String) o1);
				Integer i2 = ArrayUtil.indexOf(ORIG_ALGORITHMS, (String) o2);
				return i1.compareTo(i2);
			}
		};
	}

	private static SplitIterator<ResultSet> split(final ResultSet set, final String splitProp)
	{

		final List<Object> splitValues;
		if (splitProp != null)
			splitValues = set.getResultValues(splitProp).values();
		else
			splitValues = null;
		if (splitProp != null && splitProp.equals("Algorithm"))
		{
			Collections.sort(splitValues, getAlgComparator());
		}

		return new SplitIterator<ResultSet>()
		{
			int idx = 0;
			Object splitValue = null;

			@Override
			public boolean hasNext()
			{
				if (splitValues == null)
					return idx == 0;
				else
					return splitValues != null && idx < splitValues.size();
			}

			@Override
			public ResultSet next()
			{
				if (splitValues == null)
				{
					idx++;
					return set;
				}
				else
				{
					splitValue = splitValues.get(idx);
					ResultSet r = set.filter(new ResultSetFilter()
					{
						@Override
						public boolean accept(Result result)
						{
							return result.getValue(splitProp).equals(splitValue);
						}
					});
					idx++;
					return r;
				}
			}

			@Override
			public void remove()
			{
			}

			@Override
			public Object getSplitValue()
			{
				return splitValue;
			}
		};
	}

	public void tableWinLoss(String name, WinLossOption opt, String property, String splitProp)
	{
		ResultSet all = algResults.get(ALL_ALGS);
		all.removePropery("hashfoldSize");
		// sort to get last element in props on the right side of the win loss table
		if (opt == WinLossOption.compareToLast)
			all.sortResults(property);
		else if (opt == WinLossOption.compareToDiameter4)
		{
			final int[] sort = { 4, 0, 2, 6 };
			all.sortResults(property, new Comparator<Object>()
			{
				@Override
				public int compare(Object o1, Object o2)
				{
					int d1 = ((CFPType) o1).getDiameter();
					int d2 = ((CFPType) o2).getDiameter();
					return new Integer(ArrayUtil.indexOf(sort, d1))
							.compareTo(new Integer(ArrayUtil.indexOf(sort, d2)));
				}
			});
		}
		Object last = all.getResultValue(all.getNumResults() - 1, property);

		String preProp = "";
		Boolean leftHline[] = new Boolean[] { false };

		int numSplitValues = 0;
		int numResultsPerSplit = -1;
		ResultSet table = new ResultSet();
		for (String measure : ValidationResultsProvider.performanceMeasures)
		{
			System.out.println(measure);
			SplitIterator<ResultSet> it = split(all, splitProp);
			numSplitValues = 0;
			while (it.hasNext())
			{
				ResultSet r = it.next();
				Object splitValue1 = it.getSplitValue();
				if (splitValue1 != null)
					System.out.println(" " + splitValue1);

				if (numResultsPerSplit == -1)
					numResultsPerSplit = r.getNumResults();
				else if (numResultsPerSplit != r.getNumResults())
					throw new IllegalArgumentException();
				//			System.out.println("filtered");

				//				System.out.println(algResults.get(alg).join(equalProps(property), null, null).toNiceString());

				ResultSet set = r.pairedTTestWinLoss(property, new String[] { "Run", "Fold" },
						//ArrayUtil.toList(new String[] { "Run", "Fold", propertys[1] }),
						measure, SIG_LEVEL, TEST_CORRECTION, seriesProps(property), true);
				//			set.sortResults("Method_1");

				int resCount = 0;
				for (int i = 0; i < set.getNumResults(); i++)
				{
					Object v1 = set.getResultValue(i, property + "_1");
					Object v2 = set.getResultValue(i, property + "_2");
					if (opt == WinLossOption.compareToLast && !v2.equals(last))
						continue;
					else if (opt == WinLossOption.compareSameDiameter)
					{
						CFPType t1 = (CFPType) v1;
						CFPType t2 = (CFPType) v2;
						if (t1.getDiameter() != t2.getDiameter())
							continue;
					}
					else if (opt == WinLossOption.compareToDiameter4)
					{
						CFPType t1 = (CFPType) v1;
						if (t1.getDiameter() != 4)
							continue;
					}
					String pValue = niceValuesShort.get(v1.toString()) + " vs "
							+ niceValuesShort.get(v2.toString());
					if (table.getNumResults() - 1 < resCount)
					{
						table.addResult();
						table.setResultValue(resCount, property, pValue);
					}
					else if (!table.getResultValue(resCount, property).equals(pValue))
						throw new IllegalStateException();
					String p = "";
					if (splitValue1 != null)
						p = splitValue1 + " ";
					table.setResultValue(resCount, p + measure, set.getResultValue(i, measure));
					if (!p.isEmpty())
						table.setNicePropery(p + measure, p);
					resCount++;
				}
				numSplitValues++;
			}
			preProp += "& \\multicolumn{" + numSplitValues + "}{c}{" + measure + "} ";
			leftHline = ArrayUtil.push(leftHline, true);
			for (int i = 0; i < numSplitValues - 1; i++)
				leftHline = ArrayUtil.push(leftHline, false);
		}
		System.out.println();
		String perf = ArrayUtil.toString(ValidationResultsProvider.performanceMeasures, "-", "", "",
				"");
		System.out.println(name + " " + setName + " " + perf);
		for (String p : niceValues.keySet())
			table.setNicePropery(p, niceValues.get(p));
		System.out.println(table.toNiceString());
		if (write)
		{
			String fname = "Table_WinLoss_" + name + "_" + setName + "_" + perf + ".tex";
			String dest = destFolder + fname;
			System.out.println("write table to " + fname);
			preProp += "\\\\";
			if (numSplitValues == 1 || ValidationResultsProvider.performanceMeasures.length == 1)
			{
				leftHline = null;
				preProp = null;
			}
			FileUtil.writeStringToFile(dest, table.toLatexTable(null, leftHline, preProp));
		}

	}

	//	public void plotWinLossECFPvsFCFP(boolean onlyDefaultSize, boolean filter) throws Exception
	//	{
	//		for (String measure : ValidationResultsProvider.performanceMeasures)
	//		{
	//			HashMap<String, ResultSet> results = new LinkedHashMap<>();
	//			read("all", RemoveObsoleteSizes.no, getFilter(null, null, FeatureSelection.none, null));
	//			results.put(FeatureSelection.none + "",
	//					winLoss(measure, WinLossOption.compareSameDiameter, "CFPType", "Algorithm"));
	//			read("filt" + defaultSize, RemoveObsoleteSizes.yes_remove_none,
	//					getFilter(null, null, FeatureSelection.filt, defaultSize));
	//			results.put(FeatureSelection.filt + "",
	//					winLoss(measure, WinLossOption.compareSameDiameter, "CFPType", "Algorithm"));
	//			plotWinLoss("WinLoss_Types_" + measure, measure, "CFPType", "Algorithm", "FeatureSelection", results,
	//					"Fingerprint type", false);
	//		}
	//	}

	public void plotWinLossECFPvsFCFP(boolean onlyDefaultSize, boolean filter) throws Exception
	{
		String subCat;
		FeatureSelection sel;
		if (onlyDefaultSize)
			subCat = "FeatureSelection";
		else
			subCat = "hashfoldSize";
		if (filter)
			sel = FeatureSelection.filt;
		else
			sel = FeatureSelection.fold;

		for (String measure : ValidationResultsProvider.performanceMeasures)
		{
			HashMap<String, ResultSet> results = new LinkedHashMap<>();
			read("all", RemoveObsoleteSizes.no, getFilter(null, null, FeatureSelection.none, null));
			results.put((onlyDefaultSize ? (FeatureSelection.none + "") : "all"),
					winLoss(measure, WinLossOption.compareSameDiameter, "CFPType", "Algorithm"));
			for (Integer size : SIZES)
			{
				if (onlyDefaultSize && size != defaultSize)
					continue;
				read((filter ? "filt" : "fold") + size, RemoveObsoleteSizes.yes_remove_none,
						getFilter(null, null, sel, size));
				results.put((onlyDefaultSize ? (sel + "") : (size + "")), winLoss(measure,
						WinLossOption.compareSameDiameter, "CFPType", "Algorithm"));
			}
			String suffix = "_" + measure + "_" + sel
					+ (onlyDefaultSize ? ("_" + defaultSize) : "");
			plotWinLoss("WinLoss_Types" + suffix, measure, "CFPType", "Algorithm", subCat, results,
					(onlyDefaultSize ? "" : "(" + sel.toNiceString() + ") ") + "Fingerprint type",
					!onlyDefaultSize);
		}
	}

	public void plotWinLossECFPDiameter(boolean onlyDefaultSize, boolean filter) throws Exception
	{
		String subCat;
		FeatureSelection sel;
		if (onlyDefaultSize)
			subCat = "FeatureSelection";
		else
			subCat = "hashfoldSize";
		if (filter)
			sel = FeatureSelection.filt;
		else
			sel = FeatureSelection.fold;

		for (String measure : ValidationResultsProvider.performanceMeasures)
		{
			HashMap<String, ResultSet> results = new LinkedHashMap<>();
			read("ecfpAll", RemoveObsoleteSizes.no,
					getFilter(null, true, FeatureSelection.none, null));
			results.put((onlyDefaultSize ? (FeatureSelection.none + "") : "all"),
					winLoss(measure, WinLossOption.compareToDiameter4, "CFPType", "Algorithm"));
			for (Integer size : SIZES)
			{
				if (onlyDefaultSize && size != defaultSize)
					continue;
				read("ecfp" + (filter ? "Filt" : "Fold") + size,
						RemoveObsoleteSizes.yes_remove_none, getFilter(null, true, sel, size));
				results.put((onlyDefaultSize ? (sel + "") : (size + "")),
						winLoss(measure, WinLossOption.compareToDiameter4, "CFPType", "Algorithm"));
			}
			String suffix = "_" + measure + "_" + sel
					+ (onlyDefaultSize ? ("_" + defaultSize) : "");
			plotWinLoss("WinLoss_Diameters" + suffix, measure, "CFPType", "Algorithm", subCat,
					results, (onlyDefaultSize ? "" : "(" + sel.toNiceString() + ") ") + "Diameter",
					!onlyDefaultSize);
		}
	}

	public void plotWinLossSizes() throws Exception
	{
		for (String measure : ValidationResultsProvider.performanceMeasures)
		{
			HashMap<String, ResultSet> results = new LinkedHashMap<>();
			for (Integer size : SIZES)
			{
				read("ecfp4_" + size + "orAll", RemoveObsoleteSizes.no,
						getFilter(CFPType.ecfp4, null, null, size),
						getFilter(CFPType.ecfp4, null, FeatureSelection.none, null));
				results.put(size + "", winLoss(measure, WinLossOption.compareAll,
						"FeatureSelection", "Algorithm"));
			}
			plotWinLoss("WinLoss_FiltVsFoldVsAll_" + measure, measure, "FeatureSelection",
					"Algorithm", "hashfoldSize", results, niceValues.get("FeatureSelection"), true);
		}
	}

	private void plotWinLoss(String name, String measure, String winLossCmp, String cat1,
			String cat2, HashMap<String, ResultSet> results, String title, boolean fullPage)
	{
		ResultSet plot = null;
		for (String k : results.keySet())
		{
			ResultSet set = results.get(k);
			for (int i = 0; i < set.getNumResults(); i++)
				set.setResultValue(i, cat2, k);
			if (plot == null)
				plot = set;
			else
				plot.concat(set);
		}
		for (String p : niceValues.keySet())
			plot.setNicePropery(p, niceValues.get(p));
		renameResultValues(plot, !fullPage);
		System.err.println(plot.toNiceString());
		//		System.exit(0);
		WinLossBarChart chart = new WinLossBarChart(plot, winLossCmp, measure, cat1, cat2);

		chart.setTitle(
				"" + title + " comparison (" + (fullPage ? "based on " : "") + "" + measure + ")");
		Dimension dim = new Dimension(fullPage ? 800 : 400, WinLossBarChart.SPACE_FOR_TITLE
				+ WinLossBarChart.SPACE_FOR_LEGEND_AND_X_AXIS + 150 * chart.getNumSubPlots());
		if (showCharts)
		{
			SwingUtil.showInFrame(chart.getChart(), dim);
		}
		if (write)
		{
			chart.toPNGFile(destFolder + name + ".png", dim);
		}
	}

	public ResultSet winLoss(String measure, WinLossOption opt, final String property,
			String splitProp)
	{
		ResultSet all = algResults.get(ALL_ALGS);
		all.removePropery("hashfoldSize");
		// sort to get last element in props on the right side of the win loss table
		if (opt == WinLossOption.compareToLast)
			all.sortResults(property);
		else if (opt == WinLossOption.compareToDiameter4)
		{
			final int[] sort = { 4, 0, 2, 6 };
			all.sortResults(property, new Comparator<Object>()
			{
				@Override
				public int compare(Object o1, Object o2)
				{
					int d1 = ((CFPType) o1).getDiameter();
					int d2 = ((CFPType) o2).getDiameter();
					return new Integer(ArrayUtil.indexOf(sort, d1))
							.compareTo(new Integer(ArrayUtil.indexOf(sort, d2)));
				}
			});
		}

		ResultSet rsReturn = null;
		SplitIterator<ResultSet> it = split(all, splitProp);
		while (it.hasNext())
		{
			ResultSet r = it.next();
			Object splitValue1 = it.getSplitValue();
			if (splitValue1 != null)
				System.out.println(" " + splitValue1 + " #results:" + r.getNumResults());

			ResultSet set = r.pairedTTestWinLoss(property, new String[] { "Run", "Fold" }, measure,
					SIG_LEVEL, TEST_CORRECTION, seriesProps(property), true);
			System.out.println("win loss result: #" + set.getNumResults());

			if (opt == WinLossOption.compareToDiameter4)
			{
				set = set.filter(new ResultSetFilter()
				{
					@Override
					public boolean accept(Result result)
					{
						return result.getValue(property + "_1").toString().equals("ecfp4");
					}
				});
			}
			else if (opt == WinLossOption.compareSameDiameter)
			{
				set = set.filter(new ResultSetFilter()
				{
					@Override
					public boolean accept(Result result)
					{
						return ((CFPType) result.getValue(property + "_1"))
								.getDiameter() == ((CFPType) result.getValue(property + "_2"))
										.getDiameter();
					}
				});
			}

			for (int i = 0; i < set.getNumResults(); i++)
			{
				set.setResultValue(i, splitProp, splitValue1);
				set.setResultValue(i, "Measure", measure);
			}
			if (rsReturn == null)
				rsReturn = set;
			else
				rsReturn.concat(set);
		}
		return rsReturn;
	}

	//	public void tableWinLoss(String name, WinLossOption opt, String property)
	//	{
	//		ResultSet all = null;
	//
	//		for (String alg : ALGORITHMS)
	//		{
	//			//			String property;
	//			//			if (propertys.length == 1)
	//			//			{
	//			//				property = propertys[0];
	//			//				checkUniqueProps(algResults.get(alg), propertys);
	//			//			}
	//			//			else
	//			//			{
	//			//				property = propertys[0];
	//			//				//property = combineProps(algResults.get(alg), propertys);
	//			//				checkUniqueProps(algResults.get(alg), propertys);
	//			//			}
	//
	//			algResults.get(alg).removePropery("hashfoldSize");
	//
	//			// sort to get last element in props on the right side of the win loss table
	//			algResults.get(alg).sortResults(property);
	//			Object last = algResults.get(alg).getResultValue(algResults.get(alg).getNumResults() - 1, property);
	//
	//			ResultSet t1 = new ResultSet();
	//			for (String s : ValidationResultsProvider.performanceMeasures)
	//			{
	//				//				System.out.println(algResults.get(alg).join(equalProps(property), null, null).toNiceString());
	//
	//				ResultSet set = algResults.get(alg).pairedTTestWinLoss(property, new String[] { "Run", "Fold" },
	//				//ArrayUtil.toList(new String[] { "Run", "Fold", propertys[1] }),
	//						s, SIG_LEVEL, TEST_CORRECTION, seriesProps(property), true);
	//				//			set.sortResults("Method_1");
	//				System.out.println(set.toNiceString());
	//
	//				int resCount = 0;
	//				for (int i = 0; i < set.getNumResults(); i++)
	//				{
	//					Object v1 = set.getResultValue(i, property + "_1");
	//					Object v2 = set.getResultValue(i, property + "_2");
	//					if (opt == WinLossOption.compareToLast && !v2.equals(last))
	//						continue;
	//					if (t1.getNumResults() - 1 < resCount)
	//						t1.addResult();
	//					t1.setResultValue(resCount, property, v1 + " vs " + v2);
	//					t1.setResultValue(resCount, s, set.getResultValue(i, s));
	//					resCount++;
	//				}
	//			}
	//			System.out.println();
	//			System.out.println(name + " " + alg + " " + params);
	//			for (String p : niceValues.keySet())
	//				t1.setNicePropery(p, niceValues.get(p));
	//			System.out.println(t1.toNiceString());
	//			if (write)
	//			{
	//				String dest = destFolder + "Table_WinLoss_" + name + "_" + alg + paramsStr + ".tex";
	//				System.out.println("write table to " + dest);
	//				FileUtil.writeStringToFile(dest, t1.toLatexTable());
	//			}
	//
	//			t1.getProperties().add(0, "Algorithm");
	//			for (int i = 0; i < t1.getNumResults(); i++)
	//			{
	//				t1.setResultValue(i, "Algorithm", niceValuesShort.get(alg));
	//			}
	//			if (all == null)
	//				all = t1;
	//			else
	//				all.concat(t1);
	//		}
	//		if (ALGORITHMS.length == 1)
	//			return;
	//		System.out.println(all.toNiceString());
	//		if (write)
	//		{
	//			String dest = destFolder + "Table_WinLoss_" + name + paramsStr + ".tex";
	//			System.out.println("write table to " + dest);
	//			FileUtil.writeStringToFile(dest, all.toLatexTable());
	//		}
	//	}

	//	enum WinLossCompareAgainst
	//	{
	//		sameSize, sameDiameter, sameDiameterAndFeat, lastProp, all
	//	}

	//	public void tableWinLoss(String name, WinLossCompareAgainst cmp, final String... props)
	//	{
	//		ResultSet all = null;
	//
	//		for (String alg : ALGORITHMS)
	//		{
	//			// sort to get last element in props on the right side of the win loss table
	//			algResults.get(alg).sortResults("Method", new Comparator<Object>()
	//			{
	//				@Override
	//				public int compare(Object o1, Object o2)
	//				{
	//					String s1 = o1.toString();
	//					String s2 = o2.toString();
	//					Integer i1 = ArrayUtil.containsAt(s1, props);
	//					Integer i2 = ArrayUtil.containsAt(s2, props);
	//					return i1.compareTo(i2);
	//				}
	//			});
	//
	//			ResultSet t1 = new ResultSet();
	//			for (String s : ValidationResultsProvider.performanceMeasures)
	//			{
	//				ResultSet set = algResults.get(alg).pairedTTestWinLoss("Method",
	//						ArrayUtil.toList(new String[] { "Run", "Fold" }), s, SIG_LEVEL, TEST_CORRECTION, "Dataset",
	//						true);
	//				//			set.sortResults("Method_1");
	//
	//				int resCount = 0;
	//				for (int i = 0; i < set.getNumResults(); i++)
	//				{
	//					String method = set.getResultValue(i, "Method_1").toString();
	//					String size = sizeFromMethod(method);
	//					CFPType type = fpFromMethod(method);
	//					FeatureSelection feat = featFromMethod(method);
	//					String method2 = set.getResultValue(i, "Method_2").toString();
	//					String size2 = sizeFromMethod(method2);
	//					CFPType type2 = fpFromMethod(method2);
	//					FeatureSelection feat2 = featFromMethod(method2);
	//					if (ArrayUtil.containsAt(method, props) == -1 || ArrayUtil.containsAt(method2, props) == -1)
	//						continue;
	//					if (cmp == WinLossCompareAgainst.sameDiameter && type.getDiameter() != type2.getDiameter())
	//						continue;
	//					if (cmp == WinLossCompareAgainst.sameDiameterAndFeat && type.getDiameter() != type2.getDiameter())
	//						continue;
	//					if (cmp == WinLossCompareAgainst.sameDiameterAndFeat && feat != feat2)
	//						continue;
	//					if (cmp == WinLossCompareAgainst.lastProp && !method2.contains(ArrayUtil.last(props)))
	//						continue;
	//					if (cmp == WinLossCompareAgainst.sameSize && !ObjectUtil.equals(size, size2))
	//						continue;
	//					if (t1.getNumResults() - 1 < resCount)
	//						t1.addResult();
	//
	//					if (cmp == WinLossCompareAgainst.sameDiameterAndFeat)
	//						t1.setResultValue(resCount, "FeatureSelection", feat.toNiceString());
	//					if (cmp == WinLossCompareAgainst.sameSize)
	//						t1.setResultValue(resCount, "hashfoldSize", size);
	//
	//					if (cmp != WinLossCompareAgainst.sameSize)
	//					{
	//						boolean stripSize = cmp == WinLossCompareAgainst.sameSize;
	//						boolean stripFeat = cmp == WinLossCompareAgainst.sameDiameterAndFeat;
	//
	//						String methodProp = "Method";
	//						if (name.equals("Sizes"))
	//							methodProp = niceValues.get("hashfoldSize");
	//						if (name.equals("FCFPvsECFP") || name.equals("ECFPvsFCFP"))
	//							methodProp = "CFPType";
	//
	//						t1.setResultValue(resCount, methodProp, renameMethod(alg, method, stripSize, stripFeat)
	//								+ " vs " + renameMethod(alg, method2, stripSize, stripFeat));
	//						//							t1.setResultValue(resCount, "vs Method",
	//						//									renameMethod(alg, method2, cmp == WinLossCompareAgainst.sameSize));
	//					}
	//
	//					t1.setResultValue(resCount, s, set.getResultValue(i, s));
	//					resCount++;
	//				}
	//			}
	//			if (cmp == WinLossCompareAgainst.sameDiameterAndFeat)
	//				t1.sortResults("FeatureSelection", false);
	//
	//			System.out.println();
	//			System.out.println(name + " " + alg + " " + params);
	//			for (String p : niceValues.keySet())
	//				t1.setNicePropery(p, niceValues.get(p));
	//			System.out.println(t1.toNiceString());
	//			if (write)
	//			{
	//				String dest = destFolder + "Table_WinLoss_" + name + "_" + alg + paramsStr + ".tex";
	//				System.out.println("write table to " + dest);
	//				FileUtil.writeStringToFile(dest, t1.toLatexTable());
	//			}
	//
	//			if (cmp == WinLossCompareAgainst.sameDiameter && params.equals("ecfp"))
	//			{
	//				System.out.println("hooray");
	//			}
	//
	//			t1.getProperties().add(0, "Algorithm");
	//			for (int i = 0; i < t1.getNumResults(); i++)
	//			{
	//				t1.setResultValue(i, "Algorithm", niceValuesShort.get(alg));
	//			}
	//			if (all == null)
	//				all = t1;
	//			else
	//				all.concat(t1);
	//		}
	//		if (ALGORITHMS.length == 1)
	//			return;
	//		System.out.println(all.toNiceString());
	//		if (write)
	//		{
	//			String dest = destFolder + "Table_WinLoss_" + name + paramsStr + ".tex";
	//			System.out.println("write table to " + dest);
	//			FileUtil.writeStringToFile(dest, all.toLatexTable());
	//		}
	//	}

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
		for (String alg : ALGORITHMS)
		{
			ResultSet r = algResults.get(alg).join(new String[] { "Method", "Dataset" },
					new String[] { "Run", "Fold" }, ValidationResultsProvider.performanceMeasures);
			//r.sortResults("Method");
			//System.out.println(r.toNiceString());

			List<String> datasets = ListUtil.cast(String.class,
					r.getResultValues("Dataset").values());
			Collections.sort(datasets, DataLoader.CFPDataComparator);

			for (String m : ValidationResultsProvider.performanceMeasures)
			{
				ResultSet test = algResults.get(alg).pairedTTest("Method",
						new String[] { "Run", "Fold" }, m, SIG_LEVEL, TEST_CORRECTION,
						new String[] { "Dataset" });

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

								String value = Math.round((Double) val * 100) + "+-"
										+ Math.round((Double) var * 100);

								if (!k2.equals(paramKeys2[0]))
								{
									String method2 = method.replaceAll(k2, paramKeys2[0]);
									Boolean win = ResultSet.isWinOrLoss(test, "Method", method,
											method2, m, "Dataset", dataset);
									if (win != null)
									{
										if (win)
											value += Character.toString((char) 0x2022);
										else
											value += Character.toString((char) 0x25E6);
									}
								}

								t2.setResultValue(datasets.indexOf(dataset), p, value);
								t2.setNicePropery(p,
										niceValues.containsKey(k2) ? niceValues.get(k2) : k2);
							}
						}
					}
				}
				preProp += "\\\\";
				System.out.println(preProp);
				System.out.println(t2.toNiceString());

				if (write)
				{
					String dest = destFolder + "Table_" + name + "_" + alg + setName + "_" + m
							+ ".tex";
					System.out.println("write table to " + dest);
					FileUtil.writeStringToFile(dest,
							t2.toLatexTable(null, ArrayUtil.toArray(hlineLeadingColumn), preProp));
				}
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
		new ExternalTool(null).run("to-pdf", ("rsvg-convert -f pdf -o " + svgFileWithoutExtension
				+ ".pdf " + svgFileWithoutExtension + ".svg").split(" "));
	}

	public static void main(String[] args) throws Exception
	{
		new CreatePaperResults(args.length > 0 && args[0].equals("debug"));
		//		
		if (Window.getWindows().length > 0)
			System.out.println("\nDone - Waiting for Windows to close");
		SwingUtil.waitWhileWindowsVisible();
		System.exit(0);
	}

	// -----------------------------------------------------------------------------------------
	// helper methods for results set
	// -----------------------------------------------------------------------------------------

	//	public void filter(String filterName, ResultSetFilter... filter)
	//	{
	//		this.filterName = "_" + filterName;
	//		for (String alg : ALGORITHMS)
	//			algResults.put(alg, filter(algResults.get(alg), filter));
	//	}

	// -----------------------------------------------------------------------------------------
	// static helper methods for results set
	// -----------------------------------------------------------------------------------------

	//	public static ResultSet filter(ResultSet set, final ResultSetFilter... filter)
	//	{
	//		ResultSet r = set.filter(new ResultSetFilter()
	//		{
	//			@Override
	//			public boolean accept(Result result)
	//			{
	//				for (ResultSetFilter f : filter)
	//					if (f.accept(result))
	//						return true;
	//				return false;
	//			}
	//		});
	//		if (r.getNumResults() == 0)
	//			throw new IllegalStateException("nothing left");
	//		return r;
	//	}

	public static interface ResFilter extends ResultSetFilter
	{
		public String getKey();
	}

	public static class CompFilter implements ResFilter
	{
		ResFilter filter[];

		public CompFilter(ResFilter... filter)
		{
			this.filter = filter;
		}

		@Override
		public String getKey()
		{
			String key = "";
			for (ResFilter f : filter)
				key += f.getKey() + "#";
			return key;
		}

		@Override
		public boolean accept(Result result)
		{
			for (ResFilter f : filter)
				if (f.accept(result))
					return true;
			return false;
		}
	}

	public static ResFilter getFilter(final CFPType t, final Boolean isECFP,
			final FeatureSelection f, final Integer s)
	{
		return new ResFilter()
		{
			@Override
			public String getKey()
			{
				return t + "-" + isECFP + "-" + f + "-" + s;
			}

			@Override
			public boolean accept(Result result)
			{
				if (t != null && type(result) != t)
					return false;
				if (isECFP != null && type(result).isECFP() != isECFP)
					return false;
				if (f != null && feat(result) != f)
					return false;
				if (s != null && !ObjectUtil.equals(size(result), s))
					return false;
				return true;
			}
		};
	}

	public static CFPType type(ResultSet set, int idx)
	{
		return (CFPType) set.getResultValue(idx, "CFPType");
	}

	public static CFPType type(Result res)
	{
		return (CFPType) res.getValue("CFPType");
	}

	public static FeatureSelection feat(ResultSet set, int idx)
	{
		return (FeatureSelection) set.getResultValue(idx, "FeatureSelection");
	}

	public static FeatureSelection feat(Result res)
	{
		return (FeatureSelection) res.getValue("FeatureSelection");
	}

	public static Integer size(ResultSet set, int idx)
	{
		return (Integer) set.getResultValue(idx, "hashfoldSize");
	}

	public static Integer size(Result res)
	{
		return (Integer) res.getValue("hashfoldSize");
	}

	public static String alg(ResultSet set, int idx)
	{
		return set.getResultValue(idx, "Algorithm").toString();
	}

	public static String alg(Result res)
	{
		return res.getValue("Algorithm").toString();
	}

	public static int getResultIdx(ResultSet set, CFPType type, Integer size, FeatureSelection feat)
	{
		for (int i = 0; i < set.getNumResults(); i++)
			if (type(set, i) == type && feat(set, i) == feat
					&& ObjectUtil.equals(size(set, i), size))
				return i;
		return -1;
	}

	public static String combineProps(ResultSet set, String... props)
	{
		String comb = ArrayUtil.toString(props, " ", "", "", "");
		for (int i = 0; i < set.getNumResults(); i++)
		{
			String v = null;
			for (String p : props)
			{
				if (v == null)
					v = "";
				else
					v += " ";
				v += set.getResultValue(i, p);
			}
			set.setResultValue(i, comb, v);
		}
		for (String p : props)
			set.removePropery(p);
		return comb;
	}

	public static void checkUniqueProps(ResultSet set, String... notUniqProp)
	{
		for (String p : new String[] { "Algorithm", "CFPType", "FeatureSelection", "hashfoldSize" })
		{
			if (!set.hasProperty(p))
				continue;
			if (ArrayUtil.indexOf(notUniqProp, p) != -1)
				continue;
			if (!set.isUniqueValue(p, !p.equals("hashfoldSize")))
				throw new IllegalStateException(p + " not unique: " + set.getResultValues(p));
			set.removePropery(p);
		}
	}

	//	public static List<String> equalProps(String notUniqProp)
	//	{
	//		List<String> l = ArrayUtil.toList(new String[] { "Algorithm", "CFPType", "FeatureSelection", "hashfoldSize",
	//				"Run", "Fold" });
	//		l.remove(notUniqProp);
	//		return l;
	//	}

	public static String[] seriesProps(String notUniqProp)
	{
		List<String> l = ArrayUtil.toList(new String[] { "Dataset", "Algorithm", "CFPType",
				"FeatureSelection", "hashfoldSize" });
		l.remove(notUniqProp);
		return ArrayUtil.toArray(l);
	}

	public static boolean uniqueSize(ResultSet res)
	{
		Integer size = size(res, 0);
		if (size == null)
			return false;
		for (Object s : res.getResultValues("hashfoldSize").values())
			if (!size.equals(s))
				return false;
		return true;
	}

}
