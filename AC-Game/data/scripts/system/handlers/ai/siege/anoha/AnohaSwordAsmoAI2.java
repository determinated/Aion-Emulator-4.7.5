
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
        if (player.getInventory().getFirstItemByItemId(185000215) != null) { //Anoha Sealing Stone.
            PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 10));
        } else {
            PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 27));
        }
    }
	
	@Override
    public boolean onDialogSelect(final Player player, int dialogId, int questId, int extendedRewardIndex) {
		if (dialogId == 10000 && player.getInventory().decreaseByItemId(185000215, 1)) { //Anoha Sealing Stone.
		    switch (getNpcId()) {
			    case 804577: //Anoha Sword [Asmodians]
					announceBerserkAnoha30Min();
					spawn(702644, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) getOwner().getHeading());
					spawn(702618, 791.27985f, 489.02353f, 142.90796f, (byte) 77);
					ThreadPoolManager.getInstance().schedule(new Runnable() {
						@Override
						public void run() {
							announceReleaseAnoha();
							spawn(855263, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) getOwner().getHeading()); //Berserk Anoha.
						}
					}, 1800000); //30 Minutes.
				break;
			}
		}
		//The Anoha Sealing Stone was used to release Anoha.
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LDF5_Fortress_Named_Spawn_Item);
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
		AI2Actions.deleteOwner(this);
		AI2Actions.scheduleRespawn(this);
		return true;
	}
	
	private void announceBerserkAnoha30Min() {
		World.getInstance().doOnAllPlayers(new Visitor<Player>() {
			@Override
			public void visit(Player player) {
				//Berserk Anoha will return to Kaldor in 30 minutes.
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_Anoha_Spawn);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LDF5_Fortress_Named_Spawn_System);
			}
		});
	}
	
	private void announceReleaseAnoha() {
		World.getInstance().doOnAllPlayers(new Visitor<Player>() {
			@Override
			public void visit(Player player) {
				//Release Anoha.				
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LDF5_Fortress_Named_Spawn);
			}
		});
	}
}