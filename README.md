# xpdy
Simple annotation-powered web app engine for Java

## Quick Start
### Declaration
#### Gradle (Kotlin)
```kotlin
dependencies {
    implementation("io.github.wasabithumb:xpdy:0.1.0")
}
```

#### Gradle (Groovy)
```groovy
dependencies {
    implementation 'io.github.wasabithumb:xpdy:0.1.0'
}
```

#### Maven
```xml
<dependencies>
    <dependency>
        <groupId>io.github.wasabithumb</groupId>
        <artifactId>xpdy-format-json</artifactId>
        <version>0.1.0</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```


### Initialization
Note that the server is typically a daemon, meaning it will stop if the program
is allowed to exit.
```java
XpdyServer server = XpdyServer.builder()
    .port(9739)
    .build();

server.registerEndpoints(SampleEndpoints.class);
server.start();
```

### Endpoints
```java
class SampleEndpoints implements Endpoints {
    
    @EndpointInject
    private XpdyServer server;
    
    @Get("/")
    Response root(Request request) {
        return Response.builder()
                .body(Body.text("Hello world!"))
                .build();
    }
    
}
```

## Advanced Features

### Exceptions
Exceptions may bubble out of endpoint methods and are converted to HTTP error response status codes
by the exception handler. By default, the exception handler has a rule for ``ServeException``
which causes the status code stored on the exception to be used. Therefore, throwing ``ServeException``
at any point is a useful way to terminate endpoint execution.

### Batch Processing
The ``Endpoints`` interface has overloads to allow performing some logic for ALL requests. Below is an
example where the user must have a session token in order to use authenticated APIs.
```java
class AuthenticatedEndpoints implements Endpoints {

    @Override
    public void beforeEach(@NotNull EndpointContext ctx) {
        Cookies cookies = ctx.getRequest().cookies();
        String sessionToken = cookies.getValueAssert("_session", true);
        ctx.set(String.class, sessionToken);
    }

    @Post("/api/stuff")
    void postStuff(@NotNull Request request, @NotNull String sessionToken) {
        // ...
    }

}
```
Adding the session token to the ``EndpointContext`` allows it to be passed to the endpoint methods.
Every method parameter is provided by the ``EndpointContext``, including the ``Request``.

### Extra Formats
Extra body format extensions are supported; currently only JSON (``xpdy-format-json``) is implemented.
To coerce a request body into a given format:
```java
Foo foo = request.body()
        .as(FooBody.class)
        .foo();
```

To create a response body in the given format:
```java
FooBody.of(foo);
```

## License
```text
Copyright 2025 Wasabi Codes

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

```