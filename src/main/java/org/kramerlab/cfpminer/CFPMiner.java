package org.kramerlab.cfpminer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.kramerlab.cfpminer.weka.AttributeCrossvalidator;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.CountedSet;
import org.mg.javalib.util.DoubleArraySummary;
import org.mg.javalib.util.FileUtil;
import org.mg.javalib.util.ListUtil;
import org.mg.javalib.util.SetUtil;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.BitSetFingerprint;
import org.openscience.cdk.fingerprint.CircularFingerprinter;
import org.openscience.cdk.fingerprint.IBitFingerprint;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

public class CFPMiner implements Serializable, AttributeCrossvalidator.AttributeProvider
{
	private static final long serialVersionUID = 2L;

	int numCompounds = 0;
	List<String> endpoints;
	LinkedHashMap<Integer, LinkedHashSet<Integer>> hashCodeToCompound = new LinkedHashMap<Integer, LinkedHashSet<Integer>>();
	int numUnfoldedConflicts = 0;

	public enum CFPType
	{
		ecfp, fcfp;

		int getClassType()
		{
			if (this == ecfp)
				return CircularFingerprinter.CLASS_ECFP6;
			else
				return CircularFingerprinter.CLASS_FCFP6;
		}
	}

	public enum FeatureSelection
	{
		filt, fold, none;
	}

	CFPType type;
	FeatureSelection featureSelection;
	int hashfoldsize;
	int absMinFreq = 2;

	transient LinkedHashMap<Integer, LinkedHashSet<Integer>> hashCodeToCompound_unfiltered;
	transient CircularFingerprinter fp;
	transient Integer[] hashcodeList;
	transient HashMap<Integer, LinkedHashSet<Integer>> compoundToHashCode;
	transient HashMap<Integer, Integer> hashCodeToIteration = new HashMap<Integer, Integer>();
	transient HashMap<Integer, Integer> hashCodeToNumAtoms = new HashMap<Integer, Integer>();
	transient HashMap<IAtomContainer, LinkedHashSet<Integer>> testMoleculeToHashCode;
	transient String[] classValues;
	transient Integer activeIdx;

	public CFPMiner(List<String> endpoints) throws CDKException
	{
		this.endpoints = endpoints;
	}

	public String[] getClassValues()
	{
		if (classValues == null)
		{
			classValues = ArrayUtil.removeDuplicates(ArrayUtil.toArray(endpoints));
			Arrays.sort(classValues);
		}
		return classValues;
	}

	public boolean isActive(int classIdx)
	{
		if (activeIdx == null)
		{
			for (int i = 0; i < getClassValues().length; i++)
				if (classValues[i].matches("(?i).*active.*"))
					activeIdx = i;
			if (activeIdx == null)
				throw new IllegalStateException();
		}
		return activeIdx.intValue() == classIdx;
	}

	public List<String> getEndpoints()
	{
		return endpoints;
	}

	public void setType(CFPType type)
	{
		this.type = type;
	}

	public void setFeatureSelection(FeatureSelection featureSelection)
	{
		this.featureSelection = featureSelection;
	}

	public void setHashfoldsize(int hashfoldsize)
	{
		this.hashfoldsize = hashfoldsize;
	}

	private CFPMiner()
	{
	}

	private void initCircularFingerprinter()
	{
		fp = new CircularFingerprinter(type.getClassType())
		{
			public IBitFingerprint getBitFingerprint(IAtomContainer mol) throws CDKException
			{
				calculate(mol);
				if (featureSelection != FeatureSelection.fold)
					return null;
				final BitSet bits = new BitSet(hashfoldsize);
				for (int n = 0; n < getFPCount(); n++)
				{
					int i = getFP(n).hashCode;
					long b = i >= 0 ? i : ((i & 0x7FFFFFFF) | (1L << 31));
					bits.set((int) (b % hashfoldsize));
				}
				return new BitSetFingerprint(bits);
			}
		};
	}

