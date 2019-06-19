// FootprintText.java v1.0
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
//    FootprintText Copyright (C) 2015 Erich S. Heinzle a1039181@gmail.com

//
//  A utility for turning text strings into silkscreen line elements which can
//  be added to footprints for labelling purposes.
//  v1.0 of the utility uses the free Hershey Sans 1 Stroke Font and outputs
//  0.01mil (imperial, square bracketed) units. 
//

public class FootprintText extends FootprintElementArchetype {

  //    String output = "";
  String kicadTextDescriptor = "";

    long xCoordNm = 0;
    long yCoordNm = 0;
    long kicadMheightNm = 0;
    long kicadMwidthNm = 0;
    long kicadRotation = 0;
    long defaultGEDAlineThickness = 1000; // this is 10 mil in 0.01 mil units
    long defaultLineThicknessNm = 254000; // which is 254000 nanometres
			// which is 254 microns, which is 0.254 mm
			// which is 0.01 inches, which is 10 mil = 10 thou
    long kicadLineThicknessNm = defaultLineThicknessNm;
    boolean textVisibility = true;
    int kicadBottomSilkLayer = 20;
    int kicadTopSilkLayer = 21;
    int kicadBottomCopperLayer = 0;
    int kicadTopCopperLayer = 15;
    int textLayer = 21;

  String displayedTextField = "DefaultText-SpaceForRent";
  // default text to convert if nothing supplied


  // using chars 32 -> 126 = 95 in total

  double magnificationRatio = 1.0;      // default value of 1.0 yields default sized font text in gEDA PCB

  long yLayoutOffsetNm = 0;    // these are used to position the text relative to the module or layout centroid
  long xLayoutOffsetNm = 0;

  boolean metricFlag = false;  // not really needed for text, if we output silk strokes in decimil format regardless

  long kicadTextHeightNm = 0;
  long kicadTextWidthNm = 1320000;
  //
  // 1786000 -> ?70.37mil -> 67.3 actual
  // default value of 1.829mm for testing = 70.6mil, or 
  // 1327000 = 83.7mil

  public FootprintText(long offsetX, long offsetY) {
    // (x,y) position relative to footprint or layout centroid
    xLayoutOffsetNm = offsetX;
    yLayoutOffsetNm = offsetY;
  }

  public FootprintText()
  {
    ;
  }
  
  public String toString()
  {
    return kicadTextDescriptor;
  }



  public String generateGEDAelement(long offsetX, long offsetY, float magnificationRatio) {

    xLayoutOffsetNm = offsetX + xCoordNm;
    // x position relative to footprint or layout centroid + x position relative to footprint
    yLayoutOffsetNm = offsetY + yCoordNm;
    // y position relative to footprint or layout centroid + y position relative to footprint

    HersheySansFontClass hershey = new HersheySansFontClass();

    return hershey.renderKicadText(displayedTextField, xLayoutOffsetNm, yLayoutOffsetNm, kicadRotation, kicadTextWidthNm, magnificationRatio);
    //    hershey.renderString("abcdeffgghhiijklmmnnopqqrrstuvwxyzz112234567890", 90000, 50000, Math.PI/8, 1.0) +
    //    hershey.renderString("abcdeffgghhiijklmmnnopqqrrstuvwxyzz112234567890", 30000, 40000, 0, 1.0) +
    //    hershey.renderString("abcdeffgghhiijklmmnnopqqrrstuvwxyzz112234567890", 30000, 50000, Math.PI, 1.0) +
    //    hershey.renderString("abcdeffgghhiijklmmnnopqqrrstuvwxyz112234567890", 30000, 40000, 2*Math.PI/5, 1.0) +
    //    hershey.renderString("abcdeffgghhiijklmmnnopqqrrstuvwxyzz112234567890", 30000, 40000, 5*Math.PI/8, 1.0) +
    //    hershey.renderString("abcdeffgghhiijklmmnnopqqrrstuvwxyzz112234567890", 30000, 40000, Math.PI/2, 1.0);
  }


