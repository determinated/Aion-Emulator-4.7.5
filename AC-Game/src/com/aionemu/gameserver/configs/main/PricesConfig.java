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

package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

/**
 * @author Sarynth
 */
public class PricesConfig {

	/**
	 * Controls the "Prices:" value in influence tab.
	 */
	@Property(key = "gameserver.prices.default.prices", defaultValue = "100")
	public static int DEFAULT_PRICES;

	/**
	 * Hidden modifier for all prices.
	 */
	@Property(key = "gameserver.prices.default.modifier", defaultValue = "100")
	public static int DEFAULT_MODIFIER;

	/**
	 * Taxes: value = 100 + tax %
	 */
	@Property(key = "gameserver.prices.default.taxes", defaultValue = "100")
	public static int DEFAULT_TAXES;

	@Property(key = "gameserver.prices.vendor.buymod", defaultValue = "100")
	public static int VENDOR_BUY_MODIFIER;

    @Property(key = "gameserver.prices.vendor.sellmod", defaultValue = "20")
    public static int VENDOR_SELL_MODIFIER;

    @Property(key = "gameserver.prices.vendor.sellmodc", defaultValue = "5")
    public static int VENDOR_SELL_MODIFIER_COMMON;

    @Property(key = "gameserver.prices.vendor.sellmodr", defaultValue = "10")
    public static int VENDOR_SELL_MODIFIER_RAR;

    @Property(key = "gameserver.prices.vendor.sellmodl", defaultValue = "15")
    public static int VENDOR_SELL_MODIFIER_LEGEND;

    @Property(key = "gameserver.prices.vendor.sellmodu", defaultValue = "30")
    public static int VENDOR_SELL_MODIFIER_UNIQU;

    @Property(key = "gameserver.prices.vendor.sellmode", defaultValue = "50")
    public static int VENDOR_SELL_MODIFIER_EPIC;

    @Property(key = "gameserver.prices.vendor.sellmodem", defaultValue = "50")
    public static int VENDOR_SELL_MODIFIER_MYTHIC;


}
