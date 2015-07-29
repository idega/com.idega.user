package com.idega.user.presentation;

import java.sql.SQLException;

import com.idega.idegaweb.IWBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.user.data.User;

public class UserImage extends Block {

	private final static String IW_BUNDLE_IDENTIFIER = "com.idega.user";
	
	public void main(IWContext iwc) {
		IWBundle iwb = getBundle(iwc);
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("userImage");
		Image image = null;
		if (iwc.isLoggedOn()) {
			User user = iwc.getCurrentUser();
			if (user.getSystemImageID() > 0) {
				try {
					image = new Image(user.getSystemImageID());
				}
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		if (image == null) {
			image = iwb.getImage("user_image.gif");
		}
		layer.add(image);
		add(layer);
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}
}