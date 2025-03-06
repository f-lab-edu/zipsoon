/**
 * SQL 쿼리 정의
 * 각 인터랙션에 대한 SQL 쿼리와 파라미터 처리 방법을 정의합니다.
 */
const SQL_QUERIES = {
  findAllScoreTypes: {
    query: `SELECT
    id,
    name,
    description,
    active
FROM
    score_type
WHERE
    active = true
ORDER BY
    id`,
    
    paramFormatter: () => ({}),
    
    resultFormatter: (data) => {
      if (!data || !Array.isArray(data)) {
        return "No score types available or unexpected response format.";
      }
      
      let result = '';
      
      data.forEach((scoreType, index) => {
        result += `--- Score Type ${index + 1} ---\n`;
        result += `ID          | ${scoreType.id}\n`;
        result += `Name        | ${scoreType.name}\n`;
        result += `Description | ${scoreType.description}\n`;
        result += `Active      | ${scoreType.active ? 'Yes' : 'No'}\n`;
        result += `Enabled     | ${scoreType.enabled ? 'Yes' : 'No'}\n\n`;
      });
      
      return result;
    },
    
    affectedTables: ['score_type']
  },
  
  findDisabledScoreTypeIdsByUserId: {
    query: `SELECT score_type_id
FROM user_disabled_score_type
<!-- 다음은 예시로, userId는 현재 유즈케이스와 다릅니다. -->
<!-- UserPrincipal에서 추출된 userId를 사용합니다. -->
WHERE user_id = 1`,
    
    paramFormatter: (data) => {
      const userId = data.userId || 1;
      
      return {
        userId: userId
      };
    },
    
    resultFormatter: (data) => {
      if (!data) {
        return "No disabled score types found for this user.";
      }
      
      let disabledTypes = [];
      
      // 응답 데이터에서 비활성화된 점수 유형 ID 추출
      if (Array.isArray(data)) {
        // 배열인 경우 각 항목이 정수형 ID거나 score_type_id 속성을 사용
        disabledTypes = data.map(item => typeof item === 'number' ? item : item.score_type_id);
      } else if (data.disabledTypes && Array.isArray(data.disabledTypes)) {
        // { disabledTypes: [...] } 형태인 경우
        disabledTypes = data.disabledTypes;
      }
      
      let result = '';
      
      if (disabledTypes.length > 0) {
        result += `--- Disabled Score Types ---\n`;
        disabledTypes.forEach((typeId, index) => {
          result += `Type ${index + 1}   | ID: ${typeId}\n`;
        });
      } else {
        result = "No score types are disabled for this user.";
      }
      
      return result;
    },
    
    affectedTables: ['user_disabled_score_type']
  },
  
  insert: {
    query: `INSERT INTO user_disabled_score_type (user_id, score_type_id, created_at)
<!-- 다음은 예시로, userId는 현재 유즈케이스와 다릅니다. -->
<!-- UserPrincipal에서 추출된 userId를 사용합니다. -->
VALUES (1, #{scoreTypeId}, #{createdAt})
ON CONFLICT (1, score_type_id) DO NOTHING`,
    
    paramFormatter: (data) => {
      return {
        userId: data.userId || 1,
        scoreTypeId: data.scoreTypeId || data.id || 1,
        createdAt: new Date().toISOString()
      };
    },
    
    resultFormatter: (data) => {
      return `--- Score Type Disabled ---\n` +
             `User ID     | ${data.userId || 1}\n` +
             `Score Type  | ${data.scoreTypeId || data.id || 1}\n` +
             `Action      | Disabled\n` +
             `Status      | Success`;
    },
    
    affectedTables: ['user_disabled_score_type']
  },
  
  delete: {
    query: `DELETE FROM user_disabled_score_type
<!-- 다음은 예시로, userId는 현재 유즈케이스와 다릅니다. -->
<!-- UserPrincipal에서 추출된 userId를 사용합니다. -->
WHERE user_id = 1 AND score_type_id = #{scoreTypeId}`,
    
    paramFormatter: (data) => {
      return {
        userId: data.userId || 1,
        scoreTypeId: data.scoreTypeId || data.id || 1
      };
    },
    
    resultFormatter: (data) => {
      return `--- Score Type Enabled ---\n` +
             `User ID     | ${data.userId || 1}\n` +
             `Score Type  | ${data.scoreTypeId || data.id || 1}\n` +
             `Action      | Enabled\n` +
             `Status      | Success`;
    },
    
    affectedTables: ['user_disabled_score_type']
  },
  findAllInViewport: {
    query: `SELECT * FROM estate
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
            result += `type        | ${estate.type}\n`;
            result += `name        | ${estate.name}\n`;
            result += `trade_type  | ${estate.tradeType}\n`;
            result += `price       | ${estate.price}\n`;
            result += `rent_price  | ${estate.rentPrice}\n`;
            result += `area        | ${estate.area}\n`;
            result += `latitude    | ${estate.lat}\n`;
            result += `longitude   | ${estate.lng}\n\n`;
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
            result += `type        | ${estate.type}\n`;
            result += `name        | ${estate.name}\n`;
            result += `trade_type  | ${estate.tradeType}\n`;
            result += `price       | ${estate.price}\n`;
            result += `rent_price  | ${estate.rentPrice}\n`;
            result += `area        | ${estate.area}\n`;
            result += `latitude    | ${estate.lat}\n`;
            result += `longitude   | ${estate.lng}\n\n`;
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
    affectedTables: ['estate']
  },

  save: {
    query: `INSERT INTO app_user (
  email,
  name,
  image_url,
  email_verified,
  role,
  created_at,
  updated_at
) VALUES (
  {email},
  {name},
  {imageUrl},
  {emailVerified},
  {role},
  NOW(),
  NOW()
)`,

    // 쿼리 파라미터 변환
    paramFormatter: (userData) => ({
      email: `'${userData.email}'`,
      name: `'${userData.name}'`,
      imageUrl: `NULL`,
      emailVerified: `TRUE`,
      role: `'USER'`
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
  <!-- 다음은 예시로, estateId는 현재 유즈케이스와 다릅니다. -->
  <!-- estate_id마다 n번 반복됩니다. -->
  es.estate_id = {estateId}
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
  },

  existsByUserIdAndEstateId: {
    query: `SELECT EXISTS(
    SELECT 1
    FROM user_favorite_estate
    WHERE user_id = {userId} AND estate_id = {estateId}
) AS exists`,

    // 쿼리 파라미터 변환
    paramFormatter: (data) => {
      return {
        userId: data.userId || 1,
        estateId: data.estateId || data.id || 1
      };
    },
    
    // 쿼리 결과 포맷팅
    resultFormatter: (data) => {
      return `--- Favorite Check ---\n` +
             `User ID     | ${data.userId || 1}\n` +
             `Estate ID   | ${data.estateId || data.id || 1}\n` +
             `Is Favorite | ${data.exists ? 'Yes' : 'No'}`;
    },
    
    // 영향 받는 테이블 목록
    affectedTables: ['user_favorite_estate']
  },
  
  findById: {
    query: `SELECT * FROM estate WHERE id = {id}`,

    // 쿼리 파라미터 변환
    paramFormatter: (estateData) => {
      return {
        id: estateData.id || 1
      };
    },

    // 쿼리 결과 포맷팅
    resultFormatter: (data) => {
      let result = '';
      
      if (!data || data.error) {
        return "Estate not found or error occurred.";
      }
      
      result += `--- Estate Details ---\n`;
      result += `id          | ${data.id}\n`;
      result += `name        | ${data.name || 'N/A'}\n`;
      result += `type        | ${data.type || 'N/A'}\n`;
      result += `trade_type  | ${data.tradeType || 'N/A'}\n`;
      result += `price       | ${data.price || 'N/A'}\n`;
      result += `rent_price  | ${data.rentPrice || 'N/A'}\n`;
      result += `area_meter  | ${data.areaMeter || 'N/A'}\n`;
      result += `area_pyeong | ${data.areaPyeong || 'N/A'}\n`;
      result += `address     | ${data.address || 'N/A'}\n`;
      result += `lat, lng    | ${data.latitude}, ${data.longitude}\n`;
      result += `platform_id | ${data.platformId || 'N/A'}\n`;
      
      if (data.tags && Array.isArray(data.tags)) {
        result += `tags        | ${data.tags.join(', ') || 'N/A'}\n`;
      }
      
      return result;
    },

    // 영향 받는 테이블 목록
    affectedTables: ['estate', 'user_favorite_estate']
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
  es.estate_id = {id}
ORDER BY
  es.normalized_score DESC`,

    // 쿼리 파라미터 변환
    paramFormatter: (estateData) => {
      return {
        id: estateData.id || 1
      };
    },

    // 쿼리 결과 포맷팅
    resultFormatter: (data) => {
      let result = '';
      
      if (!data || !data.score || !data.score.factors) {
        return "No score details available for this property.";
      }
      
      result += `--- Score Details ---\n`;
      result += `Total Score | ${data.score.total || 0}\n`;
      result += `Description | ${data.score.description || 'N/A'}\n\n`;
      
      if (data.score.factors && Array.isArray(data.score.factors)) {
        data.score.factors.forEach((factor, index) => {
          result += `--- Factor ${index + 1} ---\n`;
          result += `ID          | ${factor.id || 'N/A'}\n`;
          result += `Name        | ${factor.name || 'N/A'}\n`;
          result += `Description | ${factor.description || 'N/A'}\n`;
          result += `Score       | ${factor.score || 0}\n\n`;
        });
      }
      
      return result;
    },

    // 영향 받는 테이블 목록
    affectedTables: ['estate_score', 'score_type']
  },
  
  findFavoriteEstates: {
    query: `SELECT 
  e.*,
  uf.created_at as favorited_at
FROM 
  user_favorite_estate uf
JOIN 
  estate e ON uf.estate_id = e.id
WHERE 
  uf.user_id = {userId}
ORDER BY 
  uf.created_at DESC
LIMIT {limit} OFFSET {offset}`,

    // 쿼리 파라미터 변환
    paramFormatter: (data) => {
      const page = data && data.page !== undefined ? data.page : 0;
      const size = data && data.size !== undefined ? data.size : 10;
      
      return {
        userId: data.userId || 1,
        limit: size,
        offset: page * size
      };
    },

    // 쿼리 결과 포맷팅
    resultFormatter: (data) => {
      let result = '';
      
      if (!data || !data.content || !Array.isArray(data.content)) {
        return "No favorite estates found or unexpected response format.";
      }
      
      result += `--- Favorite Estates ---\n`;
      result += `Page        | ${data.page}\n`;
      result += `Size        | ${data.size}\n`;
      result += `Total       | ${data.totalElements}\n`;
      result += `Total Pages | ${data.totalPages}\n\n`;
      
      if (data.content.length > 0) {
        data.content.forEach((estate, index) => {
          result += `--- Estate ${index + 1} ---\n`;
          result += `ID          | ${estate.id}\n`;
          result += `Name        | ${estate.name || 'N/A'}\n`;
          result += `Type        | ${estate.type || 'N/A'}\n`;
          result += `Trade Type  | ${estate.tradeType || 'N/A'}\n`;
          result += `Price       | ${estate.price || 'N/A'}\n`;
          result += `Rent Price  | ${estate.rentPrice || 'N/A'}\n`;
          result += `Area        | ${estate.area || 'N/A'}\n`;
          result += `Score       | ${estate.score && estate.score.total ? estate.score.total : 'N/A'}\n\n`;
        });
      } else {
        result += "No favorite estates found.";
      }
      
      return result;
    },

    // 영향 받는 테이블 목록
    affectedTables: ['user_favorite_estate', 'estate']
  },
  
  insertFavoriteEstate: {
    query: `INSERT INTO user_favorite_estate (user_id, estate_id, created_at)
VALUES ({userId}, {estateId}, NOW())
ON CONFLICT (user_id, estate_id) DO NOTHING`,

    // 쿼리 파라미터 변환
    paramFormatter: (data) => {
      return {
        userId: data.userId || 1,
        estateId: data.id
      };
    },

    // 쿼리 결과 포맷팅
    resultFormatter: (data) => {
      return `--- 매물 찜하기 성공 ---\n` +
             `Status      | 성공`;
    },

    // 영향 받는 테이블 목록
    affectedTables: ['user_favorite_estate']
  },
  
  deleteFavoriteEstate: {
    query: `DELETE FROM user_favorite_estate
WHERE user_id = {userId} AND estate_id = {estateId}`,

    // 쿼리 파라미터 변환
    paramFormatter: (data) => {
      return {
        userId: data.userId || 1,
        estateId: data.id
      };
    },

    // 쿼리 결과 포맷팅
    resultFormatter: (data) => {
      return `--- 찜하기 취소 성공 ---\n` +
             `Status      | 성공`;
    },

    // 영향 받는 테이블 목록
    affectedTables: ['user_favorite_estate']
  }

};