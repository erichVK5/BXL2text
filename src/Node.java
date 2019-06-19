// BXLDecoder.java - a utility for converting Huffman encoded files
// into text, ported to Java from the vala code by Geert Jordaens
//
// Node.java - a Node object used for the Huffman decoding tree
//
// BXLDecoder.java v1.0
// Node.java v1.0
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


public class Node {
  public int      level = 0; //      { get; private set; default = 0; }
  public Node     parent = null;//     { get; set; default = null; }
  public Node     left = null;       //{ get; set; default = null; }
  public Node     right = null;       //{ get; set; default = null; }
  public int      symbol = -1;     //{ get; private set; default = -1; }
  public int      weight = 0;     //{ get; set; default = 0; }
  
  
  public Node () {
    //this.level = 0;
  }
  
  public Node (Node parent, int symbol) {
    if (parent != null) {
      this.parent = parent;
      this.level  = parent.level+1;
    } else {
      this.level = 0;
    }
    if (level > 7) {
      this.symbol = symbol;
      //System.out.println("Symbol allocated is: " + symbol);
    }
  }

  public String toString() {
    //    System.out.println("toString called..." +
    //                   symbol + " "
    //                   + Character.toString((char)(symbol & 0xff)));
    return Character.toString((char)(symbol & 0xff));
  }

  public Node add_child(int symbol){
    Node ret = null;
    if (level < 7) {
      if (right != null) {
        ret = right.add_child(symbol);
        if (ret != null) return ret;
      }
      if (left != null) {
        ret = left.add_child(symbol);
        if (ret != null) return ret;
      }
      if (right == null) {  // first fill right branch
        right = new Node (this, -1);
        return right;
      }
      if (left == null) {
        left = new Node (this, -1);
        if (left !=null) return left;
      }
      return null;
    } else {
      if (right == null) {
        right = new Node (this, symbol);
        return right;
      } else if (left == null) {
        left = new Node (this, symbol);
        return left;
      } else {
        return null; // Leaves are filled
      }
    }
  }

  public boolean is_leaf() {
    //System.out.println("Am I a leaf? Checking level:" + level);
    return (level > 7);
  }

  public Node sibling(Node node){
    if  (node != right) {
      return right;
    } else  {
      return left;
    }
  }

  public void incrementWeight() {
    this.weight++;
  }

  public boolean need_swapping() {
    if (parent != null &&
        parent.parent != null && // root node
        this.weight > parent.weight) {
      //System.out.println("Hmm, need to swap during tree update.");
      return true;
    }
    return false;
  }
}
