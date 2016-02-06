// KicadModuleToGEDA - a utility for turning kicad modules to gEDA PCB footprints
// Arc.java v1.0
// Copyright (C) 2015 Erich S. Heinzle, a1039181@gmail.com

//    see LICENSE-gpl-v2.txt for software license
//    see README.txt
//    
//    This program is free software; you can redistribute it and/or
//    modify it under the terms of the GNU General Public License
//    as published by the Free Software Foundation; either version 2
//    of the License, or (at your option) any later version.
//    
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//    
//    You should have received a copy of the GNU General Public License
//    along with this program; if not, write to the Free Software
//    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
//    
//    KicadModuleToGEDA Copyright (C) 2015 Erich S. Heinzle a1039181@gmail.com



/**
 *
 * This class is passed a Kicad Draw Circle string of the
 * form "DC Xcentre Ycentre X_startpoint Y_startpoint angle lineWidth layer"
 * and implements a method which can generate a gEDA ElementArc definition for a gEDA PCB footprint
 *
 * @param long xOffset the X offset of the Draw Segment relative to the module origin
 * @param long yOffset the Y offset of the Draw Segment relative to the module origin
 * @param float magnificationRatio magnification to be applied to segment position and size, default 1.0
 *
 * @return String = "ElementArc[x y width height startangle deltaangle thickness]"
 *
 */
import java.math.*;

public class Arc extends FootprintElementArchetype
{
  String output = "";

  long gEDAxCoord = 0;
  long gEDAyCoord = 0;
  long xCoordNm = 0;
  long yCoordNm = 0;
  long xPointNm = 0;
  long yPointNm = 0;
  long kicadStartAngle = 0;    // in 0.1 degree increments
  long gEDAstartAngle = 0;     // in degrees
  long kicadDeltaAngle = 3600; // Kicad specifies angle in 0.1 degree increments
  int kicadLayer = 0; // not used for arcs
  long gEDAdeltaAngle = 0;     // in degrees CCW
  double radiusNm = 0;
  long gEDAwidth = 0;
  long gEDAheight = 0;
  long gEDAradius = 0;
  long gEDAlineThickness = 1000; // this is 10 mil in 0.01 mil units
  long lineThicknessNm = 254000; // which is 254000 nanometres
  // which is 254 microns, which is 0.254 mm
  // which is 0.01 inches, which is 10 mil = 10 thou

  String kicadArcDescriptor = "";

  public void KicadDrawingArc()
  {
    output = "#Hmm, the no arg KicadDrawingArc constructor didn't do much";
  }

  public String toString()
  {
    return kicadArcDescriptor;
  }

  // here, we populate the line object with a string
  // extracted from a BXL file
  // noting that the y-axis is inverted vs gEDA/kicad
  public void populateBXLElement(String BXLLine) {
    BXLLine = BXLLine.replaceAll("[\"(),]","");
    String [] tokens = BXLLine.split(" ");
    for (int index = 0; index < tokens.length; index++) {
      if (tokens[index].equals("Origin")) {
        xCoordNm = milToNM(Float.parseFloat(tokens[++index]));
        yCoordNm = -milToNM(Float.parseFloat(tokens[++index]));
      } else if (tokens[index].equals("TOP_SILKSCREEN")) {
        kicadLayer = 21; // i.e. F.Silk
      } else if(tokens[index].equals("Radius")) {
        radiusNm = milToNM(Float.parseFloat(tokens[++index]));
      } else if(tokens[index].equals("Width")) {
        lineThicknessNm = milToNM(Float.parseFloat(tokens[++index]));
      } else if(tokens[index].equals("StartAngle")) { // in degrees
        gEDAstartAngle = Integer.parseInt(tokens[++index]);
      } else if(tokens[index].equals("SweepAngle")) { // in degrees
        gEDAdeltaAngle = -Integer.parseInt(tokens[++index]);
        // seem to need negative here, since y-axis is flipped
      }
    }
  }

