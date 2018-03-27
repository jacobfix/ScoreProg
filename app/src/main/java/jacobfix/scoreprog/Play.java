package jacobfix.scoreprog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;

import jacobfix.scoreprog.util.Util;

public class Play {

    private int down;
    private String yardLine;

    private int quarter;
    private String clock;

    private int yardsToGo;
    private int yardsNet;

    private String posTeam;

    private String description;
    private String note;

    private SequenceItem[] sequenceItems;

    private static final Object lock = new Object();

    private int lastJsonHash;

    public void updateFromJson(JSONObject json) throws JSONException {
        int jsonHash = json.toString().hashCode();
        if (jsonHash == lastJsonHash)
            return;

        synchronized (lock) {
            down = json.getInt("down");
            yardLine = json.getString("yrdln");

            quarter = Util.parseQuarter(json.get("qtr"));
            clock = json.getString("time");

            yardsToGo = json.getInt("ydstogo");
            yardsNet = json.getInt("ydsnet");

            posTeam = json.getString("posteam");

            description = json.getString("desc");
            note = json.getString("note");

            /* It shouldn't be an issue to just create the array of SequenceItems on each update,
               because a play should really only be updated once, unlike a drive, to which plays
               are continually being appended. */
            JSONObject sequenceJson = json.getJSONObject("players");

            ArrayList<SequenceItem> items = new ArrayList<>();
            Iterator<String> sequenceItemIterator = sequenceJson.keys();
            JSONArray playerSequenceItems;
            while (sequenceItemIterator.hasNext()) {
                playerSequenceItems = sequenceJson.getJSONArray(sequenceItemIterator.next());
                JSONObject sequenceItemJson;
                for (int i = 0; i < playerSequenceItems.length(); i++) {
                    sequenceItemJson = playerSequenceItems.getJSONObject(i);

                    SequenceItem sequenceItem = new SequenceItem();
                    sequenceItem.sequenceNum = sequenceItemJson.getInt("sequence");
                    sequenceItem.teamAbbr = sequenceItemJson.getString("clubcode");
                    sequenceItem.yards = sequenceItemJson.getInt("yards");
                    sequenceItem.statId = sequenceItemJson.getInt("statId");
                    sequenceItem.playerName = sequenceItemJson.getString("playerName");

                    items.add(sequenceItem);
                }
            }
            Collections.sort(items, new Comparator<SequenceItem>() {
                @Override
                public int compare(SequenceItem item1, SequenceItem item2) {
                    return item1.sequenceNum - item2.sequenceNum;
                }
            });
            sequenceItems = items.toArray(new SequenceItem[items.size()]);

            lastJsonHash = jsonHash;
        }
    }

    public String deduceTitle() {
        for (SequenceItem item : sequenceItems) {
            if (item.hasPlayer() && item.getStatId() <= 120) {
                /* We're just assuming the most important part of the play will be the first item
                   in the sequence of events that has a player associated with it. */
                String title = StatProvider.getTitle(item.getStatId());
                if (StatProvider.includeYardage(item.getStatId())) {
                    int yardage = getPlayYardage();
                    title = String.format(title, yardage);
                }
                return title;
            }
        }
        return null;
    }

    private int getPlayYardage() {
        for (SequenceItem item : sequenceItems) {
            if (item.getYards() != 0) {
                return item.getYards();
            }
        }
        return 0;
    }

    public int getQuarter() {
        return quarter;
    }

    public int getDown() {
        return down;
    }

    public String getClock() {
        return clock;
    }

    public String getYardLine() {
        return yardLine;
    }

    public int getYardsToGo() {
        return yardsToGo;
    }

    public int getYardsNet() {
        return yardsNet;
    }

    public String getPosTeam() {
        return posTeam;
    }

    public String getDescription() {
        return description;
    }

    public String getNote() {
        return note;
    }

    public void setDown(int down) {
        this.down = down;
    }

    public void setYardLine(String yardLine) {
        this.yardLine = yardLine;
    }

    public void setQuarter(int quarter) {
        this.quarter = quarter;
    }

    public void setClock(String clock) {
        this.clock = clock;
    }

    public void setYardsToGo(int yardsToGo) {
        this.yardsToGo = yardsToGo;
    }

    public void setYardsNet(int yardsNet) {
        this.yardsNet = yardsNet;
    }

    public void setPosTeam(String posTeam) {
        this.posTeam = posTeam;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public SequenceItem[] getPlaySequence() {
        return sequenceItems;
    }

    public SequenceItem getSequenceItem(int index) {
        return sequenceItems[index];
    }

    public int numSequenceItems() {
        return sequenceItems.length;
    }

    public void addSequenceItem(int index, SequenceItem item) {
        sequenceItems[index] = item;
    }

    public void setSequenceItems(Play.SequenceItem[] sequenceItems) {
        this.sequenceItems = sequenceItems;
    }

    public boolean isGameInstance() {
        return posTeam.equals("");
    }

    public static class SequenceItem {
        private int sequenceNum;
        private String teamAbbr;
        private String playerName;
        private int statId;
        private int yards;

        public int getSequenceNum() {
            return sequenceNum;
        }

        public String getTeamAbbr() {
            return teamAbbr;
        }

        public String getPlayerName() {
            return playerName;
        }

        public int getStatId() {
            return statId;
        }

        public int getYards() {
            return yards;
        }

        public boolean hasPlayer() {
            return playerName != null;
        }

        public void setSequenceNum(int num) {
            this.sequenceNum = num;
        }

        public void setTeamAbbr(String teamAbbr) {
            this.teamAbbr = teamAbbr;
        }

        public void setYards(int yards) {
            this.yards = yards;
        }

        public void setStatId(int statId) {
            this.statId = statId;
        }

        public void setPlayerName(String playerName) {
            this.playerName = playerName;
        }

        public static SequenceItem generateRandom() {
            SequenceItem item = new SequenceItem();
            Random random = new Random();

            item.sequenceNum = 0;
            item.teamAbbr = "DEN";
            item.playerName = Util.randomString(14);
            item.statId = random.nextInt(70);
            item.yards = random.nextInt(10);

            return item;
        }
    }

    public static Play generateRandom() {
        Play play = new Play();
        Random random = new Random();

        play.posTeam = "DEN";

        play.down = random.nextInt(3) + 1;
        play.yardLine = play.posTeam + " " + (random.nextInt(50) + 1);

        play.quarter = random.nextInt(3) + 1;
        play.clock = "12:34";

        play.yardsToGo = random.nextInt(10) + 1;
        play.yardsNet = random.nextInt(20);

        play.description = Util.randomString(100);
        play.note = null;

        play.sequenceItems = new SequenceItem[]{SequenceItem.generateRandom()};

        return play;
    }

    public Object acquireLock() {
        return lock;
    }
}
