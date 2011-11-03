package tw.jouou.aRoundTable;

import java.sql.SQLException;
import java.util.List;

import tw.jouou.aRoundTable.bean.Comment;
import tw.jouou.aRoundTable.bean.Notification;
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
	private Comment[] comments;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_task);
		dbUtils = DBUtils.getInstance(this);
		mBundle = this.getIntent().getExtras();
        mTask = (Task)mBundle.get("task");
        mProject = dbUtils.projectsDelegate.get(mTask.getProjId());
        
		((TextView) findViewById(R.id.view_task_title)).setText("工作: "+mTask.getName());
		((TextView) findViewById(R.id.tx_project)).setText(mProject.getName());
		commentList = (ListView) findViewById(R.id.comments);
		
		// Load comments
		new LoadCommentsTask().execute(mTask);
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
