package org.kramerlab.cfpminer;

import java.io.File;
import java.util.List;

import org.mg.cdklib.cfp.CFPMiner;
import org.mg.javalib.util.ArrayUtil;
import org.mg.wekalib.data.ArffWritable;
import org.mg.wekalib.data.ArffWriter;
import org.openscience.cdk.interfaces.IAtomContainer;

import weka.core.Instances;

public class CFPtoArff
{
	public static Instances getTrainingDataset(CFPMiner miner, String endpointName) throws Exception
	{
		return ArffWriter.toInstances(getArffWritable(miner, endpointName, null));
	}

	public static void writeTrainingDataset(String arffFile, CFPMiner miner, String endpointName) throws Exception
	{
		ArffWriter.writeToArffFile(new File(arffFile), getArffWritable(miner, endpointName, null));
	}

	public static void writeTestDataset(String arffFile, CFPMiner miner, String endpointName, IAtomContainer testMol)
			throws Exception
	{
		ArffWriter.writeToArffFile(new File(arffFile), getArffWritable(miner, endpointName, testMol));
	}

	public static Instances getTestDataset(CFPMiner miner, String endpointName, IAtomContainer testMol)
			throws Exception
	{
		return ArffWriter.toInstances(getArffWritable(miner, endpointName, testMol));
	}

	private static ArffWritable getArffWritable(final CFPMiner miner, final String endpointName,
			final IAtomContainer testMol) throws Exception
	{
		return new ArffWritable()
		{
			@Override
			public String getRelationName()
			{
				return endpointName + (testMol != null ? "_test" : "");
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
					return 1;
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
						return "?";
					else
						return miner.getFragmentsForTestCompound(testMol).contains(miner.getFragmentViaIdx(attribute)) ? "1"
								: "0";
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
