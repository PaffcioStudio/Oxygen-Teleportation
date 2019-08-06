package austeretony.oxygen_teleportation.client;

import austeretony.oxygen.client.api.OxygenHelperClient;
import austeretony.oxygen.client.core.api.ClientReference;
import austeretony.oxygen.client.privilege.api.PrivilegeProviderClient;
import austeretony.oxygen.client.sync.gui.api.ComplexGUIHandlerClient;
import austeretony.oxygen_teleportation.common.config.TeleportationConfig;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;
import austeretony.oxygen_teleportation.common.main.TeleportationMain;
import austeretony.oxygen_teleportation.common.main.WorldPoint;
import austeretony.oxygen_teleportation.common.network.server.SPCreateWorldPoint;
import austeretony.oxygen_teleportation.common.network.server.SPEditWorldPoint;
import austeretony.oxygen_teleportation.common.network.server.SPLockPoint;
import austeretony.oxygen_teleportation.common.network.server.SPMoveToPoint;
import austeretony.oxygen_teleportation.common.network.server.SPRemoveWorldPoint;
import net.minecraft.entity.player.EntityPlayer;

public class LocationsManagerClient {

    private final TeleportationManagerClient manager;

    public LocationsManagerClient(TeleportationManagerClient manager) { 
        this.manager = manager;
    }

    public void downloadLocationsDataSynced() {
        ClientReference.getGameSettings().hideGUI = true;
        this.manager.getWorldData().reset();
        ComplexGUIHandlerClient.openScreen(TeleportationMain.TELEPORTATION_MENU_SCREEN_ID);
    }   

    public void moveToLocationSynced(long pointId) {        
        if (pointId != 0L) {
            TeleportationMain.network().sendToServer(new SPMoveToPoint(WorldPoint.EnumWorldPoint.LOCATION, pointId));
            this.manager.setTeleportationDelay(PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivilege.LOCATION_TELEPORTATION_DELAY.toString(), TeleportationConfig.LOCATIONS_TELEPORT_DELAY.getIntValue()));
        }
    }

    public void createLocationPointSynced(String name, String description) {
        if (this.canCreateLocation() && this.manager.getWorldData().getLocationsAmount() < TeleportationConfig.LOCATIONS_MAX_AMOUNT.getIntValue()) {
            WorldPoint worldPoint = new WorldPoint(
                    System.currentTimeMillis(),
                    OxygenHelperClient.getPlayerUUID(),
                    ClientReference.getClientPlayer().getName(), 
                    name, 
                    description,
                    ClientReference.getClientPlayer().dimension,
                    (float) ClientReference.getClientPlayer().posX, 
                    (float) ClientReference.getClientPlayer().posY, 
                    (float) ClientReference.getClientPlayer().posZ,
                    ClientReference.getClientPlayer().rotationYawHead, 
                    ClientReference.getClientPlayer().rotationPitch);
            worldPoint.createId();
            this.manager.getWorldData().addLocation(worldPoint);
            TeleportationMain.network().sendToServer(new SPCreateWorldPoint(WorldPoint.EnumWorldPoint.LOCATION, worldPoint));
            this.manager.getImagesManager().cacheLatestImage(worldPoint.getId());
            OxygenHelperClient.savePersistentDataDelegated(this.manager.getWorldData());
            this.manager.getImagesLoader().saveLatestLocationPreviewImageDelegated(worldPoint.getId());
            this.manager.getImagesManager().uploadLocationPreviewToServerDelegated(worldPoint.getId());
        }
    }  

    public void removeLocationPointSynced(long pointId) {
        this.manager.getWorldData().removeLocation(pointId);
        TeleportationMain.network().sendToServer(new SPRemoveWorldPoint(WorldPoint.EnumWorldPoint.LOCATION, pointId));
        OxygenHelperClient.savePersistentDataDelegated(this.manager.getWorldData());
        this.manager.getImagesManager().removeCachedImage(pointId);
    }

    public void lockLocationSynced(WorldPoint worldPoint, boolean flag) {    
        long oldPointId = worldPoint.getId();
        worldPoint.setLocked(flag);
        worldPoint.setId(worldPoint.getId() + 1L);
        this.manager.getWorldData().addLocation(worldPoint);
        this.manager.getWorldData().removeLocation(oldPointId);
        OxygenHelperClient.savePersistentDataDelegated(this.manager.getWorldData());
        TeleportationMain.network().sendToServer(new SPLockPoint(WorldPoint.EnumWorldPoint.LOCATION, oldPointId, flag));
        this.manager.getImagesLoader().renameLocationPreviewImageDelegated(oldPointId, worldPoint.getId());
        this.manager.getImagesManager().replaceCachedImage(oldPointId, worldPoint.getId());
    }

    public void editLocationPointSynced(WorldPoint worldPoint, String newName, String newDescription, boolean updateImage, boolean updatePosition) {
        long 
        oldPointId = worldPoint.getId(),
        newPointId =  oldPointId + 1L;
        boolean 
        edited = false,
        updateName = false,
        updateDescription = false;
        if (!newName.equals(worldPoint.getName())) {
            updateName = true;
            worldPoint.setName(newName);
        }
        if (!newDescription.equals(worldPoint.getDescription())) {
            updateDescription = true;
            worldPoint.setDescription(newDescription);
        }
        if (updateImage) {
            this.manager.getImagesManager().cacheLatestImage(newPointId);
            this.manager.getImagesManager().removeCachedImage(oldPointId);
            this.manager.getImagesLoader().saveLatestLocationPreviewImageDelegated(newPointId);
            this.manager.getImagesManager().uploadLocationPreviewToServerDelegated(newPointId);
        }
        if (updatePosition) {
            EntityPlayer player = ClientReference.getClientPlayer();
            worldPoint.setPosition(player.rotationYaw, player.rotationPitch, (float) player.posX, (float) player.posY, (float) player.posZ, player.dimension);

        }
        edited = updateName || updateDescription || updateImage || updatePosition;
        if (edited) {
            worldPoint.setId(newPointId);
            this.manager.getWorldData().addLocation(worldPoint);
            this.manager.getWorldData().removeLocation(oldPointId);
            OxygenHelperClient.savePersistentDataDelegated(this.manager.getWorldData());
            TeleportationMain.network().sendToServer(new SPEditWorldPoint(WorldPoint.EnumWorldPoint.LOCATION, oldPointId, newName, newDescription, 
                    updateName, updateDescription, updateImage, updatePosition));
            if (!updateImage) {
                this.manager.getImagesLoader().renameLocationPreviewImageDelegated(oldPointId, newPointId);
                this.manager.getImagesManager().replaceCachedImage(oldPointId, newPointId);
            }
        }
    }

    private boolean canCreateLocation() {
        return TeleportationConfig.ALLOW_LOCATIONS_CREATION_FOR_ALL.getBooleanValue() 
                || PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivilege.LOCATIONS_CREATION.toString(), false) 
                || PrivilegeProviderClient.getPrivilegeValue(EnumTeleportationPrivilege.LOCATIONS_MANAGEMENT.toString(), false);
    }
}
