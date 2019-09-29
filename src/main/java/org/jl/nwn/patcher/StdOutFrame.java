package org.jl.nwn.patcher;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
;

/**
 * StdOutFrame displays the standard out and standard error streams ( System.out / System.err )
 * in a JTextArea ( uses a t piece to pipe System.out/err to the usual place )
 *
 * StdOutFrame is a Singleton, use {@link StdOutFrame#getInstance()} to get the single instant.
 *
 * @author ich
 */
public class StdOutFrame extends JFrame {

	private static final StdOutFrame uniqueInstance;// = new StdOutFrame();

	public static final JTextArea text = new JTextArea();

	private static final OutputStream textAreaOutputStream = new OutputStream(){
		@Override
		public void write( int i ){
			text.append( Character.toString( (char) i ) );
			text.setCaretPosition( text.getText().length() );
		}
		@Override
		public void write(byte[] b, int offset, int length ){
			text.append( new String( b, offset, length ) );
			text.setCaretPosition( text.getText().length() );
		}
		@Override
		public void write(byte[] b){
			write( b, 0, b.length );
		}
	};
	static class Tee extends OutputStream{
		private final OutputStream out1;
		private final OutputStream out2;
		public Tee( OutputStream out1, OutputStream out2 ){
			this.out1 = out1;
			this.out2 = out2;
		}

		@Override
		public void write( int i ) throws IOException{
			out1.write(i);
			out2.write(i);
		}
		@Override
		public void write( byte[] b, int offset, int length ) throws IOException{
			out1.write( b, offset, length );
			out2.write( b, offset, length );
		}
		@Override
		public void write( byte[] b ) throws IOException{
			out1.write( b );
			out2.write( b );
		}
	};
	private static final PrintStream textAreaPrintStream = new PrintStream( textAreaOutputStream, true );

	private static final JToolBar tbar = new JToolBar();

	// original streams
	private static final PrintStream system_out;
	private static final PrintStream system_err;
	// tee piped streams that write to text area and original streams
	private static final PrintStream t_piped_out;
	private static final PrintStream t_piped_err;

	static {
		text.setVisible( true );
		text.setEditable( false );
		//text.setFont( stdErrFont );

		system_out = System.out;
		system_err = System.err;

		System.setOut( t_piped_out = new PrintStream( new Tee( textAreaPrintStream, System.out ) ) );
		System.setErr( t_piped_err = new PrintStream( new Tee( textAreaPrintStream, System.err ) ) );

		tbar.setFloatable( false );
		Action clear = new AbstractAction("clear"){
			@Override
			public void actionPerformed( ActionEvent e ){
				text.setText("");
			}
		};
		tbar.add( clear );
		uniqueInstance = new StdOutFrame();
	}

	private StdOutFrame(){
		setTitle( "std out / std err" );
		setSize( new Dimension( 600, 400 ) );
		getContentPane().setLayout( new BorderLayout() );
		getContentPane().add( new JScrollPane( text ), BorderLayout.CENTER );
		getContentPane().add( tbar, BorderLayout.NORTH );
	}

	public static StdOutFrame getInstance(){
		return uniqueInstance;
	}

	public static void display( boolean visible ){
		uniqueInstance.setVisible( visible );
	}

	public static void decouple(){
		System.setErr( system_err );
		System.setOut( system_out );
	}

	public static void couple(){
		System.setErr( t_piped_err );
		System.setOut( t_piped_out );
	}

	public static void main(String[] args){
		StdOutFrame.getInstance().setVisible(true);
		StdOutFrame.getInstance().setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		System.out.print("foo");
		System.err.println("bar");
	}
}
