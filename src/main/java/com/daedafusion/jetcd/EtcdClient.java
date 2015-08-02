package com.daedafusion.jetcd;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.Closeable;

/**
 * Created by mphilpot on 6/23/14.
 */
public interface EtcdClient extends Closeable
{
    EtcdResult get(String key) throws EtcdClientException;
    EtcdResult delete(String key) throws EtcdClientException;

    EtcdResult set(String key, String value) throws EtcdClientException;
    EtcdResult set(String key, String value, Integer ttl) throws EtcdClientException;

    //EtcdResult refreshKey(String key, Integer ttl) throws EtcdClientException;
    EtcdResult refreshDirectory(String key, Integer ttl) throws EtcdClientException;

    EtcdResult createDirectory(String key) throws EtcdClientException;
    EtcdResult createDirectory(String key, Integer ttl) throws EtcdClientException;
    java.util.List<EtcdNode> listDirectory(String key) throws EtcdClientException;
    EtcdResult deleteDirectory(String key) throws EtcdClientException;
    EtcdResult deleteDirectoryRecursive(String key) throws EtcdClientException;

    EtcdResult compareAndSet(String key, String prevValue, String value) throws EtcdClientException;
    EtcdResult compareAndSet(String key, Integer prevIndex, String value) throws EtcdClientException;
    // TODO compareAndSet based on existence

    EtcdResult compareAndDelete(String key, String prevValue) throws EtcdClientException;
    EtcdResult compareAndDelete(String key, Integer prevIndex) throws EtcdClientException;

    ListenableFuture<EtcdResult> watch(String key) throws EtcdClientException;
    ListenableFuture<EtcdResult> watch(String key, boolean recursive) throws EtcdClientException;
    ListenableFuture<EtcdResult> watch(String key, Long index, boolean recursive) throws EtcdClientException;

    EtcdResult queue(String key, String value) throws EtcdClientException;
    EtcdResult getQueue(String key) throws EtcdClientException;
}
