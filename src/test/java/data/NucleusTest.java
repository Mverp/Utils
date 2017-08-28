package data;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import data.BaseNucleus.PartOf;

public class NucleusTest
{
	public Nucleus getRandomNucleus()
	{
		final Random rand = new Random();
		return new Nucleus(rand.nextInt(), rand.nextInt(), rand.nextInt());
	}


	@Test
	public void testAddNeighbour()
	{
		final Random rand = new Random();
		final int x = rand.nextInt(1000);
		final int y = rand.nextInt(1000);
		final int z = rand.nextInt(1000);
		final Nucleus nuc = new Nucleus(x, y, z);
		final Nucleus nucNN = new Nucleus(x + 1, y + 1, z + 1);
		final Nucleus nucNN2 = new Nucleus(x + 4, y + 4, z + 4);
		final Nucleus nucNN3 = new Nucleus(x + 5, y + 4, z + 4);

		final NuclearLink<Nucleus> nnLink = new NuclearLink<>(nuc, nucNN);
		assertTrue(nuc.addNeighbour(new NuclearLink<>(nuc, nucNN3)));
		assertTrue(nuc.addNeighbour(nnLink));
		assertFalse(nuc.addNeighbour(nnLink));
		assertTrue(nuc.addNeighbour(new NuclearLink<>(nuc, nucNN2)));

		// Test that the results have been ordered to length
		final List<NuclearLink<Nucleus>> nLinks = nuc.getNeighbours();
		nLinks.get(0).equals(nnLink);
	}


	@Test
	public void testAddNucleusEvent()
	{
		final Nucleus nuc = getRandomNucleus();
		final NucleusEvent nucTrue = new NucleusEvent(new Coordinates(nuc.getXcoordinate(), nuc.getYcoordinate()));
		final NucleusEvent nucF1 = new NucleusEvent(new Coordinates(nuc.getXcoordinate() + 1, nuc.getYcoordinate()));
		final NucleusEvent nucTrue2 = new NucleusEvent(new Coordinates(nuc.getXcoordinate(), nuc.getYcoordinate() + 1));
		nucTrue.setTrueNucleus(true);
		nucTrue2.setTrueNucleus(true);

		assertTrue(nuc.addNucleusEvent(nucTrue));
		// No two true events
		assertFalse(nuc.addNucleusEvent(nucTrue2));
		assertTrue(nuc.addNucleusEvent(nucF1));
		// No event twice
		assertFalse(nuc.addNucleusEvent(nucF1));

		assertTrue(nuc.getEvents().containsNucleusEvent(nucF1));
		assertTrue(nuc.getEvents().containsNucleusEvent(nucTrue));
		assertFalse(nuc.getEvents().containsNucleusEvent(nucTrue2));
		assertTrue(nuc.getEvents().size() == 2);
	}


	@Test
	public void testEquals()
	{
		final Nucleus nuc = getRandomNucleus();
		assertFalse(nuc.equals(new Double(3)));
		assertFalse(nuc.equals(null));
		assertTrue(nuc.equals(nuc));
		assertTrue(nuc.equals(new Nucleus(nuc.getCoordinates())));
		assertFalse(nuc.equals(new Nucleus(nuc.getXcoordinate() + 1, nuc.getYcoordinate(), nuc.getZcoordinate())));
	}


	@Test
	public void testGetNearestNeighbourLink()
	{
		final Random rand = new Random();
		final int x = rand.nextInt(1000);
		final int y = rand.nextInt(1000);
		final int z = rand.nextInt(1000);
		final Nucleus nuc = new Nucleus(x, y, z);
		final Nucleus nucNN = new Nucleus(x + 1, y + 1, z + 1);
		final Nucleus nucNN2 = new Nucleus(x + 4, y + 4, z + 4);
		final Nucleus nucNN3 = new Nucleus(x + 5, y + 4, z + 4);

		final NuclearLink<Nucleus> nnLink = new NuclearLink<>(nuc, nucNN);
		nuc.addNeighbour(new NuclearLink<>(nuc, nucNN3));
		nuc.addNeighbour(nnLink);
		nuc.addNeighbour(new NuclearLink<>(nuc, nucNN2));

		assertEquals(nnLink, nuc.getNearestNeighbourLink());
	}


