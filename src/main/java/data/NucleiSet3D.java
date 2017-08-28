package data;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ij.IJ;

public class NucleiSet3D implements Iterable<Nucleus>
{
	private final List<Nucleus> nuclei = new ArrayList<>();
	private boolean is3D = false;
	private List<NucleiSet3D> collectives;
	private double radiusUsed = 0.0;


	/**
	 * Adds the Nucleus list of one NucleusSet to this NucleiSet
	 *
	 * @param aAdditionalSet
	 *            The added NucleusSet
	 */
	public void addAll(final NucleiSet3D aAdditionalSet)
	{
		for (final Nucleus nucleus : aAdditionalSet)
		{
			addNucleus(nucleus);
		}

	}


	/**
	 * Adds a nucleus to the set. If the nucleus already exists in the set, no action is taken and false is returned. Otherwise, true is returned.
	 *
	 * @param aNucleus
	 *            The new Nucleus in the NucleusSet
	 * @return True if the Nucleus was added to the set and false if the Nucleus is already present.
	 */
	public boolean addNucleus(final Nucleus aNucleus)
	{
		if (!containsNucleus(aNucleus))
		{
			// Only contain a single copy of a nucleus
			final boolean added = this.nuclei.add(aNucleus);
			if (added)
			{
				// Maintain z order in the nuclear list.
				this.nuclei.sort(new NuclearZComparator<Nucleus>());
			}
			return added;
		}
		return false;
	}


	/**
	 * Does the set of nuclei contain a Nucleus with the given x and y coordinate. The z coordinate is considered to be set to 0 (= undefined).
	 *
	 * @param aXCoordinate
	 *            The x coordinate
	 * @param aYCoordinate
	 *            The y coordinate
	 * @return True if a Nucleus has been found that uses the given x/y coordinates as its Coordinates pair, false otherwise
	 */
	public boolean containsNucleus(final float aXCoordinate, final float aYCoordinate)
	{
		return containsNucleus(aXCoordinate, aYCoordinate, 0);
	}


	/**
	 * Does the set of nuclei contain a Nucleus with the given x, y and z coordinate.
	 *
	 * @param aXCoordinate
	 *            The x coordinate
	 * @param aYCoordinate
	 *            The y coordinate
	 * @param aZCoordinate
	 *            The z coordinate
	 * @return True if a Nucleus has been found that uses the given x/y/z coordinates as its Coordinates, false otherwise
	 */
	public boolean containsNucleus(final float aXCoordinate, final float aYCoordinate, final float aZCoordinate)
	{
		return containsNucleus(new Nucleus(new Coordinates(aXCoordinate, aYCoordinate, aZCoordinate)));
	}


	/**
	 * Does the set of nuclei contain a Nucleus based on its Coordinates.
	 *
	 * @param aNucleus
	 *            The Nucleus to be matched
	 * @return True if a Nucleus has been found that uses the same Coordinates, false otherwise
	 */
	public boolean containsNucleus(final Nucleus aNucleus)
	{
		return this.nuclei.contains(aNucleus);
	}


	/**
	 * Get all the Nuclei in this NucleiSet as a flat list without any further functionality.
	 *
	 * @return An ArrayList of the Nuclei.
	 */
	public List<Nucleus> getAsFlatList()
	{
		return this.nuclei;
	}


	private double[] getBestFitLine()
	{
		double xAvg = 0;
		double yAvg = 0;

		for (final Nucleus nucleus : this)
		{
			final Coordinates nCoords = nucleus.getCoordinates();
			xAvg += nCoords.getXcoordinate();
			yAvg += nCoords.getYcoordinate();
		}

		xAvg /= this.size();
		yAvg /= this.size();

		// Calculate slope
		double sumTop = 0;
		double sumBottom = 0;
		for (final Nucleus nucleus : this)
		{
			final Coordinates nCoords = nucleus.getCoordinates();
			final double xDiff = nCoords.getXcoordinate() - xAvg;
			final double yDiff = nCoords.getYcoordinate() - yAvg;
			sumTop += (xDiff * yDiff);
			sumBottom += Math.pow(xDiff, 2);
		}

		final double slope = sumTop / sumBottom;
		final double yIntercept = yAvg - (slope * xAvg);
		final double[] result = { slope, yIntercept };

		return result;
	}


	public NucleiSet3D getCollective(final Nucleus aSeed, final double aRadius)
	{
		final NucleiSet3D collective = new NucleiSet3D();
		final NucleiSet3D currentBatch = new NucleiSet3D();
		currentBatch.addNucleus(aSeed);

		while (!currentBatch.isEmpty())
		{
			final Nucleus nextSeed = currentBatch.getFirstNucleus();
			currentBatch.removeNucleus(nextSeed);
			collective.addNucleus(nextSeed);
			final NucleiSet3D hoal = getNucleiWithinRadius(nextSeed, aRadius);
			for (final Nucleus hoaledNucleus : hoal)
			{
				if (!collective.containsNucleus(hoaledNucleus))
				{
					currentBatch.addNucleus(hoaledNucleus);
				}
			}
		}

		return collective;
	}


