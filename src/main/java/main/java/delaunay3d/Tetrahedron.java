/**
 * Copyright (C) 2009 Hal Hildebrand. All rights reserved.
 *
 * This file is part of the 3D Incremental Voronoi system
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package delaunay3d;

import static delaunay3d.V.*;
import static delaunay3d.utils.math.Geometry.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import data.Coordinates;
import delaunay3d.utils.collections.IdentitySet;

/**
 * An oriented, delaunay tetrahedral cell. The vertices of the tetrahedron are A, B, C and D. The vertices {A, B, C} are positively oriented with respect to the fourth vertex D.
 * <p>
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 *
 */
public class Tetrahedron implements Iterable<OrientedFace>
{

	/**
	 * Represents the oriented face opposite vertex C
	 *
	 * @author hhildebrand
	 *
	 */
	private class FaceADB extends OrientedFace
	{

		@Override
		public Tetrahedron getAdjacent()
		{
			return Tetrahedron.this.nC;
		}


		@Override
		public Vertex[] getEdge(final Vertex v)
		{
			switch (ordinalOf(v))
			{
			case A:
			{
				return new Vertex[] { Tetrahedron.this.d, Tetrahedron.this.b };
			}
			case D:
			{
				return new Vertex[] { Tetrahedron.this.b, Tetrahedron.this.a };
			}
			case B:
			{
				return new Vertex[] { Tetrahedron.this.a, Tetrahedron.this.d };
			}
			default:
				throw new IllegalArgumentException("Invalid vertex ordinal");
			}
		}


		@Override
		public Tetrahedron getIncident()
		{
			return Tetrahedron.this;
		}


		@Override
		public Vertex getIncidentVertex()
		{
			return Tetrahedron.this.c;
		}


		@Override
		public Vertex getVertex(final int v)
		{
			switch (v)
			{
			case 0:
				return Tetrahedron.this.a;
			case 1:
				return Tetrahedron.this.d;
			case 2:
				return Tetrahedron.this.b;
			default:
				throw new IllegalArgumentException("Invalid vertex index: " + v);
			}
		}


		@Override
		public boolean includes(final Vertex v)
		{
			if (Tetrahedron.this.a == v)
			{
				return true;
			}
			if (Tetrahedron.this.d == v)
			{
				return true;
			}
			if (Tetrahedron.this.b == v)
			{
				return true;
			}
			return false;
		}


		@Override
		public int indexOf(final Vertex v)
		{
			if (v == Tetrahedron.this.a)
			{
				return 0;
			}
			if (v == Tetrahedron.this.d)
			{
				return 1;
			}
			if (v == Tetrahedron.this.b)
			{
				return 2;
			}
			throw new IllegalArgumentException("Vertex is not on face: " + v);
		}


		@Override
		public boolean isConvex(final int vertex)
		{
			final Vertex adjacentVertex = getAdjacentVertex();
			if (adjacentVertex == null)
			{
				return false;
			}
			switch (vertex)
			{
			case 0:
				return adjacentVertex.orientation(Tetrahedron.this.c, Tetrahedron.this.d, Tetrahedron.this.b) == -1;
			case 1:
				return adjacentVertex.orientation(Tetrahedron.this.a, Tetrahedron.this.c, Tetrahedron.this.b) == -1;
			case 2:
				return adjacentVertex.orientation(Tetrahedron.this.a, Tetrahedron.this.d, Tetrahedron.this.c) == -1;
			default:
				throw new IllegalArgumentException("Invalid vertex index: " + vertex);
			}
		}


		@Override
		public boolean isReflex(final int vertex)
		{
			final Vertex adjacentVertex = getAdjacentVertex();
			if (adjacentVertex == null)
			{
				return false;
			}
			switch (vertex)
			{
			case 0:
				return adjacentVertex.orientation(Tetrahedron.this.c, Tetrahedron.this.d, Tetrahedron.this.b) == 1;
			case 1:
				return adjacentVertex.orientation(Tetrahedron.this.a, Tetrahedron.this.c, Tetrahedron.this.b) == 1;
			case 2:
				return adjacentVertex.orientation(Tetrahedron.this.a, Tetrahedron.this.d, Tetrahedron.this.c) == 1;
			default:
				throw new IllegalArgumentException("Invalid vertex index: " + vertex);
			}
		}


		@Override
		public int orientationOf(final Vertex query)
		{
			return orientationWrtADB(query);
		}


		@Override
		public String toString()
		{
			return "Face ADB";
		}

	}

	/**
	 * Represents the oriented face opposite of vertex D
	 *
	 * @author hhildebrand
	 *
	 */
	private class FaceBCA extends OrientedFace
	{

