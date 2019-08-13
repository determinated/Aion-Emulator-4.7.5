/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2015, Neon-Eleanor Team. All rights reserved.
 */
package instance;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.AbstractAI;
import com.aionemu.gameserver.controllers.effect.PlayerEffectController;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.StageList;
import com.aionemu.gameserver.model.instance.instancereward.EternalBastionReward;
import com.aionemu.gameserver.model.instance.playerreward.EternalBastionPlayerReward;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.DeathService;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.drop.DropRegistrationService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Tasks;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.knownlist.Visitor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;


@InstanceID(300540000)
public class TheEternalBastionInstance extends GeneralInstanceHandler {

    private long startTime;
    private EternalBastionReward instanceReward;
    private Map<Integer, StaticDoor> doors;
    private final AtomicInteger specNpcKilled = new AtomicInteger();
    private boolean isDestroyed;
    private Race spawnRace;
    private int CannonDestroy;
    private int Wave01Begin;
    private int Wave02Begin;
    private int Wave03Begin;
    private int Wave04Begin;
    private int AssaultBombCancel;
    private int HakundaRakunda;
    private Future<?> instanceTimer;
    private Future<?> AssaultBomb;
    private Future<?> AssaultRam;
    private Future<?> AttackTowerGate;
    private Future<?> AttackTowerGate2;
    private Future<?> siegeTower1;
    private Future<?> siegeTower2;
    private Future<?> siegeTower3;
    private Future<?> siegeTower4;
    private Future<?> siegeTower5;
    private Future<?> stage1;
    private Future<?> stage2;
    private int rank;

    protected EternalBastionPlayerReward getPlayerReward(Player player) {
        Integer object = player.getObjectId();
        if (instanceReward.getPlayerReward(object) == null) {
            addPlayerToReward(player);
        }
        return (EternalBastionPlayerReward) instanceReward.getPlayerReward(object);
    }

    private void addPlayerToReward(Player player) {
        instanceReward.addPlayerReward(new EternalBastionPlayerReward(player.getObjectId()));
    }

    private boolean containPlayer(Integer object) {
        return instanceReward.containPlayer(object);
    }

