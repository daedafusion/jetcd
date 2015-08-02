package com.daedafusion.jetcd.impl;

import com.daedafusion.jetcd.EtcdClient;
import com.daedafusion.jetcd.EtcdClientException;
import com.daedafusion.jetcd.EtcdNode;
import com.daedafusion.jetcd.EtcdResult;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by mphilpot on 6/23/14.
 */
public class EtcdClientImpl implements EtcdClient
{
    private static final Logger log = Logger.getLogger(EtcdClientImpl.class);

    private final ObjectMapper             mapper;
    private final CloseableHttpAsyncClient client;

    private static final String ROOT_PREFIX = "v2/keys";

    private URI baseUri;

    public EtcdClientImpl(String uri)
    {
        mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        client = HttpAsyncClients.createDefault();
        client.start();

        if(!uri.endsWith("/"))
        {
            uri += "/";
        }

        baseUri = URI.create(uri);
    }

    @Override
    public EtcdResult get(String key) throws EtcdClientException
    {
        URI uri = buildUri(ROOT_PREFIX, key, "");
        HttpGet request = new HttpGet(uri);

        EtcdResult result = syncExecute(request, new int[] { 200, 404 }, 100);

        if(result.getErrorCode() != null && result.getErrorCode() == 100)
        {
            return null;
        }

        return result;
    }

    @Override
    public EtcdResult delete(String key) throws EtcdClientException
    {
        URI uri = buildUri(ROOT_PREFIX, key, "");
        HttpDelete request = new HttpDelete(uri);

        return syncExecute(request, new int[] {200, 404});
    }

    @Override
    public EtcdResult set(String key, String value) throws EtcdClientException
    {
        return set(key, value, null);
    }

    @Override
    public EtcdResult set(String key, String value, Integer ttl) throws EtcdClientException
    {
        List<BasicNameValuePair> data = Lists.newArrayList();
        data.add(new BasicNameValuePair("value", value));

        if(ttl != null)
        {
            data.add(new BasicNameValuePair("ttl", Integer.toString(ttl)));
        }

        return set0(key, "", data, new int[] {200, 201});
    }

    @Override
    public EtcdResult refreshDirectory(String key, Integer ttl) throws EtcdClientException
    {
        List<BasicNameValuePair> data = Lists.newArrayList();
        data.add(new BasicNameValuePair("dir", "true"));
        data.add(new BasicNameValuePair("prevExist", "true"));

        if(ttl != null)
        {
            data.add(new BasicNameValuePair("ttl", Integer.toString(ttl)));
        }

        return set0(key, "", data, new int[] {200, 201});
    }

    @Override
    public EtcdResult createDirectory(String key) throws EtcdClientException
    {
        return createDirectory(key, null);
    }

    @Override
    public EtcdResult createDirectory(String key, Integer ttl) throws EtcdClientException
    {
        List<BasicNameValuePair> data = Lists.newArrayList();
        data.add(new BasicNameValuePair("dir", "true"));

        if(ttl != null)
        {
            data.add(new BasicNameValuePair("ttl", Integer.toString(ttl)));
        }

        return set0(key, "", data, new int[] {200, 201});
    }

    @Override
    public List<EtcdNode> listDirectory(String key) throws EtcdClientException
    {
        EtcdResult result = get(key.endsWith("/") ? key : key + "/");

        if(result == null || result.getNode() == null || result.getNode().getNodes() == null)
        {
            return new ArrayList<EtcdNode>();
        }

        return result.getNode().getNodes();
    }

    @Override
    public EtcdResult deleteDirectory(String key) throws EtcdClientException
    {
        URI uri = buildUri(ROOT_PREFIX, key, "?dir=true");
        HttpDelete request = new HttpDelete(uri);

        return syncExecute(request, new int[] {202, 200});
    }

    @Override
    public EtcdResult deleteDirectoryRecursive(String key) throws EtcdClientException
    {
        URI uri = buildUri(ROOT_PREFIX, key, "?recursive=true");
        HttpDelete request = new HttpDelete(uri);

        return syncExecute(request, new int[] {202, 200});
    }

    @Override
    public EtcdResult compareAndSet(String key, String prevValue, String value) throws EtcdClientException
    {
        List<BasicNameValuePair> data = Lists.newArrayList();
        data.add(new BasicNameValuePair("value", value));
        data.add(new BasicNameValuePair("prevValue", prevValue)); // TODO this may have to be a parameter rather than payload

        return set0(key, "", data, new int[] { 200, 412 }, 101);
    }

