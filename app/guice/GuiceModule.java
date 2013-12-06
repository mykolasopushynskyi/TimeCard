package guice;

import services.GoogleDocService;
import services.GoogleSocial;
import services.RallyService;
import services.Social;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class GuiceModule extends AbstractModule {

	@Singleton
	@Provides
	public static GoogleSocial getSocialService() {
		return new GoogleSocial();
	}

	@Singleton
	@Provides
	public static GoogleDocService getGoogleDocService() {
		return new GoogleDocService();
	}

	@Singleton
	@Provides
	public static RallyService getRallyService() {
		return new RallyService();
	}

	@Override
	protected void configure() {
	}

}
