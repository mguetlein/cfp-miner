package org.kramerlab.cfpminer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.mg.javalib.gui.MultiImageIcon;
import org.mg.javalib.gui.MultiImageIcon.Layout;
import org.mg.javalib.gui.MultiImageIcon.Orientation;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.ColorUtil;
import org.mg.javalib.util.FileUtil;
import org.mg.javalib.util.ImageLoader;
import org.mg.javalib.util.SwingUtil;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.io.INChIPlainTextReader;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.renderer.AtomContainerRenderer;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.BasicBondGenerator;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
import org.openscience.cdk.renderer.generators.HighlightGenerator;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;
import org.openscience.cdk.silent.AtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

public class CDKUtil
{
	static HashMap<String, String> replace = new HashMap<String, String>();

	static
	{
		replace.put("CC1CC(=O)Nc2c(CCN3CCN(c4n[s+]([O-])c5ccccc54)CC3)cc(F)cc21",
				"CC1CC(=O)Nc2c(CCN3CCN(C4=c5c(=[S+](N4)[O-])cccc5)CC3)cc(F)cc12");
	}

	public static List<IAtomContainer> readFromCSV(File f) throws Exception
	{
		try
		{
			SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
			List<String> propNames = new ArrayList<String>();
			HashMap<String, List<String>> props = new HashMap<String, List<String>>();
			FileUtil.CSVFile csvFile = FileUtil.readCSV(f.getAbsolutePath());
			int rowIndex = 0;
			List<IAtomContainer> list = new ArrayList<IAtomContainer>();
			for (String line[] : csvFile.content)
			{
				if (rowIndex == 0)
				{
					for (String value : line)
					{
						propNames.add(value);
						props.put(value, new ArrayList<String>());
					}
				}
				else
				{
					int columnIndex = 0;
					for (String value : line)
					{
						if (columnIndex == 0)
						{
							if (replace.containsKey(value))
								value = replace.get(value);
							list.add(sp.parseSmiles(value));
						}
						props.get(propNames.get(columnIndex)).add(value);
						columnIndex++;
					}
					while (columnIndex < props.size())
					{
						props.get(propNames.get(columnIndex)).add(null);
						columnIndex++;
					}
				}
				rowIndex++;
			}

			if (list.size() != (csvFile.content.size() - 1))
				throw new Error("wrong num molecules checking smiles");

			int molCount = 0;
			for (IAtomContainer mol : list)
			{
				int pCount = 0;
				for (String p : propNames)
				{
					if (props.get(p).size() != list.size())
						throw new IllegalStateException("num molecules: " + list.size() + ", num values for '" + p
								+ "': " + props.get(p).size());
					String prop = pCount == 0 ? "SMILES" : p;
					mol.setProperty(prop, props.get(p).get(molCount));
					pCount++;
				}
				molCount++;
			}
			boolean removeSMIdbName = true;
			for (IAtomContainer mol : list)
				if (mol.getProperties().containsKey("SMIdbNAME") && mol.getProperty("SMIdbNAME") != null
						&& mol.getProperty("SMIdbNAME").toString().trim().length() > 0)
					removeSMIdbName = false;
			if (removeSMIdbName)
				for (IAtomContainer mol : list)
					if (mol.getProperties().containsKey("SMIdbNAME"))
						mol.removeProperty("SMIdbNAME");
			return list;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}

	public static void main(String[] args) throws InvalidSmilesException, Exception
	{
		//		String s[] = new String[] { "c1ccccc1", "c1cccnc1", "Cl.c1cccnc1" };
		//		CFPMiner miner = new CFPMiner(null);
		//		miner.setType(CFPMiner.CFPType.ecfp);
		//		miner.setFeatureSelection(CFPMiner.FeatureSelection.filt);
		//		miner.setHashfoldsize(1024);
		//		miner.update(s);
		//
		//		System.out.println(ArrayUtil.toString(miner.getAtoms("c1ccccc1", miner.getHashcodeViaIdx(0))));
		//		System.out.println(ArrayUtil.toString(miner.getAtoms("c1cccnc1", miner.getHashcodeViaIdx(0))));
		//		System.out.println(ArrayUtil.toString(miner.getAtoms("Cl.c1cccnc1", miner.getHashcodeViaIdx(0))));

		draw(null, new SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles("Cl.Cl"), 100);

		//		drawFP(null, new SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles("c1ccccc1.Cl"), new int[] { 1,
		//				2, 3 }, false, -1);

		//		System.out.println(OBWrapper
		//				.runSmilesThroughOpenBabel("CC1CC(=O)Nc2c(CCN3CCN(c4n[s+]([O-])c5ccccc54)CC3)cc(F)cc21"));
	}

	public static void draw(String pngFile, IAtomContainer mol, int size) throws Exception
	{
		IAtomContainerSet set = ConnectivityChecker.partitionIntoMolecules(mol);
		BufferedImage image;
		if (set.getAtomContainerCount() < 2)
			image = draw(mol, size);
		else
		{
			List<ImageIcon> icons = new ArrayList<ImageIcon>();
			for (int i = 0; i < set.getAtomContainerCount(); i++)
				icons.add(new ImageIcon(draw(set.getAtomContainer(i), -1)));
			image = (BufferedImage) new MultiImageIcon(icons, Layout.horizontal, Orientation.center, 2).getImage();
			if (size != -1)
				image = (BufferedImage) ImageLoader.getShrinkedImage(new ImageIcon(image), size, size).getImage();
		}
		if (pngFile != null)
			ImageIO.write((RenderedImage) image, "PNG", new File(pngFile));
		else
			SwingUtil.showInDialog(new JLabel(new ImageIcon(image)));
	}

	@SuppressWarnings("unchecked")
	public static BufferedImage draw(IAtomContainer mol, int size) throws CDKException
	{

		//		IAtomContainer mol = new SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles("NCC");
		StructureDiagramGenerator sdg = new StructureDiagramGenerator();
		sdg.setMolecule(mol);
		sdg.generateCoordinates();
		mol = sdg.getMolecule();

		@SuppressWarnings("rawtypes")
		List generators = new ArrayList<>();
		generators.add(new BasicSceneGenerator());
		generators.add(new BasicBondGenerator());
		generators.add(new BasicAtomGenerator());
		AtomContainerRenderer renderer = new AtomContainerRenderer(generators, new AWTFontManager());

		int width;
		int height;
		if (size == -1)
		{
			renderer.setup(mol, new Rectangle(0, 0, 1, 1));
			Rectangle diagramRectangle = renderer.paint(mol, new AWTDrawVisitor((Graphics2D) new BufferedImage(1, 1,
					BufferedImage.TYPE_INT_ARGB).getGraphics()));
			width = (int) (1.5 * (diagramRectangle.getWidth() + diagramRectangle.x));
			height = (int) (1.5 * (diagramRectangle.getHeight() + diagramRectangle.y));
			width = Math.max(20, width);
			height = Math.max(20, height);
		}
		else
		{
			width = size;
			height = size;
		}

		Rectangle drawArea = new Rectangle(width, height);
		renderer.setup(mol, drawArea);
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) image.getGraphics();
		//		g2.setColor(Color.WHITE);
		//		g2.fillRect(0, 0, width, height);
		renderer.paint(mol, new AWTDrawVisitor(g2), drawArea, true);

		return image;
	}

