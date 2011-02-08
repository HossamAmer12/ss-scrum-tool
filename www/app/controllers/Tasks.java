package controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import notifiers.Notifications;

import models.Board;
import models.BoardColumn;
import models.Comment;
import models.Component;
import models.Log;
import models.Meeting;
import models.ProductRole;
import models.Project;
import models.Reviewer;
import models.Sprint;
import models.Task;
import models.TaskStatus;
import models.TaskType;
import models.CollaborateUpdate;
import models.User;
import play.db.jpa.JPASupport;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.mvc.Router;
import play.mvc.With;

/**
 * Represents the Task Entity in the Database and it's relations with other
 * entities.
 * 
 * @see models.Task
 */
@With( Secure.class )
public class Tasks extends SmartCRUD
{

	/**
	 * Overrides the CRUD blank method that renders the create form to create a
	 * task.
	 * 
	 * @author Monayri
	 * @param componentId
	 *            The component id that the task is added to.
	 * @param taskId
	 *            The super task id in case the task is a sub task.
	 * @param projectId
	 *            The project id that the task is added to.
	 * @return void
	 */
	public static void blank( long component_id, long task_id, long project_id )
	{
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		User user = Security.getConnected();
		Project project = null;
		Component component = null;
		Task task = null;
		Project p = null;
		if( project_id != 0 ) // adding task to project (put drop down list of
		// components)
		{
			p = Project.findById( project_id );
			project = Project.findById( project_id );
			if( project.deleted )
				notFound();
			Security.check( user.in( project ).can( "AddTask" ) );
		}
		else
		{
			if( component_id != 0 ) // adding task to component
			{
				component = Component.findById( component_id );
				project = component.project;
				if( component.deleted )
					notFound();
				Security.check( user.in( component.project ).can( "AddTask" ) );
			}
			else
			{
				if( task_id != 0 ) // adding subtask to parent task with id
				// taskId
				{
					task = Task.findById( task_id );
					if( task != null && task.deleted )
						notFound();
					if( task.component != null )
					{
						Security.check( user.in( task.component.project ).can( "AddTask" ) );
						project = task.component.project;
					}
					else if( task.project != null )
					{
						Security.check( user.in( task.project ).can( "AddTask" ) );
						project = task.project;
					}

				}
			}
		}

		List<Sprint> sprints = new ArrayList<Sprint>();
		for( int i = 0; i < project.sprints.size(); i++ )
		{
			Sprint sprint = project.sprints.get( i );
			java.util.Date Start = sprint.startDate;
			Calendar cal = new GregorianCalendar();
			if( Start.after( cal.getTime() ) && !sprint.deleted )
			{
				sprints.add( sprint );
			}
		}

		String productRoles = "";
		for( int i = 0; i < project.productRoles.size(); i++ )
		{
			if( project.productRoles.get( i ).name.charAt( 0 ) == 'a' || project.productRoles.get( i ).name.charAt( 0 ) == 'e' || project.productRoles.get( i ).name.charAt( 0 ) == 'i' || project.productRoles.get( i ).name.charAt( 0 ) == 'o' || project.productRoles.get( i ).name.charAt( 0 ) == 'u' || project.productRoles.get( i ).name.charAt( 0 ) == 'A' || project.productRoles.get( i ).name.charAt( 0 ) == 'E' || project.productRoles.get( i ).name.charAt( 0 ) == 'I' || project.productRoles.get( i ).name.charAt( 0 ) == 'O' || project.productRoles.get( i ).name.charAt( 0 ) == 'U' )
				productRoles = productRoles + "As an " + project.productRoles.get( i ).name + ",-";
			else
				productRoles = productRoles + "As a " + project.productRoles.get( i ).name + ",-";
		}

		try
		{
			render( project, p, component, task, type, sprints, productRoles, project_id, component_id, task_id );

		}
		catch( TemplateNotFoundException e )
		{
			render( "CRUD/blank.html", type );
		}

	}

	/**
	 * Overrides the CRUD create method that is invoked to submit the creation
	 * of the task on the database.
	 * 
	 * @param void
	 * @throws Exception
	 * @return void
	 */
	public static void create() throws Exception
	{
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		JPASupport object = type.entityClass.newInstance();
		validation.valid( object.edit( "object", params ) );
		Task tmp = (Task) object;
		User user = Security.getConnected();
		Project project = null;
		Component component = null;
		Task task = null;
		Project p = null;
		if( tmp.project != null && tmp.parent == null )
		{
			p = tmp.project;
			project = tmp.project;
			Security.check( Security.getConnected().in( tmp.project ).can( "AddTask" ) );

		}
		else
		{
			if( tmp.parent != null )
			{
				task = tmp.parent;
				project = tmp.project;
			}
			else
			{
				component = tmp.component;
				project = component.project;
				tmp.project = project;
				Security.check( user.in( component.project ).can( "AddTask" ) );
			}
		}
		List<Sprint> sprints = new ArrayList<Sprint>();
		for( int i = 0; i < project.sprints.size(); i++ )
		{
			Sprint sprint = project.sprints.get( i );
			java.util.Date Start = sprint.startDate;
			Calendar cal = new GregorianCalendar();
			if( Start.after( cal.getTime() ) && !sprint.deleted )
			{
				sprints.add( sprint );
			}
		}
		String productRoles = "";
		for( int i = 0; i < project.productRoles.size(); i++ )
		{
			if( project.productRoles.get( i ).name.charAt( 0 ) == 'a' || project.productRoles.get( i ).name.charAt( 0 ) == 'e' || project.productRoles.get( i ).name.charAt( 0 ) == 'i' || project.productRoles.get( i ).name.charAt( 0 ) == 'o' || project.productRoles.get( i ).name.charAt( 0 ) == 'u' || project.productRoles.get( i ).name.charAt( 0 ) == 'A' || project.productRoles.get( i ).name.charAt( 0 ) == 'E' || project.productRoles.get( i ).name.charAt( 0 ) == 'I' || project.productRoles.get( i ).name.charAt( 0 ) == 'O' || project.productRoles.get( i ).name.charAt( 0 ) == 'U' )
				productRoles = productRoles + "As an " + project.productRoles.get( i ).name + ",-";
			else
				productRoles = productRoles + "As a " + project.productRoles.get( i ).name + ",-";
		}
		if( validation.hasErrors() )
		{
			renderArgs.put( "error", Messages.get( "crud.hasErrors" ) );
			try
			{
				render( request.controller.replace( ".", "/" ) + "/blank.html", project, p, component, task, type, sprints, productRoles );
			}
			catch( TemplateNotFoundException e )
			{
				render( "CRUD/blank.html", type );
			}
		}
		tmp.init();
		tmp.getProductRole( tmp.description );
		tmp.reporter = Security.getConnected();
		Double t = tmp.estimationPoints;
		if( t.isNaN() )
		{
			tmp.estimationPoints = 0.0;
		}
		if( tmp.parent != null )
		{
			double sum = 0;
			for( int i = 0; i < tmp.parent.subTasks.size(); i++ )
			{
				if( !tmp.parent.subTasks.get( i ).deleted )
				{
					sum = tmp.parent.subTasks.get( i ).estimationPoints + sum;
				}
			}
			tmp.parent.estimationPoints = sum + tmp.estimationPoints;
			tmp.parent.save();
		}
		for( int i = 0; i < tmp.estimationPointsPerDay.size(); i++ )
		{
			tmp.estimationPointsPerDay.set( i, tmp.estimationPoints );
		}
		object.save();
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + tmp.project.id + "&isOverlay=false&url=/tasks/view_task?task_id=" + tmp.id;
		ArrayList<User> users = new ArrayList<User>();
		if( tmp.assignee != null )
			users.add( tmp.assignee );
		if( tmp.reviewer != null )
			users.add( tmp.reviewer );
		Notifications.notifyUsers( users, "added", url, "task", "task " + tmp.number, (byte) 0, tmp.project );
		Log.addUserLog( "Created new task", tmp, tmp.project );
		flash.success( Messages.get( "crud.created", type.modelName, object.getEntityId() ) );
		if( params.get( "_save" ) != null )
		{

			if( tmp.parent != null )
			{
				CollaborateUpdate.update( tmp.project, "reload('tasks-" + tmp.project.id + "','task-" + tmp.parent.id + "')" );
				Application.overlay_killer( "", "" );
			}
			else
			{
				CollaborateUpdate.update( tmp.project, "reload('tasks-" + tmp.project.id + "','task-" + tmp.id + "')" );
				Application.overlay_killer( "", "" );
			}

		}
		if( params.get( "_saveAndAddAnother" ) != null )
		{
			redirect( request.controller + ".blank" );
		}
		redirect( request.controller + ".show", object.getEntityId() );
	}

