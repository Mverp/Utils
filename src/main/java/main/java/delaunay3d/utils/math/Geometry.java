/****************************************************************************
Copyright (c) 2006, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
 ****************************************************************************/
package delaunay3d.utils.math;

/**
 * Robust geometric predicates.
 * <p>
 * These geometric predicates are notoriously susceptible to roundoff error. For
 * example, the simplest and fastest test to determine whether a point c is left
 * of a line defined by two points a and b may fail when all three points are
 * nearly co-linear.
 * <p>
 * Therefore, each predicate is implemented by two types of methods. One method
 * is fast, but may yield incorrect answers. The other method is slower, because
 * it (1) computes a bound on the roundoff error and (2) reverts to an exact
 * algorithm if the fast method might yield the wrong answer.
 * <p>
 * Most applications should use the slower exact methods. The fast methods are
 * provided only for comparison.
 * <p>
 * These predicates are adapted from those developed by Jonathan Shewchuk, 1997,
 * Delaunay Refinement Mesh Generation: Ph.D. dissertation, Carnegie Mellon
 * University. (Currently, the methods here do not use Shewchuk's adaptive
 * four-stage pipeline. Instead, only two - the fastest and the exact stages -
 * are used.)
 *
 * @author Dave Hale, Colorado School of Mines
 * @version 2001.04.03, 2006.08.02
 */
public final class Geometry
{

	/**
	 * Two doubles.
	 */
	private static class Two
	{
		double x, y;
	}

	/**
	 * Constants.
	 */
	private static final double EPSILON;

	private static final double INCERRBOUND;

	private static final double INSERRBOUND;

	private static final double IOSERRBOUND;

	private static final double O2DERRBOUND;

	private static final double O3DERRBOUND;

	private static final double SPLITTER;

	static
	{
		double epsilon = 1.0;
		double splitter = 1.0;
		boolean everyOther = true;
		do
		{
			epsilon *= 0.5;
			if (everyOther)
			{
				splitter *= 2.0;
			}
			everyOther = !everyOther;
		} while (1.0 + epsilon != 1.0);
		splitter += 1.0;
		EPSILON = epsilon;
		SPLITTER = splitter;
		O2DERRBOUND = 4.0 * EPSILON;
		O3DERRBOUND = 8.0 * EPSILON;
		INCERRBOUND = 11.0 * EPSILON;
		INSERRBOUND = 17.0 * EPSILON;
		IOSERRBOUND = 19.0 * EPSILON;
	}


	/**
	 * Computes the center of the circle defined by the points a, b, and c. The
	 * latter are assumed to be in CCW order, such that the method
	 * {@link #leftOfLine} would return a positive number.
	 * 
	 * @param po
	 *            array containing (x,y) coordinates of center.
	 */
	public static void centerCircle(final double xa, final double ya, final double xb, final double yb, final double xc, final double yc, final double[] po)
	{
		final double acx = xa - xc;
		final double bcx = xb - xc;
		final double acy = ya - yc;
		final double bcy = yb - yc;
		final double acs = acx * acx + acy * acy;
		final double bcs = bcx * bcx + bcy * bcy;
		final double scale = 0.5 / leftOfLine(xa, ya, xb, yb, xc, yc);
		po[0] = xc + scale * (acs * bcy - bcs * acy);
		po[1] = yc + scale * (bcs * acx - acs * bcx);
	}


	/**
	 * Computes the center of the circle defined by the points a, b, and c. The
	 * latter are assumed to be in CCW order, such that the method
	 * {@link #leftOfLine} would return a positive number.
	 * 
	 * @param po
	 *            array containing (x,y) coordinates of center.
	 */
	public static void centerCircle(final double[] pa, final double[] pb, final double[] pc, final double[] po)
	{
		centerCircle(pa[0], pa[1], pb[0], pb[1], pc[0], pc[1], po);
	}


	/**
	 * Computes the center of the circle defined by the points a, b, and c. The
	 * latter are assumed to be in CCW order, such that the method
	 * {@link #leftOfLine} would return a positive number.
	 * 
	 * @param po
	 *            array containing (x,y) coordinates of center.
	 */
	public static void centerCircle(final float xa, final float ya, final float xb, final float yb, final float xc, final float yc, final float[] po)
	{
		final double acx = xa - xc;
		final double bcx = xb - xc;
		final double acy = ya - yc;
		final double bcy = yb - yc;
		final double acs = acx * acx + acy * acy;
		final double bcs = bcx * bcx + bcy * bcy;
		final double scale = 0.5 / leftOfLine(xa, ya, xb, yb, xc, yc);
		po[0] = (float) (xc + scale * (acs * bcy - bcs * acy));
		po[1] = (float) (yc + scale * (bcs * acx - acs * bcx));
	}


	/**
	 * Computes the center of the circle defined by the points a, b, and c. The
	 * latter are assumed to be in CCW order, such that the method
	 * {@link #leftOfLine} would return a positive number.
	 * 
	 * @param po
	 *            array containing (x,y) coordinates of center.
	 */
	public static void centerCircle(final float[] pa, final float[] pb, final float[] pc, final float[] po)
	{
		centerCircle(pa[0], pa[1], pb[0], pb[1], pc[0], pc[1], po);
	}


	/**
	 * Computes the center of the circle defined by the 3-D points a, b, and c.
	 * Because the points have 3-D coordinates, they may specified in any order.
	 * 
	 * @param po
	 *            array containing (x,y,z) coordinates of center.
	 */
	public static void centerCircle3D(final double xa, final double ya, final double za, final double xb, final double yb, final double zb, final double xc, final double yc, final double zc,
			final double[] po)
	{
		final double acx = xa - xc;
		final double acy = ya - yc;
		final double acz = za - zc;
		final double bcx = xb - xc;
		final double bcy = yb - yc;
		final double bcz = zb - zc;
		final double acs = acx * acx + acy * acy + acz * acz;
		final double bcs = bcx * bcx + bcy * bcy + bcz * bcz;
		final double abx = leftOfLine(ya, za, yb, zb, yc, zc);
		final double aby = leftOfLine(za, xa, zb, xb, zc, xc);
		final double abz = leftOfLine(xa, ya, xb, yb, xc, yc);
		final double scale = 0.5 / (abx * abx + aby * aby + abz * abz);
		po[0] = xc + scale * ((acs * bcy - bcs * acy) * abz - (acs * bcz - bcs * acz) * aby);
		po[1] = yc + scale * ((acs * bcz - bcs * acz) * abx - (acs * bcx - bcs * acx) * abz);
		po[2] = zc + scale * ((acs * bcx - bcs * acx) * aby - (acs * bcy - bcs * acy) * abx);
	}


	/**
	 * Computes the center of the sphere defined by the points a, b, c, and d.
	 * The latter are assumed to be in CCW order, such that the method
	 * {@link #leftOfPlane} would return a positive number.
	 * 
	 * @param po
	 *            array containing (x,y,z) coordinates of center.
	 */
	public static void centerSphere(final double xa, final double ya, final double za, final double xb, final double yb, final double zb, final double xc, final double yc, final double zc,
			final double xd, final double yd, final double zd, final double[] po)
	{
		final double adx = xa - xd;
		final double bdx = xb - xd;
		final double cdx = xc - xd;
		final double ady = ya - yd;
		final double bdy = yb - yd;
		final double cdy = yc - yd;
		final double adz = za - zd;
		final double bdz = zb - zd;
		final double cdz = zc - zd;
		final double ads = adx * adx + ady * ady + adz * adz;
		final double bds = bdx * bdx + bdy * bdy + bdz * bdz;
		final double cds = cdx * cdx + cdy * cdy + cdz * cdz;
		final double scale = 0.5 / leftOfPlane(xa, ya, za, xb, yb, zb, xc, yc, zc, xd, yd, zd);
		po[0] = xd + scale * (ads * (bdy * cdz - cdy * bdz) + bds * (cdy * adz - ady * cdz) + cds * (ady * bdz - bdy * adz));
		po[1] = yd + scale * (ads * (bdz * cdx - cdz * bdx) + bds * (cdz * adx - adz * cdx) + cds * (adz * bdx - bdz * adx));
		po[2] = zd + scale * (ads * (bdx * cdy - cdx * bdy) + bds * (cdx * ady - adx * cdy) + cds * (adx * bdy - bdx * ady));
	}


	/**
	 * Computes the center of the sphere defined by the points a, b, c, and d.
	 * The latter are assumed to be in CCW order, such that the method
	 * {@link #leftOfPlane} would return a positive number.
	 * 
	 * @param po
	 *            array containing (x,y,z) coordinates of center.
	 */
	public static void centerSphere(final double[] pa, final double[] pb, final double[] pc, final double[] pd, final double[] po)
	{
		centerSphere(pa[0], pa[1], pa[2], pb[0], pb[1], pb[2], pc[0], pc[1], pc[2], pd[0], pd[1], pd[2], po);
	}


	/**
	 * Computes the center of the sphere defined by the points a, b, c, and d.
	 * The latter are assumed to be in CCW order, such that the method
	 * {@link #leftOfPlane} would return a positive number.
	 * 
	 * @param po
	 *            array containing (x,y,z) coordinates of center.
	 */
	public static void centerSphere(final float xa, final float ya, final float za, final float xb, final float yb, final float zb, final float xc, final float yc, final float zc, final float xd,
			final float yd, final float zd, final float[] po)
	{
		final double adx = xa - xd;
		final double bdx = xb - xd;
		final double cdx = xc - xd;
		final double ady = ya - yd;
		final double bdy = yb - yd;
		final double cdy = yc - yd;
		final double adz = za - zd;
		final double bdz = zb - zd;
		final double cdz = zc - zd;
		final double ads = adx * adx + ady * ady + adz * adz;
		final double bds = bdx * bdx + bdy * bdy + bdz * bdz;
		final double cds = cdx * cdx + cdy * cdy + cdz * cdz;
		final double scale = 0.5 / leftOfPlane(xa, ya, za, xb, yb, zb, xc, yc, zc, xd, yd, zd);
		po[0] = xd + (float) (scale * (ads * (bdy * cdz - cdy * bdz) + bds * (cdy * adz - ady * cdz) + cds * (ady * bdz - bdy * adz)));
		po[1] = yd + (float) (scale * (ads * (bdz * cdx - cdz * bdx) + bds * (cdz * adx - adz * cdx) + cds * (adz * bdx - bdz * adx)));
		po[2] = zd + (float) (scale * (ads * (bdx * cdy - cdx * bdy) + bds * (cdx * ady - adx * cdy) + cds * (adx * bdy - bdx * ady)));
	}


	/**
	 * Computes the center of the sphere defined by the points a, b, c, and d.
	 * The latter are assumed to be in CCW order, such that the method
	 * {@link #leftOfPlane} would return a positive number.
	 * 
	 * @param po
	 *            array containing (x,y,z) coordinates of center.
	 */
	public static void centerSphere(final float[] pa, final float[] pb, final float[] pc, final float[] pd, final float[] po)
	{
		centerSphere(pa[0], pa[1], pa[2], pb[0], pb[1], pb[2], pc[0], pc[1], pc[2], pd[0], pd[1], pd[2], po);
	}


	/**
	 * Computes the sum of two expansions h = e+f, eliminating zero components
	 * from output expansion. If round-to-even is used (as with IEEE 754),
	 * maintains the strongly nonoverlapping property. (That is, if e is
	 * strongly nonoverlapping, h will be also.) Does NOT maintain the
	 * nonoverlapping or nonadjacent properties. The expansion h cannot be
	 * aliased with e or f.
	 */
	private static int expansionSumZeroElimFast(final int elen, final double[] e, final int flen, final double[] f, final double[] h)
	{
		double q, qnew, hh;
		final Two t = new Two();
		double enow = e[0];
		double fnow = f[0];
		int eindex = 0;
		int findex = 0;
		if (fnow > enow == fnow > -enow)
		{
			q = enow;
			++eindex;
			if (eindex < elen)
			{
				enow = e[eindex];
			}
		}
		else
		{
			q = fnow;
			++findex;
			if (findex < flen)
			{
				fnow = f[findex];
			}
		}
		int hindex = 0;
		if (eindex < elen && findex < flen)
		{
			if (fnow > enow == fnow > -enow)
			{
				twoSumFast(enow, q, t);
				qnew = t.x;
				hh = t.y;
				++eindex;
				if (eindex < elen)
				{
					enow = e[eindex];
				}
			}
			else
			{
				twoSumFast(fnow, q, t);
				qnew = t.x;
				hh = t.y;
				++findex;
				if (findex < flen)
				{
					fnow = f[findex];
				}
			}
			q = qnew;
			if (hh != 0.0)
			{
				h[hindex++] = hh;
			}
			while (eindex < elen && findex < flen)
			{
				if (fnow > enow == fnow > -enow)
				{
					twoSum(q, enow, t);
					qnew = t.x;
					hh = t.y;
					++eindex;
					if (eindex < elen)
					{
						enow = e[eindex];
					}
				}
				else
				{
					twoSum(q, fnow, t);
					qnew = t.x;
					hh = t.y;
					++findex;
					if (findex < flen)
					{
						fnow = f[findex];
					}
				}
				q = qnew;
				if (hh != 0.0)
				{
					h[hindex++] = hh;
				}
			}
		}
		while (eindex < elen)
		{
			twoSum(q, enow, t);
			qnew = t.x;
			hh = t.y;
			++eindex;
			if (eindex < elen)
			{
				enow = e[eindex];
			}
			q = qnew;
			if (hh != 0.0)
			{
				h[hindex++] = hh;
			}
		}
		while (findex < flen)
		{
			twoSum(q, fnow, t);
			qnew = t.x;
			hh = t.y;
			++findex;
			if (findex < flen)
			{
				fnow = f[findex];
			}
			q = qnew;
			if (hh != 0.0)
			{
				h[hindex++] = hh;
			}
		}
		if (q != 0.0 || hindex == 0)
		{
			h[hindex++] = q;
		}
		return hindex;
	}


