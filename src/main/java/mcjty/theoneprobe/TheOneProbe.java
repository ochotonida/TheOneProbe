package mcjty.theoneprobe;

import mcjty.theoneprobe.api.IProbeInfoEntityProvider;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.apiimpl.TheOneProbeImp;
import mcjty.theoneprobe.apiimpl.providers.*;
import mcjty.theoneprobe.config.Config;
import mcjty.theoneprobe.items.AddProbeTagRecipeSerializer;
import mcjty.theoneprobe.items.ModItems;
import mcjty.theoneprobe.network.PacketHandler;
import mcjty.theoneprobe.playerdata.PlayerGotNote;
import mcjty.theoneprobe.proxy.ClientProxy;
import mcjty.theoneprobe.proxy.IProxy;
import mcjty.theoneprobe.proxy.ServerProxy;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod("theoneprobe")
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TheOneProbe {
    public static final String MODID = "theoneprobe";

    public static IProxy proxy = DistExecutor.runForDist(() -> () -> new ClientProxy(), () -> () -> new ServerProxy());
    public static final Logger logger = LogManager.getLogger();

    public static TheOneProbeImp theOneProbeImp = new TheOneProbeImp();

    public static boolean curios = false;
    public static boolean tesla = false;
    public static boolean redstoneflux = false;

    public static ItemGroup tabProbe = new ItemGroup("Probe") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModItems.probe);
        }
    };


    public TheOneProbe() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);


        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Config::onLoad);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Config::onReload);

        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);

        // Register ourselves for server, registry and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void init(final FMLCommonSetupEvent event) {

        tesla = ModList.get().isLoaded("tesla");
        if (tesla) {
            logger.log(Level.INFO, "The One Probe Detected TESLA: enabling support");
        }

        redstoneflux = ModList.get().isLoaded("redstoneflux");
        if (redstoneflux) {
            logger.log(Level.INFO, "The One Probe Detected RedstoneFlux: enabling support");
        }

        curios = ModList.get().isLoaded(CuriosApi.MODID);
        if (curios) {
            logger.log(Level.INFO, "The One Probe Detected Curios: enabling support");
        }

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());

        registerCapabilities();
        TheOneProbeImp.registerElements();
        TheOneProbe.theOneProbeImp.registerProvider(new DefaultProbeInfoProvider());
        TheOneProbe.theOneProbeImp.registerProvider(new DebugProbeInfoProvider());
        TheOneProbe.theOneProbeImp.registerProvider(new BlockProbeInfoProvider());
        TheOneProbe.theOneProbeImp.registerEntityProvider(new DefaultProbeInfoEntityProvider());
        TheOneProbe.theOneProbeImp.registerEntityProvider(new DebugProbeInfoEntityProvider());
        TheOneProbe.theOneProbeImp.registerEntityProvider(new EntityProbeInfoEntityProvider());

        PacketHandler.registerMessages("theoneprobe");

        configureProviders();
        configureEntityProviders();

        proxy.setup(event);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.HEAD.getMessageBuilder().build());
    }

    private void processIMC(final InterModProcessEvent event) {
        event.getIMCStream().forEach(message -> {
            if ("getTheOneProbe".equalsIgnoreCase(message.getMethod())) {
                Supplier<Function<ITheOneProbe, Void>> supplier = message.getMessageSupplier();
                supplier.get().apply(theOneProbeImp);
            }
        });
    }

    @SubscribeEvent
    public static void registerRecipes(final RegistryEvent.Register<IRecipeSerializer<?>> e) {
        e.getRegistry().register(new AddProbeTagRecipeSerializer().setRegistryName(new ResourceLocation(TheOneProbe.MODID, "probe_helmet")));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        ModItems.init();

        event.getRegistry().register(ModItems.probe);
        event.getRegistry().register(ModItems.creativeProbe);
        event.getRegistry().register(ModItems.probeNote);

        event.getRegistry().register(ModItems.diamondHelmetProbe);
        event.getRegistry().register(ModItems.goldHelmetProbe);
        event.getRegistry().register(ModItems.ironHelmetProbe);

        event.getRegistry().register(ModItems.probeGoggles);
    }


    private static void registerCapabilities(){
        CapabilityManager.INSTANCE.register(PlayerGotNote.class, new Capability.IStorage<PlayerGotNote>() {

            @Override
            public void readNBT(Capability<PlayerGotNote> capability, PlayerGotNote playerGotNote, Direction direction, INBT inbt) {
                throw new UnsupportedOperationException();
            }

            @Override
            public INBT writeNBT(Capability<PlayerGotNote> capability, PlayerGotNote instance, Direction side) {
                throw new UnsupportedOperationException();
            }

        }, () -> {
            throw new UnsupportedOperationException();
        });
    }

    private void configureProviders() {
        List<IProbeInfoProvider> providers = TheOneProbe.theOneProbeImp.getProviders();
        String[] defaultValues = new String[providers.size()];
        int i = 0;
        for (IProbeInfoProvider provider : providers) {
            defaultValues[i++] = provider.getID();
        }

        String[] sortedProviders = defaultValues; // @todo TheOneProbe.config.getStringList("sortedProviders", Config.CATEGORY_PROVIDERS, defaultValues, "Order in which providers should be used");
        String[] excludedProviders = new String[] {}; // @todo TheOneProbe.config.getStringList("excludedProviders", Config.CATEGORY_PROVIDERS, new String[] {}, "Providers that should be excluded");
        Set<String> excluded = new HashSet<>();
        Collections.addAll(excluded, excludedProviders);

        TheOneProbe.theOneProbeImp.configureProviders(sortedProviders, excluded);
    }

    private void configureEntityProviders() {
        List<IProbeInfoEntityProvider> providers = TheOneProbe.theOneProbeImp.getEntityProviders();
        String[] defaultValues = new String[providers.size()];
        int i = 0;
        for (IProbeInfoEntityProvider provider : providers) {
            defaultValues[i++] = provider.getID();
        }

        String[] sortedProviders = defaultValues; // @todo TheOneProbe.config.getStringList("sortedEntityProviders", Config.CATEGORY_PROVIDERS, defaultValues, "Order in which entity providers should be used");
        String[] excludedProviders = new String[] {}; // @todo TheOneProbe.config.getStringList("excludedEntityProviders", Config.CATEGORY_PROVIDERS, new String[] {}, "Entity providers that should be excluded");
        Set<String> excluded = new HashSet<>();
        Collections.addAll(excluded, excludedProviders);

        TheOneProbe.theOneProbeImp.configureEntityProviders(sortedProviders, excluded);
    }

}
