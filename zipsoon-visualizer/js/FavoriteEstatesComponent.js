/**
 * 찜한 매물 목록 컴포넌트
 * 사용자가 찜한 매물 목록을 조회하고 표시합니다.
 */
class FavoriteEstatesComponent {
    constructor() {
        this.container = null;
        this.initialized = false;
        
        // 현재 페이지 상태 (1부터 시작)
        this.currentPage = 1;
        this.pageSize = 10;
        this.totalElements = 0;
        this.totalPages = 0;
    }
    
    /**
     * 컴포넌트 초기화 및 표시
     */
    show(page = 1, size = 10) {
        // 로그인 상태 확인
        if (!window.authTokens || !window.authTokens.isLoggedIn) {
            alert('로그인이 필요한 기능입니다.');
            return;
        }
        
        // 기존 컨테이너 제거
        this.remove();
        
        // 페이지 정보 저장
        this.currentPage = page;
        this.pageSize = size;
        
        // 로딩 표시
        this.showLoading();
        
        // API 호출
        interactionModule.triggerInteraction('favoriteEstates', { page, size })
            .then(response => {
                console.log('찜한 매물 목록 조회 성공:', response);
                
                // 로딩 표시 제거
                this.hideLoading();
                
                // 데이터 저장
                this.totalElements = response.totalElements;
                this.totalPages = response.totalPages;
                
                // 결과 컨테이너 생성
                this.createContainer(response);
            })
            .catch(error => {
                console.error('찜한 매물 목록 조회 실패:', error);
                
                // 로딩 표시 제거
                this.hideLoading();
                
                // 에러 메시지 표시
                alert('찜한 매물 목록을 불러오는데 실패했습니다.');
            });
    }
    
    /**
     * 로딩 인디케이터 표시
     */
    showLoading() {
        // 기존 로딩 인디케이터 제거
        this.hideLoading();
        
        // 로딩 표시
        const loadingElement = document.createElement('div');
        loadingElement.className = 'favorites-loading';
        loadingElement.textContent = '찜한 매물 목록을 불러오는 중...';
        loadingElement.style.position = 'absolute';
        loadingElement.style.top = '50%';
        loadingElement.style.left = '50%';
        loadingElement.style.transform = 'translate(-50%, -50%)';
        loadingElement.style.backgroundColor = 'rgba(255, 255, 255, 0.9)';
        loadingElement.style.padding = '20px';
        loadingElement.style.borderRadius = '10px';
        loadingElement.style.boxShadow = '0 2px 10px rgba(0, 0, 0, 0.1)';
        loadingElement.style.zIndex = '1002';
        
        document.querySelector('.map-container').appendChild(loadingElement);
    }
    
    /**
     * 로딩 인디케이터 숨기기
     */
    hideLoading() {
        const loadingElement = document.querySelector('.favorites-loading');
        if (loadingElement) {
            loadingElement.remove();
        }
    }
    
    /**
     * 결과 컨테이너 생성
     */
    createContainer(data) {
        // 결과 컨테이너 생성
        this.container = document.createElement('div');
        this.container.className = 'favorites-container';
        
        // 스타일 설정
        this.container.style.position = 'absolute';
        this.container.style.bottom = '0';
        this.container.style.left = '0';
        this.container.style.width = '100%';
        this.container.style.maxHeight = '80%';
        this.container.style.backgroundColor = 'white';
        this.container.style.zIndex = '1001';
        this.container.style.display = 'flex';
        this.container.style.flexDirection = 'column';
        this.container.style.transition = 'transform 0.3s ease-in-out';
        this.container.style.transform = 'translateY(100%)';
        this.container.style.borderTopLeftRadius = '15px';
        this.container.style.borderTopRightRadius = '15px';
        this.container.style.boxShadow = '0 -2px 10px rgba(0, 0, 0, 0.1)';
        
        // 헤더 생성
        const header = this.createHeader();
        
        // 컨텐츠 영역
        const content = this.createContent(data);
        
        // 페이지네이션
        const pagination = this.createPagination(data);
        
        // 요소들 조립
        this.container.appendChild(header);
        
        if (data.content && data.content.length > 0) {
            // 페이지 정보
            const pageInfo = this.createPageInfo(data);
            this.container.appendChild(pageInfo);
        }
        
        this.container.appendChild(content);
        
        if (data.totalPages > 1) {
            this.container.appendChild(pagination);
        }
        
        // 지도 컨테이너에 추가
        document.querySelector('.map-container').appendChild(this.container);
        
        // 애니메이션 효과를 위해 약간의 지연 후 트랜스폼 적용
        setTimeout(() => {
            this.container.style.transform = 'translateY(0)';
        }, 10);
    }
    
