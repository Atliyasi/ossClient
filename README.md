## 介绍
本项目是个人开发的一个OSS服务的客户端，可以通过对应的API上传文件，然后会返回一个链接给你进行使用，通过该链接可以直接访问上传的对应文件。
项目服务网站为：[https://chdclouds.com/](https://chdclouds.com/welcome/object_storage)
项目完整介绍参考：[https://atliyasi.cn](https://atliyasi.cn/pages/1182f3/)
## 如何使用
现在已经将依赖同步到Maven中央仓库中，如要使用，需在pom中添加以下依赖：
```xml
<dependency>
    <groupId>io.github.atliyasi</groupId>
    <artifactId>ossClient</artifactId>
    <version>1.1</version>
</dependency>
```
按以下步骤进行使用：

1. 获取OSSClient对象

```java
OSSClient ossClient = OSSClient.getClient();
```

2. 调用文件上传方法

```java
MultipartFile file = new MultipartFile();
String s = ossClient.uploadBytes("your-bucketName", "your-userId", file.getBytes(), file.getName());
File testFile = new File("");
String s1 = ossClient.uploadFile("your-bucketName", "your-userId", testFile);
String s2 = ossClient.uploadFile("your-bucketName", "your-userId", "file-URL");
```

3. 解析返回值

```java
String link = ossClient.parseAndHandleResponse(s).get("data");
```

