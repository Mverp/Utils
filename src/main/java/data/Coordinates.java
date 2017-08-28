package data;

/**
 * A point in a 3D space.
 *
 * @author Merijn van Erp
 *
 */
public class Coordinates
{

	public static Coordinates fromString(final String aCoordString)
	{
		final String[] data = aCoordString.split(":");
		final float xCoord = Float.parseFloat(data[0]);
		final float yCoord = Float.parseFloat(data[1]);
		final Coordinates result = new Coordinates(xCoord, yCoord);

		if (data.length > 2)
		{
			result.setZcoordinate(Float.parseFloat(data[2]));
		}

		return result;
	}

	private double xcoordinate;

	private double ycoordinate;

	private double zcoordinate;


	/**
	 * Create the point 0, 0, 0.
	 **/
	public Coordinates()
	{
		this.setXcoordinate(0.0);
		this.setYcoordinate(0.0);
		this.setZcoordinate(0.0);
	}


	public Coordinates(final Coordinates aCoordinate)
	{
		this.setXcoordinate(aCoordinate.getXcoordinate());
		this.setYcoordinate(aCoordinate.getYcoordinate());
		this.setZcoordinate(aCoordinate.getZcoordinate());
	}


	/**
	 * Create the point from an X- and a Y-coordinate.
	 **/
	public Coordinates(final double aXCoordinate, final double aYCoordinate)
	{
		this.setXcoordinate(aXCoordinate);
		this.setYcoordinate(aYCoordinate);
		this.setZcoordinate(0.0);
	}


	/**
	 * Create the point from an X- and a Y-coordinate.
	 **/
	public Coordinates(final double aXCoordinate, final double aYCoordinate, final double aZCoordinate)
	{
		this.setXcoordinate(aXCoordinate);
		this.setYcoordinate(aYCoordinate);
		this.setZcoordinate(aZCoordinate);
	}


	/**
	 * Creates a new Coordinates based on a set of doubles.
	 *
	 * @param aDoubles
	 *            The doubles array. Must be size 2 (for 2D) or 3 (for 3D).
	 */
	public Coordinates(final double[] aDoubles)
	{
		this.xcoordinate = aDoubles[0];
		this.ycoordinate = aDoubles[1];

		if (aDoubles.length == 3)
		{
			this.zcoordinate = (float) aDoubles[2];
		}
		else
		{
			this.zcoordinate = -Float.MIN_VALUE;
		}
	}


	/**
	 * Creates a new Coordinates based on a set of floats.
	 *
	 * @param aFloats
	 *            The floats array. Must be size 2 (for 2D) or 3 (for 3D).
	 */
	public Coordinates(final float[] aFloats)
	{
		this.xcoordinate = aFloats[0];
		this.ycoordinate = aFloats[1];

		if (aFloats.length == 3)
		{
			this.zcoordinate = aFloats[2];
		}
	}


	public Coordinates closestPointOnLine(final double[] aLine)
	{
		final float xCoord = (float) ((getXcoordinate() + (aLine[0] * getYcoordinate()) - (aLine[0] * aLine[1])) / ((aLine[0] * aLine[0]) + 1));
		final float yCoord = (float) (((aLine[0] * getXcoordinate()) + (aLine[0] * aLine[0] * getYcoordinate()) + aLine[1]) / ((aLine[0] * aLine[0]) + 1));

		return new Coordinates(xCoord, yCoord);
	}


	/**
	 * Calculates the Euclidean distance between this Point and the given Point argument.
	 *
	 * @param aTarget
	 *            The point to which the distance will be calculated
	 * @return The distance as a double
	 */
	public double correctedDistanceFromPoint(final Coordinates aTarget, final double aZFactor)
	{
		final double xdiff = getXcoordinate() - aTarget.getXcoordinate();
		final double ydiff = getYcoordinate() - aTarget.getYcoordinate();
		final double zdiff = (getZcoordinate() - aTarget.getZcoordinate()) * aZFactor;

		return Math.sqrt(Math.pow(xdiff, 2) + Math.pow(ydiff, 2) + Math.pow(zdiff, 2));
	}


