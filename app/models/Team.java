package models;

import javax.persistence.Column;
import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class Team extends Model {

    @Column
	public String name;

	public Team(String name) {
		this.name = name;
	}
}