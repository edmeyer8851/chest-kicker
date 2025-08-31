package com.example;

import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import java.util.Arrays;

@Slf4j
@PluginDescriptor(
	name = "Chest Kicker"
)

public class ChestKickerPlugin extends Plugin
{
	@Inject
	private Client client;

	private LocalPoint supportedKick;
	private GameObject genericChest;

	// TODO: make this not dumb
	List<String> kickOptions = Arrays.asList("Claim", "Open", "Open", "Open", "Open", "Open", "Open");
	// 37341: cg chest, 51346 moons, 20973 barrows, 32993 tob, 32992 tob, 41696 toa, 44786 toa
	List<Integer> chestsToKick = Arrays.asList(51346, 37341, 20973, 32993, 32992, 41696, 44786);

	@Override
	public void shutDown()
	{
		kickOptions = null;
		chestsToKick = null;
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked e)
	{
		// set the supportedKick value to a non-null when you interact with the chest that is supported
		int target = e.getId();
		int index = chestsToKick.indexOf(target);

		if (index != -1)
		{
			String option = Text.removeFormattingTags(e.getMenuOption());

			if (kickOptions.get(index).equals(option))
			{
				supportedKick = LocalPoint.fromScene(e.getParam0(), e.getParam1(), client.getLocalPlayer().getWorldView());
			}
		}
		else
		{
			supportedKick = null;
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		// assign the genericChest to the chest that is spawned so that we can compare our distance to it
		GameObject gameObject = event.getGameObject();
		int objectId = gameObject.getId();

		if (chestsToKick.contains(objectId)) {
			genericChest = gameObject;
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		GameObject gameObject = event.getGameObject();
		int objectId = gameObject.getId();

		if (chestsToKick.contains(objectId)) {
			genericChest = null;
		}
	}

	public void playKick()
	{
		Player player = client.getLocalPlayer();
		player.setAnimation(423);
		player.setAnimationFrame(0);
		supportedKick = null;
	}

	@Subscribe
	public void onClientTick(ClientTick e)
	{

		if (genericChest != null && supportedKick != null)
		{
			Player player = client.getLocalPlayer();
			WorldPoint chestBasePoint = genericChest.getWorldLocation();
			int sizeX = genericChest.sizeX();
			int sizeY = genericChest.sizeY();
			WorldArea chestArea = new WorldArea(chestBasePoint, sizeX, sizeY);

			// the distance to check and when to kick differs depending on which chest we're working with
			switch (genericChest.getId()){
				/*
				 	barrows gets its own case because there is no vanilla open animation.
				 	not really happy with this, but it's hard to time the kick to look natural. This is the best I've come up with so far.
				 */
				case 20973:
					WorldPoint barrowsChestEastTile = new WorldPoint(3551, 9694, 0);
					WorldPoint barrowsChestWestTile = new WorldPoint(3552, 9694, 0);
					if (chestArea.distanceTo(player.getWorldArea()) == 1)
					{
						if (player.getWorldLocation().equals(barrowsChestEastTile) || player.getWorldLocation().equals(barrowsChestWestTile))
						{
							playKick();
						}
					}

					break;
				// idk why the moons distance is 2, but it is. Also has no open animation
				case 51346:
					if (chestArea.distanceTo(player.getWorldArea()) == 2)
					{
						playKick();
					}
					break;
				/*
					all the other chests play an open animation that we need to wait for before kicking.
					could add regular scenery chests here in the future
				*/
				default:
					if (chestArea.distanceTo(player.getWorldArea()) == 1 && player.getAnimation() == 536)
					{
						playKick();
					}
			}
		}
	}
}
