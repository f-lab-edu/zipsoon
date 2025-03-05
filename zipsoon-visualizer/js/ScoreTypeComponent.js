/**
 * 점수 유형 관리 컴포넌트
 * 사용자가 매물 점수 유형을 활성화/비활성화할 수 있는 인터페이스를 제공합니다.
 */
class ScoreTypeComponent {
    constructor() {
        this.container = null;
        this.initialized = false;
        this.scoreTypes = [];
        this.isLoading = false;
    }
    
    /**
     * 컴포넌트 초기화 및 DOM 요소 생성
     */
    initialize() {
        if (this.initialized) return;
        
        // 컨테이너 생성
        this.container = document.createElement('div');
        this.container.className = 'score-type-container';
        
        // 스타일 적용
        this.container.style.position = 'absolute';
        this.container.style.bottom = '0';
        this.container.style.left = '0';
        this.container.style.right = '0';
        this.container.style.width = '100%';
        this.container.style.backgroundColor = 'white';
        this.container.style.zIndex = '1002'; // 설정 페이지보다 위에 표시
        this.container.style.display = 'none';
        this.container.style.flexDirection = 'column';
        this.container.style.boxShadow = '0 -5px 15px rgba(0, 0, 0, 0.1)';
        this.container.style.borderRadius = '20px 20px 0 0';
        this.container.style.maxHeight = '80%';
        this.container.style.overflowY = 'auto';
        this.container.style.transition = 'transform 0.3s ease-in-out';
        this.container.style.transform = 'translateY(100%)';
        
        // 초기 컨텐츠 설정
        this.renderContent();
        
        // 지도 컨테이너에 추가
        const mapContainer = document.querySelector('.map-container');
        if (mapContainer) {
            mapContainer.appendChild(this.container);
            this.initialized = true;
        } else {
            console.error('지도 컨테이너를 찾을 수 없어 점수 유형 컴포넌트를 추가할 수 없습니다.');
        }
    }
    
    /**
     * 컴포넌트 컨텐츠 렌더링
     */
    renderContent() {
        // 로딩 상태 표시 또는 점수 유형 목록 표시
        if (this.isLoading) {
            this.container.innerHTML = `
                <div class="score-type-header">
                    <button class="score-type-close">&times;</button>
                    <h2>내 점수 목록</h2>
                </div>
                <div class="score-type-content">
                    <div class="score-type-loading">데이터를 불러오는 중입니다...</div>
                </div>
            `;
        } else {
            // 점수 유형 목록 생성
            const scoreTypeItems = this.scoreTypes.map(type => `
                <div class="score-type-item" data-id="${type.id}">
                    <div class="score-type-info">
                        <div class="score-type-name">${type.name}</div>
                        <div class="score-type-description">${type.description}</div>
                    </div>
                    <div class="score-type-toggle">
                        <label class="checkbox-container">
                            <input type="checkbox" ${type.enabled ? 'checked' : ''}>
                            <span>활성화</span>
                        </label>
                    </div>
                </div>
            `).join('');
            
            this.container.innerHTML = `
                <div class="score-type-header">
                    <button class="score-type-close">&times;</button>
                    <h2>내 점수 목록</h2>
                </div>
                <div class="score-type-content">
                    ${this.scoreTypes.length > 0 
                      ? `<div class="score-type-list">${scoreTypeItems}</div>` 
                      : '<div class="score-type-empty">점수 유형이 없습니다.</div>'}
                </div>
                <div class="score-type-footer">
                    <p class="score-type-help">점수 유형을 활성화/비활성화하여 매물 점수에 반영할 요소를 선택하세요.</p>
                </div>
            `;
        }
        
        // 스타일 적용
        this.applyStyles();
        
        // 이벤트 리스너 추가
        this.addEventListeners();
    }
    
