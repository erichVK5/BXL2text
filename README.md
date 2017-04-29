# BXL2text
A utility to convert huffman encoded BXL schematic and footprint files to plain text, and undertake further conversion to FOSS EDA compatible formats, which for now is gEDA and Kicad -> preliminary Kicad support has been implemented, with automatic export of eeschmea (.lib) symbols along with gschem (.sym) symbols. Kicad natively supports gEDA PCB .fp footprints, so pcbnew footprint export has not been implemented.

BXL files are a vendor neutral and EDA package neutral file format used by device vendors to distribute schematic symbols and footprints for use in EDA tools such as, but not limited to, gEDA, for PCB and circuit design.

The Huffman decoding portion of this utility is based on vala code originally developed by Geert Jordaens, and uses adaptive Huffman decoding.

Conversion of BXL encoded SMD and through hole footprints into a single file of gEDA PCB footprints has now been achieved, utilising the BXL encoded padstack definitions.

Top layer silkscreen lines and arcs are converted as well.

The issue of BXL files using an inverted y-axis relative to gEDA / kicad has been addressed.

Individual gEDA PCB compatible footprints and gEDA gschem compatible symbols are now extracted, and saved individually with .fp and .sym file endings, respectively. Attribute fields defined in the BXL file are also appended to the symbol.

Compatible footprints are listed in the decoded symbol's "footprint=..." attribute field, and additional compatible footprints are listed in "alt-footprint=..." attribute fields.

The utility can export either Huffman decoded data as plain text, or perform further conversion.

The Huffman decoding portion of the utility has been ported to some quick and dirty C++ and can be found in SourceBuffer.cc for anyone with a need to decode adaptively encoded Huffman files using C++.

This code is now included in the translate2geda utility, which can also convert many other formats to gEDA compatible design elements, such as symdef, LT-Spice (.asc), QUCS (.sch), Eagle XML symbols and footprints, BSDL (.bsd), IBIS (.ibs), and Gerber formats.

To do:

- add the kicad symbol format export to the translate2geda utility
- option to translate gschem symbol from 0,0 to bottom left of gschem display area 
- ongoing development work for the component classes is being done mostly in the translate2geda utility, but the improved classes will be put here from time to time

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

	java BXLDecoder BXLFILE.bxl 