  // here, we populate the line object with a string
  // extracted from a Kicad module    
  public void populateElement(String arg, boolean metric)
  {

    kicadArcDescriptor = arg;

    String[] tokens = kicadArcDescriptor.split(" ");

    float parsedValue = 0;

    //		System.out.print("#The passed string:" + arg + "\n");
    if (tokens[0].startsWith("DA"))
      {
        parsedValue = Float.parseFloat(tokens[1]);
        xCoordNm = convertToNanometres(parsedValue, metric); 
        parsedValue = Float.parseFloat(tokens[2]);
        yCoordNm = convertToNanometres(parsedValue, metric);
        parsedValue = Float.parseFloat(tokens[3]);
        xPointNm = convertToNanometres(parsedValue, metric);
        parsedValue = Float.parseFloat(tokens[4]);
        yPointNm = convertToNanometres(parsedValue, metric);
        kicadDeltaAngle = Integer.parseInt(tokens[5]);
        parsedValue = Float.parseFloat(tokens[6]);
        lineThicknessNm = convertToNanometres(parsedValue, metric);
      }
    else if (tokens[0].startsWith("fp_arc"))
      {
        metric = true;
        parsedValue = Float.parseFloat(tokens[2]);
        xCoordNm = convertToNanometres(parsedValue, metric);
        parsedValue = Float.parseFloat(tokens[3]);
        yCoordNm = convertToNanometres(parsedValue, metric);
        parsedValue = Float.parseFloat(tokens[5]);
        xPointNm = convertToNanometres(parsedValue, metric);
        parsedValue = Float.parseFloat(tokens[6]);
        yPointNm = convertToNanometres(parsedValue, metric);
        kicadDeltaAngle = Integer.parseInt(tokens[8]);
        parsedValue = Float.parseFloat(tokens[12]);
        lineThicknessNm = convertToNanometres(parsedValue, metric);
        // this does not establish what layer it is to be on, but no biggy

      }
    else
      {
        output = "Hmm, a Draw Arc string was not passed to the object";
      }

    // it seems $MODULE (= footprints) support arcs with arbitrary deci-degrees in Kicad,
    // http://en.wikibooks.org/wiki/Kicad/file_formats#Drawings
    // but drawing elements $DRAWSEGMENT only support 90 degree arcs

    long yDir = 0;
    long xDir = 0;

    // our first task is to establish the start point of the arc in degrees.
    // we are effectively given the start point in cartesian coordinates.
    // we use the arctan function to determine the angle.
    // we then convert this angle to degrees to suit gEDA, starting from
    // the -ve X axis, and going in a +ve = counterclockwise (CCW) direction.
    // we also catch the scenarios where cos(startAngle) = 0 since tan is
    // not well defined for tan = (sin (+/-90) / cos (+/- 90)) = 1/0 

    // we have 8 scenarios to consider
    // the simplest four scenarios being:
    //
    // the start point lies on one of the +ve X, -ve X, +ve Y or -ve Y axes
    //
    // or, the start point lies in one of the four quadrants of the x,y plane:
    //
    // the start point lies in LUQ -ve X, -ve Y,  with xDir > 0,  yDir > 0 
    // the start point lies in RUQ +ve X, -ve Y,  with xDir < 0,  yDir > 0
    // the start point lies in LLQ -ve X, +ve Y,  with xDir > 0,  yDir < 0
    // the start point lies in RLQ +ve X, +ve Y,  with xDir < 0,  yDir < 0
    //

    yDir = (yCoordNm - yPointNm); // the Y component of the start point coordinates
    xDir = (xCoordNm - xPointNm); // the X component of the start point coordinates

    // also, we determine the radius of the arc starting at the start
    // point (xDir, yDir)

    radiusNm = Math.sqrt((xDir*xDir) + (yDir*yDir));

    if ((yDir == 0) && (xDir < 0))
      {
        gEDAstartAngle = 180; // arc start point lies along +X axis
      }
    else if ((yDir == 0) && (xDir > 0))
      {
        gEDAstartAngle = 0;  // arc start point lies along -X axis
      }

    else if (yDir < 0) // this means arc starts in the y > 0 hemiplane //< 0 hemiplane
      {
        if (xDir == 0) // the start point lies on the + Y axis
          {
            gEDAstartAngle = 90; // = the +Y axis
            // 				in gEDA degrees, starting @ X- axis, +ve = CCW direction
            //				System.out.println("startangle A: " + startAngle);
          }
        else if (xDir < 0) // this is RLQ on screen, +ve X, +ve Y
          {
            gEDAstartAngle = 180 - Math.round(1.0 * Math.toDegrees(Math.atan((yDir)/(xDir))));
            //				System.out.println("startangle B: " + startAngle);
          }
        else if (xDir > 0) // this LLQ on screen, +ve X, -ve Y
          {
            gEDAstartAngle = 0 - Math.round(1.0 * Math.toDegrees(Math.atan((yDir)/(xDir)))); // atan(-veY/+veX) < 0
            //				System.out.println("startangle C: " + startAngle);
          }
      }
    else if (yDir > 0) // this means arc starts in the y > 0 hemiplane
      {
        if (xDir == 0) // the start point lies on the +Y axis
          {
            gEDAstartAngle = 270;
            // 				in gEDA degrees, starting @ x- axis, +ve = CCW direction
            //				System.out.println("startangle D: " + startAngle);
          }
        else if (xDir < 0) // this is RUQ on screen, +ve X, -ve Y
          {
            gEDAstartAngle = 180 - Math.round(1.0 * Math.toDegrees(Math.atan((yDir)/(xDir)))); // atan(-veY/+veX) < 0
            //				System.out.println("startangle E: " + startAngle);
          }
        else if (xDir > 0) // this is LUQ on screen, +ve X, +ve Y
          {
            gEDAstartAngle = 0 -  Math.round(1.0 * Math.toDegrees(Math.atan((yDir)/(xDir))));
            //				System.out.println("startangle F: " + startAngle);
          }

      }

    // first, we calculate the magnification invariant stuff, namely
    // the start angle and delta angle for the arc

    // as far as can be determined from the Kicad documentation,
    // http://en.wikibooks.org/wiki/Kicad/file_formats#Drawings
    // 
    // the origin for rotation is the positive x direction, and going CW
    //
    // whereas in gEDA, the gEDA origin for rotation is the negative x axis,
    // with rotation CCW, so we need to reverse delta angle, and scale
    //
    // deltaAngle is in CW in Kicad in deci-degrees, and CCW in degrees in gEDA
    //
    // ... having tested the code, Kicad deltaAngle is indeed opposite to gEDA
    //  

    gEDAdeltaAngle = Math.round(kicadDeltaAngle/(-10.0));

    // hmmm, it seems some arcs in Kicad foootprint modules have decidegrees of
    // arc > 3600, which is basically a circle, but defined with an arc statement for
    // some reason.
    // This seems to be the result of a dodgy Eagle2Kicad.ulp footprint
    // conversion script
    // So, we need to check for this and establish the delta angle module 360
    // to avoid funny looking partial arcs or circles when rendered by gEDA.

    //		System.out.println("deltaAngle: " + deltaAngle);


    while (gEDAdeltaAngle > 360)

      //		if ((gEDAdeltaAngle > 359) || (gEDAdeltaAngle < -359))
      {
        gEDAdeltaAngle -= 360;
      }


    while (gEDAdeltaAngle < -360)
      {
        gEDAdeltaAngle += 360;
      }
  }