	/**
	 * Overrides the CRUD show method that renders the edit form.
	 * 
	 * @author Monayri
	 * @param id
	 *            the task been edited id.
	 * @return void
	 */
	public static void show( String id )
	{
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		JPASupport object = type.findById( id );
		Task tmp = (Task) object;
		Security.check( Security.getConnected().in( tmp.project ).can( "modifyTask" ) || Security.getConnected() == tmp.assignee || Security.getConnected() == tmp.reviewer );
		List<TaskStatus> statuses = tmp.project.taskStatuses;
		List<TaskType> types = tmp.project.taskTypes;
		List<Comment> comments = Comment.find( "byTask", tmp ).fetch();
		if( comments == null )
			comments = new ArrayList<Comment>();
		String message2 = "Are you Sure you want to delete the task ?!";
		boolean deletable = tmp.isDeletable();
		String productRoles = "";
		for( int i = 0; i < tmp.project.productRoles.size(); i++ )
		{
			if( tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'a' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'e' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'i' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'o' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'u' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'A' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'E' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'I' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'O' || tmp.project.productRoles.get( i ).name.charAt( 0 ) == 'U' )
				productRoles = productRoles + "As an " + tmp.project.productRoles.get( i ).name + ",-";
			else
				productRoles = productRoles + "As a " + tmp.project.productRoles.get( i ).name + ",-";
		}
		boolean insprint = false;
		Date now = Calendar.getInstance().getTime();
		if( tmp.sprint != null )
		{
			if( tmp.sprint.startDate.before( now ) && tmp.sprint.endDate.after( now ) )
			{
				insprint = true;
			}
		}
		List<Sprint> sprints = new ArrayList<Sprint>();
		for( Sprint sprint : tmp.project.sprints )
		{
			java.util.Date Start = sprint.startDate;
			Calendar cal = new GregorianCalendar();
			if( Start.after( cal.getTime() ) && !sprint.deleted )
			{
				sprints.add( sprint );
			}
		}
		TaskType taskType = tmp.type;
		Component component = tmp.component;
		List<Reviewer> reviewers = new ArrayList();
		List<TaskType> projectTypes = TaskType.find( "byProjectAndDeleted", tmp.project, false ).fetch();
		if( taskType != null )
			reviewers = Reviewer.find( "byProjectAndAcceptedAndtaskType", tmp.project, true, taskType ).fetch();
		else if( projectTypes.size() > 0 )
			reviewers = Reviewer.find( "byProjectAndAcceptedAndtaskType", tmp.project, true, projectTypes.get( 0 ) ).fetch();
		List<User> users = new ArrayList<User>();
		for( Reviewer rev : reviewers )
		{
			if( component != null && component.number != 0 )
			{
				if( rev.user.components.contains( component ) && ((tmp.assignee == null) || (tmp.assignee != null && rev.user.id != tmp.assignee.id)) )
					users.add( rev.user );
			}
			else
			{
				if( (tmp.assignee == null) || (tmp.assignee != null && rev.user.id != tmp.assignee.id) )
					users.add( rev.user );
			}
		}
		try
		{
			render( type, object, statuses, types, message2, deletable, comments, productRoles, insprint, sprints, users );
		}
		catch( TemplateNotFoundException e )
		{
			render( "CRUD/show.html", type, object );
		}
	}

