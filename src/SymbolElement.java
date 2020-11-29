// KicadSymbolToGEDA - a utility for turning kicad modules to gEDA PCB footprints
// SymbolElement.java v1.0
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
* This class is an archetype for kicad symbol elements
*
*/

public class SymbolElement
{

  String elementDescriptor = "";  
  String output = "";
  static long maxX = 0;
  static long minX = 0;
  static long maxY = 0;
  static long minY = 0;
  
  public void SymbolPolyline()
  {
    output = "#Hmm, the no arg symbol polygon constructor didn't do much";
  }

  public void resetXYExtents() {
    maxX = 0;
    minX = 0;
    maxY = 0;
    minY = 0;
  }
  
  public void constructor(String arg)
  {
    elementDescriptor = arg; 
  } 

  public long minXCoord() {
    return minX;
  }

  public long minYCoord() {
    return minY;
  }

  public long maxXCoord() {
    return maxX;
  }

  public long maxYCoord() {
    return maxY;
  }

  public long localMinXCoord() {
    return 0;
  }

  public long localMinYCoord() {
    return 0;
  }

  public String toString(long xOffset, long yOffset) {
    return output;
  }

  // we use the following methods to keep track of the maximum
  // extents of the Symbol in the XY plane, so that we can
  // add text labels tidily. The superclass keeps track
  // of the min, max (x,y) dimensions with static variables 
  public void updateXdimensions(long instanceX) {
    if (instanceX < minX) {
      minX = instanceX;
    } else if (instanceX > maxX) {
      maxX = instanceX;
    }
  }
  public void updateYdimensions(long instanceY) {
    if (instanceY < minY) {
      minY = instanceY;
    } else if (instanceY > maxY) {
      maxY = instanceY;
    }
  }
}
