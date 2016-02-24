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
        return newInstance(String.format("%s://%s:%d",
                        System.getProperty("etcdProtocol", System.getenv().getOrDefault("ETCD_PROTOCOL", "http")),
                        System.getProperty("etcdHost", System.getenv().getOrDefault("ETCD_HOST", "localhost")),
                        Integer.getInteger("etcdPort", Integer.parseInt(System.getenv().getOrDefault("ETCD_PORT", "4001"))))
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
