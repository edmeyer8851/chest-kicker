package com.example;

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
import net.runelite.api.gameval.ObjectID;
import java.util.Map;

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

	private static final Map<Integer, String> chestMenuOptions = Map.of(
			ObjectID.CHEST_SUN01_DEFAULT01, "Claim",
			ObjectID.GAUNTLET_CHEST, "Open",
			ObjectID.BARROWS_STONE_CHEST, "Open",
			ObjectID.TOB_TREASUREROOM_CHEST_MINE_RARE, "Open",
			ObjectID.TOB_TREASUREROOM_CHEST_MINE_STANDARD, "Open",
			ObjectID.TOA_VAULT_CHEST_NOTMINE_STANDARD, "Open",
			ObjectID.TOA_VAULT_CHEST_MINE_STANDARD, "Open"
	);

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked e)
	{
		// set the supportedKick value to a non-null when you interact with the chest that is supported
		int target = e.getId();

		if (chestMenuOptions.containsKey(target))
		{
			String option = Text.removeFormattingTags(e.getMenuOption());

			if (chestMenuOptions.get(target).equals(option))
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

		if (chestMenuOptions.containsKey(objectId)) {
			genericChest = gameObject;
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		GameObject gameObject = event.getGameObject();
		int objectId = gameObject.getId();

		if (chestMenuOptions.containsKey(objectId)) {
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
