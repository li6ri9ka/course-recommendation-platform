# Платформа рекомендаций курсов

Микросервисная архитектура для платформы онлайн-курсов с системой рекомендаций.

## Архитектура

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   UserService   │     │  CourseService  │     │  RatingService  │
│  (Spring Boot)  │     │  (Spring Boot)  │     │  (Spring Boot)  │
│     :8081       │     │     :8082       │     │     :8083       │
└────────┬────────┘     └────────┬────────┘     └────────┬────────┘
         │                       │                       │
         │                       │              ┌────────▼────────┐
         │                       │              │     Kafka       │
         │                       │              │   (ratings)     │
         │                       │              └────────┬────────┘
         │                       │                       │
         │              ┌────────▼────────┐     ┌────────▼────────┐
         │              │                 │     │ Recommendation  │
         └──────────────►    PostgreSQL   ◄─────│    Service      │
                        │                 │     │  (Ktor/Kotlin)  │
                        └─────────────────┘     │     :8084       │
                                                └─────────────────┘
```

## Технологии

- **Java 17** + **Spring Boot 3.2**
- **Kotlin 1.9** + **Ktor 2.3**
- **PostgreSQL 15**
- **Apache Kafka**
- **Exposed ORM** (для Ktor)
- **JWT** авторизация
- **Docker** + **Docker Compose**

## Микросервисы

### 1. UserService (порт 8081)
Регистрация, авторизация, управление пользователями.

### 2. CourseService (порт 8082)
CRUD операции с курсами.

### 3. RatingService (порт 8083)
Приём оценок и публикация событий в Kafka.

### 4. RecommendationService (порт 8084)
Анализ оценок и персональные рекомендации (Ktor + Kotlin).

## Быстрый старт

### Требования
- Docker и Docker Compose
- JDK 17+ (для локальной разработки)

### Запуск всех сервисов

```bash
cd course-recommendation-platform
docker-compose up --build
```

### Остановка

```bash
docker-compose down
```

### Очистка данных

```bash
docker-compose down -v
```

## API Документация

### UserService (localhost:8081)

#### Регистрация
```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Ответ:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "role": "USER"
}
```

#### Вход
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123"
}
```

#### Получение пользователя
```bash
GET /api/users/{id}
Authorization: Bearer <token>
```

#### Обновление пользователя
```bash
PUT /api/users/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "firstName": "Johnny",
  "lastName": "Doe"
}
```

---

### CourseService (localhost:8082)

#### Создание курса
```bash
POST /api/courses
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Java Spring Boot",
  "description": "Полный курс по Spring Boot",
  "category": "Programming",
  "instructorName": "Ivan Petrov",
  "price": 4999.00,
  "durationHours": 40,
  "level": "INTERMEDIATE",
  "published": true
}
```

#### Получение всех курсов
```bash
GET /api/courses
GET /api/courses?published=true
```

#### Получение курса по ID
```bash
GET /api/courses/{id}
```

#### Поиск курсов
```bash
GET /api/courses/search?keyword=java
```

#### Курсы по категории
```bash
GET /api/courses/category/{category}
```

#### Топ курсов по рейтингу
```bash
GET /api/courses/top-rated
```

#### Все категории
```bash
GET /api/courses/categories
```

#### Обновление курса
```bash
PUT /api/courses/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "price": 3999.00,
  "published": true
}
```

#### Удаление курса
```bash
DELETE /api/courses/{id}
Authorization: Bearer <token>
```

---

### RatingService (localhost:8083)

#### Создание/обновление оценки
```bash
POST /api/ratings
Authorization: Bearer <token>
Content-Type: application/json

{
  "courseId": 1,
  "rating": 5,
  "review": "Отличный курс!"
}
```

#### Получение оценок курса
```bash
GET /api/ratings/course/{courseId}
```

#### Получение статистики курса
```bash
GET /api/ratings/stats/{courseId}
```

**Ответ:**
```json
{
  "courseId": 1,
  "averageRating": 4.5,
  "totalRatings": 10
}
```

#### Мои оценки
```bash
GET /api/ratings/my
Authorization: Bearer <token>
```

#### Удаление оценки
```bash
DELETE /api/ratings/{id}
Authorization: Bearer <token>
```

---

### RecommendationService (localhost:8084)

#### Получение рекомендаций
```bash
GET /api/recommendations/{userId}
```

**Ответ:**
```json
{
  "userId": 1,
  "recommendations": [
    {
      "courseId": 5,
      "score": 0.85,
      "reason": "Рекомендовано на основе похожих пользователей",
      "course": {
        "id": 5,
        "title": "Kotlin для Android",
        "category": "Programming",
        "level": "INTERMEDIATE"
      }
    }
  ]
}
```

#### Пересчёт рекомендаций
```bash
POST /api/recommendations/{userId}/recalculate
```

#### Получение оценок пользователя
```bash
GET /api/recommendations/{userId}/ratings
```

#### Health Check
```bash
GET /health
```

---

## Пример полного сценария

