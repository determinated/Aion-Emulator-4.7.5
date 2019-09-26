/**
 * This file is part of Aion-Lightning <aion-lightning.org>.
 *
 *  Aion-Lightning is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Aion-Lightning is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details. *
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Aion-Lightning.
 *  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Credits goes to all Open Source Core Developer Groups listed below
 * Please do not change here something, regarding the developer credits, except the "developed by XXXX".
 * Even if you edit a lot of files in this source, you still have no rights to call it as "your Core".
 * Everybody knows that this Emulator Core was developed by Aion Lightning 
 * @-Aion-Unique-
 * @-Aion-Lightning
 * @Aion-Engine
 * @Aion-Extreme
 * @Aion-NextGen
 * @Aion-Core Dev.
 */
package com.aionemu.gameserver.services.base;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.callbacks.EnhancedObject;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AbstractAI;
import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.base.BaseLocation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.npc.NpcTemplateType;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup2;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.BaseService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.spawnengine.SpawnHandlerType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.MapRegion;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Source
 * @author Dision
 * M.O.G. Devs Team
 */
public class Base<BL extends BaseLocation> {

	private Future<?> startAssault, stopAssault;
	private final BL baseLocation;
	private List<Race> list = new ArrayList<Race>();
	private final BossDeathListener bossDeathListener = new BossDeathListener(this);
	private List<Npc> attackers = new ArrayList<Npc>();
	private List<Npc> spawned = new CopyOnWriteArrayList<Npc>();
	private final AtomicBoolean finished = new AtomicBoolean();
	private boolean started;
	private Npc boss, guard, guard_village, guard_rivar, guard_krall, guard_werewolf, portal, flag;

	public Base(BL baseLocation) {
		list.add(Race.ASMODIANS);
		list.add(Race.ELYOS);
		list.add(Race.NPC);
		this.baseLocation = baseLocation;
	}

	public final void start() {

		boolean start = false;

		synchronized (this) {
			if (started) {
				start = true;
			} else {
		  started = true;
			}
		}
		if (start) {
			return;
		}
		spawn();
	}

	public final void stop() {
		if (finished.compareAndSet(false, true)) {
			if (getBoss() != null) {
				rmvBossListener();
			}
			despawn();
		}
	}

	private List<SpawnGroup2> getBaseSpawns() {
		List<SpawnGroup2> spawns = DataManager.SPAWNS_DATA2.getBaseSpawnsByLocId(getId());

		if (spawns == null) {
			throw new NullPointerException("No spawns for base:" + getId());
		}

		return spawns;
	}

	protected void spawn() {
		for (SpawnGroup2 group : getBaseSpawns()) {
			for (SpawnTemplate spawn : group.getSpawnTemplates()) {
				final BaseSpawnTemplate template = (BaseSpawnTemplate) spawn;
				if (template.getBaseRace().equals(getRace())) {
					if (template.getHandlerType() == null) {
						Npc npc = (Npc) SpawnEngine.spawnObject(template, 1);
						NpcTemplate npcTemplate = npc.getObjectTemplate();
						if (npcTemplate.getNpcTemplateType().equals(NpcTemplateType.FLAG)) {
							setFlag(npc);
						}
						getSpawned().add(npc);
					}
				}
			}
		}

		delayedAssault();
		delayedSpawn(getRace());
		delayedSpawnTwo(getRace());
		delayedSpawnThri(getRace());
	}

