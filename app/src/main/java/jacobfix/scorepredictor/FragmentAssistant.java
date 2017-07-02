package jacobfix.scorepredictor;

import java.util.Collection;

import jacobfix.scorepredictor.users.User;

public interface FragmentAssistant {

    Collection<User> getUsers(String gameId);
}
