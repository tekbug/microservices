ATHENA: A WEB-BASED VIRTUAL CLASSROOM SERVICE FOR HIGHER EDUCATIONS
=
## OVERVIEW
Athena is a web-based  virtual classroom service built on a robust and scalable distributed microservice architecture. It provides a comprehensive tools and functionalities to facilitate online learning and collaboration for higher education institutes in the country.
This is the BACKEND ONLY for the application.

## FEATURES
- Athena has all the fundamental and scalable tools that enable students and instructors to create and participate in a virtual classroom experiences.
- Athena enables students to work on different projects, brainstorm ideas, and share relevant documents within one platform without ever needing to leave. This makes collaboration and teamwork with peers easy and engaging.
- Athena provides a group-focused, tailored learning experience to fit the user's needs. This enables access to different curated course materials anytime,  anywhere, and fosters a deeper connection with instructors and peers.

SERVICE ENDPOINTS
-
This section outlines the endpoint mapping and structure for each microservice in our system. Each service is designed to handle a specific set of functionalities, contributing to our approach of modular and scalable architecture.

ENDPOINTS
-
These endpoints are designed to facilitate communication between different part of the application, allowing for a clear separation of concerns and easier maintenance. Each service operates independently, focusing on its designated tasks and data management.

Most interactions with these services are conducted via HTTP requests, adhering to RESTful principles wherever possible. When needed, the services uses an event-driven architecture method of communication where they publish and subscribe to each other's events.

VERSIONING
-
This is the second version of the application with enhanced look adding observability, monitoring, and other useful backend and infra components to it.

## GENERAL GUIDELINES

*   **Microservice Architecture**: The system employs a microservice architecture, where each service is responsible for a specific domain of functionality. This allows for independent deployment, scaling, and development of individual components.

*   **Base URLs**: Every endpoint is prefixed with `/api/v2/{service-name}`, for example `/api/v2/users` for the Users Service. This prefix helps route requests to the correct service. The `v2` indicates the version for the API, and when it is moved to production, the prefix will just be `{service-name}`, for example `/users` for Users Service.

*   **Authentication**: Security is paramount. All API endpoints are protected and require a valid access token for authentication and authorization. The token must be included in the `Authorization` header of each request: `Authorization: Bearer {token}`. This ensures that only authenticated and authorized users or services can access the API.

*   **HTTP Methods**: The API leverages standard HTTP methods (GET, POST, PUT, DELETE, PATCH) to perform basic and advanced CRUD (Create, Read, Update, Delete) operations on resources.

    *   `GET`: Retrieves data from the server.
    *   `POST`: Creates a new resource on the server.
    *   `PUT`: Updates an existing resource completely.
    *   `DELETE`: Deletes a resource from the server.
    *   `PATCH`: Partially updates an existing resource.

*   **Ports**: Each service listens on a specific port, as indicated in its respective section. This ensures that requests are routed to the correct service instance.

COMMUNICATION FLOW
-
### **COMMUNICATION PATTERNS**
Athena employs **both synchronous and asynchronous communication** for efficiency and scalability.

### **SYNCHRONOUS COMMUNICATION**
- External clients communicate with the **API Gateway** via **RESTful HTTP APIs**.
- Microservices communicate internally via **RESTful** for simplicity. This will be updated to **gRPC** protocol in the next versions.

