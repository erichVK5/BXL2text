// SourceBuffer.cc - C++ code for converting Huffman encoded files
// into text, ported to C++ from the vala code by Geert Jordaens
//
// NodeTree - a tree used for the Huffman decoding
// Node - node in the tree used for the Huffman decoding
// SourceBuffer.cc v1.0

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
//    SourceBuffer.cc Copyright (C) 2016 Erich S. Heinzle a1039181@gmail.com


#include <iostream>
#include <stdio.h>
#include <string>

using namespace std;

class Node {

 public:
  int      level = 0; 
  Node     *parent = 0;
  Node     *left = 0;
  Node     *right = 0;
  int      symbol = -1;
  int      weight = 0;

  ~Node () {
  }

    void deleteChildren(Node *target) {
        if (target->left != 0) {
            target->deleteChildren(target->left);
        }
        if (target->right != 0) {
            target->deleteChildren(target->right);
        }
        delete target;
    }

  
  Node () {
    //this.level = 0;
  }
  
  Node (Node *parent, int symbol) {
    if (parent != 0) {
      this->parent = parent;
      this->level  = parent->level+1;
    } else {
      this->level = 0;
    }
    if (level > 7) {
      this->symbol = symbol;
    }
  }

  Node* add_child(int symbol){
    Node *ret = 0;
    if (level < 7) {
      if (right != 0) {
        ret = right->add_child(symbol);
        if (ret != 0) return ret;
      }
      if (left != 0) {
        ret = left->add_child(symbol);
        if (ret != 0) return ret;
      }
      if (right == 0) {  // first fill right branch
        right = new Node (this, -1);
        return right;
      }
      if (left == 0) {
        left = new Node (this, -1);
        if (left !=0) return left;
      }
      return 0;
    } else {
      if (right == 0) {
        right = new Node (this, symbol);
        return right;
      } else if (left == 0) {
        left = new Node (this, symbol);
        return left;
      } else {
        return 0; // Leaves are filled
      }
    }
  }

  bool is_leaf() {
    return (level > 7);
  }

  bool is_root() {
    return (level == 0);
  }

  Node* sibling(Node *node){
    if  (node != right) {
      return right;
    } else  {
      return left;
    }
  }

  void incrementWeight() {
    this->weight++;
  }

  bool need_swapping() {
    if (parent != 0 &&
        parent->parent != 0 && // root node
        this->weight > parent->weight) {
      return true;
    }
    return false;
  }
};



class NodeTree {

 public:
      Node *root = 0;
      Node *nodeList[256];

  ~NodeTree() {
      if (root != 0) {
          root->deleteChildren(root->left);
          root->deleteChildren(root->right);
          delete root;
          root = 0;
      }
  }

  NodeTree() {
    // create root node
    Node *node = new Node(0, 0);
    root = node;
    int leaf_count = 0;
    // fill levels
    while(node != 0) {
      node = root->add_child(leaf_count);
      if(node != 0 && node->is_leaf())
        {
		nodeList[leaf_count] = node;
		// printf("leafCount: %d\n", leaf_count);
		leaf_count++; 
	}
    }
  }

  Node* getRoot() {
    return root;
  }

  Node* getSymbolNode(int symbol) {
    return nodeList[symbol];
  }

  void cleanUp() {
      if (root != 0) {
          root->deleteChildren(root->left);
          root->deleteChildren(root->right);
          delete root;
          root = 0;
      }
  }

  void swap (Node *n1, Node *n2, Node *n3) {
      if (n3 != 0)     {   n3->parent   = n1;}
      if (n1->right == n2) {   n1->right    = n3; return; }
      if (n1->left == n2)  {   n1->left     = n3; return; }
  }

  void update_tree(Node *current) {
    if (current != 0 && current->need_swapping()) {
      Node *parent = current->parent;
      Node *grand_parent = parent->parent;
      Node *parent_sibling = grand_parent->sibling(parent);
      swap(grand_parent, parent,  current);
      swap(grand_parent, parent_sibling, parent);
      swap(parent, current, parent_sibling);
      parent->weight = parent->right->weight + parent->left->weight;
      grand_parent->weight = current->weight + parent->weight;
      update_tree(parent);
      update_tree(grand_parent);
      update_tree(current);
    }
  }
    
};

typedef unsigned char BYTE;

// Get the size of a file
long getFileSize(FILE *file) {
    long lCurPos, lEndPos;
    lCurPos = ftell(file);
    fseek(file, 0, 2);
    lEndPos = ftell(file);
    fseek(file, lCurPos, 0);
    return lEndPos;
}

class SourceBuffer {

private:

    BYTE *source_buffer = 0;
    int source_index = 4;
    int plain_index = 0;
    int source_char = 0;
    long fileSize = 0;
    int bit = 0;

public:

    ~SourceBuffer() {
        if (source_buffer != 0) {
            delete [] source_buffer;
            source_buffer = 0;
        }

    }

