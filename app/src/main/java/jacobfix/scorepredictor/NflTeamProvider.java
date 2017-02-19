package jacobfix.scorepredictor;

import android.graphics.Color;

import java.util.HashMap;

public class NflTeamProvider {

    private static String[][] teams = {
            {"ARI", "Arizona",       "Cardinals",  "Arizona Cardinals",    "#b0063a", "#000000"},
            {"ATL", "Atlanta",       "Falcons",    "Atlanta Falcons",      "#c9243f", "#000000"},
            {"BAL", "Baltimore",     "Ravens",     "Baltimore Ravens",     "#2c2e8c", "#d0b240"},
            {"BUF", "Buffalo",       "Bills",      "Buffalo Bills",        "#005596", "#c60c30"},
            {"CAR", "Carolina",      "Panthers",   "Carolina Panthers",    "#009ada", "#a5acaf"},
            {"CHI", "Chicago",       "Bears",      "Chicago Bears",        "#171a3c", "#dd4814"},
            {"CIN", "Cincinnati",    "Bengals",    "Cincinnati Bengals",   "#f04e23", "#000000"},
            {"CLE", "Cleveland",     "Browns",     "Cleveland Browns",     "#3f230d", "#fe3c00"},
            {"DAL", "Dallas",        "Cowboys",    "Dallas Cowboys",       "#a0a6ab", "#c5ced6"},
            {"DEN", "Denver",        "Broncos",    "Denver Broncos",       "#002f69", "#002244"},
            {"DET", "Detroit",       "Lions",      "Detroit Lions",        "#898b8c", "#c5c7cf"},
            {"GB",  "Green Bay",     "Packers",    "Green Bay Packers",    "#29433a", "#ffb612"},
            {"HOU", "Houston",       "Texans",     "Houston Texans",       "#00123f", "#b31b34"},
            {"IND", "Indianapolis",  "Colts",      "Indianapolis Colts",   "#003d79", "#ffffff"},
            {"JAC", "Jacksonville",  "Jaguars",    "Jacksonville Jaguars", "#d8a328", "#9f792c"},
            {"KC",  "Kansas City",   "Chiefs",     "Kansas City Chiefs",   "#e51937", "#f2c800"},
            {"MIA", "Miami",         "Dolphins",   "Miami Dolphins",       "#105878", "#f5811f"},
            {"MIN", "Minnesota",     "Vikings",    "Minnesota Vikings",    "#4f2e84", "#f0bf00"},
            {"NE",  "New England",   "Patriots",   "New England Patriots", "#002551", "#c80815"},
            {"NO",  "New Orleans",   "Saints",     "New Orleans Saints",   "#978458", "#000000"},
            {"NYG", "New York",      "Giants",     "New York Giants",      "#003c7b", "#ca001a"},
            {"NYJ", "New York",      "Jets",       "New York Jets",        "#29433a", "#ffffff"},
            {"OAK", "Oakland",       "Raiders",    "Oakland Raiders",      "#a5abaf", "#c4c8cb"},
            {"PHI", "Philadelphia",  "Eagles",     "Philadelphia Eagles",  "#004c54", "#708090"},
            {"PIT", "Pittsburgh",    "Steelers",   "Pittsburgh Steelers",  "#202020", "#000000"},
            {"SD",  "San Diego",     "Chargers",   "San Diego Chargers",   "#007ec3", "#ffb81c"},
            {"SF",  "San Francisco", "49ers",      "San Francisco 49ers",  "#c8aa76", "#e6be8a"},
            {"SEA", "Seattle",       "Seahawks",   "Seattle Seahawks",     "#00295b", "#69be28"},
            {"LA",  "Los Angeles",   "Rams",       "Los Angeles Rams",     "#001f44", "#13264b"},
            {"TB",  "Tampa Bay",     "Buccaneers", "Tampa Bay Buccaneers", "#636161", "#89765f"},
            {"TEN", "Tennessee",     "Titans",     "Tennessee Titans",     "#4095d1", "#0d254c"},
            {"WAS", "Washington",    "Redskins",   "Washington Redskins",  "#681b11", "#ffb612"}
    };

    private static HashMap<String, Integer> teamNameIndices = new HashMap<String, Integer>(){
        {
            for (int i = 0; i < teams.length; i++) {
                put(teams[i][ABBR_INDEX], i);
            }
        }
    };

    private static final int ABBR_INDEX = 0;
    private static final int LOCALE_INDEX = 1;
    private static final int NAME_INDEX = 2;
    private static final int FULL_NAME_INDEX = 3;
    private static final int PRIMARY_COLOR_INDEX = 4;
    private static final int SECONDARY_COLOR_INDEX = 5;

    public static String getTeamLocale(String abbr) {
        String[] names = teams[teamNameIndices.get(abbr)];
        return names[LOCALE_INDEX];
    }

    public static String getTeamName(String abbr) {
        String[] names = teams[teamNameIndices.get(abbr)];
        return names[NAME_INDEX];
    }

    public static String getTeamFullName(String abbr) {
        String[] names = teams[teamNameIndices.get(abbr)];
        return names[FULL_NAME_INDEX];
    }

    public static int getTeamPrimaryColor(String abbr) {
        String[] names = teams[teamNameIndices.get(abbr)];
        return Color.parseColor(names[PRIMARY_COLOR_INDEX]);
    }

    public static int getTeamSecondaryColor(String abbr) {
        String[] names = teams[teamNameIndices.get(abbr)];
        return Color.parseColor(names[SECONDARY_COLOR_INDEX]);
    }
}