	public List<NucleiSet3D> getCollectives(final double aRadius)
	{
		// Don't recalculate if you have the collectives already
		if (aRadius == this.radiusUsed && this.collectives != null)
		{
			return this.collectives;
		}

		final NucleiSet3D done = new NucleiSet3D();
		final List<NucleiSet3D> collectives = new ArrayList<>();

		for (final Nucleus nucleus : this.nuclei)
		{
			if (nucleus.isSingleCell())
			{
				done.addNucleus(nucleus);
			}

			if (!done.containsNucleus(nucleus))
			{
				final NucleiSet3D collective = this.getCollective(nucleus, aRadius);

				done.addAll(collective);

				collectives.add(collective);
			}
		}

		return collectives;
	}


	public Nucleus getFirstNucleus()
	{
		return this.nuclei.get(0);
	}


	public List<NucleiSet3D> getIdentifiedCollectives()
	{
		return this.collectives;
	}


	public double getMaxDistanceFromBestFitLine()
	{
		double maxDistance = 0;
		final double[] bestFit = getBestFitLine();
		for (final Nucleus nucleus : this)
		{
			final double distance = nucleus.getCoordinates().distanceFromLine(bestFit);
			if (distance > maxDistance)
			{
				maxDistance = distance;
			}
		}

		return maxDistance;
	}


	public Nucleus getNearestNucleus(final Coordinates aPoint)
	{
		Nucleus result = null;
		double shortestDistance = Double.MAX_VALUE;
		for (final Nucleus nucleus : this.nuclei)
		{
			final double distance = nucleus.getCoordinates().distanceFromPoint(aPoint);
			if (distance < shortestDistance)
			{
				result = nucleus;
				shortestDistance = distance;
			}
		}

		return result;
	}


	/**
	 * Returns a set of all nuclear links in this NucleiSet.
	 *
	 * @return The HashSet of the links of each Nucleus, no double entries.
	 */
	public Set<NuclearLink<Nucleus>> getNuclearLinks()
	{
		final Set<NuclearLink<Nucleus>> result = new HashSet<>();

		for (final Nucleus nucleus : this.nuclei)
		{
			result.addAll(nucleus.getNeighbours());
		}

		return result;
	}


	/**
	 * Returns a set of the nearest nuclear links per Nucleus in this NucleiSet. This may coincide with the entire of set NuclearLinks for this NucleiSet if the number of links returned per Nucleus is set high enough.
	 *
	 * @param aNrOfNearest
	 *            The shortest number of links that will be returned per Nucleus
	 *
	 * @return The HashSet of the links of each Nucleus, no double entries.
	 */
	public Set<NuclearLink<Nucleus>> getNuclearLinks(final int aNrOfNearest)
	{
		final Set<NuclearLink<Nucleus>> result = new HashSet<>();

		for (final Nucleus nucleus : this.nuclei)
		{
			result.addAll(nucleus.getNeighbours(aNrOfNearest));
		}

		return result;
	}


	public NucleiSet3D getNucleiWithinRadius(final Nucleus aNucleus, final double aRadius)
	{
		final NucleiSet3D result = new NucleiSet3D();

		for (final NuclearLink<Nucleus> neighbourLink : aNucleus.getNeighbours())
		{
			if (neighbourLink.getDistance() <= aRadius)
			{
				result.addNucleus(neighbourLink.getTheOtherEnd(aNucleus));
			}
			else
			{
				// The links are sorted in length, so no smaller one will be
				// there anymore
				break;
			}
		}

		return result;
	}


	public double getRadiusUsed()
	{
		return this.radiusUsed;
	}


	public void identifyCollectives(final double aRadius)
	{
		// Don't do this twice for the same radius.
		if (aRadius == this.radiusUsed && this.collectives != null)
		{
			return;
		}

		this.collectives = getCollectives(aRadius);
		this.collectives.sort(new Comparator<NucleiSet3D>()
		{
			@Override
			public int compare(final NucleiSet3D n1, final NucleiSet3D n2)
			{
				if (n1.size() == n2.size())
				{
					return 0;
				}
				return n1.size() > n2.size() ? 1 : -1;
			}
		});
		this.radiusUsed = aRadius;

		for (final NucleiSet3D collective : this.collectives)
		{
			for (final Nucleus collectiveNucleus : collective)
			{
				collectiveNucleus.setIsPartOf(Nucleus.PartOf.MULTI_CLUSTER);
			}
		}
	}


