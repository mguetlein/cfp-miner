package org.kramerlab.cfpminer.appdomain;

import java.util.List;

import org.jfree.chart.ChartPanel;
import org.mg.cdklib.cfp.BasicCFPMiner;

public interface CFPAppDomain
{
	public void build();

	public void setCFPMiner(BasicCFPMiner miner);

	public double getPThreshold(ADPrediction prediction);

	public ADPrediction isInsideAppdomain(String smiles);

	public double getDistance(String smiles);

	public ChartPanel getPlot(String smiles);

	public static class Neighbor
	{
		public String smiles;

		public double distance;

		public Neighbor(String smiles, double distance)
		{
			this.smiles = smiles;
			this.distance = distance;
		}
	}

	public List<Neighbor> getNeighbors(String smiles);

	public int getNumNeighbors();

	public String getAveragingScheme();

	public double getMeanTrainingDistance();

	public double getCumulativeProbability(String smiles);
}