		@Override
		public Tetrahedron getAdjacent()
		{
			return Tetrahedron.this.nD;
		}


		@Override
		public Vertex[] getEdge(final Vertex v)
		{
			switch (ordinalOf(v))
			{
			case B:
			{
				return new Vertex[] { Tetrahedron.this.c, Tetrahedron.this.a };
			}
			case C:
			{
				return new Vertex[] { Tetrahedron.this.a, Tetrahedron.this.b };
			}
			case A:
			{
				return new Vertex[] { Tetrahedron.this.b, Tetrahedron.this.c };
			}
			default:
				throw new IllegalArgumentException("Invalid vertex ordinal");
			}
		}


		@Override
		public Tetrahedron getIncident()
		{
			return Tetrahedron.this;
		}


		@Override
		public Vertex getIncidentVertex()
		{
			return Tetrahedron.this.d;
		}


		@Override
		public Vertex getVertex(final int v)
		{
			switch (v)
			{
			case 0:
				return Tetrahedron.this.b;
			case 1:
				return Tetrahedron.this.c;
			case 2:
				return Tetrahedron.this.a;
			default:
				throw new IllegalArgumentException("Invalid vertex index: " + v);
			}
		}


		@Override
		public boolean includes(final Vertex v)
		{
			if (Tetrahedron.this.b == v)
			{
				return true;
			}
			if (Tetrahedron.this.c == v)
			{
				return true;
			}
			if (Tetrahedron.this.a == v)
			{
				return true;
			}
			return false;
		}


		@Override
		public int indexOf(final Vertex v)
		{
			if (v == Tetrahedron.this.b)
			{
				return 0;
			}
			if (v == Tetrahedron.this.c)
			{
				return 1;
			}
			if (v == Tetrahedron.this.a)
			{
				return 2;
			}
			throw new IllegalArgumentException("Vertex is not on face: " + v);
		}


		@Override
		public boolean isConvex(final int vertex)
		{
			final Vertex adjacentVertex = getAdjacentVertex();
			if (adjacentVertex == null)
			{
				return false;
			}

			switch (vertex)
			{
			case 0:
				return adjacentVertex.orientation(Tetrahedron.this.d, Tetrahedron.this.c, Tetrahedron.this.a) == -1;
			case 1:
				return adjacentVertex.orientation(Tetrahedron.this.b, Tetrahedron.this.d, Tetrahedron.this.a) == -1;
			case 2:
				return adjacentVertex.orientation(Tetrahedron.this.b, Tetrahedron.this.c, Tetrahedron.this.d) == -1;
			default:
				throw new IllegalArgumentException("Invalid vertex index: " + vertex);
			}
		}


		@Override
		public boolean isReflex(final int vertex)
		{
			final Vertex adjacentVertex = getAdjacentVertex();
			if (adjacentVertex == null)
			{
				return false;
			}

			switch (vertex)
			{
			case 0:
				return adjacentVertex.orientation(Tetrahedron.this.d, Tetrahedron.this.c, Tetrahedron.this.a) == 1;
			case 1:
				return adjacentVertex.orientation(Tetrahedron.this.b, Tetrahedron.this.d, Tetrahedron.this.a) == 1;
			case 2:
				return adjacentVertex.orientation(Tetrahedron.this.b, Tetrahedron.this.c, Tetrahedron.this.d) == 1;
			default:
				throw new IllegalArgumentException("Invalid vertex index: " + vertex);
			}
		}


		@Override
		public int orientationOf(final Vertex query)
		{
			return orientationWrtBCA(query);
		}


		@Override
		public String toString()
		{
			return "Face BCA";
		}

	}

	/**
	 * Represents the oriented face opposite of vertex A
	 *
	 * @author hhildebrand
	 *
	 */
	private class FaceCBD extends OrientedFace
	{

		@Override
		public Tetrahedron getAdjacent()
		{
			return Tetrahedron.this.nA;
		}


		@Override
		public Vertex[] getEdge(final Vertex v)
		{
			switch (ordinalOf(v))
			{
			case C:
			{
				return new Vertex[] { Tetrahedron.this.b, Tetrahedron.this.d };
			}
			case B:
			{
				return new Vertex[] { Tetrahedron.this.d, Tetrahedron.this.c };
			}
			case D:
			{
				return new Vertex[] { Tetrahedron.this.c, Tetrahedron.this.b };
			}
			default:
				throw new IllegalArgumentException("Invalid vertex ordinal");
			}
		}


		@Override
		public Tetrahedron getIncident()
		{
			return Tetrahedron.this;
		}


