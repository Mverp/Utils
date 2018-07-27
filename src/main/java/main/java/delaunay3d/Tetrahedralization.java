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

import static delaunay3d.V.A;
import static delaunay3d.V.B;
import static delaunay3d.V.C;
import static delaunay3d.V.D;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import data.Coordinates;
import delaunay3d.utils.collections.IdentitySet;

/**
 * A Delaunay tetrahedralization.
 *
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 *
 */

public class Tetrahedralization
{
	private static class EmptySet<T> extends AbstractSet<T> implements Serializable
	{
		private static final long serialVersionUID = 1L;


		@Override
		public boolean add(final T e)
		{
			return false;
		}


		@Override
		public boolean addAll(final Collection<? extends T> c)
		{
			return false;
		}


		@Override
		public boolean contains(final Object obj)
		{
			return false;
		}


		@Override
		public boolean isEmpty()
		{
			return true;
		}


		@Override
		public Iterator<T> iterator()
		{
			return new Iterator<T>()
			{
				@Override
				public boolean hasNext()
				{
					return false;
				}


				@Override
				public T next()
				{
					throw new NoSuchElementException();
				}


				@Override
				public void remove()
				{
					throw new UnsupportedOperationException();
				}
			};
		}


		@Override
		public int size()
		{
			return 0;
		}
	}

	/**
	 * Cannonical enumeration of the vertex ordinals
	 */
	public final static V[] VERTICES = { A, B, C, D };

	/**
	 * A pre-built table of all the permutations of remaining faces to check in
	 * location.
	 */
	private static final V[][][] ORDER = new V[][][] { { { B, C, D }, { C, B, D }, { C, D, B }, { B, D, C }, { D, B, C }, { D, C, B } },

			{ { A, C, D }, { C, A, D }, { C, D, A }, { A, D, C }, { D, A, C }, { D, C, A } },

			{ { B, A, D }, { A, B, D }, { A, D, B }, { B, D, A }, { D, B, A }, { D, A, B } },

			{ { B, C, A }, { C, B, A }, { C, A, B }, { B, A, C }, { A, B, C }, { A, C, B } } };

	/**
	 * Scale of the universe
	 */
	private static double SCALE = Math.pow(2D, 30D);


	public static Vertex[] getFourCorners()
	{
		final Vertex[] fourCorners = new Vertex[4];
		fourCorners[0] = new Vertex(-1, 1, -1, SCALE);
		fourCorners[1] = new Vertex(1, 1, 1, SCALE);
		fourCorners[2] = new Vertex(1, -1, -1, SCALE);
		fourCorners[3] = new Vertex(-1, -1, 1, SCALE);
		return fourCorners;
	}

	/**
	 * The four cornders of the maximally bounding tetrahedron
	 */
	private final Vertex[] fourCorners;

	/**
	 * The last valid tetrahedron noted
	 */
	private Tetrahedron last;

	/**
	 * A random number generator
	 */
	private final Random random;

	/**
	 * The number of points in this tetrahedralization
	 */
	private int size = 0;


	/**
	 * Construct a new tetrahedralization with the default random number
	 * generator
	 */
	public Tetrahedralization()
	{
		this(new Random());
	}


	/**
	 * Construct a tetrahedralizaion using the supplied random number generator
	 *
	 * @param random
	 */
	public Tetrahedralization(final Random random)
	{
		assert random != null;
		this.fourCorners = getFourCorners();
		this.random = random;
		this.last = new Tetrahedron(this.fourCorners);
	}


	/**
	 * Delete the vertex from the tetrahedralization. This algorithm is the
	 * deleteInSphere algorithm from Ledoux. See "Flipping to Robustly Delete a
	 * Vertex in a Delaunay Tetrahedralization", H. Ledoux, C.M. Gold and G.
	 * Baciu, 2005
	 * <p>
	 *
	 * @param v
	 *            - the vertex to be deleted
	 */
	public void delete(final Vertex v)
	{
		assert v != null;

		final LinkedList<OrientedFace> ears = getEars(v);
		while (v.getOrder() > 4)
		{
			for (int i = 0; i < ears.size();)
			{
				if (ears.get(i).flip(i, ears, v))
				{
					ears.remove(i);
				}
				else
				{
					i++;
				}
			}
		}
		this.last = flip4to1(v);
		this.size--;
	}


