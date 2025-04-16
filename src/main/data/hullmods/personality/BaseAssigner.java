package data.hullmods.personality;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAPI;
import data.hullmods.AutomatedHullMod;
import data.combatlog.Util;

import java.text.MessageFormat;
import java.util.Objects;

public class BaseAssigner extends AutomatedHullMod {
    private static final MessageFormat PERSONALITY_INAPPLICABLE_REASON = Util.resolveSubstitutions(Util.MOD_KEY +":PERSONALITY_INAPPLICABLE_REASON");
    private static final MessageFormat PERSONALITY_DISABLED_REASON = Util.resolveSubstitutions(Util.MOD_KEY +":PERSONALITY_DISABLED_REASON");
    private static final MessageFormat PERSONALITY_APPLIED = Util.resolveSubstitutions(Util.MOD_KEY +":PERSONALITY_APPLIED");
    private static final MessageFormat PERSONALITY_APPLIED_FLAGSHIP = Util.resolveSubstitutions(Util.MOD_KEY +":PERSONALITY_APPLIED_FLAGSHIP");

    private static final String PREFIX = "automated_personality_";
    private static final String CATEGORY = "personality";
    protected static final String TOKEN = "";

    //todo move personality into its own file
    enum Personality {
        TIMID,
        CAUTIOUS,
        STEADY,
        AGGRESSIVE,
        RECKLESS;
        final String id;
        final String value;

        Personality() {
            value = Global.getSettings().getString(Util.MOD_KEY, this.toString());
            id = PREFIX + value;
        }
    }

    private final Personality personality;

    public BaseAssigner(Personality personality) {
        Objects.requireNonNull(personality);
        this.personality = personality;
    }

    private String message(MessageFormat formatter, ShipAPI ship, Personality personality) {
        return formatter.format(new Object[]{ship, personality});
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return message(PERSONALITY_INAPPLICABLE_REASON, ship, personality);
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return !shipHasOtherModInCategory(ship, personality.id, CATEGORY);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0)
            return personality.value;
        return null;
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (Global.getCurrentState() != GameState.COMBAT) return;
        String tag = ship.getId() + "_" + personality.id;
        if (Global.getCombatEngine().getCustomData().containsKey(tag)) return;
        Global.getCombatEngine().getCustomData().put(tag, TOKEN);
        overrideAI(ship);
    }

    protected void overrideAI(ShipAPI ship) {
        PersonAPI captain = ship.getCaptain();
        if (captain.isPlayer()) {
            setAI(ship);
            formatShipMessage(ship, message(PERSONALITY_APPLIED_FLAGSHIP, ship, personality));

        } else if (captain.isDefault()) {
            setAI(ship);
            formatShipMessage(ship, message(PERSONALITY_APPLIED, ship, personality));

        } else {
            formatShipMessage(ship, message(PERSONALITY_DISABLED_REASON, ship, personality));
        }
    }
    protected void setAI(ShipAPI ship) {
        //Generate new AI if one doesn't exist
        if (ship.getShipAI() == null) {
            ShipAIConfig config = new ShipAIConfig();
            config.personalityOverride = personality.value;
            Global.getSettings().createDefaultShipAI(ship, config);
        } else {
            ship.getShipAI().getConfig().personalityOverride = personality.value;
            ship.getShipAI().forceCircumstanceEvaluation(); //needed to make AI change personality?
        }
    }
}
