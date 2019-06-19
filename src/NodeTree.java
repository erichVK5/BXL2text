// BXLDecoder.java - a utility for converting Huffman encoded files
// into text, ported to Java from the vala code by Geert Jordaens
//
// NodeTree.java - a tree used for the Huffman decoding
//
// BXLDecoder.java v1.0
// NodeTree.java v1.0
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


public class NodeTree {

  public Node root = null;

  public NodeTree() {
    // create root node
    //    Node node = new Node (null, 0);
    Node node = new Node(null, 0);
    root = node;
    // System.out.println("have just initialised root node of tree");
    int leaf_count = 0;
    // fill levels
    while(node != null) {
      node = root.add_child(leaf_count);
      if(node != null && node.is_leaf()) { leaf_count++; }
    }
    //System.out.println("and have finished populating tree with nodes");
    //System.out.println("Final leaf_count is: " + leaf_count);
  }

  public Node getRoot() {
    return root;
  }

  public void swap (Node n1, Node n2, Node n3) {
    if (n3 != null)     {   n3.parent   = n1;}
    if (n1.right == n2) {   n1.right    = n3; return; }
    if (n1.left == n2)  {   n1.left     = n3; return; }
  }

  public void update_tree(Node current) {
    // System.out.println("Updating tree...");
    if (current != null && current.need_swapping()) {
      Node parent = current.parent;
      Node grand_parent = parent.parent;
      Node parent_sibling = grand_parent.sibling(parent);
      swap(grand_parent, parent,  current);
      swap(grand_parent, parent_sibling, parent);
      swap(parent,       current, parent_sibling);
      parent.weight       = parent.right.weight + parent.left.weight;
      grand_parent.weight = current.weight + parent.weight;
      update_tree(parent);
      update_tree(grand_parent);
      update_tree(current);

    }
  }

}