### **ASYNCHRONOUS COMMUNICATION [EVENT-DRIVEN ARCHITECTURE]**
| Event Type                        | Publisher    | Subscribers                                                    | Purpose                                                  |
|-----------------------------------|--------------|----------------------------------------------------------------|----------------------------------------------------------|
| **User Domain**                   |              |                                                                |                                                          |
| `user.created`                    | Users        | Admins, Students, Teachers, Coordinators                       | Propagate new user information to role-specific services |
| `user.updated`                    | Users        | Admins, Students, Teachers, Coordinators                       | Sync profile changes across services                     |
| `user.deleted`                    | Users        | Admins, Students, Teachers, Coordinators, Courses, Enrollments | Remove user references and cascade deletions             |
| `user.role.changed`               | Users        | Admins, Students, Teachers, Coordinators                       | Update role-specific services when a user changes roles  |
| **Admin Domain**                  |              |                                                                |                                                          |
| `admin.system.configured`         | Admins       | All services                                                   | Broadcast system-wide configuration changes              |
| `admin.announcement.created`      | Admins       | Users, Students, Teachers, Coordinators                        | Distribute system-wide announcements                     |
| **Student Domain**                |              |                                                                |                                                          |
| `student.registered`              | Students     | Courses, Enrollments                                           | Make student available for course enrollment             |
| `student.deactivated`             | Students     | Courses, Enrollments, Assignments, Grades, Attendances         | Suspend student activities                               |
| **Teacher Domain**                |              |                                                                |                                                          |
| `teacher.registered`              | Teachers     | Courses, Admins                                                | Make teacher available for course assignment             |
| `teacher.course.assigned`         | Teachers     | Courses, Students, Enrollments                                 | Notify of teacher assignment to course                   |
| `teacher.course.removed`          | Teachers     | Courses, Students, Enrollments                                 | Notify of teacher removal from course                    |
| **Coordinator Domain**            |              |                                                                |                                                          |
| `coordinator.registered`          | Coordinators | Courses, Admins                                                | Make coordinator available for program management        |
| `coordinator.program.assigned`    | Coordinators | Courses, Teachers                                              | Notify of coordinator assignment to program              |
| **Course Domain**                 |              |                                                                |                                                          |
| `course.created`                  | Courses      | Admins, Teachers, Coordinators, Enrollments                    | Make course available in the system                      |
| `course.updated`                  | Courses      | Admins, Teachers, Students, Enrollments                        | Notify of course detail changes                          |
| `course.published`                | Courses      | Students, Teachers, Enrollments                                | Make course available for enrollment                     |
| `course.archived`                 | Courses      | Students, Teachers, Enrollments, Grades, Assignments           | Mark course as no longer active                          |
| `course.content.updated`          | Courses      | Students, Teachers                                             | Notify of new or changed course content                  |
| `course.schedule.updated`         | Courses      | Students, Teachers, Attendances                                | Notify of changes to class schedule                      |
| **Enrollment Domain**             |              |                                                                |                                                          |
| `enrollment.created`              | Enrollments  | Students, Courses, Teachers, Grades, Attendances               | Register student in course and dependent services        |
| `enrollment.dropped`              | Enrollments  | Students, Courses, Teachers, Grades, Attendances               | Remove student from course and dependent services        |
| `enrollment.status.changed`       | Enrollments  | Students, Courses, Teachers, Grades                            | Update enrollment status (active, on leave, etc.)        |
| **Assignment Domain**             |              |                                                                |                                                          |
| `assignment.created`              | Assignments  | Courses, Students, Teachers, Grades                            | Notify of new assignment                                 |
| `assignment.updated`              | Assignments  | Courses, Students, Teachers, Grades                            | Notify of assignment changes                             |
| `assignment.deadline.approaching` | Assignments  | Students                                                       | Remind students of upcoming deadlines                    |
| `assignment.submitted`            | Assignments  | Teachers, Grades                                               | Notify teacher of new submission                         |
| `assignment.feedback.provided`    | Assignments  | Students                                                       | Notify student of feedback                               |
| **Grade Domain**                  |              |                                                                |                                                          |
| `grade.submitted`                 | Grades       | Students, Teachers, Courses                                    | Record new grade and notify relevant parties             |
| `grade.updated`                   | Grades       | Students, Teachers, Courses                                    | Notify of grade changes                                  |
| `grade.finalized`                 | Grades       | Students, Teachers, Courses, Admins                            | Lock in final grades for a course                        |
| `grade.calculated`                | Grades       | Students, Teachers, Courses                                    | Update overall course grade                              |
| **Attendance Domain**             |              |                                                                |                                                          |
| `attendance.recorded`             | Attendances  | Students, Teachers, Courses                                    | Register student attendance for a session                |
| `attendance.updated`              | Attendances  | Students, Teachers, Courses                                    | Modify existing attendance record                        |
| `attendance.threshold.reached`    | Attendances  | Students, Teachers, Coordinators                               | Alert when student misses significant classes            |

***

DATABASE CHOICES
-

Each microservice has its own dedicated database for **scalability and isolation**.

| Service(s)                                                                                              | Database          | Rationale                                                                  |
|---------------------------------------------------------------------------------------------------------|-------------------|----------------------------------------------------------------------------|
| Users, Admins, Students, Teachers, Grades, Coordinators, Courses, Assignments, Attendances, Enrollments | PostgreSQL        | General-purpose relational database; ACID compliance.                      |
| Notifications                                                                                           | Redis, PostgreSQL | In-memory data store providing very fast read/write operations.            |
| Analytics                                                                                               | ClickHouse        | Column-oriented database optimized for fast analytical queries.            |

***

CACHING STRATEGY
-
Athena uses **Memcached** to cache frequently accessed data, significantly improving performance and reducing database load.

| Cached Data          | Expiry Time | Notes                                                       |
|----------------------|-------------|-------------------------------------------------------------|
| User Sessions        | 30 minutes  | Improves authentication and authorization speed.            |
| Course Lists         | 24 hours    | Reduces load on Course Service for frequent browsing.       |
| Attendance Snapshots | 10 minutes  | Provides near real-time attendance data with minimal delay. |

SERVICE DETAILS
-
The following sections provide detailed information about each service, including its purpose, available endpoints, and expected request/response formats.  This information is crucial for developers integrating with our platform and building applications that leverage these services.

For clarity, the services are categorized into two distinct types:

*   **User-Facing Services:** These services expose APIs directly accessible by users or client applications. They provide the core functionality that users interact with, such as managing user profiles, accessing course content, or submitting assignments. You'll find detailed documentation for the endpoints of these services in their respective sections.
*   **TABLE FOR USER-FACING SERVICES**

| Service              | Description                                                                                  | Port |
|----------------------|----------------------------------------------------------------------------------------------|------|
| User Service         | Manages user profiles and preferences.                                                       | 8093 |
| Teacher Service      | Manages teacher accounts and schedules.                                                      | 8092 |
| Student Service      | Handles student data, progress, and assignments.                                             | 8091 |
| Course Service       | Manages course creation, modules, and enrollments.                                           | 8087 |
| Coordinator Service  | Manages course coordination, coordinators, and other education-related administrative tasks. | 8086 |
| Enrollment Service   | Tracks student course enrollments.                                                           | 8088 |
| Assignment Service   | Handles assignments, and submissions.                                                        | 8083 |
| Classroom Service    | Manages virtual classroom sessions, WebSockets, and RTC.                                     | 8085 |
| Notification Service | Sends system notifications to users.                                                         | 8090 |
| Attendance Service   | Manages attendance records (Mostly event-driven).                                            | 8084 |
| Grade Service        | Manages grades and other related information                                                 | 8089 |
| Admin Service        | Manages admin-related tasks.                                                                 | 8082 |


