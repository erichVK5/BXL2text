// KicadModuleToGEDA - a utility for turning kicad modules to gEDA PCB footprints
// BXLDecoder.java
// DrawnElement.java v1.1
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
 * This class is passed a Kicad Draw Segment string of the form "DS x1 y1 x2 y2 thickness layer"
 * and implements a method which can generate a gEDA LineElement definition for a gEDA PCB footprint
 *
 * @param long xOffset the X offset of the Draw Segment relative to the module origin
 * @param long yOffset the Y offset of the Draw Segment realtive to the module origin
 * @param float magnificationRatio the magnification ratio to be applied to element position and size
 *
 * @return String = "LineElement[x1 y1 x2 y2 thickness]"
 *
 */

// have implemented copper pads in gEDA output where a DS statement specifies a copper layer as well
// based on http://kicad.sourcearchive.com/documentation/0.0.20090216/pcbstruct_8h-source.html
// layer "21" is SILKSCREEN_N_CMP
// layer "0" is first copper layer = "0. Back - Solder"
// and layer "15" is "15. Front - Component"
// and layer "20" SilkScreen Back
// and layer "21" SilkScreen Front

/* Layer identification (layer number)
   #define FIRST_COPPER_LAYER    0
   #define COPPER_LAYER_N        0
   #define LAYER_N_2             1     /* Numero layer 2 
   #define LAYER_N_3             2     /* Numero layer 3 
   #define LAYER_N_4             3     /* Numero layer 4 
   #define LAYER_N_5             4     /* Numero layer 5 
   #define LAYER_N_6             5     /* Numero layer 6 
   #define LAYER_N_7             6     /* Numero layer 7 
   #define LAYER_N_8             7     /* Numero layer 8 
   #define LAYER_N_9             8     /* Numero layer 9 
   #define LAYER_N_10            9     /* Numero layer 10
   #define LAYER_N_11            10    /* Numero layer 11
   #define LAYER_N_12            11    /* Numero layer 12
   #define LAYER_N_13            12    /* Numero layer 13
   #define LAYER_N_14            13    /* Numero layer 14
   #define LAYER_N_15            14    /* Numero layer 15
   #define LAYER_CMP_N           15
   #define CMP_N                 15
   #define LAST_COPPER_LAYER     15
   #define NB_COPPER_LAYERS      (LAST_COPPER_LAYER + 1)

   #define FIRST_NO_COPPER_LAYER 16
   #define ADHESIVE_N_CU         16
   #define ADHESIVE_N_CMP        17
   #define SOLDERPASTE_N_CU      18
   #define SOLDERPASTE_N_CMP     19
   #define SILKSCREEN_N_CU       20
   #define SILKSCREEN_N_CMP      21
   #define SOLDERMASK_N_CU       22
   #define SOLDERMASK_N_CMP      23
   #define DRAW_N                24
   #define COMMENT_N             25
   #define ECO1_N                26
   #define ECO2_N                27
   #define EDGE_N                28
   #define LAST_NO_COPPER_LAYER  28
   #define NB_LAYERS             (LAST_NO_COPPER_LAYER + 1)

   #define LAYER_COUNT           32 */ 

// perhaps as a KicadDrawingElementWithPad class or similar??

public class DrawnElement extends FootprintElementArchetype
{

  String output = "";

  long xCoordOneNm = 0;
  long yCoordOneNm = 0;
  long gEDAxCoordOne = 0;
  long gEDAyCoordOne = 0;
  long xCoordTwoNm = 0;
  long yCoordTwoNm = 0;
  long gEDAxCoordTwo = 0;
  long gEDAyCoordTwo = 0;

  long lineThicknessNm = 254000; // this is 10 mil in nanometres
  // which is 254 microns, which is 0.254 mm
  // which is 0.01 inches, which is 10 mil = 10 thou
  long gEDAlineThickness = 100; // this is 10 mil in 0.1 mil units

  int kicadLayer = 21; // 21 is set as the default, and is the top silkscreen layer

  String kicadDrawnSegmentDescriptor = "";

  public void DrawnElement()
  {
    output = "#Hmm, the no arg KicadDrawingElement constructor didn't do much";
  }

