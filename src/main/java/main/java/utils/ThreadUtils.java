/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

/**
Copyright (C) Thomas Boudier
License:
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 *
 * @author thomas
 */
public class ThreadUtils
{

	/**
	 * 
	 * @return
	 */
	public static Thread[] createThreadArray()
	{
		return createThreadArray(0);
	}


	/**
	 * 
	 * @param nb
	 * @return
	 */
	public static Thread[] createThreadArray(int nb)
	{
		if (nb == 0)
		{
			nb = getNbCpus();
		}

		return new Thread[nb];
	}


	/**
	 * 
	 * @return
	 */
	public static int getNbCpus()
	{
		return Runtime.getRuntime().availableProcessors();
	}


	/**
	 * Start all given threads and wait on each of them until all are done. From Stephan Preibisch's Multithreading.java class. See: http://repo.or.cz/w/trakem2.git?a=blob;f=mpi/fruitfly/general/MultiThreading.java;hb=HEAD
	 * 
	 * @param threads
	 */
	public static void startAndJoin(final Thread[] threads)
	{
		for (final Thread thread : threads)
		{
			thread.setPriority(Thread.NORM_PRIORITY);
			thread.start();
		}

		try
		{
			for (final Thread thread : threads)
			{
				thread.join();
			}
		}
		catch (final InterruptedException ie)
		{
			throw new RuntimeException(ie);
		}
	}
}