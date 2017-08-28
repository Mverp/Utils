package data;

/**
 * The addition of a value to a set of Coordinates.
 *
 * @author Merijn van Erp
 *
 */
public class PointValue extends Coordinates implements Comparable<PointValue>
{
	private final float value;


	/**
	 * The constructor with integers as coordinates and no specific value.
	 *
	 * @param aX
	 *            The x-coordinate
	 * @param aY
	 *            The y-coordinate
	 * @param aZ
	 *            The z-coordinate
	 */
	public PointValue(final double aX, final double aY, final double aZ)
	{
		super(aX, aY, aZ);
		this.value = 0;
	}


	/**
	 * The constructor with integers as coordinates.
	 *
	 * @param aX
	 *            The x-coordinate
	 * @param aY
	 *            The y-coordinate
	 * @param aZ
	 *            The z-coordinate
	 * @param aValue
	 *            The value of the point
	 */
	public PointValue(final int aX, final int aY, final int aZ, final float aValue)
	{
		super(aX, aY, aZ);
		this.value = aValue;
	}


	/**
	 * A comparison of the value attributes.
	 *
	 * @param aObject
	 *            The PointValue to compare with (no type check is done!)
	 * @return A simple float comparison.
	 */
	@Override
	public int compareTo(final PointValue aPointValue)
	{
		return new Float(getValue()).compareTo(new Float(aPointValue.getValue()));
	}


	/**
	 * @return the value
	 */
	public float getValue()
	{
		return this.value;
	}
}
