// KicadSymbolToGEDA - a utility for turning kicad modules to gEDA PCB footprints
// PinList.java v1.1
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
* This class stores a collection of SymbolPin objects and has methods to determine
* Slot definitions, to allow multiple slot devices to be converted from
* Kicad to gschem compatible symbols
*
*/

public class PinList {

  SymbolPin[][] slotArrays;
  int[] pinCounts;
  int numSlots = 1;
  int kicadSlots = 0;
  int pinsPerSlot = 10; //default value, but resizes automatically if needed
  int totalPinCount = 0;
  int maxPinNumber = 0;
  int totalPins = 0;

  long boundingBoxXMax = 0;
  long boundingBoxYMax = 0;
  long boundingBoxXMin = 0;
  long boundingBoxYMin = 0;

  long minX = 0;
  long minY = 0;
  long maxX = 0;
  long maxY = 0;

  int leftPinTally = 0;
  int downPinTally = 0;
  int rightPinTally = 0;
  int upPinTally = 0; 

  public int length() {
    return totalPins;
  }

  public PinList(int slotCount) {
    kicadSlots = slotCount;
    numSlots = slotCount + 1;
    //    System.out.println("New pinlist created with " + numSlots + " slots");
    slotArrays = new SymbolPin[numSlots][pinsPerSlot];
    pinCounts = new int[numSlots];
  }

  public void addPin(SymbolPin newPin) {
    int currentSlot = newPin.slot();
    //System.out.println("Added a pin from slot: " + currentSlot );
    //System.out.println(newPin.toString(0,0));
    slotArrays[currentSlot][pinCounts[currentSlot]] = newPin;
    pinCounts[currentSlot] = pinCounts[currentSlot] + 1;
    totalPinCount++;
    if (newPin.pinDirection() == 'L') {
      leftPinTally++;
    } else if (newPin.pinDirection() == 'D') {
      downPinTally++;
    } else if (newPin.pinDirection() == 'R') {
      rightPinTally++;
    } else if (newPin.pinDirection() == 'U') {
      upPinTally++;
    } 

    // now we work towards sorting out the dimensions of the
    // rectangle needed for the symbol
    if (totalPinCount == 1) {
      boundingBoxXMax = newPin.currentInactiveX();
      boundingBoxYMax = newPin.currentInactiveY();
      boundingBoxXMin = newPin.currentInactiveX();
      boundingBoxYMin = newPin.currentInactiveY();
      minX = newPin.localMinXCoord();
      maxX = newPin.localMaxXCoord();
      minY = newPin.localMinYCoord();
      maxY = newPin.localMaxYCoord();
    } else {
      if (boundingBoxXMax < newPin.currentInactiveX()) {
        boundingBoxXMax = newPin.currentInactiveX();
      }
      if (boundingBoxYMax < newPin.currentInactiveY()) {
        boundingBoxYMax = newPin.currentInactiveY();
      }
      if (boundingBoxXMin > newPin.currentInactiveX()) {
        boundingBoxXMin = newPin.currentInactiveX();
      }
      if (boundingBoxYMin > newPin.currentInactiveY()) {
        boundingBoxYMin = newPin.currentInactiveY();
      }
      if (minX > newPin.localMinXCoord()) {
        minX = newPin.localMinXCoord();
      };
      if (minY > newPin.localMinYCoord()) {
        minY = newPin.localMinYCoord();
      };
      if (maxX < newPin.localMaxXCoord()) {
        maxX = newPin.localMaxXCoord();
      };
      if (maxY < newPin.localMaxYCoord()) {
        maxY = newPin.localMaxYCoord();
      };

    }

    // we test to see if our pin storage structure is full.
    // If so, we create a new one twice the size, and copy
    // everything over to it
    if (pinCounts[currentSlot] == pinsPerSlot) {
      pinsPerSlot = pinsPerSlot*2;
      SymbolPin[][] biggerSlotArrays = new SymbolPin[numSlots][pinsPerSlot];
      for (int slot = 0; slot < numSlots; slot++) {
        for (int pin = 0; pin < pinCounts[slot]; pin++) {
          biggerSlotArrays[slot][pin] = slotArrays[slot][pin];
        }
      }
      slotArrays = biggerSlotArrays;
      // System.out.println("I just resized the pin data structure.");
    }
    if (newPin.pinNumber() > maxPinNumber) {
      maxPinNumber = newPin.pinNumber();
    }
    totalPins++;
  }