*   **System Services:** These services provide essential infrastructure and support functions that operate behind the scenes.  They are not directly exposed to end-users and typically lack user-facing endpoints. Examples include the Service Discovery server and the OAuth2 Authentication server. Their primary function is to facilitate the operation and security of the other services. While they don't have public endpoints, understanding their role is critical for comprehending the overall system architecture.

*   **TABLE FOR SYSTEM SERVICES**

| Service                            | Description                                                             | Port             |
|------------------------------------|-------------------------------------------------------------------------|------------------|
| Discovery Service (Eureka)         | Handles service registration and discovery.                             | 8761             |
| OAuth2 Authentication (Keycloak)   | Manages authentication and authorization.                               | 8080             |
| API Gateway (Spring Cloud Gateway) | Routes requests, handles authentication, and enforces rate limiting.    | 8081             |
| Caching (Memcached)                | Speeds up frequently accessed data (e.g., user sessions, course lists). | (See Note Below) |

**Note on Memcached Port:** Memcached typically uses port ***11211*** by default. If your implementation uses a different port, you should replace "(See Note Below)" with the correct port number.

For simplicity, we have used alphabetical order to explain each service's capabilities in the description of **USER-FACING SERVICES** section.

SYSTEM SERVICES
-
### DISCOVERY SERVICE [PORT: 8761]
* This service enables dynamic location of other services within the architecture. It allows microservices to register their location and to discover the locations of other services. This service also performs health checks to ensure service availability.

### OAUTH2 AUTHORIZATION KEYCLOAK SERVER [PORT: 9080]
* This service manages user identities and access control. It allows for user authentication (login/logout), authorization (permission management),  and user management. It also provides security tokens for secure communication between services.

USER-FACING SERVICES
-
### GATEWAY SERVER [PORT: 8081]
* This service acts as the central entry point for all incoming requests. It handles routing requests to the appropriate microservice, authenticating users, relaying tokens, applying rate limits, and transforming requests and responses. It also ensures load balancing for optimized performance.

* #### ROUTING RULES

  The API Gateway uses a URL-based routing strategy. The basic format is:

  `/api/v2/{service-name}/{endpoint}`

  Where:

    *   `/api/v2` is the base API path.
    *   `{service-name}` identifies the target microservice (e.g., `users`, `courses`, `grades`).
    *   `{endpoint}` is the specific endpoint within that microservice.

* #### IMPORTANT CONSIDERATIONS:

    *   **Authentication:** The API Gateway handles authentication and authorization. All requests must include a valid access token in the `Authorization` header.

    *   **Error Handling:**  The API Gateway handles errors from the microservices and returns a consistent error response to the client.

    *   **Versioning:** The `/api/v2` path indicates the API version. Future versions might be introduced with a different version number (e.g., `/api/v3`).

    *   **No Direct Endpoints (Generally):**  The API Gateway typically *doesn't* have its own endpoints for managing resources directly.  It primarily acts as a proxy. However,  it *might* have limited management endpoints for things like:

        *   `/api/v2/gateway/health` (for health checks)
        *   `/api/v2/gateway/routes` (for managing routing rules - potentially only accessible to administrators).  But these are less common.
***
### ADMIN SERVICE [PORT: 8082]
* This service provides administrative functionalities, including user management, analytics, settings configuration, log retrieval, system health monitoring, audit logs, and other maintenance tasks like issue tracking.

* #### LISTS OF AVAILABLE ENDPOINTS

| Method   | Endpoint                 | Description                                                            |
|----------|--------------------------|------------------------------------------------------------------------|
| **GET**  | `/admin/reports`         | Retrieves various system reports (e.g., user activity, usage).         |
| **GET**  | `/admin/users`           | Retrieves a list of all users (potentially with filtering/pagination). |
| **GET**  | `/admin/analytics`       | Retrieves system analytics data (e.g., traffic, performance).          |
| **GET**  | `/admin/logs`            | Retrieves system logs for debugging and monitoring.                    |
| **GET**  | `/admin/system-health`   | Retrieves information about the system's health and status.            |
| **GET**  | `/admin/audit-logs`      | Retrieves audit logs of user actions and system changes.               |
| **POST** | `/admin/settings`        | Updates system-wide settings.                                          |
| **POST** | `/admin/announcements`   | Creates and sends system-wide announcements to users.                  |
| **POST** | `/admin/user-management` | Performs user management actions (e.g., create, update, delete).       |
| **POST** | `/admin/maintenance`     | Triggers maintenance tasks (e.g., database backups, cache clearing).   |