    /**
     * 컴포넌트 스타일 적용
     */
    applyStyles() {
        // 헤더 스타일
        const header = this.container.querySelector('.score-type-header');
        if (header) {
            header.style.padding = '20px';
            header.style.borderBottom = '1px solid #eee';
            header.style.position = 'relative';
            header.style.display = 'flex';
            header.style.alignItems = 'center';
            header.style.justifyContent = 'center';
        }
        
        // 제목 스타일
        const title = this.container.querySelector('.score-type-header h2');
        if (title) {
            title.style.margin = '0';
            title.style.fontSize = '18px';
            title.style.fontWeight = 'bold';
            title.style.textAlign = 'center';
        }
        
        // 닫기 버튼 스타일
        const closeButton = this.container.querySelector('.score-type-close');
        if (closeButton) {
            closeButton.style.position = 'absolute';
            closeButton.style.top = '15px';
            closeButton.style.right = '15px';
            closeButton.style.background = 'none';
            closeButton.style.border = 'none';
            closeButton.style.fontSize = '24px';
            closeButton.style.cursor = 'pointer';
        }
        
        // 콘텐츠 영역 스타일
        const content = this.container.querySelector('.score-type-content');
        if (content) {
            content.style.flex = '1';
            content.style.padding = '20px';
            content.style.overflowY = 'auto';
        }
        
        // 로딩 표시 스타일
        const loading = this.container.querySelector('.score-type-loading');
        if (loading) {
            loading.style.textAlign = 'center';
            loading.style.padding = '40px 0';
            loading.style.color = '#666';
        }
        
        // 빈 목록 표시 스타일
        const empty = this.container.querySelector('.score-type-empty');
        if (empty) {
            empty.style.textAlign = 'center';
            empty.style.padding = '40px 0';
            empty.style.color = '#666';
        }
        
        // 점수 유형 목록 스타일
        const list = this.container.querySelector('.score-type-list');
        if (list) {
            list.style.display = 'flex';
            list.style.flexDirection = 'column';
            list.style.gap = '15px';
        }
        
        // 각 항목 스타일
        const items = this.container.querySelectorAll('.score-type-item');
        items.forEach(item => {
            item.style.display = 'flex';
            item.style.justifyContent = 'space-between';
            item.style.alignItems = 'center';
            item.style.padding = '15px';
            item.style.backgroundColor = '#f8f9fa';
            item.style.borderRadius = '8px';
            item.style.boxShadow = '0 1px 3px rgba(0,0,0,0.1)';
        });
        
        // 점수 유형 정보 스타일
        const infos = this.container.querySelectorAll('.score-type-info');
        infos.forEach(info => {
            info.style.flex = '1';
        });
        
        // 점수 유형 이름 스타일
        const names = this.container.querySelectorAll('.score-type-name');
        names.forEach(name => {
            name.style.fontWeight = 'bold';
            name.style.marginBottom = '5px';
            name.style.fontSize = '16px';
        });
        
        // 점수 유형 설명 스타일
        const descriptions = this.container.querySelectorAll('.score-type-description');
        descriptions.forEach(desc => {
            desc.style.fontSize = '14px';
            desc.style.color = '#666';
        });
        
        // 토글 스위치 컨테이너 스타일
        const toggles = this.container.querySelectorAll('.score-type-toggle');
        toggles.forEach(toggle => {
            toggle.style.marginLeft = '10px';
        });
        
        // 체크박스 컨테이너 스타일
        const checkboxContainers = this.container.querySelectorAll('.checkbox-container');
        checkboxContainers.forEach(container => {
            container.style.display = 'flex';
            container.style.alignItems = 'center';
            container.style.cursor = 'pointer';
        });
        
        // 체크박스 스타일
        const checkboxes = this.container.querySelectorAll('.checkbox-container input');
        checkboxes.forEach(checkbox => {
            checkbox.style.marginRight = '8px';
            checkbox.style.cursor = 'pointer';
        });
        
        // 체크박스 텍스트 스타일
        const checkboxLabels = this.container.querySelectorAll('.checkbox-container span');
        checkboxLabels.forEach(label => {
            label.style.fontSize = '14px';
            label.style.color = '#333';
        });
        
        // 푸터 스타일
        const footer = this.container.querySelector('.score-type-footer');
        if (footer) {
            footer.style.padding = '15px 20px';
            footer.style.borderTop = '1px solid #eee';
            footer.style.textAlign = 'center';
        }
        
        // 도움말 텍스트 스타일
        const help = this.container.querySelector('.score-type-help');
        if (help) {
            help.style.margin = '0';
            help.style.fontSize = '14px';
            help.style.color = '#666';
        }
    }
    
