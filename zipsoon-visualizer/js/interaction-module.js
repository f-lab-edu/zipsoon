/**
 * 인터랙션 모듈
 * 사용자 인터랙션을 관리하고 시각화 요소를 업데이트합니다.
 */
class InteractionModule {
  constructor() {
    this.currentInteraction = null;
    this.interactions = {}; // 로드된 인터랙션 데이터

    // DOM 요소 캐싱
    this.clientToAppElement = document.querySelector('.arrow-client-to-app + .arrow-label .code-content');
    this.appToClientElement = document.querySelector('.arrow-app-to-client + .arrow-label .code-content');
    this.appToDbElement = document.querySelector('.arrow-app-to-db + .arrow-label .code-content');
    this.dbToAppElement = document.querySelector('.arrow-db-to-app + .arrow-label .code-content');

    // 이벤트 리스너 등록
    document.addEventListener('viewportChanged', this.handleMapInteraction.bind(this));
    document.addEventListener('estateMarkerClicked', this.handleMarkerClick.bind(this));
    document.addEventListener('estateDetailLoaded', this.handleEstateDetailLoaded.bind(this));
    document.addEventListener('estateFavoriteChanged', this.handleFavoriteChanged.bind(this));

    // API 베이스 URL 설정
    this.apiBaseUrl = 'http://localhost:8080';
  }

  // 인터랙션 데이터 로드
  loadInteractions(interactions) {
    this.interactions = interactions;
  }

  // 맵 인터랙션 처리
  handleMapInteraction(event) {
    const viewport = event.detail;
    console.log('지도 뷰포트 변경 감지:', {
      sw: viewport.sw,
      ne: viewport.ne,
      zoom: viewport.zoom
    });
    
    // 로그인 상태에서만 API 요청을 보내지만, 어떤 형태의 요청이 될지는 항상 표시
    // 인터랙션 정보 가져오기
    const interaction = this.interactions['mapSearch'];
    if (!interaction) {
      console.error('mapSearch 인터랙션을 찾을 수 없음');
      return;
    }
    
    const endpoint = API_ENDPOINTS[interaction.endpoint];
    
    // sqlQuery는 문자열 또는 배열
    const sqlQueryNames = Array.isArray(interaction.sqlQuery) 
      ? interaction.sqlQuery 
      : [interaction.sqlQuery];
    
    // 첫 번째 SQL 쿼리 가져오기 (기본 호환성 유지)
    const sqlQuery = SQL_QUERIES[sqlQueryNames[0]];
    
    if (!endpoint || !sqlQuery) {
      console.error('mapSearch 인터랙션에 필요한 엔드포인트/SQL 쿼리 정의를 찾을 수 없음');
      return;
    }
    
    // 항상 UI는 업데이트
    this.updateClientToAppContent(endpoint, viewport);
    this.updateAppToDBContent(sqlQuery, viewport);
    
    // 로그인 상태에서만 API 요청 실행
    if (window.authTokens && window.authTokens.isLoggedIn) {
      console.log('지도 뷰포트 변경 - 로그인 상태로 매물 검색 API 요청 시작');
      this.triggerInteraction('mapSearch', viewport);
    } else {
      console.log('지도 뷰포트 변경 - 로그인 필요: API 요청은 생략');
      // 로그인 필요 UI 표시
      this.appToClientElement.textContent = '로그인이 필요합니다.\nAPI 요청은 전송되지 않았습니다.';
      this.dbToAppElement.textContent = '로그인이 필요합니다.\nDB 응답이 없습니다.';
      
      // 테이블 강조 표시 - 모든 쿼리의 영향 테이블 표시
      const affectedTables = [];
      sqlQueryNames.forEach(queryName => {
        const query = SQL_QUERIES[queryName];
        if (query && query.affectedTables) {
          affectedTables.push(...query.affectedTables);
        }
      });
      
      if (affectedTables.length > 0 && window.erdConnector) {
        // 중복 제거
        const uniqueTables = [...new Set(affectedTables)];
        window.erdConnector.highlightTables(uniqueTables);
      }
    }
  }
  
