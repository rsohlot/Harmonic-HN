package com.simon.harmonichackernews.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

/**
 * Manages swipe action preferences for story list items.
 * Similar to materialistic's swipe functionality.
 */
public class SwipePreferences {

    public enum SwipeAction {
        None("None"),
        Save("Save"),
        Share("Share"),
        Vote("Vote"),
        MarkRead("Mark Read");

        private final String displayName;

        SwipeAction(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static SwipeAction fromString(String value) {
            if (value == null) return None;
            try {
                return SwipeAction.valueOf(value);
            } catch (IllegalArgumentException e) {
                return None;
            }
        }
    }

    private static final String PREF_SWIPE_LEFT = "pref_swipe_left_action";
    private static final String PREF_SWIPE_RIGHT = "pref_swipe_right_action";

    public static SwipeAction getLeftSwipeAction(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String value = prefs.getString(PREF_SWIPE_LEFT, SwipeAction.Save.name());
        return SwipeAction.fromString(value);
    }

    public static SwipeAction getRightSwipeAction(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String value = prefs.getString(PREF_SWIPE_RIGHT, SwipeAction.Vote.name());
        return SwipeAction.fromString(value);
    }

    public static void setLeftSwipeAction(Context context, SwipeAction action) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(PREF_SWIPE_LEFT, action.name()).apply();
    }

    public static void setRightSwipeAction(Context context, SwipeAction action) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(PREF_SWIPE_RIGHT, action.name()).apply();
    }

    public static SwipeAction[] getSwipePreferences(Context context) {
        return new SwipeAction[]{getLeftSwipeAction(context), getRightSwipeAction(context)};
    }

    public static boolean isSwipeEnabled(Context context) {
        SwipeAction left = getLeftSwipeAction(context);
        SwipeAction right = getRightSwipeAction(context);
        return left != SwipeAction.None || right != SwipeAction.None;
    }
}
