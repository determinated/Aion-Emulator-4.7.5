/*
 * Copyright (c) 2015, TypeZero Engine (game.developpers.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of TypeZero Engine nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.aionemu.gameserver.services.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ai2.AbstractAI;
import com.aionemu.gameserver.ai2.eventcallback.OnDieEventCallbackBase;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.team2.TemporaryPlayerTeam;
import com.aionemu.gameserver.services.BaseService;

/**
 *
 * @author Source
 */
@SuppressWarnings("rawtypes")
public class BossDeathListener extends OnDieEventCallbackBase {
	
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
			Creature kill = (Creature) winner;
			if (kill.getRace().isPlayerRace())
				base.setRace(kill.getRace());
		}
		else if (winner instanceof TemporaryPlayerTeam) {
			TemporaryPlayerTeam team = (TemporaryPlayerTeam) winner;
			if (team.getRace().isPlayerRace())
				base.setRace(team.getRace());
		}
		else
			base.setRace(Race.NPC);

		BaseService.getInstance().capture(base.getId(), base.getRace());
		log.info("Legat kill ! BOSS: " + boss + " in BaseId: " + base.getBaseLocation().getId() + " killed by RACE: " + race);
	}

	@Override
	public void onAfterDie(AbstractAI obj) {
	}

}
