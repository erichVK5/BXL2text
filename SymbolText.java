// KicadSymbolToGEDA - a utility for turning kicad modules to gEDA PCB footprints
// SymbolText.java v1.0
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
* This class is passed a Kicad Text descriptor string 
* and implements a method which can generate a gschema definition for a gEDA symbol
*
*/

//T orientation posx posy dimension unit convert Text
//With:
//• orientation = horizontal orientation (=0) or vertical (=1).
//• type = always 0.
//• unit = 0 if common to the parts. If not, the number of the part (1. .n).
//• convert = 0 if common to the representations, if not 1 or 2.

public class SymbolText extends SymbolElement
{

  String textDescriptor = "";  
  String output = "";

  // the following three variables are used in an
  // attempt to keep the annotations tidy and properly 
  // justified relative to the existing text fields
  static long maxTextX = 0;
  static long maxTextY = 0;
  static int invisibleAttributeCount = 0;
   
  long xCoord = 0;
  long yCoord = 0;
  long radius = 0;
  int defaultTextSize = 12;
  long textSize = 0;
  String textField = "";

  //  boolean isRefDes = false;
  //  boolean isDeviceDes = false;

  public void SymbolText()
  {
    output = "#Hmm, the no arg symbol circle constructor didn't do much";
  }

  public static void resetSymbolTextAttributeOffsets() {
    resetSymbolTextAttributeOffsets(0,0);
  }


  public static void resetSymbolTextAttributeOffsets(long newX, long newY) {
    // we simply want to start all text to the right of the
    // symbol, and about 100 mil up from the x axis 
    maxTextX = -newX*2 + 100;
    maxTextY = newY + 100;
    invisibleAttributeCount = 0;
  }

  public void populateBXLElement(String BXLLine) {
    // this is a bit convoluted because the attribute
    // fields can contain spaces, and are delimited
    // with double quotes, i.e. '"', so we can't
    // just split the line into tokens around " "
    int indexOne = BXLLine.indexOf("(Attr \"");
    String residue = BXLLine.substring(indexOne);
    int indexTwo = residue.indexOf("\") ");
    residue = residue.substring(7,indexTwo);
    indexOne = residue.indexOf("\" \"");
    String residueOne = residue.substring(0,indexOne);
    String residueTwo = residue.substring(indexOne + 3);
    textField = residueOne + "=" + residueTwo;
    // System.out.println("Final extracted field: " + textField);
    BXLLine = BXLLine.replaceAll("[\"(),]","");
    String [] tokens = BXLLine.split(" ");
    for (int index = 0; index < tokens.length; index++) {
      if (tokens[index].startsWith("Origin ")) {
        tokens[index] = tokens[index].replaceAll("[\"(),]","");
        String [] coords = tokens[index].split(" ");
        xCoord = Integer.parseInt(coords[0]);
        yCoord = Integer.parseInt(coords[1]);
      }
    }
    textSize = defaultTextSize;
  }
  
  public void constructor(String arg)
  {
    arg = arg.replaceAll("^[a-zA-Z0-9]\"-_","_");
    //    System.out.println(arg);
    textDescriptor = arg;
    // herein lies a most peculiar danger, heretofore unimagined, that
    // the kicadians might perchance elect to have text fields with
    // spaces within double quote delimited text fields; oh, the horror 
    //    int firstQuotes = 0;
    //int secondQuotes = 0;
    //firstQuotes = arg.indexOf('"');
    //secondQuotes = arg.indexOf('"',firstQuotes+1);
    // so, we extract the text field, and then...
    //textField = arg.substring(firstQuotes+1,secondQuotes);
    // ...we put an underscore in to avoid falling foul of the .split(" ") method
    //arg = arg.substring(0,firstQuotes) + textField.replaceAll(" ","_") + arg.substring(secondQuotes+1);
    arg = arg.replaceAll("  "," ");
    String[] tokens = arg.split(" ");
    // and any underscores added to the text field remain untouched by .split(" ")
        
    if (tokens[0].startsWith("F0")) {
      if (tokens[1].charAt(0) == '"') {
        tokens[1] = tokens[1].substring(1);
      }
      if (tokens[1].charAt(tokens[1].length()-1) == '"') {
        tokens[1] = tokens[1].substring(0,tokens[1].length()-1);
      }
      textField = "refdes=" + tokens[1] + "?";  // we add newline in toString method 
    } else if (tokens[0].startsWith("F1")) {
      if (tokens[1].equals(textField.replaceAll(" ","_"))) { // we now get rid of the underscore
        tokens[1] = textField; // by substituting the previously stored text field back into tokens[1]
      }
      if (tokens[1].charAt(0) == '"') {
        tokens[1] = tokens[1].substring(1);
      }
      if (tokens[1].charAt(tokens[1].length()-1) == '"') {
        tokens[1] = tokens[1].substring(0,tokens[1].length()-1);
      }
      textField = "device=" + tokens[1]; // we add newline in toString method 
    } else if (tokens[0].startsWith("T")) {
      textField = tokens[8]; 
    }

    xCoord = Integer.parseInt(tokens[2]);
    yCoord = Integer.parseInt(tokens[3]);
    textSize = Integer.parseInt(tokens[4]);

    // we now update superclass min, max dimensions
    super.updateXdimensions(xCoord);
    super.updateYdimensions(yCoord);
    if (maxTextX < xCoord) {
      maxTextX = xCoord;
    }
    if (maxTextY < yCoord) {
      maxTextY = yCoord;
    }
  }

