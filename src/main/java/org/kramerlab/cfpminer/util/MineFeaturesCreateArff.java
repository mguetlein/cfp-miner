package org.kramerlab.cfpminer.util;

import org.kramerlab.cfpminer.weka.eval2.CFPtoArff;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.CDKDataset;
import org.mg.cdklib.data.DataLoader;

public class MineFeaturesCreateArff
{
	public static void main(String[] args) throws Exception
	{
		for (String data : new String[] { //"CPDBAS_Mouse",
				//"CPDBAS_Mutagenicity", "NCTRER", 
				//"AMES", 
				"ChEMBL_87", "MUV_832", "DUD_hivrt",// "ChEMBL_259", "MUV_712", "MUV_644" 
		})
		{
			CDKDataset d = DataLoader.INSTANCE.getDataset(data);

			//			int idx = 0;
			//			for (String smi : d.getSmiles())
			//				System.out.println((idx++) + " " + smi);

			CFPMiner miner = new CFPMiner(d.getEndpoints());
			miner.setType(CFPType.ecfp4);
			miner.setHashfoldsize(1024);
			miner.setFeatureSelection(FeatureSelection.fold);
			miner.mine(d.getSmiles());
			System.out.println(miner);

			CFPtoArff.writeTrainingDataset("/tmp/" + data + ".arff", miner,
					DataLoader.INSTANCE.getDatasetEndpoint(data));
		}
	}

}
