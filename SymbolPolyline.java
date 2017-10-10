// KicadSymbolToGEDA - a utility for turning kicad modules to gEDA PCB footprints
// SymbolPolyline.java v1.1
//
// v1.1 Now with KiCad export for BXL decoded symbols 
//
// Copyright (C) 2015, 2016, 2017 Erich S. Heinzle, a1039181@gmail.com

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
* This class is passed a Kicad Polyline descriptor string of the form "P Ni x1 y1 x2 y2 ... xi yi fill"
* and implements a method which can generate a gschema line definitions for a gEDA symbol
*
*/

//P Nb parts convert thickness x0 y0 x1 y1 xi yi cc
//With:
//- Nb = a number of points.
//- unit = 0 if common to the parts; if not, number of part (1. .n).
//- convert = 0 if common to the 2 representations, if not 1 or 2.
//- thickness = line thickness.
//- xi yi coordinates of end i.
//- cc = N F or F ( F = filled polygon; f = . filled polygon, N = transparent background)


public class SymbolPolyline extends SymbolElement
{

  String polylineDescriptor = "";  
  String output = "";

  boolean separateLines = false;
  
  int vertices = 0;
  long xCoords[] = new long[30];
  long yCoords[] = new long[30];
  int fillType = 0;
  long lineThickness = 0;
  long minX = 0;
  long minY = 0;

  public void SymbolPolyline()
  {
    output = "#Hmm, the no arg symbol polygon constructor didn't do much";
  }
  
  public void populateBXLElement(String BXLLine) {
    BXLLine = BXLLine.replaceAll("[\"(),]","");
    String [] tokens = BXLLine.split(" ");
    for (int index = 0; index < tokens.length; index++) {
      if (tokens[index].equals("Origin")) {
        xCoords[0] = Math.round(Float.parseFloat(tokens[++index]));
        yCoords[0] = Math.round(Float.parseFloat(tokens[++index]));
        updateCoords(0);
      } else if (tokens[index].equals("EndPoint")) {
        xCoords[1] = Math.round(Float.parseFloat(tokens[++index]));
        yCoords[1] = Math.round(Float.parseFloat(tokens[++index]));
        updateCoords(1);
      } else if (tokens[index].equals("Width")) {
        lineThickness = Math.round(Float.parseFloat(tokens[++index]));
      }
    }
    vertices = 2;
  }

  public void updateCoords(int vertex) {
    // we first update the superclass static variable
    super.updateXdimensions(xCoords[vertex]);
    super.updateYdimensions(yCoords[vertex]);
    // we sort out the local max,min variables
    if (minX > xCoords[vertex]) {
      minX = xCoords[vertex];
    }
    if (minY > yCoords[vertex]) {
      minY = yCoords[vertex];
    }
  }

  public void constructor(String arg)
  {
    polylineDescriptor = arg;
    arg = arg.replaceAll("    "," ");
    arg = arg.replaceAll("   "," ");
    // it seems some ulp converted eagle files have a lot of spaces
    arg = arg.replaceAll("  "," ");
    String[] tokens = arg.split(" ");
    
    vertices = Integer.parseInt(tokens[1]);
    // System.out.println("Vertices found: " + vertices);
    lineThickness = Integer.parseInt(tokens[4]);
    for (int vertex = 0; vertex < vertices; vertex++) {
      xCoords[vertex] = Long.parseLong(tokens[vertex*2+5]);
      yCoords[vertex] = Long.parseLong(tokens[vertex*2+6]);
      updateCoords(vertex);
      // System.out.println("(" +  xCoords[vertex] + ", " +  yCoords[vertex] + ")");
    }
  }

  public long localMinXCoord() {
    return minX;
  }

  public long localMinYCoord() {
    return minY;
  }

  public String toString(long xOffset, long yOffset) {
    int colorIndex = 3;
    for (int index = 0; index < (vertices - 1); index++) {
      output = (output
                + "L "
                + (xCoords[index] + xOffset) + " "
                + (yCoords[index] + yOffset) + " " 
                + (xCoords[index+1] + xOffset) + " "
                + (yCoords[index+1] + yOffset) + " "
                + colorIndex + " "
                + lineThickness + " "
                + "0 0 "     // for line capstyle (none) and dashstyle (solid)
                + "-1 -1"); // for dashlength and dashspace (not used) 
      if (index < (vertices - 2)) {
        output = output + "\n";
      }
    }
    return output;
  }

  public String toKicad(long xOffset, long yOffset) {
    if (separateLines)
         return toKicadLines(xOffset, yOffset);
    else
         return toKicadPolys(xOffset, yOffset);
  }

  /** Prepare a string with mulyiple separate lines made of the polygon data
   * in KiCAD format.
   */
  public String toKicadLines(long xOffset, long yOffset) {
    String kicadOutput = "";
    for (int index = 0; index < (vertices - 1); index++) {
      kicadOutput = (kicadOutput
                + "P "
                + vertices + " "
                + slot + " " // part/unit
                + 1 + " " // dmg/convert
                + 0 + " " // pen/thickness
                + (xCoords[index] + xOffset) + " "
                + (yCoords[index] + yOffset) + " "
                + (xCoords[index+1] + xOffset) + " "
                + (yCoords[index+1] + yOffset) + " "
                + "N"); // not filled
      if (index < (vertices - 2)) {
        kicadOutput = kicadOutput + "\n";
      }
    }
    return kicadOutput;
  }

  /** Prepare a string with polygon data in KiCAD format.
   */
  public String toKicadPolys(long xOffset, long yOffset) {
    String kicadOutput = "";
    // P point_count unit convert thickness (posx posy)* fill
    kicadOutput = (kicadOutput
                + "P "
                + vertices + " "
                + slot + " " // part/unit
                + 1 + " " // dmg/convert
                + 0 + " " // pen/thickness
                );
    for (int index = 0; index < vertices; index++) {
      kicadOutput = (kicadOutput
                  + (xCoords[index] + xOffset) + " "
                  + (yCoords[index] + yOffset) + " "
                  );
    }
    kicadOutput = (kicadOutput
                + "N"); // not filled
    return kicadOutput;
  }

}
