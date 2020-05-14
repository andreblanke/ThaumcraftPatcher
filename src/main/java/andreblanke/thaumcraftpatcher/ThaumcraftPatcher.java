package andreblanke.thaumcraftpatcher;

import java.util.Collections;

import com.google.common.eventbus.EventBus;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;

public final class ThaumcraftPatcher extends DummyModContainer {

    public ThaumcraftPatcher() {
        super(new ModMetadata());

        final ModMetadata metadata = getMetadata();
        metadata.authorList = Collections.singletonList("Andre Blanke");
        metadata.modId      = "ThaumcraftPatcher";
        metadata.name       = "Thaumcraft Patcher";
        metadata.version    = "1.0.0";
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public boolean registerBus(final EventBus bus, final LoadController controller) {
        bus.register(this);
        return true;
    }
}