	/**
	 * Perform the 4->1 bistellar flip. This flip is the inverse of the 4->1
	 * flip.
	 *
	 * @param n
	 *            - the vertex who's star defines the 4 tetrahedron
	 *
	 * @return the tetrahedron created from the flip
	 */
	protected Tetrahedron flip4to1(final Vertex n)
	{
		final Deque<OrientedFace> star = getStar(n);
		final ArrayList<Tetrahedron> deleted = new ArrayList<>();
		for (final OrientedFace f : star)
		{
			deleted.add(f.getIncident());
		}
		assert star.size() == 4;
		final OrientedFace base = star.pop();
		final Vertex a = base.getVertex(2);
		final Vertex b = base.getVertex(0);
		final Vertex c = base.getVertex(1);
		Vertex d = null;
		OrientedFace face = star.pop();
		for (final Vertex v : face)
		{
			if (!base.includes(v))
			{
				d = v;
				break;
			}
		}
		assert d != null;
		final Tetrahedron t = new Tetrahedron(a, b, c, d);
		base.getIncident().patch(base.getIncidentVertex(), t, D);
		if (face.includes(a))
		{
			if (face.includes(b))
			{
				assert !face.includes(c);
				face.getIncident().patch(face.getIncidentVertex(), t, C);
				face = star.pop();
				if (face.includes(a))
				{
					assert !face.includes(b);
					face.getIncident().patch(face.getIncidentVertex(), t, B);
					face = star.pop();
					assert !face.includes(a);
					face.getIncident().patch(face.getIncidentVertex(), t, A);
				}
				else
				{
					face.getIncident().patch(face.getIncidentVertex(), t, A);
					face = star.pop();
					assert !face.includes(b);
					face.getIncident().patch(face.getIncidentVertex(), t, B);
				}
			}
			else
			{
				face.getIncident().patch(face.getIncidentVertex(), t, B);
				face = star.pop();
				if (face.includes(a))
				{
					assert !face.includes(c);
					face.getIncident().patch(face.getIncidentVertex(), t, C);
					face = star.pop();
					assert !face.includes(a);
					face.getIncident().patch(face.getIncidentVertex(), t, A);
				}
				else
				{
					face.getIncident().patch(face.getIncidentVertex(), t, A);
					face = star.pop();
					assert !face.includes(c);
					face.getIncident().patch(face.getIncidentVertex(), t, C);
				}
			}
		}
		else
		{
			face.getIncident().patch(face.getIncidentVertex(), t, A);
			face = star.pop();
			if (face.includes(b))
			{
				assert !face.includes(c);
				face.getIncident().patch(face.getIncidentVertex(), t, C);
				face = star.pop();
				assert !face.includes(b);
				face.getIncident().patch(face.getIncidentVertex(), t, B);
			}
			else
			{
				face.getIncident().patch(face.getIncidentVertex(), t, B);
				face = star.pop();
				assert !face.includes(c);
				face.getIncident().patch(face.getIncidentVertex(), t, C);
			}
		}

		for (final Tetrahedron tet : deleted)
		{
			tet.delete();
		}
		return t;
	}


	public LinkedList<OrientedFace> getEars(final Vertex v)
	{
		assert v != null && v.getAdjacent() != null;
		final EarSet aggregator = new EarSet();
		v.getAdjacent().visitStar(v, aggregator);
		return aggregator.getEars();
	}


	/**
	 * Answer the collection of neighboring vertices around the indicated
	 * vertex.
	 *
	 * @param v
	 *            - the vertex determining the neighborhood
	 * @return the collection of neighboring vertices
	 */
	public Collection<Vertex> getNeighbors(final Vertex v)
	{
		assert v != null && v.getAdjacent() != null;

		final Set<Vertex> neighbors = new IdentitySet<>();
		v.getAdjacent().visitStar(v, new StarVisitor()
		{
			@Override
			public void visit(final V vertex, final Tetrahedron t, final Vertex x, final Vertex y, final Vertex z)
			{
				neighbors.add(x);
				neighbors.add(y);
				neighbors.add(z);
			}
		});
		return neighbors;
	}


	public Deque<OrientedFace> getStar(final Vertex v)
	{
		assert v != null && v.getAdjacent() != null;

		final Deque<OrientedFace> star = new ArrayDeque<>();
		v.getAdjacent().visitStar(v, new StarVisitor()
		{

			@Override
			public void visit(final V vertex, final Tetrahedron t, final Vertex x, final Vertex y, final Vertex z)
			{
				star.push(t.getFace(vertex));
			}
		});
		return star;
	}


	/**
	 * Answer the set of all tetrahedrons in this tetrahedralization
	 *
	 * @return
	 */
	public Set<Tetrahedron> getTetrahedrons()
	{
		final Set<Tetrahedron> all = new IdentitySet<>(this.size);
		this.last.traverse(all, new EmptySet<Vertex>());
		return all;
	}


	/**
	 * Answer the four corners of the universe
	 *
	 * @return
	 */
	public Vertex[] getUniverse()
	{
		return this.fourCorners;
	}


	/**
	 * Answer the set of all vertices in this tetrahedralization
	 *
	 * @return
	 */
	public Set<Vertex> getVertices()
	{
		final Set<Tetrahedron> allTets = new IdentitySet<>(this.size);
		final Set<Vertex> allVertices = new IdentitySet<>(this.size);
		this.last.traverse(allTets, allVertices);
		for (final Vertex v : this.fourCorners)
		{
			allVertices.remove(v);
		}
		return allVertices;
	}


