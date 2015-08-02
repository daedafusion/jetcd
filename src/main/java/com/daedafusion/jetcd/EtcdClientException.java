package com.daedafusion.jetcd;

import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by mphilpot on 6/23/14.
 */
public class EtcdClientException extends IOException
{
    private static final Logger log = Logger.getLogger(EtcdClientException.class);

    private final Integer statusCode;
    private final EtcdResult result;

    public EtcdClientException(String message, Throwable cause)
    {
        super(message, cause);
        statusCode = null;
        result = null;
    }

    public EtcdClientException(String message, int httpStatusCode)
    {
        super(String.format("%s (%d)", message, httpStatusCode));
        statusCode = httpStatusCode;
        result = null;
    }

    public EtcdClientException(String message, EtcdResult result)
    {
        super(message);
        statusCode = null;
        this.result = result;
    }

    public Integer getStatusCode()
    {
        return statusCode;
    }

    public EtcdResult getResult()
    {
        return result;
    }

    public boolean isHttpError(int statusCode)
    {
        return this.statusCode != null && this.statusCode == statusCode;
    }

    public boolean isEtcdError(int etcdCode)
    {
        return this.result != null && this.result.getErrorCode() != null && this.result.getErrorCode() == etcdCode;
    }
}
