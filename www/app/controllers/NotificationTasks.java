package controllers;

import java.util.List;

import models.Notification;
import models.User;
import play.mvc.With;

@With (Secure.class)
public class NotificationTasks extends SmartController {
	
	public static void getLatestNews() {
		Security.check(Security.isConnected());
		User myUser = Security.getConnected();
		List<Notification> news = Notification.find("unread = true and receiver = ?", myUser).fetch();
		for (int i = 0; i < news.size(); i++) {
			news.get(i).unread = false;
			news.get(i).save();
			news.get(i).receiver = null;
		}
		renderJSON(news);
	}
	
}
