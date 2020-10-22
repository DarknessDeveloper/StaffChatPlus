package net.dxtrus.staffchatplus.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dxtrus.staffchatplus.StaffChatPlus;

public class CommandStaffChatPlus implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!cmd.getName().equalsIgnoreCase("staffchatplus"))
			return false;

		if (!sender.hasPermission("staffchatplus.use")) {
			sender.sendMessage(StaffChatPlus.i18n("messages.no-permission", "staffchatplus.use"));
			return true;
		}
		if (!(sender instanceof Player)) {

			if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
				if (!sender.hasPermission("staffchatplus.reload")) {
					sender.sendMessage(StaffChatPlus.i18n("messages.no-permission", "staffchatplus.reload"));
					return true;
				}

				StaffChatPlus.getInstance().reloadConfig();
				sender.sendMessage(StaffChatPlus.i18n("messages.commands.reload.success"));
				return true;
			}

			sender.sendMessage(StaffChatPlus.i18n("messages.commands.not-player"));
			return true;
		}

		Player p = (Player) sender;

		if (args.length < 1) {
			if (StaffChatPlus.getInstance().isInStaffChannel(p)) {
				StaffChatPlus.getInstance().leaveStaffChat(p);
				p.sendMessage(StaffChatPlus.i18n("messages.commands.left-staff-chat"));
				return true;
			}

			StaffChatPlus.getInstance().joinChannel(p, "global");
			p.sendMessage(StaffChatPlus.i18n("messages.commands.joined-global-staff-chat"));
			return true;
		}

		if (args[0].equalsIgnoreCase("reload")) {
			if (!p.hasPermission("staffchatplus.reload")) {
				p.sendMessage(StaffChatPlus.i18n("messages.no-permission", "staffchatplus.reload"));
				return true;
			}
			StaffChatPlus.getInstance().reloadConfig();
			p.sendMessage(StaffChatPlus.i18n("messages.commands.reload.success"));
			return true;
		}

		String channel = args[0];
		if (!StaffChatPlus.getInstance().channelExists(channel)) {
			p.sendMessage(StaffChatPlus.i18n("messages.commands.nonexistant-channel", channel.toLowerCase()));
			return true;
		}

		String permission = StaffChatPlus.getInstance().getConfig()
				.getString("channels." + channel.toLowerCase() + ".permission", "staffchatplus.channels." + channel.toLowerCase() + "");
		
		if (!p.hasPermission(permission)) {

			p.sendMessage(StaffChatPlus.i18n("messages.no-channel-permission", permission));
			return true;
			
		}

		StaffChatPlus.getInstance().joinChannel(p, channel);
		p.sendMessage(StaffChatPlus.i18n("messages.commands.joined-channel", channel));
		return true;
	}

}
