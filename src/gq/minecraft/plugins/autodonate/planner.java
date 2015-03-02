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
import java.util.TreeMap;
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
		commands = new TreeMap<Integer, command>();
		reports = new Vector<Integer>();
	}
	
	TreeMap<Integer, command> commands;
	Vector<Integer> reports;
	
	public void Report(Integer id) {
		// Lock should be already accured!!
		Main._log.info("Reporting " + id);
		reports.add(id);
		commands.remove(id);
	}
	
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
				request.Api(report_url, id.toString()).Result();
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
			String result = request.Api(tasks_url, "152").Result();
			JSONParser parser = new JSONParser(); 
			newobj = new JSONObject(result);
			
			Iterator<String> it = newobj.keySet().iterator(); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		if (newobj.isNull("cache"))
			Main._log.info("IS NULL...");
		if (newobj.isNull("data"))
			return;

		JSONObject data = newobj.getJSONObject("data");
		if (data == null)
			return;

		Vector<command> tasks = JSONToCommands(data.getJSONArray("commands"));
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
