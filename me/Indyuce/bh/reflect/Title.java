package me.Indyuce.bh.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.bukkit.entity.Player;

import me.Indyuce.bh.VersionUtils;

public class Title {
	public static void title(Player p, String title, String subtitle, int fade, int time) {
		try {
			Object chatTitle = RUt.chatSerializer().getMethod("a", String.class).invoke(null, "{\"text\": \"" + title + "\"}");
			Object chatSubtitle = RUt.chatSerializer().getMethod("a", String.class).invoke(null, "{\"text\": \"" + subtitle + "\"}");

			Constructor<?> constructor = RUt.nms("PacketPlayOutTitle").getConstructor(RUt.enumTitleAction(), RUt.nms("IChatBaseComponent"), int.class, int.class, int.class);
			Object titlePacket = constructor.newInstance(RUt.enumTitleAction().getField("TITLE").get(null), chatTitle, fade, time, fade);
			Object subtitlePacket = constructor.newInstance(RUt.enumTitleAction().getField("SUBTITLE").get(null), chatSubtitle, fade, time, fade);

			RUt.sendPacket(p, titlePacket);
			RUt.sendPacket(p, subtitlePacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void actionBar(Player p, String msg) {
		try {
			Object chatBar = RUt.chatSerializer().getMethod("a", String.class).invoke(null, "{\"text\": \"" + msg + "\"}");
			Constructor<?> constructor = RUt.nms("PacketPlayOutChat").getConstructor(RUt.nms("IChatBaseComponent"), (VersionUtils.isBelow("v1_11") ? byte.class : RUt.nms("ChatMessageType")));
			Object titlePacket = constructor.newInstance(chatBar, (VersionUtils.isBelow("v1_11") ? (byte) 2 : RUt.nms("ChatMessageType").getField("GAME_INFO").get(null)));
			RUt.sendPacket(p, titlePacket);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException | NoSuchFieldException e) {
			e.printStackTrace();
		}
	}
}
