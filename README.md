# oauth-demoapi-spring

Spring OAuth demo API, acting as [OAuth2 Resource Server](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#oauth2resourceserver).


## OpenAPI definition

```
openapi: 3.0.1
info:
  title: Products API
  description: Products API (secured using OAuth)
  version: 1.0.0
servers:
- url: http://demoapi:8081/api/v1
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
        - bearerOAuth: [api.identicum.com/products:read]
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
      bearerFormat: JWT Access token
```

