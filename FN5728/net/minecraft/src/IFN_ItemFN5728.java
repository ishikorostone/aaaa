package net.minecraft.src;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.World;

public abstract class IFN_ItemFN5728 extends IFN_ItemFN5728Pre {
	
	/*
	 * リロードのシーケンス
	 * 0x0000	:射撃状態
	 * 0x1000	:リロード開始
	 * 0x2000	:マガジンリリース、下位24bitはリロード時の残弾
	 * 0x8000	:リロード完了
	 */
	public static int IFNValFire		= 0x0000;
	public static int IFNValReloadTac	= 0x0010;
	public static int IFNValReloadStart	= 0x1000;
	public static int IFNValReleaseMag	= 0x2000;
	public static int IFNValReloadEnd	= 0x8000;



	public IFN_ItemFN5728(int i) {
		super(i);
		setMaxDamage(0);
		maxStackSize = 1;
	}

	@Override
	public boolean isFull3D() {
		return true;
	}

	@Override
	public ItemStack onEaten(ItemStack par1ItemStack, World par2World,
			EntityPlayer par3EntityPlayer) {
		// リロード完了
		mod_IFN_FN5728Guns.Debug(String.format("onFoodEaten-remort:%b", par2World.isRemote));
		reloadMagazin(par1ItemStack, par2World, par3EntityPlayer);
		return par1ItemStack;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityPlayer entityplayer, int i) {
		// リロード中止
		mod_IFN_FN5728Guns.Debug(String.format("onPlayerStoppedUsing-remort:%b", world.isRemote));
		cancelReload(itemstack, IFNValReloadEnd);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world,	EntityPlayer entityplayer) {
		// トリガー
		int li = getReload(itemstack);
		mod_IFN_FN5728Guns.Debug(String.format("onItemRightClick-remort:%b, val:%04x", world.isRemote, li));
		if (li <= IFNValFire) {
			if (canReload(itemstack, entityplayer)) {
				// ノーマルリロード
				if (isEmpty(itemstack)) {
					mod_IFN_FN5728Guns.Debug(String.format("reloadNomal-remort:%b", world.isRemote));
					releaseMagazin(itemstack, world, entityplayer);
					MMM_Helper.updateCheckinghSlot(entityplayer, itemstack);
				}
			}
		}
		if (li == IFNValReloadTac) {
			if (canReload(itemstack, entityplayer)) {
				// タクティカルリロード
				mod_IFN_FN5728Guns.Debug(String.format("reloadTac-remort:%b", world.isRemote));
				releaseMagazin(itemstack, world, entityplayer);
				MMM_Helper.updateCheckinghSlot(entityplayer, itemstack);
			}
		}
		entityplayer.setItemInUse(itemstack, getMaxItemUseDuration(itemstack));
		return itemstack;
	}

	@Override
	public void onUpdate(ItemStack itemstack, World world, Entity entity, int i, boolean flag) {
//		int li = getReload(itemstack);
//		if (li != 0 && entity instanceof EntityPlayer) {
//			EntityPlayer lep = (EntityPlayer)entity;
//			mod_IFN_FN5728Guns.Debug(String.format("onUpdate-remort:%b, val:%x, dam:%d, using:%b, dt:%d",
//					world.isRemote, li, itemstack.getItemDamage(), lep.getItemInUse() == itemstack, lep.getItemInUseDuration()));
//		}
		if (world.isRemote) {
			if (MMM_Helper.mc.thePlayer != entity) {
				// クライアントの保持しているプレーヤー以外は処理する必要がない
				return;
			}
			if (isReload(itemstack)) {
				// リロード中でも終了
				return;
			}
			if (entity instanceof EntityPlayer) {
				EntityPlayer lep = (EntityPlayer)entity;
				if (lep.getCurrentEquippedItem() != itemstack) {
					// 今手に持っていなければ終了
					return;
				}
				if (lep.getItemInUse() == itemstack) {
					// 使用中でも終了
					return;
				}
				// マルチ用タクティカルリロード判定処理
				int li = getReload(itemstack);
				try {
					// クアライアント専用コードなのでForgeMPだとエラーが出る
					// というか何でマルチ側でModloaderがよべるん・・・。
					if (MMM_Helper.mc.gameSettings.keyBindAttack.pressed) {
						if (li == IFNValFire) {
							mod_IFN_FN5728Guns.Debug("tacticalIFN");
							li = IFNValReloadTac;
							ModLoader.clientSendPacket(new Packet250CustomPayload("IFN", new byte[] {(byte)((li >>> 8) & 0xff), (byte)(li & 0xff)}));
						}
					} else {
						if (li == IFNValReloadTac) {
							mod_IFN_FN5728Guns.Debug("nomalIFN");
							li = IFNValFire;
							ModLoader.clientSendPacket(new Packet250CustomPayload("IFN", new byte[] {(byte)((li >>> 8) & 0xff), (byte)(li & 0xff)}));
						}
					}
				} catch (Error e) {
				} catch (Exception e) {
				}
			}
		}
	}

