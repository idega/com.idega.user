package com.idega.user.presentation;

import com.idega.block.media.presentation.ImageInserter;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;

/**
 * Title:        A simple tab to add images to users
 * Copyright:    Idega Software Copyright (c) 2001
 * Company:      Idega Software
 * @author <a href="mailto:eiki@idega.is">Eirikur Hrafnsson</a>
 * @version 1.0
 */
public class UserImageTab extends UserTab {
	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.user";

	private static final String TAB_NAME = "usr_imag_tab_name";
	private static final String DEFAULT_TAB_NAME = "Image";
	
	/*	this is the order methods are executed
	    initializeFieldNames();
	    initializeFields();
	    initializeTexts();
	    initializeFieldValues();
	    lineUpFields();
	 */
	private ImageInserter imageField;
	private String imageFieldName;
  private Text imageText;
  
  private CheckBox removeImageField;
  private String removeImageFieldName;
  private Text removeImageText;
  
	private UserBusiness biz;


	private User user = null;
	private int systemImageId = -1;

	public UserImageTab() {
		super();
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);

		setName(iwrb.getLocalizedString(TAB_NAME, DEFAULT_TAB_NAME));
//		setName("Image");
	}

	public UserImageTab(int userId) {
		this();
		setUserID(userId);
	}

	public void initializeFieldNames() {
		imageFieldName = "usr_imag_userSystemImageId";
    removeImageFieldName = "image_removeImageFieldName";
	}

	public void initializeFields() {
		imageField = new ImageInserter(imageFieldName + getUserId());
		imageField.setHasUseBox(false);
    removeImageField = new CheckBox(removeImageFieldName);
	}

	public void initializeTexts() {
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);

		imageText = getTextObject();
		imageText.setText(iwrb.getLocalizedString(imageFieldName, "Image") + ":");
    
    removeImageText = getTextObject();
    removeImageText.setText(iwrb.getLocalizedString(removeImageFieldName, "do not show an image"));
	}

	public void initializeFieldValues() {
		systemImageId = -1;
    fieldValues.put(removeImageFieldName, new Boolean(false));
	}

	public void lineUpFields() {
		this.resize(1, 1);

		Table imageTable = new Table(1, 3);
		imageTable.setWidth("100%");
		imageTable.setCellpadding(0);
		imageTable.setCellspacing(0);

		imageTable.add(imageText, 1, 1);
		imageTable.add(this.imageField, 1, 2);
    imageTable.add(removeImageField, 1, 3);
    imageTable.add(Text.getNonBrakingSpace(),1,3);
    imageTable.add(removeImageText,1,3);
		this.add(imageTable, 1, 1);
	}

	public void updateFieldsDisplayStatus() {
		imageField.setImageId(systemImageId);
    removeImageField.setChecked(((Boolean)fieldValues.get(removeImageFieldName)).booleanValue());
	}

	public boolean collect(IWContext iwc) {
		String imageID = iwc.getParameter(imageFieldName + this.getUserId());
		if (imageID != null) {
			fieldValues.put(imageFieldName, imageID);
		}
    
    fieldValues.put(removeImageFieldName, new Boolean(iwc.isParameterSet(removeImageFieldName)));

		return true;
	}

	public boolean store(IWContext iwc) {
		try {
			if (getUserId() > -1) {

				String image = (String)fieldValues.get(imageFieldName);

				if ((image != null) && (!image.equals("-1")) && (!image.equals(""))) {
          if (user == null)
						user = getUser();
          int tempId;
          if (((Boolean) fieldValues.get(removeImageFieldName)).booleanValue())  {
            user.setSystemImageID(null);
            // set variables to default values
            systemImageId = -1;
            fieldValues.put(imageFieldName, "-1");
            user.store();
            updateFieldsDisplayStatus();
          }
          else if ((tempId = Integer.parseInt(image)) != systemImageId) {
						systemImageId = tempId;
						user.setSystemImageID(systemImageId);
						user.store();
						updateFieldsDisplayStatus();
					}

					iwc.removeSessionAttribute(imageFieldName + getUserId());

				}

			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("update user exception");
		}
		return true;
	}

	public void initFieldContents() {

		try {

			imageField.setImSessionImageName(imageFieldName + getUserId());

			if (user == null)
				user = getUser();

			systemImageId = getSelectedImageId(user);

			if (systemImageId != -1) {
				fieldValues.put(this.imageFieldName, Integer.toString(systemImageId));
			}
      
      fieldValues.put(removeImageFieldName, new Boolean(false));
    
			this.updateFieldsDisplayStatus();
		}
		catch (Exception e) {
			System.err.println(
				"UserImageTab error initFieldContents, userId : " + getUserId());
		}

	}

	private void setSelectedImageId() {
		try {
			String image = (String)fieldValues.get(this.imageFieldName);
			if ((image != null)
				&& (!image.equals("-1"))
				&& (!image.equals(""))
				&& (!image.equals("0"))) {
				systemImageId = Integer.parseInt(image);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
		}

	}

	private int getSelectedImageId(User user) {
		try {
			int tempImageId = user.getSystemImageID();
			if ((systemImageId == -1) && (tempImageId != -1))
				systemImageId = tempImageId;
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
		}

		return systemImageId;
	}
	
	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}	
}