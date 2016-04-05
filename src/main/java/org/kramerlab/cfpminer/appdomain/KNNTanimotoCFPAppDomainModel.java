package org.kramerlab.cfpminer.appdomain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.math3.stat.inference.TestUtils;
import org.jfree.chart.ChartPanel;
import org.mg.cdklib.cfp.BasicCFPMiner;
import org.mg.cdklib.cfp.CFPFragment;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.Binning;
import org.mg.javalib.util.DoubleArraySummary;
import org.mg.javalib.util.ListUtil;
import org.mg.javalib.util.SetUtil;
import org.mg.javalib.util.SortedList;
import org.mg.javalib.util.StringUtil;
import org.mg.javalib.util.SwingUtil;
import org.openscience.cdk.exception.CDKException;

public class KNNTanimotoCFPAppDomainModel implements CFPAppDomain, Serializable
{
	private static final long serialVersionUID = 10L;

	// ------- params ------- 

	protected int k = 3;

	protected double minP = 0.001;

	/** if false -> use median instead of mean to average */
	protected boolean mean = true;

	// ------- member variables ------- 

	protected double meanTrainingDistance;

	protected double maxTrainingDistance;

	protected Binning binning;

	// ------- transient member variables ------- 

	protected transient BasicCFPMiner miner;

	public KNNTanimotoCFPAppDomainModel(int k, double minP, boolean mean)
	{
		this.k = k;
		this.minP = minP;
		this.mean = mean;
	}

	@Override
	public void build()
	{
		List<Double> knnTrainingDistances = new ArrayList<>();
		for (int compound = 0; compound < miner.getNumCompounds(); compound++)
			knnTrainingDistances.add(computeKnnDist(compound));
		DoubleArraySummary stats = DoubleArraySummary.create(knnTrainingDistances);
		meanTrainingDistance = stats.getMean();
		maxTrainingDistance = stats.getMax();
		binning = new Binning(ArrayUtil.toPrimitiveDoubleArray(knnTrainingDistances), 10, false);
	}

	@Override
	public void setCFPMiner(BasicCFPMiner miner)
	{
		this.miner = miner;
	}

	@Override
	public double getPValueThreshold()
	{
		return minP;
	}

	@Override
	public double getDistance(String smiles)
	{
		return computeKnnDist(smiles);
	}

	@Override
	public double getMeanTrainingDistance()
	{
		return meanTrainingDistance;
	}

	@Override
	public double getMaxTrainingDistance()
	{
		return maxTrainingDistance;
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

	@Override
	public final boolean isInsideAppdomain(String smiles)
	{
		return pValue(smiles) > getPValueThreshold();
	}

	protected transient HashMap<String, Double> cachedPValues;

	@Override
	public final double pValue(String smiles)
	{
		if (cachedPValues == null)
			cachedPValues = new HashMap<>();
		if (!cachedPValues.containsKey(smiles))
		{
			double dist = computeKnnDist(smiles);
			double p;
			if (dist <= meanTrainingDistance)
				p = 1.0;
			else if (dist > maxTrainingDistance)
				p = 0.0;
			else
			{
				long all[] = binning.getAllCounts();
				long selected[] = binning.getSelectedCounts(dist);
				p = TestUtils.chiSquareTestDataSetsComparison(selected, all);
			}
			System.out.println(p);
			if (p < minP)
			{
				//				System.out.println("distance: " + dist);
				//				SwingUtil.showInFrame(binning.plot(dist), dist + " : " + p, false);
			}
			cachedPValues.put(smiles, p);
		}
		return cachedPValues.get(smiles);
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
		ChartPanel p;
		if (smiles == null)
			p = binning.plot();
		else
			p = binning.plot(computeKnnDist(smiles));
		p.getChart().getXYPlot().getRangeAxis().setLabel("# training compounds");
		p.getChart().getXYPlot().getDomainAxis().setLabel("distance");
		return p;
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
		List<String> smilesList = ListUtil.createList("c1ccccc1", "c1cccnc1", "c1cccnc1", "CCC",
				"CCCC=O", "CCCCCCC", "Br", "Cl", "CCCC");
		BasicCFPMiner miner = new BasicCFPMiner();
		miner.setType(CFPType.ecfp4);
		miner.setFeatureSelection(FeatureSelection.none);
		miner.mine(smilesList);
		System.out.println(miner);

		CFPAppDomain ad = new KNNTanimotoCFPAppDomainModel(3, 0.001, true);
		ad.setCFPMiner(miner);
		ad.build();

		System.out.println("threshold: " + ad.getPValueThreshold() + " nice: "
				+ StringUtil.formatSmallDoubles(ad.getPValueThreshold()));

		String smiles = "CCCCC";
		List<CFPAppDomain.Neighbor> l = ad.getNeighbors(smiles);
		for (Neighbor n : l)
			System.out.println(n.distance + " " + n.smiles);
		SwingUtil.showInDialog(ad.getPlot(smiles));
	}

}