	/**
	 * Determines if a point d is inside the circle defined by the points a, b,
	 * and c. The latter are assumed to be in CCW order, such that the method
	 * {@link #leftOfLine} would return a positive number.
	 * 
	 * @return positive, if inside the circle; negative, if outside the circle;
	 *         zero, otherwise.
	 */
	public static double inCircle(final double xa, final double ya, final double xb, final double yb, final double xc, final double yc, final double xd, final double yd)
	{
		final double adx = xa - xd;
		final double bdx = xb - xd;
		final double cdx = xc - xd;
		final double ady = ya - yd;
		final double bdy = yb - yd;
		final double cdy = yc - yd;

		double bdxcdy = bdx * cdy;
		double cdxbdy = cdx * bdy;
		final double alift = adx * adx + ady * ady;

		double cdxady = cdx * ady;
		double adxcdy = adx * cdy;
		final double blift = bdx * bdx + bdy * bdy;

		double adxbdy = adx * bdy;
		double bdxady = bdx * ady;
		final double clift = cdx * cdx + cdy * cdy;

		final double det = alift * (bdxcdy - cdxbdy) + blift * (cdxady - adxcdy) + clift * (adxbdy - bdxady);

		if (bdxcdy < 0.0)
		{
			bdxcdy = -bdxcdy;
		}
		if (cdxbdy < 0.0)
		{
			cdxbdy = -cdxbdy;
		}
		if (adxcdy < 0.0)
		{
			adxcdy = -adxcdy;
		}
		if (cdxady < 0.0)
		{
			cdxady = -cdxady;
		}
		if (adxbdy < 0.0)
		{
			adxbdy = -adxbdy;
		}
		if (bdxady < 0.0)
		{
			bdxady = -bdxady;
		}

		final double permanent = alift * (bdxcdy + cdxbdy) + blift * (cdxady + adxcdy) + clift * (adxbdy + bdxady);
		final double errbound = INCERRBOUND * permanent;
		if (det > errbound || -det > errbound)
		{
			return det;
		}

		return inCircleExact(xa, ya, xb, yb, xc, yc, xd, yd);
	}


	/**
	 * Determines if a point d is inside the circle defined by the points a, b,
	 * and c. The latter are assumed to be in CCW order, such that the method
	 * {@link #leftOfLine} would return a positive number.
	 * 
	 * @return positive, if inside the circle; negative, if outside the circle;
	 *         zero, otherwise.
	 */
	public static double inCircle(final double[] pa, final double[] pb, final double[] pc, final double[] pd)
	{
		return inCircle(pa[0], pa[1], pb[0], pb[1], pc[0], pc[1], pd[0], pd[1]);
	}


	/**
	 * Determines if a point d is inside the circle defined by the points a, b,
	 * and c. The latter are assumed to be in CCW order, such that the method
	 * {@link #leftOfLine} would return a positive number.
	 * 
	 * @return positive, if inside the circle; negative, if outside the circle;
	 *         zero, otherwise.
	 */
	public static double inCircle(final float[] pa, final float[] pb, final float[] pc, final float[] pd)
	{
		return inCircle(pa[0], pa[1], pb[0], pb[1], pc[0], pc[1], pd[0], pd[1]);
	}


	/**
	 * Slow exact in-circle test. Returns a positive value if the point pd lies
	 * inside the circle passing through pa, pb, and pc; a negative value if it
	 * lies outside; and zero if the four points are cocircular. The points pa,
	 * pb, and pc must be in counterclockwise order, or the sign of the result
	 * will be reversed.
	 */
	private static double inCircleExact(final double xa, final double ya, final double xb, final double yb, final double xc, final double yc, final double xd, final double yd)
	{
		final Two t = new Two();
		twoDiff(xa, xd, t);
		final double adx = t.x;
		final double adxtail = t.y;
		twoDiff(ya, yd, t);
		final double ady = t.x;
		final double adytail = t.y;
		twoDiff(xb, xd, t);
		final double bdx = t.x;
		final double bdxtail = t.y;
		twoDiff(yb, yd, t);
		final double bdy = t.x;
		final double bdytail = t.y;
		twoDiff(xc, xd, t);
		final double cdx = t.x;
		final double cdxtail = t.y;
		twoDiff(yc, yd, t);
		final double cdy = t.x;
		final double cdytail = t.y;

		final double[] axby = new double[8];
		final double[] bxay = new double[8];
		twoTwoProduct(adx, adxtail, bdy, bdytail, axby);
		double negate = -ady;
		double negatetail = -adytail;
		twoTwoProduct(bdx, bdxtail, negate, negatetail, bxay);

		final double[] bxcy = new double[8];
		final double[] cxby = new double[8];
		twoTwoProduct(bdx, bdxtail, cdy, cdytail, bxcy);
		negate = -bdy;
		negatetail = -bdytail;
		twoTwoProduct(cdx, cdxtail, negate, negatetail, cxby);

		final double[] cxay = new double[8];
		final double[] axcy = new double[8];
		twoTwoProduct(cdx, cdxtail, ady, adytail, cxay);
		negate = -cdy;
		negatetail = -cdytail;
		twoTwoProduct(adx, adxtail, negate, negatetail, axcy);

		final double[] t16 = new double[16];
		int t16len = expansionSumZeroElimFast(8, bxcy, 8, cxby, t16);

		final double[] detx = new double[32];
		final double[] detxx = new double[64];
		final double[] detxt = new double[32];
		final double[] detxxt = new double[64];
		final double[] detxtxt = new double[64];
		final double[] x1 = new double[128];
		final double[] x2 = new double[192];
		int xlen = scaleExpansionZeroElim(t16len, t16, adx, detx);
		int xxlen = scaleExpansionZeroElim(xlen, detx, adx, detxx);
		int xtlen = scaleExpansionZeroElim(t16len, t16, adxtail, detxt);
		int xxtlen = scaleExpansionZeroElim(xtlen, detxt, adx, detxxt);
		for (int i = 0; i < xxtlen; ++i)
		{
			detxxt[i] *= 2.0;
		}
		int xtxtlen = scaleExpansionZeroElim(xtlen, detxt, adxtail, detxtxt);
		int x1len = expansionSumZeroElimFast(xxlen, detxx, xxtlen, detxxt, x1);
		int x2len = expansionSumZeroElimFast(x1len, x1, xtxtlen, detxtxt, x2);

		final double[] dety = new double[32];
		final double[] detyy = new double[64];
		final double[] detyt = new double[32];
		final double[] detyyt = new double[64];
		final double[] detytyt = new double[64];
		final double[] y1 = new double[128];
		final double[] y2 = new double[192];
		int ylen = scaleExpansionZeroElim(t16len, t16, ady, dety);
		int yylen = scaleExpansionZeroElim(ylen, dety, ady, detyy);
		int ytlen = scaleExpansionZeroElim(t16len, t16, adytail, detyt);
		int yytlen = scaleExpansionZeroElim(ytlen, detyt, ady, detyyt);
		for (int i = 0; i < yytlen; ++i)
		{
			detyyt[i] *= 2.0;
		}
		int ytytlen = scaleExpansionZeroElim(ytlen, detyt, adytail, detytyt);
		int y1len = expansionSumZeroElimFast(yylen, detyy, yytlen, detyyt, y1);
		int y2len = expansionSumZeroElimFast(y1len, y1, ytytlen, detytyt, y2);

		final double[] adet = new double[384];
		final double[] bdet = new double[384];
		final double[] cdet = new double[384];
		final int alen = expansionSumZeroElimFast(x2len, x2, y2len, y2, adet);

		t16len = expansionSumZeroElimFast(8, cxay, 8, axcy, t16);
		xlen = scaleExpansionZeroElim(t16len, t16, bdx, detx);
		xxlen = scaleExpansionZeroElim(xlen, detx, bdx, detxx);
		xtlen = scaleExpansionZeroElim(t16len, t16, bdxtail, detxt);
		xxtlen = scaleExpansionZeroElim(xtlen, detxt, bdx, detxxt);
		for (int i = 0; i < xxtlen; ++i)
		{
			detxxt[i] *= 2.0;
		}
		xtxtlen = scaleExpansionZeroElim(xtlen, detxt, bdxtail, detxtxt);
		x1len = expansionSumZeroElimFast(xxlen, detxx, xxtlen, detxxt, x1);
		x2len = expansionSumZeroElimFast(x1len, x1, xtxtlen, detxtxt, x2);

		ylen = scaleExpansionZeroElim(t16len, t16, bdy, dety);
		yylen = scaleExpansionZeroElim(ylen, dety, bdy, detyy);
		ytlen = scaleExpansionZeroElim(t16len, t16, bdytail, detyt);
		yytlen = scaleExpansionZeroElim(ytlen, detyt, bdy, detyyt);
		for (int i = 0; i < yytlen; ++i)
		{
			detyyt[i] *= 2.0;
		}
		ytytlen = scaleExpansionZeroElim(ytlen, detyt, bdytail, detytyt);
		y1len = expansionSumZeroElimFast(yylen, detyy, yytlen, detyyt, y1);
		y2len = expansionSumZeroElimFast(y1len, y1, ytytlen, detytyt, y2);
		final int blen = expansionSumZeroElimFast(x2len, x2, y2len, y2, bdet);

		t16len = expansionSumZeroElimFast(8, axby, 8, bxay, t16);
		xlen = scaleExpansionZeroElim(t16len, t16, cdx, detx);
		xxlen = scaleExpansionZeroElim(xlen, detx, cdx, detxx);
		xtlen = scaleExpansionZeroElim(t16len, t16, cdxtail, detxt);
		xxtlen = scaleExpansionZeroElim(xtlen, detxt, cdx, detxxt);
		for (int i = 0; i < xxtlen; ++i)
		{
			detxxt[i] *= 2.0;
		}
		xtxtlen = scaleExpansionZeroElim(xtlen, detxt, cdxtail, detxtxt);
		x1len = expansionSumZeroElimFast(xxlen, detxx, xxtlen, detxxt, x1);
		x2len = expansionSumZeroElimFast(x1len, x1, xtxtlen, detxtxt, x2);
		ylen = scaleExpansionZeroElim(t16len, t16, cdy, dety);
		yylen = scaleExpansionZeroElim(ylen, dety, cdy, detyy);
		ytlen = scaleExpansionZeroElim(t16len, t16, cdytail, detyt);
		yytlen = scaleExpansionZeroElim(ytlen, detyt, cdy, detyyt);
		for (int i = 0; i < yytlen; ++i)
		{
			detyyt[i] *= 2.0;
		}
		ytytlen = scaleExpansionZeroElim(ytlen, detyt, cdytail, detytyt);
		y1len = expansionSumZeroElimFast(yylen, detyy, yytlen, detyyt, y1);
		y2len = expansionSumZeroElimFast(y1len, y1, ytytlen, detytyt, y2);
		final int clen = expansionSumZeroElimFast(x2len, x2, y2len, y2, cdet);

		final double[] abdet = new double[768];
		final double[] det = new double[1152];
		final int ablen = expansionSumZeroElimFast(alen, adet, blen, bdet, abdet);
		final int detlen = expansionSumZeroElimFast(ablen, abdet, clen, cdet, det);

		return det[detlen - 1];
	}


	/**
	 * Determines if a point d is inside the circle defined by the points a, b,
	 * and c. The latter are assumed to be in CCW order, such that the method
	 * {@link #leftOfLine} would return a positive number.
	 * <p>
	 * <em>Note: this fast method may return an incorrect result.</em>
	 * 
	 * @return positive, if inside the circle; negative, if outside the circle;
	 *         zero, otherwise.
	 */
	public static double inCircleFast(final double xa, final double ya, final double xb, final double yb, final double xc, final double yc, final double xd, final double yd)
	{
		final double adx = xa - xd;
		final double ady = ya - yd;
		final double bdx = xb - xd;
		final double bdy = yb - yd;
		final double cdx = xc - xd;
		final double cdy = yc - yd;

		final double abdet = adx * bdy - bdx * ady;
		final double bcdet = bdx * cdy - cdx * bdy;
		final double cadet = cdx * ady - adx * cdy;
		final double alift = adx * adx + ady * ady;
		final double blift = bdx * bdx + bdy * bdy;
		final double clift = cdx * cdx + cdy * cdy;

		return alift * bcdet + blift * cadet + clift * abdet;
	}


	/**
	 * Determines if a point d is inside the circle defined by the points a, b,
	 * and c. The latter are assumed to be in CCW order, such that the method
	 * {@link #leftOfLine} would return a positive number.
	 * <p>
	 * <em>Note: this fast method may return an incorrect result.</em>
	 * 
	 * @return positive, if inside the circle; negative, if outside the circle;
	 *         zero, otherwise.
	 */
	public static double inCircleFast(final double[] pa, final double[] pb, final double[] pc, final double[] pd)
	{
		return inCircleFast(pa[0], pa[1], pb[0], pb[1], pc[0], pc[1], pd[0], pd[1]);
	}


	/**
	 * Determines if a point d is inside the circle defined by the points a, b,
	 * and c. The latter are assumed to be in CCW order, such that the method
	 * {@link #leftOfLine} would return a positive number.
	 * <p>
	 * <em>Note: this fast method may return an incorrect result.</em>
	 * 
	 * @return positive, if inside the circle; negative, if outside the circle;
	 *         zero, otherwise.
	 */
	public static double inCircleFast(final float[] pa, final float[] pb, final float[] pc, final float[] pd)
	{
		return inCircleFast(pa[0], pa[1], pb[0], pb[1], pc[0], pc[1], pd[0], pd[1]);
	}


