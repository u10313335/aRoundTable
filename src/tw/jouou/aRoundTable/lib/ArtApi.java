package tw.jouou.aRoundTable.lib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
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
	
	public Project[] getProjectList() throws ServerException, IOException{
		HashMap<String, String> params = makeTokenHash();
		Project r[];
		try {
			HttpResponse response = performGet(projectsPath, params);
			if(response.getStatusLine().getStatusCode() == 200){
				
				JSONArray projects = new JSONArray(EntityUtils.toString(response.getEntity()));			
				JSONObject project = null;
				
				r = new Project[projects.length()];
				for(int i=0; i < projects.length(); project = projects.getJSONObject(i)){			
					r[i] = new Project(project.getInt("id"), project.getString("name")); 
				}
				return r;
			}else
				throw new ServerException("Server respond with: "+response.getStatusLine());		
		} catch (JSONException e) {
			throw new ServerException("Server respond with invalid json");
		}
	}
	
	/**
	 * Create a new project with current user
	 * @param name Name of new project
	 * @return new project's id, -1 if failed
	 * @throws ServerException 
	 */
	public int createProject(String name) throws IOException, ServerException{
		HashMap<String, String> params = makeTokenHash();
		params.put("project[name]", name);
		
		HttpResponse response =  performPost(projectsPath, params);
		if(response.getStatusLine().getStatusCode() == 200){
			try{
				String post_json = EntityUtils.toString(response.getEntity());
				JSONObject json1 = new JSONObject(post_json);
				JSONObject json2 = new JSONObject(json1.getString("project"));
				return json2.getInt("id");
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
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