		@Override
		public Vertex getIncidentVertex()
		{
			return Tetrahedron.this.a;
		}


		@Override
		public Vertex getVertex(final int v)
		{
			switch (v)
			{
			case 0:
				return Tetrahedron.this.c;
			case 1:
				return Tetrahedron.this.b;
			case 2:
				return Tetrahedron.this.d;
			default:
				throw new IllegalArgumentException("Invalid vertex index: " + v);
			}
		}


		@Override
		public boolean includes(final Vertex v)
		{
			if (Tetrahedron.this.c == v)
			{
				return true;
			}
			if (Tetrahedron.this.b == v)
			{
				return true;
			}
			if (Tetrahedron.this.d == v)
			{
				return true;
			}
			return false;
		}


		@Override
		public int indexOf(final Vertex v)
		{
			if (v == Tetrahedron.this.c)
			{
				return 0;
			}
			if (v == Tetrahedron.this.b)
			{
				return 1;
			}
			if (v == Tetrahedron.this.d)
			{
				return 2;
			}
			throw new IllegalArgumentException("Vertex is not on face: " + v);
		}


		@Override
		public boolean isConvex(final int vertex)
		{
			final Vertex adjacentVertex = getAdjacentVertex();
			if (adjacentVertex == null)
			{
				return false;
			}
			switch (vertex)
			{
			case 0:
				return adjacentVertex.orientation(Tetrahedron.this.a, Tetrahedron.this.b, Tetrahedron.this.d) == -1;
			case 1:
				return adjacentVertex.orientation(Tetrahedron.this.c, Tetrahedron.this.a, Tetrahedron.this.d) == -1;
			case 2:
				return adjacentVertex.orientation(Tetrahedron.this.c, Tetrahedron.this.b, Tetrahedron.this.a) == -1;
			default:
				throw new IllegalArgumentException("Invalid vertex index: " + vertex);
			}
		}


		@Override
		public boolean isReflex(final int vertex)
		{
			final Vertex adjacentVertex = getAdjacentVertex();
			if (adjacentVertex == null)
			{
				return false;
			}
			switch (vertex)
			{
			case 0:
				return adjacentVertex.orientation(Tetrahedron.this.a, Tetrahedron.this.b, Tetrahedron.this.d) == 1;
			case 1:
				return adjacentVertex.orientation(Tetrahedron.this.c, Tetrahedron.this.a, Tetrahedron.this.d) == 1;
			case 2:
				return adjacentVertex.orientation(Tetrahedron.this.c, Tetrahedron.this.b, Tetrahedron.this.a) == 1;
			default:
				throw new IllegalArgumentException("Invalid vertex index: " + vertex);
			}
		}


		@Override
		public int orientationOf(final Vertex query)
		{
			return orientationWrtCBD(query);
		}


		@Override
		public String toString()
		{
			return "Face CBD";
		}

	}

	/**
	 * Represents the oriented face opposite of vertex B
	 *
	 * @author hhildebrand
	 *
	 */
	private class FaceDAC extends OrientedFace
	{

		@Override
		public Tetrahedron getAdjacent()
		{
			return Tetrahedron.this.nB;
		}


		@Override
		public Vertex[] getEdge(final Vertex v)
		{
			switch (ordinalOf(v))
			{
			case D:
			{
				return new Vertex[] { Tetrahedron.this.a, Tetrahedron.this.c };
			}
			case A:
			{
				return new Vertex[] { Tetrahedron.this.c, Tetrahedron.this.d };
			}
			case C:
			{
				return new Vertex[] { Tetrahedron.this.d, Tetrahedron.this.a };
			}
			default:
				throw new IllegalArgumentException("Invalid vertex ordinal");
			}
		}


		@Override
		public Tetrahedron getIncident()
		{
			return Tetrahedron.this;
		}


		@Override
		public Vertex getIncidentVertex()
		{
			return Tetrahedron.this.b;
		}


		@Override
		public Vertex getVertex(final int v)
		{
			switch (v)
			{
			case 0:
				return Tetrahedron.this.d;
			case 1:
				return Tetrahedron.this.a;
			case 2:
				return Tetrahedron.this.c;
			default:
				throw new IllegalArgumentException("Invalid vertex index: " + v);
			}
		}


		@Override
		public boolean includes(final Vertex v)
		{
			if (Tetrahedron.this.d == v)
			{
				return true;
			}
			if (Tetrahedron.this.a == v)
			{
				return true;
			}
			if (Tetrahedron.this.c == v)
			{
				return true;
			}
			return false;
		}