  // 매물 마커 클릭 인터랙션 처리
  handleMarkerClick(event) {
    const estateData = event.detail;
    if (!estateData || !estateData.id) {
      console.error('매물 정보가 없거나 ID가 없습니다.');
      return;
    }
    
    console.log('매물 마커 클릭 감지:', estateData.id);
    
    // 인터랙션 정보 가져오기
    const interaction = this.interactions['estateDetail'];
    if (!interaction) {
      console.error('estateDetail 인터랙션을 찾을 수 없음');
      return;
    }
    
    const endpoint = API_ENDPOINTS[interaction.endpoint];
    
    // sqlQuery는 문자열 또는 배열
    const sqlQueryNames = Array.isArray(interaction.sqlQuery) 
      ? interaction.sqlQuery 
      : [interaction.sqlQuery];
    
    // 첫 번째 SQL 쿼리 가져오기
    const sqlQuery = SQL_QUERIES[sqlQueryNames[0]];
    
    if (!endpoint || !sqlQuery) {
      console.error('estateDetail 인터랙션에 필요한 엔드포인트/SQL 쿼리 정의를 찾을 수 없음');
      return;
    }
    
    // 매물 ID 기반으로 클라이언트->앱 요청 정보 업데이트
    const requestData = { id: estateData.id };
    this.updateClientToAppContent(endpoint, requestData);
    
    // 앱->DB 쿼리 정보 업데이트
    this.updateAppToDBContent(sqlQuery, requestData);
    
    // 로그인 상태에서만 실제 API 요청은 EstateDetailComponent에서 처리
    // 여기서는 UI만 미리 업데이트
    if (!window.authTokens || !window.authTokens.isLoggedIn) {
      console.log('매물 상세 조회 - 로그인 필요: API 요청은 생략');
      // 로그인 필요 UI 표시
      this.appToClientElement.textContent = '로그인이 필요합니다.\nAPI 요청은 전송되지 않았습니다.';
      this.dbToAppElement.textContent = '로그인이 필요합니다.\nDB 응답이 없습니다.';
    }
    
    // ERD 테이블 강조 표시 - estateDetail 인터랙션의 모든 테이블
    const affectedTables = [];
    sqlQueryNames.forEach(queryName => {
      const query = SQL_QUERIES[queryName];
      if (query && query.affectedTables) {
        affectedTables.push(...query.affectedTables);
      }
    });
    
    if (affectedTables.length > 0 && window.erdConnector) {
      // 중복 제거
      const uniqueTables = [...new Set(affectedTables)];
      window.erdConnector.highlightTables(uniqueTables);
    }
  }
  
  // 매물 상세 정보 로드 완료 인터랙션 처리
  // 찜하기 상태 변경 이벤트 처리
  handleFavoriteChanged(event) {
    const { id, isFavorite } = event.detail;
    
    if (!id) {
      console.error('찜하기 상태 변경 이벤트에 매물 ID가 없습니다.');
      return;
    }
    
    console.log('찜하기 상태 변경 이벤트:', id, isFavorite);
    
    // 인터랙션 ID 결정 - 찜하기 또는 찜하기 취소
    const interactionId = isFavorite ? 'addFavorite' : 'removeFavorite';
    
    // 인터랙션 정보 가져오기
    const interaction = this.interactions[interactionId];
    if (!interaction) {
      console.error(`${interactionId} 인터랙션을 찾을 수 없음`);
      return;
    }
    
    const endpoint = API_ENDPOINTS[interaction.endpoint];
    
    // sqlQuery는 문자열 또는 배열
    const sqlQueryNames = Array.isArray(interaction.sqlQuery) 
      ? interaction.sqlQuery 
      : [interaction.sqlQuery];
    
    // 첫 번째 SQL 쿼리 가져오기
    const sqlQuery = SQL_QUERIES[sqlQueryNames[0]];
    
    if (!endpoint || !sqlQuery) {
      console.error(`${interactionId} 인터랙션에 필요한 엔드포인트/SQL 쿼리 정의를 찾을 수 없음`);
      return;
    }
    
    // 요청 파라미터 생성
    const requestData = { id };
    
    // UI 업데이트
    this.updateClientToAppContent(endpoint, requestData);
    this.updateAppToDBContent(sqlQuery, requestData);
    
    // ERD 테이블 강조 표시
    const affectedTables = [];
    sqlQueryNames.forEach(queryName => {
      const query = SQL_QUERIES[queryName];
      if (query && query.affectedTables) {
        affectedTables.push(...query.affectedTables);
      }
    });
    
    if (affectedTables.length > 0 && window.erdConnector) {
      // 중복 제거
      const uniqueTables = [...new Set(affectedTables)];
      window.erdConnector.highlightTables(uniqueTables);
    }
  }
  
