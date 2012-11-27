package org.dotcms.forum.util;

import java.util.List;

import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.UserAPI;
import com.dotmarketing.cache.FieldsCache;
import com.dotmarketing.portlets.contentlet.business.ContentletAPI;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.structure.factories.RelationshipFactory;
import com.dotmarketing.portlets.structure.model.Field;
import com.dotmarketing.portlets.structure.model.Relationship;
import com.dotmarketing.portlets.structure.model.Structure;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.liferay.portal.model.User;

public class CleanSubscriptionsThread extends Thread {
	
	private Structure contentStructure;
	private Contentlet content;
	private static UserAPI userAPI = APILocator.getUserAPI();
	private static ContentletAPI conAPI = APILocator.getContentletAPI();

	public CleanSubscriptionsThread (Structure contentStructure, Contentlet content) {
		super ("CleanSubscriptionsThread");
		this.contentStructure = contentStructure;
		this.content = content;
	}

	@Override
	public void run() {
		try {
			Logger.debug(this, "Hitting the Subscriptions Cleaner for structure "+ contentStructure.getName());
			String luceneQuery = "";
			Structure subscriptionStructure = ForumUtils.getSubscriptionStructureToSendEmails(contentStructure);
			if(UtilMethods.isSet(subscriptionStructure.getInode())){
				List <Field> fields = FieldsCache.getFieldsByStructureInode(subscriptionStructure.getInode());
				Field userIdField = null;
				Field contentIdField = null;
				
				for (Field f : fields){
					if(f.getVelocityVarName().contains("topicId") || (f.getVelocityVarName().contains("threadId"))){
						contentIdField = f;
					}
					else if(f.getVelocityVarName().contains("userId")){
						userIdField = f;
					}
				}
				
				List<Relationship> rels = RelationshipFactory.getRelationshipsByChild(contentStructure);
				
				List<Contentlet> parentContents = conAPI.getRelatedContent(content, rels.get(0), userAPI.getSystemUser(), true);
				
				luceneQuery = "+structureName:" + subscriptionStructure.getVelocityVarName() + 
				" +"+ subscriptionStructure.getVelocityVarName() +  "." + contentIdField.getVelocityVarName() + ":" + parentContents.get(0).getIdentifier() +
				" +working:true +deleted:false";
				
				List<Contentlet> cons = conAPI.search(luceneQuery, 0, -1, "modDate", userAPI.getSystemUser(), true);
				
				
				if(cons.size()>0){
					for(Contentlet c : cons){
						String userIdFromContent = (String) c.getMap().get(userIdField.getVelocityVarName());
						if(UtilMethods.isSet(userIdFromContent)){
							User userToCheck = null;
							try{
								userToCheck = userAPI.loadUserById(userIdFromContent, userAPI.getSystemUser(), true);
							}
							catch (Exception e){
								Logger.error(ForumUtils.class, "User " + userIdFromContent + " doesn't exist");
							}
							if (!UtilMethods.isSet(userToCheck))
								ForumUtils.deleteSubscription(c);
						}
						
					}
				}
				
			}
		}
		catch(Exception e){
			Logger.error(this,"Unable to run EmailSubscribersThread");
		}
	}
	
}