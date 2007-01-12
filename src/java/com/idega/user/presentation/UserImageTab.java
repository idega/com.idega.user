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
		this.imageFieldName = "usr_imag_userSystemImageId";
    this.removeImageFieldName = "image_removeImageFieldName";
	}

	public void initializeFields() {
		this.imageField = new ImageInserter(this.imageFieldName + getUserId());
		this.imageField.setHasUseBox(false);
    this.removeImageField = new CheckBox(this.removeImageFieldName);
	}

	public void initializeTexts() {
		IWContext iwc = IWContext.getInstance();
		IWResourceBundle iwrb = getResourceBundle(iwc);

		this.imageText = getTextObject();
		this.imageText.setText(iwrb.getLocalizedString(this.imageFieldName, "Image") + ":");
    
    this.removeImageText = getTextObject();
    this.removeImageText.setText(iwrb.getLocalizedString(this.removeImageFieldName, "do not show an image"));
	}

	public void initializeFieldValues() {
		this.systemImageId = -1;
    this.fieldValues.put(this.removeImageFieldName, new Boolean(false));
	}

	public void lineUpFields() {
		this.resize(1, 1);

		Table imageTable = new Table(1, 3);
		imageTable.setWidth("100%");
		imageTable.setCellpadding(0);
		imageTable.setCellspacing(0);

		imageTable.add(this.imageText, 1, 1);
		imageTable.add(this.imageField, 1, 2);
    imageTable.add(this.removeImageField, 1, 3);
    imageTable.add(Text.getNonBrakingSpace(),1,3);
    imageTable.add(this.removeImageText,1,3);
		this.add(imageTable, 1, 1);
	}

	public void updateFieldsDisplayStatus() {
		this.imageField.setImageId(this.systemImageId);
    this.removeImageField.setChecked(((Boolean)this.fieldValues.get(this.removeImageFieldName)).booleanValue());
	}

	public boolean collect(IWContext iwc) {
		String imageID = iwc.getParameter(this.imageFieldName + this.getUserId());
		if (imageID != null) {
			this.fieldValues.put(this.imageFieldName, imageID);
		}
    
    this.fieldValues.put(this.removeImageFieldName, new Boolean(iwc.isParameterSet(this.removeImageFieldName)));

		return true;
	}

	public boolean store(IWContext iwc) {
		try {
			if (getUserId() > -1) {

				String image = (String)this.fieldValues.get(this.imageFieldName);

				if ((image != null) && (!image.equals("-1")) && (!image.equals(""))) {
          if (this.user == null) {
			this.user = getUser();
		}
          int tempId;
          if (((Boolean) this.fieldValues.get(this.removeImageFieldName)).booleanValue())  {
            this.user.setSystemImageID(null);
            // set variables to default values
            this.systemImageId = -1;
            this.fieldValues.put(this.imageFieldName, "-1");
            this.user.store();
            updateFieldsDisplayStatus();
          }
          else if ((tempId = Integer.parseInt(image)) != this.systemImageId) {
						this.systemImageId = tempId;
						this.user.setSystemImageID(this.systemImageId);
						this.user.store();
						updateFieldsDisplayStatus();
					}

					iwc.removeSessionAttribute(this.imageFieldName + getUserId());

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

			this.imageField.setImSessionImageName(this.imageFieldName + getUserId());

			if (this.user == null) {
				this.user = getUser();
			}

			this.systemImageId = getSelectedImageId(this.user);

			if (this.systemImageId != -1) {
				this.fieldValues.put(this.imageFieldName, Integer.toString(this.systemImageId));
			}
      
      this.fieldValues.put(this.removeImageFieldName, new Boolean(false));
    
			this.updateFieldsDisplayStatus();
		}
		catch (Exception e) {
			System.err.println(
				"UserImageTab error initFieldContents, userId : " + getUserId());
		}

	}

	private void setSelectedImageId() {
		try {
			String image = (String)this.fieldValues.get(this.imageFieldName);
			if ((image != null)
				&& (!image.equals("-1"))
				&& (!image.equals(""))
				&& (!image.equals("0"))) {
				this.systemImageId = Integer.parseInt(image);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
		}

	}

	private int getSelectedImageId(User user) {
		try {
			int tempImageId = user.getSystemImageID();
			if ((this.systemImageId == -1) && (tempImageId != -1)) {
				this.systemImageId = tempImageId;
			}
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
		}

		return this.systemImageId;
	}
	
	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}	
}