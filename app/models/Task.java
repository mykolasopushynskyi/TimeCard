package models;

import java.util.LinkedList;
import java.util.List;

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
	public String taskId;
	
	@OneToMany
	@JoinColumn(name="taskId")
	public List<TeamTask> teamTasks = new LinkedList<TeamTask>();
	
	public String description;

	public Task() {
	}

	public Task(String userStoryID, String userStoryDesc) {
		this.taskId = userStoryID;
		this.description = userStoryDesc;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	/*
	//Need to find by key
    @Override
    public Object _key() {
        return getTaskId();
    }*/

}
