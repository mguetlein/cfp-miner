package org.kramerlab.cfpminer.appdomain;

import java.util.List;

import org.mg.cdklib.cfp.BasicCFPMiner;

public interface ADModel
{
	public void build();

	public void setCFPMiner(BasicCFPMiner miner);

	public ADPrediction isInsideAppdomain(String smiles);

	public double getDistance(String smiles);

	public List<ADNeighbor> getNeighbors(String smiles);
}