	public int[] getAtoms(String smiles, Integer fingerprint) throws Exception
	{
		return getAtoms(CDKUtil.parseSmiles(smiles), fingerprint);
	}

	public int[] getAtoms(IAtomContainer mol, Integer fingerprint) throws Exception
	{
		if (featureSelection == FeatureSelection.fold)
			throw new IllegalArgumentException();
		int atoms[] = null;
		initCircularFingerprinter();
		fp.getBitFingerprint(mol);
		for (int i = 0; i < fp.getFPCount(); i++)
		{
			if (fp.getFP(i).hashCode == fingerprint)
			{
				atoms = fp.getFP(i).atoms;
				break;
			}
		}
		return atoms;
	}

	public void update(String... smiles) throws Exception
	{
		for (String smi : smiles)
			update(CDKUtil.parseSmiles(smi));
	}

	private void update(IAtomContainer... mols) throws CDKException
	{
		initCircularFingerprinter();
		for (IAtomContainer mol : mols)
		{
			IBitFingerprint finger = fp.getBitFingerprint(mol);
			if (featureSelection == FeatureSelection.fold)
				for (int i : finger.getSetbits())
					insert(hashCodeToCompound, i, numCompounds);
			else
				for (int i = 0; i < fp.getFPCount(); i++)
				{
					//					if (fp.getFP(i).hashCode == 1039376976)
					//					{
					//						System.err.println(fp.getFP(i).atoms.length + " " + ArrayUtil.toString(fp.getFP(i).atoms));
					//						try
					//						{
					//							drawFP(null, mol, fp.getFP(i).atoms);
					//						}
					//						catch (Exception e)
					//						{
					//							e.printStackTrace();
					//						}
					//					}

					insert(hashCodeToCompound, fp.getFP(i).hashCode, numCompounds);
					boolean conflict = check(hashCodeToIteration, fp.getFP(i).hashCode, fp.getFP(i).iteration);
					conflict |= check(hashCodeToNumAtoms, fp.getFP(i).hashCode, fp.getFP(i).atoms.length);
					if (conflict)
					{
						//						System.err.println(fp.getFP(i).hashCode);
						//						if (numUnfoldedConflicts > 5)
						//							System.exit(1);
						numUnfoldedConflicts++;
					}
				}
			numCompounds++;
		}
	}

	private static void insert(HashMap<Integer, LinkedHashSet<Integer>> map, int key, int val)
	{
		if (!map.containsKey(key))
			map.put(key, new LinkedHashSet<Integer>());
		map.get(key).add(val);
	}

	private static boolean check(HashMap<Integer, Integer> map, int key, int val)
	{
		if (map.containsKey(key) && map.get(key) != val)
			return true; //System.err.println("conflict " + key + " val1: " + val + " val2:" + map.get(key));
		map.put(key, val);
		return false;
	}

	public LinkedHashSet<Integer> getCompoundsForHashcode(Integer hashcode)
	{
		return hashCodeToCompound.get(hashcode);
	}

	public LinkedHashSet<Integer> getHashcodesForCompound(Integer compound)
	{
		if (compoundToHashCode == null)
		{
			compoundToHashCode = new HashMap<Integer, LinkedHashSet<Integer>>();
			for (Integer h : hashCodeToCompound.keySet())
				for (Integer c : hashCodeToCompound.get(h))
					insert(compoundToHashCode, c, h);
		}
		if (compoundToHashCode.containsKey(compound))
			return compoundToHashCode.get(compound);
		else
			return new LinkedHashSet<Integer>();
	}

	public Integer getHashcodeViaIdx(int attribute)
	{
		if (hashcodeList == null)
		{
			hashcodeList = new Integer[hashCodeToCompound.size()];
			int idx = 0;
			for (Integer h : hashCodeToCompound.keySet())
				hashcodeList[idx++] = h;
		}
		return hashcodeList[attribute];
	}