	@Test
	public void testGetNearestNeighbourNucleus()
	{
		final Random rand = new Random();
		final int x = rand.nextInt(1000);
		final int y = rand.nextInt(1000);
		final int z = rand.nextInt(1000);
		final Nucleus nuc = new Nucleus(x, y, z);
		final Nucleus nucNN = new Nucleus(x + 1, y + 1, z + 1);
		final Nucleus nucNN2 = new Nucleus(x + 4, y + 4, z + 4);
		final Nucleus nucNN3 = new Nucleus(x + 5, y + 4, z + 4);

		final NuclearLink<Nucleus> nnLink = new NuclearLink<>(nuc, nucNN);
		nuc.addNeighbour(new NuclearLink<>(nuc, nucNN3));
		nuc.addNeighbour(nnLink);
		nuc.addNeighbour(new NuclearLink<>(nuc, nucNN2));

		assertEquals(nucNN, nuc.getNearestNeighbourNucleus());
	}


	@Test
	public void testGetNeighboursIndex()
	{
		final Random rand = new Random();
		final int x = rand.nextInt(1000);
		final int y = rand.nextInt(1000);
		final int z = rand.nextInt(1000);
		final Nucleus nuc = new Nucleus(x, y, z);
		final Nucleus nucNN = new Nucleus(x + 1, y + 1, z + 1);
		final Nucleus nucNN2 = new Nucleus(x + 4, y + 4, z + 4);
		final Nucleus nucNN3 = new Nucleus(x + 5, y + 4, z + 4);

		final NuclearLink<Nucleus> nnLink = new NuclearLink<>(nuc, nucNN);
		final NuclearLink<Nucleus> nnLink2 = new NuclearLink<>(nuc, nucNN2);
		final NuclearLink<Nucleus> nnLink3 = new NuclearLink<>(nuc, nucNN3);
		nuc.addNeighbour(nnLink2);
		nuc.addNeighbour(nnLink);
		nuc.addNeighbour(nnLink3);

		List<NuclearLink<Nucleus>> nucLinks = nuc.getNeighbours(1);
		assertEquals(nucLinks.size(), 1);
		assertTrue(nucLinks.contains(nnLink));
		assertFalse(nucLinks.contains(nnLink2));
		assertFalse(nucLinks.contains(nnLink3));

		nucLinks = nuc.getNeighbours(2);
		assertEquals(nucLinks.size(), 2);
		assertTrue(nucLinks.contains(nnLink));
		assertTrue(nucLinks.contains(nnLink2));
		assertFalse(nucLinks.contains(nnLink3));

		nucLinks = nuc.getNeighbours(3);
		assertEquals(nucLinks.size(), 3);
		assertTrue(nucLinks.contains(nnLink));
		assertTrue(nucLinks.contains(nnLink2));
		assertTrue(nucLinks.contains(nnLink3));
	}


	@Test
	public void testgetTrueEvent()
	{
		final Nucleus nuc = getRandomNucleus();
		assertNull(nuc.getTrueEvent());
		final NucleusEvent nucTrue = new NucleusEvent(new Coordinates(nuc.getXcoordinate(), nuc.getYcoordinate()));
		final NucleusEvent nucF1 = new NucleusEvent(new Coordinates(nuc.getXcoordinate() + 1, nuc.getYcoordinate()));
		final NucleusEvent nucf2 = new NucleusEvent(new Coordinates(nuc.getXcoordinate(), nuc.getYcoordinate() + 1));
		nucTrue.setTrueNucleus(true);

		nuc.addNucleusEvent(nucF1);
		assertNull(nuc.getTrueEvent());
		nuc.addNucleusEvent(nucTrue);
		nuc.addNucleusEvent(nucf2);

		assertEquals(nucTrue, nuc.getTrueEvent());
	}


	@Test
	public void testIsPartOfCluster()
	{
		final Nucleus nuc = getRandomNucleus();
		nuc.setIsPartOf(PartOf.MULTI_CLUSTER);
		assertTrue(nuc.isPartOfSomething());
		assertTrue(nuc.isPartOfCluster());
		assertFalse(nuc.isPartOfEdge());
		assertFalse(nuc.isPartOfSpheroid());
		assertFalse(nuc.isPartOfStrand());
		assertFalse(nuc.isSingleCell());
	}


	@Test
	public void testIsPartOfEdge()
	{
		final Nucleus nuc = getRandomNucleus();
		nuc.setIsPartOf(PartOf.EDGE);
		assertTrue(nuc.isPartOfSomething());
		assertFalse(nuc.isPartOfCluster());
		assertTrue(nuc.isPartOfEdge());
		assertFalse(nuc.isPartOfSpheroid());
		assertFalse(nuc.isPartOfStrand());
		assertFalse(nuc.isSingleCell());
	}


