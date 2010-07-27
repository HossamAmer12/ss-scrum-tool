import models.Component;
import models.Project;
import models.Sprint;
import models.Story;
import models.Task;
import models.TaskStatus;
import models.TaskType;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import controllers.Tasks;

/**
 * editing task description
 * 
 * @author Moumen Mohamed
 */
public class C3S36editTaskReviewer extends UnitTest {
	@Before
	public void setUp() {
		Fixtures.deleteAll();

	}

	@Test
	public void createComponent() {
		Project x = new Project("amr", "Hany");
		x.save();
		User assignee = new User("one", "one@gmail.com", "test", "avatar", true);
		assignee.save();
		User reporter = new User("two", "two@gmail.com", "test", "avatar", true);
		reporter.save();
		User reviewer = new User("three", "three@gmail.com", "test", "avatar", true);
		reviewer.save();
		User user1 = new User("thdfafhhvbcree", "three111aaaa111@gmail.com", "tsssest", "avatar", true);
		user1.save();
		Story story1 = new Story("story", "none", "none", 1, "fdsf", assignee.id);
		story1.save();

		Component comp = new Component();
		comp.componentStories.add(story1);
		comp.componentUsers.add(assignee);
		comp.componentUsers.add(reviewer);
		comp.componentUsers.add(reporter);
		comp.project = x;
		comp.save();

		Task task1 = new Task("test", x);
		task1.assignee = assignee;
		task1.reporter = reporter;
		task1.reviewer = reviewer;
		task1.taskStory = story1;
		TaskType old = new TaskType();
		old.name = "old one";
		old.save();
		TaskStatus oldstatus = new TaskStatus();
		oldstatus.name = "old status";
		oldstatus.save();
		task1.taskStatus = oldstatus;
		task1.taskType = old;
		task1.estimationPoints = 1;
		Sprint s = new Sprint(2010, 3, 3, x);
		s.save();
		task1.taskSprint = s;
		task1.save();
		story1.componentID = comp;
		story1.storiesTask.add(task1);
		story1.save();

		boolean z = Tasks.editTaskReviewer(task1.id, user1.id);

		assertTrue(task1.reviewer.id == user1.id);
		boolean m = Tasks.editTaskReviewer(task1.id, user1.id);
		assertTrue(task1.reviewer.id == reviewer.id);

	}
}