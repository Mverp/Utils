package data;

import java.io.Serializable;
import java.util.Comparator;

public class NuclearLinkComparator<T extends BaseNucleus> implements Comparator<NuclearLink<T>>, Serializable
{
	private static final long serialVersionUID = 1L;


	/**
	 * Compare Lines by their length.
	 */
	@Override
	public int compare(final NuclearLink<T> aLink1, final NuclearLink<T> aLink2)
	{
		return new Double(aLink1.getDistance()).compareTo(new Double(aLink2.getDistance()));
	}

}
