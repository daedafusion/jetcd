package com.daedafusion.jetcd;

import com.google.common.util.concurrent.ListenableFuture;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assume.assumeTrue;

/**
 * Created by mphilpot on 6/24/14.
 */
public class ClientTest
{
    private static final Logger log = Logger.getLogger(ClientTest.class);

    @BeforeClass
    public static void checkEtcd()
    {
        try
        {
            CloseableHttpClient client = HttpClients.createDefault();

            HttpGet request = new HttpGet("http://localhost:4001/version");

            HttpResponse response = client.execute(request);

            String version = EntityUtils.toString(response.getEntity());

            log.info(version);
        }
        catch(Exception e)
        {
            log.info("Unable to connect to etcd. Stopping test");
            assumeTrue(false);
        }

    }

    private String prefix;
    private EtcdClient client;

    @Before
    public void init()
    {
        prefix = String.format("/test-%s", UUID.randomUUID().toString());
        client = EtcdClientFactory.newInstance();
    }

    @Test
    public void setAndGet() throws EtcdClientException
    {
        String key = prefix + "/message";

        EtcdResult result;

        result = client.set(key, "hello");
        assertThat(result.getAction(), is("set"));
        assertThat(result.getNode().getValue(), is("hello"));
        assertThat(result.getPrevNode(), is(nullValue()));

        result = client.get(key);
        assertThat(result.getAction(), is("get"));
        assertThat(result.getNode().getValue(), is("hello"));
        assertThat(result.getPrevNode(), is(nullValue()));

        result = client.set(key, "world");
        assertThat(result.getAction(), is("set"));
        assertThat(result.getNode().getValue(), is("world"));
        assertThat(result.getPrevNode(), is(not(nullValue())));
        assertThat(result.getPrevNode().getValue(), is("hello"));

        result = client.get(key);
        assertThat(result.getAction(), is("get"));
        assertThat(result.getNode().getValue(), is("world"));
        assertThat(result.getPrevNode(), is(nullValue()));
    }

    @Test
    public void getNonExistentKey() throws EtcdClientException
    {
        String key = prefix + "/doesnotexist";

        EtcdResult result = client.get(key);
        assertThat(result, is(nullValue()));
    }

    @Test
    public void testDelete() throws EtcdClientException
    {
        String key = prefix + "/testDelete";

        EtcdResult result = client.set(key, "hello");

        result = client.get(key);
        assertThat(result.getNode().getValue(), is("hello"));

        result = client.delete(key);
        assertThat(result.getAction(), is("delete"));
        assertThat(result.getNode().getValue(), is(nullValue()));
        assertThat(result.getPrevNode(), is(not(nullValue())));
        assertThat(result.getPrevNode().getValue(), is("hello"));

        result = client.get(key);
        assertThat(result, is(nullValue()));
    }

    @Test
    public void deleteNonExistentKey()
    {
        String key = prefix + "/doesnotexist";

        try
        {
            client.delete(key);
            fail();
        }
        catch (EtcdClientException e)
        {
            assertThat(e.isEtcdError(100), is(true));
        }
    }

    @Test
    public void testTTL() throws EtcdClientException, InterruptedException
    {
        String key = prefix + "/ttl";

        EtcdResult result = client.set(key, "hello", 2);
        assertThat(result.getNode().getExpiration(), is(not(nullValue())));
        assertThat(result.getNode().getTtl(), anyOf(equalTo(1), equalTo(2)));

        result = client.get(key);
        assertThat(result.getNode().getValue(), is("hello"));

        TimeUnit.SECONDS.sleep(3);

        result = client.get(key);
        assertThat(result, is(nullValue()));
    }

    @Test
    public void testCAS() throws EtcdClientException
    {
        String key = prefix + "/cas";

        EtcdResult result = client.set(key, "hello");
        result = client.get(key);
        assertThat(result.getNode().getValue(), is("hello"));

        result = client.compareAndSet(key, "world", "world");
        assertThat(result.isError(), is(true));
        result = client.get(key);
        assertThat(result.getNode().getValue(), is("hello"));

        result = client.compareAndSet(key, "hello", "world");
        assertThat(result.isError(), is(false));
        result = client.get(key);
        assertThat(result.getNode().getValue(), is("world"));
    }

    @Test
    public void testWatchPrefix() throws EtcdClientException, ExecutionException, InterruptedException, TimeoutException
    {
        String key = prefix + "/watch";

        EtcdResult result = client.set(key+"/f2", "f2");
        assertThat(result.isError(), is(false));
        assertThat(result.getNode(), is(not(nullValue())));
        assertThat(result.getNode().getValue(), is("f2"));

        ListenableFuture<EtcdResult> watchFuture = client.watch(key, result.getNode().getModifiedIndex()+1, true);

        try
        {
            EtcdResult watchResult = watchFuture.get(100, TimeUnit.MILLISECONDS);
            fail("Subtree watch fired unexpectedly");
        }
        catch (TimeoutException e)
        {
            // Expected
        }

        assertThat(watchFuture.isDone(), is(false));

        result = client.set(key+"/f1", "f1");
        assertThat(result.isError(), is(false));
        assertThat(result.getNode(), is(not(nullValue())));
        assertThat(result.getNode().getValue(), is("f1"));

        EtcdResult watchResult = watchFuture.get(1, TimeUnit.SECONDS);

        assertThat(watchResult, is(not(nullValue())));
        assertThat(watchResult.isError(), is(false));
        assertThat(watchResult.getNode(), is(not(nullValue())));

        assertThat(watchResult.getNode().getKey(), is(key+"/f1"));
        assertThat(watchResult.getNode().getValue(), is("f1"));
        assertThat(watchResult.getAction(), is("set"));
        assertThat(watchResult.getNode().getModifiedIndex(), is(result.getNode().getModifiedIndex()));

    }

    @Test
    public void testList() throws EtcdClientException
    {
        String key = prefix + "/dir";

        EtcdResult result = client.set(key + "/f1", "f1");
        assertThat(result.getNode().getValue(), is("f1"));
        result = client.set(key + "/f2", "f2");
        assertThat(result.getNode().getValue(), is("f2"));
        result = client.set(key + "/f3", "f3");
        assertThat(result.getNode().getValue(), is("f3"));
        result = client.set(key + "/subdir1/f", "f");
        assertThat(result.getNode().getValue(), is("f"));

        List<EtcdNode> list = client.listDirectory(key);
        assertThat(list.size(), is(4));
    }

    @After
    public void teardown() throws IOException
    {
        try
        {
            client.deleteDirectoryRecursive(prefix);
        }
        catch(EtcdClientException e)
        {
            if(!e.isHttpError(404))
            {
                log.error("Error cleaning up test", e);
            }
        }
        client.close();
    }
}
