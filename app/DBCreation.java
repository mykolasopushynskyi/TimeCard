import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

/**
 * Created with IntelliJ IDEA. User: dendy4 Date: 11/4/13 Time: 3:17 PM To
 * change this template use File | Settings | File Templates.
 */
@OnApplicationStart
public class DBCreation extends Job {

	public void doJob() {

//		Fixtures.deleteAllModels();
//		Fixtures.loadModels("rallydata.yml");
	}
}
