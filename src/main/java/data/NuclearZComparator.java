package data;

import java.io.Serializable;
import java.util.Comparator;

public class NuclearZComparator<T extends BaseNucleus> implements Comparator<T>, Serializable
{
	private static final long serialVersionUID = 1L;


	/**
	 * Compare Lines by their length.
	 */
	@Override
	public int compare(final T aNucleus1, final T aNucleus2)
	{
		return new Double(aNucleus1.getZcoordinate()).compareTo(new Double(aNucleus2.getZcoordinate()));
	}

}
