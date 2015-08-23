# jetcd - Java client for etcd

[![Build Status](https://travis-ci.org/daedafusion/jetcd.svg?branch=master)](https://travis-ci.org/daedafusion/jetcd)

This is a basic client for connecting to etcd.

## Maven

```xml
<dependency>
    <groupId>com.daedafusion</groupId>
    <artifactId>jetcd</artifactId>
    <version>1.0</version>
</dependency
```

## Examples

```java

EtcdClient client = EtcdClientFactory.newInstance() // http://localhost:4001

EtcdResult result = client.set("/foobar", "test")

result = client.get(key);
assertThat(result.getNode().getValue(), is("hello"));
```

## Supported Features

* set/get
* delete
* set w/ ttl
* refresh
* create/list/delete directory
* cas/cad
* watch