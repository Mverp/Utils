package data;

import interfaces.INucleus3D;

public abstract class BaseNucleus implements INucleus3D
{
	public enum PartOf
	{
		NO_PART_OF, DUAL_CLUSTER, SINGLE_CELL, MULTI_CLUSTER, EDGE, STRAND, SPHEROID
	}

	protected Coordinates coordinates;

	private PartOf partOf;

	private double localIntensity;

	private double frstMaximumValue;


	public BaseNucleus(final Coordinates aPoint)
	{
		this.coordinates = aPoint;
	}


	public BaseNucleus(final double aXCoordinate, final double aYCoordinate, final double aZCoordinate)
	{
		this.coordinates = new Coordinates(aXCoordinate, aYCoordinate, aZCoordinate);
	}


	@Override
	public abstract boolean equals(final Object aBaseNucleus);


	public Coordinates getCoordinates()
	{
		return this.coordinates;
	}


	/**
	 * Returns the maximum value found during te run of the FRST algorithm. Note that this can be either just the count of the number of gradient vectors pointing to these Coordinates or the sum of the magnitudes of these gradient vectors. This
	 * depends on how the algorithm has been run.
	 *
	 * @return the frstMaximumValue
	 */
	public double getFrstMaximumValue()
	{
		return this.frstMaximumValue;
	}


	public PartOf getIsPartOf()
	{
		return this.partOf;
	}


	/**
	 * Gets the local intensity value. This can be the intensity on the Coordinates itself or the average intensity of its surroundings, depending on application.
	 *
	 * @return the LocalIntensity
	 */
	public double getLocalIntensity()
	{
		return this.localIntensity;
	}


	/**
	 * Gets the nearest nucleus neighbour
	 *
	 * @return The first Nucleus object of the sorted neighbours list. Note that this is the nearest 'known' neighbour! Can return null if no neighbours have been added yet.
	 */
	public abstract BaseNucleus getNearestNeighbourNucleus();


	/**
	 * @return the X-coordinate
	 */
	@Override
	public double getXcoordinate()
	{
		return this.coordinates.getXcoordinate();
	}


	/**
	 * @return the Y-coordinate
	 */
	@Override
	public double getYcoordinate()
	{
		return this.coordinates.getYcoordinate();
	}


	/**
	 * @return the Z-coordinate
	 */
	public double getZcoordinate()
	{
		return this.coordinates.getZcoordinate();
	}


	@Override
	public abstract int hashCode();


	public boolean isPartOfCluster()
	{
		return this.partOf == PartOf.DUAL_CLUSTER || this.partOf == PartOf.MULTI_CLUSTER;
	}


	public boolean isPartOfDualCluster()
	{
		return this.partOf == PartOf.DUAL_CLUSTER;
	}


	public boolean isPartOfEdge()
	{
		return this.partOf == PartOf.EDGE;
	}


	public boolean isPartOfMultiCluster()
	{
		return this.partOf == PartOf.MULTI_CLUSTER;
	}


	public boolean isPartOfSomething()
	{
		return this.partOf != null;
	}


	public boolean isPartOfSpheroid()
	{
		return this.partOf == PartOf.SPHEROID;
	}


	public boolean isPartOfStrand()
	{
		return this.partOf == PartOf.STRAND;
	}


	public boolean isSingleCell()
	{
		return this.partOf == PartOf.SINGLE_CELL;
	}


	/**
	 * @param frstMaximumValue
	 *            the frstMaximumValue to set
	 */
	public void setFrstMaximumValue(final double frstMaximumValue)
	{
		this.frstMaximumValue = frstMaximumValue;
	}


	public void setIsPartOf(final PartOf aPartOf)
	{
		this.partOf = aPartOf;
	}


	/**
	 * @param localIntensity
	 *            the localIntensity to set
	 */
	public void setLocalIntensity(final double localIntensity)
	{
		this.localIntensity = localIntensity;
	}
}