		@Override
		public int indexOf(final Vertex v)
		{
			if (v == Tetrahedron.this.d)
			{
				return 0;
			}
			if (v == Tetrahedron.this.a)
			{
				return 1;
			}
			if (v == Tetrahedron.this.c)
			{
				return 2;
			}
			throw new IllegalArgumentException("Vertex is not on face: " + v);
		}


		@Override
		public boolean isConvex(final int vertex)
		{
			final Vertex adjacentVertex = getAdjacentVertex();
			if (adjacentVertex == null)
			{
				return false;
			}
			switch (vertex)
			{
			case 0:
				return adjacentVertex.orientation(Tetrahedron.this.b, Tetrahedron.this.a, Tetrahedron.this.c) == -1;
			case 1:
				return adjacentVertex.orientation(Tetrahedron.this.d, Tetrahedron.this.b, Tetrahedron.this.c) == -1;
			case 2:
				return adjacentVertex.orientation(Tetrahedron.this.d, Tetrahedron.this.a, Tetrahedron.this.b) == -1;
			default:
				throw new IllegalArgumentException("Invalid vertex index: " + vertex);
			}
		}


		@Override
		public boolean isReflex(final int vertex)
		{
			final Vertex adjacentVertex = getAdjacentVertex();
			if (adjacentVertex == null)
			{
				return false;
			}
			switch (vertex)
			{
			case 0:
				return adjacentVertex.orientation(Tetrahedron.this.b, Tetrahedron.this.a, Tetrahedron.this.c) == 1;
			case 1:
				return adjacentVertex.orientation(Tetrahedron.this.d, Tetrahedron.this.b, Tetrahedron.this.c) == 1;
			case 2:
				return adjacentVertex.orientation(Tetrahedron.this.d, Tetrahedron.this.a, Tetrahedron.this.b) == 1;
			default:
				throw new IllegalArgumentException("Invalid vertex index: " + vertex);
			}
		}


		@Override
		public int orientationOf(final Vertex query)
		{
			return orientationWrtDAC(query);
		}


		@Override
		public String toString()
		{
			return "Face DAC";
		}

	}

	/**
	 * Matrix used to determine the next neighbor in a voronoi face traversal
	 */
	private static final V[][][] VORONOI_FACE_NEXT = { { null, { null, null, D, C }, { null, D, null, B }, { null, C, B, null } },
			{ { null, null, D, C }, null, { D, null, null, A }, { C, null, A, null } }, { { null, D, null, B }, { D, null, null, A }, null, { B, A, null, null } },
			{ { null, C, B, null }, { C, null, A, null }, { B, A, null, null }, null } };

	/**
	 * Matrix used to determine the origin neighbor in a vororoni face traversal
	 */
	private static final V[][] VORONOI_FACE_ORIGIN = { { null, C, D, B }, { C, null, D, A }, { D, A, null, B }, { B, C, A, null } };

	/**
	 * Vertex A
	 */
	private Vertex a;

	/**
	 * Vertx B
	 */
	private Vertex b;

	/**
	 * Vertex C
	 */
	private Vertex c;

	/**
	 * Vertex D
	 */
	private Vertex d;

	/**
	 * The neighboring tetrahedron opposite of vertex A
	 */
	private Tetrahedron nA;

	/**
	 * The neighboring tetrahedron opposite of vertex B
	 */
	private Tetrahedron nB;

	/**
	 * The neighboring tetrahedron opposite of vertex C
	 */
	private Tetrahedron nC;

	/**
	 * The neighboring tetrahedron opposite of vertex D
	 */
	private Tetrahedron nD;


	/**
	 * Construct a tetrahedron from the four vertices
	 *
	 * @param x
	 * @param y
	 * @param z
	 * @param w
	 */
	public Tetrahedron(final Vertex x, final Vertex y, final Vertex z, final Vertex w)
	{
		assert x != null & y != null & z != null & w != null;

		this.a = x;
		this.b = y;
		this.c = z;
		this.d = w;

		this.a.setAdjacent(this);
		this.b.setAdjacent(this);
		this.c.setAdjacent(this);
		this.d.setAdjacent(this);
	}


	/**
	 * Construct a tetrahedron from the array of four vertices
	 *
	 * @param vertices
	 */
	public Tetrahedron(final Vertex[] vertices)
	{
		this(vertices[0], vertices[1], vertices[2], vertices[3]);
		assert vertices.length == 4;
	}