    /**
     * 이벤트 리스너 추가
     */
    addEventListeners() {
        // 닫기 버튼 클릭 이벤트
        const closeButton = this.container.querySelector('.score-type-close');
        if (closeButton) {
            closeButton.addEventListener('click', this.hide.bind(this));
        }
        
        // 체크박스 변경 이벤트
        const checkboxes = this.container.querySelectorAll('.checkbox-container input');
        checkboxes.forEach(checkbox => {
            checkbox.addEventListener('change', (e) => {
                const scoreTypeId = e.target.closest('.score-type-item').dataset.id;
                const isEnabled = e.target.checked;
                
                this.toggleScoreType(scoreTypeId, isEnabled);
            });
        });
    }
    
    /**
     * 점수 유형 상태 토글
     */
    async toggleScoreType(scoreTypeId, isEnabled) {
        if (!window.authTokens || !window.authTokens.isLoggedIn) {
            alert('로그인이 필요한 기능입니다.');
            return;
        }
        
        try {
            // API 경로 결정 - 활성화/비활성화 엔드포인트 사용
            const endpoint = isEnabled 
                ? `/api/v1/estates/score-types/${scoreTypeId}/enable`  // 체크됨 - 활성화
                : `/api/v1/estates/score-types/${scoreTypeId}/disable`; // 체크 해제 - 비활성화
            
            // API 기본 URL 가져오기 (interaction 모듈에서)
            const apiBaseUrl = window.interactionModule ? window.interactionModule.apiBaseUrl : 'http://localhost:8080';
            const fullUrl = `${apiBaseUrl}${endpoint}`;
                
            console.log(`점수 유형 ${scoreTypeId} ${isEnabled ? '활성화' : '비활성화'} 요청: ${fullUrl}`);
            
            // 인터랙션 ID 결정
            const interactionId = isEnabled ? 'enableScoreType' : 'disableScoreType';
            
            // 인터랙션 모듈이 있으면 화살표와 테이블 하이라이트를 위해 인터랙션 이벤트 발생
            if (window.interactionModule) {
                try {
                    console.log(`인터랙션 모듈 사용: ${interactionId} 인터랙션 트리거`);
                    
                    // 인터랙션 모듈을 통해 API 호출
                    const responseData = await window.interactionModule.triggerInteraction(interactionId, { 
                        id: scoreTypeId, 
                        userId: window.authTokens.userId,
                        scoreTypeId: scoreTypeId
                    });
                    
                    console.log(`인터랙션 응답:`, responseData);
                    
                    // 토글 결과를 UI에 즉시 반영 (낙관적 UI 업데이트)
                    const scoreType = this.scoreTypes.find(type => type.id === parseInt(scoreTypeId));
                    if (scoreType) {
                        scoreType.enabled = isEnabled;
                        this.renderContent();
                    }
                    
                    console.log(`점수 유형 ${scoreTypeId}가 ${isEnabled ? '활성화' : '비활성화'} 되었습니다.`);
                    return;
                } catch (error) {
                    console.error(`인터랙션 모듈을 통한 ${isEnabled ? '활성화' : '비활성화'} 실패:`, error);
                    // 실패 시 직접 API 호출로 fallback
                }
            }
            
            // API 호출
            const response = await fetch(fullUrl, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${window.authTokens.accessToken}`,
                    'Content-Type': 'application/json'
                }
            });
            
            if (!response.ok) {
                throw new Error(`API 요청 실패: ${response.status}`);
            }
            
            // 토글 결과를 UI에 즉시 반영 (낙관적 UI 업데이트)
            const scoreType = this.scoreTypes.find(type => type.id === parseInt(scoreTypeId));
            if (scoreType) {
                scoreType.enabled = isEnabled;
                this.renderContent();
            }
            
            console.log(`점수 유형 ${scoreTypeId}가 ${isEnabled ? '활성화' : '비활성화'} 되었습니다.`);
            
        } catch (error) {
            console.error('점수 유형 상태 변경 실패:', error);
            alert('점수 유형 상태 변경에 실패했습니다.');
            
            // 실패 시 전체 목록 다시 로드
            this.loadScoreTypes();
        }
    }
    
    /**
     * 점수 유형 목록 로드
     */
    async loadScoreTypes() {
        this.isLoading = true;
        this.renderContent();
        
        try {
            // API 요청을 위한 엔드포인트
            const endpoint = '/api/v1/estates/score-types';
            const headers = {};
            
            // 인증 토큰 추가 (로그인한 경우)
            if (window.authTokens && window.authTokens.isLoggedIn) {
                headers['Authorization'] = `Bearer ${window.authTokens.accessToken}`;
            }
            
            // 인터랙션 모듈이 있으면 화살표와 테이블 하이라이트를 위해 인터랙션 이벤트 발생
            if (window.interactionModule) {
                console.log('인터랙션 모듈 사용: scoreTypes 인터랙션 트리거');
                
                // 인터랙션 모듈을 통해 API 호출
                const responseData = await window.interactionModule.triggerInteraction('scoreTypes', {
                    userId: window.authTokens ? window.authTokens.userId : null
                });
                
                if (responseData) {
                    console.log('점수 유형 데이터:', responseData);
                    this.scoreTypes = responseData;
                    this.isLoading = false;
                    this.renderContent();
                    return;
                }
            }
            
            // 인터랙션 모듈이 없거나 실패한 경우, 직접 API 호출
            console.log('직접 API 호출로 대체');
            
            // API 기본 URL 가져오기
            const apiBaseUrl = window.interactionModule ? window.interactionModule.apiBaseUrl : 'http://localhost:8080';
            const fullUrl = `${apiBaseUrl}${endpoint}`;
            
            console.log(`API 요청: ${fullUrl}`);
            
            // API 요청 수행
            const response = await fetch(fullUrl, { headers });
            
            if (!response.ok) {
                throw new Error(`API 요청 실패: ${response.status}`);
            }
            
            // API 응답 데이터 파싱
            const data = await response.json();
            console.log('점수 유형 데이터:', data);
            
            this.scoreTypes = data;
            this.isLoading = false;
            this.renderContent();
            
        } catch (error) {
            console.error('점수 유형 목록 로드 실패:', error);
            this.isLoading = false;
            this.scoreTypes = [];
            this.renderContent();
        }
    }
    
    /**
     * 컴포넌트 표시
     */
    show() {
        if (!this.initialized) {
            this.initialize();
        }
        
        // 데이터 로드
        this.loadScoreTypes();
        
        // 화면에 표시
        if (this.container) {
            this.container.style.display = 'flex';
            
            // 애니메이션 효과를 위해 약간의 지연 후 transform 적용
            setTimeout(() => {
                this.container.style.transform = 'translateY(0)';
            }, 10);
        }
    }
    
    /**
     * 컴포넌트 숨김
     */
    hide() {
        if (this.container) {
            // 슬라이드 애니메이션으로 숨김
            this.container.style.transform = 'translateY(100%)';
            
            // 애니메이션 완료 후 숨김 처리
            setTimeout(() => {
                this.container.style.display = 'none';
            }, 300); // CSS transition 시간과 일치
        }
    }
}

// 싱글톤 인스턴스 생성
const scoreTypeComponent = new ScoreTypeComponent();