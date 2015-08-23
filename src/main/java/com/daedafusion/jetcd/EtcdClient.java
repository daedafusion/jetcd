package com.daedafusion.jetcd;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.Closeable;

/**
 * Created by mphilpot on 6/23/14.
 */
public interface EtcdClient extends Closeable
{
    /**
     * https://coreos.com/etcd/docs/latest/api.html#get-the-value-of-a-key
     *
     * @param key
     * @return
     * @throws EtcdClientException
     */
    EtcdResult get(String key) throws EtcdClientException;

    /**
     * https://coreos.com/etcd/docs/latest/api.html#deleting-a-key
     *
     * @param key
     * @return
     * @throws EtcdClientException
     */
    EtcdResult delete(String key) throws EtcdClientException;

    /**
     * https://coreos.com/etcd/docs/latest/api.html#setting-the-value-of-a-key
     *
     * @param key
     * @return
     * @throws EtcdClientException
     */
    EtcdResult set(String key, String value) throws EtcdClientException;

    /**
     * https://coreos.com/etcd/docs/latest/api.html#using-key-ttl
     *
     * @param key
     * @return
     * @throws EtcdClientException
     */
    EtcdResult set(String key, String value, Integer ttl) throws EtcdClientException;

    EtcdResult refreshDirectory(String key, Integer ttl) throws EtcdClientException;

    /**
     * https://coreos.com/etcd/docs/latest/api.html#creating-directories
     *
     * @param key
     * @return
     * @throws EtcdClientException
     */
    EtcdResult createDirectory(String key) throws EtcdClientException;

    /**
     * https://coreos.com/etcd/docs/latest/api.html#using-a-directory-ttl
     *
     * @param key
     * @param ttl
     * @return
     * @throws EtcdClientException
     */
    EtcdResult createDirectory(String key, Integer ttl) throws EtcdClientException;

    /**
     * https://coreos.com/etcd/docs/latest/api.html#listing-a-directory
     *
     * @param key
     * @return
     * @throws EtcdClientException
     */
    java.util.List<EtcdNode> listDirectory(String key) throws EtcdClientException;

    /**
     * https://coreos.com/etcd/docs/latest/api.html#deleting-a-directory
     *
     * @param key
     * @return
     * @throws EtcdClientException
     */
    EtcdResult deleteDirectory(String key) throws EtcdClientException;

    /**
     * https://coreos.com/etcd/docs/latest/api.html#deleting-a-directory
     *
     * @param key
     * @return
     * @throws EtcdClientException
     */
    EtcdResult deleteDirectoryRecursive(String key) throws EtcdClientException;

    /**
     * https://coreos.com/etcd/docs/latest/api.html#atomic-compare-and-swap
     *
     * @param key
     * @param prevValue
     * @param value
     * @return
     * @throws EtcdClientException
     */
    EtcdResult compareAndSet(String key, String prevValue, String value) throws EtcdClientException;

    /**
     * https://coreos.com/etcd/docs/latest/api.html#atomic-compare-and-swap
     *
     * @param key
     * @param prevIndex
     * @param value
     * @return
     * @throws EtcdClientException
     */
    EtcdResult compareAndSet(String key, Integer prevIndex, String value) throws EtcdClientException;

    /**
     * https://coreos.com/etcd/docs/latest/api.html#atomic-compare-and-delete
     *
     * @param key
     * @param prevValue
     * @return
     * @throws EtcdClientException
     */
    EtcdResult compareAndDelete(String key, String prevValue) throws EtcdClientException;

    /**
     * https://coreos.com/etcd/docs/latest/api.html#atomic-compare-and-delete
     *
     * @param key
     * @param prevIndex
     * @return
     * @throws EtcdClientException
     */
    EtcdResult compareAndDelete(String key, Integer prevIndex) throws EtcdClientException;

    /**
     * https://coreos.com/etcd/docs/latest/api.html#waiting-for-a-change
     *
     * @param key
     * @return
     * @throws EtcdClientException
     */
    ListenableFuture<EtcdResult> watch(String key) throws EtcdClientException;

    /**
     * https://coreos.com/etcd/docs/latest/api.html#waiting-for-a-change
     *
     * @param key
     * @param recursive
     * @return
     * @throws EtcdClientException
     */
    ListenableFuture<EtcdResult> watch(String key, boolean recursive) throws EtcdClientException;

    /**
     * https://coreos.com/etcd/docs/latest/api.html#waiting-for-a-change
     *
     * @param key
     * @param index
     * @param recursive
     * @return
     * @throws EtcdClientException
     */
    ListenableFuture<EtcdResult> watch(String key, Long index, boolean recursive) throws EtcdClientException;

    /**
     * https://coreos.com/etcd/docs/latest/api.html#atomically-creating-in-order-keys
     *
     * @param key
     * @param value
     * @return
     * @throws EtcdClientException
     */
    EtcdResult queue(String key, String value) throws EtcdClientException;

    /**
     * https://coreos.com/etcd/docs/latest/api.html#atomically-creating-in-order-keys
     * 
     * @param key
     * @return
     * @throws EtcdClientException
     */
    EtcdResult getQueue(String key) throws EtcdClientException;
}