	public static void drawFP(String pngFile, IAtomContainer mol, int atoms[], boolean crop, int size) throws Exception
	{
		IAtomContainerSet set = ConnectivityChecker.partitionIntoMolecules(mol);
		BufferedImage image = null;
		if (set.getAtomContainerCount() < 2)
			image = drawFP(mol, atoms, crop, size);
		else
		{
			int minAtomIdx = ArrayUtil.getMinMax(atoms)[0];
			List<ImageIcon> icons = new ArrayList<ImageIcon>();
			for (int i = 0; i < set.getAtomContainerCount(); i++)
			{
				if (minAtomIdx < 0 || minAtomIdx >= set.getAtomContainer(i).getAtomCount())
				{
					if (!crop)
						icons.add(new ImageIcon(draw(set.getAtomContainer(i), -1)));
				}
				else
				{
					image = drawFP(set.getAtomContainer(i), atoms, crop, crop ? size : -1);
					icons.add(new ImageIcon(image));
				}
				minAtomIdx -= set.getAtomContainer(i).getAtomCount();
				for (int j = 0; j < atoms.length; j++)
					atoms[j] -= set.getAtomContainer(i).getAtomCount();
			}
			if (!crop)
			{
				image = (BufferedImage) new MultiImageIcon(icons, Layout.horizontal, Orientation.center, 2).getImage();
				if (size != -1)
					image = (BufferedImage) ImageLoader.getShrinkedImage(new ImageIcon(image), size, size).getImage();
			}
		}

		if (pngFile != null)
			ImageIO.write((RenderedImage) image, "PNG", new File(pngFile));
		else
			SwingUtil.showInDialog(new JLabel(new ImageIcon(image)));
	}

