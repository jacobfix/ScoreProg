package jacobfix.scorepredictor;

import java.util.ArrayList;

import jacobfix.scorepredictor.users.User;

public interface GameProvider {
    NflGame getGame();
    ArrayList<User> getRankedParticipants();
}
