package the_fireplace.caterpillar;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.ShapedOreRecipe;
import the_fireplace.caterpillar.blocks.BlockDrillBase;
import the_fireplace.caterpillar.blocks.BlockDrillHeads;
import the_fireplace.caterpillar.containers.ContainerCaterpillar;
import the_fireplace.caterpillar.handlers.HandlerEvents;
import the_fireplace.caterpillar.handlers.HandlerGUI;
import the_fireplace.caterpillar.handlers.HandlerNBTTag;
import the_fireplace.caterpillar.inits.InitBlocks;
import the_fireplace.caterpillar.packets.PacketCaterpillarControls;
import the_fireplace.caterpillar.packets.PacketParticles;
import the_fireplace.caterpillar.proxy.ProxyCommon;
import the_fireplace.caterpillar.tabs.TabCaterpillar;
import the_fireplace.caterpillar.tileentity.TileEntityDrillHead;
import the_fireplace.caterpillar.timers.TimerMain;

import java.util.HashMap;
import java.util.Map.Entry;

import static net.minecraft.init.Items.LAVA_BUCKET;

@Mod(name = Caterpillar.MODNAME, modid = Caterpillar.MODID, guiFactory=Reference.guiFactory)
public class Caterpillar
{
	public static final String MODID = "simplycaterpillar";
	public static String VERSION = "";
	public static final String MODNAME = "Simply Caterpillar";
	public static final String curseCode = "";
	@Instance(Caterpillar.MODID)
	public static Caterpillar instance;

	public static final CreativeTabs TabCaterpillar = new TabCaterpillar();
	public int saveCount = 0;
	public TimerMain ModTasks;

	private HashMap<String, ContainerCaterpillar> mainContainers;
	private ContainerCaterpillar selectedCaterpillar;

	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static ProxyCommon proxy;

	public static SimpleNetworkWrapper network;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		String[] version = event.getModMetadata().version.split("\\.");
		if(version[3].equals("BUILDNUMBER"))//Dev environment
			VERSION = event.getModMetadata().version.replace("BUILDNUMBER", "9001");
		else//Released build
			VERSION = event.getModMetadata().version;
		Config.init(event.getSuggestedConfigurationFile());

		Reference.MainNBT = new HandlerNBTTag(MODID);

