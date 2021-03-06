package gq.minecraft.plugins.autodonate;
 
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
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
   public static final String server_id = "152";
 
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
                _log.info("Finishing command");
                p.Report(entry.getKey());
                break;
             }
             p.main_here.unlock();
           }
       }, 0L, 20L);
       scheduler.scheduleAsyncRepeatingTask(this, new Runnable() {
		public void run() {
			p.ThreadIteration();
		}
       }, 0L, 2000L);
   }
   
   @Override
   public void onDisable() {
   }
   
   public Boolean DoCommand(command c) {	   
	   if (c.type.equals("GIVE"))
		   return GiveCommand(c);
	   if (c.type.equals("V0_GROUP_ADD"))
		   return CardsCommand(c);
	   _log.info("Command not found. Maybe you should update? " + c.type);
	   return false;
   }
   
   private Boolean GiveCommand(command c) {
	   if (c.type != "GIVE")
		   return false;
	   Player player = Bukkit.getPlayerExact(c.target);
	   _log.info(c.target);
	   if (player == null || !player.isOnline())
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
   
   private Boolean CardsCommand(command c) {
	   String group = c.data.getString("group");
	   if (group == null)
	   {
		   _log.info("Card command broken");
		   return false;
	   }
	   String command = "pex user " + c.target + " group add " + group;
	   _log.info(command);
	   Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
	   return true;
   }
}