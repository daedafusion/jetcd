package com.daedafusion.jetcd;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.log4j.Logger;

/**
 * Created by mphilpot on 6/23/14.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EtcdResult
{
    private static final Logger log = Logger.getLogger(EtcdResult.class);

    private String action;
    private EtcdNode node;
    private EtcdNode prevNode;

    private Integer errorCode;
    private String message;
    private String cause;
    private Integer index;

    public EtcdResult()
    {

    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public EtcdNode getNode()
    {
        return node;
    }

    public void setNode(EtcdNode node)
    {
        this.node = node;
    }

    public EtcdNode getPrevNode()
    {
        return prevNode;
    }

    public void setPrevNode(EtcdNode prevNode)
    {
        this.prevNode = prevNode;
    }

    public Integer getErrorCode()
    {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode)
    {
        this.errorCode = errorCode;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getCause()
    {
        return cause;
    }

    public void setCause(String cause)
    {
        this.cause = cause;
    }

    public Integer getIndex()
    {
        return index;
    }

    public void setIndex(Integer index)
    {
        this.index = index;
    }

    public boolean isError()
    {
        return errorCode != null;
    }
}