    /**
     * 헤더 영역 생성
     */
    createHeader() {
        const header = document.createElement('div');
        header.className = 'favorites-header';
        header.style.padding = '20px';
        header.style.borderBottom = '1px solid #eee';
        header.style.position = 'relative';
        
        // 닫기 버튼
        const closeButton = document.createElement('button');
        closeButton.textContent = '×';
        closeButton.style.position = 'absolute';
        closeButton.style.top = '15px';
        closeButton.style.right = '15px';
        closeButton.style.background = 'none';
        closeButton.style.border = 'none';
        closeButton.style.fontSize = '24px';
        closeButton.style.cursor = 'pointer';
        
        // 제목
        const title = document.createElement('h2');
        title.textContent = '내가 찜한 매물 목록';
        title.style.margin = '0';
        title.style.fontSize = '18px';
        title.style.fontWeight = 'bold';
        
        // 닫기 버튼 이벤트 추가
        closeButton.addEventListener('click', () => {
            this.hide();
        });
        
        header.appendChild(title);
        header.appendChild(closeButton);
        
        return header;
    }
    
    /**
     * 페이지 정보 영역 생성
     */
    createPageInfo(data) {
        const pageInfo = document.createElement('div');
        pageInfo.className = 'favorites-page-info';
        pageInfo.style.padding = '15px 20px';
        pageInfo.style.fontSize = '14px';
        pageInfo.style.color = '#666';
        
        const start = (data.page - 1) * data.size + 1;
        const end = Math.min(data.page * data.size, data.totalElements);
        pageInfo.textContent = `총 ${data.totalElements}개 중 ${start}-${end}번째 매물`;
        
        return pageInfo;
    }
    
    /**
     * 매물 목록 컨텐츠 영역 생성
     */
    createContent(data) {
        const content = document.createElement('div');
        content.className = 'favorites-content';
        content.style.flex = '1';
        content.style.overflowY = 'auto';
        content.style.padding = '0 20px';
        
        // 매물 목록 생성
        if (data.content && data.content.length > 0) {
            data.content.forEach(estate => {
                const estateItem = this.createEstateItem(estate);
                content.appendChild(estateItem);
            });
        } else {
            // 매물이 없는 경우
            const emptyMessage = document.createElement('div');
            emptyMessage.style.padding = '30px 0';
            emptyMessage.style.textAlign = 'center';
            emptyMessage.style.color = '#999';
            emptyMessage.textContent = '찜한 매물이 없습니다.';
            content.appendChild(emptyMessage);
        }
        
        return content;
    }
    
    /**
     * 페이지네이션 영역 생성
     */
    createPagination(data) {
        const pagination = document.createElement('div');
        pagination.className = 'favorites-pagination';
        pagination.style.display = 'flex';
        pagination.style.justifyContent = 'center';
        pagination.style.padding = '15px 0';
        pagination.style.borderTop = '1px solid #eee';
        
        // 이전 페이지 버튼
        const prevButton = document.createElement('button');
        prevButton.textContent = '이전';
        prevButton.style.marginRight = '10px';
        prevButton.style.padding = '5px 10px';
        prevButton.style.border = '1px solid #ddd';
        prevButton.style.borderRadius = '4px';
        prevButton.style.background = '#f8f8f8';
        prevButton.style.cursor = 'pointer';
        prevButton.disabled = data.page === 1;
        prevButton.style.opacity = data.page === 1 ? '0.5' : '1';
        
        // 다음 페이지 버튼
        const nextButton = document.createElement('button');
        nextButton.textContent = '다음';
        nextButton.style.padding = '5px 10px';
        nextButton.style.border = '1px solid #ddd';
        nextButton.style.borderRadius = '4px';
        nextButton.style.background = '#f8f8f8';
        nextButton.style.cursor = 'pointer';
        nextButton.disabled = data.page >= data.totalPages;
        nextButton.style.opacity = data.page >= data.totalPages ? '0.5' : '1';
        
        // 페이지 번호 표시
        const pageText = document.createElement('div');
        pageText.style.padding = '5px 10px';
        pageText.style.margin = '0 10px';
        pageText.textContent = `${data.page} / ${data.totalPages}`;
        
        // 이벤트 리스너 추가
        prevButton.addEventListener('click', () => {
            if (data.page > 1) {
                this.show(data.page - 1, data.size);
            }
        });
        
        nextButton.addEventListener('click', () => {
            if (data.page < data.totalPages) {
                this.show(data.page + 1, data.size);
            }
        });
        
        pagination.appendChild(prevButton);
        pagination.appendChild(pageText);
        pagination.appendChild(nextButton);
        
        return pagination;
    }
    
