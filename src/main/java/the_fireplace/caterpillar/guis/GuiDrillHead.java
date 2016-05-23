package the_fireplace.caterpillar.guis;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import the_fireplace.caterpillar.Caterpillar;
import the_fireplace.caterpillar.Caterpillar.GuiTabs;
import the_fireplace.caterpillar.Caterpillar.Replacement;
import the_fireplace.caterpillar.Config;
import the_fireplace.caterpillar.Reference;
import the_fireplace.caterpillar.abstracts.AbstractRunnerWidgets;
import the_fireplace.caterpillar.containers.ContainerCaterpillar;
import the_fireplace.caterpillar.containers.ContainerDrillHead;
import the_fireplace.caterpillar.packets.PacketCaterpillarControls;
import the_fireplace.caterpillar.parts.PartsGuiWidgets;
import the_fireplace.caterpillar.parts.PartsTexture;
import the_fireplace.caterpillar.parts.PartsTutorial;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiDrillHead extends GuiContainer
{
	/** The player inventory bound to this GUI. */
	private final InventoryPlayer playerInventory;
	/** The inventory contained within the corresponding Dispenser. */
	public IInventory dispenserInventory;
	private ContainerCaterpillar catapillar;
	private HashMap<GuiTabs, List<PartsGuiWidgets>> widgetsHolder;
	private List<PartsGuiWidgets> selectedWidgets;
	private boolean[] tutclone;
	private PartsTutorial howTut;
	public GuiDrillHead(EntityPlayer player, IInventory dispenserInv, ContainerCaterpillar DH)
	{
		super(new ContainerDrillHead(player, dispenserInv, DH));
		this.playerInventory = player.inventory;
		this.dispenserInventory = dispenserInv;
		this.widgetsHolder = new HashMap<>();
		this.setupWidgets();
		this.catapillar = DH;
		this.catapillar.tabs.selected = GuiTabs.MAIN;
		this.tutclone = Config.tutorial.clone();
		this.sendUpdates();
	}
	public void setupWidgets()
	{
		AbstractRunnerWidgets tmpHoover = null;
		AbstractRunnerWidgets tmpRun = null;
		AbstractRunnerWidgets clickedtmp = null;
		AbstractRunnerWidgets tmpCheck = null;

		for (GuiTabs tab: GuiTabs.values()) {
			List<PartsGuiWidgets> toAddWid = new ArrayList<>();
			PartsGuiWidgets tmpAdding = null;
			PartsTexture guiTextureA = null;
			PartsTexture guiTextureB = null;
			switch (tab) {
			case MAIN://drill head
				tmpAdding = new PartsGuiWidgets("power",this,81 - 18, 45 + 17, 16, 16);
				guiTextureA = new PartsTexture("poweron", GuiTabs.MAIN.guiTextures, 176 + 14 + 16, 0 , 16, 16);
				guiTextureB = new PartsTexture("poweroff", GuiTabs.MAIN.guiTextures, 176 + 14, 0, 16, 16);
				tmpAdding.setTexture(guiTextureA, guiTextureB);
				tmpAdding.hooverrun = new AbstractRunnerWidgets() {

					@Override
					public void run(PartsGuiWidgets widget) {
						List tmoString = new ArrayList<String>();
						if (widget.Name.equals("power"))
						{
							String powertxt = TextFormatting.GREEN + I18n.translateToLocal("on");
							if (!GuiDrillHead.this.catapillar.running)
							{
								powertxt = TextFormatting.RED + I18n.translateToLocal("off");
							}
							tmoString.add(I18n.translateToLocal("power") + powertxt);
						}
						GuiDrillHead.this.drawHoveringText(tmoString,widget.getMouseX() - widget.getGuiX(), widget.getMouseY() - widget.getGuiY());
					}
				};
				tmpAdding.clicked = new AbstractRunnerWidgets() {

					@Override
					public void run(PartsGuiWidgets widget) {
						if (widget.Name.equals("power"))
						{
							GuiDrillHead.this.catapillar.running = !GuiDrillHead.this.catapillar.running;
						}


					}
				};
				tmpAdding.beforeDraw = new AbstractRunnerWidgets() {

					@Override
					public void run(PartsGuiWidgets widget) {
						if (widget.Name.equals("power"))
						{
							if (GuiDrillHead.this.catapillar.running)
							{
								widget.drawA = true;
								widget.drawB = false;
							}
							else
							{
								widget.drawA = false;
								widget.drawB = true;
							}
						}

					}
				};
				toAddWid.add(tmpAdding);

				tmpAdding = new PartsGuiWidgets("burner",this,81, 45,12, 14);
				guiTextureA = new PartsTexture("burner", GuiTabs.MAIN.guiTextures,176, 0, 12, 14);
				tmpAdding.setTexture(guiTextureA, null);
				tmpAdding.hooverrun = new AbstractRunnerWidgets() {

					@Override
					public void run(PartsGuiWidgets widget) {
						List tmoString = new ArrayList<String>();

						if (widget.Name.equals("burner"))
						{
							int i1 = 0;
							int cbt1 = GuiDrillHead.this.catapillar.burntime;
							if (cbt1 < 0)
							{
								cbt1 = 0;
							}
							if (GuiDrillHead.this.catapillar.maxburntime > 0)
							{
								i1 = 100* (cbt1) / GuiDrillHead.this.catapillar.maxburntime;
							}
							tmoString.add(I18n.translateToLocal("furnace") + i1 + "%");
						}
						GuiDrillHead.this.drawHoveringText(tmoString,widget.getMouseX() - widget.getGuiX(), widget.getMouseY() - widget.getGuiY());
					}
				};
				tmpAdding.beforeDraw = new AbstractRunnerWidgets() {

					@Override
					public void run(PartsGuiWidgets widget) {
						if (widget.Name.equals("burner"))
						{
							widget.drawA = true;
							int ct = GuiDrillHead.this.catapillar.burntime;
							if (ct < 0)
							{
								ct = 0;
								widget.drawA = false;
							}
							int maxBurn = GuiDrillHead.this.catapillar.maxburntime;
							double total = (double)ct / (double)maxBurn;
							widget.YPercentShownA = total;

						}

					}
				};
				toAddWid.add(tmpAdding);

				int indexForBar = 2;

				tmpHoover  = new AbstractRunnerWidgets() {

					@Override
					public void run(PartsGuiWidgets widget) {
						List tmoString = new ArrayList<String>();
						if (widget.Name.equals("scrollbar"))
						{

							tmoString.add(TextFormatting.RED + I18n.translateToLocal("wheelstorage"));

						}
						GuiDrillHead.this.drawHoveringText(tmoString,widget.getMouseX() - widget.getGuiX(), widget.getMouseY() - widget.getGuiY());
					}
				};
				tmpRun = new AbstractRunnerWidgets() {

					@Override
					public void run(PartsGuiWidgets widget) {
						if (widget.Name.equals("scrollbar"))
						{
							widget.drawA = GuiDrillHead.this.catapillar.storage.added != 0;
						}
						if (widget.Name.equals("scrollbarmoving"))
						{
							if (GuiDrillHead.this.catapillar.storage.added == 0)
							{
								widget.drawA = false;
							}
							else
							{
								widget.drawA = true;
								int where = (GuiDrillHead.this.catapillar.storage.startingIndex - 2) / 3;
								int Max = (GuiDrillHead.this.catapillar.storage.added) / 6;
								//Max -= 11;
								//-18 - 36 == 54
								widget.Y = (int)(-18 + 54 *((double)where/(double)Max));

							}
						}
					}
				};

				indexForBar = 1;
				tmpAdding = new PartsGuiWidgets("scrollbar",this, 107, 58 - 52, 5, 42);
				guiTextureA = new PartsTexture("base", GuiTabs.MAIN.guiTextures, 176 + (indexForBar *10), 12 + 3, 5, 42);
				tmpAdding.setTexture(guiTextureA, guiTextureB);
				tmpAdding.beforeDraw = tmpRun;
				tmpAdding.hooverrun = tmpHoover;
				toAddWid.add(tmpAdding);

				tmpAdding = new PartsGuiWidgets("scrollbar",this, 107, 58 - 52 + 28, 5, 42);
				guiTextureA = new PartsTexture("base", GuiTabs.MAIN.guiTextures, 176 + (indexForBar *10), 12 + 3, 5, 42);
				tmpAdding.YPercentShownA = 0.9f;
				tmpAdding.setTexture(guiTextureA, guiTextureB);
				tmpAdding.beforeDraw = tmpRun;
				tmpAdding.hooverrun = tmpHoover;
				toAddWid.add(tmpAdding);
				// -18 - 36
				tmpAdding = new PartsGuiWidgets("scrollbarmoving",this, 107, -18, 5, 42);
				guiTextureA = new PartsTexture("base", GuiTabs.MAIN.guiTextures, 176 + (indexForBar *10) + 5, 12 + 3 , 5, 38);
				tmpAdding.YPercentShownA = 0.3f;
				tmpAdding.setTexture(guiTextureA, guiTextureB);
				tmpAdding.beforeDraw = tmpRun;
				toAddWid.add(tmpAdding);


				break;
			case DECORATION://decoration
				tmpHoover = new AbstractRunnerWidgets() {

					@Override
					public void run(PartsGuiWidgets widget) {
						List tmoString = new ArrayList<String>();
						if (widget.Name.equals("scrollbar") || widget.Name.equals("scrollbarmoving"))
						{

							int where = GuiDrillHead.this.catapillar.decoration.countindex;
							//tmoString.add(StatCollector.translateToLocal("selected")  + (GuiDrillHead.this.catapillar.decoration.selected));
							tmoString.add(I18n.translateToLocal("where")  + (where));

						}
						GuiDrillHead.this.drawHoveringText(tmoString,widget.getMouseX() - widget.getGuiX(), widget.getMouseY() - widget.getGuiY());
					}
				};
				tmpRun = new AbstractRunnerWidgets() {

					@Override
					public void run(PartsGuiWidgets widget) {
						if (widget.Name.equals("scrollbar"))
						{
							widget.drawA = true;
						}
						if (widget.Name.equals("scrollbarmoving"))
						{

							widget.drawA = true;
							//int where = GuiDrillHead.this.catapillar.decoration.selected;
							int where = GuiDrillHead.this.catapillar.decoration.countindex;
							int Max = 9;
							//Max -= 11;
							//-18 - 36 == 54
							widget.Y = (int)(-18 + 54 *((double)where/(double)Max));
						}
					}
				};

				indexForBar = 1;
				tmpAdding = new PartsGuiWidgets("scrollbar",this, 120, 58 - 52, 5, 42);
				guiTextureA = new PartsTexture("base", GuiTabs.MAIN.guiTextures, 176 + (indexForBar *10), 12 + 3, 5, 42);
				tmpAdding.setTexture(guiTextureA, guiTextureB);
				tmpAdding.beforeDraw = tmpRun;
				tmpAdding.hooverrun = tmpHoover;
				toAddWid.add(tmpAdding);

				tmpAdding = new PartsGuiWidgets("scrollbar",this, 120, 58 - 52 + 28, 5, 42);
				guiTextureA = new PartsTexture("base", GuiTabs.MAIN.guiTextures, 176 + (indexForBar *10), 12 + 3, 5, 42);
				tmpAdding.YPercentShownA = 0.9f;
				tmpAdding.setTexture(guiTextureA, guiTextureB);
				tmpAdding.beforeDraw = tmpRun;
				tmpAdding.hooverrun = tmpHoover;
				toAddWid.add(tmpAdding);
				// -18 - 36
				tmpAdding = new PartsGuiWidgets("scrollbarmoving",this, 120, -18, 5, 42);
				guiTextureA = new PartsTexture("base", GuiTabs.MAIN.guiTextures, 176 + (indexForBar *10) + 5, 12 + 3 , 5, 38);
				tmpAdding.YPercentShownA = 0.3f;
				tmpAdding.setTexture(guiTextureA, guiTextureB);
				tmpAdding.beforeDraw = tmpRun;
				toAddWid.add(tmpAdding);

				tmpHoover = new AbstractRunnerWidgets() {

					@Override
					public void run(PartsGuiWidgets widget) {
						List tmoString = new ArrayList<String>();
						if (widget.Name.equals("scrollbar2") || widget.Name.equals("scrollbarmoving2"))
						{

							tmoString.add(I18n.translateToLocal("wheelstorage"));
							tmoString.add(I18n.translateToLocal("selected")  + (GuiDrillHead.this.catapillar.decoration.selected));
							//tmoString.add(StatCollector.translateToLocal("selected")  + (GuiDrillHead.this.catapillar.decoration.countindex));

						}
						GuiDrillHead.this.drawHoveringText(tmoString,widget.getMouseX() - widget.getGuiX(), widget.getMouseY() - widget.getGuiY());
					}
				};
				tmpRun = new AbstractRunnerWidgets() {

					@Override
					public void run(PartsGuiWidgets widget) {
						if (widget.Name.equals("scrollbar2"))
						{
							widget.drawA = true;
						}
						if (widget.Name.equals("scrollbarmoving2"))
						{

							widget.drawA = true;
							int where = GuiDrillHead.this.catapillar.decoration.selected;
							//int where = GuiDrillHead.this.catapillar.decoration.countindex + 1;
							int Max = 9;
							//Max -= 11;
							//-18 - 36 == 54
							widget.Y = (int)(-18 + 54 *((double)where/(double)Max));
						}
					}
				};

				indexForBar = 1;
				tmpAdding = new PartsGuiWidgets("scrollbar2",this, 130, 58 - 52, 5, 42);
				guiTextureA = new PartsTexture("base", GuiTabs.MAIN.guiTextures, 176 + (indexForBar *10), 12 + 3, 5, 42);
				tmpAdding.setTexture(guiTextureA, guiTextureB);
				tmpAdding.beforeDraw = tmpRun;
				tmpAdding.hooverrun = tmpHoover;
				toAddWid.add(tmpAdding);

				tmpAdding = new PartsGuiWidgets("scrollbar2",this, 130, 58 - 52 + 28, 5, 42);
				guiTextureA = new PartsTexture("base", GuiTabs.MAIN.guiTextures, 176 + (indexForBar *10), 12 + 3, 5, 42);
				tmpAdding.YPercentShownA = 0.9f;
				tmpAdding.setTexture(guiTextureA, guiTextureB);
				tmpAdding.beforeDraw = tmpRun;
				tmpAdding.hooverrun = tmpHoover;
				toAddWid.add(tmpAdding);
				// -18 - 36
				tmpAdding = new PartsGuiWidgets("scrollbarmoving2",this, 130, -18, 5, 42);
				guiTextureA = new PartsTexture("base", GuiTabs.MAIN.guiTextures, 176 + (indexForBar *10) + 5, 12 + 3 , 5, 38);
				tmpAdding.YPercentShownA = 0.3f;
				tmpAdding.setTexture(guiTextureA, guiTextureB);
				tmpAdding.beforeDraw = tmpRun;
				toAddWid.add(tmpAdding);

				/*

				clickedtmp = new AbstractRunnerWidgets() {

					@Override
					public void run(PartsGuiWidgets widget) {
						if (widget.Name.startsWith("selection"))
						{
							int index = Integer.parseInt(widget.Name.replace("selection", ""));
							index += 1;
							GuiDrillHead.this.catapillar.decoration.selected = index;
							GuiDrillHead.this.catapillar.placeSlotsforDecorations(GuiDrillHead.this.inventorySlots);
							GuiDrillHead.this.dispenserInventory.markDirty();
							GuiDrillHead.this.sendUpdates();
						}

					}
				};*/
				tmpAdding = new PartsGuiWidgets("selected",this,13+12, 44, 18, 18);
				tmpAdding.hooverrun = new AbstractRunnerWidgets() {

					@Override
					public void run(PartsGuiWidgets widget) {
						List tmoString = new ArrayList<String>();
						if (widget.Name.equals("selected"))
						{
							tmoString.add(I18n.translateToLocal("selected")  + (GuiDrillHead.this.catapillar.decoration.selected));
						}
						GuiDrillHead.this.drawHoveringText(tmoString,widget.getMouseX() - widget.getGuiX(), widget.getMouseY() - widget.getGuiY());
					}
				};
				toAddWid.add(tmpAdding);
				/*
				for (int i = 0; i < 9; i++) {
					tmpAdding = new PartsGuiWidgets("selection" + i,this,6 +18*i, 6, 18, 18);
					tmpAdding.clicked = clickedtmp;
					tmpAdding.hooverrun = new AbstractRunnerWidgets() {

						@Override
						public void run(PartsGuiWidgets widget) {
							List tmoString = new ArrayList<String>();
							if (widget.Name.startsWith("selection"))
							{
								int index = Integer.parseInt(widget.Name.replace("selection", ""));
								index += 1;
								tmoString.add(StatCollector.translateToLocal("clicktoselect")  + (index));
							}
							GuiDrillHead.this.drawHoveringText(tmoString,widget.getMouseX() - widget.getGuiX(), widget.getMouseY() - widget.getGuiY());
						}
					};

					toAddWid.add(tmpAdding);
				}
				 */
				this.addDefaultButtons(toAddWid, 5, 5, 40, 5);


				break;
			case REINFORCEMENT://decoration

				tmpAdding = new PartsGuiWidgets("background",this, 0, -23, 176, 29);
				guiTextureA = new PartsTexture("background", GuiTabs.REINFORCEMENT.guiTextures, 0, 166 , 176, 29);
				tmpAdding.setTexture(guiTextureA, null);
				tmpAdding.drawA = true;
				toAddWid.add(tmpAdding);
				tmpCheck = new AbstractRunnerWidgets() {

					@Override
					public void run(PartsGuiWidgets widget) {
						if (widget.Name.startsWith("check"))
						{
							String[] getinfo = widget.Name.split(",");
							int j = Integer.parseInt(getinfo[1]);
							int k = Integer.parseInt(getinfo[2]);
							if (GuiDrillHead.this.catapillar.reinforcement.replacers.get(j)[k] == 0)
							{
								GuiDrillHead.this.catapillar.reinforcement.replacers.get(j)[k] = 1;
							}
							else
							{
								GuiDrillHead.this.catapillar.reinforcement.replacers.get(j)[k] = 0;
							}
						}
					}
				};
				tmpHoover  = new AbstractRunnerWidgets() {

					@Override
					public void run(PartsGuiWidgets widget) {
						List tmoString = new ArrayList<String>();
						if (widget.Name.startsWith("check"))
						{
							String[] getinfo = widget.Name.split(",");
							int j = Integer.parseInt(getinfo[1]);
							int k = Integer.parseInt(getinfo[2]);
							if (GuiDrillHead.this.catapillar.reinforcement.replacers.get(j)[k] == 0)
							{
								tmoString.add(I18n.translateToLocal("wontreplace") + TextFormatting.RED + Replacement.values()[k].name);
							}
							else
							{
								tmoString.add(I18n.translateToLocal("willreplace") + TextFormatting.GREEN  + Replacement.values()[k].name);
							}
						}
						GuiDrillHead.this.drawHoveringText(tmoString,widget.getMouseX() - widget.getGuiX(), widget.getMouseY() - widget.getGuiY());
					}
				};
				tmpRun = new AbstractRunnerWidgets() {

					@Override
					public void run(PartsGuiWidgets widget) {
						if (widget.Name.startsWith("check"))
						{
							String[] getinfo = widget.Name.split(",");
							int j = Integer.parseInt(getinfo[1]);
							int k = Integer.parseInt(getinfo[2]);
							int whatcolor = GuiDrillHead.this.catapillar.reinforcement.replacers.get(j)[k];
							if (whatcolor == 0)
							{
								widget.drawA = true;
								widget.drawB = false;
							}
							else
							{
								widget.drawA = false;
								widget.drawB = true;
							}

						}

					}
				};
				for (int i = 0; i < 5; i++) {//Left
					tmpAdding = new PartsGuiWidgets("check,1," + i,this, 63, 35 - 18 + 4*i, 4, 4);
					guiTextureA = new PartsTexture("check0", GuiTabs.REINFORCEMENT.guiTextures, 176, 0 , 4, 4);
					guiTextureB = new PartsTexture("check1", GuiTabs.REINFORCEMENT.guiTextures, 176 + 4, 0, 4, 4);
					tmpAdding.setTexture(guiTextureA, guiTextureB);
					tmpAdding.beforeDraw = tmpRun;
					tmpAdding.clicked = tmpCheck;
					tmpAdding.hooverrun = tmpHoover;
					toAddWid.add(tmpAdding);
				}

				for (int i = 0; i < 5; i++) {//right
					tmpAdding = new PartsGuiWidgets("check,2," + i,this, 51 + 58, 35 - 18 + 4*i, 4, 4);
					guiTextureA = new PartsTexture("check0", GuiTabs.REINFORCEMENT.guiTextures, 176, 0 , 4, 4);
					guiTextureB = new PartsTexture("check1", GuiTabs.REINFORCEMENT.guiTextures, 176 + 4, 0, 4, 4);
					tmpAdding.setTexture(guiTextureA, guiTextureB);
					tmpAdding.beforeDraw = tmpRun;
					tmpAdding.clicked = tmpCheck;
					tmpAdding.hooverrun = tmpHoover;
					toAddWid.add(tmpAdding);
				}

				for (int i = 0; i < 5; i++) {//top
					tmpAdding = new PartsGuiWidgets("check,0," + i,this,57 + 23 + 4*i, 0, 4, 4);
					guiTextureA = new PartsTexture("check0", GuiTabs.REINFORCEMENT.guiTextures, 176, 0 , 4, 4);
					guiTextureB = new PartsTexture("check1", GuiTabs.REINFORCEMENT.guiTextures, 176 + 4, 0, 4, 4);
					tmpAdding.setTexture(guiTextureA, guiTextureB);
					tmpAdding.beforeDraw = tmpRun;
					tmpAdding.clicked = tmpCheck;
					tmpAdding.hooverrun = tmpHoover;
					toAddWid.add(tmpAdding);
				}

				for (int i = 0; i < 5; i++) {//lower
					tmpAdding = new PartsGuiWidgets("check,3," + i,this,57 + 23 + 4*i, 12 + 34, 4, 4);
					guiTextureA = new PartsTexture("check0", GuiTabs.REINFORCEMENT.guiTextures, 176, 0 , 4, 4);
					guiTextureB = new PartsTexture("check1", GuiTabs.REINFORCEMENT.guiTextures, 176 + 4, 0, 4, 4);
					tmpAdding.setTexture(guiTextureA, guiTextureB);
					tmpAdding.beforeDraw = tmpRun;
					tmpAdding.clicked = tmpCheck;
					tmpAdding.hooverrun = tmpHoover;
					toAddWid.add(tmpAdding);
				}


				this.addDefaultButtons(toAddWid, 140, 0, 140, 22);

				break;
			case INCINERATOR:

				this.addDefaultButtons(toAddWid, 5, 5, 5, 30);

				break;
			default:
				break;
			}
			this.widgetsHolder.put(tab,toAddWid);
		}
		this.selectedWidgets = this.widgetsHolder.get(GuiTabs.MAIN);
	}

	public void addDefaultButtons(List<PartsGuiWidgets> toAddWid, int defaultx, int defaulty, int defaultallx, int defaultally) {
		PartsGuiWidgets tmpAdding;
		PartsTexture guiTextureA;
		PartsTexture guiTextureB;
		tmpAdding = new PartsGuiWidgets("default",this , defaultx, defaulty, 31, 20);
		guiTextureA = new PartsTexture("default1", GuiTabs.MAIN.guiTextures, 176, 98 , 31, 20);
		guiTextureB = new PartsTexture("default2", GuiTabs.MAIN.guiTextures, 176, 98 + 20, 31, 20);
		tmpAdding.setTexture(guiTextureA, guiTextureB);
		tmpAdding.drawA = true;
		tmpAdding.hooverrun = new AbstractRunnerWidgets() {

			@Override
			public void run(PartsGuiWidgets widget) {
				GuiDrillHead.this.buttonpress(widget);

			}
		};
		tmpAdding.hoovernotrun = new AbstractRunnerWidgets() {

			@Override
			public void run(PartsGuiWidgets widget) {
				if (widget.Name.equals("default") || widget.Name.equals("defaultall"))
				{
					widget.drawA = true;
					widget.drawB = false;
				}
			}
		};
		tmpAdding.text = "Set World\n Default";
		toAddWid.add(tmpAdding);

		tmpAdding = new PartsGuiWidgets("defaultall",this , defaultallx, defaultally, 31, 20);
		guiTextureA = new PartsTexture("default1", GuiTabs.MAIN.guiTextures, 176, 98 , 31, 20);
		guiTextureB = new PartsTexture("default2", GuiTabs.MAIN.guiTextures, 176, 98 + 20, 31, 20);
		tmpAdding.setTexture(guiTextureA, guiTextureB);
		tmpAdding.drawA = true;
		tmpAdding.hooverrun = new AbstractRunnerWidgets() {
			@Override
			public void run(PartsGuiWidgets widget) {
				GuiDrillHead.this.buttonpressall(widget);
			}
		};
		tmpAdding.hoovernotrun = new AbstractRunnerWidgets() {
			@Override
			public void run(PartsGuiWidgets widget) {
				if (widget.Name.equals("default") || widget.Name.equals("defaultall"))
				{
					widget.drawA = true;
					widget.drawB = false;
				}
			}
		};
		tmpAdding.text = "Set Global\n Default";
		toAddWid.add(tmpAdding);
	}
	public void buttonpress(PartsGuiWidgets widget)
	{
		if (widget.Name.equals("default"))
		{
			if (Mouse.isButtonDown(0))
			{
				widget.drawA = false;
				widget.drawB = true;
			}
			else
			{
				if (!widget.drawA)
				{
					NBTTagCompound tmpNBT = new NBTTagCompound();
					NBTTagCompound tmpNBTsub;
					// button was clicked
					switch (this.catapillar.tabs.selected) {
					case MAIN://drill head
						break;
					case DECORATION://decoration
						tmpNBTsub = this.catapillar.decoration.saveNBT();
						tmpNBTsub.setInteger("howclose", 0);
						tmpNBT.setTag("decoration", tmpNBTsub);
						Reference.MainNBT.saveNBTSettings(tmpNBT, Reference.MainNBT.getFolderLocationWorld(), "DecorationDefault.dat");
						break;
					case REINFORCEMENT://REINFORCEMENT

						tmpNBTsub = this.catapillar.reinforcement.saveNBT();
						tmpNBTsub.setInteger("howclose", 0);
						tmpNBT.setTag("reinforcement", tmpNBTsub);
						Reference.MainNBT.saveNBTSettings(tmpNBT, Reference.MainNBT.getFolderLocationWorld(), "ReinforcementDefault.dat");
						break;
					case INCINERATOR://INCINERATOR
						tmpNBTsub = this.catapillar.incinerator.saveNBT();
						tmpNBTsub.setInteger("howclose", 0);
						tmpNBT.setTag("incinerator", tmpNBTsub);
						Reference.MainNBT.saveNBTSettings(tmpNBT, Reference.MainNBT.getFolderLocationWorld(), "IncineratorDefault.dat");
						break;
					default:
						break;
					}
				}
				widget.drawA = true;
				widget.drawB = false;
			}

		}
	}
	public void buttonpressall(PartsGuiWidgets widget)
	{
		if (widget.Name.equals("defaultall"))
		{
			if (Mouse.isButtonDown(0))
			{
				widget.drawA = false;
				widget.drawB = true;
			}
			else
			{
				if (!widget.drawA)
				{
					NBTTagCompound tmpNBT = new NBTTagCompound();
					NBTTagCompound tmpNBTsub;
					// button was clicked
					switch (this.catapillar.tabs.selected) {
					case MAIN://drill head
						break;
					case DECORATION://decoration
						tmpNBTsub = this.catapillar.decoration.saveNBT();
						tmpNBTsub.setInteger("howclose", 0);
						tmpNBT.setTag("decoration", tmpNBTsub);

						Reference.MainNBT.saveNBTSettings(tmpNBT, Reference.MainNBT.getFolderLocationMod(), "DecorationDefault.txt");
						break;
					case REINFORCEMENT://REINFORCEMENT
						tmpNBTsub = this.catapillar.reinforcement.saveNBT();
						tmpNBTsub.setInteger("howclose", 0);
						tmpNBT.setTag("reinforcement", tmpNBTsub);
						Reference.MainNBT.saveNBTSettings(tmpNBT, Reference.MainNBT.getFolderLocationMod(), "ReinforcementDefault.txt");
						break;
					case INCINERATOR://REINFORCEMENT
						tmpNBTsub = this.catapillar.incinerator.saveNBT();
						tmpNBTsub.setInteger("howclose", 0);
						tmpNBT.setTag("incinerator", tmpNBTsub);
						Reference.MainNBT.saveNBTSettings(tmpNBT, Reference.MainNBT.getFolderLocationMod(), "IncineratorDefault.txt");
						break;
					default:
						break;
					}
				}
				widget.drawA = true;
				widget.drawB = false;
			}
		}
	}


	/**
	 * Args : renderPartialTicks, mouseX, mouseY
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		switch (this.catapillar.tabs.selected) {
		case MAIN://drill head
			this.drawGuiDrillHeadBackgroundLayer();
			break;
		case DECORATION://decoration
			this.drawGuiDecorationBackgroundLayer();
			break;
		case REINFORCEMENT://REINFORCEMENT
			this.drawGuiReinforcementBackgroundLayer();
			break;
		case INCINERATOR://REINFORCEMENT
			this.drawGuiIncineratorBackgroundLayer();
			break;
		default:
			this.drawGuiDrillHeadBackgroundLayer();
			break;
		}

		for (PartsGuiWidgets widget : this.selectedWidgets) {
			widget.drawGuiWidgets();
		}

		for (GuiTabs p : GuiTabs.values())
		{
			boolean drawtab = this.checkShowtabs(p);
			if (drawtab)
			{
				this.drawTabsBackground(p);
			}
		}
	}
	private boolean checkShowtabs(GuiTabs p) {
		boolean drawtab = true;
		if (p.equals(Caterpillar.GuiTabs.DECORATION))
		{
			if (this.catapillar.decoration.howclose < 1)
			{
				drawtab = false;
			}
		}
		if (p.equals(Caterpillar.GuiTabs.REINFORCEMENT))
		{
			if (this.catapillar.reinforcement.howclose < 1)
			{
				drawtab = false;
			}
		}
		if (p.equals(Caterpillar.GuiTabs.INCINERATOR))
		{
			if (this.catapillar.incinerator.howclose < 1)
			{
				drawtab = false;
			}
		}
		return drawtab;
	}
	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items). Args : mouseX, mouseY
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		for (GuiTabs p : GuiTabs.values())
		{
			boolean drawtab = this.checkShowtabs(p);
			if (drawtab)
			{
				this.drawTabsForeground(p);
			}
		}

		switch (this.catapillar.tabs.selected) {
		case MAIN://drill head
			this.drawGuiDrillHeadForegroundLayer();
			break;
		case DECORATION://decoration
			this.drawGuiDecorationForegroundLayer();
			break;
		case REINFORCEMENT://reinforcement
			this.drawGuiReinforcementForegroundLayer();
			break;
		case INCINERATOR://incinerator
			//this.drawGuiReinforcementForegroundLayer();
			break;
		default:
			this.drawGuiDrillHeadForegroundLayer();
			break;
		}

		for (PartsGuiWidgets widget : this.selectedWidgets) {
			widget.drawforgroundlayer();
			widget.hooverdGuiWidgets();
		}

		for (GuiTabs p : GuiTabs.values())
		{
			boolean drawtab = this.checkShowtabs(p);
			if (drawtab)
			{
				this.drawTabsHover(p);
			}
		}

		this.drawTutHover();
	}
	private void drawTutHover() {
		if (this.catapillar.tabs.selected == GuiTabs.MAIN)
		{
			if (Config.tutorial[0])
			{
				this.howTut = new PartsTutorial("placeheads", 0, this, -74, -111, false);
			}
			else if (Config.tutorial[1])
			{
				this.howTut = new PartsTutorial("placefuel", 1, this, -74, 11, true);
			}else if (Config.tutorial[2])
			{
				this.howTut = new PartsTutorial("poweron", 2, this, -91, 11, true);
			}else if (this.catapillar.storage.added > 0 && Config.tutorial[3])
			{
				this.howTut = new PartsTutorial("wheelstorage", 3, this, -52, -111, false);
			}
			else
			{
				this.howTut = null;
			}
		}
		if (this.catapillar.tabs.selected == GuiTabs.DECORATION)
		{
			if (Config.tutorial[4])
			{
				this.howTut = new PartsTutorial("selection", 5, this, -35, 15, true);
			}
			else if (Config.tutorial[5])
			{
				this.howTut = new PartsTutorial("selectionzero", 6, this, -175, -24, true);
			}
			else if (Config.tutorial[6])
			{
				this.howTut = new PartsTutorial("selectionpatter", 7, this, -75, 14, true);
			}
			else
			{
				this.howTut = null;
			}
		}
		if (this.catapillar.tabs.selected == GuiTabs.REINFORCEMENT)
		{
			if (Config.tutorial[7])
			{
				this.howTut = new PartsTutorial("options", 8, this, -95, -28, true);
			}
			else if (Config.tutorial[8])
			{
				this.howTut = new PartsTutorial("options2", 9, this, -75, 5, true);
			}
			else
			{
				this.howTut = null;
			}
		}


		if (this.howTut != null)
		{
			this.howTut.draw();
		}

	}
	@Override
	public void drawHoveringText(List textLines, int x, int y)
	{
		super.drawHoveringText(textLines, x, y);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{

		if (this.howTut != null)
		{
			if( this.howTut.checkClicked())
			{
				return;
			}
		}

		super.mouseClicked(mouseX, mouseY, mouseButton);

		int k = (this.width - this.xSize) / 2;
		int l = (this.height - this.ySize) / 2;

		for (PartsGuiWidgets widget : this.selectedWidgets) {
			widget.clickedGuiWidgets();
		}

		for (GuiTabs p : GuiTabs.values())
		{
			boolean drawtab = this.checkShowtabs(p);
			if (drawtab)
			{
				this.checkMouseClickTabs(mouseX, mouseY, k, l, p.value);
			}
		}
	}


	private void checkMouseClickTabs(int mouseX, int mouseY, int k, int l, int index) {
		//(k + this.xSize, l + 3 + index*20, 176, 58, 31, 20);
		int XSide = k - 31;
		int XWidth = 31;

		int YSide = l + 3 + index*20;
		int YHeight = 20;

		if (mouseX > XSide && mouseX < XSide + XWidth)
		{
			if (mouseY > YSide && mouseY < YSide + YHeight)
			{
				this.catapillar.tabs.selected = GuiTabs.values()[index];
				this.selectedWidgets = this.widgetsHolder.get(this.catapillar.tabs.selected);

				switch (this.catapillar.tabs.selected) {
				case MAIN://drill head
					this.catapillar.placeSlotsforMain(this.inventorySlots);
					this.sendUpdates();
					break;
				case DECORATION://decoration
					this.catapillar.decoration.selected = 0;
					this.catapillar.placeSlotsforDecorations(this.inventorySlots);
					this.sendUpdates();
					this.dispenserInventory.markDirty();
					break;
				case REINFORCEMENT://decoration
					this.catapillar.placeSlotsforReinforcements(this.inventorySlots);
					this.sendUpdates();
					this.dispenserInventory.markDirty();
					break;
				case INCINERATOR://decoration
					this.catapillar.placeSlotsforIncinerator(this.inventorySlots);
					this.sendUpdates();
					this.dispenserInventory.markDirty();
					break;
				default:
					this.catapillar.placeSlotsforMain(this.inventorySlots);
					this.sendUpdates();
					break;
				}
			}
		}
	}
	private void drawTabsHover(GuiTabs p) {
		//(k + this.xSize, l + 3 + index*20, 176, 58, 31, 20);
		int i = Mouse.getX() * this.width / this.mc.displayWidth;
		int j = this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1;
		int k = (this.width - this.xSize) / 2;
		int l = (this.height - this.ySize) / 2;

		int XSide = k - 31;
		int XWidth = 31;

		int YSide = l + 3 + p.value*20;
		int YHeight = 20;

		if (i > XSide && i < XSide + XWidth)
		{
			if (j > YSide && j < YSide + YHeight)
			{
				List tmoString = new ArrayList<String>();
				tmoString.add(p.name);
				if (p.equals(GuiTabs.DECORATION) && this.catapillar.tabs.selected.equals(p))
				{
					tmoString.add(I18n.translateToLocal("switchtozero"));
				}
				this.drawHoveringText(tmoString, i - k, j - l);
			}
		}
	}
	public FontRenderer getfontRendererObj()
	{
		return this.fontRendererObj;
	}
	private void drawTabsForeground(GuiTabs p) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(p.guiTextures);
		String Caption = p.name;
		if (Caption.length() > 5)
		{
			Caption = Caption.substring(0, 3) + "...";
		}

		if (this.catapillar.tabs.selected.equals(p))
		{
			if (p.equals(GuiTabs.DECORATION))
			{
				this.fontRendererObj.drawString("  0",-31 + 1,  3 + p.value*20 + 5, Color.BLACK.getRGB());
			}
			else
			{
				this.fontRendererObj.drawString(Caption,-31 + 5 ,  3 + p.value*20 + 5, Color.BLACK.getRGB());
			}
		}
		else
		{
			this.fontRendererObj.drawString(Caption,-31 + 3,  3 + p.value*20 + 5, Color.GRAY.getRGB());
		}
	}

	private void drawTabsBackground(GuiTabs p) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(GuiTabs.MAIN.guiTextures);//has to be main, thats where the tab graphics are
		int k = (this.width - this.xSize) / 2;
		int l = (this.height - this.ySize) / 2;


		if (this.catapillar.tabs.selected.equals(p))
		{
			this.drawTexturedModalRect(k - 31 + 3, l + 3 + p.value*20, 176, 58 + 20, 31, 20);
		}
		else
		{
			this.drawTexturedModalRect(k - 31, l + 3 + p.value*20, 176, 58, 31, 20);
		}
	}

	private void drawGuiReinforcementForegroundLayer() {
		/*
		this.fontRendererObj.drawString(StatCollector.translateToLocal("ceiling"),  55 + 18, 3, Color.BLACK.getRGB());
		this.fontRendererObj.drawString(StatCollector.translateToLocal("leftwall"),  13 , 39, Color.BLACK.getRGB());
		this.fontRendererObj.drawString(StatCollector.translateToLocal("rightwall"),  123, 39, Color.BLACK.getRGB());

		this.fontRendererObj.drawString(StatCollector.translateToLocal("floor"),  55 + 20, 74, Color.BLACK.getRGB());
		 */
	}

	private void drawGuiDecorationForegroundLayer() {
		/*
		for (int i = 0; i < 9; i++) {
			int colort = Color.BLACK.getRGB();
			if (!this.catapillar.decoration.isInventoryEmpty(i + 1))
			{
				colort = Color.GREEN.getRGB();
			}
			if (this.catapillar.decoration.selected == i + 1)
			{
				colort = Color.BLUE.getRGB();
			}
			this.fontRendererObj.drawString("" + (i + 1), 13 + 18*i, 10, colort);
		}
		 */
		this.fontRendererObj.drawString(this.catapillar.decoration.selected + "", 13 + 18, 10 + 20*2, Color.BLUE.getRGB());
	}

	private void drawGuiIncineratorBackgroundLayer() {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(GuiTabs.INCINERATOR.guiTextures);
		int k = (this.width - this.xSize) / 2;
		int l = (this.height - this.ySize) / 2;

		this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
	}

	private void drawGuiReinforcementBackgroundLayer() {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(GuiTabs.REINFORCEMENT.guiTextures);
		int k = (this.width - this.xSize) / 2;
		int l = (this.height - this.ySize) / 2;

		this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
	}

	private void drawGuiDecorationBackgroundLayer() {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(GuiTabs.DECORATION.guiTextures);
		int k = (this.width - this.xSize) / 2;
		int l = (this.height - this.ySize) / 2;

		this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
	}

	private void drawGuiDrillHeadForegroundLayer() {
		this.fontRendererObj.drawString(I18n.translateToLocal("consumption"), 2, -8, Color.WHITE.getRGB());
		this.fontRendererObj.drawString(I18n.translateToLocal("gathered"), 120, -8, Color.WHITE.getRGB());
	}

	private void drawGuiDrillHeadBackgroundLayer() {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(GuiTabs.MAIN.guiTextures);
		int k = (this.width - this.xSize) / 2;
		int l = (this.height - this.ySize) / 2;

		this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
	}

	@Override
	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();
		int speed = Mouse.getDWheel();

		if (speed != 0)
		{
			if (this.catapillar.tabs.selected.equals(GuiTabs.MAIN))
			{
				this.mouseWheelMoved(speed);
			}
			if (this.catapillar.tabs.selected.equals(GuiTabs.DECORATION))
			{
				this.mouseWheelMovedDecoration(speed);
			}
		}

	}
	public void mouseWheelMovedDecoration(int Speed)
	{
		if (Speed < 0)
		{
			this.catapillar.decoration.selected++;
			if (this.catapillar.decoration.selected > 9)
			{
				this.catapillar.decoration.selected = 0;
			}

		}
		else
		{
			this.catapillar.decoration.selected--;
			if (this.catapillar.decoration.selected < 0)
			{
				this.catapillar.decoration.selected = 9;
			}
		}
	}
	@Override
	public void onGuiClosed()
	{
		Reference.printDebug("GUI: Closing, " + this.catapillar.name);
		this.catapillar.tabs.selected = GuiTabs.MAIN;
		Caterpillar.network.sendToServer(new PacketCaterpillarControls(this.catapillar));

		Reference.printDebug("Closing: Saving...");
		Caterpillar.instance.saveNBTDrills();

		Caterpillar.instance.removeSelectedCaterpillar();
		/*Config.tutorial = this.tutclone.clone();
		Config.forceSave();*/
	}
	private void sendUpdates() {
		Reference.printDebug("GUI: Updateing Server, " + this.catapillar.name);
		boolean whatamI = this.catapillar.running;
		this.catapillar.running = false;
		Caterpillar.network.sendToServer(new PacketCaterpillarControls(this.catapillar));
		this.catapillar.running = whatamI;
	}

	public void mouseWheelMoved(int Speed)
	{
		if (Speed < 0)
		{
			this.catapillar.storage.startingIndex++;
			this.catapillar.storage.startingIndex++;
			this.catapillar.storage.startingIndex++;

			int Resetindex = ((this.catapillar.storage.added + ContainerCaterpillar.getMaxSize() - 2) / 2) - 10;

			if (Integer.compare(this.catapillar.storage.startingIndex, Resetindex)  >  0)
			{
				this.catapillar.storage.startingIndex = Resetindex;
			}
		}
		else
		{
			this.catapillar.storage.startingIndex--;
			this.catapillar.storage.startingIndex--;
			this.catapillar.storage.startingIndex--;

			if (this.catapillar.storage.startingIndex < 2)
			{
				this.catapillar.storage.startingIndex = 2;
			}
		}

		this.catapillar.updateScroll(this.inventorySlots);
	}
}