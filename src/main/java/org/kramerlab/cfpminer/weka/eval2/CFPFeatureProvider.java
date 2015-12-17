package org.kramerlab.cfpminer.weka.eval2;

import org.kramerlab.cfpminer.CFPtoArff;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.CDKDataset;
import org.mg.javalib.util.HashUtil;
import org.mg.wekalib.eval2.Blocker;
import org.mg.wekalib.eval2.DataSet;
import org.mg.wekalib.eval2.DefaultJobOwner;
import org.mg.wekalib.eval2.FeatureProvider;
import org.mg.wekalib.eval2.FoldDataSet;
import org.mg.wekalib.eval2.WekaInstancesDataSet;

import weka.core.Instances;

public class CFPFeatureProvider extends DefaultJobOwner<DataSet[]> implements FeatureProvider
{
	int hashfoldSize;
	FeatureSelection featSelection;
	CFPType type;
	DataSet train;
	DataSet test;

	public CFPFeatureProvider(int hashfoldSize, FeatureSelection featSelection, CFPType type)
	{
		this.hashfoldSize = hashfoldSize;
		this.featSelection = featSelection;
		this.type = type;
	}

	public String getName()
	{
		return "CFP " + featSelection;
	}

	@Override
	public int hashCode()
	{
		//		System.out.println(hashfoldSize + " " + featSelection + " " + type + " " + train + "\n" + train.hashCode()
		//				+ "\n" + test.hashCode());
		return HashUtil.hashCode(hashfoldSize, featSelection, type, train, test);
	}

	@Override
	public Runnable nextJob() throws Exception
	{
		if (!Blocker.block(hashCode()))
			return null;
		return new Runnable()
		{
			public void run()
			{
				try
				{
					System.out.println(CFPFeatureProvider.this.hashCode() + " mining features on " + train.getName());

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
					cfp.setFeatureSelection(featSelection);
					cfp.setHashfoldsize(hashfoldSize);
					cfp.setType(type);
					cfp.mine(((CDKDataSet) trainX).getCDKDataset().getSmiles());
					if (featSelection == FeatureSelection.filt)
						cfp.applyFilter();
					//					System.err.println(cfp.getSummary(false));
					//					System.err.flush();
					Instances trainI = CFPtoArff.getTrainingDataset(cfp, trainCDK.getDatasetName());
					trainI.setRelationName("Mined features on " + train.getName());
					//					System.err.println(trainI);
					Instances testI = CFPtoArff.getTestDataset(cfp, trainCDK.getDatasetName(), testCDK.getSmiles(),
							testCDK.getEndpoints());
					testI.setRelationName("Mined features on " + test.getName());
					//					System.err.println(testI);
					setResult(new DataSet[] { new WekaInstancesDataSet(trainI), new WekaInstancesDataSet(testI) });
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
				finally
				{
					Blocker.unblock(CFPFeatureProvider.this.hashCode());
				}
			};
		};
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
	public FeatureProvider cloneFeatureProvider()
	{
		FeatureProvider fp = new CFPFeatureProvider(hashfoldSize, featSelection, type);
		fp.setTestDataset(test);
		fp.setTrainingDataset(train);
		return fp;
	}
}
