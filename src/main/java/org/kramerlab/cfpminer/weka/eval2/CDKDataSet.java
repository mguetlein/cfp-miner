package org.kramerlab.cfpminer.weka.eval2;

import java.util.ArrayList;
import java.util.List;

import org.mg.cdklib.data.CDKDataset;
import org.mg.javalib.util.HashUtil;
import org.mg.wekalib.eval2.AbstractDataSet;
import org.mg.wekalib.eval2.DataSet;

import weka.core.Instances;

public class CDKDataSet extends AbstractDataSet
{
	String name;
	CDKDataset d;

	public CDKDataSet(String name, CDKDataset d)
	{
		this.name = name;
		this.d = d;
	}

	@Override
	public int hashCode()
	{
		return HashUtil.hashCode(name, d);
	}

	public CDKDataset getCDKDataset()
	{
		return d;
	}

	@Override
	public Instances getWekaInstances()
	{
		throw new IllegalStateException("no weka instances available, use feature provider");
	}

	@Override
	public DataSet getFilteredDataset(String name, List<Integer> idx)
	{
		List<String> smiles = new ArrayList<>();
		List<String> endpoints = new ArrayList<>();
		for (Integer i : idx)
		{
			smiles.add(d.getSmiles().get(i));
			endpoints.add(d.getEndpoints().get(i));
		}
		return new CDKDataSet(name, new CDKDataset(d.getDatasetName(), smiles, endpoints, d.getWarnings()));
	}

	@Override
	public int getSize()
	{
		return d.getSmiles().size();
	}

	@Override
	public String getName()
	{
		return name;
	}

}
