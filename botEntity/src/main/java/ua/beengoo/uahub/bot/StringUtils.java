package ua.beengoo.uahub.bot;

public class StringUtils {

    /**
     * Truncate to a hard limit and append ellipsis so that the result length <= limit.
     * Keeps surrogate pairs intact.
     */
    public static String ellipsize(String s, int limit) {
        return ellipsize(s, limit, "...");
    }

    public static String ellipsize(String s, int limit, String ellipsis) {
        if (s == null) return null;
        if (limit < 0) throw new IllegalArgumentException("limit must be >= 0");
        if (ellipsis == null) ellipsis = "";
        if (s.length() <= limit) return s;

        if (limit <= ellipsis.length()) {
            // If even ellipsis doesn't fit fully, return a slice of it
            return ellipsis.substring(0, limit);
        }

        int keep = limit - ellipsis.length();
        int end = keep;

        // Avoid cutting in the middle of a surrogate pair
        if (end > 0 && end < s.length()
            && Character.isHighSurrogate(s.charAt(end - 1))
            && Character.isLowSurrogate(s.charAt(end))) {
            end--;
        }

        return s.substring(0, end) + ellipsis;
    }

}