	@Override
	public int getMaxItemUseDuration(ItemStack itemstack) {
		// リロード時は時間を変更
		int li = getReload(itemstack);
		if ((li >= IFNValReloadStart) && (li & 0xf000) < IFNValReloadEnd) {
			return reloadTime();
		} else {
			return super.getMaxItemUseDuration(itemstack);
		}
	}

	@Override
	public EnumAction getItemUseAction(ItemStack itemstack) {
		// リロード時は構えが違う
		return isReload(itemstack) ? EnumAction.block : EnumAction.bow;
	}

	// 独自
	/**
	 * 弾の発射。
	 * 弾薬を別クラスにしてそっちに移すか？
	 * @param f1:弾道安定性。
	 * @param f2:装薬効率
	 * @param f3:反動制御率
	 */
	protected void fireBullet(ItemStack itemstack, World world, EntityPlayer entityplayer, float f1, float f2, float f3) {
		// 発射
		ItemStack lis = getAmmo(itemstack, itemstack.getMaxDamage() - itemstack.getItemDamage());
		if (lis == null) {
			lis = new ItemStack(getBulletID(itemstack), 1, 0);
		}
		boolean lflag = true;
		if (lis.getItem() instanceof IFN_ItemSS190) {
			IFN_ItemSS190 lib = (IFN_ItemSS190)lis.getItem();
			lflag = lib.fireBullet(itemstack, world, entityplayer, lis.getItemDamage(), f1, f2, f3);
		}
		if (lflag) {
			clearAmmo(itemstack, itemstack.getMaxDamage() - itemstack.getItemDamage());
			itemstack.damageItem(1, entityplayer);
		}
	}

	protected void cancelReload(ItemStack itemstack, int force) {
		if (getReload(itemstack) >= force) {
			// リロードのキャンセル
			setReload(itemstack, IFNValFire);
		}
	}

	protected boolean canReload(ItemStack itemstack, EntityPlayer entityplayer) {
		// リロードが可能かどうかの判定（エンチャント対応）
		if (entityplayer.capabilities.isCreativeMode) return true;
		for (ItemStack is : entityplayer.inventory.mainInventory) {
			if (isConformityBullet(is)) return true;
		}
		return false;
	}

	protected boolean isEmpty(ItemStack itemstack) {
		// 残弾ゼロ？
		return itemstack.getItemDamage() >= getMaxDamage();
	}

	protected void releaseMagazin(ItemStack itemstack, World world, Entity entity) {
		// マガジンをリリースしたときの動作、残弾を記録
		setReload(itemstack, (IFNValReleaseMag | (itemstack.getItemDamage() & 0x0fff)));
		itemstack.setItemDamage(getMaxDamage());
	}