	/**
	 * Look for number of neighbours within a certain diameter. After a second pass, select all those that have max one neighbour which also has one neighbour
	 *
	 * @param aMaxRadius
	 *            The radius in which a neighbour cell is considered a potential part of the same cluster
	 * @param aThresholdRadius
	 *            The radius within which a pair of cells is considered an over-segmentation
	 * @param aForce
	 *            If true, all cells will be considered as potential single cells, if false, only those cells that haven't been assigned a category will be.
	 * @return The number of single cells found
	 */
	public int identifySingleCells(final double aMaxRadius, final double aThresholdRadius, final boolean aForce)
	{
		int nrSingleCells = 0;
		final NucleiSet3D maybe = new NucleiSet3D();
		for (final Nucleus nucleus : this.nuclei)
		{

			if ((!nucleus.isPartOfSomething() || aForce) && nucleus.getNeighbours() != null && nucleus.getNeighbours().size() > 1)
			{
				final double firstDistance = nucleus.getNearestNeighbourLink().getDistance();
				final boolean firstIsClose = firstDistance <= aMaxRadius;

				if (firstIsClose)
				{
					final boolean secondIsClose = nucleus.getNeighbours().get(1).getDistance() <= aMaxRadius;
					final boolean firstIsVeryClose = firstDistance <= aThresholdRadius;
					if (!secondIsClose && firstIsVeryClose)
					{
						maybe.addNucleus(nucleus);
					}
				}
				else
				{
					nucleus.setIsPartOf(Nucleus.PartOf.SINGLE_CELL);
					nrSingleCells++;
				}
			}
			else
			{
				nucleus.setIsPartOf(Nucleus.PartOf.SINGLE_CELL);
				nrSingleCells++;
			}
		}

		// Now the second pass. All nuclei in the maybe with just one neighbour
		// also in maybe will be judged as single cells.
		for (final Nucleus nucleus : maybe)
		{
			final Nucleus neighbour = nucleus.getNearestNeighbourNucleus();
			if (maybe.containsNucleus(neighbour))
			{
				nucleus.setIsPartOf(Nucleus.PartOf.SINGLE_CELL);
				nrSingleCells++;
			}
		}

		return nrSingleCells++;
	}


	public void identifySphere(final double aRadius)
	{
		// Small, well-formed triangles?
		// Take a seed nucleus and form its potential core around it by growing
		// from its neighbours. Largest core wins.
		NucleiSet3D maxCollective = new NucleiSet3D();

		identifyCollectives(aRadius);
		for (final NucleiSet3D collective : this.collectives)
		{
			if (collective.size() > maxCollective.size())
			{
				maxCollective = collective;
			}
		}

		for (final Nucleus nucleus : maxCollective)
		{
			nucleus.setIsPartOf(Nucleus.PartOf.SPHEROID);
		}
	}


	/**
	 * Is this a 3D or a 2D (slice) Nucleiset?
	 *
	 * @return true if this is a 3D set, false if 2D
	 */
	public boolean is3D()
	{
		return this.is3D;
	}


	public boolean isEmpty()
	{
		return this.nuclei.isEmpty();
	}


	@Override
	public Iterator<Nucleus> iterator()
	{
		return this.nuclei.iterator();
	}


	public boolean removeNucleus(final Nucleus aNucleus)
	{
		return this.nuclei.remove(aNucleus);
	}


	/**
	 * Resets the single/collective/etc classification on all cells.
	 */
	public void reset()
	{
		for (final Nucleus nucleus : this.nuclei)
		{
			nucleus.setIsPartOf(null);
		}

		this.collectives = null;
		this.radiusUsed = 0.0;
	}


	/**
	 * Sets if this set is a 3D set or not. Note, this is not checked in any way, so be sure what you set.
	 *
	 * @param aIs3D
	 *            Use true if the set is a 3D set. False is default.
	 */
	public void set3D(final boolean aIs3D)
	{
		this.is3D = aIs3D;
	}


	public int size()
	{
		return this.nuclei.size();
	}


	/**
	 * Writes the nuclei data to file. The file contains the data of one Nucleus per line, with each feature separated by a tab.
	 *
	 * @param The
	 *            file name (including path).
	 */
	public void writeDataToFile(final String aFileName)
	{
		try
		{
			final PrintWriter resultsFile = new PrintWriter(aFileName);

			int i = 0;
			for (final Nucleus nucleus : this.nuclei)
			{
				// Write the nucleus info to file
				final StringBuilder sb = new StringBuilder();
				sb.append(i + "\t");
				sb.append(nucleus.getXcoordinate() + "\t");
				sb.append(nucleus.getYcoordinate() + "\t");
				sb.append(nucleus.getZcoordinate() + "\t");
				sb.append(nucleus.getLocalIntensity() + "\t");
				sb.append(nucleus.getFrstMaximumValue());
				resultsFile.println(sb.toString());
				i++;
			}

			resultsFile.close();
		}
		catch (final FileNotFoundException fnfEx)
		{
			IJ.handleException(fnfEx);
		}
	}
}
