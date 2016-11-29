package org.kramerlab.cfpminer.appdomain;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.function.Function2D;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.mg.cdklib.cfp.BasicCFPMiner;
import org.mg.cdklib.cfp.CFPFragment;
import org.mg.javalib.freechart.HistogramPanel;
import org.mg.javalib.util.DoubleArraySummary;
import org.mg.javalib.util.ListUtil;
import org.mg.javalib.util.ResourceBundleOwner;
import org.mg.javalib.util.SetUtil;
import org.mg.javalib.util.SortedList;
import org.mg.javalib.util.StringUtil;
import org.openscience.cdk.exception.CDKException;

public class KNNTanimotoCFPAppDomainModel implements ADInfoModel, Serializable
{
	private static final long serialVersionUID = 12L;

	// ------- params ------- 

	protected int k = 3;

	protected double pOutside = 0.99;

	protected double pPossiblyOutside = 0.95;

	/** if false -> use median instead of mean to average */
	protected boolean mean = true;

	// ------- member variables ------- 

	protected double[] trainingDistances;

	// ------- transient member variables ------- 

	private static ResourceBundleOwner bundle = new ResourceBundleOwner("cfp-miner");

	protected transient BasicCFPMiner miner;

	public KNNTanimotoCFPAppDomainModel(int k, boolean mean)
	{
		this.k = k;
		this.mean = mean;
	}

	@Override
	public void build()
	{
		trainingDistances = new double[miner.getNumCompounds()];
		for (int compound = 0; compound < miner.getNumCompounds(); compound++)
		{
			if (compound % 500 == 0)
				System.out.println("building ad " + compound);
			trainingDistances[compound] = computeKnnDist(compound);
		}
	}

	@Override
	public void setCFPMiner(BasicCFPMiner miner)
	{
		this.miner = miner;
	}

	@Override
	public double getDistance(String smiles)
	{
		return computeKnnDist(smiles);
	}

	@Override
	public double getDistanceWithoutIdenticalSmiles(String smiles)
	{
		int idx = miner.getTrainingDataSmiles().indexOf(smiles);
		if (idx == -1)
			return computeKnnDist(smiles);
		else
			return computeKnnDist(idx);
	}

	protected transient DescriptiveStatistics stats;

	protected DescriptiveStatistics getStats()
	{
		if (stats == null)
			stats = new DescriptiveStatistics(trainingDistances);
		return stats;
	}

	public double getCumulativeProbability(String smiles)
	{
		return new NormalDistribution(getStats().getMean(), getStats().getStandardDeviation())
				.cumulativeProbability(getDistance(smiles));
	}

	@Override
	public final ADPrediction isInsideAppdomain(String smiles)
	{
		double p = getCumulativeProbability(smiles);

		if (p > pOutside)
			return ADPrediction.Outside;
		else if (p > pPossiblyOutside)
			return ADPrediction.PossiblyOutside;
		else
			return ADPrediction.Inside;
	}

	private double computeKnnDist(String smiles)
	{
		try
		{
			return computeKnnDist(miner.getFragmentsForTestCompound(smiles), -1);
		}
		catch (CDKException e)
		{
			throw new RuntimeException(e);
		}
	}

	private double computeKnnDist(Integer compound)
	{
		return computeKnnDist(miner.getFragmentsForCompound(compound), compound);
	}

	protected transient HashMap<HashSet<CFPFragment>, HashMap<Integer, Double>> cachedKnnDist;

	private double computeKnnDist(HashSet<CFPFragment> frags, int trainingCompound)
	{
		if (cachedKnnDist == null)
			cachedKnnDist = new HashMap<>();
		if (!cachedKnnDist.containsKey(frags)
				|| !cachedKnnDist.get(frags).containsKey(trainingCompound))
		{
			SortedList<Double> dists = new SortedList<>();
			for (int compound = 0; compound < miner.getNumCompounds(); compound++)
			{
				if (trainingCompound == compound)
					continue;
				double d = distance(frags, compound);

				if (dists.size() < k)
				{
					dists.add(d);
				}
				else if (d < dists.get(k - 1))
				{
					dists.add(d);
					dists.remove(k);
				}
			}
			Double d;
			if (mean)
				d = DoubleArraySummary.create(dists).getMean();
			else
				d = DoubleArraySummary.create(dists).getMedian();
			if (!cachedKnnDist.containsKey(frags))
				cachedKnnDist.put(frags, new HashMap<Integer, Double>());
			cachedKnnDist.get(frags).put(trainingCompound, d);
		}
		return cachedKnnDist.get(frags).get(trainingCompound);
	}

