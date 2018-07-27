/**
 * (C) Copyright 2009 Hal Hildebrand, All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package delaunay3d.utils.collections;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 *
 */
public abstract class OpenAddressingSet<T> extends AbstractSet<T>
{

	private static final Object DELETED = new Object();
	private static final int PRIME = -1640531527;
	private static final float THRESHOLD = 0.75f;
	int load;
	int size = 0;
	Object table[];


	public OpenAddressingSet()
	{
		this(4);
	}


	public OpenAddressingSet(final int initialCapacity)
	{
		init(initialCapacity);
	}


	@Override
	public final boolean add(final Object key)
	{
		if (key == null)
		{
			throw new IllegalArgumentException("Null key");
		}
		if (this.table == null)
		{
			init(1);
		}
		else if (this.size >= this.table.length * THRESHOLD)
		{
			rehash();
		}
		return insert(key);
	}


	@Override
	public void clear()
	{
		this.table = null;
		this.size = 0;
	}


	@Override
	public OpenAddressingSet<T> clone()
	{
		try
		{
			@SuppressWarnings("unchecked")
			final OpenAddressingSet<T> t = (OpenAddressingSet<T>) super.clone();
			if (this.table != null)
			{
				t.table = new Object[this.table.length];
				for (int i = this.table.length; i-- > 0;)
				{
					t.table[i] = this.table[i];
				}
			}
			return t;
		}
		catch (final CloneNotSupportedException e)
		{
			throw new InternalError();
		}
	}


	@Override
	public boolean contains(final Object key)
	{
		if (key == null || this.size == 0)
		{
			return false;
		}
		final int hash = PRIME * getHash(key) >>> this.load;
		int index = hash;
		do
		{
			final Object ob = this.table[index];
			if (ob == null)
			{
				return false;
			}
			if (equals(key, ob))
			{
				return true;
			}
			index = index + (hash | 1) & this.table.length - 1;
		} while (index != hash);
		return false;
	}


	abstract protected boolean equals(Object key, Object ob);


	abstract protected int getHash(Object key);


	protected void init(int initialCapacity)
	{
		if (initialCapacity < 4)
		{
			initialCapacity = 4;
		}
		int cap = 4;
		this.load = 2;
		while (cap < initialCapacity)
		{
			this.load += 1;
			cap += cap;
		}
		this.table = new Object[cap];
		this.load = 32 - this.load;
	}


	private boolean insert(final Object key)
	{
		final int hash = PRIME * getHash(key) >>> this.load;
		int index = hash;
		do
		{
			final Object ob = this.table[index];
			if (ob == null || ob == DELETED)
			{
				this.table[index] = key;
				this.size += 1;
				return true;
			}
			if (equals(key, ob))
			{
				this.table[index] = key;
				return false;
			}
			index = index + (hash | 1) & this.table.length - 1;
		} while (index != hash);
		rehash();
		return insert(key);
	}


	@Override
	public final boolean isEmpty()
	{
		return size() == 0;
	}


	@Override
	public Iterator<T> iterator()
	{
		return new Iterator<T>()
		{
			int next = 0;


			@Override
			public boolean hasNext()
			{
				while (this.next < OpenAddressingSet.this.table.length)
				{
					if (OpenAddressingSet.this.table[this.next] != null && OpenAddressingSet.this.table[this.next] != DELETED)
					{
						return true;
					}
					this.next++;
				}
				return false;
			}


			@SuppressWarnings("unchecked")
			@Override
			public T next()
			{
				while (this.next < OpenAddressingSet.this.table.length)
				{
					if (OpenAddressingSet.this.table[this.next] != null && OpenAddressingSet.this.table[this.next] != DELETED)
					{
						return (T) OpenAddressingSet.this.table[this.next++];
					}
					this.next++;
				}
				throw new NoSuchElementException("Enumerator");
			}


			@Override
			public void remove()
			{
				throw new UnsupportedOperationException("Remove is not supported");
			}
		};
	}


	private void rehash()
	{
		final Object[] oldMap = this.table;
		final int oldCapacity = oldMap.length;
		this.load -= 1;
		this.table = new Object[oldCapacity * 2];
		this.size = 0;
		for (int i = oldCapacity - 1; i >= 0; i -= 1)
		{
			final Object ob = oldMap[i];
			if (ob != null && ob != DELETED)
			{
				insert(ob);
			}
		}
	}


	@Override
	public final boolean remove(final Object key)
	{
		if (key == null)
		{
			throw new IllegalArgumentException("Null key");
		}
		if (!isEmpty())
		{
			final int hash = PRIME * getHash(key) >>> this.load;
			int index = hash;
			do
			{
				final Object ob = this.table[index];
				if (ob == null)
				{
					return false;
				}
				if (equals(key, ob))
				{
					this.table[index] = DELETED;
					this.size -= 1;
					return true;
				}
				index = index + (hash | 1) & this.table.length - 1;
			} while (index != hash);
		}
		return false;
	}


	@Override
	public final int size()
	{
		return this.size;
	}

}