	/**
	 * Calculates the distance of these coordinates from a line by means of the least square method:
	 *
	 * <p>
	 * distance(ax+by+c=0, (x_0, y_0)) = |ax_0+by_0+c|/sqrt(a^2+b^2).
	 * </p>
	 *
	 * @param aLine
	 *            The line given as a pair of doubles describing it's slope (a) and y-intercept (c), when b = -1.
	 * @return The double of this formula (i.e. the shortest distance between line and coordinates)
	 */
	public double distanceFromLine(final double[] aLine)
	{
		return Math.abs(aLine[0] * getXcoordinate() - getYcoordinate() + aLine[1]) / Math.sqrt((aLine[0] * aLine[0]) + 1);
	}


	/**
	 * Calculates the Euclidean distance between this Point and the given Point argument.
	 *
	 * @param aTarget
	 *            The point to which the distance will be calculated
	 * @return The distance as a double
	 */
	public double distanceFromPoint(final Coordinates aTarget)
	{
		final double xdiff = getXcoordinate() - aTarget.getXcoordinate();
		final double ydiff = getYcoordinate() - aTarget.getYcoordinate();
		final double zdiff = getZcoordinate() - aTarget.getZcoordinate();

		return Math.sqrt(Math.pow(xdiff, 2) + Math.pow(ydiff, 2) + Math.pow(zdiff, 2));
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object aCoordinates)
	{

		if (aCoordinates == null)
		{
			return false;
		}
		if (!Coordinates.class.isAssignableFrom(aCoordinates.getClass()))
		{
			return false;
		}

		return (getXcoordinate() == ((Coordinates) aCoordinates).getXcoordinate()) && (getYcoordinate() == ((Coordinates) aCoordinates).getYcoordinate())
				&& (getZcoordinate() == ((Coordinates) aCoordinates).getZcoordinate());
	}


	public double[] getAsArray()
	{
		final double[] result = { this.xcoordinate, this.ycoordinate, this.zcoordinate };
		return result;
	}


	/**
	 * @return the X-coordinate
	 */
	public double getXcoordinate()
	{
		return this.xcoordinate;
	}


	/**
	 * @return the Y-coordinate
	 */
	public double getYcoordinate()
	{
		return this.ycoordinate;
	}


	/**
	 * @return the Z-coordinate
	 */
	public double getZcoordinate()
	{
		return this.zcoordinate;
	}


	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}


	/**
	 * Deducts the coordinates of the given Coordinates from this one (pairwise). The resulting Coordinates is equal to the vector starting in the given point to this point.
	 *
	 * @param aStartPoint
	 *            The start point of the 'Vector'
	 *
	 * @return The Coordinates produces after the pairwise deduction of coordinates
	 */
	public Coordinates minus(final Coordinates aStartPoint)
	{
		return new Coordinates(this.xcoordinate - aStartPoint.getXcoordinate(), this.ycoordinate - aStartPoint.getYcoordinate(), this.zcoordinate - aStartPoint.getZcoordinate());
	}


	/**
	 * Adds the coordinates of the given Coordinates from this one (pairwise).
	 *
	 * @param aStartPoint
	 *            The other Coordinates
	 *
	 * @return The Coordinates produces after the pairwise addition of coordinates
	 */
	public Coordinates plus(final Coordinates aStartPoint)
	{
		return new Coordinates(this.xcoordinate + aStartPoint.getXcoordinate(), this.ycoordinate + aStartPoint.getYcoordinate(), this.zcoordinate + aStartPoint.getZcoordinate());
	}


	/**
	 * Sets the Coordinates to the values given by a double array.
	 *
	 * @param aDoubles
	 *            The array of new values. Can be used in 2D and 3D.
	 */
	public void setAs(final double[] aDoubles)
	{
		this.xcoordinate = aDoubles[0];
		this.ycoordinate = aDoubles[1];

		if (aDoubles.length == 3)
		{
			this.zcoordinate = aDoubles[2];
		}
	}


	/**
	 * @param aXcoordinate
	 *            the X-coordinate to set
	 */
	public void setXcoordinate(final double aXcoordinate)
	{
		this.xcoordinate = aXcoordinate;
	}


	/**
	 * @param aYcoordinate
	 *            the Y-coordinate to set
	 */
	public void setYcoordinate(final double aYcoordinate)
	{
		this.ycoordinate = aYcoordinate;
	}


	/**
	 * @param aZcoordinate
	 *            the Z-coordinate to set
	 */
	public void setZcoordinate(final double aZcoordinate)
	{
		this.zcoordinate = aZcoordinate;
	}


	@Override
	public String toString()
	{
		return this.xcoordinate + ":" + this.ycoordinate + ":" + this.zcoordinate;
	}
}
