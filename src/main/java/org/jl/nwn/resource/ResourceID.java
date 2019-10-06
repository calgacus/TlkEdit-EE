package org.jl.nwn.resource;

import java.io.File;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/*
 * ResourceIDs can have a length of 16 characters in NWN1 and 32 in NWN2,
 * this class doesn't care about the length, except when creating ResourceIDs
 * from file names where the name is truncated to 32 characters.
 */
public class ResourceID implements Comparable {

    private String name;
    private short type;

    public static final Map<String, Short> extension2typeMap = new TreeMap<>();

    public static final Map<Short, String> type2extensionMap = new TreeMap<>();

    public static final Comparator<ResourceID> COMPARATOR = (ResourceID o1, ResourceID o2) -> {
        final int r = o1.getName().compareTo(o2.getName());
        return r == 0 ? o1.getType() - o2.getType() : r;
    };

    public static final Comparator<ResourceID> TYPECOMPARATOR = (ResourceID o1, ResourceID o2) -> {
        final int r = o1.getType() - o2.getType();
        return r == 0 ? o1.getName().compareTo(o2.getName()) : r;
    };

    public static final short TYPE_RES = 0x0;
    public static final short TYPE_BMP = 0x1;
    public static final short TYPE_MVE = 0x2;
    public static final short TYPE_TGA = 0x3;
    public static final short TYPE_WAV = 0x4;
    public static final short TYPE_WFX = 0x5;
    public static final short TYPE_PLT = 0x6;
    public static final short TYPE_INI = 0x7;
    public static final short TYPE_MP3 = 0x8;
    public static final short TYPE_MPG = 0x9;
    public static final short TYPE_TXT = 0xa;
    public static final short TYPE_PLH = 0x7d0;
    public static final short TYPE_TEX = 0x7d1;
    public static final short TYPE_MDL = 0x7d2;
    public static final short TYPE_THG = 0x7d3;
    public static final short TYPE_FNT = 0x7d5;
    public static final short TYPE_LUA = 0x7d7;
    public static final short TYPE_SLT = 0x7d8;
    public static final short TYPE_NSS = 0x7d9;
    public static final short TYPE_NCS = 0x7da;
    public static final short TYPE_MOD = 0x7db;
    public static final short TYPE_ARE = 0x7dc;
    public static final short TYPE_SET = 0x7dd;
    public static final short TYPE_IFO = 0x7de;
    public static final short TYPE_BIC = 0x7df;
    public static final short TYPE_WOK = 0x7e0;
    public static final short TYPE_2DA = 0x7e1;
    public static final short TYPE_TLK = 0x7e2;
    public static final short TYPE_TXI = 0x7e6;
    public static final short TYPE_GIT = 0x7e7;
    public static final short TYPE_BTI = 0x7e8;
    public static final short TYPE_UTI = 0x7e9;
    public static final short TYPE_BTC = 0x7ea;
    public static final short TYPE_UTC = 0x7eb;
    public static final short TYPE_DLG = 0x7ed;
    public static final short TYPE_ITP = 0x7ee;
    public static final short TYPE_BTT = 0x7ef;
    public static final short TYPE_UTT = 0x7f0;
    public static final short TYPE_DDS = 0x7f1;
    public static final short TYPE_BTS = 0x7f2;
    public static final short TYPE_UTS = 0x7f3;
    public static final short TYPE_LTR = 0x7f4;
    public static final short TYPE_GFF = 0x7f5;
    public static final short TYPE_FAC = 0x7f6;
    public static final short TYPE_BTE = 0x7f7;
    public static final short TYPE_UTE = 0x7f8;
    public static final short TYPE_BTD = 0x7f9;
    public static final short TYPE_UTD = 0x7fa;
    public static final short TYPE_BTP = 0x7fb;
    public static final short TYPE_UTP = 0x7fc;
    public static final short TYPE_DFT = 0x7fd;
    public static final short TYPE_GIC = 0x7fe;
    public static final short TYPE_GUI = 0x7ff;
    public static final short TYPE_CSS = 0x800;
    public static final short TYPE_CCS = 0x801;
    public static final short TYPE_BTM = 0x802;
    public static final short TYPE_UTM = 0x803;
    public static final short TYPE_DWK = 0x804;
    public static final short TYPE_PWK = 0x805;
    public static final short TYPE_BTG = 0x806;
    public static final short TYPE_UTG = 0x807;
    public static final short TYPE_JRL = 0x808;
    public static final short TYPE_SAV = 0x809;
    public static final short TYPE_UTW = 0x80a;
    public static final short TYPE_4PC = 0x80b;
    public static final short TYPE_SSF = 0x80c;
    public static final short TYPE_HAK = 0x80d;
    public static final short TYPE_NWM = 0x80e;
    public static final short TYPE_BIK = 0x80f;
    public static final short TYPE_NDB = 0x810;
    public static final short TYPE_PTM = 0x811;
    public static final short TYPE_PTT = 0x812;
    public static final short TYPE_BAK = 0x813;
    public static final short TYPE_OSC = 0xbb8;
    public static final short TYPE_USC = 0xbb9;
    public static final short TYPE_TRN = 0xbba;
    public static final short TYPE_UTR = 0xbbb;
    public static final short TYPE_UEN = 0xbbc;
    public static final short TYPE_ULT = 0xbbd;
    public static final short TYPE_SEF = 0xbbe;
    public static final short TYPE_PFX = 0xbbf;
    public static final short TYPE_CAM = 0xbc0;
    public static final short TYPE_LFX = 0xbc1;
    public static final short TYPE_BFX = 0xbc2;
    public static final short TYPE_UPE = 0xbc3;
    public static final short TYPE_ROS = 0xbc4;
    public static final short TYPE_RST = 0xbc5;
    public static final short TYPE_IFX = 0xbc6;
    public static final short TYPE_PFB = 0xbc7;
    public static final short TYPE_ZIP = 0xbc8;
    public static final short TYPE_WMP = 0xbc9;
    public static final short TYPE_BBX = 0xbca;
    public static final short TYPE_TFX = 0xbcb;
    public static final short TYPE_WLK = 0xbcc;
    public static final short TYPE_XML = 0xbcd;
    public static final short TYPE_SCC = 0xbce;
    public static final short TYPE_PTX = 0xbd9;
    public static final short TYPE_LTX = 0xbda;
    public static final short TYPE_TRX = 0xbdb;
    public static final short TYPE_MDB = 0xfa0;
    public static final short TYPE_MDA = 0xfa1;
    public static final short TYPE_SPT = 0xfa2;
    public static final short TYPE_GR2 = 0xfa3;
    public static final short TYPE_FXA = 0xfa4;
    public static final short TYPE_FXE = 0xfa5;
    public static final short TYPE_JPG = 0xfa7;
    public static final short TYPE_PWC = 0xfa8;
    public static final short TYPE_IDS = 0x270c;
    public static final short TYPE_ERF = 0x270d;
    public static final short TYPE_BIF = 0x270e;
    public static final short TYPE_KEY = 0x270f;

