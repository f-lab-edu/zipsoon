/**
 * 매물 상세 정보 컴포넌트
 * 마커 클릭 시 하단에서 슬라이드업되는 상세 정보 화면을 관리합니다.
 */
class EstateDetailComponent {
    constructor() {
        this.container = null;
        this.currentEstate = null;
        this.initialized = false;
        
        // 이벤트 리스너 등록
        document.addEventListener('estateMarkerClicked', this.handleMarkerClick.bind(this));
    }
    
    /**
     * 컴포넌트 초기화 - DOM에 상세 페이지 요소 추가
     */
    initialize() {
        if (this.initialized) return;
        
        // 이미 컨테이너가 있는지 확인
        const existingContainer = document.querySelector('.estate-detail-container');
        if (existingContainer) {
            this.container = existingContainer;
            this.initialized = true;
            return;
        }
        
        // 상세 페이지 컨테이너 생성
        this.container = document.createElement('div');
        this.container.className = 'estate-detail-container';
        
        // 빈 상태로 컨테이너 초기화
        this.container.innerHTML = `
            <div class="estate-detail-header">
                <button class="estate-detail-close">&times;</button>
                <div class="estate-detail-score">
                    <div class="score-circle" style="
                        background-color: #4CAF50;
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        width: 60px;
                        height: 60px;
                        border-radius: 50%;
                        margin-right: 15px;
                        text-align: center;
                    ">
                        <span style="font-size: 0.7rem; margin-bottom: -2px; color: white;">총점</span>
                        <span class="score-value" style="font-size: 1.2rem; font-weight: bold; color: white;">0.0</span>
                    </div>
                    <div class="estate-detail-info">
                        <h2 class="estate-detail-title">매물 정보</h2>
                        <p class="estate-detail-subtitle">로딩 중...</p>
                        <div class="estate-detail-type-badges" style="margin-top: 10px;"></div>
                    </div>
                </div>
            </div>
            <div class="estate-detail-content">
                <div class="estate-images"></div>
                <div class="score-details"></div>
            </div>
        `;
        
        // 닫기 버튼에 이벤트 리스너 추가
        const closeButton = this.container.querySelector('.estate-detail-close');
        closeButton.addEventListener('click', this.hide.bind(this));
        
        // 지도 컨테이너에 상세 페이지 요소 추가
        const mapContainer = document.querySelector('.map-container');
        if (mapContainer) {
            mapContainer.appendChild(this.container);
            this.initialized = true;
        } else {
            console.error('지도 컨테이너를 찾을 수 없어 상세 페이지를 추가할 수 없습니다.');
        }
    }
    
