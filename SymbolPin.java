// KicadSymbolToGEDA - a utility for turning kicad modules to gEDA PCB footprints
// SymbolPin.java v1.0
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

import java.lang.Exception;

/**
 *
 * This class is passed a Kicad Pin descriptor string of the form "X Ni x1 y1 x2 y2 ... xi yi fill"
 * and implements a method which can generate a gschema line definitions for a gEDA symbol
 *
 */

//X name number posx posy length orientation Snum Snom unit convert Etype [shape].
//With:
//• orientation = U (up) D (down) R (right) L (left).
//• name = name (without space) of the pin. if ~: no name
//• number = n pin number (4 characters maximum).
//• length = pin length.
//• Snum = pin number text size.
//• Snom = pin name text size.
//• unit = 0 if common to all parts. If not, number of the part (1. .n).
//• convert = 0 if common to the representations, if not 1 or 2.
//• Etype = electric type (1 character)
//• shape = if present: pin shape (clock, inversion...).

public class SymbolPin extends SymbolElement
{
  // the following static variable keeps track
  // of how many pin descriptions have been generated
  // in multislot devices when the symbols is exported
  // in gschem format
  // hmm, need to re-initialise it after a symbol has been
  // generated if batches of symbols being done...
  static int pinSeqTally = 0;

  String pinDescriptor = "";
  String output = "";
  
  String pinName = "";
  String pinNumber = ""; // kicad sometimes uses "~" != integer
  String pinDesc = "";
  long xCoord1 = 0;
  long yCoord1 = 0;
  long xCoord2 = 0;
  long yCoord2 = 0;
  long pinNumberX = 0;
  long pinNumberY = 0;
  long pinNameX = 0;
  long pinNameY = 0;
  long pinLength = 0;
  String pinDirection = "0"; // default non-sensical value
  int pinType = 0; // 0 = normal, and 1 = bus/unused
  int activeEnd = 0; // 1 = first end, 0 = second end
  int kicadUnit = 0; // equivalent to gschem "slot"

  String kicadEType = ""; // kicad equivalent to gschem pintype=
  String pinEType = "pas"; //default setting

  // the following variables define the visual appearance of the
  // pin, plus its pin label, pin number, and pinseq attribute text
  // colours, size and visibility in gschem
  long textKerning = 15; // to make text sit adjacent to other elements
  int pinNumberAlignment = 3; // this centres pin number mid pin
  int pinNumberOrientation = 0; // degrees rotation from +ve x-axis
  int pinNameAlignment = 0; // default 0 => bottom left corner at (x,y)
  int pinNameOrientation = 0;
  int pinColourIndex = 3;
  int pinLabelColour = 3;
  int pinLabelSize = 5;
  int pinLabelVis = 1;
  int pinLabelShow = 1;
  int pinNumberColour = 5;
  int pinNumberSize = 7;
  int pinNumberVis = 1;
  int pinNumberShow = 1;
  int pinSeqColour = 5;
  int pinSeqTextSize = 7;
  int pinSeqVis = 0;
  int pinSeqShow = 1;

  public static void resetPinSeqTally() {
    pinSeqTally = 0;
  }

  public void SymbolPin()
  {
    output = "#Hmm, the no arg symbol pin constructor didn't do much";
  }
  
  public SymbolPin copy() {
    SymbolPin copyOf = new SymbolPin();
    copyOf.constructor(this.pinDescriptor);
    return copyOf;
  }

  public SymbolPin copy(SymbolPin orig) {
    SymbolPin copyOf = new SymbolPin();
    copyOf.constructor(orig.pinDescriptor);
    return copyOf;
  }

  public void populateBXLElement(String BXLLine) {
    pinDescriptor = BXLLine;
    BXLLine = BXLLine.replaceAll("[\"(),]","");
    String [] tokens = BXLLine.split(" ");
    int pinRotation = 0;
    for (int index = 0; index < tokens.length; index++) {
      if (tokens[index].equals("Origin")) {
        xCoord1 = Integer.parseInt(tokens[++index]);
        yCoord1 = Integer.parseInt(tokens[++index]);
      } else if (tokens[index].equals("PinLength")) {
        pinLength = Integer.parseInt(tokens[++index]);
      } else if(tokens[index].equals("PinNum")) {
        pinNumber = tokens[++index];
      } else if(tokens[index].equals("PinDes")) {
        pinDesc = tokens[++index];
      } else if(tokens[index].equals("PinName")) {
        pinName = tokens[++index];
      } else if(tokens[index].equals("Rotate")) { // in degrees
        pinRotation = Integer.parseInt(tokens[++index]);
        // need to remember, y-axis is flipped
      }
    }

    if (pinRotation == 0) {
      pinDirection = "L";
      xCoord1 += pinLength;
    } else if (pinRotation == 180) {
      pinDirection = "R";
      xCoord1 -= pinLength;
    } else if (pinRotation == 90) {
      pinDirection = "D";
      yCoord1 += pinLength;
    } else if (pinRotation == 270) {
      pinDirection = "U";
      yCoord1 -= pinLength;
    } 

    super.updateXdimensions(xCoord1);
    super.updateYdimensions(yCoord1);
    organiseLabelAndPinCoords();

    kicadUnit = 0; // assume only one slot
    pinEType = "pas"; // default for now, may be able to fix

  }


