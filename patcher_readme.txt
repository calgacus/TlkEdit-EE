(Important : This is only for NWN1 !)

Credits :
	other peoples' work included in this release :
	- patch version of dlpcpakv01 by Garad Moonbeam ( dragonlance races )
2da Patcher Readme ----------------------------------------------------------------------------

Contents :

1. What is this thing anyway ?
2. Included Examples and how to use them
3. How to create a patch
4. Default 2da references

1. What is this thing anyway ?
	2da patcher is designed to help people who create custom spells, races, baseitems
	(or feats when SoU arrives...) etc. to distribute their work as well as merge their
	work with that of others.
	
	Currently new spells etc. are distributed as whole 2da files which replace the existing
	files plus a whole dialog.tlk file.
	
	A 2da patch however is made up of 2da files and a single patch.tlk file
	which only include the new ( added ) lines. these patches can be merged with one another
	and added to the existing nwn files without manually editing ANY 2da or tlk file.
	In order to replace existing tlk entries the patch may contain a diff file ( called diff.tlu ). Using a diff.tlu
	file does not affect the 2da patch in any way ( no changes needed ) - only the files created by
	the patcher might change

	Example :
	You want to add a single new spell, let's say Divine Favour.
	A patch for this spell would include the following files :
	patch.tlk            - containing the spell description and strings used in iprp_spells
	spells.2da           - containing only a single line for the new spell.
	ewld_sp_DivFavor.nss - the spell script ( located in the "scripts" subdir )
	iprp_spells.2da      - containing 1 line for each spell level you want to use on items	
	references.txt       - a file specifying all the references contained in the 2da files
			( the line for iprp_spells2.da is : "iprp_spells.2da : 2,tlk; 6,spells.2da;" which
			means that column 2 references a tlk entry and column 6 references spells.2da )
	includedefs.txt      - a file specifying how to generate the nwscript constant defintions for the new spell
			( "spells.2da       : patchincludes.nss, 1;" )

	
	All references within these files are relative to the other patch files, that means
	column 6 in iprp_spells.2da would contain the value 0 since the referenced spell is
	at line 0 within spells.2da. Likewise, the tlk references start at 0 ( existing (i.e. bioware's) 2da lines
	and tlk entries can be referenced by putting a '!' before the number )
	
	When this patch is applied 2da Patcher will do the following
	( none of your nwn files will be overwritten ):
	- Create a new dialog.tlk file containing all the lines in the old dialog.tlk + patch.tlk
	- append spells.2da to the existing spells.2da file, updating the tlk references in the
		'patch' lines to point to the correct position
	- append iprp_spells.2da to the existing file and update the tlk references as well as
		the reference into spells.2da ( column 6 )
	- append constant definitions for the new spell to a file called "patchdefinitions.nss"
		( lines look like this : const int <spell label> = <spells.2da line number>; )
	- call the script compiler to compile the spell script ( uses Torlack's nwnnsscomp on linux )
	- package all the generated files into a hak file
	and that's it
	
	2da Patcher can also merge multiple patches automatically.
	

2. Included Examples and how to use them :
	included in this release are patch versions of
	- dlpcpakv01 by Garad Moonbeam ( dragonlance races )
	- a patch by me which adds the Dwarven Defender prestige class
		
	building patches :
	( linux note : the patch program will try to call Torlack's script compiler. if 
	you do not have it the patch will build but the included spells
	will be unusable in the game )
	
	use the GUI ( PatcherGUI.bat or 'java -cp tlkedit.jar org.jl.nwn.patcher.PatcherGUI' ) and read "ReadmePatcherGui.html"
	
3. How to create a patch ( to be done ):
	Take a look at the dlpcpackv01 patch, it should give you a good first impression of how it works ;)
	the Dwarven Defender patch uses all features of the patcher and is somewhat more complicated.

	The format for references.txt is
		<2da filename> ':' ( <column number> ',' ( tlk | <2da filename> ) ';' )+

	The format for includedefs.txt is
		<2da filename> ':' <script name> ',' <column number> ';'
		The line
			racialtypes.2da  : racialtypesconst.nss, 20;
		will cause 2da Patcher to generate lines of the form
			'int' <value of column 20> '=' <line number> ';'
		for every line in the racialtypes.2da patch file. The lines will be written to a file
		called "patchdefinitions.nss", the <script name> value is currently ignored
	
4. Default 2da references ( NWN v1.61 )
	The following 2da reference definitions are "built in" and can't be replaced/altered by the user :

baseitems.2da   : 1,tlk; 31,tlk; 37,feat.2da; 38,feat.2da; 39,feat.2da; 40,feat.2da; 41,feat.2da; 45,tlk;
feat.2da        : 2,tlk; 3,tlk; 13,feat.2da; 14,feat.2da; 20,spells.2da; 21,feat.2da; 24,feat.2da; 26,feat.2da; 27,feat.2da; 28,feat.2da; 29,feat.2da; 30,feat.2da; 31,skills.2da; 33,skills.2da;
iprp_feats.2da  : 1,tlk; 4,feat.2da;
iprp_spells.2da : 2,tlk; 6,spells.2da;
racialtypes.2da : 3,tlk; 4,tlk; 5,tlk; 6,tlk; 7,tlk; 8,appearance.2da; 16,classes.2da; 18,tlk; 22,classes.2da;
skills.2da      : 2,tlk; 3,tlk;
spells.2da      : 2,tlk; 47,tlk; 50,tlk; 39,spells.2da; 40,spells.2da; 41,spells.2da; 42,spells.2da; 43,spells.2da; 45,spells.2da; 52,feat.2da;
classes.2da     : 2, tlk ; 3, tlk ; 4, tlk ; 5, tlk;
