package tw.jouou.aRoundTable.lib;

import java.io.IOException;
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
	private static final String baseURL = "http://api.hime.loli.tw";
	private static final String projectsPath = "/projects";	
	private static final String addMemberPath = "/projects/%d/users";
	
	private String token;
	
	public ArtApi(String token){
		this.token = token;		
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
	public Project[] getProjectList() throws ServerException, IOException{
		HashMap<String, String> params = makeTokenHash();
		Project r[];
		try {
			HttpResponse response = performGet(projectsPath, params);
			if(response.getStatusLine().getStatusCode() == 200){
				
				JSONArray projects = new JSONArray(EntityUtils.toString(response.getEntity()));			
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
	public Project getProject(int projectId) throws IOException, ServerException{
		HashMap<String, String> params = makeTokenHash();
		HttpResponse response = performGet(projectsPath + "/" + projectId, params);
		if(response.getStatusLine().getStatusCode() == 200){
			try {
				JSONObject projectJson = new JSONObject(EntityUtils.toString(response.getEntity()));
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
	public TaskEvent[] getTaskeventList(int projectId) throws IOException, ServerException{
		HashMap<String, String> params = makeTokenHash();
		HttpResponse response = performGet(projectsPath + "/" + projectId + "/taskevents", params);
		TaskEvent[] taskEvents;
		JSONObject taskEventJson = null;
		if(response.getStatusLine().getStatusCode() == 200){
			try {
				JSONArray taskEventsJson = new JSONArray(EntityUtils.toString(response.getEntity()));

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
	public int createTaskevent(int projectId, String name, Date due, String note) throws IOException, ServerException{
		HashMap<String, String> params = makeTokenHash();

		params.put("taskevent[name]", name);
		params.put("taskevent[due]", due.toString());
		params.put("taskevent[note]", note);
		
		HttpResponse response =  performPost(projectsPath + "/" + projectId + "/taskevents", params);
		if(response.getStatusLine().getStatusCode() == 200){
			try{
				String taskeventJson = EntityUtils.toString(response.getEntity());
				JSONObject projectJson = new JSONObject(taskeventJson);
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
	 * @return new project's id, -1 if failed
	 * @throws ServerException 
	 */
	// FIXME: Internal Server Error 500
	public int createProject(String name) throws IOException, ServerException{
		HashMap<String, String> params = makeTokenHash();
		params.put("project[name]", name);
		
		HttpResponse response =  performPost(projectsPath, params);
		if(response.getStatusLine().getStatusCode() == 200){
			try{
				String post_json = EntityUtils.toString(response.getEntity());
				JSONObject projectJson = new JSONObject(post_json);
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
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public JoinStatus[] addMember(int projectId, String emails[]) throws ClientProtocolException, IOException{
		HashMap<String, String> params = makeTokenHash(); 
		
		for(int i=0; i< emails.length; i++){
			params.put("email["+i+"]", emails[i]);
		}
		
		HttpResponse response = performPost(baseURL + String.format(addMemberPath, projectId), params);
		
		try {
			
			JSONArray respArr = new JSONArray(EntityUtils.toString(response.getEntity()));
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
	private HttpResponse performPost(String path, HashMap<String, String> params) throws ClientProtocolException, IOException{
		HttpPost post = new HttpPost(baseURL + path);
				
		// Perform request
		post.setEntity(new UrlEncodedFormEntity(prepareParams(params), "UTF-8"));
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		return httpClient.execute(post);
	}
	
	private HttpResponse performGet(String path, HashMap<String, String> params) throws ClientProtocolException, IOException{
		HttpGet get = new HttpGet(baseURL + path + "?" + URLEncodedUtils.format(prepareParams(params), "UTF-8"));
		DefaultHttpClient httpClient = new DefaultHttpClient();
		return httpClient.execute(get);
	}
	
	private List<NameValuePair> prepareParams(HashMap<String, String> params){		
		// Prepare parameters
		List<NameValuePair> httpParams = new ArrayList<NameValuePair>();
		for(String key: params.keySet()){
			httpParams.add(new BasicNameValuePair(key, params.get(key)));
		}
		
		return httpParams;
	}
}
