/*
 * Created on 06.08.2004
 */
package org.jl.swing;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 */
public class SearchAndReplaceMessages {

	private static final String BUNDLE_NAME = "org.jl.nwn.editor.SearchAndReplaceMessages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE =
		ResourceBundle.getBundle(BUNDLE_NAME);

	/**
	 * 
	 */
	private SearchAndReplaceMessages() {

		// TODO Auto-generated constructor stub
	}
	/**
	 * @param key
	 * @return
	 */
	public static String getString(String key) {
		// TODO Auto-generated method stub
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
