package org.kramerlab.cfpminer.weka.eval2;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.mg.cdklib.CDKConverter;
import org.mg.cdklib.cfp.BasicCFPMiner;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.javalib.util.ListUtil;
import org.mg.wekalib.data.ArffWritable;
import org.mg.wekalib.data.ArffWriter;
import org.mg.wekalib.data.InstancesCreator;
import org.openscience.cdk.interfaces.IAtomContainer;

import weka.core.Instances;

public class CFPtoArff
{
	public static Instances getDataset(BasicCFPMiner miner, String relationName) throws Exception
	{
		return InstancesCreator.create(getArffWritable(miner, relationName, null, null));
	}

	public static Instances getTrainingDataset(CFPMiner miner, String endpointName) throws Exception
	{
		//Instances inst = ArffWriter.toInstances(getArffWritable(miner, endpointName, null, null));
		Instances inst = InstancesCreator.create(getArffWritable(miner, endpointName, null, null));
		inst.setClassIndex(inst.numAttributes() - 1);
		return inst;
	}

	public static void writeTrainingDataset(String arffFile, CFPMiner miner, String endpointName)
			throws Exception
	{
		ArffWriter.writeToArffFile(new File(arffFile),
				getArffWritable(miner, endpointName, null, null));
	}

	public static void writeTestDataset(String arffFile, CFPMiner miner, String endpointName,
			IAtomContainer testMol) throws Exception
	{
		ArffWriter.writeToArffFile(new File(arffFile),
				getArffWritable(miner, endpointName, new IAtomContainer[] { testMol }, null));
	}

	public static Instances getTestDataset(CFPMiner miner, String endpointName,
			IAtomContainer testMol) throws Exception
	{
		//		Instances inst = ArffWriter.toInstances(
		//				getArffWritable(miner, endpointName, new IAtomContainer[] { testMol }, null));
		Instances inst = InstancesCreator.create(
				getArffWritable(miner, endpointName, new IAtomContainer[] { testMol }, null));
		inst.setClassIndex(inst.numAttributes() - 1);
		return inst;
	}

	public static Instances getTestDataset(BasicCFPMiner miner, String endpointName,
			List<String> smiles, List<String> endpoints) throws Exception
	{
		IAtomContainer mols[] = new IAtomContainer[smiles.size()];
		for (int i = 0; i < mols.length; i++)
			mols[i] = CDKConverter.parseSmiles(smiles.get(i));
		//		Instances inst = ArffWriter.toInstances(
		//				getArffWritable(miner, endpointName, mols, ListUtil.toArray(endpoints)));
		String eps[] = (endpoints != null) ? ListUtil.toArray(endpoints) : null;
		Instances inst = InstancesCreator.create(getArffWritable(miner, endpointName, mols, eps));
		inst.setClassIndex(inst.numAttributes() - 1);
		return inst;
	}

	private static ArffWritable getArffWritable(final BasicCFPMiner miner,
			final String endpointName, final IAtomContainer[] testMol, final String[] testEndpoints)
			throws Exception
	{
		final boolean isClassAvailable = (miner instanceof CFPMiner);
		final HashMap<String, Double> endpointToDouble;
		if (isClassAvailable)
		{
			endpointToDouble = new HashMap<>();
			int idx = 0;
			for (String e : ((CFPMiner) miner).getClassValues())
				endpointToDouble.put(e, (double) idx++);
		}
		else
			endpointToDouble = null;

		return new ArffWritable()
		{
			@Override
			public String getRelationName()
			{
				return endpointName + (testMol == null ? "_test" : "");
			}

			@Override
			public boolean isSparse()
			{
				return true;
			}

			//			@Override
			//			public boolean isInstanceWithoutAttributeValues(int instance)
			//			{
			//				return false;
			//			}

			@Override
			public int getNumInstances()
			{
				if (testMol == null)
					return miner.getNumCompounds();
				else
					return testMol.length;
			}

			@Override
			public int getNumAttributes()
			{
				return miner.getNumFragments() + (isClassAvailable ? 1 : 0);
			}

			@Override
			public String getMissingValue(int attribute)
			{
				throw new IllegalStateException("no missing values");
			}

			@Override
			public String[] getAttributeDomain(int attribute)
			{
				if (isClassAvailable && attribute == miner.getNumFragments())
					return ((CFPMiner) miner).getClassValues();
				else
					return new String[] { "0", "1" };
			}

			@Override
			public String getAttributeValue(int instance, int attribute) throws Exception
			{
				if (testMol == null)
				{
					if (isClassAvailable && attribute == miner.getNumFragments())
						return ((CFPMiner) miner).getEndpoints().get(instance);
					else
						return miner.getFragmentsForCompound(instance)
								.contains(miner.getFragmentViaIdx(attribute)) ? "1" : "0";
				}
				else
				{
					if (isClassAvailable && attribute == miner.getNumFragments())
					{
						if (testEndpoints == null)
							return "?";
						else
							return testEndpoints[instance];
					}
					else
						return miner.getFragmentsForTestCompound(testMol[instance])
								.contains(miner.getFragmentViaIdx(attribute)) ? "1" : "0";
				}
			}

			@Override
			public double getAttributeValueAsDouble(int instance, int attribute) throws Exception
			{
				if (testMol == null)
				{
					if (isClassAvailable && attribute == miner.getNumFragments())
						return endpointToDouble
								.get(((CFPMiner) miner).getEndpoints().get(instance));
					else
						return miner.getFragmentsForCompound(instance)
								.contains(miner.getFragmentViaIdx(attribute)) ? 1.0 : 0.0;
				}
				else
				{
					if (isClassAvailable && attribute == miner.getNumFragments())
					{
						if (testEndpoints == null)
							return Double.NaN;
						else
							return endpointToDouble.get(testEndpoints[instance]);
					}
					else
						return miner.getFragmentsForTestCompound(testMol[instance])
								.contains(miner.getFragmentViaIdx(attribute)) ? 1.0 : 0.0;
				}
			}

			@Override
			public String getAttributeName(int attribute)
			{
				if (isClassAvailable && attribute == miner.getNumFragments())
					return endpointName;
				else
					return miner.getFragmentViaIdx(attribute) + "";

			}

			@Override
			public List<String> getAdditionalInfo()
			{
				return null;
			}
		};
	}
}
