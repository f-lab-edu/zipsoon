/**
 * SQL 쿼리 정의
 * 각 인터랙션에 대한 SQL 쿼리와 파라미터 처리 방법을 정의합니다.
 */
const SQL_QUERIES = {
  findAllInViewport: {
    query: `SELECT * FROM estate_snapshot
WHERE ST_Intersects(
    location,
    ST_MakeEnvelope(
        {swLng},
        {swLat},
        {neLng},
        {neLat},
        4326
    )
)
LIMIT {limit}`,

    // 쿼리 파라미터 변환
    paramFormatter: (viewport) => {
      // 보다 정확한 표현을 위해 6자리 고정 소수점 사용
      const swLat = viewport.sw.lat.toFixed(6);
      const swLng = viewport.sw.lng.toFixed(6);
      const neLat = viewport.ne.lat.toFixed(6);
      const neLng = viewport.ne.lng.toFixed(6);
      
      return {
        swLat: swLat,
        swLng: swLng,
        neLat: neLat,
        neLng: neLng,
        limit: 100
      };
    },

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
  },
  
  findScoresByEstateId: {
    query: `SELECT
  es.id as score_id,
  st.id as score_type_id,
  st.name as score_type_name,
  st.description,
  es.raw_score,
  es.normalized_score
FROM
  estate_score es
JOIN
  score_type st ON es.score_type_id = st.id
WHERE
  es.estate_snapshot_id = {estateId} <!-- n번 반복 -->
ORDER BY
  es.normalized_score DESC`,

    // 쿼리 파라미터 변환
    paramFormatter: (data) => {
      // data가 단일 매물인 경우 또는 매물 목록인 경우 처리
      const estateId = Array.isArray(data) && data.length > 0 ? data[0].id : (data.id || 1);
      
      return {
        estateId: estateId
      };
    },

    // 쿼리 결과 포맷팅
    resultFormatter: (data) => {
      let result = '';
      
      // 매물 목록 추출
      let estates = [];
      
      if (Array.isArray(data)) {
        // 매물 목록인 경우
        estates = data;
      } else if (data && data.estates && Array.isArray(data.estates)) {
        // { estates: [...] } 형태인 경우
        estates = data.estates;
      } else if (data && data.score) {
        // 단일 매물인 경우
        estates = [data];
      }
      
      // 점수 정보가 있는 매물만 필터링
      const estatesWithScores = estates.filter(estate => estate && estate.score);
      
      if (estatesWithScores.length > 0) {
        // 모든 매물의 점수 정보 표시
        estatesWithScores.forEach((estate, estateIndex) => {
          const scoreData = estate.score;
          
          // 매물 정보 헤더
          result += `--- Score ${estateIndex + 1} ---\n`;
          
          // 총점 표시
          result += `Total Score | ${scoreData.total || 0}\n`;
          
          // Top Factors 표시
          if (scoreData.topFactors && Array.isArray(scoreData.topFactors)) {
            scoreData.topFactors.forEach((factor, factorIndex) => {
              result += ` + ID       | ${factor.id || 'N/A'}\n`;
              result += `   Type     | ${factor.name || 'N/A'}\n`;
              result += `   Score    | ${factor.score || 0}\n`;
            });
          }

          result += '\n';
        });
      } else {
        result = "No scores available for any property.";
      }
      
      return result;
    },

    // 영향 받는 테이블 목록
    affectedTables: ['estate_score', 'score_type']
  }

};