up:
	docker compose -f deploy/docker-compose.yml up

volume ?= false
.PHONY: down
down:
ifeq ($(volume),true)
	docker compose -f deploy/docker-compose.yml down -v
else
	docker compose -f deploy/docker-compose.yml down
endif

update:
	docker compose -f deploy/docker-compose.yml down
	docker compose -f deploy/docker-compose.yml up --build

build:
	docker build -t ghcr.io/zenfulcode/commercify:$(tag) .

push:
	docker build -t ghcr.io/zenfulcode/commercify:dev .
	docker push ghcr.io/zenfulcode/commercify:dev