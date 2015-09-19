/**
*****************************************************************************
*
* Class:   XPlot
*
* Purpose: Extended Plot.  
*
* This class extends the Plot plotting class (from Berkley's Ptolemy 
* PtPlot package).
*
* ---------------------------------------------------------------------
*  Version    Date         Who          What
* ---------------------------------------------------------------------
*   01      03/01/00    P.Brodsky   Initial Version
*
*****************************************************************************
*/

package edu.washington.apl.aganse.ptolemyUpdates.plot;

// Standard Java imports
import java.util.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class XPlot extends Plot implements MouseListener {

  /* Attributes */

  // Current (clicked) mouse position in pixels
  int _MouseX;
  int _MouseY;  

  // Vector of MouseListeners, who wish to be notified
  // when the mouse is clicked in the plot region
  Vector _MouseListeners = new Vector();

  // Vector of vertical lines, specified by their data X coordinates
  // and labels
  Vector _VerticalLineX = new Vector();
  Vector _VerticalLineLabel = new Vector();


  /**
   * Constructor
  */
  public XPlot() {
    super();
    addMouseListener( this );    // for getting mouse position
  }

  /**
   * Override these to layout actual desired size.  Otherwise, some
   * area is lost along the top due to banner and menu bars.
  */
  public Dimension getPreferredSize() {
    return getSize();
  }
  public Dimension getMinimumSize() {
    return getSize();
  }


 /** 
  *  Method:  addPoints()
  *
  *  Purpose: Set an array of points.
  *
  *  Added to original Ptolemy.Plot package by P.Brodsky 
  *
  *  @param dataset The data set index.
  *  @param x Array of X positions of the points.
  *  @param y Array of Y position of the points.
  *  @param nPts Number of points to plot
  *  @param connected If true, a line is drawn between all points
  */
  public void addPoints( int dataset, double x[], double y[],
                         int nPts, boolean connected ) {

     for( int i=0; i<nPts; i++ ) {
        addPoint( dataset, x[i], y[i], connected );
     }
  }

 /** 
  *  Method:  addPoints()
  *
  *  PurposeSet an array of points.
  *
  *  Added to original Ptolemy.Plot package by P.Brodsky 
  *
  *  @param dataset The data set index.
  *  @param x Array of X positions of the points.
  *  @param y Array of Y position of the points.
  *  @param connected If true, a line is drawn between all points
  */
  public void addPoints( int dataset, double x[], double y[],
                          boolean connected ) {

    // If arrays are not the same size, take the smaller size
     int count = Math.min( x.length, y.length );
     addPoints( dataset, x, y, count, connected );

  }

 /** 
  *  Method:  setData()
  *
  *  Purpose: Set plot data directly (without need for file).
  *
  *  Data is assumed to be in the form of 2 double arrays.
  *
  *  Added to original Ptolemy.Plot package by P.Brodsky 
  *
  *  @param x The independent (x) axis data set
  *  @param y The dependent (y) axis data set
  */
  public void setData( double x[], double y[] ) throws Exception {

    // If arrays are not the same size, take the smaller size
     setData( x, y, Math.min( x.length, y.length ) );

  }

  /** 
   *  Method:  setData
   *
   *  Purpose: Set plot data directly (without need for file).
   *
   *  Data is assumed to be in the form of 2 double arrays.
   *
   *  Added to original Ptolemy.Plot package by P.Brodsky 
   *
   *  @param x The independent (x) axis data set
   *  @param y The dependent (y) axis data set
   *  @param nPts The number of points to actually plot
   */
   public void setData( double x[], double y[], int nPts ) throws Exception {

      // Check nPts, and auto-scale down to logical number
      nPts = Math.min( x.length, Math.min(y.length,nPts) );        

      // Put data in the String form:  "xvalue , yvalue"
      // and send to parent _parseLine method
      for( int i=0; i<=nPts-1; i++ ) {
         String line = Double.toString(x[i]) + " , " + Double.toString(y[i]);
         _parseLine(line);
      }
      // Not sure what these do, but read() does it...
      _firstinset = true;
      _sawfirstdataset = false;

   }

 /** 
  *  Method:  clearLegend()
  *
  *  Purpose: Clear the specified legend item.
  *  
  *  Added by P.Brodsky, 2/00
  *
  *  @param dataset The dataset index.
  */
  public void clearLegend(int dataset) {
        _checkDatasetIndex(dataset);
        super.clearLegend(dataset);
  }


 /**
  * Method:  getDataX()
  *
  * Purpose: Returns the X position, in data coordinates,
  *          given the X value in plot coordinates (pixels).
  */
  public double getDataX( int plotX ) {
      return (double)(plotX - _ulx)/_xscale + (double)_xMin;
  }
 /**
  * Method:  getDataY()
  *
  * Purpose: Returns the Y position, in data coordinates,
  *          given the Y value in plot coordinates (pixels).
  */
  public double getDataY( int plotY ) {
      return (double)_yMax - (double)(plotY - _uly)/_yscale;
  }
 /**
  * Method:  getPlotX()
  *
  * Purpose: Returns the X position, in plot coordinates (pixels),
  *          given X in data coordinates.
  */
  public int getPlotX( double dataX ) {
      return (int) Math.round(_ulx + (dataX - _xMin)*_xscale);
  }
 /**
  * Method:  getPlotY()
  *
  * Purpose: Returns the Y position, in plot coordinates (pixels),
  *          given Y in data coordinates.
  */
  public int getPlotY( double dataY ) {
      return (int) Math.round(_lry - (dataY - _yMin)*_yscale);
  }

 /**
  * Method:  getDataMouseX()
  *
  * Purpose: Returns the X position of the mouse in units of the data.
  *
  */
  public double getDataMouseX() { return getDataX( _MouseX ); }
 /**
  * Method:  getDataMouseY()
  *
  * Purpose: Returns the Y position of the mouse in units of the data.
  */
  public double getDataMouseY() { return getDataY( _MouseY ); }

 /** 
  * Method:  addVerticalLine()
  *
  * Purpose: Add a vertical line through the plot at the
  *          specified X coordinate (in pixels).
  *
  *          Depending on the X position, return true or false
  *          if the line was added or not.
  */
  public boolean addVerticalLine( int plotX, String label ) {

    // Position must be somewhere inside the plot region
    if( !inPlotRegion( plotX, _uly ) )     // pick y definitely inside
      return false;

    // Convert pixels to data x coordinate, so if the plot is resized,
    // the line occupies the same place in data space.
    Double dataXCoord = new Double( getDataX( plotX ) );
    _VerticalLineX.addElement( dataXCoord );
    // Add the label
    _VerticalLineLabel.addElement( label );
    return true;

  }

 /** 
  * Method:  clearVerticalLine()
  *
  * Purpose: Remove the specified vertical line.
  */
  public void clearVerticalLine( int i ) {
    _VerticalLineX.removeElementAt(i);
    _VerticalLineLabel.removeElementAt(i);
  }

 /** 
  * Method:  clearAllVerticalLines()
  *
  * Purpose: Convenience method to remove all vertical lines.
  */
  public void clearAllVerticalLines() {
    _VerticalLineX.clear();
    _VerticalLineLabel.clear();
  }

 /** 
  * Method:  inPlotRegion()
  *
  * Purpose: Determine if the specified coordinates (in pixels) are 
  *          inside the plotting region (between X and Y axes).
  */     
  public boolean inPlotRegion( int x, int y ) { 
    return( _ulx <= x && x <= _lrx && _uly <= y && y <= _lry );
  }

 /** 
  * Method:  drawVerticalLine()
  *
  * Purpose: Draw the specified vertical line.
  */     
  public void drawVerticalLine( Graphics g, int i ) { 

    double dataX = ((Double)_VerticalLineX.elementAt(i)).doubleValue();
    int x1 = getPlotX( dataX );
    int x2 = x1;
    int y1 = _lry;
    int y2 = _uly;
    // Use 2-pixel dash and space
    drawDashedLine( g, x1, y1, x2, y2, 2., 2. );

    // Draw the label under the dashed line, 5 pixels below the X axis
    String label = (String)_VerticalLineLabel.elementAt(i);
    int labelx =  x1 - _labelFontMetrics.stringWidth(label)/2;
    g.drawString(label, labelx, _lry+_labelFontMetrics.getAscent()+3 );

  }

 /** 
  * Method:  drawDashedLine()
  *
  * Purpose: Draws a dashed line between the specified points.
  *
  * Borrowed from code posted at: 
  *         http://codeguru.developer.com/java/articles/390.shtml
  */     
  public void drawDashedLine(Graphics g,int x1,int y1,int x2,int y2,
                             double dashlength, double spacelength) {


    double linelength=Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
    double yincrement=(y2-y1)/(linelength/(dashlength+spacelength));
    double xincdashspace=(x2-x1)/(linelength/(dashlength+spacelength));
    double yincdashspace=(y2-y1)/(linelength/(dashlength+spacelength));
    double xincdash=(x2-x1)/(linelength/(dashlength));
    double yincdash=(y2-y1)/(linelength/(dashlength));
    int counter=0;

    g.setColor( Color.black );

    for (double i=0;i<linelength-dashlength;i+=dashlength+spacelength){
      g.drawLine( (int) (x1+xincdashspace*counter),
                  (int) (y1+yincdashspace*counter),
                  (int) (x1+xincdashspace*counter+xincdash),
                  (int) (y1+yincdashspace*counter+yincdash) );
      counter++;
    }
    if ((dashlength+spacelength)*counter<=linelength)
      g.drawLine( (int) (x1+xincdashspace*counter),
                  (int) (y1+yincdashspace*counter),
                  x2, y2 );

  }


 /** 
  * Method:  mouseClicked()
  *
  * Purpose: Handle mouse clicks
  */     
  public void mouseClicked( MouseEvent evt ) {

    // Notify all listeners
    Enumeration enum1 = _MouseListeners.elements();
    while( enum1.hasMoreElements() ) {
      MouseListener ml = (MouseListener)enum1.nextElement();
      ml.mouseClicked( evt );          // by callback to mouseClicked method
    }

  // For Testing
	// _MouseX = evt.getX();
	// _MouseY = evt.getY();
	// System.out.println("\n - Upper Left Corner: (" + _ulx + "," + _uly + ")");
	// System.out.println(" - Lower Right Corner: (" + _lrx + "," + _lry + ")");
	// System.out.println(" - Mouse Position = " + _MouseX + ", " + _MouseY );
	// System.out.println(" - Data X,Y = " + getDataMouseX() 
    //                                   + ", " + getDataMouseY() );
  }

 /** 
  * Method:  registerMouseClickListener()
  *
  * Purpose: Add the passed MouseListener to the list of those
  *          to be notified upon mouse clicks.
  */
  public void registerMouseListener( MouseListener ml ) {
    _MouseListeners.addElement( ml );
  }

 /** 
  * Methods to round out the MouseListener interface
  */
  public void mouseEntered(MouseEvent event) {}
  public void mouseExited(MouseEvent event) {}
  public void mousePressed(MouseEvent event) {}
  public void mouseReleased(MouseEvent event) {}

 /**
  * Method:  paint()
  *
  * Purpose: Override paint() to include additional plot stuff
  *          defined in this class
  */
  public void paint(Graphics graphics) {

    super.paint( graphics );

  /* FOR TESTING
    System.out.println("\n - Upper Left Corner: (" + _ulx + "," + _uly + ")");
    System.out.println(" - Lower Right Corner: (" + _lrx + "," + _lry + ")");
    System.out.println(" - _xmin, _xmax: " + _xMin + ", " + _xMax );
    System.out.println(" - _ymin, _ymax: " + _yMin + ", " + _yMax );
    double[] xrange = getXRange();
    double[] yrange = getYRange();
    System.out.println(" - XRange: " + xrange[0] + ", " + xrange[1] );
    System.out.println(" - YRange: " + yrange[0] + ", " + yrange[1] );
  */

    // Draw any vertical lines
    for( int i=0; i<_VerticalLineX.size(); i++ )
      drawVerticalLine( graphics, i );

  }

}
