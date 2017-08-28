package data;

public class Spheroid
{
	private Coordinates centre;
	private double radius;


	public Spheroid(final Coordinates aCoordinates, final double aRadius)
	{
		this.setCentre(aCoordinates);
		this.radius = aRadius;
	}


	/**
	 * @return the centre Coordinates
	 */
	public Coordinates getCentre()
	{
		return this.centre;
	}


	/**
	 * @return the Spheroid radius
	 */
	public double getRadius()
	{
		return this.radius;
	}


	/**
	 * @param aCentre
	 *            the new centre Coordinates for this Spheroid
	 */
	public void setCentre(final Coordinates aCentre)
	{
		this.centre = aCentre;
	}


	/**
	 * @param aRadius
	 *            the new radius for this Spheroid
	 */
	public void setRadius(final double aRadius)
	{
		this.radius = aRadius;
	}
}
