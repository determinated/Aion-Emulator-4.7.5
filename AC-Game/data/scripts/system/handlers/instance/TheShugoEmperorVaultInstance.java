/*
 * This file is part of Encom. **ENCOM FUCK OTHER SVN**
 *
 *  Encom is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Encom is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser Public License
 *  along with Encom.  If not, see <http://www.gnu.org/licenses/>.
 */
package instance;

import java.util.*;
import java.util.concurrent.Future;

import javolution.util.*;

import com.aionemu.commons.utils.Rnd;

import com.aionemu.gameserver.ai2.*;
import com.aionemu.gameserver.ai2.manager.*;
import com.aionemu.gameserver.instance.handlers.*;
import com.aionemu.gameserver.controllers.effect.*;
import com.aionemu.gameserver.model.*;
import com.aionemu.gameserver.model.drop.DropItem;
import com.aionemu.gameserver.model.gameobjects.*;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancereward.*;
import com.aionemu.gameserver.model.instance.playerreward.*;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.network.aion.serverpackets.*;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.services.drop.DropRegistrationService;
import com.aionemu.gameserver.utils.*;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.knownlist.Visitor;

/****/
/** Author Rinzler (Encom)
/****/

@InstanceID(301400000)
public class TheShugoEmperorVaultInstance extends GeneralInstanceHandler
{
	private int rank;
	private Race spawnRace;
	private long instanceTime;
	private boolean isInstanceDestroyed;
	private Map<Integer, StaticDoor> doors;
	private ShugoEmperorVaultReward instanceReward;
	private final FastList<Future<?>> vaultTask = FastList.newInstance();
	private FastMap<Integer, VisibleObject> objects = new FastMap<Integer, VisibleObject>();
	
	protected ShugoEmperorVaultPlayerReward getPlayerReward(Integer object) {
		return (ShugoEmperorVaultPlayerReward) instanceReward.getPlayerReward(object);
	}
	
	@SuppressWarnings("unchecked")
	protected void addPlayerReward(Player player) {
		instanceReward.addPlayerReward(new ShugoEmperorVaultPlayerReward(player.getObjectId()));
	}
	
	private boolean containPlayer(Integer object) {
		return instanceReward.containPlayer(object);
	}
	
	@Override
	public InstanceReward<?> getInstanceReward() {
		return instanceReward;
	}
	
	public void onDropRegistered(Npc npc) {
		Set<DropItem> dropItems = DropRegistrationService.getInstance().getCurrentDropMap().get(npc.getObjectId());
		int npcId = npc.getNpcId();
		int index = dropItems.size() + 1;
		switch (npcId) {
			case 235643: //Indirunerk Jonakak's Supply Box.
				for (Player player: instance.getPlayersInside()) {
				    if (player.isOnline()) {
					    dropItems.add(DropRegistrationService.getInstance().regDropItem(index++, player.getObjectId(), npcId, 162002031, 2)); //Shugo Warrior's Minor Salve.
						dropItems.add(DropRegistrationService.getInstance().regDropItem(index++, player.getObjectId(), npcId, 162002032, 2)); //Shugo Warrior's Greater Salve.
						dropItems.add(DropRegistrationService.getInstance().regDropItem(index++, player.getObjectId(), npcId, 162002033, 2)); //Shugo Warrior's Minor Adrenaline.
						dropItems.add(DropRegistrationService.getInstance().regDropItem(index++, player.getObjectId(), npcId, 162002034, 2)); //Shugo Warrior's Greater Adrenaline.
						dropItems.add(DropRegistrationService.getInstance().regDropItem(index++, player.getObjectId(), npcId, 162002035, 2)); //Shugo Warrior's Minor Salve.
						dropItems.add(DropRegistrationService.getInstance().regDropItem(index++, player.getObjectId(), npcId, 162002036, 2)); //Shugo Warrior's Greater Salve.
					}
				}
			break;
		}
	}
	
	private void removeItems(Player player) {
		Storage storage = player.getInventory();
		storage.decreaseByItemId(185000222, storage.getItemCountByItemId(185000222)); //Rusted Vault Key.
		storage.decreaseByItemId(162002031, storage.getItemCountByItemId(162002031)); //Shugo Warrior's Minor Salve.
		storage.decreaseByItemId(162002032, storage.getItemCountByItemId(162002032)); //Shugo Warrior's Greater Salve.
		storage.decreaseByItemId(162002033, storage.getItemCountByItemId(162002033)); //Shugo Warrior's Minor Adrenaline.
		storage.decreaseByItemId(162002034, storage.getItemCountByItemId(162002034)); //Shugo Warrior's Greater Adrenaline.
		storage.decreaseByItemId(162002035, storage.getItemCountByItemId(162002035)); //Shugo Warrior's Minor Salve.
		storage.decreaseByItemId(162002036, storage.getItemCountByItemId(162002036)); //Shugo Warrior's Greater Salve.
	}
	
