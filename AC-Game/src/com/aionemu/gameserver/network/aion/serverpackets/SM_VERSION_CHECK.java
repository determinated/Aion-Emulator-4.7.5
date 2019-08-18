package com.aionemu.gameserver.network.aion.serverpackets;


import com.aionemu.commons.network.IPRange;
import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.configs.main.MembershipConfig;
import com.aionemu.gameserver.configs.network.IPConfig;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.network.NetworkController;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.ChatService;
import com.aionemu.gameserver.services.EventService;
import java.util.Iterator;


public class SM_VERSION_CHECK extends AionServerPacket {


   private int version;
   private int characterLimitCount;
   private final int characterFactionsMode;
   private final int characterCreateMode;




   public SM_VERSION_CHECK(int var1) {
      this.version = var1;
      if(MembershipConfig.CHARACTER_ADDITIONAL_ENABLE != 10 && MembershipConfig.CHARACTER_ADDITIONAL_COUNT > GSConfig.CHARACTER_LIMIT_COUNT) {
         this.characterLimitCount = MembershipConfig.CHARACTER_ADDITIONAL_COUNT;
      } else {
         this.characterLimitCount = GSConfig.CHARACTER_LIMIT_COUNT;
      }


      this.characterLimitCount *= NetworkController.getInstance().getServerCount();
      if(GSConfig.CHARACTER_CREATION_MODE >= 0 && GSConfig.CHARACTER_CREATION_MODE <= 2) {
         this.characterFactionsMode = GSConfig.CHARACTER_CREATION_MODE;
      } else {
         this.characterFactionsMode = 0;
      }


      if(GSConfig.CHARACTER_FACTION_LIMITATION_MODE >= 0 && GSConfig.CHARACTER_FACTION_LIMITATION_MODE <= 3) {
         this.characterCreateMode = GSConfig.CHARACTER_FACTION_LIMITATION_MODE * 4;
      } else {
         this.characterCreateMode = 0;
      }


   }


   protected void writeImpl(AionConnection var1) {
      if(this.version < 206) {
         this.writeC(2);
      } else {
         this.writeC(0);
         this.writeC(NetworkConfig.GAMESERVER_ID);
         this.writeD(150430);
         this.writeD(150309);
         this.writeD(0);
         this.writeD(141120);
         this.writeD(1434537729);
         this.writeC(0);
         this.writeC(GSConfig.SERVER_COUNTRY_CODE);
         this.writeC(0);
         int var2 = this.characterLimitCount * 16 | this.characterFactionsMode;
         this.writeC(var2 | this.characterCreateMode);
         this.writeD((int)(System.currentTimeMillis() / 1000L));
         this.writeD(83951966);
         this.writeD(16845327);
         this.writeD(131394);
         this.writeC(GSConfig.CHARACTER_REENTRY_TIME);
         this.writeC(EventsConfig.ENABLE_DECOR);
         this.writeC(EventService.getInstance().getEventType().getId());
         this.writeD(-268435456);
         this.writeD(83886065);
         this.writeC(120);
         this.writeD(16933521);
         this.writeD(0);
         this.writeH(0);
         this.writeC(0);
         this.writeD(17545216);
         this.writeD(16777216);
         this.writeD(-3600);
         this.writeD(256);
         byte[] var3 = IPConfig.getDefaultAddress();
         Iterator var4 = IPConfig.getRanges().iterator();


         while(var4.hasNext()) {
            IPRange var5 = (IPRange)var4.next();
            if(var5.isInRange(var1.getIP())) {
               var3 = var5.getAddress();
               break;
            }
         }


         this.writeB(var3);
         this.writeH(ChatService.getPort());
      }
   }
}