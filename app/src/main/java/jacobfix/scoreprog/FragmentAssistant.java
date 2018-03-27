package jacobfix.scoreprog;

import java.util.Collection;

import jacobfix.scoreprog.users.User;

public interface FragmentAssistant {

    Collection<User> getUsers(String gameId);
}