	private void SpawnRaceInstance() {
		final int templarerk1 = spawnRace == Race.ASMODIANS ? 833494 : 833491; //Brave Templarerk's Soul.
        final int gladiatorerk1 = spawnRace == Race.ASMODIANS ? 833495 : 833492; //Furious Gladiatorerk's Soul.
        final int sorcererk1 = spawnRace == Race.ASMODIANS ? 833496 : 833493; //Roiling Sorcererk's Soul.
		spawn(templarerk1, 540.7726f, 306.14774f, 400.5f, (byte) 91);
		spawn(gladiatorerk1, 547.18335f, 306.39987f, 400.30103f, (byte) 93);
		spawn(sorcererk1, 553.35895f, 305.98343f, 400.45993f, (byte) 90);
    }
	
	@Override
	public void onDie(Npc npc) {
		int points = 0;
		int npcId = npc.getNpcId();
		Player player = npc.getAggroList().getMostPlayerDamage();
		switch (npc.getObjectTemplate().getTemplateId()) {
		    case 235629: //Intruder Skirmisher.
			case 235630: //Intruder Scout.
				points = 180;
				despawnNpc(npc);
			break;
			case 235631: //Brainwashed Peon.
			    points = 160;
				despawnNpc(npc);
			break;
			case 235633: //Intruder Marksman.
			    points = 1070;
				despawnNpc(npc);
			break;
			case 235634: //Watchman Hokuruki.
				points = 2040;
				despawnNpc(npc);
				spawn(832925, 469.62042f, 657.500f, 396.950f, (byte) 105);// Opened Vault Door
				spawn(832919, 464.49826f, 641.73578f, 395.51651f, (byte) 111);// Healing Spring
				spawn(832924, 549.01807f, 313.47986f, 400.34335f, (byte) 89, 433);// Start Point Vault Door
				sendMsg("Use the open entrance to move to the next area.");
				sendPacket(0, 0);			
			break;
			case 235635: //Intruder Challenger.
			case 235650: //Intruder Assassin.
				points = 700;
				despawnNpc(npc);
			break;
			case 235637: //Intruder Guard.
				points = 820;
				despawnNpc(npc);
			break;
			case 235640: //Captain Mirez.
				points = 12000;
				despawnNpc(npc);
				sendMsg("Gradi's second officer has appeared! Prepare for Longknife Zodica!");
				sendPacket(0, 0);
				sendMsg("The Second Henchman of Gradi appears!");
				sendPacket(0, 0);
				spawn(235685, 360.03033f, 757.95233f, 398.42203f, (byte) 104); //Longknife Zodica.
			break;
			case 235641: //Shugo Turncoat.
				points = 660;
				doors.get(428).setOpen(true);
				despawnNpc(npc);
			break;
			case 235647: //Grand Commander Gradi.
				points = 400000;
				despawnNpc(npc);
				//All the intruders have fled. You've cleared the Vault!
				sendMsgByRace(1402681, Race.PC_ALL, 2000);
                spawn(832932, 360.03033f, 757.95233f, 398.42203f, (byte) 104); //The Shugo Emperor's Butler.
				ThreadPoolManager.getInstance().schedule(new Runnable() {
					@Override
					public void run() {
					    instance.doOnAllPlayers(new Visitor<Player>() {
						    @Override
						    public void visit(Player player) {
							    stopInstance(player);
						    }
					    });
					}
				}, 3000);
			break;
			case 235649: //Intruder Sniper.
				points = 760;
				despawnNpc(npc);
			break;
			case 235651: //Intruder Gladiator.
				points = 1400;
				despawnNpc(npc);
			break;
			case 235652: //Intruder Warrior.
			case 235653: //Intruder Sharpeye.
				points = 250;
				despawnNpc(npc);
			break;
			case 235660: //Ruthless Jabaraki.
				points = 1740;
				spawn(235643, 530.12891f, 555.79938f, 396.84256f, (byte) 90); //Indirunerk Jonakak's Supply Box.
				doors.get(431).setOpen(true);
				despawnNpc(npc);
				//Gradi's first officer has appeared! Prepare for Captain Mirez!
				sendMsgByRace(1402678, Race.PC_ALL, 0);
				spawn(235640, 360.03033f, 757.95233f, 398.42203f, (byte) 104); //Captain Mirez.
			break;
			case 235680: //Intruder Brawler.
			case 235681: //Intruder Lookout.
				points = 530;
				despawnNpc(npc);
			break;
			case 235683: //Elite Captain Rupasha.
				points = 272000;
				despawnNpc(npc);
				sendMsg("Gradi, the intruder commander, has appeared. Get ready for a fight!");
				sendPacket(0, 0);
				sendMsg("The Fifth Henchman of Gradi appears!");
				sendPacket(0, 0);
				spawn(235647, 360.03033f, 757.95233f, 398.42203f, (byte) 104); //Grand Commander Gradi.
			break;
			case 235684: //Sorcerer Budyn.
				points = 48000;
				despawnNpc(npc);
				sendMsg("Gradi's final officer has appeared! Prepare for Elite Captain Rupasha!");
				sendPacket(0, 0);
				//The Fourth Henchman of Gradi appears!
				sendMsg("The Fourth Henchman of Gradi appears!");
				sendPacket(0, 0);
				spawn(235683, 360.03033f, 757.95233f, 398.42203f, (byte) 104); //Elite Captain Rupasha.
			break;
			case 235685: //Longknife Zodica.
				points = 14400;
				despawnNpc(npc);
				//Gradi's third officer has appeared! Prepare for Sorcerer Budyn!
				sendMsgByRace(1402680, Race.PC_ALL, 0);
				sendMsg("The Third Henchman of Gradi appears!");
				sendPacket(0, 0);
				spawn(235684, 360.03033f, 757.95233f, 398.42203f, (byte) 104); //Sorcerer Budyn.
			break;
		} if (instanceReward.getInstanceScoreType().isStartProgress()) {
			instanceReward.addNpcKill();
			instanceReward.addPoints(points);
			sendPacket(npc.getObjectTemplate().getNameId(), points);
		}
	}
	