* #### IMPLEMENTATION FLOW
    <details>
    <summary><code>GET</code> <code><b>/admin/logs</b></code> <code>(Retrieve system logs)</code></summary>

  ##### Description

  Retrieves system logs with filtering, pagination, and summary statistics.

  ##### Query Parameters

  > | name      | type     | data type | description                                             |
      > |-----------|----------|-----------|---------------------------------------------------------|
  > | page      | Optional | Integer   | Page number for pagination (default: 1)                 |
  > | limit     | Optional | Integer   | Number of records per page (default: 20)                |
  > | type      | Optional | String    | Filter by log type: `error`, `warning`, `info`, `debug` |
  > | service   | Optional | String    | Filter logs by service name                             |
  > | startDate | Optional | String    | Filter logs from this date (ISO 8601 format)            |
  > | endDate   | Optional | String    | Filter logs until this date (ISO 8601 format)           |
  > | search    | Optional | String    | Search term to filter logs by message content           |

  ##### Response

  > | http code | content-type       | response                                                    |
      > |-----------|--------------------|-------------------------------------------------------------|
  > | `200`     | `application/json` | Success response with logs data and summary                 |
  > | `400`     | `application/json` | `{"message":"Invalid parameters"}`                          |

  ##### Success Response Structure

    ```json
    {
      "data": [
        {
          "id": "string",
          "timestamp": "2024-02-05T12:00:00Z",
          "type": "error",
          "service": "string",
          "message": "string",
          "details": {
            "path": "string",
            "method": "string",
            "statusCode": 500,
            "userId": "string",
            "errorStack": "string",
            "metadata": {}
          },
          "context": {
            "environment": "string",
            "version": "string",
            "host": "string"
          }
        }
      ],
      "summary": {
        "total": 0,
        "byType": {
          "error": 0,
          "warning": 0,
          "info": 0,
          "debug": 0
        },
        "byService": {}
      },
      "pagination": {
        "total": 0,
        "page": 1,
        "limit": 20,
        "hasMore": false
      }
    }
    ```

  ##### Example cURL

    ```bash
    curl -X GET \
      "http://localhost:8080/api/v1/admin/logs?page=1&limit=20&type=error&startDate=2024-02-01T00:00:00Z&endDate=2024-02-05T23:59:59Z" \
      -H "Authorization: Bearer {token}"
    ```

    </details>

    <details>
    <summary><code>POST</code> <code><b>/admin/announcements</b></code> <code>(Create announcement)</code></summary>

  ##### Description

  Creates a new announcement with targeting options and display settings.

  ##### Request Body

  > | name     | type     | data type | description                                     |
      > |----------|----------|-----------|-------------------------------------------------|
  > | title    | Required | String    | Announcement title                              |
  > | content  | Required | String    | Announcement content                            |
  > | priority | Required | String    | Priority level: `high`, `medium`, `low`         |
  > | audience | Required | Object    | Target audience configuration                   |
  > | display  | Required | Object    | Display settings including timing and placement |
  > | actions  | Optional | Array     | Array of action buttons/links to include        |

  ##### Audience Object Structure

  > | name           | type     | data type | description                                                |
      > |----------------|----------|-----------|------------------------------------------------------------|
  > | roles          | Optional | Array     | Target roles: `student`, `teacher`, `coordinator`, `admin` |
  > | departments    | Optional | Array     | Array of department IDs                                    |
  > | courses        | Optional | Array     | Array of course IDs                                        |
  > | specific_users | Optional | Array     | Array of specific user IDs                                 |

  ##### Display Object Structure

  > | name        | type     | data type | description                                         |
      > |-------------|----------|-----------|-----------------------------------------------------|
  > | startDate   | Required | String    | Start date (ISO 8601 format)                        |
  > | endDate     | Optional | String    | End date (ISO 8601 format)                          |
  > | placement   | Required | String    | Display location: `banner`, `popup`, `notification` |
  > | dismissible | Required | Boolean   | Whether users can dismiss the announcement          |

  ##### Response

  > | http code | content-type       | response                                                    |
      > |-----------|--------------------|-------------------------------------------------------------|
  > | `201`     | `application/json` | Success response with created announcement details          |
  > | `400`     | `application/json` | `{"message":"Invalid request parameters"}`                  |

  ##### Success Response Structure

    ```json
    {
      "id": "string",
      "status": "scheduled",
      "recipientsCount": 0,
      "metadata": {
        "createdBy": "string",
        "createdAt": "2024-02-05T12:00:00Z",
        "scheduledFor": "2024-02-05T12:00:00Z"
      }
    }
    ```

  ##### Example cURL

    ```bash
    curl -X POST \
      "http://localhost:8080/api/v1/admin/announcements" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer {token}" \
      -d '{
        "title": "System Maintenance",
        "content": "Scheduled maintenance window on Saturday",
        "priority": "high",
        "audience": {
          "roles": ["student", "teacher"],
          "departments": ["dept_123"]
        },
        "display": {
          "startDate": "2024-02-10T00:00:00Z",
          "endDate": "2024-02-10T23:59:59Z",
          "placement": "banner",
          "dismissible": true
        },
        "actions": [
          {
            "type": "link",
            "label": "Learn More",
            "url": "/maintenance-details"
          }
        ]
      }'
    ```

    </details>

    <details>
    <summary><code>GET</code> <code><b>/admin/users</b></code> <code>(Retrieve users list)</code></summary>

  ##### Description

  Retrieves paginated list of users with filtering, sorting, and summary statistics.

  ##### Query Parameters

  > | name   | type     | data type | description                                                  |
      > |--------|----------|-----------|--------------------------------------------------------------|
  > | page   | Optional | Integer   | Page number for pagination (default: 1)                      |
  > | limit  | Optional | Integer   | Number of records per page (default: 20)                     |
  > | search | Optional | String    | Search term for email, first name, or last name              |
  > | role   | Optional | String    | Filter by role: `student`, `teacher`, `coordinator`, `admin` |
  > | status | Optional | String    | Filter by status: `active`, `inactive`, `suspended`          |
  > | sortBy | Optional | String    | Sort field: `createdAt`, `lastLogin`, `role`                 |
  > | order  | Optional | String    | Sort order: `asc`, `desc` (default: `desc`)                  |

  ##### Response

  > | http code | content-type       | response                                                    |
      > |-----------|--------------------|-------------------------------------------------------------|
  > | `200`     | `application/json` | Success response with users data and summary                |
  > | `400`     | `application/json` | `{"message":"Invalid parameters"}`                          |

  ##### Success Response Structure

    ```json
    {
      "data": [
        {
          "id": "string",
          "email": "string",
          "firstName": "string",
          "lastName": "string",
          "role": "student",
          "status": "active",
          "lastLogin": "2024-02-05T12:00:00Z",
          "accountDetails": {
            "createdAt": "2024-02-05T12:00:00Z",
            "verifiedAt": "2024-02-05T12:00:00Z",
            "suspendedAt": null,
            "loginAttempts": 0
          },
          "permissions": ["string"],
          "metadata": {
            "createdBy": "string",
            "updatedAt": "2024-02-05T12:00:00Z"
          }
        }
      ],
      "summary": {
        "total": 0,
        "active": 0,
        "suspended": 0,
        "byRole": {
          "student": 0,
          "teacher": 0,
          "coordinator": 0,
          "admin": 0
        }
      },
      "pagination": {
        "total": 0,
        "page": 1,
        "limit": 20,
        "hasMore": false
      }
    }
    ```

  ##### Example cURL

    ```bash
    curl -X GET \
      "http://localhost:8080/api/v1/admin/users?page=1&limit=20&role=student&status=active&sortBy=lastLogin&order=desc" \
      -H "Authorization: Bearer {token}"
    ```

    </details>

    <details>
    <summary><code>GET</code> <code><b>/admin/analytics</b></code> <code>(Retrieve system analytics)</code></summary>

  ##### Description

  Retrieves comprehensive system analytics and metrics across various domains including users, courses, engagement, and system performance.

  ##### Query Parameters

  > | name      | type     | data type | description                                                          |
      > |-----------|----------|-----------|----------------------------------------------------------------------|
  > | timeRange | Optional | String    | Analysis period: `daily`, `weekly`, `monthly`, `yearly`              |
  > | startDate | Optional | String    | Start date for custom range (ISO 8601 format)                        |
  > | endDate   | Optional | String    | End date for custom range (ISO 8601 format)                          |
  > | metrics   | Optional | Array     | Array of specific metrics to retrieve                                |

  ##### Response

  > | http code | content-type       | response                                                    |
      > |-----------|--------------------|-------------------------------------------------------------|
  > | `200`     | `application/json` | Success response with analytics data                        |
  > | `400`     | `application/json` | `{"message":"Invalid parameters"}`                          |

  ##### Success Response Structure

    ```json
    {
      "overview": {
        "activeUsers": 0,
        "totalCourses": 0,
        "totalEnrollments": 0,
        "revenue": {
          "total": 0,
          "lastPeriod": 0,
          "growth": 0
        }
      },
      "users": {
        "total": 0,
        "active": 0,
        "newUsers": 0,
        "growth": 0,
        "byRole": {
          "student": 0,
          "teacher": 0,
          "coordinator": 0,
          "admin": 0
        },
        "activityTrend": [
          {
            "period": "string",
            "activeUsers": 0,
            "newRegistrations": 0
          }
        ]
      },
      "courses": {
        "total": 0,
        "active": 0,
        "completed": 0,
        "averageRating": 0,
        "popularCategories": [
          {
            "category": "string",
            "count": 0,
            "enrollment": 0
          }
        ],
        "performance": {
          "completionRate": 0,
          "dropoutRate": 0,
          "averageGrade": 0
        }
      },
      "engagement": {
        "averageSessionDuration": 0,
        "averageCoursesPerStudent": 0,
        "materialAccessRate": 0,
        "assignments": {
          "submitted": 0,
          "completed": 0,
          "averageScore": 0
        }
      },
      "system": {
        "uptime": 0,
        "errorRate": 0,
        "responseTime": 0,
        "storageUsage": {
          "total": 0,
          "used": 0,
          "available": 0
        }
      }
    }
    ```

  ##### Example cURL

    ```bash
    curl -X GET \
      "http://localhost:8080/api/v1/admin/analytics?timeRange=monthly&startDate=2024-01-01T00:00:00Z&endDate=2024-02-05T23:59:59Z" \
      -H "Authorization: Bearer {token}"
    ```

    </details>