	private double distance(HashSet<CFPFragment> frags, int compound)
	{
		HashSet<CFPFragment> frags2 = miner.getFragmentsForCompound(compound);
		double and = SetUtil.intersectSize(frags, frags2);
		double or = frags.size() + frags2.size() - and;
		return 1.0 - and / or;
	}

	@Override
	public List<ADNeighbor> getNeighbors(String smiles)
	{
		try
		{
			List<ADNeighbor> neighbors = new ArrayList<>();
			HashSet<CFPFragment> frags = miner.getFragmentsForTestCompound(smiles);
			for (int compound = 0; compound < miner.getNumCompounds(); compound++)
				neighbors.add(new ADNeighbor(miner.getTrainingDataSmiles().get(compound),
						distance(frags, compound)));
			Collections.sort(neighbors, new Comparator<ADNeighbor>()
			{
				@Override
				public int compare(ADNeighbor o1, ADNeighbor o2)
				{
					return Double.compare(o1.getDistance(), o2.getDistance());
				}
			});
			return neighbors;
		}
		catch (CDKException e)
		{
			throw new RuntimeException(e);
		}
	}

	// -------------- info ----------------------------

	public String getDocumentation()
	{
		return bundle.text("appdomain.documentation", mean ? "mean" : "median", k,
				StringUtil.formatDouble(pOutside), ADPrediction.Outside.toNiceString(),
				StringUtil.formatDouble(pPossiblyOutside),
				ADPrediction.PossiblyOutside.toNiceString(), ADPrediction.Inside.toNiceString());
	}

	@Override
	public String getGeneralInfo(boolean details)
	{
		return bundle.text("appdomain.general.appdomain" + (details ? ".details" : ""));
	}

	@Override
	public String getDistanceInfo(boolean details)
	{
		return bundle.text("appdomain.general.distance" + (details ? ".details" : ""),
				mean ? "mean" : "median", k);
	}

	@Override
	public String getNeighborInfo(boolean details)
	{
		if (!details)
			return bundle.text("appdomain.general.neighbor", k);
		else
			return getDistanceInfo(true);
	}

	@Override
	public String getPredictionDistanceInfo(String smiles, boolean details)
	{
		if (!details)
			return bundle.text("appdomain.prediction.distance",
					StringUtil.formatDouble(getStats().getMean()),
					StringUtil.formatDouble(getDistance(smiles)));
		else
			return getDistanceInfo(true);
	}

	@Override
	public String getPredictionRationalInfo(String smiles, boolean details)
	{
		if (!details)
		{
			ADPrediction prediction = isInsideAppdomain(smiles);
			return bundle.text("appdomain.prediction.rationale", prediction.toNiceString())
					+ bundle.text("appdomain.prediction.rationale." + prediction);
		}
		else
		{
			return bundle.text("appdomain.prediction.rationale.details",
					StringUtil.formatSmallDoubles(getDistance(smiles)),
					StringUtil.formatSmallDoubles(getCumulativeProbability(smiles)),
					pPossiblyOutside, ADPrediction.PossiblyOutside.toNiceString(), pOutside,
					ADPrediction.Outside.toNiceString());
		}
	}

	@Override
	public ChartPanel getPlot(String smiles)
	{
		return getPlot(smiles == null ? null : new String[] { smiles });
	}

