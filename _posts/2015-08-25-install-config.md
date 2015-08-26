---
layout: page
title: "Install & Config"
category: doc
date: 2015-08-25 21:36:09
order: 1
---

jetcd is available from the [Maven Central Repository](https://search.maven.org)

## Maven

Add the following to the `<dependencies>` section in your `pom.xml`

```xml
<dependency>
    <groupId>com.daedafusion</groupId>
    <artifactId>jetcd</artifactId>
    <version>1.0</version>
</dependency>
```

## Configuration

By default, a client will connect to `localhost` on the default etcd port (4001)

```java
EtcdClient client = EtcdClientFactory.newInstance()
```

If you want to specify an alternate host, port or protocol, provide the entire connection URL

```java
EtcdClient client = EtcdClientFactory.newInstance("https://etcd.mydomain.com:4001")
```
