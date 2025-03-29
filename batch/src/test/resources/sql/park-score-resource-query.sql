CREATE TABLE IF NOT EXISTS public.parks (
	관리번호 varchar(255) NULL,
	공원명 varchar(255) NULL,
	공원구분 varchar(255) NULL,
	소재지도로명주소 varchar(255) NULL,
	소재지지번주소 varchar(255) NULL,
	위도 float8 NULL,
	경도 float8 NULL,
	공원면적 float8 NULL,
	공원보유시설_운동시설 text NULL,
	공원보유시설_유희시설 text NULL,
	공원보유시설_편익시설 text NULL,
	공원보유시설_교양시설 text NULL,
	공원보유시설_기타시설 text NULL,
	지정고시일 date NULL,
	관리기관명 varchar(255) NULL,
	전화번호 varchar(50) NULL,
	데이터기준일자 date NULL,
	제공기관코드 varchar(50) NULL,
	제공기관명 varchar(255) NULL
--	location geometry(Point, 4326) NULL     // ParkSourceCollector.preprocess()에 의해 추가 생성
);