package org.dotcms.forum.ajax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dotcms.forum.util.ForumUtils;

import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.UserAPI;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.contentlet.business.ContentletAPI;
import com.dotmarketing.portlets.contentlet.business.HostAPI;
import com.dotmarketing.portlets.languagesmanager.business.LanguageAPI;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.liferay.portal.model.User;

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