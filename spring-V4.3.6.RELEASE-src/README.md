# spring-V4.3.6.RELEASE-src

#### 项目介绍
spring source study


#### 安装教程

- 将原Spring代码的tag（V4.3.6.RELEASE）按步骤将spring-core/spring-beans/spring-context/spring-express/spring-aop/spring
-instrument导入到项目中
- 参照原Spring根目录的配置文件`build.gradle`处理spring-core子模块，添加对应的依赖，需要特殊处理的是org.objenesis:objenesis:2.5.1和cglib:cglib:3.2
.4，版本可以从配置文件中看到，下载它们的源码，并将其放到spring-core模块的对应目录下，对应关系是
    * cglib包里的"net.sf.cglib.\*" 复制到 "org.springframework.cglib"包下
    * cglib包里的"org.objectweb.asm.\*" 复制到 "org.springframework.asm"包下
    * objenesis包里的"org.objenesis.\*" 复制到 "org.springframework.objenesis"包下
 
 （注意，这三个包下的代码复制到目标文件夹后，会出现所有引用的类名的路径都不正确，需要手动修改成功，我修改这些花了4个小时。其实可以下载spring-core的jar包对应的sources包，这里我不懂jarjar
    这个插件，所以走了弯路，但印象更深刻了。）
- 编写主程序（Spring1st），用以测试。
  
  
  