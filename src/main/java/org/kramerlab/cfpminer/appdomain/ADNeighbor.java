package org.kramerlab.cfpminer.appdomain;

public class ADNeighbor
{
	private String smiles;

	private double distance;

	public ADNeighbor(String smiles, double distance)
	{
		this.smiles = smiles;
		this.distance = distance;
	}

	public String getSmiles()
	{
		return smiles;
	}

	public double getDistance()
	{
		return distance;
	}
}