    /**
     * 마커 클릭 이벤트 핸들러
     * @param {CustomEvent} event - 마커 클릭 이벤트 (detail에 매물 정보 포함)
     */
    async handleMarkerClick(event) {
        // 컴포넌트가 초기화되지 않았다면 초기화
        if (!this.initialized) {
            this.initialize();
        }
        
        const estateData = event.detail;
        if (!estateData) {
            console.error('매물 데이터가 없습니다.');
            return;
        }
        
        console.log('매물 상세 정보 불러오기 시작:', estateData.id);
        
        // 기본 정보를 현재 매물로 저장하고 로딩 상태로 UI 업데이트
        this.currentEstate = estateData;
        
        // 일단 기본 정보로 UI 표시 (로딩 중 표시)
        this.updateContent(estateData);
        
        // 상세 페이지 표시
        this.show();
        
        try {
            // 로그인 상태 확인
            if (window.authTokens && window.authTokens.isLoggedIn) {
                // 인터랙션 모듈을 통해 매물 상세 조회 트리거
                if (window.interactionModule) {
                    // estateDetail 인터랙션 호출
                    const detailData = await window.interactionModule.triggerInteraction('estateDetail', { id: estateData.id });
                    console.log('매물 상세 정보 로드 완료:', detailData);
                    
                    // 커스텀 이벤트 발생 - 매물 상세 정보 로드 완료
                    const estateDetailEvent = new CustomEvent('estateDetailLoaded', { 
                        detail: { 
                            id: estateData.id,
                            data: detailData
                        } 
                    });
                    document.dispatchEvent(estateDetailEvent);
                    
                    // 현재 매물 정보 갱신
                    if (detailData) {
                        this.currentEstate = detailData;
                        
                        // 상세 페이지 내용 업데이트
                        this.updateContent(detailData);
                    }
                } else {
                    // API 엔드포인트 직접 호출 (fallback)
                    console.log('인터랙션 모듈이 없음, 직접 API 호출');
                    const endpoint = API_ENDPOINTS['/api/v1/estates/:id'];
                    const apiBaseUrl = 'http://localhost:8080';
                    
                    // URL 템플릿 변수 처리
                    const urlPath = endpoint.url.replace('{id}', estateData.id);
                    const url = `${apiBaseUrl}${urlPath}`;
                    
                    const options = {
                        method: endpoint.method,
                        headers: {
                            'Content-Type': 'application/json',
                            'Accept': 'application/json',
                            'Authorization': `Bearer ${window.authTokens.accessToken}`
                        }
                    };
                    
                    console.log(`상세 정보 API 직접 호출: ${url}`);
                    
                    const response = await fetch(url, options);
                    if (!response.ok) {
                        throw new Error(`API 요청 실패: ${response.status} ${response.statusText}`);
                    }
                    
                    const detailData = await response.json();
                    const formattedData = endpoint.responseFormatter ? endpoint.responseFormatter(detailData) : detailData;
                    
                    this.currentEstate = formattedData;
                    this.updateContent(formattedData);
                }
            } else {
                console.log('로그인 필요: 기본 매물 정보만 표시합니다.');
            }
        } catch (error) {
            console.error('상세 정보 로드 실패:', error);
            // 에러 메시지를 UI에 표시할 수 있음
        }
    }
    
    /**
     * 상세 페이지 내용 업데이트
     * @param {Object} estateData - 매물 데이터
     */
    updateContent(estateData) {
        if (!this.container) return;
        
        // 가격 포맷팅 함수
        const formatPrice = (price) => {
            return price ? price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',') : '정보 없음';
        };
        
        // 총점 정보 업데이트
        const scoreCircle = this.container.querySelector('.score-circle');
        const scoreValue = this.container.querySelector('.score-value');
        
        const totalScore = (estateData.score && estateData.score.total) ? estateData.score.total : 0;
        
        // 점수에 따라 원형 색상 설정
        let scoreColor = '#e0e0e0'; // 기본 회색
        if (totalScore >= 8) {
            scoreColor = '#4CAF50'; // 녹색 (높은 점수)
        } else if (totalScore >= 6) {
            scoreColor = '#FFC107'; // 노랑 (중간 점수)
        } else if (totalScore > 0) {
            scoreColor = '#FF5722'; // 주황 (낮은 점수)
        }
        
        // 스타일 설정 (HTML 초기화 시 설정한 스타일과 일치하게 유지)
        scoreCircle.style.backgroundColor = scoreColor;
        scoreCircle.style.display = 'flex';
        scoreCircle.style.flexDirection = 'column';
        scoreCircle.style.alignItems = 'center';
        scoreCircle.style.justifyContent = 'center';
        scoreCircle.style.width = '60px';
        scoreCircle.style.height = '60px';
        scoreCircle.style.borderRadius = '50%';
        scoreCircle.style.marginRight = '15px';
        scoreCircle.style.textAlign = 'center';
        
        // 점수 값 업데이트 (HTML 구조가 변경되면 이 부분도 수정 필요)
        // '총점' 라벨이 이미 있으므로 점수 값만 업데이트
        scoreValue.textContent = totalScore ? totalScore.toFixed(1) : '?';
        scoreValue.style.fontSize = '1.2rem';
        scoreValue.style.fontWeight = 'bold';
        scoreValue.style.color = 'white';
        
