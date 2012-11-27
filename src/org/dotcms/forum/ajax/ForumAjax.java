package org.dotcms.forum.ajax;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.directwebremoting.WebContext;
import org.dotcms.forum.util.ForumUtils;

import com.dotmarketing.beans.Host;
import com.dotmarketing.beans.Permission;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.NoSuchUserException;
import com.dotmarketing.business.UserAPI;
import com.dotmarketing.business.web.UserWebAPI;
import com.dotmarketing.business.web.WebAPILocator;
import com.dotmarketing.cache.FieldsCache;
import com.dotmarketing.cms.factories.PublicCompanyFactory;
import com.dotmarketing.db.DotHibernate;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.factories.EmailFactory;
import com.dotmarketing.factories.InodeFactory;
import com.dotmarketing.portlets.categories.business.CategoryAPI;
import com.dotmarketing.portlets.categories.model.Category;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.contentlet.business.ContentletAPI;
import com.dotmarketing.portlets.contentlet.business.HostAPI;
import com.dotmarketing.portlets.htmlpages.model.HTMLPage;
import com.dotmarketing.portlets.languagesmanager.business.LanguageAPI;
import com.dotmarketing.portlets.structure.factories.StructureFactory;
import com.dotmarketing.portlets.structure.model.Field;
import com.dotmarketing.portlets.structure.model.Structure;
import com.dotmarketing.portlets.usermanager.factories.UserManagerListBuilderFactory;
import com.dotmarketing.portlets.usermanager.struts.UserManagerListSearchForm;
import com.dotmarketing.tag.model.Tag;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.dotmarketing.util.WebKeys;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.User;
import com.liferay.util.servlet.SessionMessages;

public class ForumAjax {
	
	private UserAPI userAPI = APILocator.getUserAPI();
	private ContentletAPI conAPI = APILocator.getContentletAPI();
	private LanguageAPI langAPI = APILocator.getLanguageAPI();
	private HostAPI hostAPI = APILocator.getHostAPI();

	
	public Map<String, Object> subscribeToForumContent(String userId, String contentIdentifier) {
    	List<String> subscribeError = new ArrayList<String>();
    	Map<String,Object> callbackData = new HashMap<String,Object>();
    	
		try{
			User subscribingUser = userAPI.loadUserById(userId, userAPI.getSystemUser(), true);
			Logger.debug(this, "Hitting the subscribing option with user: " + subscribingUser.getFullName());
			Contentlet content =  conAPI.findContentletByIdentifier(contentIdentifier, true, langAPI.getDefaultLanguage().getId(), userAPI.getSystemUser(), true);
			if(!UtilMethods.isSet(content.getInode())){
				subscribeError.add("There was an error. Please try again");
			}
			else if(UtilMethods.isSet(subscribingUser) && UtilMethods.isSet(contentIdentifier)){
				List<Contentlet> cons = ForumUtils.getUserSubscriptionsPerStructure(subscribingUser.getUserId(), content.getIdentifier());
				if(cons.size () == 0){
					ForumUtils.createSubscription(content,subscribingUser);
				} else{
					subscribeError.add("User is already subscribed to this content.");
				}
			}
		}
		catch(Exception e) {
			subscribeError.add("There was an error. User can't subscribe to this content. Please try again.");
		}
		finally{
			if(subscribeError.size()>0)
				callbackData.put("subscribeError", subscribeError);
		}
		return callbackData;

	}
	
	public Map<String, Object> unsubscribeToForumContent(String userId, String contentIdentifier) {
		List<String> subscribeError = new ArrayList<String>();
    	Map<String,Object> callbackData = new HashMap<String,Object>();
    	
		try{
			User unsubscribingUser = userAPI.loadUserById(userId, userAPI.getSystemUser(), true);
			Logger.debug(this, "Hitting the unsubscribing option with user: " + unsubscribingUser.getFullName());
			Contentlet content =  conAPI.findContentletByIdentifier(contentIdentifier, true, langAPI.getDefaultLanguage().getId(), userAPI.getSystemUser(), true);
			if(!UtilMethods.isSet(content.getInode())){
				subscribeError.add("There was an error. Please try again");
			}
			else if(UtilMethods.isSet(unsubscribingUser) && UtilMethods.isSet(content)){
				List<Contentlet> cons = ForumUtils.getUserSubscriptionsPerStructure(unsubscribingUser.getUserId(), content.getIdentifier());
				if(cons.size()>0){
					ForumUtils.deleteSubscription(cons.get(0));
				}
			}
		}
		catch(Exception e) {
			subscribeError.add("Unable to unsubscribe. Try again later");
			Logger.error(this, "Unable to unsubscribe. Try again later");
		}
		finally{
			if(subscribeError.size()>0)
				callbackData.put("subscribeError", subscribeError);
		}
		return callbackData;

	}
	
	public boolean isUserSubscribed (String userId, String contentIdentifier){
		try {
			List<Contentlet> subscriptions = new ArrayList<Contentlet>();
			if(UtilMethods.isSet(userId) && UtilMethods.isSet(contentIdentifier))
				subscriptions = ForumUtils.getUserSubscriptionsPerStructure (userId, contentIdentifier);
				if (subscriptions.size() > 0)
					return true;

		} catch (Exception e) {
			
			e.printStackTrace();
		} 
		return false;
	}
	
	



	
	
	

	
}