CREATE TABLE IF NOT EXISTS public.dongcodes (
	법정동코드 varchar(255) PRIMARY KEY,
	법정동명 varchar(255) NOT NULL,
	폐지여부 varchar(50) NOT NULL
);