  public String generateGEDAelement(long xOffsetNm, long yOffsetNm, float magnificationRatio)
  // offsets are in nm, magnificationRatio is a float, default 1.0
  {
    // having established the start angle in gEDA degrees
    // we can move on to magnification variant stuff, namely,
    // x and y positions, and radius, to then be able to
    // generate an arc definition in gEDA format 

    // we take care of magnification here
    // this allows a family of footprint silkscreens graphics to
    // be generated fairly easily, for example different sized 7
    // or 17 segment LED displays
    // Device outlines may need to be hand tweaked, as some
    // device families share the same outline, despite different sized
    // features across the family of device, i.e. 0.5 inch and
    // 0.8 inch LED displays have the same outline silkscreen
    // which doesn't need to be magnified

    gEDAxCoord = (long)((xCoordNm + xOffsetNm)*magnificationRatio/254);
    // divide nm by 254 to produce
    gEDAyCoord = (long)((yCoordNm + yOffsetNm)*magnificationRatio/254);
    // 0.01 mil units

    // apply the magnificationRatio to radiusNm
    radiusNm = magnificationRatio*radiusNm;

    // now convert the radius in nm to gEDA 0.01 mil units

    gEDAradius = Math.round(radiusNm/254); // in 0.01mil units for gEDA, so nm/254

    gEDAlineThickness = (lineThicknessNm / 254); // every 254 nm is 0.01 mil

    output = "ElementArc[" +
        gEDAxCoord + " " +
        gEDAyCoord + " " +
        gEDAradius + " " + // gEDAradius is equal to width and height for a circle
        gEDAradius + " " +
        gEDAstartAngle + " " + // in degrees
        gEDAdeltaAngle + " " + // in degrees
        gEDAlineThickness + "]\n"; // in 0.01 mil units
    return output;
  }

  private long milToNM(float rawValue)
  {
    return (long)(rawValue * 25400 ); // 1 mil unit = 25400 nm
  }

  private long convertToNanometres(float rawValue, Boolean metricSystem)
  {
    if (!metricSystem) // this implies the input is in Kicad legacy decimil = 0.1mil units
      {
        return (long)(2540 * rawValue); // a 0.1 mil unit = 2540 nm
      }
    else // metric = the newer legacy format with mm instead of decimil = 0.1 mil units
      {
        return (long)(rawValue * 1000000);
      }
  }

}
