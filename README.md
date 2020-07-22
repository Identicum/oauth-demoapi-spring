# oauth-demoapi-spring
Spring OAuth demo api acting as [OAuth2 Resource Server](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#oauth2resourceserver).

Support:
* Opaque Token Introspection with Basic Authentication
* Opaque Token Introspection with Bearer Authentication

## Installation

Clone this repository
```
git clone git@github.com:https://github.com/Identicum/oauth-demoapi-spring.git
```
### Configure

Adjust the [aplication.yml](/src/main/resources/application.yml)

### Run

#### Run locally

```
mvn spring-boot:run
```
You can access to the API on http://hostname:8081/api/v1

#### Run as Docker container
```
docker build -t identicum/oauth-demoapi-spring .
docker run -d --name oauth-demoapi-spring -p xxxx:8081 identicum/oauth-demoapi-spring
```
## OpenAPI definition

```
openapi: 3.0.1
info:
  title: API products
  description: 'OAuth API products'
  version: 1.0.0
  contact:
    name: Martin Besozzi
    url: http://identicum.com
    email: mbesozzi@identicum.com
servers:
- url: https://api.identicum.com/api/v1
tags:
- name: product
  description: Operations about products
paths:
  /products:
    get:
      tags:
      - product
      summary: Get products
      security:
        - bearerOAuth: [api.identicum.com/product:read]
      responses:
        401:
          $ref: '#/components/responses/UnauthorizedError'
        200:
          description: successful operation
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/Product'
components:
  responses:
    UnauthorizedError:
      description: Access token is missing or invalid
  schemas:
    Product:
      type: object
      properties:
        id:
          type: integer
          format: int64
        name:
          type: string
      xml:
        name: Product
  securitySchemes:
    bearerOAuth:
      type: http
      scheme: bearer
      bearerFormat: Opaque Access token
```

## Test API

To test the API, just click [here](https://petstore.swagger.io/?url=https://raw.githubusercontent.com/Identicum/oauth-demoapi-spring/master/src/main/resources/v1-openapi.yml)