	/**
	 * Add the four faces defined by the tetrahedron to the list of faces
	 *
	 * @param faces
	 */
	public void addFaces(final List<Vertex[]> faces)
	{
		faces.add(new Vertex[] { this.a, this.d, this.b });
		faces.add(new Vertex[] { this.b, this.c, this.a });
		faces.add(new Vertex[] { this.c, this.b, this.d });
		faces.add(new Vertex[] { this.d, this.a, this.c });
	}


	/**
	 * Add the four faces defined by the tetrahedron to the list of faces
	 *
	 * @param faces
	 */
	public void addFacesCoordinates(final List<Coordinates[]> faces)
	{
		faces.add(new Coordinates[] { this.a.asCoordinates(), this.d.asCoordinates(), this.b.asCoordinates() });
		faces.add(new Coordinates[] { this.b.asCoordinates(), this.c.asCoordinates(), this.a.asCoordinates() });
		faces.add(new Coordinates[] { this.c.asCoordinates(), this.b.asCoordinates(), this.d.asCoordinates() });
		faces.add(new Coordinates[] { this.d.asCoordinates(), this.a.asCoordinates(), this.c.asCoordinates() });
	}


	/**
	 * Clean up the pointers
	 */
	public void delete()
	{
		this.a.deleteAdjacent();
		this.b.deleteAdjacent();
		this.c.deleteAdjacent();
		this.d.deleteAdjacent();
		this.nA = this.nB = this.nC = this.nD = null;
		this.a = this.b = this.c = this.d = null;
	}


	/**
	 *
	 * Perform the 1 -> 4 bistellar flip. This produces 4 new tetrahedron from the original tetrahdron, by inserting the new point in the interior of the receiver tetrahedron. The star set of the newly inserted point is pushed onto the supplied
	 * stack.
	 * <p>
	 *
	 * @param n
	 *            - the inserted point
	 * @param ears
	 *            - the stack of oriented faces that make up the ears of the inserted point
	 * @return one of the four new tetrahedra
	 */
	public Tetrahedron flip1to4(final Vertex n, final List<OrientedFace> ears)
	{
		final Tetrahedron t0 = new Tetrahedron(this.a, this.b, this.c, n);
		final Tetrahedron t1 = new Tetrahedron(this.a, this.d, this.b, n);
		final Tetrahedron t2 = new Tetrahedron(this.a, this.c, this.d, n);
		final Tetrahedron t3 = new Tetrahedron(this.b, this.d, this.c, n);

		t0.setNeighborA(t3);
		t0.setNeighborB(t2);
		t0.setNeighborC(t1);

		t1.setNeighborA(t3);
		t1.setNeighborB(t0);
		t1.setNeighborC(t2);

		t2.setNeighborA(t3);
		t2.setNeighborB(t1);
		t2.setNeighborC(t0);

		t3.setNeighborA(t2);
		t3.setNeighborB(t0);
		t3.setNeighborC(t1);

		patch(D, t0, D);
		patch(C, t1, D);
		patch(B, t2, D);
		patch(A, t3, D);

		delete();

		OrientedFace newFace = t0.getFace(D);
		if (newFace.hasAdjacent())
		{
			ears.add(newFace);
		}
		newFace = t1.getFace(D);
		if (newFace.hasAdjacent())
		{
			ears.add(newFace);
		}
		newFace = t2.getFace(D);
		if (newFace.hasAdjacent())
		{
			ears.add(newFace);
		}
		newFace = t3.getFace(D);
		if (newFace.hasAdjacent())
		{
			ears.add(newFace);
		}
		return t1;
	}


	Vertex getA()
	{
		return this.a;
	}


	Vertex getB()
	{
		return this.b;
	}


	Vertex getC()
	{
		return this.c;
	}


	Vertex getD()
	{
		return this.d;
	}


	/**
	 * Answer the oriented face of the tetrahedron
	 * <p>
	 *
	 * @param v
	 *            - the vertex opposite the face
	 * @return the OrientedFace
	 */
	public OrientedFace getFace(final V v)
	{
		// return new OrientedFace(this, v);
		switch (v)
		{
		case A:
			return new FaceCBD();
		case B:
			return new FaceDAC();
		case C:
			return new FaceADB();
		case D:
			return new FaceBCA();
		default:
			throw new IllegalArgumentException("Invalid vertex: " + v);
		}
	}


	/**
	 * Answer the oriented face opposite the vertex
	 *
	 * @param v
	 * @return
	 */
	public OrientedFace getFace(final Vertex v)
	{
		return getFace(ordinalOf(v));
	}


