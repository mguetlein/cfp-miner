package org.kramerlab.cfpminer.weka.eval2;

import java.io.File;

import org.kramerlab.cfpminer.experiments.NumFragmentsAndCollisions;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.CDKDataset;
import org.mg.wekalib.eval2.data.DataSet;
import org.mg.wekalib.eval2.data.WekaInstancesDataSet;
import org.mg.wekalib.eval2.data.WrappedDataSet;
import org.mg.wekalib.eval2.job.DefaultJobOwner;
import org.mg.wekalib.eval2.job.FeatureProvider;
import org.mg.wekalib.eval2.job.Printer;

import weka.core.Instances;
import weka.core.SparseInstance;

public class CFPFeatureProvider extends DefaultJobOwner<DataSet[]> implements FeatureProvider
{
	private int hashfoldSize;
	private FeatureSelection featureSelection;
	private CFPType type;
	private DataSet train;
	private DataSet test;

	public CFPFeatureProvider(int hashfoldSize, FeatureSelection featSelection, CFPType type)
	{
		this.hashfoldSize = hashfoldSize;
		this.featureSelection = featSelection;
		this.type = type;
		if (featureSelection == FeatureSelection.none && hashfoldSize != 0)
			throw new IllegalArgumentException();
	}

	public String getName()
	{
		return "CFP " + hashfoldSize + " " + featureSelection + " " + type;
	}

	@Override
	public String getKeyPrefix()
	{
		String prefix = "";
		if (train != null)
			prefix += train.getKeyPrefix() + File.separator;
		prefix += "CFP-" + hashfoldSize + '-' + featureSelection + '-' + type;
		return prefix;
	}

	@Override
	public String getKeyContent()
	{
		return getKeyContent(hashfoldSize, featureSelection, type, train, test);
	}

	@Override
	public Runnable nextJob() throws Exception
	{
		return blockedJob("CFPFeatures: mining features on " + train.getName(), new Runnable()
		{
			public void run()
			{
				try
				{
					DataSet trainX = train;
					while (trainX instanceof WrappedDataSet)
						trainX = ((WrappedDataSet) trainX).getSelf();
					if (!(trainX instanceof CDKDataSet))
						throw new IllegalArgumentException();
					DataSet testX = test;
					while (testX instanceof WrappedDataSet)
						testX = ((WrappedDataSet) testX).getSelf();
					if (test != null && !(testX instanceof CDKDataSet))
						throw new IllegalArgumentException();
					CDKDataset trainCDK = ((CDKDataSet) trainX).getCDKDataset();
					CDKDataset testCDK = null;
					if (test != null)
						testCDK = ((CDKDataSet) testX).getCDKDataset();

					CFPMiner cfp = new CFPMiner(trainCDK.getEndpoints());
					cfp.setFeatureSelection(featureSelection);
					if (featureSelection != FeatureSelection.none)
						cfp.setHashfoldsize(hashfoldSize);
					cfp.setType(type);
					cfp.mine(((CDKDataSet) trainX).getCDKDataset().getSmiles());
					int num = cfp.getNumFragments();
					if (featureSelection == FeatureSelection.filt)
						cfp.applyFilter();
					Printer.println(cfp.getNumFragments() + "/" + num + " fragments");

					//					System.err.println(cfp.getSummary(false));
					//					System.err.flush();
					Instances trainI = CFPtoArff.getTrainingDataset(cfp, trainCDK.getDatasetName());
					trainI.setRelationName(getKeyPrefix());
					if (!(trainI.get(0) instanceof SparseInstance))
						throw new IllegalStateException();

					//					System.err.println(trainI);
					Instances testI = null;
					if (test != null)
					{
						testI = CFPtoArff.getTestDataset(cfp, trainCDK.getDatasetName(),
								testCDK.getSmiles(), testCDK.getEndpoints());
						testI.setRelationName(getKeyPrefix());
						if (!(testI.get(0) instanceof SparseInstance))
							throw new IllegalStateException();
					}

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

	static NumFragmentsAndCollisions frags = new NumFragmentsAndCollisions();

	@Override
	public boolean isValid(DataSet dataSet)
	{
		if (getHashfoldSize() == 4096 && dataSet.getName().equals("CPDBAS_Mutagenicity")
				&& getType() == CFPType.ecfp4)
		{
			// hack num is slightly > 4096, but not sufficient for internal validation
			return false;
		}
		int n = frags.getNumFeatures(dataSet.getName(), getType());
		return n > getHashfoldSize();
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

	public int getHashfoldSize()
	{
		return hashfoldSize;
	}

	public CFPType getType()
	{
		return type;
	}

	public FeatureSelection getFeatureSelection()
	{
		return featureSelection;
	}

	@Override
	public FeatureProvider cloneJob()
	{
		FeatureProvider fp = new CFPFeatureProvider(hashfoldSize, featureSelection, type);
		fp.setTestDataset(test);
		fp.setTrainingDataset(train);
		return fp;
	}
}
