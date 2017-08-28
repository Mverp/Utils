package ui;

import java.awt.Color;
import java.awt.event.MouseEvent;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PlotEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;

import interfaces.IChartMouseEventCallback;

/**
 * Listens for mouse clicks and moves on the a JFreeChart Histogram and
 * delegates the results to the callback after fetching the correct x and y
 * values related to the event.
 *
 * @author Merijn van Erp
 */
public class HistogramListener implements ChartMouseListener
{
	private final ChartPanel chartPanel;
	private ValueMarker marker;

	final private IChartMouseEventCallback callback;


	public HistogramListener(final ChartPanel aChartPanel, final IChartMouseEventCallback aCallback)
	{
		this.chartPanel = aChartPanel;
		this.callback = aCallback;
	}


	/**
	 * Translates the click into the proper x and y value (not coordinates)
	 * depending on the data shown. In case and actual item has been selected,
	 * those x and y value are used, otherwise the x-coordinate is translated
	 * according to the data x-scale and the y-coordinate according to the
	 * y-scale. This does not directly correspond to an actual item!
	 *
	 * A vertical marker is set at the x-coordinate clicked and the callback for
	 * a mouse click is called with the calculated x and y values.
	 *
	 * @see org.jfree.chart.ChartMouseListener#chartMouseClicked(org.jfree.chart.ChartMouseEvent)
	 */
	@Override
	public void chartMouseClicked(final ChartMouseEvent aMouseEvent)
	{
		if (aMouseEvent.getTrigger().getID() == MouseEvent.MOUSE_CLICKED)
		{
			final ChartEntity entity = aMouseEvent.getEntity();
			double xValue = 0;
			double yValue = 0;

			final XYPlot plot = (XYPlot) aMouseEvent.getChart().getPlot();
			if (entity instanceof XYItemEntity)
			{
				final XYItemEntity xyEntity = (XYItemEntity) entity;
				xValue = xyEntity.getDataset().getX(xyEntity.getSeriesIndex(), xyEntity.getItem()).doubleValue();
				yValue = xyEntity.getDataset().getY(xyEntity.getSeriesIndex(), xyEntity.getItem()).doubleValue();
			}
			else if (entity instanceof PlotEntity)
			{
				ValueAxis axis = plot.getDomainAxis();
				xValue = axis.java2DToValue(aMouseEvent.getTrigger().getX(), this.chartPanel.getScreenDataArea(), plot.getDomainAxisEdge());
				axis = plot.getRangeAxis();
				yValue = axis.java2DToValue(aMouseEvent.getTrigger().getY(), this.chartPanel.getScreenDataArea(), plot.getRangeAxisEdge());
			}

			if (this.marker == null)
			{
				this.marker = new ValueMarker(xValue);
				this.marker.setPaint(Color.black);
				plot.addDomainMarker(this.marker);
			}
			else
			{
				this.marker.setValue(xValue);
			}

			this.callback.handleMouseClick(aMouseEvent, xValue, yValue);
		}
	}


	@Override
	public void chartMouseMoved(final ChartMouseEvent aMouseEvent)
	{
		final double xValue = 0;
		final double yValue = 0;

		this.callback.handleMouseMove(aMouseEvent, xValue, yValue);
	}

}