    @Override
    public void onEnterInstance(final Player player) {
        if (!containPlayer(player.getObjectId())) {
            addPlayerToReward(player);
        }
        if (instanceTimer == null) {
            startTime = System.currentTimeMillis();
            instanceTimer = ThreadPoolManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    openDoor(311);
                    if (spawnRace == null) {
                        spawnRace = player.getRace();
                        instanceReward.addPoints(20000);
                        SpawnRace();
                        startAssault();
                        startAssaultBombTimer();
                        StartSpawnRam();
                        onChangeStageList(StageList.START_STAGE_2_PHASE_1);
                        startAssaultPodTimer();
                    }
                    instanceReward.setInstanceScoreType(InstanceScoreType.START_PROGRESS);
                    sendPacket(0);
                }
            }, 150000L);
            instanceTimer = ThreadPoolManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    stopInstance(player);
                }
            }, 1950000L);
        }
        sendPacket(0);
    }

    @Override
    public void onDropRegistered(Npc npc) {
        int npcId = npc.getNpcId();
        Integer object = getInstance().getSoloPlayerObj();
        if (npcId == 831328 && npc.getSpawn().getStaticId() == 330) {
            DropRegistrationService.getInstance().regDropItem(npc.getObjectId(), object, npcId, 185000136, 24);
        }
    }

    @Override
    public void onInstanceCreate(WorldMapInstance instance) {
        super.onInstanceCreate(instance);
        instanceReward = new EternalBastionReward(getMapId(), getInstanceId());
        instanceReward.setInstanceScoreType(InstanceScoreType.PREPARING);
        doors = instance.getDoors();
        int rnd = Rnd.get(1, 2);
        switch (rnd) {
            case 1:
                spawn(230746, 552.5082f, 414.074f, 222.75688f, (byte) 17); //Pashid Assault Tribuni Sentry.
                spawn(231177, 820.55133f, 606.02814f, 239.70607f, (byte) 20); //Deathbringer Tariksha.
                break;
            case 2:
                spawn(231177, 552.5082f, 414.074f, 222.75688f, (byte) 17); //Deathbringer Tariksha.
                spawn(230746, 820.55133f, 606.02814f, 239.70607f, (byte) 20); //Pashid Assault Tribuni Sentry.
                break;
        }
    }

    @Override
    public void onChangeStageList(StageList list) {
        switch (list) {
            case START_STAGE_1_PHASE_1:
                ThreadPoolManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        spawnWalk(231113, 608.53816f, 398.5974f, 226.0108f, (byte) 111, 1000, "30054000001");
                        spawnWalk(231110, 607.84991f, 397.9318f, 226.0604f, (byte) 96, 1100, "30054000002");
                        spawnWalk(231110, 606.19899f, 396.60455f, 226.0078f, (byte) 110, 1200, "30054000003");
                        onChangeStageList(StageList.START_STAGE_1_PHASE_2);
                    }
                }, 120000L);
                break;
            case START_STAGE_1_PHASE_2:
                ThreadPoolManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        spawnWalk(231113, 608.53816f, 398.5974f, 226.0108f, (byte) 111, 1000, "30054000001");
                        spawnWalk(231110, 607.84991f, 397.9318f, 226.0604f, (byte) 96, 1100, "30054000002");
                        spawnWalk(231110, 606.19899f, 396.60455f, 226.0078f, (byte) 110, 1200, "30054000003");
                        onChangeStageList(StageList.START_STAGE_1_PHASE_3);
                    }
                }, 120000L);
                break;
            case START_STAGE_1_PHASE_3:
                ThreadPoolManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        spawnWalk(231113, 608.53816f, 398.5974f, 226.0108f, (byte) 111, 1000, "30054000001");
                        spawnWalk(231110, 607.84991f, 397.9318f, 226.0604f, (byte) 96, 1100, "30054000002");
                        spawnWalk(231110, 606.19899f, 396.60455f, 226.0078f, (byte) 110, 1200, "30054000003");
                        spawnWalk(231113, 594.53816f, 266.5974f, 227.0108f, (byte) 111, 1300, "30054000004");
                        spawnWalk(231112, 595.84991f, 267.9318f, 227.0604f, (byte) 96, 1400, "30054000005");
                        spawnWalk(231112, 593.19899f, 265.60455f, 227.0078f, (byte) 110, 1500, "30054000006");
                        onChangeStageList(StageList.START_STAGE_1_PHASE_4);
                    }
                }, 120000L);
                break;
            case START_STAGE_1_PHASE_4:
                ThreadPoolManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        spawnWalk(231113, 608.53816f, 398.5974f, 226.0108f, (byte) 111, 1000, "30054000001");
                        spawnWalk(231110, 607.84991f, 397.9318f, 226.0604f, (byte) 96, 1100, "30054000002");
                        spawnWalk(231110, 606.19899f, 396.60455f, 226.0078f, (byte) 110, 1200, "30054000003");
                        spawnWalk(231113, 594.53816f, 266.5974f, 227.0108f, (byte) 111, 1300, "30054000004");
                        spawnWalk(231112, 595.84991f, 267.9318f, 227.0604f, (byte) 96, 1400, "30054000005");
                        spawnWalk(231112, 593.19899f, 265.60455f, 227.0078f, (byte) 110, 1500, "30054000006");
                        onChangeStageList(StageList.START_STAGE_1_PHASE_5);
                    }
                }, 120000L);
                break;
            case START_STAGE_1_PHASE_5:
                ThreadPoolManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        spawnWalk(231113, 608.53816f, 398.5974f, 226.0108f, (byte) 111, 1000, "30054000001");
                        spawnWalk(231110, 607.84991f, 397.9318f, 226.0604f, (byte) 96, 1100, "30054000002");
                        spawnWalk(231110, 606.19899f, 396.60455f, 226.0078f, (byte) 110, 1200, "30054000003");
                        spawnWalk(231113, 594.53816f, 266.5974f, 227.0108f, (byte) 111, 1300, "30054000004");
                        spawnWalk(231112, 595.84991f, 267.9318f, 227.0604f, (byte) 96, 1400, "30054000005");
                        spawnWalk(231112, 593.19899f, 265.60455f, 227.0078f, (byte) 110, 1500, "30054000006");
                        onChangeStageList(StageList.START_STAGE_1_PHASE_6);
                    }
                }, 120000L);
                break;
            case START_STAGE_1_PHASE_6:
                stage1 = ThreadPoolManager.getInstance().scheduleAtFixedRate( new Runnable() {
                @Override
                public void run() {
                    spawnWalk(231113, 608.53816f, 398.5974f, 226.0108f, (byte) 111, 1000, "30054000001");
                    spawnWalk(231110, 607.84991f, 397.9318f, 226.0604f, (byte) 96, 1100, "30054000002");
                    spawnWalk(231110, 606.19899f, 396.60455f, 226.0078f, (byte) 110, 1200, "30054000003");
                    spawnWalk(231113, 594.53816f, 266.5974f, 227.0108f, (byte) 111, 1300, "30054000004");
                    spawnWalk(231112, 595.84991f, 267.9318f, 227.0604f, (byte) 96, 1400, "30054000005");
                    spawnWalk(231112, 593.19899f, 265.60455f, 227.0078f, (byte) 110, 1500, "30054000006");
                    spawnWalk(231113, 609.53816f, 399.5974f, 226.0108f, (byte) 111, 1600, "30054000001");
                    spawnWalk(231110, 610.84991f, 400.9318f, 226.0604f, (byte) 96, 1700, "30054000002");
                    spawnWalk(231110, 611.19899f, 401.60455f, 226.0078f, (byte) 110, 1800, "30054000003");
                }
            }, 120000L, 120000L);

                break;
            case START_STAGE_2_PHASE_1:
                ThreadPoolManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        spawnWalk(231113, 643.53816f, 475.5974f, 226.5108f, (byte) 111, 1000, "30054000007");
                        spawnWalk(231110, 644.84991f, 474.9318f, 226.5108f, (byte) 96, 1100, "30054000008");
                        spawnWalk(231110, 645.19899f, 476.60455f, 226.5108f, (byte) 110, 1200, "30054000009");
                        onChangeStageList(StageList.START_STAGE_1_PHASE_1);
                        onChangeStageList(StageList.START_STAGE_2_PHASE_2);
                    }
                }, 120000L);
                break;
            case START_STAGE_2_PHASE_2:
                ThreadPoolManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        spawnWalk(231113, 643.53816f, 475.5974f, 226.5108f, (byte) 111, 1000, "30054000007");
                        spawnWalk(231110, 644.84991f, 474.9318f, 226.5108f, (byte) 96, 1100, "30054000008");
                        spawnWalk(231110, 645.19899f, 476.60455f, 226.5108f, (byte) 110, 1200, "30054000009");
                        onChangeStageList(StageList.START_STAGE_2_PHASE_3);
                    }
                }, 120000L);
                break;
            case START_STAGE_2_PHASE_3:
                ThreadPoolManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        spawnWalk(231113, 643.53816f, 475.5974f, 226.5108f, (byte) 111, 1000, "30054000007");
                        spawnWalk(231110, 644.84991f, 474.9318f, 226.5108f, (byte) 96, 1100, "30054000008");
                        spawnWalk(231110, 645.19899f, 476.60455f, 226.5108f, (byte) 110, 1200, "30054000009");
                        spawnWalk(231113, 677.53816f, 477.5974f, 224.5108f, (byte) 111, 1300, "30054000010");
                        spawnWalk(231108, 678.84991f, 478.9318f, 224.5108f, (byte) 96, 1400, "30054000011");
                        spawnWalk(231108, 679.19899f, 479.60455f, 224.5108f, (byte) 110, 1500, "30054000012");
                        onChangeStageList(StageList.START_STAGE_2_PHASE_4);
                    }
                }, 120000L);
                break;
            case START_STAGE_2_PHASE_4:
                ThreadPoolManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        spawnWalk(231113, 643.53816f, 475.5974f, 226.5108f, (byte) 111, 1000, "30054000007");
                        spawnWalk(231110, 644.84991f, 474.9318f, 226.5108f, (byte) 96, 1100, "30054000008");
                        spawnWalk(231110, 645.19899f, 476.60455f, 226.5108f, (byte) 110, 1200, "30054000009");
                        spawnWalk(231113, 677.53816f, 477.5974f, 224.5108f, (byte) 111, 1300, "30054000010");
                        spawnWalk(231108, 678.84991f, 478.9318f, 224.5108f, (byte) 96, 1400, "30054000011");
                        spawnWalk(231108, 679.19899f, 479.60455f, 224.5108f, (byte) 110, 1500, "30054000012");
                        onChangeStageList(StageList.START_STAGE_2_PHASE_5);
                    }
                }, 120000L);
                break;
            case START_STAGE_2_PHASE_5:
                ThreadPoolManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        spawnWalk(231113, 643.53816f, 475.5974f, 226.5108f, (byte) 111, 1000, "30054000007");
                        spawnWalk(231110, 644.84991f, 474.9318f, 226.5108f, (byte) 96, 1100, "30054000008");
                        spawnWalk(231110, 645.19899f, 476.60455f, 226.5108f, (byte) 110, 1200, "30054000009");
                        spawnWalk(231113, 677.53816f, 477.5974f, 224.5108f, (byte) 111, 1300, "30054000010");
                        spawnWalk(231108, 678.84991f, 478.9318f, 224.5108f, (byte) 96, 1400, "30054000011");
                        spawnWalk(231108, 679.19899f, 479.60455f, 224.5108f, (byte) 110, 1500, "30054000012");
                        onChangeStageList(StageList.START_STAGE_2_PHASE_6);
                    }
                }, 120000L);
                break;
            case START_STAGE_2_PHASE_6:
                stage2 = ThreadPoolManager.getInstance().scheduleAtFixedRate( new Runnable() {
                @Override
                public void run() {
                    spawnWalk(231113, 643.53816f, 475.5974f, 226.5108f, (byte) 111, 1000, "30054000007");
                    spawnWalk(231110, 644.84991f, 474.9318f, 226.5108f, (byte) 96, 1100, "30054000008");
                    spawnWalk(231110, 645.19899f, 476.60455f, 226.5108f, (byte) 110, 1200, "30054000009");
                    spawnWalk(231113, 677.53816f, 477.5974f, 224.5108f, (byte) 111, 1300, "30054000010");
                    spawnWalk(231108, 678.84991f, 478.9318f, 224.5108f, (byte) 96, 1400, "30054000011");
                    spawnWalk(231108, 679.19899f, 479.60455f, 224.5108f, (byte) 110, 1500, "30054000012");
                    spawnWalk(231113, 642.53816f, 473.5974f, 226.9108f, (byte) 111, 1600, "30054000007");
                    spawnWalk(231110, 641.84991f, 472.9318f, 226.9108f, (byte) 96, 1700, "30054000008");
                    spawnWalk(231110, 640.19899f, 471.60455f, 226.9108f, (byte) 110, 1800, "30054000009");
                }
            }, 120000L, 120000L);
                break;
            case START_SPAWN_DEAD_GATE:
                AttackTowerGate = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        spawnWalk(231142, 797.99f, 473.36f, 225.9f, (byte) 77, 1000, "spawn_dead_gate1_300540000");
                        spawnWalk(231142, 799.99f, 473.36f, 225.9f, (byte) 77, 1100, "spawn_dead_gate1_300540000");
                        spawnWalk(231142, 798.99f, 473.36f, 225.9f, (byte) 77, 1200, "spawn_dead_gate1_300540000");
                    }
                }, 0L, 120000L);
                break;
            case START_SPAWN_DEAD_GATE_2:
                AttackTowerGate2 = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        spawnWalk(231108, 655.99f, 212.36f, 244.5f, (byte) 77, 1000, "spawn_dead_gate2_300540000");
                        spawnWalk(231110, 654.99f, 212.36f, 244.5f, (byte) 77, 1100, "spawn_dead_gate2_300540000_2");
                        spawnWalk(231108, 653.99f, 213.36f, 244.5f, (byte) 77, 1200, "spawn_dead_gate2_300540000");
                    }
                }, 0L, 120000L);
                break;
            case TURET_SPAWN_ASSAULT:
                spawnWalk(230744, 709.61f, 257.72f, 253.53f, (byte) 43, 1000, "turetspawn_300540000");
                spawnWalk(230745, 710.61f, 258.72f, 253.53f, (byte) 43, 1100, "turetspawn_300540000_2");
                spawnWalk(230744, 710.61f, 257.72f, 253.53f, (byte) 43, 1200, "turetspawn_300540000");
                break;
            case START_SIEGE_TOWER_1:
                siegeTower1 = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        spawnWalk(231110, 621.61f, 262.72f, 238.53f, (byte) 15, 1000, "siegetower1_300540000");
                        spawnWalk(231113, 621.61f, 264.72f, 238.53f, (byte) 15, 1100, "siegetower1_300540000_2");
                        spawnWalk(231110, 621.61f, 260.72f, 238.53f, (byte) 15, 1200, "siegetower1_300540000");
                    }
                }, 5000L, 120000L);
                break;
            case START_SIEGE_TOWER_2:
                siegeTower2 = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        spawnWalk(231110, 619.61f, 301.72f, 238.53f, (byte) 54, 1000, "siegetower2_300540000");
                        spawnWalk(231113, 619.61f, 302.72f, 238.53f, (byte) 54, 1100, "siegetower2_300540000_2");
                        spawnWalk(231110, 619.61f, 300.72f, 238.53f, (byte) 54, 1200, "siegetower2_300540000");
                    }
                }, 5000L, 120000L);
                break;
            case START_SIEGE_TOWER_3:
                siegeTower3 = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        spawnWalk(231112, 636.61f, 345.72f, 238.53f, (byte) 43, 1000, "siegetower3_300540000_2");
                        spawnWalk(231110, 636.61f, 346.72f, 238.53f, (byte) 43, 1100, "siegetower3_300540000");
                        spawnWalk(231112, 636.61f, 344.72f, 238.53f, (byte) 43, 1200, "siegetower3_300540000_2");
                    }
                }, 5000L, 120000L);
                break;
            case START_SIEGE_TOWER_4:
                siegeTower4 = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        spawnWalk(231112, 664.61f, 399.72f, 240.53f, (byte) 43, 1000, "siegetower4_300540000_2");
                        spawnWalk(231110, 662.61f, 398.72f, 240.53f, (byte) 43, 1100, "siegetower4_300540000");
                        spawnWalk(231112, 663.61f, 397.72f, 240.53f, (byte) 43, 1200,"siegetower4_300540000_2");
                    }
                }, 5000L, 120000L);
                break;
            case START_SIEGE_TOWER_5:
                siegeTower5 = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        spawnWalk(231110, 692.61f, 399.72f, 241.53f, (byte) 43, 1000, "siegetower5_300540000_2");
                        spawnWalk(231113, 690.61f, 398.72f, 241.53f, (byte) 43, 1100, "siegetower5_300540000");
                        spawnWalk(231110, 692.61f, 398.72f, 241.53f, (byte) 43, 1200, "siegetower5_300540000_2");
                    }
                }, 5000L, 120000L);
                break;
            case START_COMANDER_BADUT:
                spawnWalk(231172, 608.61f, 398.72f, 226.0108f, (byte) 43, 1000, "30054000001_badut");
                spawnWalk(233313, 607.61f, 397.72f, 226.0108f, (byte) 43, 1100, "30054000002_badut");
                spawnWalk(233313, 606.61f, 396.72f, 226.0108f, (byte) 43, 1200, "30054000003_badut");
                break;
            case START_COMANDER_KASTU:
                spawnWalk(231173, 643.61f, 475.72f, 226.5108f, (byte) 43, 1000, "30054000007_kastu");
                spawnWalk(233315, 644.61f, 474.72f, 226.5108f, (byte) 43, 1100, "30054000008_kastu");
                spawnWalk(233315, 645.61f, 476.72f, 226.5108f, (byte) 43, 1200, "30054000009_kastu");
                break;
            case START_COMANDER_KAIMDU:
                spawn(231175, 595.1f, 369.36f, 223.6f, (byte) 115);
                spawn(233313, 586.07f, 377.72f, 225.24f, (byte) 115);
                spawn(233313, 583.31f, 364.42f, 225.42f, (byte) 115);
                break;
            case START_COMANDER_MURAT:
                spawn(231174, 652.82f, 461.1f, 225.62f, (byte) 100);
                spawn(233315, 659.21f, 463.41f, 225.19f, (byte) 100);
                spawn(233315, 646.80f, 458.81f, 225.6f, (byte) 100);
                break;
            case RND_SPAWN_DRILL_1:
                spawnWalk(231105, 725.61f, 367.72f, 230.96f, (byte) 0, 1000, "rndDrill");
                spawnWalk(231107, 726.61f, 368.72f, 230.96f, (byte) 0, 1100, "rndDrill2");
                spawnWalk(231107, 724.61f, 366.72f, 230.96f, (byte) 0, 1200, "rndDrill2");
                break;
            case RND_SPAWN_DRILL_2:
                spawnWalk(231105, 763.61f, 354.72f, 231.96f, (byte) 0, 1000, "rndDrill");
                spawnWalk(231107, 764.61f, 353.72f, 231.96f, (byte) 0, 1100, "rndDrill2");
                spawnWalk(231107, 765.61f, 355.72f, 231.96f, (byte) 0, 1200, "rndDrill2");
                break;
            case RND_SPAWN_DRILL_3:
                spawnWalk(231109, 663.61f, 285.72f, 225.96f, (byte) 21, 1000, "rndDrill3");
                spawnWalk(231106, 664.61f, 286.72f, 225.96f, (byte) 21, 1100, "rndDrill4");
                spawnWalk(231106, 662.61f, 284.72f, 225.96f, (byte) 21, 1200, "rndDrill4");
                break;
            case RND_SPAWN_DRILL_4:
                spawnWalk(231109, 653.61f, 266.72f, 225.96f, (byte) 0, 1000, "rndDrill3");
                spawnWalk(231106, 652.61f, 267.72f, 225.96f, (byte) 0, 1100, "rndDrill4");
                spawnWalk(231106, 654.61f, 268.72f, 225.96f, (byte) 0, 1200, "rndDrill4");
                break;
            case RND_SPAWN_POD_1:
                spawnWalk(231105, 623.80f, 299.20f, 238.96f, (byte) 0, 1000, "rndPod");
                spawnWalk(231107, 624.80f, 298.20f, 238.96f, (byte) 0, 1100, "rndPod2");
                spawnWalk(231107, 622.80f, 297.20f, 238.96f, (byte) 0, 1200, "rndPod2");
                break;
            case RND_SPAWN_POD_2:
                spawnWalk(231105, 778.01f, 389.72f, 243.94f, (byte) 0, 1000, "rndPod3");
                spawnWalk(231107, 779.01f, 388.72f, 243.94f, (byte) 0, 1100, "rndPod4");
                spawnWalk(231107, 777.01f, 387.72f, 243.94f, (byte) 0, 1200, "rndPod4");
                break;
            case RND_SPAWN_POD_3:
                spawnWalk(231109, 781.17f, 323.80f, 253.69f, (byte) 21, 1000, "rndPod5");
                spawnWalk(231106, 782.17f, 322.80f, 253.69f, (byte) 21, 1100, "rndPod6");
                spawnWalk(231106, 783.17f, 321.80f, 253.69f, (byte) 21, 1200, "rndPod6");
                break;
            case RND_SPAWN_POD_4:
                spawnWalk(231109, 697.61f, 306.72f, 249.3f, (byte) 0, 1000, "rndPod7");
                spawnWalk(231106, 698.61f, 305.72f, 249.3f, (byte) 0, 1100, "rndPod8");
                spawnWalk(231106, 696.61f, 304.72f, 249.3f, (byte) 0, 1200, "rndPod8");
                break;
        }
    }

    @Override
    public void onDie(Npc npc) {
        int rewardedPoints = 0;
        int npcId = npc.getNpcId();
        Player player = npc.getAggroList().getMostPlayerDamage();
        switch (npc.getObjectTemplate().getTemplateId()) {
            case 230782: //The Eternal Bastion Barricade.
            case 231105:
            case 231106:
            case 231107:
                despawnNpc(npc);
                break;
            case 233313:
                despawnNpc(npc);
                rewardedPoints = 20;
                break;
            case 231115:
            case 231116:
            case 233309:
                despawnNpc(npc);
                rewardedPoints = 33;
                break;
            case 233312:
            case 233314:
            case 233315:
                despawnNpc(npc);
                rewardedPoints = 36;
                break;
            case 231117:
            case 231118:
            case 231119:
            case 231120:
            case 231123:
            case 231124:
            case 231125:
            case 231126:
            case 231127:
            case 231128:
            case 233310:
            case 233311:
                despawnNpc(npc);
                rewardedPoints = 42;
                break;
            case 231149:
            case 231181:
                despawnNpc(npc);
                rewardedPoints = 266;
                break;
            case 230785:
            case 231137:
            case 231138:
            case 231143:
            case 231144:
            case 231148:
            case 231151:
            case 231152:
            case 231153:
            case 231154:
            case 231155:
            case 231156:
            case 231157:
            case 231158:
            case 231159:
            case 231163:
            case 231164:
            case 231165:
            case 231166:
                despawnNpc(npc);
                rewardedPoints = 334;
                break;
            case 230784:
            case 230744:
            case 230745:
            case 230746: //Pashid Assault Tribuni Sentry.
            case 230749:
            case 230753:
            case 230754:
            case 230756:
            case 230757:
            case 231131:
            case 231132:
            case 231177: //Deathbringer Tariksha.
                rewardedPoints = 1002;
                despawnNpc(npc);
                break;
            case 231168:
            case 231169:
            case 231170:
            case 231171:
            case 231172:
            case 231173:
            case 231174:
            case 231175:
            case 231176:
            case 231178:
            case 231179:
            case 231180:
                AssaultBombCancel++;
                if (AssaultBombCancel == 5) {
                    AssaultBombCancelTask();
                }
                rewardedPoints = 1880;
                sendMsg(1401940); // MSG TD Notice 06.
                break;
            case 231133:
                rewardedPoints = 13424;
                break;
            // Died Guards
            case 209554:
            case 209555:
            case 209556:
            case 209557:
                instanceReward.addPoints(-50);
                npc.getController().onDelete();
                sendMsg(1401939); // MSG TD Main Wave 06.
                break;
            // Destroy Gate
            case 831335:
            case 831333:
                instanceReward.addPoints(-150);
                npc.getController().onDelete();
                break;
            case 231108:
            case 231110:
            case 231112:
            case 231113:
                despawnNpc(npc);
                break;

        }
        if (!isDestroyed && instanceReward.getInstanceScoreType().isStartProgress()) {
            instanceReward.addNpcKill();
            instanceReward.addPoints(rewardedPoints);
            if (player != null) {
                sendSystemMsg(player, npc, rewardedPoints);
            }
            sendPacket(rewardedPoints);
        }

        switch (npcId) {
            case 231141: // Assault Drill
            case 231163: // Assault Drill
            case 231164: // Assault Drill
            case 231165: // Assault Drill
            case 231166: // Assault Drill
            case 231167: // Assault Drill
            case 231140: // Assault Pod
            case 231156: // Assault Pod
            case 231157: // Assault Pod
            case 231158: // Assault Pod
            case 231159: // Assault Pod
            case 231160: // Assault Pod
            case 231161: // Assault Pod
            case 231162: // Assault Pod
            case 231136: // Danuar Turret
            case 231137: // Danuar Turret
            case 231138: // Danuar Turret
            case 231139: // Danuar Turret
                CannonDestroy++;
                if (CannonDestroy == 1) {
                    sendMsg(1401820); // First Destroy msg.
                }
                if (CannonDestroy >= 2) {
                    sendMsg(1401821); // First Destroy msg.
                }
                despawnNpc(npc);
                rewardedPoints = 500;
                break;
        }

        switch (npcId) {
            // 1 Wave Begin
            case 231169: // Commander.
            case 231168: // Commander.
            case 231170: // Commander.
                Wave01Begin++;
                if (Wave01Begin == 3) {
                    ThreadPoolManager.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                            sendMsg(1401816); // MSG Main Wave 02
                            // Uderground
                            sendMsg(1401823);
                            attackGate(getNpc(831335), (Npc) spawn(231171, 656.05f, 212.84f, 223.95f, (byte) 77));
                            // Left Flang
                            onChangeStageList(StageList.START_COMANDER_BADUT);
                            // Right FLang
                            onChangeStageList(StageList.START_COMANDER_KASTU);
                        }
                    }, 30000L);
                }
                despawnNpc(npc);
                break;
            // 2 Wave Begin
            case 231171: // Commander.
            case 231172: // Commander.
            case 231173: // Commander.
                Wave02Begin++;
                if (Wave02Begin == 3) {
                    ThreadPoolManager.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                            sendMsg(1401817); // MSG Main Wave 03
                            // Top
                            spawn(231176, 758.98f, 393.70f, 243.35f, (byte) 47); // Commander Nitra
                            // Left Flang
                            onChangeStageList(StageList.START_COMANDER_KAIMDU);
                            // Right FLang
                            onChangeStageList(StageList.START_COMANDER_MURAT);
                        }
                    }, 30000L);
                }
                despawnNpc(npc);
                break;
            // 3 Wave Begin
            case 231175: // Commander.
            case 231174: // Commander.
            case 231176: // Commander.
                Wave03Begin++;
                if (Wave03Begin == 3) {
                    sendMsg(1401818); // MSG Main Wave 04
                    spawn(231143, 613.27f, 262.20f, 228.26f, (byte) 3); // Pashid Siege Tower.
                    spawn(231152, 608.41f, 303.55f, 228.29f, (byte) 113); // Pashid Siege Tower.
                    spawn(231153, 626.25f, 351.28f, 226.01f, (byte) 113); // Pashid Siege Tower.
                    spawn(231154, 665.08f, 408.16f, 228.01f, (byte) 75); // Pashid Siege Tower.
                    spawn(231155, 690.40f, 409.50f, 230.01f, (byte) 97); // Pashid Siege Tower.
                    onChangeStageList(StageList.START_SIEGE_TOWER_1);
                    onChangeStageList(StageList.START_SIEGE_TOWER_2);
                    onChangeStageList(StageList.START_SIEGE_TOWER_3);
                    onChangeStageList(StageList.START_SIEGE_TOWER_4);
                    onChangeStageList(StageList.START_SIEGE_TOWER_5);
                }
                despawnNpc(npc);
                break;
            // 4 Wave Begin
            case 231143: // Pashid Siege Tower.
            case 231152: // Pashid Siege Tower.
            case 231153: // Pashid Siege Tower.
            case 231154: // Pashid Siege Tower.
            case 231155: // Pashid Siege Tower.
                Wave04Begin++;
                if (Wave04Begin <= 4) {
                    sendMsg(1401821); // Destroy msg.
                }
                if (Wave04Begin == 5) { // 5 Wave Begin
                    ThreadPoolManager.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                            sendMsg(1401819); // MSG Main Wave 05
                            spawn(231130, 744.22f, 293.07f, 233.69f, (byte) 42); // General Fly Fashid.
                            spawn(231131, 712.75f, 289.28f, 249.28f, (byte) 1); // Guard Fly Fashid.
                            spawn(231131, 747.78f, 323.02f, 249.28f, (byte) 88); // Guard Fly Fashid.
                            spawn(231131, 690.17f, 354.07f, 244.45f, (byte) 103); // Guard Fly Fashid.
                        }
                    }, 90000L);
                }
                despawnNpc(npc);
                break;
            case 284321: //Signal Tower 1.
            case 231148: //Signal Tower 2.
                sendMsg(1401821);
                despawnNpc(npc);
                rewardedPoints = 500;
                break;
            case 231130: // Boss Rank S.
                instanceReward.addPoints(24000);
                CancelStage1();
                CancelStage2();
                CancelAttackTowerGate2();
                stopInstance(player);
                if (checkRank(instanceReward.getPoints()) == 1) {
                    spawn(701913, 744.22f, 293.07f, 233.69f, (byte) 42); //Chest Rank S.
                } else if (checkRank(instanceReward.getPoints()) == 2) {
                    spawn(701914, 744.22f, 293.07f, 233.69f, (byte) 42); //Chest Rank A.
                } else if (checkRank(instanceReward.getPoints()) == 3) {
                    spawn(701915, 744.22f, 293.07f, 233.69f, (byte) 42); //Chest Rank B.
                } else if (checkRank(instanceReward.getPoints()) == 4) {
                    spawn(701916, 744.22f, 293.07f, 233.69f, (byte) 42); //Chest Rank C.
                } else if (checkRank(instanceReward.getPoints()) == 5) {
                    spawn(701917, 744.22f, 293.07f, 233.69f, (byte) 42); //Chest Rank D.
                }
                spawn(730871, 767.10693f, 264.60303f, 233.49748f, (byte) 43); //The Eternal Bastion Exit.
                break;
            case 209516: // Kill Commander Elyos
            case 209517: // Kill Commander Asmodians
                instanceReward.addPoints(-100000); // Fail Bastion
                stopInstance(player);
                spawn(730871, 767.10693f, 264.60303f, 233.49748f, (byte) 43); //The Eternal Bastion Exit.
                break;
        }

        switch (npcId) {
            // Cancel
            case 231143:
                CancelSiegeTower1();
                break;
            case 231152:
                CancelSiegeTower2();
                break;
            case 231153:
                CancelSiegeTower3();
                break;
            case 231154:
                CancelSiegeTower4();
                break;
            case 231155:
                CancelSiegeTower5();
                break;
            case 231179:
            case 231178:
                HakundaRakunda++;
                if (HakundaRakunda == 2) {
                    AssaultRamCancelTask();
                }
                break;
            case 831333:
                sendMsg(1401826);
                despawnNpc(npc); //despawnByNpcId(831333);
                onChangeStageList(StageList.START_SPAWN_DEAD_GATE);
                AssaultBombCancelTask();
                AssaultRamCancelTask();
                break;
            case 831335:
                sendMsg(1401824);
                onChangeStageList(StageList.START_SPAWN_DEAD_GATE_2);
                break;
        }
    }

    @Override
    public void handleUseItemFinish(Player player, Npc npc) {
        switch (npc.getNpcId()) {
            case 701625: // Eternal Bastion Turet Elyos.
                despawnNpc(npc);
                spawn(231160, 710.61f, 257.72f, 253.53f, (byte) 43); // Pashid Assault Pod.
                break;
            case 701922: // Eternal Bastion Turet Asmodians.
                despawnNpc(npc);
                spawn(231160, 710.61f, 257.72f, 253.53f, (byte) 43); // Pashid Assault Pod.
                break;
            case 831330: // Eternal Bastion Bomb.
                SkillEngine.getInstance().getSkill(npc, 21272, 60, player).useNoAnimationSkill();
                despawnNpc(npc);
                break;
        }
    }

    private void SpawnRace() {
        // Commander.
        final int comander = spawnRace == Race.ASMODIANS ? 209517 : 209516;
        spawn(comander, 748.70026f, 287.67944f, 233.81416f, (byte) 44);
        // Guard.
        final int guard1 = spawnRace == Race.ASMODIANS ? 209556 : 209554;
        final int guard2 = spawnRace == Race.ASMODIANS ? 209556 : 209554;
        final int guard3 = spawnRace == Race.ASMODIANS ? 209556 : 209554;
        final int guard4 = spawnRace == Race.ASMODIANS ? 209556 : 209554;
        final int guard5 = spawnRace == Race.ASMODIANS ? 209557 : 209555;
        final int guard6 = spawnRace == Race.ASMODIANS ? 209557 : 209555;
        final int guard7 = spawnRace == Race.ASMODIANS ? 209557 : 209555;
        final int guard8 = spawnRace == Race.ASMODIANS ? 209557 : 209555;
        final int guard9 = spawnRace == Race.ASMODIANS ? 209557 : 209555;
        final int guard10 = spawnRace == Race.ASMODIANS ? 209557 : 209555;
        final int guard11 = spawnRace == Race.ASMODIANS ? 209557 : 209555;
        final int guard12 = spawnRace == Race.ASMODIANS ? 209557 : 209555;
        final int guard13 = spawnRace == Race.ASMODIANS ? 209557 : 209555;
        final int guard14 = spawnRace == Race.ASMODIANS ? 209557 : 209555;
        final int guard15 = spawnRace == Race.ASMODIANS ? 209557 : 209555;
        final int guard16 = spawnRace == Race.ASMODIANS ? 209557 : 209555;
        spawn(guard1, 719.28f, 417.82f, 231.04f, (byte) 41);
        spawn(guard2, 725.68f, 420.94f, 231.00f, (byte) 40);
        spawn(guard3, 598.93f, 331.60f, 226.14f, (byte) 85);
        spawn(guard4, 607.60f, 327.75f, 225.84f, (byte) 84);
        spawn(guard5, 714.31f, 425.46f, 230.23f, (byte) 40);
        spawn(guard6, 720.82f, 428.14f, 230.10f, (byte) 38);
        spawn(guard7, 608.80f, 351.30f, 225.08f, (byte) 35);
        spawn(guard8, 601.31f, 349.28f, 225.33f, (byte) 34);
        spawn(guard9, 689.28f, 342.57f, 228.67f, (byte) 103);
        spawn(guard10, 694.25f, 336.73f, 228.67f, (byte) 43);
        spawn(guard11, 598.25f, 283.73f, 226.67f, (byte) 90);
        spawn(guard12, 592.25f, 283.73f, 226.67f, (byte) 90);
        spawn(guard13, 688.25f, 353.73f, 244.67f, (byte) 43);
        spawn(guard14, 691.25f, 356.73f, 244.67f, (byte) 43);
        spawn(guard15, 744.25f, 362.73f, 231.67f, (byte) 58);
        spawn(guard16, 745.25f, 367.73f, 231.67f, (byte) 58);
        // Defence Weapon.
        final int defenceWeapon1 = spawnRace == Race.ASMODIANS ? 701610 : 701596;
        final int defenceWeapon2 = spawnRace == Race.ASMODIANS ? 701611 : 701597;
        final int defenceWeapon3 = spawnRace == Race.ASMODIANS ? 701612 : 701598;
        final int defenceWeapon4 = spawnRace == Race.ASMODIANS ? 701613 : 701599;
        final int defenceWeapon5 = spawnRace == Race.ASMODIANS ? 701614 : 701600;
        final int defenceWeapon6 = spawnRace == Race.ASMODIANS ? 701615 : 701601;
        final int defenceWeapon7 = spawnRace == Race.ASMODIANS ? 701616 : 701602;
        final int defenceWeapon8 = spawnRace == Race.ASMODIANS ? 701617 : 701603;
        final int defenceWeapon9 = spawnRace == Race.ASMODIANS ? 701618 : 701604;
        final int defenceWeapon10 = spawnRace == Race.ASMODIANS ? 701619 : 701605;
        final int defenceWeapon11 = spawnRace == Race.ASMODIANS ? 701620 : 701606;
        final int defenceWeapon12 = spawnRace == Race.ASMODIANS ? 701621 : 701607;
        final int defenceWeapon13 = spawnRace == Race.ASMODIANS ? 701922 : 701625;
        spawn(defenceWeapon1, 617.95416f, 248.32031f, 235.74449f, (byte) 63);
        spawn(defenceWeapon2, 613.11914f, 275.30057f, 235.74294f, (byte) 64);
        spawn(defenceWeapon3, 616.4774f, 313.85846f, 235.74289f, (byte) 52);
        spawn(defenceWeapon4, 625.97675f, 339.55414f, 235.7432f, (byte) 54);
        spawn(defenceWeapon5, 651.3247f, 373.3068f, 238.60867f, (byte) 44);
        spawn(defenceWeapon6, 678.08124f, 396.04736f, 238.63474f, (byte) 43);
        spawn(defenceWeapon7, 710.27765f, 409.9322f, 241.02042f, (byte) 31);
        spawn(defenceWeapon8, 737.3579f, 413.3636f, 241.02278f, (byte) 33);
        spawn(defenceWeapon9, 772.7887f, 410.0723f, 241.02089f, (byte) 6);
        spawn(defenceWeapon10, 798.2277f, 400.5876f, 241.02304f, (byte) 38);
        spawn(defenceWeapon11, 709.54443f, 313.67133f, 254.21622f, (byte) 103);
        spawn(defenceWeapon12, 726.6982f, 328.01038f, 254.21628f, (byte) 103);
        spawn(defenceWeapon13, 640.8445f, 412.9476f, 243.93938f, (byte) 103);
    }

    private void startAssault() {
        ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                sendMsg(1401815); // MSG Main Wave 01
                spawn(231140, 741.09f, 302.52f, 233.75f, (byte) 99); // Pashid Assault Pod.
                spawn(231141, 735.23f, 298.94f, 233.75f, (byte) 110); // Pashid Assault Drill.
            }
        }

                , 5000L);
    }

    private void attackGate(final Npc gate, final Npc npc) {
        ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if (!isDestroyed) {
                    npc.setTarget(gate);
                    ((AbstractAI) npc.getAi2()).setStateIfNot(AIState.WALKING);
                    npc.setState(1);
                    npc.getMoveController().moveToTargetObject();
                    PacketSendUtility.broadcastPacket(npc, new SM_EMOTION(npc, EmotionType.START_EMOTE2, 0, npc.getObjectId()));
                }
            }
        }
                , 4000L);
    }

    private void startAssaultBombTimer() {
        AssaultBomb = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                sendMsg(1401825);
                attackGate(getNpc(831333), (Npc) spawn(231142, 795.99f, 424.36f, 232.9f, (byte) 77));
                attackGate(getNpc(831333), (Npc) spawn(231142, 795.99f, 425.36f, 232.9f, (byte) 77));
                attackGate(getNpc(831333), (Npc) spawn(231142, 795.99f, 426.36f, 232.9f, (byte) 77));
                attackGate(getNpc(831333), (Npc) spawn(231142, 795.99f, 423.36f, 232.9f, (byte) 77));
            }
        }
                , 600000L, 60000L);
    }

    private void StartSpawnRam() {
        AssaultRam = ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                attackGate(getNpc(831333), (Npc) spawn(231150, 798.76f, 426.68f, 231.77f, (byte) 77));
            }
        }
                , 900000L);
    }

    private void startAssaultPodTimer() {
        int rnd = Rnd.get(1, 4);
        switch (rnd) {
            case 1:
                startRndAssaultPod();
                break;
            case 2:
                ThreadPoolManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        startRndAssaultPod();
                    }
                }, 180000L);
                break;
            case 3:
                ThreadPoolManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        startRndAssaultPod();
                    }
                }, 380000L);
                break;
            case 4:
                break;
        }
    }

    private void startRndAssaultPod() {
        ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                int rnd = Rnd.get(1, 4);
                switch (rnd) {
                    case 1:
                        spawn(231156, 622.80f, 298.20f, 238.96f, (byte) 27); // Pashid Assault Pod.
                        spawn(231163, 725.40f, 367.19f, 230.96f, (byte) 0); // Pashid Assault Drill.
                        break;
                    case 2:
                        spawn(231157, 778.01f, 388.72f, 243.94f, (byte) 55); // Pashid Assault Pod.
                        spawn(231164, 663.94f, 285.56f, 225.70f, (byte) 21); // Pashid Assault Drill.
                        break;
                    case 3:
                        spawn(231158, 781.17f, 322.80f, 253.69f, (byte) 56); // Pashid Assault Pod.
                        spawn(231165, 763.63f, 354.47f, 231.69f, (byte) 23); // Pashid Assault Drill.
                        break;
                    case 4:
                        spawn(231159, 698.70f, 306.36f, 249.69f, (byte) 3); // Pashid Assault Pod.
                        spawn(231166, 653.85f, 266.61f, 225.69f, (byte) 118); // Pashid Assault Drill.
                        break;
                }
            }
        }

                , 300000L);
    }

    private void AssaultBombCancelTask() {
        Tasks.cancel(AssaultBomb);
    }

    private void AssaultRamCancelTask() {
        Tasks.cancel(AssaultRam);
    }

    private void CancelAttackBomber() {
        Tasks.cancel(AttackTowerGate);
    }

    private void CancelSiegeTower1() {
        Tasks.cancel(siegeTower1);
    }

    private void CancelSiegeTower2() {
        Tasks.cancel(siegeTower2);
    }

    private void CancelSiegeTower3() {
        Tasks.cancel(siegeTower3);
    }

    private void CancelSiegeTower4() {
        Tasks.cancel(siegeTower4);
    }

    private void CancelSiegeTower5() {
        Tasks.cancel(siegeTower5);
    }

    private void CancelStage1() {
        Tasks.cancel(stage1);
    }

    private void CancelStage2() {
        Tasks.cancel(stage2);
    }

    private void CancelAttackTowerGate2() {
        Tasks.cancel(AttackTowerGate2);
    }

    private void cancelAllTasks() {
        Tasks.cancel(AssaultBomb);
        Tasks.cancel(AssaultRam);
        Tasks.cancel(AttackTowerGate);
        Tasks.cancel(AttackTowerGate2);
        Tasks.cancel(siegeTower1);
        Tasks.cancel(siegeTower2);
        Tasks.cancel(siegeTower3);
        Tasks.cancel(siegeTower4);
        Tasks.cancel(siegeTower5);
        Tasks.cancel(stage1);
        Tasks.cancel(stage2);
    }

    private void removeEffects(Player player) {
        PlayerEffectController effectController = player.getEffectController();
        effectController.removeEffect(21065);
        effectController.removeEffect(21066);
        effectController.removeEffect(21138);
        effectController.removeEffect(21139);
        effectController.removeEffect(21141);
    }

    @Override
    public void onLeaveInstance(Player player) {
        removeEffects(player);
    }

    private int getTime() {
        long result = System.currentTimeMillis() - startTime;
        if (result < 150000) {
            return (int) (150000 - result);
        } else if (result < 1800000) {
            return (int) (1800000 - (result - 150000));
        }
        return 0;
    }

    protected void sendSystemMsg(Player player, Creature creature, int rewardPoints) {
        int nameId = creature.getObjectTemplate().getNameId();
        DescriptionId name = new DescriptionId(nameId * 2 + 1);
        PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400237, nameId == 0 ? creature.getName() : name, rewardPoints));
    }

    private void sendPacket(final int point) {
        instance.doOnAllPlayers(new Visitor<Player>() {

            @Override
            public void visit(Player player) {
                PacketSendUtility.sendPacket(player, new SM_INSTANCE_SCORE(getTime(), instanceReward, null));
            }
        });
    }

    private int checkRank(int totalPoints) {
        if (totalPoints > 91999) { //Rank S.
            rank = 1;
        } else if (totalPoints > 83999) { //Rank A.
            rank = 2;
        } else if (totalPoints > 75999) { //Rank B.
            rank = 3;
        } else if (totalPoints > 49999) { //Rank C.
            rank = 4;
        } else if (totalPoints > 9999) { //Rank D.
            rank = 5;
        } else if (totalPoints >= 9999) { //Rank F.
            rank = 8;
        } else {
            rank = 8;
        }
        return rank;
    }

    protected void stopInstance(Player player) {
        stopInstanceTask();
        cancelAllTasks();
        instanceReward.setRank(checkRank(instanceReward.getPoints()));
        instanceReward.setInstanceScoreType(InstanceScoreType.END_PROGRESS);
        rewardGroup();
        sendPacket(0);
        //despawnAll();
    }

    private void stopInstanceTask() {
        Tasks.cancel(instanceTimer);
    }

    private void rewardGroup() {
        for (Player p : instance.getPlayersInside()) {
            doReward(p);
        }
    }

    @Override
    public void doReward(Player player) {
        if (!instanceReward.isRewarded()) {
            instanceReward.setRewarded();
            switch (rank) {
                case 1: // S
                    instanceReward.setBasicAP(35000);
                    instanceReward.setCeramiumMedal(4);
                    instanceReward.setPowerfulBundleWater(1);
                    instanceReward.setPowerfulBundleEssence(1);
                    break;
                case 2: // A
                    instanceReward.setBasicAP(25000);
                    instanceReward.setCeramiumMedal(2);
                    instanceReward.setPowerfulBundleEssence(1);
                    instanceReward.setLargeBundleWater(1);
                    break;
                case 3: // B
                    instanceReward.setBasicAP(15000);
                    instanceReward.setCeramiumMedal(1);
                    instanceReward.setLargeBundleEssence(1);
                    instanceReward.setSmallBundleWater(1);
                    break;
                case 4: // C
                    instanceReward.setBasicAP(11000);
                    instanceReward.setSmallBundleWater(1);
                    break;
                case 5: // D
                    instanceReward.setBasicAP(7000);
                    break;
                case 6: // F
                    break;
            }
            AbyssPointsService.addAp(player, instanceReward.getBasicAP());
            ItemService.addItem(player, 186000242, instanceReward.getCeramiumMedal());
            ItemService.addItem(player, 188052596, instanceReward.getPowerfulBundleWater());
            ItemService.addItem(player, 188052594, instanceReward.getPowerfulBundleEssence());
            ItemService.addItem(player, 188052597, instanceReward.getLargeBundleWater());
            ItemService.addItem(player, 188052595, instanceReward.getLargeBundleEssence());
            ItemService.addItem(player, 188052598, instanceReward.getSmallBundleWater());
        }
    }

    @Override
    public void onInstanceDestroy() {
        isDestroyed = true;
        Tasks.cancel(instanceTimer);
        cancelAllTasks();
        doors.clear();
    }

    protected void despawnNpc(Npc npc) {
        if (npc != null) {
            npc.getController().onDelete();
        }
    }

    protected void despawnNpcs(List<Npc> npcs) {
        for (Npc npc : npcs) {
            npc.getController().onDelete();
        }
    }



    protected void openDoor(int doorId) {
        StaticDoor door = doors.get(doorId);
        if (door != null) {
            door.setOpen(true);
        }
    }


    @Override
    public boolean onDie(Player player, Creature lastAttacker) {
        DeathService.sendDie(player, lastAttacker);
        return true;
    }


    @Override
    public boolean onReviveEvent(Player player) {
        PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME);
        PlayerReviveService.revive(player, 20, 20, false, 0);
        player.getGameStats().updateStatsAndSpeedVisually();
        TeleportService2.teleportTo(player, player.getWorldId(), player.getInstanceId(), 449f, 448f, 271f, (byte) 75);
        return true;
    }

    @Override
    public void onExitInstance(Player player) {
        TeleportService2.moveToInstanceExit(player, getMapId(), player.getRace());
    }
}
