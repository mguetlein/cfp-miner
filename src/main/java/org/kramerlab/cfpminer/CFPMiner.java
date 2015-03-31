package org.kramerlab.cfpminer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
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
import org.mg.javalib.util.HashUtil;
import org.mg.javalib.util.ListUtil;
import org.mg.javalib.util.SetUtil;
import org.mg.javalib.util.StringUtil;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.BitSetFingerprint;
import org.openscience.cdk.fingerprint.CircularFingerprinter;
import org.openscience.cdk.fingerprint.IBitFingerprint;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

public class CFPMiner implements Serializable, AttributeCrossvalidator.AttributeProvider
{
	private static final long serialVersionUID = 2L;

	int numCompounds = 0;
	List<String> endpoints;
	List<String> trainingDataSmiles;
	LinkedHashMap<Integer, LinkedHashSet<Integer>> hashCodeToCompound = new LinkedHashMap<Integer, LinkedHashSet<Integer>>();
	int numUnfoldedConflicts = 0;

	public enum CFPType
	{
		ecfp6, ecfp4, ecfp2, ecfp0, fcfp6, fcfp4, fcfp2, fcfp0;

		int getClassType()
		{
			if (this == ecfp6)
				return CircularFingerprinter.CLASS_ECFP6;
			else if (this == fcfp6)
				return CircularFingerprinter.CLASS_FCFP6;
			else if (this == ecfp4)
				return CircularFingerprinter.CLASS_ECFP4;
			else if (this == fcfp4)
				return CircularFingerprinter.CLASS_FCFP4;
			else if (this == ecfp2)
				return CircularFingerprinter.CLASS_ECFP2;
			else if (this == fcfp2)
				return CircularFingerprinter.CLASS_FCFP2;
			else if (this == ecfp0)
				return CircularFingerprinter.CLASS_ECFP0;
			else if (this == fcfp0)
				return CircularFingerprinter.CLASS_FCFP0;
			else
				throw new IllegalStateException("wtf");
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

	boolean addPairs = false;
	transient List<Pair> pairs;

	transient LinkedHashMap<Integer, LinkedHashSet<Integer>> hashCodeToCompound_unfiltered;
	transient CircularFingerprinter fp;
	transient Integer[] hashcodeList;
	transient HashMap<Integer, LinkedHashSet<Integer>> compoundToHashCode;
	transient HashMap<Integer, Integer> hashCodeToIteration = new HashMap<Integer, Integer>();
	transient HashMap<Integer, Integer> hashCodeToNumAtoms = new HashMap<Integer, Integer>();
	transient HashMap<IAtomContainer, LinkedHashSet<Integer>> testMoleculeToHashCode;
	transient String[] classValues;
	transient Integer activeIdx;
	transient private HashMap<Integer, Set<Integer>> collisionMap;

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

	public int getActiveIdx()
	{
		if (activeIdx == null)
		{
			for (int i = 0; i < getClassValues().length; i++)
				if (classValues[i].equals("active") || classValues[i].equals("mutagen"))
					activeIdx = i;
			if (activeIdx == null)
				throw new IllegalStateException("what is active? " + ArrayUtil.toString(getClassValues()));
		}
		return activeIdx;
	}

	public List<String> getEndpoints()
	{
		return endpoints;
	}

	public List<String> getTrainingDataSmiles()
	{
		return trainingDataSmiles;
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
		collisionMap = new HashMap<>();
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
					int bit = (int) (b % hashfoldsize);
					if (!collisionMap.containsKey(bit))
						collisionMap.put(bit, new HashSet<Integer>());
					collisionMap.get(bit).add(i);
					bits.set(bit);
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

	private static transient HashMap<Integer, Set<Integer>> atomsMultCache = new HashMap<>();

	/**
	 * fragment may occur multiple times
	 * 
	 * @param mol
	 * @param fingerprint
	 * @return
	 * @throws Exception
	 */
	public Set<Integer> getAtomsMultiple(IAtomContainer mol, Integer fingerprint) throws Exception
	{
		if (featureSelection == FeatureSelection.fold)
			throw new IllegalArgumentException();

		Integer key = HashUtil.hashCode(type, mol, fingerprint);
		if (!atomsMultCache.containsKey(key))
		{
			Set<Integer> atoms = new HashSet<>();
			initCircularFingerprinter();
			fp.getBitFingerprint(mol);
			for (int i = 0; i < fp.getFPCount(); i++)
				if (fp.getFP(i).hashCode == fingerprint)
					for (int a : fp.getFP(i).atoms)
						atoms.add(a);
			atomsMultCache.put(key, atoms);
		}
		return atomsMultCache.get(key);
	}

	public void mine(List<String> smiles) throws Exception
	{
		this.trainingDataSmiles = smiles;

		initCircularFingerprinter();
		for (String smi : smiles)
		{
			IBitFingerprint finger = fp.getBitFingerprint(CDKUtil.parseSmiles(smi));
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

		Set<Integer> toRemove = new HashSet<>();
		// apply min-freq while size > hashfoldsize
		for (int i = hashfoldsize; i < hf.size(); i++)
		{
			if (hf.get(i)[1] < absMinFreq)
				toRemove.add((int) hf.get(i)[0]);
		}
		removeHashCodes(toRemove);

		//		List<Integer> hashCodeToDelete = new ArrayList<Integer>();
		//		for (Integer h : hashCodeToCompound.keySet())
		//			if (hashCodeToCompound.get(h).size() < minFreq)
		//				hashCodeToDelete.add(h);
		//		removeHashCodes(hashCodeToDelete);

		//		System.err.println("after min freq:");
		//		System.err.println(this);
		System.err.println("applied min freq filter: " + hashCodeToCompound.size());

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

	public void applyClosedSetFilter(Set<Integer> compoundSubset)
	{
		if (hashCodeToCompound.size() <= hashfoldsize)
			return;
		int maxNumRemove = hashCodeToCompound.size() - hashfoldsize;

		LinkedHashMap<Integer, Set<Integer>> hashCodeToCompoundSubset = new LinkedHashMap<>();
		for (Integer h : hashCodeToCompound.keySet())
		{
			@SuppressWarnings("unchecked")
			Set<Integer> hCompounds = (Set<Integer>) hashCodeToCompound.get(h).clone();
			hCompounds.retainAll(compoundSubset);
			hashCodeToCompoundSubset.put(h, hCompounds);
		}

		if (hashcodeList == null)
			getHashcodeViaIdx(0);
		Set<Integer> hashCodesToRemove = new HashSet<>();
		for (int i = 0; i < hashcodeList.length - 1; i++)
		{
			Integer h1 = hashcodeList[i];
			if (hashCodesToRemove.contains(h1))
				continue;

			for (int j = i + 1; j < hashcodeList.length; j++)
			{
				Integer h2 = hashcodeList[j];
				if (hashCodesToRemove.contains(h2))
					continue;

				if (hashCodeToCompoundSubset.get(h1).equals(hashCodeToCompoundSubset.get(h2)))
				{
					Integer obsolete = getNonClosed(h1, h2, hashCodeToCompoundSubset.get(h1));
					if (obsolete != null)
					{
						hashCodesToRemove.add(obsolete);
						if (hashCodesToRemove.size() >= maxNumRemove || obsolete == h1)
							break;
					}
				}
			}
			if (hashCodesToRemove.size() >= maxNumRemove)
				break;
		}

		removeHashCodes(hashCodesToRemove);

		System.err.println("applied closed fragment filter: " + hashCodeToCompound.size());
	}

	public Integer getNonClosed(Integer h1, Integer h2, Set<Integer> compounds)
	{

		//		System.out.println("check if " + h1 + " is superset of " + h2 + ", compounds: " + compounds);
		try
		{
			boolean h1SupersetCandiate = true;
			boolean h2SupersetCandiate = true;
			for (Integer c : compounds)
			{
				IAtomContainer mol = CDKUtil.parseSmiles(trainingDataSmiles.get(c));
				Set<Integer> atoms1 = getAtomsMultiple(mol, h1);
				Set<Integer> atoms2 = getAtomsMultiple(mol, h2);
				//				System.out.println("mol " + c + " atoms h1: " + atoms1 + " atoms h2: " + atoms2);
				if (h1SupersetCandiate)
					if (!SetUtil.isSubSet(atoms1, atoms2))
						h1SupersetCandiate = false;
				if (h2SupersetCandiate)
					if (!SetUtil.isSubSet(atoms2, atoms1))
						h2SupersetCandiate = false;
				if (!h1SupersetCandiate && !h2SupersetCandiate)
					return null;
			}
			if (h1SupersetCandiate)
				return h2;
			else if (h2SupersetCandiate)
				return h1;
			else
				throw new IllegalStateException();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
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
		Set<Integer> toRemove = new HashSet<>();
		for (int i = hashfoldsize; i < hp.size(); i++)
		{
			//			System.err.println("remove hash-code with p-value " + hp.get(i)[1]);
			toRemove.add((int) hp.get(i)[0]);
		}
		removeHashCodes(toRemove);
		if (hashCodeToCompound.size() != hashfoldsize)
			throw new IllegalStateException();

		System.err.println("applied chi square filter: " + hashCodeToCompound.size());
	}

	private void removeHashCodes(Collection<Integer> hashCodeToDelete)
	{
		for (Integer h : hashCodeToDelete)
			hashCodeToCompound.remove(h);
		compoundToHashCode = null;
		hashcodeList = null;
	}

	public ResultSet getSummary(boolean nice)
	{
		ResultSet set = new ResultSet();
		int idx = set.addResult();
		//set.setResultValue(idx, "name", getName());
		if (!nice)
			set.setResultValue(idx, "Endpoints", CountedSet.create(endpoints));
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

			if (featureSelection == FeatureSelection.fold)
			{
				estimateCollisions(set, idx, "");
			}
		}
		return set;
	}

	private void estimateCollisions(ResultSet set, int idx, String prefix)
	{
		List<Integer> counts = new ArrayList<Integer>();
		List<String> countsStr = new ArrayList<String>();
		int numCollisions = 0;
		int numBits = 0;
		for (int i = 0; i < hashfoldsize; i++)
			if (collisionMap.containsKey(i))
			{
				if (collisionMap.get(i).size() > 1)
					numCollisions++;
				numBits++;
				counts.add(collisionMap.get(i).size());
				countsStr.add(collisionMap.get(i).size() + "");
			}
			else
				countsStr.add("0");

		set.setResultValue(idx, prefix + "collisions", numCollisions + "/" + numBits);
		//		set.setResultValue(idx, prefix + "collision ratio", numCollisions / (double) numBits);
		DoubleArraySummary occu = DoubleArraySummary.create(counts);
		set.setResultValue(idx, prefix + "fragments per bit", occu);
		//		CountedSet<String> occuStr = CountedSet.create(countsStr);
		//		for (Integer f : new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 })
		//			set.setResultValue(idx, prefix + "bits with #" + f + " fragments", occuStr.getCount(f + ""));
	}

	public String toString()
	{
		return getSummary(false).translate().toNiceString();
	}

	//	LinkedHashMap<Pair, Set<Integer>> pairAdjacent = new LinkedHashMap<>();

	public class Pair
	{
		int h1;
		int h2;
		Set<Integer> commonCompounds;
		Set<Integer> commonAdjacentCompounds;
		double pCommon;
		double pCommonAdj;
		double pDiff;

		public Pair(int h1, int h2, Set<Integer> commonCompounds, Set<Integer> commonAdjacentCompounds)
		{
			if (h2 < h1)
			{
				this.h1 = h2;
				this.h2 = h1;
			}
			else
			{
				this.h1 = h1;
				this.h2 = h2;
			}
			this.commonCompounds = commonCompounds;
			this.commonAdjacentCompounds = commonAdjacentCompounds;

			pCommonAdj = pValue(commonAdjacentCompounds);
			pCommon = pValue(commonCompounds);
			pDiff = pCommon - pCommonAdj;
		}

		public boolean equals(Object o)
		{
			return (o instanceof Pair && h1 == ((Pair) o).h1 && h2 == ((Pair) o).h2);
		}

		@Override
		public int hashCode()
		{
			return HashUtil.hashCode(h1, h2);
		}

		@Override
		public String toString()
		{
			String str = "pDiff:" + StringUtil.formatDouble(pDiff) + " ";
			List<String> values = new ArrayList<String>();
			for (Integer c : commonCompounds)
				values.add(endpoints.get(c));
			str += "common:" + CountedSet.create(values) + "(p:" + StringUtil.formatDouble(pCommon) + ") ";
			values = new ArrayList<String>();
			for (Integer c : commonAdjacentCompounds)
				values.add(endpoints.get(c));
			str += "adjacent:" + CountedSet.create(values) + "(p:" + StringUtil.formatDouble(pCommonAdj) + ") ";
			return str;
		}

		public String getName()
		{
			return "pair-" + h1 + "-" + h2;
		}

	}

	transient List<String> domain;
	transient long[] all;

	private double pValue(Set<Integer> compounds)
	{
		List<String> values = new ArrayList<String>();
		for (Integer c : compounds)
			values.add(endpoints.get(c));
		long sel[] = nominalCounts(domain, values);
		return TestUtils.chiSquareTestDataSetsComparison(sel, all);
	}

	@SuppressWarnings("unchecked")
	public void minePairs(Set<Integer> compoundSubset)
	{
		if (hashcodeList == null)
			getHashcodeViaIdx(0);

		domain = new ArrayList<String>(new HashSet<String>(endpoints));
		List<String> subsetEndpoints = new ArrayList<String>();
		for (Integer c : compoundSubset)
		{
			//			System.out.println(c);
			subsetEndpoints.add(endpoints.get(c));
		}
		System.out.println("subset endpoints " + CountedSet.create(subsetEndpoints));
		all = nominalCounts(domain, subsetEndpoints);

		pairs = new ArrayList<>();

		int minFrequency = Math.max(1, (int) (compoundSubset.size() * 0.05));
		System.out.println("min-freq for pairs: " + minFrequency);
		for (int i = 0; i < hashcodeList.length - 1; i++)
		{
			if (i % 100 == 0)
				System.out.println(i);

			Integer h1 = hashcodeList[i];
			if (hashCodeToCompound.get(h1).size() < minFrequency)
				continue;
			Set<Integer> h1Compounds = (Set<Integer>) hashCodeToCompound.get(h1).clone();
			h1Compounds.retainAll(compoundSubset);
			if (h1Compounds.size() < minFrequency)
				continue;

			for (int j = i + 1; j < hashcodeList.length; j++)
			{
				Integer h2 = hashcodeList[j];
				if (hashCodeToCompound.get(h2).size() < minFrequency)
					continue;
				HashSet<Integer> intersect = new HashSet<Integer>(h1Compounds);
				intersect.retainAll(hashCodeToCompound.get(h2));
				if (intersect.size() < minFrequency)
					continue;
				Set<Integer> adj = getAdjacent(h1, h2, intersect);
				if (adj == null) // overlap
					continue;
				if (adj.size() < minFrequency)
					continue;
				//				System.out.println(numAdj + "/" + intersect.size());
				Pair pair = new Pair(h1, h2, intersect, adj);
				//				pairAdjacent.put(new Pair(h1, h2), adj);
				if (pair.pDiff > 0.1)
					pairs.add(pair);
			}
		}

		Collections.sort(pairs, new Comparator<Pair>()
		{
			@Override
			public int compare(Pair o1, Pair o2)
			{
				return Double.valueOf(o2.pDiff).compareTo(o1.pDiff);
			}
		});
		int idx = 0;
		for (Pair pair : pairs)
		{
			System.out.println((idx++) + " " + pair);
		}
	}

	private Set<Integer> getAdjacent(Integer h1, Integer h2, HashSet<Integer> intersectCompounds)
	{
		try
		{
			Set<Integer> adj = new HashSet<>();
			for (Integer c : intersectCompounds)
			{
				IAtomContainer mol = CDKUtil.parseSmiles(trainingDataSmiles.get(c));
				//			int[] atoms1 = getAtoms(mol, h1);
				//			int[] atoms2 = getAtoms(mol, h2);
				Set<Integer> atoms1 = getAtomsMultiple(mol, h1);
				Set<Integer> atoms2 = getAtomsMultiple(mol, h2);

				boolean adjacent = false;
				for (int a1 : atoms1)
				{
					List<IAtom> connected = null;
					if (!adjacent)
						connected = mol.getConnectedAtomsList(mol.getAtom(a1));

					for (int a2 : atoms2)
					{
						if (a1 == a2)
							return null;
						if (!adjacent && connected.contains(mol.getAtom(a2)))
							adjacent = true;
					}
				}
				if (adjacent)
					adj.add(c);
			}
			return adj;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
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
		{
			hashCodeToCompound = (LinkedHashMap<Integer, LinkedHashSet<Integer>>) hashCodeToCompound_unfiltered.clone();
			compoundToHashCode = null;
			hashcodeList = null;
		}
		else
			hashCodeToCompound_unfiltered = (LinkedHashMap<Integer, LinkedHashSet<Integer>>) hashCodeToCompound.clone();

		System.err.println("apply filtering: " + hashCodeToCompound.size());

		// apply new filter
		applyMinFreq(filterSubset, absMinFreq);

		if (addPairs)
			minePairs(filterSubset);

		applyClosedSetFilter(filterSubset);

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
		if (addPairs)
			suffix += "_pairs";
		return type + "_" + featureSelection + suffix;
	}

	@Override
	public int getNumAttributes()
	{
		return hashCodeToCompound.size() + (addPairs ? pairs.size() : 0);
	}

	@Override
	public String getAttributeName(int a)
	{
		if (a < hashCodeToCompound.size())
			return getHashcodeViaIdx(a) + "";
		else
			return pairs.get(a - hashCodeToCompound.size()).getName();
	}

	@Override
	public List<String> getAttributeDomain(int a)
	{
		return ArrayUtil.toList(new String[] { "0", "1" });
	}

	@Override
	public double getAttributeValue(int i, int a)
	{
		if (a < hashCodeToCompound.size())
		{
			if (hashCodeToCompound.get(getHashcodeViaIdx(a)) == null)
				throw new IllegalStateException("no compounds for hashcode, should have been removed! " + a + " "
						+ getHashcodeViaIdx(a) + " " + hashCodeToCompound.get(getHashcodeViaIdx(a)));
			if (hashCodeToCompound.get(getHashcodeViaIdx(a)).contains(i))
				return 1.0;
			else
				return 0.0;
		}
		if (pairs.get(a - hashCodeToCompound.size()).commonAdjacentCompounds.contains(i))
			return 1.0;
		else
			return 0.0;
	}

	public static void printCollisions() throws Exception
	{
		CFPType type = CFPType.ecfp6;
		CFPDataLoader l = new CFPDataLoader("data");
		ResultSet res = new ResultSet();
		String datasets[] = l.allDatasets();
		for (String name : datasets)
		{
			if (!name.startsWith("CPDBAS") && !name.startsWith("AMES") && !name.startsWith("NCTRER"))
				continue;

			System.out.println(name);
			int idx = res.addResult();
			res.setResultValue(idx, "name", name);

			CFPMiner miner = new CFPMiner(l.getDataset(name).endpoints);
			miner.type = type;
			miner.featureSelection = FeatureSelection.filt;
			miner.hashfoldsize = 1024;
			miner.mine(l.getDataset(name).smiles);

			res.setResultValue(idx, "num compounds", miner.getNumCompounds() + "");
			res.setResultValue(idx, "num fragments", miner.getNumAttributes() + "");

			for (int size : new int[] { 1024, 2048, 4096, 8192 })
			{
				miner = new CFPMiner(l.getDataset(name).endpoints);
				miner.type = type;
				miner.featureSelection = FeatureSelection.fold;
				miner.hashfoldsize = size;
				miner.mine(l.getDataset(name).smiles);
				miner.estimateCollisions(res, idx, size + " ");
			}

			if (res.getNumResults() % 5 == 0)
			{
				res.sortResults("name");
				System.out.println("\n");
				System.out.println(res.toNiceString());
			}
		}
		res.sortResults("name");
		System.out.println("\n");
		System.out.println(res.toNiceString());
		System.exit(1);
	}

	public static void main(String[] args) throws Exception
	{
		Locale.setDefault(Locale.US);
		//		printCollisions();

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

			args = "--datasetName ChEMBL_10188 --run 2 --classifier RaF --type ecfp6 --featureSelection filt --hashfoldsize 1024"
					.split(" ");

			//ChEMBL_259
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
		CFPType type = CFPType.ecfp6;
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

		CFPDataLoader.Dataset dataset = new CFPDataLoader("data").getDataset(datasetName, run);
		List<String> list = dataset.smiles;
		List<String> endpointValues = dataset.endpoints;
		ListUtil.scramble(new Random(1), list, endpointValues);

		CFPMiner cfps = new CFPMiner(ListUtil.cast(String.class, endpointValues));
		cfps.type = type;
		cfps.featureSelection = featureSelection;
		cfps.hashfoldsize = hashfoldsize;
		cfps.mine(list);
		System.out.println(cfps);

		boolean validate = true;
		if (!validate && featureSelection == FeatureSelection.filt)
		{
			cfps.applyFilter();
			System.out.println(cfps);
			//		cfps.minePairs(mols);
		}
		else
		{
			String pairs_suffix = cfps.addPairs ? "_pairs" : "";
			String outfile = "results/" + "r" + String.format("%02d", run) + "_" + type + "_" + featureSelection + "_"
					+ hashfoldsize + "_" + classifier + "_" + datasetName + pairs_suffix + ".arff";
			validate(datasetName, run, outfile, new String[] { classifier }, endpointValues, new CFPMiner[] { cfps });
		}
	}

}
