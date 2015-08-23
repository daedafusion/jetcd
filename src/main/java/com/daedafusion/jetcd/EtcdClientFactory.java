package com.daedafusion.jetcd;

import com.daedafusion.jetcd.impl.EtcdClientImpl;

/**
 * Created by mphilpot on 6/23/14.
 */
public class EtcdClientFactory
{
    private static EtcdClientFactory ourInstance = new EtcdClientFactory();

    /**
     * Default client to http://localhost:4001
     *
     * System property overrides:
     *
     * -DetcdHost overrides hostname
     * -DetcdPort overrids port
     *
     * @return new default client instance
     */
    public static EtcdClient newInstance()
    {
        return newInstance(String.format("http://%s:%d",
                        System.getProperty("etcdHost", "localhost"),
                        Integer.getInteger("etcdPort", 4001))
        );
    }

    /**
     * Specify etcd server
     *
     * @param server ${protocol}://${hostname}:${port}
     * @return client instance
     */
    public static EtcdClient newInstance(String server)
    {
        return new EtcdClientImpl(server);
    }

    private EtcdClientFactory()
    {
    }
}
