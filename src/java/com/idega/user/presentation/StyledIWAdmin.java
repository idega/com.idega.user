/*
 * Created on Oct 6, 2003
 */
package com.idega.user.presentation;

import com.idega.idegaweb.presentation.IWAdminWindow;

/**
 * Description: <br>
 * Copyright: Idega Software 2003 <br>
 * Company: Idega Software <br>
 * @author <a href="mailto:eiki@idega.is">Eirikur S. Hrafnsson</a>
 */
public class StyledIWAdmin extends IWAdminWindow {

	/**
	 * 
	 */
	public StyledIWAdmin() {
		super();
		//this.setStyleSheetURL()
	}

	/**
	 * @param name
	 */
	public StyledIWAdmin(String name) {
		super(name);
	}

	/**
	 * @param width
	 * @param heigth
	 */
	public StyledIWAdmin(int width, int heigth) {
		super(width, heigth);
	}

	/**
	 * @param name
	 * @param width
	 * @param height
	 */
	public StyledIWAdmin(String name, int width, int height) {
		super(name, width, height);
	}

	/**
	 * @param name
	 * @param url
	 */
	public StyledIWAdmin(String name, String url) {
		super(name, url);
	}

	/**
	 * @param name
	 * @param width
	 * @param height
	 * @param url
	 */
	public StyledIWAdmin(String name, int width, int height, String url) {
		super(name, width, height, url);
	}

	/**
	 * @param name
	 * @param classToInstanciate
	 * @param template
	 */
	public StyledIWAdmin(String name, String classToInstanciate, String template) {
		super(name, classToInstanciate, template);
	}


	/**
	 * @param name
	 * @param classToInstanciate
	 */
	public StyledIWAdmin(String name, Class classToInstanciate) {
		super(name, classToInstanciate);
	}

}
