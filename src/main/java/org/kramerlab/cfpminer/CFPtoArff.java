package org.kramerlab.cfpminer;

import java.io.File;
import java.util.List;

import org.mg.cdklib.CDKConverter;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.ListUtil;
import org.mg.wekalib.data.ArffWritable;
import org.mg.wekalib.data.ArffWriter;
import org.openscience.cdk.interfaces.IAtomContainer;

import weka.core.Instances;

public class CFPtoArff
{
	public static Instances getTrainingDataset(CFPMiner miner, String endpointName) throws Exception
	{
		Instances inst = ArffWriter.toInstances(getArffWritable(miner, endpointName, null, null));
		inst.setClassIndex(inst.numAttributes() - 1);
		return inst;
	}

	public static void writeTrainingDataset(String arffFile, CFPMiner miner, String endpointName) throws Exception
	{
		ArffWriter.writeToArffFile(new File(arffFile), getArffWritable(miner, endpointName, null, null));
	}

	public static void writeTestDataset(String arffFile, CFPMiner miner, String endpointName, IAtomContainer testMol)
			throws Exception
	{
		ArffWriter.writeToArffFile(new File(arffFile),
				getArffWritable(miner, endpointName, new IAtomContainer[] { testMol }, null));
	}

	public static Instances getTestDataset(CFPMiner miner, String endpointName, IAtomContainer testMol)
			throws Exception
	{
		Instances inst = ArffWriter.toInstances(getArffWritable(miner, endpointName, new IAtomContainer[] { testMol },
				null));
		inst.setClassIndex(inst.numAttributes() - 1);
		return inst;
	}

	public static Instances getTestDataset(CFPMiner miner, String endpointName, List<String> smiles,
			List<String> endpoints) throws Exception
	{
		IAtomContainer mols[] = new IAtomContainer[smiles.size()];
		for (int i = 0; i < mols.length; i++)
			mols[i] = CDKConverter.parseSmiles(smiles.get(i));
		Instances inst = ArffWriter
				.toInstances(getArffWritable(miner, endpointName, mols, ListUtil.toArray(endpoints)));
		inst.setClassIndex(inst.numAttributes() - 1);
		return inst;
	}

	private static ArffWritable getArffWritable(final CFPMiner miner, final String endpointName,
			final IAtomContainer[] testMol, final String[] testEndpoints) throws Exception
	{
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
				return false;
			}

			@Override
			public boolean isInstanceWithoutAttributeValues(int instance)
			{
				return false;
			}

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
				return miner.getNumFragments() + 1;
			}

			@Override
			public String getMissingValue(int attribute)
			{
				throw new IllegalStateException("no missing values");
			}

			@Override
			public String getAttributeValueSpace(int attribute)
			{
				if (attribute == miner.getNumFragments())
					return ArrayUtil.toString(miner.getClassValues(), ",", "{", "}");
				else
					return "{0,1}";
			}

			@Override
			public String getAttributeValue(int instance, int attribute) throws Exception
			{
				if (testMol == null)
				{
					if (attribute == miner.getNumFragments())
						return miner.getEndpoints().get(instance);
					else
						return miner.getFragmentsForCompound(instance).contains(miner.getFragmentViaIdx(attribute)) ? "1"
								: "0";
				}
				else
				{
					if (attribute == miner.getNumFragments())
					{
						if (testEndpoints == null)
							return "?";
						else
							return testEndpoints[instance];
					}
					else
						return miner.getFragmentsForTestCompound(testMol[instance]).contains(
								miner.getFragmentViaIdx(attribute)) ? "1" : "0";
				}
			}

			@Override
			public String getAttributeName(int attribute)
			{
				if (attribute == miner.getNumFragments())
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
