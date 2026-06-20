# TalentFlow — Applicant Tracking System

A console-based ATS built with **Core Java + JDBC + MySQL**. No frameworks. No Spring. Plain Java only.

## Features
- Recruiter: post jobs, view ranked candidates, advance application status, schedule interviews
- Candidate: browse jobs, apply, upload resume (skill map), track application status
- Resume scoring against job requirements (0–100 scale)
- Candidate ranking by skill match, experience as tiebreaker
- Interview queue (FIFO) with auto status advancement on result

## Tech Stack
- Java (Core) — no frameworks
- JDBC — hand-rolled DAO layer
- MySQL — table-per-subclass inheritance mapping
- Eclipse IDE

## Setup
1. Clone the repo
2. Create `src/util/db.properties` from `src/util/db.properties.example`
3. Fill in your MySQL credentials
4. Import into Eclipse as existing Java project
5. Add MySQL JDBC driver to build path
6. Run `src/main/App.java`

## Project Structure
- `model/` — domain entities
- `dao/` — database access layer
- `service/` — business logic
- `util/` — DB connection, validation
- `main/` — console menu driver
