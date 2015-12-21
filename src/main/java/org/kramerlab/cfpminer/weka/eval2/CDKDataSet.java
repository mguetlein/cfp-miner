package org.kramerlab.cfpminer.weka.eval2;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.mg.cdklib.data.CDKDataset;
import org.mg.wekalib.eval2.data.AbstractDataSet;
import org.mg.wekalib.eval2.data.DataSet;

import weka.core.Instances;

public class CDKDataSet extends AbstractDataSet
{
	String name;
	CDKDataset d;
	String key;

	public CDKDataSet(String name, CDKDataset d)
	{
		this.name = name;
		this.d = d;

		StringBuffer b = new StringBuffer();
		b.append(name);
		b.append('#');
		b.append(d.getDatasetName());
		b.append('#');
		b.append(d.getEndpoints());
		b.append('#');
		b.append(d.getSmiles());
		b.append('#');
		b.append(d.getWarnings());
		key = DigestUtils.md5Hex(b.toString());
	}

	@Override
	public String getKey()
	{
		return getKey(key);
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
