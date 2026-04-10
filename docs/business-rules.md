# Business Rules

## Status
This document is intentionally conservative. It captures only rules that can be inferred from the current code layout and naming. Replace assumptions with confirmed rules as the project evolves.

## Inferred Domain
The project appears to model an education platform with:
- users/accounts
- courses and lessons
- enrollments
- lesson videos and progress
- payments
- course reviews
- admin management for students and accounts

## Confirmed Or Likely Rules
- Users authenticate through Spring Security-backed login flows.
- The application supports multiple locales through message bundles.
- Admin pages exist separately from public pages and should be treated as privileged flows.
- Student management and account management are distinct admin concerns.
- Course content appears hierarchical:
  - course
  - lesson
  - lesson video
- Payment and enrollment status are modeled as enums, so status-driven behavior likely exists and should be preserved when editing related logic.

## Safe Working Assumptions
- Do not rename route parameters, DTO fields, or template model keys without checking all consumers.
- Do not change status semantics in enums without reviewing repository queries, templates, and any conditional UI.
- Do not merge admin and public logic casually; authorization boundaries likely matter.
- Do not assume mock HTML reflects final business rules.
- Do not assume SQL practice files under `sql` are production schema sources.

## Rules To Confirm Before Major Changes
- Account lifecycle and role assignment rules
- Student creation/edit validation rules
- Enrollment eligibility rules
- Payment success/failure/cancel transitions
- Course visibility and publication rules
- Upload authorization and file retention rules

## When Adding New Knowledge
For every clarified business rule, add:
- the rule itself
- where it came from
- which files depend on it

Suggested format:

```md
## Confirmed Rule: Example
- Rule: Only admin users may access `/admin/**`.
- Source: `SecurityConfig`
- Dependent files: `controller/admin/*`, admin templates, login redirect flow
```
