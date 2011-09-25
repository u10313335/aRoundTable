package tw.jouou.aRoundTable.lib;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tw.jouou.aRoundTable.bean.Event;
import tw.jouou.aRoundTable.bean.GroupDoc;
import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.bean.Notification;
import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.Task;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Interface for around-table API
 * @author albb0920
 *
 */
public class ArtApi {
	private static ArtApi instance;
	private static final String baseURL = "http://api.hime.loli.tw";
	private static final String projectsPath = "/projects";	
	private static final String tasksPath = "/tasks";
	private static final String eventsPath = "/events";
	private static final String projectPath = "/projects/%d";
	private static final String taskPath = "/tasks/%d";
	private static final String eventPath = "/events/%d";
	private static final String addUserPath = "/projects/%d/users";
	private static final String notificationsPath = "/notifications";
	private static final String notificationResponsePath = "/notifications/%d/notification_responses";
	private static final String notepadPath = "/projects/%d/notepad";
	
	private String token;
	
	public ArtApi(String token){
		this.token = token;		
	}
	
	public static String getLoginUrl(){
		return baseURL + "/login";
	}
	
	public static ArtApi getInstance(Context context){
		if(instance != null)
			return instance;
		
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        
        return instance = new ArtApi(prefs.getString("TOKEN", ""));
	}
	
