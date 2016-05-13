// KicadModuleToGEDA - a utility for turning kicad modules to gEDA PCB footprints
// FootprintHeader.java v1.0
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
* This class is passed Kicad T0 and/or T1 text description strings of the
* form "Tx Xcentre Ycentre Xsize Ysize rotation penWidth unusedFlagN visible layer text"
* and implements a method which can generate a gEDA text definition for a gEDA PCB footprint
*
* @param long xOffset the X offset of the module origin
* @param long yOffset the Y offset of the module origin
*
* @return String = "Element["Sflag" "device description" "name" "val" Mx My Tx Ty TDir Tscale TSflags]"
*
*/

//
// TO DO - consider embedding text on the footprint in a gEDA compatibale way
//         based on a default_font file for any text labels over and above ?T0 ?T1
//

public class FootprintHeader extends FootprintElementArchetype
{

	String output = "";
	String kicadDeviceDescription = "";

        long textxCoordNm = 0;
        long textyCoordNm = 0;
	long gEDAtextxCoord = 0;
        long gEDAtextyCoord = 0;
	long gEDAxOffset = 0;
	long gEDAyOffset = 0;
	long kicadTextxScaleNm = 0; // character 'M' width collected but not used
	long kicadTextyScaleNm = 0; // character 'M' height collected nut not used
	long kicadTextRotation = 0;
				// Kicad specifies this in decidegrees CCW, starting from horizontal 
	long gEDAtextDirection = 0; 
				// 0: default rightwards, 1: 90 degrees CCW, 2: 180 CCW, 3: 270 CCW
				// gEDA simply uses a 0,1,2,3 set of options for the text direction
	long gEDAtextScale = 100;
				// 100 is gEDA's default 100% text size
				// equating to 40 mil high text

	String kicadTextDescriptor = "";

	public FootprintHeader()
	{
		output = "#Hmm, the no arg KicadTextField constructor didn't do much";
	}

	public String toString()
	{
		return kicadTextDescriptor;
	}

	public void populateHeader(String arg, Boolean metric)
	{
		kicadTextDescriptor = arg;

		float parsedValue = 0;
	
		String[] tokens = arg.split(" ");
	
//		System.out.print("#The passed string:" + arg + "\n");
		if (tokens[0].startsWith("T0"))
		{
			parsedValue = Float.parseFloat(tokens[1]);
			textxCoordNm = convertToNanometres(parsedValue, metric);
                        parsedValue = Float.parseFloat(tokens[2]);
                        textyCoordNm = convertToNanometres(parsedValue, metric);
			parsedValue = Float.parseFloat(tokens[3]);
                      	kicadTextxScaleNm = convertToNanometres(parsedValue, metric);
			parsedValue = Float.parseFloat(tokens[4]);
                        kicadTextyScaleNm = convertToNanometres(parsedValue, metric);
                        kicadTextRotation = Integer.parseInt(tokens[5]);
			// we skip Kicad's penWidth, unusedFlagN, visible and layer fields as
			// they aren't useful in gEDA, and jump to the final text string
                        kicadDeviceDescription =
					tokens[tokens.length-1].replaceAll("[^a-zA-Z0-9.-]", "_");
                        if (kicadDeviceDescription.length() >= 2) {
                          if (kicadDeviceDescription.startsWith("_") &&
                              kicadDeviceDescription.startsWith("_")) {
                            kicadDeviceDescription =
                                kicadDeviceDescription.substring(1,kicadDeviceDescription.length()-1);
                          }
                        }
                        // avoid off by one error

		}
		else if (tokens[0].startsWith("fp_text"))
                {
			metric = true;
                        parsedValue = Float.parseFloat(tokens[4]);
                        textxCoordNm = convertToNanometres(parsedValue, metric);
                        parsedValue = Float.parseFloat(tokens[5]);
                        textyCoordNm = convertToNanometres(parsedValue, metric);
//                        parsedValue = Float.parseFloat(tokens[3]);
//                        kicadTextxScaleNm = convertToNanometres(parsedValue, metric);
//                        parsedValue = Float.parseFloat(tokens[4]);
//                        kicadTextyScaleNm = convertToNanometres(parsedValue, metric);
//                        kicadTextRotation = Integer.parseInt(tokens[5]);
                        // we skip Kicad's penWidth, unusedFlagN, visible and layer fields as
                        // they aren't useful in gEDA, and jump to the final text string
                        kicadDeviceDescription = tokens[2].replaceAll("[^a-zA-Z0-9.-]", " "); // avoid off by one error

                }
		else if (tokens[0].startsWith("T1"))
		{
			output = ""; // we aren't very interested in a value for the footprint
		}			// but may change our minds if a text drawing module is done
		else
		{
			output = "Hmm, a Text Descriptor string was not passed to the object";
		}
	}

	public String generateGEDAtextField(long xOffsetNm, long yOffsetNm) // offset in nm
	{
		// we turn Kicad's text direction in decidegrees into one of gEDA's 4 options
		if (kicadTextRotation < 450)
		{
			gEDAtextDirection = 0;
		}
		else if (kicadTextRotation < 1350)
		{
			gEDAtextDirection = 1;
		}
		else if (kicadTextRotation < 2250)
		{
			gEDAtextDirection = 2;
		}
		else if (kicadTextRotation < 3150)
		{
			gEDAtextDirection = 3;
		}
		else
		{
			gEDAtextDirection = 0;
		}

		gEDAxOffset = (xOffsetNm / 254); // divide nm by 254 to get 0.01 mil units
		gEDAyOffset = (yOffsetNm / 254);
					// x and y offsets indicate centre of footprint

		gEDAtextxCoord = (textxCoordNm / 254);
		gEDAtextyCoord = (textyCoordNm / 254);
					// these indicate text position relative to footprint centre

		output = "Element[\"\" \"" +  // The SFlags field remains blank, "[" = 0.01 mil units
		kicadDeviceDescription + "\" " +
		"\"\" " + // name
		"\"\" " + // value
		gEDAxOffset + " " +
		gEDAyOffset + " " +
		gEDAtextxCoord + " " +
		gEDAtextyCoord + " " +
		gEDAtextDirection + " " +
		gEDAtextScale + " " +
		"\"\"]\n(\n";		    // The TFlags field is left empty
		return output;
	}

	private long convertToNanometres(float rawValue, Boolean metricSystem)
	{
		if (metricSystem)
		{
			return (long)(rawValue * 1000000); // convert mm to nm
		}
		else
		{
			return (long)(rawValue * 2540); // convert 0.1 mil kicad units to nm
		}
	}	

}
