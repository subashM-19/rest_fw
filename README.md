# Sentinal REST Framework

Sentinal REST Framework is a lightweight Java framework for exposing Java methods as HTTP APIs using XML-based request configuration.

The framework is designed to keep routing, request parsing, parameter extraction, type conversion, and method invocation outside application business logic.

It is used by the RoadRescue backend as the REST API layer.

---

## Overview

The framework receives an HTTP request, matches it against XML request configuration, extracts the required parameters, converts them into Java types, resolves the configured Java class, and invokes the target method.

```text
HTTP Request
     |
     v
Servlet Container
     |
     v
Sentinal REST Framework
     |
     +-- Route Matching
     +-- Parameter Extraction
     +-- Type Conversion
     +-- Bean Resolution
     +-- Method Invocation
     |
     v
Application API Class
```

## Features

- XML-based API configuration
- URI pattern matching
- Regex path parameter extraction
- Request parameter mapping
- Request body parameter mapping
- Form parameter mapping
- Java type conversion
- Java class resolution
- Method invocation
- Servlet-based request handling
- Lightweight architecture
- Reusable across Java web applications

## Project Structure

```text
rest_fw/
├── README.md
├── build.gradle
├── settings.gradle
└── src/
    ├── main/
    │   ├── java/
    │   └── resources/
    └── test/
        └── java/
```

## Requirements

- Java 17
- Gradle
- Javax Servlet API
- Tomcat 9 compatible servlet container

## Build

Run:

```bash
./gradlew clean build
```

## How It Works

Application endpoints are defined using XML configuration.

Each request definition identifies:
- URI
- Java class
- Java method
- Parameters
- Parameter source
- Parameter type

### Example Configuration

```xml
<Requests>
    <Request
            uri="/users/([0-9]+)"
            bean="com.example.api.UserAPI"
            method="getUser">

        <Parameter
                name="user_id"
                source="regex"
                type="long"
                value="1"/>

    </Request>
</Requests>
```

A request such as:

```http
GET /api/users/42
```

is matched against `/users/([0-9]+)`. The value `42` is extracted and passed to the configured Java method.

### Example Java API

```java
package com.example.api;

import java.util.LinkedHashMap;
import java.util.Map;

public class UserAPI
{
    public Map<String, Object> getUser(long userID)
    {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("user_id", userID);
        return response;
    }
}
```

The framework invokes:

```java
getUser(42L);
```

---

## Parameter Sources

The framework supports multiple parameter sources:
- `regex`
- `request`
- `form`
- `body`

### Regex Parameters
Used to extract a value from the request URI.

```xml
<Parameter
        name="user_id"
        source="regex"
        type="long"
        value="1"/>
```

For `/users/42`, the extracted value becomes `42`.

### Request Parameters
Used to read regular request query parameters.

```xml
<Parameter
        name="status"
        source="request"
        type="string"/>
```

Example request: `/api/users?status=active`

### Form Parameters
Used to read form-encoded values.

```xml
<Parameter
        name="name"
        source="form"
        type="string"/>
```

### Body Parameters
Used to extract values from the request payload body.

```xml
<Parameter
        name="latitude"
        source="body"
        type="double"/>
```

Example JSON body:

```json
{
  "latitude": 11.1085,
  "longitude": 77.3411
}
```

---

## Java Type Conversion

The framework converts incoming string values into the Java types required by the configured target method.

```xml
<Parameter
        name="service_request_id"
        source="regex"
        type="long"
        value="1"/>
```

The framework converts the extracted value into a `long` before invoking the application method:

```java
public Map<String, Object> getServiceRequest(long serviceRequestID)
{
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("service_request_id", serviceRequestID);
    return response;
}
```

Application classes do not need to manually parse raw HTTP parameters for normal configured endpoints.

---

## Request Flow

```text
HTTP Request
      |
      v
Servlet
      |
      v
Request Configuration Lookup
      |
      v
URI Match
      |
      v
Parameter Extraction
      |
      v
Type Conversion
      |
      v
Class Resolution
      |
      v
Java Method Invocation
      |
      v
Response
```