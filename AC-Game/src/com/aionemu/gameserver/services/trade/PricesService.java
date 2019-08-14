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

package com.aionemu.gameserver.services.trade;

import com.aionemu.gameserver.configs.main.PricesConfig;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.siege.Influence;
import com.aionemu.gameserver.model.templates.item.ItemQuality;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.tradelist.TradeListTemplate;

/**
 * @author Sarynth modified by wakizashi Used to get prices for the player. - Packets: SM_PRICES, SM_TRADELIST,
 *         SM_SELL_ITEM - Services: Godstone socket, teleporter, other fees. TODO: Add Player owner; value and check for
 *         PremiumRates or faction price influence.
 */
public class PricesService {

	/**
	 * Used in SM_PRICES
	 *
	 * @return buyingPrice
	 */
	public static final int getGlobalPrices(Race playerRace) {
		int defaultPrices = PricesConfig.DEFAULT_PRICES;

		if (!SiegeConfig.SIEGE_ENABLED)
			return defaultPrices;

		float influenceValue = 0;
		switch (playerRace) {
			case ASMODIANS:
				influenceValue = Influence.getInstance().getGlobalAsmodiansInfluence();
				break;
			case ELYOS:
				influenceValue = Influence.getInstance().getGlobalElyosInfluence();
				break;
			default:
				influenceValue = 0.5f;
				break;
		}
		if (influenceValue == 0.5f) {
			return defaultPrices;
		}
		else if (influenceValue > 0.5f) {
			float diff = influenceValue - 0.5f;
			return Math.round(defaultPrices - ((diff / 2) * 100));
		}
		else {
			float diff = 0.5f - influenceValue;
			return Math.round(defaultPrices + ((diff / 2) * 100));
		}
	}

	/**
	 * Used in SM_PRICES
	 *
	 * @return
	 */
	public static final int getGlobalPricesModifier() {
		return PricesConfig.DEFAULT_MODIFIER;
	}

	/**
	 * Used in SM_PRICES
	 *
	 * @return taxes
	 */
	public static final int getTaxes(Race playerRace) {
		int defaultTax = PricesConfig.DEFAULT_TAXES;

		if (!SiegeConfig.SIEGE_ENABLED)
			return defaultTax;

		float influenceValue = 0;
		switch (playerRace) {
			case ASMODIANS:
				influenceValue = Influence.getInstance().getGlobalAsmodiansInfluence();
				break;
			case ELYOS:
				influenceValue = Influence.getInstance().getGlobalElyosInfluence();
				break;
			default:
				influenceValue = 0.5f;
				break;
		}
		if (influenceValue >= 0.5f) {
			return defaultTax;
		}
		float diff = 0.5f - influenceValue;
		return Math.round(defaultTax + ((diff / 4) * 100));
	}

	/**
	 * Used in SM_TRADELIST.
	 *
	 * @return buyPriceModifier
	 */
	public static final int getVendorBuyModifier() {
		return PricesConfig.VENDOR_BUY_MODIFIER;
	}

	/**
	 * Used in SM_SELL_ITEM - Can be unique per NPC!
	 *
	 * @return sellingModifier
	 */
	public static final int getVendorSellModifier(Race playerRace) {
		return (int) ((int) ((int) (PricesConfig.VENDOR_SELL_MODIFIER * getGlobalPrices(playerRace) / 100F)
			* getGlobalPricesModifier() / 100F)
			* getTaxes(playerRace) / 100F);
	}

    public static final int getVendorSellModifieNew(Race playerRace, ItemTemplate item) {
        return (int) ((int) ((int) (getItemQualityModifer(item) * getGlobalPrices(playerRace) / 100F)
                * getGlobalPricesModifier() / 100F)
                * getTaxes(playerRace) / 100F);
    }
	/**
	 * @param basePrice
	 * @return modifiedPrice
	 */
	public static final long getPriceForService(long basePrice, Race playerRace) {
		// Tricky. Requires multiplication by Prices, Modifier, Taxes
		// In order, and round down each time to match client calculation.
		return (long) ((long) ((long) (basePrice * getGlobalPrices(playerRace) / 100D) * getGlobalPricesModifier() / 100D)
			* getTaxes(playerRace) / 100D);
	}

    public static final int getTaxModiferForService(Race playerRace) {
        // Tricky. Requires multiplication by Prices, Modifier, Taxes
        // In order, and round down each time to match client calculation.
        return getGlobalPrices(playerRace) + getGlobalPricesModifier()
                + getTaxes(playerRace) - 300;

    }

	/**
	 * @param requiredKinah
	 * @return modified requiredKinah
	 */
	public static final long getKinahForBuy(long requiredKinah, Race playerRace) {
		// Requires double precision for 2mil+ kinah items
		return (long) ((long) ((long) ((long) (requiredKinah * getVendorBuyModifier() / 100.0D)
			* getGlobalPrices(playerRace) / 100.0D)
			* getGlobalPricesModifier() / 100.0D)
			* getTaxes(playerRace) / 100.0D);
	}

	/**
	 * @param kinahReward
	 * @return
	 */
	public static final long getKinahForSell(long kinahReward, Race playerRace) {
		return (long) (kinahReward * getVendorSellModifier(playerRace) / 100D);
	}

    public static final long getItemQualityModifer(ItemTemplate item)
    {
      if (item.getItemQuality()== ItemQuality.COMMON)
        return (long) (PricesConfig.VENDOR_SELL_MODIFIER_COMMON);

    else if (item.getItemQuality()== ItemQuality.RARE)
        return (long) (PricesConfig.VENDOR_SELL_MODIFIER_RAR);

    else if (item.getItemQuality()== ItemQuality.LEGEND)
        return (long) (PricesConfig.VENDOR_SELL_MODIFIER_LEGEND);

    else if (item.getItemQuality()== ItemQuality.UNIQUE)
        return (long) (PricesConfig.VENDOR_SELL_MODIFIER_UNIQU);

    else if (item.getItemQuality()== ItemQuality.EPIC)
        return (long) (PricesConfig.VENDOR_SELL_MODIFIER_EPIC);

      else if (item.getItemQuality()== ItemQuality.MYTHIC)
          return (long) (PricesConfig.VENDOR_SELL_MODIFIER_MYTHIC);

      else
          return (long) (PricesConfig.VENDOR_SELL_MODIFIER);
    }

    public static final int getKinahForSellWithOutTax(int kinahReward, ItemTemplate item, Race race) {
        if(item.getItemQuality() == null)
            return Math. round(kinahReward * getVendorSellModifier(race) / 100);
        else {
            int pricewithqualitimod = (int) (kinahReward * getItemQualityModifer(item) / 100);
            int pricewithtax = pricewithqualitimod * getTaxModiferForService(race) / 100;
            return Math.round(pricewithqualitimod + pricewithtax);
        }
    }
}
