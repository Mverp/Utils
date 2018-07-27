package data.spheroid;

import data.Coordinates;

public class Spheroid
{
	private Coordinates centre;
	private double radius;
	private double zCoefficient;


	public Spheroid(final Coordinates aCoordinates, final double aRadius, final double aZCoefficient)
	{
		this.setCentre(aCoordinates);
		this.radius = aRadius;
		this.zCoefficient = aZCoefficient;
	}


	/**
	 * Get the Coordinates of the center of the Spheroid. This central point determines the position of the Spheroid in the XYZ coordinate set.
	 *
	 * @return The centre Coordinates
	 */
	public Coordinates getCentre()
	{
		return this.centre;
	}


	/**
	 * Calculate the distance from the given point to the center of the Spheroid taking the z-coefficient into account; i.e. the z-distance between the parameter Point and the centre is multiplied by this coefficient to bring it into line with the XY
	 * distances.
	 *
	 * @param aPoint
	 *            The point to which the distance will be measured
	 * @return The distance (in pixel units) between the center of the Spheroid and aPoint
	 */
	public double getDistanceFromPointWithZCoefficient(final Coordinates aPoint)
	{
		return this.centre.correctedDistanceFromPoint(aPoint, this.zCoefficient);
	}


	/**
	 * Get the radius of the Spheroid. The radius is the distance between the center and the edge of the Spheroid.
	 *
	 * @return the Spheroid radius
	 */
	public double getRadius()
	{
		return this.radius;
	}


	/**
	 * Calculate the radius the Spheroid will have when sliced at the given z-coordinate.
	 *
	 * @param aZCoordinate
	 *            The height at which to slice the Spheroid.
	 *
	 * @return The radius of the Spheroid in the z-slice of aZCoordinate.
	 */
	public double getRadiusAtZCoordinate(final double aZCoordinate)
	{
		final double zDist = Math.abs(aZCoordinate - this.centre.getZcoordinate()) * this.zCoefficient;
		return Math.sqrt(this.radius * this.radius - zDist * zDist);
	}


	/**
	 * Calculate the distance from the given point to the edge of the Spheroid taking the z-coefficient into account; i.e. the z-distance between the parameter Point and the edge is multiplied by this coefficient to bring it into line with the XY
	 * distances.
	 *
	 * @param aPoint
	 *            The point to which the distance will be measured
	 * @return The distance (in pixel units) between the center of the Spheroid and aPoint
	 */
	public double getRadiusDistanceFromPointWithZCoefficient(final Coordinates aPoint)
	{
		return this.centre.correctedDistanceFromPoint(aPoint, this.zCoefficient) - this.radius;
	}


	/**
	 * Get the z-coefficient of the Spheroid. The z-coefficient determines the factor with which distances in the z-direction are multiplied where this Spheroid is concerned.
	 *
	 * @return The z-coefficient of this Spheroid. Use it to determine distances when compared to the Spheroid.
	 */
	public double getZCoefficient()
	{
		return this.zCoefficient;
	}


	/**
	 * Define new central coordinates for this Spheroid. This is the point around which the Spheroid is set up and from which its radius is defined.
	 *
	 * @param aCentre
	 *            the new centre Coordinates for this Spheroid
	 */
	public void setCentre(final Coordinates aCentre)
	{
		this.centre = aCentre;
	}


	/**
	 * Change the z-coefficient of the Spheroid. The z-coefficient determines the factor with which distances in the z-direction are multiplied where this Spheroid is concerned.
	 *
	 * @param aCoefficient
	 */
	public void setCoefficient(final double aCoefficient)
	{
		this.zCoefficient = aCoefficient;
	}


	/**
	 * Change the radius for this Spheroid. Note that the radius should be measured in the XY resolution!
	 *
	 * @param aRadius
	 *            The new radius for this Spheroid
	 */
	public void setRadius(final double aRadius)
	{
		this.radius = aRadius;
	}
}
