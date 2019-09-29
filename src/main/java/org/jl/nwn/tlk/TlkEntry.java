package org.jl.nwn.tlk;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

public class TlkEntry implements Cloneable{

	public static final byte TEXT_PRESENT = 1;
	public static final byte SND_PRESENT = 2;
	public static final byte SNDLENGTH_PRESENT = 4;

	protected byte flags = 0;
	protected String string = "";
	protected String soundResRef = "";
	protected float soundLength = 0.0f;

	public TlkEntry( byte flags, String string, String sndResRef, float sndLength ){
		this.flags = flags;
		this.string = string;
		this.soundResRef = sndResRef;
		this.soundLength = sndLength;
	}

	public TlkEntry(){}

	public TlkEntry( TlkEntry entry ){
		flags = entry.flags;
		string = entry.string;
		soundResRef = entry.soundResRef;
		soundLength = entry.soundLength;
	}

	public TlkEntry( DataInputStream dis ) throws IOException{
		this();
		readObject(dis);
	}

	public void setStringFlag( boolean b ){
		setFlags( (byte) ((flags & 6) | (b? TEXT_PRESENT : 0)) );
	}

	public boolean hasString(){
		return (flags & TEXT_PRESENT) != 0;
	}

	public void setSoundFlag( boolean b ){
		setFlags( (byte) ((flags & 5) | (b? SND_PRESENT : 0)) );
	}

	public boolean hasSound(){
		return (flags & SND_PRESENT) != 0;
	}

	public void setSoundLengthFlag( boolean b ){
		setFlags( (byte) ((flags & 3) | (b? SNDLENGTH_PRESENT : 0)) );
	}

	public boolean hasSoundLength(){
		return (flags & SND_PRESENT) != 0;
	}

	public float getSoundLength() {
		return soundLength;
	}

	public String getSoundResRef() {
		return soundResRef;
	}

	public String getString() {
		return string;
	}

	public void setSoundLength(float f) {
		soundLength = f;
	}

	public void setSoundResRef(String ref) {
		soundResRef = ref;
	}

	public void setString(String string) {
		this.string = string;
	}

	public byte getFlags() {
		return flags;
	}

	public void setFlags(byte b) {
		flags = b;
	}

	@Override
	public TlkEntry clone(){
		TlkEntry ret = new TlkEntry();
		ret.setSoundResRef(this.soundResRef);
		ret.setString(this.string);
		ret.setSoundLength(this.soundLength);
		ret.setFlags(this.flags);
		return ret;
	}

	private void readObject( DataInput in )
		throws IOException{
		   setFlags(in.readByte());
		   setSoundLength(in.readFloat());
		   byte[] resRef = new byte[ in.readByte() ];
		   in.readFully( resRef );
		   setSoundResRef(new String( resRef ));
		   byte[] cntBuf = new byte[ in.readInt() ];
		   in.readFully( cntBuf );
		   setString(new String( cntBuf ));
		}

	// basically code for custom serialization
	// screw this ! no i18n support ( encoding / charset )
	public void writeEntry(DataOutput out) throws IOException {
        out.writeByte( getFlags() ); //flags
        out.writeFloat( getSoundLength() ); //soundLength
        out.writeByte( getSoundResRef().length() ); // sndResRef size
        out.write( getSoundResRef().getBytes() );
        out.writeInt( getString().length() );
        out.write( getString().getBytes() );
    }
}