  public void constructor(String arg)
  {
    pinDescriptor = arg;
    arg = arg.replaceAll("  "," ");    
    String[] tokens = arg.split(" ");

    pinName = tokens[1];
    pinNumber = tokens[2];
    pinDesc = tokens[2];
    xCoord1 = Integer.parseInt(tokens[3]);
    yCoord1 = Integer.parseInt(tokens[4]);
    super.updateXdimensions(xCoord1);
    super.updateYdimensions(yCoord1);
    pinLength = Integer.parseInt(tokens[5]);
    pinDirection = tokens[6].toUpperCase();

    organiseLabelAndPinCoords();

    // the kicadUnit is equivalent to the slot in gschem... useful
    kicadUnit = Integer.parseInt(tokens[9]);

    kicadEType = tokens[11]; // the electrical type of the pin
    if (kicadEType.equals("I")) {
      pinEType = "in";
    } else if (kicadEType.equals("O")) {
      pinEType = "out";
    } else if (kicadEType.equals("T")) {
      pinEType = "tri";
    } else if (kicadEType.equals("W") || kicadEType.equals("w")) {
      pinEType = "pwr";
    } else if (kicadEType.equals("C")) {
      pinEType = "oc";
    } else if (kicadEType.equals("E")) {
      pinEType = "oe";
    } else {
      pinEType = "pas"; // default setting catches bidir, unspec. 
    } // kicad has no gschem totem pole or clock pin equivalents

  }

  public int pinNumber() {
    // we assume the kicad library has a valid number field
    // if not, return default of zero
    int returnNumber = 0;
    // System.out.println("The pinNumber method returns:" + pinNumber);
    try {
      returnNumber = Integer.parseInt(pinNumber);
    }
    catch (Exception e) {
      // ho hum
    }
    // System.out.println("The pinNumber method returns revised:" + pinNumber);
    return returnNumber;
  }

  public long localMinXCoord() {
    if (xCoord1 < xCoord2) {
      return xCoord1;
    } else {
      return xCoord2;
    }
  }

  public long localMinYCoord() {
    if (yCoord1 < yCoord2) {
      return yCoord1;
    } else {
      return yCoord2;
    }
  }

  public long localMaxXCoord() {
    if (xCoord1 < xCoord2) {
      return xCoord2;
    } else {
      return xCoord1;
    }
  }

  public long localMaxYCoord() {
    if (yCoord1 < yCoord2) {
      return yCoord2;
    } else {
      return yCoord1;
    }
  }

  public long currentActiveX() {
    return xCoord1;
  }

  public long currentActiveY() {
    return yCoord1;
  }

  public long currentInactiveX() {
    return xCoord2;
  }

  public long currentInactiveY() {
    return yCoord2;
  }

  public void setNewInactiveY(long newY) {
    long deltaY = newY - yCoord2;
    yCoord2 = newY;
    yCoord1 += deltaY;
    organiseLabelAndPinCoords();
  }

  public void setNewInactiveX(long newX) {
    long deltaX = newX - xCoord2;
    xCoord2 = newX;
    xCoord1 += deltaX;
    organiseLabelAndPinCoords();
  }

  public void setNewActiveY(long newY) {
    long deltaY = newY - yCoord1;
    yCoord1 = newY;
    yCoord2 += deltaY;
    organiseLabelAndPinCoords();
  }

  public void setNewActiveX(long newX) {
    long deltaX = newX - xCoord1;
    xCoord1 = newX;
    xCoord2 += deltaX;
    organiseLabelAndPinCoords();
  }


  public void setNewActiveOrigin(long newX, long newY) {
    long deltaX = newX - xCoord1;
    long deltaY = newY - yCoord1;
    xCoord1 = newX;
    yCoord1 = newY;
    xCoord2 += deltaX;
    yCoord2 += deltaY;
    organiseLabelAndPinCoords();
  }

  public void setNewInactiveOrigin(long newX, long newY) {
    long deltaX = newX - xCoord2;
    long deltaY = newY - yCoord2;
    xCoord2 = newX;
    yCoord2 = newY;
    xCoord1 += deltaX;
    yCoord1 += deltaY;
    organiseLabelAndPinCoords();
  }

  public String toString() {
    return pinDescriptor;
  }