  // this is used to generate a "snapped to grid" symbol,
  // i.e. from unusually spaced kicad symbols
  public PinList pinsGridAligned(int spacing) { // default usu. 200
    SymbolPin [] ordered = new SymbolPin[totalPins];
    ordered = this.pinOrderedList();
    // we reset the pin tallies from the addpin routine
    leftPinTally = 0;
    downPinTally = 0;
    rightPinTally = 0;
    upPinTally = 0; 
    // System.out.println("Length of ordered pin list: "
    //                   + ordered.length);
    for (int index = 0; index < totalPins; index++) {
      //System.out.println(ordered[index]);
      //System.out.println(ordered[index+1]);
      if (ordered[index].pinDirection() == 'L') {
        leftPinTally++;
      } else if (ordered[index].pinDirection() == 'D') {
        downPinTally++;
      } else if (ordered[index].pinDirection() == 'R') {
        rightPinTally++;
      } else if (ordered[index].pinDirection() == 'U') {
        upPinTally++;
      } 
    }    
    int overallWidth = 0;
    if ((downPinTally < 5) && (upPinTally <5)) {
      overallWidth = (10 * spacing);
    } else if (downPinTally > upPinTally) {
      overallWidth = (downPinTally + 6) * spacing;
    } else {
      overallWidth = (upPinTally + 6) * spacing;
    }
    int overallHeight = 0;
    if (leftPinTally > rightPinTally) {
      overallHeight = (leftPinTally + 1) * spacing;
    } else {
      overallHeight = (rightPinTally + 1) * spacing;
    }
    long originalY = 0;
    long originalX = 0;
    long currentY = 0;
    long currentX = 0;

    int RIndex = 0;
    int LIndex = 0;
    int DIndex = 0;
    int UIndex = 0;
    // now, just to confuse matters, a 'R' pin has its active
    // end going to the left, and inactive end towards the
    // right, along the left edge of the symbol.
    // everything is relative to pin 1 snapped to spacing grid
    for (int index = 0; index < totalPins; index++) {
      if (ordered[index].pinDirection() == 'R') {
        if (RIndex == 0) {
          currentY
              = spacing*(ordered[index].currentInactiveY()/spacing);
          originalY = currentY; // snapped to grid
          // System.out.println("First R pin current Y: " + originalY);
          originalX
              = spacing*(ordered[index].currentInactiveX()/spacing);
           // snapped to grid
          ordered[index].setNewInactiveOrigin(originalX, originalY); 
        } else {
          currentY -= spacing;
          ordered[index].setNewInactiveOrigin(originalX, currentY); 
        }
        RIndex++;
      }
    }

    for (int index = totalPins - 1; index >= 0; index--) {
      if (ordered[index].pinDirection() == 'L') {
        // System.out.println("LIndex: " + LIndex);
        if (LIndex == 0) {
          currentY = originalY;
          currentX
              = spacing*(ordered[index].currentInactiveX()/spacing);
          // System.out.println("L pins current first Y: " + currentY);
          if (currentX < (originalX + overallWidth)) {
            currentX = originalX + overallWidth;
          } 
          ordered[index].setNewInactiveOrigin(currentX, currentY); 
        } else {
          currentY -= spacing;
          ordered[index].setNewInactiveOrigin(currentX, currentY);
        }
        LIndex++;
      }
    }

    for (int index = 0; index < totalPins; index++) {
    //    for (int index = totalPins - 1; index >= 0; index--) {
      if (ordered[index].pinDirection() == 'U') {
        if (UIndex == 0) {
          currentY
              = spacing*(ordered[index].currentInactiveY()/spacing);
          // System.out.println("Up pins initial Y :" + currentY);
          // System.out.println("Overall height :" + overallHeight);
          if (currentY > (originalY - overallHeight + spacing)) {
            currentY = originalY - overallHeight + spacing;
          }
          //System.out.println("Up, corrected initial origin Y :"
          //                   + currentY);
          currentX
              = spacing*(ordered[index].currentInactiveX()/spacing);
          if (currentX != (originalX + spacing)) {
            // currentX = originalX + overallWidth - spacing;
            currentX = originalX + spacing;
          }
          if (DIndex == 0) {
            currentX += 3*spacing;
          }

          ordered[index].setNewInactiveOrigin(currentX, currentY); 
        } else {
          currentX += spacing;
          ordered[index].setNewInactiveOrigin(currentX, currentY); 
        }
        UIndex++;
      }
    }

    for (int index = totalPins - 1; index >= 0; index--) {
    //    for (int index = 0; index < totalPins; index++) {
      if (ordered[index].pinDirection() == 'D') {
        if (DIndex == 0) {
          currentY
              = spacing*(ordered[index].currentInactiveY()/spacing);
          if (currentY < (originalY + spacing)) {
            currentY = originalY + spacing;
          } 
          //System.out.println("First D pin current Y: " + currentY);
          currentX
              = spacing*(ordered[index].currentInactiveX()/spacing);
          if (currentX != (originalX + overallWidth - spacing)) {
            // currentX = originalX + spacing;
            currentX = originalX + overallWidth - spacing;
          }
          if (DIndex == 0) {
            currentX -= 3*spacing;
          }
          ordered[index].setNewInactiveOrigin(currentX, currentY); 
        } else {
          currentX -= spacing;
          ordered[index].setNewInactiveOrigin(currentX, currentY);
        }
        DIndex++;
      }
    }

    PinList gridAlignedPins = new PinList(kicadSlots);
    //    gridAlignedPins.resetXYExtents();
        // now need to recalculate bounds while
        // adding transmogrified pins to new pin list
    for (int index = 0; index < totalPins; index++) {
      gridAlignedPins.addPin(ordered[index]);
    }
    gridAlignedPins.calculateBoundingBox(spacing);
    return gridAlignedPins;
  }

