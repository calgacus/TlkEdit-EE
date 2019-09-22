package org.jl.nwn.patcher;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jl.nwn.NwnLanguage;
import org.jl.nwn.Version;
import org.jl.nwn.erf.ErfFile;
import org.jl.nwn.gff.GffCExoLocString;
import org.jl.nwn.resource.NwnRepository;
import org.jl.nwn.resource.ResourceID;
import org.jl.nwn.tlk.DefaultTlkReader;
import org.jl.nwn.tlk.TlkContent;
import org.jl.nwn.tlk.TlkLookup;
import org.jl.nwn.tlk.TlkTool;
import org.jl.nwn.twoDa.TwoDaTable;

/* when joining patches :
  - DO NOT update absolute values
  - when computing offset for 2da files DO NOT count absolute lines ( marked with ! )
 */

public class Patcher {

	private static final String includedefFilename = "includedefs.txt";
	private static final String referencesFilename = "references.txt";
	private static final String tlkPatchFilename = "patch.tlk";
	private static final String tlkUpdateFilename = "diff.tlu";
	static final String lineSeparator = System.getProperty("line.separator");

	static class daReference {
		int column;
		String target;
		public daReference(String s, int col) {
			column = col;
			target = s;
		}
		@Override
		public String toString() {
			return "column " + column + " -> " + target;
		}
	}

	// return the output dir thats used when patch in patchDir is built
	public static File getOutputDir( File patchDir ){
		return new File( patchDir, "out" );
	}

	// return the tlk file thats written when patch in patchDir is built
	public static File getOutputTlk( File patchDir ){
		return new File( new File( getOutputDir( patchDir ), "tlk" ), "dialog.tlk" );
	}

	// return the hak file thats written when patch in patchDir is built
	public static File getOutputHak( File patchDir ){
		return new File( new File( getOutputDir( patchDir ), "hak" ), patchDir.getName() + ".hak" );
	}

	private static void compileScript( File script, File nwnhome, File outputdir ) throws IOException{
		String compilerExe = "";
		boolean use_clcompile = false;
		if (System
			.getProperty("os.name")
			.toLowerCase()
			.startsWith("win")){
				File clcompile_exe = new File( new File( nwnhome, "utils" ), "clcompile.exe" );
				compilerExe = clcompile_exe.getAbsolutePath();
				use_clcompile = true;
			}
		else compilerExe = "nwnnsscomp";

		String[] exec = use_clcompile ?
			new String[]{
				compilerExe,
				script.getAbsolutePath(),
				outputdir.getAbsolutePath()
			}
			: new String[]{ // use nwnnsscomp
				compilerExe,
				nwnhome.getAbsolutePath(),
				script.getAbsolutePath()
			};
		System.out.println( "exec : " + exec[0] + " " + exec[1] + " " + exec[2] );

		Process p = Runtime.getRuntime().exec( exec, null, outputdir );

		//p.waitFor();
		InputStream is = p.getInputStream();
		int b = 0;
		while ((b = is.read()) != -1)
			System.out.write(b);
		is = p.getErrorStream();
		while ((b = is.read()) != -1)
			System.out.write(b);
	}

	static void filemove(File src, File target) throws IOException {
		filecopy( src, target );
		src.delete();
	}

	static void filecopy(File src, File target) throws IOException {
		BufferedInputStream in =
			new BufferedInputStream(new FileInputStream(src));
		BufferedOutputStream out =
			new BufferedOutputStream(new FileOutputStream(target));
		byte[] buffer = new byte[32000];
		int p = 0;
		while ((p = in.read(buffer)) != -1)
			out.write(buffer, 0, p);
		in.close();
		out.close();
	}

	private static void delRek(File f) {
		if (f.isDirectory()) {
			File[] files = f.listFiles();
            for (final File file : files) {
                delRek(file);
            }
			f.delete();
		} else if (f.isFile())
			f.delete();
	}

	static void catTextFiles(File[] input, File output, boolean append)
		throws IOException {
		FileWriter out = new FileWriter(output, append);
		//char[] buffer = new char[32000];
		//int p = 0;
		String line = "";
        for (final File file : input) {
            if (file.exists() && file.isFile()) {
                final BufferedReader in = new BufferedReader(new FileReader(file));
                while ((line = in.readLine()) != null) {
                    out.write(line);
                    out.write(lineSeparator);
                }
                in.close();
            }
        }
		out.close();
	}

	static void shiftReference(
		TwoDaTable t,
		int shift,
		int column,
		boolean updateAbsolute) {
		for (int i = 0; i < t.getRowCount(); i++) {
			String value = t.getValueAt(i, column);
			if (!value.startsWith("*")) {
				if (value.startsWith("!")) {
					// absolute value, remove leading '!', don't shift
					if (updateAbsolute)
						t.setValueAt(value.substring(1), i, column);
				} else
					t.setValueAt(
						Integer.toString(Integer.parseInt(value) + shift),
						i,
						column);
			}
		}
	}

	static Map readConstantDefs(File in) throws IOException {
		return readConstantDefs(new BufferedReader(new FileReader(in)));
	}