    // witcher, according to http://witcher.wikia.com/wiki/KEY_BIF_V1.1_Format
    public static final short TYPE_NCM = 0x0813;
    public static final short TYPE_MFX = 0x0814;
    public static final short TYPE_MAT = 0x0815;
    public static final short TYPE_MDB_WI = 0x0816;
    public static final short TYPE_SAY = 0x0817;
    public static final short TYPE_TTF = 0x0818; //standard .ttf font files
    public static final short TYPE_TTC = 0x0819;
    public static final short TYPE_CUT = 0x081A; //cutscene? (GFF)
    public static final short TYPE_KA = 0x081B; //karma file (XML)
    public static final short TYPE_JPG_WI = 0x081C;
    public static final short TYPE_ICO = 0x081D; //standard windows .ico files
    public static final short TYPE_OGG = 0x081E; //ogg vorbis sound file
    public static final short TYPE_SPT_WI = 0x081F;
    public static final short TYPE_SPW = 0x0820;
    public static final short TYPE_WFX_WI = 0x0821;
    public static final short TYPE_UGM = 0x0822; // 2082 ??? [textures00.bif]
    public static final short TYPE_QDB = 0x0823; //quest database (GFF v3.38)
    public static final short TYPE_QST = 0x0824; //quest (GFF)
    public static final short TYPE_NPC = 0x0825;
    public static final short TYPE_SPN = 0x0826;
    public static final short TYPE_UTX = 0x0827;
    public static final short TYPE_MMD = 0x0828;
    public static final short TYPE_SMM = 0x0829;
    public static final short TYPE_UTA = 0x082A; //uta (GFF)
    public static final short TYPE_MDE = 0x082B;
    public static final short TYPE_MDV = 0x082C;
    public static final short TYPE_MBA = 0x082E;
    public static final short TYPE_OCT = 0x082F;
    public static final short TYPE_PDB = 0x0831;
    public static final short TYPE_THEWITCHERSAVE = 0x0832;
    public static final short TYPE_PVS = 0x0833;
    public static final short TYPE_CFX = 0x0834;
    public static final short TYPE_LUC = 0x0835; //compiled lua script
    public static final short TYPE_PRB = 0x0837;
    public static final short TYPE_CAM_WI = 0x0838;
    public static final short TYPE_WOB = 0x083B;

