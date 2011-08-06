package tw.jouou.aRoundTable.lib;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.TaskEvent;
import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.util.DBUtils;
import android.content.Context;

/**
 * Interface for around-table API
 * @author albb0920
 *
 */
public class ArtApi {
	private static ArtApi instance;
	private static final String baseURL = "http://api.hime.loli.tw:3000";
	private static final String projectsPath = "/projects";	
	private static final String addMemberPath = "/projects/%d/users";
	
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
		
		DBUtils dbUtils = new DBUtils(context);
		List<User> users = (new DBUtils(context)).userDelegate.get();    	
        dbUtils.close();
        
        return instance = new ArtApi(users.get(0).getToken());
	}
	
	/* XXX: NOT tested yet. */
	public Project[] getProjectList() throws ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash();
		Project r[];
		try {
			HttpResponse response = performGet(projectsPath, params);
			if(response.getStatusLine().getStatusCode() == 200){
				
				JSONArray projects = extractJsonArray(response);
				JSONObject project = null;
				
				r = new Project[projects.length()];
				for(int i=0; i < projects.length(); i++){
					project = projects.getJSONObject(i);
					if(r == null)
						System.out.println("1");
					if(project == null)
						System.out.println("2");
					r[i] = new Project(project.getInt("id"), project.getString("name")); 
				}
				return r;
			}else
				throw new ServerException("Server respond with: "+response.getStatusLine());		
		} catch (JSONException e) {
			throw new ServerException("Server respond with invalid json");
		}
	}
	
	/* XXX: NOT tested yet. */
	public Project getProject(int projectId) throws  ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash();
		HttpResponse response = performGet(projectsPath + "/" + projectId, params);
		if(response.getStatusLine().getStatusCode() == 200){
			JSONObject projectJson = extractJsonObject(response);
			try {
				return new Project(projectJson);
			} catch (ParseException e) {
				throw new ServerException("Server returned unexpected data");
			} catch (JSONException e) {
				throw new ServerException("Server returned unexpected data");
			}
		}else{
			throw new ServerException("Response code:"+response.getStatusLine());
		}
	}
	
	/* XXX: not tested yet. */
	public TaskEvent[] getTaskeventList(int projectId) throws ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash();
		HttpResponse response = performGet(projectsPath + "/" + projectId + "/taskevents", params);
		TaskEvent[] taskEvents;
		JSONObject taskEventJson = null;
		if(response.getStatusLine().getStatusCode() == 200){
			try {
				JSONArray taskEventsJson = extractJsonArray(response);

				taskEvents = new TaskEvent[taskEventsJson.length()];
				for(int i=0; i < taskEventsJson.length(); taskEventJson = taskEventsJson.getJSONObject(i)){			
					taskEvents[i] = new TaskEvent(taskEventJson); 
				}
				return taskEvents;
			} catch (JSONException e) {
				throw new ServerException("Server returned unexpected data");
			} catch (ParseException e) {
				throw new ServerException("Server returned unexpected data");
			}	
		}else{
			throw new ServerException("Response code:"+response.getStatusLine());
		}
	} 
	
	/* XXX: Not tested yet. */
	public int createTaskevent(int projectId, int type, String name, Date due, String note) throws  ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash();

		params.put("taskevent[name]", name);
		params.put("taskevent[type]", String.valueOf(type));
		params.put("taskevent[due]", due.toString());
		params.put("taskevent[note]", note);
		
		HttpResponse response =  performPost(projectsPath + "/" + projectId + "/taskevents", params);
		if(response.getStatusLine().getStatusCode() == 200){
			try{
				JSONObject projectJson = extractJsonObject(response);
				return projectJson.getInt("id");
			}catch(JSONException je){
				throw new ServerException("Server returned unexpected data");
			}
		}else{
			throw new ServerException("Response code:"+response.getStatusLine());
		}
	}
	
	/**
	 * Create a new project with current user
	 * @param name Name of new project
	 * @return new project's id
	 * @throws ServerException 
	 * @throws ConnectionFailException 
	 */
	public int createProject(String name) throws ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash();
		params.put("project[name]", name);
		
		HttpResponse response =  performPost(projectsPath, params);
		if(response.getStatusLine().getStatusCode() == 200){
			try{
				JSONObject projectJson = extractJsonObject(response);
				return projectJson.getInt("id");
			}catch(JSONException je){
				throw new ServerException("Server returned unexpected data");
			}
		}else{
			throw new ServerException("Response code:"+response.getStatusLine());
		}
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
	public JoinStatus[] addMember(int projectId, String emails[]) throws ServerException, ConnectionFailException{
		HashMap<String, String> params = makeTokenHash(); 
		
		for(int i=0; i< emails.length; i++){
			params.put("email["+i+"]", emails[i]);
		}
		
		HttpResponse response = performPost(baseURL + String.format(addMemberPath, projectId), params);
		
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
