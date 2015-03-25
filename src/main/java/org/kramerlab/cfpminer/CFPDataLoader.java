package org.kramerlab.cfpminer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mg.javalib.util.FileUtil;
import org.mg.javalib.util.FileUtil.CSVFile;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

public class CFPDataLoader
{
	public String dataFolder = "data";
	private HashMap<String, Dataset> datasets = new HashMap<>();

	private static HashMap<String, String> sdfDatasets = new HashMap<>();
	private static HashMap<String, String> sdfEndpoints = new HashMap<>();
	static
	{
		for (String e : new String[] { "MultiCellCall", "SingleCellCall", "Rat", "Mouse", "Hamster", "Mutagenicity",
				"Dog_Primates" })
		{
			sdfDatasets.put("CPDBAS_" + e, "CPDBAS_v5d_1547_20Nov2008.sdf");
			sdfEndpoints.put("CPDBAS_" + e, "ActivityOutcome_CPDBAS_" + e);
		}

		sdfDatasets.put("NCTRER", "NCTRER_v4b_232_15Feb2008.sdf");
		sdfEndpoints.put("NCTRER", "ActivityOutcome_NCTRER");

		sdfDatasets.put("AMES", "cas_4337.ob.sdf");
		sdfEndpoints.put("AMES", "Ames test categorisation");
	}

	public HashMap<String, Dataset> allDatasets()
	{
		for (String endpoint : FileUtil.readStringFromFile("endpoints.txt").split("\n"))
			getDataset(endpoint);
		return datasets;
	}

	public CFPDataLoader(String dataFolder)
	{
		this.dataFolder = dataFolder;
	}

	public static class Dataset
	{
		List<String> smiles;
		List<String> endpoints;

		public Dataset(List<String> smiles, List<String> endpoints)
		{
			this.smiles = smiles;
			this.endpoints = endpoints;
		}

		public List<String> getEndpoints()
		{
			return endpoints;
		}

		public List<String> getSmiles()
		{
			return smiles;
		}
	}

	public boolean exists(String name)
	{
		if (datasets.containsKey(name))
			return true;
		if (sdfDatasets.containsKey(name))
			return new File(dataFolder + File.separator + sdfDatasets.get(name)).exists();
		return new File(dataFolder + File.separator + name + ".csv").exists();
	}

	public Dataset getDataset(String name)
	{
		if (!datasets.containsKey(name))
		{
			try
			{
				Dataset data;
				if (sdfDatasets.containsKey(name))
					data = getDatasetFromSDF(name);
				else
					data = getDatasetFromCSV(name);
				datasets.put(name, data);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		return datasets.get(name);
	}

	private Dataset getDatasetFromSDF(String name) throws Exception
	{
		List<String> endpoints = new ArrayList<>();
		List<String> smiles = new ArrayList<>();

		ISimpleChemObjectReader reader = new ReaderFactory().createReader(new InputStreamReader(new FileInputStream(
				dataFolder + File.separator + sdfDatasets.get(name))));
		IChemFile content = (IChemFile) reader.read((IChemObject) new ChemFile());
		String endpoint = sdfEndpoints.get(name);
		for (IAtomContainer a : ChemFileManipulator.getAllAtomContainers(content))
			if (a.getAtomCount() > 0 && a.getProperty(endpoint) != null
					&& !a.getProperty(endpoint).toString().equals("unspecified")
					&& !a.getProperty(endpoint).toString().equals("blank")
					&& !a.getProperty(endpoint).toString().equals("inconclusive"))
			{
				endpoints.add(a.getProperty(endpoint).toString());
				String smi = new SmilesGenerator().create(a);
				CDKUtil.setMolForSmiles(smi, a);
				smiles.add(smi);
			}
		reader.close();

		return new Dataset(smiles, endpoints);
	}

	private Dataset getDatasetFromCSV(String name)
	{
		List<String> endpoints = new ArrayList<>();
		List<String> smiles = new ArrayList<>();

		CSVFile csv = FileUtil.readCSV(dataFolder + File.separator + name + ".csv");
		for (int i = 1; i < csv.content.size(); i++)
		{
			smiles.add(csv.content.get(i)[0]);
			endpoints.add(csv.content.get(i)[1]);
		}

		return new Dataset(smiles, endpoints);
	}

}
