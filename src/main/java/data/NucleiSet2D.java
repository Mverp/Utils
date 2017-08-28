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

public class NucleiSet2D implements Iterable<NucleusEvent>
{
	private final List<NucleusEvent> nuclei = new ArrayList<>();
	private List<NucleiSet2D> collectives;
	private double radiusUsed = 0.0;


	/**
	 * Adds the Nucleus list of one NucleiSlice to this NucleiSlice
	 *
	 * @param aAdditionalSet
	 *            The added NucleiSlice
	 */
	public void addAll(final NucleiSet2D aAdditionalSet)
	{
		for (final NucleusEvent nucleus : aAdditionalSet)
		{
			addNucleusEvent(nucleus);
		}

	}


	/**
	 * Adds a nucleus to the set. If the nucleus already exists in the set, no action is taken and false is returned. Otherwise, true is returned.
	 *
	 * @param aNucleus
	 *            The new NucleusEvent in the NucleusSet
	 * @return True if the NucleusEvent was added to the set and false if the NucleusEvent is already present.
	 */
	public boolean addNucleusEvent(final NucleusEvent aNucleus)
	{
		if (!containsNucleusEvent(aNucleus))
		{
			// Only contain a single copy of a nucleus
			return this.nuclei.add(aNucleus);
		}
		return false;
	}


	/**
	 * Does the set of NucleusEvents contain a NucleusEvent with the given x and y coordinate. The z coordinate is considered to be set to 0 (= undefined).
	 *
	 * @param aXCoordinate
	 *            The x coordinate
	 * @param aYCoordinate
	 *            The y coordinate
	 * @return True if a Nucleus has been found that uses the given x/y coordinates as its Coordinates pair, false otherwise
	 */
	public boolean containsNucleusEvent(final float aXCoordinate, final float aYCoordinate)
	{
		return containsNucleusEvent(aXCoordinate, aYCoordinate, 0);
	}


	/**
	 * Does the set of NucleusEvents contain a NucleusEvent with the given x, y and z coordinate.
	 *
	 * @param aXCoordinate
	 *            The x coordinate
	 * @param aYCoordinate
	 *            The y coordinate
	 * @param aZCoordinate
	 *            The z coordinate
	 * @return True if a Nucleus has been found that uses the given x/y/z coordinates as its Coordinates, false otherwise
	 */
	public boolean containsNucleusEvent(final float aXCoordinate, final float aYCoordinate, final float aZCoordinate)
	{
		return containsNucleusEvent(new NucleusEvent(new Coordinates(aXCoordinate, aYCoordinate, aZCoordinate)));
	}


	/**
	 * Does the set of NucleusEvent contain a NucleusEvent based on its Coordinates.
	 *
	 * @param NucleusEvent
	 *            The NucleusEvent to be matched
	 * @return True if a NucleusEvent has been found that uses the same Coordinates, false otherwise
	 */
	public boolean containsNucleusEvent(final NucleusEvent aNucleus)
	{
		return this.nuclei.contains(aNucleus);
	}


	/**
	 * Get all the NucleusEvents in this NucleiSlice as a flat list without any further functionality.
	 *
	 * @return An ArrayList of the NucleusEvents.
	 */
	public List<NucleusEvent> getAsFlatList()
	{
		return this.nuclei;
	}


