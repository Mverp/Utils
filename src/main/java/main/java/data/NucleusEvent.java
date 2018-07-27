package data;

import java.util.ArrayList;
import java.util.List;

import ij.gui.Line;

public class NucleusEvent extends BaseNucleus
{
	private List<NuclearLink<NucleusEvent>> neighbours;
	private boolean trueNucleus = false;
	private Nucleus parent;


	public NucleusEvent(final Coordinates aPoint)
	{
		super(aPoint);
	}


	public NucleusEvent(final float aXCoordinate, final float aYCoordinate, final int aZCoordinate)
	{
		super(aXCoordinate, aYCoordinate, aZCoordinate);
	}


	/**
	 * Add a new Delaunay neighbour connection.
	 *
	 * @param the
	 *            NuclearLink to the new neighbour
	 */
	public void addNeighbour(final NuclearLink<NucleusEvent> aNewNeighbour)
	{
		if (this.neighbours == null)
		{
			this.neighbours = new ArrayList<>();
		}
		this.neighbours.add(aNewNeighbour);
		this.neighbours.sort(new NuclearLinkComparator<NucleusEvent>());
	}


	@Override
	public boolean equals(final Object aNucleusEvent)
	{
		if (aNucleusEvent == null)
		{
			return false;
		}
		if (!NucleusEvent.class.isAssignableFrom(aNucleusEvent.getClass()))
		{
			return false;
		}
		return getCoordinates().equals(((NucleusEvent) aNucleusEvent).getCoordinates());
	}


	public Line getFromLine(final Line aLine)
	{
		if (aLine.x1 == getXcoordinate() && aLine.y1 == getYcoordinate())
		{
			return aLine;
		}
		else
		{
			return new Line(aLine.x2, aLine.y2, aLine.x1, aLine.y1);
		}
	}


	/**
	 * Gets the NuclearLink with the nearest neighbour
	 *
	 * @return The first NuclearLink object of the sorted neighbours list. Note
	 *         that this is the nearest 'known' neighbour! Can return null if no
	 *         neighbours have been added yet.
	 */
	public NuclearLink<NucleusEvent> getNearestNeighbourLink()
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
	public NucleusEvent getNearestNeighbourNucleus()
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
	public List<NuclearLink<NucleusEvent>> getNeighbours()
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
	public List<NuclearLink<NucleusEvent>> getNeighbours(final int aNrOfNearest)
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


	public Nucleus getParent()
	{
		return this.parent;
	}


	@Override
	public int hashCode()
	{
		return getCoordinates().hashCode() * 17;
	}


	/**
	 * Is this found instance considered a the actual position of the nucleus or
	 * is it an out-of-focus hit?
	 *
	 * @return False, if this is detected to be an out-of-focus instance, true
	 *         otherwise (default).
	 */
	public boolean isTrueNucleus()
	{
		return this.trueNucleus;
	}


	/**
	 * @param aNeighbours
	 *            the neighbour Lines to set
	 */
	public void setNeighbours(final List<NuclearLink<NucleusEvent>> aNeighbours)
	{
		this.neighbours = aNeighbours;
		this.neighbours.sort(new NuclearLinkComparator<NucleusEvent>());
	}


	public void setParent(final Nucleus aParent)
	{
		this.parent = aParent;
	}


	/**
	 * The Nucleus can be set to be a 'false' instance of the actual Nucleus. An
	 * out-of-focus image on this slice.
	 *
	 * @param aTrueNucleus
	 *            Set to false if this is not the actual spot of the Nucleus
	 *            detected
	 */
	public void setTrueNucleus(final boolean aTrueNucleus)
	{
		this.trueNucleus = aTrueNucleus;
	}

}
