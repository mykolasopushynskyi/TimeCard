package unit;
import org.junit.*;
import java.util.*;
import play.test.*;
import models.*;

public class BasicTest extends UnitTest {

    @After @Before
    public void cleanUp() {
        //Fixtures.deleteAllModels();
    }

    @Test
    public void aVeryImportantThingToTest() {
        assertEquals(2, 1 + 1);

        //Fixtures.loadModels("initial-data.yml");
    }

}