	public ChartPanel getPlot(String... smiles)
	{
		List<String> labels = ListUtil.createList("Distance of training compounds");
		List<double[]> vals = ListUtil.createList(trainingDistances);

		if (smiles != null && smiles.length > 1)
		{
			double[] values = new double[smiles.length];
			for (int i = 0; i < values.length; i++)
				values[i] = computeKnnDist(smiles[i]);
			labels.add(0, "Test compounds");
			vals.add(0, values);
		}

		HistogramPanel p = new HistogramPanel("", null, "Distance", "# Training compounds", labels,
				vals, 20);

		JFreeChart chart = p.getChart();
		XYPlot plot = (XYPlot) chart.getPlot();

		//		chart.removeLegend();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setRangeGridlinePaint(Color.GRAY);
		plot.setDomainGridlinePaint(Color.GRAY);
		chart.setBackgroundPaint(new Color(0, 0, 0, 0));

		XYBarRenderer render = new XYBarRenderer();
		render.setShadowVisible(false);
		StandardXYBarPainter painter = new StandardXYBarPainter();
		render.setBarPainter(painter);
		render.setSeriesPaint(0, new Color(0, 0, 0, 0));
		render.setDrawBarOutline(true);
		render.setSeriesOutlinePaint(0, Color.BLACK);
		if (smiles != null && smiles.length > 1)
		{
			render.setSeriesPaint(1, new Color(100, 0, 0, 50));
			render.setSeriesOutlinePaint(1, new Color(255, 0, 0, 255));
		}
		plot.setRenderer(render);

		Color probCol = new Color(0, 139, 139);

		final NormalDistribution dist = new NormalDistribution(getStats().getMean(),
				getStats().getStandardDeviation());
		Function2D func = new Function2D()
		{
			@Override
			public double getValue(double x)
			{
				return dist.cumulativeProbability(x);
			}
		};
		p.addFunction("Cumulative probability P(X \u2264 x)", func, probCol);

		plot.getRangeAxis(1).setRange(0, plot.getRangeAxis(1).getRange().getUpperBound() * 1.1);

		double val = dist.inverseCumulativeProbability(0.99);
		addMarker(p, val, probCol, true, "P>0.99", false, 0);
		addMarker(p, val, probCol, false, "\u21d2 Outside", false, 1);
		val = dist.inverseCumulativeProbability(0.95);
		addMarker(p, val, probCol, true, "P\u22640.95", true, 0);
		addMarker(p, val, probCol, false, "\u21d2 Inside", true, 1);

		if (smiles != null && smiles.length == 1)
		{
			String smi = smiles[0];
			val = computeKnnDist(smi);
			addMarker(p, val, Color.RED, true, null, false, -1);
			if (val >= plot.getDomainAxis().getRange().getUpperBound())
				plot.getDomainAxis().setRange(plot.getDomainAxis().getRange().getLowerBound(),
						val * 1.1);
		}

		if (plot.getDomainAxis().getRange().getUpperBound() > 1.05)
			plot.getDomainAxis().setRange(plot.getDomainAxis().getRange().getLowerBound(), 1.05);

		return p.getChartPanel();

	}

	private void addMarker(HistogramPanel p, double val, Color col, boolean drawLine, String msg,
			boolean left, int row)
	{
		XYPlot plot = (XYPlot) p.getChart().getPlot();
		Font f = plot.getDomainAxis().getTickLabelFont();
		FontMetrics fm = p.getFontMetrics(f);

		ValueMarker marker = new ValueMarker(val);
		if (msg != null)
		{
			marker.setLabel(msg);
			marker.setLabelFont(f);
			marker.setLabelPaint(col);
			marker.setLabelAnchor(left ? RectangleAnchor.TOP_LEFT : RectangleAnchor.TOP_RIGHT);
			double offset = 5 + fm.stringWidth(msg) * 0.5;
			marker.setLabelOffset(
					new RectangleInsets(15 + row * 15, left ? offset : 0, 0, left ? 0 : offset));
		}
		if (drawLine)
		{
			marker.setPaint(col);
			marker.setStroke(new BasicStroke(2.0F));
		}

		plot.addDomainMarker(marker);
	}

	// -------------- info ----------------------------

