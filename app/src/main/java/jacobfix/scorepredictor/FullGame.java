package jacobfix.scorepredictor;

import org.json.JSONObject;

import jacobfix.scorepredictor.schedule.Schedule;
import jacobfix.scorepredictor.schedule.ScheduledGame;
import jacobfix.scorepredictor.schedule.Season;

public class FullGame {

    private AtomicGame atom;

    private final Object lock = new Object();

    private String clock;
    private DriveFeed driveFeed = new DriveFeed();

    private boolean updatesRestricted;

    public FullGame(AtomicGame atom) {
        this.atom = atom;
    }

    public AtomicGame getAtom() {
        return atom;
    }

    public Object getLock() {
        return lock;
    }

    public String getId() {
        return atom.getId();
    }

    public boolean isLocked() {
        return atom.isLocked();
    }

    public int getQuarter() {
        return atom.getQuarter();
    }

    public boolean isPregame() {
        return atom.isPregame();
    }

    public boolean isFinal() {
        return atom.isFinal();
    }

    public String getClock() {
        return clock;
    }

    public String getAwayAbbr() {
        return atom.getAwayAbbr();
    }

    public String getHomeAbbr() {
        return atom.getHomeAbbr();
    }

    public String getAwayName() {
        return atom.getAwayName();
    }

    public String getHomeName() {
        return atom.getHomeName();
    }

    public int getAwayScore() {
        return atom.getAwayScore();
    }

    public int getHomeScore() {
        return atom.getHomeScore();
    }

    public int getAwayColor() {
        return atom.getAwayColor();
    }

    public int getHomeColor() {
        return atom.getHomeColor();
    }

    public Season.WeekType getWeekType() {
        return atom.getWeekType();
    }

    public int getWeek() {
        return atom.getWeek();
    }

    public Schedule.Day getDayOfWeek() {
        return atom.getDayOfWeek();
    }

    public long getStartTime() {
        return atom.getStartTime();
    }

    public String getStartTimeDisplay() {
        return atom.getStartTimeDisplay();
    }

    public Team getAwayTeam() {
        return atom.getAwayTeam();
    }

    public Team getHomeTeam() {
        return atom.getHomeTeam();
    }

    public DriveFeed getDriveFeed() {
        return driveFeed;
    }

    public void restrictUpdates() {
        updatesRestricted = true;
    }

    public void allowUpdates() {
        updatesRestricted = false;
    }

    public static void disableUpdates(FullGame game) {
        synchronized (game.getLock()) {
            game.updatesRestricted = true;
        }
    }

    public static void enableUpdates(FullGame game) {
        synchronized (game.getLock()) {
            game.updatesRestricted = false;
        }
    }

    public void sync(JSONObject json) {

    }
}
