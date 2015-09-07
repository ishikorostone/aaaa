package net.minecraft.src;

import java.util.Map;

import cpw.mods.fml.common.registry.EntityRegistry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.Packet23VehicleSpawn;
import net.minecraft.network.packet.Packet250CustomPayload;

public class mod_IFN_FN5728Guns extends BaseMod {

	@MLProp(info="Bullet SS190's ItemID.(shiftedindex = -256. -1 is All Items Disable.)")
	public static int ID_SS190 = 22240; 
	@MLProp(info="Gun Five-seveN's ItemID.(shiftedindex = -256. -1 is Disable.)")
	public static int ID_FiveseveN = 22241; 
	@MLProp(info="Gun P90's ItemID.(shiftedindex = +256. -1 is Disable.)")
	public static int ID_P90 = 22242; 
	@MLProp(info="Ignore heartstime.")
	public static boolean isArmorPiercing = true; 
	@MLProp()
	public static boolean UnlimitedInfinity = false;
	@MLProp
	public static boolean isDebugMessage = false;
	
	public static Item fn_fiveseven;
	public static Item fn_p90;
	public static Item fn_SS190;
	public static Class classSS190;


	public static void Debug(String pText, Object... pData) {
		// デバッグメッセージ
		if (isDebugMessage) {
			System.out.println(String.format("FN5728-" + pText, pData));
		}
	}

	@Override
	public String getName() {
		return "FN5728Gun's";
	}

	@Override
	public String getPriorities() {
		return "required-after:mod_MMM_MMMLib";
	}

	@Override
	public String getVersion() {
		return "1.6.4-kai-1";
	}

	@Override
	public void load() {
		/*
		// MMMLibのRevisionチェック
		MMM_Helper.checkRevision("3");
		
		classSS190 = MMM_Helper.getForgeClass(this, "IFN_EntitySS190");
		if (classSS190 == null) {
			return;
		}
		*/
		
		// 5.7x28mm SS190
		fn_SS190 = (new IFN_ItemSS190(ID_SS190 - 256)).setUnlocalizedName("SS190").setTextureName("SS190");
		ModLoader.addName(fn_SS190, "5.7x28mm SS190");
		ModLoader.addRecipe(new ItemStack(fn_SS190, 16), new Object[] {
			"i", "g", "g",  
			Character.valueOf('i'), Item.ingotIron,
			Character.valueOf('g'), Item.gunpowder
		});
		//MMM_Helper.registerEntity(classSS190, "SS190", 0, this, 64, 20, false);
		EntityRegistry.registerGlobalEntityID(IFN_EntitySS190.class, "SS190", 733);
		EntityRegistry.registerModEntity(IFN_EntitySS190.class, "SS190", 0, this, 64, 20, false);
		
		// Five-seveN
		if (ID_FiveseveN > -1) {
			fn_fiveseven = (new IFN_ItemFiveseveN(ID_FiveseveN - 256)).setUnlocalizedName("FiveSeven").setTextureName("FiveSeven");
			ModLoader.addName(fn_fiveseven, "Five-seveN");
			ModLoader.addRecipe(new ItemStack(fn_fiveseven, 1, fn_fiveseven.getMaxDamage()), new Object[] {
				"iii", "  i", 
				Character.valueOf('i'), Item.ingotIron
			});
		}
		
		// P90
		if (ID_P90 > -1) {
			fn_p90 = (new IFN_ItemP90(ID_P90 - 256)).setUnlocalizedName("P90").setTextureName("P90");
			ModLoader.addName(fn_p90, "P90");
			ModLoader.addRecipe(new ItemStack(fn_p90, 1, fn_p90.getMaxDamage()), new Object[] {
				"i  ", "iii", "iii", 
				Character.valueOf('i'), Item.ingotIron
			});
		}
		
		// タクティカルリロード用パケット
		ModLoader.registerPacketChannel(this, "IFN");
		
		// 音声追加実験
//		MMM_Helper.mc.sndManager.addSound("FN5728/fnP90_s.ogg");
	}

	@Override
	public void addRenderer(Map map) {
		if (ID_SS190 >= 0) {
			// 継承クラスにも適用されるので個別登録は必要ない
			map.put(IFN_EntitySS190.class, new IFN_RenderSS190());
		}
	}

	/*
	//Modloader
	@Override
	public Packet23VehicleSpawn getSpawnPacket(Entity var1, int var2) {
		// 弾を発生させる
		// Forge環境下では呼ばれない
		EntityLivingBase lentity = ((IFN_EntitySS190)var1).thrower;
		return new IFN_PacketSS190Spawn(var1, 0, lentity == null ? 0 : lentity.entityId);
	}
	*/

	@Override
	public void serverCustomPayload(NetServerHandler handler, Packet250CustomPayload packet) {
		// タクティカルリロード用
		EntityPlayerMP lplayer = handler.playerEntity;
		ItemStack lis = lplayer.getCurrentEquippedItem();
		if (lis != null && lis.getItem() instanceof IFN_ItemFN5728) {
			IFN_ItemFN5728 lifn = (IFN_ItemFN5728)lis.getItem();
			Debug(String.format("reciveIFN:%s:%04x", packet.channel, (packet.data[0] << 8) | packet.data[1]));
			if (lifn.isReload(lis)) {
				Debug(String.format("reloadNow."));
			} else {
				Debug(String.format("setReload."));
				lifn.setReload(lis, (packet.data[0] << 8) | packet.data[1]);
				lplayer.clearItemInUse();
			}
//			MMM_Helper.updateCheckinghSlot(lplayer, lis);
		}
	}

}
