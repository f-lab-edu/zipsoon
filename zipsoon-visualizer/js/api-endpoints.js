// 서버 설정 - 개발/배포 환경에 따라 달라짐
const SERVER_ADDRESS = window.location.hostname;
const SERVER_PORT = '8080';
const isCodespace = SERVER_ADDRESS.includes('.github.dev');

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
        },
        isFavorite: data.isFavorite || false
      };
    }
  },
  '/api/v1/estates/score-types': {
    url: '/api/v1/estates/score-types',
    method: 'GET',
    description: '점수 유형 목록 조회',

    // 요청 데이터 포맷팅
    requestFormatter: () => {
      return {}; // GET 요청이라 body가 필요 없음
    },

    // 응답 데이터 포맷팅
    responseFormatter: (data) => {
      if (Array.isArray(data)) {
        return data.map(type => ({
          id: type.id,
          name: type.name,
          description: type.description,
          enabled: type.enabled
        }));
      }
      return [];
    }
  },
  '/api/v1/estates/score-types/:id/enable': {
    url: '/api/v1/estates/score-types/{id}/enable', // 템플릿 형식의 URL
    method: 'POST',
    description: '점수 유형 활성화',

    // 요청 데이터 포맷팅
    requestFormatter: (id) => {
      return {}; // 바디가 필요 없음, 경로 매개변수로 ID 전달
    },

    // 응답 데이터 포맷팅
    responseFormatter: (data) => {
      return { success: true };
    }
  },
  '/api/v1/estates/score-types/:id/disable': {
    url: '/api/v1/estates/score-types/{id}/disable', // 템플릿 형식의 URL
    method: 'POST',
    description: '점수 유형 비활성화',

    // 요청 데이터 포맷팅
    requestFormatter: (id) => {
      return {}; // 바디가 필요 없음, 경로 매개변수로 ID 전달
    },

    // 응답 데이터 포맷팅
    responseFormatter: (data) => {
      return { success: true };
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
  },
  '/api/v1/users/me/favorites': {
    url: '/api/v1/users/me/favorites',
    method: 'GET',
    description: '내가 찜한 매물 목록 조회',

    // 요청 데이터 포맷팅
    requestFormatter: (data) => {
      // 서버는 page를 1부터 시작하는 것으로 기대 (기본값 1)
      return {
        page: data && data.page !== undefined ? data.page : 1,
        size: data && data.size !== undefined ? data.size : 10
      };
    },

    // 응답 데이터 포맷팅
    responseFormatter: (data) => {
      return {
        content: data.content.map(estate => ({
          id: estate.id,
          name: estate.name,
          type: estate.type,
          typeName: estate.typeName,
          tradeType: estate.tradeType,
          tradeTypeName: estate.tradeTypeName,
          price: estate.price,
          rentPrice: estate.rentPrice,
          area: estate.area,
          latitude: estate.lat,
          longitude: estate.lng,
          score: estate.score || {
            total: 0,
            topFactors: []
          }
        })),
        page: data.page,
        size: data.size,
        totalElements: data.totalElements,
        totalPages: data.totalPages
      };
    }
  },
  '/api/v1/estates/:id/favorite': {
    url: '/api/v1/estates/{id}/favorite',
    method: 'POST',
    description: '매물 찜하기',

    // 요청 데이터 포맷팅 (ID는 URL에 추가)
    requestFormatter: (id) => {
      return {}; // POST 요청이지만 body가 필요 없음
    },

    // 응답 데이터 포맷팅
    responseFormatter: (data) => {
      return { success: true };
    }
  },
  '/api/v1/estates/:id/favorite/delete': {
    url: '/api/v1/estates/{id}/favorite',
    method: 'DELETE',
    description: '매물 찜하기 취소',

    // 요청 데이터 포맷팅 (ID는 URL에 추가)
    requestFormatter: (id) => {
      return {}; // DELETE 요청이라 body가 필요 없음
    },

    // 응답 데이터 포맷팅
    responseFormatter: (data) => {
      return { success: true };
    }
  },
  '/api/v1/users/me/delete': {
    url: '/api/v1/users/me',
    method: 'DELETE',
    description: '회원 탈퇴',
    
    // 요청 데이터 포맷팅
    requestFormatter: () => {
      return {}; // DELETE 요청이라 body가 필요 없음
    },
    
    // 응답 데이터 포맷팅
    responseFormatter: (data) => {
      return { success: true };
    }
  }
};