		this.mainContainers = new HashMap<>();

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new HandlerGUI());

		this.ModTasks = new TimerMain();

		network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

		network.registerMessage(PacketCaterpillarControls.Handler.class, PacketCaterpillarControls.class, 0, Side.SERVER);
		network.registerMessage(PacketCaterpillarControls.Handler.class, PacketCaterpillarControls.class, 0, Side.CLIENT);

		network.registerMessage(PacketParticles.Handler.class, PacketParticles.class, 1, Side.CLIENT);
	}


	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		this.register();

		this.recipes();

		Reference.cleanModsFolder();

		Config.load();

		Reference.ModTick.scheduleAtFixedRate(this.ModTasks, 10, 10);
	}

	private void recipes() {
		this.addRecipe(InitBlocks.drillbase, "C C", "CRC", "CPC", 'C', "cobblestone", 'R', "dustRedstone", 'P', "plankWood");
		this.addRecipe(InitBlocks.drillheads, "PPP", " D ", " F ", 'D', InitBlocks.drillbase,  'P', "plankWood", 'F', Blocks.FURNACE);
		this.addRecipe(InitBlocks.reinforcements, " P ", "PDP", " P ", 'D', InitBlocks.drillbase, 'I', "ingotIron", 'P', Blocks.PISTON);
		this.addRecipe(InitBlocks.decoration, "PDP", 'D', InitBlocks.drillbase, 'P', Blocks.DISPENSER);
		this.addRecipe(InitBlocks.collector, "D", "H", 'D', InitBlocks.drillbase, 'I', "ingotIron", 'H', Blocks.HOPPER);
		this.addRecipe(InitBlocks.storage, "PDP", 'D', InitBlocks.drillbase, 'I', "ingotIron", 'P', Blocks.CHEST);
		this.addRecipe(InitBlocks.incinerator, "F", "D", "P", 'D', InitBlocks.drillbase, 'F', Blocks.FURNACE, 'P', LAVA_BUCKET);
	}

	private void addRecipe(Block block, Object... args){
		GameRegistry.addRecipe(new ShapedOreRecipe(block, args));
	}

	public String getCaterpillarID(int[] movingXZ, BlockPos Wherepos)
	{
		int firstID = movingXZ[1] * Wherepos.getX() + movingXZ[0] * Wherepos.getZ();
		int secondID = Wherepos.getY();
		int third = 0;
		if (movingXZ[0] != 0)
		{
			third = movingXZ[0] + 2;
		}
		if (movingXZ[1] != 0)
		{
			third = movingXZ[1] + 3;
		}
		//Reference.printDebug("Cat ID: " + firstID + "," + secondID + "," + third);
		return firstID + "," + secondID + "," + third;
	}

	public int[] getWayMoving(IBlockState state) {
		try {

			int[] movingXZ = {0, 0};
			if (state.getValue(BlockDrillHeads.FACING) == EnumFacing.EAST)
			{
				movingXZ[0] = -1;	//1
			}
			if (state.getValue(BlockDrillHeads.FACING) == EnumFacing.WEST)
			{
				movingXZ[0] = 1;	//3
			}
			if (state.getValue(BlockDrillHeads.FACING) == EnumFacing.NORTH)
			{
				movingXZ[1] = 1;	//4
			}
			if (state.getValue(BlockDrillHeads.FACING) == EnumFacing.SOUTH)
			{
				movingXZ[1] = -1;	//2
			}

			return movingXZ;
		} catch (Exception e) {
			return new int[]{-2, -2};
		}
	}

	public ContainerCaterpillar getSelectedCaterpillar()
	{
		return this.selectedCaterpillar;
	}
	public void setSelectedCaterpillar(ContainerCaterpillar selectedcat)
	{
		this.selectedCaterpillar = selectedcat;
	}
	public void removeSelectedCaterpillar()
	{
		this.selectedCaterpillar = null;
	}
	public void removeCaterpillar(String CaterpillarID)
	{
		Caterpillar.instance.mainContainers.remove(CaterpillarID);
		this.removeSelectedCaterpillar();
	}

	public boolean doesHaveCaterpillar(String CaterpillarID)
	{
		try {
			return this.mainContainers.containsKey(CaterpillarID);
		} catch (Exception e) {
			return false;
		}
	}

	public boolean doesHaveCaterpillar(BlockPos pos, IBlockState thisState)
	{
		int[] movingXZ = this.getWayMoving(thisState);
		if (movingXZ[0] == -2 || movingXZ[1] == -2)
		{
			Reference.printDebug("Null: facing");
			return false;
		}
		String CatID = this.getCaterpillarID(movingXZ, pos);
		return this.doesHaveCaterpillar(CatID);
	}

	public void putContainerCaterpillar(ContainerCaterpillar conCat, World objworld) {
		IBlockState thisState =  objworld.getBlockState(conCat.pos);
		int[] movingXZ = this.getWayMoving(thisState);
		if (movingXZ[0] == -2 || movingXZ[1] == -2)
		{
			Reference.printDebug("Null: facing");
		}
		String CatID = this.getCaterpillarID(movingXZ, conCat.pos);
		this.putContainerCaterpillar(CatID, conCat);
	}

	public void putContainerCaterpillar(String CaterpillarID, ContainerCaterpillar conCat) {
		/*if (this.mainContainers.containsKey(conCat))
		{
			this.mainContainers.remove(conCat);
		}*/
		this.mainContainers.put(CaterpillarID, conCat);
		//this.mainContainersRemote.put(CaterpillarID, conCat.clone());
	}

	public ContainerCaterpillar getContainerCaterpillar(String caterpillarID) {
		return this.mainContainers.get(caterpillarID);
	}

	public ContainerCaterpillar getContainerCaterpillar(BlockPos pos, World objWorld)
	{
		IBlockState thisState =  objWorld.getBlockState(pos);
		int[] movingXZ = this.getWayMoving(thisState);
		if (movingXZ[0] == -2 || movingXZ[1] == -2)
		{
			Reference.printDebug("Null: facing");
			return null;
		}
		String catID = this.getCaterpillarID(movingXZ, pos);
		return this.getContainerCaterpillar(catID);
	}

	public ContainerCaterpillar getContainerCaterpillar(BlockPos pos, IBlockState thisState)
	{
		int[] movingXZ = this.getWayMoving(thisState);
		if (movingXZ[0] == -2 || movingXZ[1] == -2)
		{
			Reference.printDebug("Null: facing");
			return null;
		}
		String catID =this.getCaterpillarID(movingXZ, pos);
		return this.getContainerCaterpillar(catID);
	}

	public void saveNBTDrills()
	{
		if (Reference.Loaded)
		{
			NBTTagCompound tmpNBT = new NBTTagCompound();
			int i = 0;
			for (Entry<String, ContainerCaterpillar> key : this.mainContainers.entrySet()) {
				ContainerCaterpillar conCat = key.getValue();
				tmpNBT.setTag("caterpillar" + i, conCat.writeNBTCaterpillar());
				i++;
			}
			tmpNBT.setInteger("count", i);
			Reference.MainNBT.saveNBTSettings(tmpNBT, Reference.MainNBT.getFolderLocationWorld(), "DrillHeads.dat");
		}
	}
	private World getCaterpillarWorld(BlockPos pos){
		if (FMLCommonHandler.instance().getMinecraftServerInstance().worldServers != null)
		{
			if (FMLCommonHandler.instance().getMinecraftServerInstance().worldServers.length >0)
			{
				for (WorldServer worldServer : FMLCommonHandler.instance().getMinecraftServerInstance().worldServers) {
					IBlockState state  = worldServer.getBlockState(pos);
					if (state.getBlock() instanceof BlockDrillBase)
					{
						return worldServer;
					}
				}

			}
		}
		return null;
	}

	public void clearOldBarrierBlocks()
	{
		for (Entry<String, ContainerCaterpillar> caterpillar : this.mainContainers.entrySet()) {
			BlockPos headisat = caterpillar.getValue().pos;
			World objWorld = this.getCaterpillarWorld(headisat);
			for (int x = -1; x < 2; x++) {
				for (int y = -1; y < 2; y++) {
					for (int z = -1; z < 2; z++) {
						if (objWorld.getBlockState(headisat.add(x, y, z)).getBlock().equals(Blocks.BARRIER))
						{
							objWorld.setBlockState(headisat.add(x, y, z), InitBlocks.drill_blades.getDefaultState());
						}
					}
				}
			}
		}
	}
	public void readNBTDrills()
	{
		NBTTagCompound tmpNBT =  Reference.MainNBT.readNBTSettings(Reference.MainNBT.getFolderLocationWorld(), "DrillHeads.dat");
		this.mainContainers.clear();

		if (tmpNBT.hasKey("count"))
		{
			int size = tmpNBT.getInteger("count");
			for(int i=0;i<size;i++)
			{
				ContainerCaterpillar conCata = ContainerCaterpillar.readCatapiller(tmpNBT.getCompoundTag("catapillar" + i));
				conCata.tabs.selected = GuiTabs.MAIN;
				World objWorld = this.getCaterpillarWorld(conCata.pos);
				if (objWorld != null)
				{
					IBlockState state = objWorld.getBlockState(conCata.pos);
					if (state.getBlock() instanceof BlockDrillBase)
					{
						int[] movingXZ = this.getWayMoving(state);
						if (movingXZ[0] != -2 && movingXZ[1] != -2)
						{
							this.mainContainers.put(conCata.name, conCata);
						}
					}
				}
				else
				{
					Reference.printDebug("load error NBT Drills");
				}
			}
		}
	}

	public void reset() {
		Reference.printDebug("Resetting....");
		Reference.Loaded = false;
		this.ModTasks.inSetup = false;
		this.mainContainers.clear();
	}
	private void register() {
		InitBlocks.init();

		InitBlocks.register();

		GameRegistry.registerTileEntity(TileEntityDrillHead.class, "DrillHead");

		proxy.registerRenders();

		MinecraftForge.EVENT_BUS.register(new HandlerEvents());
	}

	public ItemStack[] getInventory(ContainerCaterpillar MyCaterpillar, GuiTabs selected)
	{
		if (MyCaterpillar != null)
		{
			switch (selected.value) {
			case 0:
				//Reference.printDebug("Getting: Main, 0");
				return MyCaterpillar.inventory;
			case 1:
				//Reference.printDebug("Getting: Decoration, 1");
				return MyCaterpillar.decoration.getSelectedInventory();
			case 2:
				//Reference.printDebug("Getting: Reinforcement, 2");
				return MyCaterpillar.reinforcement.reinforcementMap;
			case 3:
				//Reference.printDebug("Getting: Reinforcement, 2");
				return MyCaterpillar.incinerator.placementMap;
			default:
				break;
			}
		}
		return new ItemStack[256];

	}
	public enum Replacement {
		AIR(0, I18n.translateToLocal("replacement1")),
		WATER(1, I18n.translateToLocal("replacement2")),
		LAVE(2, I18n.translateToLocal("replacement3")),
		SANDGRAVEL(3, I18n.translateToLocal("replacement4")),
		ALL(4, I18n.translateToLocal("replacement5"));
		public final int value;
		public final String name;

		Replacement(int value, String name) {
			this.value = value;
			this.name = name;
		}
	}
	public enum GuiTabs {
		MAIN(0, I18n.translateToLocal("tabs1"), false, new ResourceLocation(MODID  + ":textures/gui/guicatapiller.png")),
		DECORATION(1, I18n.translateToLocal("tabs2"), true, new ResourceLocation(MODID  + ":textures/gui/guidecoration.png")),
		REINFORCEMENT(2, I18n.translateToLocal("tabs3"), true, new ResourceLocation(MODID  + ":textures/gui/guireinfocement.png")),
		INCINERATOR(3, I18n.translateToLocal("tabs4"), true, new ResourceLocation(MODID  + ":textures/gui/guiincinerator.png"));
		public final int value;
		public final String name;
		public final boolean isCrafting;
		public final ResourceLocation guiTextures;

		GuiTabs(int value, String name, boolean isCrafting, ResourceLocation guiTextures) {
			this.value = value;
			this.name = name;
			this.guiTextures = guiTextures;
			this.isCrafting = isCrafting;
		}
	}
}