        // 제목 및 부제목 업데이트
        const title = this.container.querySelector('.estate-detail-title');
        const subtitle = this.container.querySelector('.estate-detail-subtitle');
        
        title.textContent = estateData.name || '이름 없는 매물';
        
        // 가격 정보 구성
        let priceText = '';
        if (estateData.price) {
            priceText = formatPrice(estateData.price) + '만원';
            if (estateData.rentPrice) {
                priceText += ' / ' + formatPrice(estateData.rentPrice) + '만원';
            }
        } else {
            priceText = '가격 정보 없음';
        }
        
        subtitle.textContent = `${estateData.address || '주소 정보 없음'} · ${priceText}`;
        
        // 매물 유형 뱃지 업데이트
        const badgesContainer = this.container.querySelector('.estate-detail-type-badges');
        badgesContainer.innerHTML = '';
        badgesContainer.style.display = 'flex';
        badgesContainer.style.gap = '8px';
        badgesContainer.style.marginTop = '10px';
        badgesContainer.style.marginBottom = '15px';
        badgesContainer.style.flexWrap = 'wrap';
        
        // 플랫폼 뱃지 추가 (네이버로 하드코딩)
        const platformBadge = document.createElement('div');
        platformBadge.className = 'estate-badge platform-badge';
        platformBadge.textContent = '네이버';
        platformBadge.style.backgroundColor = '#03C75A';
        platformBadge.style.color = 'white';
        platformBadge.style.padding = '3px 8px';
        platformBadge.style.borderRadius = '4px';
        platformBadge.style.fontSize = '0.8rem';
        platformBadge.style.fontWeight = 'bold';
        badgesContainer.appendChild(platformBadge);
        
        // 매물 유형 뱃지 추가
        if (estateData.type) {
            const typeBadge = document.createElement('div');
            typeBadge.className = 'estate-badge type-badge';
            typeBadge.textContent = this.getEstateTypeName(estateData.type);
            typeBadge.style.backgroundColor = '#4361EE';
            typeBadge.style.color = 'white';
            typeBadge.style.padding = '3px 8px';
            typeBadge.style.borderRadius = '4px';
            typeBadge.style.fontSize = '0.8rem';
            typeBadge.style.fontWeight = 'bold';
            badgesContainer.appendChild(typeBadge);
        }
        
        // 거래 유형 뱃지 추가
        if (estateData.tradeType) {
            const tradeBadge = document.createElement('div');
            tradeBadge.className = 'estate-badge trade-badge';
            tradeBadge.textContent = this.getTradeTypeName(estateData.tradeType);
            
            // 거래 유형에 따라 다른 색상 부여
            let bgColor = '#FF9F1C'; // 기본 색상
            if (estateData.tradeType === '전세') {
                bgColor = '#2EC4B6'; // 전세
            } else if (estateData.tradeType === '매매') {
                bgColor = '#E71D36'; // 매매
            }
            
            tradeBadge.style.backgroundColor = bgColor;
            tradeBadge.style.color = 'white';
            tradeBadge.style.padding = '3px 8px';
            tradeBadge.style.borderRadius = '4px';
            tradeBadge.style.fontSize = '0.8rem';
            tradeBadge.style.fontWeight = 'bold';
            badgesContainer.appendChild(tradeBadge);
        }
        
        // 이미지 갤러리 업데이트
        const imagesContainer = this.container.querySelector('.estate-images');
        imagesContainer.innerHTML = '';

        const hasValidImages = estateData.images &&
                              Array.isArray(estateData.images) &&
                              estateData.images.length > 0;
        
