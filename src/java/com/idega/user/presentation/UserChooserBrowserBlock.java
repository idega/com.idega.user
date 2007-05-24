package com.idega.user.presentation;

import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.StyledButton;
import com.idega.presentation.ui.TextInput;
import com.idega.presentation.ui.util.AbstractChooserBlock;

public class UserChooserBrowserBlock extends AbstractChooserBlock {

	private static final String RESULTS_CONTAINER_ID = "user_search_container";
	
	public void main(IWContext iwc) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Layer container = getMainContaier();
		
		addSearchInput(container, iwrb);
		
		Layer resultsContainer = new Layer();
		resultsContainer.setId(RESULTS_CONTAINER_ID);
		container.add(resultsContainer);
		
		add(container);
	}
	
	private void addSearchInput(Layer container, IWResourceBundle iwrb) {
		TextInput searchInput = new TextInput(UserChooserBrowserWindow.SEARCH_KEY);
		container.add(searchInput);
		
		GenericButton mainButton = new GenericButton(UserChooserBrowserWindow.SEARCH_SUBMIT_KEY,
				iwrb.getLocalizedString("search", "Search"));
		StringBuffer action = new StringBuffer("executeSearch('").append(RESULTS_CONTAINER_ID).append("', document.getElementById('");
		action.append(searchInput.getId()).append("').value, '").append(iwrb.getLocalizedString("searching", "Searching..."));
		action.append("');");
		mainButton.setOnClick(action.toString());
		StyledButton searchButton = new StyledButton(mainButton);
		container.add(searchButton);
	}
	
	@Override
	public boolean getChooserAttributes() {
		// TODO Auto-generated method stub
		return false;
	}

}