	static Map readConstantDefs(BufferedReader in) throws IOException {
		Map cdefMap = new TreeMap();
		String line = "";
		while ((line = in.readLine()) != null) {
			if (line.trim().equals(""))
				continue;
			String filename =
				line.substring(0, line.indexOf(':')).trim().toLowerCase();
			//System.out.println( " " + filename + " :" );
			String scriptname =
				line
					.substring(line.indexOf(':') + 1, line.indexOf(','))
					.trim()
					.toLowerCase();
			int column =
				Integer.parseInt(
					line
						.substring(line.indexOf(',') + 1, line.indexOf(';'))
						.trim());
			if (!cdefMap.containsKey(filename)) {
				System.out.println(
					"will write constant definitions for "
						+ filename
						+ " to "
						+ scriptname
						+ ", using column "
						+ column);
				cdefMap.put(filename, new daReference(scriptname, column));
			}
		}
		in.close();
		return cdefMap;
	}

	static Map readReferenceDefs(File in) throws IOException {
		return readReferenceDefs(new BufferedReader(new FileReader(in)));
	}

	static Map readReferenceDefs(BufferedReader in) throws IOException {
		//BufferedReader in = new BufferedReader( new FileReader( new File( patchDir, "references.txt" ) ) );
		Map refMap = new TreeMap();
		String line = "";
		while ((line = in.readLine()) != null) {
			if (line.trim().equals(""))
				continue;
			String filename =
				line.substring(0, line.indexOf(':')).trim().toLowerCase();
			//System.out.println( "references for 2da file " + filename + " :" );
			String[] r = line.substring(line.indexOf(':') + 1).split(";");
			daReference[] refs = new daReference[r.length];
			for (int i = 0; i < refs.length; i++) {
				refs[i] =
					new daReference(
						r[i].substring(r[i].indexOf(',') + 1).toLowerCase().trim(),
						Integer.parseInt(
							r[i].substring(0, r[i].indexOf(',')).trim()));
				//System.out.println( refs[i] );
			}
			if (!refMap.containsKey(filename)) {
				System.out.println("read references for 2da file " + filename);
				refMap.put(filename, refs);
			}
		}
		in.close();
		return refMap;
	}

	/* if updateAbsolute, lines with an absolute reference in the 1st column replace the existing line,
	 * others are appended
	 */
	static void patch2da(
		TwoDaTable main,
		TwoDaTable patch,
		boolean updateAbsolute) {
		if (updateAbsolute)
			for (int i = 0; i < patch.getRowCount(); i++) {
				if (patch.getValueAt(i, 0).startsWith("!")) {
					int line =
						Integer.parseInt(patch.getValueAt(i, 0).substring(1));
					main.setValueAt(
						patch.getValueAt(i, 0).substring(1),
						line,
						0);
					for (int j = 1; j < patch.getColumnCount(); j++)
						main.setValueAt(patch.getValueAt(i, j), line, j);
					patch.removeRow(i);
				}
			}
		main.append(patch, updateAbsolute);
	}

	/*
	 * write integer constant definitions for <table> to file <constScript>, use
	 * <column> for constant names, shift line number by <shift>
	*/
	static void writeScriptConstants(
		TwoDaTable table,
		File constScript,
		int column,
		int shift,
		boolean append)
		throws IOException {
		//System.out.println("writing script constants : " + constScript.getName() );
		FileWriter out = new FileWriter(constScript, append);
		for (int j = 0; j < table.getRowCount(); j++) {
			if (!table.getValueAt(j, 0).startsWith("!"))
				// write no constant definition for replaced lines
				if (!table.getValueAt(j, column).startsWith("*"))
					out.write(
						"const int "
							+ table.getValueAt(j, column)
							+ " = "
							+ (shift + j)
							+ ";"
							+ lineSeparator);
		}
		out.close();
	}


	public static void applyPatch(
			File patchDir,
			NwnRepository sourceRep,
			File nwnDir,
			File tlkSourceFile,
			boolean compile,
			boolean buildHak ) throws IOException {
				 applyPatch( patchDir, sourceRep, nwnDir, tlkSourceFile, compile, buildHak, false );
			}


