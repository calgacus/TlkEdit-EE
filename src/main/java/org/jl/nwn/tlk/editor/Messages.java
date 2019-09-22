/*
 * Created on 15.08.2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.jl.nwn.tlk.editor;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

class Messages {

	private static final String BUNDLE_NAME = "org.jl.nwn.tlk.editor.MessageBundle";

	private static ResourceBundle RESOURCE_BUNDLE;
	static {
		Locale defaultLocale = Locale.getDefault();
		System.out.println( "Messages.java locale is : " + defaultLocale );
		try{
			RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
		} catch ( MissingResourceException mre ){
			System.out.println( "Messages.java MissingResourceException  : " + mre.getLocalizedMessage() );
		}
	}

	/**
	 * 
	 */
	private Messages() {
	}
	/**
	 * @param key
	 * @return
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