        if (hasValidImages) {
            // 이미지가 있는 경우 이미지 갤러리 생성
            estateData.images.forEach(imageUrl => {
                const imgContainer = document.createElement('div');
                imgContainer.className = 'estate-image';
                imgContainer.style.display = 'flex';
                imgContainer.style.alignItems = 'center';
                imgContainer.style.justifyContent = 'center';
                
                const img = document.createElement('img');
                img.src = imageUrl;
                img.alt = estateData.name || '매물 이미지';
                img.style.maxWidth = '100%';
                img.style.maxHeight = '100%';
                img.style.objectFit = 'contain';
                
                // 이미지 로드 오류 처리
                img.onerror = () => {
                    imgContainer.textContent = '이미지 로드 실패';
                    imgContainer.style.backgroundColor = '#f5f5f5';
                };
                
                imgContainer.appendChild(img);
                imagesContainer.appendChild(imgContainer);
            });
        } else {
            // 이미지가 없는 경우 기본 메시지 표시
            const noImg = document.createElement('div');
            noImg.className = 'estate-image';
            noImg.style.backgroundColor = '#f5f5f5';
            noImg.style.display = 'flex';
            noImg.style.alignItems = 'center';
            noImg.style.justifyContent = 'center';
            noImg.textContent = '이미지가 없습니다';
            imagesContainer.appendChild(noImg);
        }
        
        // 점수 세부 정보 업데이트
        const scoreDetailsContainer = this.container.querySelector('.score-details');
        scoreDetailsContainer.innerHTML = '';
        