	/**
	 * Overrides the CRUD save method that is invoked to submit the edit, in
	 * order to check if the edits are acceptable.
	 * 
	 * @author Monayri
	 * @param id
	 *            the id of the task being edited.
	 * @throws Exception
	 * @return void
	 */
	public static void save( String id ) throws Exception
	{
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		JPASupport object = type.findById( id );
		String changes = "";
		Task tmp = (Task) object;
		Security.check( Security.getConnected().in( tmp.project ).can( "modifyTask" ) || Security.getConnected() == tmp.assignee || Security.getConnected() == tmp.reviewer );
		List<User> users = tmp.component.users;
		List<TaskStatus> statuses = tmp.project.taskStatuses;
		List<TaskType> types = tmp.project.taskTypes;
		List<Task> dependencies = Task.find( "byProjectAndDeleted", tmp.project, false ).fetch();
		List<Comment> comments = Comment.find( "byTask", tmp ).fetch();
		String message2 = "Are you Sure you want to delete the task ?!";
		boolean deletable = tmp.isDeletable();
		String oldDescription = tmp.description;// done
		long oldComponent = 0;
		if( tmp.component != null )
			oldComponent = tmp.component.id;

		long oldTaskType;
		if( tmp.type != null )
			oldTaskType = tmp.type.id;
		else
			oldTaskType = 0;
		long oldTaskStatus;
		if( tmp.status != null )
			oldTaskStatus = tmp.status.id;// done
		else
			oldTaskStatus = 0;

		double oldEstPoints = tmp.estimationPoints;
		if( !tmp.subTasks.isEmpty() )
		{
			double sum = 0;
			for( int i = 0; i < tmp.subTasks.size(); i++ )
			{
				sum = tmp.subTasks.get( i ).estimationPoints + sum;
			}
			tmp.estimationPoints = sum;
		}
		long oldAssignee;
		if( tmp.assignee != null )
			oldAssignee = tmp.assignee.id;// done
		else
			oldAssignee = 0;
		long oldReviewer;
		if( tmp.reviewer != null )
			oldReviewer = tmp.reviewer.id;// done
		else
			oldReviewer = 0;
		ArrayList<Task> oldDependencies = new ArrayList<Task>();
		for( Task current : tmp.dependentTasks )
		{
			oldDependencies.add( current );
		}
		object = object.edit( "object", params );
		validation.valid( object );
		if( validation.hasErrors() )
		{
			renderArgs.put( "error", Messages.get( "crud.hasErrors" ) );
			try
			{
				render( request.controller.replace( ".", "/" ) + "/show.html", type, object, users, statuses, types, dependencies, message2, deletable, comments );
			}
			catch( TemplateNotFoundException e )
			{
				render( "CRUD/show.html", type, object );
			}
		}
		object.save();
		if( tmp.parent != null )
		{
			double sum = 0;
			for( int i = 0; i < tmp.parent.subTasks.size(); i++ )
			{
				if( !tmp.parent.subTasks.get( i ).deleted )
				{
					sum = tmp.parent.subTasks.get( i ).estimationPoints + sum;
				}
			}
			tmp.parent.estimationPoints = sum;
			tmp.parent.save();
		}
		/*********** Changes as Comment by Galal Aly **************/
		tmp.productRole = null;
		tmp.getProductRole( tmp.description );
		// start resetting the deadline
		if( tmp.assignee == null || oldAssignee != tmp.assignee.id )
		{
			tmp.deadline = 0;
		}
		// end resetting the deadline
		tmp.save();
		if( !(tmp.description.equals( oldDescription )) )
			changes += "Description changed from <i>" + oldDescription + "</i> to <i>" + tmp.description + "</i><br>";
		if( tmp.type != null && oldTaskType != 0 )
			if( tmp.type.id != oldTaskType )
			{
				TaskType temp = TaskType.findById( oldTaskType );
				changes += "Task's Type was changed from <i>" + temp.name + "</i> to <i>" + tmp.type.name + "</i><br>";
			}
		if( tmp.status != null && oldTaskStatus != 0 )
			if( tmp.status.id != oldTaskStatus )
			{
				TaskStatus temp = TaskStatus.findById( oldTaskStatus );
				changes += "Task's status was changed from <i>" + temp.name + "</i> to <i>" + tmp.status.name + "</i><br>";
			}
		if( tmp.estimationPoints != oldEstPoints )
			changes += "Estimation points for the task were changed from <i>" + oldEstPoints + "</i> to <i>" + tmp.estimationPoints + "</i><br>";
		if( tmp.assignee != null && oldAssignee != 0 )
		{
			if( tmp.assignee.id != oldAssignee )
			{
				User temp = User.findById( oldAssignee );
				changes += "Task's assignee was changed from <i>" + temp.name + "</i> to <i>" + tmp.assignee.name + "</i><br>";
			}
		}
		else if( tmp.assignee != null && oldAssignee == 0 )
		{
			changes += "Task's assignee is now <i>" + tmp.assignee.name + "</i><br>";
		}
		else if( tmp.assignee == null && oldAssignee != 0 )
		{
			changes += "Task's assignee was removed<br>";
		}
		if( tmp.reviewer != null && oldReviewer != 0 )
		{
			if( tmp.reviewer.id != oldReviewer )
			{
				User temp = User.findById( oldReviewer );
				changes += "Task's reviewer was changed from <i>" + temp.name + "</i> to <i>" + tmp.reviewer.name + "</i><br>";
			}
		}
		else if( tmp.reviewer != null && oldReviewer == 0 )
		{
			changes += "Task's reviewer is now <i>" + tmp.reviewer.name + "</i><br>";
		}
		else if( tmp.reviewer == null && oldReviewer != 0 )
		{
			changes += "Task's reviewer was removed.<br>";
		}
		for( Task oldTask : oldDependencies )
		{
			if( !(tmp.dependentTasks.contains( oldTask )) )
			{
				changes += "Task " + oldTask.number + " was removed from Dependent tasks.<br>";
			}
		}
		for( Task newTask : tmp.dependentTasks )
		{
			if( !(oldDependencies.contains( newTask )) )
			{
				changes += "Task " + newTask.number + " was added to dependent tasks.<br>";
			}
		}
		if( tmp.component != null && oldComponent != 0 )
		{
			if( tmp.component.id != oldComponent )
			{
				Component c = Component.findById( oldComponent );
				changes += "Component changed from <i>" + c.name + "</i> to <i>" + tmp.component.name + "</i><br>";
			}
		}
		else if( tmp.component != null && oldComponent == 0 )
		{
			changes += "Task's component is now <i>" + tmp.component.name + "</i><br>";
		}

		// Now finally save the comment
		if( !changes.equals( "" ) )
		{
			changes = "<font color=\"green\">" + changes;
			changes = changes + "</font>";
			Comment changesComment = new Comment( Security.getConnected(), tmp.id, changes );
			changesComment.save();
		}
		// /********** End of Changes as Comment ********/
		if( tmp.comment != null && tmp.comment.trim().length() != 0 )
		{
			Comment comment = new Comment( Security.getConnected(), tmp.id, tmp.comment );
			comment.save();
		}

		boolean flag = true;
		Date now = Calendar.getInstance().getTime();
		if( tmp.sprint != null )
		{
			if( tmp.sprint.startDate.before( now ) && tmp.sprint.endDate.after( now ) )
			{
				flag = true;
			}
		}
		long compId = 0;
		if( tmp.component != null )
			compId = tmp.component.id;
		if( tmp.sprint != null && (!(tmp.description.equals( oldDescription )) || (tmp.assignee != null && oldAssignee != 0 && tmp.assignee.id != oldAssignee) || (tmp.reviewer != null && oldReviewer != 0 && tmp.reviewer.id != oldReviewer) || (tmp.type != null && oldTaskType != 0 && tmp.type.id != oldTaskType)) )
		{
			CollaborateUpdate.update( Security.getConnected(), "reload_note_open(" + tmp.sprint.id + "," + tmp.id + "," + compId + ",0)" );
			CollaborateUpdate.update( tmp.project.users, Security.getConnected(), "reload_note_close(" + tmp.sprint.id + "," + tmp.id + "," + compId + ");sprintLoad(" + tmp.id + ",'" + tmp.id + "_des')" );
		}

		if( tmp.sprint != null && (tmp.component != null && (tmp.assignee != null && oldAssignee != 0 && tmp.assignee.id != oldAssignee)) )
		{
			CollaborateUpdate.update( tmp.project, "drag_note_assignee(" + tmp.sprint.id + "," + oldAssignee + "," + tmp.assignee.id + "," + tmp.status.id + "," + compId + "," + tmp.id + ")" );
			CollaborateUpdate.update( Security.getConnected(), "reload_note_open(" + tmp.sprint.id + "," + tmp.id + "," + compId + ",0)" );
			CollaborateUpdate.update( tmp.project.users, Security.getConnected(), "reload_note_close(" + tmp.sprint.id + "," + tmp.id + "," + compId + ")" );
		}
		if( tmp.sprint != null && tmp.status != null && oldTaskStatus != 0 && tmp.status.id != oldTaskStatus )
		{
			CollaborateUpdate.update( tmp.project, "drag_note_status(" + tmp.sprint.id + "," + tmp.assignee.id + "," + oldTaskStatus + "," + tmp.status.id + "," + compId + "," + tmp.id + ")" );
		}

		for( Task subTask : tmp.subTasks )
		{
			subTask.component = tmp.component;
			subTask.assignee = tmp.assignee;
			subTask.reviewer = tmp.reviewer;
			subTask.status = tmp.status;
			subTask.sprint = tmp.sprint;
			subTask.save();
		}

		flash.success( Messages.get( "crud.saved", type.modelName, object.getEntityId() ) );
		if( params.get( "_save" ) != null )
		{
			CollaborateUpdate.update( tmp.project, "reload('tasks','task-" + tmp.id + "'" + (tmp.parent != null ? ",'task-" + tmp.parent.id + "'" : "") + ")" );
			String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + tmp.project.id + "&isOverlay=false&url=/tasks/view_task?task_id=" + tmp.id;
			ArrayList<User> nusers = new ArrayList<User>();
			if( tmp.assignee != null )
				nusers.add( tmp.assignee );
			if( tmp.reviewer != null )
				nusers.add( tmp.reviewer );
			if( tmp.reporter != null )
				nusers.add( tmp.reporter );
			User oldAssign = User.findById( oldAssignee );
			User oldrev = User.findById( oldAssignee );
			if( oldAssign != null )
			{
				if( !oldAssign.equals( tmp.assignee ) )
					nusers.add( oldAssign );
			}
			if( oldrev != null )
			{
				if( !oldrev.equals( tmp.reviewer ) )
					nusers.add( oldrev );
			}
			Notifications.notifyUsers( nusers, "edited", url, "task", "task " + tmp.number, (byte) 0, tmp.project );
			Log.addUserLog( "Edit task", tmp, tmp.project );
			Application.overlay_killer( "", "" );
			// Logs.addLog( tmp.project, "edit", "Task", tmp.id );
		}
	}

	/**
	 * Overrides the CRUD delete method that is invoked to delete a task, in
	 * order to delete the task by setting the deleted boolean variable to true
	 * instead of deleting it from the data base.
	 * 
	 * @author Monayri
	 * @param id
	 *            the task id.
	 * @return void
	 */
	public static void delete( long id )
	{
		Task tmp = Task.findById( id );
		Security.check( Security.getConnected().in( tmp.project ).can( "modifyTask" ) );
		try

		{
			tmp.DeleteTask();
			tmp.save();
			if( tmp.parent != null )
			{
				double sum = 0;
				for( int i = 0; i < tmp.parent.subTasks.size(); i++ )
				{
					if( !tmp.parent.subTasks.get( i ).deleted )
					{
						sum = tmp.parent.subTasks.get( i ).estimationPoints + sum;
					}
				}
				tmp.parent.estimationPoints = sum;
				tmp.parent.save();
				CollaborateUpdate.update( tmp.project, "reload('tasks-" + tmp.project.id + "', 'task-" + tmp.id + "', 'task-" + tmp.parent.id + "', 'tasks')" );
			}
			CollaborateUpdate.update( tmp.project, "reload('tasks-" + tmp.project.id + "', 'task-" + tmp.id + "', 'tasks')" );
			delete_sub_tasks( id );
			renderText( "Task deleted successfully." );

		}
		catch( Exception e )
		{
			flash.error( Messages.get( "crud.delete.error", "Task", tmp.getEntityId() ) );
		}

	}

	/**
	 * Returns a list of all the reviewers on a component
	 * 
	 * @param id
	 *            the component id.
	 * @param id2
	 *            the task assignee id.
	 * @return void
	 */
	public static void reviewers( long id, long id2, long compId, long projId )
	{
		List<User> users = new ArrayList<User>();
		Component component = Component.findById( compId );
		Project project = Project.findById( projId );
		User Assignee = User.findById( id2 );
		if( component != null )
		{
			if( component.number == 0 )
				users = component.project.users;
			else
				users = component.users;
		}
		else
		{
			users = project.users;
		}
		List<User.Object> reviewers = new ArrayList<User.Object>();
		for( User user : users )
		{
			if( user != Assignee )
			{
				reviewers.add( new User.Object( user.id, user.name ) );
			}
		}
		renderJSON( reviewers );
	}