***

### ASSIGNMENT SERVICE [PORT: 8083]
* This service manages assignments for courses, including creation, retrieval, updating, and deletion, as well as handling student submissions, grading, and upcoming assignment tracking.

* #### LIST OF AVAILABLE ENDPOINTS

| Method     | Endpoint                           | Description                                              |
|------------|------------------------------------|----------------------------------------------------------|
| **GET**    | `/assignments`                     | Retrieves a list of all assignments.                     |
| **GET**    | `/assignments/{id}`                | Retrieves a specific assignment by its ID.               |
| **GET**    | `/assignments/course/{courseId}`   | Retrieves assignments associated with a specific course. |
| **GET**    | `/assignments/student/{studentId}` | Retrieves assignments assigned to a specific student.    |
| **GET**    | `/assignments/{id}/submissions`    | Retrieves submissions for a specific assignment.         |
| **GET**    | `/assignments/upcoming`            | Retrieves a list of upcoming assignments.                |
| **POST**   | `/assignments`                     | Creates a new assignment.                                |
| **POST**   | `/assignments/{id}/submit`         | Submits a solution for a specific assignment.            |
| **POST**   | `/assignments/{id}/grade`          | Grades a submission for a specific assignment.           |
| **PUT**    | `/assignments/{id}`                | Updates an existing assignment by its ID.                |
| **DELETE** | `/assignments/{id}`                | Deletes a specific assignment by its ID.                 |
* #### IMPLEMENTATION FLOW
---
### ATTENDANCE SERVICE [PORT: 8084]
* This service manages student attendance records for sessions and courses. It allows for creating, retrieving, updating, and deleting attendance records, as well as generating attendance statistics.
* #### LIST OF AVAILABLE ENDPOINTS
| Method     | Endpoint                                     | Description                                                          |
|------------|----------------------------------------------|----------------------------------------------------------------------|
| **GET**    | `/attendance`                                | Retrieves a list of all attendance records.                          |
| **GET**    | `/attendance/{id}`                           | Retrieves a specific attendance record by its ID.                    |
| **GET**    | `/attendance/session/{sessionId}`            | Retrieves attendance records for a specific session (e.g., lecture). |
| **GET**    | `/attendance/student/{studentId}`            | Retrieves attendance records for a specific student.                 |
| **GET**    | `/attendance/course/{courseId}`              | Retrieves attendance records for a specific course.                  |
| **GET**    | `/attendance/statistics/course/{courseId}`   | Retrieves attendance statistics for a specific course.               |
| **GET**    | `/attendance/statistics/student/{studentId}` | Retrieves attendance statistics for a specific student.              |
| **POST**   | `/attendance`                                | Creates a new attendance record.                                     |
| **PUT**    | `/attendance/{id}`                           | Updates an existing attendance record by its ID.                     |
| **DELETE** | `/attendance/{id}`                           | Deletes a specific attendance record by its ID.                      |
* #### IMPLEMENTATION FLOW
***
### CLASSROOM SERVICE [PORT: 8085]
* This service manages virtual classroom sessions, including creating, joining, leaving, and ending sessions, as well as managing settings, participants, chats, recordings, and breakout rooms. It also includes WebSocket endpoints for real-time communication.
* #### LIST OF AVAILABLE ENDPOINTS

