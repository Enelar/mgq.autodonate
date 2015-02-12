package gq.minecraft.plugins.autodonate;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.json.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class planner {
	public static final String origin_url = "https://minecraft.gq/api/v0/invoke/";
	public static final String tasks_url = origin_url + "GetTasks";
	public static final String report_url = origin_url + "Report";
	
	public Lock main_here;
	
	public planner() {
		main_here = new ReentrantLock();
	}
	
	Map<Integer, command> commands;
	Vector<Integer> reports;
	
	public void ThreadIteration() {
		ReportComplete();
		AppendCommands();
	}
	
	public void ReportComplete() {
		HTTPRequest request = new HTTPRequest();
		main_here.lock();
		Iterator<Integer> i = reports.iterator();
		while (i.hasNext()) {
			Integer id = i.next();
			try {
				request.Api(report_url, i.toString() + "1");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		reports.clear();
		main_here.unlock();
	}

	public void AppendCommands() {
		HTTPRequest request = new HTTPRequest();
		JSONObject newobj = null;

		try {
			String result = null;
			result = request.Api(tasks_url, "1").Result();
			JSONParser parser = new JSONParser();
			Object javahackoridk = parser.parse(result); 
			newobj = (JSONObject)javahackoridk;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		JSONObject data = newobj.getJSONObject("data");
		if (data == null)
			return;
		Vector<command> tasks = JSONToCommands(newobj.getJSONArray("commands"));
		main_here.lock();
		synchronized (commands) {
			Iterator<command> i = tasks.iterator();
			while (i.hasNext()) {
				command cur = i.next();
				if (commands.containsKey(cur.id))
					continue;
				commands.put(cur.id, cur);
			}
		}
		main_here.unlock();
	}
	
	private Vector<command> JSONToCommands( JSONArray tasks ) {
		Vector<command> ret = new Vector<command>();
		for (int i = 0; i < tasks.length(); i++) {
			JSONObject task = tasks.getJSONObject(i);
			command construct = new command();
			construct.id = task.getInt("id");
			construct.type = task.getString("command");
			construct.target = task.getString("target");
			construct.data = task.getJSONObject("params");
			ret.add(construct);
		}
		return ret;
	}
}
