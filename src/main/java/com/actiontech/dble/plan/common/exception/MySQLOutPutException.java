package com.actiontech.dble.plan.common.exception;

/**
 * MySQLOutPutException
 *
 * @author ActionTech
 */
public class MySQLOutPutException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -7089907072181836842L;

    private final int errorCode;
    private final String sqlState;

    public MySQLOutPutException(int errorCode, String sqlState, String msg) {
        super(msg);
        this.errorCode = errorCode;
        this.sqlState = sqlState;
    }

    public MySQLOutPutException(int errorCode, String sqlState, String msg, Throwable cause) {
        super(msg, cause);
        this.errorCode = errorCode;
        this.sqlState = sqlState;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getSqlState() {
        return sqlState;
    }

    public String toMysqlErrorMsg() {
        return String.format("ERROR %d (%s): %s", errorCode, sqlState, getMessage());
    }

}