	/**
	 * Sets a task to be dependent on another task.
	 * 
	 * @param id
	 *            the task id.
	 * @param id2
	 *            the task id.
	 * @return void
	 */
	public static void set_dependency( long id, long id2 )
	{
		Task taskFrom = Task.findById( id );
		Task taskTo = Task.findById( id2 );
		Security.check( Security.getConnected().in( taskFrom.project ).can( "modifyTask" ) );
		taskFrom.dependentTasks.add( taskTo );
		taskFrom.save();
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + taskFrom.project.id + "&isOverlay=false&url=/tasks/view_task?task_id=" + taskFrom.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( taskFrom.assignee != null )
			nusers.add( taskFrom.assignee );
		if( taskFrom.reviewer != null )
			nusers.add( taskFrom.reviewer );
		if( taskFrom.reporter != null )
			nusers.add( taskFrom.reporter );
		if( taskTo.assignee != null )
			nusers.add( taskTo.assignee );
		if( taskTo.reviewer != null )
			nusers.add( taskTo.reviewer );
		if( taskTo.reporter != null )
			nusers.add( taskTo.reporter );
		Notifications.notifyUsers( nusers, "added", url, "task " + taskTo.number + "to the dependent tasks on", "task " + taskFrom.number, (byte) 0, taskFrom.project );
	}

	/**
	 * Saves a specific effort of a given day for a certain task in a specific
	 * sprint. It also Notifies all the users in the corresponding component of
	 * the change and type of change. It also logs the change.
	 * 
	 * @author Hadeer Younis
	 * @param id
	 *            The id of the task to be updated.
	 * @param effort
	 *            The effort points of a specific day.
	 * @param day
	 *            The number of the day to which the effort belongs.
	 * @return void
	 */
	public static void enter_effort( long id, double effort, int day )
	{
		Task temp = Task.findById( id );
		Security.check( Security.getConnected().in( temp.project ).can( "modifyTask" ) || temp.assignee == Security.getConnected() );
		Double e = effort;
		if( e.isNaN() )
		{
			renderText( "Please enter a number!" );
		}
		else if( temp.estimationPoints < effort )
		{
			renderText( "The entered effort cannot be more than the estimated effort" );
		}
		else if( effort < 0 )
		{
			renderText( "The entered effort cannot be less than 0" );
		}

		temp.setEffortOfDay( effort, day );
		CollaborateUpdate.update( temp.project, "sprintLoad(" + id + ",'" + id + "_day_" + day + "');" );
		temp.save();
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + temp.project.id + "&isOverlay=false&url=/tasks/view_task?task_id=" + temp.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( temp.assignee != null )
			nusers.add( temp.assignee );
		if( temp.reviewer != null )
			nusers.add( temp.reviewer );
		if( temp.reporter != null )
			nusers.add( temp.reporter );
		Notifications.notifyUsers( nusers, "changed", url, "the effort points for the", "task " + temp.number, (byte) 0, temp.project );
		Log.addLog( "Effort entered for task", Security.getConnected(), temp, temp.project );
		renderText( "Effort changed successfully" );
	}

	/**
	 * Returns a the inputed date in the yyyy-mm-dd format
	 * 
	 * @author Hadeer Youni
	 * @param c
	 *            , A certain date
	 * @return
	 */
	public static String getStrDate( GregorianCalendar c )
	{
		int m = c.get( GregorianCalendar.MONTH ) + 1;
		int d = c.get( GregorianCalendar.DATE );
		String mm = Integer.toString( m );
		String dd = Integer.toString( d );
		return "" + c.get( GregorianCalendar.YEAR ) + '-' + (m < 10 ? "0" + mm : mm) + '-' + (d < 10 ? "0" + dd : dd);
	}

	/**
	 * Fetches all the data needed to generate a report on a given task.
	 * 
	 * @author Hadeer Younis
	 * @param id
	 *            , The id of the task whose report will be generated.
	 * @return void
	 */
	public static void report( long id )
	{
		Task task = Task.findById( id );
		List<Log> temp = task.logs;
		Security.check( task.project.users.contains( Security.getConnected() ) );
		if( task.deleted )
			notFound();
		boolean empty = temp.isEmpty();
		String last_modified = null;
		int number_of_modifications = 0;
		String efforts = "[";
		boolean flag = false;
		double n = task.getEffortPerDay( 0 );
		String changes = "[";
		if( task.sprint != null )
		{
			for( int j = 0; j < task.sprint.getDuration(); j++ )
			{
				if( !flag )
					n = task.getEffortPerDay( j );
				if( n == -1 )
				{
					flag = true;
					n = task.getEffortPerDay( j - 1 );
				}
				if( j == task.sprint.getDuration() - 1 )
					efforts = efforts + "[" + j + "," + n + "]]";
				else
					efforts = efforts + "[" + j + "," + n + "],";
			}
		}
		else
		{
			efforts = "[]";
		}
		for( int i = 0; i < temp.size(); i++ )
		{
			int k = 1;

			GregorianCalendar today = new GregorianCalendar();
			today.setTimeInMillis( temp.get( i ).timestamp );
			if( i < temp.size() - 1 )
			{
				GregorianCalendar tomorrow = new GregorianCalendar();
				tomorrow.setTimeInMillis( temp.get( i + 1 ).timestamp );

				keepLoop : while( getStrDate( today ).equals( getStrDate( tomorrow ) ) )
				{
					i++;
					k++;
					if( i == temp.size() - 1 )
						break keepLoop;

				}
			}
			if( i == temp.size() - 1 )
				changes = changes + "['" + getStrDate( today ) + "'," + k + "]]";
			else
				changes = changes + "['" + getStrDate( today ) + "'," + k + "],";

		}

		if( !empty )
		{
			last_modified = new Date( temp.get( temp.size() - 1 ).timestamp ).toString().substring( 0, 10 ) + " @ " + new Date( temp.get( temp.size() - 1 ).timestamp ).toString().substring( 11 );
			number_of_modifications = temp.size();
		}
		GregorianCalendar maxdate = new GregorianCalendar();
		maxdate.setTimeInMillis( temp.get( temp.size() - 1 ).timestamp );
		if( !temp.isEmpty() || temp.size() == 1 )
			maxdate.setTimeInMillis( temp.get( temp.size() - 1 ).timestamp + (3 * 86400000) );
		String maximum_date = getStrDate( maxdate );
		GregorianCalendar mindate = new GregorianCalendar();
		mindate.setTimeInMillis( temp.get( 0 ).timestamp );
		if( !temp.isEmpty() || temp.size() == 1 )
			mindate.setTimeInMillis( temp.get( 0 ).timestamp - (3 * 86400000) );
		String minimum_date = getStrDate( mindate );
		Project project = task.project;
		render( project, minimum_date, temp, empty, efforts, changes, task, maximum_date );
	}

	/**
	 * This method changes the given task description.
	 * 
	 * @author Moumen Mohamed
	 * @param id
	 *            , The id of the given task.
	 * @param userId
	 *            , The id of the user who will do the change in description.
	 * @param desc
	 *            , The new description.
	 * @return boolean
	 */
	public static boolean edit_task_description( long id, long user_id, String description )
	{
		Task task1 = Task.findById( id );
		Security.check( Security.getConnected().in( task1.project ).can( "modifyTask" ) || task1.assignee == Security.getConnected() );
		if( task1 == null )
			return false;
		if( user_id == 0 )
		{
			user_id = Security.getConnected().id;
		}
		User user1 = User.findById( user_id );
		Project currentProject = task1.project;
		boolean permession = user1.in( currentProject ).can( "changeTaskDescreption" );

		if( task1.reviewer != null && task1.reviewer.id != user_id && task1.assignee != null && task1.assignee.id != user_id )
		{
			if( !permession )
				return false;
		}
		task1.description = description;
		task1.save();
		long compId = 0;
		if( task1.component != null )
			compId = task1.component.id;
		if( task1.sprint != null )
		{
			CollaborateUpdate.update( Security.getConnected(), "reload_note_open(" + task1.sprint.id + "," + task1.id + "," + compId + "," + user_id + ")" );
			CollaborateUpdate.update( task1.project.users, Security.getConnected(), "reload_note_close(" + task1.sprint.id + "," + task1.id + "," + compId + ");sprintLoad(" + task1.id + ",'" + task1.id + "_des')" );
		}
		if( user_id == Security.getConnected().id )
		{
			Log.addUserLog( "Edited task description", task1, task1.project );
		}
		else
		{
			Log.addUserLog( user1.name + " has edited task description", task1, user1, task1.project );
		}
		CollaborateUpdate.update( task1.project, "reload('tasks','task-" + task1.id + "')" );
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + task1.project.id + "&isOverlay=false&url=/tasks/view_task?task_id=" + task1.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( task1.assignee != null )
			nusers.add( task1.assignee );
		if( task1.reviewer != null )
			nusers.add( task1.reviewer );
		if( task1.reporter != null )
			nusers.add( task1.reporter );
		Notifications.notifyUsers( nusers, "changed", url, "the description of the", "task " + task1.number, (byte) 0, task1.project );
		renderText( "The description was changed successfully" );
		return true;
	}