        // 실제 점수 데이터 렌더링
        if (estateData.score && estateData.score.factors && estateData.score.factors.length > 0) {
            // 각 점수 요소 추가
            estateData.score.factors.forEach(factor => {
                // score_type.description, score_type.name 매핑
                const name = factor.name;
                const description = factor.description;
                
                // estate_score.normalized_score 또는 estate_score.raw_score 사용
                // normalized_score가 없으면 raw_score 사용
                const score = factor.score; // 프론트엔드에서는 이미 normalized_score로 받아짐
                
                this.addScoreItem(scoreDetailsContainer, name, score, description);
            });
        } else {
            // 점수 데이터가 없는 경우 안내 메시지 표시
            const noDataDiv = document.createElement('div');
            noDataDiv.className = 'score-no-data';
            noDataDiv.textContent = '점수 데이터가 없습니다.';
            scoreDetailsContainer.appendChild(noDataDiv);
        }
    }
    
    /**
     * 점수 항목 추가
     * @param {HTMLElement} container - 점수 항목을 추가할 컨테이너
     * @param {String} name - 점수 항목 이름
     * @param {Number|null} value - 점수 값 (null인 경우 '?' 표시)
     * @param {String} description - 점수 항목 설명
     */
    addScoreItem(container, name, value, description) {
        const item = document.createElement('div');
        item.className = 'score-item';
        
        // 아이콘 (실제 아이콘은 나중에 추가해도 됨)
        const icon = document.createElement('div');
        icon.className = 'score-item-icon';
        icon.textContent = 'img';
        
        // 내용
        const content = document.createElement('div');
        content.className = 'score-item-content';
        
        // 제목 줄
        const title = document.createElement('div');
        title.className = 'score-item-title';
        
        // 이름
        const nameSpan = document.createElement('span');
        nameSpan.className = 'score-item-name';
        nameSpan.textContent = name;
        
        // 점수 값
        const valueDiv = document.createElement('div');
        valueDiv.className = 'score-item-value';
        
        const scoreCircle = document.createElement('div');
        
        if (value !== null) {
            scoreCircle.className = 'score-circle-small';
            
            // 점수에 따라 원형 색상 설정
            let scoreColor = '#e0e0e0';
            if (value >= 8) {
                scoreColor = '#4CAF50';
            } else if (value >= 6) {
                scoreColor = '#FFC107';
            } else {
                scoreColor = '#FF5722';
            }
            
            // 작은 원형 스타일링
            scoreCircle.style.backgroundColor = scoreColor;
            scoreCircle.style.display = 'flex';
            scoreCircle.style.alignItems = 'center';
            scoreCircle.style.justifyContent = 'center';
            scoreCircle.style.width = '30px';
            scoreCircle.style.height = '30px';
            scoreCircle.style.borderRadius = '50%';
            scoreCircle.style.color = 'white';
            scoreCircle.style.fontWeight = 'bold';
            scoreCircle.style.fontSize = '0.9rem';
            
            scoreCircle.textContent = value.toFixed(1);
        } else {
            scoreCircle.className = 'score-circle-small score-empty';
            
            // 빈 원형 스타일링 (색상만 다르게)
            scoreCircle.style.backgroundColor = '#e0e0e0';
            scoreCircle.style.display = 'flex';
            scoreCircle.style.alignItems = 'center';
            scoreCircle.style.justifyContent = 'center';
            scoreCircle.style.width = '30px';
            scoreCircle.style.height = '30px';
            scoreCircle.style.borderRadius = '50%';
            scoreCircle.style.color = 'white';
            scoreCircle.style.fontWeight = 'bold';
            scoreCircle.style.fontSize = '0.9rem';
            
            scoreCircle.textContent = '?';
        }
        
        valueDiv.appendChild(scoreCircle);
        
        // 제목 줄 구성
        title.appendChild(nameSpan);
        title.appendChild(valueDiv);
        
        // 설명
        const desc = document.createElement('div');
        desc.className = 'score-item-description';
        desc.textContent = description;
        
        // 내용 구성
        content.appendChild(title);
        content.appendChild(desc);
        
        // 항목 구성
        item.appendChild(icon);
        item.appendChild(content);
        
        // 컨테이너에 추가
        container.appendChild(item);
    }
    
    /**
     * 플랫폼 이름 반환
     * @param {String} platformType - 플랫폼 타입 코드
     * @returns {String} 사용자 친화적인 플랫폼 이름
     */
    getPlatformName(platformType) {
        const platformNames = {
            'NAVER': '네이버',
            'JIKBANG': '직방',
            'DABANG': '다방',
            'PETERPAN': '피터팬',
            'ZIGBANG': '직방'
        };
        
        return platformNames[platformType] || platformType;
    }
    
    /**
     * 매물 유형 이름 반환
     * @param {String} estateType - 매물 유형 코드
     * @returns {String} 사용자 친화적인 매물 유형 이름
     */
    getEstateTypeName(estateType) {
        const typeNames = {
            'APT': '아파트',
            'OFFICETEL': '오피스텔',
            'ONEROOM': '원룸',
            'VILLA': '빌라',
            'HOUSE': '주택'
        };
        
        return typeNames[estateType] || estateType;
    }
    
    /**
     * 거래 유형 이름 반환
     * @param {String} tradeType - 거래 유형 코드
     * @returns {String} 사용자 친화적인 거래 유형 이름
     */
    getTradeTypeName(tradeType) {
        const tradeNames = {
            'SALE': '매매',
            'JEONSE': '전세',
            'RENT': '월세'
        };
        
        return tradeNames[tradeType] || tradeType;
    }
    
    /**
     * 상세 페이지 표시
     */
    show() {
        if (!this.container) return;
        
        // 컨테이너 표시
        this.container.style.display = 'block';
        
        // 애니메이션 효과를 위해 약간의 지연 후 활성화 클래스 추가
        setTimeout(() => {
            this.container.classList.add('active');
        }, 10);
    }
    
    /**
     * 상세 페이지 숨김
     */
    hide() {
        if (!this.container) return;
        
        // 활성화 클래스 제거
        this.container.classList.remove('active');
        
        // 애니메이션 완료 후 숨김 처리
        setTimeout(() => {
            this.container.style.display = 'none';
        }, 300); // CSS transition 시간과 일치시킴
    }
}

// 싱글톤 인스턴스 생성
const estateDetailComponent = new EstateDetailComponent();