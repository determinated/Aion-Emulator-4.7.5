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
package ai.instance.transidiumAnnex;

import java.util.*;

import com.aionemu.commons.network.util.ThreadPoolManager;

import com.aionemu.gameserver.ai2.*;
import com.aionemu.gameserver.model.*;
import com.aionemu.gameserver.model.gameobjects.*;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.*;
import com.aionemu.gameserver.utils.*;
import com.aionemu.gameserver.world.*;

/****/
/** Author Rinzler (Encom)
/****/

@AIName("bulwark_shield")
public class Bulwark_ShieldAI2 extends NpcAI2
{
	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		startShieldVulnerable();
	}
	
   /**
	* Walls around "Ahserion" become vulnerable
	* You can start to destroy walls between you and "Ahserion".
	* You can say that after 30min the real battle begins.
	*/
	private void startShieldVulnerable() {
		final Npc GAb1SubNamedBarricadeDa65Ah = getPosition().getWorldMapInstance().getNpc(277231); //Bulwark Shield.
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				GAb1SubNamedBarricadeDa65Ah.setTarget(getOwner());
				GAb1SubNamedBarricadeDa65Ah.setNpcType(NpcType.ATTACKABLE);
				WorldMapInstance instance = getPosition().getWorldMapInstance();
				for (Player player: instance.getPlayersInside()) {
					if (MathUtil.isIn3dRange(player, GAb1SubNamedBarricadeDa65Ah, 5)) {
						player.clearKnownlist();
						player.updateKnownlist();
					}
				}
				//The effect of the Transidium Annex has weakened the Bulwark Shield.
				PacketSendUtility.npcSendPacketTime(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_ALARM_12, 0);
			}
		}, 1800000); //...30 Minutes.
	}
	
	@Override
	public boolean isMoveSupported() {
		return false;
	}
}