    /**
     * 매물 아이템 생성
     */
    createEstateItem(estate) {
        const item = document.createElement('div');
        item.className = 'estate-item';
        item.style.display = 'flex';
        item.style.padding = '15px 0';
        item.style.borderBottom = '1px solid #eee';
        item.style.cursor = 'pointer';
        
        // 매물 정보 영역
        const info = document.createElement('div');
        info.className = 'estate-info';
        info.style.flex = '1';
        
        // 매물 이름
        const name = document.createElement('div');
        name.className = 'estate-name';
        name.textContent = estate.name || '이름 없는 매물';
        name.style.fontSize = '16px';
        name.style.fontWeight = 'bold';
        name.style.marginBottom = '5px';
        
        // 매물 타입 및 거래 유형
        const type = document.createElement('div');
        type.className = 'estate-type';
        type.textContent = `${estate.typeName || estate.type || '매물'} ${estate.tradeTypeName || estate.tradeType || ''}`;
        type.style.fontSize = '14px';
        type.style.color = '#666';
        type.style.marginBottom = '5px';
        
        // 가격 포맷팅
        const formatPrice = (price) => {
            if (!price) return '';
            return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
        };
        
        // 매물 가격
        const price = document.createElement('div');
        price.className = 'estate-price';
        
        if (estate.price) {
            price.textContent = `${formatPrice(estate.price)}만원`;
            if (estate.rentPrice) {
                price.textContent += ` / ${formatPrice(estate.rentPrice)}만원`;
            }
        } else if (estate.rentPrice) {
            price.textContent = `${formatPrice(estate.rentPrice)}만원`;
        } else {
            price.textContent = '가격 정보 없음';
        }
        
        price.style.fontSize = '14px';
        price.style.fontWeight = 'bold';
        price.style.color = '#ff5252';
        
        // 점수 영역
        const score = document.createElement('div');
        score.className = 'estate-score';
        score.style.marginLeft = '15px';
        score.style.display = 'flex';
        score.style.alignItems = 'center';
        score.style.justifyContent = 'center';
        
        // 점수 원형 컨테이너
        const scoreCircle = document.createElement('div');
        scoreCircle.className = 'score-circle';
        
        // 점수에 따라 색상 결정
        const totalScore = estate.score && estate.score.total ? estate.score.total : 0;
        
        // 점수에 따라 원형 색상 설정
        let scoreColor = '#e0e0e0'; // 기본 회색
        if (totalScore >= 8) {
            scoreColor = '#4CAF50'; // 녹색 (높은 점수)
        } else if (totalScore >= 6) {
            scoreColor = '#FFC107'; // 노랑 (중간 점수)
        } else if (totalScore > 0) {
            scoreColor = '#FF5722'; // 주황 (낮은 점수)
        }
        
        // 스타일 설정
        scoreCircle.style.backgroundColor = scoreColor;
        scoreCircle.style.display = 'flex';
        scoreCircle.style.flexDirection = 'column';
        scoreCircle.style.alignItems = 'center';
        scoreCircle.style.justifyContent = 'center';
        scoreCircle.style.width = '40px';
        scoreCircle.style.height = '40px';
        scoreCircle.style.borderRadius = '50%';
        scoreCircle.style.textAlign = 'center';
        
        // 점수 값
        const scoreValue = document.createElement('div');
        scoreValue.className = 'score-value';
        scoreValue.textContent = totalScore ? totalScore.toFixed(1) : 'N/A';
        scoreValue.style.fontSize = '14px';
        scoreValue.style.fontWeight = 'bold';
        scoreValue.style.color = 'white';
        
        // 점수 라벨
        const scoreLabel = document.createElement('div');
        scoreLabel.className = 'score-label';
        scoreLabel.textContent = '점수';
        scoreLabel.style.fontSize = '10px';
        scoreLabel.style.color = 'white';
        
        // 아이템 클릭 이벤트 - 매물 상세 정보 표시
        item.addEventListener('click', () => {
            // 매물 상세 컴포넌트가 있다면 호출
            if (window.estateDetailComponent) {
                // 컨테이너 숨기기
                this.hide();
                
                // 매물 상세 정보 표시
                window.estateDetailComponent.loadEstateDetails(estate.id);
            }
        });
        
        // 요소 조립
        info.appendChild(name);
        info.appendChild(type);
        info.appendChild(price);
        
        // 점수 원형 안에 라벨과 값 추가
        scoreCircle.appendChild(scoreLabel);
        scoreCircle.appendChild(scoreValue);
        
        // 점수 원형을 점수 영역에 추가
        score.appendChild(scoreCircle);
        
        item.appendChild(info);
        item.appendChild(score);
        
        return item;
    }
    
    /**
     * 컨테이너 숨기기
     */
    hide() {
        if (this.container) {
            this.container.style.transform = 'translateY(100%)';
            
            // 애니메이션 완료 후 제거
            setTimeout(() => {
                this.remove();
            }, 300);
        }
    }
    
    /**
     * 컨테이너 제거
     */
    remove() {
        if (this.container) {
            this.container.remove();
            this.container = null;
        } else {
            // 기존 컨테이너가 DOM에 남아있을 수 있으므로 확인
            const existingContainer = document.querySelector('.favorites-container');
            if (existingContainer) {
                existingContainer.remove();
            }
        }
    }
}

// 싱글톤 인스턴스 생성
const favoriteEstatesComponent = new FavoriteEstatesComponent();