	private void removeEffects(Player player) {
		PlayerEffectController effectController = player.getEffectController();
		effectController.removeEffect(21829);
		effectController.removeEffect(21830);
		effectController.removeEffect(21831);
		effectController.removeEffect(21832);
		effectController.removeEffect(21833);
		effectController.removeEffect(21834);
	}
	
	@Override
	public void onLeaveInstance(Player player) {
		removeItems(player);
		removeEffects(player);
		//"Player Name" has left the battle.
		PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400255, player.getName()));
	}
	
	@Override
	public void onPlayerLogOut(Player player) {
		removeItems(player);
		removeEffects(player);
	}
	
	private int getTime() {
		long result = System.currentTimeMillis() - instanceTime;
		if (result < 48000) {
			return (int) (48000 - result);
		} else if (result < 480000) { //8 Minutes.
			return (int) (480000 - (result - 48000));
		}
		return 0;
	}
	
	private void sendPacket(final int nameId, final int point) {
		instance.doOnAllPlayers(new Visitor<Player>() {
			@Override
			public void visit(Player player) {
				if (nameId != 0) {
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400237, new DescriptionId(nameId * 2 + 1), point));
				}
				PacketSendUtility.sendPacket(player, new SM_INSTANCE_SCORE(getTime(), instanceReward, null));
			}
		});
	}
	
	private int checkRank(int totalPoints) {
		if (totalPoints > 471200) { //Rank S.
			rank = 1;
		} else if (totalPoints > 233700) { //Rank A.
			rank = 2;
		} else if (totalPoints > 86400) { //Rank B.
			rank = 3;
		} else if (totalPoints > 52100) { //Rank C.
			rank = 4;
		} else if (totalPoints > 180) { //Rank D.
			rank = 5;
		} else if (totalPoints >= 0) { //Rank F.
			rank = 8;
		} else {
			rank = 8;
		}
		return rank;
	}
	
	protected void startInstanceTask() {
    	instanceTime = System.currentTimeMillis();
		vaultTask.add(ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
				doors.get(430).setOpen(true);
				instanceReward.setInstanceScoreType(InstanceScoreType.START_PROGRESS);
				sendPacket(0, 0);
            }
        }, 48000));
		vaultTask.add(ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
				instance.doOnAllPlayers(new Visitor<Player>() {
				    @Override
				    public void visit(Player player) {
					    stopInstance(player);
				    }
			    });
				spawn(832950, 362.71112f, 760.5198f, 398.42203f, (byte) 104); //The Shugo Emperor's Exit.
            }
        }, 480000));
    }
	
	@Override
	public void onEnterInstance(final Player player) {
		if (!instanceReward.containPlayer(player.getObjectId())) {
			addPlayerReward(player);
		}
		ShugoEmperorVaultPlayerReward playerReward = getPlayerReward(player.getObjectId());
		if (playerReward.isRewarded()) {
			doReward(player);
		} if (spawnRace == null) {
			spawnRace = player.getRace();
			SpawnRaceInstance();
		}
		sendPacket(0, 0);
	}
	
	protected void stopInstance(Player player) {
        stopInstanceTask();
        instanceReward.setRank(6);
		instanceReward.setRank(checkRank(instanceReward.getPoints()));
		instanceReward.setInstanceScoreType(InstanceScoreType.END_PROGRESS);
		doReward(player);
		sendPacket(0, 0);
	}
	
	private void rewardGroup() {
		for (Player p: instance.getPlayersInside()) {
			doReward(p);
		}
	}
	
	@Override
	public void doReward(Player player) {
		ShugoEmperorVaultPlayerReward playerReward = getPlayerReward(player.getObjectId());
		if (!playerReward.isRewarded()) {
			playerReward.setRewarded();
			int vaultRank = instanceReward.getRank();
			switch (vaultRank) {
				case 1: //Rank S
					playerReward.setRustedVaultKey(7);
					ItemService.addItem(player, 185000222, 7); //Rusted Vault Key.
				break;
				case 2: //Rank A
					playerReward.setRustedVaultKey(3);
					ItemService.addItem(player, 185000222, 3); //Rusted Vault Key.
				break;
				case 3: //Rank B
					playerReward.setRustedVaultKey(2);
					ItemService.addItem(player, 185000222, 2); //Rusted Vault Key.
				break;
				case 4: //Rank C
					playerReward.setRustedVaultKey(1);
					ItemService.addItem(player, 185000222, 1); //Rusted Vault Key.
				break;
				case 5: //Rank D
				break;
				case 6: //Rank F
				break;
			}
		}
	}
	
	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		instanceReward = new ShugoEmperorVaultReward(mapId, instanceId);
		instanceReward.setInstanceScoreType(InstanceScoreType.PREPARING);
		doors = instance.getDoors();
		startInstanceTask();
	}
	
	private void stopInstanceTask() {
        for (FastList.Node<Future<?>> n = vaultTask.head(), end = vaultTask.tail(); (n = n.getNext()) != end; ) {
            if (n.getValue() != null) {
                n.getValue().cancel(true);
            }
        }
    }
	
	@Override
	public void onInstanceDestroy() {
		stopInstanceTask();
		isInstanceDestroyed = true;
		instanceReward.clear();
		doors.clear();
	}
	
	protected void despawnNpc(Npc npc) {
        if (npc != null) {
            npc.getController().onDelete();
        }
    }
	
	private void sendMsg(final String str) {
		instance.doOnAllPlayers(new Visitor<Player>() {
			@Override
			public void visit(Player player) {
				PacketSendUtility.sendMessage(player, str);
			}
		});
	}
	
	protected void sendMsgByRace(final int msg, final Race race, int time) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				instance.doOnAllPlayers(new Visitor<Player>() {
					@Override
					public void visit(Player player) {
						if (player.getRace().equals(race) || race.equals(Race.PC_ALL)) {
							PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(msg));
						}
					}
				});
			}
		}, time);
	}
	
	@Override
    public boolean onReviveEvent(Player player) {
		player.getGameStats().updateStatsAndSpeedVisually();
		PlayerReviveService.revive(player, 100, 100, false, 0);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME);
        return TeleportService2.teleportTo(player, mapId, instanceId, 549.35394f, 300.31052f, 399.91537f, (byte) 30);
    }
	
	@Override
	public boolean onDie(final Player player, Creature lastAttacker) {
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.DIE, 0, player.equals(lastAttacker) ? 0 : lastAttacker.getObjectId()), true);
		PacketSendUtility.sendPacket(player, new SM_DIE(player.haveSelfRezEffect(), player.haveSelfRezItem(), 0, 8));
		return true;
	}
}