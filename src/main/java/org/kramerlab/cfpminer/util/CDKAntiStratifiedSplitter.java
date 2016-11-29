package org.kramerlab.cfpminer.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.kramerlab.cfpminer.weka.eval2.CDKDataSet;
import org.kramerlab.cfpminer.weka.eval2.CFPtoArff;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.javalib.util.HashUtil;
import org.mg.javalib.util.ListUtil;
import org.mg.wekalib.eval2.data.AntiStratifiedSplitter;
import org.mg.wekalib.eval2.data.DataSet;
import org.mg.wekalib.eval2.data.TanimotoWekaAntiStratifiedSplitter;
import org.mg.wekalib.eval2.data.WekaInstancesDataSet;
import org.mg.wekalib.eval2.job.DefaultComposedKeyProvider;

import weka.core.Instances;

public class CDKAntiStratifiedSplitter extends DefaultComposedKeyProvider
		implements AntiStratifiedSplitter
{
	int hashfoldSize = 1024;
	CFPType type = CFPType.ecfp4;
	FeatureSelection feats = FeatureSelection.filt;

	class SplitSettings
	{
		List<String> smiles;
		List<String> endpoints;
		double ratio;
		long seed;

		public SplitSettings(List<String> smiles, List<String> endpoints, double ratio, long seed)
		{
			this.smiles = smiles;
			this.endpoints = endpoints;
			this.ratio = ratio;
			this.seed = seed;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof SplitSettings))
				return false;
			SplitSettings s = (SplitSettings) obj;
			return s.smiles.equals(smiles) && s.endpoints.equals(endpoints) && s.ratio == ratio
					&& s.seed == seed;
		}

		@Override
		public int hashCode()
		{
			return HashUtil.hashCode(smiles, endpoints, ratio, seed);
		}
	}

	private static HashMap<SplitSettings, Map<Boolean, List<Integer>>> cache = new HashMap<>();

	@Override
	public String getKeyContent()
	{
		return getKeyContent(hashfoldSize, type, feats);
	}

	@Override
	public String getKeyPrefix()
	{
		return this.getClass().getSimpleName();
	}

	@Override
	public List<Integer> antiStratifiedSplitIndices(DataSet dataset, double ratio, long seed,
			boolean train)
	{
		CDKDataSet d = (CDKDataSet) dataset;
		SplitSettings s = new SplitSettings(d.getSmiles(), d.getEndpoints(), ratio, seed);
		if (!cache.containsKey(s))
		{
			try
			{
				System.err.println("mining features");
				CFPMiner miner = new CFPMiner(s.endpoints);
				miner.setFeatureSelection(feats);
				miner.setHashfoldsize(hashfoldSize);
				miner.setType(type);
				miner.mine(s.smiles);
				System.err.println("to instances");
				Instances inst = CFPtoArff.getTrainingDataset(miner, "endpoint");
				WekaInstancesDataSet data = new WekaInstancesDataSet(inst, -1);
				TanimotoWekaAntiStratifiedSplitter splitter = new TanimotoWekaAntiStratifiedSplitter();
				System.err.println("anti-split");
				List<Integer> trainIdx = splitter.antiStratifiedSplitIndices(data, ratio, seed,
						train);
				List<Integer> testIdx = new ArrayList<>();
				for (int i = 0; i < s.smiles.size(); i++)
					if (!trainIdx.contains(i))
						testIdx.add(i);
				ListUtil.scramble(testIdx, new Random(seed));
				Map<Boolean, List<Integer>> m = new HashMap<>();
				m.put(true, trainIdx);
				m.put(false, testIdx);
				cache.put(s, m);
				System.err.println("anti-split done");
			}
			catch (Exception e)
			{
				throw new IllegalStateException();
			}
		}
		return cache.get(s).get(train);
	}
}
