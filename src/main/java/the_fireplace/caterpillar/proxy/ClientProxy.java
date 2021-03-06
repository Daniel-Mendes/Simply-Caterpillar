package the_fireplace.caterpillar.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.io.File;

public class ClientProxy extends CommonProxy {

    @Override
    public boolean checkLoaded() {
        return Minecraft.getInstance().currentScreen instanceof MainMenuScreen || this.getWorld() == null;
    }

    @Override
    public File getDataDir() {
        return Minecraft.getInstance().gameDir;
    }

    @Override
    public PlayerEntity getPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public ServerWorld getWorld() {
        return getWorld();
    }

    @Override
    public boolean isServerSide() {
        return false;
    }

    @Override
    public String translateToLocal(String string) {
        return I18n.format(string);
    }
}
