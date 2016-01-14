package org.kramerlab.cfpminer.weka.eval2;

import java.io.File;

import org.kramerlab.cfpminer.CFPtoArff;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.CDKDataset;
import org.mg.wekalib.eval2.data.DataSet;
import org.mg.wekalib.eval2.data.FoldDataSet;
import org.mg.wekalib.eval2.data.WekaInstancesDataSet;
import org.mg.wekalib.eval2.job.DefaultJobOwner;
import org.mg.wekalib.eval2.job.FeatureProvider;

import weka.core.Instances;

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
	}

	public String getName()
	{
		return "CFP " + hashfoldSize + " " + featureSelection + " " + type;
	}

	@Override
	public String getKeyPrefix()
	{
		return "CFP-" + hashfoldSize + '-' + featureSelection + '-' + type
				+ (train != null ? (File.separator + train.getKeyPrefix()) : "");
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
					while (trainX instanceof FoldDataSet)
						trainX = ((FoldDataSet) trainX).getSelf();
					if (!(trainX instanceof CDKDataSet))
						throw new IllegalArgumentException();
					DataSet testX = test;
					while (testX instanceof FoldDataSet)
						testX = ((FoldDataSet) testX).getSelf();
					if (!(testX instanceof CDKDataSet))
						throw new IllegalArgumentException();
					CDKDataset trainCDK = ((CDKDataSet) trainX).getCDKDataset();
					CDKDataset testCDK = ((CDKDataSet) testX).getCDKDataset();

					CFPMiner cfp = new CFPMiner(trainCDK.getEndpoints());
					cfp.setFeatureSelection(featureSelection);
					cfp.setHashfoldsize(hashfoldSize);
					cfp.setType(type);
					cfp.mine(((CDKDataSet) trainX).getCDKDataset().getSmiles());
					if (featureSelection == FeatureSelection.filt)
						cfp.applyFilter();
					//					System.err.println(cfp.getSummary(false));
					//					System.err.flush();
					Instances trainI = CFPtoArff.getTrainingDataset(cfp, trainCDK.getDatasetName());
					trainI.setRelationName(getKeyPrefix());
					//					System.err.println(trainI);
					Instances testI = CFPtoArff.getTestDataset(cfp, trainCDK.getDatasetName(),
							testCDK.getSmiles(), testCDK.getEndpoints());
					testI.setRelationName(getKeyPrefix());
					//					System.err.println(testI);
					setResult(new DataSet[] {
							new WekaInstancesDataSet(trainI, trainX.getPositiveClass()),
							new WekaInstancesDataSet(testI, trainX.getPositiveClass()) });
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
			};
		});
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
