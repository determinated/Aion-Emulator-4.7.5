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

package quest.ishalgen;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author vlog
 */
public class _2123TheImprisonedGourmet extends QuestHandler {

	private final static int questId = 2123;

	public _2123TheImprisonedGourmet() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203550).addOnQuestStart(questId);
		qe.registerQuestNpc(203550).addOnTalkEvent(questId);
		qe.registerQuestNpc(700128).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 203550) { // Munin
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 1011);
					}
					default: {
						return sendQuestStartDialog(env);
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203550: { // Munin
					switch (dialog) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 1352);
						}
						case SETPRO1: {
							if (player.getInventory().getItemCountByItemId(182203121) >= 1) {
								qs.setQuestVar(5); // 5
								qs.setStatus(QuestStatus.REWARD); // rewatd
								updateQuestStatus(env);
								removeQuestItem(env, 182203121, 1);
								return sendQuestDialog(env, 5);
							}
							else {
								return sendQuestDialog(env, 1693);
							}
						}
						case SETPRO2: {
							if (player.getInventory().getItemCountByItemId(182203122) >= 1) {
								qs.setQuestVar(6); // 6
								qs.setStatus(QuestStatus.REWARD); // rewatd
								updateQuestStatus(env);
								removeQuestItem(env, 182203122, 1);
								return sendQuestDialog(env, 6);
							}
							else {
								return sendQuestDialog(env, 1693);
							}
						}
						case SETPRO3: {
							if (player.getInventory().getItemCountByItemId(182203123) >= 1) {
								qs.setQuestVar(7); // 7
								qs.setStatus(QuestStatus.REWARD); // rewatd
								updateQuestStatus(env);
								removeQuestItem(env, 182203123, 1);
								return sendQuestDialog(env, 7);
							}
							else {
								return sendQuestDialog(env, 1693);
							}
						}
						case FINISH_DIALOG: {
							return sendQuestSelectionDialog(env);
						}
					}
					break;
				}
				case 700128: { // Methu Egg
					return true;
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203550) { // Munin
				return sendQuestEndDialog(env, qs.getQuestVarById(0) - 5);
			}
		}
		return false;
	}
}
