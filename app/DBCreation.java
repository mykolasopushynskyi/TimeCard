import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.jobs.OnApplicationStop;
import play.test.Fixtures;

/**
 * Created with IntelliJ IDEA.
 * User: dendy4
 * Date: 11/4/13
 * Time: 3:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBCreation {
    @OnApplicationStart
    public class Bootstrap extends Job {

        public void doJob() {
           Fixtures.loadModels("initial-data.yml");
        }

    }

    @OnApplicationStop
    public class End extends Job {

        public void doJob() {
            Fixtures.deleteAllModels();
        }

    }
}
