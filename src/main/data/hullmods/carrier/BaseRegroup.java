package data.hullmods.carrier;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.combatlog.Util;
import data.hullmods.AutomatedHullMod;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;

import static com.fs.starfarer.api.Global.getCombatEngine;

public class BaseRegroup extends AutomatedHullMod {
    protected static final String REGROUP = Global.getSettings().getString(Util.MOD_KEY, "REGROUP");
    protected static final String ENGAGE = Global.getSettings().getString(Util.MOD_KEY, "ENGAGE");
    protected static final MessageFormat REGROUP_INAPPLICABLE = Util.resolveSubstitutions(Util.MOD_KEY + ":REGROUP_INAPPLICABLE");

    //private static final String NO_WINGS = "NO_WINGS";
    protected static final float DELAY = 3.0f; //3 seconds

    private static final float THRESHOLD = 0.8f;
    private static final String THRESHOLD_TEXT = Util.percentToString(THRESHOLD);

    private final float limit;
    private final String limitText;

    public BaseRegroup() {
        limit = 0.2f;
        limitText = Util.percentToString(limit);
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship.hasLaunchBays();
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return REGROUP_INAPPLICABLE.format(new Object[]{ship});
    }

    private String generateTag(ShipAPI ship) {
        return ship.getId() + "_regroup" + limitText;
    }

    private boolean hasExceededDelay(Object mapValue, float goal) {
        if (mapValue == null) return true; //Has never been run before
        if (!(mapValue instanceof Float)) return true; //Somehow shipTag has been used by another mod
        //TODO log overlapping values
        //returning true to replace value with a float
        return Float.compare((Float)mapValue,goal) > 0;
    }

    //TODO look into using an 'EveryFrameCombatPlugin` and a 'DeplayedFleetListener' to reduce needless processing of time delays
    //Need to find a combatStarts() type method to achieve the above
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (Global.getCurrentState() != GameState.COMBAT) return;
        final float timeElapsed = getCombatEngine().getTotalElapsedTime(false);
        final String shipTag = generateTag(ship);

        Map<String, Object> data = Global.getCombatEngine().getCustomData();
        if (!hasExceededDelay(data.get(shipTag),timeElapsed + DELAY))
            return;

        //new IntervalUtil()
        float rate = (float)calculateReplacementRate(ship);
        if (rate > 1.0f) return; //there are no fighter wings installed in the carrier

        if (rate > THRESHOLD) {
            ship.setPullBackFighters(false);
        } else if (rate < limit) {
            ship.setPullBackFighters(true);
        }
        data.put(shipTag, timeElapsed);
    }
    //Left in place till new replacement code is tested, todo remove later
    private float calculateReplacementRateOld(ShipAPI ship) {
        float rate = 0.0f;
        int count = 0;

        for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
            if (bay.getWing() == null) continue;
            rate += bay.getCurrRate();
            count++;
        }

        if (count == 0) {
            return 2.0f; //there are no fighter wings installed in the carrier
        }
        return rate / count;
    }

    private double calculateReplacementRate(ShipAPI ship) {
        return ship.getLaunchBaysCopy().stream()
                .mapToDouble(FighterLaunchBayAPI::getCurrRate)
                .average()
                .orElse(2.0f); //there are no fighter wings installed in the carrier
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return switch (index) {
            case 0 -> REGROUP;
            case 1 -> limitText;
            case 2 -> ENGAGE;
            case 3 -> THRESHOLD_TEXT;
            default -> null;
        };
    }
}
