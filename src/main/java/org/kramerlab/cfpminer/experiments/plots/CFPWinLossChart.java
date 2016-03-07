package org.kramerlab.cfpminer.experiments.plots;

import java.awt.Dimension;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.kramerlab.cfpminer.experiments.CFPCrossValidation;
import org.kramerlab.cfpminer.experiments.CreatePlots;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.javalib.datamining.Result;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.datamining.ResultSetFilter;
import org.mg.javalib.freechart.WinLossBarChart;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.ListUtil;
import org.mg.javalib.util.SwingUtil;
import org.mg.wekalib.eval2.persistance.DB;
import org.mg.wekalib.eval2.persistance.ResultProviderImpl;
import org.mg.wekalib.evaluation.PredictionUtil.ClassificationMeasure;

public class CFPWinLossChart extends CreatePlots
{
	public CFPWinLossChart()
	{

	}

	public void plotWinLossECFPvsFCFP() throws Exception
	{
		ClassificationMeasure[] measures = new ClassificationMeasure[] {
				ClassificationMeasure.AUPRC, ClassificationMeasure.AUC };

		boolean onlyDefaultSize = true;
		final int size = 1024;
		//		int defaultSize = size;

		CFPCrossValidation cv = CFPCrossValidation.compareCFPs();
		//		cv.datasets = ListUtil.createList("CPDBAS_Mutagenicity", "DUD_vegfr2", "DUD_hivrt",
		//				"DUD_cdk2");
		cv.datasets = DATASETS; // ArrayUtil.toList(new DataLoader("data").balancedDatasets());
		cv.sizes = ListUtil.createList(size);
		cv.featureSelections = ListUtil.createList(FeatureSelection.filt, FeatureSelection.none);
		//cv.featureSelections = ListUtil.createList(FeatureSelection.none);
		//		cv.classifiers = ListUtil.create(Model.class, new RandomForestModel(),
		//				new NaiveBayesModel());
		cv.types = ArrayUtil.toList(CFPType.values());

		ResultSet res = cv.selectModelResultsPerRepetition(measures, false, true);

		String subCat;
		FeatureSelection sel;
		if (onlyDefaultSize)
			subCat = "FeatureSelection";
		else
			subCat = "hashfoldSize";
		boolean filter = true;
		if (filter)
			sel = FeatureSelection.filt;
		else
			sel = FeatureSelection.fold;

		for (ClassificationMeasure measure : measures)
		{
			HashMap<String, ResultSet> results = new LinkedHashMap<>();

			ResultSet r = res.filter(new ResultSetFilter()
			{
				@Override
				public boolean accept(Result result)
				{
					int s = ((Integer) result.getValue("HashfoldSize")).intValue();
					return s == 0;
				}
			});
			r.sortResults("Type");
			ResultSet wl = winLoss(r, measure.toString(), WinLossOption.compareSameDiameter, "Type",
					"Algorithm");
			results.put(
					(onlyDefaultSize ? (FeatureSelection.none.toNiceShortString() + "") : "all"),
					wl);

			r = res.filter(new ResultSetFilter()
			{
				@Override
				public boolean accept(Result result)
				{
					int s = ((Integer) result.getValue("HashfoldSize")).intValue();
					return s == size;
				}
			});
			r.sortResults("Type");
			wl = winLoss(r, measure.toString(), WinLossOption.compareSameDiameter, "Type",
					"Algorithm");
			results.put((onlyDefaultSize ? (sel.toNiceShortString() + "") : (size + "")), wl);

			//			String suffix = "_" + measure + "_" + sel
			//					+ (onlyDefaultSize ? ("_" + defaultSize) : "");
			plotWinLoss("WinLoss_ECFPvsFCFP_" + measure, measure.toString(), "Type", "Algorithm",
					subCat, results, "Fingerprint type comparison", !onlyDefaultSize);
		}
	}

