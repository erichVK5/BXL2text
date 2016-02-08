# BXL2text
A utility to convert huffman encoded BXL schematic and footprint files to plain text, and undertake further conversion to FOSS EDA compatible formats.

BXL files are a vendor neutral and EDA package neutral file format used by device vendors to distribute schematic symbols and footprints for use in EDA tools such as, but not limited to, gEDA, for PCB and circuit design.

The Huffman decoding portion of this utility is based on vala code originally developed by Geert Jordaens, and uses adaptive Huffman decoding.

Conversion of BXL encoded SMD and through hole footprints into a single file of gEDA PCB footprints has now been achieved, utilising the BXL encoded padstack definitions.

Top layer silkscreen lines and arcs are converted as well.

The issue of BXL files using an inverted y-axis relative to gEDA / kicad has been addressed.

Working gschem symbols are now exported, appended to the file of extracted footprints. Attribute fields defined in the BXL file are also appended to the symbol.

Compatible footprints are listed in the decoded symbol's "footprint=..." attribute field, and additional compatible footprints are listed in "alt-footprint=..." attribute fields.

The utility can export either Huffman decoded data as plain text, or perform further conversion.

To do:

- saving of individual, converted footprints and symbols (until then, use a text editor to split them)
- combining this code with KicadModuleToGEDA and KicadSymbolToGEDA

Licence: GPL2

For example vendor BXL files, see:

- http://webench.ti.com/cad/cad.cgi
- http://www.silabs.com/support/Pages/CAD-CAE-Schematic-Footprints-and-Symbols.aspx
- http://www.microchip.com/pagehandler/en-us/devtools/cad-cae-symbols.html

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

	java BXLDecoder BXLFILE.bxl > concatenatedPCBFootprintsAndSymbols.symAndFPs

then use a text editor to extract your footprints and symbol... but only until I implement discrete file saving.... then it will be "automagic"

