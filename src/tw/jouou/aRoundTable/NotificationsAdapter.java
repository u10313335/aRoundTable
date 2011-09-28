package tw.jouou.aRoundTable;

import java.sql.SQLException;
import java.util.List;

import tw.jouou.aRoundTable.bean.Notification;
import tw.jouou.aRoundTable.bean.User;
import tw.jouou.aRoundTable.util.DBUtils;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

//FIXME: dirty implementation
public class NotificationsAdapter extends BaseAdapter implements OnItemClickListener {
	private Context context;
	private List<Notification> notifications;
	private LayoutInflater inflater;
	private DBUtils dbUtils;

	public NotificationsAdapter(Context context) throws SQLException {
		this.context = context;
		this.notifications = DBUtils.getInstance(context.getApplicationContext()).notificationDao.queryForAll();
		this.inflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.dbUtils = DBUtils.getInstance(context);
	}
	
	public View	getView(int position, View convertView, ViewGroup parent){
		Notification notification = notifications.get(position);
		final User user = findUser(notification);
		
		if(convertView == null)
			convertView = inflater.inflate(R.layout.notification_item, parent, false);
		
		final ImageView avatarImageView = (ImageView) convertView.findViewById(R.id.avatar);
		
		String message = notification.message;
		if(user != null && user.name != null){
			message = user.name + " " + message;
		}
		((TextView) convertView.findViewById(R.id.notificaton_context)).setText(message);
		
		new Thread(new Runnable() {	
			@Override
			public void run() {
				avatarImageView.setImageBitmap(user.getGravatar());
			}
		}).run();
		
		return convertView;
	}

	@Override
	public int getCount() {
		return notifications.size();
	}

	@Override
	public Object getItem(int position) {
		return notifications.get(position);
	}

	@Override
	public long getItemId(int position) {
		return notifications.get(position).id;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Notification notification = notifications.get(position);
		if(notification.notifiableType == "Task"){
			
		}else if(notification.notifiableType == "Event"){
			
		}else if(notification.notifiableType == "Project"){
			
		}else if(notification.notifiableType == "Notepad"){
			 
		}
			
	}
	private User findUser(Notification notification){
		List<User> users;
		try {
			users = dbUtils.userDao.queryForEq("server_id", notification.userId);
			if(users != null && users.size() != 0)
				return users.get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
