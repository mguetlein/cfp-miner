package org.kramerlab.cfpminer.experiments;

import java.io.File;
import java.util.HashMap;

import org.mg.cdklib.cfp.CFPMiner;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.CDKDataset;
import org.mg.cdklib.data.DataProvider;
import org.mg.cdklib.data.DataProvider.DataID;
import org.mg.javalib.datamining.Result;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.datamining.ResultSetFilter;
import org.mg.javalib.datamining.ResultSetIO;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.DoubleKeyHashMap;
import org.mg.javalib.util.FileUtil;

public class NumFragmentsAndCollisions extends PaperResults
{
	File file = new File(
			System.getProperty("user.home") + "/results/cdklib/data_collisions/collisions.result");

	//	KeyValueFileStore<String, CFPMiner> minedFeatures = new KeyValueFileStore<>(
	//			"store/minedFeatures", true, true, "store/minedFeatures/tmp", true);

	public CFPMiner mine(CDKDataset data, CFPType type, FeatureSelection feat, int hashfoldsize)
			throws Exception
	{
		//		String key = data.getDatasetName() + "#" + type + "#" + feat + "#" + hashfoldsize;
		CFPMiner miner;
		//		if (!minedFeatures.contains(key))
		//		{
		//		System.out.println("mining");
		miner = new CFPMiner(data.getEndpoints());
		miner.setType(type);
		miner.setFeatureSelection(feat);
		miner.setHashfoldsize(hashfoldsize);
		miner.mine(data.getSmiles());
		//			minedFeatures.store(key, miner);
		//		}
		//		else
		//		{
		//			System.out.println("reading");
		//			miner = minedFeatures.get(key);
		//		}
		//		System.out.println(miner);
		return miner;
	}

	DoubleKeyHashMap<CFPType, Integer, Double> collisionRate = new DoubleKeyHashMap<>();

	public void sortDatasets(ResultSet r)
	{
		r.sortResults("Dataset", DataProvider.CFPDataComparator);
	}

	public boolean skipFiltFoldMethod(CFPType type, Integer size)
	{
		if (type == CFPType.ecfp0 || type == CFPType.fcfp0 || type == CFPType.fcfp2)
			return true;
		if (type == CFPType.ecfp2)
			return size > 1024;
		if (type == CFPType.fcfp4)
			return size > 4096;
		return false;
	}

	public HashMap<DataID, HashMap<CFPType, Integer>> numFeatures;

	public Integer getNumFeatures(DataID dataset, CFPType type)
	{
		if (numFeatures == null)
		{
			ResultSet res = ResultSetIO.readFromFile(file);
			numFeatures = new HashMap<>();
			for (int i = 0; i < res.getNumResults(); i++)
			{
				DataID d = DataID.valueOf(res.getResultValue(i, "Dataset").toString());
				CFPType t = CFPType.valueOf(res.getResultValue(i, "Type").toString());
				Integer s = Integer.valueOf(res.getResultValue(i, "Fragments").toString());
				if (s == null)
					throw new IllegalArgumentException();
				if (!numFeatures.containsKey(d))
					numFeatures.put(d, new HashMap<CFPType, Integer>());
				numFeatures.get(d).put(t, s);
			}
		}
		if (!numFeatures.containsKey(dataset))
			throw new IllegalArgumentException(
					"dataset " + dataset + " not included in " + numFeatures.keySet());
		if (!numFeatures.get(dataset).containsKey(type))
			throw new IllegalArgumentException("type " + type + " not included for dataset "
					+ dataset + " not included in " + numFeatures.get(dataset).keySet());
		if (numFeatures.get(dataset).get(type) == null)
			throw new IllegalStateException("WTF");
		return numFeatures.get(dataset).get(type);
	}

