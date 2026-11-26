# PetShop Huellitas · Backend

Backend de la tienda PetShop Huellitas. El frontend consulta el API Gateway, que reenvía las peticiones a los microservicios de Usuarios, Catálogo, Pedidos y Pagos. Cada uno administra su propia base de datos MySQL.

## Microservicios

| Módulo | Puerto | Base de datos | Rutas principales |
|---|---:|---|---|
| `codigoms_gateway` | 8080 | | Entrada del frontend y estado en `/actuator/health` |
| `codigosms_usuario` | 8090 | `usuarios_db` | `/api/users` |
| `codigoms_catalogo` | 8082 | `catalogo_db` | `/api/v1/products`, `/api/v1/categories` |
| `codigoms_pedidos` | 8084 | `pedidos_db` | `/api/orders` |
| `codigoms_pagos` | 8091 | `pagos_db` | `/api/payments`, `/api/webhooks` |

Pedidos consulta Catálogo para validar y ajustar el stock. Pagos consulta Pedidos para obtener el total y notifica el resultado. La pasarela de pagos es simulada.

## Requisitos

- JDK 25 para levantar todos los módulos. Catálogo lo declara en su `pom.xml`. Los otros módulos declaran Java 17.
- Maven disponible como `mvn` y MySQL encendido. La conexión predeterminada usa `localhost:3306`.
- Un usuario de MySQL con permisos sobre las cuatro bases.

Crea las bases si todavía no existen. Hibernate crea o actualiza las tablas al arrancar mediante `ddl-auto=update`.

```sql
CREATE DATABASE IF NOT EXISTS usuarios_db CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS catalogo_db CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS pedidos_db CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS pagos_db CHARACTER SET utf8mb4;
```

## Configuración local

En la copia completa del proyecto, el lanzador lee el `.env` que está **junto a `iniciar-huellitas.ps1`**, un nivel por encima de este README. Si falta, copia el `.env.example` de esa carpeta a `.env` y completa los valores locales.

| Variable | Uso |
|---|---|
| `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD` | Credenciales de MySQL. La contraseña puede quedar vacía si la cuenta local está configurada así. |
| `HUELLITAS_JWT_SECRET` | Clave de firma de Usuarios. Debe contener al menos 32 bytes codificados en Base64. |
| `HUELLITAS_INTERNAL_KEY` | Clave compartida por Usuarios, Catálogo, Pedidos y Pagos para llamadas internas. |
| `HUELLITAS_DB_USUARIOS_URL`, `HUELLITAS_DB_CATALOGO_URL`, `HUELLITAS_DB_PEDIDOS_URL`, `HUELLITAS_DB_PAGOS_URL` | URLs JDBC opcionales. Si quedan vacías, se usan las bases locales de la tabla anterior. |
| `HUELLITAS_USERS_URL`, `USERS_SERVICE_URL`, `CATALOG_SERVICE_URL`, `ORDERS_SERVICE_URL` | URLs internas. Los valores predeterminados corresponden a los puertos de la tabla. |

Si las claves están vacías, el lanzador las genera para ese arranque. Guarda claves propias en el `.env` local para conservarlas entre reinicios. Un cambio de clave JWT requiere volver a iniciar sesión. En Git se comparte únicamente la plantilla `.env.example`.

## Arranque

Con MySQL encendido, ejecuta desde la raíz del proyecto completo:

```powershell
.\iniciar-huellitas.bat
```

El lanzador inicia **Usuarios → Catálogo → Pedidos → Pagos → Gateway**, espera la disponibilidad de cada uno y muestra la carpeta de logs. Para detenerlos, ejecuta `detener-huellitas.bat`. Ambos accesos llaman a sus respectivos scripts de PowerShell.

Si clonaste únicamente este repositorio, arranca cada módulo con `mvn spring-boot:run` desde su carpeta, en terminales separadas y en el mismo orden. Configura las variables de entorno en esas terminales antes de iniciar. Spring Boot lee las variables del proceso. Para sobrescribir la conexión de un módulo al arrancarlo manualmente, configura `SPRING_DATASOURCE_URL` en su terminal.

Usuarios y Pedidos tienen `app.seed.demo.enabled=true` en sus configuraciones locales. Pedidos necesita que Usuarios y Catálogo hayan terminado de iniciar para preparar sus datos de demostración.

## Autenticación y prueba en Postman

1. Envía `POST http://localhost:8080/api/users/login` con un JSON que incluya `email` y `password` de tu cuenta de prueba.
2. Copia el campo `accessToken` de la respuesta. En Postman, elige **Authorization → Bearer Token** y pega el token.
3. Ejecuta `GET http://localhost:8080/api/users/session`. Con una sesión válida, devuelve el ID y el rol. Sin token o con uno inválido, devuelve `401`.
4. Con una cuenta `CUSTOMER`, consulta `GET /api/orders/user/<tu-id>` para ver tus pedidos. Prueba `GET /api/users` con la misma cuenta para comprobar que devuelve `403`.

Usuarios firma el JWT con vigencia de una hora y verifica que la cuenta siga activa y conserve su rol. Catálogo, Pedidos y Pagos consultan esa validación y aplican sus permisos. El catálogo público y el listado de regiones para el registro están disponibles para invitados. Las llamadas internas autorizadas usan la clave compartida en `X-Huellitas-Internal`.

Las contraseñas de usuarios se guardan en texto plano en esta versión académica. Usa cuentas de prueba.

## Pruebas

Desde la carpeta del backend, ejecuta las pruebas de validación y del catálogo territorial:

```powershell
mvn -f codigosms_usuario/pom.xml test
mvn -f codigoms_catalogo/pom.xml test
```

Las diez pruebas de la página se ejecutan por separado con `npm test` desde `petshop`.
