package com.example.antoine.testapp.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class LeagueClubs {

    public static final int leagueId = 0;
    /**
     * An array of sample (dummy) items.
     */
    public static final List<Club> ITEMS = new ArrayList<Club>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Club> ITEM_MAP = new HashMap<String, Club>();

    public static void addItem(Club item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static boolean isEmpty(){
        if(ITEMS.size()==0 || ITEM_MAP.size() == 0)
            return true;
        if(ITEM_MAP.size() != ITEMS.size())
            return true;
        return false;
    }
    public static void clear() {
        ITEMS.clear();
        ITEM_MAP.clear();
    }
}
