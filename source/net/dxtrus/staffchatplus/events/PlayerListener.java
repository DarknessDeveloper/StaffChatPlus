package net.dxtrus.staffchatplus.events;

import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.dxtrus.staffchatplus.StaffChatPlus;

public class PlayerListener implements Listener {

	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {

		StaffChatPlus main = StaffChatPlus.getInstance();

		if (!main.isInStaffChannel(e.getPlayer()))
			return;
		
		e.setCancelled(true);
		
		String channel = StaffChatPlus.getPlayerChannels().get(e.getPlayer());
		String message = StaffChatPlus.i18n("messages.format", e.getPlayer().getName(), channel, e.getMessage());
		
		for (Entry<Player,String> channels : StaffChatPlus.getPlayerChannels().entrySet()) {
			if (channels.getValue().equalsIgnoreCase(channel))
				channels.getKey().sendMessage(message);
		}
		
		Bukkit.getConsoleSender().sendMessage(message);

	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		if (!StaffChatPlus.getInstance().channelPersistence())
			return;
		
		FileConfiguration config = StaffChatPlus.getInstance().getUsersConfig();
		
		if (!config.getBoolean(e.getPlayer().getUniqueId() + ".is-in-channel", false))
			return;
		
		String channel = config.getString(e.getPlayer().getUniqueId() + ".channel");
		if (!StaffChatPlus.getInstance().channelExists(channel))
		{
			config.set(e.getPlayer().getUniqueId() + ".is-in-channel", false);
			StaffChatPlus.getInstance().saveConfig();
			
			e.getPlayer().sendMessage(StaffChatPlus.i18n("messages.join.channel-no-longer-exists"));
			return;
		}
		
		StaffChatPlus.getInstance().joinChannel(e.getPlayer(), channel);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		if (StaffChatPlus.getInstance().isInStaffChannel(e.getPlayer()))
			StaffChatPlus.getInstance().leaveStaffChat(e.getPlayer());
	}

}
