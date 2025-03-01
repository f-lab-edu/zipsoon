/**
 * SQL 쿼리 정의
 * 각 인터랙션에 대한 SQL 쿼리와 파라미터 처리 방법을 정의합니다.
 */
const SQL_QUERIES = {
  findAllInViewport: {
    query: `SELECT
  id,
  estate_type as type,
  estate_name as name,
  trade_type,
  price,
  area_meter as area,
  ST_Y(location) as latitude,
  ST_X(location) as longitude
FROM
  estate_snapshot
WHERE
  ST_Y(location) BETWEEN {swLat} AND {neLat}
  AND ST_X(location) BETWEEN {swLng} AND {neLng}
ORDER BY
  created_at DESC
LIMIT 100`,

    // 쿼리 파라미터 변환
    paramFormatter: (viewport) => ({
      swLat: viewport.sw.lat.toFixed(6),
      swLng: viewport.sw.lng.toFixed(6),
      neLat: viewport.ne.lat.toFixed(6),
      neLng: viewport.ne.lng.toFixed(6)
    }),

    // 쿼리 결과 포맷팅
    resultFormatter: (data) => {
      let result = '';
      
      // 오류 응답 처리
      if (data && data.code && data.message) {
        result = `--- Response Info ---\n`;
        result += `Status      | ${data.code}\n`;
        result += `Message     | ${data.message}\n`;
        if (data.requestId) {
          result += `Request ID  | ${data.requestId}\n`;
        }
        if (data.timestamp) {
          result += `Timestamp   | ${data.timestamp}\n`;
        }
        return result;
      }
      
      // 배열 형태의 응답 처리
      if (Array.isArray(data)) {
        if (data.length > 0) {
          data.forEach((estate, index) => {
            result += `--- Estate ${index + 1} ---\n`;
            result += `id          | ${estate.id}\n`;
            result += `type        | ${estate.type || estate.estate_type}\n`;
            result += `name        | ${estate.name || estate.estate_name}\n`;
            result += `trade_type  | ${estate.trade_type || estate.tradeType}\n`;
            result += `price       | ${estate.price}\n`;
            result += `area        | ${estate.area || estate.area_meter}\n`;
            result += `latitude    | ${estate.latitude || estate.lat}\n`;
            result += `longitude   | ${estate.longitude || estate.lng}\n\n`;
          });
        } else {
          result = "No properties found in this area.";
        }
        return result;
      }
      
      // 기존 예상 객체 형태 처리 (estates 프로퍼티 확인)
      if (data && data.estates) {
        if (data.estates.length > 0) {
          data.estates.forEach((estate, index) => {
            result += `--- Estate ${index + 1} ---\n`;
            result += `id          | ${estate.id}\n`;
            result += `type        | ${estate.type || estate.estate_type}\n`;
            result += `name        | ${estate.name || estate.estate_name}\n`;
            result += `trade_type  | ${estate.trade_type || estate.tradeType}\n`;
            result += `price       | ${estate.price}\n`;
            result += `area        | ${estate.area || estate.area_meter}\n`;
            result += `latitude    | ${estate.latitude || estate.lat}\n`;
            result += `longitude   | ${estate.longitude || estate.lng}\n\n`;
          });
        } else {
          result = "No properties found in this area.";
        }
        return result;
      }
      
      // 데이터 구조가 예상과 다른 경우
      result = "Unexpected response format.\n";
      result += JSON.stringify(data, null, 2);
      return result;
    },

    // 영향 받는 테이블 목록
    affectedTables: ['estate_snapshot']
  },

  save: {
    query: `INSERT INTO app_user (
  email,
  name,
  image_url,
  email_verified,
  role,
  provider,
  provider_id,
  created_at,
  updated_at
) VALUES (
  {email},
  {name},
  {imageUrl},
  {emailVerified},
  {role},
  {provider},
  {providerId},
  NOW(),
  NOW()
) RETURNING id`,

    // 쿼리 파라미터 변환
    paramFormatter: (userData) => ({
      email: `'${userData.email}'`,
      name: `'${userData.name}'`,
      imageUrl: `NULL`,
      emailVerified: `TRUE`,
      role: `'USER'`,
      provider: `'local'`,
      providerId: `NULL`
    }),

    // 쿼리 결과 포맷팅
    resultFormatter: (data) => {
      console.log('회원가입 응답 데이터:', data);
      
      // AuthToken 형태 확인
      if (data && data.accessToken) {
        return `--- New User Created Successfully ---\n` +
               `Access Token | ${data.accessToken.substring(0, 20)}...\n` +
               `Refresh Token| ${data.refreshToken.substring(0, 20)}...\n` +
               `Expires At   | ${data.expiresAt || 'N/A'}\n` +
               `Token Type   | Bearer`;
      } else if (data && (data.userId || data.id)) {
        // 이전 형식 지원
        const userId = data.userId || data.id;
        return `--- New User Created ---\n` +
               `id          | ${userId}\n` +
               `email       | ${data.email || 'N/A'}\n` +
               `name        | ${data.name || 'N/A'}\n` +
               `provider    | local`;
      } else if (Array.isArray(data) && data.length > 0) {
        // 배열 형태의 응답일 경우
        const user = data[0];
        return `--- New User Created ---\n` +
               `id          | ${user.id || user.userId || 'N/A'}\n` +
               `email       | ${user.email || 'N/A'}\n` +
               `name        | ${user.name || 'N/A'}\n` +
               `provider    | local`;
      } else {
        return "Error creating user. Email might already be in use.";
      }
    },

    // 영향 받는 테이블 목록
    affectedTables: ['app_user']
  },
  
  findUserByEmail: {
    query: `SELECT
  id,
  email,
  name,
  image_url,
  email_verified,
  role,
  provider,
  provider_id,
  created_at,
  updated_at
FROM
  app_user
WHERE
  email = {email}`,

    // 쿼리 파라미터 변환
    paramFormatter: (loginData) => ({
      email: `'${loginData.email}'`
    }),

    // 쿼리 결과 포맷팅
    resultFormatter: (data) => {
      console.log('로그인 응답 데이터:', data);
      
      // AuthToken 형태 확인
      if (data && data.accessToken) {
        return `--- Login Successful ---\n` +
               `Access Token | ${data.accessToken.substring(0, 20)}...\n` +
               `Refresh Token| ${data.refreshToken.substring(0, 20)}...\n` +
               `Expires At   | ${data.expiresAt || 'N/A'}\n` +
               `Token Type   | Bearer`;
      } else if (data && (data.userId || data.id)) {
        // 이전 형식 지원 
        const userId = data.userId || data.id;
        return `--- User Found ---\n` +
               `id          | ${userId}\n` +
               `email       | ${data.email || 'N/A'}\n` +
               `name        | ${data.name || 'N/A'}\n` +
               `role        | ${data.role || 'USER'}\n` +
               `provider    | ${data.provider || 'local'}\n` +
               `created_at  | ${data.createdAt || new Date().toISOString()}\n\n` +
               `[Authentication successful]`;
      } else {
        return "No user found with provided email or authentication failed.";
      }
    },

    // 영향 받는 테이블 목록
    affectedTables: ['app_user']
  }

};