	public LinkedHashSet<Integer> getHashcodesForTestCompound(String smiles) throws Exception
	{
		return getHashcodesForTestCompound(CDKUtil.parseSmiles(smiles));
	}

	public LinkedHashSet<Integer> getHashcodesForTestCompound(IAtomContainer testMol) throws CDKException
	{
		if (testMoleculeToHashCode == null)
			testMoleculeToHashCode = new HashMap<IAtomContainer, LinkedHashSet<Integer>>();
		if (!testMoleculeToHashCode.containsKey(testMol))
		{
			if (fp == null)
				initCircularFingerprinter();
			LinkedHashSet<Integer> hashCodes = new LinkedHashSet<Integer>();
			IBitFingerprint finger = fp.getBitFingerprint(testMol);
			if (featureSelection == FeatureSelection.fold)
				for (int i : finger.getSetbits())
					hashCodes.add(i);
			else
				for (int i = 0; i < fp.getFPCount(); i++)
					hashCodes.add(fp.getFP(i).hashCode);
			testMoleculeToHashCode.put(testMol, hashCodes);
		}
		return testMoleculeToHashCode.get(testMol);
	}

	@SuppressWarnings("unchecked")
	public CFPMiner clone()
	{
		CFPMiner f = new CFPMiner();
		f.type = type;
		f.hashfoldsize = hashfoldsize;
		f.featureSelection = featureSelection;
		f.absMinFreq = absMinFreq;
		f.numCompounds = numCompounds;
		for (Integer integer : hashCodeToCompound.keySet())
			f.hashCodeToCompound.put(integer, (LinkedHashSet<Integer>) hashCodeToCompound.get(integer).clone());
		return f;
	}

	public int getNumCompounds()
	{
		return numCompounds;
	}

	private void applyMinFreq(Set<Integer> compoundSubset, int minFreq)
	{
		// remove with min-freq 0, would not have been mined on subset
		List<Integer> hashCodeToDelete = new ArrayList<Integer>();
		for (Integer h : hashCodeToCompound.keySet())
			if (SetUtil.intersectSize(hashCodeToCompound.get(h), compoundSubset) == 0)
				hashCodeToDelete.add(h);
		removeHashCodes(hashCodeToDelete);

		// remove only if larger than hashfoldize
		if (hashCodeToCompound.size() <= hashfoldsize)
			return;

		// collect tuples of hash code and min-freq
		List<int[]> hf = new ArrayList<int[]>();
		for (Integer h : hashCodeToCompound.keySet())
			hf.add(new int[] { h, SetUtil.intersectSize(hashCodeToCompound.get(h), compoundSubset) });

		// sort according to min freq, in decreasing order
		Collections.sort(hf, new Comparator<int[]>()
		{
			@Override
			public int compare(int[] o1, int[] o2)
			{
				// swap params to sort in descending order
				return Integer.valueOf(o2[1]).compareTo(Integer.valueOf(o1[1]));
			}
		});

		// apply min-freq while size > hashfoldsize
		for (int i = hashfoldsize; i < hf.size(); i++)
		{
			if (hf.get(i)[1] < absMinFreq)
				hashCodeToCompound.remove((int) hf.get(i)[0]);
		}
		compoundToHashCode = null;

		//		List<Integer> hashCodeToDelete = new ArrayList<Integer>();
		//		for (Integer h : hashCodeToCompound.keySet())
		//			if (hashCodeToCompound.get(h).size() < minFreq)
		//				hashCodeToDelete.add(h);
		//		removeHashCodes(hashCodeToDelete);

		//		System.err.println("after min freq:");
		//		System.err.println(this);

	}