	public static void main(String[] args) throws Exception
	{
		String modelIds[] = { "AMES", "CPDBAS_Mouse", "NCTRER", "ChEMBL_93", "MUV_733",
				"ChEMBL_100", "MUV_644", "DUD_vegfr2" };
		List<String> smiles = ListUtil.createList("c1ccccc1", "c1cccnc1", "c1cccnc1", "CCC",
				"CCCC=O", "CCCCCCC", "Br", "Cl", "CCCC");

		//		for (String data : modelIds)
		//		{
		//			String cfpFile = "/home/martin/results/coffer/persistance/model/" + data + ".cfp";
		//			FSTObjectInput in = new FSTObjectInput(new FileInputStream(cfpFile));
		//			CFPMiner c = (CFPMiner) in.readObject();
		//			in.close();
		//			String adFile = "/home/martin/results/coffer/persistance/model/" + data + ".appdomain";
		//			in = new FSTObjectInput(new FileInputStream(adFile));
		//			KNNTanimotoCFPAppDomainModel ad = (KNNTanimotoCFPAppDomainModel) in.readObject();
		//			in.close();
		//
		//			ad.setCFPMiner(c);
		//
		//			for (String smi : smiles)
		//			{
		//				StopWatchUtil.start("predict-ad");
		//				System.out.println(smi + " " + ad.isInsideAppdomain(smi));
		//				StopWatchUtil.stop("predict-ad");
		//			}
		//			StopWatchUtil.print();
		//		}
		//
		//		CDKDataset d2 = DataLoader.getDatasetFromSMILES("mastermatrix",
		//				"/home/martin/data/envipath/mastermatrix.ob.smi");
		//		CDKDataset d = DataLoader.getDatasetFromSMILES("kegg",
		//				"/home/martin/data/envipath/KEGG_matrix_100316.ob.smi");
		//
		//		//CDKDataset d = DataLoader.INSTANCE.getDataset("CPDBAS_Mouse");
		//
		//		List<String> smilesList = d.getSmiles();
		//		List<String> endpoints = d.getEndpoints();
		//		ListUtil.scramble(new Random(1), smilesList, endpoints);
		//
		//		//		System.out.println(smilesList.get(332));
		//
		//		//		BasicCFPMiner miner = new BasicCFPMiner();
		//		CFPMiner miner = new CFPMiner(endpoints);
		//
		//		miner.setType(CFPType.ecfp4);
		//		miner.setHashfoldsize(2048);
		//		miner.setFeatureSelection(FeatureSelection.none);
		//		miner.mine(smilesList);
		//		System.out.println(miner);
		//
		//		//miner.applyFilter();
		//
		//		KNNTanimotoCFPAppDomainModel ad = new KNNTanimotoCFPAppDomainModel(3, true);
		//
		//		//		System.out.println(ad.getGeneralInfo(false));
		//		//		System.exit(1);
		//
		//		ad.setCFPMiner(miner);
		//
		//		//		String smiles = "CC=C";
		//		//		{
		//		//			System.out.println(ad.distance(miner.getFragmentsForTestCompound(smiles), 332));
		//		//			System.exit(0);
		//		//		}
		//
		//		ad.build();
		//
		//		//		System.out.println("threshold: " + ad.getPValueThreshold() + " nice: "
		//		//				+ StringUtil.formatSmallDoubles(ad.getPValueThreshold()));
		//
		//		//		List<ADNeighbor> l = ad.getNeighbors(smiles);
		//		//		int i = 0;
		//		//		for (ADNeighbor n : l)
		//		//		{
		//		//			System.out.println(n.getDistance() + " " + n.getSmiles());
		//		//			i++;
		//		//			if (i >= 3)
		//		//				break;
		//		//		}
		//
		//		SwingUtil.showInFrame(ad.getPlot(ArrayUtil.toArray(d2.getSmiles())));
		//		SwingUtil.waitWhileWindowsVisible();
		//		System.exit(0);

	}

	public BasicCFPMiner getCFPMiner()
	{
		return miner;
	}
}
