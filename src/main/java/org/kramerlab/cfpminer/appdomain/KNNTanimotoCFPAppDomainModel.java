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
import java.util.Random;

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
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.DataLoader;
import org.mg.javalib.freechart.HistogramPanel;
import org.mg.javalib.util.DoubleArraySummary;
import org.mg.javalib.util.ListUtil;
import org.mg.javalib.util.SetUtil;
import org.mg.javalib.util.SortedList;
import org.mg.javalib.util.SwingUtil;
import org.openscience.cdk.exception.CDKException;

public class KNNTanimotoCFPAppDomainModel implements CFPAppDomain, Serializable
{
	private static final long serialVersionUID = 11L;

	// ------- params ------- 

	protected int k = 3;

	protected double pOutside = 0.99;

	protected double pPossiblyOutside = 0.95;

	/** if false -> use median instead of mean to average */
	protected boolean mean = true;

	// ------- member variables ------- 

	protected double[] trainingDistances;

	// ------- transient member variables ------- 

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
			trainingDistances[compound] = computeKnnDist(compound);
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
	public String getAveragingScheme()
	{
		return mean ? "mean" : "median";
	}

	@Override
	public int getNumNeighbors()
	{
		return k;
	}

	protected transient DescriptiveStatistics stats;

	protected DescriptiveStatistics getStats()
	{
		if (stats == null)
			stats = new DescriptiveStatistics(trainingDistances);
		return stats;
	}

	@Override
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

	@Override
	public double getMeanTrainingDistance()
	{
		return getStats().getMean();
	}

	@Override
	public double getPThreshold(ADPrediction prediction)
	{
		switch (prediction)
		{
			case Outside:
				return pOutside;
			case PossiblyOutside:
			case Inside:
				return pPossiblyOutside;
		}
		throw new IllegalStateException();
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
	public ChartPanel getPlot(String smiles)
	{
		HistogramPanel p = new HistogramPanel("", null, "distance", "# training compounds",
				ListUtil.createList("distance of training compounds"),
				ListUtil.createList(trainingDistances), 20);

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
		plot.setRenderer(render);

		Color col = Color.CYAN.darker().darker();
		Color fontCol = col.darker().darker();

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
		p.addFunction("Cumulative Probability P(X \u2264 x)", func, col);

		plot.getRangeAxis(1).setRange(0, plot.getRangeAxis(1).getRange().getUpperBound() * 1.1);

		double val = dist.inverseCumulativeProbability(0.99);
		addMarker(p, val, fontCol, true, "P>0.99", false, 0);
		addMarker(p, val, fontCol, false, "\u21d2 Outside", false, 1);
		val = dist.inverseCumulativeProbability(0.95);
		addMarker(p, val, fontCol, true, "P\u22640.95", true, 0);
		addMarker(p, val, fontCol, false, "\u21d2 Inside", true, 1);

		if (smiles != null)
		{
			val = computeKnnDist(smiles);
			addMarker(p, val, Color.RED, true, null, false, -1);
		}
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

	@Override
	public List<Neighbor> getNeighbors(String smiles)
	{
		try
		{
			List<Neighbor> neighbors = new ArrayList<>();
			HashSet<CFPFragment> frags = miner.getFragmentsForTestCompound(smiles);
			for (int compound = 0; compound < miner.getNumCompounds(); compound++)
				neighbors.add(new Neighbor(miner.getTrainingDataSmiles().get(compound),
						distance(frags, compound)));
			Collections.sort(neighbors, new Comparator<Neighbor>()
			{
				@Override
				public int compare(Neighbor o1, Neighbor o2)
				{
					return Double.compare(o1.distance, o2.distance);
				}
			});
			return neighbors;
		}
		catch (CDKException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) throws Exception
	{
		//		List<String> smilesList = ListUtil.createList("c1ccccc1", "c1cccnc1", "c1cccnc1", "CCC",
		//				"CCCC=O", "CCCCCCC", "Br", "Cl", "CCCC");
		List<String> smilesList = DataLoader.INSTANCE.getDataset("CPDBAS_Mouse").getSmiles();
		List<String> endpoints = DataLoader.INSTANCE.getDataset("CPDBAS_Mouse").getEndpoints();
		ListUtil.scramble(new Random(1), smilesList, endpoints);

		System.out.println(smilesList.get(332));

		//		BasicCFPMiner miner = new BasicCFPMiner();
		CFPMiner miner = new CFPMiner(endpoints);

		miner.setType(CFPType.ecfp4);
		miner.setHashfoldsize(2048);
		miner.setFeatureSelection(FeatureSelection.filt);
		miner.mine(smilesList);
		System.out.println(miner);

		miner.applyFilter();

		KNNTanimotoCFPAppDomainModel ad = new KNNTanimotoCFPAppDomainModel(3, true);
		ad.setCFPMiner(miner);

		String smiles = "CC=C";
		{
			System.out.println(ad.distance(miner.getFragmentsForTestCompound(smiles), 332));
			System.exit(0);
		}

		ad.build();

		//		System.out.println("threshold: " + ad.getPValueThreshold() + " nice: "
		//				+ StringUtil.formatSmallDoubles(ad.getPValueThreshold()));

		List<CFPAppDomain.Neighbor> l = ad.getNeighbors(smiles);
		int i = 0;
		for (Neighbor n : l)
		{
			System.out.println(n.distance + " " + n.smiles);
			i++;
			if (i >= 10)
				break;
		}
		SwingUtil.showInFrame(ad.getPlot(smiles));
		SwingUtil.waitWhileWindowsVisible();
		System.exit(0);
	}
}
