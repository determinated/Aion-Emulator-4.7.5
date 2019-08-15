
package ai.siege.anoha;

import ai.ActionItemNpcAI2;

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

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author yayaya
 */
@AIName("anoha_siege_bombard") //
public class AnohaSiegeBombardAI2 extends ActionItemNpcAI2 {	

	private AtomicBoolean canUse = new AtomicBoolean(true);

    @Override
    protected void handleDialogStart(Player player) {
		

        if (player.getInventory().getItemCountByItemId(186000246) > 0) {	// Magic Cannonball.
		 // начинаем разговор если есть
          TalkEventHandler.onTalk(this, player);
        }else {
			// no Magic Cannonball.
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(2281763));
		}
                
    }

    @Override
    public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		
		  if (dialogId == 10000) {
			
            super.handleUseItemStart(player);
            PacketSendUtility.sendYellowMessageOnCenter(player, "RAMPAGE WILL START SOON:D");

          }
         return true;
        }
		
	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				startLifeTask();
			}
		}, 1000);
	}

	private void startLifeTask() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AI2Actions.deleteOwner(AnohaSiegeBombardAI2.this);
			}
		}, 7200000);
	}
		
		
	@Override
	protected void handleUseItemFinish(Player player) {
		
		  if (canUse.compareAndSet(true, false)) {
		  switch (player.getRace()) {
			case ELYOS:
		SkillEngine.getInstance().getSkill(player, 21385, 1, player).useNoAnimationSkill();	
		  break;
			case ASMODIANS:
	    SkillEngine.getInstance().getSkill(player, 21386, 1, player).useNoAnimationSkill();
		  break;
		  }
		Npc owner = getOwner();
		player.getController().stopProtectionActiveTask();
		AI2Actions.deleteOwner(this);
		}		
	}
	 	
	
}