	/**
	 * Changes the given task status.
	 * 
	 * @author Moumen Mohamed
	 * @param id
	 *            The id of the given task.
	 * @param newStatus
	 *            The new task status.
	 * @param userId
	 *            The id of the user who will change the task status.
	 * @return boolean
	 */
	public static boolean edit_task_status2( long id, long userId, TaskStatus newStatus )
	{
		Task task1 = Task.findById( id );
		if( task1 == null )
			return false;
		if( userId == 0 )
		{
			userId = Security.getConnected().id;
		}
		User user1 = User.findById( userId );
		if( user1 == null )
			return false;
		Security.check( user1.in( task1.project ).can( "modifyTask" ) || task1.assignee == user1 );
		Project currentProject = task1.project;
		boolean permession = user1.in( currentProject ).can( "changeTaskStatus" );

		if( task1.reviewer != null && task1.reviewer.id != userId && task1.assignee != null && task1.assignee.id != userId )
		{
			if( !permession )
				return false;
		}
		long oldstatus = 0;
		if( task1.status != null )
			oldstatus = task1.status.id;
		task1.status = newStatus;
		task1.save();
		long newstatus = task1.status.id;
		long compId = 0;
		if( task1.component != null )
			compId = task1.component.id;
		if( task1.assignee != null )
			CollaborateUpdate.update( task1.project, "drag_note_status(" + task1.sprint.id + "," + task1.assignee.id + "," + oldstatus + "," + newstatus + "," + compId + "," + task1.id + ")" );
		else
			CollaborateUpdate.update( task1.project, "drag_note_status(" + task1.sprint.id + "," + 0 + "," + oldstatus + "," + newstatus + "," + compId + "," + task1.id + ")" );
		if( userId == Security.getConnected().id )
		{
			Log.addUserLog( "Edited task status", task1, task1.project );

		}
		else
		{
			Log.addUserLog( user1.name + " edited task status", user1, task1, task1.project );
		}
		CollaborateUpdate.update( task1.project, "reload('tasks','task-" + task1.id + "')" );
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + task1.project.id + "&isOverlay=false&url=/tasks/view_task?task_id=" + task1.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( task1.assignee != null )
			nusers.add( task1.assignee );
		if( task1.reviewer != null )
			nusers.add( task1.reviewer );
		if( task1.reporter != null )
			nusers.add( task1.reporter );
		Notifications.notifyUsers( nusers, "changed", url, "the status of the", "task " + task1.number, (byte) 0, task1.project );
		return true;
	}

	/**
	 * Changes the status of a given task to the specified status
	 * 
	 * @author Hadeer Younis
	 * @param id
	 *            , the id of the task to be edited
	 * @param statusId
	 *            , the id of the new status
	 */
	public static void edit_task_status( long id, long status_id )
	{
		Task task = Task.findById( id );
		TaskStatus stat = TaskStatus.findById( status_id );
		if( task == null )
			notFound();
		if( stat == null )
			notFound();
		Security.check( Security.getConnected().in( task.project ).can( "modifyTask" ) || task.assignee == Security.getConnected() );
		task.status.Tasks.remove( task );
		task.status.save();
		task.status = stat;
		if( task.parent != null )
		{
			boolean flag = true;
			for( int i = 0; i < task.parent.subTasks.size(); i++ )
			{
				if( task.parent.subTasks.get( i ).status != stat )
					flag = false;
			}
			if( flag )
			{
				task.parent.status = stat;
				task.parent.save();
			}
			CollaborateUpdate.update( task.project, "sprintLoad(" + task.parent.id + ",'" + task.parent.id + "_status');" );
		}
		if( task.subTasks.size() > 0 )
		{
			for( int i = 0; i < task.subTasks.size(); i++ )
			{
				task.subTasks.get( i ).status = stat;
				task.subTasks.get( i ).save();
				CollaborateUpdate.update( task.project, "sprintLoad(" + task.subTasks.get( i ).id + ",'" + task.subTasks.get( i ).id + "_status');" );

			}
		}
		CollaborateUpdate.update( task.project, "reload('task-" + id + "');" );
		CollaborateUpdate.update( task.project, "sprintLoad(" + id + ",'" + id + "_status');" );
		task.save();
		renderText( "Status updates successfully" );
		Log.addUserLog( "Edited task status", task, task.project );
	}

	/**
	 * Changes the given task estimation points.
	 * 
	 * @author Moumen Mohamed
	 * @param id
	 *            The id of the given task.
	 * @param estimation
	 *            The value of the new estimation.
	 * @return boolean
	 */
	public static boolean edit_task_points( long id, double estimation )
	{
		Task task = Task.findById( id );
		Security.check( Security.getConnected().in( task.project ).can( "modifyTask" ) || task.assignee == Security.getConnected() );
		if( task == null )
			notFound();
		if( estimation < 0 )
		{
			renderText( "Please enter a number more than 0" );
		}
		Double e = estimation;
		if( e.isNaN() )
		{
			renderText( "Please enter a number!" );
		}
		task.estimationPoints = estimation;
		task.save();

		CollaborateUpdate.update( task.project, "reload('task-" + id + "');" );
		CollaborateUpdate.update( task.project, "sprintLoad(" + id + ",'" + id + "_points');" );
		if( task.parent != null )
		{
			task.parent.estimationPoints = 0;
			for( int i = 0; i < task.parent.subTasks.size(); i++ )
			{
				task.parent.estimationPoints += task.parent.subTasks.get( i ).estimationPoints;
			}
			task.parent.save();
			CollaborateUpdate.update( task.project, "sprintLoad(" + task.parent.id + ",'" + task.parent.id + "_points');" );
		}
		Log.addUserLog( "Edit task estimation", task, task.project );
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + task.project.id + "&isOverlay=false&url=/tasks/view_task?task_id=" + task.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( task.assignee != null )
			nusers.add( task.assignee );
		if( task.reviewer != null )
			nusers.add( task.reviewer );
		if( task.reporter != null )
			nusers.add( task.reporter );
		Notifications.notifyUsers( nusers, "changed", url, "the estimation points of the", "task " + task.number, (byte) 0, task.project );
		renderText( "The task's total points was updated successfully" );
		return true;
	}

	/**
	 * This method changes the given task assignee.
	 * 
	 * @author Moumen Mohamed
	 * @param id
	 *            The id of the given task.
	 * @param userId
	 *            The id of the user who will do the change.
	 * @param assigneId
	 *            The id of the user who will be the assignee of the task.
	 * @return boolean
	 */
	public static boolean edit_task_assignee( long id, long user_id, long assignee_id )
	{
		Task task1 = Task.findById( id );
		if( task1 == null )
			return false;
		if( user_id == 0 )
		{
			user_id = Security.getConnected().id;
		}
		User user1 = User.findById( user_id );
		if( user1 == null )
			return false;
		Security.check( user1.in( task1.project ).can( "modifyTask" ) || task1.assignee == user1 );

		User oldAssignee = task1.assignee;
		User assignee = User.findById( assignee_id );
		if( assignee == null )
			return false;
		if( task1.reviewer != null && task1.reviewer.getId() == assignee_id )
			return false;

		Project currentProject = task1.project;
		boolean permession = user1.in( currentProject ).can( "changeAssignee" );

		if( !permession )
			return false;
		// String oldAssignee = task1.assignee.name;

		User oldassi = task1.assignee;

		task1.assignee = assignee;
		if( oldAssignee != null && !oldAssignee.equals( assignee ) )
		{
			task1.deadline = 0;
		}
		task1.save();

		long newassi = task1.assignee.id;

		long compId = 0;
		if( task1.component != null )
			compId = task1.component.id;
		assignee.tasks.add( task1 );
		assignee.save();
		if( task1.sprint != null )
		{
			if( compId != 0 )

			{
				if( oldassi != null )
					CollaborateUpdate.update( task1.project, "drag_note_assignee(" + task1.sprint.id + "," + oldassi.id + "," + newassi + "," + task1.status.id + "," + compId + "," + task1.id + ")" );
				else
					CollaborateUpdate.update( task1.project, "drag_note_assignee(" + task1.sprint.id + "," + 0 + "," + newassi + "," + task1.status.id + "," + compId + "," + task1.id + ")" );
				CollaborateUpdate.update( Security.getConnected(), "reload_note_open(" + task1.sprint.id + "," + task1.id + "," + compId + "," + user_id + ")" );
				CollaborateUpdate.update( task1.project.users, Security.getConnected(), "note_close(" + task1.sprint.id + "," + task1.id + "," + compId + ")" );
			}
			else

			{
				CollaborateUpdate.update( Security.getConnected(), "reload_note_open(" + task1.sprint.id + "," + task1.id + "," + compId + "," + user_id + ")" );
				CollaborateUpdate.update( task1.project.users, Security.getConnected(), "reload_note_close(" + task1.sprint.id + "," + task1.id + "," + compId + ")" );
			}
		}
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + task1.project.id + "&isOverlay=false&url=/tasks/view_task?task_id=" + task1.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( task1.assignee != null )
			nusers.add( task1.assignee );
		if( task1.reviewer != null )
			nusers.add( task1.reviewer );
		if( task1.reporter != null )
			nusers.add( task1.reporter );
		if( oldassi != null )
			nusers.add( oldassi );
		Notifications.notifyUsers( nusers, "changed", url, "the assignee of the", "task " + task1.number, (byte) 0, task1.project );
		if( user_id == Security.getConnected().id )
		{
			Log.addUserLog( "Edited task assignee", task1, task1.project );
			// Logs.addLog( user1, "Edit", "Task Assignee", id, task1.project,
			// new Date( System.currentTimeMillis() ) );
		}
		else
		{
			Log.addUserLog( user1.name + " has edited task assignee", user1, task1, task1.project );
			// Logs.addLog( user1 +
			// " has performed action (Edit) using resource (Task Assignee) in project "
			// + task1.project.name + " from the account of " +
			// Security.getConnected().name );
		}
		CollaborateUpdate.update( task1.project, "reload('tasks','task-" + task1.id + "')" );
		return true;
	}

