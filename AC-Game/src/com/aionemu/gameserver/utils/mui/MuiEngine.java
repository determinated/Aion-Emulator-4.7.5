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

package com.aionemu.gameserver.utils.mui;

import com.aionemu.commons.scripting.classlistener.AggregatedClassListener;
import com.aionemu.commons.scripting.classlistener.OnClassLoadUnloadListener;
import com.aionemu.commons.scripting.classlistener.ScheduledTaskClassListener;
import com.aionemu.commons.scripting.scriptmanager.ScriptManager;
import com.aionemu.gameserver.GameServerError;
import com.aionemu.gameserver.model.GameEngine;
import com.aionemu.gameserver.utils.mui.handlers.GeneralMuiHandler;
import com.aionemu.gameserver.utils.mui.handlers.MuiHandler;
import com.aionemu.gameserver.utils.mui.handlers.MuiName;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import javolution.util.FastMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dr2co
 */


public class MuiEngine implements GameEngine {

    private static final Logger log = LoggerFactory.getLogger(MuiEngine.class);
    private static ScriptManager scriptManager = new ScriptManager();
    public static final File MUI_DESCRIPTOR_FILE = new File("./data/scripts/system/muihandlers.xml");
    public static final MuiHandler DUMMY_MUI_HANDLER = new GeneralMuiHandler();
    private FastMap<String, Class<? extends MuiHandler>> handlers = new FastMap<String, Class<? extends MuiHandler>>().shared();

    @Override
    public void load(CountDownLatch progressLatch) {
        log.info("Mui engine load started");
        scriptManager = new ScriptManager();

        AggregatedClassListener acl = new AggregatedClassListener();
        acl.addClassListener(new OnClassLoadUnloadListener());
        acl.addClassListener(new ScheduledTaskClassListener());
        acl.addClassListener(new MuiHandlerClassListener());
        scriptManager.setGlobalClassListener(acl);

        try {
            scriptManager.load(MUI_DESCRIPTOR_FILE);
            log.info("Loaded " + handlers.size() + " mui handlers.");
        } catch (Exception e) {
            throw new GameServerError("Can't initialize mui handlers.", e);
        } finally {
            if (progressLatch != null) {
                progressLatch.countDown();
            }
        }
    }

    @Override
    public void shutdown() {
        log.info("Mui engine shutdown started");
        scriptManager.shutdown();
        scriptManager = null;
        handlers.clear();
        log.info("Mui engine shutdown complete");
    }

    public MuiHandler getNewMuiHandler(String eventName) {
        Class<? extends MuiHandler> instanceClass = handlers.get(eventName);
        MuiHandler instanceHandler = null;
        if (instanceClass != null) {
            try {
                instanceHandler = instanceClass.newInstance();
            } catch (Exception ex) {
                log.warn("Can't instantiate event handler " + eventName, ex);
            }
        }
        if (instanceHandler == null) {
            instanceHandler = DUMMY_MUI_HANDLER;
        }
        return instanceHandler;
    }

    /**
     * @param handler
     */
    final void addMuiHandlerClass(Class<? extends MuiHandler> handler) {
        MuiName nameAnnotation = handler.getAnnotation(MuiName.class);
        if (nameAnnotation != null) {
            handlers.put(nameAnnotation.value(), handler);
        }
    }

    public FastMap<String, Class<? extends MuiHandler>> getHendlers() {
        return handlers;
    }

    public static final MuiEngine getInstance() {
        return SingletonHolder.instance;
    }

    @SuppressWarnings("synthetic-access")
    private static class SingletonHolder {

        protected static final MuiEngine instance = new MuiEngine();
    }
}
