package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class Game extends SmartModel
{
	//
	// @OneToMany
	// public List<Round> rounds;

	@ManyToMany
	public List<Task> tasks;

	@ManyToOne
	public Component component;

	@ManyToOne
	public Task currentTask;

	@OneToOne
	public ChatRoom chatroom;

	public boolean gameOver;

	public Game()
	{
		tasks = new ArrayList<Task>();
	}

	public void init()
	{

		chatroom = new ChatRoom().save();
		this.save();
	}

	public Round getRound()
	{
		List<Round> rounds = Round.find( "game = ? order by id desc", this ).fetch();
		if( rounds.isEmpty() )
		{
			return null;
		}
		return rounds.get( 0 );
	}

	public User getModerator()
	{

		List<GameSession> sessionsInGame = GameSession.find( "byGame", this ).fetch();
		Collections.sort( sessionsInGame );
		return sessionsInGame.get( 0 ).user;
	}
}
