package austeretony.oxygen_teleportation.server;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_teleportation.common.TeleportationPlayerData;

public class PlayersDataContainerServer {

    private final Map<UUID, TeleportationPlayerData> players = new ConcurrentHashMap<>();

    protected PlayersDataContainerServer() {}

    public Collection<TeleportationPlayerData> getPlayersData() {
        return this.players.values();
    }

    public TeleportationPlayerData createPlayerData(UUID playerUUID) {     
        TeleportationPlayerData playerData = new TeleportationPlayerData();
        playerData.setPlayerUUID(playerUUID);
        playerData.setPath(OxygenHelperServer.getDataFolder() + "/server/players/" + playerUUID + "/teleportation/player_data.dat");
        this.players.put(playerUUID, playerData);
        return playerData;
    }

    public void removePlayerData(UUID playerUUID) {
        this.players.remove(playerUUID);
    }

    public boolean isPlayerDataExist(UUID playerUUID) {
        return this.players.containsKey(playerUUID);
    }

    public TeleportationPlayerData getPlayerData(UUID playerUUID) {
        return this.players.get(playerUUID);
    }

    public void saveData() {
        OxygenHelperServer.addRoutineTask(()->{
            for (TeleportationPlayerData playerData : this.players.values()) {
                if (playerData.isChanged()) {
                    playerData.setChanged(false);
                    OxygenHelperServer.savePersistentDataAsync(playerData);
                }
            }   
        });
    }
}
