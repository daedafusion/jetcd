# jetcd - Java client for etcd

[![Build Status](https://travis-ci.org/daedafusion/jetcd.svg?branch=master)](https://travis-ci.org/daedafusion/jetcd)

[![Coverage Status](https://coveralls.io/repos/github/daedafusion/jetcd/badge.svg?branch=master)](https://coveralls.io/github/daedafusion/jetcd?branch=master)

This is a basic client for connecting to etcd.

## Maven

```xml
<dependency>
    <groupId>com.daedafusion</groupId>
    <artifactId>jetcd</artifactId>
    <version>1.0</version>
</dependency
```

```xml
<dependency>
    <groupId>com.daedafusion</groupId>
    <artifactId>jetcd</artifactId>
    <version>1.1-SNAPSHOT</version>
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