package tw.jouou.aRoundTable;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import tw.jouou.aRoundTable.bean.Comment;
import tw.jouou.aRoundTable.bean.Project;
import tw.jouou.aRoundTable.bean.Task;
import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.lib.ArtApi;
import tw.jouou.aRoundTable.lib.ArtApi.ConnectionFailException;
import tw.jouou.aRoundTable.lib.ArtApi.NotLoggedInException;
import tw.jouou.aRoundTable.lib.ArtApi.ServerException;
import tw.jouou.aRoundTable.util.DBUtils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DisplayTaskActivity extends Activity {
	private ListView commentList;
	private DBUtils dbUtils;
	private Bundle mBundle;
	private Task mTask;
	private Project mProject;
	private Date mTaskDue;
	private Comment[] comments;
	public static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_task);
		dbUtils = DBUtils.getInstance(this);
		mBundle = this.getIntent().getExtras();
        mTask = (Task)mBundle.get("task");
        mProject = dbUtils.projectsDelegate.get(mTask.getProjId());
        mTaskDue = mTask.getDueDate();
        
		((TextView) findViewById(R.id.view_task_title)).setText(getString(R.string.task) + mTask.getName());
		((TextView) findViewById(R.id.tx_project)).setText(mProject.getName());
    	if(mTaskDue != null) {
    		((TextView) findViewById(R.id.tx_task_due)).setText(formatter.format(mTaskDue));
    	}
    	((TextView) findViewById(R.id.tx_task_owner)).setText(genUserNames(mTask.getServerId()));
    	((TextView) findViewById(R.id.tx_task_remarks)).setText(mTask.getNote());
		commentList = (ListView) findViewById(R.id.comments);
		
		// Load comments
		new LoadCommentsTask().execute(mTask);
	}
	
	private String genUserNames(long taskId){
	     String nameList = "";
	     String[] names = dbUtils.tasksUsersDelegate.getUsersName(taskId);
	     if(names != null) {
	    	 nameList = names[0];
	    	 int i = 1;
	    	 while(i < names.length) {
	    		 nameList = names[i] + ", " + nameList;
	    		 i++;
	    	 }
	     }
	     return nameList;
	}

	class LoadCommentsTask extends AsyncTask<Task, Void, Void>{
		private ProgressDialog dialog;
		
		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(DisplayTaskActivity.this);
			dialog.setMessage(getString(R.string.processing));
			dialog.show();
		}
		
		@Override
		protected Void doInBackground(Task... params) {
			try {
				comments = ArtApi.getInstance(DisplayTaskActivity.this).getTaskComments(params[0].getServerId());
			} catch (ServerException e) {
				e.printStackTrace();
			} catch (ConnectionFailException e) {
				e.printStackTrace();
			} catch (NotLoggedInException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			dialog.dismiss();
			if(comments != null)
				commentList.setAdapter(new CommentsAdapter());
		}
	}
	
	class CommentsAdapter extends BaseAdapter{
		private Handler handler = new Handler();
		
		@Override
		public int getCount() {
			return comments.length;
		}

		@Override
		public Object getItem(int position) {
			return comments[position];
		}

		@Override
		public long getItemId(int position) {
			return comments[position].id;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final Comment comment = comments[position];
			final User user = findUser(comment.userId);
			
			if(convertView == null) {
				convertView = DisplayTaskActivity.this.getLayoutInflater().inflate(R.layout.comment_item, parent, false);
			}
			((TextView) convertView.findViewById(R.id.comment_text)).setText(user.name+": "+comment.content);
			final ImageView avatarImageView = (ImageView) convertView.findViewById(R.id.avatar);
			new Thread(new Runnable() {	
				@Override
				public void run() {
					final Bitmap avatar = user.getGravatar();
					handler.post(new Runnable(){
						@Override
						public void run() {
							avatarImageView.setImageBitmap(avatar);	
						}
					});
				}
			}).start();
			return convertView;
		}
		
		private User findUser(int userId){
			try {
				List<User> users = dbUtils.userDao.queryForEq("server_id", userId);
				if(users != null && users.size() != 0)
					return users.get(0);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