	/**
	 * Answer the faces of the voronoi region around the vertex
	 *
	 * @param v
	 *            - the vertex of interest
	 * @return the list of faces defining the voronoi region defined by v
	 */
	public List<Coordinates[]> getVoronoiRegion(final Vertex v)
	{
		assert v != null && v.getAdjacent() != null;

		final ArrayList<Coordinates[]> faces = new ArrayList<>();
		v.getAdjacent().visitStar(v, new StarVisitor()
		{
			Set<Vertex> neighbors = new IdentitySet<>(10);


			@Override
			public void visit(final V vertex, final Tetrahedron t, final Vertex x, final Vertex y, final Vertex z)
			{
				if (this.neighbors.add(x))
				{
					t.traverseVoronoiFace(v, x, faces);
				}
				if (this.neighbors.add(y))
				{
					t.traverseVoronoiFace(v, y, faces);
				}
				if (this.neighbors.add(z))
				{
					t.traverseVoronoiFace(v, z, faces);
				}
			}
		});
		return faces;
	}


	/**
	 * Insert the vertex into the tetrahedralization. See "Computing the 3D
	 * Voronoi Diagram Robustly: An Easy Explanation", by Hugo Ledoux
	 * <p>
	 *
	 * @param v
	 *            - the vertex to be inserted
	 */
	public void insert(final Vertex v)
	{
		assert v != null;
		v.reset();
		final List<OrientedFace> ears = new ArrayList<>();
		this.last = locate(v).flip1to4(v, ears);
		while (!ears.isEmpty())
		{
			final Tetrahedron l = ears.remove(ears.size() - 1).flip(v, ears);
			if (l != null)
			{
				this.last = l;
			}
		}
		this.size++;
	}


	/**
	 * Locate the tetrahedron which contains the query point via a stochastic
	 * walk through the delaunay triangulation. This location algorithm is a
	 * slight variation of the 3D jump and walk algorithm found in: "Fast
	 * randomized point location without preprocessing in two- and
	 * three-dimensional Delaunay triangulations", Computational Geometry 12
	 * (1999) 63-83.
	 * <p>
	 * In this variant, the intial "random" triangle used is simply the one of
	 * the triangles in the last tetrahedron created by a flip, or the
	 * previously located tetrahedron.
	 * <p>
	 * This location algorithm provides fast location results with no memory
	 * overhead. Further, because there is no search structure to maintain, this
	 * algorithm is ideally suited for incremental deletions and kinetic
	 * maintenance of the delaunay tetrahedralization.
	 * <p>
	 *
	 * @param query
	 *            - the query point
	 * @return
	 */
	public Tetrahedron locate(final Vertex query)
	{
		assert query != null;

		V o = null;
		// Check each vertex of the tetrahedron to see if its opposite face
		// leads to the query.
		for (final V face : Tetrahedralization.VERTICES)
		{
			if (this.last.orientationWrt(face, query) < 0)
			{
				o = face;
				break;
			}
		}
		if (o == null)
		{
			// The query point is contained in the receiver
			return this.last;
		}
		Tetrahedron current = this.last;
		while (true)
		{
			// get the tetrahedron on the other side of the face
			final Tetrahedron tetrahedron = current.getNeighbor(o);
			int i = 0;
			// Go through the other three faces in random order.
			for (final V v : Tetrahedralization.ORDER[tetrahedron.ordinalOf(current).ordinal()][this.random.nextInt(6)])
			{
				o = v;
				current = tetrahedron;
				if (tetrahedron.orientationWrt(v, query) < 0)
				{
					// we have found a face which the query point is on the
					// other side
					break;
				}
				if (i++ == 2) // If none of the faces is correct, this
								// tetrahedron contains the query point.
				{
					this.last = tetrahedron;
					return this.last;
				}
			}
		}
	}


	/**
	 * Construct a Tetrahedron which is set up to encompass the numerical span
	 *
	 * @return
	 */
	public Tetrahedron myOwnPrivateIdaho()
	{
		final Vertex[] U = new Vertex[4];
		int i = 0;
		for (final Vertex v : this.fourCorners)
		{
			U[i++] = v;
		}
		return new Tetrahedron(U);
	}


	/**
	 * Traverse all the tetrahedrons in the tetrahedralization. The set of
	 * tetrahedons will be filled with all the tetrahedrons and the set of
	 * vertices will be filled with all the vertices defining the
	 * tetrahedralization.
	 * <p>
	 *
	 * @param tetrahedrons
	 * @param vertices
	 */
	public void traverse(final Set<Tetrahedron> tetrahedrons, final Set<Vertex> vertices)
	{
		assert tetrahedrons.isEmpty() && vertices.isEmpty();

		this.last.traverse(tetrahedrons, vertices);
	}
}
