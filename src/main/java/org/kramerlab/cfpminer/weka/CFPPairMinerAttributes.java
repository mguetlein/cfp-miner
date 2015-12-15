package org.kramerlab.cfpminer.weka;

import java.util.List;

import org.mg.cdklib.cfp.CFPPairMiner;
import org.mg.javalib.util.ArrayUtil;

public class CFPPairMinerAttributes extends CFPMinerAttributes
{
	private static final long serialVersionUID = 1L;

	protected CFPPairMiner miner;

	public CFPPairMinerAttributes(CFPPairMiner miner)
	{
		super(miner);
	}

	@Override
	public List<String> getAttributeDomain(int a)
	{
		return ArrayUtil.toList(new String[] { "0", "1" });
	}

	@Override
	public int getNumAttributes()
	{
		return miner.getNumFragments() + miner.getNumPairs();
	}

	@Override
	public String getAttributeName(int a)
	{
		if (a < super.getNumAttributes())
			return super.getAttributeName(a);
		else
			return miner.getPair(a - super.getNumAttributes()).getName();
	}

	@Override
	public double getAttributeValue(int i, int a)
	{
		if (a < super.getNumAttributes())
			return super.getAttributeValue(i, a);
		if (miner.getPair(a - super.getNumAttributes()).isCompoundAdjacent(i))
			return 1.0;
		else
			return 0.0;
	}
}
