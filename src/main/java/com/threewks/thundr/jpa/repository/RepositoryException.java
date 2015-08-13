package com.threewks.thundr.jpa.repository;

import com.threewks.thundr.exception.BaseException;

public class RepositoryException extends BaseException {
    private static final long serialVersionUID = 1L;

    public RepositoryException(String format, Object... formatArgs) {
        super(format, formatArgs);
    }

    public RepositoryException(Throwable cause, String format, Object... formatArgs) {
        super(cause, format, formatArgs);
    }

}