  public String toString()
  {
    return kicadDrawnSegmentDescriptor;
  }

  // here, we populate the line object with a string
  // extracted from a BXL file
  // noting that the y-axis is inverted vs gEDA/kicad
  public void populateBXLElement(String BXLLine) {
    BXLLine = BXLLine.replaceAll("[\"(),]","");
    String [] tokens = BXLLine.split(" ");
    for (int index = 0; index < tokens.length; index++) {
      if (tokens[index].equals("TOP_SILKSCREEN")) {
        kicadLayer = 21; // i.e. F.Silk
      } else if (tokens[index].equals("Origin")) {
        xCoordOneNm = milToNM(Float.parseFloat(tokens[++index]));
        yCoordOneNm = -milToNM(Float.parseFloat(tokens[++index]));
      } else if(tokens[index].equals("EndPoint")) {
        xCoordTwoNm = milToNM(Float.parseFloat(tokens[++index]));
        yCoordTwoNm = -milToNM(Float.parseFloat(tokens[++index]));
      } else if(tokens[index].equals("Width")) {
        lineThicknessNm = milToNM(Float.parseFloat(tokens[++index]));
      }
    }
  }

  // here, we populate the line object with a string
  // extracted from a Kicad module    
  public void populateElement(String arg, boolean metric)
  {
    kicadDrawnSegmentDescriptor = arg;

    float parsedValue = 0;
		
    String[] tokens = arg.split(" ");

    //		System.out.print("#The passed string:" + arg + "\n");

    if (tokens[0].startsWith("DS"))
      {
        parsedValue = Float.parseFloat(tokens[1]);
        xCoordOneNm = convertToNanometres(parsedValue, metric);
        parsedValue = Float.parseFloat(tokens[2]);
        yCoordOneNm = convertToNanometres(parsedValue, metric);
        parsedValue = Float.parseFloat(tokens[3]);
        xCoordTwoNm = convertToNanometres(parsedValue, metric);
        parsedValue = Float.parseFloat(tokens[4]);
        yCoordTwoNm = convertToNanometres(parsedValue, metric);
        parsedValue = Float.parseFloat(tokens[5]);
        lineThicknessNm = convertToNanometres(parsedValue, metric);
        kicadLayer = Integer.parseInt(tokens[6]);
        //			System.out.println("Kicad DS Layer is :" + kicadLayer);
      }
    else if (tokens[0].startsWith("fp_line"))
      {
        metric = true;
        parsedValue = Float.parseFloat(tokens[2]);
        xCoordOneNm = convertToNanometres(parsedValue, metric);
        parsedValue = Float.parseFloat(tokens[3]);
        yCoordOneNm = convertToNanometres(parsedValue, metric);
        parsedValue = Float.parseFloat(tokens[5]);
        xCoordTwoNm = convertToNanometres(parsedValue, metric);
        parsedValue = Float.parseFloat(tokens[6]);
        yCoordTwoNm = convertToNanometres(parsedValue, metric);
        parsedValue = Float.parseFloat(tokens[10]);
        lineThicknessNm = convertToNanometres(parsedValue, metric);
        // need to sort out layers though and parse text options though
        if (tokens[8].startsWith("F.Cu")) {
          kicadLayer = 15; // front most copper layer
        } else if (tokens[8].startsWith("B.Cu")) {
          kicadLayer = 0;
        } else if (tokens[8].startsWith("B.Paste")) {
          kicadLayer = 18;
        } else if (tokens[8].startsWith("F.Paste")) {
          kicadLayer = 19;
        } else if (tokens[8].startsWith("B.Silk")) {
          kicadLayer = 20;
        } else if (tokens[8].startsWith("F.Silk")) {
          kicadLayer = 21;
        } else if (tokens[8].startsWith("B.Mask")) {
          kicadLayer = 22;
        } else if (tokens[8].startsWith("F.Mask")) {
          kicadLayer = 23;
        }
        //                        kicadLayer = Integer.parseInt(tokens[8]);
        //                      System.out.println("Kicad DS Layer is :" + kicadLayer);
      }

    else
      {
        System.out.println("Why wasn't the drawn segment passed something useful?");
        output = "Hmm, a Draw Segment string was not passed to the object";
      }
  }