    static {
        ResourceID.type2extensionMap.put(TYPE_RES, "res");
        ResourceID.type2extensionMap.put(TYPE_BMP, "bmp");
        ResourceID.type2extensionMap.put(TYPE_MVE, "mve");
        ResourceID.type2extensionMap.put(TYPE_TGA, "tga");
        ResourceID.type2extensionMap.put(TYPE_WAV, "wav");
        ResourceID.type2extensionMap.put(TYPE_WFX, "wfx");
        ResourceID.type2extensionMap.put(TYPE_PLT, "plt");
        ResourceID.type2extensionMap.put(TYPE_INI, "ini");
        ResourceID.type2extensionMap.put(TYPE_MP3, "mp3");
        ResourceID.type2extensionMap.put(TYPE_MPG, "mpg");
        ResourceID.type2extensionMap.put(TYPE_TXT, "txt");
        ResourceID.type2extensionMap.put(TYPE_PLH, "plh");
        ResourceID.type2extensionMap.put(TYPE_TEX, "tex");
        ResourceID.type2extensionMap.put(TYPE_MDL, "mdl");
        ResourceID.type2extensionMap.put(TYPE_THG, "thg");
        ResourceID.type2extensionMap.put(TYPE_FNT, "fnt");
        ResourceID.type2extensionMap.put(TYPE_LUA, "lua");
        ResourceID.type2extensionMap.put(TYPE_SLT, "slt");
        ResourceID.type2extensionMap.put(TYPE_NSS, "nss");
        ResourceID.type2extensionMap.put(TYPE_NCS, "ncs");
        ResourceID.type2extensionMap.put(TYPE_MOD, "mod");
        ResourceID.type2extensionMap.put(TYPE_ARE, "are");
        ResourceID.type2extensionMap.put(TYPE_SET, "set");
        ResourceID.type2extensionMap.put(TYPE_IFO, "ifo");
        ResourceID.type2extensionMap.put(TYPE_BIC, "bic");
        ResourceID.type2extensionMap.put(TYPE_WOK, "wok");
        ResourceID.type2extensionMap.put(TYPE_2DA, "2da");
        ResourceID.type2extensionMap.put(TYPE_TLK, "tlk");
        ResourceID.type2extensionMap.put(TYPE_TXI, "txi");
        ResourceID.type2extensionMap.put(TYPE_GIT, "git");
        ResourceID.type2extensionMap.put(TYPE_BTI, "bti");
        ResourceID.type2extensionMap.put(TYPE_UTI, "uti");
        ResourceID.type2extensionMap.put(TYPE_BTC, "btc");
        ResourceID.type2extensionMap.put(TYPE_UTC, "utc");
        ResourceID.type2extensionMap.put(TYPE_DLG, "dlg");
        ResourceID.type2extensionMap.put(TYPE_ITP, "itp");
        ResourceID.type2extensionMap.put(TYPE_BTT, "btt");
        ResourceID.type2extensionMap.put(TYPE_UTT, "utt");
        ResourceID.type2extensionMap.put(TYPE_DDS, "dds");
        ResourceID.type2extensionMap.put(TYPE_BTS, "bts");
        ResourceID.type2extensionMap.put(TYPE_UTS, "uts");
        ResourceID.type2extensionMap.put(TYPE_LTR, "ltr");
        ResourceID.type2extensionMap.put(TYPE_GFF, "gff");
        ResourceID.type2extensionMap.put(TYPE_FAC, "fac");
        ResourceID.type2extensionMap.put(TYPE_BTE, "bte");
        ResourceID.type2extensionMap.put(TYPE_UTE, "ute");
        ResourceID.type2extensionMap.put(TYPE_BTD, "btd");
        ResourceID.type2extensionMap.put(TYPE_UTD, "utd");
        ResourceID.type2extensionMap.put(TYPE_BTP, "btp");
        ResourceID.type2extensionMap.put(TYPE_UTP, "utp");
        ResourceID.type2extensionMap.put(TYPE_DFT, "dft");
        ResourceID.type2extensionMap.put(TYPE_GIC, "gic");
        ResourceID.type2extensionMap.put(TYPE_GUI, "gui");
        ResourceID.type2extensionMap.put(TYPE_CSS, "css");
        ResourceID.type2extensionMap.put(TYPE_CCS, "ccs");
        ResourceID.type2extensionMap.put(TYPE_BTM, "btm");
        ResourceID.type2extensionMap.put(TYPE_UTM, "utm");
        ResourceID.type2extensionMap.put(TYPE_DWK, "dwk");
        ResourceID.type2extensionMap.put(TYPE_PWK, "pwk");
        ResourceID.type2extensionMap.put(TYPE_BTG, "btg");
        ResourceID.type2extensionMap.put(TYPE_UTG, "utg");
        ResourceID.type2extensionMap.put(TYPE_JRL, "jrl");
        ResourceID.type2extensionMap.put(TYPE_SAV, "sav");
        ResourceID.type2extensionMap.put(TYPE_UTW, "utw");
        ResourceID.type2extensionMap.put(TYPE_4PC, "4pc");
        ResourceID.type2extensionMap.put(TYPE_SSF, "ssf");
        ResourceID.type2extensionMap.put(TYPE_HAK, "hak");
        ResourceID.type2extensionMap.put(TYPE_NWM, "nwm");
        ResourceID.type2extensionMap.put(TYPE_BIK, "bik");
        ResourceID.type2extensionMap.put(TYPE_NDB, "ndb");
        ResourceID.type2extensionMap.put(TYPE_PTM, "ptm");
        ResourceID.type2extensionMap.put(TYPE_PTT, "ptt");
        ResourceID.type2extensionMap.put(TYPE_BAK, "bak");
        ResourceID.type2extensionMap.put(TYPE_OSC, "osc");
        ResourceID.type2extensionMap.put(TYPE_USC, "usc");
        ResourceID.type2extensionMap.put(TYPE_TRN, "trn");
        ResourceID.type2extensionMap.put(TYPE_UTR, "utr");
        ResourceID.type2extensionMap.put(TYPE_UEN, "uen");
        ResourceID.type2extensionMap.put(TYPE_ULT, "ult");
        ResourceID.type2extensionMap.put(TYPE_SEF, "sef");
        ResourceID.type2extensionMap.put(TYPE_PFX, "pfx");
        ResourceID.type2extensionMap.put(TYPE_CAM, "cam");
        ResourceID.type2extensionMap.put(TYPE_LFX, "lfx");
        ResourceID.type2extensionMap.put(TYPE_BFX, "bfx");
        ResourceID.type2extensionMap.put(TYPE_UPE, "upe");
        ResourceID.type2extensionMap.put(TYPE_ROS, "ros");
        ResourceID.type2extensionMap.put(TYPE_RST, "rst");
        ResourceID.type2extensionMap.put(TYPE_IFX, "ifx");
        ResourceID.type2extensionMap.put(TYPE_PFB, "pfb");
        ResourceID.type2extensionMap.put(TYPE_ZIP, "zip");
        ResourceID.type2extensionMap.put(TYPE_WMP, "wmp");
        ResourceID.type2extensionMap.put(TYPE_BBX, "bbx");
        ResourceID.type2extensionMap.put(TYPE_TFX, "tfx");
        ResourceID.type2extensionMap.put(TYPE_WLK, "wlk");
        ResourceID.type2extensionMap.put(TYPE_XML, "xml");
        ResourceID.type2extensionMap.put(TYPE_SCC, "scc");
        ResourceID.type2extensionMap.put(TYPE_PTX, "ptx");
        ResourceID.type2extensionMap.put(TYPE_LTX, "ltx");
        ResourceID.type2extensionMap.put(TYPE_TRX, "trx");
        ResourceID.type2extensionMap.put(TYPE_MDB, "mdb");
        ResourceID.type2extensionMap.put(TYPE_MDA, "mda");
        ResourceID.type2extensionMap.put(TYPE_SPT, "spt");
        ResourceID.type2extensionMap.put(TYPE_GR2, "gr2");
        ResourceID.type2extensionMap.put(TYPE_FXA, "fxa");
        ResourceID.type2extensionMap.put(TYPE_FXE, "fxe");
        ResourceID.type2extensionMap.put(TYPE_JPG, "jpg");
        ResourceID.type2extensionMap.put(TYPE_PWC, "pwc");
        ResourceID.type2extensionMap.put(TYPE_IDS, "ids");
        ResourceID.type2extensionMap.put(TYPE_ERF, "erf");
        ResourceID.type2extensionMap.put(TYPE_BIF, "bif");
        ResourceID.type2extensionMap.put(TYPE_KEY, "key");

        // witcher stuff starts here
        ResourceID.type2extensionMap.put(TYPE_BIK, "bik");
        ResourceID.type2extensionMap.put(TYPE_NDB, "ndb");
        ResourceID.type2extensionMap.put(TYPE_PTM, "ptm");
        ResourceID.type2extensionMap.put(TYPE_PTT, "ptt");
        ResourceID.type2extensionMap.put(TYPE_NCM, "ncm");
        ResourceID.type2extensionMap.put(TYPE_MFX, "mfx");
        ResourceID.type2extensionMap.put(TYPE_MAT, "mat");
        ResourceID.type2extensionMap.put(TYPE_MDB, "mdb");
        ResourceID.type2extensionMap.put(TYPE_SAY, "say");
        ResourceID.type2extensionMap.put(TYPE_TTF, "ttf");
        ResourceID.type2extensionMap.put(TYPE_TTC, "ttc");
        ResourceID.type2extensionMap.put(TYPE_CUT, "cut");
        ResourceID.type2extensionMap.put(TYPE_KA, "ka");
        ResourceID.type2extensionMap.put(TYPE_ICO, "ico");
        ResourceID.type2extensionMap.put(TYPE_OGG, "ogg");
        ResourceID.type2extensionMap.put(TYPE_SPW, "spw");
        ResourceID.type2extensionMap.put(TYPE_UGM, "ugm");
        ResourceID.type2extensionMap.put(TYPE_QDB, "qdb");
        ResourceID.type2extensionMap.put(TYPE_QST, "qst");
        ResourceID.type2extensionMap.put(TYPE_NPC, "npc");
        ResourceID.type2extensionMap.put(TYPE_SPN, "spn");
        ResourceID.type2extensionMap.put(TYPE_UTX, "utx");
        ResourceID.type2extensionMap.put(TYPE_MMD, "mmd");
        ResourceID.type2extensionMap.put(TYPE_SMM, "smm");
        ResourceID.type2extensionMap.put(TYPE_UTA, "uta");
        ResourceID.type2extensionMap.put(TYPE_MDE, "mde");
        ResourceID.type2extensionMap.put(TYPE_MDV, "mdv");
        ResourceID.type2extensionMap.put(TYPE_MDA, "mda");
        ResourceID.type2extensionMap.put(TYPE_MBA, "mba");
        ResourceID.type2extensionMap.put(TYPE_OCT, "oct");
        ResourceID.type2extensionMap.put(TYPE_BFX, "bfx");
        ResourceID.type2extensionMap.put(TYPE_PDB, "pdb");
        ResourceID.type2extensionMap.put(TYPE_THEWITCHERSAVE, "TheWitcherSave");
        ResourceID.type2extensionMap.put(TYPE_PVS, "pvs");
        ResourceID.type2extensionMap.put(TYPE_CFX, "cfx");
        ResourceID.type2extensionMap.put(TYPE_LUC, "luc");
        ResourceID.type2extensionMap.put(TYPE_PRB, "prb");
        ResourceID.type2extensionMap.put(TYPE_WOB, "wob");
        // witcher stuff end

        for (final Map.Entry<Short, String> e : ResourceID.type2extensionMap.entrySet()) {
            ResourceID.extension2typeMap.put(e.getValue(), e.getKey());
        }

        //FIXME:
        // some types appearing in The Witcher bifs with same extensions
        // as a type from nwn; putting them in after building the type2ext map
        ResourceID.type2extensionMap.put(TYPE_MDB_WI, "mdb");
        ResourceID.type2extensionMap.put(TYPE_JPG_WI, "jpg");
        ResourceID.type2extensionMap.put(TYPE_WFX_WI, "wfx");
        ResourceID.type2extensionMap.put(TYPE_SPT_WI, "spt");
        ResourceID.type2extensionMap.put(TYPE_CAM_WI, "cam");

    }