  public String toString(long xOffset, long yOffset) {
    return ("P "
            + (xCoord1 + xOffset) + " "
            + (yCoord1 + yOffset) + " " 
            + (xCoord2 + xOffset) + " "
            + (yCoord2 + yOffset)  + " "
            + pinColourIndex + " "
            + pinType + " "
            + activeEnd  // one implies (xCoord1, yCoord1)
            + "\n{\n" 
            + attributeFieldPinNumber(pinDesc, pinNumberX + xOffset, pinNumberY + yOffset, pinNumberOrientation, pinNumberAlignment)
            + "\n"
            + attributeFieldPinLabel(pinName, pinNameX + xOffset, pinNameY + yOffset, pinNameOrientation, pinNameAlignment)
            + "\n"
            + attributeFieldPinSeq(pinDesc, pinNumberX + xOffset, pinNumberY + yOffset, pinNumberOrientation, pinNumberAlignment)
            + "\n"
            + attributeFieldPinType(pinEType, pinNumberX + xOffset, pinNumberY + yOffset, pinNumberOrientation, pinNumberAlignment)
            + "\n}");
  }

  public int slot() {
    return kicadUnit; // kicadUnit is equivalent to slot in gschem
  }

  public char pinDirection() {
    return pinDirection.charAt(0);
  }

  private String attributeFieldPinLabel(String pinLabel, long X, long Y, int orientation, int alignment)  {
    int numLines = 1;
    return SymbolText.toString(X, Y, pinLabelColour, pinLabelSize, pinLabelVis, pinLabelShow, orientation, alignment, numLines, ("pinlabel=" + pinLabel));
  }

  private String attributeFieldPinNumber(String pinDesc, long X, long Y, int orientation, int alignment)  {
    int numLines = 1;
    return SymbolText.toString(X, Y, pinNumberColour, pinNumberSize, pinNumberVis, pinNumberShow, orientation, alignment, numLines, ("pinnumber=" + pinDesc));
  }

  private String attributeFieldPinSeq(String pinDesc, long X, long Y, int orientation, int alignment)  {
    int numLines = 1;
    // we use the class static variable pinSeqTally to keep track
    // of how many rendered pins have been generated
    pinSeqTally++;
    return SymbolText.toString(X, Y, pinSeqColour, pinSeqTextSize, pinSeqVis, pinSeqShow, orientation, alignment, numLines, ("pinseq=" + pinSeqTally));
  }

  private String attributeFieldPinType(String pinDesc, long X, long Y, int orientation, int alignment)  {
    int numLines = 1;
    // we use the same settings for colour, size and visibility as the hidden pin sequence fields
    return SymbolText.toString(X, Y, pinSeqColour, pinSeqTextSize, pinSeqVis, pinSeqShow, orientation, alignment, numLines, ("pintype=" + pinDesc));
  }

  private void organiseLabelAndPinCoords() {
    // we now sort out the orientation and position
    // of the pin number and pin label, based on the
    // direction of the pin & the pin location,
    // and add some kerning while we are at it
    // to prevent the text obscuring adjacent
    // elements/features
    // importantl, xCoord2, yCoord2, are the non active end
    // of the pin, and the L, R, U, D indicate the direction
    // in which the non active end goes.
    if (pinDirection.startsWith("R")) {
      xCoord2 = xCoord1 + pinLength;
      pinNumberX = (xCoord1 + xCoord2)/2;
      pinNameX = xCoord2 + textKerning;
      yCoord2 = yCoord1;
      pinNameY = yCoord1;
      pinNumberY = yCoord1 + textKerning;
      pinNameAlignment = 1;
    } else if (pinDirection.startsWith("L")) {
      xCoord2 = xCoord1 - pinLength;
      pinNumberX = (xCoord1 + xCoord2)/2;
      pinNameX = xCoord2 - textKerning;
      yCoord2 = yCoord1;
      pinNameY = yCoord1;
      pinNumberY = yCoord1 + textKerning;
      pinNameAlignment = 7;
    } else if (pinDirection.startsWith("U")) {
      xCoord2 = xCoord1;
      pinNameX = xCoord1;
      pinNumberX = xCoord1 - textKerning;
      yCoord2 = yCoord1 + pinLength;
      pinNumberY = (yCoord1 + yCoord2)/2;
      pinNameY = yCoord2 + textKerning;
      pinNumberOrientation = pinNameOrientation = 90; // degrees from +ve x-axis
      pinNameAlignment = 1;
    } else if (pinDirection.startsWith("D")) {
      xCoord2 = xCoord1;
      pinNameX = xCoord1;
      pinNumberX = xCoord1 - textKerning;
      yCoord2 = yCoord1 - pinLength;
      pinNumberY = (yCoord1 + yCoord2)/2;
      pinNameY = yCoord2 - textKerning;
      pinNumberOrientation = pinNameOrientation = 90; // degrees from +ve x-axis
      pinNameAlignment = 7;
    } 
    // we keep track of the overall size of the drawn elements
    super.updateXdimensions(xCoord1);
    super.updateYdimensions(yCoord1);
    super.updateXdimensions(xCoord2);
    super.updateYdimensions(yCoord2);
  }
}
