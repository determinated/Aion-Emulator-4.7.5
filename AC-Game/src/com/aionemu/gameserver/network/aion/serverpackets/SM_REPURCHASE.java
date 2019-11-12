package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.PacketLoggerService;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob;
import com.aionemu.gameserver.services.RepurchaseService;

import java.util.Collection;

/**
 * @author xTz, KID
 * @author Skunkworks
 */
public class SM_REPURCHASE extends AionServerPacket {

    private Player player;
    private final int targetObjectId;
    private final Collection<Item> items;

    public SM_REPURCHASE(Player player, int npcId) {
        this.player = player;
        this.targetObjectId = npcId;
        items = RepurchaseService.getInstance().getRepurchaseItems(player.getObjectId());
    }

    @Override
    protected void writeImpl(AionConnection con) {
    	PacketLoggerService.getInstance().logPacketSM(this.getPacketName());
        writeD(targetObjectId);
        writeD(1);
        writeH(Math.min(items.size(), 10));

        int i = 0;
        for (Item item : items) {
            ItemTemplate itemTemplate = item.getItemTemplate();

            writeD(item.getObjectId());
            writeD(itemTemplate.getTemplateId());
            writeNameId(itemTemplate.getNameId());

            ItemInfoBlob itemInfoBlob = ItemInfoBlob.getFullBlob(player, item);
            itemInfoBlob.writeMe(getBuf());

            writeQ(item.getRepurchasePrice());

            i++;
            if (i == 10)
                break;
        }
    }
}
