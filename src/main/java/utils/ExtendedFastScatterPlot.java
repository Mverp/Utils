package utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.ui.RectangleEdge;

/**
 * An extension of the JFreeChart FastScatterPlot to quickly create a scatter plot that can be configured in one go.
 */
public class ExtendedFastScatterPlot extends FastScatterPlot
{

	private static final long serialVersionUID = 1L;

	// The extended scatter plot settings
	int size;
	Paint[] color;
	int shape;


	/**
	 * Create a new ExtendedFastScatterPlot object.
	 *
	 * @param aData
	 *            The data set for this scatter plot. Each data set consists of a double float array (0 for x and 1 for y)
	 * @param aDomainAxis
	 *            The settings for the domain (x) axis
	 * @param aRangeAxis
	 *            The settings for the range (y) axis
	 * @param aSize
	 *            The shape size of each plot point
	 * @param aColor
	 *            The set of colours to display the plot points, one for each point. If less colours are given then data points, any superfluous point is assigned the first colour of the set. If null, all points are set to black.
	 * @param aShape
	 *            The shape of the plot points
	 */
	public ExtendedFastScatterPlot(final float[][] aData, final NumberAxis aDomainAxis, final NumberAxis aRangeAxis, final int aSize, final Paint[] aColor, final int aShape)
	{
		super(aData, aDomainAxis, aRangeAxis);
		this.size = aSize;
		this.color = aColor;
		this.shape = aShape;
	}


	/**
	 * {@inheritDoc}
	 *
	 * This render takes into account the stored size, shape and data set specific colours to render the data. It also presets the axis to precisely fit the data range offered.
	 */
	@Override
	public void render(final Graphics2D g2, final Rectangle2D dataArea, final PlotRenderingInfo info, final CrosshairState crosshairState)
	{
		if (this.getData() != null)
		{
			for (int i = 0; i < this.getData()[0].length; i++)
			{
				if (i < this.color.length)
				{
					g2.setPaint(this.color[i]);
				}
				else if (this.color != null)
				{
					g2.setPaint(this.color[0]);
				}
				else
				{
					g2.setColor(Color.BLACK);
				}
				final float x = this.getData()[0][i];
				final float y = this.getData()[1][i];
				final int transX = (int) this.getDomainAxis().valueToJava2D(x, dataArea, RectangleEdge.BOTTOM);
				final int transY = (int) this.getRangeAxis().valueToJava2D(y, dataArea, RectangleEdge.LEFT);
				if (1 == this.shape)
				{
					g2.fillRect(transX, transY, this.size, this.size);
				}
				else
				{
					g2.fillOval(transX, transY, this.size, this.size);
				}
			}
		}
	}
}