package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

import play.db.jpa.Model;

@Entity
public class Rally extends Model{
	
	@OneToOne
	@PrimaryKeyJoinColumn
	public Team team;
	
	@Column(nullable = false, length = 64, unique = false)
	public String password;
	
	@Column(nullable = false, length = 64, unique = false)
	public String login;
}
