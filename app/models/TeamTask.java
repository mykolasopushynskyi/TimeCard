package models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class TeamTask extends Model {

	public TeamTask() {
		this.updatedDate = new Date();
	}
	
	@ManyToOne
	@JoinColumn(name = "tmId")
	public Team team;
	
	@ManyToOne
	@JoinColumn(name = "taskId")
	public Task task;
	
	
	public Date getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}



	public Date updatedDate;
}
