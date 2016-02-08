// BXLDecoder.java - a utility for converting Huffman encoded files
// into text, ported to Java from the vala code by Geert Jordaens
//
// BXLDecoder.java v1.0
// Copyright (C) 2016 Erich S. Heinzle, a1039181@gmail.com

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
//    BXLDecoder Copyright (C) 2016 Erich S. Heinzle a1039181@gmail.com


import java.io.*;
import java.util.Scanner;

public class BXLDecoder {

  public static void main (String [] args) {

    boolean textOutputOnly = false;
    String filename = "";

    if (args.length == 0) {
      printHelp();
      System.exit(0);
    } else {
      filename = args[0];
    }
    
    if (args.length == 2) {
      if (args[1].equals("-t")) {
        textOutputOnly = true;
      }
    }

    SourceBuffer buffer = new SourceBuffer(filename); 

    if (textOutputOnly) {
      System.out.println(buffer.decode());
      System.exit(0);
    }

    Scanner textBXL = new Scanner(buffer.decode());

    //parseBXL(decodedBXL);
    
    //}

  //private static void parseBXL(Scanner textBXL) {
    String currentLine = "";
    String newElement = "";
    String newSymbol = "";
    String symAttributes = "";
    PadStackList padStacks = new PadStackList();
    PinList pins = new PinList(0); // slots = 0

    long xOffset = 0;
    long yOffset = 0;
    while (textBXL.hasNext()) {
      currentLine = textBXL.nextLine().trim();
      if (currentLine.startsWith("PadStack")) {
          newElement = currentLine;
          while (textBXL.hasNext() &&
                 !currentLine.startsWith("EndPadStack")) {
            currentLine = textBXL.nextLine().trim();
            newElement = newElement + "\n" + currentLine;
          }
          padStacks.addPadStack(newElement);
          newElement = ""; // reset the variable
      } else if (currentLine.startsWith("Pattern ")) {
        String [] tokens = currentLine.split(" ");
        String FPName = tokens[1].replaceAll("[\"]","");
        while (textBXL.hasNext() &&
               !currentLine.startsWith("EndPattern")) {
          currentLine = textBXL.nextLine().trim();
          if (currentLine.startsWith("Pad")) {
            //System.out.println("#Making new pad: " + currentLine);
            Pad newPad = padStacks.GEDAPad(currentLine);
            newElement = newElement
                + newPad.generateGEDAelement(xOffset,yOffset,1.0f);
          } else if (currentLine.startsWith("Line (Layer TOP_SILKSCREEN)")) {
            DrawnElement silkLine = new DrawnElement();
            silkLine.populateBXLElement(currentLine);
            newElement = newElement
                + silkLine.generateGEDAelement(xOffset,yOffset,1.0f);
          } else if (currentLine.startsWith("Arc (Layer TOP_SILKSCREEN)")) {
            Arc silkArc = new Arc();
            silkArc.populateBXLElement(currentLine);
            newElement = newElement
                + silkArc.generateGEDAelement(xOffset,yOffset,1.0f);
          }


        }
        try {
          File newFP = new File(FPName + ".fp");
          PrintWriter FPOutput = new PrintWriter(newFP);
          FPOutput.println("Element[\"\" \""
                           + FPName
                           + "\" \"\" \"\" 0 0 0 25000 0 100 \"\"]\n(\n"
                           + newElement
                           + ")");
          FPOutput.close();
          System.out.println(FPName + ".fp");
        } catch(Exception e) {
          System.out.println("There was an error saving: "
                             + FPName + ".fp");
          System.out.println(e);
        }
        newElement = ""; // reset the variable
      } else if (currentLine.startsWith("Symbol ")) {
        String [] tokens = currentLine.split(" ");
        String SymbolName = tokens[1].replaceAll("[\"]","");
        //        PinList pins = new PinList(0); // slots = 0
        pins = new PinList(0); // slots = 0
        while (textBXL.hasNext() &&
               !currentLine.startsWith("EndSymbol")) {
          currentLine = textBXL.nextLine().trim();
          if (currentLine.startsWith("Pin")) {
            //System.out.println("#Making new pin: " + currentLine);
            SymbolPin latestPin = new SymbolPin();
            currentLine = currentLine + " " +
                textBXL.nextLine().trim() + " " +
                textBXL.nextLine().trim(); // we combine the 3 lines
            latestPin.populateBXLElement(currentLine);
            pins.addPin(latestPin);
          } else if (currentLine.startsWith("Line")) {
            SymbolPolyline symbolLine = new SymbolPolyline();
            symbolLine.populateBXLElement(currentLine);
            newElement = newElement
                + "\n" + symbolLine.toString(0,0);
          } else if (currentLine.startsWith("Arc (Layer TOP_SILKSCREEN)")) {
            Arc silkArc = new Arc();
            silkArc.populateBXLElement(currentLine);
            newElement = newElement
                + silkArc.generateGEDAelement(xOffset,yOffset,1.0f);
          } else if (currentLine.startsWith("Attribute")) {
            SymbolText attrText = new SymbolText();
            attrText.populateBXLElement(currentLine);
            if (symAttributes.length() == 0) {
              symAttributes = symAttributes
                  + SymbolText.BXLAttributeString(0,0, currentLine);
                  // + attrText.toString(0,0, false);
            } else {
              symAttributes = symAttributes
                  + SymbolText.BXLAttributeString(0,0, currentLine);
                  // + attrText.toString(0,0, false);
            }

          }
        }
        newSymbol = "v 20110115 1"
            + newElement; // we have created the header for the symbol
        newElement = "";
      } else if (currentLine.startsWith("Component ")) {
        String [] tokens = currentLine.split(" ");
        String symbolName = tokens[1].replaceAll("[\"]","");
        while (textBXL.hasNext() &&
               !currentLine.startsWith("EndComponent")) {
          currentLine = textBXL.nextLine().trim();
          if (currentLine.startsWith("Attribute")) {
            SymbolText attrText = new SymbolText();
            attrText.populateBXLElement(currentLine);
            symAttributes = symAttributes
                + SymbolText.BXLAttributeString(0,0, currentLine);
          } else if (currentLine.startsWith("RefDesPrefix")) {
            currentLine = currentLine.replaceAll(" ", "");
            currentLine = currentLine.split("\"")[1];
            String refDesAttr = "refdes=" + currentLine + "?";
            symAttributes = symAttributes
                  + SymbolText.BXLAttributeString(0,0, refDesAttr);
          } else if (currentLine.startsWith("PatternName")) {
            currentLine = currentLine.replaceAll(" ", "");
            currentLine = currentLine.split("\"")[1];
            String FPAttr = "footprint=" + currentLine;
            symAttributes = symAttributes
                  + SymbolText.BXLAttributeString(0,0, FPAttr);
          } else if (currentLine.startsWith("AlternatePattern")) {
            currentLine = currentLine.replaceAll(" ", "");
            currentLine = currentLine.split("\"")[1];
            String AltFPAttr = "alt-footprint=" + currentLine;
            symAttributes = symAttributes
                  + SymbolText.BXLAttributeString(0,0, AltFPAttr);
          } else if (currentLine.startsWith("CompPin ")) {
            pins.setBXLPinType(currentLine);
          }
        }
        try {
          File newSym = new File(symbolName + ".sym");
          PrintWriter symOutput = new PrintWriter(newSym);
          symOutput.println(newSymbol   // we now add pins to the
                            + pins.toString(0,0) // the header, and then
                            + symAttributes); // the final attributes
          symOutput.close();
          System.out.println(symbolName + ".sym");
        } catch(Exception e) {
          System.out.println("There was an error saving: "
                             + symbolName + ".sym"); 
          System.out.println(e);
        }
        symAttributes = "";
      }
    }
  }      

  public static void printHelp() {
    System.out.println("usage:\n\n\tjava BXLDecoder BXLFILE.bxl\n\n"
                       + "options:\n\n"
                       + "\t\t-t\tonly output converted text"
                       + " without further conversion\n\n"
                       + "example:\n\n"
                       + "\tjava BXLDecoder BXLFILE.bxl"
                       + " -t > BXLFILE.txt\n");
  }
  
}

