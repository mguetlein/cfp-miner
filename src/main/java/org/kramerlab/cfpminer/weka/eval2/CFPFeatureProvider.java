package org.kramerlab.cfpminer.weka.eval2;

import org.kramerlab.cfpminer.CFPtoArff;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.CDKDataset;
import org.mg.wekalib.eval2.DataSet;
import org.mg.wekalib.eval2.DefaultJobOwner;
import org.mg.wekalib.eval2.FeatureProvider;
import org.mg.wekalib.eval2.FoldDataSet;
import org.mg.wekalib.eval2.WekaInstancesDataSet;
import org.mg.wekalib.eval2.util.Blocker;
import org.mg.wekalib.eval2.util.Printer;

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
	public String key()
	{
		StringBuffer b = new StringBuffer();
		b.append(hashfoldSize);
		b.append('#');
		b.append(featSelection);
		b.append('#');
		b.append(type);
		b.append('#');
		b.append(train == null ? null : train.key());
		b.append('#');
		b.append(test == null ? null : test.key());
		return b.toString();
	}

	@Override
	public Runnable nextJob() throws Exception
	{
		if (!Blocker.block(key()))
			return null;
		return new Runnable()
		{
			public void run()
			{
				try
				{
					Printer.println("CFPFeatures: mining features on " + train.getName() + " "
							+ CFPFeatureProvider.this.key());

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
					Blocker.unblock(CFPFeatureProvider.this.key());
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
