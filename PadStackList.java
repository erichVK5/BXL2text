import java.util.Scanner;

public class PadStackList {

  PadStack [] list = new PadStack [10]; // default size
  int padStackCount = 0;

  public PadStackList() {
  }

  public PadStackList(String padStackDefinition) {
    addPadStacks(padStackDefinition);
  }

  public void addPadStacks(String definition) { // multi stack def
    Scanner multiPadStackDefinition = new Scanner(definition);
    boolean inPadStack = false;
    String currentLine = "";
    String currentPadStack = "";
    while (multiPadStackDefinition.hasNext()) {
      currentLine = multiPadStackDefinition.nextLine().trim();
      if (currentLine.startsWith("PadStack")) {
        inPadStack = true;
        currentPadStack = currentLine;
      } else if (currentLine.startsWith("EndPadStack")) {
        currentPadStack = currentPadStack + "\n" + currentLine;
        addPadStack(currentPadStack);
        inPadStack = false;        
      } else if (inPadStack) {
        currentPadStack = currentPadStack + "\n" + currentLine;
      }
    }
  }
  
  public void addPadStack(String definition) { // single stack def
    list[padStackCount] = new PadStack(definition);
    padStackCount++;
    if (padStackCount == list.length) {
      PadStack [] newList = new PadStack [list.length * 2]; // double it
      for (int index = 0; index < (list.length -1); index++) {
        newList[index] = list[index];
      }
      list = newList;
    }
  } 

  public String GEDAdef(String identifier, long x, long y, int rot) {
    String returnFP = "";
    for (int index = 0; index < padStackCount; index++) {
      if (list[index].is("identifier")) {
        returnFP = returnFP + list[index].fpText(x, y, rot);
      }
    } 
    return returnFP;
  }

  public Pad GEDAPad(String BXLPadDef) { //, long x, long y, int rot) {
    Pad newGEDAPad = null;
    String [] tempPadDef = BXLPadDef.trim().split(" ");
    String padIdentifier = "";
    for (int index = 0; index < tempPadDef.length - 1; index++) {
      if (tempPadDef[index].equals("(PadStyle")) {
        padIdentifier = tempPadDef[index + 1].replaceAll("[\"():,]","");
      }
    }
    int index = 0;
    //    System.out.println("Sought pad identifier in padstack: "
    //                   + padIdentifier);
    while ((newGEDAPad == null) && (index < padStackCount)) {
      if (list[index].is(padIdentifier)) {
        newGEDAPad = list[index].BXLDefToPad(BXLPadDef);
      }
      index++;
    }
    return newGEDAPad;
  }

}