  // this applies an Eagle set of pin mapping to the
  // pin list
  public void applyEagleDeviceDef(EagleDevice deviceDef) {
    for (int index = 0; index < numSlots; index++) {
      for (int index2 = 0; index2 < pinCounts[index]; index2++) {
        String newPadNum = "0"; //default
        String pinName = slotArrays[index][index2].pinName;
        //System.out.println("This pin is called: " + pinName);
        newPadNum = deviceDef.supplyPadNum(pinName);
        //System.out.println("EagleDevice calls it: " + newPadNum);
        slotArrays[index][index2].pinNumber = newPadNum;
        // apply pad number to symbol pin
      }
    }
  }

  // this returns an array of SymbolPins that have
  // been sorted into ascending order by pin number
  public SymbolPin [] pinOrderedList() {
    SymbolPin [] tempList = new SymbolPin[totalPins];
    int currentPin = 0;
    // we create a single array of pins for later sorting
    for (int index = 0; index < numSlots; index++) {
      for (int index2 = 0; index2 < pinCounts[index]; index2++) {
        //SymbolPin tempPin = slotArrays[index][index2].copy());
        //System.out.println("Constructor text: " +
        //                   slotArrays[index][index2].pinDescriptor);
        //System.out.println("and pin is AKA tempPin: " + tempPin);
        tempList[currentPin] = slotArrays[index][index2].copy();
        currentPin++;
        //System.out.println("Adding pin "
        //                   + currentPin + " to ordered list"); 
        //System.out.println("and pin is : " + tempList[currentPin-1]);
        //System.out.println("and pin is AKA tempPin: " + tempPin);

      }
    }
    // we create the return array
    SymbolPin [] returnList = new SymbolPin[totalPins];
    currentPin = 0;
    // we populate the return array in pin number order
    // which might range from 0 to maxPinNumber
    for (int index = 0; index <= maxPinNumber; index++) {
      for (int index2 = 0; index2 < totalPins; index2++) {
        //SymbolPin tempPin = tempList[index2];
        //System.out.println("TempPin during SymbolPin[] ordering: "
        //                   + tempPin + " , " + tempList[index2]);
        //System.out.println("TempPin pin Number: " + 
        //                   tempList[index2].pinNumber);
        if (tempList[index2].pinNumber() == index) {
          returnList[currentPin] = tempList[index2].copy();
          //System.out.println("now readying return array");
          currentPin++;
        }
      } 
    }
    return returnList;
  }

