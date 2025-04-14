package data.combatlog;

import com.fs.starfarer.api.Global;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Util {
    ;
    public static final String MOD_KEY = "AutomatedCommands";
    private static final String REPLACE_VALUE = "variable";

    //Pattern to find items matching `$AutomatedCommands:PERSONALITY_APPLIED` and using
    private static final Pattern TEMPLATE_VARIABLE = Pattern.compile("\\$(?<" + REPLACE_VALUE + ">(?:[A-Za-z_]*[A-Za-z]:)?([A-Z_]*[A-Z]))+");
    private static final MessageFormat PERCENT_FORMATTER = Util.resolveSubstitutions(MOD_KEY + ":PERCENT");

    /**
     * Fetches a defined string from a JSON file using `category:id` as shorthand for getString(category,id)
     * @param key A string in the format `category:id`
     * @return the fetched value
     */
    public static String getString(String key) {
        int index = key.indexOf(":");
        if (index == -1) {
            return Global.getSettings().getString(key);
        }
        return Global.getSettings().getString(key.substring(0,index), key.substring(index+1));
    }

    /**
     * Replaces values of `$category:id` with the substitutions defined for them in the strings JSON files
     * @param message A message to get parsed
     * @return a string with `$category:id` replaced by predefined values
     */
    public static MessageFormat resolveSubstitutions(String message) {
        String input = getString(message);
        Matcher matcher = TEMPLATE_VARIABLE.matcher(input);

        // TODO make matcher find group and id
        while (matcher.find()) {
            String replacement = getString(matcher.group(REPLACE_VALUE));
            input = matcher.replaceFirst(replacement);
        }

        return new MessageFormat(input);
    }

    public static String percentToString(float value) {
        return PERCENT_FORMATTER.format(new Object[]{value});
    }

    //TODO implement NumberFormat
    public static String percentage(float value) {
        return (int)(value * 100) + "%";
    }
    public static String percentage(double value) {
        return (int)(value * 100) + "%";
    }
}