  handleEstateDetailLoaded(event) {
    const { id, data } = event.detail;
    
    if (!id || !data) {
      console.error('매물 상세 정보 로드 결과가 없습니다.');
      return;
    }
    
    console.log('매물 상세 정보 로드 완료 이벤트:', id);
    
    // 인터랙션 정보 가져오기
    const interaction = this.interactions['estateDetail'];
    if (!interaction) {
      console.error('estateDetail 인터랙션을 찾을 수 없음');
      return;
    }
    
    const endpoint = API_ENDPOINTS[interaction.endpoint];
    
    // sqlQuery는 문자열 또는 배열 - 상세 점수 정보 쿼리 가져오기 (두 번째 쿼리)
    const sqlQueryNames = Array.isArray(interaction.sqlQuery) 
      ? interaction.sqlQuery 
      : [interaction.sqlQuery];
    
    // 두 번째 SQL 쿼리 (상세 점수 정보) 가져오기
    const scoreQuery = sqlQueryNames.length > 1 ? SQL_QUERIES[sqlQueryNames[1]] : null;
    
    if (!endpoint) {
      console.error('estateDetail 인터랙션에 필요한 엔드포인트 정의를 찾을 수 없음');
      return;
    }
    
    // 앱->클라이언트 응답 정보 업데이트
    if (endpoint.responseFormatter) {
      const formattedResponse = endpoint.responseFormatter(data);
      this.appToClientElement.textContent = JSON.stringify(formattedResponse, null, 4);
    } else {
      this.appToClientElement.textContent = JSON.stringify(data, null, 4);
    }
    
    // DB->앱 응답 정보 업데이트 (상세 점수 정보)
    if (scoreQuery && scoreQuery.resultFormatter) {
      const resultText = scoreQuery.resultFormatter(data);
      this.dbToAppElement.textContent = resultText;
    }
  }

  // 인터랙션 트리거
  async triggerInteraction(interactionId, data) {
    return new Promise(async (resolve, reject) => {
      if (!this.interactions[interactionId]) {
        const error = `인터랙션 ID가 존재하지 않습니다: ${interactionId}`;
        console.error(error);
        reject(new Error(error));
        return;
      }

      // 현재 로그인 상태 로깅
      console.log(`인터랙션 ${interactionId} 실행 - 로그인 상태:`, 
        window.authTokens ? {
          isLoggedIn: window.authTokens.isLoggedIn,
          hasToken: !!window.authTokens.accessToken
        } : 'authTokens 없음'
      );

      this.currentInteraction = interactionId;
      const interaction = this.interactions[interactionId];

      // API 엔드포인트 가져오기
      const endpoint = API_ENDPOINTS[interaction.endpoint];
      
      // sqlQuery 가져오기 (단일 문자열 또는 배열)
      const sqlQueryNames = Array.isArray(interaction.sqlQuery) 
        ? interaction.sqlQuery 
        : [interaction.sqlQuery];
        
      // 첫 번째 SQL 쿼리 가져오기 (기본 호환성 유지)
      const sqlQuery = SQL_QUERIES[sqlQueryNames[0]];

      if (!endpoint || !sqlQuery) {
        const error = '엔드포인트 또는 SQL 쿼리가 정의되지 않았습니다.';
        console.error(error);
        reject(new Error(error));
        return;
      }

      // Client -> App 화살표 업데이트
      this.updateClientToAppContent(endpoint, data);

      // App -> DB 화살표 업데이트
      this.updateAppToDBContent(sqlQuery, data);

      try {
        // 실제 API 호출
        const responseData = await this.fetchAPI(endpoint, data);

        // 응답 데이터 처리
        this.handleAPIResponse(endpoint, sqlQuery, responseData, data);

        // ERD 테이블 강조 표시 - 다중 쿼리인 경우 모든 테이블 표시
        const affectedTables = [];
        sqlQueryNames.forEach(queryName => {
          const query = SQL_QUERIES[queryName];
          if (query && query.affectedTables) {
            affectedTables.push(...query.affectedTables);
          }
        });
        
        if (affectedTables.length > 0 && window.erdConnector) {
          // 중복 제거
          const uniqueTables = [...new Set(affectedTables)];
          window.erdConnector.highlightTables(uniqueTables);
        }
        
        // 프로미스 해결 - 응답 데이터 반환
        resolve(responseData);
      } catch (error) {
        console.error('API 요청 실패:', error);
        this.appToClientElement.textContent = JSON.stringify({
          error: true,
          message: '서버 요청 중 오류가 발생했습니다.',
          details: error.message
        }, null, 4);
        
        // 프로미스 거부
        reject(error);
      }
    });
  }