	/**
	 * Determines whether or not a weighted point e is inside the ortho-sphere
	 * defined by the weighted points a, b, c, and d. The latter are assumed to
	 * be in CCW order, such that the method {@link #leftOfPlane} would return a
	 * positive number.
	 * <p>
	 * The weights wa, wb, wc, wd equal the squared radii of spheres associated
	 * with the corresponding points a, b, c, and d. The ortho-sphere is
	 * orthogonal to each of these four spheres.
	 * <p>
	 * If all four weights (and radii) equal zero, then the ortho-sphere is the
	 * circumsphere. In this case, the method {@link #inSphere} is more
	 * efficient.
	 */
	public static double inOrthoSphere(final double xa, final double ya, final double za, final double wa, final double xb, final double yb, final double zb, final double wb, final double xc,
			final double yc, final double zc, final double wc, final double xd, final double yd, final double zd, final double wd, final double xe, final double ye, final double ze, final double we)
	{
		final double aex = xa - xe;
		final double bex = xb - xe;
		final double cex = xc - xe;
		final double dex = xd - xe;
		final double aey = ya - ye;
		final double bey = yb - ye;
		final double cey = yc - ye;
		final double dey = yd - ye;
		double aez = za - ze;
		double bez = zb - ze;
		double cez = zc - ze;
		double dez = zd - ze;
		double aew = wa - we;
		double bew = wb - we;
		double cew = wc - we;
		double dew = wd - we;

		double aexbey = aex * bey;
		double bexaey = bex * aey;
		final double ab = aexbey - bexaey;
		double bexcey = bex * cey;
		double cexbey = cex * bey;
		final double bc = bexcey - cexbey;
		double cexdey = cex * dey;
		double dexcey = dex * cey;
		final double cd = cexdey - dexcey;
		double dexaey = dex * aey;
		double aexdey = aex * dey;
		final double da = dexaey - aexdey;

		double aexcey = aex * cey;
		double cexaey = cex * aey;
		final double ac = aexcey - cexaey;
		double bexdey = bex * dey;
		double dexbey = dex * bey;
		final double bd = bexdey - dexbey;

		final double abc = aez * bc - bez * ac + cez * ab;
		final double bcd = bez * cd - cez * bd + dez * bc;
		final double cda = cez * da + dez * ac + aez * cd;
		final double dab = dez * ab + aez * bd + bez * da;

		final double alift = aex * aex + aey * aey + aez * aez;
		final double blift = bex * bex + bey * bey + bez * bez;
		final double clift = cex * cex + cey * cey + cez * cez;
		final double dlift = dex * dex + dey * dey + dez * dez;

		final double det = (dlift - dew) * abc - (clift - cew) * dab + ((blift - bew) * cda - (alift - aew) * bcd);

		if (aez < 0.0)
		{
			aez = -aez;
		}
		if (bez < 0.0)
		{
			bez = -bez;
		}
		if (cez < 0.0)
		{
			cez = -cez;
		}
		if (dez < 0.0)
		{
			dez = -dez;
		}
		if (aew < 0.0)
		{
			aew = -aew;
		}
		if (bew < 0.0)
		{
			bew = -bew;
		}
		if (cew < 0.0)
		{
			cew = -cew;
		}
		if (dew < 0.0)
		{
			dew = -dew;
		}
		if (aexbey < 0.0)
		{
			aexbey = -aexbey;
		}
		if (bexaey < 0.0)
		{
			bexaey = -bexaey;
		}
		if (bexcey < 0.0)
		{
			bexcey = -bexcey;
		}
		if (cexbey < 0.0)
		{
			cexbey = -cexbey;
		}
		if (cexdey < 0.0)
		{
			cexdey = -cexdey;
		}
		if (dexcey < 0.0)
		{
			dexcey = -dexcey;
		}
		if (dexaey < 0.0)
		{
			dexaey = -dexaey;
		}
		if (aexdey < 0.0)
		{
			aexdey = -aexdey;
		}
		if (aexcey < 0.0)
		{
			aexcey = -aexcey;
		}
		if (cexaey < 0.0)
		{
			cexaey = -cexaey;
		}
		if (bexdey < 0.0)
		{
			bexdey = -bexdey;
		}
		if (dexbey < 0.0)
		{
			dexbey = -dexbey;
		}
		final double permanent = ((cexdey + dexcey) * bez + (dexbey + bexdey) * cez + (bexcey + cexbey) * dez) * (alift + aew)
				+ ((dexaey + aexdey) * cez + (aexcey + cexaey) * dez + (cexdey + dexcey) * aez) * (blift + bew)
				+ ((aexbey + bexaey) * dez + (bexdey + dexbey) * aez + (dexaey + aexdey) * bez) * (clift + cew)
				+ ((bexcey + cexbey) * aez + (cexaey + aexcey) * bez + (aexbey + bexaey) * cez) * (dlift + dew);
		final double errbound = IOSERRBOUND * permanent;
		if (det > errbound || -det > errbound)
		{
			return det;
		}

		return inOrthoSphereExact(xa, ya, za, wa, xb, yb, zb, wb, xc, yc, zc, wc, xd, yd, zd, wd, xe, ye, ze, we);
	}


	/**
	 * Determines whether or not a weighted point e is inside the ortho-sphere
	 * defined by the weighted points a, b, c, and d. The latter are assumed to
	 * be in CCW order, such that the method {@link #leftOfPlane} would return a
	 * positive number.
	 */
	public static double inOrthoSphere(final double[] pa, final double[] pb, final double[] pc, final double[] pd, final double[] pe)
	{
		return inOrthoSphere(pa[0], pa[1], pa[2], pa[3], pb[0], pb[1], pb[2], pb[3], pc[0], pc[1], pc[2], pc[3], pd[0], pd[1], pd[2], pd[3], pe[0], pe[1], pe[2], pe[3]);
	}


	/**
	 * Determines whether or not a weighted point e is inside the ortho-sphere
	 * defined by the weighted points a, b, c, and d. The latter are assumed to
	 * be in CCW order, such that the method {@link #leftOfPlane} would return a
	 * positive number.
	 */
	public static double inOrthoSphere(final float[] pa, final float[] pb, final float[] pc, final float[] pd, final float[] pe)
	{
		return inOrthoSphere(pa[0], pa[1], pa[2], pa[3], pb[0], pb[1], pb[2], pb[3], pc[0], pc[1], pc[2], pc[3], pd[0], pd[1], pd[2], pd[3], pe[0], pe[1], pe[2], pe[3]);
	}