  // BXL pin defs are contained in a separate section
  // to the symbol description
  public void setBXLPinType(String BXLCompPinDef) {
    BXLCompPinDef = BXLCompPinDef.replaceAll("  ", " ");
    int indexOne = BXLCompPinDef.indexOf("CompPin ");
    int indexTwo = BXLCompPinDef.indexOf(" ", indexOne + 8);
    String pinDesc = BXLCompPinDef.substring(indexOne + 8, indexTwo);
    //System.out.println("PinDesc:" + pinDesc); 
    pinDesc = pinDesc.replaceAll("\"", "");
    pinDesc = pinDesc.replaceAll(" ", "");
    int BXLPinNum = Integer.parseInt(pinDesc);
    //System.out.println("Pin num:" + BXLPinNum);
    indexOne = BXLCompPinDef.indexOf("(PinType");
    indexTwo = BXLCompPinDef.indexOf(")", indexOne);
    String pinType = BXLCompPinDef.substring(indexOne + 9, indexTwo);
    //System.out.println("Pin type:" + pinType);
    for (int index = 0; index < numSlots; index++) {
      for (int index2 = 0; index2 < pinCounts[index]; index2++) {
        if (slotArrays[index][index2].pinNumber() == BXLPinNum) {
          slotArrays[index][index2].setPinType(pinType);
        }
      }
    }
  }

  // BSD pin types are contained in a separate "ports" section
  // vs the symbol's pin mapping section
  // we use thise method to define the electrical type
  // of the pin in the port def String 
  public void setBSDPinType(String [] BSDPortDefs) {
    for (String BSDPortDef : BSDPortDefs) {
      BSDPortDef = BSDPortDef.trim().replaceAll("[:;\"]", "");
      BSDPortDef = BSDPortDef.replaceAll("\t", " ");
      BSDPortDef = BSDPortDef.replaceAll(" \\s*", " ");
      String [] tokens = BSDPortDef.split(" ");
      for (int index = 0; index < numSlots; index++) {
        for (int index2 = 0; index2 < pinCounts[index]; index2++) {
          if (slotArrays[index][index2].pinName().equals(tokens[0])) {
            slotArrays[index][index2].setBSDPinType(tokens);
          }
        }
      }
    }
  }