	public void plotWinLossECFPDiameter() throws Exception
	{
		ClassificationMeasure[] measures = new ClassificationMeasure[] { ClassificationMeasure.AUC,
				ClassificationMeasure.AUPRC };

		boolean onlyDefaultSize = true;
		final int size = 1024;
		//		int defaultSize = size;

		CFPCrossValidation cv = CFPCrossValidation.compareCFPs();
		//		cv.datasets = ListUtil.createList("CPDBAS_Mutagenicity", "DUD_vegfr2", "DUD_hivrt",
		//				"DUD_cdk2");
		cv.datasets = DATASETS;
		//cv.datasets = ArrayUtil.toList(new DataLoader("data").balancedDatasets());
		cv.sizes = ListUtil.createList(size);
		//cv.classifiers = ListUtil.create(Model.class, new SupportVectorMachineModel());
		//cv.featureSelections = ListUtil.createList(FeatureSelection.none);
		cv.featureSelections = ListUtil.createList(FeatureSelection.filt, FeatureSelection.none);
		//				new NaiveBayesModel());
		cv.types = ListUtil.createList(CFPType.ecfp0, CFPType.ecfp2, CFPType.ecfp4, CFPType.ecfp6);
		//cv.types = ListUtil.createList(CFPType.ecfp2, CFPType.ecfp4);

		ResultSet resX = cv.selectModelResults(measures, false);
		System.out.println(resX.toNiceString());

		ResultSet res = cv.selectModelResultsPerRepetition(measures, false, true);

		String subCat;
		FeatureSelection sel;
		//		if (onlyDefaultSize)
		subCat = "FeatureSelection";
		//		else
		//			subCat = "hashfoldSize";

		boolean filter = true;
		if (filter)
			sel = FeatureSelection.filt;
		else
			sel = FeatureSelection.fold;

		for (ClassificationMeasure measure : measures)
		{
			HashMap<String, ResultSet> results = new LinkedHashMap<>();
			//			for (final Integer size : SIZES)
			//			{
			//				if (onlyDefaultSize && size != defaultSize)
			//					continue;
			ResultSet r = res.filter(new ResultSetFilter()
			{
				@Override
				public boolean accept(Result result)
				{
					int s = ((Integer) result.getValue("HashfoldSize")).intValue();
					return s == 0;
				}
			});

			//			ResultSet print = r.join(new String[] { "Dataset", "Algorithm", "FeatureSelection",
			//					"Type", "HashfoldSize" }, new String[] { "RandomSeed", "Fold" }, null);
			//			print.setNumDecimalPlaces(3);
			//			for (ClassificationMeasure m : ClassificationMeasure.values())
			//				if (!m.equals(measure))
			//					print.removePropery(m.toString());
			//			System.out.println(print.toNiceString());

			results.put(
					(onlyDefaultSize ? (FeatureSelection.none.toNiceShortString() + "") : "all"),
					winLoss(r, measure.toString(), WinLossOption.compareToDiameter4, "Type",
							"Algorithm"));

			if (cv.featureSelections.contains(FeatureSelection.filt))
			{
				r = res.filter(new ResultSetFilter()
				{
					@Override
					public boolean accept(Result result)
					{
						int s = ((Integer) result.getValue("HashfoldSize")).intValue();
						return s == size;
					}
				});
				results.put((onlyDefaultSize ? (sel.toNiceShortString() + "") : (size + "")),
						winLoss(r, measure.toString(), WinLossOption.compareToDiameter4, "Type",
								"Algorithm"));
			}

			//			}
			//			String suffix = "_" + measure + "_" + sel
			//					+ (onlyDefaultSize ? ("_" + defaultSize) : "");

			plotWinLoss("WinLoss_ECFPDiameter_" + measure, measure.toString(), "Type", "Algorithm",
					subCat, results, "ECFP diameter comparison", !onlyDefaultSize);
		}
	}

