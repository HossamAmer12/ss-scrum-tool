package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Artifact;
import models.Component;
import models.Meeting;
import models.MeetingAttendance;
import models.Project;
import models.Sprint;
import models.User;
import play.mvc.Controller;
import play.mvc.With;

@With( Secure.class )
public class ReviewLog extends Controller
{
	/**
	 * ReviewLog is the class for manipulating and viewing the review log which
	 * contains various information about the sprint review meetings as its'
	 * tasks and users.
	 * <p>
	 * Operations which gets the list of meetings are done by sending the list
	 * of meetings found in the database to the view by using the findById
	 * method.
	 * <p>
	 * Operations which retrieve the associated items of the selected meeting
	 * operate by getting the selected meeting's id from the view and then
	 * Retrieving the list of users and tasks of that meeting id.
	 * <p>
	 * Operations which edit the meetings and changing their review log status
	 * is done by allowing the user to input his data and reflect it back in the
	 * database.
	 * 
	 * @author Hossam Amer
	 * @version %I%, %G%
	 * @since 1.0
	 */

	/**
	 * S10, S11 Renders the project id to be used by the view
	 * 
	 * @param projectID
	 *            the id of a given project
	 * @param cid
	 *            the id of a given component
	 * @param sid
	 *            the id of a given sprint
	 */

	public static void index( long projectID, long cid, long sid )
	{

		Component c = Component.findById( cid );
		Project p = Project.findById( projectID );
		Sprint s = Sprint.findById( sid );

		boolean sExist = (sid == 0) ? false : true;
		boolean cExist = (cid == 0) ? false : true;
		boolean pExist = (projectID == 0) ? false : true;

		render( projectID, cid, sid, c, p, s, sExist, cExist, pExist );
	}

	/**
	 * S10, S11 Used for sending a list all the meetings of a certain project
	 * found in the database to be displayed by the view for any given scrum
	 * master
	 * <p>
	 * Displays all the meetings to any developer and sends them to the view
	 * using render().
	 * <p>
	 * Allows editing the description of notes' description plus the tasks'
	 * description
	 * 
	 * @param projectID
	 *            the id of a given project
	 *@param cid
	 *            the id of a given component
	 *@param sid
	 *            the id of a given sprint
	 */

	public static void showMeetings( long projectID, long cid, long sid )
	{
		boolean empty = false;
		boolean directLink = false;
		boolean sExist = (sid == 0) ? false : true;
		boolean cExist = (cid == 0) ? false : true;
		boolean pExist = (projectID == 0) ? false : true;

		Component c = Component.findById( cid );
		Project p = Project.findById( projectID );
		Sprint s = Sprint.findById( sid );

		List<Meeting> reviewMeetings = Meeting.find( "project.id=" + projectID + " and isReviewLog=true and deleted=false" ).fetch();

		if( reviewMeetings.isEmpty() )
			empty = true;

		if( projectID == 0 )
			directLink = true;

		render( reviewMeetings, empty, directLink, projectID, cid, sid, c, p, s, sExist, cExist, pExist );
	}

	/**
	 * S10 Updates the note with the new description
	 * 
	 * @param id
	 *            the id of a given artifact of type note
	 * @param des
	 *            the new description of the note
	 * @param meetingID
	 *            the id of a given meeting
	 */

	@Check( "canEditNoteReviewLog" )
	public static boolean editNote( long id, String des, long meetingID )
	{
		try
		{
			Artifact note = Artifact.findById( id );
			note.description = des;
			note.save();

			User userWhoChanged = Security.getConnected();
			String header = "The Note " + id + " has been edited by " + userWhoChanged.name;
			String body = "Note " + id + ":" + '\n' + "The new description is " + note.description;

			/*
			 * Notifications.notifyUsers( User.find(//
			 * "from (User user inner join MeetingAttendance ma on ma.user = user) inner join (Meeting m inner join ma.meeting) where m = ? "
			 * , note.meetingsArtifacts.get( 0 ) ).<User> fetch(), header, body,
			 * (byte) 1 );
			 */

			Notifications.notifyUsers( getAttendanceConfirmed( meetingID ), header, body, (byte) 1 );
			Logs.addLog( userWhoChanged, body, "Note", id, note.meetingsArtifacts.get( 0 ).project, new Date() );
		}

		catch( Exception e )
		{
			render();
		}

		return true;

	}

