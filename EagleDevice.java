// translate2geda - a utility for turning all the things to gEDA elements
// EagleDevice.java v1.0
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
//    translate2geda Copyright (C) 2016 Erich S. Heinzle a1039181@gmail.com

import java.util.ArrayList;

public class EagleDevice {

  private ArrayList<String[]> connections = new ArrayList<String[]>();
  private String FPName = "unknown"; // default

  private ArrayList<String> applicableSymbols = new ArrayList<String>();

  private String[] gateNameList;

  public EagleDevice(ArrayList<String> deviceDesc,
                     ArrayList<String> gateNames) {

    for (String line : deviceDesc) {
      //System.out.println("EagleDevice now parsing: " + line);
      String[] tokens = line.split(" ");
      if (tokens[0].startsWith("<device")) {
        for (int index = 0; index < tokens.length; index++) {
          if (tokens[index].startsWith("package=")) {
            FPName
                = tokens[index].substring(9).replaceAll("[\"\\/>]","");
          }
        }
      } else if (tokens[0].startsWith("<connect")) {
        String gate = "";
        String pin = "";
        String pad = "";
        for (int index = 0; index < tokens.length; index++) {
          if (tokens[index].startsWith("gate=")) {
            gate
                = tokens[index].substring(5).replaceAll("[\"/>]","");
          } else if (tokens[index].startsWith("pin=")) {
            pin
                = tokens[index].substring(4).replaceAll("[\"/>]","");
            // NB replace all items must match those used for SymbolPin
          } else if (tokens[index].startsWith("pad=")) {
            pad
                = tokens[index].substring(4).replaceAll("[\"/>]","");
            // NB replace all items must match those used for Pad
          }
        }
        connections.add(new String[] {gate, pin, pad});
      }
    }

    String currentSymbol = "";
    for (String gateDetails : gateNames) {
      //System.out.println("Stepping through gateNames: "
      //                   + gateDetails); 
      if (gateDetails.startsWith("<gate ")) {
        String[] tokens = gateDetails.split(" ");
        for (int index = 0; index < tokens.length; index++) {
          if (tokens[index].startsWith("symbol=")) {
            currentSymbol
                = tokens[index].substring(7).replaceAll("[\"\\/>]","");
            //  System.out.println("New symbol pin mapping for: " 
            //  + currentSymbol + " in EagleDevice");
          }
        }          
      }
      if (applicableSymbols.size() == 0) {
        applicableSymbols.add(currentSymbol);
      } else {
        boolean duplicate = false;
        for (String storedSymName : applicableSymbols) {
          if (storedSymName.equals(currentSymbol)) {
            duplicate = true;
          }
        }
        if (!duplicate) {
          applicableSymbols.add(currentSymbol);
        }
      }
    }

  }

  // this method is used to see if this pin mapping device
  // definition can be applied to a given symbol
  public boolean isForSymbol(String symName) {
    //System.out.println("Provided with: " + symName + " for testing");
    boolean relevant = false;
    for (String storedSymName : applicableSymbols) {
      //System.out.println("comparing to: " + storedSymName);
      if (storedSymName.equals(symName)) {
        relevant = true;
      }
    }
    return relevant;
  }

  // we use this method to interrogate the pin mapping
  // data and return the pad's number foor the footprint
  public String supplyPadNum(String symPinLabel) {
    String returnVal = "0"; // default
    //System.out.println("Pin being looked for in pin mapping is: "
    //                   + symPinLabel);
    for (String[] connection : connections) {
      //System.out.println("... now comparing to: " + connection[1]);
      if (connection[1].equals(symPinLabel)) {
        returnVal = connection[2];
      }
    }
    return returnVal;
  }

  // we use this method to interrogate the pin mapping
  // data and return the pad's gate details
  public int supplyGateNum(String symPinLabel) {
    String eagleGate = "";
    for (String[] connection : connections) {
      if (connection[1].equals(symPinLabel)) {
        eagleGate = connection[0];
      }
    }
    // this is a simple hack that assigns gate numbers based
    // on where in the list of gates the the gate def falls
    int returnVal = 0; // default
    for (int index = 0; index < gateNameList.length; index++) {
      if (gateNameList[index].equals(eagleGate)) {
        returnVal = index;
      }
    }
    return returnVal;
  }

  public String supplyFPName() {
    return FPName; // default is "unknown"
  }

  public String supplyGEDAFPField() {
    return ("footprint=" + FPName); // default is "unknown"
  }


}