  // this method is used to create a DIL style symbol
  // with pins ordered by pin number, useful if
  // a list of pin definitions has been provided in
  // no particular order and without useful geometry
  // i.e. pin defs from a csv, IBIS or BSDL file  
  public PinList createDILSymbol() {
    SymbolPin [] orderedPins = this.pinOrderedList();
    PinList newPinList = new PinList(0);
    int defaultSpacing = 200;
    int defaultPinLength = 300;
    long symHeight = (orderedPins.length/2 + 1)*defaultSpacing;
    //        orderedPins[0].resetXYExtents();
    orderedPins[0].setNewActiveOrigin(0,symHeight);
    // we now arrange the spacing of the LHS of the symbol
    // working downwards
    for (int index = 1; index < orderedPins.length/2; index++) {
      orderedPins[index].propagateSpacing(orderedPins[index-1], 200);
    }
    // we now start with the first pin on the RHS of the DIL symbol
    orderedPins[orderedPins.length/2].setNewActiveOrigin(orderedPins[orderedPins.length/2-1]);
    // we now create a symbol ~2000 mil wide, given pin lengths of 300
    int RSideShift = 2*defaultPinLength + 2000;
    orderedPins[orderedPins.length/2].shiftBSDLPinToRHS(RSideShift);
    // now we propagate the pin spacing up the RHS
    for (int index = orderedPins.length/2+1;
         index < orderedPins.length;
         index++) {
      orderedPins[index].setPinDirection("L"); // move pin to RHS
      orderedPins[index].propagateSpacing(orderedPins[index-1], 200);
    }
    for (int index = 0; index < orderedPins.length; index++) {
      newPinList.addPin(orderedPins[index]);
    }
    // newPinList.calculateBoundingBox(200); don't need this
    newPinList.setMinY(newPinList.minY() - defaultSpacing);
    return newPinList;
  }

  // this returns the bounding box rectangle object
  public SymbolRectangle boundingBox(long xOffset, long yOffset) {
    int thickness = 0;
    int fill = 0;
    //System.out.println("Bounding box coords: " +
    //                 boundingBoxXMin + 
    //                 ", " + boundingBoxYMin +
    //                 ", " + boundingBoxXMax +
    //                 ", " + boundingBoxYMax);
    SymbolRectangle temp
        = new SymbolRectangle((int) (boundingBoxXMin + xOffset),
                              (int) (boundingBoxYMin + yOffset),
                              (int) (boundingBoxXMax + xOffset),
                              (int) (boundingBoxYMax + yOffset),
                              thickness, fill);
    //System.out.println("Generated new bounding box");
    return temp;
  }

  // this is used to generate a bounding box to
  // suit pins that have been generated on an
  // arbitrary grid spacing, i.e. symbol being
  // generated from a BSDL, IBIS, csv etc... pin
  // list that does not include symbol outline
  // box information
  public SymbolRectangle calculatedBoundingBox() {
    return calculatedBoundingBox(0, 0, 200);
  } //default spacing

  public SymbolRectangle calculatedBoundingBox(long xOffset,
                                         long yOffset) {
    return calculatedBoundingBox(xOffset, yOffset, 200);
  } //default spacing

  public SymbolRectangle calculatedBoundingBox(long xOffset,
                                         long yOffset,
                                         int spacing) {
    // we now make the bounding box bigger along
    // sides which have no pins
    int thickness = 0;
    int fill = 0;
    long BBYMin = boundingBoxYMin;
    long BBYMax = boundingBoxYMax;
    long BBXMin = boundingBoxXMin;
    long BBXMax = boundingBoxXMax;
    if (upPinTally == 0) {
      BBYMin -= spacing;
      if (BBYMin < minY) {
        minY = BBYMin;
      }
    }
    if (downPinTally == 0) {
      BBYMax += spacing;      
    }
    if (leftPinTally == 0) {
      BBXMax += spacing;      
    }
    if (rightPinTally == 0) {
      BBXMin -= spacing;
      if (BBXMin < minX) {
        minX = BBXMin;
      }
    }
    SymbolRectangle temp
        = new SymbolRectangle((int) (BBXMin + xOffset),
                              (int) (BBYMin + yOffset),
                              (int) (BBXMax + xOffset),
                              (int) (BBYMax + yOffset),
                              thickness, fill);
    //System.out.println("Generated new bounding box");
    return temp;
  }

  private void calculateBoundingBox(int spacing) {
    // we now make the bounding box bigger along
    // sides which have no pins
    if (upPinTally == 0) {
      boundingBoxYMin -= spacing;
    }
    if (downPinTally == 0) {
      boundingBoxYMax += spacing;      
    }
    if (leftPinTally == 0) {
      boundingBoxXMax += spacing;      
    }
    if (rightPinTally == 0) {
      boundingBoxXMin -= spacing;
    }
    //System.out.println("Generated new bounding box");
  }

