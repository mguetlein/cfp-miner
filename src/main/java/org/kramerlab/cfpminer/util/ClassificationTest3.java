package org.kramerlab.cfpminer.util;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.kramerlab.cfpminer.weka.eval2.CDKDataSet;
import org.kramerlab.cfpminer.weka.eval2.CFPFeatureProvider;
import org.kramerlab.cfpminer.weka.eval2.PubChemADModel;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.DataLoader;
import org.mg.javalib.datamining.Result;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.datamining.ResultSetBoxPlot;
import org.mg.javalib.datamining.ResultSetFilter;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.CountedSet;
import org.mg.javalib.util.SwingUtil;
import org.mg.javalib.util.ThreadUtil;
import org.mg.wekalib.eval2.Holdout;
import org.mg.wekalib.eval2.model.DistanceBasedAppDomainPredictionModel;
import org.mg.wekalib.eval2.model.FeatureModel;
import org.mg.wekalib.eval2.model.LOFOutlierModel;
import org.mg.wekalib.eval2.model.Model;
import org.mg.wekalib.eval2.model.RandomForestModel;
import org.mg.wekalib.eval2.persistance.DB;
import org.mg.wekalib.eval2.persistance.ResultProviderImpl;
import org.mg.wekalib.evaluation.PredictionUtil;
import org.mg.wekalib.evaluation.PredictionUtil.ClassificationMeasure;
import org.mg.wekalib.evaluation.Predictions;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;

public class ClassificationTest3
{
	static ClassificationMeasure measure = ClassificationMeasure.AUC;

	static double splitRatio = 0.66;

	static boolean useTopPercentInsteadOfThreshold = true;

	enum FilterCriterion
	{
		Confidence, AppDomainNN, AppDomainCentroid, AppDomainPubChem, LOF;

		public boolean isAppDomain()
		{
			return this != Confidence;
		}

		public Model getAppDomainModel()
		{
			if (isAppDomain())
			{
				Model m;
				switch (this)
				{
					case AppDomainNN:
						m = new DistanceBasedAppDomainPredictionModel();
						((DistanceBasedAppDomainPredictionModel) m)
								.setType(DistanceBasedAppDomainPredictionModel.Type.TanimotoNN);
						break;
					case AppDomainCentroid:
						m = new DistanceBasedAppDomainPredictionModel();
						((DistanceBasedAppDomainPredictionModel) m).setType(
								DistanceBasedAppDomainPredictionModel.Type.TanimotoCentroidV2);
						break;
					case AppDomainPubChem:
						m = new PubChemADModel();
						break;
					case LOF:
						m = new LOFOutlierModel();
						break;
					default:
						throw new IllegalStateException();
				}
				return m;
			}
			else
				return null;
		}
	}

	static FilterCriterion filterCriteria[] = new FilterCriterion[] { FilterCriterion.Confidence,
			FilterCriterion.AppDomainNN, //FilterCriterion.AppDomainPubChem, 
			FilterCriterion.LOF }; //FilterCriterion.values();

	enum SplitTechnique
	{
		Random, AntiStrat;

		public boolean isAntiStrat()
		{
			return this == SplitTechnique.AntiStrat;
		}
	}

	static SplitTechnique splitTechniques[] = new SplitTechnique[] { SplitTechnique.Random }; // SplitTechnique.values();

	static boolean boxPlot = false;

	//	static String datasets[] = new String[] { "CPDBAS_Mouse", "AMES", "CPDBAS_Mutagenicity",
	//			"ChEMBL_8", "ChEMBL_259", "MUV_712", "MUV_644", "ChEMBL_87", "MUV_832", "DUD_hivrt" };

	//	static String datasets[] = new String[] { "AMES", "CPDBAS_Mouse", "CPDBAS_Mutagenicity",
	//			"DUD_hivrt", "ChEMBL_8", "ChEMBL_259", "ChEMBL_87", "MUV_712", "MUV_644", "MUV_832", };

	//	this alg is too slow on this data! ChEMBL_8 LOF CFP 2048 filt ecfp4

	static
	{
		FeatureModel.MAX_RUNTIME = Long.MAX_VALUE;
	}

	static String datasets[] = new String[] { "CPDBAS_Mouse", "DUD_hivrt", "AMES",
			"CPDBAS_Mutagenicity", "ChEMBL_8" };
	//	static String datasets[] = new String[] { "ChEMBL_259", "ChEMBL_87", "MUV_712", "MUV_644",
	//			"MUV_832", };
	// "NCTRER"

