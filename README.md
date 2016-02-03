# BXL2text
A utility to convert huffman encoded BXL schematic and footprint files to plain text.

The Huffman decoding portion of this utility is based on vala code originally by Geert Jordaens.

A first pass of conversion of BXL encoded SMD footprints into a single file of gEDA PCB footprints has now been achieved, utilising the BXL encoded padstack definitions.

The utility can export either Huffman decoded data as plain text, or perform further conversion.

To do:

- saving of individual, converted footprints
- conversion of BXL encoded symbols into gschem compatible symbols
- saving of individual, converted symbols
- combining this code with KicadModuleToGEDA and KicadSymbolToGEDA

Licence: GPL2

Usage:

	1) install a git client, a java compiler and java virtual machine for your operating system

	2) git clone https://github.com/erichVK5/BXL2text.git

	3) cd BXL2text

	4) javac *.java

	5) java BXLDecoder YOURBXLFILEHERE.bxl -t > YOURBXLFILE.txt

Options:

	-t	only output converted text without further conversion

Example usage:

	java BXLDecoder BXLFILE.bxl -t > BXLFILE.txt

	java BXLDecoder BXLFILE.bxl > newPCBFootprints.fp

