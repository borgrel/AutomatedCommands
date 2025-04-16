package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.mission.FleetSide;
import org.apache.log4j.Logger;

import java.awt.Color;

public abstract class AutomatedHullMod extends com.fs.starfarer.api.combat.BaseHullMod {
    private static final Logger LOGGER = Global.getLogger(AutomatedHullMod.class);

    private static final Color TEXT_COLOR = Global.getSettings().getColor("standardTextColor");
    private static final Color FRIEND_COLOR = Global.getSettings().getColor("textFriendColor");
    private static final Color MESSAGE_COLOR = Color.CYAN;
    private static final Color ENEMY_COLOR = Global.getSettings().getColor("textEnemyColor");

    protected static String getShipName(ShipAPI ship) {
        // TODO does 'getHullNameWithDashClass()' already discriminate for fighters
        if (ship.isFighter()) {
            return ship.getHullSpec().getHullName() + " wing";
        }
        return ship.getName() + " (" + ship.getHullSpec().getHullNameWithDashClass() + ")";
    }
    protected static void formatShipMessage(ShipAPI ship, String reason) {
        LOGGER.info(ship.getName() + " - " + reason);

        DeployedFleetMemberAPI deployedMemberAPI
                = Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).getDeployedFleetMember(ship);
        if (deployedMemberAPI == null) return;

        String shipName = getShipName(ship);
        Object[] message = new Object[]{
                deployedMemberAPI,
                FRIEND_COLOR, shipName,
                TEXT_COLOR, ": ",
                MESSAGE_COLOR, reason };
        Global.getCombatEngine().getCombatUI().addMessage(1, message);
    }
}