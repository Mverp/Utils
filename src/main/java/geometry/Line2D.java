package geometry;

public class Line2D
{
	private final double slope;
	private final double offset;


	public Line2D(final double aSlope, final double aOffset)
	{
		this.slope = aSlope;
		this.offset = aOffset;
	}


	public double getOffset()
	{
		return this.offset;
	}


	public double getSlope()
	{
		return this.slope;
	}


	public double getY(final double aX)
	{
		return this.slope * aX + this.offset;
	}
}
