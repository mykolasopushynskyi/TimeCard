package models;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.jpa.GenericModel;
import play.db.jpa.Model;

/**
 * Created with IntelliJ IDEA. User: dendy4 Date: 11/4/13 Time: 3:26 PM To
 * change this template use File | Settings | File Templates.
 */
@Entity
public class Task extends GenericModel {
	@Id
	public String id;
	
	@OneToMany(mappedBy = "id.task")
	public List<TeamTask> teamTasks = new LinkedList<TeamTask>();
	
	@Column(length = 255)
	public String description;

	public Task() {
	}

	public Task(String id) {
		this.id = id;
	}

	public Task(String userStoryID, String userStoryDesc) {
		this.id = userStoryID;
		this.description = userStoryDesc;
	}

	public String getTaskId() {
		return id;
	}

	public void setTaskId(String taskId) {
		this.id = taskId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
