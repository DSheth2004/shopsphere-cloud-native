# Git workflow

ShopSphere uses short-lived feature branches and pull requests.

## Branches

- `main` is always releasable and protected.
- Start work from `main` using `feature/<short-description>` (for example, `feature/auth-login`).
- Use `fix/<short-description>` for bug fixes and `docs/<short-description>` for documentation-only changes.

## Daily workflow

1. Create a branch from the latest `main`.
2. Make small, focused commits using Conventional Commits, such as `feat(auth): add login endpoint`.
3. Push the branch and open a pull request.
4. Ensure CI, tests, security scans, and review pass before merging with squash merge.

## Commit convention

Use `type(scope): summary`. Allowed types include `feat`, `fix`, `docs`, `test`, `build`, `ci`, `refactor`, and `chore`. Keep the summary imperative and under 72 characters.

## Releases

After a stable demo milestone, tag `main` using semantic versioning, for example `v1.0.0`. Release notes should identify the included labs, user-visible changes, known limitations, and deployment instructions.

## Branch protection

Configure GitHub to require a pull request, one approval, passing CI/security checks, and an up-to-date branch before changes reach `main`. Direct pushes and force pushes to `main` should be disabled.
