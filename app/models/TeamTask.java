package models;

import java.io.Serializable;
import java.util.Date;

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

	@EmbeddedId
	public PK id = new PK();

	@Embeddable
	public static class PK implements Serializable {
		@ManyToOne
		@JoinColumn(name = "tmId")
		public Team team;

		@ManyToOne
		@JoinColumn(name = "taskId")
		public Task task;
	}

	public Date getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

	public Date updatedDate;
}