	@Test
	public void testIsPartOfSomething()
	{
		final Nucleus nuc = getRandomNucleus();
		nuc.setIsPartOf(PartOf.MULTI_CLUSTER);
		assertTrue(nuc.isPartOfSomething());
		nuc.setIsPartOf(null);
		assertFalse(nuc.isPartOfSomething());
		nuc.setIsPartOf(PartOf.EDGE);
		assertTrue(nuc.isPartOfSomething());
		nuc.setIsPartOf(PartOf.SPHEROID);
		assertTrue(nuc.isPartOfSomething());
		nuc.setIsPartOf(PartOf.STRAND);
		assertTrue(nuc.isPartOfSomething());
		nuc.setIsPartOf(PartOf.SINGLE_CELL);
		assertTrue(nuc.isPartOfSomething());
	}


	@Test
	public void testIsPartOfSpheroid()
	{
		final Nucleus nuc = getRandomNucleus();
		nuc.setIsPartOf(PartOf.SPHEROID);
		assertTrue(nuc.isPartOfSomething());
		assertFalse(nuc.isPartOfCluster());
		assertFalse(nuc.isPartOfEdge());
		assertTrue(nuc.isPartOfSpheroid());
		assertFalse(nuc.isPartOfStrand());
		assertFalse(nuc.isSingleCell());
	}


	@Test
	public void testIsPartOfStrand()
	{
		final Nucleus nuc = getRandomNucleus();
		nuc.setIsPartOf(PartOf.STRAND);
		assertTrue(nuc.isPartOfSomething());
		assertFalse(nuc.isPartOfCluster());
		assertFalse(nuc.isPartOfEdge());
		assertFalse(nuc.isPartOfSpheroid());
		assertTrue(nuc.isPartOfStrand());
		assertFalse(nuc.isSingleCell());
	}


	@Test
	public void testIsSingleCell()
	{
		final Nucleus nuc = getRandomNucleus();
		nuc.setIsPartOf(PartOf.SINGLE_CELL);
		assertTrue(nuc.isPartOfSomething());
		assertFalse(nuc.isPartOfCluster());
		assertFalse(nuc.isPartOfEdge());
		assertFalse(nuc.isPartOfSpheroid());
		assertFalse(nuc.isPartOfStrand());
		assertTrue(nuc.isSingleCell());
	}


	@Test
	public void testSetCoordinates()
	{
		final Nucleus nuc = getRandomNucleus();
		final Coordinates coord = new Coordinates(nuc.getXcoordinate() + 1, nuc.getYcoordinate(), nuc.getZcoordinate());
		assertFalse(nuc.getCoordinates().equals(coord));
		nuc.setCoordinates(coord);
		assertTrue(nuc.getCoordinates().equals(coord));
	}


	@Test
	public void testSetFrstMaximumValue()
	{
		final double value = new Random().nextDouble();
		final Nucleus nuc = getRandomNucleus();
		nuc.setFrstMaximumValue(value);
		assertTrue(nuc.getFrstMaximumValue() == value);
	}


	@Test
	public void testSetLocalIntensity()
	{
		final double value = new Random().nextDouble();
		final Nucleus nuc = getRandomNucleus();
		nuc.setLocalIntensity(value);
		assertTrue(nuc.getLocalIntensity() == value);
	}


	@Test
	public void testSetNeighbours()
	{
		final Random rand = new Random();
		final int x = rand.nextInt(1000);
		final int y = rand.nextInt(1000);
		final int z = rand.nextInt(1000);
		final Nucleus nuc = new Nucleus(x, y, z);

		final Nucleus nucNN = new Nucleus(x + 1, y + 1, z + 1);
		final Nucleus nucNN2 = new Nucleus(x + 4, y + 4, z + 4);
		final Nucleus nucNN3 = new Nucleus(x + 5, y + 4, z + 4);
		final List<NuclearLink<Nucleus>> nucList = new ArrayList<>();

		final NuclearLink<Nucleus> nnLink = new NuclearLink<>(nuc, nucNN);
		final NuclearLink<Nucleus> nnLink2 = new NuclearLink<>(nuc, nucNN2);
		final NuclearLink<Nucleus> nnLink3 = new NuclearLink<>(nuc, nucNN3);
		nucList.add(nnLink3);
		nucList.add(nnLink);
		nucList.add(nnLink2);
		final List<NuclearLink<Nucleus>> orig = new ArrayList<>();
		orig.addAll(nucList);

		nuc.setNeighbours(nucList);
		assertFalse(orig.equals(nucList));
		orig.sort(new NuclearLinkComparator<Nucleus>());
		assertEquals(orig, nucList);
	}

}
