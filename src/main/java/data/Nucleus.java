package data;

import java.util.ArrayList;
import java.util.List;

public class Nucleus extends BaseNucleus
{
	private List<NuclearLink<Nucleus>> neighbours;

	private final NucleiSet2D events = new NucleiSet2D();


	public Nucleus(final Coordinates aPoint)
	{
		super(aPoint);
	}


	public Nucleus(final double aXCoordinate, final double aYCoordinate, final double aZCoordinate)
	{
		super(aXCoordinate, aYCoordinate, aZCoordinate);
	}


	/**
	 * Add a new Delaunay neighbour connection if the link did not appear in the
	 * list yet.
	 *
	 * @param the
	 *            NuclearLink to the new neighbour
	 * @return True if the link did not appear in the list yet, false if it did.
	 */
	public boolean addNeighbour(final NuclearLink<Nucleus> aNewNeighbour)
	{
		if (this.neighbours == null)
		{
			this.neighbours = new ArrayList<>();
		}

		if (this.neighbours.contains(aNewNeighbour))
		{
			return false;
		}
		this.neighbours.add(aNewNeighbour);
		this.neighbours.sort(new NuclearLinkComparator<Nucleus>());
		return true;
	}


	public boolean addNucleusEvent(final NucleusEvent aEvent)
	{
		if (this.events.containsNucleusEvent(aEvent))
		{
			return false;
		}

		if (aEvent.isTrueNucleus() && getTrueEvent() != null)
		{
			return false;
		}

		this.events.addNucleusEvent(aEvent);
		aEvent.setParent(this);

		return true;
	}


	@Override
	public boolean equals(final Object aNucleus)
	{
		if (aNucleus == null)
		{
			return false;
		}
		if (!Nucleus.class.isAssignableFrom(aNucleus.getClass()))
		{
			return false;
		}
		return getCoordinates().equals(((Nucleus) aNucleus).getCoordinates());
	}


	public NucleiSet2D getEvents()
	{
		return this.events;
	}


	/**
	 * Gets the NuclearLink with the nearest neighbour
	 *
	 * @return The first NuclearLink object of the sorted neighbours list. Note
	 *         that this is the nearest 'known' neighbour! Can return null if no
	 *         neighbours have been added yet.
	 */
	public NuclearLink<Nucleus> getNearestNeighbourLink()
	{
		if (this.neighbours != null)
		{
			return this.neighbours.get(0);
		}
		return null;
	}


	/**
	 * Gets the nearest nucleus neighbour
	 *
	 * @return The first Nucleus object of the sorted neighbours list. Note that
	 *         this is the nearest 'known' neighbour! Can return null if no
	 *         neighbours have been added yet.
	 */
	@Override
	public Nucleus getNearestNeighbourNucleus()
	{
		if (this.neighbours != null)
		{
			return getNearestNeighbourLink().getTheOtherEnd(this);
		}
		return null;
	}


	/**
	 * @return the set of Lines to each Delaunay neighbour of this nucleus
	 */
	public List<NuclearLink<Nucleus>> getNeighbours()
	{
		return this.neighbours;
	}


	/**
	 * Return the given of nearest neighbour links for this Nucleus. If the
	 * number is greater than the number of neighbours, just return the whole
	 * list.
	 *
	 * @param aNrOfNearest
	 *            The number of neighbour links returned, from nearest to
	 *            farthest.
	 *
	 * @return the set of Lines to each Delaunay neighbour of this nucleus
	 */
	public List<NuclearLink<Nucleus>> getNeighbours(final int aNrOfNearest)
	{
		if (aNrOfNearest <= this.neighbours.size())
		{
			return this.neighbours.subList(0, aNrOfNearest);
		}
		else
		{
			return this.neighbours;
		}
	}


	public NucleusEvent getTrueEvent()
	{
		for (final NucleusEvent event : this.events)
		{
			if (event.isTrueNucleus())
			{
				return event;
			}
		}
		return null;
	}


	@Override
	public int hashCode()
	{
		return getCoordinates().hashCode() * 17;
	}


	public void setCoordinates(final Coordinates aCoordinates)
	{
		this.coordinates = aCoordinates;
	}


	/**
	 * @param aNeighbours
	 *            the neighbour Lines to set
	 */
	public void setNeighbours(final List<NuclearLink<Nucleus>> aNeighbours)
	{
		this.neighbours = aNeighbours;
		this.neighbours.sort(new NuclearLinkComparator<Nucleus>());
	}
}