| Method     | Endpoint                        | Description                                                            |
|------------|---------------------------------|------------------------------------------------------------------------|
| **GET**    | `/sessions`                     | Retrieves a list of all sessions.                                      |
| **GET**    | `/sessions/{id}`                | Retrieves a specific session by its ID.                                |
| **GET**    | `/sessions/{id}/participants`   | Retrieves a list of participants in a specific session.                |
| **GET**    | `/sessions/{id}/recording`      | Retrieves the recording URL or information for a specific session.     |
| **POST**   | `/sessions`                     | Creates a new session.                                                 |
| **POST**   | `/sessions/{id}/join`           | Allows a user to join a specific session.                              |
| **POST**   | `/sessions/{id}/leave`          | Allows a user to leave a specific session.                             |
| **POST**   | `/sessions/{id}/end`            | Ends a specific session.                                               |
| **POST**   | `/sessions/{id}/chat`           | Sends a chat message to a specific session.                            |
| **POST**   | `/sessions/{id}/breakout-rooms` | Manages breakout rooms within a session.                               |
| **PATCH**  | `/sessions/{id}/settings`       | Updates the settings of a specific session.                            |
| **DELETE** | `/sessions/{id}`                | Deletes a specific session.                                            |
| **WS**     | `/sessions/{id}/rtc`            | WebSocket endpoint for Real-Time Communication (RTC) within a session. |
| **WS**     | `/sessions/{id}/chat`           | WebSocket endpoint for real-time chat within a session.                |
| **WS**     | `/sessions/{id}/whiteboard`     | WebSocket endpoint for real-time whiteboard collaboration.             |
* #### IMPLEMENTATION FLOW
***
### COORDINATOR SERVICE [PORT: 8086]
* This service manages course coordinators and their related data, including assigned courses, assignments, teachers, and department affiliations.
* #### LIST OF AVAILABLE ENDPOINTS