	static Model classifier = new RandomForestModel();
	//static Model classifier = new NaiveBayesModel();

	public static void main(String[] args) throws Exception
	{
		//		String data = "CPDBAS_Mouse";
		//		String dir = "/tmp/jobs/";
		//		FilterCriterion filter = FilterCriterion.LOF;
		//		DB.init(new ResultProviderImpl(dir + "store", dir + "tmp"), null);
		//		CDKDataSet ds = new CDKDataSet(data, DataLoader.INSTANCE.getDataset(data));
		//		Holdout holdout = new Holdout();
		//		holdout.setDataSet(ds);
		//		FeatureModel fm = new FeatureModel();
		//		fm.setFeatureProvider(new CFPFeatureProvider(256, FeatureSelection.filt, CFPType.ecfp4));
		//		fm.setModel(filter.getAppDomainModel());
		//		holdout.setModel(fm);
		//		holdout.setSplitRatio(splitRatio);
		//		holdout.setRandomSeed(4);
		//
		//		while (!holdout.isDone())
		//		{
		//			StopWatchUtil.start("run");
		//			Printer.println("running > ...");
		//			holdout.nextJob().run();
		//			Printer.println("running > done");
		//			StopWatchUtil.stop("run");
		//		}
		//		StopWatchUtil.print();
		//		Predictions appDomain = holdout.getResult();
		//		System.out.println(PredictionUtil.summaryClassification(appDomain));
		//		System.exit(0);

		//DB.setResultProvider(new ResultProviderImpl("/tmp/jobs/store", "/tmp/jovs/tmp"));

		//String datasets[] = new String[] { "NCTRER" };

		//				"ChEMBL_8", "ChEMBL_259", "MUV_712", "MUV_644" };
		//too-big: 
		//too-easy: "mushroom"
		//too-unstable: "hepatitis", "labor"
		//not-working: "iris" "balance-scale "splice" "waveform-5000"
		//ArrayUtil.scramble(datasets);

		Thread th = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				ThreadUtil.sleep(3000);

				while (true)
				{
					try
					{
						plotConfidence();
					}
					catch (Exception e)
					{
						//System.err.println("plotting failed, retrying in 3 seconds");
						e.printStackTrace();
						System.exit(1);
					}

					if (boxPlot)
					{
						ResultSetBoxPlot bp = new ResultSetBoxPlot(res, "", measure + "", "Split",
								"Dataset", measure + "");
						bp.setHideMean(true);
						bp.setRotateXLabels(true);
						SwingUtil.showInFrame(bp.getChart(), measure + "", false,
								new Dimension(1200, 800));
					}
					SwingUtil.waitWhileWindowsVisible();
				}
				//				for (Window w : Window.getWindows())
				//					w.dispose();
				//				SwingUtil.waitForAWTEventThread();

				//				ResultSetBoxPlot bp = new ResultSetBoxPlot(res, "", "Performance", "Algorithm", "Dataset", "Pearson");
				//				bp.setHideMean(true);
				//				SwingUtil.showInFrame(bp.getChart(), "Pearson", false);

				//					bp = new ResultSetBoxPlot(res, "", "Performance", "Algorithm", "Dataset", "RMSE");
				//					bp.setHideMean(true);
				//					SwingUtil.showInFrame(bp.getChart(), "RMSE", false);

				//				ScreenUtil.centerWindowsOnScreen();
			}
		});
		th.start();

		for (int seed = 0; seed < 30; seed++)
		{
			for (int i = 0; i < datasets.length; i++) //datasets.length
			{
				run(datasets[i], seed);
			}
		}
		//		SwingUtil.waitWhileWindowsVisible();
		//		System.exit(1);
	}

	static ResultSet res = new ResultSet();

	static ResultSet res2 = new ResultSet();

	public static String getName(Classifier c)
	{
		if (c instanceof SingleClassifierEnhancer)
		{
			return c.getClass().getSimpleName() + "-"
					+ getName(((SingleClassifierEnhancer) c).getClassifier());
		}
		else
			return c.getClass().getSimpleName();
	}

	static HashMap<String, String> datasetNames = new HashMap<>();

	public static String getDatasetName(String dataset)
	{
		//		if (datasetNames.containsKey(dataset))
		//			return datasetNames.get(dataset);
		//		else
		return dataset;
	}

	//	static DescriptiveStatistics v1 = new DescriptiveStatistics();
	//	static DescriptiveStatistics v2 = new DescriptiveStatistics();

	public static boolean run(String data, int seed) throws Exception
	{
		String dir = System.getProperty("user.home") + "/results/appdomain/jobs/";
		DB.init(new ResultProviderImpl(dir + "store", dir + "tmp"), null);

		CDKDataSet ds = new CDKDataSet(data, DataLoader.INSTANCE.getDataset(data));

		for (SplitTechnique split : splitTechniques)
		{
			for (FilterCriterion filter : filterCriteria)
			{
				Holdout holdout;
				Predictions appDomain = null;

				System.out.println("'" + data + "' '" + split + "' '" + filter + "'");

				if (filter.isAppDomain())
				{
					holdout = new Holdout();
					if (split.isAntiStrat())
						holdout.setAntiStratifiedSplitter(new CDKAntiStratifiedSplitter());
					holdout.setDataSet(ds);
					FeatureModel fm = new FeatureModel();
					fm.setFeatureProvider(
							new CFPFeatureProvider(2048, FeatureSelection.filt, CFPType.ecfp4));
					fm.setModel(filter.getAppDomainModel());
					holdout.setModel(fm);
					holdout.setSplitRatio(splitRatio);
					holdout.setRandomSeed(seed);
					while (!holdout.isDone())
					{
						System.out.println("run ad model > ...");
						holdout.nextJob().run();
						System.out.println("run ad model > done");
					}
					appDomain = holdout.getResult();
				}

				String classfierName = classifier.getName();
				System.out.println(classfierName + " " + seed);

				holdout = new Holdout();
				if (split.isAntiStrat())
					holdout.setAntiStratifiedSplitter(new CDKAntiStratifiedSplitter());
				holdout.setDataSet(ds);
				FeatureModel fm = new FeatureModel();
				fm.setFeatureProvider(
						new CFPFeatureProvider(2048, FeatureSelection.filt, CFPType.ecfp4));
				fm.setModel((Model) classifier.cloneJob());
				holdout.setModel(fm);
				holdout.setSplitRatio(splitRatio);
				holdout.setRandomSeed(seed);
				while (!holdout.isDone())
				{
					System.out.println("run pred model > ...");
					holdout.nextJob().run();
					System.out.println("run pred model > done");
				}
				Predictions p = holdout.getResult();

				for (double thres : new double[] { 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9,
						1.0 })
				{
					Predictions filtered;
					double filterValues[] = filter.isAppDomain() ? appDomain.confidence
							: p.confidence;
					if (useTopPercentInsteadOfThreshold)
						filtered = PredictionUtil.topConfAllClasses(p, thres, filterValues);
					else
						filtered = PredictionUtil.filterConfAllClasses(p, thres, filterValues);

					if (filtered.actual.length > 0 && !ArrayUtil.isUniq(filtered.confidence))
					//&& ArrayUtil.getDistinctValues(filtered.actual).size() > 1)
					{
						synchronized (res2)
						{
							//										if (thres == 0.9 || thres == 0.0)
							//										{
							//											double v = PredictionUtil.getClassificationMeasure(
							//													filtered, measure, 1.0);
							//											DescriptiveStatistics d = null;
							//											if (thres == 0.0)
							//												d = v1;
							//											if (thres == 0.9)
							//												d = v2;
							//											d.addValue(v);
							//											System.err.println(thres + " " + filtered.actual.length
							//													+ " " + v + " " + d.getMean() + " +- "
							//													+ d.getStandardDeviation());
							//										}

							//										if (thres == 0)
							//										System.out.println("threshold: " + thres + " predictions: "
							//												+ filtered.actual.length + " actual:"
							//												+ CountedSet.create(
							//														ArrayUtil.toDoubleArray(filtered.actual))
							//												+ " predicted:" + CountedSet.create(ArrayUtil
							//														.toDoubleArray(filtered.predicted)));

							int idx = res2.addResult();
							res2.setResultValue(idx, "Algorithm", classfierName);
							res2.setResultValue(idx, "Measure", measure);
							res2.setResultValue(idx, "SplitTechnique", split.toString());
							res2.setResultValue(idx, "FilterCriterion", filter.toString());
							res2.setResultValue(idx, "Dataset", getDatasetName(data));
							res2.setResultValue(idx, "Threshold", thres);
							res2.setResultValue(idx, "Value",
									PredictionUtil.getClassificationMeasure(filtered, measure,
											ds.getPositiveClass()));
						}
					}
				}

				if (boxPlot)
				{
					for (Predictions pf : PredictionUtil.perFold(p))
					{
						//					System.out.println(PredictionUtil.summaryClassification(pf));
						//					System.out.println(PredictionUtil.AUC(pf));
						//					System.exit(1);

						int idx = res.addResult();
						res.setResultValue(idx, "Algorithm", classfierName);
						res.setResultValue(idx, "SplitTechnique", split.toString());
						res.setResultValue(idx, "Dataset", getDatasetName(data));
						res.setResultValue(idx, "Fold", pf.fold[0]);
						res.setResultValue(idx, "AUC", PredictionUtil.AUC(pf));
						res.setResultValue(idx, "AUPRC",
								PredictionUtil.AUPRC(pf, ds.getPositiveClass()));
						res.setResultValue(idx, ClassificationMeasure.Accuracy + "",
								PredictionUtil.accuracy(pf));

					}
				}
				//				}
			}
		}
		return true;
	}

	public static JComponent labelPanel(String text)
	{
		return labelPanel(text, SwingConstants.CENTER);
	}

	public static JComponent labelPanel(String text, int orientation)
	{
		//		JPanel label = new JPanel(new BorderLayout());
		return new JLabel(text, orientation);
		//		label.setBackground(Color.WHITE);
		//		return label;
	}

	public static void plotConfidence()
	{
		if (res2.getNumResults() == 0)
			return;

		List<JComponent> panels = new ArrayList<>();
		//		List<Double> scores = new ArrayList<>();

		//HashMap<String, DescriptiveStatistics> slopeDecrCount = new HashMap<>();

		ResultSet res2Copy;
		synchronized (res2)
		{
			res2Copy = res2.copy();
		}

		{
			panels.add(labelPanel(classifier.getName()));
			for (final SplitTechnique split : splitTechniques)
			{
				boolean first = true;
				for (final FilterCriterion filt : filterCriteria)
				{
					panels.add(labelPanel(first ? split.toString() : ""));
					first = false;
				}
			}

			panels.add(labelPanel(""));
			for (final SplitTechnique split : splitTechniques)
				for (final FilterCriterion filt : filterCriteria)
					panels.add(labelPanel(filt.toString()));
		}

		for (final String data : datasets)
		{
			panels.add(labelPanel(data.toString()));

			ResultSet res2dataset = res2Copy.filter(new ResultSetFilter()
			{
				@Override
				public boolean accept(Result result)
				{
					return result.getValue("Dataset").equals(data);
				}
			});

			for (final SplitTechnique split : splitTechniques)
			{
				ResultSet res2datasetSplit = res2dataset.filter(new ResultSetFilter()
				{
					@Override
					public boolean accept(Result result)
					{
						return result.getValue("SplitTechnique").equals(split.toString());
					}
				});
				if (res2datasetSplit.getNumResults() == 0)
					continue;

				List<JFreeChart> charts = new ArrayList<>();
				double low = 1.0;
				double lowVal = 1.0;

				for (final FilterCriterion filt : filterCriteria)
				{
					System.out.println(filt);
					ResultSet res2datasetSplitProb = res2datasetSplit.filter(new ResultSetFilter()
					{
						@Override
						public boolean accept(Result result)
						{
							return result.getValue("FilterCriterion").equals(filt.toString());
						}
					});

					if (res2datasetSplitProb.getNumResults() == 0)
					{
						System.out
								.println("missing:  '" + data + "' '" + split + "' '" + filt + "'");
						charts.add(null);
					}
					else
					{
						String title = null;// d + " " + split;
						String yAttr = res2datasetSplitProb.getUniqueValue("Measure").toString();
						String xAttr;
						if (useTopPercentInsteadOfThreshold)
							xAttr = "% top predictions";
						else
							xAttr = "\u2265 " + filt;
						yAttr = "";
						xAttr = "";
						final XYIntervalSeriesCollection dataset = new XYIntervalSeriesCollection();

						//					double score = Double.NaN;

						List<TextTitle> subtitles = new ArrayList<>();

						CountedSet<String> numValsPerThreshold = new CountedSet<>();

						HashMap<Double, DescriptiveStatistics> valueStats = new HashMap<>();
						for (int i = 0; i < res2datasetSplitProb.getNumResults(); i++)
						{
							double t = (Double) res2datasetSplitProb.getResultValue(i, "Threshold");
							double v = (Double) res2datasetSplitProb.getResultValue(i, "Value");
							if (Double.isNaN(v))
								continue;
							if (!valueStats.containsKey(t))
								valueStats.put(t, new DescriptiveStatistics());
							valueStats.get(t).addValue(v);
							numValsPerThreshold.add(t + "");
						}

						res2datasetSplitProb = res2datasetSplitProb.join(new String[] { "Algorithm",
								"Measure", "FilterCriterion", "Dataset", "Threshold" },
								new String[] {}, new String[] {});
						System.out.println(res2datasetSplitProb.toNiceString());

						//					SimpleRegression regr = new SimpleRegression();

						final XYIntervalSeries series = new XYIntervalSeries(filt.toString());

						DescriptiveStatistics yVals = new DescriptiveStatistics();

						for (int i = 0; i < res2datasetSplitProb.getNumResults(); i++)
						{
							double t = (Double) res2datasetSplitProb.getResultValue(i, "Threshold");
							double v = (Double) res2datasetSplitProb.getResultValue(i, "Value");
							if (!Double.isNaN(v))
							{
								//							regr.addData(t, v);
								if (valueStats.containsKey(t))
								{
									series.add(t, t, t, v, valueStats.get(t).getMin(),
											valueStats.get(t).getMax());
								}
								else
									series.add(t, t, t, v, v, v);
								lowVal = Math.min(lowVal, v);
							}
							yVals.addValue(v);

						}

						//					if (plotCurve != FilterCriterion.Both || p.toString().equals("Confidence"))
						//						score = regr.getSlope() / (yVals.getMax() - yVals.getMin());

						//					//					{
						//					if (!slopeDecrCount.containsKey(p.toString()))
						//						slopeDecrCount.put(p.toString(), new DescriptiveStatistics());
						//					double slope = regr.getSlope();
						//					if (!Double.isNaN(slope))
						//						slopeDecrCount.get(p.toString()).addValue(slope);
						//					//slopeDecrCount.put(p.toString(), slopeDecrCount.get(p.toString()) + 1)
						//					//					}

						dataset.addSeries(series);
						subtitles
								.add(new TextTitle(numValsPerThreshold.toStringDeviationFromMax()));

						final JFreeChart chart = ChartFactory.createXYLineChart(title, // chart title
								xAttr, // x axis label
								yAttr, // y axis label
								dataset, // data
								PlotOrientation.VERTICAL, true, // dataset.getSeriesCount() > 1, // include legend
								true, // tooltips
								false // urls
						);

						chart.setSubtitles(subtitles);

						chart.setBackgroundPaint(Color.WHITE);

						final XYErrorRenderer renderer = new XYErrorRenderer();

						renderer.setBaseLinesVisible(true);

						final XYPlot plot = chart.getXYPlot();
						//		renderer.setSeriesLinesVisible(0, true);
						//		renderer.setSeriesShapesVisible(0, false);//preds.length == 1);
						//			renderer.setSeriesPaint(0, FreeChartUtil.BRIGHT_RED);

						int idx = ArrayUtil.indexOf(FilterCriterion.values(), filt);
						if (idx >= 3)
							idx++;
						renderer.setSeriesPaint(0, ChartColor.createDefaultPaintArray()[idx]);

						plot.setRenderer(renderer);

						//				{
						//					Function2D func = new Function2D()
						//					{
						//						@Override
						//						public double getValue(double x)
						//						{
						//							return confRegr.predict(x);
						//						}
						//					};
						//					XYDataset result = DatasetUtilities.sampleFunction2D(func, 0, 1, 300, "exp");
						//					plot.setDataset(1, result);
						//					final XYItemRenderer renderer2 = new StandardXYItemRenderer();
						//					plot.setRenderer(1, renderer2);
						//					//			final ValueAxis rangeAxis2 = new NumberAxis("exp");
						//					//			plot.setRangeAxis(1, rangeAxis2);
						//					//			plot.mapDatasetToRangeAxis(1, 1);
						//					renderer2.setSeriesPaint(0, Color.BLACK);
						//					plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
						//					//			rangeAxis2.setLabelFont(plot.getRangeAxis().getLabelFont());
						//					//			rangeAxis2.setTickLabelFont(plot.getRangeAxis().getTickLabelFont());
						//
						//					ValueMarker marker = new ValueMarker(0);
						//					marker.setPaint(Color.black);
						//					plot.addDomainMarker(marker);
						//					plot.addRangeMarker(marker);
						//				}

						//				plot.getDomainAxis().setAutoRange(false);
						//				plot.getRangeAxis().setAutoRange(false);
						//				plot.getDomainAxis().setRange(-0.033, 1.033);

						((NumberAxis) plot.getRangeAxis()).setAutoRangeIncludesZero(false);
						((NumberAxis) plot.getRangeAxis()).setAutoRangeStickyZero(false);
						((NumberAxis) plot.getRangeAxis()).configure();

						if (!useTopPercentInsteadOfThreshold)
							((NumberAxis) plot.getDomainAxis()).setInverted(true);

						plot.setDomainGridlinePaint(Color.GRAY);
						plot.setRangeGridlinePaint(Color.GRAY);

						//				((NumberAxis) plot.getDomainAxis()).setTickUnit(new NumberTickUnit(0.2));
						//				((NumberAxis) plot.getRangeAxis()).setTickUnit(new NumberTickUnit(0.05));

						plot.setBackgroundAlpha(0.0F);
						//				chart.getLegend().setBackgroundPaint(new Color(0, 0, 0, 0));

						//				if (score > 0)
						chart.setBackgroundPaint(Color.WHITE);
						//				else
						//					chart.setBackgroundPaint(Color.LIGHT_GRAY);

						low = Math.min(low, plot.getRangeAxis().getRange().getLowerBound());

						charts.add(chart);
					}
				}

				for (JFreeChart chart : charts)
				{
					JPanel cp;
					int size = 180;
					if (chart != null)
					{
						chart.getXYPlot().getRangeAxis().setRange(Math.max(low, lowVal - 0.05),
								1.01);
						cp = new ChartPanel(chart);
						((ChartPanel) cp).setMinimumDrawHeight(size);
						((ChartPanel) cp).setMinimumDrawWidth(size);
					}
					else
					{
						cp = new JPanel();
					}
					cp.setPreferredSize(new Dimension(size, size));
					panels.add(cp);
					//					scores.add(score);
				}
			}
		}

		//		for (String k : slopeDecrCount.keySet())
		//		{
		//			System.out.println(k + " " + slopeDecrCount.get(k).getMean() + " +- "
		//					+ slopeDecrCount.get(k).getStandardDeviation() + " (median: "
		//					+ slopeDecrCount.get(k).getPercentile(50.0) + ")");
		//		}

		//		int[] order = ArrayUtil.getOrdering(
		//				ArrayUtil.toPrimitiveDoubleArray(ListUtil.toArray(scores)),
		//				useTopPercentInsteadOfThreshold);

		//		JPanel b = new JPanel(new GridLayout(2 + datasets.length,
		//				1 + filterCriteria.length * splitTechniques.length));
		//		b.setBackground(Color.WHITE);

		String layout = "";
		for (int i = 0; i < 2 * (1 + filterCriteria.length * splitTechniques.length); i++)
			layout += "p,";
		System.err.println(layout);
		layout = layout.substring(0, layout.length() - 1);
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(layout));

		for (JComponent panel : panels)
		{
			//			b.add(panel);
			builder.append(panel);
		}

		JPanel b = builder.getPanel();
		b.setBackground(Color.WHITE);

		//		for (int i = 0; i < order.length; i++)
		//		{
		//			b.add(panels.get(order[i]));
		//			System.out.println(scores.get(order[i]));
		//		}

		//		String filename = "";
		//		if (plotCurve == FilterCriterion.Both || plotCurve == FilterCriterion.Confidence)
		//			filename += "confidence";
		//		if (plotCurve == FilterCriterion.Both || plotCurve == FilterCriterion.AppDomain)
		//		{
		//			if (!filename.isEmpty())
		//				filename += "-";
		//			filename += "app-domain";
		//		}
		SwingUtil.toFile("/tmp/" + measure + "_" + classifier.getName() + "_1.png", b,
				b.getPreferredSize());

		SwingUtil.showInFrame(b, measure + " confidence app-domain plots", false);
	}
}
