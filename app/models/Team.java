package models;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

import play.db.jpa.GenericModel;
import play.db.jpa.Model;

@Entity
public class Team extends Model {

	@OneToMany
	@JoinColumn(name="tmId")
	public List<TeamTask> teamTasks = new LinkedList<TeamTask>();
	
	public String name;

	public Team() {
	}
	
	public Team(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}