	/**
	 * Answer the neighbor that is adjacent to the face opposite of the vertex
	 * <p>
	 *
	 * @param v
	 *            - the opposing vertex defining the face
	 * @return the neighboring tetrahedron, or null if none.
	 */
	public Tetrahedron getNeighbor(final V v)
	{
		switch (v)
		{
		case A:
			return this.nA;
		case B:
			return this.nB;
		case C:
			return this.nC;
		case D:
			return this.nD;
		default:
			throw new IllegalArgumentException("Invalid opposing vertex: " + v);
		}
	}


	/**
	 * Answer the neighbor that is adjacent to the face opposite of the vertex
	 * <p>
	 *
	 * @param vertex
	 * @return
	 */
	public Tetrahedron getNeighbor(final Vertex vertex)
	{
		return getNeighbor(ordinalOf(vertex));
	}


	/**
	 * Answer the vertex of the tetrahedron
	 *
	 * @param v
	 *            the vertex
	 * @return the vertex
	 */
	public Vertex getVertex(final V v)
	{
		switch (v)
		{
		case A:
			return this.a;
		case B:
			return this.b;
		case C:
			return this.c;
		case D:
			return this.d;
		default:
			throw new IllegalStateException("No such point");
		}
	}


	/**
	 * Answer the four vertices that define the tetrahedron
	 *
	 * @return
	 */
	public Vertex[] getVertices()
	{
		return new Vertex[] { this.a, this.b, this.c, this.d };
	}


	public boolean includes(final Vertex query)
	{
		return this.a == query || this.b == query || this.c == query || this.d == query;
	}


	/**
	 * Answer true if the query point is contained in the circumsphere of the tetrahedron
	 *
	 * @param query
	 * @return
	 */
	public boolean inSphere(final Vertex query)
	{
		return query.inSphere(this.a, this.b, this.c, this.d) > 0;
	}


	boolean isDeleted()
	{
		return this.a == null;
	}


