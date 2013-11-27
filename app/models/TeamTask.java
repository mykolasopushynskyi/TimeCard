package models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import play.db.jpa.GenericModel;

@Entity
public class TeamTask extends GenericModel {

	public TeamTask() {
		this.updatedDate = new Date();
	}

	public TeamTask(Team team, Task task) {
		this();
		this.id.team = team;
		this.id.task = task;
	}

	@EmbeddedId
	public PK id = new PK();

	@Embeddable
	public static class PK implements Serializable {
		@ManyToOne
		@JoinColumn(name = "teamId")
		public Team team;

		@ManyToOne
		@JoinColumn(name = "taskId")
		public Task task;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((task == null) ? 0 : task.hashCode());
			result = prime * result + ((team == null) ? 0 : team.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PK other = (PK) obj;
			if (task == null) {
				if (other.task != null)
					return false;
			} else if (!task.equals(other.task))
				return false;
			if (team == null) {
				if (other.team != null)
					return false;
			} else if (!team.equals(other.team))
				return false;
			return true;
		}
	}

	public Date getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

	public Date updatedDate;

}
