INSERT INTO estate_snapshot (
    id,
    platform_type,
    platform_id,
    raw_data,
    estate_name,
    estate_type,
    trade_type,
    price,
    rent_price,
    area_meter,
    area_pyeong,
    location,
    address,
    tags,
    dong_code,
    created_at
) VALUES
(
    1,
    '네이버',
    'TEST1',
    '{"articleNo":"TEST1","articleName":"독립문역 신축아파트","tradeTypeName":"매매"}',
    '독립문역 도보 1분 신축아파트',
    '아파트',
    '매매',
    '1000000000',
    null,
    '84.93',
    '25.7',
    ST_GeomFromText('POINT(126.96 37.572)', 4326),
    '서울특별시 종로구 독립문로 어딘가',
    ARRAY['역세권','신축'],
    '1111018000',
    '2024-02-02 12:00:00'
),
(
    2,
    '네이버',
    'TEST2',
    '{"articleNo":"TEST2","articleName":"경복궁역 오피스텔","tradeTypeName":"월세"}',
    '경복궁역 역세권 오피스텔',
    '오피스텔',
    '월세',
    '200000000',
    '1500000',
    '23.14',
    '7.0',
    ST_GeomFromText('POINT(126.97 37.576)', 4326),
    '서울특별시 종로구 경복궁 근처',
    ARRAY['역세권','풀옵션'],
    '1111018000',
    '2024-02-02 12:00:00'
);