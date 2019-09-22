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
 *  You should have received a copy of the GNU General Public License
 *  along with Aion-Lightning.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.services.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ai2.AbstractAI;
import com.aionemu.gameserver.ai2.eventcallback.OnDieEventCallback;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.TemporaryPlayerTeam;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.BaseService;
import com.aionemu.gameserver.services.HTMLService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Source
 */
@SuppressWarnings("rawtypes")
public class BossDeathListener extends OnDieEventCallback {

	private static final Logger log = LoggerFactory.getLogger(BossDeathListener.class);

	private final Base<?> base;

	public BossDeathListener(Base base) {
		this.base = base;
	}

	@Override
	public void onBeforeDie(AbstractAI obj) {
		AionObject winner = base.getBoss().getAggroList().getMostDamage();
		Npc boss = base.getBoss();
		Race race = null;

		if (winner instanceof Creature) {
			final Creature kill = (Creature) winner;
            if (kill.getRace().isPlayerRace()) {
                base.setRace(kill.getRace());
                race = kill.getRace();
			}
			announceCapture(null, kill);
		}
		else {
			base.setRace(Race.NPC);
		}
		BaseService.getInstance().capture(base.getId(), base.getRace());
		log.info("Legat kill ! BOSS: " + boss + " in BaseId: " + base.getBaseLocation().getId() + " killed by RACE: " + race);
	}

	public void announceCapture(final TemporaryPlayerTeam team, final Creature kill) {
		final String baseName = base.getBaseLocation().getName();
		World.getInstance().doOnAllPlayers(new Visitor<Player>() {

			@Override
			public void visit(Player player) {
				if (team != null && kill == null) {
					// %0 succeeded in conquering %1
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1301039, team.getRace().getRaceDescriptionId(), baseName));
				}
				else {
					// %0 succeeded in conquering %1
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1301039, kill.getRace().getRaceDescriptionId(), baseName));
				}
			}
		});
	}

	@Override
	public void onAfterDie(AbstractAI obj) {
	}

}