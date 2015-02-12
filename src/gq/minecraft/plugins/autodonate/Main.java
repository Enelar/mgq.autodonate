package gq.minecraft.plugins.autodonate;
 
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
 
public class Main extends JavaPlugin implements Listener {
   public static final Logger _log = Logger.getLogger("Minecraft");
 
   @Override
   public void onEnable() {
	   _log.info("[MGQ][Donut] Enabled");
       planner p = new planner();
   }
   
   @Override
   public void onDisable() {
	   _log.info("[MGQ][Donut] Disabled");
   }
}