  public void populateElement(String arg, boolean metric)
  {
    kicadTextDescriptor = arg;
    String[] tokens = kicadTextDescriptor.split(" ");
    float parsedValue = 0;
    
    if (tokens[0].startsWith("T"))
      {
        parsedValue = Float.parseFloat(tokens[1]);
        xCoordNm = convertToNanometres(parsedValue, metric); 
        parsedValue = Float.parseFloat(tokens[2]);
        yCoordNm = convertToNanometres(parsedValue, metric);
        parsedValue = Float.parseFloat(tokens[3]);
        kicadMheightNm = convertToNanometres(parsedValue, metric);
        parsedValue = Float.parseFloat(tokens[4]);
        kicadMwidthNm = convertToNanometres(parsedValue, metric);
        kicadRotation = Integer.parseInt(tokens[5]);
        // tenths of degrees Angular
        // rotation from horizontal, counterclockwise
        parsedValue = Float.parseFloat(tokens[6]);
        kicadLineThicknessNm = convertToNanometres(parsedValue, metric);
        if (tokens[8].startsWith("I"))
          {
            textVisibility = false;
          }
        else if (tokens[8].startsWith("V"))
          {
            textVisibility = true;
          }
        textLayer = Integer.parseInt(tokens[9]);
        String rawString = tokens[10];
        // we only want what is inside double quotes
        displayedTextField = rawString.substring(rawString.indexOf('"') + 1, rawString.lastIndexOf('"'));
        
      }
    else if (tokens[0].startsWith("fbtext"))
      { // s-files seem to have limited support for multiple text fields
        for (int counter = 1; counter < tokens.length; counter++)
          {
            if (tokens[counter].startsWith("reference"))
              {
                displayedTextField = tokens[counter + 1];
                counter++;
              }
            else if (tokens[counter].startsWith("value"))
              {
                displayedTextField = tokens[counter + 1];
                counter++;
              }
            else if (tokens[counter].startsWith("at"))
              {
                counter++;
                parsedValue = Float.parseFloat(tokens[counter]);
                xCoordNm = convertToNanometres(parsedValue, metric);
                counter++;
                parsedValue = Float.parseFloat(tokens[counter]);
                yCoordNm = convertToNanometres(parsedValue, metric);                            
              }
            else if (tokens[counter].startsWith("size"))
              {
                counter++;
                parsedValue = Float.parseFloat(tokens[counter]);
                kicadMheightNm = convertToNanometres(parsedValue, metric);
                counter++;
                parsedValue = Float.parseFloat(tokens[counter]);
                kicadMwidthNm = convertToNanometres(parsedValue, metric);                            
              }
            else if (tokens[counter].startsWith("thickness"))
              {
                counter++;
                parsedValue = Float.parseFloat(tokens[counter]);
                kicadLineThicknessNm = convertToNanometres(parsedValue, metric);
              }
            else if (tokens[counter].startsWith("layer"))
              {
                counter++;
                if (tokens[counter].startsWith("F.Silks"))
                  {
                    textLayer = kicadTopSilkLayer;
                  }
                else if (tokens[counter].startsWith("F.Silks"))
                  {
                    textLayer = kicadBottomSilkLayer;
                  }
                // we could add additional layer suport here if keen
              }
            else if (tokens[counter].startsWith("hide"))
              {
                textVisibility = false;
              }
            
          }
        
      }
  }
  

  //  public void populateElement(String moduleDefinition, boolean metric) {
  //  kicadTextDescriptor = moduleDefinition;
        // for testing
  //  workingText = kicadTextDescriptor; //"Sample Text";
  //}

  private long convertToNanometres(float rawValue, boolean metricSystem)
  {
    if (metricSystem)//  metric = the newer legacy format with mm instead of decimil = 0.1 mil units
      {
        return (long)(rawValue * 10000000); 
      }
    else // this implies the input is in Kicad legacy decimil = 0.1mil units
      {
        return (long)(2540 * rawValue); // a 0.1 mil unit = 2540 nm
      }
  }


}
