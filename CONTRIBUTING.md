# Contributing to NextBar

Thank you for your interest in contributing to NextBar! This document provides guidelines and instructions for contributing.

---

## Getting Started

1. **Fork** the repository on GitHub
2. **Clone** your fork locally:
   ```bash
   git clone https://github.com/m-ali-moradi/nextBar.git
   cd nextBar
   ```
3. **Set up** the development environment — see [QUICK-START.md](QUICK-START.md)

---

## Branch Naming

Use descriptive, prefixed branch names:

| Prefix | Purpose | Example |
|--------|---------|---------|
| `feature/` | New functionality | `feature/bar-auto-replenish` |
| `fix/` | Bug fixes | `fix/websocket-reconnect` |
| `refactor/` | Code improvements | `refactor/warehouse-dtos` |
| `docs/` | Documentation changes | `docs/api-reference` |
| `test/` | Adding or fixing tests | `test/supply-request-service` |

---

## Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/) format:

```
<type>(<scope>): <description>

[optional body]
```

**Types:** `feat`, `fix`, `refactor`, `docs`, `test`, `chore`, `ci`, `style`

**Scopes:** `bar`, `warehouse`, `droppoint`, `eventplanner`, `users`, `gateway`, `frontend`, `config`, `ci`

**Examples:**
```
feat(bar): add auto-replenishment threshold config
fix(gateway): resolve WebSocket ticket expiry race condition
refactor(warehouse): extract supply fulfillment into dedicated service
docs(readme): update architecture diagram
test(users): add LoginAttemptService unit tests
ci: add Trivy container scanning to build pipeline
```

---

## Pull Request Process

1. **Create a feature branch** from `main`:
   ```bash
   git checkout -b feature/your-feature
   ```

2. **Make your changes** — follow the existing code patterns and conventions

3. **Run tests** before committing:
   ```bash
   # Backend
   mvn test
   
   # Frontend
   cd servers/frontend && npm run lint && npm run test:unit
   ```

4. **Commit** your changes with a descriptive message

5. **Push** to your fork:
   ```bash
   git push origin feature/your-feature
   ```

6. **Open a Pull Request** against the `main` branch

### PR Requirements

- All CI checks pass (Maven verify, frontend lint & build, CodeQL, security gates)
- No new high/critical vulnerabilities introduced
- Tests added or updated for new functionality
- Documentation updated if applicable

---

## Code Style

### Java (Backend)
- Follow existing Spring Boot conventions
- Use Lombok annotations (`@Data`, `@Builder`, `@AllArgsConstructor`, etc.)
- DTOs for all request/response payloads — no entity exposure in controllers
- Mapper classes for entity ↔ DTO conversions
- RabbitMQ events as dedicated event classes in `event/` packages

### TypeScript (Frontend)
- Vue 3 Composition API with `<script setup lang="ts">`
- TanStack Query for all server-state management
- Composables for reusable logic (`useXxx` naming)
- Typed API modules in `src/api/` with shared DTOs in `types.ts`
- Tailwind CSS utility classes for styling

---

## Reporting Issues

When filing an issue, please include:
- A clear description of the problem
- Steps to reproduce
- Expected vs. actual behavior
- Relevant logs or screenshots
- Service name and version (if applicable)

---

## Questions?

If you have questions about the codebase or need help getting started, feel free to open a discussion on GitHub.

**Thank you for contributing!**
