package org.kramerlab.cfpminer.weka;

import java.awt.Dimension;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jfree.chart.ChartPanel;
import org.kramerlab.cfpminer.CFPDataLoader;
import org.kramerlab.cfpminer.CFPMiner;
import org.kramerlab.cfpminer.CFPMiner.CFPType;
import org.kramerlab.cfpminer.CFPMiner.FeatureSelection;
import org.kramerlab.cfpminer.CFPUtil;
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
import org.mg.javalib.util.ObjectUtil;
import org.mg.javalib.util.StringUtil;
import org.mg.javalib.util.SwingUtil;
import org.mg.javalib.weka.MergeArffFiles;

public class CreatePaperResults
{

	public void paper() throws Exception
	{
		ALGORITHMS = new String[] { "SMO" };

		//		DATASETS = ArrayUtil.toArray(CFPDataLoader.listCategoryDatasets(CFPDataLoader.BALANCED_DATASETS));
		//		DATASETS = ArrayUtil.toArray(CFPDataLoader.listCategoryDatasets(CFPDataLoader.VS_DATASETS));
		//		DATASETS = ArrayUtil.toArray(CFPDataLoader.listSubCategoryDatasets("ChEMBL"));
		//		ArrayUtil.scramble(DATASETS, new Random(3));

		write = true;
		showCharts = true;

		//tableCollisions(false);
		//ranking("", "AUP");
		//datasets();

		//		read("ecfp4");
		//		filterMethods("AllOr1024", true, "1024", "none");
		//		addRuntimes();
		//		lineChart();
		//		ValidationResultsProvider.setPerformanceMeasures(new String[] { "AUP" });
		//		setParamsStr();
		//		lineChart();

		ValidationResultsProvider.setPerformanceMeasures(new String[] { "AUC", "AUP" });

		//		String size = "1024";
		//		read("ecfp4");
		//		filterMethods("AllOrFold" + size, true, "fold_" + size, "none");
		//		diffChart();
		//		read("ecfp4");
		//		filterMethods("AllOrFilt" + size, true, "filt_" + size, "none");
		//		diffChart();
		//		read("ecfp4_" + size);
		//		diffChart();

		//		String size = "1024";
		//		read("ecfp4");
		//		filterMethods(size + "orAll", true, size, "none");
		//		tableWinLoss("FiltVsFoldVsAll_" + size, WinLossCompareAgainst.all, "filt", "fold", "none");

		String size = "1024";
		read("ecfp");
		filterMethods(size + "orAll", true, size, "none");
		for (String alg : ALGORITHMS)
			remove_obs_sizes(alg);
		tableWinLoss("FiltVsFoldVsAll_" + size, WinLossCompareAgainst.sameDiameter, "filt", "fold", "none");

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
	static double SIG_LEVEL_RELAXED = 0.1;
	static Double TEST_CORRECTION = 1 / 9.0;
	static String[] SIZES = { "1024", "2048", "4096", "8192" };
	static String ALL_ALGS = "AllAlgs";
	static final HashMap<String, String> WEKA_ALGORITHM_NAMES = new HashMap<>();
	{
		WEKA_ALGORITHM_NAMES.put("RandomForest", "RaF");
		WEKA_ALGORITHM_NAMES.put("RandomFores2", "RnF");
		WEKA_ALGORITHM_NAMES.put("SMO", "SMO");
		WEKA_ALGORITHM_NAMES.put("NaiveBayes", "NBy");
		WEKA_ALGORITHM_NAMES.put("Vote", "Ens");
	}
	static String[] ORIG_ALGORITHMS = new String[] { "NBy", "RnF", "SMO" }; //, "Ens" };
	static String[] ORIG_ALGORITHMS_NICE = new String[ORIG_ALGORITHMS.length];
	static String[] ALGORITHMS = ORIG_ALGORITHMS;
	static String[] ALL_DATASETS = new CFPDataLoader("data").allDatasets();
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

	String params = "";
	String paramsStr = "";
	String filterName = "";
	//ValidationResultsProvider res;

	LinkedHashMap<String, ResultSet> algResults = new LinkedHashMap<>();
	boolean write = false;
	boolean showCharts = true;

	public void filterMethods(String filterName, final boolean include, final String... pattern)
	{
		this.filterName = "_" + filterName;
		for (String alg : ALGORITHMS)
		{
			algResults.put(alg, algResults.get(alg).filter(new ResultSetFilter()
			{
				@Override
				public boolean accept(Result result)
				{
					boolean match = false;
					for (String p : pattern)
						if (result.getValue("Method").toString().contains(p))
						{
							match = true;
							break;
						}
					if (include)
						return match;
					else
						return !match;
				}
			}));
		}
	}

	public void addRuntimes()
	{
		ValidationResultsProvider.setPerformanceMeasures(ArrayUtil.push(ValidationResultsProvider.performanceMeasures,
				"Time"));
		for (String alg : ALGORITHMS)
		{
			ResultSet r = algResults.get(alg);
			for (int i = 0; i < r.getNumResults(); i++)
			{
				String d = r.getResultValue(i, "Dataset").toString();
				String m = r.getResultValue(i, "Method").toString();
				CFPType t = fpFromMethod(m);
				FeatureSelection f = featFromMethod(m);
				int s = (f == FeatureSelection.none) ? 0 : Integer.parseInt(sizeFromMethod(m));
				double run = CFPUtil.getRuntime(d, t, f, s, alg, true);
				r.setResultValue(i, "Time", run);// 5 + new Random().nextDouble() * 20.0);
			}
		}
	}

	public void read(String params) throws Exception
	{
		read(ALGORITHMS, params);
	}

	private void setParamsStr()
	{
		paramsStr = params.isEmpty() ? "" : ("_" + params);
		if (DATASETS.length < ALL_DATASETS.length)
			paramsStr += "_d" + DATASETS.length;
		if (ValidationResultsProvider.performanceMeasures.length < ALL_PERFORMANCE_MEASURES.length)
			paramsStr += "_m" + ValidationResultsProvider.performanceMeasures.length;
	}

	public void read(String algs[], String params) throws Exception
	{
		for (String alg : algs)
		{
			this.params = params;
			setParamsStr();
			filterName = "";
			String name = alg + (params.isEmpty() ? "" : ("_" + params));
			System.out.println("reading " + name);
			ValidationResultsProvider valRes = new ValidationResultsProvider(
					ValidationResultsProvider.RESULTS_MERGED_FOLDER + name + ".arff");
			valRes.results = valRes.results.filter(new ResultSetFilter()
			{
				@Override
				public boolean accept(Result result)
				{
					return ArrayUtil.indexOf(DATASETS, result.getValue("Dataset").toString()) != -1;
				}
			});
			System.out.println("done");
			algResults.put(alg, valRes.results);
		}
	}

	public void renameDatasets(ResultSet results)
	{
		for (int i = 0; i < results.getNumResults(); i++)
		{
			String d = results.getResultValue(i, "Dataset").toString();
			results.setResultValue(i, "Dataset", d.replaceAll("_", " "));
		}
	}

	public void sortDatasets(ResultSet r)
	{
		r.sortResults("Dataset", CFPDataLoader.CFPDataComparator);
	}

	public void renameMethods(String alg)
	{
		renameMethods(alg, algResults.get(alg));
	}

	public void renameMethods(String alg, ResultSet results)
	{
		Boolean stripSize = oneSize(results);
		for (int i = 0; i < results.getNumResults(); i++)
		{
			String method = results.getResultValue(i, "Method").toString();
			results.setResultValue(i, "Method", renameMethod(alg, method, stripSize, false));
		}
	}

	public String renameMethod(String alg, String method)
	{
		return renameMethod(alg, method, false, false);
	}

	public String renameMethod(String alg, String method, boolean stripSize, boolean stripFeatureSelection)
	{
		if (alg != null)
		{
			if (alg.equals(ALL_ALGS))
				return algFromMethod(method);
			//				return niceValues.get(algFromMethod(method));
			for (String w : WEKA_ALGORITHM_NAMES.keySet())
				method = method.replace(w + " ", "");
		}
		if (params != null)
		{
			for (String p : params.split("_"))
				if (!p.equals("ecfp") && !p.equals("fcfp"))
					method = method.replace(p, "");
		}
		if (stripFeatureSelection)
		{
			for (FeatureSelection s : FeatureSelection.values())
			{
				method = method.replace(s.toString(), "");
			}
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

	public static void merge(final String alg, final String p, final String p2) throws FileNotFoundException,
			IOException
	{
		//		final boolean orP2 = false;

		//csvToArff("/home/martin/data/arffs/nctrer.csv", "/home/martin/data/arffs/nctrer.arff");
		String dir = ValidationResultsProvider.RESULTS_FOLDER;
		String dest = ValidationResultsProvider.RESULTS_MERGED_FOLDER + alg + (p == null ? "" : ("_" + p))
				+ (p2 == null ? "" : ("_" + p2)) + ".arff";

		if (!new File(dest).exists())
			MergeArffFiles.merge(dir, new FilenameFilter()
			{

				@Override
				public boolean accept(File dir, String name)
				{
					if (!name.contains(alg))
						return false;

					if (p != null
							&& (p.equals(CFPDataLoader.BALANCED_DATASETS) || p.equals("MUV") || p.equals("ChEMBL") || p
									.equals("DUD")))
					{
						final Set<String> cat = CFPDataLoader.listCategoryDatasets(p);
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

			merge(alg, "CPDBAS_Rat", null);

			merge(alg, "1024", null);
			merge(alg, "ecfp", null);
			merge(alg, "ecfp4", "1024");
			merge(alg, "ecfp4", "2048");

			merge(alg, CFPDataLoader.BALANCED_DATASETS, null);
			merge(alg, "MUV", null);
			merge(alg, "DUD", null);
			merge(alg, "ChEMBL", null);

			for (String feat : new String[] { "fold", "filt" })
			{
				merge(alg, feat, null);
				merge(alg, "ecfp", feat);
				merge(alg, "ecfp4", feat);
				merge(alg, feat, "1024");
			}

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

	public boolean skipFiltFoldMethod(CFPType type, int size)
	{
		if (type == CFPType.ecfp0 || type == CFPType.fcfp0 || type == CFPType.fcfp2)
			return true;
		if (type == CFPType.ecfp2)
			return size > 1024;
		if (type == CFPType.fcfp4)
			return size > 4096;
		return false;
	}

	// delete ecfp0/fcfp0/fcfp2 1024/2048/4096/8192
	// delete ecfp2 2048/4096/8192
	// delete fcfp4 8192
	public void remove_obs_sizes(String alg)
	{
		ResultSet results = algResults.get(alg);
		results = results.filter(new ResultSetFilter()
		{
			@Override
			public boolean accept(Result result)
			{
				String m = result.getValue("Method").toString();
				if (m.contains("_none"))
					return true;
				else if (m.contains("_filt_") || m.contains("_fold_"))
				{
					CFPType type = fpFromMethod(m);
					int size = Integer.parseInt(sizeFromMethod(m));
					return !skipFiltFoldMethod(type, size);
				}
				else
					throw new IllegalArgumentException(m);
			}
		});
		//		for (int i = 0; i < results.getNumResults(); i++)
		//		{
		//			String m = results.getResultValue(i, "Method").toString();
		//			if (m.contains("ecfp0_filt_1024"))
		//				results.setResultValue(i, "Method", m.replace("ecfp0_filt_1024", "ecfp0_" + FeatureSelection.none));
		//			if (m.contains("fcfp0_filt_1024"))
		//				results.setResultValue(i, "Method", m.replace("fcfp0_filt_1024", "fcfp0_" + FeatureSelection.none));
		//			if (m.contains("fcfp2_filt_1024"))
		//				results.setResultValue(i, "Method", m.replace("fcfp2_filt_1024", "fcfp2_" + FeatureSelection.none));
		//			if (m.contains("ecfp2_filt_8192"))
		//				results.setResultValue(i, "Method", m.replace("ecfp2_filt_8192", "ecfp2_" + FeatureSelection.none));
		//			if (m.contains("fcfp4_filt_8192"))
		//				results.setResultValue(i, "Method", m.replace("fcfp4_filt_8192", "fcfp4_" + FeatureSelection.none));
		//		}
		algResults.put(alg, results);
	}

	public CreatePaperResults(boolean debug) throws Exception
	{
		Locale.setDefault(Locale.US);
		merge();
		paper();
	}

	public void datasets()
	{
		CFPDataLoader d = new CFPDataLoader("data");
		String datasets[] = d.allDatasets();
		Arrays.sort(datasets, 0, datasets.length, CFPDataLoader.CFPDataComparator);

		{
			ResultSet r = d.getInfo(datasets);
			System.out.println(r.toNiceString());
			if (write)
			{
				String dest = destFolder + "datasets.tex";
				System.out.println("write table to " + dest);
				FileUtil.writeStringToFile(dest, r.toLatexTable());
			}
			System.out.println();
		}

		{
			ResultSet r = d.getCategoryInfo(datasets);
			System.out.println(r.toNiceString());
			if (write)
			{
				String dest = destFolder + "datasets_overview.tex";
				System.out.println("write table to " + dest);
				FileUtil.writeStringToFile(dest, r.toLatexTable());
			}
		}
	}

	/** leaves only a single method per algorithm and dataset in algResutls */
	public void estimateBest2(final String measure) throws Exception
	{
		int algCount = 1;
		for (String alg : ALGORITHMS)
		{
			System.out.println(alg);

			File f = new File(ValidationResultsProvider.RESULTS_MERGED_FOLDER + alg + "." + measure + ".d"
					+ DATASETS.length + ".best");
			if (false && f.exists())
			{
				this.params = measure;
				setParamsStr();
				algResults.put(alg, ResultSetIO.parseFromFile(f));
			}
			else
			{
				read(new String[] { alg }, "");
				remove_obs_sizes(alg);

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

					final ResultSet test = r.pairedTTest_All("Method",
							ArrayUtil.toList(new String[] { "Run", "Fold" }), measure, SIG_LEVEL_RELAXED, null);
					//System.err.println(test.toNiceString());

					r = r.join(new String[] { "Method", "Dataset" }, new String[] { "Run", "Fold" }, null);

					//					System.err.println();

					ResultSet winners = r.filter(new ResultSetFilter()
					{
						@Override
						public boolean accept(Result result)
						{
							Boolean w = ResultSet.isWinOrLoss(test, "Method", result.getValueToString("Method"),
									measure);
							return (w != null && w);
						}
					});
					if (winners.getNumResults() > 0)
						r = winners;
					else
						System.err.println("no differences");

					ResultSet r_non_fold = r.filter(new ResultSetFilter()
					{
						@Override
						public boolean accept(Result result)
						{
							return featFromMethod(result.getValue("Method").toString()) != FeatureSelection.fold;
						}
					});
					if (r_non_fold.getNumResults() > 0)
						r = r_non_fold;
					else
					{
						System.err.println(r.toNiceString());
						throw new IllegalStateException("damn, only fold left!");
					}

					r.sortResults("Features", true, true, -1);
					r.sortResults(measure, false);

					System.err.println(r.toNiceString());

					//					System.err.println();
					//					System.err.println(r_non_filt.toNiceString());
					//					System.exit(1);

					final String bestMethod = r.getResultValue(0, "Method").toString();

					algResults.put(alg, algResults.get(alg).filter(new ResultSetFilter()
					{
						@Override
						public boolean accept(Result result)
						{
							if (result.getValue("Dataset").toString().equals(dataset))
								return result.getValue("Method").equals(bestMethod);
							else
								return true;
						}
					}));
				}
				ResultSetIO.printToFile(f, algResults.get(alg), true);
			}
			//			System.out.println(algResults.get(alg).toNiceString());

			if (algResults.containsKey(ALL_ALGS))
				algResults.get(ALL_ALGS).concat(algResults.get(alg));
			else
				algResults.put(ALL_ALGS, algResults.get(alg));

			System.out.println(DATASETS.length * algCount++ * 5 * 10);
			System.out.println(algResults.get(ALL_ALGS).getNumResults());
		}

		System.out.println(algResults.get(ALL_ALGS)
				.join(new String[] { "Method", "Dataset" }, new String[] { "Run", "Fold" }, null).toNiceString());

		ALGORITHMS = new String[] { ALL_ALGS };
	}

	/** leaves only a single method per algorithm and dataset in algResutls */
	public void estimateBest(String measure) throws Exception
	{
		int algCount = 1;
		for (String alg : ALGORITHMS)
		{
			System.out.println(alg);

			File f = new File(ValidationResultsProvider.RESULTS_MERGED_FOLDER + alg + "." + measure + ".d"
					+ DATASETS.length + ".best");
			if (f.exists())
			{
				this.params = measure;
				setParamsStr();
				algResults.put(alg, ResultSetIO.parseFromFile(f));
			}
			else
			{
				read(new String[] { alg }, "");
				remove_obs_sizes(alg);

				for (final String dataset : DATASETS)
				{
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
					r = r.join(new String[] { "Method", "Dataset" }, new String[] { "Run", "Fold" }, null);
					r.sortResults("Method", new Comparator<Object>()
					{
						@Override
						public int compare(Object o1, Object o2)
						{
							String method = o1.toString();
							String method2 = o2.toString();
							String size = sizeFromMethod(method);
							String size2 = sizeFromMethod(method2);
							if (size == null)
								size = Integer.MAX_VALUE + "";
							if (size2 == null)
								size2 = Integer.MAX_VALUE + "";
							if (!size.equals(size2))
								return new Integer(size).compareTo(new Integer(size2));
							int diameter = fpFromMethod(method).getDiameter();
							int diameter2 = fpFromMethod(method2).getDiameter();
							if (diameter != diameter2)
								return new Integer(diameter).compareTo(new Integer(diameter2));
							FeatureSelection feat = featFromMethod(method);
							FeatureSelection feat2 = featFromMethod(method2);
							if (feat != feat2)
							{
								if (feat == FeatureSelection.filt)
									return -1;
								else if (feat2 == FeatureSelection.filt)
									return 1;
								if (feat == FeatureSelection.none)
									return -1;
								else if (feat2 == FeatureSelection.none)
									return 1;
							}
							return 0;
							//                                CFPType type = fpFromMethod(method);
							//                                CFPType type2 = fpFromMethod(method2);
							//                                if (type.getDiameter() != type2.getDiameter())
							//                                    return new Integer(type.getDiameter()).compareTo(new Integer(type.getDiameter()));
							//                                if (type != type2)
						}
					});
					r.sortResults("Features"); //, !dataset.equals("AMES"));
					r.sortResults(measure, false);
					final String bestMethod = r.getResultValue(0, "Method").toString();

					if (r.getResultValue(0, measure).equals(r.getResultValue(1, measure))
							&& r.getResultValue(0, "Features").equals(r.getResultValue(1, "Features"))
							&& sizeFromMethod(bestMethod).equals(
									sizeFromMethod(r.getResultValue(1, "Method").toString()))
							&& featFromMethod(bestMethod).equals(
									featFromMethod(r.getResultValue(1, "Method").toString()))
							&& fpFromMethod(bestMethod).getDiameter() == fpFromMethod(
									r.getResultValue(1, "Method").toString()).getDiameter())
						throw new Error("equal: auc && #features && size && diam && feat-sel:\n" + r.toNiceString());

					algResults.put(alg, algResults.get(alg).filter(new ResultSetFilter()
					{
						@Override
						public boolean accept(Result result)
						{
							if (result.getValue("Dataset").toString().equals(dataset))
								return result.getValue("Method").equals(bestMethod);
							else
								return true;
						}
					}));
				}
				ResultSetIO.printToFile(f, algResults.get(alg), true);
			}
			//			System.out.println(algResults.get(alg).toNiceString());

			if (algResults.containsKey(ALL_ALGS))
				algResults.get(ALL_ALGS).concat(algResults.get(alg));
			else
				algResults.put(ALL_ALGS, algResults.get(alg));

			System.out.println(DATASETS.length * algCount++ * 5 * 10);
			System.out.println(algResults.get(ALL_ALGS).getNumResults());
		}

		System.out.println(algResults.get(ALL_ALGS)
				.join(new String[] { "Method", "Dataset" }, new String[] { "Run", "Fold" }, null).toNiceString());

		ALGORITHMS = new String[] { ALL_ALGS };
	}

	public void best(String measure) throws Exception
	{
		ResultSet cmp = new ResultSet();
		ResultSet cmp2 = new ResultSet();
		File f = new File("/tmp/best.out");
		if (f.exists())
		{
			cmp = ResultSetIO.parseFromFile(f);
		}
		else
		{
			boolean scriptAndCopy = false;

			StringBuffer rerunScript = new StringBuffer();
			rerunScript.append("#!/bin/bash\n");
			rerunScript.append("module load Java/jdk1.8.0_25\n");

			for (String alg : ALGORITHMS)
			{
				algResults.clear();
				read(new String[] { alg }, "");
				remove_obs_sizes(alg);

				ResultSet best = new ResultSet();
				//        System.out.println(algResults.get(alg).toNiceString());

				int datasetIdx = 0;
				for (final String dataset : new CFPDataLoader("data").allDatasets())
				{
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
					r = r.join(new String[] { "Method", "Dataset" }, new String[] { "Run", "Fold" }, null);

					r.sortResults("Method", new Comparator<Object>()
					{
						@Override
						public int compare(Object o1, Object o2)
						{
							String method = o1.toString();
							String method2 = o2.toString();
							String size = sizeFromMethod(method);
							String size2 = sizeFromMethod(method2);
							if (size == null)
								size = Integer.MAX_VALUE + "";
							if (size2 == null)
								size2 = Integer.MAX_VALUE + "";
							if (!size.equals(size2))
								return new Integer(size).compareTo(new Integer(size2));
							int diameter = fpFromMethod(method).getDiameter();
							int diameter2 = fpFromMethod(method2).getDiameter();
							if (diameter != diameter2)
								return new Integer(diameter).compareTo(new Integer(diameter2));
							FeatureSelection feat = featFromMethod(method);
							FeatureSelection feat2 = featFromMethod(method2);
							if (feat != feat2)
							{
								if (feat == FeatureSelection.filt)
									return -1;
								else if (feat2 == FeatureSelection.filt)
									return 1;
								if (feat == FeatureSelection.none)
									return -1;
								else if (feat2 == FeatureSelection.none)
									return 1;
							}
							return 0;
							//                                CFPType type = fpFromMethod(method);
							//                                CFPType type2 = fpFromMethod(method2);
							//                                if (type.getDiameter() != type2.getDiameter())
							//                                    return new Integer(type.getDiameter()).compareTo(new Integer(type.getDiameter()));
							//                                if (type != type2)
						}
					});
					r.sortResults("Features", !dataset.equals("AMES"));
					r.sortResults(measure, false);

					if (r.getResultValue(0, measure).equals(r.getResultValue(1, measure))
							&& r.getResultValue(0, "Features").equals(r.getResultValue(1, "Features"))
							&& sizeFromMethod(r.getResultValue(0, "Method").toString()).equals(
									sizeFromMethod(r.getResultValue(1, "Method").toString()))
							&& featFromMethod(r.getResultValue(0, "Method").toString()).equals(
									featFromMethod(r.getResultValue(1, "Method").toString()))
							&& fpFromMethod(r.getResultValue(0, "Method").toString()).getDiameter() == fpFromMethod(
									r.getResultValue(1, "Method").toString()).getDiameter())
						throw new Error("equal: auc && #features && size && diam && feat-sel:\n" + r.toNiceString());

					//			System.out.println(dataset);
					//			System.out.println(r.getResultValue(0, "Method"));
					//			//			System.out.println(r.toNiceString());
					//			System.out.println();

					int idx = best.addResult();
					best.setResultValue(idx, "Dataset", dataset);
					CFPType type = fpFromMethod(r.getResultValue(0, "Method").toString());
					best.setResultValue(idx, "type", type);
					FeatureSelection feat = featFromMethod(r.getResultValue(0, "Method").toString());
					best.setResultValue(idx, "feat", feat);
					int size = 0;
					if (feat != FeatureSelection.none)
					{
						size = Integer.valueOf(sizeFromMethod(r.getResultValue(0, "Method").toString()));
						best.setResultValue(idx, "hashfoldsize", size);
					}

					idx = cmp.addResult();
					cmp.setResultValue(idx, "Dataset", dataset);
					cmp.setResultValue(idx, "Algorithm", alg);
					cmp.setResultValue(idx, measure, r.getResultValue(0, measure));
					if (datasetIdx >= cmp2.getNumResults())
					{
						cmp2.addResult();
						cmp2.setResultValue(datasetIdx, "Dataset", dataset);
					}
					cmp2.setResultValue(datasetIdx, alg, r.getResultValue(0, measure));

					if (scriptAndCopy)
					{
						if (dataset.startsWith("ChEMBL") || dataset.startsWith("MUV"))
						{
							for (int run = 1; run <= 5; run++)
							{
								String resFile = CFPMiner.resultFileName(run, type, feat, size, alg, dataset);
								String outFile = resFile.replace(".arff", ".output");
								String hashfoldStr = "";
								if (feat != FeatureSelection.none)
									hashfoldStr = "--hashfoldsize " + size;
								rerunScript.append("bsub -q short -W 300 -n 1 -app Reserve5G -o output/" + outFile
										+ "  java -jar -Xmx2G cfpminer.jar --datasetName " + dataset + " --run " + run
										+ " --classifier " + alg + " --type " + type + " --featureSelection " + feat
										+ " " + hashfoldStr + " -x\n");
								rerunScript.append("sleep 0.5\n");
							}
						}
						else
						{
							//				String src = "/home/martin/workspace/CFPMiner/results_r5_all/";
							//				String dest = "/home/martin/workspace/CFPMiner/results_r5_best_no_resample/";
							//				for (int run = 1; run <= 5; run++)
							//				{
							//					String resFile = CFPMiner.resultFileName(run, type, FeatureSelection.filt, size, alg, dataset);
							//					FileUtil.copy(src + resFile, dest + resFile);
							//				}
						}
					}
					datasetIdx++;
				}
				System.out.println(best.toNiceString());
				if (scriptAndCopy)
				{
					String dest = ValidationResultsProvider.RESULTS_MERGED_FOLDER + alg + paramsStr + ".best";
					ResultSetIO.printToFile(new File(dest), best, true);
					System.out.println("written best methods to " + dest);

					FileUtil.writeStringToFile("/tmp/killme", rerunScript.toString());
					System.out.println();
					System.out.flush();
					System.err.println(rerunScript);
					break;
				}
			}
			ResultSetIO.printToFile(f, cmp, true);
		}
		System.out.println(cmp.toNiceString());
		ResultSet diff = cmp.diff("Algorithm", ArrayUtil.toList(new String[] { "Dataset" }), null);
		System.out.println(diff.toNiceString());
		ResultSet winLoss = diff.winLoss(ArrayUtil.toList(new String[] { "Algorithm" }), null);
		System.out.println("\nwin loss (uses diff as input)\n");
		System.out.println(winLoss.toNiceString());

		System.out.println(cmp2.toNiceString());
	}

	public void ranking(String params, String measure) throws Exception
	{
		ResultSet combined = new ResultSet();
		for (String alg : ALGORITHMS)
		{
			System.out.println("\n\n\n" + alg + "\n--------------\n");
			ResultSet r;

			this.params = params;
			setParamsStr();
			String cache = ValidationResultsProvider.RESULTS_MERGED_FOLDER + alg + paramsStr + ".joined";
			if (!new File(cache).exists())
			{
				read(new String[] { alg }, params);
				remove_obs_sizes(alg);
				r = algResults.get(alg)
						.join(new String[] { "Method", "Dataset" }, new String[] { "Run", "Fold" }, null);
				ResultSetIO.printToFile(new File(cache), r, true);
			}
			else
			{
				System.err.println("read cached from: " + cache);
				r = ResultSetIO.parseFromFile(new File(cache));
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
			renameMethods(alg, r);
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
				for (String size : SIZES)
				{
					//					System.out.println(t + " " + size);
					int iFilt = getResult(r, t, size, FeatureSelection.filt);
					if (iFilt == -1)
						continue;
					Double rFilt = (Double) r.getResultValue(iFilt, measure + ResultSet.RANK_SUFFIX);
					Double rFold = (Double) r.getResultValue(getResult(r, t, size, FeatureSelection.fold), measure
							+ ResultSet.RANK_SUFFIX);
					//					System.out.println(rFilt + " vs " + rFold);
					if (rFilt < rFold)
						winFold += 1;
					else
						System.out.println("fold loss: " + t + " " + size);
					diffFold += rFold - rFilt;

					Double rAll = (Double) r.getResultValue(getResult(r, t, null, FeatureSelection.none), measure
							+ ResultSet.RANK_SUFFIX);
					if (rFilt < rAll)
						winAll += 1;
					diffAll += rAll - rFilt;

					count += 1;
				}
			}
			System.out.println("filt vs fold - < " + winFold + "/" + count + " - diff " + (diffFold / (double) count));
			System.out.println("filt vs all - < " + winAll + "/" + count + " - diff " + (diffAll / (double) count));

			int sumBest = 0;
			for (int i = 0; i < r.getNumResults(); i++)
			{
				if (alg.equals(ALGORITHMS[0]))
					combined.addResult();
				//				else if (!combined.getResultValue(i, "Method").equals(r.getResultValue(i, "Method")))
				//					throw new IllegalStateException();
				String m = r.getResultValue(i, "Method").toString();
				combined.setResultValue(i, alg + " CFPType", fpFromMethod(m));
				combined.setResultValue(i, alg + " FeatureSelection", featFromMethod(m).toNiceShortString());
				String size = sizeFromMethod(m);
				combined.setResultValue(i, alg + " hashfoldSize",
						size != null ? size : Math.round((Double) r.getResultValue(i, "Features")) + "");

				//				combined.setResultValue(i, "Coll.", "to be overwritten");
				//				combined.setResultValue(i, alg + " Top3", "");
				combined.setResultValue(i, alg + " " + measure + " Rank",
						StringUtil.formatDouble(((Double) r.getResultValue(i, measure + ResultSet.RANK_SUFFIX)), 1));
				double best = ((Number) r.getResultValue(i, measure + ResultSet.RANK_BEST_SUFFIX)).doubleValue()
						* numDatasets;
				sumBest += best;
				combined.setResultValue(i, alg + " " + measure + " Best", (int) best);
				//				combined.setNicePropery(alg + " Top3", "Top3");
				combined.setNicePropery(alg + " " + measure + " Rank", "Rank");

				combined.setNicePropery(alg + " CFPType", niceValuesShort.get("CFPType"));
				combined.setNicePropery(alg + " FeatureSelection", niceValuesShort.get("FeatureSelection"));
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
				preProp += "\\multicolumn{3}{c}{" + niceValues.get(alg) + "} & \\multicolumn{2}{c}{" + measure + "} ";
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

	private int getResult(ResultSet set, CFPType type, String size, FeatureSelection feat)
	{
		for (int i = 0; i < set.getNumResults(); i++)
		{
			String m = set.getResultValue(i, "Method").toString();
			if (fpFromMethod(m) == type && featFromMethod(m) == feat && ObjectUtil.equals(sizeFromMethod(m), size))
				return i;
		}
		return -1;
	}

	public void rankingOld(String params, String measure) throws Exception
	{
		ResultSet combined = new ResultSet();
		for (String alg : ALGORITHMS)
		{
			ResultSet r;

			this.params = params;
			setParamsStr();
			String cache = ValidationResultsProvider.RESULTS_MERGED_FOLDER + alg + paramsStr + ".joined";
			if (!new File(cache).exists())
			{
				read(new String[] { alg }, params);
				remove_obs_sizes(alg);
				r = algResults.get(alg)
						.join(new String[] { "Method", "Dataset" }, new String[] { "Run", "Fold" }, null);
				ResultSetIO.printToFile(new File(cache), r, true);
			}
			else
			{
				System.err.println("read cached from: " + cache);
				r = ResultSetIO.parseFromFile(new File(cache));
			}
			r = r.filter(new ResultSetFilter()
			{
				@Override
				public boolean accept(Result result)
				{
					String size = sizeFromMethod(result.getValue("Method").toString());
					return size == null || size.equals("1024") || size.equals("8192");
				}
			});

			//			if (alg.equals("SMO") && r.getResultValues("Dataset").getNumValues() > 1)
			//				r = r.filter(new ResultSetFilter()
			//				{
			//					@Override
			//					public boolean accept(Result result)
			//					{
			//						return !result.getValue("Dataset").equals("AMES");
			//					}
			//				});

			r = r.rank(measure, new String[] { "Dataset" });
			r.clearMergeCountAndVariance();
			int numDatasets = r.getResultValues("Dataset").getNumValues();
			System.err.println("num datasets " + numDatasets);
			r = r.join("Method");
			renameMethods(alg, r);
			r.removePropery("Dataset");
			for (String p : ValidationResultsProvider.performanceMeasures)
				r.removePropery(p);
			r.removePropery("Sensitivity");
			r.removePropery("Selectivity");
			//			r.sortResults("AUC" + ResultSet.RANK_SUFFIX, true, true, -1);
			//			System.out.println(r.toNiceString());

			int sumBest = 0;
			for (int i = 0; i < r.getNumResults(); i++)
			{
				if (alg.equals(ALGORITHMS[0]))
					combined.addResult();
				else if (!combined.getResultValue(i, "Method").equals(r.getResultValue(i, "Method")))
					throw new IllegalStateException();
				combined.setResultValue(i, "Method", r.getResultValue(i, "Method"));
				combined.setResultValue(i, "Features", Math.round((Double) r.getResultValue(i, "Features")));
				combined.setResultValue(i, "Coll.", "to be overwritten");
				combined.setResultValue(i, alg + " Top3", "");
				combined.setResultValue(i, alg + " " + measure + " Rank",
						r.getResultValue(i, measure + ResultSet.RANK_SUFFIX));
				double best = ((Number) r.getResultValue(i, measure + ResultSet.RANK_BEST_SUFFIX)).doubleValue()
						* numDatasets;
				sumBest += best;
				combined.setResultValue(i, alg + " " + measure + " Best", (int) best);
				combined.setNicePropery(alg + " Top3", "Top3");
				combined.setNicePropery(alg + " " + measure + " Rank", "Rank");
				combined.setNicePropery(alg + " " + measure + " Best", "Best");
			}
			System.err.println("num best sum " + sumBest);

			r.sortResults(measure + ResultSet.RANK_SUFFIX, true, true, -1);

			for (int i = 0; i < r.getNumResults(); i++)
			{
				String m = r.getResultValue(i, "Method").toString();
				FeatureSelection f = featFromMethod(m);
				r.setResultValue(i, "folding-enabled", f == FeatureSelection.fold);
			}
			System.out.println(r.toNiceString());
			for (String p : new String[] { "Features", "folding-enabled" })
				System.out.println("correlation to " + p + " " + r.spearmanCorrelation(measure + "_rank", p));

			//			System.exit(1);

			for (int i = 0; i < 3; i++)
			{
				double rank = (Double) r.getResultValue(i, measure + ResultSet.RANK_SUFFIX);
				for (int c = 0; c < combined.getNumResults(); c++)
				{
					double rankC = (Double) combined.getResultValue(c, alg + " " + measure + " Rank");
					if (rankC == rank)
						combined.setResultValue(c, alg + " Top3", "*");
				}

			}
		}
		tableCollisions(true); // to load collisions

		int addP = 0;
		combined.getProperties().add(addP++, "idx");
		combined.getProperties().add(addP++, niceValuesShort.get("CFPType"));
		combined.getProperties().add(addP++, "FeatureSelection");
		combined.getProperties().add(addP++, "hashfoldSize");

		for (int i = 0; i < combined.getNumResults(); i++)
		{
			String method = combined.getResultValue(i, "Method").toString();

			combined.setResultValue(i, niceValuesShort.get("CFPType"), fpFromMethod(method).toNiceString());
			combined.setResultValue(i, "FeatureSelection", featFromMethod(method).toNiceString());
			combined.setResultValue(i, "hashfoldSize", sizeFromMethod(method));

			Double rate;
			if (method.contains(niceValues.get("fold")))
				rate = collisionRate.get(fpFromMethod(method), sizeFromMethod(method));
			else
				rate = 0.0;
			combined.setResultValue(i, "Coll.", rate);
			double rankSum = 0;
			for (String alg : ALGORITHMS)
				rankSum += ((Number) combined.getResultValue(i, alg + " " + measure + " Rank")).doubleValue();
			combined.setResultValue(i, "Combined Rank", rankSum / (double) ALGORITHMS.length);
		}
		combined.sortResults("Combined Rank", true, true, -1);

		//combined.sortResults("RaF "+measure+" Rank", true, true, -1);
		//combined.removePropery("Combined Rank");

		for (String p : niceValues.keySet())
			combined.setNicePropery(p, niceValues.get(p));

		combined.removePropery("Method");
		for (String alg : ALGORITHMS)
			combined.removePropery(alg + " Top3");

		for (int i = 0; i < combined.getNumResults(); i++)
			combined.setResultValue(i, "idx", i + 1);

		System.out.println(combined.toNiceString());
		System.out.println(combined.getNumResults());

		//		System.out.println(9 / (double) combined.getNumResults());
		//		System.out.println(27 / (double) combined.getNumResults());

		if (write)
		{
			String dest = destFolder + "ranking.tex";
			System.out.println("write table to " + dest);

			String preProp = " \\multicolumn{" + addP + "}{c}{Selected features} & & ";
			Boolean hlines[] = { false, true, false, false, true, false };
			for (String alg : ALGORITHMS)
			{
				preProp += "& \\multicolumn{2}{c}{" + niceValuesShort.get(alg) + " " + measure + "} ";
				hlines = ArrayUtil.concat(hlines, new Boolean[] { true, false });
			}
			hlines = ArrayUtil.concat(hlines, new Boolean[] { true });
			preProp += "&\\\\\n";

			FileUtil.writeStringToFile(dest, combined.toLatexTable(null, hlines, preProp));
		}
	}

	DoubleKeyHashMap<CFPType, String, Double> collisionRate = new DoubleKeyHashMap<>();

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
				for (String size : SIZES)
				{

					if (skipFiltFoldMethod(t, Integer.parseInt(size)))
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
						FileUtil.writeStringToFile(dest, r.toLatexTable(null, (Integer[]) null, datasetPreProp));
					}
				}
			}

			ResultSet joined = res.join("Type");
			for (int i = 0; i < joined.getNumResults(); i++)
			{
				CFPType t = CFPType.valueOf(joined.getResultValue(i, "Type").toString());
				for (String size : SIZES)
				{
					if (skipFiltFoldMethod(t, Integer.parseInt(size)))
					{
						joined.setResultValue(i, size + " bit-load", null);
						joined.setResultValue(i, size + " collisions", null);
					}
				}
			}

			joined.removePropery("Dataset");
			joined.removePropery("Compounds");

			for (String size : SIZES)
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
					FileUtil.writeStringToFile(dest, joined.toLatexTable(null, (Integer[]) null, preProp));
				}
			}
		}
	}

	public void tableWinLossFiltering()
	{
		for (String alg : ALGORITHMS)
		{
			algResults.get(alg).sortResults("Method");
			ResultSet t1 = new ResultSet();
			for (String s : ValidationResultsProvider.performanceMeasures)
			{
				ResultSet set = ValidationResultsProvider.ttest(algResults.get(alg), s, "filt", true);
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
		for (String alg : ALGORITHMS)
		{
			ResultSet r = algResults.get(alg).join(new String[] { "Method", "Dataset" },
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
			renameMethods(alg, r);
			r.sortResults("Method", false);

			//			}
			renameDatasets(r);

			//			System.out.println(r.toNiceString());
			ResultSetLinePlot plot = new ResultSetLinePlot(r, ValidationResultsProvider.performanceMeasures, "Method",
					"Dataset");
			plot.setTitle(null);
			plot.setXAxisLabel(null);
			plot.setRotateXLabels(ResultSetLinePlot.XLabelsRotation.vertical);
			if (ValidationResultsProvider.performanceMeasures.length > 1)
			{
				for (String p : ValidationResultsProvider.performanceMeasures)
					if (p.equals("AUC") || p.equals("Accuracy"))
					{
						plot.setYAxisRange(p, 0.5, 1.0);
						plot.setYAxisTickUnits(p, 0.125);
					}
					else if (p.equals("FMeasure") || p.equals("Sensitivity") || p.equals("Selectivity")
							|| p.equals("AUP"))
					{
						plot.setYAxisRange(p, 0.0, 1.0);
						plot.setYAxisTickUnits(p, 0.125);
					}
					else if (p.equals("TN"))
					{
						plot.setYAxisRange(p, 197, 201);
					}
			}
			else if (ValidationResultsProvider.performanceMeasures.length == 1
					&& ValidationResultsProvider.performanceMeasures[0].equals("AUC"))
				plot.setYAxisRange(0.5, 1.0);

			for (String p : ValidationResultsProvider.performanceMeasures)
			{
				//			if (p.equals("Accuracy"))
				//			{
				//				if (r.getResultValues("Dataset").contains("AMES"))
				plot.addMarker(p, "AMES", CFPDataLoader.BALANCED_DATASETS);
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

			ChartPanel c = plot.getChartPanel();
			c.setMaximumDrawWidth(10000);
			c.setMaximumDrawHeight(5000);
			//c.setPreferredSize(new Dimension(800, 600));

			String name = "Chart_" + alg + paramsStr;
			int height = 290 + ValidationResultsProvider.performanceMeasures.length * 85;
			if (showCharts)
				SwingUtil.showInFrame(c, destFolder + name, false, new Dimension(1000, 50 + height));

			if (write)
			{
				plot.toSVGFile(destFolder + name + ".svg", new Dimension(800, height));
				toPDF(destFolder + name);
			}
		}
	}

	public void diffChart()
	{
		for (String alg : ALGORITHMS)
		{
			ResultSet r = algResults.get(alg).join(new String[] { "Method", "Dataset" },
					new String[] { "Run", "Fold" }, null);
			sortDatasets(r);
			r.sortResults("Method", true);
			renameMethods(alg, r);
			System.out.println(r.toNiceString());
			r.clearMergeCountAndVariance();
			r = r.diff("Method", ArrayUtil.toList(new String[] { "Dataset" }), null);
			System.out.println(r.toNiceString());

			ResultSetBoxPlot plot = new ResultSetBoxPlot(r, niceValues.get(alg), r.getUniqueValue("Method").toString(),
					"Method", ArrayUtil.toList(ValidationResultsProvider.performanceMeasures));
			plot.setHideMean(true);
			plot.setRotateXLabels(false);
			plot.printResultsPerPlot(false);
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

			String name = "Chart_Diff_" + alg + paramsStr + filterName;
			if (showCharts)
				SwingUtil.showInFrame(c, name, false);

			if (write)
			{
				plot.boxPlotToSVGFile(destFolder + name + ".svg", size);
				toPDF(destFolder + name);
			}
		}
	}

	public void chartFiltering()
	{
		for (String alg : ALGORITHMS)
		{
			ResultSet r = algResults.get(alg).join(new String[] { "Method", "Dataset" },
					new String[] { "Run", "Fold" }, null);
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
	}

	public void tableWinLossSizes()
	{
		for (String alg : ALGORITHMS)
		{
			if (algResults.get(alg).getResultValues("Method").getNumValues() != SIZES.length)
				throw new Error("Methods differ in more aspects but sizes "
						+ algResults.get(alg).getResultValues("Method").values());

			//reverse sort to have 1024 at the bottom, and therefore on the right side (method_2)
			algResults.get(alg).sortResults("Method", false, false, -1);
			String compareAgainstMethod = algResults.get(alg)
					.getResultValue(algResults.get(alg).getNumResults() - 1, "Method").toString();

			ResultSet t1 = new ResultSet();
			for (String s : ValidationResultsProvider.performanceMeasures)
			{
				ResultSet set = algResults.get(alg).pairedTTestWinLoss("Method",
						ArrayUtil.toList(new String[] { "Run", "Fold" }), s, SIG_LEVEL, TEST_CORRECTION, "Dataset",
						true);
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
					t1.setResultValue(resCount, "Num bits", renameMethod(alg, method));//method.substring(method.lastIndexOf("_") + 1));
					t1.setResultValue(resCount, "vs num bits", renameMethod(alg, method2));//method.substring(method.lastIndexOf("_") + 1));
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
	}

	public String algFromMethod(String m)
	{
		for (String s : WEKA_ALGORITHM_NAMES.keySet())
			if (m.contains(s))
				return WEKA_ALGORITHM_NAMES.get(s);
		for (String s : ORIG_ALGORITHMS)
			if (m.equals(s))
				return s;
		throw new IllegalArgumentException(m);
	}

	public CFPType fpFromMethod(String m)
	{
		for (CFPType t : CFPType.values())
		{
			if (m.contains(t.toString()))
				return t;
			if (m.contains(t.toNiceString()))
				return t;
			if (params.contains(t.toString()))
				return t;
		}
		if (ArrayUtil.indexOf(ORIG_ALGORITHMS, m) != -1 || ArrayUtil.indexOf(ORIG_ALGORITHMS_NICE, m) != -1)
			return null;
		throw new IllegalArgumentException(m);
	}

	public FeatureSelection featFromMethod(String m)
	{
		for (FeatureSelection t : FeatureSelection.values())
		{
			if (m.contains(t.toString()))
				return t;
			if (m.contains(t.toNiceString()))
				return t;
			if (params.contains(t.toString()))
				return t;
		}
		if (ArrayUtil.indexOf(ORIG_ALGORITHMS, m) != -1 || ArrayUtil.indexOf(ORIG_ALGORITHMS_NICE, m) != -1)
			return null;
		throw new IllegalArgumentException(m);
	}

	public static String sizeFromMethod(String m)
	{
		for (String s : SIZES)
			if (m.contains(s))
				return s;
		if (m.contains(FeatureSelection.none.toString()) || m.contains(FeatureSelection.none.toNiceString()))
			return null;
		if (ArrayUtil.indexOf(ORIG_ALGORITHMS, m) != -1 || ArrayUtil.indexOf(ORIG_ALGORITHMS_NICE, m) != -1)
			return null;
		throw new IllegalArgumentException(m);
	}

	enum WinLossCompareAgainst
	{
		sameSize, sameDiameter, sameDiameterAndFeat, lastProp, all
	}

	public void tableWinLoss(String name, WinLossCompareAgainst cmp, final String... props)
	{
		ResultSet all = null;

		for (String alg : ALGORITHMS)
		{
			// sort to get last element in props on the right side of the win loss table
			algResults.get(alg).sortResults("Method", new Comparator<Object>()
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
				ResultSet set = algResults.get(alg).pairedTTestWinLoss("Method",
						ArrayUtil.toList(new String[] { "Run", "Fold" }), s, SIG_LEVEL, TEST_CORRECTION, "Dataset",
						true);
				//			set.sortResults("Method_1");

				int resCount = 0;
				for (int i = 0; i < set.getNumResults(); i++)
				{
					String method = set.getResultValue(i, "Method_1").toString();
					String size = sizeFromMethod(method);
					CFPType type = fpFromMethod(method);
					FeatureSelection feat = featFromMethod(method);
					String method2 = set.getResultValue(i, "Method_2").toString();
					String size2 = sizeFromMethod(method2);
					CFPType type2 = fpFromMethod(method2);
					FeatureSelection feat2 = featFromMethod(method2);
					if (ArrayUtil.containsAt(method, props) == -1 || ArrayUtil.containsAt(method2, props) == -1)
						continue;
					if (cmp == WinLossCompareAgainst.sameDiameter && type.getDiameter() != type2.getDiameter())
						continue;
					if (cmp == WinLossCompareAgainst.sameDiameterAndFeat && type.getDiameter() != type2.getDiameter())
						continue;
					if (cmp == WinLossCompareAgainst.sameDiameterAndFeat && feat != feat2)
						continue;
					if (cmp == WinLossCompareAgainst.lastProp && !method2.contains(ArrayUtil.last(props)))
						continue;
					if (cmp == WinLossCompareAgainst.sameSize && !ObjectUtil.equals(size, size2))
						continue;
					if (t1.getNumResults() - 1 < resCount)
						t1.addResult();

					if (cmp == WinLossCompareAgainst.sameDiameterAndFeat)
						t1.setResultValue(resCount, "FeatureSelection", feat.toNiceString());
					if (cmp == WinLossCompareAgainst.sameSize)
						t1.setResultValue(resCount, "hashfoldSize", size);

					if (cmp != WinLossCompareAgainst.sameSize)
					{
						boolean stripSize = cmp == WinLossCompareAgainst.sameSize;
						boolean stripFeat = cmp == WinLossCompareAgainst.sameDiameterAndFeat;

						String methodProp = "Method";
						if (name.equals("Sizes"))
							methodProp = niceValues.get("hashfoldSize");
						if (name.equals("FCFPvsECFP") || name.equals("ECFPvsFCFP"))
							methodProp = "CFPType";

						t1.setResultValue(resCount, methodProp, renameMethod(alg, method, stripSize, stripFeat)
								+ " vs " + renameMethod(alg, method2, stripSize, stripFeat));
						//							t1.setResultValue(resCount, "vs Method",
						//									renameMethod(alg, method2, cmp == WinLossCompareAgainst.sameSize));
					}

					t1.setResultValue(resCount, s, set.getResultValue(i, s));
					resCount++;
				}
			}
			if (cmp == WinLossCompareAgainst.sameDiameterAndFeat)
				t1.sortResults("FeatureSelection", false);

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

			if (cmp == WinLossCompareAgainst.sameDiameter && params.equals("ecfp"))
			{
				System.out.println("hooray");
			}

			t1.getProperties().add(0, "Algorithm");
			for (int i = 0; i < t1.getNumResults(); i++)
			{
				t1.setResultValue(i, "Algorithm", niceValuesShort.get(alg));
			}
			if (all == null)
				all = t1;
			else
				all.concat(t1);
		}
		if (ALGORITHMS.length == 1)
			return;
		System.out.println(all.toNiceString());
		if (write)
		{
			String dest = destFolder + "Table_WinLoss_" + name + paramsStr + ".tex";
			System.out.println("write table to " + dest);
			FileUtil.writeStringToFile(dest, all.toLatexTable());
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
		for (String alg : ALGORITHMS)
		{
			ResultSet r = algResults.get(alg).join(new String[] { "Method", "Dataset" },
					new String[] { "Run", "Fold" }, ValidationResultsProvider.performanceMeasures);
			//r.sortResults("Method");
			//System.out.println(r.toNiceString());

			List<String> datasets = ListUtil.cast(String.class, r.getResultValues("Dataset").values());
			Collections.sort(datasets, CFPDataLoader.CFPDataComparator);

			for (String m : ValidationResultsProvider.performanceMeasures)
			{
				ResultSet test = algResults.get(alg).pairedTTest("Method",
						ArrayUtil.toList(new String[] { "Run", "Fold" }), m, SIG_LEVEL, TEST_CORRECTION, "Dataset");

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
		new ExternalTool(null).run("to-pdf", ("rsvg-convert -f pdf -o " + svgFileWithoutExtension + ".pdf "
				+ svgFileWithoutExtension + ".svg").split(" "));
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
}
