package org.kramerlab.cfpminer.weka.eval2;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.mg.cdklib.data.CDKDataset;
import org.mg.cdklib.data.DataLoader;
import org.mg.openbabellib.OpenBabelComputePC;
import org.mg.wekalib.data.ArffWritable;
import org.mg.wekalib.data.InstancesCreator;
import org.mg.wekalib.eval2.data.DataSet;
import org.mg.wekalib.eval2.data.WekaInstancesDataSet;
import org.mg.wekalib.eval2.data.WrappedDataSet;
import org.mg.wekalib.eval2.job.DefaultJobOwner;
import org.mg.wekalib.eval2.job.FeatureProvider;
import org.mg.wekalib.eval2.job.Printer;

import weka.core.Instances;

public class OBFeatureProvider extends DefaultJobOwner<DataSet[]> implements FeatureProvider
{
	private DataSet train;
	private DataSet test;

	public OBFeatureProvider()
	{
	}

	public String getName()
	{
		return "OB";
	}

	@Override
	public String getKeyPrefix()
	{
		String prefix = "";
		if (train != null)
			prefix += train.getKeyPrefix() + File.separator;
		prefix += "OB";
		return prefix;
	}

	@Override
	public String getKeyContent()
	{
		return getKeyContent(train, test);
	}

	@Override
	public Runnable nextJob() throws Exception
	{
		return blockedJob("OBFeatures: computing features for " + train.getName(), new Runnable()
		{
			public void run()
			{
				try
				{
					DataSet trainX = train;
					while (trainX instanceof WrappedDataSet)
						trainX = ((WrappedDataSet) trainX).getSelf();
					if (!(trainX instanceof CDKDataSet))
						throw new IllegalArgumentException(
								"dataset is no cdk-dataset: " + trainX.getClass());
					DataSet testX = test;
					while (testX instanceof WrappedDataSet)
						testX = ((WrappedDataSet) testX).getSelf();
					if (test != null && !(testX instanceof CDKDataSet))
						throw new IllegalArgumentException();
					CDKDataset trainCDK = ((CDKDataSet) trainX).getCDKDataset();
					CDKDataset testCDK = null;
					if (test != null)
						testCDK = ((CDKDataSet) testX).getCDKDataset();

					OpenBabelComputePC ob = new OpenBabelComputePC();
					LinkedHashMap<String, List<Double>> trainVals = new LinkedHashMap<>();
					LinkedHashMap<String, List<Double>> testVals = new LinkedHashMap<>();
					for (String desc : OpenBabelComputePC.DESCRIPTORS)
					{
						Printer.println(desc);
						trainVals.put(desc, ob.compute(desc, trainCDK.getSmiles()
								.toArray(new String[trainCDK.getSmiles().size()])));
						testVals.put(desc, ob.compute(desc, testCDK.getSmiles()
								.toArray(new String[testCDK.getSmiles().size()])));
					}

					Instances trainI = toInstances(trainCDK.getDatasetName(),
							trainCDK.getDatasetName(), trainCDK, trainVals, true);
					Instances testI = null;
					if (test != null)
						testI = toInstances(testCDK.getDatasetName(), trainCDK.getDatasetName(),
								testCDK, testVals, true);

					//					System.err.println(testI);
					setResult(new DataSet[] {
							new WekaInstancesDataSet(trainI, trainX.getPositiveClass()),
							test != null
									? new WekaInstancesDataSet(testI, trainX.getPositiveClass())
									: null });
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
			};
		});
	}

	public static Instances toInstances(String relationName, String endpointName, CDKDataset d,
			LinkedHashMap<String, List<Double>> featureVals, boolean addClass)
	{
		String[] domain;
		if (addClass)
			domain = DataLoader.getClassValues(d.getEndpoints());
		else
			domain = null;
		List<String> features = new ArrayList<>(featureVals.keySet());

		try
		{
			Instances inst = InstancesCreator.create(new ArffWritable()
			{
				@Override
				public boolean isSparse()
				{
					return false;
				}

				@Override
				public String getRelationName()
				{
					return relationName;
				}

				@Override
				public int getNumInstances()
				{
					return d.getSmiles().size();
				}

				@Override
				public int getNumAttributes()
				{
					return featureVals.size() + 1;
				}

				@Override
				public double getAttributeValueAsDouble(int instance, int attribute)
						throws Exception
				{
					if (attribute == featureVals.size())
					{
						if (addClass)
							return ArrayUtils.indexOf(domain, d.getEndpoints().get(instance));
						else
							return Double.NaN;
					}
					else
					{
						return featureVals.get(features.get(attribute)).get(instance);
					}
				}

				@Override
				public String getAttributeName(int attribute)
				{
					if (attribute == featureVals.size())
						return endpointName;
					else
						return features.get(attribute);
				}

				@Override
				public String[] getAttributeDomain(int attribute)
				{
					if (attribute == featureVals.size())
						return domain;
					else
						return null;
				}
			});
			inst.setClassIndex(inst.numAttributes() - 1);
			return inst;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isValid(DataSet dataSet)
	{
		return true;
	}

	@Override
	public void setTrainingDataset(DataSet train)
	{
		this.train = train;
	}

	@Override
	public void setTestDataset(DataSet test)
	{
		this.test = test;
	}

	@Override
	public DataSet getTrainingDataset()
	{
		return train;
	}

	@Override
	public DataSet getTestDataset()
	{
		return test;
	}

	@Override
	public FeatureProvider cloneJob()
	{
		FeatureProvider fp = new OBFeatureProvider();
		fp.setTestDataset(test);
		fp.setTrainingDataset(train);
		return fp;
	}
}
