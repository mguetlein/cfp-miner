package org.kramerlab.cfpminer.weka.appdomain;

import java.util.HashSet;

import org.kramerlab.cfpminer.weka.AttributeProvider;

import weka.core.Instance;
import weka.core.Instances;

public class TanimotoDistanceAppDomainModel extends KNNDistanceBasedAppDomainModel
{
	AttributeProvider prov;

	public TanimotoDistanceAppDomainModel(AttributeProvider prov)
	{
		this.prov = prov;
	}

	@Override
	public void buildInternal(Instances trainingData)
	{
		HashSet<Integer> filterSubset = new HashSet<Integer>();
		for (Instance oldInstance : trainingData)
			filterSubset.add((int) oldInstance.value(0));
		prov.applyFilter(filterSubset);
	}

	@Override
	public double computeDistance(Instance i1, Instance i2)
	{
		return 1 - prov.getTanimotoSimilarity((int) i1.value(0), (int) i2.value(0));
	}
}
