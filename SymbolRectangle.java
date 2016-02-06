// KicadSymbolToGEDA - a utility for turning kicad modules to gEDA PCB footprints
// SymbolRectangle.java v1.0
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
//    KicadSymbolToGEDA Copyright (C) 2015 Erich S. Heinzle a1039181@gmail.com



/**
*
* This class is passed a Kicad Rectangle descriptor string of the form "DS x1 y1 x2 y2 thickness layer"
* and implements a method which can generate a gschame definition for a gEDA symbol
*
* @param long xOffset the X offset of the Draw Segment relative to the module origin
* @param long yOffset the Y offset of the Draw Segment realtive to the module origin
* @param float magnificationRatio the magnification ratio to be applied to element position and size
*
* @return String = "LineElement[x1 y1 x2 y2 thickness]"
*
*/

//S startx starty endx endy unit convert thickness cc
// With
//• unit = 0 if common to the parts; if not, number of part (1. .n).
//• convert = 0if common to all parts. If not, number of the part (1. .n).
//• thickness = thickness of the outline.
//• cc = N F or F ( F = filled Rectangle,; f = . filled Rectangle, N = transparent background)


public class SymbolRectangle extends SymbolElement
{

  String rectangleDescriptor = "";  
  String output = "";
  
  long xCoordOne = 0;
  long yCoordOne = 0;
  long xCoordTwo = 0;
  long yCoordTwo = 0;
  int fillType = 0;
  long lineThickness = 0;

  public SymbolRectangle()
  {
    output = "#Hmm, the no arg symbol rectangle constructor didn't do much";
  }

  public SymbolRectangle(int x1, int y1, int x2, int y2, int thick, int fill) {
    xCoordOne = x1;
    yCoordOne = y1;
    xCoordTwo = x2;
    yCoordTwo = y2;
    super.updateXdimensions(xCoordOne);
    super.updateYdimensions(yCoordOne);
    super.updateXdimensions(xCoordTwo);
    super.updateYdimensions(yCoordTwo);
    lineThickness = thick;
    fillType = fill; // 0 is no fill, 1 is solid IIRC
  }
  
  public void constructor(String arg)
  {
    rectangleDescriptor = arg;
    //System.out.println(arg);
    arg = arg.replaceAll("  "," ");
    String[] tokens = arg.split(" ");
    
    xCoordOne = Integer.parseInt(tokens[1]);
    yCoordOne = Integer.parseInt(tokens[2]);
    xCoordTwo = Integer.parseInt(tokens[3]);
    yCoordTwo = Integer.parseInt(tokens[4]);
    super.updateXdimensions(xCoordOne);
    super.updateYdimensions(yCoordOne);
    super.updateXdimensions(xCoordTwo);
    super.updateYdimensions(yCoordTwo);
    lineThickness = Integer.parseInt(tokens[7]);
    if (tokens[8].startsWith("N")) {
      fillType = 0;
    } else {
      fillType = 1;
    } // could support more fill types here, but, meh...
  }


  public long localMinXCoord() {
    if (xCoordOne < xCoordTwo) {
      return xCoordOne;
    } else {
      return xCoordTwo;
    }
  }

  public long localMinYCoord() {
    if (yCoordOne < yCoordTwo) {
      return yCoordOne;
    } else {
      return yCoordTwo;
    }
  }

  public String toString(long xOffset, long yOffset) {
    int colorIndex = 3;
    return ("B "
            + (xCoordOne + xOffset) + " "
            + (yCoordOne + yOffset) + " " 
            + (xCoordTwo - xCoordOne) + " " + (yCoordTwo - yCoordOne) + " "
            + colorIndex + " "
            + lineThickness + " "
            + "0 0 "   // for line capstyle (none) and dashstyle (solid)
            + "-1 -1 " // for dashlength and dashspace
            + fillType // 0 for hollow, 1 for solid
            + " -1 -1 -1 -1 -1"); // fill type and fill hatching options not used
  }
  

}
