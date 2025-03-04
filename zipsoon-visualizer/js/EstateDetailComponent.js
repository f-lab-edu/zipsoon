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
                    <div class="score-circle" style="background-color: #4CAF50;">
                        <span class="score-value">0.0</span>
                    </div>
                    <div>
                        <h2 class="estate-detail-title">매물 정보</h2>
                        <p class="estate-detail-subtitle">로딩 중...</p>
                    </div>
                </div>
                <div class="estate-detail-type-badges"></div>
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
    handleMarkerClick(event) {
        // 컴포넌트가 초기화되지 않았다면 초기화
        if (!this.initialized) {
            this.initialize();
        }
        
        const estateData = event.detail;
        if (!estateData) {
            console.error('매물 데이터가 없습니다.');
            return;
        }
        
        console.log('매물 상세 정보 표시:', estateData);
        
        // 현재 매물 정보 저장
        this.currentEstate = estateData;
        
        // 상세 페이지 내용 업데이트
        this.updateContent(estateData);
        
        // 상세 페이지 표시
        this.show();
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
        
        scoreCircle.style.backgroundColor = scoreColor;
        scoreValue.textContent = totalScore ? totalScore.toFixed(1) : '?';
        
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
        
        // 플랫폼 뱃지 추가
        if (estateData.platformType) {
            const platformBadge = document.createElement('div');
            platformBadge.className = `estate-type-badge badge-${estateData.platformType.toLowerCase()}`;
            platformBadge.textContent = this.getPlatformName(estateData.platformType);
            badgesContainer.appendChild(platformBadge);
        }
        
        // 매물 유형 뱃지 추가
        if (estateData.type) {
            const typeBadge = document.createElement('div');
            typeBadge.className = `estate-type-badge badge-${estateData.type.toLowerCase()}`;
            typeBadge.textContent = this.getEstateTypeName(estateData.type);
            badgesContainer.appendChild(typeBadge);
        }
        
        // 거래 유형 뱃지 추가
        if (estateData.tradeType) {
            const tradeBadge = document.createElement('div');
            tradeBadge.className = `estate-type-badge badge-${estateData.tradeType.toLowerCase()}`;
            tradeBadge.textContent = this.getTradeTypeName(estateData.tradeType);
            badgesContainer.appendChild(tradeBadge);
        }
        
        // 이미지 갤러리 업데이트
        const imagesContainer = this.container.querySelector('.estate-images');
        imagesContainer.innerHTML = '';
        
        if (estateData.imageUrls && estateData.imageUrls.length > 0) {
            estateData.imageUrls.forEach(imageUrl => {
                const img = document.createElement('img');
                img.className = 'estate-image';
                img.src = imageUrl;
                img.alt = estateData.name || '매물 이미지';
                imagesContainer.appendChild(img);
            });
        } else {
            // 이미지가 없는 경우 기본 이미지 표시
            const noImg = document.createElement('div');
            noImg.className = 'estate-image';
            noImg.style.backgroundColor = '#f5f5f5';
            noImg.style.display = 'flex';
            noImg.style.alignItems = 'center';
            noImg.style.justifyContent = 'center';
            noImg.textContent = '이미지 없음';
            imagesContainer.appendChild(noImg);
        }
        
        // 점수 세부 정보 업데이트
        const scoreDetailsContainer = this.container.querySelector('.score-details');
        scoreDetailsContainer.innerHTML = '';
        
        // 샘플 점수 항목 추가 (상세 점수 정보가 있다면 실제 데이터로 대체)
        this.addScoreItem(scoreDetailsContainer, '역세권', 8.5, '역, 정류장이 가까운 정도');
        this.addScoreItem(scoreDetailsContainer, '병원', 7.5, '의마나 병원이 많은지');
        this.addScoreItem(scoreDetailsContainer, '편의점', 6.5, '의마나 편의점이 많은지');
        this.addScoreItem(scoreDetailsContainer, '평탄한 지형', null, '주변 지형 경사 정도');
        this.addScoreItem(scoreDetailsContainer, '공원', null, '의마나 공원이 많은지');
        this.addScoreItem(scoreDetailsContainer, '사전 정의된 옵션1', null, '서버에서 사전 세팅');
        this.addScoreItem(scoreDetailsContainer, '사전 정의된 옵션2', null, '서버에서 사전 세팅');
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
        icon.textContent = '#';
        
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
            
            scoreCircle.style.backgroundColor = scoreColor;
            scoreCircle.textContent = value.toFixed(1);
        } else {
            scoreCircle.className = 'score-circle-small score-empty';
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