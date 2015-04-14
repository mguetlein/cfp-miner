package org.kramerlab.cfpminer;

import java.io.File;
import java.util.List;
import java.util.Random;

import org.kramerlab.cfpminer.CFPMiner.CFPType;
import org.kramerlab.cfpminer.CFPMiner.FeatureSelection;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.datamining.ResultSetIO;
import org.mg.javalib.util.ListUtil;

public class CFPUtil
{
	public static void printCollisions() throws Exception
	{
		CFPDataLoader l = new CFPDataLoader("data");
		ResultSet res = new ResultSet();
		String datasets[] = l.allDatasets();
		//		CFPType types[] = new CFPType[] { CFPType.ecfp6, CFPType.ecfp4, CFPType.ecfp2, CFPType.ecfp0 };
		CFPType types[] = new CFPType[] { CFPType.fcfp6, CFPType.fcfp4, CFPType.fcfp2, CFPType.fcfp0 };
		int dCount = 0;
		for (String name : datasets)
		{
			System.out.println(dCount + ": " + name);

			for (CFPType type : types)
			{
				//			if (!name.startsWith("CPDBAS") && !name.startsWith("AMES") && !name.startsWith("NCTRER"))
				//				continue;

				System.out.println(type);

				int idx = res.addResult();

				CFPMiner miner = new CFPMiner(l.getDataset(name).endpoints);
				miner.type = type;
				miner.featureSelection = FeatureSelection.filt;
				miner.hashfoldsize = 1024;
				miner.mine(l.getDataset(name).smiles);

				res.setResultValue(idx, "Dataset", name);
				res.setResultValue(idx, "Type", type + "");
				res.setResultValue(idx, "Compounds", miner.getNumCompounds());
				res.setResultValue(idx, "Fragments", miner.getNumAttributes());

				for (int size : new int[] { 1024, 2048, 4096, 8192 })
				{
					miner = new CFPMiner(l.getDataset(name).endpoints);
					miner.type = type;
					miner.featureSelection = FeatureSelection.fold;
					miner.hashfoldsize = size;
					miner.mine(l.getDataset(name).smiles);
					miner.estimateCollisions(res, idx, size + " ");
				}
			}

			res.sortResults("Dataset");
			System.out.println("\n");
			System.out.println(res.toNiceString());

			if (types.length > 1)
			{
				ResultSet joined = res.copy().join("Type");
				joined.removePropery("Dataset");

				System.out.println("\n");
				System.out.println(joined.toNiceString());
			}

			dCount++;
			//			if (dCount > 2)
			//				break;
		}
		ResultSetIO.printToFile(new File("data_collisions/collisions_fcfp.result"), res, true);
		System.exit(1);
	}

	public static void amesRuntimeTest() throws Exception
	{
		String datasetName = "AMES";
		int run = 1;
		CFPType type = CFPType.ecfp6;
		FeatureSelection featureSelection = FeatureSelection.filt;
		int hashfoldsize = 1024;

		CFPDataLoader.Dataset dataset = new CFPDataLoader("data").getDataset(datasetName, run);
		List<String> list = dataset.smiles;
		List<String> endpointValues = dataset.endpoints;
		ListUtil.scramble(new Random(1), list, endpointValues);

		CFPMiner cfps = new CFPMiner(ListUtil.cast(String.class, endpointValues));
		cfps.type = type;
		cfps.featureSelection = featureSelection;
		cfps.hashfoldsize = hashfoldsize;
		cfps.mine(list);

		String classifier = "RaF";
		for (int i : new int[] { 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192 })
		{
			cfps.hashfoldsize = i;
			CFPMiner.validate(datasetName, run, "/dev/null", new String[] { classifier }, endpointValues,
					new CFPMiner[] { cfps });
		}

		System.exit(0);
	}
}
