
package ai.siege.anoha;

import ai.AggressiveNpcAI2;
import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.world.WorldPosition;

import java.util.concurrent.Future;

/**
 * @author yayaya
 */
@AIName("anoha_lava") //855264
public class lavaNpcAI2 extends AggressiveNpcAI2 {

    private Future<?> task;
    private int spawnCount;

    @Override
    public boolean canThink() {
        return false;
    }

    @Override
    protected void handleSpawned() {
        super.handleSpawned();
        if (getNpcId() == 855263) {
            startSpawnTask();
        } else {
            ThreadPoolManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    if (!isAlreadyDead()) {
                        SkillEngine.getInstance().getSkill(getOwner(), 21767, 60, getOwner()).useNoAnimationSkill();
                        AI2Actions.deleteOwner(lavaNpcAI2.this);
                    }
                }
            }, 1000);

        }
    }

    private void startSpawnTask() {
        task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (isAlreadyDead()) {
                    cancelTask();
                } else {
                    spawnCount++;
                    WorldPosition p = getPosition();
                    spawn(855264, p.getX(), p.getY(), p.getZ(), p.getHeading());
                    if (spawnCount >= 20) {
                        cancelTask();
                        AI2Actions.deleteOwner(lavaNpcAI2.this);
                    }
                }
            }
        }, 90000, 90000);
    }

    private void cancelTask() {
        if (task != null && !task.isDone()) {
            task.cancel(true);
        }
    }

    @Override
    public AIAnswer ask(AIQuestion question) {
        switch (question) {
            case CAN_ATTACK_PLAYER:
                return AIAnswers.POSITIVE;
            default:
                return AIAnswers.NEGATIVE;
        }
    }

    @Override
    protected void handleDespawned() {
        cancelTask();
        super.handleDespawned();
    }

    @Override
    protected void handleDied() {
        cancelTask();
        super.handleDied();
        AI2Actions.deleteOwner(this);
    }
}