	public static void applyPatch(
		File patchDir,
		NwnRepository sourceRep,
		File nwnDir,
		File tlkSourceFile,
		boolean compile,
		boolean buildHak, boolean isUserTlk )
		throws IOException {
		File outputDir = new File(patchDir, "out");
		File scriptSrcDir = new File(patchDir, "scripts");
		outputDir.mkdirs();
		File tlkOutDir = new File(outputDir, "tlk");
		File tlkOutputFile = new File(tlkOutDir, "dialog.tlk");
		// remove files in output dir
        for (final File file : outputDir.listFiles()) {
            if (file.isFile()) {
                file.delete();
            }
        }
		// copy files from script dir to output dir
		if (scriptSrcDir.exists()) {
            for (final File file : scriptSrcDir.listFiles()) {
                if (file.isFile()) {
                    filecopy(file, new File(outputDir, file.getName()));
                }
            }
		}

		PrintWriter patchinfo = new PrintWriter( new FileOutputStream( new File( outputDir, "patchinfo.txt")));

		// read reference file ...............
		Map refMap = readReferenceDefs(new File(patchDir, referencesFilename));
		Map defaultRefMap =
			readReferenceDefs(
				new BufferedReader(
					new InputStreamReader(
						ClassLoader.getSystemResourceAsStream(
							"defaultreferences.txt"))));
		// adding reference defaults to patch definitions ( overwriting patch definitions )
		refMap.putAll(defaultRefMap);

		// read constant definition file ...............
		Map cdefMap = readConstantDefs(new File(patchDir, includedefFilename));
		// read all 2da files from patch directory
		System.out.println("reading patch 2da files : ");
		File[] daPatchFiles = patchDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".2da");
			}
		});
		TwoDaTable[] daPatchTables = new TwoDaTable[daPatchFiles.length];
		for (int i = 0; i < daPatchFiles.length; i++) {
			daPatchTables[i] = new TwoDaTable(daPatchFiles[i]);
			System.out.println(daPatchFiles[i].getName().toLowerCase());
		}
		//	note : daPatchFiles[i] <--> daPatchTables[i],
		//  i.e. daPatchFiles[i].getName() is the filename for daPatchTables[i]

		// read the 2da source files
		System.out.println("reading source 2da files : ");
		TwoDaTable[] daSourceTables = new TwoDaTable[daPatchFiles.length];
		for (int i = 0; i < daSourceTables.length; i++) {
			String resName =
				daPatchFiles[i]
					.getName()
					.substring(0, daPatchFiles[i].getName().length() - 4)
					.toLowerCase();
			InputStream is =
				sourceRep.getResource(new ResourceID(resName, "2da"));
			if (is == null) {
				System.out.println(
					"couldn't find source file for " + resName + ".2da");
			} else {
				System.out.println("loading source : " + resName);
				daSourceTables[i] = new TwoDaTable(is);
			}
			/*
			File src = new File( sourceRep, daPatchFiles[i].getName() );
			if ( !src.exists() ){
				System.out.println( "couldn't find source file for " + src.getName() );
			}
			else {
				System.out.println( daPatchFiles[i].getName().toLowerCase() );
				daSourceTables[i] = new TwoDaTable( src );
			}
			*/
		}

		// build map for minimum sizes of 2da files ( filling lines )
		InputStream is =
			ClassLoader.getSystemResourceAsStream("min2dasizes.txt");
		Map minsizes = new TreeMap();
		BufferedReader bin = new BufferedReader(new InputStreamReader(is));
		String aline;
		while ((aline = bin.readLine()) != null) {
			System.out.print(".");
			minsizes.put(
				aline.substring(0, aline.indexOf(' ')).trim().toLowerCase(),
				Integer.parseInt(aline.substring(aline.indexOf(' ') + 1).trim()));
		}
		System.out.println();

		// create the offset map for all the files
		// the offset is simply the number of lines in the source 2da file
		// or the minsize entry for that file
		Map offsetMap = new TreeMap();
		for (int i = 0; i < daSourceTables.length; i++) {
			if (daSourceTables[i] != null) {
				Integer min =
					(Integer) minsizes.get(
						daPatchFiles[i].getName().toLowerCase());
				if (min == null)
					min = 0;
				if (daSourceTables[i].getRowCount() >= min.intValue())
					offsetMap.put(daPatchFiles[i].getName().toLowerCase(), daSourceTables[i].getRowCount());
				else
					offsetMap.put(daPatchFiles[i].getName().toLowerCase(), min);
			} else
				offsetMap.put(daPatchFiles[i].getName().toLowerCase(), 0);
		}

		// load and patch tlk file
		List mainTlk = null;
		File tlkPatchFile = new File(patchDir, tlkPatchFilename);
		//File tlkSourceFile = new File(sourceRep, "dialog.tlk" );
		//File tlkSourceFile = new File(nwnDir, "dialog.tlk");
		int tlkOffset = 0;
		if (tlkPatchFile.exists()) {
			if (!tlkSourceFile.exists()) {
				System.out.println("warning : no dialog.tlk found !");
			} else {
				System.out.println("creating patched tlk file ...");
				tlkOutDir.mkdirs();
				TlkContent src =
                                        new DefaultTlkReader(org.jl.nwn.Version.getDefaultVersion())
                                        .load( tlkSourceFile, null );
				System.out.println("source tlk loaded");
				File tlkDiff = new File( patchDir, tlkUpdateFilename );
				if ( tlkDiff.exists() ){
					if ( !isUserTlk ){
						System.out.println("applying tlk diff ...");
						src.mergeDiff( tlkDiff );
						System.out.println("diff applied");
					}
					else
						System.out.println( "warning : tlk diff exists but cannot be applied because tlk source is a user tlk file" );
				}
				tlkOffset = src.size();
				System.out.println("appending patch.tlk ( at " + tlkOffset + " )");
				TlkContent patch = new DefaultTlkReader(Version.getDefaultVersion()).load( tlkPatchFile, null );
				patchinfo.write( tlkSourceFile.getName() + " " + src.size() + "-" + (src.size()+patch.size()-1) + lineSeparator );
				src.addAll( patch );
				src.saveAs( tlkOutputFile, Version.getDefaultVersion() );
				System.out.println("tlk file saved");
			}
		} else {
			System.out.println("no tlk patch found");
		}
		offsetMap.put("tlk", tlkOffset + (isUserTlk? TlkLookup.USERTLKOFFSET : 0));

		//List includeScripts = new Vector();
		String includefilename = "patchdefinitions";
		//File nwscriptFile = new File( sourceRep, "nwscript.nss" );
		InputStream incIS =
			sourceRep.getResource(new ResourceID(includefilename, "nss"));
		File includeFile = null;
		/*
		if ( nwscriptFile.exists() ){
			includeFile = new File(outputDir, "nwscript.nss");
			filecopy( nwscriptFile, includeFile );
		}
		*/
		if (incIS != null) {
			includeFile = new File(outputDir, includefilename + ".nss");
			FileOutputStream fos = new FileOutputStream(includeFile);
			int b = 0;
			while ((b = incIS.read()) != -1)
				fos.write(b);
		} else {
			includeFile = new File(outputDir, "patchdefinitions.nss");
			includeFile.createNewFile();
		}

		// apply 2da patches, update references and write constant definitions
		for (int i = 0; i < daPatchFiles.length; i++) {
			Integer shift = null;
			daReference[] refs =
				(daReference[]) refMap.get(
					daPatchFiles[i].getName().toLowerCase());
			if (refs == null) {
				System.out.println(
					"no reference definition found for file : "
						+ daPatchFiles[i].getName());
			} else {
				System.out.println(
					"updating 2da references for patch file "
						+ daPatchFiles[i].getName()
						+ " ----------------------------------");
                for (final daReference ref : refs) {
                    shift = (Integer) offsetMap.get(ref.target);
                    if (shift == null) {
                        System.out.println("no patch applied to " + ref.target + ", checking for absolute references");
                        shift = 0;
                    }
                    System.out.println("updating reference from column " + ref.column + " into " + ref.target + " ( shift by " + shift + " )");
                    shiftReference(daPatchTables[i], shift.intValue(), ref.column, true);
                }
			}
			daReference cdef =
				(daReference) cdefMap.get(
					daPatchFiles[i].getName().toLowerCase());
			if (cdef != null) {
				// write constant definitions, not neccessary if joining patches
				//File includeFile = new File(outputDir, cdef.target);
				writeScriptConstants(
					daPatchTables[i],
					includeFile,
					cdef.column,
					((Integer) offsetMap
						.get(daPatchFiles[i].getName().toLowerCase()))
						.intValue(),
					true);
				//includeScripts.add( cdef.target );
			}
			if (daSourceTables[i] == null) {
				daSourceTables[i] = new TwoDaTable(daPatchTables[i]);
				// new empty table
				patch2da(daSourceTables[i], daPatchTables[i], false);
				//patchinfo.write( daPatchFiles[i].getName() + " " + daSourceTables[i].getRowCount() + "-" + (daSourceTables[i].getRowCount()+countNonAbsoluteLines(daPatchTables[i])) + lineSeparator );
				// can't set absolute line position if source unavailable
			} else {
				// fill up to minimum size ...
				Integer minsize =
					(Integer) minsizes.get(
						daPatchFiles[i].getName().toLowerCase());
				if (minsize != null) {
					if (daSourceTables[i].getRowCount() < minsize.intValue()) {
						for (int fill = daSourceTables[i].getRowCount();
							fill < minsize.intValue();
							fill++) {
							daSourceTables[i].appendRow(
								daSourceTables[i].emptyRow());
							daSourceTables[i].setValueAt(
								Integer.toString(fill),
								fill,
								0);
						}
					}
				}
				patchinfo.write( daPatchFiles[i].getName() + " " + (daSourceTables[i].getRowCount()) + "-" + (daSourceTables[i].getRowCount()+countNonAbsoluteLines(daPatchTables[i])-1) + lineSeparator );
				patch2da(daSourceTables[i], daPatchTables[i], true);
			}
			//racialtypes.nss needs some special treatment now ( damn ) ...........
			/* nwn v1.30 doesn't allow replacing of nwscript.nss anymore,
			 * so using RACIAL_TYPE_ALL / INVALID is no longer possible
			 * (cannot assign values in an include file )
			*/
			if (false && cdef != null)
				if (daPatchFiles[i]
					.getName()
					.toLowerCase()
					.equals("racialtypes.2da")) {
					int lastRace = daSourceTables[i].getRowCount();
					String script = "";
					boolean append = true;
					//if ( nwscriptFile.exists() ){
					if (incIS != null) {
						// need to read the script and remove existing RACIAL_TYPE_ALL and
						// RACIAL_TYPE_INVALID definitions
						append = false;
						StringBuffer sb = new StringBuffer();
						String line = "";
						BufferedReader in =
							new BufferedReader(new FileReader(includeFile));
						while ((line = in.readLine()) != null) {
							sb.append(line);
							sb.append(lineSeparator);
						}
						in.close();
						script = sb.toString();
						//System.out.println(script);
						Matcher mat =
							Pattern.compile(
								"int\\s+RACIAL_TYPE_ALL\\s*=").matcher(
								script);
						mat.find();
						script = mat.replaceAll("//" + mat.group());
						mat =
							Pattern.compile(
								"int\\s+RACIAL_TYPE_INVALID\\s*=").matcher(
								script);
						mat.find();
						script = mat.replaceAll("//" + mat.group());
					}
					FileWriter out = new FileWriter(includeFile, append);
					out.write(script);
					out.write(
						"const int RACIAL_TYPE_ALL = "
							+ (lastRace)
							+ ";"
							+ lineSeparator);
					out.write(
						"const int RACIAL_TYPE_INVALID = "
							+ (lastRace + 1)
							+ ";"
							+ lineSeparator);
					out.write(lineSeparator);
					out.close();
					/*
					//FileWriter out = new FileWriter( new File( outputDir, "racialtypes2.nss" ) );
					FileWriter out = new FileWriter( includeFile, true );
					out.write( "int RACIAL_TYPE_ALL = " + (lastRace) + ";" + lineSeparator );
					out.write( "int RACIAL_TYPE_INVALID = " + (lastRace + 1) + ";" + lineSeparator );
					out.close();
					*/
				}
			if (false && cdef != null) //unused !
				if (daPatchFiles[i]
					.getName()
					.toLowerCase()
					.equals("racialtypes.2da")) {
					int lastRace = daSourceTables[i].getRowCount();
					FileWriter out = new FileWriter(includeFile, true);
					out.write(
						"RACIAL_TYPE_ALL = "
							+ (lastRace)
							+ ";"
							+ lineSeparator);
					out.write(
						"RACIAL_TYPE_INVALID = "
							+ (lastRace + 1)
							+ ";"
							+ lineSeparator);
					out.write(lineSeparator);
					out.close();
				}
		}

		// save files .........................................................
		System.out.println(
			"saving 2da files to dir " + outputDir.getAbsolutePath());
		for (int i = 0; i < daPatchFiles.length; i++)
			daSourceTables[i].writeToFile(
				new File(outputDir, daPatchFiles[i].getName()));

		// compile scripts ...............................................
		if (compile) {
			/*
			FileWriter out = new FileWriter( new File( outputDir, "patchdefs.nss"), true ); //append
			for ( int j = 0; j < includeScripts.size(); j++ )
				out.write( "#include \"" + includeScripts.get(j) + "\"" + lineSeparator );
			out.close();
			*/
			try {
				System.out.println("trying to compile scripts ...");
				File[] scripts = outputDir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith(".nss");
					}
				});
                for (final File script : scripts) {
                    if (script.getName().equalsIgnoreCase("nwscript.nss")) {
                        continue;
                    }
                    if (script.getName().equalsIgnoreCase("patchdefinitions.nss")) {
                        continue;
                    }
                    compileScript(script, nwnDir, outputDir);
                }

			} catch (Exception ex) {
				System.out.println(
					"exception during scripts compilation : ");
				ex.printStackTrace();
			}
		}

		// build hak file ...........................................
		if (buildHak) {
			System.out.println("writing hak file");
			File hakDir = new File(outputDir, "hak");
			hakDir.mkdir();
			List files = new Vector();
			ErfFile erf = new ErfFile( new File(hakDir, patchDir.getName() + ".hak"), ErfFile.HAK, new GffCExoLocString("foo") );
            for (final File file : outputDir.listFiles()) {
                if (file.isFile()) {
                    erf.putResource(file);
                }
            }
			erf.write();
			/*
			for (int i = 0; i < f.length; i++)
				if (f[i].isFile())
					files.add(f[i]);
			RevinorHakTest.buildHak(
				new File(hakDir, patchDir.getName() + ".hak"),
				files);
			*/
			/*
			HakpakRep hakRepository = new HakpakRep( new File( hakDir, patchDir.getName()+".hak" ) );
			DirRep dirRepository = new DirRep( outputDir );
			dirRepository.listRealContents();
			hakRepository.putAllResources( dirRepository );
			*/
		}
		patchinfo.flush();
		patchinfo.close();
		System.out.println("done");
	}

	private static int countNonAbsoluteLines( TwoDaTable t ){
		int r = 0;
		for ( int i = 0; i < t.getRowCount(); i++ )
			if ( !t.getValueAt( i,0 ).startsWith("!") )
				r++;
		return r;
	}

	public static void joinPatches( List inputpatches, File outputDir ) throws IOException{
		List files = inputpatches;//(List) optionMap.get("-join");
		//System.out.println( files.size() );
		String[] patchdirnames = new String[files.size()];
		//( String[] )files.toArray( args );
		// reverse order of arguments
		for (int i = 0; i < patchdirnames.length; i++)
			patchdirnames[i] =
				(String) files.get((patchdirnames.length - 1) - i);
		File[] outputdirs = new File[patchdirnames.length - 1];
		for (int i = 0; i < outputdirs.length - 1; i++) {
			outputdirs[i] = File.createTempFile("nwnpatch", null);
			outputdirs[i].delete();
			outputdirs[i].mkdirs();
			outputdirs[i].deleteOnExit();
		}
		outputdirs[outputdirs.length - 1] = outputDir;
		joinPatches(
			new File(patchdirnames[0]),
			new File(patchdirnames[1]),
			outputdirs[0]);
		for (int i = 2; i < patchdirnames.length; i++)
			joinPatches(
				outputdirs[i - 2],
				new File(patchdirnames[i]),
				outputdirs[i - 1]);
		// delete temp dirs
		for (int i = 0; i < outputdirs.length - 1; i++)
			delRek(outputdirs[i]);

	}

	/* append patch in patchDir1 to the patch in patchDir2, write new patch to joinedDir
	 *
	 */
	public static void joinPatches(
		File patchDir1,
		File patchDir2,
		File joinedDir)
		throws IOException {
		if (!joinedDir.exists())
			joinedDir.mkdirs();
		System.out.println(
			"joining patches in "
				+ patchDir1
				+ " and "
				+ patchDir2
				+ ", write to "
				+ joinedDir);

		// remove files in output dir
		/*
		File[] rem = joinedDir.listFiles();
		for (int i = 0; i < rem.length; i++)
			if (rem[i].isFile())
				rem[i].delete();
		*/
		// copy files from script dirs
		System.out.println("copying scripts ...");
		File scriptDir1 = new File(patchDir1, "scripts");
		File scriptDirNew = new File(joinedDir, "scripts");
		if (scriptDir1.exists()) {
			scriptDirNew.mkdirs();
            for (final File file : scriptDir1.listFiles()) {
                if (file.isFile()) {
                    filecopy(file, new File(scriptDirNew, file.getName()));
                }
            }
		}
		File scriptDir2 = new File(patchDir2, "scripts");
		if (scriptDir2.exists()) {
			scriptDirNew.mkdirs();
            for (final File file : scriptDir2.listFiles()) {
                if (file.isFile()) {
                    filecopy(file, new File(scriptDirNew, file.getName()));
                }
            }
		}
		// copy contents of patchDir1 to joinedDir
		System.out.println("copying patch2 2da files ...");
        for (final File file : patchDir2.listFiles()) {
            if (file.isFile()) {
                filecopy(file, new File(joinedDir, file.getName()));
            }
        }
		// read reference file ...............
		Map refMap = readReferenceDefs(new File(patchDir1, referencesFilename));
		Map defaultRefMap =
			readReferenceDefs(
				new BufferedReader(
					new InputStreamReader(
						ClassLoader.getSystemResourceAsStream(
							"defaultreferences.txt"))));
		// adding reference defaults to patch definitions ( overwriting patch definitions )
		refMap.putAll(defaultRefMap);

		System.out.println(
			"reading 2da files from " + patchDir1.getAbsolutePath());
		File[] daPatchFiles = patchDir1.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".2da");
			}
		});
		TwoDaTable[] daPatchTables = new TwoDaTable[daPatchFiles.length];
		for (int i = 0; i < daPatchFiles.length; i++) {
			daPatchTables[i] = new TwoDaTable(daPatchFiles[i]);
			System.out.println(daPatchFiles[i].getName().toLowerCase());
		}
		//	note : daPatchFiles[i] <--> daPatchTables[i],
		//  i.e. daPatchFiles[i].getName() is the filename for daPatchTables[i]

		// read the 2da source files
		System.out.println("reading 'source' 2da files : ");
		TwoDaTable[] daSourceTables = new TwoDaTable[daPatchFiles.length];
		for (int i = 0; i < daSourceTables.length; i++) {
			File src = new File(patchDir2, daPatchFiles[i].getName());
			if (!src.exists()) {
				//System.out.println( "couldn't find source file for " + src.getName() );
			} else {
				System.out.println(daPatchFiles[i].getName().toLowerCase());
				daSourceTables[i] = new TwoDaTable(src);
			}
		}
		// create the offset map for all the files
		// ( the offset is simply the number of lines in the source 2da file )
		Map offsetMap = new TreeMap();
		for (int i = 0; i < daSourceTables.length; i++) {
			if (daSourceTables[i] != null) {
				int realLines = 0;
				// don't count absolute lines
				for (int r = 0; r < daSourceTables[i].getRowCount(); r++)
					if (!daSourceTables[i].getValueAt(r, 0).startsWith("!"))
						realLines++;
				offsetMap.put(daPatchFiles[i].getName().toLowerCase(), realLines);
			} else
				offsetMap.put(daPatchFiles[i].getName().toLowerCase(), 0);
		}

		File tlkPatch1 = new File(patchDir1, tlkPatchFilename);
		File tlkPatch2 = new File(patchDir2, tlkPatchFilename);
		File tlkJoined = new File(joinedDir, tlkPatchFilename);
		if (tlkPatch1.exists() && tlkPatch2.exists()) {
			offsetMap.put("tlk", TlkTool.getTlkFileSize(tlkPatch2));
			System.out.println("concatenating patch tlk files ...");
			TlkTool.concat(tlkPatch2, tlkPatch1, tlkJoined, TlkTool.TLKAPPEND);
		} else {
			if (tlkPatch1.exists())
				filecopy(tlkPatch1, tlkJoined);
			if (tlkPatch2.exists())
				filecopy(tlkPatch2, tlkJoined);
		}

		// same for difs
		File tlkDiff1 = new File(patchDir1, tlkUpdateFilename);
		File tlkDiff2 = new File(patchDir2, tlkUpdateFilename);
		File tlkDiffJoined = new File(joinedDir, tlkUpdateFilename);
		if (tlkDiff1.exists() && tlkDiff2.exists()) {
			System.out.println("merging diff tlk files ...");
			TlkContent c = new TlkContent( NwnLanguage.ENGLISH );
			int[] diffs1 = c.mergeDiff( tlkDiff1 );
			int[] diffs2 = c.mergeDiff( tlkDiff2 );
			TreeSet ts = new TreeSet();
			for (final int diff : diffs1)
				ts.add(diff);
			for (final int diff : diffs2)
				ts.add(diff);
			int[] newDiffs = new int[ ts.size() ];
			int itCount = 0;
			for ( Iterator it = ts.iterator(); it.hasNext(); itCount++ )
				newDiffs[itCount] = ( (Integer) it.next() ).intValue();
			c.writeDiff( tlkDiffJoined, newDiffs );
		} else {
			if (tlkDiff1.exists())
				filecopy(tlkDiff1, tlkDiffJoined);
			if (tlkDiff2.exists())
				filecopy(tlkDiff2, tlkDiffJoined);
		}

		for (int i = 0; i < daPatchFiles.length; i++) {
			Integer shift = null;
			daReference[] refs =
				(daReference[]) refMap.get(
					daPatchFiles[i].getName().toLowerCase());
			if (refs == null){
				System.out.println(
					"no reference definition found for file : "
						+ daPatchFiles[i].getName());
			} else {
				System.out.println(
					"updating 2da references for patch file "
						+ daPatchFiles[i].getName()
						+ " ----------------------------------");
                for (final daReference ref : refs) {
                    shift = (Integer) offsetMap.get(ref.target);
                    if (shift == null) {
                        System.out.println("no patch applied to " + ref.target);
                        shift = 0;
                    }
                    System.out.println("updating reference from column " + ref.column + " into " + ref.target + " ( shift by " + shift + " )");
                    shiftReference(daPatchTables[i], shift.intValue(), ref.column, false);
                }
			}
			if (daSourceTables[i] == null)
				daSourceTables[i] = new TwoDaTable(daPatchTables[i]);
			// new empty table
			patch2da(daSourceTables[i], daPatchTables[i], false);
		}

		System.out.println("writing joined 2da files ...");
		for (int i = 0; i < daPatchFiles.length; i++)
			daSourceTables[i].writeToFile(
				new File(joinedDir, daPatchFiles[i].getName()));

		System.out.println(
			"concatenating references.txt and  includedefs.txt ...");
		File ref1file = new File(patchDir1, referencesFilename);
		File ref2file = new File(patchDir2, referencesFilename);
		catTextFiles(
			new File[] { ref1file, ref2file },
			new File(joinedDir, referencesFilename),
			false);
		File cdef1file = new File(patchDir1, includedefFilename);
		File cdef2file = new File(patchDir2, includedefFilename);
		catTextFiles(
			new File[] { cdef1file, cdef2file },
			new File(joinedDir, includedefFilename),
			false);

		System.out.println("done");
	}

	public static void main(String[] args) throws IOException{
		/*
		PrintStream def = System.out;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setOut( new PrintStream( baos ) );
		*/
		/*
		TreeMap optionMap = new TreeMap();
		List params = null;
		String option = "";
		for (int i = 0; i < args.length; i++) {
			option = args[i];
			if (args[i].startsWith("-")) {
				params = new Vector();
				for (int j = i + 1; j < args.length; j++) {
					if (args[j].startsWith("-"))
						break;
					params.add(args[j]);
				}
				optionMap.put(option, params);
				i += params.size();
			} else
				optionMap.put(option, null);
		}
		Iterator options = optionMap.keySet().iterator();
		while (options.hasNext()) {
			Object o = options.next();
			System.out.print(o + " ");
			List l = (List) optionMap.get(o);
			if (l != null)
				for (int i = 0; i < l.size(); i++)
					System.out.print(l.get(i) + " ");
			System.out.println();
		}
		//if ( true ) return;
		if (optionMap.containsKey("-join")) {
			String outpath = (String) ((List) optionMap.get("-out")).get(0);
			if (outpath == null) {
				System.out.println(
					"must specify output dir when joining patches ( -out <outputdir> )");
				return;//System.exit(0);
			}

			List files = (List) optionMap.get("-join");
			joinPatches( files, new File( outpath ) );
		}
		if (optionMap.containsKey("-apply")) {
			// --------------------------------------------- apply ------------------------------
			String nwnpath = (String) ((List) optionMap.get("-nwn")).get(0);
			if (nwnpath == null) {
				System.out.println(
					"must specify NWN home ( -nwn <path/to/nwn> )");
				return;//System.exit(0);
			}
			File nwn = new File(nwnpath);
			String tlkfilename = (String) ((List) optionMap.get("-tlk")).get(0);
			if (tlkfilename == null) {
				System.out.println(
					"must specify tlk file name ( -tlk <path/to/tlk> )");
				return;//System.exit(0);
			}
			File tlk = new File(tlkfilename);

			File tlkout = null;
			if (optionMap.containsKey("-tlkout")) {
				String tlkoutname =
					(String) ((List) optionMap.get("-tlkout")).get(0);
				if (tlkoutname == null) {
					System.out.println("-tlkout option used without parameter");
					return;//System.exit(0);
				} else
					tlkout = new File(tlkoutname);
			}

			String patchdirname =
				(String) ((List) optionMap.get("-apply")).get(0);
			if (patchdirname == null) {
				System.out.println(
					"must specify patch dir to apply ( -apply <path/to/patch> )");
				return;//System.exit(0);
			}
			File patchdir = new File(patchdirname);

			List repositories = new Vector();
			File hakFile = null;
			if (optionMap.get("-hak") != null) {
				List files = (List) optionMap.get("-hak");
				hakFile = new File( ( String ) files.get(0) );
				for ( int i = 0; i < files.size(); i++ )
					repositories.add(new HakpakRep( new File( (String) files.get(i) ) ));
			}
			if (optionMap.containsKey("-source")) {
				File srcDir =
					new File((String) ((List) optionMap.get("-source")).get(0));
				repositories.add(new DirRep(srcDir));
			}
			if (optionMap.containsKey("-override"))
				repositories.add(new DirRep(new File(nwn, "override")));
			if (optionMap.containsKey("-keyfiles")) {
				List files = (List) optionMap.get("-keyfiles");
				//System.out.println( files.size() );
				String[] keyFiles = new String[files.size()];
				//( String[] )files.toArray( args );
				for (int i = 0; i < keyFiles.length; i++)
					keyFiles[i] = (String) files.get(i);
				repositories.add(new BifRepository(nwn, keyFiles));
			}
			Repository sourceRep = null;
			if (repositories.size() == 0) {
				System.out.println(
					"must specify at least one source ( -hak|-source|-override|-keyfiles )");
				return;//System.exit(0);
			} else if (repositories.size() == 1)
				sourceRep = (Repository) repositories.get(0);
			else {
				sourceRep =
					new NwnChainRepository(
						(NwnRepository) repositories.get(0),
						(NwnRepository) repositories.get(1));
				for (int i = 2; i < repositories.size(); i++)
					sourceRep =
						new NwnChainRepository(
							sourceRep,
							(Repository) repositories.get(i));
			}
			applyPatch(patchdir, sourceRep, nwn, tlk, true, true);
			if (optionMap.containsKey("-repackage")) {
				if (hakFile == null)
					System.out.println("-repackage requires -hak option");
				else {
					File newHak =
						new File(
							new File(new File(patchdir, "out"), "hak"),
							hakFile.getName());
					File patchHak =
						new File(
							new File(new File(patchdir, "out"), "hak"),
							patchdir.getName() + ".hak");
					//HakpakRep newHakRep = new HakpakRep( newHak );
					HakpakRep patchHakRep = new HakpakRep(patchHak);
					Set sourceHakSet =
						(new HakpakRep(hakFile)).listRealContents();
					Set patchSet = patchHakRep.listRealContents();
					patchSet.addAll(sourceHakSet);
					HakpakRep repackagedHak = new HakpakRep(newHak);
					repackagedHak.putAllResources(new SetRepository(patchSet));
				}
			}
			if (tlkout != null) {
				File patchedTlk =
					new File(
						new File(new File(patchdir, "out"), "tlk"),
						"dialog.tlk");
				filecopy(patchedTlk, tlkout);
				patchedTlk.delete();
			}
		}
		*/
		/*

				if (args.length < 4) {
					System.out.println("apply patch");
					File nwndir = new File(args[0]);
					File patchdir = new File(args[1]);
					File sourcedir =
						(args.length == 2) ? new File("source") : new File(args[2]);
					//applyPatch(patchdir, new DirRep(sourcedir), nwndir, true, true);
				} else {
					System.out.println("join patches");
					File nwndir = new File(args[0]);
					File patchdir1 = new File(args[1]);
					File patchdir2 = new File(args[2]);
					File joined = new File(args[3]);
					joinPatches(patchdir1, patchdir2, joined);
				}
		*/
		//def.println( baos.toString() );
	}
}
