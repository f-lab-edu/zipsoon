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
    sqlQuery: ['findById', 'findScoresByEstateId'],
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
  }
};