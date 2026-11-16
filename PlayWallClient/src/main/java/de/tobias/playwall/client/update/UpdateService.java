package de.tobias.playwall.client.update;

import de.thecodelabs.utils.application.App;
import de.tobias.playwall.client.AppUserInfoStrings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import tools.jackson.core.Version;
import tools.jackson.core.util.VersionUtil;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class UpdateService
{
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

	private final App app;
	private final HttpClient httpClient;
	private final JsonMapper jsonMapper;

	@InjectConstructor
	UpdateService(App app)
	{
		this(app, HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(REQUEST_TIMEOUT).build(),
				JsonMapper.builder().findAndAddModules().build());
	}

	UpdateService(App app, HttpClient httpClient, JsonMapper jsonMapper)
	{
		this.app = app;
		this.httpClient = httpClient;
		this.jsonMapper = jsonMapper;
	}

	public String getCurrentVersion()
	{
		return app.getInfo().getVersion();
	}

	public String fetchLatestVersion() throws UpdateCheckException
	{
		try
		{
			final String url = app.getUserInfo(AppUserInfoStrings.class).versionApiUrl();

			final HttpRequest request = HttpRequest.newBuilder(URI.create(url))
					.timeout(REQUEST_TIMEOUT)
					.header("Accept", "application/json")
					.GET()
					.build();

			final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if(response.statusCode() != 200)
			{
				throw new UpdateCheckException("Unexpected response status while fetching latest version: " + response.statusCode());
			}

			final UpdateInfo updateInfo = jsonMapper.readValue(response.body(), UpdateInfo.class);
			if(updateInfo == null || updateInfo.version() == null || updateInfo.version().isBlank())
			{
				throw new UpdateCheckException("Latest version response does not contain a version");
			}

			return updateInfo.version();
		}
		catch(InterruptedException e)
		{
			Thread.currentThread().interrupt();
			throw new UpdateCheckException("Interrupted while fetching latest version", e);
		}
		catch(IOException | RuntimeException e)
		{
			throw new UpdateCheckException("Cannot fetch latest version", e);
		}
	}

	public static boolean isVersionNewer(String currentVersion, String latestVersion) throws UpdateCheckException
	{
		final Version current = VersionUtil.parseVersion(currentVersion, null, null);
		if(current.isUnknownVersion() || isZeroVersion(current))
		{
			throw new UpdateCheckException("Cannot parse current version: " + currentVersion);
		}

		final Version latest = VersionUtil.parseVersion(latestVersion, null, null);
		if(latest.isUnknownVersion() || isZeroVersion(latest))
		{
			throw new UpdateCheckException("Cannot parse latest version: " + latestVersion);
		}

		return current.compareTo(latest) < 0;
	}

	private static boolean isZeroVersion(Version version)
	{
		return version.getMajorVersion() == 0
				&& version.getMinorVersion() == 0
				&& version.getPatchLevel() == 0;
	}
}
