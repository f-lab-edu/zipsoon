.PHONY: local down clean localdb

local:
	docker-compose --env-file .env.local -f docker-compose.yml -f docker-compose.local.yml build --no-cache
	docker-compose --env-file .env.local -f docker-compose.yml -f docker-compose.local.yml up

# prod:
# 	docker-compose --env-file .env.prod -f docker-compose.yml -f docker-compose.prod.yml up

down:
	docker-compose -f docker-compose.yml -f docker-compose.local.yml down -v

clean: down
	docker volume rm $$(docker volume ls -q | grep zipsoon) 2>/dev/null || true
	docker system prune -f

localdb:
	docker-compose --env-file .env.local -f docker-compose.yml -f docker-compose.local.yml up -d db