// KicadSymbolToGEDA - a utility for turning kicad modules to gEDA PCB footprints
// PinList.java v1.0
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
      maxX = newPin.maxXCoord();
      minY = newPin.localMinYCoord();
      maxY = newPin.maxYCoord();
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
        maxX = newPin.localMinXCoord();
      };
      if (minY < newPin.localMaxYCoord()) {
        maxY = newPin.localMinYCoord();
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
    if (downPinTally > upPinTally) {
      overallWidth = (downPinTally + 1) * spacing;
    } else {
      overallWidth = (upPinTally + 1) * spacing;
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

  public void calculateBoundingBox(int spacing) {
    // we now make the bounding box bigger along
    // sides which have no pins
    if (upPinTally == 0) {
      boundingBoxYMin = boundingBoxYMin - spacing;
      if (boundingBoxYMin < minY) {
        minY = boundingBoxYMin;
      }
    }
    if (downPinTally == 0) {
      boundingBoxYMax = boundingBoxYMax + spacing;      
    }
    if (leftPinTally == 0) {
      boundingBoxXMax = boundingBoxXMax + spacing;      
    }
    if (rightPinTally == 0) {
      boundingBoxXMin = boundingBoxXMin - spacing;
      if (boundingBoxXMin < minX) {
        minX = boundingBoxXMin;
      }
    }
  }


  public SymbolPin [] pinOrderedList() {
    SymbolPin [] tempList = new SymbolPin[totalPins];
    int currentPin = 0;
    // we create a single array of pins for later sorting
    for (int index = 0; index < numSlots; index++) {
      for (int index2 = 0; index2 < pinCounts[index]; index2++) {
        SymbolPin tempPin = new SymbolPin();
        tempPin.constructor(slotArrays[index][index2].pinDescriptor);
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
        SymbolPin tempPin = tempList[index2];
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
    output = output + slotSummary(xOffset, yOffset);
    return output;
  }

  private String slotSummary(long xOffset, long yOffset) {
    String summary = "";
    if (kicadSlots < 2) {
      summary = SymbolText.attributeString(xOffset, yOffset, "numslots=0");
    } else { // this is a multi-slot device
      // we summarise the number of slots
      summary = SymbolText.attributeString(xOffset, yOffset, "numslots=" + kicadSlots);
      // we explain which slot is implemented in the symbol
      summary = summary + SymbolText.attributeString(xOffset, yOffset, "slot=1");
      // then we generate some slotdefs
      for (int index = 1; index < numSlots; index++) {
        summary = summary + SymbolText.attributeString(xOffset, yOffset, "slotdef=" + index + ":");
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
  
  public long minX() {
    return minX;
  }

  public long minY() {
    return minY;
  }

  public long maxX() {
    return maxX;
  } 

  public long maxY() {
    return maxY;
  } 

}
