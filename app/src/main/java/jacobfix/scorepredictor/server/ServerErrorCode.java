package jacobfix.scorepredictor.server;

public enum ServerErrorCode {
    NONE,
    INSUFFICIENT_PARAMS,
    EMAIL_NO_MATCH,
    USERNAME_NO_MATCH,
    INVALID_PASSWORD,
    DATABASE_FAILURE,
    USERNAME_ALREADY_EXISTS,
}
