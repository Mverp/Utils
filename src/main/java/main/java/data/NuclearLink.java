package data;

import ij.gui.Line;

public class NuclearLink<T extends BaseNucleus>
{
	private final T n1;
	private final T n2;
	private final double distance;
	private double[] profile;
	private double profileWidth;


	/**
	 * Create a NuclearLink between the two given Nuclei. The order of the Nuclei has no significance and no order can be assumed in any of the NuclearLink methods.
	 *
	 * @param aN1
	 *            One Nucleus end of the link
	 * @param aN2
	 *            The other Nucleus end
	 */
	public NuclearLink(final T aN1, final T aN2)
	{
		assert !aN1.equals(aN2) : "Both ends of a nuclear link cannot be the same Nucleus";
		this.n1 = aN1;
		this.n2 = aN2;
		// TODO corrected distance???
		this.distance = aN1.getCoordinates().distanceFromPoint(aN2.getCoordinates());
		this.profile = null;
		this.profileWidth = 0;
	}


	/**
	 * Does this NuclearLink contain the given Nucleus as either end.
	 *
	 * @param aNucleus
	 *            The Nucleus object
	 * @return true if the Nucleus isn't null and matches either the n1 Nucleus or the n2 Nucleus
	 */
	public boolean contains(final T aNucleus)
	{
		return aNucleus != null && (aNucleus.equals(this.n1) || aNucleus.equals(this.n2));
	}


	/**
	 * Two NuclearLinks are equal if their two Nucleus ends are equal, though the order of the ends (n1 vs n2) doesn't matter.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final Object aNuclearLink)
	{
		if (aNuclearLink == null)
		{
			return false;
		}
		if (!NuclearLink.class.isAssignableFrom(aNuclearLink.getClass()))
		{
			return false;
		}

		// We are dealing with an actual NuclearLink. Time to see if they are
		// equal.
		final NuclearLink<T> theOther = (NuclearLink<T>) aNuclearLink;
		return theOther.contains(this.n1) && theOther.contains(this.n2);
	}


	/**
	 * Get the coordinates of both ends of the NuclearLink..
	 *
	 * @return A double array containing {n1.x, n1.y, n2.x, n2.y}.
	 */
	public double[] get2DCoordinates()
	{
		final double[] result = { this.n1.getXcoordinate(), this.n1.getYcoordinate(), this.n2.getXcoordinate(), this.n2.getYcoordinate() };
		return result;
	}


	public Line getAsRoi()
	{
		return new Line(this.n1.getXcoordinate(), this.n1.getYcoordinate(), this.n2.getXcoordinate(), this.n2.getYcoordinate());
	}


	public double getDistance()
	{
		return this.distance;
	}


	public Coordinates getEndCoordinates()
	{
		return this.n2.getCoordinates();
	}


	public double[] getProfile()
	{
		return this.profile;
	}


	public double getProfileWidth()
	{
		return this.profileWidth;
	}


	public Coordinates getStartCoordinates()
	{
		return this.n1.getCoordinates();
	}


	public T getTheOtherEnd(final T aOrigin)
	{
		assert aOrigin != null : "Always give an origin when asking for the other end of a nuclear link.";
		if (aOrigin.equals(this.n1))
		{
			return this.n2;
		}
		return this.n1;
	}


	@Override
	public int hashCode()
	{
		return this.n1.hashCode() ^ this.n2.hashCode();
	}


	public void setProfile(final double[] aProfile, final double aWidth)
	{
		this.profile = aProfile;
		this.profileWidth = aWidth;
	}
}