	/**
	 * Gets the confirmed attendance of a given meeting
	 * 
	 * @param meetingID
	 *            the id of a given meeting
	 * @return a list of all users attended the given meeting
	 */

	private static List<User> getAttendanceConfirmed( long meetingID )
	{
		Meeting tmp = Meeting.findById( meetingID );
		List<User> out = new ArrayList<User>();

		for( MeetingAttendance mA : tmp.users )
			if( mA.status.equals( "confirmed" ) )
				out.add( mA.user );

		return out;
	}

	/**
	 * S10 Used for displaying a list of meetings of a certain project that do
	 * not have a review log for the scrum master to change their status if he
	 * wanted to
	 * 
	 * @param projectID
	 *            the id of a given project
	 *@param cid
	 *            the id of a given component
	 *@param sid
	 *            the id of a given sprint
	 */

	@Check( "canAddReviewLog" )
	public static void showMeetingsNoReviewLog( long projectID, long cid, long sid )
	{
		boolean directLink = false;
		boolean sExist = (sid == 0) ? false : true;
		boolean cExist = (cid == 0) ? false : true;
		boolean pExist = (projectID == 0) ? false : true;

		Component c = Component.findById( cid );
		Project p = Project.findById( projectID );
		Sprint s = Sprint.findById( sid );

		List<Meeting> reviewMeetings = Meeting.find( "project.id=" + projectID + " and isReviewLog=false and deleted=false" ).fetch();

		if( projectID == 0 )
			directLink = true;

		int size = reviewMeetings.size();

		render( reviewMeetings, directLink, projectID, cid, sid, c, p, s, sExist, cExist, pExist, size );

	}

	/**
	 * S10 Gets the id of a given meeting that does not have a review log and
	 * sends it to the view using render()
	 * 
	 * @param id
	 *            the id of a given meeting that do not have a review log
	 */

	@Check( "canAddReviewLog" )
	public static void changeReviewLogStatus( long id )
	{
		Meeting meeting = Meeting.findById( id );
		render( meeting );
	}

	/**
	 * S10 Gets the id of a given meeting that does not have a review log and
	 * changes its status
	 * <p>
	 * Used for changing the review log status of all meetings
	 * 
	 * @param id
	 *            the id of a given meeting that do not have a review log
	 * @param hasReview
	 *            the new status of a given meeting
	 * @param projectID
	 *            the id of a given project
	 * @param importance
	 *            the importance of the notification
	 */

	@Check( "canAddReviewLog" )
	public static void changeReviewLogStatusDone( long id, boolean hasReview, long projectID, boolean importance )
	{
		try
		{

			Meeting tmp = Meeting.findById( id );
			tmp.isReviewLog = hasReview;
			tmp.save();

			User userWhoChanged = Security.getConnected();
			String header = "Meeting " + id + " in " + Project.findById( projectID ) + " has the review log status changed by " + userWhoChanged.name;
			String body = "Meeting " + id + " in " + Project.findById( projectID ) + '\n' + "The new review log status of " + tmp.name + " is:  " + hasReview + ".";

			Notifications.notifyUsers( getUsersFromMeetingAttendaceInACertainMeeting( tmp ), header, body, (byte) (importance ? 1 : -1) );
			Logs.addLog( userWhoChanged, body, "Meeting", id, tmp.project, new Date() );

			render( tmp );
		}

		catch( Exception e )
		{
			e.printStackTrace();
			render( Meeting.findById( id ) );
		}
	}

	/**
	 * Displays the confirmation message for changing status of the review log
	 * 
	 * @param projectID
	 *            the id of a given project
	 */
	@Check( "canAddReviewLog" )
	public static void changeReviewLogStatusConfirm( long projectID )
	{
		render( projectID );
	}

	/**
	 * S10 Gets the users from the meeting attendance list of a given meeting
	 * 
	 * @param tmp
	 *            a given meeting
	 */

	private static List<User> getUsersFromMeetingAttendaceInACertainMeeting( Meeting tmp )
	{
		List<User> out = new ArrayList<User>();
		List<MeetingAttendance> mA = tmp.users;

		for( int i = 0; i < mA.size(); i++ )
			if( !mA.get( i ).user.deleted && mA.get( i ).status.equals( "confirmed" ) )
				out.add( mA.get( i ).user );

		return out;
	}
}
