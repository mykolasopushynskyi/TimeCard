package models;

import java.sql.Date;
import java.util.*;
import javax.persistence.*;
import play.db.jpa.*;

@Entity
public class Report extends Model {

    @Column
	public int storyTime;

    @Column
	public int bugFixed;

    //TODO Add more fields (17 fields must be)

	@ManyToOne
	public User author;

	public Report(User author, int storyTime, int bugFixed) {
		this.author = author;
		this.bugFixed = bugFixed;
		this.storyTime = storyTime;
	}
}