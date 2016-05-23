package the_fireplace.caterpillar.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.ITickable;
import the_fireplace.caterpillar.Caterpillar;
import the_fireplace.caterpillar.blocks.BlockDrillBase;
import the_fireplace.caterpillar.blocks.BlockDrillHeads;
import the_fireplace.caterpillar.containers.ContainerCaterpillar;
import the_fireplace.caterpillar.containers.ContainerDrillHead;
import the_fireplace.caterpillar.guis.GuiDrillHead;

import java.util.Random;
public class TileEntityDrillHead extends TileEntityLockable implements ITickable
{
	private static final Random RNG = new Random();

	protected String customName;
	public boolean isRemote = false;
	public ContainerDrillHead MyDrillhead;
	public boolean isSelected = false;
	public TileEntityDrillHead()
	{
		//Reference.printDebug("No Remote World?");
	}
	private ContainerCaterpillar MyCaterpillar()
	{
		if (this.isSelected)
		{
			return Caterpillar.instance.getSelectedCaterpillar();
		}
		return Caterpillar.instance.getContainerCaterpillar(this.pos, this.worldObj);
	}
	private ItemStack[] FixZeroItem(ItemStack[] toFix)
	{
		for (int i = 0; i < toFix.length; i++) {
			ItemStack K = toFix[i];
			if (K != null)
			{
				if (K.stackSize < 1)
				{
					toFix[i] = null;
				}
			}
		}
		return toFix;
	}
	public ItemStack[] getItemStacks()
	{

		try {
			return  this.FixZeroItem(Caterpillar.instance.getInventory(this.MyCaterpillar(), this.MyCaterpillar().tabs.selected));
		} catch (Exception e) {
			if (!Caterpillar.proxy.isServer()){
				Minecraft.getMinecraft().currentScreen = null;
			}
			if (!Caterpillar.proxy.isServer())
			{
				if (Minecraft.getMinecraft().currentScreen instanceof GuiDrillHead)
				{
					Minecraft.getMinecraft().currentScreen = null;
				}
			}
			return new ItemStack[256];
		}

	}

	@Override
	public int getSizeInventory()
	{
		return this.getItemStacks().length;
	}
	private void setCustomItem(int index, ItemStack inwego)
	{
		if (index < this.getItemStacks().length)
		{
			this.getItemStacks()[index] = inwego;
		}
	}
	private ItemStack getCustomItem(int index)
	{
		if (index >= this.getItemStacks().length)
		{
			return null;
		}
		return this.getItemStacks()[index];
	}
	@Override
	public ItemStack getStackInSlot(int index)
	{

		return this.getCustomItem(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count)
	{
		if (this.getCustomItem(index) != null)
		{
			ItemStack itemstack;

			if (this.getCustomItem(index).stackSize <= count)
			{
				itemstack =this.getCustomItem(index);
				this.setCustomItem(index, null);
				this.markDirty();
				return itemstack;
			}
			else
			{
				itemstack =this.getCustomItem(index).splitStack(count);

				if (this.getCustomItem(index).stackSize == 0)
				{
					this.setCustomItem(index, null);
				}

				this.markDirty();
				return itemstack;
			}
		}
		else
		{
			return null;
		}
	}

	@Override
	public ItemStack removeStackFromSlot(int index)
	{
		this.clear();
		return null;
	}

	public int getDispenseSlot()
	{
		int i = -1;
		int j = 1;

		for (int k = 0; k <this.getItemStacks().length; ++k)
		{
			if (this.getItemStacks()[k] != null && RNG.nextInt(j++) == 0)
			{
				i = k;
			}
		}

		return i;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack)
	{
		this.setCustomItem(index, stack);

		if (stack != null && stack.stackSize > this.getInventoryStackLimit())
		{
			stack.stackSize = this.getInventoryStackLimit();
		}
		this.markDirty();
	}

	/**
	 * Add the given ItemStack to this Dispenser. Return the Slot the Item was placed in or -1 if no free slot is
	 * available.
	 */
	public int addItemStack(ItemStack stack)
	{
		for (int i = 0; i <this.getItemStacks().length; ++i)
		{
			if (this.getItemStacks()[i] == null ||this.getItemStacks()[i].getItem() == null)
			{
				this.setInventorySlotContents(i, stack);
				return i;
			}
		}

		return -1;
	}

	/**
	 * Gets the name of this command sender (usually username, but possibly "Rcon")
	 */
	@Override
	public String getName()
	{
		return this.hasCustomName() ? this.customName : "Item Teleporter";
	}

	public void setCustomName(String customName)
	{
		this.customName = customName;
	}

	/**
	 * Returns true if this thing is named
	 */
	@Override
	public boolean hasCustomName()
	{
		return this.customName != null;
	}

	@Override
	public int getInventoryStackLimit()
	{
		if (this.MyCaterpillar() != null)
		{

			if (this.MyCaterpillar().tabs.selected.equals(Caterpillar.GuiTabs.DECORATION) || this.MyCaterpillar().tabs.selected.equals(Caterpillar.GuiTabs.REINFORCEMENT))
			{
				return 1;
			}
		}
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return this.worldObj.getTileEntity(this.pos) == this && player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public void openInventory(EntityPlayer player) {}

	@Override
	public void closeInventory(EntityPlayer player) {}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack)
	{
		return true;
	}

	@Override
	public String getGuiID()
	{
		return Caterpillar.MODID  + ":" + this.blockType.getUnlocalizedName().substring(5);
	}

	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
	{
		return new ContainerDrillHead(playerIn, this, this.MyCaterpillar());
	}

	@Override
	public int getField(int id)
	{
		return 0;
	}

	@Override
	public void setField(int id, int value) {}

	@Override
	public int getFieldCount()
	{
		return 0;
	}
	public boolean MergeStack(TileEntityLockable tmpTileE, Item toAdd)
	{
		for(int i=0;i<tmpTileE.getSizeInventory();i++)
		{
			if (tmpTileE.getStackInSlot(i) != null)
			{
				ItemStack thisPlace = tmpTileE.getStackInSlot(i);
				if (thisPlace.getItem().equals(toAdd))
				{
					if (thisPlace.stackSize < thisPlace.getMaxStackSize())
					{
						thisPlace = new ItemStack(toAdd, thisPlace.stackSize + 1);
						tmpTileE.setInventorySlotContents(i, thisPlace);
						return true;
					}
				}
			}
		}
		for(int i=0;i<tmpTileE.getSizeInventory();i++)
		{
			if (tmpTileE.getStackInSlot(i) == null)
			{
				ItemStack thisPlace = new ItemStack(toAdd, 1);
				tmpTileE.setInventorySlotContents(i, thisPlace);
				return true;
			}
		}
		return false;
	}
	@Override
	public void clear()
	{
	}
	@Override
	public void update() {

		IBlockState blockdriller =  this.worldObj.getBlockState(this.pos);

		if (blockdriller.getBlock() instanceof BlockDrillBase || blockdriller.getBlock() instanceof BlockDrillHeads)
		{
			((BlockDrillBase)blockdriller.getBlock()).movieMe(this.worldObj, this.pos, this.worldObj.getBlockState(this.pos));
		}



	}
}