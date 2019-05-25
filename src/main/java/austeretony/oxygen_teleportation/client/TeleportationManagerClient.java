package austeretony.oxygen_teleportation.client;

import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen_teleportation.client.gui.menu.TeleportationMenuGUIScreen;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.main.TeleportationPlayerData;
import austeretony.oxygen_teleportation.common.main.TeleportationWorldData;
import austeretony.oxygen_teleportation.common.network.server.SPRequest;

public class TeleportationManagerClient {

    private static TeleportationManagerClient instance;

    //Data      
    private final TeleportationPlayerData playerData;

    private final TeleportationWorldData worldData;

    //Camps
    private final CampsManagerClient campsManager;

    private final SharedCampsManagerClient sharedCampsManager;

    //Locations
    private final LocationsManagerClient locationsManager;

    //Players
    private final PlayersManagerClient playersManager;
    
    //Preview Images
    private final ImagesManagerClient imagesManager;

    private final ImagesLoaderClient imagesLoader;

    private long time, delay;

    private TeleportationManagerClient() {
        this.playerData = new TeleportationPlayerData();
        this.worldData = new TeleportationWorldData();
        this.campsManager = new CampsManagerClient(this);
        this.sharedCampsManager = new SharedCampsManagerClient(this);
        this.locationsManager = new LocationsManagerClient(this);
        this.playersManager = new PlayersManagerClient(this);
        this.imagesManager = new ImagesManagerClient(this);
        this.imagesLoader = new ImagesLoaderClient(this);
    }

    public static void create() {
        if (instance == null) 
            instance = new TeleportationManagerClient();
    }

    public static TeleportationManagerClient instance() {
        return instance;
    }

    public TeleportationPlayerData getPlayerData() {
        return this.playerData;
    }

    public TeleportationWorldData getWorldData() {
        return this.worldData;
    }

    public CampsManagerClient getCampsManager() {
        return this.campsManager;
    }

    public SharedCampsManagerClient getSharedCampsManager() {
        return this.sharedCampsManager;
    }

    public LocationsManagerClient getLocationsManager() {
        return this.locationsManager;
    }

    public PlayersManagerClient getPlayersManager() {
        return this.playersManager;
    }

    public ImagesManagerClient getImagesManager() {
        return this.imagesManager;
    }

    public ImagesLoaderClient getImagesLoader() {
        return this.imagesLoader;
    }

    public void openMenuSynced() {
        if (!this.teleporting()) {
            ClientReference.getMinecraft().gameSettings.hideGUI = true;
            TeleportationMain.network().sendToServer(new SPRequest(SPRequest.EnumRequest.OPEN_MENU));
        }
    }

    public boolean teleporting() {
        return System.currentTimeMillis() < this.time + this.delay;
    }

    public void setTeleportationDelay(long delay) {
        this.delay = delay * 1000;
        this.time = System.currentTimeMillis();
    }

    public void openMenuDelegated() {
        ClientReference.getMinecraft().addScheduledTask(new Runnable() {

            @Override
            public void run() {
                openMenu();
            }
        });
    }

    public void openMenu() {
        this.getImagesManager().preparePreviewImage();
        ClientReference.getMinecraft().gameSettings.hideGUI = false;
        ClientReference.displayGuiScreen(new TeleportationMenuGUIScreen());
    }

    public void reset() {
        this.playerData.reset();
        this.worldData.reset();
        this.sharedCampsManager.reset();
    }
}