#  java-template



##  用到的技术

- spring boot 

  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  ```

- mysql

  ```xml
  <!--  mysql -->
  <dependency>
      <groupId>mysql</groupId>
      <artifactId>mysql-connector-java</artifactId>
      <scope>runtime</scope>
  </dependency>
  ```

- 阿里Druid

  ```xml
  <!-- 阿里系的Druid依赖包 -->
  <dependency>
      <groupId>com.alibaba</groupId>
      <artifactId>druid-spring-boot-starter</artifactId>
      <version>1.1.9</version>
  </dependency>
  ```

- Mybatis

  ```xml
  <dependency>
      <groupId>org.mybatis.spring.boot</groupId>
      <artifactId>mybatis-spring-boot-starter</artifactId>
      <version>2.1.4</version>
  </dependency>
  ```

- Lombok

  ```xml
  <!--  lombok -->
  <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <optional>true</optional>
  </dependency>
  ```

- 二维码工具Zxing

  ```xml
  <!--    二维码生成    -->
  <dependency>
      <groupId>com.google.zxing</groupId>
      <artifactId>core</artifactId>
      <version>3.3.0</version>
  </dependency>
  ```

- hutool

  ```xml
  <!--  hutool  -->
  <dependency>
      <groupId>cn.hutool</groupId>
      <artifactId>hutool-all</artifactId>
      <version>5.5.0</version>
  </dependency>
  ```

- Redis

  ```xml
  <!--集成redis-->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-redis</artifactId>
      <version>1.4.1.RELEASE</version>
  </dependency>
  ```

- JWT

  ```xml
  <!-- jwt  -->
  <dependency>
      <groupId>com.auth0</groupId>
      <artifactId>java-jwt</artifactId>
      <version>3.4.0</version>
  </dependency>
  ```

- Pagehelper

  ```xml
  <!-- https://mvnrepository.com/artifact/com.github.pagehelper/pagehelper -->
  <dependency>
      <groupId>com.github.pagehelper</groupId>
      <artifactId>pagehelper</artifactId>
      <version>5.1.10</version>
  </dependency>
  ```

- Valid参数校验

  ```xml
  <!-- 参数校验 -->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>
  ```

- @ControllerAdvice全局异常捕获

- 统一的错误码机制

- 权限拦截器

- GeneratorConfig脚本生成

- Logback日志

##  模块化开发

- config
  - LoginInterceptor ---登录拦截器
  - RedisConfig ---redis配置
  - WebConfig ---web相关配置

- controller

- dto
  - request ---请求DTO
  - response ---响应DTO

- entity
  - common ---存放响应实体

- enums ---响应枚举类

- exception ---全局异常

- mapper ---mapper接口

- service ---业务相关

- utils ---工具相关

##  功能相关

1. 初始化工程项目，标准模块化开发
2. SQL、Java脚本生成省去大部分开发时间，将时间放到业务开发
3. 拿来即用的权限模块，提供用户-角色-权限基础模块
