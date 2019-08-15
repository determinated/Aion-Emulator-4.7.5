
package ai.siege.anoha;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.ai2.handler.*;
import com.aionemu.gameserver.ai2.manager.SkillAttackManager;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.model.templates.npcshout.ShoutEventType;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.model.team.legion.LegionPermissionsMask;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.WorldType;
import com.aionemu.gameserver.world.knownlist.Visitor;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.model.Race;

/**
 * @author yayaya
 */
 
@AIName("Anoha_sword_asmo") // 804577 
public class AnohaSwordAsmoAI2 extends NpcAI2 {	

    @Override
    protected void handleDialogStart(Player player) {
	

        if (player.getInventory().getItemCountByItemId(185000215) >= 1 && player.getRace() == Race.ASMODIANS) {	
	

        TalkEventHandler.onTalk(this, player);
		
        } else {
			if (player.getRace() != Race.ASMODIANS) {
			 PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));	
			}else{
			
            PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 27));
			}
        } 
    }

    @Override
    public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		// 
		if (dialogId == 10000) {
			
				World.getInstance().doOnAllPlayers(new Visitor<Player>() {
					@Override
					public void visit(Player player) {
					}
				});	
	
        SpawnAhoha();		

		despawn();
        spawn(702618, 791.27985f, 489.02353f, 142.90796f, (byte) 77);    //todo despawn (mapNPC) 
        }
		return true;
	}

    private void SpawnAhoha() {
        ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {

        spawn(855263, 791.27985f, 489.02353f, 142.90796f, (byte) 77);	

				World.getInstance().doOnAllPlayers(new Visitor<Player>() {
					@Override
					public void visit(Player player) {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_Anoha_Spawn);
					}
				});	
            }
        }, 1800 * 1000); //1800 * 1000 = 30min
    }		
	
		
	
    private void despawn() {
        ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if (!isAlreadyDead()) {
                    AI2Actions.deleteOwner(AnohaSwordAsmoAI2.this);
                }
            }
        }, 500);
    }
	
	
}