| Method     | Endpoint                                  | Description                                                             |
|------------|-------------------------------------------|-------------------------------------------------------------------------|
| **GET**    | `/coordinators`                           | Retrieves a list of all coordinators.                                   |
| **GET**    | `/coordinators/{id}`                      | Retrieves a specific coordinator by their ID.                           |
| **GET**    | `/coordinators/{id}/courses`              | Retrieves a list of courses managed by a specific coordinator.          |
| **GET**    | `/coordinators/{id}/teachers`             | Retrieves a list of teachers assigned to a specific coordinator.        |
| **GET**    | `/coordinators/department/{departmentId}` | Retrieves coordinators belonging to a specific department.              |
| **POST**   | `/coordinators`                           | Creates a new coordinator.                                              |
| **POST**   | `/coordinators/{id}/assignments`          | Creates a new assignment for courses managed by a specific coordinator. |
| **PUT**    | `/coordinators/{id}`                      | Updates an existing coordinator by their ID.                            |
| **DELETE** | `/coordinators/{id}`                      | Deletes a specific coordinator.                                         |
* #### IMPLEMENTATION FLOW
***
### COURSE SERVICE [PORT: 8087]
* This service manages course information, including course creation, retrieval, updating, and deletion, as well as managing modules, student lists, teacher assignments, course materials, and searching/categorizing courses.
* #### LIST OF AVAILABLE ENDPOINTS

| Method     | Endpoint                           | Description                                                                 |
|------------|------------------------------------|-----------------------------------------------------------------------------|
| **GET**    | `/courses`                         | Retrieves a list of all courses.                                            |
| **GET**    | `/courses/{id}`                    | Retrieves a specific course by its ID.                                      |
| **GET**    | `/courses/{id}/modules`            | Retrieves a list of modules for a specific course.                          |
| **GET**    | `/courses/{id}/students`           | Retrieves a list of students enrolled in a specific course.                 |
| **GET**    | `/courses/{id}/teacher`            | Retrieves information about the teacher of a specific course.               |
| **GET**    | `/courses/category/{category}`     | Retrieves courses belonging to a specific category.                         |
| **GET**    | `/courses/search`                  | Searches for courses based on a query (e.g., name, description).            |
| **POST**   | `/courses`                         | Creates a new course.                                                       |
| **POST**   | `/courses/{id}/modules`            | Adds a new module to a specific course.                                     |
| **POST**   | `/courses/{id}/materials`          | Adds new learning materials to a specific course (e.g., documents, videos). |
| **PUT**    | `/courses/{id}`                    | Updates an existing course by its ID.                                       |
| **PUT**    | `/courses/{id}/modules/{moduleId}` | Updates a specific module within a course.                                  |
| **DELETE** | `/courses/{id}`                    | Deletes a specific course.                                                  |
* #### IMPLEMENTATION FLOW

***
### ENROLLMENT SERVICE [PORT: 8088]
* This service manages student enrollments in courses. It allows for creating, retrieving, updating, and deleting enrollments, as well as managing enrollment status, payments, and generating statistics.
* #### LIST OF AVAILABLE ENDPOINTS

| Method     | Endpoint                                    | Description                                                                    |
|------------|---------------------------------------------|--------------------------------------------------------------------------------|
| **GET**    | `/enrollments`                              | Retrieves a list of all enrollments.                                           |
| **GET**    | `/enrollments/{id}`                         | Retrieves a specific enrollment by its ID.                                     |
| **GET**    | `/enrollments/course/{courseId}`            | Retrieves enrollments for a specific course.                                   |
| **GET**    | `/enrollments/student/{studentId}`          | Retrieves enrollments for a specific student.                                  |
| **GET**    | `/enrollments/statistics/course/{courseId}` | Retrieves enrollment statistics for a specific course (e.g., total enrolled).  |
| **POST**   | `/enrollments`                              | Creates a new enrollment.                                                      |
| **PUT**    | `/enrollments/{id}`                         | Updates an existing enrollment by its ID.                                      |
| **PATCH**  | `/enrollments/{id}/status`                  | Updates the status of a specific enrollment (e.g., active, inactive, pending). |
| **DELETE** | `/enrollments/{id}`                         | Deletes a specific enrollment.                                                 |
* #### IMPLEMENTATION FLOW
***
### GRADE SERVICE [PORT: 8089]
* This service manages student grades for courses. It supports retrieving, creating, updating, and deleting grades, as well as handling grade disputes and generating statistics.
* #### LIST OF AVAILABLE ENDPOINTS

| Method     | Endpoint                                 | Description                                                                     |
|------------|------------------------------------------|---------------------------------------------------------------------------------|
| **GET**    | `/grades`                                | Retrieves a list of all grades.                                                 |
| **GET**    | `/grades/{id}`                           | Retrieves a specific grade by its ID.                                           |
| **GET**    | `/grades/course/{courseId}`              | Retrieves grades for a specific course.                                         |
| **GET**    | `/grades/student/{studentId}`            | Retrieves grades for a specific student.                                        |
| **GET**    | `/grades/statistics/course/{courseId}`   | Retrieves grade statistics for a specific course (e.g., average, distribution). |
| **GET**    | `/grades/statistics/student/{studentId}` | Retrieves grade statistics for a specific student.                              |
| **POST**   | `/grades`                                | Creates a new grade.                                                            |
| **POST**   | `/grades/{id}/dispute`                   | Creates a dispute for a specific grade.                                         |
| **PUT**    | `/grades/{id}`                           | Updates an existing grade by its ID.                                            |
| **PUT**    | `/grades/{id}/resolve`                   | Resolves a dispute for a specific grade.                                        |
| **DELETE** | `/grades/{id}`                           | Deletes a specific grade.                                                       |
* #### IMPLEMENTATION FLOW
***
### NOTIFICATION SERVICE [PORT: 8090]
* This service manages notifications for users. It allows for creating, retrieving, updating (read status), and deleting notifications, as well as managing notification preferences and sending bulk notifications.
* #### LIST OF AVAILABLE ENDPOINTS

