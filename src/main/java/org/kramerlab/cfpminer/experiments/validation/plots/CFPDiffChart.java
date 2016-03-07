package org.kramerlab.cfpminer.experiments.validation.plots;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.kramerlab.cfpminer.experiments.validation.CFPCrossValidation;
import org.kramerlab.cfpminer.experiments.validation.PaperValidationResults;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.javalib.datamining.Result;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.datamining.ResultSetBoxPlot;
import org.mg.javalib.datamining.ResultSetFilter;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.ListUtil;
import org.mg.javalib.util.SwingUtil;
import org.mg.wekalib.evaluation.PredictionUtil.ClassificationMeasure;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class CFPDiffChart extends PaperValidationResults
{
	float fontSize = 15.0F;

	public CFPDiffChart()
	{

	}

	public void composeDiffCharts(String name,
			Map<FeatureSelection[], Map<String, ChartPanel>> charts)
	{
		DefaultFormBuilder builder = new DefaultFormBuilder(
				new FormLayout("p,3dlu,p,3dlu,p,3dlu,p,3dlu,p"));
		builder.append("");
		Font f = null;

		for (FeatureSelection[] vs : charts.keySet())
		{
			Map<String, ChartPanel> map = charts.get(vs);
			if (f == null)
			{
				JFreeChart p = map.values().iterator().next().getChart();
				p.setTitle("bla");
				f = p.getTitle().getFont().deriveFont(fontSize * 1.2F);
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
		if (SHOW_PLOTS)
			SwingUtil.showInFrame(builder.getPanel(), DEST_FOLDER + name + ".png");
		if (WRITE_FILES)
		{
			System.err.println("write to " + DEST_FOLDER + name + ".png");
			SwingUtil.toFile(DEST_FOLDER + name + ".png", builder.getPanel(),
					builder.getPanel().getPreferredSize());
		}
	}

	public ChartPanel diffChart(String diffProb, ResultSet res)
	{
		ResultSet r = res.copy();
		r.clearMergeCountAndVariance();

		List<String> diff = new ArrayList<>();
		List<String> ratio = new ArrayList<>();
		if (diffProb.equals(RUNTIME))
		{
			r.sortResults("FeatureSelection", false);
			ratio.add(diffProb);
		}
		else
		{
			r.sortResults("FeatureSelection", true);
			diff.add(diffProb);
		}

		//		System.out.println(r.toNiceString());
		r = r.diff("FeatureSelection", ArrayUtil.toList(new String[] { "Algorithm", "Dataset" }),
				diff, ratio);
		//		r.sortResults("Algorithm", getAlgComparator());
		System.out.println(r.toNiceString());

		//		String cmp = r.getUniqueValue("FeatureSelection").toString().replaceAll(" ", "")
		//				.replaceAll("/", "-");

		String title = null; //measure;
		String label = null;//r.getUniqueValue("FeatureSelection").toString();
		String series1 = "FeatureSelection";
		String series2 = "Algorithm";
		String category = diffProb;
		//			List<String> categories = ArrayUtil.toList(new String[] { measure });

		r.sortResults("Algorithm", ALG_COMPARATOR);

		ResultSetBoxPlot plot = new ResultSetBoxPlot(r, title, label, series1, series2, category);

		plot.setHideMean(true);
		plot.setRotateXLabels(false);
		plot.printNumResultsPerPlot(false);
		plot.setPrintMeanAndStdev(true);

		if (diffProb.equals(RUNTIME))
		{
			plot.setYTickUnit(1.0);
			plot.setYRange(-0.5, 8.5);
		}
		else if (diffProb.equals(ClassificationMeasure.AUC.toString()))
		{
			plot.setYTickUnit(0.1);
			plot.setYRange(-0.35, 0.35);
		}
		else if (diffProb.equals(ClassificationMeasure.AUPRC.toString()))
		{
			plot.setYTickUnit(0.1);
			plot.setYRange(-0.70, 0.70);
		}

		plot.setFontSize(fontSize);

		ChartPanel c = plot.getChart();
		c.setMaximumDrawWidth(10000);
		c.setMaximumDrawHeight(5000);
		c.setMinimumDrawWidth(200);
		c.setMinimumDrawHeight(200);
		//Dimension size = new Dimension(220, 220);
		Dimension size = new Dimension(280, 280);
		c.setPreferredSize(size);
		c.setOpaque(false);

		//charts.put(measure + " (" + (measure.equals("Time") ? "factor" : "\u0394") + ")", c);

		//		String name = "Chart_Diff_" + diffProb + "_" + cmp;
		//		if (SHOW_PLOTS)
		//			SwingUtil.showInFrame(c, name, false);
		//		if (WRITE_FILES)
		//		{
		//			plot.boxPlotToSVGFile(DEST_FOLDER + name + ".svg", size);
		//			//toPDF(CreatePlotsAndTables.DEST_FOLDER + name);
		//		}

		return c;
	}

	public void create() throws Exception
	{
		ClassificationMeasure[] measures = new ClassificationMeasure[] { ClassificationMeasure.AUC,
				ClassificationMeasure.AUPRC };

		//Integer chartSize = 1024;
		for (Integer chartSize : SIZES)
		{
			CFPCrossValidation cv = CFPCrossValidation.compareCFPs();
			cv.datasets = DATASETS; // ListUtil.createList("CPDBAS_Mutagenicity", "DUD_vegfr2");
			cv.types = ListUtil.createList(CFPType.ecfp4);
			cv.sizes = ListUtil.createList(chartSize);
			//			cv.classifiers = ListUtil.create(Model.class, new RandomForestModel(),
			//					new NaiveBayesModel());

			ResultSet rs = cv.selectModelResults(measures, true);

			Map<FeatureSelection[], Map<String, ChartPanel>> charts = new LinkedHashMap<>();
			//int chartSize = 4096;

			for (final FeatureSelection leaveOut : FeatureSelection.values())
			{
				ResultSet f = rs.filter(new ResultSetFilter()
				{
					@Override
					public boolean accept(Result result)
					{
						return !result.getValue("FeatureSelection").equals(leaveOut.toNiceString());
					}
				});

				Map<String, ChartPanel> map = new LinkedHashMap<String, ChartPanel>();

				for (ClassificationMeasure measure : measures)
					map.put(measure + " (\u0394)", diffChart(measure.toString(), f));

				map.put(RUNTIME + " (factor)", diffChart(RUNTIME, f));

				FeatureSelection[] feat = ArrayUtil.remove(FeatureSelection.class,
						FeatureSelection.values(), leaveOut);
				charts.put(feat, map);
			}
			composeDiffCharts("DiffChart_" + chartSize + "_" + cv.types.get(0), charts);
		}
	}

	public static void main(String[] args) throws Exception
	{
		new CFPDiffChart().create();
		SwingUtil.waitWhileWindowsVisible();
		System.exit(0);
	}
}
