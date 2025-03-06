/**
 * 인터랙션 데이터 정의
 * 사용자 인터랙션 유형과 관련 정보를 정의합니다.
 */
const INTERACTIONS = {
  mapSearch: {
    endpoint: '/api/v1/estates/map',
    sqlQuery: ['findAllInViewport', 'findScoresByEstateId'],
    description: '지도 내 매물 목록 조회'
  },
  estateDetail: {
    endpoint: '/api/v1/estates/:id',
    sqlQuery: ['findById', 'findScoresByEstateId', 'existsByUserIdAndEstateId'],
    description: '매물 상세 정보 조회'
  },
  signup: {
    endpoint: '/api/v1/auth/signup',
    sqlQuery: 'save',
    description: '회원가입'
  },
  login: {
    endpoint: '/api/v1/auth/login',
    sqlQuery: 'findUserByEmail',
    description: '로그인'
  },
  scoreTypes: {
    endpoint: '/api/v1/estates/score-types',
    sqlQuery: ['findAllScoreTypes', 'findDisabledScoreTypeIdsByUserId'],
    description: '점수 유형 목록 조회'
  },
  enableScoreType: {
    endpoint: '/api/v1/estates/score-types/:id/enable',
    sqlQuery: 'delete',
    description: '점수 유형 활성화'
  },
  disableScoreType: {
    endpoint: '/api/v1/estates/score-types/:id/disable',
    sqlQuery: 'insert',
    description: '점수 유형 비활성화'
  },
  favoriteEstates: {
    endpoint: '/api/v1/users/me/favorites',
    sqlQuery: 'findFavoriteEstates',
    description: '내가 찜한 매물 목록 조회'
  },
  userFavorites: {
    endpoint: '/api/v1/users/me/favorites',
    sqlQuery: 'findFavoriteEstates',
    description: '사용자 찜 목록 조회'
  },
  addFavorite: {
    endpoint: '/api/v1/estates/:id/favorite',
    sqlQuery: 'insertFavoriteEstate',
    description: '매물 찜하기'
  },
  removeFavorite: {
    endpoint: '/api/v1/estates/:id/favorite/delete',
    sqlQuery: 'deleteFavoriteEstate',
    description: '매물 찜하기 취소'
  }
};