    protected ResourceID() {
    }

    public ResourceID(String name, short type) {
        this.name = name;
        this.type = type;
    }

    public ResourceID(String name, String ext) {
        setType(ResourceID.getTypeForExtension(ext));
        setName(name);
    }

    @Override
    public int compareTo(Object o) {
        ResourceID id = (ResourceID) o;
        int s = getName().compareToIgnoreCase(id.getName());
        return (s == 0) ? getType() - id.getType() : s;
    }

    @Override
    public boolean equals(Object o) {
        return compareTo(o) == 0;
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + getType();
    }

    /**
     * {@inheritDoc }
     * @return filename for this resource ID
     */
    @Override
    public String toString() {
        return getFileName();
    }

    public static ResourceID forFile(File file) {
        return forFileName(file.getName());
    }

    public static ResourceID forFileName(String fname) {
        fname = fname.toLowerCase();

        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fname.length(); i++) {
            char c = fname.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '.') {
                sb.append(fname.charAt(i));
            }
        }
        fname = sb.toString();

        int last = fname.lastIndexOf('.');
        int first = fname.indexOf('.');
        String ext = (last == -1) ? "txt" : fname.substring(last + 1);
        String name = (first == -1) ? fname.substring(0, Math.min(fname.length(), 32)) : fname.substring(0, Math.min(first, 32));
        return new ResourceID(name, ext);
    }

    /**
     * @return the int value representing the type given by the parameter or -1 if the extension is not known
     */
    public static short getTypeForExtension(String extension) {
        Short type = extension2typeMap.get(extension);
        return (type == null) ? -1 : type.shortValue();
    }

    public static String getExtensionForType(short type) {
        String ext = type2extensionMap.get(type);
        return ext == null ? "0x" + Integer.toHexString(type) : ext;
    }

    private void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getExtension() {
        return getExtensionForType(type);
    }

    /**
     * Retrieve full filename (name and extension) for this resource.
     *
     * @return filename for this resource ID
     */
    public String getFileName() {
        return name + "." + getExtension();
    }

    private void setType(short type) {
        this.type = type;
    }

    public short getType() {
        return type;
    }
}