### 1. Регистрация пользователя
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "student1",
    "email": "student1@test.com",
    "password": "password123"
  }'
```

Сохраните токен из ответа.

### 2. Создание курсов (с токеном)
```bash
TOKEN="ваш_токен"

curl -X POST http://localhost:8082/api/courses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Java Basics",
    "category": "Programming",
    "level": "BEGINNER",
    "published": true
  }'

curl -X POST http://localhost:8082/api/courses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Spring Boot Advanced",
    "category": "Programming",
    "level": "ADVANCED",
    "published": true
  }'
```

### 3. Оценка курса
```bash
curl -X POST http://localhost:8083/api/ratings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "courseId": 1,
    "rating": 5,
    "review": "Отличный курс для начинающих!"
  }'
```

### 4. Получение рекомендаций
```bash
curl http://localhost:8084/api/recommendations/1
```

---

## Kafka Topics

| Topic | Producer | Consumer | Описание |
|-------|----------|----------|----------|
| `ratings` | RatingService | RecommendationService | События оценок |

### Формат сообщения
```json
{
  "eventType": "CREATED",
  "ratingId": 1,
  "userId": 1,
  "courseId": 1,
  "rating": 5,
  "review": "Отличный курс!",
  "timestamp": "2024-01-15T10:30:00"
}
```

**eventType**: `CREATED`, `UPDATED`, `DELETED`

---

## Структура проекта

```
course-recommendation-platform/
├── docker-compose.yml
├── init-db.sql
├── README.md
├── user-service/
│   ├── build.gradle
│   ├── settings.gradle
│   ├── Dockerfile
│   └── src/main/java/com/example/userservice/
│       ├── UserServiceApplication.java
│       ├── config/
│       ├── controller/
│       ├── dto/
│       ├── entity/
│       ├── repository/
│       ├── security/
│       └── service/
├── course-service/
│   ├── build.gradle
│   ├── settings.gradle
│   ├── Dockerfile
│   └── src/main/java/com/example/courseservice/
│       ├── CourseServiceApplication.java
│       ├── config/
│       ├── controller/
│       ├── dto/
│       ├── entity/
│       ├── repository/
│       ├── security/
│       └── service/
├── rating-service/
│   ├── build.gradle
│   ├── settings.gradle
│   ├── Dockerfile
│   └── src/main/java/com/example/ratingservice/
│       ├── RatingServiceApplication.java
│       ├── config/
│       ├── controller/
│       ├── dto/
│       ├── entity/
│       ├── kafka/
│       ├── repository/
│       ├── security/
│       └── service/
└── recommendation-service/
    ├── build.gradle.kts
    ├── settings.gradle.kts
    ├── Dockerfile
    └── src/main/kotlin/com/example/recommendationservice/
        ├── Application.kt
        ├── config/
        ├── dto/
        ├── entity/
        ├── kafka/
        ├── repository/
        ├── routes/
        └── service/
```

---

## Переменные окружения

### UserService
| Переменная | Значение по умолчанию |
|------------|----------------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/userdb` |
| `SPRING_DATASOURCE_USERNAME` | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` |
| `JWT_SECRET` | (base64 encoded secret) |

### CourseService
| Переменная | Значение по умолчанию |
|------------|----------------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/coursedb` |
| `JWT_SECRET` | (base64 encoded secret) |

### RatingService
| Переменная | Значение по умолчанию |
|------------|----------------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/ratingdb` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:29092` |
| `JWT_SECRET` | (base64 encoded secret) |

### RecommendationService
| Переменная | Значение по умолчанию |
|------------|----------------------|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/recommendationdb` |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:29092` |
| `COURSE_SERVICE_URL` | `http://course-service:8082` |

---

## Алгоритм рекомендаций

RecommendationService использует **Collaborative Filtering**:

1. **Сбор данных**: Получение всех оценок из Kafka
2. **Поиск похожих пользователей**: Jaccard similarity на основе пересечения оценённых курсов
3. **Генерация рекомендаций**: Курсы, высоко оценённые похожими пользователями
4. **Fallback**: Если данных недостаточно — топ курсов по рейтингу

---

## Тестирование

### Запуск тестов (Spring Boot сервисы)
```bash
cd user-service && ./gradlew test
cd course-service && ./gradlew test
cd rating-service && ./gradlew test
```

### Запуск тестов (Ktor)
```bash
cd recommendation-service && ./gradlew test
```

---

## Мониторинг (опционально)

Для добавления мониторинга можно использовать:
- **Prometheus** + **Grafana**
- **Spring Boot Actuator** (для Spring сервисов)
- **Ktor Metrics** (для Ktor сервиса)

---

## Возможные улучшения

- [ ] Добавить Redis кеширование
- [ ] Реализовать OAuth2/Keycloak
- [ ] Добавить OpenAPI/Swagger документацию
- [ ] Настроить Prometheus + Grafana
- [ ] Добавить Circuit Breaker (Resilience4j)
- [ ] Реализовать API Gateway
- [ ] Добавить распределённую трассировку (Zipkin/Jaeger)

---

## Авторы

# li6ri9ka
