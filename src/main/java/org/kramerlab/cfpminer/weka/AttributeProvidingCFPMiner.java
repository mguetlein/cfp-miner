package org.kramerlab.cfpminer.weka;

import java.util.List;
import java.util.Set;

import org.mg.cdklib.cfp.CFPMiner;
import org.mg.javalib.util.ArrayUtil;

public class AttributeProvidingCFPMiner implements AttributeProvider
{
	private static final long serialVersionUID = 1L;

	protected CFPMiner miner;

	public AttributeProvidingCFPMiner(CFPMiner miner)
	{
		this.miner = miner;
	}

	@Override
	public int getNumAttributes()
	{
		return miner.getNumFragments();
	}

	@Override
	public String getAttributeName(int a)
	{
		return miner.getFragmentViaIdx(a) + "";
	}

	@Override
	public List<String> getAttributeDomain(int a)
	{
		return ArrayUtil.toList(new String[] { "0", "1" });
	}

	@Override
	public double getAttributeValue(int i, int a)
	{
		if (miner.isFragmentIncludedInCompound(i, miner.getFragmentViaIdx(a)))
			return 1.0;
		else
			return 0.0;
	}

	@Override
	public String getName()
	{
		return miner.getName();
	}

	@Override
	public void applyFilter(Set<Integer> filterSubset)
	{
		miner.applyFilter(filterSubset);
	}

	@Override
	public double getTanimotoSimilarity(int i, int j)
	{
		return miner.getTanimotoSimilarity(i, j);
	}

}
