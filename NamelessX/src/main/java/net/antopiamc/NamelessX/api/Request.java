package net.antopiamc.NamelessX.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Request {

	private URL url;
	private final RequestMethod method;
	private final String parameters;
	private final Action action;
	private final String userAgent;

	private JsonObject response;
	private boolean hasError;
	private int errorCode = -1;

	public Request(final URL baseUrl, final String userAgent, final Action action, final String... parameters) {
		this.userAgent = userAgent;
		this.action = action;
		this.method = action.method;
		this.parameters = String.join("&", parameters);
		
		try {
			/*
			 * If the url contains a question mark, it's most likely not a friendly url.
			 * Friendly urls have the normal url as a parameter like this:
			 * https://example.com/index.php?route=/api/v2/key/action
			 * Parameters should be added after the route parameter (&param1=value1&param2=value2)
			 * 
			 * If the url does not contain a question mark, it's most likely a friendly url
			 * https://example.com/api/v2/key/action
			 * Parameters need to be prefixed with a question mark (?param1&value1&param2=value2)
			 * 
			 * For post method, parameters need to be sent like this: param1=value1&param2=value2
			 * regardless of the friendly url option.
			 */
			
			String base = baseUrl.toString();
			base = base.endsWith("/") ? base : base + "/"; // Append trailing slash if not present
			
			if (action.method == RequestMethod.GET){
				if (parameters.length > 0) {
					final char prefix = baseUrl.toString().contains("?") ? '&' : '?';
					this.url = new URL(base + action.toString() + prefix + this.parameters);
				} else {
					this.url = new URL(base + action.toString());
				}
			}
			
			if (action.method == RequestMethod.POST) {
				this.url = new URL(base + action.toString());
			}
		} catch (final MalformedURLException e) {
			final IllegalArgumentException ex = new IllegalArgumentException("URL is malformed (" + e.getMessage() + ")");
			ex.initCause(e);
			throw ex;
		}
	}

	public boolean hasError() {
		return this.hasError;
	}

	public int getError() throws NamelessException {
		if (!this.hasError)
			throw new NamelessException("Requested error code but there is no error.");

		return this.errorCode;
	}

	public JsonObject getResponse() throws NamelessException {
		return this.response;
	}

	public void connect() throws NamelessException {

		if (NamelessAPI.DEBUG_MODE) {
			System.out.println(String.format("NamelessAPI > Making %s request (%s", this.action.toString(), this.method.toString()));
			System.out.println(String.format("NamelessAPI > URL: %s", this.url));
			System.out.println(String.format("NamelessAPI > Parameters: %s", this.parameters));
		}

		try {
			if (this.url.toString().startsWith("https://")){
				final HttpsURLConnection connection = (HttpsURLConnection) this.url.openConnection();

				connection.setRequestMethod(this.method.toString());
				connection.setRequestProperty("Content-Length", Integer.toString(this.parameters.length()));
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.addRequestProperty("User-Agent", this.userAgent);

				// If request method is POST, send parameters. Otherwise, they're already included in the URL
				if (this.action.method == RequestMethod.POST) {
					connection.setDoOutput(true);
					final DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
					final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
					writer.write(this.parameters);
					writer.flush();
					writer.close();
					outputStream.close();
				}

				// Initialize input stream
				final InputStream inputStream = connection.getInputStream();

				// Handle response
				final BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				final StringBuilder responseBuilder = new StringBuilder();

				String responseString;
				while ((responseString = streamReader.readLine()) != null) {
					responseBuilder.append(responseString);
				}

				final JsonParser parser = new JsonParser();

				this.response = parser.parse(responseBuilder.toString()).getAsJsonObject();

				inputStream.close();

				// Disconnect
				connection.disconnect();
			} else {
				final HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();

				connection.setRequestMethod(this.method.toString());
				connection.setRequestProperty("Content-Length", Integer.toString(this.parameters.length()));
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.addRequestProperty("User-Agent", this.userAgent);

				// If request method is POST, send parameters. Otherwise, they're already included in the URL
				if (this.action.method == RequestMethod.POST) {
					connection.setDoOutput(true);
					final DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
					final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
					writer.write(this.parameters);
					writer.flush();
					writer.close();
					outputStream.close();
				}

				// Initialize input stream
				final InputStream inputStream = connection.getInputStream();

				// Handle response
				final BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				final StringBuilder responseBuilder = new StringBuilder();

				String responseString;
				while ((responseString = streamReader.readLine()) != null) {
					responseBuilder.append(responseString);
				}

				final JsonParser parser = new JsonParser();
				
				if (NamelessAPI.DEBUG_MODE) {
					System.out.println(String.format("NamelessAPI > Response: %s", responseBuilder.toString()));
				}
				
				this.response = parser.parse(responseBuilder.toString()).getAsJsonObject();

				inputStream.close();

				// Disconnect
				connection.disconnect();
			}

			this.hasError = this.response.get("error").getAsBoolean();
			if (this.hasError) {
				this.errorCode = this.response.get("code").getAsInt();
			}
		} catch (final IOException e) {
			throw new NamelessException(e);
		}
	}

	public enum Action {

		INFO("info", RequestMethod.GET),
		GET_ANNOUNCEMENTS("getAnnouncements", RequestMethod.GET),
		REGISTER("register", RequestMethod.POST),
		USER_INFO("userInfo", RequestMethod.GET),
		SET_GROUP("setGroup", RequestMethod.POST),
		CREATE_REPORT("createReport", RequestMethod.POST),
		GET_NOTIFICATIONS("getNotifications", RequestMethod.GET),
		SERVER_INFO("serverInfo", RequestMethod.POST),
		VALIDATE_USER("validateUser", RequestMethod.POST),
		LIST_USERS("listUsers", RequestMethod.GET),

		;

		RequestMethod method;
		String name;

		Action(final String name, final RequestMethod method){
			this.name = name;
			this.method = method;
		}

		@Override
		public String toString() {
			return this.name;
		}
		
		/*@Override
		public String toString() {
			List<String> list = Arrays.asList(super.toString().split("_"));
			StringBuilder builder = new StringBuilder();
			builder.append(list.remove(0).toLowerCase(Locale.ENGLISH));
			list.forEach((element) -> builder.append(element.substring(0, 1) + element.substring(1).toLowerCase()));
			return builder.toString();
		}*/

	}

	public enum RequestMethod {

		GET, POST

	}

}
