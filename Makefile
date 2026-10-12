.DEFAULT_GOAL := help

.PHONY: help test verify up down smoke

help:
	@echo "ShopSphere Phase 0 commands"
	@echo "  make test    Run unit tests"
	@echo "  make verify  Run the Maven quality lifecycle"
	@echo "  make up      Available in Phase 3 (Docker Compose)"
	@echo "  make smoke   Run the local API smoke test"

test:
	mvn -Dmaven.repo.local=.m2 test

verify:
	mvn -Dmaven.repo.local=.m2 verify

up:
	docker compose up --build -d

down:
	docker compose down -v

smoke:
	powershell -ExecutionPolicy Bypass -File scripts/smoke-test.ps1
