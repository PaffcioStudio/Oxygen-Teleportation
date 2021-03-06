package austeretony.oxygen_teleportation.client.gui.teleportation.locations.context;

import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.client.gui.elements.OxygenContextMenu.OxygenContextMenuAction;
import austeretony.oxygen_teleportation.client.gui.teleportation.LocationsSection;
import austeretony.oxygen_teleportation.common.main.EnumTeleportationPrivilege;

public class RemoveContextAction implements OxygenContextMenuAction {

    private LocationsSection section;

    public RemoveContextAction(LocationsSection section) {
        this.section = section;
    }   

    @Override
    public String getLocalizedName(GUIBaseElement currElement) {
        return ClientReference.localize("oxygen_teleportation.gui.menu.remove");
    }

    @Override
    public boolean isValid(GUIBaseElement currElement) {
        return PrivilegesProviderClient.getAsBoolean(EnumTeleportationPrivilege.LOCATIONS_MANAGEMENT.id(), false) 
                || this.section.getCurrentPoint().isOwner(OxygenHelperClient.getPlayerUUID());
    }

    @Override
    public void execute(GUIBaseElement currElement) {
        this.section.openRemovePointCallback();
    }
}
