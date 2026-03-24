# Proyecto Base Implementando Clean Architecture

## Antes de Iniciar

Empezaremos por explicar los diferentes componentes del proyectos y partiremos de los componentes externos, continuando con los componentes core de negocio (dominio) y por último el inicio y configuración de la aplicación.

Lee el artículo [Clean Architecture — Aislando los detalles](https://medium.com/bancolombia-tech/clean-architecture-aislando-los-detalles-4f9530f35d7a)

# Arquitectura

![Clean Architecture](https://miro.medium.com/max/1400/1*ZdlHz8B0-qu9Y-QO3AXR_w.png)

## Domain

Es el módulo más interno de la arquitectura, pertenece a la capa del dominio y encapsula la lógica y reglas del negocio mediante modelos y entidades del dominio.

## Usecases

Este módulo gradle perteneciente a la capa del dominio, implementa los casos de uso del sistema, define lógica de aplicación y reacciona a las invocaciones desde el módulo de entry points, orquestando los flujos hacia el módulo de entities.

## Infrastructure

### Helpers

En el apartado de helpers tendremos utilidades generales para los Driven Adapters y Entry Points.

Estas utilidades no están arraigadas a objetos concretos, se realiza el uso de generics para modelar comportamientos
genéricos de los diferentes objetos de persistencia que puedan existir, este tipo de implementaciones se realizan
basadas en el patrón de diseño [Unit of Work y Repository](https://medium.com/@krzychukosobudzki/repository-design-pattern-bc490b256006)

Estas clases no puede existir solas y debe heredarse su compartimiento en los **Driven Adapters**

### Driven Adapters

Los driven adapter representan implementaciones externas a nuestro sistema, como lo son conexiones a servicios rest,
soap, bases de datos, lectura de archivos planos, y en concreto cualquier origen y fuente de datos con la que debamos
interactuar.

### Entry Points

Los entry points representan los puntos de entrada de la aplicación o el inicio de los flujos de negocio.

## Application

Este módulo es el más externo de la arquitectura, es el encargado de ensamblar los distintos módulos, resolver las dependencias y crear los beans de los casos de use (UseCases) de forma automática, inyectando en éstos instancias concretas de las dependencias declaradas. Además inicia la aplicación (es el único módulo del proyecto donde encontraremos la función “public static void main(String[] args)”.

**Los beans de los casos de uso se disponibilizan automaticamente gracias a un '@ComponentScan' ubicado en esta capa.**

---

## Consideraciones para Producción

### Claves RSA (CRÍTICO)

Actualmente `SecurityConfig` genera un par de claves RSA **en cada arranque** de la aplicación mediante `KeyPairGenerator`. Esto funciona en desarrollo, pero en producción causa dos problemas graves:

1. **Todos los tokens existentes quedan inválidos** al reiniciar o redesplegar el servicio.
2. **En entornos con múltiples instancias** (escalado horizontal), cada instancia tendrá claves distintas, por lo que un token emitido por la instancia A será rechazado por la instancia B.

**Solución requerida antes de producción:** externalizar las claves RSA a un gestor de secretos. Opciones recomendadas:

- **HashiCorp Vault** (recomendado): almacenar la clave privada como secreto y leerla al arranque con Spring Cloud Vault.
- **AWS Secrets Manager / GCP Secret Manager**: similar al anterior, inyectado como variable de entorno o via Spring Cloud.
- **Java KeyStore (.jks)**: mínimo aceptable, el archivo `.jks` no debe estar en el repositorio. Configurar en `application.yaml`:

```yaml
security:
  jwt:
    key-store: classpath:keystore.jks        # o ruta absoluta externa al jar
    key-store-password: ${KEY_STORE_PASSWORD} # variable de entorno
    key-alias: ms-user-key
```

### Configuración de Base de Datos

El archivo `application.yaml` contiene credenciales de base de datos en texto plano apuntando a `localhost`. Antes de producción:

- Las credenciales deben inyectarse como **variables de entorno** o desde un gestor de secretos.
- Nunca subir al repositorio credenciales de producción.

```yaml
adapters:
  postgresql:
    host: ${DB_HOST}
    port: ${DB_PORT}
    database: ${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### Control de Acceso

Este microservicio **no gestiona autorización de rutas**. Esa responsabilidad recae en el **API Gateway**, que valida el token JWT antes de enrutar las peticiones. Este ms actúa como proveedor de identidad: emite y verifica tokens, pero no protege sus propios endpoints internamente.

### Recuperación de Contraseña

El `RequestPasswordResetUseCase` retorna el token plano en la respuesta para facilitar el desarrollo. En producción este token **debe enviarse únicamente por email** y nunca exponerse en la respuesta HTTP. Integrar con un servicio de envío de correo (SES, SendGrid, etc.) antes del despliegue.

### Expiración de Tokens

Los valores actuales en `application.yaml` son:

```yaml
security:
  jwt:
    access-token-expiration-minutes: 15
    refresh-token-expiration-days: 7
```

Revisar y ajustar según la política de seguridad del proyecto.
