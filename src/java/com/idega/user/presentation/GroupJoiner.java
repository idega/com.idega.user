/**
 * @(#)GroupJoiner.java    1.0.0 09:45:33
 *
 * Idega Software hf. Source Code Licence Agreement x
 *
 * This agreement, made this 10th of February 2006 by and between 
 * Idega Software hf., a business formed and operating under laws 
 * of Iceland, having its principal place of business in Reykjavik, 
 * Iceland, hereinafter after referred to as "Manufacturer" and Agura 
 * IT hereinafter referred to as "Licensee".
 * 1. License Grant: Upon completion of this agreement, the source 
 *     code that may be made available according to the documentation for 
 *     a particular software product (Software) from Manufacturer 
 *     (Source Code) shall be provided to Licensee, provided that 
 *     (1) funds have been received for payment of the License for Software and 
 *     (2) the appropriate License has been purchased as stated in the 
 *     documentation for Software. As used in this License Agreement, 
 *     �Licensee� shall also mean the individual using or installing 
 *     the source code together with any individual or entity, including 
 *     but not limited to your employer, on whose behalf you are acting 
 *     in using or installing the Source Code. By completing this agreement, 
 *     Licensee agrees to be bound by the terms and conditions of this Source 
 *     Code License Agreement. This Source Code License Agreement shall 
 *     be an extension of the Software License Agreement for the associated 
 *     product. No additional amendment or modification shall be made 
 *     to this Agreement except in writing signed by Licensee and 
 *     Manufacturer. This Agreement is effective indefinitely and once
 *     completed, cannot be terminated. Manufacturer hereby grants to 
 *     Licensee a non-transferable, worldwide license during the term of 
 *     this Agreement to use the Source Code for the associated product 
 *     purchased. In the event the Software License Agreement to the 
 *     associated product is terminated; (1) Licensee's rights to use 
 *     the Source Code are revoked and (2) Licensee shall destroy all 
 *     copies of the Source Code including any Source Code used in 
 *     Licensee's applications.
 * 2. License Limitations
 *     2.1 Licensee may not resell, rent, lease or distribute the 
 *         Source Code alone, it shall only be distributed as a 
 *         compiled component of an application.
 *     2.2 Licensee shall protect and keep secure all Source Code 
 *         provided by this this Source Code License Agreement. 
 *         All Source Code provided by this Agreement that is used 
 *         with an application that is distributed or accessible outside
 *         Licensee's organization (including use from the Internet), 
 *         must be protected to the extent that it cannot be easily 
 *         extracted or decompiled.
 *     2.3 The Licensee shall not resell, rent, lease or distribute 
 *         the products created from the Source Code in any way that 
 *         would compete with Idega Software.
 *     2.4 Manufacturer's copyright notices may not be removed from 
 *         the Source Code.
 *     2.5 All modifications on the source code by Licencee must 
 *         be submitted to or provided to Manufacturer.
 * 3. Copyright: Manufacturer's source code is copyrighted and contains 
 *     proprietary information. Licensee shall not distribute or 
 *     reveal the Source Code to anyone other than the software 
 *     developers of Licensee's organization. Licensee may be held 
 *     legally responsible for any infringement of intellectual property 
 *     rights that is caused or encouraged by Licensee's failure to abide 
 *     by the terms of this Agreement. Licensee may make copies of the 
 *     Source Code provided the copyright and trademark notices are 
 *     reproduced in their entirety on the copy. Manufacturer reserves 
 *     all rights not specifically granted to Licensee.
 *
 * 4. Warranty & Risks: Although efforts have been made to assure that the 
 *     Source Code is correct, reliable, date compliant, and technically 
 *     accurate, the Source Code is licensed to Licensee as is and without 
 *     warranties as to performance of merchantability, fitness for a 
 *     particular purpose or use, or any other warranties whether 
 *     expressed or implied. Licensee's organization and all users 
 *     of the source code assume all risks when using it. The manufacturers, 
 *     distributors and resellers of the Source Code shall not be liable 
 *     for any consequential, incidental, punitive or special damages 
 *     arising out of the use of or inability to use the source code or 
 *     the provision of or failure to provide support services, even if we 
 *     have been advised of the possibility of such damages. In any case, 
 *     the entire liability under any provision of this agreement shall be 
 *     limited to the greater of the amount actually paid by Licensee for the 
 *     Software or 5.00 USD. No returns will be provided for the associated 
 *     License that was purchased to become eligible to receive the Source 
 *     Code after Licensee receives the source code. 
 */
package com.idega.user.presentation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;

import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.Label;
import com.idega.user.IWBundleStarter;
import com.idega.user.business.UserBusiness;
import com.idega.user.business.UserConstants;
import com.idega.user.data.Group;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

/**
 * Class description goes here.
 * You can report about problems to: <a href="mailto:martynas@idega.com">Martynas Stakė</a>
 * You can expect to find some test cases notice in the end of the file.
 *
 * @version 1.0.0 2011.09.05
 * @author martynas
 */
public class GroupJoiner extends IWBaseComponent{
    
