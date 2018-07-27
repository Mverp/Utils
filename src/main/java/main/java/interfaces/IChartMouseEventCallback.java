package interfaces;

import org.jfree.chart.ChartMouseEvent;

/**
 * Any class that implements this interface should be able to handle mouse
 * events on a JFreeChart. Use the {@link} CallbackChartMouseListener to catch
 * the mouse events and delegate them to this class.
 *
 * @author Merijn van Erp
 * @see CallbackChartMouseListener
 */
public interface IChartMouseEventCallback
{
	/**
	 * Handle a mouse click on a JFreeChart..
	 *
	 * @param aMouseEvent
	 *            The mouse click event
	 * @param aXValue
	 *            The value on the x-axis belonging to the point clicked (so not
	 *            just the x-coordinate!)
	 * @param aYValue
	 *            The value on the y-axis belonging to the point clicked (so not
	 *            just the y-coordinate!)
	 */
	public void handleMouseClick(ChartMouseEvent aMouseEvent, double aXValue, double aYValue);


	/**
	 * Handle a mouse move on a JFreeChart..
	 *
	 * @param aMouseEvent
	 *            The mouse move event
	 * @param aXValue
	 *            The value on the x-axis belonging to the point moved to (so
	 *            not just the x-coordinate!)
	 * @param aYValue
	 *            The value on the y-axis belonging to the point moved to (so
	 *            not just the y-coordinate!)
	 */
	public void handleMouseMove(ChartMouseEvent aMouseEvent, double aXValue, double aYValue);
}
