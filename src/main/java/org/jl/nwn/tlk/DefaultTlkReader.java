package org.jl.nwn.tlk;

import org.jl.nwn.NwnLanguage;
import org.jl.nwn.Version;

/**
 * Creates {@link TlkContent} objects from tlk files.
 */
public class DefaultTlkReader extends AbstractTlkReader<TlkContent>{
    
    /** Creates a new instance of DefaultTlkReader */
    public DefaultTlkReader(Version v) {
        super(v);
    }
    
    @Override
    protected void createEntry(TlkContent tlk, int position,
            byte flags, String resRef, float sndLength, String string) {
        tlk.add( position,
                new TlkEntry(
                flags,
                string,
                resRef==null ? "" : resRef,
                sndLength));
    }
    
    @Override
    protected TlkContent createTlk(
            int size,
            NwnLanguage lang,
            Version nwnVersion) {
        return new TlkContent(lang);
    }
    
}
