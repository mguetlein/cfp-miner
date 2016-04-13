package org.kramerlab.cfpminer.appdomain;

import org.jfree.chart.ChartPanel;

public interface ADInfoModel extends ADModel
{
	public String getDocumentation();

	public String getGeneralInfo(boolean details);

	public String getDistanceInfo(boolean details);

	public String getNeighborInfo(boolean details);

	public String getPredictionDistanceInfo(String smiles, boolean details);

	public String getPredictionRationalInfo(String smiles, boolean details);

	public ChartPanel getPlot(String smiles);
}
