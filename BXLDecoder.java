// BXLDecoder.java - a utility for converting Huffman encoded files
// into text, ported to Java from the vala code by Geert Jordaens
//
// BXLDecoder.java v1.1
// Copyright (C) 2016, 2017 Erich S. Heinzle, a1039181@gmail.com
//
// v1.1 includes KiCad .lib export for symbols

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
import java.lang.Math;

public class BXLDecoder {

  public static void main (String [] args) {

    boolean textOutputOnly = false;
    String filename = "";

    if (args.length == 0) {
      printHelp();
      System.exit(0);
    } else {
      filename = args[0];
      System.out.println("About to use filename: " + filename);
    }
    
    if (args.length == 2) {
      if (args[1].equals("-t")) {
        textOutputOnly = true;
        System.out.println("Planning to do text only on: " + filename);
      }
    }

    SourceBuffer buffer = new SourceBuffer(filename); 

      System.out.println("Created new buffer: " + filename);

    if (textOutputOnly) {
      System.out.println("About to do text only conversion on: " + filename);
      System.out.println(buffer.decode());
      System.exit(0);
    }

    Scanner textBXL = new Scanner(buffer.decode());

    //parseBXL(decodedBXL);
    
    //}

  //private static void parseBXL(Scanner textBXL) {
    String currentLine = "";
    String newElement = "";

    String SymbolName[] = new String[20];
    String SymbolDefs[] = new String[20];

    // some variables for kicad eeschema symbol export
    String kicadFPList = "";
    String kicadDrawn[] = new String[20];
    int dfIndex = 0;
    kicadDrawn[dfIndex] = "";

    String newSymbol = "";
    String symAttributes = "";
    PadStackList padStacks = new PadStackList();
    PinList pins = new PinList(20); // slots = 20

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
	dfIndex++;

        SymbolName[dfIndex] = tokens[1].replaceAll("[\"]","");

        // Construct string for storing kicad drawn items
        kicadDrawn[dfIndex] = "";

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
            latestPin.setSlot(dfIndex);
            pins.addPin(latestPin);
          } else if (currentLine.startsWith("Line")) {
            SymbolPolyline symbolLine = new SymbolPolyline();
            symbolLine.populateBXLElement(currentLine);
            symbolLine.setSlot(dfIndex);
            SymbolDefs[dfIndex] = SymbolDefs[dfIndex]
                + "\n" + symbolLine.toString(0,0);
            kicadDrawn[dfIndex] = kicadDrawn[dfIndex]
                + "\n" + symbolLine.toKicad(0,0);
          } else if (currentLine.startsWith("Arc (Layer TOP_SILKSCREEN)")) {
            Arc silkArc = new Arc();
            silkArc.populateBXLElement(currentLine);
            SymbolDefs[dfIndex] = SymbolDefs[dfIndex]
                + silkArc.generateGEDAelement(xOffset,yOffset,1.0f);
            //kicadDrawn = kicadDrawn    // skip arcs for now. might be broken for gschem .sym
            //    + silkArc.toKicad(xOffset,yOffset,1.0f);
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
        SymbolDefs[dfIndex] = "v 20110115 1"
            + SymbolDefs[dfIndex]; // we have created the header for the symbol
        newElement = ""; // probably not needed
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
            kicadFPList = kicadFPList + "\n " + currentLine;
          } else if (currentLine.startsWith("AlternatePattern")) {
            currentLine = currentLine.replaceAll(" ", "");
            currentLine = currentLine.split("\"")[1];
            String AltFPAttr = "alt-footprint=" + currentLine;
            symAttributes = symAttributes
                  + SymbolText.BXLAttributeString(0,0, AltFPAttr);
            kicadFPList = kicadFPList + "\n " + currentLine;
          } else if (currentLine.startsWith("CompPin ")) {
            pins.setBXLPinType(currentLine);
          }
        }

        try {
          int usedSlots = Math.max(pins.usedSlots(), 2);
          // Skip slot=0 as it contains items common to all other slots
          for (int i=1; i < usedSlots; i++) {
              File newSym = new File(SymbolName[i] + ".sym");
              PrintWriter symOutput = new PrintWriter(newSym);
              symOutput.println(SymbolDefs[i] // we now add pins to the
                            + pins.toString(i,0,0) // the header, and then
                            + symAttributes); // the final attributes
              symOutput.close();
              System.out.println(SymbolName[i] + ".sym");
          }
        } catch(Exception e) {
          System.out.println("There was an error saving: "
                             + symbolName + ".sym");
          System.out.println(e);
        }
        symAttributes = "";

        try {
          String kicad = "";
          // First SymbolName should be without slot index, so it's best to
          // use it for the kiCAD format which doesn't have separate symbols
          // for each slot/unit; all units are within one DRAW section.
          String kicadSymName = "";
          // Count only used slots
          int usedSlots = pins.usedSlots();
          String unitsLocked = (usedSlots > 2) ? "L" : "F";
          for (int i=0; i < usedSlots; i++) {
              if ((SymbolName[i] == null) || SymbolName[i].isEmpty())
                  continue;
              if (kicadSymName.isEmpty() || kicadSymName.compareTo(SymbolName[i]) > 0)
                  kicadSymName = SymbolName[i];
          }
          kicad += "EESchema-LIBRARY Version 2.3"
                 // start with a comment
                 + "\n#\n# converted by BXL2text https://github.com/erichVK5/BXL2text"
                 + "\n# " + kicadSymName + "\n#\n";
                 // build a kicad symbol header DEF section
                 // DEF name reference unused text_offset draw_pinnumber draw_pinname unit_count units_locked option_flag
          kicad += "DEF " + kicadSymName + " U 0 40 Y Y " + (usedSlots - 1) + " " + unitsLocked + " N\n"
                 + "F0 \"U\" 0 -150 50 H V C CNN\n"
                 + "F1 \"" + kicadSymName + "\" 0 150 50 H V C CNN";
          kicad += "\n$FPLIST" + kicadFPList + "\n$ENDFPLIST" // list of matching footprints
                 + "\nDRAW";
          for (int i=0; i < usedSlots; i++) {
              kicad += kicadDrawn[i];  // drawn elements here
              kicad += pins.toKicad(i,0,0); // we now add pins
          }
          kicad += "\nENDDRAW\nENDDEF";

          File newKicad = new File(symbolName + ".lib");
          PrintWriter symOutput = new PrintWriter(newKicad);
          symOutput.println(kicad);
          symOutput.close();
          System.out.println(symbolName + ".lib");

        } catch(Exception e) {
          System.out.println("There was an error saving: "
                             + symbolName + ".lib");
          System.out.println(e);
        }
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

