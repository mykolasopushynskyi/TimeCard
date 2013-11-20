package models;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

import org.apache.commons.io.filefilter.FalseFileFilter;

import play.db.jpa.GenericModel;
import play.db.jpa.Model;

@Entity
public class Team extends Model {

	@OneToMany(mappedBy = "id.team")
	public List<TeamTask> teamTasks = new LinkedList<TeamTask>();

	@Column(nullable = false, length = 64, unique = true)
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