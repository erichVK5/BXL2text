# BXL2text
A utility to convert huffman encoded BXL schematic and footprint files to plain text, and undertake further conversion to FOSS EDA compatible formats.

The Huffman decoding portion of this utility is based on vala code originally developed by Geert Jordaens.

Conversion of BXL encoded SMD and through hole footprints into a single file of gEDA PCB footprints has now been achieved, utilising the BXL encoded padstack definitions.

Top layer silkscreen lines and arcs are converted as well.

The issue of BXL files using an inverted y-axis relative to gEDA / kicad has been addressed.

The utility can export either Huffman decoded data as plain text, or perform further conversion.

To do:

- saving of individual, converted footprints
- conversion of BXL encoded symbols into gschem compatible symbols
- saving of individual, converted symbols
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

	java BXLDecoder BXLFILE.bxl > newPCBFootprints.fp