    @Override
    public EtcdResult compareAndSet(String key, Integer prevIndex, String value) throws EtcdClientException
    {
        List<BasicNameValuePair> data = Lists.newArrayList();
        data.add(new BasicNameValuePair("value", value));
        data.add(new BasicNameValuePair("prevIndex", prevIndex.toString())); // TODO this may have to be a parameter

        return set0(key, "", data, new int[] { 200, 412 }, 101);
    }

    @Override
    public EtcdResult compareAndDelete(String key, String prevValue) throws EtcdClientException
    {
        URI uri = buildUri(ROOT_PREFIX, key, String.format("?prevValue=%s", urlEscape(prevValue)));

        HttpDelete request = new HttpDelete(uri);

        return syncExecute(request, new int[] {200, 412}, 101);
    }

    @Override
    public EtcdResult compareAndDelete(String key, Integer prevIndex) throws EtcdClientException
    {
        URI uri = buildUri(ROOT_PREFIX, key, String.format("?prevIndex=%d", prevIndex));

        HttpDelete request = new HttpDelete(uri);

        return syncExecute(request, new int[] {200, 412}, 101);
    }

    @Override
    public ListenableFuture<EtcdResult> watch(String key) throws EtcdClientException
    {
        return watch(key, null, false);
    }

    @Override
    public ListenableFuture<EtcdResult> watch(String key, boolean recursive) throws EtcdClientException
    {
        return watch(key, null, recursive);
    }

    @Override
    public ListenableFuture<EtcdResult> watch(String key, Long index, boolean recursive) throws EtcdClientException
    {
        String suffix = "?wait=true";
        if(index != null)
        {
            suffix += String.format("&waitIndex=%d", index.intValue());
        }
        if(recursive)
        {
            suffix += "&recursive=true";
        }

        URI uri = buildUri(ROOT_PREFIX, key, suffix);

        HttpGet request = new HttpGet(uri);

        return asyncExecute(request, new int[] { 200 });
    }

    @Override
    public EtcdResult queue(String key, String value) throws EtcdClientException
    {
        URI uri = buildUri(ROOT_PREFIX, key, "");
        HttpPost request = new HttpPost(uri);

        List<BasicNameValuePair> data = Lists.newArrayList();
        data.add(new BasicNameValuePair("value", value));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(data, Charsets.UTF_8);
        request.setEntity(entity);

        return syncExecute(request, new int[] { 200 });
    }

    @Override
    public EtcdResult getQueue(String key) throws EtcdClientException
    {
        URI uri = buildUri(ROOT_PREFIX, key, "?recursive=true&sorted=true");
        HttpGet request = new HttpGet(uri);

        return syncExecute(request, new int[] { 200 });
    }

    @Override
    public void close() throws IOException
    {
        client.close();
    }

    protected EtcdResult set0(String key, String suffix, List<BasicNameValuePair> data, int[] httpErrorCodes, int... expectedErrorCodes) throws EtcdClientException
    {
        URI uri = buildUri(ROOT_PREFIX, key, suffix);

        HttpPut request = new HttpPut(uri);

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(data, Charsets.UTF_8);
        request.setEntity(entity);

        return syncExecute(request, httpErrorCodes, expectedErrorCodes);
    }

    private static class JsonResponse
    {
        private final String json;
        private final int statusCode;

        public JsonResponse(String json, int statusCode)
        {
            this.json = json;
            this.statusCode = statusCode;
        }

        public String getJson()
        {
            return json;
        }

        public int getStatusCode()
        {
            return statusCode;
        }
    }

