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
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Aion-Lightning.
 *  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Credits goes to all Open Source Core Developer Groups listed below
 * Please do not change here something, regarding the developer credits, except the "developed by XXXX".
 * Even if you edit a lot of files in this source, you still have no rights to call it as "your Core".
 * Everybody knows that this Emulator Core was developed by Aion Lightning 
 * @-Aion-Unique-
 * @-Aion-Lightning
 * @Aion-Engine
 * @Aion-Extreme
 * @Aion-NextGen
 * @Aion-Core Dev.
 */
package com.aionemu.gameserver.model.templates.item.actions;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.controllers.ObserveController;
import com.aionemu.gameserver.controllers.PlayerController;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Equipment;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.stats.container.PlayerGameStats;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INVENTORY_UPDATE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemPacketService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="AuthorizeAction")
public class AuthorizeAction
  extends AbstractItemAction
{
  @XmlAttribute(name="count")
  private int count;
  
  public boolean canAct(Player player, Item parentItem, Item targetItem)
  {
    if (!targetItem.getItemTemplate().isAccessory()) {
      return false;
    }
    if (targetItem.getItemTemplate().getAuthorize() == 0) {
      return false;
    }
    return targetItem.getAuthorize() < targetItem.getItemTemplate().getAuthorize();
  }
  
  public void act(final Player player, final Item parentItem, final Item targetItem)
  {
    PacketSendUtility.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId().intValue(), parentItem.getObjectId().intValue(), parentItem.getItemTemplate().getTemplateId(), 5000, 0, 0));
    final ItemUseObserver local1 = new ItemUseObserver()
    {
      public void abort()
      {
        player.getController().cancelTask(TaskId.ITEM_USE);
        player.getObserveController().removeObserver(this);
        PacketSendUtility.sendPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId().intValue(), parentItem.getObjectId().intValue(), parentItem.getItemTemplate().getTemplateId(), 0, 3, 0));
        ItemPacketService.updateItemAfterInfoChange(player, targetItem);
        PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_AUTHORIZE_CANCEL(targetItem.getNameId()));
      }
    };
    player.getObserveController().attach(local1);
    final boolean bool = isSuccess(player);
    player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable()
    {
      public void run()
      {
        if (player.getInventory().decreaseByItemId(parentItem.getItemId(), 1L))
        {
          if (!bool)
          {
            PacketSendUtility.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId().intValue(), player.getObjectId().intValue(), parentItem.getObjectId().intValue(), parentItem.getItemId(), 0, 2, 0));
            targetItem.setAuthorize(0);
            if (targetItem.getItemTemplate().isPlume()) {
              if (targetItem.isEquipped())
              {
                player.getEquipment().unEquipItem(targetItem.getObjectId().intValue(), player.getEquipment().getEquipedPlume().getEquipmentSlot());
                player.getInventory().decreaseByObjectId(targetItem.getObjectId().intValue(), targetItem.getItemCount());
              }
              else
              {
                player.getInventory().decreaseByObjectId(targetItem.getObjectId().intValue(), targetItem.getItemCount());
              }
            }
            if (targetItem.getItemTemplate().isPlume()) {
              PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_STR_MSG_ITEM_AUTHORIZE_FAILED_TSHIRT(targetItem.getNameId()));
            } else {
              PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_AUTHORIZE_FAILED(targetItem.getNameId()));
            }
          }
          else
          {
            PacketSendUtility.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId().intValue(), player.getObjectId().intValue(), parentItem.getObjectId().intValue(), parentItem.getItemId(), 0, 1, 0));
            targetItem.setAuthorize(targetItem.getAuthorize() + 1);
            PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_AUTHORIZE_SUCCEEDED(targetItem.getNameId(), targetItem.getAuthorize()));
          }
          PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE_ITEM(player, targetItem));
          player.getObserveController().removeObserver(local1);
          if (targetItem.isEquipped()) {
            player.getGameStats().updateStatsVisually();
          }
          ItemPacketService.updateItemAfterInfoChange(player, targetItem);
          if (targetItem.isEquipped()) {
            player.getEquipment().setPersistentState(PersistentState.UPDATE_REQUIRED);
          } else {
            player.getInventory().setPersistentState(PersistentState.UPDATE_REQUIRED);
          }
        }
      }
    }, 5000L));
  }
  
  public boolean isSuccess(Player player)
  {
    int i = Rnd.get(0, 1000);
    if (i < 700)
    {
      if (player.getAccessLevel() > 2) {
        PacketSendUtility.sendMessage(player, "Success! Rnd: " + i + " Luck: 700");
      }
      return true;
    }
    if (player.getAccessLevel() > 2) {
      PacketSendUtility.sendMessage(player, "Fail! Rnd: " + i + " Luck: 700");
    }
    return false;
  }
}