| Method     | Endpoint                              | Description                                                          |
|------------|---------------------------------------|----------------------------------------------------------------------|
| **GET**    | `/notifications`                      | Retrieves a list of all notifications.                               |
| **GET**    | `/notifications/{id}`                 | Retrieves a specific notification by its ID.                         |
| **GET**    | `/notifications/user/{userId}`        | Retrieves notifications for a specific user.                         |
| **GET**    | `/notifications/unread`               | Retrieves a list of unread notifications.                            |
| **GET**    | `/notifications/preferences/{userId}` | Retrieves notification preferences for a specific user.              |
| **POST**   | `/notifications`                      | Creates a new notification.                                          |
| **POST**   | `/notifications/bulk`                 | Creates multiple notifications in bulk (e.g., for a group of users). |
| **POST**   | `/notifications/templates`            | Manages notification templates (e.g., create, update, delete).       |
| **PATCH**  | `/notifications/{id}/read`            | Marks a specific notification as read.                               |
| **DELETE** | `/notifications/{id}`                 | Deletes a specific notification.                                     |
* #### IMPLEMENTATION FLOW
***
### STUDENT SERVICE [PORT: 8091]
*   This service manages student accounts and related data, including enrolled courses, progress, grades, preferences, and assignments.
* ####  LIST OF AVAILABLE ENDPOINTS

| Method     | Endpoint                     | Description                                                              |
|------------|------------------------------|--------------------------------------------------------------------------|
| **GET**    | `/students`                  | Retrieves a list of all students.                                        |
| **GET**    | `/students/{id}`             | Retrieves a specific student by their ID.                                |
| **GET**    | `/students/{id}/courses`     | Retrieves a list of courses the student is enrolled in.                  |
| **GET**    | `/students/{id}/progress`    | Retrieves the student's overall progress (e.g., course completion rate). |
| **GET**    | `/students/{id}/grades`      | Retrieves the student's grades.                                          |
| **GET**    | `/students/{id}/assignments` | Retrieves a list of assignments for a specific student.                  |
| **POST**   | `/students`                  | Creates a new student.                                                   |
| **POST**   | `/students/{id}/preferences` | Updates the student's preferences (e.g., notification settings).         |
| **PUT**    | `/students/{id}`             | Updates an existing student by their ID.                                 |
| **DELETE** | `/students/{id}`             | Deletes a specific student.                                              |
* #### IMPLEMENTATION FLOW
***
### TEACHER SERVICE [PORT: 8092]
* This service manages teacher accounts and related information, including courses taught, schedules, ratings, qualifications, and student lists.
* #### LIST OF AVAILABLE ENDPOINTS
| Method     | Endpoint                        | Description                                                      |
|------------|---------------------------------|------------------------------------------------------------------|
| **GET**    | `/teachers`                     | Retrieves a list of all teachers.                                |
| **GET**    | `/teachers/{id}`                | Retrieves a specific teacher by their ID.                        |
| **GET**    | `/teachers/{id}/courses`        | Retrieves a list of courses taught by a specific teacher.        |
| **GET**    | `/teachers/{id}/schedule`       | Retrieves the teaching schedule for a specific teacher.          |
| **GET**    | `/teachers/{id}/ratings`        | Retrieves the ratings and reviews for a specific teacher.        |
| **GET**    | `/teachers/{id}/students`       | Retrieves the list of students of a specific teacher.            |
| **POST**   | `/teachers`                     | Creates a new teacher.                                           |
| **POST**   | `/teachers/{id}/schedule`       | Updates or creates the teaching schedule for a specific teacher. |
| **POST**   | `/teachers/{id}/qualifications` | Updates or adds qualifications for a specific teacher.           |
| **PUT**    | `/teachers/{id}`                | Updates an existing teacher by their ID.                         |
| **DELETE** | `/teachers/{id}`                | Deletes a specific teacher.                                      |
* #### IMPLEMENTATION FLOW
***
### USERS SERVICE [PORT: 8093]
* This service manages user accounts and related data. It handles user creation, retrieval, updating, and deletion, as well as preferences and notifications.
* #### LIST OF AVAILABLE ENDPOINTS

| Method     | Endpoint                    | Description                                                                 |
|------------|-----------------------------|-----------------------------------------------------------------------------|
| **GET**    | `/users`                    | Retrieves a list of all users.                                              |
| **GET**    | `/users/{id}`               | Retrieves a specific user by their ID.                                      |
| **GET**    | `/users/{id}/notifications` | Retrieves the notifications for a specific user.                            |
| **POST**   | `/users`                    | Creates a new user.                                                         |
| **PUT**    | `/users/{id}`               | Updates an existing user by their ID.                                       |
| **PATCH**  | `/users/{id}/preferences`   | Updates the preferences for a specific user (e.g., settings, profile info). |
| **DELETE** | `/users/{id}`               | Deletes a specific user.                                                    |
* #### IMPLEMENTATION FLOW
***
