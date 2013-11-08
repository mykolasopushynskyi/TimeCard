package models;

import play.db.jpa.Model;

import javax.persistence.Entity;

/**
 * Created with IntelliJ IDEA.
 * User: dendy4
 * Date: 11/4/13
 * Time: 3:26 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class UserStory extends Model {

    public String userStoryID;
    public String userStoryDesc;

    UserStory(String userStoryID, String userStoryDesc) {
        this.userStoryID = userStoryID;
        this.userStoryDesc = userStoryDesc;
    }

}
