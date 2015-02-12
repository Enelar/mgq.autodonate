package gq.minecraft.plugins.autodonate;
 
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.json.JSONArray;
import org.json.JSONObject;
 
public class Main extends JavaPlugin implements Listener {
   public static final Logger _log = Logger.getLogger("Minecraft");
   public planner p;
 
   @Override
   public void onEnable() {
       p = new planner();
       
       BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
       scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
           public void run() {
             if (!p.main_here.tryLock())
               return;
             if (p.commands.size() == 0) {
               p.main_here.unlock();
               return;
             }
             Iterator<Entry<Integer, command>> i =
                p.commands.entrySet().iterator();
             while (i.hasNext()) {
                Entry<Integer, command> entry = i.next();
                if (!DoCommand(entry.getValue()))
                   continue;
                p.Report(entry.getKey());
                break;
             }
             p.main_here.unlock();
           }
       }, 0L, 20L);
   }
   
   @Override
   public void onDisable() {
   }
   
   public Boolean DoCommand(command c) {
	   if (c.type != "GIVE")
		   return false;
	   String base = c.type + " " + c.target + " ";
	   JSONObject items = c.data.getJSONObject("items");
	   JSONArray names = items.names();
	   for (int i = 0; i < names.length(); i++) {
		   String name = names.getString(i);
		   Integer count = items.getInt(name);
		   
		   String cmd = base + name + " " + count;
		   if (cmd.contains("\n"))
			   continue; // Simple command execution protect
		   Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
	   }
	
	   _log.info("[MGQ] Executed GIVE command " + c.id.toString());
	   return true;
   }
}