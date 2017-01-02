package org.kramerlab.cfpminer.util;

import org.kramerlab.cfpminer.weka.eval2.CFPtoArff;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.CDKDataset;
import org.mg.cdklib.data.DataProvider;
import org.mg.cdklib.data.DataProvider.DataID;

import weka.core.Instances;

public class MineFeaturesCreateArff
{
	public static void main(String[] args) throws Exception
	{
		for (DataID data : new DataID[] { DataID.CPDBAS_Mouse,
				//"CPDBAS_Mutagenicity", "NCTRER", 
				//"AMES", 
				//"ChEMBL_87", "MUV_832", "DUD_hivrt",// "ChEMBL_259", "MUV_712", "MUV_644" 
		})
		{
			CDKDataset d = DataProvider.getDataset(data);
			//			List<String> endpoints = new ArrayList<>();
			//			for (String s : d.getEndpoints())
			//				endpoints.add("");
			//			d = new CDKDataset("mouse", d.getSmiles(), endpoints, null);

			//			int idx = 0;
			//			for (String smi : d.getSmiles())
			//				System.out.println((idx++) + " " + smi);

			CFPMiner miner = new CFPMiner(d.getEndpoints());
			miner.setType(CFPType.ecfp4);
			miner.setHashfoldsize(1024);
			miner.setFeatureSelection(FeatureSelection.filt);
			miner.mine(d.getSmiles());
			miner.applyFilter();
			System.out.println(miner);
			//			CFPtoArff.writeTrainingDataset("/tmp/" + data + ".arff", miner,
			//					DataLoader.INSTANCE.getDatasetEndpoint(data));

			Instances inst = CFPtoArff.getTrainingDataset(miner, data.toString());

			while (inst.size() > 100)
				inst.remove(inst.numInstances() - 1);

			//			InstanceUtil.stripAttributes(inst, Arrays.asList(inst.classAttribute()));
			//			String vals[] = new String[inst.numInstances()];
			//			Arrays.fill(vals, "same-class");
			//			InstanceUtil.attachNominalAttribute(inst, "class", Arrays.asList("same-class"),
			//					Arrays.asList(vals), true);
			//			inst.setClassIndex(inst.numAttributes() - 1);

			//			OneClassClassifier one = new OneClassClassifier();
			//			one.setTargetClassLabel("same-class");
			//
			//			one.setTargetRejectionRate(0.5);
			//			one.buildClassifier(inst);
			//
			//			int total = 0;
			//			int zero = 0;
			//			for (Instance i : inst)
			//			{
			//				total++;
			//				if (one.distributionForInstance(i)[0] == 0.0)
			//					zero++;
			//			}
			//			System.out.println(zero + "/" + total);
		}
	}
}
