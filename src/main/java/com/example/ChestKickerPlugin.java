package com.example;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import javax.inject.Inject;

import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;

@Slf4j
@PluginDescriptor(
	name = "Chest Kicker"
)
public class ChestKickerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ChatMessageManager chatMessageManager;

	private LocalPoint chestLocation;
	private enum ChestType {
		STANDARD, MOONS, GAUNTLET
	};
	ChestType chestType;

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked e)
	{
		Player player = client.getLocalPlayer();
		WorldView wv = player.getWorldView();

		setChestType(e);

		if
		(chestType != null)
		{
			chestLocation = LocalPoint.fromScene(e.getParam0(), e.getParam1(), wv);
		}
		else
		{
			chestLocation = null;
		}
	}

	@Subscribe
	public void onClientTick(ClientTick e) {
		Player player = client.getLocalPlayer();

		int openChestAnimationID = 536;

		if (chestLocation != null && chestType != null) {
			// TODO: figure out a better way to calculate distance that works for objects large than 1x1
			switch (chestType)
			{
				case GAUNTLET:
					int currentAnimation = player.getAnimation();
					if (currentAnimation == openChestAnimationID)
					{
						playKick();
					}
					break;
				case MOONS:
					if (chestLocation.distanceTo(player.getLocalLocation()) <= 2 * Perspective.LOCAL_TILE_SIZE)
					{
						playKick();
					}
					break;
				case STANDARD:
					if (chestLocation.distanceTo(player.getLocalLocation()) <= Perspective.LOCAL_TILE_SIZE)
					{
						playKick();
					}
					break;
			}
		}
	}

	public void setChestType(MenuOptionClicked e){
		final int gauntletChestId = 37341;
		final int moonsChestId = 51346;

		if (e.getMenuOption().equals("Open") && (e.getId() == gauntletChestId))
		{
			chestType = ChestType.GAUNTLET;
		}
		else if (e.getMenuOption().equals("Claim") && e.getId() == moonsChestId)
		{
			chestType = ChestType.MOONS;
		}
		else if (e.getMenuOption().equals("Open") && e.getMenuTarget().endsWith("Chest"))
		{
			chestType = ChestType.STANDARD;
		}
		else
		{
			chestType = null;
		}
	}

	public void playKick(){
		Player player = client.getLocalPlayer();
		int kickAnimationID = 423;

		player.setAnimation(kickAnimationID);
		player.setAnimationFrame(0);
		chestLocation = null;
	}
}

