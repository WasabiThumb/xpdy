# Formats
Format extensions for ``xpdy``. Format extensions are versioned
identically to the main ``xpdy`` artifact.

## JSON
JSON support can be enabled with ``xpdy-format-json``

### Usage
```java
JsonElement body = request.body()
        .as(JsonBody.class)
        .json();
```

### Declaration
#### Gradle (Kotlin)
```kotlin
dependencies {
    implementation("io.github.wasabithumb:xpdy-format-json:VERSION")
}
```

#### Gradle (Groovy)
```groovy
dependencies {
    implementation 'io.github.wasabithumb:xpdy-format-json:VERSION'
}
```

#### Maven
```xml
<dependencies>
    <dependency>
        <groupId>io.github.wasabithumb</groupId>
        <artifactId>xpdy-format-json</artifactId>
        <version>VERSION</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```
