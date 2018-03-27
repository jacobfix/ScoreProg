package jacobfix.scoreprog.server;

import java.util.HashMap;
import java.util.Map;

public class ServerException extends Exception {

    private int errno;

    public static final int ERR_NONE = 0;
    public static final int ERR_MYSQL_CONN = 1;
    public static final int ERR_MYSQL_EXEC = 2;
    public static final int ERR_INVALID_PARAMS = 3;
    public static final int ERR_BAD_AUTH = 4;
    public static final int ERR_WRONG_PASSWD = 5;
    public static final int ERR_USERNAME_TAKEN = 6;
    public static final int ERR_EMAIL_TAKEN = 7;
    public static final int ERR_USERNAME_OR_EMAIL_TAKEN = 8;

    public ServerException(int errno) {
        this.errno = errno;
    }

    public int getErrno() {
        return errno;
    }

    @Override
    public String toString() {
        return String.valueOf(errno);
    }
}
