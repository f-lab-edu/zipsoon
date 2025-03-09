.PHONY: help up down db emptydb clean

# .env -> .env.example 순으로 로드
ENV_FILE := $(if $(wildcard .env),.env,.env.example)
include $(ENV_FILE)
export $(shell sed 's/=.*//' $(ENV_FILE))

help:
	@echo "Available commands:"
	@echo "  make up      - 전체 개발 환경(API, 데이터베이스)을 실행합니다."
	@echo "  make down    - 전체 개발 환경을 중지합니다."
	@echo "  make dummydb - 스키마와 테스트 데이터가 존재하는 db 컨테이너를 생성합니다. api를 별도로 실행하고 싶을 때 사용합니다."
	@echo "  make db      - 스키마만 존재하는 db 컨테이너를 생성합니다. batch를 별도로 실행하고 싶을 때 사용합니다."
	@echo "  make clean   - 생성된 모든 컨테이너와 네트워크를 중지하고 삭제합니다."

up:
	@echo "전체 개발 환경을 시작합니다..."
	@cp -f .env.example .env 2>/dev/null || true
	@SPRING_PROFILES_ACTIVE=local docker-compose up -d
	@echo "개발 환경이 실행되었습니다."
	@echo "✅  API 서버: http://localhost:8080"
	@echo "⚠️ Note: 이 컨테이너는 테스트 데이터를 제공하므로, batch 모듈을 실행할 필요가 없습니다."
	@SPRING_PROFILES_ACTIVE=local docker-compose up

down:
	@echo "개발 환경을 중지합니다..."
	@docker-compose down
	@echo "✅  개발 환경이 중지되었습니다."

db:
	@echo "테스트 데이터가 포함된 PostGIS 데이터베이스를 시작합니다..."
	@docker run --name zipsoon-db \
		-e POSTGRES_DB=$(LOCAL_DB_NAME) \
		-e POSTGRES_USER=$(LOCAL_DB_USERNAME) \
		-e POSTGRES_PASSWORD=$(LOCAL_DB_PASSWORD) \
		-v $(PWD)/zipsoon_dump.sql:/docker-entrypoint-initdb.d/01-dumped-data.sql \
		-p 5432:5432 \
		-d postgis/postgis:15-3.4
	@echo "✅  psql 접속 명령어: PGPASSWORD=$(LOCAL_DB_PASSWORD) psql -h localhost -U $(LOCAL_DB_USERNAME) -d $(LOCAL_DB_NAME)"
	@echo "⚠️ Note: 이 컨테이너는 테스트 데이터를 제공하므로, batch 모듈을 실행할 필요가 없습니다."

emptydb:
	@echo "빈 스키마 PostGIS 데이터베이스를 시작합니다..."
	@docker run --name zipsoon-db \
		-e POSTGRES_DB=$(LOCAL_DB_NAME) \
		-e POSTGRES_USER=$(LOCAL_DB_USERNAME) \
		-e POSTGRES_PASSWORD=$(LOCAL_DB_PASSWORD) \
		-v $(PWD)/common/src/main/resources/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql \
		-v $(PWD)/batch/src/main/resources/schema/schema-postgresql.sql:/docker-entrypoint-initdb.d/02-schema-postgresql.sql \
		-p 5432:5432 \
		-d postgis/postgis:15-3.4
	@echo "✅  psql 접속 명령어: PGPASSWORD=$(LOCAL_DB_PASSWORD) psql -h localhost -U $(LOCAL_DB_USERNAME) -d $(LOCAL_DB_NAME)"
	@echo "⚠️ Note: 이 컨테이너는 테스트 데이터를 포함하지 않습니다. batch 모듈을 실행해야 합니다."

clean:
	@echo "데이터베이스 컨테이너를 중지하고 삭제합니다..."
	@docker-compose down -v 2>/dev/null || true
	@docker stop zipsoon-db 2>/dev/null || true
	@docker rm zipsoon-db 2>/dev/null || true
	@docker volume prune -f
	@echo "✅  삭제 완료."