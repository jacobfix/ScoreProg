package jacobfix.scorepredictor;

public class NflGame {

    private static final String TAG = NflGame.class.getSimpleName();

    private String mGameId;
    private boolean locked;

    private int quarter;
    private int down;
    private int toGo;
    private String yardLine;
    private String clock;
    private boolean redZone;
    private String stadium;
    private String tv;
    private boolean isPregame;
    private boolean isFinal;

    private NflTeam awayTeam;
    private NflTeam homeTeam;
    private NflTeam posTeam;

    private DriveFeed mDriveFeed;

    public static final int QTR_FIRST = 1;
    public static final int QTR_SECOND = 2;
    public static final int QTR_THIRD = 3;
    public static final int QTR_FOURTH = 4;
    public static final int QTR_OVERTIME = 5;
    public static final int QTR_PREGAME = 0xfff0;
    public static final int QTR_HALFTIME = 0xfff1;
    public static final int QTR_FINAL = 0xfff2;
    public static final int QTR_FINAL_OVERTIME = 0xfff3;

    public NflGame(String gameId) {
        mGameId = gameId;
        awayTeam = new NflTeam(false);
        homeTeam = new NflTeam(true);
        mDriveFeed = new DriveFeed();
    }

    public String getGameId() {
        return mGameId;
    }

    public boolean isPredicted() {
        return awayTeam.isPredicted() && homeTeam.isPredicted();
    }

    public NflTeam getPredictedWinner() {
        // Returns null if a tie is predicted
        if (awayTeam.getPredictedScore() < homeTeam.getPredictedScore()) {
            return homeTeam;
        } else if (awayTeam.getPredictedScore() == homeTeam.getPredictedScore()) {
            return null;
        } else {
            return awayTeam;
        }
    }

    public boolean wasPredictedCorrectly() {
        NflTeam winner = getLeadingTeam();
        if (winner == null) {
            if (getPredictedWinner() == null) {
                return true;
            } else {
                return false;
            }
        } else if (winner == getPredictedWinner()) {
            return true;
        } else {
            return false;
        }
    }

    private NflTeam getLeadingTeam() {
        if (awayTeam.getScore() < homeTeam.getScore()) {
            return homeTeam;
        } else if (awayTeam.getScore() == homeTeam.getScore()) {
            return null;
        } else {
            return awayTeam;
        }
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void lock() {
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }

    public NflTeam getAwayTeam() {
        return this.awayTeam;
    }

    public void setAwayTeam(NflTeam team) {
        this.awayTeam = team;
    }

    public void setHomeTeam(NflTeam team) {
        this.homeTeam = team;
    }

    public NflTeam getHomeTeam() {
        return this.homeTeam;
    }

    public boolean isPregame() {
        return this.isPregame;
    }

    public void setPregame(boolean isPregame) {
        this.isPregame = isPregame;
    }

    public boolean isFinal() {
        return this.isFinal;
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public boolean inProgress() {
        return !this.isFinal && !this.isPregame;
    }

    public int getQuarter() {
        return this.quarter;
    }

    public void setQuarter(int qtr) {
        this.quarter = qtr;
    }

    public int getDown() {
        return this.down;
    }

    public void setDown(int down) {
        this.down = down;
    }

    public int getToGo() {
        return this.toGo;
    }

    public void setToGo(int toGo) {
        this.toGo = toGo;
    }

    public String getClock() {
        return this.clock;
    }

    public void setClock(String clock) {
        this.clock = clock;
    }

    public String getYardLine() {
        return this.yardLine;
    }

    public void setYardLine(String yl) {
        this.yardLine = yl;
    }

    public NflTeam getPosTeam() {
        return this.posTeam;
    }

    public void setPosTeam(NflTeam team) {
        /*
        if (team != awayTeam && team != homeTeam) {
            throw new AssertionError("Team with possession does not belong to game");
        }
        */
        this.posTeam = team;
    }

    public boolean inRedZone() {
        return this.redZone;
    }

    public void setRedZone(boolean redZone) {
        this.redZone = redZone;
    }

    public String getStadium() {
        return this.stadium;
    }

    public void setStadium(String stadium) {
        this.stadium = stadium;
    }

    public String getTv() {
        return this.tv;
    }

    public void setTv(String tv) {
        this.tv = tv;
    }

    public DriveFeed getDriveFeed() {
        return mDriveFeed;
    }

}