  public String generateGEDAelement(long xOffsetNm, long yOffsetNm, float magnificationRatio) // offsets in nm, and magnificationRatio as float
  {
    // we take care of magnification here
    // this allows a family of footprint silkscreens graphics to
    // be generated fairly easily, for example different sized 7
    // or 17 segment LED displays
    // Device outlines may need to be hand tweaked, as some
    // device families share the same outline, despite different sized
    // features across the family of device, i.e. 0.5 inch and
    // 0.8 inch LED displays have the same outline silkscreen
    // which doesn't need to be magnified

    gEDAxCoordOne = (long)((xCoordOneNm + xOffsetNm)*magnificationRatio/254);
    // divide nm by 254 to produce
    gEDAyCoordOne = (long)((yCoordOneNm + yOffsetNm)*magnificationRatio/254);
    // 0.01 mil units
    gEDAxCoordTwo = (long)((xCoordTwoNm + xOffsetNm)*magnificationRatio/254);
    gEDAyCoordTwo = (long)((yCoordTwoNm + yOffsetNm)*magnificationRatio/254);

    long gEDAdefaultMetalClearance = 20;
    // NB defined here in thousandths of an inch = mils
    // (clearance/2) = minimum distance from pad/pin metal
    // to nearest copper
    // this gets multiplied by 100 for 0.01 mil units in output

    String gEDAflag = "";

    if (kicadLayer == 0)
      {
        gEDAflag = "onsolder";
      }

    //		gEDAxCoordOne = ((xCoordOneNm + xOffsetNm)/254); // divide nm by 254 to produce
    //		gEDAyCoordOne = ((yCoordOneNm + yOffsetNm)/254); // 0.01 mil units
    //              gEDAxCoordTwo = ((xCoordTwoNm + xOffsetNm)/254);
    //              gEDAyCoordTwo = ((yCoordTwoNm + yOffsetNm)/254);

    gEDAlineThickness = (lineThicknessNm / 254); // every 254 nm is 0.01 mil

    if (kicadLayer == 21) // i.e. drawing segment drawn on top silkscreen
      { // currently ignoring bottom silkscreen B.SilkS = 20, and
        // B.Paste = 18, F.Paste = 19, since gEDA uses the clearance value
        // and ignoring the F.Mask = 23 and B.Mask = 22 as well    
        output = "ElementLine[" +
            gEDAxCoordOne + " " +  // we multiply by 10 for gEDAs .01mil units
            gEDAyCoordOne + " " +
            gEDAxCoordTwo + " " +
            gEDAyCoordTwo + " " +
            gEDAlineThickness + "]\n";
      }
    // this catches the drawing segment lines drawn on front or back copper
    else if ((kicadLayer == 0) || (kicadLayer == 15))
      {
        output = "Pad[" +
            gEDAxCoordOne + " " +
            gEDAyCoordOne + " " +
            gEDAxCoordTwo + " " +
            gEDAyCoordTwo + " " +
            gEDAlineThickness + " " +
            (100*gEDAdefaultMetalClearance) + " " +
            "0 " + // let's give the pads zero solder mask relief
            //                                (100*gEDAdefaultSolderMaskRelief + (kicadShapeYsizeNm/254)) + " " +
            '"' + "DrawnElement" + "\" " +
            //                      '"' + kicadShapeNetName + "\" " +
            '"' + "DrawnElement" + "\" " +
            //                      '"' + kicadShapePadName + "\" " +
            '"' +
            gEDAflag +   // the flag is useful,  top vs onsolder placement
            '"' + "]\n";
      }
    return output;
  }


  private long milToNM(float rawValue)
  {
    return (long)(rawValue * 25400 ); // 1 mil unit = 25400 nm
  }


  private long convertToNanometres(float rawValue, Boolean metricSystem)
  {
    if (metricSystem)
      {
        return (long)(rawValue * 1000000); // 1 mm = 1000000 nm
      }
    else
      {
        return (long)(rawValue * 2540 ); // 0.1 mil unit = 2540 nm
      }
  }

}