	/**
	 * Answer the iterator over the faces of the tetrahedron
	 * <p>
	 *
	 * @return the iterator of the faces, in the order of the index their opposite vertex
	 */
	@Override
	public Iterator<OrientedFace> iterator()
	{
		return new Iterator<OrientedFace>()
		{
			OrientedFace[] faces = { getFace(A), getFace(B), getFace(C), getFace(D) };
			int i = 0;


			@Override
			public boolean hasNext()
			{
				return this.i < 4;
			}


			@Override
			public OrientedFace next()
			{
				if (this.i < this.faces.length - 1)
				{
					return this.faces[this.i++];
				}
				else
				{
					throw new NoSuchElementException();
				}
			}


			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}


	/**
	 * Answer the canonical ordinal of the opposite vertex of the neighboring tetrahedron
	 *
	 * @param neighbor
	 * @return
	 */
	public V ordinalOf(final Tetrahedron neighbor)
	{
		if (neighbor == null)
		{
			return null;
		}
		if (this.nA == neighbor)
		{
			return A;
		}
		if (this.nB == neighbor)
		{
			return B;
		}
		if (this.nC == neighbor)
		{
			return C;
		}
		if (this.nD == neighbor)
		{
			return D;
		}
		throw new IllegalArgumentException("Not a neighbor: " + neighbor);
	}


	/**
	 * Answer the vertex indicator of the the point
	 *
	 * @param v
	 *            - the vertex
	 * @return the indicator of this vertex or null if not a vertex of this tetrahedron or the supplied vertex is null
	 */
	public V ordinalOf(final Vertex v)
	{
		if (v == null)
		{
			return null;
		}
		if (v == this.a)
		{
			return A;
		}
		else if (v == this.b)
		{
			return B;
		}
		else if (v == this.c)
		{
			return C;
		}
		else if (v == this.d)
		{
			return D;
		}
		else
		{
			return null;
		}
	}


	/**
	 * Answer 1 if the query point is positively oriented with respect to the face opposite the vertex, -1 if negatively oriented, 0 if the query point is coplanar to the face
	 *
	 * @param face
	 * @param query
	 * @return
	 */
	public int orientationWrt(final V face, final Vertex query)
	{
		switch (face)
		{
		case A:
			return orientationWrtCBD(query);
		case B:
			return orientationWrtDAC(query);
		case C:
			return orientationWrtADB(query);
		case D:
			return orientationWrtBCA(query);
		default:
			throw new IllegalArgumentException("Invalid face: " + face);
		}
	}


	/**
	 * Answer 1 if the query point is positively oriented with respect to the face ADB, -1 if negatively oriented, 0 if the query point is coplanar to the face
	 *
	 * @param query
	 * @return
	 */
	public int orientationWrtADB(final Vertex query)
	{
		return query.orientation(this.a, this.d, this.b);
	}


	/**
	 * Answer 1 if the query point is positively oriented with respect to the face BCA, -1 if negatively oriented, 0 if the query point is coplanar to the face
	 *
	 * @param query
	 * @return
	 */
	public int orientationWrtBCA(final Vertex query)
	{
		return query.orientation(this.b, this.c, this.a);
	}


	/**
	 * Answer 1 if the query point is positively oriented with respect to the face CBD, -1 if negatively oriented, 0 if the query point is coplanar to the face
	 *
	 * @param query
	 * @return
	 */
	public int orientationWrtCBD(final Vertex query)
	{
		return query.orientation(this.c, this.b, this.d);
	}


	/**
	 * Answer 1 if the query point is positively oriented with respect to the face DAC, -1 if negatively oriented, 0 if the query point is coplanar to the face
	 *
	 * @param query
	 * @return
	 */
	public int orientationWrtDAC(final Vertex query)
	{
		return query.orientation(this.d, this.a, this.c);
	}


	/**
	 * Patch the new tetrahedron created by a flip of the receiver by seting the neighbor to the value in the receiver
	 * <p>
	 *
	 * @param vOld
	 *            - the opposing vertex the neighboring tetrahedron in the receiver
	 * @param n
	 *            - the new tetrahedron to patch
	 * @param vNew
	 *            - the opposing vertex of the neighbor to assign in the new tetrahedron
	 */
	void patch(final V vOld, final Tetrahedron n, final V vNew)
	{
		final Tetrahedron neighbor = getNeighbor(vOld);
		if (neighbor != null)
		{
			neighbor.setNeighbor(neighbor.ordinalOf(this), n);
			n.setNeighbor(vNew, neighbor);
		}
	}


	/**
	 * Patch the new tetrahedron created by a flip of the receiver by seting the neighbor to the value in the receiver
	 * <p>
	 *
	 * @param old
	 * @param n
	 * @param vNew
	 */
	public void patch(final Vertex old, final Tetrahedron n, final V vNew)
	{
		patch(ordinalOf(old), n, vNew);
	}


	void setNeighbor(final V v, final Tetrahedron n)
	{
		switch (v)
		{
		case A:
			this.nA = n;
			break;
		case B:
			this.nB = n;
			break;
		case C:
			this.nC = n;
			break;
		case D:
			this.nD = n;
			break;
		default:
			throw new IllegalArgumentException("Invalid vertex: " + v);
		}
	}


	void setNeighborA(final Tetrahedron t)
	{
		this.nA = t;
	}


	void setNeighborB(final Tetrahedron t)
	{
		this.nB = t;
	}


	void setNeighborC(final Tetrahedron t)
	{
		this.nC = t;
	}


	void setNeighborD(final Tetrahedron t)
	{
		this.nD = t;
	}


	@Override
	public String toString()
	{
		final StringBuffer buf = new StringBuffer();
		buf.append("Tetrahedron [");
		if (isDeleted())
		{
			buf.append("DELETED]");
			return buf.toString();
		}
		for (final Vertex v : getVertices())
		{
			buf.append(v);
			buf.append(", ");
		}
		buf.append(']');
		return buf.toString();
	}


	/**
	 * Traverse all the tetradrons in a tetrahedralization, filling in the set of all visited tetrahedron and the vertices defining them
	 * <p>
	 *
	 * @param visited
	 *            - the set of visited tetrahedrons
	 * @param vertices
	 *            - the set of visited vertices
	 */
	public void traverse(final Set<Tetrahedron> visited, final Set<Vertex> vertices)
	{
		if (visited.add(this))
		{
			vertices.add(this.a);
			vertices.add(this.b);
			vertices.add(this.c);
			vertices.add(this.d);
			if (this.nA != null)
			{
				this.nA.traverse(visited, vertices);
			}
			if (this.nB != null)
			{
				this.nB.traverse(visited, vertices);
			}
			if (this.nC != null)
			{
				this.nC.traverse(visited, vertices);
			}
			if (this.nD != null)
			{
				this.nD.traverse(visited, vertices);
			}
		}
	}


	/**
	 * Traverse the points which define the voronoi face defined by the dual of the line segement defined by the center point and the axis. Terminate the traversal if we have returned to the originating tetrahedron.
	 * <p>
	 *
	 * @param origin
	 * @param from
	 * @param vC
	 * @param axis
	 * @param face
	 */
	void traverseVoronoiFace(final Tetrahedron origin, final Tetrahedron from, final Vertex vC, final Vertex axis, final List<Coordinates> face)
	{
		if (origin == this)
		{
			return;
		}
		final double[] center = new double[3];
		centerSphere(this.a.x, this.a.y, this.a.z, this.b.x, this.b.y, this.b.z, this.c.x, this.c.y, this.c.z, this.d.x, this.d.y, this.d.z, center);
		face.add(new Coordinates((float) center[0], (float) center[1], (float) center[2]));
		final V next = VORONOI_FACE_NEXT[ordinalOf(from).ordinal()][ordinalOf(vC).ordinal()][ordinalOf(axis).ordinal()];
		final Tetrahedron t = getNeighbor(next);
		if (t != null)
		{
			t.traverseVoronoiFace(origin, this, vC, axis, face);
		}

	}


	/**
	 * Traverse the points which define the voronoi face defined by the dual of the line segement defined by the center point and the axis.
	 * <p>
	 *
	 * @param vC
	 * @param axis
	 * @param face
	 */
	public void traverseVoronoiFace(final Vertex vC, final Vertex axis, final List<Coordinates[]> faces)
	{
		final ArrayList<Coordinates> face = new ArrayList<>();
		final double[] center = new double[3];
		centerSphere(this.a.x, this.a.y, this.a.z, this.b.x, this.b.y, this.b.z, this.c.x, this.c.y, this.c.z, this.d.x, this.d.y, this.d.z, center);
		face.add(new Coordinates((float) center[0], (float) center[1], (float) center[2]));
		final V v = VORONOI_FACE_ORIGIN[ordinalOf(vC).ordinal()][ordinalOf(axis).ordinal()];
		final Tetrahedron next = getNeighbor(v);
		if (next != null)
		{
			next.traverseVoronoiFace(this, this, vC, axis, face);
		}
		faces.add(face.toArray(new Coordinates[face.size()]));
	}


	/**
	 * Visit the star tetrahedra set of the of the center vertex
	 *
	 * @param vC
	 *            - the center vertex
	 * @param visitor
	 *            - the visitor to invoke for each tetrahedron in the star
	 */
	public void visitStar(final Vertex vC, final StarVisitor visitor)
	{
		final IdentitySet<Tetrahedron> visited = new IdentitySet<>(10);
		visitStar(vC, visitor, visited);
	}


	/**
	 * If the visited set doesn't contain this tetrahedron yet, 'visit' (depends on implementation of StarVisitor) this tetrahedron and all its vertices. Then execute visitStar on all tetrahedrons that are adjacent to this one with the given vertex.
	 * This way, all the tertahedrons of which the given vertex is a part should be visited.
	 *
	 * @param vC
	 *            - the center vertex
	 * @param visitor
	 *            - the visitor to invoke for each tetrahedron in the star
	 * @param visited
	 *            - the set of previously visited tetrahedra
	 */
	public void visitStar(final Vertex vC, final StarVisitor visitor, final IdentitySet<Tetrahedron> visited)
	{
		if (visited.add(this))
		{
			final V vOfC = ordinalOf(vC);
			if (vOfC != null)
			{
				switch (vOfC)
				{
				case A:
					visitor.visit(A, this, this.c, this.b, this.d);
					if (this.nC != null)
					{
						this.nC.visitStar(vC, visitor, visited);
					}
					if (this.nB != null)
					{
						this.nB.visitStar(vC, visitor, visited);
					}
					if (this.nD != null)
					{
						this.nD.visitStar(vC, visitor, visited);
					}
					break;
				case B:
					visitor.visit(B, this, this.d, this.a, this.c);
					if (this.nD != null)
					{
						this.nD.visitStar(vC, visitor, visited);
					}
					if (this.nA != null)
					{
						this.nA.visitStar(vC, visitor, visited);
					}
					if (this.nC != null)
					{
						this.nC.visitStar(vC, visitor, visited);
					}
					break;
				case C:
					visitor.visit(C, this, this.a, this.d, this.b);
					if (this.nA != null)
					{
						this.nA.visitStar(vC, visitor, visited);
					}
					if (this.nD != null)
					{
						this.nD.visitStar(vC, visitor, visited);
					}
					if (this.nB != null)
					{
						this.nB.visitStar(vC, visitor, visited);
					}
					break;
				case D:
					visitor.visit(D, this, this.b, this.c, this.a);
					if (this.nB != null)
					{
						this.nB.visitStar(vC, visitor, visited);
					}
					if (this.nA != null)
					{
						this.nA.visitStar(vC, visitor, visited);
					}
					if (this.nC != null)
					{
						this.nC.visitStar(vC, visitor, visited);
					}
					break;
				default:
					throw new IllegalArgumentException("Invalid center vertex: " + vC);
				}
			}
		}
	}
}