	public Project[] getProjectList() throws ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash();
		Project r[];
		try {
			HttpResponse response = performGet(projectsPath, params);
				
			JSONArray projects = extractJsonArray(response);
			JSONObject project = null;
			
			r = new Project[projects.length()];
			for(int i=0; i < projects.length(); i++){
				project = projects.getJSONObject(i);
				if(r == null)
					System.out.println("1");
				if(project == null)
					System.out.println("2");
				r[i] = new Project(project); 
			}
			return r;
		} catch (JSONException e) {
			throw new ServerException("Server respond with invalid json");
		}
	}
	
	public Project getProject(long projectId) throws  ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash();
		HttpResponse response = performGet(projectsPath + "/" + projectId, params);
		
		JSONObject projectJson = extractJsonObject(response);
		try {
			return new Project(projectJson);
		} catch (JSONException e) {
			throw new ServerException("Server returned unexpected data");
		}
	}
	
	/**
	 * Update project attributes
	 * @param projectId
	 * @param name
	 * @param color
	 * @throws ServerException
	 * @throws ConnectionFailException
	 */
	public void updateProject(long projectId, String name, String color) throws  ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash();
		
		params.put("project[name]", name);
		params.put("project[color]", color);
		
		performPut(String.format(projectPath, projectId), params);
	}
	
	public Task[] getTaskList(long projectId) throws ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash();
		HttpResponse response = performGet(projectsPath + "/" + projectId + "/tasks", params);
		Task[] tasks;

		try {
			JSONArray tasksJson = extractJsonArray(response);

			tasks = new Task[tasksJson.length()];
			for(int i=0; i < tasksJson.length(); i++ ){
				tasks[i] = new Task(tasksJson.getJSONObject(i)); 
			}
			return tasks;
		} catch (JSONException e) {
			throw new ServerException("Server returned unexpected data");
		}
	}
	
	public Task getTask(long taskId) throws  ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash();
		HttpResponse response = performGet(tasksPath + "/" + taskId, params);
		
		JSONObject taskJson = extractJsonObject(response);
		try {
			return new Task(taskJson);
		} catch (JSONException e) {
			throw new ServerException("Server returned unexpected data");
		}
	}
	
	/**
	 * Create a new task (single owners)
	 * @param projectId project belongs to
	 * @param name
	 * @param due
	 * @param note
	 * @return newly created task id
	 * @throws ServerException
	 * @throws ConnectionFailException
	 */
	public int createTask(long projectId, String name, Long owner, Date due, String note) throws  ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash();

		params.put("task[name]", name);
		if(!(due == null)) {
			params.put("task[due]", due.toString());
		} else {
			params.put("task[due]", "");
		}
		if(owner!=-1) {
			params.put("task[user_ids][]", owner.toString());
		}
		params.put("task[note]", note);
		HttpResponse response =  performPost(projectsPath + "/" + projectId + "/tasks", params);
		try{
			JSONObject projectJson = extractJsonObject(response);
			return projectJson.getInt("id");
		}catch(JSONException je){
			throw new ServerException("Server returned unexpected data");
		}
	}
	
	/**
	 * Create a new task (multiple owners)
	 * @param projectId project belongs to
	 * @param name
	 * @param due
	 * @param note
	 * @return newly created task id
	 * @throws ServerException
	 * @throws ConnectionFailException
	 */
	public int createTask(long projectId, String name, Long[] owners, Date due, String note) throws  ServerException, ConnectionFailException{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		
		params.add(new BasicNameValuePair("task[name]", name));
		if(due != null) {
			params.add(new BasicNameValuePair("task[due]", due.toString()));
		} else {
			params.add(new BasicNameValuePair("task[due]", ""));
		}
		for(int i=0; i<owners.length; i++) {
			if(owners[i]!=-1) {
				params.add(new BasicNameValuePair("task[user_ids][]", owners[i].toString()));
			}
		}
		params.add(new BasicNameValuePair("task[note]", note));
		HttpResponse response =  performPost(projectsPath + "/" + projectId + "/tasks", params);
		try{
			JSONObject projectJson = extractJsonObject(response);
			return projectJson.getInt("id");
		}catch(JSONException je){
			throw new ServerException("Server returned unexpected data");
		}
	}
		
	/**
	 * Update task attributes	
	 * @param taskId
	 * @param name
	 * @param due
	 * @param note
	 * @throws ServerException
	 * @throws ConnectionFailException
	 */
	public void updateTask(long taskId, String name, Long[] owners, Date due, String note, boolean finished) throws  ServerException, ConnectionFailException{
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		
		params.add(new BasicNameValuePair("task[name]", name));
		if(!(due == null)) {
			params.add(new BasicNameValuePair("task[due]", due.toString()));
		} else {
			params.add(new BasicNameValuePair("task[due]", ""));
		}
		if(owners!=null) {
			for(int i=0; i<owners.length; i++) {
				if(owners[i]!=-1) {
					params.add(new BasicNameValuePair("task[user_ids][]", owners[i].toString()));
				}
			}
		}
		params.add(new BasicNameValuePair("task[note]", note));
		params.add(new BasicNameValuePair("task[finished]", (finished)? "1" : "0"));
		performPut(String.format(taskPath, taskId), params);
	}
	
	/**
	 * Delete a task
	 * @param taskId task's id
	 * @return 
	 * @throws ServerException 
	 * @throws ConnectionFailException 
	 */
	public void deleteTask(long taskId) throws ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash();
		performDelete(tasksPath + "/" + taskId, params);
	}

	public Event[] getEventList(long projectId) throws ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash();
		HttpResponse response = performGet(projectsPath + "/" + projectId + "/events", params);
		Event[] events;

		try {
			JSONArray eventsJson = extractJsonArray(response);

			events = new Event[eventsJson.length()];
			for(int i=0; i < eventsJson.length(); i++ ){
				events[i] = new Event(eventsJson.getJSONObject(i)); 
			}
			return events;
		} catch (JSONException e) {
			throw new ServerException("Server returned unexpected data");
		}
	}
	
	public Event getEvent(long eventId) throws  ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash();
		HttpResponse response = performGet(eventsPath + "/" + eventId, params);
		
		JSONObject eventsJson = extractJsonObject(response);
		try {
			return new Event(eventsJson);
		} catch (JSONException e) {
			throw new ServerException("Server returned unexpected data");
		}
	}
	
	/**
	 * Create a new event
	 * @param projectId project belongs to
	 * @param name
	 * @param start_at
	 * @param end_at
	 * @param location
	 * @param note
	 * @return newly created event id
	 * @throws ServerException
	 * @throws ConnectionFailException
	 */
	public int createEvent(long projectId, String name, Date start_at, Date end_at, String location, String note) throws  ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash();

		params.put("event[name]", name);
		if(!(start_at == null)) {
			params.put("event[start_at]", start_at.toString());
			params.put("event[end_at]", end_at.toString());
		} else {
			params.put("event[start_at]", "");
			params.put("event[end_at]", "");
		}
		params.put("event[location]", location);
		params.put("event[note]", note);
		HttpResponse response =  performPost(projectsPath + "/" + projectId + "/events", params);
		try{
			JSONObject projectJson = extractJsonObject(response);
			return projectJson.getInt("id");
		}catch(JSONException je){
			throw new ServerException("Server returned unexpected data");
		}
	}

	/**
	 * Update event attributes	
	 * @param eventId
	 * @param name
	 * @param due
	 * @param note
	 * @throws ServerException
	 * @throws ConnectionFailException
	 */
	public void updateEvent(long eventId, String name, Date start_at, Date end_at, String location, String note) throws  ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash();

		params.put("event[name]", name);
		params.put("event[start_at]", start_at.toString());
		params.put("event[end_at]", end_at.toString());
		params.put("event[location]", location);
		params.put("event[note]", note);
		
		performPut(String.format(eventPath, eventId), params);
	}
	
	/**
	 * Delete a event
	 * @param eventId event's id
	 * @return 
	 * @throws ServerException 
	 * @throws ConnectionFailException 
	 */
	public void deleteEvent(long eventId) throws ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash();
		performDelete(eventsPath + "/" + eventId, params);
	}
	
	/**
	 * Get dependency list of an taskevent
	 * @param taskeventId
	 * @return an array contains dependency taskevent ids
	 * @throws ServerException
	 * @throws ConnectionFailException
	 */
	public int[] getDependencies(int taskeventId) throws ServerException, ConnectionFailException{
		HttpResponse response = performGet(String.format(taskPath, taskeventId), makeTokenHash());
		try {
			JSONArray jsonArray = extractJsonObject(response).getJSONArray("dependency_ids");
			int dependencies[] = new int[jsonArray.length()];
			for(int i=0; i<dependencies.length; i++){
				dependencies[i] = jsonArray.getInt(i);
			}
			return dependencies;
		} catch (JSONException e) {
			throw new ServerException("fail to get dependency array from json");
		}
	}
	
	/**
	 * Set dependencies for taskevent
	 * @param taskeventId
	 * @param dependencies
	 * @throws ServerException
	 * @throws ConnectionFailException
	 */
	public void setDependencies(long taskeventId, Long dependencies[], int duration) throws ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash();
		for(long id: dependencies){
			params.put("task[dependency_ids][]", Long.toString(id));
		}
		params.put("task[duration]", Integer.toString(duration));
		
		performPut(String.format(taskPath, taskeventId), params);
	}
	
	/**
	 * Create a new project with current user
	 * @param name Name of new project
	 * @param color Color of new project
	 * @return new project's id
	 * @throws ServerException 
	 * @throws ConnectionFailException 
	 */
	public int createProject(String name, String color) throws ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash();
		params.put("project[name]", name);
		params.put("project[color]", color);
		
		HttpResponse response =  performPost(projectsPath, params);
		try{
			JSONObject projectJson = extractJsonObject(response);
			return projectJson.getInt("id");
		}catch(JSONException je){
			throw new ServerException("Server returned unexpected data");
		}
	}

	/**
	 * Quit a project
	 * @param projectId project's id
	 * @return 
	 * @throws ServerException 
	 * @throws ConnectionFailException 
	 */
	public void quitProject(long projectId) throws ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash();
		performDelete(projectsPath + "/" + projectId, params);
	}
	
	public enum JoinStatus{
		SUCCESS, INVITED, FAILED
	}
	
	/**
	 * Join a list of emails to specific project
	 * @param projectId project identifier
	 * @param emails emails to be invited
	 * @return
	 * @throws ConnectionFailException 
	 * @throws ServerException 
	 */
	public JoinStatus[] addUser(long projectId, String emails[]) throws ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash(); 
		
		for(int i=0; i< emails.length; i++){
			params.put("email["+i+"]", emails[i]);
		}
		
		HttpResponse response = performPost(String.format(addUserPath, projectId), params);
		
		try {
			
			JSONArray respArr = extractJsonArray(response);
			JoinStatus[] result = new JoinStatus[respArr.length()];
			String iState;
			
			for(int i=0; i<respArr.length(); i++){
				iState = respArr.getString(i);
				if(iState.equals("success"))
					result[i] = JoinStatus.SUCCESS;
				else if(iState.equals("invited"))
					result[i] = JoinStatus.INVITED;
				else
					result[i] = JoinStatus.FAILED;
			}
			
			return result;
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public User[] getUsers(int projectId) throws ServerException, ConnectionFailException {
		HashMap<String, String> params = makeTokenHash();
		HttpResponse response = performGet(String.format(addUserPath, projectId), params);
		JSONArray respArr = extractJsonArray(response);
		User[] result = new User[respArr.length()];
		try {
			for(int i=0; i<respArr.length(); i++){
				result[i] = new User(projectId, respArr.getJSONObject(i));
			}
		}catch(JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	public void updateNotepad(long projectId, String content) throws ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash();
		params.put("notepad[content]", content);
		
		performPut(String.format(notepadPath, projectId), params);
	}

	public GroupDoc getNotepad(long projectId) throws ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash();
		HttpResponse response = performGet(String.format(notepadPath, projectId), params);
		
		JSONObject eventsJson = extractJsonObject(response);
		try {
			return new GroupDoc(eventsJson);
		} catch (JSONException e) {
			throw new ServerException("Server returned unexpected data");
		}
	}

	/**
	 * Get notifications
	 * @return all notification of current user
	 * @throws ServerException
	 * @throws ConnectionFailException
	 */
	public Notification[] getNotifications() throws ServerException, ConnectionFailException{
		HttpResponse response = performGet(notificationsPath, makeTokenHash());
		
		Notification[] notifications;
			try {
				JSONArray notificationJson = extractJsonArray(response);
				notifications = new Notification[notificationJson.length()];
				for(int i=0; i < notificationJson.length(); i++ ){
					notifications[i] = new Notification(notificationJson.getJSONObject(i)); 
				}
				return notifications;
			} catch (JSONException e) {
				throw new ServerException("Server return invalid json");
			}
	}
	
	/**
	 * Server fault
	 */
	public class ServerException extends Exception{
		private static final long serialVersionUID = 9019747876485278413L;

		public ServerException(String msg) {
			super(msg);
		}
	};
	
	private JSONArray extractJsonArray(HttpResponse response) throws ServerException, ConnectionFailException{
		try {
			return new JSONArray(EntityUtils.toString(response.getEntity()));
		} catch (org.apache.http.ParseException e) {
			throw new ConnectionFailException();
		} catch (JSONException e) {
			throw new ServerException("Json error");
		} catch (IOException e) {
			throw new ConnectionFailException();
		}	
	}
	
	private JSONObject extractJsonObject(HttpResponse response) throws ConnectionFailException, ServerException{
		try {
			return new JSONObject(EntityUtils.toString(response.getEntity()));
		} catch (org.apache.http.ParseException e) {
			throw new ConnectionFailException();
		} catch (JSONException e) {
			throw new ServerException("Json error");
		} catch (IOException e) {
			throw new ConnectionFailException();
		}	
	}

	/**
	 * Make a new hash with token in it.
	 * @return params hash
	 */
	private HashMap<String, String> makeTokenHash(){
		HashMap<String, String> hash =  new HashMap<String, String>();
		hash.put("token", token);
		return hash;
	}
	
	/**
	 * Perform HTTP POST Request
	 * @param path path to API endpoint
	 * @param params HTTP Parameters
	 * @return HttpResponse object
	 * @throws ClientProtocolException 
	 * @throws IOException
	 */
	private HttpResponse performPost(String path, List<NameValuePair> params) throws ServerException, ConnectionFailException{
		HttpPost post = new HttpPost(baseURL + path);
		
		// Perform request
		try {
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		try {
			HttpResponse response = httpClient.execute(post);
			if(response.getStatusLine().getStatusCode() != 200)
				throw new ServerException("Got code: "+response.getStatusLine().getStatusCode());
			else
				return response;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ConnectionFailException();
		}
	}
	
	/**
	 * Perform HTTP POST Request
	 * @param path path to API endpoint
	 * @param params HTTP Parameters
	 * @return HttpResponse object
	 * @throws ClientProtocolException 
	 * @throws IOException
	 */
	private HttpResponse performPost(String path, HashMap<String, String> params) throws ServerException, ConnectionFailException{
		HttpPost post = new HttpPost(baseURL + path);
		
		// Perform request
		try {
			post.setEntity(new UrlEncodedFormEntity(prepareParams(params), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		try {
			HttpResponse response = httpClient.execute(post);
			if(response.getStatusLine().getStatusCode() != 200)
				throw new ServerException("Got code: "+response.getStatusLine().getStatusCode());
			else
				return response;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ConnectionFailException();
		}
	}
	
	/**
	 * Perform HTTP PUT Request
	 * @param path path to API endpoint
	 * @param params HTTP Parameters
	 * @return HttpResponse object
	 * @throws ClientProtocolException 
	 * @throws IOException
	 */
	private HttpResponse performPut(String path, List<NameValuePair> params) throws ServerException, ConnectionFailException{
		HttpPut put = new HttpPut(baseURL + path);
		
		// Perform request
		try {
			put.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		try {
			HttpResponse response = httpClient.execute(put);
			if(response.getStatusLine().getStatusCode() != 200)
				throw new ServerException("Got code: "+response.getStatusLine().getStatusCode());
			else
				return response;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ConnectionFailException();
		}
	}
	
	/**
	 * Perform HTTP PUT Request
	 * @param path path to API endpoint
	 * @param params HTTP Parameters
	 * @return HttpResponse object
	 * @throws ClientProtocolException 
	 * @throws IOException
	 */
	private HttpResponse performPut(String path, HashMap<String, String> params) throws ServerException, ConnectionFailException{
		HttpPut put = new HttpPut(baseURL + path);
		
		// Perform request
		try {
			put.setEntity(new UrlEncodedFormEntity(prepareParams(params), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		try {
			HttpResponse response = httpClient.execute(put);
			if(response.getStatusLine().getStatusCode() != 200)
				throw new ServerException("Got code: "+response.getStatusLine().getStatusCode());
			else
				return response;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ConnectionFailException();
		}
	}
	
	private HttpResponse performGet(String path, HashMap<String, String> params) throws ServerException, ConnectionFailException{
		HttpGet get = new HttpGet(baseURL + path + "?" + URLEncodedUtils.format(prepareParams(params), "UTF-8"));
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpResponse response;
		try {
			response = httpClient.execute(get);
		} catch (IOException e) {
			throw new ConnectionFailException();
		}
		if(response.getStatusLine().getStatusCode() != 200)
			throw new ServerException("Got code: "+response.getStatusLine().getStatusCode());
		else
			return response;
	}
	
	private HttpResponse performDelete(String path, HashMap<String, String> params) throws ServerException, ConnectionFailException{
		HttpDelete delete = new HttpDelete(baseURL + path + "?" + URLEncodedUtils.format(prepareParams(params), "UTF-8"));
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpResponse response;
		
		try {
			response = httpClient.execute(delete);
			
			if(response.getStatusLine().getStatusCode() == 200)
				return response;
			else
				throw new ServerException("Got code: "+response.getStatusLine().getStatusCode());
			
		} catch (IOException e) {
			throw new ConnectionFailException();
		}
	}
	
	private List<NameValuePair> prepareParams(HashMap<String, String> params){		
		// Prepare parameters
		List<NameValuePair> httpParams = new ArrayList<NameValuePair>();
		for(String key: params.keySet()){
			httpParams.add(new BasicNameValuePair(key, params.get(key)));
		}
		
		return httpParams;
	}
	public class ConnectionFailException extends Exception{
		private static final long serialVersionUID = -5345016442518985501L;
	}
	public class InvalidTokenException extends Exception{
		private static final long serialVersionUID = -5159078241509001724L;
	}
}
