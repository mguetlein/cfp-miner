package org.kramerlab.cfpminer.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.mg.cdklib.CDKConverter;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.CDKDataset;
import org.mg.cdklib.data.DataProvider;
import org.mg.cdklib.data.DataProvider.DataID;
import org.mg.cdklib.depict.CDKDepict;
import org.mg.javalib.util.SwingUtil;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smarts.SmartsFragmentExtractor;

public class DrawCFPs
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
			List<String> endpoints = new ArrayList<>();
			for (String s : d.getEndpoints())
				endpoints.add("");
			d = new CDKDataset("mouse", d.getSmiles(), endpoints, null);

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

			Random r = new Random();
			while (true)
			{
				int compound = r.nextInt(d.getSmiles().size());
				int fragment = r.nextInt(miner.getNumFragments());

				if (miner.isFragmentIncludedInCompound(compound, miner.getFragmentViaIdx(fragment)))
				{
					IAtomContainer mol = CDKConverter.parseSmiles(d.getSmiles().get(compound));
					int atoms[] = miner.getAtoms(mol, miner.getFragmentViaIdx(fragment));

					SmartsFragmentExtractor ext = new SmartsFragmentExtractor(mol);
					ext.setMode(SmartsFragmentExtractor.MODE_EXACT);
					System.out.println(ext.generate(atoms));
					ext.setMode(SmartsFragmentExtractor.MODE_JCOMPOUNDMAPPER);
					System.out.println(ext.generate(atoms));

					BufferedImage img = CDKDepict.depictMatch(mol, atoms, true, Color.RED, false,
							500, true);

					int numMatches = miner
							.getAtomsMultipleDistinct(mol, miner.getFragmentViaIdx(fragment))
							.size();

					System.out.println("#" + numMatches);

					SwingUtil.showInFrame(new JLabel(new ImageIcon(img)), "#" + numMatches, true);
				}
			}

			//			CFPtoArff.writeTrainingDataset("/tmp/" + data + ".arff", miner,
			//					DataLoader.INSTANCE.getDatasetEndpoint(data));
		}
	}
}