	private void delayedAssault() {
		startAssault = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				chooseAttackersRace();
			}
		}, Rnd.get(5, 10) * 60000); // Randomly every 15 - 20 min start assault
	}

	private void delayedSpawn(final Race race) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (getRace().equals(race) && getBoss() == null) {
					spawnBoss();
				}
			}
		}, Rnd.get(1, 2) * 60000); // Boss spawn between 30 min and 4 hours delay
	}

	protected void spawnBoss() {
		for (SpawnGroup2 group : getBaseSpawns()) {
			for (SpawnTemplate spawn : group.getSpawnTemplates()) {
				final BaseSpawnTemplate template = (BaseSpawnTemplate) spawn;
				if (template.getBaseRace().equals(getRace())) {
					if (template.getHandlerType() != null && template.getHandlerType().equals(SpawnHandlerType.BOSS)) {
						Npc npc = (Npc) SpawnEngine.spawnObject(template, 1);
						setBoss(npc);
						addBossListeners();
						getSpawned().add(npc);
					}
				}
			}
		}
	}

	protected void chooseAttackersRace() {
        AtomicBoolean next = new AtomicBoolean(Math.random() < 0.5);
        for (Race race : list) {
        	if (race == null) {
                throw new NullPointerException("Base:" + race + " race is null chooseAttackersRace!");
            } else if (!race.equals(getRace())) {
                if (next.compareAndSet(true, false)) {
                    continue;
                }
                
                spawnAttackers(race);
                break;
            }
        }
    }

	private void delayedSpawnTwo(final Race race) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				if (getRace().equals(race) && getGuard() == null) {
					spawnGuard();
				}
				if (getRace().equals(race) && getGuardRivar() == null) {
					spawnGuardRivar();
				}
				if (getRace().equals(race) && getGuardKrall() == null) {
					spawnGuardKrall();
				}
				if (getRace().equals(race) && getGuardWerewolf() == null) {
					spawnGuardWerewolf();
				}
				if (getRace().equals(race) && getPortal() == null) {
					spawnPortal();
				}
			}

		}, 1 * 6000); // Guard 1 min spawn delay
	}

	protected void spawnGuard() {
		for (SpawnGroup2 group : getBaseSpawns()) {
			for (SpawnTemplate spawn : group.getSpawnTemplates()) {
				final BaseSpawnTemplate template = (BaseSpawnTemplate) spawn;
				if (template.getBaseRace().equals(getRace())) {
					if (template.getHandlerType() != null && template.getHandlerType().equals(SpawnHandlerType.GUARD)) {
						Npc npc = (Npc) SpawnEngine.spawnObject(template, 1);
						setGuard(npc);
						getSpawned().add(npc);
					}
				}
			}
		}
	}

	protected void spawnGuardRivar() {
		for (SpawnGroup2 group : getBaseSpawns()) {
			for (SpawnTemplate spawn : group.getSpawnTemplates()) {
				final BaseSpawnTemplate template = (BaseSpawnTemplate) spawn;
				if (template.getBaseRace().equals(getRace())) {

					if (template.getHandlerType() != null && template.getHandlerType().equals(SpawnHandlerType.GUARD_RIVAR)) {
						Npc npc = (Npc) SpawnEngine.spawnObject(template, 1);
						setGuardRivar(npc);
						getSpawned().add(npc);
					}
				}
			}
		}
	}

	protected void spawnGuardKrall() {
		for (SpawnGroup2 group : getBaseSpawns()) {
			for (SpawnTemplate spawn : group.getSpawnTemplates()) {
				final BaseSpawnTemplate template = (BaseSpawnTemplate) spawn;
				if (template.getBaseRace().equals(getRace())) {
					if (template.getHandlerType() != null && template.getHandlerType().equals(SpawnHandlerType.GUARD_KRALL)) {
						Npc npc = (Npc) SpawnEngine.spawnObject(template, 1);
						setGuardKrall(npc);
						getSpawned().add(npc);
					}
				}
			}
		}
	}

	protected void spawnGuardWerewolf() {
		for (SpawnGroup2 group : getBaseSpawns()) {
			for (SpawnTemplate spawn : group.getSpawnTemplates()) {
				final BaseSpawnTemplate template = (BaseSpawnTemplate) spawn;
				if (template.getBaseRace().equals(getRace())) {
					if (template.getHandlerType() != null && template.getHandlerType().equals(SpawnHandlerType.GUARD_WEREWOLF)) {
						Npc npc = (Npc) SpawnEngine.spawnObject(template, 1);
						setGuardWerewolf(npc);
						getSpawned().add(npc);
					}
				}
			}
		}
	}

	private void delayedSpawnThri(final Race race) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				if (getRace().equals(race) && getGuardVillage() == null) {
					spawnGuardVillage();
				}
			}

		}, 2 * 6000); // Guard Village 2 min spawn delay
	}

	protected void spawnGuardVillage() {
		for (SpawnGroup2 group : getBaseSpawns()) {
			for (SpawnTemplate spawn : group.getSpawnTemplates()) {
				final BaseSpawnTemplate template = (BaseSpawnTemplate) spawn;
				if (template.getBaseRace().equals(getRace())) {
					if (template.getHandlerType() != null && template.getHandlerType().equals(SpawnHandlerType.GUARD_VILLAGE)) {
						Npc npc = (Npc) SpawnEngine.spawnObject(template, 1);
						setGuardVillage(npc);
						getSpawned().add(npc);
					}
				}
			}
		}
	}

	protected void spawnPortal() {
		for (SpawnGroup2 group : getBaseSpawns()) {
			for (SpawnTemplate spawn : group.getSpawnTemplates()) {
				final BaseSpawnTemplate template = (BaseSpawnTemplate) spawn;
				if (template.getBaseRace().equals(getRace())) {
					if (template.getHandlerType() != null && template.getHandlerType().equals(SpawnHandlerType.PORTAL)) {
						Npc npc = (Npc) SpawnEngine.spawnObject(template, 1);
						setPortal(npc);
						getSpawned().add(npc);
					}
				}
			}
		}
	}

	public void spawnAttackers(Race race) {
		if (getFlag() == null) {
			throw new NullPointerException("Base:" + getId() + " flag is null!");
		}
		else if (!getFlag().getPosition().getMapRegion().isMapRegionActive()) {
			// 20% chance to capture base in not active region by invaders assault
			if (Math.random() < 0.2) {
				BaseService.getInstance().capture(getId(), race);
			}
			else {
				// Next attack
				delayedAssault();
			}
			return;
		}

		if (!isAttacked()) {
			getAttackers().clear();

			for (SpawnGroup2 group : getBaseSpawns()) {
				for (SpawnTemplate spawn : group.getSpawnTemplates()) {
					final BaseSpawnTemplate template = (BaseSpawnTemplate) spawn;
					if (template.getBaseRace().equals(race)) {
						if (template.getHandlerType() != null && template.getHandlerType().equals(SpawnHandlerType.ATTACKER)) {
							Npc npc = (Npc) SpawnEngine.spawnObject(template, 1);
							getAttackers().add(npc);
						}
					}
				}
			}

			// Since patch 4.7 in kaldor are siege important bases that only have balaur attackers
            // for back occupying.
            if (getAttackers().isEmpty() && !isOnlyBalaur(getId())) {
                throw new NullPointerException("No attackers was found for base:" + getId());
            } else {
                stopAssault = ThreadPoolManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        despawnAttackers();

                        // Next attack
                        delayedAssault();
                    }
                }, 5 * 60000); // After 5 min attackers despawned
            }
        }
    }
		
    public boolean isOnlyBalaur(int id) {
        switch (id) {
            case 90:
                return true;
            case 91:
                return true;
            default:
                return false;
        }
    }

	public boolean isAttacked() {
		for (Npc attacker : getAttackers()) {
			if (!attacker.getLifeStats().isAlreadyDead()) {
				return true;
			}
		}
		return false;
	}

	protected void despawn() {
		setFlag(null);

		for (Npc npc : getSpawned()) {
			npc.getController().cancelTask(TaskId.RESPAWN);
			npc.getController().onDelete();
		}
		getSpawned().clear();

		despawnAttackers();
		if (startAssault != null) {
			startAssault.cancel(true);
		}
		if (stopAssault != null) {
			stopAssault.cancel(true);
		}
	}

	protected void despawnAttackers() {
		for (Npc attacker : getAttackers()) {
			attacker.getController().cancelTask(TaskId.RESPAWN);
			attacker.getController().onDelete();
		}
		getAttackers().clear();
	}

	protected void addBossListeners() {
		AbstractAI ai = (AbstractAI) getBoss().getAi2();
		EnhancedObject eo = (EnhancedObject) ai;
		eo.addCallback(getBossListener());
	}

	protected void rmvBossListener() {
		AbstractAI ai = (AbstractAI) getBoss().getAi2();
		EnhancedObject eo = (EnhancedObject) ai;
		eo.removeCallback(getBossListener());
	}

	/**
	 * @return
	 * @return
	 */
	public Npc getFlag() {
		return flag;
	}

	public void setFlag(Npc flag) {
		this.flag = flag;
	}

	public Npc getBoss() {
		return boss;
	}

	public void setBoss(Npc boss) {
		this.boss = boss;
	}

	public Npc getGuard() {
		return guard;
	}

	public void setGuard(Npc guard) {
		this.guard = guard;
	}

	public Npc getGuardRivar() {
		return guard_rivar;
	}

	public void setGuardRivar(Npc guard_rivar) {
		this.guard_rivar = guard_rivar;
	}

	public Npc getGuardKrall() {
		return guard_krall;
	}

	public void setGuardKrall(Npc guard_krall) {
		this.guard_krall = guard_krall;
	}

	public Npc getGuardWerewolf() {
		return guard_werewolf;
	}

	public void setGuardWerewolf(Npc guard_werewolf) {
		this.guard_werewolf = guard_werewolf;
	}

	public Npc getGuardVillage() {
		return guard_village;
	}

	public void setGuardVillage(Npc guard_village) {
		this.guard_village = guard_village;
	}

	public Npc getPortal() {
		return portal;
	}

	public void setPortal(Npc portal) {
		this.portal = portal;
	}

	public BossDeathListener getBossListener() {
		return bossDeathListener;
	}

	public boolean isFinished() {
		return finished.get();
	}

	public BL getBaseLocation() {
		return baseLocation;
	}

	public int getId() {
		return baseLocation.getId();
	}

	public Race getRace() {
		return baseLocation.getRace();
	}

	public void setRace(Race race) {
		baseLocation.setRace(race);
	}

	public List<Npc> getAttackers() {
		return attackers;
	}

	public List<Npc> getSpawned() {
		return spawned;
	}

}