	public void applyAnnovaFilter(List<Double> endpoints, double d)
	{
		if (endpoints.size() != numCompounds)
			throw new IllegalArgumentException();
		double all[] = ArrayUtil.toPrimitiveDoubleArray(ListUtil.toArray(endpoints));

		List<Integer> hashCodeToDelete = new ArrayList<Integer>();
		for (Integer h : hashCodeToCompound.keySet())
		{
			double sel[] = new double[hashCodeToCompound.get(h).size()];
			int idx = 0;
			for (Integer c : hashCodeToCompound.get(h))
				sel[idx++] = all[c];

			List<double[]> l = new ArrayList<double[]>();
			l.add(sel);
			l.add(all);

			double p = TestUtils.oneWayAnovaPValue(l);
			//System.err.println("p value of hash code " + h + " is " + p);
			if (p > d)
				hashCodeToDelete.add(h);
		}
		removeHashCodes(hashCodeToDelete);
	}

	private static <T> long[] nominalCounts(List<T> domain, List<T> values)
	{
		CountedSet<T> countedValues = CountedSet.create(values);
		long[] counts = new long[domain.size()];
		int i = 0;
		for (T v : domain)
			counts[i++] = countedValues.getCount(v);
		return counts;
	}

	public void applyChiSquareFilter(Set<Integer> compoundSubset)
	{
		// chi square is apply to a counts array for each class
		// e.g. it compares 45 x active 41 x inactive in the compoundSubset
		// to feature-x with 31 x active, 5 x inactive for all compounds with feature-value(feature-x, compound-y)= active
		if (endpoints.size() != numCompounds)
			throw new IllegalArgumentException();
		if (hashCodeToCompound.size() <= hashfoldsize)
			return;
		List<String> domain = new ArrayList<String>(new HashSet<String>(endpoints));
		List<String> subsetEndpoints = new ArrayList<String>();
		for (Integer c : compoundSubset)
			subsetEndpoints.add(endpoints.get(c));
		long[] all = nominalCounts(domain, subsetEndpoints);
		//		System.out.println(domain);
		//		System.out.println(ArrayUtil.toString(all));

		// create tuples of hash code and p-value
		List<double[]> hp = new ArrayList<double[]>();
		for (Integer h : hashCodeToCompound.keySet())
		{
			List<String> values = new ArrayList<String>();
			for (Integer c : hashCodeToCompound.get(h))
				if (compoundSubset.contains(c))
					values.add(endpoints.get(c));
			//			System.out.println(values);
			//			System.out.println(ArrayUtil.toString(nominalCounts(domain, values)));

			double p = Double.MAX_VALUE;
			if (values.size() > 0)
			{
				long sel[] = nominalCounts(domain, values);
				//System.err.println(ArrayUtil.toString(all));
				//				System.err.println(values.size());
				//System.err.println(ArrayUtil.toString(sel));
				p = TestUtils.chiSquareTestDataSetsComparison(sel, all);
				//				System.err.println("p value of hash code " + h + " is " + p);
				//				if (p > pValueThreshold)
				//					hashCodeToDelete.add(h);
			}
			hp.add(new double[] { h, p });
		}

		//sort tuples according to p-values
		Collections.sort(hp, new Comparator<double[]>()
		{
			@Override
			public int compare(double[] o1, double[] o2)
			{
				return Double.valueOf(o1[1]).compareTo(Double.valueOf(o2[1]));
			}
		});

		//sort compounds higher than hash
		for (int i = hashfoldsize; i < hp.size(); i++)
		{
			//			System.err.println("remove hash-code with p-value " + hp.get(i)[1]);
			hashCodeToCompound.remove((int) hp.get(i)[0]);
		}
		if (hashCodeToCompound.size() != hashfoldsize)
			throw new IllegalStateException();
		compoundToHashCode = null;
	}

	private void removeHashCodes(List<Integer> hashCodeToDelete)
	{
		for (Integer h : hashCodeToDelete)
			hashCodeToCompound.remove(h);
		compoundToHashCode = null;
	}

