package org.jl.nwn.tlk;

public class TlkLookup{

	public static final int USERTLKOFFSET = 1 << 24;
	public static final int INVALIDSTRREF = -1;

	private static final TlkEntry INVALIDENTRY = new TlkEntry( (byte)0, "", "", 0 );
	private static final int STRREFMASK = USERTLKOFFSET - 1;

	private boolean useFlags = true;
	TlkContent main;
	TlkContent user = null;

	public TlkLookup( TlkContent main ){
		this.main = main;
	}

	public TlkLookup( TlkContent main, TlkContent user ){
		this.main = main;
	}

	private boolean isUserStrRef( int strRef ){
		return (strRef & USERTLKOFFSET) != 0;
	}

	protected TlkEntry getEntry( int pos ){
		if ( pos == INVALIDSTRREF )
			return INVALIDENTRY;
		if ( isUserStrRef(pos) ){
			pos &= STRREFMASK;
			if ( user != null && pos < user.size() )
				return user.get(pos);
			// fall back to main talk table
		}
		pos &= STRREFMASK;
		if ( pos < main.size() )
			return main.get(pos);
		// if all fails return entry 0 of main table ( usually "BAD STRREF" )
		return main.get(0);
	}

	public String getString( int pos ){
		TlkEntry e = getEntry(pos);
		return useFlags && !e.hasString() ? "" : e.getString();
	}

	public String getSoundResRef( int pos ){
		TlkEntry e = getEntry(pos);
		return useFlags && !e.hasSound() ? "" : e.getSoundResRef();
	}

	public float getSoundLength( int pos ){
		TlkEntry e = getEntry(pos);
		return useFlags && !e.hasSoundLength() ? 0 : e.getSoundLength();
	}

	public void setUserTable( TlkContent user ){
		this.user = user;
	}

	public void setMainTable( TlkContent main ){
		this.main = main;
	}

	public void useFlags( boolean flags ){
		useFlags = flags;
	}
}