  // URL 템플릿 문자열에서 실제 URL을 생성하는 헬퍼 함수
  processUrlTemplate(urlTemplate, data) {
    // {id}와 같은 템플릿 변수를 찾아 데이터에서 값을 가져와 치환
    let processedUrl = urlTemplate;
    
    // 템플릿 변수 패턴을 찾습니다. 예: {id}, {userId} 등
    const templateVarPattern = /{([^{}]+)}/g;
    let match;
    
    // 모든 템플릿 변수를 치환
    while ((match = templateVarPattern.exec(urlTemplate)) !== null) {
      const placeholder = match[0]; // 예: {id}
      const varName = match[1];     // 예: id
      
      // data 객체에서 값을 가져옴
      const value = data[varName];
      if (value !== undefined) {
        processedUrl = processedUrl.replace(placeholder, value);
      } else {
        console.warn(`템플릿 변수 ${varName}에 대한 값이 없습니다.`);
      }
    }
    
    return processedUrl;
  }

  // 실제 API 호출
  async fetchAPI(endpoint, data) {
    console.log('API 호출 시작', {
      endpoint: endpoint.url,
      method: endpoint.method,
      dataType: typeof data
    });
    
    // 데이터가 유효한지 확인
    if (!data) {
      console.warn('API 호출 데이터가 없음, 기본값 사용');
    }
    
    try {
      const requestParams = endpoint.requestFormatter(data);
      console.log('요청 파라미터 생성 완료:', requestParams);
      
      // URL 템플릿 처리 - {id}와 같은 변수를 실제 값으로 치환
      let urlPath = this.processUrlTemplate(endpoint.url, data);
      let url = `${this.apiBaseUrl}${urlPath}`;
      
      let options = {
        method: endpoint.method,
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        }
      };
    
      // 로그인 되어 있고 인증 토큰이 있는 경우 헤더에 추가
      if (window.authTokens && window.authTokens.isLoggedIn && window.authTokens.accessToken) {
        console.log('인증 헤더 추가:', window.authTokens.accessToken);
        options.headers['Authorization'] = `Bearer ${window.authTokens.accessToken}`;
        
        // 인증 헤더가 추가되었음을 명확히 확인
        console.log('인증 헤더 확인:', {
          added: true,
          token: window.authTokens.accessToken.substring(0, 10) + '...',
          headerValue: options.headers['Authorization']
        });
      } else {
        console.log('인증 헤더 추가되지 않음:', {
          authTokensExists: !!window.authTokens,
          isLoggedIn: window.authTokens ? window.authTokens.isLoggedIn : false,
          hasAccessToken: window.authTokens ? !!window.authTokens.accessToken : false
        });
      }
  
      // GET 요청인 경우 URL에 쿼리 파라미터 추가
      if (endpoint.method === 'GET') {
        const queryParams = new URLSearchParams();
        Object.entries(requestParams).forEach(([key, value]) => {
          queryParams.append(key, value);
        });
        url = `${url}?${queryParams.toString()}`;
      } else {
        // POST, PUT 등의 요청인 경우 body에 데이터 추가
        options.body = JSON.stringify(requestParams);
      }
  
      console.log(`API 요청: ${url}`, {
        ...options,
        headers: {
          ...options.headers,
          Authorization: options.headers.Authorization ? '(토큰 포함)' : '(토큰 없음)'
        }
      });
  
      const response = await fetch(url, options);
      console.log('API 응답 상태:', response.status, response.statusText);
      
      // 응답 클론 생성 (response.clone()은 response.text()와 함께 사용할 때 필요)
      const responseClone = response.clone();
      
      // 먼저 응답 본문을 텍스트로 가져와서 비어있는지 확인
      const text = await responseClone.text();
      let jsonResponse;
      
      if (text.trim() === '') {
        // 본문이 비어있는 경우 성공 응답 생성
        console.log('빈 응답 본문 처리: 기본 성공 객체 반환');
        jsonResponse = {
          success: true,
          _httpStatus: response.status,
          _httpStatusText: response.statusText
        };
      } else {
        // 본문이 있는 경우 JSON 파싱 시도
        try {
          jsonResponse = JSON.parse(text);
        } catch (parseError) {
          console.warn('JSON 파싱 실패, 원본 텍스트 반환:', text);
          jsonResponse = {
            success: response.ok,
            _httpStatus: response.status,
            _httpStatusText: response.statusText,
            rawContent: text
          };
        }
      }
      
      console.log('API 응답 데이터:', jsonResponse);
      
      // 비록 HTTP 오류 상태코드라도 응답을 반환하여 처리할 수 있게 함
      if (!response.ok) {
        console.warn(`HTTP 오류 응답 (${response.status}) 처리:`, jsonResponse);
        
        // 이전 DB -> App 내용 초기화
        if (this.dbToAppElement) {
          this.dbToAppElement.textContent = '';
        }
        
        // 오류 정보를 포함하여 반환
        return {
          ...jsonResponse,
          _httpStatus: response.status,
          _httpStatusText: response.statusText,
          _isError: true
        };
      }
      
      return jsonResponse;
    } catch (error) {
      console.error('API 호출 중 오류 발생:', error);
      throw error;
    }
  }


  // API 응답 처리
  handleAPIResponse(endpoint, sqlQuery, responseData, data) {
    console.log('API 응답 처리:', responseData);
    
    try {
      // 에러 응답인지 확인
      const isErrorResponse = 
        (responseData && responseData._isError) || // fetchAPI에서 표시한 오류
        (responseData && responseData.code && (responseData.code >= 400 || responseData.message)); // API 응답의 오류
      
      // 현재 인터랙션 가져오기
      const interaction = this.interactions[this.currentInteraction];
      if (!interaction) {
        console.error('현재 인터랙션 정보를 찾을 수 없음');
        return;
      }
      
      // sqlQuery는 문자열 또는 배열
      const sqlQueryNames = Array.isArray(interaction.sqlQuery) 
        ? interaction.sqlQuery 
        : [interaction.sqlQuery];
      
      // DB -> App 화살표 콘텐츠 업데이트
      if (isErrorResponse) {
        // 에러 응답인 경우 - DB -> App 화살표는 비워둠
        console.log('에러 응답 감지: DB -> App 화살표 콘텐츠 초기화');
        this.dbToAppElement.textContent = '';
      } else if (sqlQueryNames.length === 1) {
        // 단일 쿼리인 경우 - 기존 로직 사용
        const currentSqlQuery = SQL_QUERIES[sqlQueryNames[0]];
        if (currentSqlQuery && currentSqlQuery.resultFormatter) {
          const resultText = currentSqlQuery.resultFormatter(responseData);
          this.dbToAppElement.textContent = resultText;
        }
      } else {
        // 다중 쿼리인 경우 - 각 쿼리 결과 포맷팅
        let formattedResults = '';
        
        sqlQueryNames.forEach((queryName, index) => {
          const currentSqlQuery = SQL_QUERIES[queryName];
          
          if (!currentSqlQuery || !currentSqlQuery.resultFormatter) {
            console.error(`SQL 쿼리 '${queryName}'가 없거나 resultFormatter 속성이 없음`);
            return;
          }
          
          // 구분자 추가 (첫 번째 결과는 제외)
          if (index > 0) {
            formattedResults += '\n\n';
          }
          
          // 여러 쿼리 결과인 경우 헤더 추가
          formattedResults += `--- Result ${index + 1}: ${queryName} ---\n`;
          
          // 모든 쿼리에 원본 데이터 사용
          const resultText = currentSqlQuery.resultFormatter(responseData);
          formattedResults += resultText;
        });
        
        this.dbToAppElement.textContent = formattedResults;
      }

      // 지도 검색 결과인 경우 지도에 마커 표시
      if (this.currentInteraction === 'mapSearch' && window.mapModule) {
        // 원본 응답 데이터를 사용 (formatter가 필터링할 수 있음)
        this.displayMarkersOnMap(responseData);
      }
  
      // App -> Client 화살표 콘텐츠 업데이트
      if (isErrorResponse) {
        // 에러 응답인 경우 - 원본 응답 그대로 표시
        const errorResponse = JSON.stringify(responseData, null, 4);
        this.appToClientElement.textContent = errorResponse;
        
        // 에러 표시를 위해 스타일 변경 (빨간색 텍스트)
        if (this.appToClientElement) {
          this.appToClientElement.style.color = 'red';
        }
      } else if (endpoint.responseFormatter) {
        // 정상 응답이고 포맷터가 있는 경우
        const formattedResponse = endpoint.responseFormatter(responseData);
        this.appToClientElement.textContent = JSON.stringify(formattedResponse, null, 4);
        
        // 정상 응답 스타일로 복구
        if (this.appToClientElement) {
          this.appToClientElement.style.color = '';
        }
        
      } else {
        // 정상 응답이지만 포맷터가 없는 경우
        this.appToClientElement.textContent = JSON.stringify(responseData, null, 4);
        
        // 정상 응답 스타일로 복구
        if (this.appToClientElement) {
          this.appToClientElement.style.color = '';
        }
      }
    } catch (error) {
      console.error('응답 처리 중 오류:', error);
      this.dbToAppElement.textContent = '응답 처리 중 오류 발생: ' + error.message;
      this.appToClientElement.textContent = JSON.stringify(responseData, null, 4);
    }
  }

  // Client -> App 화살표 콘텐츠 업데이트
  updateClientToAppContent(endpoint, data) {
    console.log('Client -> App 화살표 업데이트 시작', {
      endpoint: endpoint ? endpoint.url : '없음',
      hasFormatter: endpoint ? !!endpoint.requestFormatter : false
    });
    
    if (!endpoint || !endpoint.requestFormatter) {
      console.error('엔드포인트 또는 requestFormatter가 없어 업데이트 불가');
      return;
    }

    try {
      const requestData = endpoint.requestFormatter(data);
      console.log('요청 데이터 생성 완료:', requestData);
      
      const formattedRequest = JSON.stringify({
        url: endpoint.url,
        method: endpoint.method,
        params: requestData
      }, null, 4);
      
      console.log('Client -> App 콘텐츠 업데이트:', formattedRequest.substring(0, 100) + '...');
      
      // DOM 요소가 존재하는지 확인
      if (this.clientToAppElement) {
        this.clientToAppElement.textContent = formattedRequest;
      } else {
        console.error('clientToAppElement DOM 요소를 찾을 수 없음');
      }
    } catch (error) {
      console.error('Client -> App 업데이트 중 오류:', error);
    }
  }

  // App -> DB 화살표 콘텐츠 업데이트
  updateAppToDBContent(sqlQuery, data) {
    console.log('App -> DB 화살표 업데이트 시작');
    
    // 현재 인터랙션 가져오기
    const interaction = this.interactions[this.currentInteraction];
    if (!interaction) {
      console.error('현재 인터랙션 정보를 찾을 수 없음');
      return;
    }
    
    // sqlQuery는 문자열 또는 배열
    const sqlQueryNames = Array.isArray(interaction.sqlQuery) 
      ? interaction.sqlQuery 
      : [interaction.sqlQuery];
    
    // 쿼리가 하나도 없으면 종료
    if (sqlQueryNames.length === 0) {
      console.error('SQL 쿼리 이름이 없어 업데이트 불가');
      return;
    }
    
    try {
      let formattedQueries = '';
      
      // 각 쿼리 처리
      sqlQueryNames.forEach((queryName, index) => {
        const currentSqlQuery = SQL_QUERIES[queryName];
        
        if (!currentSqlQuery || !currentSqlQuery.query) {
          console.error(`SQL 쿼리 '${queryName}'가 없거나 query 속성이 없음`);
          return;
        }
        
        // 쿼리 구분자 추가 (첫 번째 쿼리는 제외)
        if (index > 0) {
          formattedQueries += '\n\n';
        }
        
        // 여러 쿼리인 경우 쿼리 헤더 추가
        if (sqlQueryNames.length > 1) {
          formattedQueries += `--- Query ${index + 1}: ${queryName} ---\n`;
        }
        
        let query = currentSqlQuery.query;
        
        // 파라미터 포맷터가 있으면 적용
        if (currentSqlQuery.paramFormatter) {
          // 첫 번째 쿼리는 원본 데이터 사용, 나머지는 첫 번째 쿼리 결과의 첫 항목 사용 (예: 첫 매물 ID)
          const params = currentSqlQuery.paramFormatter(
            index === 0 ? data : { id: 1 } // 두 번째 이상 쿼리에는 임의의 ID 사용
          );
          console.log(`SQL 파라미터 생성 완료 (${queryName}):`, params);
          
          // 쿼리 파라미터 치환
          Object.keys(params).forEach(key => {
            const before = query;
            query = query.replace(new RegExp(`\\{${key}\\}`, 'g'), params[key]);
            
            // 치환 여부 확인
            if (before !== query) {
              console.log(`파라미터 {${key}} 치환 완료`);
            }
          });
        }
        
        formattedQueries += query;
      });
      
      console.log('App -> DB 콘텐츠 업데이트 완료');
      
      // DOM 요소가 존재하는지 확인
      if (this.appToDbElement) {
        this.appToDbElement.textContent = formattedQueries;
      } else {
        console.error('appToDbElement DOM 요소를 찾을 수 없음');
      }
    } catch (error) {
      console.error('App -> DB 업데이트 중 오류:', error);
    }
  }
  
  /**
   * 지도에 마커 표시
   * @param {Object|Array} responseData - API 응답 데이터
   */
  displayMarkersOnMap(responseData) {
    if (!window.mapModule) {
      console.error('mapModule이 존재하지 않아 마커를 표시할 수 없습니다.');
      return;
    }
    
    console.log('지도에 마커 표시 시작:', responseData);
    
    // 기존 마커 모두 제거 (매 요청마다 새로 표시)
    window.mapModule.clearMarkers();

    if (responseData.length === 0) {
      return;
    }
    
    try {
      // 응답 데이터 형식 확인 및 처리
      let estates = [];
      
      if (Array.isArray(responseData)) {
        // 응답이 배열인 경우
        estates = responseData;
      } else if (responseData.estates && Array.isArray(responseData.estates)) {
        // 응답이 estates 배열을 포함하는 객체인 경우
        estates = responseData.estates;
      } else {
        console.warn('응답 데이터에서 매물 정보를 찾을 수 없습니다.');
        return;
      }
      
      console.log(`${estates.length}개의 매물 마커 표시`);
      
      // 마커 표시
      estates.forEach(estate => {
        // 위도/경도 정보 확인
        const lat = estate.lat;
        const lng = estate.lng;
        
        if (lat && lng) {
          // 점수 정보 처리
          let scoreInfo = '';
          if (estate.score && estate.score.total) {
            scoreInfo = `<div class="score">점수: ${estate.score.total}</div>`;
          }
          
          // 가격 포맷팅
          const formatPrice = (price) => {
            return price ? price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',') : '';
          };
          
          // 가격 정보 구성
          let priceText = '';
          if (estate.price) {
            priceText = formatPrice(estate.price);
            if (estate.rentPrice) {
              priceText += '/' + formatPrice(estate.rentPrice);
            }
          }
          
          // 매물 정보로 마커 툴팁 생성
          const tooltipContent = `
            <div class="estate-tooltip">
              <strong>${estate.name || ''}</strong><br>
              ${estate.type || ''} ${estate.tradeType || ''}<br>
              ${priceText}<br>
              ${scoreInfo}
            </div>
          `;
          
          // 마커 추가 (매물 데이터 포함)
          const marker = window.mapModule.addMarker([lat, lng], estate);
          marker.bindTooltip(tooltipContent, { 
            permanent: false,
            direction: 'top'
          });
        }
      });
    } catch (error) {
      console.error('마커 표시 중 오류 발생:', error);
    }
  }
}

// 전역 인스턴스 생성
const interactionModule = new InteractionModule();