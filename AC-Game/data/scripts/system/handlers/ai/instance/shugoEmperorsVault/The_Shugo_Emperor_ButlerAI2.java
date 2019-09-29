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
package ai.instance.shugoEmperorVault;

import com.aionemu.gameserver.ai2.*;
import com.aionemu.gameserver.model.*;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.*;
import com.aionemu.gameserver.services.teleport.*;
import com.aionemu.gameserver.utils.*;

/****/
/** Author Rinzler (Encom)
/****/

@AIName("emperor_butler")
public class The_Shugo_Emperor_ButlerAI2 extends NpcAI2
{
	@Override
	protected void handleDialogStart(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}
	
	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		int instanceId = getPosition().getInstanceId();
		if (dialogId == 10000) {
			switch (getNpcId()) {
				case 832932: //The Shugo Emperor's Butler.
					switch (player.getWorldId()) {
					    case 301400000: //The Shugo Emperor's Vault 4.7.5
						    TeleportService2.teleportTo(player, 301400000, instanceId, 174.95818f, 371.1251f, 395.49478f, (byte) 73, TeleportAnimation.BEAM_ANIMATION);
			            break;
					    case 301590000: //Emperor Trillirunerk's Safe 4.9.1
					        TeleportService2.teleportTo(player, 301590000, instanceId, 174.95818f, 371.1251f, 395.49478f, (byte) 73, TeleportAnimation.BEAM_ANIMATION);
			            break;
					}
				break;
			}
		}
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
		return true;
	}
}