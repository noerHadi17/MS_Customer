# Customer Service

Java 17 Spring Boot service for customer auth and KYC.

- Port: 8081
- Package: `com.wms.customer`
- DB: `jdbc:postgresql://localhost:5432/wms` (postgres/postgress)
- Kafka: `localhost:29092` (produces `audit.events`, consumes `crp.riskprofile.updated`)

## Run

`mvn -DskipTests spring-boot:run`

## cURL

- Check email

```
curl -s http://localhost:8081/v1/user/check-email -H 'Content-Type: application/json' -d '{"email":"john@example.com"}'
```

- Register

```
curl -s http://localhost:8081/v1/auth/register -H 'Content-Type: application/json' -d '{"name":"John","email":"john@example.com","password":"Test1234","address":"Jl. A","dob":"1990-01-01"}'
```

- Login

```
curl -s http://localhost:8081/v1/auth/login -H 'Content-Type: application/json' -d '{"email":"john@example.com","password":"Test1234"}'
```

- Change password (requires `X-User-Id` header)

```
curl -s http://localhost:8081/v1/auth/change-password -H 'Content-Type: application/json' -H 'X-User-Id: <UUID>' -d '{"currentPassword":"Test1234","newPassword":"NewPass123","confirmNewPassword":"NewPass123"}'
```

- KYC status

```
curl -s http://localhost:8081/v1/kyc/status -H 'X-User-Id: <UUID>'
```

- KYC submit

```
curl -s http://localhost:8081/v1/kyc -H 'Content-Type: application/json' -H 'X-User-Id: <UUID>' -d '{"nik":"1234567890123456","address":"Jl. B"}'
```