    SourceBuffer(char *filename) {
        FILE *file = 0;      // File pointer
        // Open the file in binary mode using the "rb" format string
        // checks if file exists and/or can be opened correctly
        if ((file = fopen(filename, "rb")) == 0)
            cout << "Couldn't open file" << filename << endl;
        else
            cout << filename << " opened successfully" << endl;
        // Get size of file in bytes
        fileSize = getFileSize(file);
        // Allocate space in the buffer for the whole file
        source_buffer = new BYTE[fileSize];
        // Read the file in to the buffer
        fread(source_buffer, fileSize, 1, file);
        fclose(file);
    }

    int read_next_bit() {
        int result = 0;
        if (bit < 0) {
            // Fetch next byte from source_buffer
            bit = 7;
            source_char = (int)source_buffer[source_index];
            result = source_char & (1 << bit);
            source_index++;
        } else {
            result = source_char & (1 << bit);
        }
        bit--;
        return result;
    }

    int read_next_char() {
        int result = (int)source_buffer[plain_index];
        plain_index++;
        return result;
    }

    int uncompressed_size() {
        /* Uncompressed size =
           B0b7 * 1<<0 + B0b6 * 1<<1 + ... + B0b0 * 1<<7 +
           B1b7 * 1<<0 + B1b6 * 1<<1 + ... + B2b0 * 1<<7 +
           B2b7 * 1<<0 + B2b6 * 1<<1 + ... + B3b0 * 1<<7 +
           B3b7 * 1<<0 + B3b6 * 1<<1 + ... + B4b0 * 1<<7
        */
        int size = 0;
        int mask = 0;
        for (int i = 7 ; i >=0 ; i--) {
            if ((source_buffer[0] & (1 << i)) != 0) {
                size |= (1 << mask);
            }
            mask++;
        }
        for (int i = 7 ; i >=0 ; i--) {
            if ((source_buffer[1] & (1 << i)) != 0) {
                size |= (1<<mask);
            }
            mask++;
        }
        for (int i = 7 ; i >=0 ; i--) {
            if ((source_buffer[2] & (1 << i)) != 0) {
                size |= (1<<mask);
            }
            mask++;
        }
        for (int i = 7 ; i >=0 ; i--) {
            if ((source_buffer[3] & (1 << i)) != 0) {
                size |= (1<<mask);
            }
            mask++;
        }
        return size;
    }

  string decode() {
      
      NodeTree *tree = new NodeTree();
      
      int out_file_length = uncompressed_size();
      string sb = "";
      while (source_index < fileSize) {
          Node *node = tree->getRoot();
          while (!node->is_leaf()) {
              // find leaf node
              if (read_next_bit() != 0) {
                  node = node->left;
              } else {
                  node = node->right;
              }
          }
          sb = sb + (char)(node->symbol);
          node->incrementWeight();
          tree->update_tree(node);
      }
      tree->cleanUp();
      delete tree;
      tree = 0;
      return sb;
  }

  string encode() {

      NodeTree *tree = new NodeTree();

      long out_file_length_in_bits = 0;
      string sb = "";
      while (plain_index < fileSize) {
	  int symbol = read_next_char();
	  // printf("About to process : %d\n", symbol);
	  int depth = 0;
	  int encoded[257]; // we need to account for very asymmetric tree topologies
          Node *node = tree->getSymbolNode(symbol);
	  // printf("Found node corresponding to symbol : %d\n", symbol);
	  node->incrementWeight();
	  // printf("Incremented weight for symbol : %d\n", symbol);
          while (!node->is_root()) {
              // traverse tree up towards root node
              if (node == node->parent->left) {
		  encoded[256-depth] = 1; // left of parent
		  //printf("node associated with symbol is left of parent\n");
              } else {
                  encoded[256-depth] = 0; // right of parent
		  //printf("node associated with symbol is right of parent\n");
              }
	      depth++;
	      // printf("Now at depth : %d\n", depth);
	      node = node->parent;
	      out_file_length_in_bits++;
          }
	  // printf("Found node to be at depth : %d\n", depth);
	  for (; depth > 0; depth--) {
		  if (encoded[257-depth]) {
			sb = sb + "1";
			// printf("1"); // can stream this
		  } else {
			sb = sb + "0";
			// printf("0"); // can stream this
		  }  
	  }
	  // printf("file length in bits: %ld\n", out_file_length_in_bits);
	  // printf("about to update tree after processing symbol %c\n", (char)symbol);
          tree->update_tree(tree->getSymbolNode(symbol));
      }
      printf("encoded file length in bits: %ld\n", out_file_length_in_bits);
      tree->cleanUp();
      delete tree;
      tree = 0;
      return sb;
  }

};

int main() {
    string filename = "test.txt";
    //string filename = "LM339_N_14.bxl"; //"MKL27Z256VFM4.bxl";
    char* fn = (char*)filename.c_str();	
    SourceBuffer *sb = new SourceBuffer(fn);
    //cout << sb->decode() << endl;
    cout << sb->encode() << endl;
    delete sb;
    return 0;
}
