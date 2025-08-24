package com.example;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.events.ClientTick;

@Slf4j
@PluginDescriptor(
	name = "Chest Kicker"
)
public class ChestKickerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Subscribe
	public void onClientTick(ClientTick e)
	{
		Player local = client.getLocalPlayer();
		int currentAnimation = local.getAnimation();
		int openChestAnimationID = 536;
		int kickAnimationID = 423;
		if (currentAnimation == openChestAnimationID)
		{
			local.setAnimation(kickAnimationID);
			local.setAnimationFrame(0);
		}
	}
}
