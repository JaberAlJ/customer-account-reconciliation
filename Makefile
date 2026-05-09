.PHONY: build test run up down db-up db-down clean

build:
	mvn clean package

test:
	mvn test

run:
	mvn spring-boot:run

up:
	docker compose up --build -d

down:
	docker compose down

db-up:
	docker compose up -d postgres

db-down:
	docker compose down

clean:
	mvn clean
