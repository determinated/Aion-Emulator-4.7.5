
package ai.siege.anoha;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.skillengine.SkillEngine;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import ai.AggressiveNpcAI2;
import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.manager.EmoteManager;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.world.knownlist.Visitor;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.utils.i18n.CustomMessageId;
import com.aionemu.gameserver.utils.i18n.LanguageHandler;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Npc;


/**
 * @author yayaya
 */
@AIName("anoha_boss") //855263 
public class AnohaNpcAI2 extends AggressiveNpcAI2 {
	
	private AtomicBoolean isHome = new AtomicBoolean(true);
    protected List<Integer> percents = new ArrayList<Integer>();
	private boolean canThink = true;
	private Future <? > skillTask;

    @Override
    protected void handleSpawned() {
        addPercent();
        super.handleSpawned();
	}		  
	
	@Override
	public boolean canThink() {
		return canThink;
	}

    @Override
    protected void handleAttack(Creature creature) {
        super.handleAttack(creature);
        checkPercentage(getLifeStats().getHpPercentage());
		if (isHome.compareAndSet(true, false)) {
			startSkillTask();

		}
    }
	


    @Override
    protected void handleBackHome() {
        addPercent();
        super.handleBackHome();

    }

    @Override
    protected void handleDespawned() {
        percents.clear();
        super.handleDespawned();
    }

    @Override
    protected void handleDied() {
        percents.clear();
        super.handleDied();

    }
	
	/**
    *  метод процентности
    **/
		

    private void addPercent() {
        percents.clear();
        Collections.addAll(percents, new Integer[]{85, 75, 50, 5});
    }

	/**
    *  старт скилов по времени 
    **/
	
	private void startSkillTask() {
		skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (isAlreadyDead())
					cancelTask();
				else {
					chooseRandomEvent();
				}
			}
		}, 10000, 20000);
	}
	
	
	private void cancelTask() {
		if (skillTask != null && !skillTask.isCancelled()) {
			skillTask.cancel(true);
		}
	}
	
	/**
    *  случайный ивент скилов по времени
    **/
	
	private void chooseRandomEvent() {
		int rand = Rnd.get(0, 4);
		if (rand == 0)
			skill1();
		if (rand == 1)
			skill2();
		if (rand == 2)
			skill3();	
		if (rand == 3)
			skill3();		
		else
			AlwaysSkill();
	}
	
    /**
    *  скилы случайные 1 из 5 будут выскакивать по времени
    **/	
	
	private void skill1() { //ID: 21762 Горение лавы 85%
		AI2Actions.targetSelf(AnohaNpcAI2.this);
		SkillEngine.getInstance().getSkill(getOwner(), 21762, 55, getOwner()).useNoAnimationSkill();
	}
	private void skill2() { 
		AI2Actions.targetSelf(AnohaNpcAI2.this);
		SkillEngine.getInstance().getSkill(getOwner(), 21767, 55, getOwner()).useNoAnimationSkill();
	}
	private void skill3() { //ID: 21765 Призыв кислоты 75% 
		AI2Actions.targetSelf(AnohaNpcAI2.this);
		SkillEngine.getInstance().getSkill(getOwner(), 21765, 55, getOwner()).useNoAnimationSkill();
	}
	private void skill4() { //ID: 21766 испепеление снять нельзя тик дамаг 50 50% 
		AI2Actions.targetSelf(AnohaNpcAI2.this);
		SkillEngine.getInstance().getSkill(getOwner(), 21766, 55, getOwner()).useNoAnimationSkill();
	}
	private void AlwaysSkill() { 
		AI2Actions.targetSelf(AnohaNpcAI2.this);
		SkillEngine.getInstance().getSkill(getOwner(), 21766, 55, getOwner()).useNoAnimationSkill();
	}
	
    /**
    *   Спавн нпц рядом с рб с попроавкой в нескольких шагах  от него
    **/	
	//855264 855265 855266 855267 855268 - лава для нее скилл 21767 21761

	
	/**
    *   метод проверки процентности хп нпц
    **/	
	
    private synchronized void checkPercentage(int hpPercent) {	 
        for(Integer percent : percents) {			
            if(hpPercent <= percent) {				
                switch(percent) {
                    case 85:
					//ID: 21763 Завихрение лавы 85% - дамаг дикий 100 000хп)
		                 SkillEngine.getInstance().getSkill(getOwner(), 21763, 55, getOwner()).useNoAnimationSkill();
                        break;					
                    case 75:
					//ID: 21763 Завихрение лавы 85% - дамаг дикий 100 000хп)
		                 SkillEngine.getInstance().getSkill(getOwner(), 21763, 55, getOwner()).useNoAnimationSkill();
                        break;
                    case 50:
					//ID: 21763 Завихрение лавы 85% - дамаг дикий 100 000хп)
		                 SkillEngine.getInstance().getSkill(getOwner(), 21763, 55, getOwner()).useNoAnimationSkill();
                        break;	
                    case 5:
				
                        break;	

                }
                percents.remove(percent);
                break;
            }
        }
    }

	/**
    *   удаляем НПЦ
    **/		
	


}