    protected URI buildUri(String prefix, String key, String suffix)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);

        if(key.startsWith("/"))
        {
            key = key.substring(1);
        }

        for(String token : Splitter.on('/').split(key))
        {
            sb.append("/");
            sb.append(urlEscape(token));
        }

        sb.append(suffix);

        return baseUri.resolve(sb.toString());
    }

    protected String urlEscape(String s)
    {
        try
        {
            return URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            log.warn("URLEncoder Error", e);
            return s;
        }
    }

    protected EtcdResult syncExecute(HttpUriRequest request, int[] expectedHttpStatusCodes, int... expectedErrorCodes) throws EtcdClientException
    {
        try
        {
            return asyncExecute(request, expectedHttpStatusCodes, expectedErrorCodes).get();
        }
        catch(InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new EtcdClientException("Request Interrupted", e);
        }
        catch(ExecutionException e)
        {
            throw unwrap(e);
        }
    }

    protected ListenableFuture<EtcdResult> asyncExecute(HttpUriRequest request, int[] expectedHttpStatusCodes, final int... expectedErrorCodes)
    {
        ListenableFuture<JsonResponse> jsonFuture = asyncExecuteJson(request, expectedHttpStatusCodes);

        return Futures.transform(jsonFuture, new AsyncFunction<JsonResponse, EtcdResult>(){

            @Override
            public ListenableFuture<EtcdResult> apply(JsonResponse jsonResponse) throws Exception
            {
                EtcdResult result = jsonToEtcdResult(jsonResponse, expectedErrorCodes);
                return Futures.immediateFuture(result);
            }
        });
    }

    protected ListenableFuture<JsonResponse> asyncExecuteJson(HttpUriRequest request, final int[] expectedHttpStatusCodes)
    {
        ListenableFuture<HttpResponse> response = asyncExecuteHttp(request);

        return Futures.transform(response, new AsyncFunction<HttpResponse, JsonResponse>()
        {
            @Override
            public ListenableFuture<JsonResponse> apply(HttpResponse httpResponse) throws Exception
            {
                JsonResponse json = extractJsonResponse(httpResponse, expectedHttpStatusCodes);
                return Futures.immediateFuture(json);
            }
        });
    }

    protected ListenableFuture<HttpResponse> asyncExecuteHttp(HttpUriRequest request)
    {
        final SettableFuture<HttpResponse> future = SettableFuture.create();

        client.execute(request, new FutureCallback<HttpResponse>()
        {
            @Override
            public void completed(HttpResponse result)
            {
                future.set(result);
            }

            @Override
            public void failed(Exception e)
            {
                future.setException(e);
            }

            @Override
            public void cancelled()
            {
                future.setException(new InterruptedException());
            }
        });

        return future;
    }

    protected JsonResponse extractJsonResponse(HttpResponse response, int[] expectedHttpStatusCodes) throws EtcdClientException
    {
        try
        {
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();

            String json = null;

            if (response.getEntity() != null)
            {
                try
                {
                    json = EntityUtils.toString(response.getEntity());
                }
                catch (IOException e)
                {
                    throw new EtcdClientException("Error extracting response entity", e);
                }
            }

            if (!contains(expectedHttpStatusCodes, statusCode))
            {
                if(statusCode == 400 && json != null)
                {
                    // More information in JSON
                }
                else
                {
                    throw new EtcdClientException(String.format("etcd error :: %s", statusLine.getReasonPhrase()), statusCode);
                }
            }

            return new JsonResponse(json, statusCode);
        }
        finally
        {
            if(response != null)
            {
                HttpEntity entity = response.getEntity();
                if(entity != null)
                {
                    EntityUtils.consumeQuietly(entity);
                }

            }
        }
    }

    protected EtcdResult jsonToEtcdResult(JsonResponse response, int... expectedErrorCodes) throws EtcdClientException
    {
        if(response == null || response.json == null)
        {
            return null;
        }

        EtcdResult result = parseEtcdResult(response.json);

        if(result.getErrorCode() != null && !contains(expectedErrorCodes, result.getErrorCode()))
        {
            throw new EtcdClientException(result.getMessage(), result);
        }

        return result;
    }

    protected EtcdResult parseEtcdResult(String json) throws EtcdClientException
    {
        EtcdResult result;

        try
        {
            result = mapper.readValue(json, EtcdResult.class);
        }
        catch (IOException e)
        {
            throw new EtcdClientException("Error parsing etcd response", e);
        }

        return result;
    }

    protected boolean contains(int[] haystack, int needle)
    {
        for(int i : haystack)
        {
            if(i == needle)
            {
                return true;
            }
        }

        return false;
    }

    protected EtcdClientException unwrap(ExecutionException e)
    {
        Throwable cause = e.getCause();
        if(cause instanceof EtcdClientException)
        {
            return (EtcdClientException) cause;
        }
        return new EtcdClientException("Error executing request", e);
    }
}
