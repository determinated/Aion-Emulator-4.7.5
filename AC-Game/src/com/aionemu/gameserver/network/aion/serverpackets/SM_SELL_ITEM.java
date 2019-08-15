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

package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.tradelist.TradeListTemplate;
import com.aionemu.gameserver.model.templates.tradelist.TradeListTemplate.TradeTab;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.TradeService;
import com.aionemu.gameserver.services.trade.PricesService;
import com.aionemu.gameserver.world.World;

/**
 * @author orz, Sarynth, modified by Artur
 */
public class SM_SELL_ITEM extends AionServerPacket {

	private int targetObjectId;
	private TradeListTemplate plist;
	private int sellPercentage;

	public SM_SELL_ITEM(int targetObjectId, TradeListTemplate plist, int sellPercentage) {

		this.targetObjectId = targetObjectId;
		this.plist = plist;
		this.sellPercentage = sellPercentage;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con) {
	if ((plist != null) && (plist.getNpcId() != 0) && (plist.getCount() != 0)) {
		writeD(targetObjectId);
		writeC(plist.getTradeNpcType().index());
        int modifier = 0;
        int taxmodifer = 0;
        if(con.getActivePlayer().getTarget() instanceof Npc)
            modifier = TradeService.getPriceModifier((Npc) con.getActivePlayer().getTarget());
            taxmodifer = PricesService.getTaxModiferForService(con.getActivePlayer().getRace());
        if(modifier != 0){
            sellPercentage /= modifier;
        }
        writeD(sellPercentage + (taxmodifer > 0 ? taxmodifer : 0));//Buy Price * (sellPercentage / 100) = Display price.
		writeH(256);
		writeH(plist.getCount());
		for (TradeTab tradeTabl : plist.getTradeTablist()) {
			writeD(tradeTabl.getId());
		}
	}
	else
        {
        int taxmodifer = PricesService.getTaxModiferForService(con.getActivePlayer().getRace());
		writeD(targetObjectId);
		writeC(1);
		writeD(sellPercentage + taxmodifer > 0 ? sellPercentage + taxmodifer : 0); // Buy Price * (sellPercentage / 100) = Display price.
		writeH(256);
		writeH(0);
		}
	}
}
