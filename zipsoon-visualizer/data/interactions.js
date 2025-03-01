/**
 * 인터랙션 데이터 정의
 * 사용자 인터랙션 유형과 관련 정보를 정의합니다.
 */
const INTERACTIONS = {
  mapSearch: {
    endpoint: '/api/v1/estates/map',
    sqlQuery: 'findAllInViewport',
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
  }
};