	/**
	 * Changes the given task reviewer.
	 * 
	 * @author Moumen Mohamed
	 * @param id
	 *            The id of the given task.
	 * @param userId
	 *            The id of the user who will be doing the change.
	 * @param reviewerId
	 *            The id of the user who will be the reviewer of the task.
	 * @return boolean
	 */
	public static boolean edit_task_reviewer( long id, long user_id, long reviewer_id )
	{
		Task task1 = Task.findById( id );
		if( user_id == 0 )
		{
			user_id = Security.getConnected().id;
		}
		User user1 = User.findById( user_id );
		if( user1 == null )
			return false;
		Security.check( user1.in( task1.project ).can( "modifyTask" ) || task1.assignee == user1 );
		if( task1 == null )
			return false;
		User reviewer = User.findById( reviewer_id );
		if( reviewer == null )
			return false;
		if( task1.assignee.getId() == reviewer_id )
			return false;

		Project currentProject = task1.project;
		boolean permession = user1.in( currentProject ).can( "changeReviewer" );

		if( !permession )
			return false;
		User oldReviewer = task1.reviewer;
		task1.reviewer = reviewer;
		task1.save();
		long compId = 0;
		if( task1.component != null )
			compId = task1.component.id;
		if( task1.sprint != null )
		{
			CollaborateUpdate.update( Security.getConnected(), "reload_note_open(" + task1.sprint.id + "," + task1.id + "," + compId + "," + user_id + ")" );
			CollaborateUpdate.update( task1.project.users, Security.getConnected(), "reload_note_close(" + task1.sprint.id + "," + task1.id + "," + compId + ")" );
		}
		reviewer.tasks.add( task1 );
		reviewer.save();
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + task1.project.id + "&isOverlay=false&url=/tasks/view_task?task_id=" + task1.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( task1.assignee != null )
			nusers.add( task1.assignee );
		if( task1.reviewer != null )
			nusers.add( task1.reviewer );
		if( task1.reporter != null )
			nusers.add( task1.reporter );
		if( oldReviewer != null )
			nusers.add( oldReviewer );
		Notifications.notifyUsers( nusers, "changed", url, "the reviewer of the", "task " + task1.number, (byte) 0, task1.project );
		if( user_id == Security.getConnected().id )
		{
			Log.addUserLog( "Edit task reviewer", task1, task1.project );
			// Logs.addLog( user1, "Edit", "Task Reviewer", id, task1.project,
			// new Date( System.currentTimeMillis() ) );
		}
		else
		{
			Log.addUserLog( user1.name + " has edited task reviewer", user1, task1, task1.project );
			// Logs.addLog( user1 +
			// " has performed action (Edit) using resource (Task Reviewer) in project "
			// + task1.project.name + " from the account of " +
			// Security.getConnected().name );
		}
		CollaborateUpdate.update( task1.project, "reload('tasks','task-" + task1.id + "')" );
		return true;
	}

	/**
	 * This method changes the given task type.
	 * 
	 * @author Moumen Mohamed
	 * @param id
	 *            The id of the given task.
	 * @param type
	 *            The new Task Type.
	 * @param userId
	 *            The id of the user who will change the task Type.
	 * @return boolean
	 */
	public static boolean edit_task_type( long id, long type_id, long user_id )
	{
		Task task1 = Task.findById( id );
		if( user_id == 0 )
		{
			user_id = Security.getConnected().id;
		}
		User user1 = User.findById( user_id );
		if( user1 == null )
			return false;

		Security.check( user1.in( task1.project ).can( "modifyTask" ) || task1.assignee == user1 || task1.reviewer == user1 );
		if( task1 == null )
			return false;

		Project currentProject = task1.project;
		boolean permession = user1.in( currentProject ).can( "changeTaskType" );
		if( task1.reviewer != user1 && task1.assignee != user1 )
		{
			if( !permession )
				return false;
		}
		TaskType type = TaskType.findById( type_id );
		if( task1.subTasks.size() > 0 )
		{
			String n = "";
			for( int i = 0; i < task1.subTasks.size(); i++ )
			{
				task1.subTasks.get( i ).type = type;
				task1.subTasks.get( i ).save();
				n += "sprintLoad(" + task1.subTasks.get( i ).id + ",'" + task1.subTasks.get( i ).id + "_type');";
			}
			CollaborateUpdate.update( task1.project, n );

		}
		TaskType oldType = task1.type;
		if( task1.type != type )
			task1.reviewer = null;
		task1.type = type;
		task1.save();

		CollaborateUpdate.update( task1.project, "parent_message_bar('The task type was changed successfully');" );
		CollaborateUpdate.update( task1.project, "sprintLoad(" + id + ",'" + id + "_type');" );
		long compId = 0;
		if( task1.component != null )
			compId = task1.component.id;
		if( task1.sprint != null )
		{
			CollaborateUpdate.update( Security.getConnected(), "reload_note_open(" + task1.sprint.id + "," + task1.id + "," + compId + "," + user_id + ")" );
			CollaborateUpdate.update( task1.project.users, Security.getConnected(), "reload_note_close(" + task1.sprint.id + "," + task1.id + "," + compId + ")" );
		}
		if( task1.subTasks.size() > 0 )
		{
			for( int i = 0; i < task1.subTasks.size(); i++ )
			{
				task1.subTasks.get( i ).type = type;
				if( oldType != task1.type )
					task1.subTasks.get( i ).reviewer = null;
				task1.subTasks.get( i ).save();
			}
		}
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + task1.project.id + "&isOverlay=false&url=/tasks/view_task?task_id=" + task1.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( task1.assignee != null )
			nusers.add( task1.assignee );
		if( task1.reviewer != null )
			nusers.add( task1.reviewer );
		if( task1.reporter != null )
			nusers.add( task1.reporter );
		Notifications.notifyUsers( nusers, "changed", url, "the type of the", "task " + task1.number, (byte) 0, task1.project );

		CollaborateUpdate.update( task1.project, "reload('tasks','task-" + task1.id + "')" );

		return true;
	}


	/**
	 * Renders a list of the users on the component to select an assignee from
	 * them.
	 * 
	 * @author Dina Helal
	 * @param taskId
	 *            The task to be edited.
	 * @param compId
	 *            Component of the users.
	 */

	public static void choose_assignee( long task_id, long component_id, long user_id )
	{
		if( user_id == 0 )
		{
			user_id = Security.getConnected().id;
		}
		User user1 = User.findById( user_id );
		List<User> users = new ArrayList<User>();
		Task task = Task.findById( task_id );
		Component c = Component.findById( component_id );
		users = c.users;
		for( User u : users )
		{
			if( u.deleted )
				users.remove( u );
		}
		users.remove( task.reviewer );
		render( task, users, user1 );
	}