	public void plotWinLossFeatureSelection() throws Exception
	{
		ClassificationMeasure[] measures = new ClassificationMeasure[] {
				ClassificationMeasure.AUPRC, ClassificationMeasure.AUC,
				ClassificationMeasure.EnrichmentFactor5 };

		CFPCrossValidation cv = CFPCrossValidation.compareCFPs();
		//cv.numCVRepetitions = 1;

		//		cv.datasets = DATASETS;
		//		cv.datasets = ListUtil.createList("ChEMBL_130");
		//cv.datasets = ListUtil.createList("ChEMBL_17045");
		//		cv.datasets = ArrayUtil.toList(new DataLoader("data").balancedDatasets());
		//cv.datasets = ArrayUtil.toList(new DataLoader("data").chemblDatasets());

		//cv.featureSelections = ListUtil.createList(FeatureSelection.filt, FeatureSelection.none);
		//
		//cv.sizes = SIZES;// ListUtil.createList(1024);
		//cv.sizes = ListUtil.createList(4096);
		//cv.classifiers = ListUtil.create(Model.class, new SupportVectorMachineModel());
		//cv.classifiers = ListUtil.create(Model.class, new RandomForestModel());
		//new RandomForestModel());
		//				new NaiveBayesModel());
		//		cv.types = ListUtil.createList(CFPType.ecfp4);

		ResultSet res = cv.selectModelResultsPerRepetition(measures, false, true);

		//		{
		//			//res.sortProperties(ArrayUtil.toStringArray(ClassificationMeasure.values()));
		//			res.sortProperties(new String[] { "Dataset", "Algorithm", "AlgParams",
		//					"FeatureSelection", "HashfoldSize", "Type", "Sensitivity", "Accuracy",
		//					"Specificity", "AUC", "AUPRC" });
		//			res.sortResults("Fold");
		//			res.sortResults("RandomSeed");
		//			System.out.println(res.toNiceString());
		//
		//			ResultSet print = res.join(new String[] { "Dataset", "Algorithm", "AlgParams",
		//					"FeatureSelection", "Type", "HashfoldSize" },
		//					new String[] { "RandomSeed", "Fold" }, null);
		//			print.setNumDecimalPlaces(3);
		//			System.out.println(print.toNiceString());
		//			ScreenUtil.centerWindowsOnScreen();
		//			SwingUtil.waitWhileWindowsVisible();
		//			System.exit(0);
		//		}

		for (ClassificationMeasure measure : measures)
		{
			HashMap<String, ResultSet> results = new LinkedHashMap<>();
			for (final Integer size : cv.sizes)//, 2048, 4096, 8192 })
			{
				//				System.out.println(res.toNiceString());
				ResultSet r = res.filter(new ResultSetFilter()
				{
					@Override
					public boolean accept(Result result)
					{
						int s = ((Integer) result.getValue("HashfoldSize")).intValue();
						return s == 0 || s == (int) size;
					}
				});

				//System.out.println(r.toNiceString());

				//System.out.println(r.toNiceString());
				//				ResultSet print = r.join(new String[] { "Dataset", "Algorithm", "FeatureSelection",
				//						"Type", "HashfoldSize" }, new String[] { "RandomSeed", "Fold" }, null);
				//				print.setNumDecimalPlaces(3);
				//System.out.println(print.toNiceString());
				//				System.exit(1);

				System.out.println("num results " + r.getNumResults());

				ResultSet wl = winLoss(r, measure.toString(), WinLossOption.compareAll,
						"FeatureSelection", "Algorithm");
				//System.out.println(wl.toNiceString());

				wl.sortResults("Algorithm", ALG_COMPARATOR);

				results.put(size + "", wl);
			}
			plotWinLoss("WinLoss_FeatureSelection_" + measure, measure.toString(),
					"FeatureSelection", "Algorithm", "HashfoldSize", results,
					"Feature selection comparison", true);
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
		//		for (String p : niceValues.keySet())
		//			plot.setNicePropery(p, niceValues.get(p));
		//		renameResultValues(plot, !fullPage);
		System.err.println(plot.toNiceString());
		//		System.exit(0);
		WinLossBarChart chart = new WinLossBarChart(plot, winLossCmp, measure, cat1, cat2);

		chart.setTitle("" + title + " (" + (fullPage ? "based on " : "") + "" + measure + ")");
		//		Dimension dim = new Dimension(fullPage ? 800 : 400, WinLossBarChart.SPACE_FOR_TITLE
		//				+ WinLossBarChart.SPACE_FOR_LEGEND_AND_X_AXIS + 150 * chart.getNumSubPlots());
		Dimension dim = new Dimension(fullPage ? 1200 : 600, WinLossBarChart.SPACE_FOR_TITLE
				+ WinLossBarChart.SPACE_FOR_LEGEND_AND_X_AXIS + 250 * chart.getNumSubPlots());
		chart.setFontSize(15.0F);

		if (SHOW_PLOTS)
		{
			SwingUtil.showInFrame(chart.getChart(), DEST_FOLDER + name + ".png", false, dim);
		}
		if (WRITE_FILES)
		{
			chart.toPNGFile(DEST_FOLDER + name + ".png", dim);
		}
	}

	enum WinLossOption
	{
		compareAll, compareToLast, compareSameDiameter, compareToDiameter4
	}

	static double SIG_LEVEL = 0.05;
	static Double TEST_CORRECTION = 1 / 9.0;

	public ResultSet winLoss(ResultSet all, String measure, WinLossOption opt,
			final String property, String splitProp)
	{
		all.removePropery("HashfoldSize");
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

			ResultSet set = r.pairedTTestWinLoss(property, new String[] { "RandomSeed", "Fold" },
					measure, SIG_LEVEL, TEST_CORRECTION, seriesProps(property), true);
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

	public static String[] seriesProps(String notUniqProp)
	{
		List<String> l = ArrayUtil.toList(new String[] { "Dataset", "Algorithm", "CFPType",
				"FeatureSelection", "hashfoldSize" });
		l.remove(notUniqProp);
		return ArrayUtil.toArray(l);
	}

	static interface SplitIterator<T> extends Iterator<T>
	{
		public Object getSplitValue();
	}

	static String[] ORIG_ALGORITHMS = new String[] { "RF", "SMV", "NB" };

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

	public static void main(String[] args) throws Exception
	{
		DB.init(new ResultProviderImpl("jobs/store", "jobs/tmp"), null);
		new CFPWinLossChart().plotWinLossFeatureSelection();
		//new CFPWinLossChart().plotWinLossECFPDiameter();
		//new CFPWinLossChart().plotWinLossECFPvsFCFP();
		//		ScreenUtil.centerWindowsOnScreen();
		SwingUtil.waitWhileWindowsVisible();
		System.exit(0);
	}

}
