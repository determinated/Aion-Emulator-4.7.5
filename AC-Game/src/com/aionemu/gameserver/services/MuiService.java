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

package com.aionemu.gameserver.services;

import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.mui.MuiEngine;
import com.aionemu.gameserver.utils.mui.handlers.MuiHandler;

/**
 * @author Dr2co
 */


public class MuiService {

    private MuiHandler handler;

    public void load() {
        handler = MuiEngine.getInstance().getNewMuiHandler(GSConfig.SERVER_LANGUAGE);
    }

    public String getNonUTFMessage(String name, Object... params) {
        return handler.getMessage(name, params);
    }

    public String getMessage(String name, Object... params) {
        return convertFromUTF8(handler.getMessage(name, params));
    }


    public void sendNonUTFMessage(Player player, String message) {
        PacketSendUtility.sendMessage(player, message);
    }

    public void sendMessage(Player player, String name, Object... params) {
        PacketSendUtility.sendMessage(player, convertFromUTF8(handler.getMessage(name, params)));
    }

    public String convertFromUTF8(String s) {
        String out;
        try {
            byte[] bytes = s.getBytes();
            for (int i = 0; i < bytes.length - 1; i++) {
                if (bytes[i] == -48 && bytes[i + 1] == 63) {
                    bytes[i] = (byte) 208;
                    bytes[i + 1] = (byte) 152;
                }
            }
            out = new String(bytes, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }
        return out;
    }

    public static MuiService getInstance() {
        return SingletonHolder.instance;
    }

    @SuppressWarnings("synthetic-access")
    private static class SingletonHolder {

        protected static final MuiService instance = new MuiService();
    }
}
