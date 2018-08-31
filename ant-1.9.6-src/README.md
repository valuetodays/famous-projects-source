## ant-1.9.6-src


#### Ver 1.1 [2016-04-20]

To understand the execution process of **ant**, i do something below:

> * Add build.xml in /, and its content is 

    <project name="helloProject" default="hello">
        <target name="hello" >
            <echo message1111="[step1]init..." />
        </target>
    </project>

>  Please ensure the message1111 is an `illegal` word.(i.e., you should not use `message`).

> * Run the class `org.apache.tools.ant.launch.Launcher`, and I get an error in console:

>    `echo doesn't support the "message1111" attribute`.
    
> * Next, I search (hot-key: ctrl + shift + f) the first key text ` doesn't support the `, and find 22 hints in total.
But, I know ` attribute ` is after the first key text, 
so I found the ONLY hint -- in  Line 407 of class `org.apache.tools.ant.IntrospectionHelper`.

> * The former step locates the method `setAttribute`. I search it in project (hot-key: ctrl + alt + h).
Wow, then i am brought into method `org.apache.tools.ant.RuntimeConfigurable.maybeConfigure()`





#### Ver 1.0 


> * [2016-04-01] project can be runnable now, because it is a maven project. Resource files in src/main/java does not take affect. so:

    copy src/main/java/org/apache/tools/ant/taskdefs/defaults.properties to src/main/resources/org/apache/tools/ant/taskdefs/defaults.properties
    copy src/main/java/org/apache/tools/ant/types/defaults.properties to src/main/resources/org/apache/tools/ant/types/defaults.properties
    copy src/main/java/org/apache/tools/ant/version.txt to src/main/resources/org/apache/tools/ant/version.txt

> * [2016-03-31] use [http://maven.oschina.net](http://maven.oschina.net/) to download maven jars.
> * [2016-03-28] make ant-src non-error compilation. Gather jars by file %apache-src%/lib/libraries.properties.

> * [2016-03-28] make ant-src non-error compilation. Gather jars by file %apache-src%/lib/libraries.properties.

> * And some jars can not be downloaded from [mvnrepository](http://mvnrepository.com/):

>> - jai_codec.jar
>> - jai_core.jar
>> - NetRexxC.jar
>> - which-1.0.jar