	/**
	 * Renders a list of the users on the component except the assignee to
	 * select an assignee from them.
	 * 
	 * @param task_id
	 *            Task id
	 * @param component_id
	 *            Component id
	 * @param user_id
	 *            User id
	 */
	public static void choose_reviewer( long task_id, long component_id, long user_id )
	{
		if( user_id == 0 )
		{
			user_id = Security.getConnected().id;
		}
		Task task = Task.findById( task_id );
		Component component = Component.findById( component_id );
		List<Reviewer> reviewers = new ArrayList();
		if( task.type != null )
			reviewers = Reviewer.find( "byProjectAndAcceptedAndtaskType", task.project, true, task.type ).fetch();

		List<User> users = new ArrayList<User>();
		for( Reviewer rev : reviewers )
		{
			if( component != null && component.number != 0 )
			{
				if( rev.user.components.contains( component ) && ((task.assignee == null) || (task.assignee != null && rev.user.id != task.assignee.id)) )
					users.add( rev.user );
			}
			else
			{
				if( (task.assignee == null) || (task.assignee != null && rev.user.id != task.assignee.id) )
					users.add( rev.user );
			}
		}
		User user1 = User.findById( user_id );
		users.remove( task.assignee );
		render( task, users, user1 );
	}

	/**
	 * Takes a task id, and renders it to a page to choose task type.
	 * 
	 * @author dina_helal
	 * @param taskId
	 *            The task id to be edited.
	 * @param userId
	 *            The user id who is editing the task.
	 * @return void
	 */
	public static void choose_type( long task_id, long user_id )
	{
		if( user_id == 0 )
		{
			user_id = Security.getConnected().id;
		}
		Task task = Task.findById( task_id );
		List<TaskType> types = TaskType.find( "byProjectAndDeleted", task.project, false ).fetch();
		render( task, types, user_id );
	}

	/**
	 * Takes renders to the view task(s) and the title and the project id and an
	 * indicator to list the tasks
	 * 
	 * @param project_id
	 *            The task'(s) project id.
	 * @param component_id
	 *            The task'(s) component id.
	 * @param mine
	 *            An indicator whether the list of my tasks or all project
	 *            tasks.
	 * @param meeting_id
	 *            The meeting id.
	 * @param task_id
	 *            The task id.
	 * @return void
	 */
	public static void view_task( long project_id, long component_id, int mine, long meeting_id, long task_id )
	{
		String title;
		if( component_id != 0 )
		{
			Component component = Component.findById( component_id );
			title = "C" + component.number + ": Tasks";
			List<Task> tasks = new ArrayList<Task>();
			tasks = Task.find( "byComponentAndDeleted", component, false ).fetch();
			int counter = tasks.size();
			for( int i = 0; i < tasks.size(); i++ )
			{

				if( tasks.contains( tasks.get( i ).parent ) )
				{
					tasks.remove( tasks.get( i ) );
					i = 0;
					counter--;
				}

			}
			boolean isComponent = true;
			project_id = component.project.id;
			render( counter, tasks, title, mine, project_id, isComponent );
		}
		else
		{
			if( task_id != 0 )
			{
				Task task = Task.findById( task_id );
				if( task.deleted )
					notFound();
				if( task.parent != null )
					title = "Task " + task.parent.number + "." + task.number;
				else
					title = "Task " + task.number;
				List<Task> tasks = new ArrayList<Task>();
				for( Task task2 : task.subTasks )
				{
					if( !task2.deleted )
						tasks.add( task2 );
				}
				int counter = tasks.size();
				project_id = task.project.id;
				render( counter, task, title, tasks, project_id );
			}
			else
			{
				if( mine == 1 )
				{
					title = "My Tasks";
					User user = Security.getConnected();
					Project project = Project.findById( project_id );
					List<Task> tasks = new ArrayList<Task>();
					for( Task task1 : project.projectTasks )
					{
						if( !task1.deleted && task1.assignee != null && task1.assignee.equals( user ) && task1.checkUnderImpl() && task1.status != null && !task1.status.closed )
						{
							tasks.add( task1 );
						}
					}
					for( Task task1 : project.projectTasks )
					{
						if( !task1.deleted && task1.reviewer != null && task1.reviewer.equals( user ) && task1.checkUnderImpl() && task1.status != null && task1.status.pending )
						{
							tasks.add( task1 );
						}
					}
					for( Task task1 : project.projectTasks )
					{
						if( !task1.deleted && task1.assignee != null && task1.assignee.equals( user ) && task1.status != null && !task1.status.closed && !tasks.contains( task1 ) )
						{
							tasks.add( task1 );
						}
					}
					for( Task task1 : project.projectTasks )
					{
						if( !task1.deleted && task1.reviewer != null && task1.reviewer.equals( user ) && task1.status != null && task1.status.pending && !tasks.contains( task1 ) )
						{
							tasks.add( task1 );
						}
					}
					int counter = tasks.size();
					render( counter, tasks, title, mine, project_id );
				}
				else
				{
					if( project_id != 0 )
					{
						title = "Project Tasks";
						Project project = Project.findById( project_id );
						List<Task> tasks = Task.find( "byProjectAndDeletedAndParentIsNull", project, false ).fetch();
						boolean isProject = true;
						int counter = tasks.size();
						render( counter, tasks, title, mine, project_id, isProject );
					}
					else
					{
						if( meeting_id != 0 )
						{
							Meeting meeting = Meeting.findById( meeting_id );
							List<Task> tasks = new ArrayList<Task>();
							for( Task task2 : meeting.tasks )
							{
								if( !task2.deleted )
								{
									tasks.add( task2 );
								}
							}

							title = "Meeting " + meeting.name + " Tasks";
							int counter = tasks.size();
							project_id = meeting.project.id;
							render( counter, title, tasks, project_id );
						}
					}
				}
			}
		}
	}

	/**
	 * Associates task to component.
	 * 
	 * @author mahmoudsakr
	 * @param taskId
	 *            The task id.
	 * @param componentId
	 *            The component id.
	 * @return void
	 */
	public static void associate_to_component( long task_id, long component_id )
	{
		Task task = Task.findById( task_id );
		Component component = Component.findById( component_id );
		User connected = Security.getConnected();
		boolean flag = false;
		Date now = Calendar.getInstance().getTime();
		if( task.sprint != null )
		{
			if( task.sprint.startDate.before( now ) && task.sprint.endDate.after( now ) )
			{
				flag = true;
			}
		}
		Security.check( connected.in( task.project ).can( "modifyTask" ) && task.project == component.project && task.component.project == component.project && task.parent == null && !flag );

		// first remove task from the component
		task.component.tasks.remove( task );
		task.component.save();

		task.component = component;
		task.save();
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + task.project.id + "&isOverlay=false&url=/components/viewthecomponent?componentId=" + component.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( task.assignee != null )
			nusers.add( task.assignee );
		if( task.reviewer != null )
			nusers.add( task.reviewer );
		if( task.reporter != null )
			nusers.add( task.reporter );
		for( User u : component.users )
		{
			if( !nusers.contains( u ) )
				nusers.add( u );
		}
		Notifications.notifyUsers( nusers, "associated", url, "task " + task.number + " to the component", component.get_full_name(), (byte) 0, task.project );
		Log.addUserLog( "Assigned task to component", task, component, component.project );
		CollaborateUpdate.update( task.project, "reload('component-" + component_id + "', 'task-" + task_id + "')" );
		renderText( "Associated successfully" );
	}

	/**
	 * Assigns a given user as an assignee for a given task.
	 * 
	 * @param taskId
	 *            The task id.
	 * @param assigneeId
	 *            The user id.
	 * @return void
	 */
	public static void assign_task_assignee( long task_id, long assignee_id )
	{
		Task task = Task.findById( task_id );
		User user = User.findById( assignee_id );
		User connected = Security.getConnected();
		if( task.reviewer == user )
			renderText( "The reviewer can't be the assignee" );
		if( task.component.number != 0 && !user.components.contains( task.component ) )
			renderText( "The task & the assignee can't be in different components" );
		Security.check( connected.in( task.project ).can( "modifyTask" ) && user.projects.contains( task.project ) && task.reviewer != user && (task.component == null || task.component.number == 0 || user.components.contains( task.component )) );
		task.assignee = user;
		task.save();

		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + task.project.id + "&isOverlay=false&url=/tasks/view_task?taskId=" + task.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( task.assignee != null )
			nusers.add( task.assignee );
		if( task.reviewer != null )
			nusers.add( task.reviewer );
		if( task.reporter != null )
			nusers.add( task.reporter );
		Notifications.notifyUsers( nusers, "assigned", url, user.name + " to the", "task " + task.number, (byte) 0, task.project );
		Log.addUserLog( "Assigned task assignee", task, user, task.project );
		CollaborateUpdate.update( task.project, "reload('task-" + task_id + "');" );
		if( task.subTasks.size() > 0 )
		{
			for( int i = 0; i < task.subTasks.size(); i++ )
			{
				task.subTasks.get( i ).assignee = user;
				task.subTasks.get( i ).save();
				CollaborateUpdate.update( task.project, "sprintLoad(" + task.subTasks.get( i ).id + ",'" + task.subTasks.get( i ).id + "_reviewer');" );
				CollaborateUpdate.update( task.project, "sprintLoad(" + task.subTasks.get( i ).id + ",'" + task.subTasks.get( i ).id + "_assignee');" );
			}
		}
		CollaborateUpdate.update( task.project, "sprintLoad(" + task_id + ",'" + task_id + "_reviewer');" );
		CollaborateUpdate.update( task.project, "sprintLoad(" + task_id + ",'" + task_id + "_assignee');" );
		renderText( "Assignee added successfully" );
	}

