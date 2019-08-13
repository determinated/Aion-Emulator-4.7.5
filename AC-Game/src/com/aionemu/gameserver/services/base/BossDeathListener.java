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
import com.aionemu.gameserver.model.team2.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team2.alliance.PlayerAllianceGroup;
import com.aionemu.gameserver.model.team2.group.PlayerGroup;
import com.aionemu.gameserver.model.team2.league.League;
import com.aionemu.gameserver.services.BaseService;

/**
 *
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
        AionObject killer = base.getBoss().getAggroList().getMostDamage();
        Npc boss = base.getBoss();
        Race race = null;

        if (killer instanceof PlayerGroup) {
            race = ((PlayerGroup) killer).getRace();
        } else if (killer instanceof PlayerAlliance) {
            race = ((PlayerAlliance) killer).getRace();
        } else if (killer instanceof PlayerAllianceGroup) {
            race = ((PlayerAllianceGroup) killer).getRace();
        } else if (killer instanceof League) {
            race = ((League) killer).getRace();
        } else if (killer instanceof Player) {
            race = ((Player) killer).getRace();
        } else if (killer instanceof Creature) {
			if (((Creature) killer).getRace() !=  Race.DRAKAN || ((Creature) killer).getRace() !=  Race.ELYOS || ((Creature) killer).getRace() !=  Race.ASMODIANS) { //TEMP Fix because some NPC's have no Race in Template
				race = Race.NPC;
			} else {
				race = ((Creature) killer).getRace();
			}
        }

        base.setRace(race);
        BaseService.getInstance().capture(base.getId(), base.getRace());
        log.info("Legat kill ! BOSS: " + boss + " in BaseId: " + base.getBaseLocation().getId() + " killed by RACE: " + race);
    }

    @Override
    public void onAfterDie(AbstractAI obj) {
    }

}