	@SuppressWarnings("unchecked")
	public static BufferedImage drawFP(IAtomContainer mol, int atoms[], boolean crop, int size) throws Exception
	{
		if (atoms == null || atoms.length == 0)
			if (crop)
				throw new IllegalArgumentException();
		if (crop && size == 1)
			throw new IllegalArgumentException();

		//		IAtomContainer mol = new SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles("NCC");
		StructureDiagramGenerator sdg = new StructureDiagramGenerator();
		sdg.setMolecule(mol);
		sdg.generateCoordinates();
		mol = sdg.getMolecule();
		HashMap<IChemObject, Integer> ids = new HashMap<IChemObject, Integer>();
		IAtomContainer s = new AtomContainer();
		for (int j = 0; j < mol.getAtomCount(); j++)
			if (atoms != null && ArrayUtil.indexOf(atoms, j) != -1)
			{
				ids.put(mol.getAtom(j), 1);
				//				System.out.println("s " + mol.getAtom(j).getPoint2d());
				s.addAtom(mol.getAtom(j));
			}
		//			else
		//				System.out.println("n " + mol.getAtom(j).getPoint2d());
		mol.setProperty(HighlightGenerator.ID_MAP, ids);

		@SuppressWarnings("rawtypes")
		List generators = new ArrayList<>();
		generators.add(new BasicSceneGenerator());
		generators.add(new HighlightGenerator());
		generators.add(new BasicBondGenerator());
		generators.add(new BasicAtomGenerator());
		AtomContainerRenderer renderer = new AtomContainerRenderer(generators, new AWTFontManager());

		renderer.getRenderer2DModel().set(HighlightGenerator.HighlightPalette.class,
				HighlightGenerator.createPalette(null, ColorUtil.transparent(Color.RED, 150)));
		renderer.getRenderer2DModel().set(HighlightGenerator.HighlightRadius.class, 15.0);

		// determine preferred size of molecule
		renderer.setup(mol, new Rectangle(0, 0, 1, 1));
		Rectangle diagramRectangle = renderer.paint(mol, new AWTDrawVisitor((Graphics2D) new BufferedImage(1, 1,
				BufferedImage.TYPE_INT_ARGB).getGraphics()));
		int width = (int) (1.5 * (diagramRectangle.getWidth() + diagramRectangle.x));
		int height = (int) (1.5 * (diagramRectangle.getHeight() + diagramRectangle.y));
		width = Math.max(20, width);
		height = Math.max(20, height);

		// draw according to preferred size (with 10 pixels extra for the highlights)
		Rectangle drawArea = new Rectangle(5, 5, width - 10, height - 10);
		renderer.setup(mol, drawArea);
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) image.getGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, width, height);
		renderer.paint(mol, new AWTDrawVisitor(g2), drawArea, true);

		final Rectangle2D r = renderer.calculateDiagramBounds(s);
		int x, y, w, h;
		if (size >= width)
		{
			x = 0;
			w = width - 1;
		}
		else
		{
			x = (int) (r.getX() - (size - r.getWidth()) / 2.0);
			if (x + size > width)
				x -= (x + size - width);
			if (x < 0)
				x = 0;
			w = size;
		}
		if (size >= height)
		{
			y = 0;
			h = height - 1;
		}
		else
		{
			y = (int) (r.getY() - (size - r.getHeight()) / 2.0);
			if (y + size > height)
				y -= (y + size - height);
			if (y < 0)
				y = 0;
			h = size;
		}
		final Rectangle2D r2 = r.getFrame();
		r2.setFrame(x, y, w, h);
		//		System.out.println(r);
		//		System.out.println(r2);
		//		System.out.println(image.getWidth() + ", " + image.getHeight());

		//		JLabel l = new JLabel(new ImageIcon(image))
		//		{
		//			@Override
		//			public void paint(Graphics g)
		//			{
		//				super.paint(g);
		//				g.setColor(Color.GREEN);
		//				g.drawRect(0, 0, width, height);
		//				g.setColor(Color.RED);
		//				g.drawRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
		//				g.setColor(Color.BLUE);
		//				g.drawRect((int) r2.getX(), (int) r2.getY(), (int) r2.getWidth(), (int) r2.getHeight());
		//
		//			}
		//		};
		//		SwingUtil.showInDialog(l);

		if (crop)
			image = image.getSubimage(x, y, w, h);
		if (size != -1 && (image.getWidth() > size || image.getHeight() > size))
			image = (BufferedImage) ImageLoader.getShrinkedImage(new ImageIcon(image), size, size).getImage();
		if (crop && (image.getWidth() < size || image.getHeight() < size))
		{
			BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
			Graphics g = img.getGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, size, size);
			g.drawImage(image, (size - image.getWidth()) / 2, (size - image.getHeight()) / 2, null);
			image = img;
		}
		return image;
	}

	public static String toSmiles(IAtomContainer mol) throws CDKException
	{
		return new SmilesGenerator().create(mol);
	}

	public static String toInchi(IAtomContainer mol) throws CDKException
	{
		return InChIGeneratorFactory.getInstance().getInChIGenerator(mol).getInchi();
	}

	public static String toInchiKey(IAtomContainer mol) throws CDKException
	{
		return InChIGeneratorFactory.getInstance().getInChIGenerator(mol).getInchiKey();
	}

	private static HashMap<String, IAtomContainer> smilesToMol = new HashMap<String, IAtomContainer>();

	public static IAtomContainer parseSmiles(String smiles) throws InvalidSmilesException
	{
		if (!smilesToMol.containsKey(smiles))
			smilesToMol.put(smiles, new SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles(smiles));
		return smilesToMol.get(smiles);
	}

	private static HashMap<String, IAtomContainer> inchiToMol = new HashMap<String, IAtomContainer>();

	public static IAtomContainer parseInchi(String inchi) throws IOException, CDKException
	{
		if (!inchiToMol.containsKey(inchi))
		{
			INChIPlainTextReader reader = new INChIPlainTextReader(new ByteArrayInputStream(inchi.getBytes()));
			IChemFile content = (IChemFile) reader.read((IChemObject) new ChemFile());
			reader.close();
			List<IAtomContainer> l = ChemFileManipulator.getAllAtomContainers(content);
			if (l.size() != 1)
				throw new RuntimeException("Could not read inchi: " + inchi);
			IAtomContainer m = l.get(0);
			inchiToMol.put(inchi, m);
		}
		return inchiToMol.get(inchi);
	}

}
