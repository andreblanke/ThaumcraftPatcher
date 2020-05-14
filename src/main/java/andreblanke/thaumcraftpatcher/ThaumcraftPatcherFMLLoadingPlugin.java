package andreblanke.thaumcraftpatcher;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@SuppressWarnings("unused")
@IFMLLoadingPlugin.Name("Thaumcraft Patcher")
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions("andreblanke.thaumcraftpatcher")
public final class ThaumcraftPatcherFMLLoadingPlugin implements IFMLLoadingPlugin {

    private static boolean obfuscated;

    // <editor-fold desc="IFMLLoadingPlugin">
    @Override
    public String[] getASMTransformerClass() {
        return new String[] { WarpEventsClassTransformer.class.getName() };
    }

    @Override
    public String getModContainerClass() {
        return ThaumcraftPatcher.class.getName();
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(final Map<String, Object> data) {
        obfuscated = (Boolean) data.get("runtimeDeobfuscationEnabled");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
    // </editor-fold>

    static boolean isObfuscated() {
        return obfuscated;
    }
}