  public String toString(long xOffset, long yOffset) {
    String output = "";
    SymbolPin.resetPinSeqTally();
    for (int index = 0; index < pinCounts[0]; index++) {
      output = output + "\n" + slotArrays[0][index].toString(xOffset, yOffset);
    }
    if (pinCounts.length > 1) { // length == 1 for bxl files
      for (int index = 0; index < pinCounts[1]; index++) {
        // by default, for a multislot device, we only display slot 1
        output = output + "\n" + slotArrays[1][index].toString(xOffset, yOffset); 
      }
    }
    // we offset text attributes to the RHS by default
    // this should not cause issues with schematic conversions
    output = output + slotSummaryRHS(xOffset, yOffset);
    return output;
  }

  public String slotSummary(long xOffset, long yOffset) {
    return slotSummary(xOffset, yOffset, 0); // no RHS offset
  }

  public String slotSummaryRHS(long xOffset, long yOffset) {
    return slotSummary(xOffset, yOffset, textRHS()); // offset to RHS
  }

  private String slotSummary(long xOffset, long yOffset, long ROffset) {
    String summary = "";
    if (kicadSlots < 2) {
      summary = SymbolText.attributeString(ROffset + xOffset, yOffset, "numslots=0");
    } else { // this is a multi-slot device
      // we summarise the number of slots
      summary = SymbolText.attributeString(ROffset + xOffset, yOffset, "numslots=" + kicadSlots);
      // we explain which slot is implemented in the symbol
      summary = summary + SymbolText.attributeString(ROffset + xOffset, yOffset, "slot=1");
      // then we generate some slotdefs
      for (int index = 1; index < numSlots; index++) {
        summary = summary + SymbolText.attributeString(ROffset + xOffset, yOffset, "slotdef=" + index + ":");
        for (int pin = 0 ; pin < pinCounts[index]; pin ++) {
          summary = summary + slotArrays[index][pin].pinNumber;
          if (pin < (pinCounts[index] -1)) {
            summary = summary + ",";
          }
        }
      }
    }
    return summary;
  }

  public long textRHS() {
    return maxX() + 200; // an offset to move the text off the symbol 
  }

  public void setMinY(long newMinY) {
    minY = newMinY;
  }
  
  public long minX() { // this returns pin minX
    minX = slotArrays[0][0].localMinXCoord();
    for (int index = 0; index < numSlots; index++) {
      for (int pin = 0 ; pin < pinCounts[index]; pin ++) {
        if (minX > slotArrays[index][pin].localMinXCoord()) {
          minX = slotArrays[index][pin].localMinXCoord();
        }
      }
    }
    return minX;
  }

  public long minY() { // this returns pin minY
    minY = slotArrays[0][0].localMinYCoord();
    for (int index = 0; index < numSlots; index++) {
      for (int pin = 0 ; pin < pinCounts[index]; pin ++) {
        if (minY > slotArrays[index][pin].localMinYCoord()) {
          minY = slotArrays[index][pin].localMinYCoord();
        };
      }
    }
    return minY;
  }

  public long maxX() { // this returns pin maxX
   maxX = slotArrays[0][0].localMaxXCoord();
    for (int index = 0; index < numSlots; index++) {
      for (int pin = 0 ; pin < pinCounts[index]; pin ++) {
        if (maxX < slotArrays[index][pin].localMaxXCoord()) {
          maxX = slotArrays[index][pin].localMaxXCoord();
        }
      }
    }
    return maxX;
  } 

  public long maxY() { // this returns pin maxY
   maxY = slotArrays[0][0].localMaxYCoord();
    for (int index = 0; index < numSlots; index++) {
      for (int pin = 0 ; pin < pinCounts[index]; pin ++) {
        if (maxY < slotArrays[index][pin].localMaxYCoord()) {
          maxY = slotArrays[index][pin].localMaxYCoord();
        }
      }
    }
    return maxY;
  } 

  public boolean empty() {
    return (totalPins == 0);
  }

}