  public long localMinXCoord() {
    return xCoord;
  }

  public long localMinYCoord() {
    return yCoord;
  }

  public String toString(long xOffset, long yOffset, boolean finesse) {
    int colorIndex = 3;
    int textSize = defaultTextSize;
    int fieldVis = 1;
    int attrVis = 0;
    int textAngle = 0;
    int textAlignment = 0; // bottom left alignment/origin
    int numLines = 1;
    String text = textField;
    return toString((xCoord+xOffset), (yCoord+yOffset), colorIndex, textSize, fieldVis, attrVis, textAngle, textAlignment, numLines, text);
  }

  public static String toString(long xOffset, long yOffset, int colorIndex, int textSize, int fieldVis, int attrVis, int textAngle, int textAlignment, int numLines, String text) {
    return ("T "
            + xOffset + " " 
            + yOffset + " " 
            + colorIndex + " "
            + textSize + " "
            + fieldVis + " " // visibility on = 1
            + attrVis + " " // "0 " //attribute visibility off
            + textAngle + " " // not rotated = 0 
            + textAlignment + " " //default value
            + numLines + "\n"
            + text);
  }

  public static String BXLAttributeString(long xOffset, long yOffset, String attribute) {
    String extract = "";
    if (attribute.startsWith("refdes=") ||
        attribute.startsWith("footprint=") ||
        attribute.startsWith("alt-footprint=")) {
      extract = attribute;
    } else {
      int indexOne = attribute.indexOf("(Attr ");
      int indexTwo = attribute.indexOf(")", indexOne);
      attribute = attribute.substring(indexOne + 6, indexTwo);
      indexOne = attribute.indexOf("\" \"");
      extract = attribute.substring(0,indexOne);
      extract = extract + "=" + attribute.substring(indexOne + 3); 
      extract = extract.replaceAll("\"", "");
    }
    return attributeString(xOffset, yOffset, extract);
  }

  public static String attributeString(long xOffset, long yOffset, String attribute) {
    long annotationTextYIncrement = 110;
    maxTextY += annotationTextYIncrement;
    // we add a bit of space above the previous visible text fields
    // before adding the invisible attribute fields to the symbol
    if (invisibleAttributeCount == 0) {
      maxTextY += annotationTextYIncrement;
    }
    invisibleAttributeCount++;
    int colorIndex = 3;
    int textSize = 7;
    int fieldVis = 0;
    int attrVis = 0;
    int textAngle = 0;
    int textAlignment = 0;
    int numLines = 1;
    return ("\nT "
            + (maxTextX + xOffset) + " " 
            + (maxTextY + yOffset) + " " 
            + colorIndex + " "
            + textSize + " "
            + fieldVis + " " // visibility on = 1
            + attrVis + " " // "0 " //attribute visibility off
            + textAngle + " " // not rotated = 0 
            + textAlignment + " " //default value
            + numLines + "\n"
            + attribute );
    // return toString(maxTextX, maxTextY, colorIndex, textSize, fieldVis, attrVis, textAngle, textAlignment, numLines, attribute);
  }
}
