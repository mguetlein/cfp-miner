package org.kramerlab.cfpminer.experiments.plots;

import java.awt.Dimension;

import org.jfree.chart.ChartPanel;
import org.kramerlab.cfpminer.experiments.CFPCrossValidation;
import org.kramerlab.cfpminer.experiments.CreatePlots;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.data.DataLoader;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.datamining.ResultSetLinePlot;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.ListUtil;
import org.mg.javalib.util.SwingUtil;
import org.mg.wekalib.eval2.model.Model;
import org.mg.wekalib.eval2.model.ModelProvider;
import org.mg.wekalib.eval2.persistance.DB;
import org.mg.wekalib.eval2.persistance.ResultProviderImpl;
import org.mg.wekalib.evaluation.PredictionUtil.ClassificationMeasure;

public class CFPLineChart extends CreatePlots
{
	public CFPLineChart(CFPCrossValidation cv, ClassificationMeasure measures[], boolean addRuntime)
			throws Exception
	{
		if (cv.classifiers.size() != 1)
			throw new IllegalArgumentException();
		Model model = cv.classifiers.get(0);
		ResultSet res = cv.selectModelResults(measures, addRuntime);
		for (int i = 0; i < measures.length; i++)
			res.setNicePropery(measures[i].toString(), measures[i].shortName());

		if (cv.types.size() != 1)
			throw new IllegalArgumentException();
		CFPType type = cv.types.get(0);

		res.sortResults("FeatureSelection", false);

		System.out.println(res.toNiceString());

		String measureStr[] = ArrayUtil.toStringArray(measures);
		if (addRuntime)
			measureStr = ArrayUtil.push(measureStr, RUNTIME);
		ResultSetLinePlot plot = new ResultSetLinePlot(res, measureStr, "FeatureSelection",
				"Dataset");

		plot.setTitle(null);
		plot.setXAxisLabel(null);
		plot.setRotateXLabels(ResultSetLinePlot.XLabelsRotation.vertical);
		plot.setYAxisLabelSuffix(" (" + model.getAlgorithmShortName() + ")");
		if (measures.length > 1)
		{
			for (ClassificationMeasure m : measures)
				if (m == ClassificationMeasure.AUC || m == ClassificationMeasure.Accuracy
						|| m == ClassificationMeasure.BEDROC20
						|| m == ClassificationMeasure.BEDROC100)
				{
					plot.setYAxisRange(m.toString(), 0.5, 1.0);
					plot.setYAxisTickUnits(m.toString(), 0.125);
				}
				else if (m == ClassificationMeasure.Sensitivity
						|| m == ClassificationMeasure.Specificity
						|| m == ClassificationMeasure.AUPRC)
				{
					plot.setYAxisRange(m.toString(), 0.0, 1.0);
					plot.setYAxisTickUnits(m.toString(), 0.2);
				}
			//plot.setYAxisRange(RUNTIME, 0, 50);
		}
		else if (measures.length == 1 && measures[0] == ClassificationMeasure.AUC)
			plot.setYAxisRange(0.5, 1.0);
		else if (measures.length == 1 && measures[0] == ClassificationMeasure.AUPRC)

		{
			plot.setYAxisRange(0.0, 1.0);
			plot.setYAxisTickUnits(ClassificationMeasure.AUPRC.toString(), 0.2);
		}

		for (ClassificationMeasure m : measures)
		{
			//			if (p.equals("Accuracy"))
			//			{
			//				if (r.getResultValues("Dataset").contains("AMES"))
			plot.addMarker(m.toString(), "AMES", DataLoader.BALANCED_DATASETS);
			//				else
			//					plot.addMarker(p, "CPDBAS Dog Primates", CFPDataLoader.BALANCED_DATASETS);
			plot.addMarker(m.toString(), "ChEMBL 8", "ChEMBL");
			plot.addMarker(m.toString(), "DUD cdk2", "DUD");
			plot.addMarker(m.toString(), "MUV 466", "MUV");
			//			}
			//			else
			//			{
			//				plot.addMarker(p, "AMES", "");
			//				plot.addMarker(p, "ChEMBL_100", "");
			//				plot.addMarker(p, "DUD_cdk2", "");
			//				plot.addMarker(p, "MUV_466", "");
			//			}
		}

		int height;
		int chartH = 145;
		int legendH = 135;

		if (measures.length == 1)
		{
			plot.setShowLegend(false);
			plot.setShowDomainAxis(false);
			height = chartH;
		}
		else
		{
			height = legendH + measures.length * chartH;
		}

		ChartPanel c = plot.getChartPanel();
		c.setMaximumDrawWidth(10000);
		c.setMaximumDrawHeight(5000);
		//			//c.setPreferredSize(new Dimension(800, 600));
		//
		String name = "LineChart_" + model.getAlgorithmShortName() + "_" + type + "_"
				+ (measures.length + (addRuntime ? 1 : 0));
		//
		if (SHOW_PLOTS)
			SwingUtil.showInFrame(c, DEST_FOLDER + name, false, new Dimension(1000, 50 + height));
		if (WRITE_FILES)
		{
			plot.toSVGFile(DEST_FOLDER + name + ".svg", new Dimension(800, height));
			toPDF(DEST_FOLDER + name);
		}
	}

	public static void create() throws Exception
	{
		//Model model = ModelProvider.RANDOM_FOREST;
		for (Model model : ModelProvider.ALL_MODELS_PARAM_DEFAULT)
		{
			CFPCrossValidation cv = CFPCrossValidation.compareCFPs();
			cv.datasets = DATASETS;
			cv.sizes = ListUtil.createList(1024);
			cv.classifiers = ListUtil.create(Model.class, model);
			cv.types = ListUtil.createList(CFPType.ecfp4);

			new CFPLineChart(cv,
					new ClassificationMeasure[] { ClassificationMeasure.Accuracy,
							ClassificationMeasure.AUC, ClassificationMeasure.EnrichmentFactor5,
							ClassificationMeasure.AUPRC,

					//							ClassificationMeasure.BEDROC1,
					//							ClassificationMeasure.BEDROC2, ClassificationMeasure.BEDROC5,
					//							ClassificationMeasure.BEDROC10, ClassificationMeasure.BEDROC20,
					//							ClassificationMeasure.BEDROC30, 
					//ClassificationMeasure.BEDROC5,
					//ClassificationMeasure.BEDROC20,
					//ClassificationMeasure.BEDROC50
			}, //, ClassificationMeasure.BEDROCa100 },
					true);
			new CFPLineChart(cv, new ClassificationMeasure[] { ClassificationMeasure.AUPRC },
					false);
		}
	}

	public static void main(String[] args) throws Exception
	{
		DB.init(new ResultProviderImpl("jobs/store", "jobs/tmp"), null);
		create();
		SwingUtil.waitWhileWindowsVisible();
		System.exit(0);
	}
}
