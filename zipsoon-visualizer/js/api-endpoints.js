/**
 * API 엔드포인트 정의
 * 각 엔드포인트에 대한 요청/응답 형식을 정의합니다.
 */
const API_ENDPOINTS = {
  '/api/v1/estates/map': {
    url: '/api/v1/estates/map',
    method: 'GET',
    description: '지도 뷰포트 내 매물 검색',

    // 요청 데이터 포맷팅
    requestFormatter: (viewport) => {
      return {
        swLng: viewport.sw.lng.toFixed(6),
        swLat: viewport.sw.lat.toFixed(6),
        neLng: viewport.ne.lng.toFixed(6),
        neLat: viewport.ne.lat.toFixed(6),
        zoom: viewport.zoom
      };
    },

    // 응답 데이터 포맷팅
    responseFormatter: (data) => {
      // 배열인 경우 배열을 estates 프로퍼티로 변환
      if (Array.isArray(data)) {
        return {
          total: data.length,
          estates: data.map(estate => ({
            id: estate.id,
            name: estate.name,
            type: estate.type,
            trade_type: estate.tradeType,
            price: estate.price,
            rent_price: estate.rentPrice,
            area: estate.area,
            latitude: estate.lat,
            longitude: estate.lng,
            score: estate.score || {
              total: 0,
              topFactors: []
            }
          }))
        };
      }
      
      // 객체인 경우 (예: { estates: [...] }) 처리
      return {
        total: data.estates ? data.estates.length : 0,
        estates: data.estates ? data.estates.map(estate => ({
          id: estate.id,
          name: estate.name,
          type: estate.type,
          typeName: estate.typeName,
          trade_type: estate.tradeType,
          tradeTypeName: estate.tradeTypeName,
          price: estate.price,
          rent_price: estate.rentPrice,
          area: estate.area_meter || estate.area,
          latitude: estate.latitude || estate.lat,
          longitude: estate.longitude || estate.lng,
          score: estate.score || {
            total: 0,
            topFactors: []
          }
        })) : []
      };
    }
  },
  '/api/v1/estates/:id': {
    url: '/api/v1/estates/{id}',  // 템플릿 형식의 URL
    method: 'GET',
    description: '매물 상세 정보 조회',

    // 요청 데이터 포맷팅 (ID는 URL에 추가)
    requestFormatter: (id) => {
      return {}; // GET 요청이라 body가 필요 없음
    },

    // 응답 데이터 포맷팅
    responseFormatter: (data) => {
      return {
        id: data.id,
        name: data.name,
        type: data.type,
        tradeType: data.tradeType,
        price: data.price,
        rentPrice: data.rentPrice,
        areaMeter: data.areaMeter,
        areaPyeong: data.areaPyeong,
        latitude: data.latitude,
        longitude: data.longitude,
        address: data.address,
        tags: data.tags || [],
        imageUrls: data.imageUrls || [],
        platformType: data.platformType,
        platformId: data.platformId,
        score: data.score || {
          total: 0,
          description: '점수 정보가 없습니다',
          factors: []
        }
      };
    }
  },
  '/api/v1/auth/signup': {
    url: '/api/v1/auth/signup',
    method: 'POST',
    description: '회원가입',

    // 요청 데이터 포맷팅 - SignupRequest에 맞춤 (email, name)
    requestFormatter: (data) => {
      const uuid = Math.random().toString(36).substring(2, 14);
      return {
        email: data.email || `user-${uuid}@example.com`,
        name: data.name || `User ${uuid}`
      };
    },

    // 응답 데이터 포맷팅 - AuthToken에 맞춤
    responseFormatter: (data) => {
      // AuthToken 객체 형식에 맞춤 (accessToken, refreshToken, expiresAt)
      return {
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
        expiresAt: data.expiresAt,
        // UI 표시용 추가 정보
        tokenType: 'Bearer'
      };
    }
  },
  '/api/v1/auth/login': {
    url: '/api/v1/auth/login',
    method: 'POST',
    description: '로그인',

    // 요청 데이터 포맷팅 - LoginRequest에 맞춤 (email)
    requestFormatter: (data) => {
      const uuid = Math.random().toString(36).substring(2, 14);
      return {
        email: data.email || `user-${uuid}@example.com`
      };
    },

    // 응답 데이터 포맷팅 - AuthToken에 맞춤
    responseFormatter: (data) => {
      // AuthToken 객체 형식에 맞춤 (accessToken, refreshToken, expiresAt)
      return {
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
        expiresAt: data.expiresAt,
        // UI 표시용 추가 정보
        tokenType: 'Bearer'
      };
    }
  }
};