	private double[] getBestFitLine()
	{
		double xAvg = 0;
		double yAvg = 0;

		for (final NucleusEvent nucleus : this)
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
		for (final NucleusEvent nucleus : this)
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


	public NucleiSet2D getCollective(final NucleusEvent aSeed, final double aRadius)
	{
		final NucleiSet2D collective = new NucleiSet2D();
		final NucleiSet2D currentBatch = new NucleiSet2D();
		currentBatch.addNucleusEvent(aSeed);

		while (!currentBatch.isEmpty())
		{
			final NucleusEvent nextSeed = currentBatch.getFirstNucleusEvent();
			currentBatch.removeNucleusEvent(nextSeed);
			collective.addNucleusEvent(nextSeed);
			final NucleiSet2D hoal = getNucleiWithinRadius(nextSeed, aRadius);
			for (final NucleusEvent hoaledNucleus : hoal)
			{
				if (!collective.containsNucleusEvent(hoaledNucleus))
				{
					currentBatch.addNucleusEvent(hoaledNucleus);
				}
			}
		}

		return collective;
	}


	public List<NucleiSet2D> getCollectives(final double aRadius)
	{
		// Don't recalculate if you have the collectives already
		if (aRadius == this.radiusUsed && this.collectives != null)
		{
			return this.collectives;
		}

		final NucleiSet2D done = new NucleiSet2D();
		final List<NucleiSet2D> collectives = new ArrayList<>();

		for (final NucleusEvent nucleus : this.nuclei)
		{
			if (nucleus.isSingleCell())
			{
				done.addNucleusEvent(nucleus);
			}

			if (!done.containsNucleusEvent(nucleus))
			{
				final NucleiSet2D collective = this.getCollective(nucleus, aRadius);

				done.addAll(collective);

				collectives.add(collective);
			}
		}

		return collectives;
	}


	public NucleusEvent getFirstNucleusEvent()
	{
		return this.nuclei.get(0);
	}


	public List<NucleiSet2D> getIdentifiedCollectives()
	{
		return this.collectives;
	}


	public double getMaxDistanceFromBestFitLine()
	{
		double maxDistance = 0;
		final double[] bestFit = getBestFitLine();
		for (final NucleusEvent nucleus : this)
		{
			final double distance = nucleus.getCoordinates().distanceFromLine(bestFit);
			if (distance > maxDistance)
			{
				maxDistance = distance;
			}
		}

		return maxDistance;
	}


	public NucleusEvent getNearestNucleusEvent(final Coordinates aPoint)
	{
		NucleusEvent result = null;
		double shortestDistance = Double.MAX_VALUE;
		for (final NucleusEvent nucleus : this.nuclei)
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
	 * Returns a set of all nuclear links in this NucleiSlice.
	 *
	 * @return The HashSet of the links of each NucleusEvent, no double entries.
	 */
	public Set<NuclearLink<NucleusEvent>> getNuclearLinks()
	{
		final Set<NuclearLink<NucleusEvent>> result = new HashSet<>();

		for (final NucleusEvent nucleus : this.nuclei)
		{
			result.addAll(nucleus.getNeighbours());
		}

		return result;
	}


	/**
	 * Returns a set of the n nearest nuclear links per NucleusEvent in this NucleiSlice. This may coincide with the entire of set NuclearLinks for this NucleiSlice if the number n of links returned per Nucleus is set high enough.
	 *
	 * @param aNrOfNearest
	 *            The number of links (starting from the shortest) that will be returned per NucleusEvent
	 *
	 * @return The HashSet of the links of each NucleusEvent, no double entries.
	 */
	public Set<NuclearLink<NucleusEvent>> getNuclearLinks(final int aNrOfNearest)
	{
		final Set<NuclearLink<NucleusEvent>> result = new HashSet<>();

		for (final NucleusEvent nucleus : this.nuclei)
		{
			result.addAll(nucleus.getNeighbours(aNrOfNearest));
		}

		return result;
	}


	public NucleiSet2D getNucleiWithinRadius(final NucleusEvent aNucleus, final double aRadius)
	{
		final NucleiSet2D result = new NucleiSet2D();

		for (final NuclearLink<NucleusEvent> neighbourLink : aNucleus.getNeighbours())
		{
			if (neighbourLink.getDistance() <= aRadius)
			{
				result.addNucleusEvent(neighbourLink.getTheOtherEnd(aNucleus));
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
		this.collectives.sort(new Comparator<NucleiSet2D>()
		{
			@Override
			public int compare(final NucleiSet2D n1, final NucleiSet2D n2)
			{
				if (n1.size() == n2.size())
				{
					return 0;
				}
				return n1.size() > n2.size() ? 1 : -1;
			}
		});
		this.radiusUsed = aRadius;

		for (final NucleiSet2D collective : this.collectives)
		{
			for (final NucleusEvent collectiveNucleus : collective)
			{
				collectiveNucleus.setIsPartOf(Nucleus.PartOf.MULTI_CLUSTER);
			}
		}
	}


	public void identifySingleCells(final double aMaxRadius, final double aThresholdRadius, final boolean aForce)
	{
		// Look for number of neighbours within a certain diameter
		// After a second pass, select all those that have max one neighbour
		// which also has one neighbour

		final NucleiSet2D maybe = new NucleiSet2D();
		for (final NucleusEvent nucleus : this.nuclei)
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
						maybe.addNucleusEvent(nucleus);
					}
				}
				else
				{
					nucleus.setIsPartOf(Nucleus.PartOf.SINGLE_CELL);
				}
			}
			else
			{
				nucleus.setIsPartOf(Nucleus.PartOf.SINGLE_CELL);
			}
		}

		// Now the second pass. All nuclei in the maybe with just one neighbour
		// also in maybe will be judged as single cells.
		for (final NucleusEvent nucleus : maybe)
		{
			final NucleusEvent neighbour = nucleus.getNearestNeighbourNucleus();
			if (maybe.containsNucleusEvent(neighbour))
			{
				nucleus.setIsPartOf(Nucleus.PartOf.SINGLE_CELL);
			}
		}
	}


	public void identifySphere(final double aRadius)
	{
		// Small, well-formed triangles?
		// Take a seed nucleus and form its potential core around it by growing
		// from its neighbours. Largest core wins.
		NucleiSet2D maxCollective = new NucleiSet2D();

		identifyCollectives(aRadius);
		for (final NucleiSet2D collective : this.collectives)
		{
			if (collective.size() > maxCollective.size())
			{
				maxCollective = collective;
			}
		}

		for (final NucleusEvent nucleus : maxCollective)
		{
			nucleus.setIsPartOf(Nucleus.PartOf.SPHEROID);
		}
	}


	public boolean isEmpty()
	{
		return this.nuclei.isEmpty();
	}


	@Override
	public Iterator<NucleusEvent> iterator()
	{
		return this.nuclei.iterator();
	}


	public boolean removeNucleusEvent(final NucleusEvent aNucleusEvent)
	{
		return this.nuclei.remove(aNucleusEvent);
	}


	/**
	 * Resets the single/collective/etc classification on all cells.
	 */
	public void reset()
	{
		for (final NucleusEvent nucleus : this.nuclei)
		{
			nucleus.setIsPartOf(null);
		}

		this.collectives = null;
		this.radiusUsed = 0.0;
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
			for (final NucleusEvent nucleus : this.nuclei)
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