	/**
	 * Assigns a given user as a reviewer for a given task.
	 * 
	 * @param task_id
	 *            The task id.
	 * @param reviewer_id
	 *            The user id.
	 * @return void
	 */
	public static void assign_task_reviewer( long reviewer_id, long task_id )
	{
		Task task = Task.findById( task_id );
		User user = User.findById( reviewer_id );
		User connected = Security.getConnected();
		if( task.assignee == user )
			renderText( "The assignee can't be the reviewer" );
		if( task.component.number != 0 && !(user.components.contains( task.component )) )
			renderText( "The task & the reviewer can't be in different components" );
		Security.check( connected.in( task.project ).can( "modifyTask" ) && user.projects.contains( task.project ) && task.assignee != user && (task.component == null || task.component.number == 0 || user.components.contains( task.component )) && (task.type != null) );
		Component component = null;
		if( task.component != null && task.component.number != 0 )
			component = task.component;
		List<Reviewer> reviewers = new ArrayList();
		reviewers = Reviewer.find( "byProjectAndAcceptedAndtaskType", task.type.project, true, task.type ).fetch();
		List<User> users = new ArrayList<User>();
		for( Reviewer rev : reviewers )
		{
			if( component != null )
			{
				if( rev.user.components.contains( component ) )
					users.add( rev.user );
			}
			else
			{
				users.add( rev.user );
			}
		}
		if( users.contains( user ) )
		{
			task.reviewer = user;
			task.save();
		}
		else
		{
			renderText( user.name + " is not a reviewer for task type " + task.type.name );
		}
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + task.project.id + "&isOverlay=false&url=/tasks/view_task?task_id=" + task.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( task.assignee != null )
			nusers.add( task.assignee );
		if( task.reviewer != null )
			nusers.add( task.reviewer );
		if( task.reporter != null )
			nusers.add( task.reporter );
		Notifications.notifyUsers( nusers, "assigned", url, user.name + " to review the", "task " + task.number, (byte) 0, task.project );
		Log.addUserLog( "Assigned task reviewer", task, user, task.project );
		CollaborateUpdate.update( task.project, "reload('task-" + task_id + "');" );
		if( task.subTasks.size() > 0 )
		{
			for( int i = 0; i < task.subTasks.size(); i++ )
			{
				task.subTasks.get( i ).reviewer = user;
				task.subTasks.get( i ).save();
				CollaborateUpdate.update( task.project, "sprintLoad(" + task.subTasks.get( i ).id + ",'" + task.subTasks.get( i ).id + "_reviewer');" );
				CollaborateUpdate.update( task.project, "sprintLoad(" + task.subTasks.get( i ).id + ",'" + task.subTasks.get( i ).id + "_assignee');" );
			}
		}
		CollaborateUpdate.update( task.project, "sprintLoad(" + task_id + ",'" + task_id + "_reviewer');" );
		CollaborateUpdate.update( task.project, "sprintLoad(" + task_id + ",'" + task_id + "_assignee');" );
		renderText( "Reviewer assigned successfully" );
	}

	/**
	 * Renders a list of users on a component. In case the user is not in a
	 * component it renders all the users on the project.
	 * 
	 * @param id
	 *            The component id.
	 * @return void
	 */
	public static void component_users( long id )
	{
		Component c = Component.findById( id );
		List<User> users = new ArrayList<User>();
		if( c.number == 0 )
		{
			users = c.project.users;
		}
		else
		{
			users = c.users;
		}
		List<User.Object> u = new ArrayList<User.Object>();
		for( User user : users )
		{
			u.add( new User.Object( user.id, user.name ) );
		}
		renderJSON( u );
	}

	/**
	 * Set a deadline for a task to remind the assignee to it.
	 * 
	 * @param id
	 */
	public static void set_deadline( long id )
	{
		Task task = Task.findById( id );
		if( !task.assignee.equals( Security.getConnected() ) )
		{
			forbidden();
		}
		else
			render( task );
	}

	public static void change_task_deadline( long id, long new_deadline )
	{
		Task task = Task.findById( id );
		if( !task.assignee.equals( Security.getConnected() ) )
		{
			forbidden();
		}
		if( new_deadline < new Date().getTime() )
		{
			renderJSON( false );
		}
		task.deadline = new_deadline;
		task.save();
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + task.project.id + "&isOverlay=false&url=/tasks/view_task?task_id=" + task.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( task.assignee != null )
			nusers.add( task.assignee );
		if( task.reviewer != null )
			nusers.add( task.reviewer );
		if( task.reporter != null )
			nusers.add( task.reporter );
		Notifications.notifyUsers( nusers, "changed", url, "the deadline of the", "task " + task.number, (byte) 0, task.project );
		CollaborateUpdate.update( Security.getConnected(), "reload('task-" + task.id + "','tasks" + task.project.id + "')" );
		renderJSON( true );

	}

	/**
	 * remove the task deadline
	 * 
	 * @param id
	 *            task id
	 */
	public static void remove_task_deadline( long id )
	{
		Task task = Task.findById( id );
		if( !task.assignee.equals( Security.getConnected() ) )
		{
			forbidden();
		}
		task.deadline = 0;
		task.save();
		String url = Router.getFullUrl( "Application.externalOpen" ) + "?id=" + task.project.id + "&isOverlay=false&url=/tasks/view_task?task_id=" + task.id;
		ArrayList<User> nusers = new ArrayList<User>();
		if( task.assignee != null )
			nusers.add( task.assignee );
		if( task.reviewer != null )
			nusers.add( task.reviewer );
		if( task.reporter != null )
			nusers.add( task.reporter );
		Notifications.notifyUsers( nusers, "removed", url, "the deadline of the", "task " + task.number, (byte) -1, task.project );
		CollaborateUpdate.update( Security.getConnected(), "reload('task-" + task.project.id + "')" );
		renderJSON( true );
	}

	/**
	 * reloads the task when the deadline comes
	 * 
	 * @param id
	 */
	public static void reload_task( long id )
	{
		Task task = Task.findById( id );
		while( task.deadline >= new Date().getTime() )
		{
		}
		CollaborateUpdate.update( Security.getConnected(), "reload('task-" + task.id + "','tasks-" + task.project.id + "')" );
	}

	/**
	 * A method that renders the reviewer of a certain type in a certain
	 * component. and if that reviewer doesn't exist then it returns the
	 * component users.
	 * 
	 * @param typeId
	 *            the Id of the required type to be reviewed.
	 * @param componentId
	 *            the Id of the component in which the task belong.
	 */
	public static void type_reviewer( long type_id, long component_id, long assignee_id )
	{
		TaskType type = TaskType.findById( type_id );
		Component component = Component.findById( component_id );
		List<Reviewer> reviewers = new ArrayList();
		if( type_id != 0 )
			reviewers = Reviewer.find( "byProjectAndAcceptedAndtaskType", type.project, true, type ).fetch();
		List<User.Object> users = new ArrayList<User.Object>();
		for( Reviewer rev : reviewers )
		{
			if( component != null && component.number != 0 )
			{
				if( rev.user.components.contains( component ) && rev.user.id != assignee_id )
					users.add( new User.Object( rev.user ) );
			}
			else
			{
				if( rev.user.id != assignee_id )
					users.add( new User.Object( rev.user ) );
			}
		}
		renderJSON( users );
	}

	public static void delete_sub_tasks( long id )
	{
		Task task = Task.findById( id );
		for( Task sub : task.subTasks )
		{
			sub.DeleteTask();
			CollaborateUpdate.update( sub.project, "reload('tasks-" + sub.project.id + "', 'task-" + sub.id + "')" );
		}
		renderText( "Task deleted successfully." );
	}

}