	/**
	 * Slow exact 3D in-ortho-sphere test. Returns a positive value if the
	 * weighted point pe lies inside the ortho-sphere defined by the weighted
	 * points pa, pb, pc, and pd; a negative value if it lies outside; and zero
	 * if the five points lie on the same ortho-sphere. The points pa, pb, pc,
	 * and pd must be ordered so that they have a positive orientation (as
	 * defined by leftOfPlaneExact), or the sign of the result will be reversed.
	 */
	private static double inOrthoSphereExact(final double xa, final double ya, final double za, final double wa, final double xb, final double yb, final double zb, final double wb, final double xc,
			final double yc, final double zc, final double wc, final double xd, final double yd, final double zd, final double wd, final double xe, final double ye, final double ze, final double we)
	{
		final Two t = new Two();
		twoDiff(xa, xe, t);
		final double aex = t.x;
		final double aextail = t.y;
		twoDiff(ya, ye, t);
		final double aey = t.x;
		final double aeytail = t.y;
		twoDiff(za, ze, t);
		final double aez = t.x;
		final double aeztail = t.y;
		twoDiff(wa, we, t);
		// double aew = t.x;
		// double aewtail = t.y;
		twoDiff(xb, xe, t);
		final double bex = t.x;
		final double bextail = t.y;
		twoDiff(yb, ye, t);
		final double bey = t.x;
		final double beytail = t.y;
		twoDiff(zb, ze, t);
		final double bez = t.x;
		final double beztail = t.y;
		twoDiff(wb, we, t);
		final double bew = t.x;
		final double bewtail = t.y;
		twoDiff(xc, xe, t);
		final double cex = t.x;
		final double cextail = t.y;
		twoDiff(yc, ye, t);
		final double cey = t.x;
		final double ceytail = t.y;
		twoDiff(zc, ze, t);
		final double cez = t.x;
		final double ceztail = t.y;
		twoDiff(wc, we, t);
		final double cew = t.x;
		final double cewtail = t.y;
		twoDiff(xd, xe, t);
		final double dex = t.x;
		final double dextail = t.y;
		twoDiff(yd, ye, t);
		final double dey = t.x;
		final double deytail = t.y;
		twoDiff(zd, ze, t);
		final double dez = t.x;
		final double deztail = t.y;
		twoDiff(wd, we, t);
		final double dew = t.x;
		final double dewtail = t.y;

		final double[] axby = new double[8];
		final double[] bxay = new double[8];
		final double[] ab = new double[16];
		twoTwoProduct(aex, aextail, bey, beytail, axby);
		double negate = -aey;
		double negatetail = -aeytail;
		twoTwoProduct(bex, bextail, negate, negatetail, bxay);
		int ablen = expansionSumZeroElimFast(8, axby, 8, bxay, ab);

		final double[] bxcy = new double[8];
		final double[] cxby = new double[8];
		final double[] bc = new double[16];
		twoTwoProduct(bex, bextail, cey, ceytail, bxcy);
		negate = -bey;
		negatetail = -beytail;
		twoTwoProduct(cex, cextail, negate, negatetail, cxby);
		final int bclen = expansionSumZeroElimFast(8, bxcy, 8, cxby, bc);

		final double[] cxdy = new double[8];
		final double[] dxcy = new double[8];
		final double[] cd = new double[16];
		twoTwoProduct(cex, cextail, dey, deytail, cxdy);
		negate = -cey;
		negatetail = -ceytail;
		twoTwoProduct(dex, dextail, negate, negatetail, dxcy);
		int cdlen = expansionSumZeroElimFast(8, cxdy, 8, dxcy, cd);

		final double[] dxay = new double[8];
		final double[] axdy = new double[8];
		final double[] da = new double[16];
		twoTwoProduct(dex, dextail, aey, aeytail, dxay);
		negate = -dey;
		negatetail = -deytail;
		twoTwoProduct(aex, aextail, negate, negatetail, axdy);
		final int dalen = expansionSumZeroElimFast(8, dxay, 8, axdy, da);

		final double[] axcy = new double[8];
		final double[] cxay = new double[8];
		final double[] ac = new double[16];
		twoTwoProduct(aex, aextail, cey, ceytail, axcy);
		negate = -aey;
		negatetail = -aeytail;
		twoTwoProduct(cex, cextail, negate, negatetail, cxay);
		final int aclen = expansionSumZeroElimFast(8, axcy, 8, cxay, ac);

		final double[] bxdy = new double[8];
		final double[] dxby = new double[8];
		final double[] bd = new double[16];
		twoTwoProduct(bex, bextail, dey, deytail, bxdy);
		negate = -bey;
		negatetail = -beytail;
		twoTwoProduct(dex, dextail, negate, negatetail, dxby);
		final int bdlen = expansionSumZeroElimFast(8, bxdy, 8, dxby, bd);

		final double[] t32a = new double[32];
		final double[] t32b = new double[32];
		final double[] t64a = new double[64];
		final double[] t64b = new double[64];
		final double[] t64c = new double[64];
		final double[] t128 = new double[128];
		final double[] t192 = new double[192];
		int t32alen, t32blen, t64alen, t64blen, t64clen, t128len, t192len;
		t32alen = scaleExpansionZeroElim(cdlen, cd, -bez, t32a);
		t32blen = scaleExpansionZeroElim(cdlen, cd, -beztail, t32b);
		t64alen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64a);
		t32alen = scaleExpansionZeroElim(bdlen, bd, cez, t32a);
		t32blen = scaleExpansionZeroElim(bdlen, bd, ceztail, t32b);
		t64blen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64b);
		t32alen = scaleExpansionZeroElim(bclen, bc, -dez, t32a);
		t32blen = scaleExpansionZeroElim(bclen, bc, -deztail, t32b);
		t64clen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64c);
		t128len = expansionSumZeroElimFast(t64alen, t64a, t64blen, t64b, t128);
		t192len = expansionSumZeroElimFast(t64clen, t64c, t128len, t128, t192);

		final double[] detx = new double[384];
		final double[] detxx = new double[768];
		final double[] detxt = new double[384];
		final double[] detxxt = new double[768];
		final double[] detxtxt = new double[768];
		final double[] x1 = new double[1536];
		final double[] x2 = new double[2304];
		int xlen = scaleExpansionZeroElim(t192len, t192, aex, detx);
		int xxlen = scaleExpansionZeroElim(xlen, detx, aex, detxx);
		int xtlen = scaleExpansionZeroElim(t192len, t192, aextail, detxt);
		int xxtlen = scaleExpansionZeroElim(xtlen, detxt, aex, detxxt);
		for (int i = 0; i < xxtlen; ++i)
		{
			detxxt[i] *= 2.0;
		}
		int xtxtlen = scaleExpansionZeroElim(xtlen, detxt, aextail, detxtxt);
		int x1len = expansionSumZeroElimFast(xxlen, detxx, xxtlen, detxxt, x1);
		int x2len = expansionSumZeroElimFast(x1len, x1, xtxtlen, detxtxt, x2);

		final double[] dety = new double[384];
		final double[] detyy = new double[768];
		final double[] detyt = new double[384];
		final double[] detyyt = new double[768];
		final double[] detytyt = new double[768];
		final double[] y1 = new double[1536];
		final double[] y2 = new double[2304];
		int ylen = scaleExpansionZeroElim(t192len, t192, aey, dety);
		int yylen = scaleExpansionZeroElim(ylen, dety, aey, detyy);
		int ytlen = scaleExpansionZeroElim(t192len, t192, aeytail, detyt);
		int yytlen = scaleExpansionZeroElim(ytlen, detyt, aey, detyyt);
		for (int i = 0; i < yytlen; ++i)
		{
			detyyt[i] *= 2.0;
		}
		int ytytlen = scaleExpansionZeroElim(ytlen, detyt, aeytail, detytyt);
		int y1len = expansionSumZeroElimFast(yylen, detyy, yytlen, detyyt, y1);
		int y2len = expansionSumZeroElimFast(y1len, y1, ytytlen, detytyt, y2);

		final double[] detz = new double[384];
		final double[] detzz = new double[768];
		final double[] detzt = new double[384];
		final double[] detzzt = new double[768];
		final double[] detztzt = new double[768];
		final double[] z1 = new double[1536];
		final double[] z2 = new double[2304];
		int zlen = scaleExpansionZeroElim(t192len, t192, aez, detz);
		int zzlen = scaleExpansionZeroElim(zlen, detz, aez, detzz);
		int ztlen = scaleExpansionZeroElim(t192len, t192, aeztail, detzt);
		int zztlen = scaleExpansionZeroElim(ztlen, detzt, aez, detzzt);
		for (int i = 0; i < zztlen; ++i)
		{
			detzzt[i] *= 2.0;
		}
		int ztztlen = scaleExpansionZeroElim(ztlen, detzt, aeztail, detztzt);
		int z1len = expansionSumZeroElimFast(zzlen, detzz, zztlen, detzzt, z1);
		int z2len = expansionSumZeroElimFast(z1len, z1, ztztlen, detztzt, z2);

		final double[] detw = new double[384];
		final double[] detwt = new double[384];
		final double[] w2 = new double[768];
		int wlen = scaleExpansionZeroElim(t192len, t192, -bew, detw);
		int wtlen = scaleExpansionZeroElim(t192len, t192, -bewtail, detwt);
		int w2len = expansionSumZeroElimFast(wlen, detw, wtlen, detwt, w2);

		final double[] detxy = new double[4608];
		final double[] detxyz = new double[6912];
		final double[] adet = new double[7680];
		int xylen = expansionSumZeroElimFast(x2len, x2, y2len, y2, detxy);
		int xyzlen = expansionSumZeroElimFast(z2len, z2, xylen, detxy, detxyz);
		final int alen = expansionSumZeroElimFast(w2len, w2, xyzlen, detxyz, adet);

		t32alen = scaleExpansionZeroElim(dalen, da, cez, t32a);
		t32blen = scaleExpansionZeroElim(dalen, da, ceztail, t32b);
		t64alen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64a);
		t32alen = scaleExpansionZeroElim(aclen, ac, dez, t32a);
		t32blen = scaleExpansionZeroElim(aclen, ac, deztail, t32b);
		t64blen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64b);
		t32alen = scaleExpansionZeroElim(cdlen, cd, aez, t32a);
		t32blen = scaleExpansionZeroElim(cdlen, cd, aeztail, t32b);
		t64clen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64c);
		t128len = expansionSumZeroElimFast(t64alen, t64a, t64blen, t64b, t128);
		t192len = expansionSumZeroElimFast(t64clen, t64c, t128len, t128, t192);
		xlen = scaleExpansionZeroElim(t192len, t192, bex, detx);
		xxlen = scaleExpansionZeroElim(xlen, detx, bex, detxx);
		xtlen = scaleExpansionZeroElim(t192len, t192, bextail, detxt);
		xxtlen = scaleExpansionZeroElim(xtlen, detxt, bex, detxxt);
		for (int i = 0; i < xxtlen; ++i)
		{
			detxxt[i] *= 2.0;
		}
		xtxtlen = scaleExpansionZeroElim(xtlen, detxt, bextail, detxtxt);
		x1len = expansionSumZeroElimFast(xxlen, detxx, xxtlen, detxxt, x1);
		x2len = expansionSumZeroElimFast(x1len, x1, xtxtlen, detxtxt, x2);
		ylen = scaleExpansionZeroElim(t192len, t192, bey, dety);
		yylen = scaleExpansionZeroElim(ylen, dety, bey, detyy);
		ytlen = scaleExpansionZeroElim(t192len, t192, beytail, detyt);
		yytlen = scaleExpansionZeroElim(ytlen, detyt, bey, detyyt);
		for (int i = 0; i < yytlen; ++i)
		{
			detyyt[i] *= 2.0;
		}
		ytytlen = scaleExpansionZeroElim(ytlen, detyt, beytail, detytyt);
		y1len = expansionSumZeroElimFast(yylen, detyy, yytlen, detyyt, y1);
		y2len = expansionSumZeroElimFast(y1len, y1, ytytlen, detytyt, y2);
		zlen = scaleExpansionZeroElim(t192len, t192, bez, detz);
		zzlen = scaleExpansionZeroElim(zlen, detz, bez, detzz);
		ztlen = scaleExpansionZeroElim(t192len, t192, beztail, detzt);
		zztlen = scaleExpansionZeroElim(ztlen, detzt, bez, detzzt);
		for (int i = 0; i < zztlen; ++i)
		{
			detzzt[i] *= 2.0;
		}
		ztztlen = scaleExpansionZeroElim(ztlen, detzt, beztail, detztzt);
		z1len = expansionSumZeroElimFast(zzlen, detzz, zztlen, detzzt, z1);
		z2len = expansionSumZeroElimFast(z1len, z1, ztztlen, detztzt, z2);
		wlen = scaleExpansionZeroElim(t192len, t192, -bew, detw);
		wtlen = scaleExpansionZeroElim(t192len, t192, -bewtail, detwt);
		w2len = expansionSumZeroElimFast(wlen, detw, wtlen, detwt, w2);
		xylen = expansionSumZeroElimFast(x2len, x2, y2len, y2, detxy);
		xyzlen = expansionSumZeroElimFast(z2len, z2, xylen, detxy, detxyz);
		final double[] bdet = new double[7680];
		final int blen = expansionSumZeroElimFast(w2len, w2, xyzlen, detxyz, bdet);

		t32alen = scaleExpansionZeroElim(ablen, ab, -dez, t32a);
		t32blen = scaleExpansionZeroElim(ablen, ab, -deztail, t32b);
		t64alen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64a);
		t32alen = scaleExpansionZeroElim(bdlen, bd, -aez, t32a);
		t32blen = scaleExpansionZeroElim(bdlen, bd, -aeztail, t32b);
		t64blen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64b);
		t32alen = scaleExpansionZeroElim(dalen, da, -bez, t32a);
		t32blen = scaleExpansionZeroElim(dalen, da, -beztail, t32b);
		t64clen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64c);
		t128len = expansionSumZeroElimFast(t64alen, t64a, t64blen, t64b, t128);
		t192len = expansionSumZeroElimFast(t64clen, t64c, t128len, t128, t192);
		xlen = scaleExpansionZeroElim(t192len, t192, cex, detx);
		xxlen = scaleExpansionZeroElim(xlen, detx, cex, detxx);
		xtlen = scaleExpansionZeroElim(t192len, t192, cextail, detxt);
		xxtlen = scaleExpansionZeroElim(xtlen, detxt, cex, detxxt);
		for (int i = 0; i < xxtlen; ++i)
		{
			detxxt[i] *= 2.0;
		}
		xtxtlen = scaleExpansionZeroElim(xtlen, detxt, cextail, detxtxt);
		x1len = expansionSumZeroElimFast(xxlen, detxx, xxtlen, detxxt, x1);
		x2len = expansionSumZeroElimFast(x1len, x1, xtxtlen, detxtxt, x2);
		ylen = scaleExpansionZeroElim(t192len, t192, cey, dety);
		yylen = scaleExpansionZeroElim(ylen, dety, cey, detyy);
		ytlen = scaleExpansionZeroElim(t192len, t192, ceytail, detyt);
		yytlen = scaleExpansionZeroElim(ytlen, detyt, cey, detyyt);
		for (int i = 0; i < yytlen; ++i)
		{
			detyyt[i] *= 2.0;
		}
		ytytlen = scaleExpansionZeroElim(ytlen, detyt, ceytail, detytyt);
		y1len = expansionSumZeroElimFast(yylen, detyy, yytlen, detyyt, y1);
		y2len = expansionSumZeroElimFast(y1len, y1, ytytlen, detytyt, y2);
		zlen = scaleExpansionZeroElim(t192len, t192, cez, detz);
		zzlen = scaleExpansionZeroElim(zlen, detz, cez, detzz);
		ztlen = scaleExpansionZeroElim(t192len, t192, ceztail, detzt);
		zztlen = scaleExpansionZeroElim(ztlen, detzt, cez, detzzt);
		for (int i = 0; i < zztlen; ++i)
		{
			detzzt[i] *= 2.0;
		}
		ztztlen = scaleExpansionZeroElim(ztlen, detzt, ceztail, detztzt);
		z1len = expansionSumZeroElimFast(zzlen, detzz, zztlen, detzzt, z1);
		z2len = expansionSumZeroElimFast(z1len, z1, ztztlen, detztzt, z2);
		wlen = scaleExpansionZeroElim(t192len, t192, -cew, detw);
		wtlen = scaleExpansionZeroElim(t192len, t192, -cewtail, detwt);
		w2len = expansionSumZeroElimFast(wlen, detw, wtlen, detwt, w2);
		xylen = expansionSumZeroElimFast(x2len, x2, y2len, y2, detxy);
		xyzlen = expansionSumZeroElimFast(z2len, z2, xylen, detxy, detxyz);
		final double[] cdet = new double[7680];
		final int clen = expansionSumZeroElimFast(w2len, w2, xyzlen, detxyz, cdet);

		t32alen = scaleExpansionZeroElim(bclen, bc, aez, t32a);
		t32blen = scaleExpansionZeroElim(bclen, bc, aeztail, t32b);
		t64alen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64a);
		t32alen = scaleExpansionZeroElim(aclen, ac, -bez, t32a);
		t32blen = scaleExpansionZeroElim(aclen, ac, -beztail, t32b);
		t64blen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64b);
		t32alen = scaleExpansionZeroElim(ablen, ab, cez, t32a);
		t32blen = scaleExpansionZeroElim(ablen, ab, ceztail, t32b);
		t64clen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64c);
		t128len = expansionSumZeroElimFast(t64alen, t64a, t64blen, t64b, t128);
		t192len = expansionSumZeroElimFast(t64clen, t64c, t128len, t128, t192);
		xlen = scaleExpansionZeroElim(t192len, t192, dex, detx);
		xxlen = scaleExpansionZeroElim(xlen, detx, dex, detxx);
		xtlen = scaleExpansionZeroElim(t192len, t192, dextail, detxt);
		xxtlen = scaleExpansionZeroElim(xtlen, detxt, dex, detxxt);
		for (int i = 0; i < xxtlen; ++i)
		{
			detxxt[i] *= 2.0;
		}
		xtxtlen = scaleExpansionZeroElim(xtlen, detxt, dextail, detxtxt);
		x1len = expansionSumZeroElimFast(xxlen, detxx, xxtlen, detxxt, x1);
		x2len = expansionSumZeroElimFast(x1len, x1, xtxtlen, detxtxt, x2);
		ylen = scaleExpansionZeroElim(t192len, t192, dey, dety);
		yylen = scaleExpansionZeroElim(ylen, dety, dey, detyy);
		ytlen = scaleExpansionZeroElim(t192len, t192, deytail, detyt);
		yytlen = scaleExpansionZeroElim(ytlen, detyt, dey, detyyt);
		for (int i = 0; i < yytlen; ++i)
		{
			detyyt[i] *= 2.0;
		}
		ytytlen = scaleExpansionZeroElim(ytlen, detyt, deytail, detytyt);
		y1len = expansionSumZeroElimFast(yylen, detyy, yytlen, detyyt, y1);
		y2len = expansionSumZeroElimFast(y1len, y1, ytytlen, detytyt, y2);
		zlen = scaleExpansionZeroElim(t192len, t192, dez, detz);
		zzlen = scaleExpansionZeroElim(zlen, detz, dez, detzz);
		ztlen = scaleExpansionZeroElim(t192len, t192, deztail, detzt);
		zztlen = scaleExpansionZeroElim(ztlen, detzt, dez, detzzt);
		for (int i = 0; i < zztlen; ++i)
		{
			detzzt[i] *= 2.0;
		}
		ztztlen = scaleExpansionZeroElim(ztlen, detzt, deztail, detztzt);
		z1len = expansionSumZeroElimFast(zzlen, detzz, zztlen, detzzt, z1);
		z2len = expansionSumZeroElimFast(z1len, z1, ztztlen, detztzt, z2);
		wlen = scaleExpansionZeroElim(t192len, t192, -dew, detw);
		wtlen = scaleExpansionZeroElim(t192len, t192, -dewtail, detwt);
		w2len = expansionSumZeroElimFast(wlen, detw, wtlen, detwt, w2);
		xylen = expansionSumZeroElimFast(x2len, x2, y2len, y2, detxy);
		xyzlen = expansionSumZeroElimFast(z2len, z2, xylen, detxy, detxyz);
		final double[] ddet = new double[7680];
		final int dlen = expansionSumZeroElimFast(w2len, w2, xyzlen, detxyz, ddet);

		final double[] abdet = new double[15360];
		final double[] cddet = new double[15360];
		final double[] det = new double[30720];
		ablen = expansionSumZeroElimFast(alen, adet, blen, bdet, abdet);
		cdlen = expansionSumZeroElimFast(clen, cdet, dlen, ddet, cddet);
		final int detlen = expansionSumZeroElimFast(ablen, abdet, cdlen, cddet, det);

		return det[detlen - 1];
	}


	/**
	 * Determines whether or not a weighted point e is inside the ortho-sphere
	 * defined by the weighted points a, b, c, and d. The latter are assumed to
	 * be in CCW order, such that the method {@link #leftOfPlane} would return a
	 * positive number.
	 * <p>
	 * <em>Note: this fast method may return an incorrect result.</em>
	 */
	public static double inOrthoSphereFast(final double xa, final double ya, final double za, final double wa, final double xb, final double yb, final double zb, final double wb, final double xc,
			final double yc, final double zc, final double wc, final double xd, final double yd, final double zd, final double wd, final double xe, final double ye, final double ze, final double we)
	{
		final double aex = xa - xe;
		final double bex = xb - xe;
		final double cex = xc - xe;
		final double dex = xd - xe;
		final double aey = ya - ye;
		final double bey = yb - ye;
		final double cey = yc - ye;
		final double dey = yd - ye;
		final double aez = za - ze;
		final double bez = zb - ze;
		final double cez = zc - ze;
		final double dez = zd - ze;
		final double aew = wa - we;
		final double bew = wb - we;
		final double cew = wc - we;
		final double dew = wd - we;

		final double ab = aex * bey - bex * aey;
		final double bc = bex * cey - cex * bey;
		final double cd = cex * dey - dex * cey;
		final double da = dex * aey - aex * dey;

		final double ac = aex * cey - cex * aey;
		final double bd = bex * dey - dex * bey;

		final double abc = aez * bc - bez * ac + cez * ab;
		final double bcd = bez * cd - cez * bd + dez * bc;
		final double cda = cez * da + dez * ac + aez * cd;
		final double dab = dez * ab + aez * bd + bez * da;

		final double alift = aex * aex + aey * aey + aez * aez - aew;
		final double blift = bex * bex + bey * bey + bez * bez - bew;
		final double clift = cex * cex + cey * cey + cez * cez - cew;
		final double dlift = dex * dex + dey * dey + dez * dez - dew;

		return dlift * abc - clift * dab + (blift * cda - alift * bcd);
	}


	/**
	 * Determines whether or not a weighted point e is inside the ortho-sphere
	 * defined by the weighted points a, b, c, and d. The latter are assumed to
	 * be in CCW order, such that the method {@link #leftOfPlane} would return a
	 * positive number.
	 * <p>
	 * <em>Note: this fast method may return an incorrect result.</em>
	 */
	public static double inOrthoSphereFast(final double[] pa, final double[] pb, final double[] pc, final double[] pd, final double[] pe)
	{
		return inOrthoSphereFast(pa[0], pa[1], pa[2], pa[3], pb[0], pb[1], pb[2], pb[3], pc[0], pc[1], pc[2], pc[3], pd[0], pd[1], pd[2], pd[3], pe[0], pe[1], pe[2], pe[3]);
	}


	/**
	 * Determines whether or not a weighted point e is inside the ortho-sphere
	 * defined by the weighted points a, b, c, and d. The latter are assumed to
	 * be in CCW order, such that the method {@link #leftOfPlane} would return a
	 * positive number.
	 * <p>
	 * <em>Note: this fast method may return an incorrect result.</em>
	 */
	public static double inOrthoSphereFast(final float[] pa, final float[] pb, final float[] pc, final float[] pd, final float[] pe)
	{
		return inOrthoSphereFast(pa[0], pa[1], pa[2], pa[3], pb[0], pb[1], pb[2], pb[3], pc[0], pc[1], pc[2], pc[3], pd[0], pd[1], pd[2], pd[3], pe[0], pe[1], pe[2], pe[3]);
	}


	/**
	 * Determines if a point e is inside the sphere defined by the points a, b,
	 * c, and d. The latter are assumed to be in CCW order, such that the method
	 * {@link #leftOfPlane} would return a positive number.
	 * 
	 * @return positive, if inside the sphere; negative, if outside the sphere;
	 *         zero, otherwise.
	 */
	public static double inSphere(final double xa, final double ya, final double za, final double xb, final double yb, final double zb, final double xc, final double yc, final double zc,
			final double xd, final double yd, final double zd, final double xe, final double ye, final double ze)
	{
		final double aex = xa - xe;
		final double bex = xb - xe;
		final double cex = xc - xe;
		final double dex = xd - xe;
		final double aey = ya - ye;
		final double bey = yb - ye;
		final double cey = yc - ye;
		final double dey = yd - ye;
		double aez = za - ze;
		double bez = zb - ze;
		double cez = zc - ze;
		double dez = zd - ze;

		double aexbey = aex * bey;
		double bexaey = bex * aey;
		final double ab = aexbey - bexaey;
		double bexcey = bex * cey;
		double cexbey = cex * bey;
		final double bc = bexcey - cexbey;
		double cexdey = cex * dey;
		double dexcey = dex * cey;
		final double cd = cexdey - dexcey;
		double dexaey = dex * aey;
		double aexdey = aex * dey;
		final double da = dexaey - aexdey;

		double aexcey = aex * cey;
		double cexaey = cex * aey;
		final double ac = aexcey - cexaey;
		double bexdey = bex * dey;
		double dexbey = dex * bey;
		final double bd = bexdey - dexbey;

		final double abc = aez * bc - bez * ac + cez * ab;
		final double bcd = bez * cd - cez * bd + dez * bc;
		final double cda = cez * da + dez * ac + aez * cd;
		final double dab = dez * ab + aez * bd + bez * da;

		final double alift = aex * aex + aey * aey + aez * aez;
		final double blift = bex * bex + bey * bey + bez * bez;
		final double clift = cex * cex + cey * cey + cez * cez;
		final double dlift = dex * dex + dey * dey + dez * dez;

		final double det = dlift * abc - clift * dab + (blift * cda - alift * bcd);

		if (aez < 0.0)
		{
			aez = -aez;
		}
		if (bez < 0.0)
		{
			bez = -bez;
		}
		if (cez < 0.0)
		{
			cez = -cez;
		}
		if (dez < 0.0)
		{
			dez = -dez;
		}
		if (aexbey < 0.0)
		{
			aexbey = -aexbey;
		}
		if (bexaey < 0.0)
		{
			bexaey = -bexaey;
		}
		if (bexcey < 0.0)
		{
			bexcey = -bexcey;
		}
		if (cexbey < 0.0)
		{
			cexbey = -cexbey;
		}
		if (cexdey < 0.0)
		{
			cexdey = -cexdey;
		}
		if (dexcey < 0.0)
		{
			dexcey = -dexcey;
		}
		if (dexaey < 0.0)
		{
			dexaey = -dexaey;
		}
		if (aexdey < 0.0)
		{
			aexdey = -aexdey;
		}
		if (aexcey < 0.0)
		{
			aexcey = -aexcey;
		}
		if (cexaey < 0.0)
		{
			cexaey = -cexaey;
		}
		if (bexdey < 0.0)
		{
			bexdey = -bexdey;
		}
		if (dexbey < 0.0)
		{
			dexbey = -dexbey;
		}
		final double permanent = ((cexdey + dexcey) * bez + (dexbey + bexdey) * cez + (bexcey + cexbey) * dez) * alift
				+ ((dexaey + aexdey) * cez + (aexcey + cexaey) * dez + (cexdey + dexcey) * aez) * blift + ((aexbey + bexaey) * dez + (bexdey + dexbey) * aez + (dexaey + aexdey) * bez) * clift
				+ ((bexcey + cexbey) * aez + (cexaey + aexcey) * bez + (aexbey + bexaey) * cez) * dlift;
		final double errbound = INSERRBOUND * permanent;
		if (det > errbound || -det > errbound)
		{
			return det;
		}

		return inSphereExact(xa, ya, za, xb, yb, zb, xc, yc, zc, xd, yd, zd, xe, ye, ze);
	}


	/**
	 * Determines if a point e is inside the sphere defined by the points a, b,
	 * c, and d. The latter are assumed to be in CCW order, such that the method
	 * {@link #leftOfPlane} would return a positive number.
	 * 
	 * @return positive, if inside the sphere; negative, if outside the sphere;
	 *         zero, otherwise.
	 */
	public static double inSphere(final double[] pa, final double[] pb, final double[] pc, final double[] pd, final double[] pe)
	{
		return inSphere(pa[0], pa[1], pa[2], pb[0], pb[1], pb[2], pc[0], pc[1], pc[2], pd[0], pd[1], pd[2], pe[0], pe[1], pe[2]);
	}


	/**
	 * Determines if a point e is inside the sphere defined by the points a, b,
	 * c, and d. The latter are assumed to be in CCW order, such that the method
	 * {@link #leftOfPlane} would return a positive number.
	 * 
	 * @return positive, if inside the sphere; negative, if outside the sphere;
	 *         zero, otherwise.
	 */
	public static double inSphere(final float[] pa, final float[] pb, final float[] pc, final float[] pd, final float[] pe)
	{
		return inSphere(pa[0], pa[1], pa[2], pb[0], pb[1], pb[2], pc[0], pc[1], pc[2], pd[0], pd[1], pd[2], pe[0], pe[1], pe[2]);
	}


	/**
	 * Slow exact 3D in-sphere test. Returns a positive value if the point pe
	 * lies inside the sphere passing through pa, pb, pc, and pd; a negative
	 * value if it lies outside; and zero if the five points are cospherical.
	 * The points pa, pb, pc, and pd must be ordered so that they have a
	 * positive orientation (as defined by leftOfPlaneExact), or the sign of the
	 * result will be reversed.
	 */
	private static double inSphereExact(final double xa, final double ya, final double za, final double xb, final double yb, final double zb, final double xc, final double yc, final double zc,
			final double xd, final double yd, final double zd, final double xe, final double ye, final double ze)
	{
		final Two t = new Two();
		twoDiff(xa, xe, t);
		final double aex = t.x;
		final double aextail = t.y;
		twoDiff(ya, ye, t);
		final double aey = t.x;
		final double aeytail = t.y;
		twoDiff(za, ze, t);
		final double aez = t.x;
		final double aeztail = t.y;
		twoDiff(xb, xe, t);
		final double bex = t.x;
		final double bextail = t.y;
		twoDiff(yb, ye, t);
		final double bey = t.x;
		final double beytail = t.y;
		twoDiff(zb, ze, t);
		final double bez = t.x;
		final double beztail = t.y;
		twoDiff(xc, xe, t);
		final double cex = t.x;
		final double cextail = t.y;
		twoDiff(yc, ye, t);
		final double cey = t.x;
		final double ceytail = t.y;
		twoDiff(zc, ze, t);
		final double cez = t.x;
		final double ceztail = t.y;
		twoDiff(xd, xe, t);
		final double dex = t.x;
		final double dextail = t.y;
		twoDiff(yd, ye, t);
		final double dey = t.x;
		final double deytail = t.y;
		twoDiff(zd, ze, t);
		final double dez = t.x;
		final double deztail = t.y;

		final double[] axby = new double[8];
		final double[] bxay = new double[8];
		final double[] ab = new double[16];
		twoTwoProduct(aex, aextail, bey, beytail, axby);
		double negate = -aey;
		double negatetail = -aeytail;
		twoTwoProduct(bex, bextail, negate, negatetail, bxay);
		int ablen = expansionSumZeroElimFast(8, axby, 8, bxay, ab);

		final double[] bxcy = new double[8];
		final double[] cxby = new double[8];
		final double[] bc = new double[16];
		twoTwoProduct(bex, bextail, cey, ceytail, bxcy);
		negate = -bey;
		negatetail = -beytail;
		twoTwoProduct(cex, cextail, negate, negatetail, cxby);
		final int bclen = expansionSumZeroElimFast(8, bxcy, 8, cxby, bc);

		final double[] cxdy = new double[8];
		final double[] dxcy = new double[8];
		final double[] cd = new double[16];
		twoTwoProduct(cex, cextail, dey, deytail, cxdy);
		negate = -cey;
		negatetail = -ceytail;
		twoTwoProduct(dex, dextail, negate, negatetail, dxcy);
		int cdlen = expansionSumZeroElimFast(8, cxdy, 8, dxcy, cd);

		final double[] dxay = new double[8];
		final double[] axdy = new double[8];
		final double[] da = new double[16];
		twoTwoProduct(dex, dextail, aey, aeytail, dxay);
		negate = -dey;
		negatetail = -deytail;
		twoTwoProduct(aex, aextail, negate, negatetail, axdy);
		final int dalen = expansionSumZeroElimFast(8, dxay, 8, axdy, da);

		final double[] axcy = new double[8];
		final double[] cxay = new double[8];
		final double[] ac = new double[16];
		twoTwoProduct(aex, aextail, cey, ceytail, axcy);
		negate = -aey;
		negatetail = -aeytail;
		twoTwoProduct(cex, cextail, negate, negatetail, cxay);
		final int aclen = expansionSumZeroElimFast(8, axcy, 8, cxay, ac);

		final double[] bxdy = new double[8];
		final double[] dxby = new double[8];
		final double[] bd = new double[16];
		twoTwoProduct(bex, bextail, dey, deytail, bxdy);
		negate = -bey;
		negatetail = -beytail;
		twoTwoProduct(dex, dextail, negate, negatetail, dxby);
		final int bdlen = expansionSumZeroElimFast(8, bxdy, 8, dxby, bd);

		final double[] t32a = new double[32];
		final double[] t32b = new double[32];
		final double[] t64a = new double[64];
		final double[] t64b = new double[64];
		final double[] t64c = new double[64];
		final double[] t128 = new double[128];
		final double[] t192 = new double[192];
		int t32alen, t32blen, t64alen, t64blen, t64clen, t128len, t192len;
		t32alen = scaleExpansionZeroElim(cdlen, cd, -bez, t32a);
		t32blen = scaleExpansionZeroElim(cdlen, cd, -beztail, t32b);
		t64alen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64a);
		t32alen = scaleExpansionZeroElim(bdlen, bd, cez, t32a);
		t32blen = scaleExpansionZeroElim(bdlen, bd, ceztail, t32b);
		t64blen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64b);
		t32alen = scaleExpansionZeroElim(bclen, bc, -dez, t32a);
		t32blen = scaleExpansionZeroElim(bclen, bc, -deztail, t32b);
		t64clen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64c);
		t128len = expansionSumZeroElimFast(t64alen, t64a, t64blen, t64b, t128);
		t192len = expansionSumZeroElimFast(t64clen, t64c, t128len, t128, t192);

		final double[] detx = new double[384];
		final double[] detxx = new double[768];
		final double[] detxt = new double[384];
		final double[] detxxt = new double[768];
		final double[] detxtxt = new double[768];
		final double[] x1 = new double[1536];
		final double[] x2 = new double[2304];
		int xlen = scaleExpansionZeroElim(t192len, t192, aex, detx);
		int xxlen = scaleExpansionZeroElim(xlen, detx, aex, detxx);
		int xtlen = scaleExpansionZeroElim(t192len, t192, aextail, detxt);
		int xxtlen = scaleExpansionZeroElim(xtlen, detxt, aex, detxxt);
		for (int i = 0; i < xxtlen; ++i)
		{
			detxxt[i] *= 2.0;
		}
		int xtxtlen = scaleExpansionZeroElim(xtlen, detxt, aextail, detxtxt);
		int x1len = expansionSumZeroElimFast(xxlen, detxx, xxtlen, detxxt, x1);
		int x2len = expansionSumZeroElimFast(x1len, x1, xtxtlen, detxtxt, x2);

		final double[] dety = new double[384];
		final double[] detyy = new double[768];
		final double[] detyt = new double[384];
		final double[] detyyt = new double[768];
		final double[] detytyt = new double[768];
		final double[] y1 = new double[1536];
		final double[] y2 = new double[2304];
		int ylen = scaleExpansionZeroElim(t192len, t192, aey, dety);
		int yylen = scaleExpansionZeroElim(ylen, dety, aey, detyy);
		int ytlen = scaleExpansionZeroElim(t192len, t192, aeytail, detyt);
		int yytlen = scaleExpansionZeroElim(ytlen, detyt, aey, detyyt);
		for (int i = 0; i < yytlen; ++i)
		{
			detyyt[i] *= 2.0;
		}
		int ytytlen = scaleExpansionZeroElim(ytlen, detyt, aeytail, detytyt);
		int y1len = expansionSumZeroElimFast(yylen, detyy, yytlen, detyyt, y1);
		int y2len = expansionSumZeroElimFast(y1len, y1, ytytlen, detytyt, y2);

		final double[] detz = new double[384];
		final double[] detzz = new double[768];
		final double[] detzt = new double[384];
		final double[] detzzt = new double[768];
		final double[] detztzt = new double[768];
		final double[] z1 = new double[1536];
		final double[] z2 = new double[2304];
		int zlen = scaleExpansionZeroElim(t192len, t192, aez, detz);
		int zzlen = scaleExpansionZeroElim(zlen, detz, aez, detzz);
		int ztlen = scaleExpansionZeroElim(t192len, t192, aeztail, detzt);
		int zztlen = scaleExpansionZeroElim(ztlen, detzt, aez, detzzt);
		for (int i = 0; i < zztlen; ++i)
		{
			detzzt[i] *= 2.0;
		}
		int ztztlen = scaleExpansionZeroElim(ztlen, detzt, aeztail, detztzt);
		int z1len = expansionSumZeroElimFast(zzlen, detzz, zztlen, detzzt, z1);
		int z2len = expansionSumZeroElimFast(z1len, z1, ztztlen, detztzt, z2);

		final double[] detxy = new double[4608];
		final double[] adet = new double[6912];
		final double[] bdet = new double[6912];
		final double[] cdet = new double[6912];
		final double[] ddet = new double[6912];
		int xylen = expansionSumZeroElimFast(x2len, x2, y2len, y2, detxy);
		final int alen = expansionSumZeroElimFast(z2len, z2, xylen, detxy, adet);

		t32alen = scaleExpansionZeroElim(dalen, da, cez, t32a);
		t32blen = scaleExpansionZeroElim(dalen, da, ceztail, t32b);
		t64alen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64a);
		t32alen = scaleExpansionZeroElim(aclen, ac, dez, t32a);
		t32blen = scaleExpansionZeroElim(aclen, ac, deztail, t32b);
		t64blen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64b);
		t32alen = scaleExpansionZeroElim(cdlen, cd, aez, t32a);
		t32blen = scaleExpansionZeroElim(cdlen, cd, aeztail, t32b);
		t64clen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64c);
		t128len = expansionSumZeroElimFast(t64alen, t64a, t64blen, t64b, t128);
		t192len = expansionSumZeroElimFast(t64clen, t64c, t128len, t128, t192);
		xlen = scaleExpansionZeroElim(t192len, t192, bex, detx);
		xxlen = scaleExpansionZeroElim(xlen, detx, bex, detxx);
		xtlen = scaleExpansionZeroElim(t192len, t192, bextail, detxt);
		xxtlen = scaleExpansionZeroElim(xtlen, detxt, bex, detxxt);
		for (int i = 0; i < xxtlen; ++i)
		{
			detxxt[i] *= 2.0;
		}
		xtxtlen = scaleExpansionZeroElim(xtlen, detxt, bextail, detxtxt);
		x1len = expansionSumZeroElimFast(xxlen, detxx, xxtlen, detxxt, x1);
		x2len = expansionSumZeroElimFast(x1len, x1, xtxtlen, detxtxt, x2);
		ylen = scaleExpansionZeroElim(t192len, t192, bey, dety);
		yylen = scaleExpansionZeroElim(ylen, dety, bey, detyy);
		ytlen = scaleExpansionZeroElim(t192len, t192, beytail, detyt);
		yytlen = scaleExpansionZeroElim(ytlen, detyt, bey, detyyt);
		for (int i = 0; i < yytlen; ++i)
		{
			detyyt[i] *= 2.0;
		}
		ytytlen = scaleExpansionZeroElim(ytlen, detyt, beytail, detytyt);
		y1len = expansionSumZeroElimFast(yylen, detyy, yytlen, detyyt, y1);
		y2len = expansionSumZeroElimFast(y1len, y1, ytytlen, detytyt, y2);
		zlen = scaleExpansionZeroElim(t192len, t192, bez, detz);
		zzlen = scaleExpansionZeroElim(zlen, detz, bez, detzz);
		ztlen = scaleExpansionZeroElim(t192len, t192, beztail, detzt);
		zztlen = scaleExpansionZeroElim(ztlen, detzt, bez, detzzt);
		for (int i = 0; i < zztlen; ++i)
		{
			detzzt[i] *= 2.0;
		}
		ztztlen = scaleExpansionZeroElim(ztlen, detzt, beztail, detztzt);
		z1len = expansionSumZeroElimFast(zzlen, detzz, zztlen, detzzt, z1);
		z2len = expansionSumZeroElimFast(z1len, z1, ztztlen, detztzt, z2);
		xylen = expansionSumZeroElimFast(x2len, x2, y2len, y2, detxy);
		final int blen = expansionSumZeroElimFast(z2len, z2, xylen, detxy, bdet);

		t32alen = scaleExpansionZeroElim(ablen, ab, -dez, t32a);
		t32blen = scaleExpansionZeroElim(ablen, ab, -deztail, t32b);
		t64alen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64a);
		t32alen = scaleExpansionZeroElim(bdlen, bd, -aez, t32a);
		t32blen = scaleExpansionZeroElim(bdlen, bd, -aeztail, t32b);
		t64blen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64b);
		t32alen = scaleExpansionZeroElim(dalen, da, -bez, t32a);
		t32blen = scaleExpansionZeroElim(dalen, da, -beztail, t32b);
		t64clen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64c);
		t128len = expansionSumZeroElimFast(t64alen, t64a, t64blen, t64b, t128);
		t192len = expansionSumZeroElimFast(t64clen, t64c, t128len, t128, t192);
		xlen = scaleExpansionZeroElim(t192len, t192, cex, detx);
		xxlen = scaleExpansionZeroElim(xlen, detx, cex, detxx);
		xtlen = scaleExpansionZeroElim(t192len, t192, cextail, detxt);
		xxtlen = scaleExpansionZeroElim(xtlen, detxt, cex, detxxt);
		for (int i = 0; i < xxtlen; ++i)
		{
			detxxt[i] *= 2.0;
		}
		xtxtlen = scaleExpansionZeroElim(xtlen, detxt, cextail, detxtxt);
		x1len = expansionSumZeroElimFast(xxlen, detxx, xxtlen, detxxt, x1);
		x2len = expansionSumZeroElimFast(x1len, x1, xtxtlen, detxtxt, x2);
		ylen = scaleExpansionZeroElim(t192len, t192, cey, dety);
		yylen = scaleExpansionZeroElim(ylen, dety, cey, detyy);
		ytlen = scaleExpansionZeroElim(t192len, t192, ceytail, detyt);
		yytlen = scaleExpansionZeroElim(ytlen, detyt, cey, detyyt);
		for (int i = 0; i < yytlen; ++i)
		{
			detyyt[i] *= 2.0;
		}
		ytytlen = scaleExpansionZeroElim(ytlen, detyt, ceytail, detytyt);
		y1len = expansionSumZeroElimFast(yylen, detyy, yytlen, detyyt, y1);
		y2len = expansionSumZeroElimFast(y1len, y1, ytytlen, detytyt, y2);
		zlen = scaleExpansionZeroElim(t192len, t192, cez, detz);
		zzlen = scaleExpansionZeroElim(zlen, detz, cez, detzz);
		ztlen = scaleExpansionZeroElim(t192len, t192, ceztail, detzt);
		zztlen = scaleExpansionZeroElim(ztlen, detzt, cez, detzzt);
		for (int i = 0; i < zztlen; ++i)
		{
			detzzt[i] *= 2.0;
		}
		ztztlen = scaleExpansionZeroElim(ztlen, detzt, ceztail, detztzt);
		z1len = expansionSumZeroElimFast(zzlen, detzz, zztlen, detzzt, z1);
		z2len = expansionSumZeroElimFast(z1len, z1, ztztlen, detztzt, z2);
		xylen = expansionSumZeroElimFast(x2len, x2, y2len, y2, detxy);
		final int clen = expansionSumZeroElimFast(z2len, z2, xylen, detxy, cdet);

		t32alen = scaleExpansionZeroElim(bclen, bc, aez, t32a);
		t32blen = scaleExpansionZeroElim(bclen, bc, aeztail, t32b);
		t64alen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64a);
		t32alen = scaleExpansionZeroElim(aclen, ac, -bez, t32a);
		t32blen = scaleExpansionZeroElim(aclen, ac, -beztail, t32b);
		t64blen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64b);
		t32alen = scaleExpansionZeroElim(ablen, ab, cez, t32a);
		t32blen = scaleExpansionZeroElim(ablen, ab, ceztail, t32b);
		t64clen = expansionSumZeroElimFast(t32alen, t32a, t32blen, t32b, t64c);
		t128len = expansionSumZeroElimFast(t64alen, t64a, t64blen, t64b, t128);
		t192len = expansionSumZeroElimFast(t64clen, t64c, t128len, t128, t192);
		xlen = scaleExpansionZeroElim(t192len, t192, dex, detx);
		xxlen = scaleExpansionZeroElim(xlen, detx, dex, detxx);
		xtlen = scaleExpansionZeroElim(t192len, t192, dextail, detxt);
		xxtlen = scaleExpansionZeroElim(xtlen, detxt, dex, detxxt);
		for (int i = 0; i < xxtlen; ++i)
		{
			detxxt[i] *= 2.0;
		}
		xtxtlen = scaleExpansionZeroElim(xtlen, detxt, dextail, detxtxt);
		x1len = expansionSumZeroElimFast(xxlen, detxx, xxtlen, detxxt, x1);
		x2len = expansionSumZeroElimFast(x1len, x1, xtxtlen, detxtxt, x2);
		ylen = scaleExpansionZeroElim(t192len, t192, dey, dety);
		yylen = scaleExpansionZeroElim(ylen, dety, dey, detyy);
		ytlen = scaleExpansionZeroElim(t192len, t192, deytail, detyt);
		yytlen = scaleExpansionZeroElim(ytlen, detyt, dey, detyyt);
		for (int i = 0; i < yytlen; ++i)
		{
			detyyt[i] *= 2.0;
		}
		ytytlen = scaleExpansionZeroElim(ytlen, detyt, deytail, detytyt);
		y1len = expansionSumZeroElimFast(yylen, detyy, yytlen, detyyt, y1);
		y2len = expansionSumZeroElimFast(y1len, y1, ytytlen, detytyt, y2);
		zlen = scaleExpansionZeroElim(t192len, t192, dez, detz);
		zzlen = scaleExpansionZeroElim(zlen, detz, dez, detzz);
		ztlen = scaleExpansionZeroElim(t192len, t192, deztail, detzt);
		zztlen = scaleExpansionZeroElim(ztlen, detzt, dez, detzzt);
		for (int i = 0; i < zztlen; ++i)
		{
			detzzt[i] *= 2.0;
		}
		ztztlen = scaleExpansionZeroElim(ztlen, detzt, deztail, detztzt);
		z1len = expansionSumZeroElimFast(zzlen, detzz, zztlen, detzzt, z1);
		z2len = expansionSumZeroElimFast(z1len, z1, ztztlen, detztzt, z2);
		xylen = expansionSumZeroElimFast(x2len, x2, y2len, y2, detxy);
		final int dlen = expansionSumZeroElimFast(z2len, z2, xylen, detxy, ddet);

		final double[] abdet = new double[13824];
		final double[] cddet = new double[13824];
		final double[] det = new double[27648];
		ablen = expansionSumZeroElimFast(alen, adet, blen, bdet, abdet);
		cdlen = expansionSumZeroElimFast(clen, cdet, dlen, ddet, cddet);
		final int detlen = expansionSumZeroElimFast(ablen, abdet, cdlen, cddet, det);

		return det[detlen - 1];
	}


	/**
	 * Determines if a point e is inside the sphere defined by the points a, b,
	 * c, and d. The latter are assumed to be in CCW order, such that the method
	 * {@link #leftOfPlane} would return a positive number.
	 * <p>
	 * <em>Note: this fast method may return an incorrect result.</em>
	 * 
	 * @return positive, if inside the sphere; negative, if outside the sphere;
	 *         zero, otherwise.
	 */
	public static double inSphereFast(final double xa, final double ya, final double za, final double xb, final double yb, final double zb, final double xc, final double yc, final double zc,
			final double xd, final double yd, final double zd, final double xe, final double ye, final double ze)
	{
		final double aex = xa - xe;
		final double bex = xb - xe;
		final double cex = xc - xe;
		final double dex = xd - xe;
		final double aey = ya - ye;
		final double bey = yb - ye;
		final double cey = yc - ye;
		final double dey = yd - ye;
		final double aez = za - ze;
		final double bez = zb - ze;
		final double cez = zc - ze;
		final double dez = zd - ze;

		final double ab = aex * bey - bex * aey;
		final double bc = bex * cey - cex * bey;
		final double cd = cex * dey - dex * cey;
		final double da = dex * aey - aex * dey;

		final double ac = aex * cey - cex * aey;
		final double bd = bex * dey - dex * bey;

		final double abc = aez * bc - bez * ac + cez * ab;
		final double bcd = bez * cd - cez * bd + dez * bc;
		final double cda = cez * da + dez * ac + aez * cd;
		final double dab = dez * ab + aez * bd + bez * da;

		final double alift = aex * aex + aey * aey + aez * aez;
		final double blift = bex * bex + bey * bey + bez * bez;
		final double clift = cex * cex + cey * cey + cez * cez;
		final double dlift = dex * dex + dey * dey + dez * dez;

		return dlift * abc - clift * dab + (blift * cda - alift * bcd);
	}


	/**
	 * Determines if a point e is inside the sphere defined by the points a, b,
	 * c, and d. The latter are assumed to be in CCW order, such that the method
	 * {@link #leftOfPlane} would return a positive number.
	 * <p>
	 * <em>Note: this fast method may return an incorrect result.</em>
	 * 
	 * @return positive, if inside the sphere; negative, if outside the sphere;
	 *         zero, otherwise.
	 */
	public static double inSphereFast(final double[] pa, final double[] pb, final double[] pc, final double[] pd, final double[] pe)
	{
		return inSphereFast(pa[0], pa[1], pa[2], pb[0], pb[1], pb[2], pc[0], pc[1], pc[2], pd[0], pd[1], pd[2], pe[0], pe[1], pe[2]);
	}

	///////////////////////////////////////////////////////////////////////////
	// private


	///////////////////////////////////////////////////////////////////////////
	// Java implementation of Jonathan Shewchuk's functions for arbitrary
	// floating-point arithmetic and fast robust geometric predicates.
	// Only Shewchuk's "slow" exact methods are implemented here.
	// If the methods above lack sufficient precision, then they call
	// the slow methods below. This is equivalent to using only stages A
	// and D of Shewchuk's adaptive methods with stages A, B, C, and D.
	// Note that the error bounds used here to determine whether an fast
	// method is accurate are simpler and more conservative than Shewchuk's.

	/**
	 * Determines if a point e is inside the sphere defined by the points a, b,
	 * c, and d. The latter are assumed to be in CCW order, such that the method
	 * {@link #leftOfPlane} would return a positive number.
	 * <p>
	 * <em>Note: this fast method may return an incorrect result.</em>
	 * 
	 * @return positive, if inside the sphere; negative, if outside the sphere;
	 *         zero, otherwise.
	 */
	public static double inSphereFast(final float[] pa, final float[] pb, final float[] pc, final float[] pd, final float[] pe)
	{
		return inSphereFast(pa[0], pa[1], pa[2], pb[0], pb[1], pb[2], pc[0], pc[1], pc[2], pd[0], pd[1], pd[2], pe[0], pe[1], pe[2]);
	}


	/**
	 * Determines if a point c is left of the line defined by the points a and
	 * b. This is equivalent to determining whether the points a, b, and c are
	 * in counter-clockwise (CCW) order.
	 * 
	 * @return positive, if left of line; negative, if right of line; zero,
	 *         otherwise.
	 */
	public static double leftOfLine(final double xa, final double ya, final double xb, final double yb, final double xc, final double yc)
	{
		final double detleft = (xa - xc) * (yb - yc);
		final double detright = (ya - yc) * (xb - xc);
		final double det = detleft - detright;
		double detsum;
		if (detleft > 0.0)
		{
			if (detright <= 0.0)
			{
				return det;
			}
			else
			{
				detsum = detleft + detright;
			}
		}
		else if (detleft < 0.0)
		{
			if (detright >= 0.0)
			{
				return det;
			}
			else
			{
				detsum = -detleft - detright;
			}
		}
		else
		{
			return det;
		}
		final double errbound = O2DERRBOUND * detsum;
		if (det >= errbound || -det >= errbound)
		{
			return det;
		}

		return leftOfLineExact(xa, ya, xb, yb, xc, yc);
	}


	/**
	 * Determines if a point c is left of the line defined by the points a and
	 * b. This is equivalent to determining whether the points a, b, and c are
	 * in counter-clockwise (CCW) order.
	 * 
	 * @return positive, if left of line; negative, if right of line; zero,
	 *         otherwise.
	 */
	public static double leftOfLine(final double[] pa, final double[] pb, final double[] pc)
	{
		return leftOfLine(pa[0], pa[1], pb[0], pb[1], pc[0], pc[1]);
	}


	/**
	 * Determines if a point c is left of the line defined by the points a and
	 * b. This is equivalent to determining whether the points a, b, and c are
	 * in counter-clockwise (CCW) order.
	 * 
	 * @return positive, if left of line; negative, if right of line; zero,
	 *         otherwise.
	 */
	public static double leftOfLine(final float[] pa, final float[] pb, final float[] pc)
	{
		return leftOfLine(pa[0], pa[1], pb[0], pb[1], pc[0], pc[1]);
	}


	/**
	 * Slow exact 2D orientation test. Returns a positive value if the points
	 * pa, pb, and pc occur in counterclockwise order; a negative value if they
	 * occur in clockwise order; and zero if they are collinear. The result is
	 * also a rough approximation of twice the signed area of the triangle
	 * defined by the three points.
	 */
	private static double leftOfLineExact(final double xa, final double ya, final double xb, final double yb, final double xc, final double yc)
	{
		final Two t = new Two();
		twoDiff(xa, xc, t);
		final double acx = t.x;
		final double acxtail = t.y;
		twoDiff(ya, yc, t);
		final double acy = t.x;
		final double acytail = t.y;
		twoDiff(xb, xc, t);
		final double bcx = t.x;
		final double bcxtail = t.y;
		twoDiff(yb, yc, t);
		final double bcy = t.x;
		final double bcytail = t.y;

		final double[] axby = new double[8];
		final double[] bxay = new double[8];
		twoTwoProduct(acx, acxtail, bcy, bcytail, axby);
		final double negate = -acy;
		final double negatetail = -acytail;
		twoTwoProduct(bcx, bcxtail, negate, negatetail, bxay);

		final double[] det = new double[16];
		final int detlen = expansionSumZeroElimFast(8, axby, 8, bxay, det);

		return det[detlen - 1];
	}


	/**
	 * Determines if a point c is left of the line defined by the points a and
	 * b. This is equivalent to determining whether the points a, b, and c are
	 * in counter-clockwise (CCW) order.
	 * <p>
	 * <em>Note: this fast method may return an incorrect result.</em>
	 * 
	 * @return positive, if left of line; negative, if right of line; zero,
	 *         otherwise.
	 */
	public static double leftOfLineFast(final double xa, final double ya, final double xb, final double yb, final double xc, final double yc)
	{
		final double acx = xa - xc;
		final double bcx = xb - xc;
		final double acy = ya - yc;
		final double bcy = yb - yc;
		return acx * bcy - acy * bcx;
	}


	/**
	 * Determines if a point c is left of the line defined by the points a and
	 * b. This is equivalent to determining whether the points a, b, and c are
	 * in counter-clockwise (CCW) order.
	 * <p>
	 * <em>Note: this fast method may return an incorrect result.</em>
	 * 
	 * @return positive, if left of line; negative, if right of line; zero,
	 *         otherwise.
	 */
	public static double leftOfLineFast(final double[] pa, final double[] pb, final double[] pc)
	{
		return leftOfLineFast(pa[0], pa[1], pb[0], pb[1], pc[0], pc[1]);
	}


	/**
	 * Determines if a point c is left of the line defined by the points a and
	 * b. This is equivalent to determining whether the points a, b, and c are
	 * in counter-clockwise (CCW) order.
	 * <p>
	 * <em>Note: this fast method may return an incorrect result.</em>
	 * 
	 * @return positive, if left of line; negative, if right of line; zero,
	 *         otherwise.
	 */
	public static double leftOfLineFast(final float[] pa, final float[] pb, final float[] pc)
	{
		return leftOfLineFast(pa[0], pa[1], pb[0], pb[1], pc[0], pc[1]);
	}


	/**
	 * Determines if a point d is left of the plane defined by the points a, b,
	 * and c. The latter are assumed to be in CCW order, as viewed from the
	 * right side of the plane.
	 * 
	 * @return positive, if left of plane; negative, if right of plane; zero,
	 *         otherwise.
	 */
	public static double leftOfPlane(final double xa, final double ya, final double za, final double xb, final double yb, final double zb, final double xc, final double yc, final double zc,
			final double xd, final double yd, final double zd)
	{
		final double adx = xa - xd;
		final double bdx = xb - xd;
		final double cdx = xc - xd;
		final double ady = ya - yd;
		final double bdy = yb - yd;
		final double cdy = yc - yd;
		double adz = za - zd;
		double bdz = zb - zd;
		double cdz = zc - zd;

		double bdxcdy = bdx * cdy;
		double cdxbdy = cdx * bdy;

		double cdxady = cdx * ady;
		double adxcdy = adx * cdy;

		double adxbdy = adx * bdy;
		double bdxady = bdx * ady;

		final double det = adz * (bdxcdy - cdxbdy) + bdz * (cdxady - adxcdy) + cdz * (adxbdy - bdxady);

		if (adz < 0.0)
		{
			adz = -adz;
		}
		if (bdz < 0.0)
		{
			bdz = -bdz;
		}
		if (cdz < 0.0)
		{
			cdz = -cdz;
		}
		if (bdxcdy < 0.0)
		{
			bdxcdy = -bdxcdy;
		}
		if (cdxbdy < 0.0)
		{
			cdxbdy = -cdxbdy;
		}
		if (cdxady < 0.0)
		{
			cdxady = -cdxady;
		}
		if (adxcdy < 0.0)
		{
			adxcdy = -adxcdy;
		}
		if (adxbdy < 0.0)
		{
			adxbdy = -adxbdy;
		}
		if (bdxady < 0.0)
		{
			bdxady = -bdxady;
		}
		final double permanent = (bdxcdy + cdxbdy) * adz + (cdxady + adxcdy) * bdz + (adxbdy + bdxady) * cdz;
		final double errbound = O3DERRBOUND * permanent;
		if (det > errbound || -det > errbound)
		{
			return det;
		}

		return leftOfPlaneExact(xa, ya, za, xb, yb, zb, xc, yc, zc, xd, yd, zd);
	}


	/**
	 * Determines if a point d is left of the plane defined by the points a, b,
	 * and c. The latter are assumed to be in CCW order, as viewed from the
	 * right side of the plane.
	 * 
	 * @return positive, if left of plane; negative, if right of plane; zero,
	 *         otherwise.
	 */
	public static double leftOfPlane(final double[] pa, final double[] pb, final double[] pc, final double[] pd)
	{
		return leftOfPlane(pa[0], pa[1], pa[2], pb[0], pb[1], pb[2], pc[0], pc[1], pc[2], pd[0], pd[1], pd[2]);
	}


	/**
	 * Determines if a point d is left of the plane defined by the points a, b,
	 * and c. The latter are assumed to be in CCW order, as viewed from the
	 * right side of the plane.
	 * 
	 * @return positive, if left of plane; negative, if right of plane; zero,
	 *         otherwise.
	 */
	public static double leftOfPlane(final float[] pa, final float[] pb, final float[] pc, final float[] pd)
	{
		return leftOfPlane(pa[0], pa[1], pa[2], pb[0], pb[1], pb[2], pc[0], pc[1], pc[2], pd[0], pd[1], pd[2]);
	}


	/**
	 * Slow exact 3D orientation test. Returns a positive value if the point d
	 * lies left of the plane passing through pa, pb, and pc; here, "left" is
	 * defined so that pa, pb, and pc appear in counterclockwise order when
	 * viewed from right of the plane. Returns zero if the points are coplanar.
	 * The result is also a rough approximation of six times the signed volume
	 * of the tetrahedron defined by the four points.
	 */
	private static double leftOfPlaneExact(final double xa, final double ya, final double za, final double xb, final double yb, final double zb, final double xc, final double yc, final double zc,
			final double xd, final double yd, final double zd)
	{
		final Two t = new Two();
		twoDiff(xa, xd, t);
		final double adx = t.x;
		final double adxtail = t.y;
		twoDiff(ya, yd, t);
		final double ady = t.x;
		final double adytail = t.y;
		twoDiff(za, zd, t);
		final double adz = t.x;
		final double adztail = t.y;
		twoDiff(xb, xd, t);
		final double bdx = t.x;
		final double bdxtail = t.y;
		twoDiff(yb, yd, t);
		final double bdy = t.x;
		final double bdytail = t.y;
		twoDiff(zb, zd, t);
		final double bdz = t.x;
		final double bdztail = t.y;
		twoDiff(xc, xd, t);
		final double cdx = t.x;
		final double cdxtail = t.y;
		twoDiff(yc, yd, t);
		final double cdy = t.x;
		final double cdytail = t.y;
		twoDiff(zc, zd, t);
		final double cdz = t.x;
		final double cdztail = t.y;

		final double[] axby = new double[8];
		twoTwoProduct(adx, adxtail, bdy, bdytail, axby);
		double negate = -ady;
		double negatetail = -adytail;
		final double[] bxay = new double[8];
		twoTwoProduct(bdx, bdxtail, negate, negatetail, bxay);

		final double[] bxcy = new double[8];
		twoTwoProduct(bdx, bdxtail, cdy, cdytail, bxcy);
		negate = -bdy;
		negatetail = -bdytail;
		final double[] cxby = new double[8];
		twoTwoProduct(cdx, cdxtail, negate, negatetail, cxby);

		final double[] cxay = new double[8];
		twoTwoProduct(cdx, cdxtail, ady, adytail, cxay);
		negate = -cdy;
		negatetail = -cdytail;
		final double[] axcy = new double[8];
		twoTwoProduct(adx, adxtail, negate, negatetail, axcy);

		final double[] t16 = new double[16];
		final double[] t32 = new double[32];
		final double[] t32t = new double[32];
		int t16len, t32len, t32tlen;

		t16len = expansionSumZeroElimFast(8, bxcy, 8, cxby, t16);
		t32len = scaleExpansionZeroElim(t16len, t16, adz, t32);
		t32tlen = scaleExpansionZeroElim(t16len, t16, adztail, t32t);
		final double[] adet = new double[64];
		final int alen = expansionSumZeroElimFast(t32len, t32, t32tlen, t32t, adet);

		t16len = expansionSumZeroElimFast(8, cxay, 8, axcy, t16);
		t32len = scaleExpansionZeroElim(t16len, t16, bdz, t32);
		t32tlen = scaleExpansionZeroElim(t16len, t16, bdztail, t32t);
		final double[] bdet = new double[64];
		final int blen = expansionSumZeroElimFast(t32len, t32, t32tlen, t32t, bdet);

		t16len = expansionSumZeroElimFast(8, axby, 8, bxay, t16);
		t32len = scaleExpansionZeroElim(t16len, t16, cdz, t32);
		t32tlen = scaleExpansionZeroElim(t16len, t16, cdztail, t32t);
		final double[] cdet = new double[64];
		final int clen = expansionSumZeroElimFast(t32len, t32, t32tlen, t32t, cdet);

		final double[] abdet = new double[128];
		final int ablen = expansionSumZeroElimFast(alen, adet, blen, bdet, abdet);
		final double[] det = new double[192];
		final int detlen = expansionSumZeroElimFast(ablen, abdet, clen, cdet, det);

		return det[detlen - 1];
	}


	/**
	 * Computes difference a-b, assuming that |a|&gt;=|b|. Puts result in x and
	 * error in y.
	 */
	/*
	 * private strictfp static void twoDiffFast(double a, double b, Two t) {
	 * double x = a-b; double bvirt = a-x; t.x = x; t.y = bvirt-b; }
	 */

	/**
	 * Determines if a point d is left of the plane defined by the points a, b,
	 * and c. The latter are assumed to be in CCW order, as viewed from the
	 * right side of the plane.
	 * <p>
	 * <em>Note: this fast method may return an incorrect result.</em>
	 * 
	 * @return positive, if left of plane; negative, if right of plane; zero,
	 *         otherwise.
	 */
	public static double leftOfPlaneFast(final double xa, final double ya, final double za, final double xb, final double yb, final double zb, final double xc, final double yc, final double zc,
			final double xd, final double yd, final double zd)
	{
		final double adx = xa - xd;
		final double bdx = xb - xd;
		final double cdx = xc - xd;
		final double ady = ya - yd;
		final double bdy = yb - yd;
		final double cdy = yc - yd;
		final double adz = za - zd;
		final double bdz = zb - zd;
		final double cdz = zc - zd;

		return adx * (bdy * cdz - bdz * cdy) + bdx * (cdy * adz - cdz * ady) + cdx * (ady * bdz - adz * bdy);
	}


	/**
	 * Computes the product a*b. Puts the product in x and the error in y.
	 */
	/*
	 * private strictfp static void twoProduct(double a, double b, Two t) {
	 * double x = a*b; split(a,t); double ahi = t.x; double alo = t.y;
	 * split(b,t); double bhi = t.x; double blo = t.y; double err1 =
	 * x-(ahi*bhi); double err2 = err1-(alo*bhi); double err3 = err2-(ahi*blo);
	 * t.x = x; t.y = (alo*blo)-err3; }
	 */

	/**
	 * Determines if a point d is left of the plane defined by the points a, b,
	 * and c. The latter are assumed to be in CCW order, as viewed from the
	 * right side of the plane.
	 * <p>
	 * <em>Note: this fast method may return an incorrect result.</em>
	 * 
	 * @return positive, if left of plane; negative, if right of plane; zero,
	 *         otherwise.
	 */
	public static double leftOfPlaneFast(final double[] pa, final double[] pb, final double[] pc, final double[] pd)
	{
		return leftOfPlaneFast(pa[0], pa[1], pa[2], pb[0], pb[1], pb[2], pc[0], pc[1], pc[2], pd[0], pd[1], pd[2]);
	}


	/**
	 * Determines if a point d is left of the plane defined by the points a, b,
	 * and c. The latter are assumed to be in CCW order, as viewed from the
	 * right side of the plane.
	 * <p>
	 * <em>Note: this fast method may return an incorrect result.</em>
	 * 
	 * @return positive, if left of plane; negative, if right of plane; zero,
	 *         otherwise.
	 */
	public static double leftOfPlaneFast(final float[] pa, final float[] pb, final float[] pc, final float[] pd)
	{
		return leftOfPlaneFast(pa[0], pa[1], pa[2], pb[0], pb[1], pb[2], pc[0], pc[1], pc[2], pd[0], pd[1], pd[2]);
	}


	/**
	 * Computes the scaled expansion h = e*b, eliminating zero components from
	 * the output expansion. Maintains the nonoverlapping property. If
	 * round-to-even is used (as with IEEE 754), maintains the strongly
	 * nonoverlapping and nonadjacent properties as well. (That is, if e has one
	 * of these properties, so will h.) The expansion h cannot be aliased with
	 * e.
	 */
	private static int scaleExpansionZeroElim(final int elen, final double[] e, final double b, final double[] h)
	{
		final Two t = new Two();
		split(b, t);
		final double bhi = t.x;
		final double blo = t.y;
		twoProduct1Presplit(e[0], b, bhi, blo, t);
		double q = t.x;
		double hh = t.y;
		int hindex = 0;
		if (hh != 0)
		{
			h[hindex++] = hh;
		}
		for (int eindex = 1; eindex < elen; ++eindex)
		{
			final double enow = e[eindex];
			twoProduct1Presplit(enow, b, bhi, blo, t);
			final double product1 = t.x;
			final double product0 = t.y;
			twoSum(q, product0, t);
			final double sum = t.x;
			hh = t.y;
			if (hh != 0)
			{
				h[hindex++] = hh;
			}
			twoSumFast(product1, sum, t);
			q = t.x;
			hh = t.y;
			if (hh != 0)
			{
				h[hindex++] = hh;
			}
		}
		if (q != 0.0 || hindex == 0)
		{
			h[hindex++] = q;
		}
		return hindex;
	}


	/**
	 * Splits a into two overlapping parts. Puts the high bits in x and the low
	 * bits in y.
	 */
	private strictfp static void split(final double a, final Two t)
	{
		final double c = SPLITTER * a;
		final double abig = c - a;
		t.x = c - abig;
		t.y = a - t.x;
	}


	/**
	 * Computes difference a-b. Puts result in x and error in y.
	 */
	private strictfp static void twoDiff(final double a, final double b, final Two t)
	{
		final double x = a - b;
		final double bvirt = a - x;
		final double avirt = x + bvirt;
		final double bround = bvirt - b;
		final double around = a - avirt;
		t.x = x;
		t.y = around + bround;
	}


	/**
	 * Computes the product a*b, where b has already been split. Puts the
	 * product in x and the error in y.
	 */
	private strictfp static void twoProduct1Presplit(final double a, final double b, final double bhi, final double blo, final Two t)
	{
		split(a, t);
		final double ahi = t.x;
		final double alo = t.y;
		t.x = a * b;
		final double err1 = t.x - ahi * bhi;
		final double err2 = err1 - alo * bhi;
		final double err3 = err2 - ahi * blo;
		t.y = alo * blo - err3;
	}


	/**
	 * Computes the product a*b, where a and b have already been split. Puts the
	 * product in x and the error in y.
	 */
	private strictfp static void twoProduct2Presplit(final double a, final double ahi, final double alo, final double b, final double bhi, final double blo, final Two t)
	{
		t.x = a * b;
		final double err1 = t.x - ahi * bhi;
		final double err2 = err1 - alo * bhi;
		final double err3 = err2 - ahi * blo;
		t.y = alo * blo - err3;
	}


	/**
	 * Computes sum a+b. Puts result in x and error in y.
	 */
	private strictfp static void twoSum(final double a, final double b, final Two t)
	{
		final double x = a + b;
		final double bvirt = x - a;
		final double avirt = x - bvirt;
		final double bround = b - bvirt;
		final double around = a - avirt;
		t.x = x;
		t.y = around + bround;
	}


	/**
	 * Computes sum a+b, assuming that |a|&gt;=|b|. Puts result in x and error
	 * in y.
	 */
	private strictfp static void twoSumFast(final double a, final double b, final Two t)
	{
		final double x = a + b;
		final double bvirt = x - a;
		t.x = x;
		t.y = b - bvirt;
	}


	/**
	 * Computes the product a*b, where a and b are two-component expansions.
	 * Puts the product in the array x[8].
	 */
	private strictfp static void twoTwoProduct(final double a1, final double a0, final double b1, final double b0, final double[] x)
	{
		double u0, u1, u2, ui, uj, uk, ul, um, un;
		final Two t = new Two();
		split(a0, t);
		final double a0hi = t.x;
		final double a0lo = t.y;
		split(b0, t);
		final double b0hi = t.x;
		final double b0lo = t.y;
		twoProduct2Presplit(a0, a0hi, a0lo, b0, b0hi, b0lo, t);
		ui = t.x;
		x[0] = t.y;
		split(a1, t);
		final double a1hi = t.x;
		final double a1lo = t.y;
		twoProduct2Presplit(a1, a1hi, a1lo, b0, b0hi, b0lo, t);
		uj = t.x;
		u0 = t.y;
		twoSum(ui, u0, t);
		uk = t.x;
		u1 = t.y;
		twoSumFast(uj, uk, t);
		ul = t.x;
		u2 = t.y;
		split(b1, t);
		final double b1hi = t.x;
		final double b1lo = t.y;
		twoProduct2Presplit(a0, a0hi, a0lo, b1, b1hi, b1lo, t);
		ui = t.x;
		u0 = t.y;
		twoSum(u1, u0, t);
		uk = t.x;
		x[1] = t.y;
		twoSum(u2, uk, t);
		uj = t.x;
		u1 = t.y;
		twoSum(ul, uj, t);
		um = t.x;
		u2 = t.y;
		twoProduct2Presplit(a1, a1hi, a1lo, b1, b1hi, b1lo, t);
		uj = t.x;
		u0 = t.y;
		twoSum(ui, u0, t);
		un = t.x;
		u0 = t.y;
		twoSum(u1, u0, t);
		ui = t.x;
		x[2] = t.y;
		twoSum(u2, ui, t);
		uk = t.x;
		u1 = t.y;
		twoSum(um, uk, t);
		ul = t.x;
		u2 = t.y;
		twoSum(uj, un, t);
		uk = t.x;
		u0 = t.y;
		twoSum(u1, u0, t);
		uj = t.x;
		x[3] = t.y;
		twoSum(u2, uj, t);
		ui = t.x;
		u1 = t.y;
		twoSum(ul, ui, t);
		um = t.x;
		u2 = t.y;
		twoSum(u1, uk, t);
		ui = t.x;
		x[4] = t.y;
		twoSum(u2, ui, t);
		uk = t.x;
		x[5] = t.y;
		twoSum(um, uk, t);
		x[7] = t.x;
		x[6] = t.y;
	}
}