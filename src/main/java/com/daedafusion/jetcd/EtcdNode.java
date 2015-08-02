package com.daedafusion.jetcd;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by mphilpot on 6/23/14.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EtcdNode
{
    private static final Logger log = Logger.getLogger(EtcdNode.class);

    private String key;
    private Long createdIndex;
    private Long modifiedIndex;
    private String value;

    private String expiration;
    private Integer ttl;

    private boolean        dir;
    private List<EtcdNode> nodes;

    public EtcdNode()
    {

    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public Long getCreatedIndex()
    {
        return createdIndex;
    }

    public void setCreatedIndex(Long createdIndex)
    {
        this.createdIndex = createdIndex;
    }

    public Long getModifiedIndex()
    {
        return modifiedIndex;
    }

    public void setModifiedIndex(Long modifiedIndex)
    {
        this.modifiedIndex = modifiedIndex;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getExpiration()
    {
        return expiration;
    }

    public void setExpiration(String expiration)
    {
        this.expiration = expiration;
    }

    public Integer getTtl()
    {
        return ttl;
    }

    public void setTtl(Integer ttl)
    {
        this.ttl = ttl;
    }

    public boolean isDir()
    {
        return dir;
    }

    public void setDir(boolean dir)
    {
        this.dir = dir;
    }

    public List<EtcdNode> getNodes()
    {
        return nodes;
    }

    public void setNodes(List<EtcdNode> nodes)
    {
        this.nodes = nodes;
    }

    @Override
    public String toString()
    {
        return "EtcdNode{" +
                "key='" + key + '\'' +
                ", createdIndex=" + createdIndex +
                ", modifiedIndex=" + modifiedIndex +
                ", value='" + value + '\'' +
                ", expiration='" + expiration + '\'' +
                ", ttl=" + ttl +
                ", dir=" + dir +
                ", nodes=" + nodes +
                '}';
    }
}
