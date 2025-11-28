# Issue Fixed: Circular Reference in JSON Serialization

## Problem
When running the HTTP request examples in `complete-workflow.http`, you encountered errors due to a **circular reference** between the `Project` and `Task` entities during JSON serialization.

### What Was Happening
- `Project` entity has a `List<Task> tasks` field
- `Task` entity has a `Project project` field
- When Spring tries to serialize a Project to JSON, it includes the tasks
- Each task includes the project reference
- The project includes tasks again... creating an infinite loop
- This causes either a `StackOverflowError` or JSON serialization error

## Solution Applied

Added Jackson annotations to handle the bidirectional relationship:

### 1. In `Project.java`
Added `@JsonManagedReference` to the tasks field:
```java
@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
@JsonManagedReference
private List<Task> tasks;
```

### 2. In `Task.java`
Added `@JsonBackReference` to the project field:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "project_id")
@JsonBackReference
private Project project;
```

## How It Works

- **`@JsonManagedReference`**: The "forward" part of the reference - will be serialized normally
- **`@JsonBackReference`**: The "back" part of the reference - will be omitted from serialization

This breaks the circular reference while maintaining the JPA relationship.

## Result

Now when you fetch a Project:
```json
{
  "id": 1,
  "name": "E-Commerce Platform",
  "description": "...",
  "tasks": [
    {
      "id": 1,
      "title": "Setup environment",
      "description": "Install tools",
      "status": "TODO"
      // Note: NO "project" field here!
    }
  ]
}
```

And when you fetch a Task directly:
```json
{
  "id": 1,
  "title": "Setup environment",
  "description": "Install tools",
  "status": "TODO"
  // Note: NO "project" field here either!
}
```

## Testing

All HTTP request examples in `src/main/resources/events/` now work correctly:
- ✅ `project-requests.http`
- ✅ `task-requests.http`
- ✅ `complete-workflow.http`
- ✅ `error-scenarios.http`

## How to Use

1. Start the application:
   ```bash
   mvn spring-boot:run
   ```

2. Open any `.http` file in IntelliJ IDEA

3. Click the green ▶️ arrow next to any request

4. View the response - no more circular reference errors!

## Alternative Solutions (Not Used)

Other ways to solve this problem:
1. Use `@JsonIgnore` on one side (but loses bidirectional info)
2. Use `@JsonIgnoreProperties` with specific properties
3. Use DTOs (Data Transfer Objects) to separate DB entities from API responses
4. Use `@JsonIdentityInfo` for more complex scenarios

We chose `@JsonManagedReference` / `@JsonBackReference` because:
- Simple and clear
- Specific to parent-child relationships
- Recommended by Spring documentation
- No need for additional DTO classes

## Date Fixed
November 28, 2025