	public ResultSet getSummary(boolean nice)
	{
		ResultSet set = new ResultSet();
		int idx = set.addResult();
		//set.setResultValue(idx, "name", getName());
		set.setResultValue(idx, "Num fragments", hashCodeToCompound.size());
		if (!nice)
			set.setResultValue(idx, "Num compounds", numCompounds);
		set.setResultValue(idx, "Fragment type", type);
		set.setResultValue(idx, "Feature selection", featureSelection);
		//		if (featureSelection == FeatureSelection.filter)
		//		{
		//			set.setResultValue(idx, "Min frequency (relative/absolute)",
		//					relMinFreq + " / " + (int) Math.round(relMinFreq * numCompounds));
		//			set.setResultValue(idx, Character.toString((char) 967) + Character.toString((char) 178) + " max p-value",
		//					pValueThreshold);
		//		}
		if (!nice)
			set.setResultValue(idx, "Fingerprint size", hashfoldsize);
		if (!nice)
		{
			set.setResultValue(idx, "num unfolded conflicts", numUnfoldedConflicts);
			int n = 0;
			for (int c = 0; c < numCompounds; c++)
				if (getHashcodesForCompound(c).isEmpty())
					n++;
			set.setResultValue(idx, "compounds w/o hash code", n);
			List<Integer> cmps = new ArrayList<>();
			List<String> cmpsStr = new ArrayList<>();
			for (Integer hashCode : hashCodeToCompound.keySet())
			{
				cmps.add(hashCodeToCompound.get(hashCode).size());
				cmpsStr.add(hashCodeToCompound.get(hashCode).size() + "");
			}
			set.setResultValue(idx, "mean compounds per hash-code", DoubleArraySummary.create(cmps));
			CountedSet<String> setStr = CountedSet.create(cmpsStr);
			for (Integer f : new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 })
				set.setResultValue(idx, "hash-code with freq " + f + ": ", setStr.getCount(f + ""));

			List<Integer> hashCodes = new ArrayList<>();
			for (int c = 0; c < numCompounds; c++)
				hashCodes.add(getHashcodesForCompound(c).size());
			set.setResultValue(idx, "mean hash-codes per compound", DoubleArraySummary.create(hashCodes));
		}
		return set;
	}

	public String toString()
	{
		return getSummary(false).translate().toNiceString();
	}

	public String pairWiseInfo(List<IAtomContainer> mols) throws Exception
	{
		int numPairs = 0;
		if (hashcodeList == null)
			getHashcodeViaIdx(0);
		int minFrequency = (int) (numCompounds * 0.01);
		for (int i = 0; i < hashcodeList.length - 1; i++)
		{
			if (i % 100 == 0)
				System.out.println(i);

			Integer h1 = hashcodeList[i];
			if (hashCodeToCompound.get(h1).size() < minFrequency * 2)
				continue;
			for (int j = i + 1; j < hashcodeList.length; j++)
			{
				Integer h2 = hashcodeList[j];
				if (hashCodeToCompound.get(h2).size() < minFrequency * 2)
					continue;
				HashSet<Integer> intersect = new HashSet<Integer>(hashCodeToCompound.get(h1));
				intersect.retainAll(hashCodeToCompound.get(h2));
				if (intersect.size() < minFrequency)
					continue;
				Integer numAdj = numAdjacent(h1, h2, intersect, mols);
				if (numAdj == null) // overlap
					continue;
				if (numAdj < minFrequency)
					continue;
				if ((intersect.size() - numAdj) < minFrequency)
					continue;
				System.out.println(numAdj + "/" + intersect.size());
				numPairs++;
			}
		}
		return numPairs + "";
	}

	private Integer numAdjacent(Integer h1, Integer h2, HashSet<Integer> intersectCompounds, List<IAtomContainer> mols)
			throws Exception
	{
		int adj = 0;
		for (Integer c : intersectCompounds)
		{
			IAtomContainer mol = mols.get(c);
			int atoms1[] = getAtoms(mol, h1);
			int atoms2[] = getAtoms(mol, h2);
			boolean adjacent = false;
			for (int i = 0; i < atoms1.length; i++)
			{
				List<IAtom> connected = null;
				if (!adjacent)
					connected = mol.getConnectedAtomsList(mol.getAtom(atoms1[i]));
				for (int j = 0; j < atoms2.length; j++)
				{
					if (atoms1[i] == atoms2[j])
						return null;
					if (!adjacent && connected.contains(mol.getAtom(atoms2[j])))
						adjacent = true;
				}
			}
			if (adjacent)
				adj++;
		}
		return adj;
	}

	public void toCSVFile(String path, List<String> smiles, List<?> endpoints)
	{
		if (smiles.size() != numCompounds)
			throw new IllegalArgumentException();

		StringBuffer s = new StringBuffer();
		s.append("SMILES,endpoint");
		for (Integer h : hashCodeToCompound.keySet())
			s.append("," + h);
		s.append("\n");
		for (int c = 0; c < numCompounds; c++)
		{
			s.append(smiles.get(c) + "," + endpoints.get(c));
			for (Integer h : hashCodeToCompound.keySet())
				s.append("," + (getHashcodesForCompound(c).contains(h) ? "1" : "0"));
			s.append("\n");
		}
		FileUtil.writeStringToFile(path, s.toString());
	}

	public static void validate(String datasetName, int run, String outfile, String classifiers[], List<?> endpoints,
			CFPMiner... ecfps) throws Exception
	{
		AttributeCrossvalidator cv = new AttributeCrossvalidator(datasetName, ListUtil.cast(String.class, endpoints),
				ecfps);
		cv.setOutfile(outfile);
		cv.setRun(run);
		cv.setClassifiers(classifiers);
		cv.run();
	}

	public void applyFilter()
	{
		HashSet<Integer> allCompounds = new HashSet<Integer>();
		for (int i = 0; i < numCompounds; i++)
			allCompounds.add(i);
		applyFilter(allCompounds);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void applyFilter(Set<Integer> filterSubset)
	{
		if (featureSelection != FeatureSelection.filt)
			return;

		// undo old filter
		if (hashCodeToCompound_unfiltered != null)
			hashCodeToCompound = (LinkedHashMap<Integer, LinkedHashSet<Integer>>) hashCodeToCompound_unfiltered.clone();
		else
			hashCodeToCompound_unfiltered = (LinkedHashMap<Integer, LinkedHashSet<Integer>>) hashCodeToCompound.clone();

		// apply new filter
		applyMinFreq(filterSubset, absMinFreq);
		applyChiSquareFilter(filterSubset);

		//		System.out.println("filtered to: " + this);
	}

	@Override
	public String getName()
	{
		String suffix = "";
		if (featureSelection == FeatureSelection.fold)
			suffix = "_" + hashfoldsize;
		else if (featureSelection == FeatureSelection.filt)
			suffix = "_" + hashfoldsize;
		return type + "_" + featureSelection + suffix;
	}

	@Override
	public int getNumAttributes()
	{
		return hashCodeToCompound.size();
	}

	@Override
	public String getAttributeName(int a)
	{
		return getHashcodeViaIdx(a) + "";
	}

	@Override
	public List<String> getAttributeDomain(int a)
	{
		return ArrayUtil.toList(new String[] { "0", "1" });
	}

	@Override
	public String getAttributeValue(int i, int a)
	{
		if (hashCodeToCompound.get(getHashcodeViaIdx(a)).contains(Integer.valueOf(i)))
			return "1";
		else
			return "0";
	}

	private static List<String> files = new ArrayList<String>();
	private static List<String> endpointNames = new ArrayList<String>();
	private static List<String> names = new ArrayList<String>();

	static
	{
		try
		{
			for (File s : new File("data").listFiles())
			{
				if (s.getAbsolutePath().endsWith(".csv"))
				{
					files.add(s.getAbsolutePath());
					endpointNames.add("endpoint");
					names.add(FileUtil.getFilename(s.getName(), false));
				}
			}
			if (new File("data/CPDBAS_v5d_1547_20Nov2008.sdf").exists())
			{
				for (String e : new String[] { "MultiCellCall", "SingleCellCall", "Rat", "Mouse", "Hamster",
						"Mutagenicity", "Dog_Primates" })
				{
					files.add(new File("data/CPDBAS_v5d_1547_20Nov2008.sdf").getAbsolutePath());
					endpointNames.add("ActivityOutcome_CPDBAS_" + e);
					names.add("CPDBAS_" + e);
				}
			}
		}
		catch (Exception e)
		{

		}
	}

	private static int getDatasetIdx(String datasetName)
	{
		for (int i = 0; i < names.size(); i++)
			if (names.get(i).equals(datasetName))
				return i;
		throw new IllegalArgumentException(datasetName + " not found in " + ListUtil.toString(names));
	}

	private static List<IAtomContainer> getDatasetMols(String datasetName) throws Exception
	{
		String file = files.get(getDatasetIdx(datasetName));
		String endpoint = endpointNames.get(getDatasetIdx(datasetName));
		System.out.println(file + " " + endpoint);
		List<IAtomContainer> list;
		if (file.endsWith("csv"))
			list = CDKUtil.readFromCSV(new File(file));
		else
		{
			ISimpleChemObjectReader reader = new ReaderFactory().createReader(new InputStreamReader(
					new FileInputStream(file)));
			IChemFile content = (IChemFile) reader.read((IChemObject) new ChemFile());
			list = new ArrayList<IAtomContainer>();
			for (IAtomContainer a : ChemFileManipulator.getAllAtomContainers(content))
				if (a.getProperty(endpoint) != null && !a.getProperty(endpoint).toString().equals("unspecified")
						&& !a.getProperty(endpoint).toString().equals("blank"))
					list.add(a);
			reader.close();
		}
		System.out.println("total num compounds " + list.size());
		//				for (IAtomContainer mol : list)
		//				{
		//					System.out.println(mol);
		//				}
		return list;
	}

	private static List<String> getEndpointValues(String datasetName, List<IAtomContainer> list) throws Exception
	{
		//		boolean classification = true;
		String endpoint = endpointNames.get(getDatasetIdx(datasetName));
		List<String> endpointValues = new ArrayList<String>();
		for (IAtomContainer mol : list)
			//		{
			//			if (classification)
			endpointValues.add((String) mol.getProperty(endpoint));
		//			else
		//				endpointValues.add(Double.parseDouble((String) mol.getProperty(endpoint)));
		//		}
		System.out.println(CountedSet.create(endpointValues));
		return endpointValues;
	}

	//	private static String[] getString(CommandLine cmd, String opt, String defaultValues[])
	//	{
	//		if (cmd.hasOption(opt))
	//			return cmd.getOptionValue(opt).split(",");
	//		return defaultValues;
	//	}

	//	private static Double[] getDouble(CommandLine cmd, String opt, Double defaultValues[])
	//	{
	//		if (cmd.hasOption(opt))
	//			return ArrayUtil.parseDoubleArray(cmd.getOptionValue(opt).split(","));
	//		return defaultValues;
	//	}

	//	private static boolean[] getBooleans(CommandLine cmd, String opt, boolean defaultValues[])
	//	{
	//		if (cmd.hasOption(opt))
	//			return ArrayUtil.parseBoolean(cmd.getOptionValue(opt).split(","));
	//		return defaultValues;
	//	}

	//	private static Integer[] getIntegers(CommandLine cmd, String opt, Integer defaultValues[])
	//	{
	//		if (cmd.hasOption(opt))
	//			return ArrayUtil.parseIntegers(cmd.getOptionValue(opt).split(","));
	//		return defaultValues;
	//	}

	public static void main(String[] args) throws Exception
	{
		Locale.setDefault(Locale.US);
		//		//String smiles = "CCCCCCCCCCCCCCCCCCCCCCOC(=O)C1(CCC=CC1N(C)C)C2=CC=CC=C2";
		//		String smiles = "Cn1cnc2c1c(=O)n(c(=O)n2C)C";
		//		drawFP(null, new SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles(smiles), new int[] { 0, 1, 2,
		//				3, 4, 5, 6, 7 });

		//		System.exit(1);

		Options options = new Options();
		options.addOption("d", "datasetName", true, "");
		options.addOption("r", "run", true, "cv run");
		options.addOption("t", "type", true, "ecfp|fcfp");

		options.addOption("c", "classifier", true, "RaF|SMO");
		options.addOption("f", "featureSelection", true, "fold|filt|none");
		options.addOption("s", "hashfoldsize", true, "default:1024, requires folded=true");

		if (args.length == 0)
		{
			System.err.println(options);
			System.exit(1);
		}
		if (args.length > 0 && args[0].equals("debug"))
		{
			//			for (String name : names)
			//			{
			//				main(("--datasetName " + name + " --classifier RandomForest --featureSelection filter").split(" "));
			//			}

			args = "--datasetName CPDBAS_Dog_Primates --run 1 --classifier RaF --type ecfp --featureSelection filt --hashfoldsize 1024"
					.split(" ");

			//			args = "--datasetName DUD_vegfr2 --classifier RandomForest --folded false --pValueThreshold 0.05,0.1"
			//					.split(" ");
			//			args = "--datasetName ChEMBL_10188 --classifier RandomForest --featureSelection filter".split(" ");
			//			args = ("--datasetName CPDBAS_Dog_Primates --runs 3 --classifier RandomForest,SMO "
			//					+ "--type ecfp,fcfp --folded true,false --hashfoldsize 1024,4096 "
			//					+ "--pValueThreshold 0.05,0.1 --relMinFreq 0.02,0.01").split(" ");
		}

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);
		String datasetName = cmd.getOptionValue("d");
		int run = 1;
		if (cmd.hasOption("r"))
			run = Integer.parseInt(cmd.getOptionValue("r"));
		CFPType type = CFPType.ecfp;
		if (cmd.hasOption("t"))
			type = CFPType.valueOf(cmd.getOptionValue("t"));
		FeatureSelection featureSelection = FeatureSelection.filt;
		if (cmd.hasOption("f"))
			featureSelection = FeatureSelection.valueOf(cmd.getOptionValue("f"));
		int hashfoldsize = 1024;
		if (cmd.hasOption("s"))
			hashfoldsize = Integer.parseInt(cmd.getOptionValue("s"));
		String classifier = "RaF";
		if (cmd.hasOption("c"))
			classifier = cmd.getOptionValue("c");

		System.out.println(datasetName);

		List<IAtomContainer> list = getDatasetMols(datasetName);
		ListUtil.scramble(list, new Random(1));
		List<String> endpointValues = getEndpointValues(datasetName, list);
		System.out.println();

		//		SmilesGenerator sg = new SmilesGenerator();
		//		List<String> smiles = new ArrayList<String>();
		//		for (IAtomContainer mol : list)
		//			smiles.add(sg.create(mol));
		//		System.out.println(ListUtil.toString(endpoint));

		CFPMiner cfps = new CFPMiner(ListUtil.cast(String.class, endpointValues));
		cfps.type = type;
		cfps.featureSelection = featureSelection;
		cfps.hashfoldsize = hashfoldsize;
		cfps.update(ArrayUtil.toArray(list));
		System.out.println(cfps);
		//			cfps.applyFilter();
		//			System.out.println(cfps);
		//System.out.println(cfps.pairWiseInfo(list));

		String outfile = "results/" + "r" + run + "_" + type + "_" + featureSelection + "_" + hashfoldsize + "_"
				+ classifier + "_" + datasetName + ".arff";
		validate(datasetName, run, outfile, new String[] { classifier }, endpointValues, new CFPMiner[] { cfps });
	}
}
