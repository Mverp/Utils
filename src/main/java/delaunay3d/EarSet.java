package delaunay3d;

import java.util.LinkedList;
import java.util.Set;

import delaunay3d.utils.collections.OaHashSet;

/**
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 *
 */
public class EarSet implements StarVisitor
{
	private static class Ear
	{
		final OrientedFace face;
		final int hashcode;


		Ear(final OrientedFace face)
		{
			this.face = face;
			int hash = 0;
			for (final Vertex v : face)
			{
				hash ^= v.hashCode();
			}
			this.hashcode = hash;
		}


		@Override
		public boolean equals(final Object obj)
		{
			if (obj instanceof Ear)
			{
				final Ear ear = (Ear) obj;
				if (this.face.getIncident() == ear.face.getIncident() && this.face.getAdjacent() == ear.face.getAdjacent())
				{
					return true;
				}
				if (this.face.getAdjacent() == ear.face.getIncident() && this.face.getIncident() == ear.face.getAdjacent())
				{
					return true;
				}
			}
			return false;
		}


		@Override
		public int hashCode()
		{
			return this.hashcode;
		}
	}

	private final LinkedList<OrientedFace> ears = new LinkedList<>();
	private final Set<Ear> visited = new OaHashSet<>();


	public LinkedList<OrientedFace> getEars()
	{
		return this.ears;
	}


	@Override
	public void visit(final V vertex, final Tetrahedron t, final Vertex x, final Vertex y, final Vertex z)
	{
		OrientedFace face = t.getFace(z);
		if (this.visited.add(new Ear(face)))
		{
			this.ears.add(face);
		}
		face = t.getFace(x);
		if (this.visited.add(new Ear(face)))
		{
			this.ears.add(face);
		}
		face = t.getFace(y);
		if (this.visited.add(new Ear(face)))
		{
			this.ears.add(face);
		}
	}
}