	protected void reloadMagazin(ItemStack itemstack, World world, EntityPlayer entityplayer) {
		// マガジンを入れたときの動作
//		if (!world.isRemote) 
		{
			// リロード
			if (entityplayer == null || entityplayer.capabilities.isCreativeMode) {
				itemstack.setItemDamage(0);
			} else {
				// インベントリから弾薬を減らす
				boolean linfinity = EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, itemstack) > 0;
				int lk = getReload(itemstack);
				lk = (lk > 0) ? lk & 0x0fff : 0;
				itemstack.setItemDamage(lk);
				for (int ll = 0; lk > 0 && ll < entityplayer.inventory.mainInventory.length; ll++) {
					ItemStack lis = entityplayer.inventory.mainInventory[ll];
					if (isConformityBullet(lis)) {
						for (;lk > 0 && lis.stackSize > 0;) {
							setAmmo(itemstack, lk--, lis);
							itemstack.setItemDamage(itemstack.getItemDamage() - 1);
							if (!linfinity) {
								// いんふぃが付いてなかったら弾を減らす。
								lis.stackSize--;
							}
							if (lis.stackSize <= 0) {
								entityplayer.inventory.mainInventory[ll] = null;
							}
						}
					}
				}
			}
		}
		mod_IFN_FN5728Guns.Debug(String.format("AmmoList."));
		for (int li = 0; li < itemstack.getMaxDamage(); li++) {
			ItemStack lis = getAmmo(itemstack, li);
			if (lis != null) {
				mod_IFN_FN5728Guns.Debug(String.format("Ammo:%03d=%s(%d, %d)", li, lis.getUnlocalizedName(), lis.itemID, lis.getItemDamage()));
			}
		}
		setReload(itemstack, IFNValReloadEnd);
		clearBolt(itemstack);
		MMM_Helper.updateCheckinghSlot(entityplayer, itemstack);
	}

	// リロードにかかる時間
	public abstract int reloadTime();

	public int getBulletID(ItemStack itemstack) {
		// 弾薬の種類
		return mod_IFN_FN5728Guns.fn_SS190.itemID;
	}

	/**
	 * 使用出来る弾薬かどうかの判定
	 */
	public boolean isConformityBullet(ItemStack pItemstack) {
		if (pItemstack != null && pItemstack.itemID == mod_IFN_FN5728Guns.fn_SS190.itemID) {
			return true;
		}
		return false;
	}

	// littleMaidMobはこのメソッドを参照して特殊動作を行います
	public boolean isWeaponReload(ItemStack itemstack, EntityPlayer entityplayer) {
		// リロード実行するべきか？
		cancelReload(itemstack, IFNValReloadEnd);
		return isEmpty(itemstack) && canReload(itemstack, entityplayer);
	}

	public boolean isWeaponFullAuto(ItemStack itemstack) {
		// フルオート武器か？
		// （右クリックした時点で射撃開始されるもの）
		return false;
	}

	/**
	 * リロードカウンタ読み取り
	 */
	public int getReload(ItemStack pItemstack) {
		checkTags(pItemstack);
		return pItemstack.getTagCompound().getInteger("Reload");
	}

	/**
	 * リロードカウンタのセット
	 */
	public void setReload(ItemStack pItemstack, int pValue) {
		checkTags(pItemstack);
		NBTTagCompound lnbt = pItemstack.getTagCompound();
		lnbt.setInteger("Reload", pValue);
	}

	/**
	 * リロード中かね？
	 */
	public boolean isReload(ItemStack pItemstack) {
		return getReload(pItemstack) >= IFNValReloadStart;
	}

	// 連射用のタイミング回路
	/**
	 * 連射タイミングの設定。
	 * 1=50ms、20=1000ms=1s。
	 */
	public byte getCycleCount(ItemStack pItemstack) {
		return (byte)1;
	}

	protected void resetBolt(ItemStack pItemstack) {
		checkTags(pItemstack);
		pItemstack.getTagCompound().setByte("Bolt", getCycleCount(pItemstack));
	}

	protected void clearBolt(ItemStack pItemstack) {
		checkTags(pItemstack);
		pItemstack.getTagCompound().setByte("Bolt", (byte)0);
	}

	/**
	 * 発射タイミングの確認
	 */
	protected boolean cycleBolt(ItemStack pItemstack) {
		checkTags(pItemstack);
		NBTTagCompound lnbt = pItemstack.getTagCompound();
		byte lb = lnbt.getByte("Bolt");
		if (lb <= 0) {
//			if (pReset) resetBolt(pItemstack);
			return true;
		} else {
			lnbt.setByte("Bolt", --lb);
			return false;
		}
	}

	protected int getBolt(ItemStack pItemstack) {
		checkTags(pItemstack);
		NBTTagCompound lnbt = pItemstack.getTagCompound();
		return lnbt.getByte("Bolt");
	}

	/**
	 * マガジンに弾を込める
	 */
	public void setAmmo(ItemStack pGun, int pIndex, ItemStack pAmmo) {
		if (!pGun.getTagCompound().hasKey("Ammo")) {
			pGun.getTagCompound().setCompoundTag("Ammo", new NBTTagCompound());
		}
		NBTTagCompound lnbt = pGun.getTagCompound().getCompoundTag("Ammo");
		lnbt.setInteger(Integer.toString(pIndex) + "i", pAmmo.itemID);
		lnbt.setInteger(Integer.toString(pIndex) + "d", pAmmo.getItemDamage());
	}

	/**
	 * 装弾されている弾を取り出す
	 */
	public ItemStack getAmmo(ItemStack pGun, int pIndex) {
		NBTTagCompound lnbt = pGun.getTagCompound().getCompoundTag("Ammo");
		int lid = lnbt.getInteger(Integer.toString(pIndex) + "i");
		int ldam = lnbt.getInteger(Integer.toString(pIndex) + "d");
		return lid == 0 ? null : new ItemStack(lid, 1, ldam);
	}

	public void clearAmmo(ItemStack pGun, int pIndex) {
		NBTTagCompound lnbt = pGun.getTagCompound().getCompoundTag("Ammo");
		String ls = Integer.toString(pIndex);
		lnbt.removeTag(ls + "i");
		lnbt.removeTag(ls + "d");
	}

	public boolean checkTags(ItemStack pitemstack) {
		// NBTTagの初期化
		if (pitemstack.hasTagCompound()) {
			return true;
		}
		NBTTagCompound ltags = new NBTTagCompound();
		pitemstack.setTagCompound(ltags);
		ltags.setInteger("Reload", 0x0000);
		ltags.setByte("Bolt", (byte)0);
		NBTTagCompound lammo = new NBTTagCompound();
		for (int li = 0; li < getMaxDamage(); li++) {
			lammo.setLong(Integer.toString(li), 0L);
		}
		ltags.setCompoundTag("Ammo", lammo);
		return false;
	}

}
