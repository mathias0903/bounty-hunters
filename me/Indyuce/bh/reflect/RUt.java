package me.Indyuce.bh.reflect;

import org.bukkit.entity.Player;

import me.Indyuce.bh.VersionUtils;

public class RUt {
	public static void sendPacket(Player p, Object packet) {
		try {
			Object handle = p.getClass().getMethod("getHandle").invoke(p);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			playerConnection.getClass().getMethod("sendPacket", nms("Packet")).invoke(playerConnection, packet);
		} catch (Exception e) {
		}
	}

	public static Class<?> nms(String str) throws ClassNotFoundException {
		String name = "net.minecraft.server." + VersionUtils.version + "." + str;
		return Class.forName(name);
	}

	public static Class<?> obc(String str) throws ClassNotFoundException {
		String name = "org.bukkit.craftbukkit." + VersionUtils.version + "." + str;
		return Class.forName(name);
	}

	public static Class<?> chatSerializer() throws ClassNotFoundException {
		if (nms("IChatBaseComponent").getDeclaredClasses().length > 0)
			return nms("IChatBaseComponent").getDeclaredClasses()[0];
		return nms("ChatSerializer");
	}

	public static Class<?> enumTitleAction() throws ClassNotFoundException {
		if (nms("PacketPlayOutTitle").getDeclaredClasses().length > 0)
			return nms("PacketPlayOutTitle").getDeclaredClasses()[0];
		return nms("EnumTitleAction");
	}
}
