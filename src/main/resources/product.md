# Task Management Application - Product Documentation

## Overview
The Task Management Application is designed to help teams and organizations efficiently manage projects and tasks. It provides a centralized platform for tracking project progress, assigning responsibilities, and collaborating on deliverables. The application is suitable for a wide range of business environments, from small teams to large enterprises.

## Business Purpose
- **Centralized Project & Task Management:** Serves as the single source of truth for all projects and tasks within an organization or team.
- **Transparency & Collaboration:** Enables teams to track progress, assign responsibilities, and collaborate efficiently.
- **Productivity & Accountability:** Helps ensure that tasks are completed on time and that everyone knows their responsibilities.
- **Scalable for Teams:** Supports multiple projects and users, making it suitable for both small and large teams.

## Core Functionalities

### Project Management
- **Create Project:** Add a new project with a name and optional description.
- **List Projects:** View all existing projects.
- **Get Project Details:** See detailed information about a specific project.
- **Update Project:** Edit the name and/or description of an existing project.
- **Delete Project:** Remove a project and all its associated tasks.

### Task Management
- **Create Task:** Add a new task to a project, specifying title, description, and status (e.g., TODO, IN_PROGRESS, DONE).
- **List Tasks:** View all tasks for a given project.
- **Get Task Details:** See detailed information about a specific task.
- **Update Task:** Edit the title, description, or status of an existing task.
- **Delete Task:** Remove a task from a project.
- **Search Tasks by Status:** Find all tasks across all projects with a specific status (e.g., all tasks that are IN_PROGRESS).

---

## Endpoints: Business Operations, Requests, and Responses

### Project Management

#### Create Project
- **Description:** Create a new project.
- **Request:**
  - `name` (string, required): Project name
  - `description` (string, optional): Project description
- **Response:**
  - `id` (number): Unique project ID
  - `name` (string): Project name
  - `description` (string): Project description
  - `tasks` (array): List of tasks (initially empty)

#### List Projects
- **Description:** Retrieve all projects.
- **Request:** _No parameters_
- **Response:** Array of projects, each with:
  - `id` (number)
  - `name` (string)
  - `description` (string)
  - `tasks` (array)

#### Get Project Details
- **Description:** Get details for a specific project.
- **Request:**
  - `id` (number, required): Project ID
- **Response:**
  - `id` (number)
  - `name` (string)
  - `description` (string)
  - `tasks` (array)

#### Update Project
- **Description:** Update a project's name or description.
- **Request:**
  - `id` (number, required): Project ID
  - `name` (string, required): New name
  - `description` (string, optional): New description
- **Response:**
  - `id` (number)
  - `name` (string)
  - `description` (string)
  - `tasks` (array)

#### Delete Project
- **Description:** Delete a project and its tasks.
- **Request:**
  - `id` (number, required): Project ID
- **Response:**
  - `deleted` (number): ID of deleted project

### Task Management

#### Create Task
- **Description:** Add a new task to a project.
- **Request:**
  - `projectId` (number, required): Project ID
  - `title` (string, required): Task title
  - `description` (string, optional): Task description
  - `status` (string, optional): Task status (TODO, IN_PROGRESS, DONE; default: TODO)
- **Response:**
  - `id` (number): Task ID
  - `title` (string)
  - `description` (string)
  - `status` (string)

#### List Tasks
- **Description:** List all tasks for a project.
- **Request:**
  - `projectId` (number, required): Project ID
- **Response:** Array of tasks, each with:
  - `id` (number)
  - `title` (string)
  - `description` (string)
  - `status` (string)

#### Get Task Details
- **Description:** Get details for a specific task.
- **Request:**
  - `id` (number, required): Task ID
- **Response:**
  - `id` (number)
  - `title` (string)
  - `description` (string)
  - `status` (string)

#### Update Task
- **Description:** Update a task's title, description, or status.
- **Request:**
  - `id` (number, required): Task ID
  - `title` (string, required): New title
  - `description` (string, optional): New description
  - `status` (string, optional): New status (TODO, IN_PROGRESS, DONE)
- **Response:**
  - `id` (number)
  - `title` (string)
  - `description` (string)
  - `status` (string)

#### Delete Task
- **Description:** Delete a task from a project.
- **Request:**
  - `id` (number, required): Task ID
- **Response:**
  - `deleted` (number): ID of deleted task

#### Search Tasks by Status
- **Description:** Find all tasks across all projects with a specific status.
- **Request:**
  - `status` (string, required): Status to filter by (TODO, IN_PROGRESS, DONE)
- **Response:** Array of tasks, each with:
  - `id` (number)
  - `title` (string)
  - `description` (string)
  - `status` (string)
  - `projectId` (number)

---

## Intended Users
- **Project Managers:** To create, assign, and track projects and tasks.
- **Team Members:** To view, update, and complete assigned tasks.
- **Business Leaders:** To monitor project progress and team productivity.

## Example Use Cases
- Building a dashboard for project and task tracking.
- Assigning and monitoring tasks within a team.
- Generating reports on project and task completion rates.
- Improving team collaboration and accountability.

---

For further details on workflows and feature usage, refer to the user manual or contact the product team.