	public void compute(DataID... datasets) throws Exception
	{
		//String datasets[] = SMALL_DATASETS;
		if (datasets == null || datasets.length == 0)
			datasets = DATASETS;
		//		CFPType types[] = new CFPType[] { CFPType.ecfp6, CFPType.ecfp4, CFPType.ecfp2,
		//				CFPType.ecfp0 };
		//		CFPType types[] = new CFPType[] { CFPType.fcfp6, CFPType.fcfp4, CFPType.fcfp2,
		//				CFPType.fcfp0 };
		int dCount = 0;
		for (final DataID d : datasets)
		{
			System.out.println(dCount + ": " + d);
			CDKDataset data = DataProvider.getDataset(d);

			for (final CFPType type : CFPType.values())
			{
				ResultSet res;
				if (file.exists())
					res = ResultSetIO.readFromFile(file);
				else
					res = new ResultSet();

				//			if (!name.startsWith("CPDBAS") && !name.startsWith("AMES") && !name.startsWith("NCTRER"))
				//				continue;

				if (res.filter(new ResultSetFilter()
				{
					@Override
					public boolean accept(Result result)
					{
						return result.getValue("Dataset").equals(d.toString())
								&& result.getValue("Type").equals(type.toString());
					}
				}).getNumResults() == 1)
					continue;

				System.out.println(type);

				int idx = res.addResult();
				CFPMiner miner = mine(data, type, FeatureSelection.none, 0);
				res.setResultValue(idx, "Dataset", d.toString());
				res.setResultValue(idx, "Type", type + "");
				res.setResultValue(idx, "Compounds", miner.getNumCompounds());
				res.setResultValue(idx, "Fragments", miner.getNumFragments());

				//				System.out.println("mining collisions");
				for (int size : new int[] { 1024, 2048, 4096, 8192 })
				{
					miner = mine(data, type, FeatureSelection.fold, size);
					//					miner = new CFPMiner(l.getDataset(name).getEndpoints());
					//					miner.setType(type);
					//					miner.setFeatureSelection(FeatureSelection.fold);
					//					miner.setHashfoldsize(size);
					//					miner.mine(l.getDataset(name).getSmiles());
					miner.estimateCollisions(res, idx, size + " ");
				}

				//			res.sortResults("Dataset");

				if (ArrayUtil.indexOf(CFPType.values(), type) == CFPType.values().length - 1)
				{
					System.out.println("\n");
					System.out.println(res.toNiceString());
				}

				//			if (types.length > 1)
				//			{
				//				ResultSet joined = res.copy().join("Type");
				//				joined.removePropery("Dataset");
				//
				//				System.out.println("\n");
				//				System.out.println(joined.toNiceString());
				//			}

				ResultSetIO.writeToFile(file, res);
			}

			dCount++;
			//			if (dCount > 2)
			//				break;
		}
		//		ResultSetIO.printToTxtFile(new File(System.getProperty("user.home")
		// + "/results/cdklib/data_collisions/collisions.result"), res, true);
		//System.exit(1);
	}

	public void printTables()
	{
		String preProp = "& ";
		for (Integer size : SIZES)
			preProp += "& \\multicolumn{2}{c}{" + size + "} ";
		preProp += "\\\\\n";

		for (final String type : new String[] { "ecfp", "fcfp" })
		{

			ResultSet res = ResultSetIO.readFromFile(file);
			res = res.filter(new ResultSetFilter()
			{
				@Override
				public boolean accept(Result result)
				{
					return result.getValue("Type").toString().contains(type);
				}
			});

			//				System.out.println(res.toNiceString());
			sortDatasets(res);

			for (final String typeSize : new String[] { "6", "4", "2", "0" })
			{
				CFPType t = CFPType.valueOf(type + typeSize);

				ResultSet r = res.copy().filter(new ResultSetFilter()
				{
					@Override
					public boolean accept(Result result)
					{
						return result.getValue("Type").toString().equals(type + typeSize);
					}
				});
				r.removePropery("Type");
				for (Integer size : SIZES)
				{
					if (skipFiltFoldMethod(t, size))
					{
						System.err.println("skip " + t + " " + size);
						for (int i = 0; i < r.getNumResults(); i++)
						{
							r.setResultValue(i, size + " bit-load", null);
							r.setResultValue(i, size + " collisions", null);
						}
					}
					else
					{
						r.setNicePropery(size + " bit-load", "bit-load");
						r.setNicePropery(size + " collisions", "rate");
					}
				}

				System.out.println("\n");
				String datasetPreProp = "& " + preProp;
				System.out.println(datasetPreProp);
				System.out.println(r.toNiceString());

				if (WRITE_FILES)
				{
					String dest = DEST_FOLDER + "collisions_" + type + typeSize + ".tex";
					System.out.println("write table to " + dest);
					FileUtil.writeStringToFile(dest,
							r.toLatexTable(null, (Integer[]) null, datasetPreProp));
				}
			}

			ResultSet joined = res.join("Type");
			for (int i = 0; i < joined.getNumResults(); i++)
			{
				CFPType t = CFPType.valueOf(joined.getResultValue(i, "Type").toString());
				for (Integer size : SIZES)
				{
					if (skipFiltFoldMethod(t, size))
					{
						joined.setResultValue(i, size + " bit-load", null);
						joined.setResultValue(i, size + " collisions", null);
					}
				}
			}

			joined.removePropery("Dataset");
			joined.removePropery("Compounds");

			for (Integer size : SIZES)
			{
				joined.setNicePropery(size + " bit-load", "bit-load");
				joined.setNicePropery(size + " collisions", "rate");
			}

			for (int i = 0; i < joined.getNumResults(); i++)
			{
				CFPType t = CFPType.valueOf(joined.getResultValue(i, "Type").toString());
				for (Integer s : SIZES)
				{
					Double rate = (Double) joined.getResultValue(i, s + " collisions");
					//					System.out.println(t + " " + s + " " + rate);
					collisionRate.put(t, s, rate);
				}
			}

			System.out.println("\n");
			System.out.println(preProp);
			System.out.println(joined.toNiceString());

			if (WRITE_FILES)
			{
				String dest = DEST_FOLDER + "collisions_" + type + ".tex";
				System.out.println("write table to " + dest);
				FileUtil.writeStringToFile(dest,
						joined.toLatexTable(null, (Integer[]) null, preProp));
			}
		}
	}

	public static void main(String[] args) throws Exception
	{
		WRITE_FILES = false;
		new NumFragmentsAndCollisions().compute(DataID.LTKB);
		new NumFragmentsAndCollisions().printTables();
	}
}
