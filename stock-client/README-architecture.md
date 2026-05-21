# Stock Client Architecture Guardrails

## Goals

- Keep module behavior generic and metadata-driven.
- Prevent controller-level one-off patches.
- Keep API path differences centralized.
- Make failures observable (no silent exception swallowing).

## Layering

- `constant`
  - `AppConstants`: shared literal keys and reusable constants.
  - `ModuleEndpointStrategy`: module-to-endpoint method/path strategy.
- `meta`
  - `ModuleMeta`: module schema metadata and dependency rules.
- `service`
  - `ModuleDataService`: CRUD/page/query orchestration through strategy.
  - `TableActionService`: table-level actions and row/batch operation helpers.
  - `DependencyResolver`: form linkage option loading and cascade clear behavior.
  - `UiFeedbackService`: unified user feedback and common notifications.
- `controller`
  - `MainController`, `ModuleFormController`, `LoginController`: event orchestration only.
  - Controllers should not contain module special-cases or API path assembly.

## Extension Playbook

1. Add or change endpoint behavior in `ModuleEndpointStrategy`.
2. Add fields, table columns, relation config, and form linkage rules in `ModuleMeta`.
3. Keep network invocation in service layer (`ModuleDataService` / related services).
4. Keep controllers thin: bind events, call services, update view state.

## Dependency Rules

- Define dependencies via metadata (such as depends-on/source/query key/cascade clear).
- Resolve linkage via `DependencyResolver`.
- Do not hardcode control-id chains in controllers.

## Error Handling Rules

- Never use `catch (Exception ignored)`.
- At minimum, log warning with context: module, field, action, and request key.
- Show user-facing message through `UiFeedbackService` when action is user-triggered.

## Build Guard

- `maven-checkstyle-plugin` runs at `validate`.
- Rule blocks the exact anti-pattern `catch (Exception ignored)`.
- If build fails, fix by explicit handling or contextual logging.

## Anti-Regression Checklist

- No module special-casing in controllers.
- No path normalization patch in business call sites.
- No silent catches.
- New module behavior is configurable through metadata/strategy, not if-else patches.
