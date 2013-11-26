import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import controllers.Security;
import play.mvc.Http.Response;
import play.test.FunctionalTest;

public class ApplicationTest extends FunctionalTest {

    @Test
    public void testThatIndexPageWorks() {
        Response response = GET("/");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
    }
    
    @Test
    public void testTeamList() {
        Response response = GET("/getTeamList");
        assertIsOk(response);
        assertContentMatch("\\[\"[a-zA-Z]+\"(,\"[a-zA-Z]+\")*\\]", response);
    }
    
    @Test
    public void testUserStories() {
        Response response = GET("//getStories?teamName=Polaris");
       // assertContentMatch("[\\{[a-zA-Z]+\\}(,\\{[a-zA-Z]+\\})*]", response);
        assertIsOk(response);
    }
    
    @Test
    public void testUserIsFoundAndPassedToView() {
    	if(!Security.isLogged())
    	{
    		Security.authGoogle();
    	}
    	
    	GET("/");
        String mail = (String) renderArgs("email");

        assertTrue(StringUtils.isNotBlank(mail));
        assertTrue(mail.endsWith("@globallogic.com"));
    }
    
    
    
}