    private String groupId = null;
    private Integer userId = null;
    private UserBusiness userBusiness = null;
    private IWResourceBundle iwrb = null;
    private  IWContext iwc = null;
    private static String JS_STR_INITIALIZATION_END = "';";
    
    
    public GroupJoiner() {}
    
    public GroupJoiner(String groupId,Integer userId) {
        this.groupId = groupId;
        this.userId = userId;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected void initializeComponent(FacesContext context) {
        ELUtil.getInstance().autowire(this);
        iwc = IWContext.getIWContext(context);
        if (!iwc.isLoggedOn()) {
            return;
        }
        if(userId == null){
        	userId = iwc.getCurrentUserId();
        }
        
//        HtmlTag div = new HtmlTag();//(HtmlTag)context.getApplication().createComponent(HtmlTag.COMPONENT_TYPE);
//        getChildren().add(div);
//        div.setValue(divTag);
        Layer main = new Layer();
        this.add(main);
        
        IWBundle bundle = getBundle(context, IWBundleStarter.IW_BUNDLE_IDENTIFIER);
        iwrb = bundle.getResourceBundle(iwc);     

        if(groupId == null){
        	Label label = new Label();
        	main.add(label);
        	label.addText(iwrb.getLocalizedString("no_group_set", "No group set"));
        	return;
        }
        
        Group group = null;
        Collection <Group> groups =  null;
        try{
        	group = this.getUserBusiness().getGroupBusiness().getGroupByGroupID(Integer.valueOf(groupId));
        	groups =  this.getUserBusiness().getUserGroups(userId);
        }catch(Exception e){
        	Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "failed getting groups by ids", e);
        }
        
        GenericButton joinButton = new GenericButton();
        main.add(joinButton);
        GenericButton leaveButton = new GenericButton();
        main.add(leaveButton);
        
        StringBuilder parameters = new StringBuilder().append(this.userId)
		        .append(CoreConstants.JS_STR_PARAM_SEPARATOR).append(this.groupId)
				.append("','#").append(joinButton.getId())
				.append("','#").append(leaveButton.getId())
				.append(CoreConstants.JS_STR_PARAM_END);
        
        joinButton.setValue(iwrb.getLocalizedString("join", "Join"));
    	String action = new StringBuilder("GroupJoinerHelper.joinGroup('").append(parameters).toString();
    	joinButton.setOnClick(action);
    	leaveButton.setValue(iwrb.getLocalizedString("leave", "Leave"));
    	action = new StringBuilder("GroupJoinerHelper.leaveGroup('").append(parameters).toString();
    	leaveButton.setOnClick(action);
    	
    	 if(groups.contains(group)){
    		 joinButton.setStyleAttribute("display : none;");
         }else{
        	 leaveButton.setStyleAttribute("display : none;");
         }
         
    	 addActions(main);
        
    }
    
    private void addActions(Layer main){
    	StringBuilder actions = new StringBuilder("GroupJoinerHelper.FAILURE_MSG = '")
    		.append(iwrb.getLocalizedString("failed", "Failed")).append(JS_STR_INITIALIZATION_END)
    		.append("GroupJoinerHelper.ADDING_TO_GROUP_MSG = '")
    		.append(iwrb.getLocalizedString("adding_to_group", "Adding to group")).append(JS_STR_INITIALIZATION_END)
    		.append("GroupJoinerHelper.REMOVING_FROM_GROUP_MSG = '")
    		.append(iwrb.getLocalizedString("removing_from_group", "Removing from group")).append(JS_STR_INITIALIZATION_END);
		String actionString = PresentationUtil.getJavaScriptAction(actions.toString());
		main.add(actionString);
    }
    
    public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	/**
	 * Gets the scripts that is need for this element to work
	 * @return script files uris
	 */
	public static List<String> getNeededScripts(IWContext iwc){
		
		List<String> scripts = new ArrayList<String>();

		scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
		scripts.add(CoreConstants.DWR_UTIL_SCRIPT);

		Web2Business web2 = WFUtil.getBeanInstance(iwc, Web2Business.SPRING_BEAN_IDENTIFIER);
		if (web2 != null) {
			JQuery  jQuery = web2.getJQuery();
			scripts.add(jQuery.getBundleURIToJQueryLib());

			scripts.add(web2.getBundleUriToHumanizedMessagesScript());

		}else{
			Logger.getLogger("ContentShareComponent").log(Level.WARNING, "Failed getting Web2Business no jQuery and it's plugins files were added");
		}

		IWMainApplication iwma = iwc.getApplicationContext().getIWMainApplication();
		IWBundle iwb = iwma.getBundle(UserConstants.IW_BUNDLE_IDENTIFIER);
		scripts.add(iwb.getVirtualPathWithFileNameString("javascript/GroupJoinerHelper.js"));
		scripts.add("/dwr/interface/GroupService.js");
		
		return scripts;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	private UserBusiness getUserBusiness() {
		try{
			if(userBusiness == null){
				this.userBusiness = IBOLookup.getServiceInstance(iwc == null ? 
							CoreUtil.getIWContext(): iwc, UserBusiness.class);
			}
		}catch(IBOLookupException e){
			Logger.getLogger("ContentShareComponent").log(Level.WARNING, "Failed getting UserBusiness",e);
		}
		return userBusiness;
	}

}
