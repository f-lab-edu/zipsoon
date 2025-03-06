/**
 * 설정 및 사용자 프로필 컴포넌트
 * 사용자 프로필 정보와 앱 설정을 관리합니다.
 */
class SettingsComponent {
    constructor() {
        this.container = null;
        this.initialized = false;
        this.profileButton = null;
        
        // 초기화 시 프로필 버튼 추가
        this.addProfileButton();
        
        // 헤더에 프로필 버튼 추가
        document.addEventListener('DOMContentLoaded', () => {
            this.addProfileButton();
        });
    }
    
    /**
     * 우측 상단에 프로필 버튼 추가
     */
    addProfileButton() {
        // 이미 버튼이 있는지 확인
        if (this.profileButton) return;
        
        // 지도 컨테이너 확인
        const mapContainer = document.querySelector('.map-container');
        if (!mapContainer) {
            console.error('지도 컨테이너를 찾을 수 없어 프로필 버튼을 추가할 수 없습니다.');
            return;
        }
        
        // 프로필 버튼 생성
        this.profileButton = document.createElement('div');
        this.profileButton.className = 'profile-button';
        
        // 로그인 상태 및 이미지 URL 확인
        const isLoggedIn = window.authTokens && window.authTokens.isLoggedIn;
        const userImageUrl = isLoggedIn && window.authTokens.imageUrl;
        
        if (userImageUrl) {
            // 프로필 이미지가 있는 경우
            this.profileButton.innerHTML = `
                <img src="${userImageUrl}" alt="프로필" class="profile-image">
            `;
        } else {
            // 프로필 이미지가 없는 경우 - 회색 배경
            this.profileButton.innerHTML = `
                <div class="profile-placeholder"></div>
            `;
        }
        
        // 스타일 적용
        this.profileButton.style.position = 'absolute';
        this.profileButton.style.top = '15px';
        this.profileButton.style.right = '15px';
        this.profileButton.style.zIndex = '1000';
        this.profileButton.style.cursor = 'pointer';
        this.profileButton.style.borderRadius = '50%';
        this.profileButton.style.overflow = 'hidden';
        this.profileButton.style.width = '80px';
        this.profileButton.style.height = '80px';
        this.profileButton.style.boxShadow = '0 2px 5px rgba(0, 0, 0, 0.2)';
        this.profileButton.style.border = '2px solid gray'; // 검정색 테두리 추가
        
        if (userImageUrl) {
            // 이미지 스타일
            const profileImage = this.profileButton.querySelector('.profile-image');
            profileImage.style.width = '100%';
            profileImage.style.height = '100%';
            profileImage.style.objectFit = 'cover';
        } else {
            // 플레이스홀더 스타일
            const placeholder = this.profileButton.querySelector('.profile-placeholder');
            placeholder.style.width = '100%';
            placeholder.style.height = '100%';
            placeholder.style.backgroundColor = '#e0e0e0'; // 회색 배경
        }
        
        // 클릭 이벤트 리스너 추가
        this.profileButton.addEventListener('click', this.handleProfileClick.bind(this));
        
        // 맵 컨테이너에 추가
        mapContainer.appendChild(this.profileButton);
    }
    
    /**
     * 프로필 버튼 클릭 핸들러
     */
    handleProfileClick() {
        // 설정 페이지가 초기화되지 않았다면 초기화
        if (!this.initialized) {
            this.initialize();
        }
        
        // 설정 페이지 표시
        this.show();
    }
    
    /**
     * 컴포넌트 초기화 - DOM에 설정 페이지 요소 추가
     */
    initialize() {
        if (this.initialized) return;
        
        // 이미 컨테이너가 있는지 확인
        const existingContainer = document.querySelector('.settings-container');
        if (existingContainer) {
            this.container = existingContainer;
            this.initialized = true;
            return;
        }
        
        // 설정 페이지 컨테이너 생성
        this.container = document.createElement('div');
        this.container.className = 'settings-container';
        
        // 설정 페이지 초기화
        const userNameDisplay = window.authTokens && window.authTokens.isLoggedIn 
            ? `${window.authTokens.userId || 'User'}님, 안녕하세요.`
            : '게스트님, 안녕하세요.';
            
        // 로그인 상태 및 이미지 URL 확인
        const isLoggedIn = window.authTokens && window.authTokens.isLoggedIn;
        const userImageUrl = isLoggedIn && window.authTokens.imageUrl;
        
        // 프로필 이미지 또는 회색 배경으로 설정
        const profileImageHtml = userImageUrl 
            ? `<img src="${userImageUrl}" alt="프로필" class="profile-avatar-image">`
            : `<div class="profile-avatar-placeholder" style="width:100%; height:100%; background-color:#e0e0e0;"></div>`;
        
        this.container.innerHTML = `
            <div class="settings-header">
                <button class="settings-close">&times;</button>
                <div class="settings-profile">
                    <div class="profile-avatar">
                        ${profileImageHtml}
                    </div>
                    <h2 class="profile-name">${userNameDisplay}</h2>
                </div>
            </div>
            <div class="settings-content">
                <div class="settings-section">
                    <div class="settings-item">
                        <div class="settings-item-label">내 점수 목록</div>
                        <div class="settings-item-value">
                            <button class="settings-button" data-action="edit-score-types">수정</button>
                        </div>
                    </div>

                    <div class="settings-item">
                        <div class="settings-item-label">내가 찜한 매물 목록</div>
                        <div class="settings-item-value">
                            <button class="settings-button" data-action="view-favorites">조회</button>
                        </div>
                    </div>

                    <div class="settings-item">
                        <div class="settings-item-label">회원 탈퇴</div>
                        <div class="settings-item-value">
                            <button class="settings-button danger" data-action="delete-account">탈퇴</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        // 스타일 설정 - EstateDetailComponent와 유사하게 구현
        this.container.style.position = 'absolute';
        this.container.style.bottom = '0';
        this.container.style.left = '0';
        this.container.style.width = '100%';
        this.container.style.maxHeight = '75%'; // 화면의 최대 75% 높이만 사용
        this.container.style.backgroundColor = 'white';
        this.container.style.zIndex = '1001';
        this.container.style.display = 'none';
        this.container.style.flexDirection = 'column';
        this.container.style.transition = 'transform 0.3s ease-in-out';
        this.container.style.transform = 'translateY(100%)';
        this.container.style.borderTopLeftRadius = '15px';
        this.container.style.borderTopRightRadius = '15px';
        this.container.style.boxShadow = '0 -2px 10px rgba(0, 0, 0, 0.1)';
        
        // 헤더 스타일
        const header = this.container.querySelector('.settings-header');
        header.style.padding = '20px';
        header.style.borderBottom = '1px solid #eee';
        header.style.position = 'relative';
        
        // 닫기 버튼 스타일
        const closeButton = this.container.querySelector('.settings-close');
        closeButton.style.position = 'absolute';
        closeButton.style.top = '15px';
        closeButton.style.right = '15px';
        closeButton.style.background = 'none';
        closeButton.style.border = 'none';
        closeButton.style.fontSize = '24px';
        closeButton.style.cursor = 'pointer';
        
        // 프로필 섹션 스타일
        const profileSection = this.container.querySelector('.settings-profile');
        profileSection.style.display = 'flex';
        profileSection.style.alignItems = 'center';
        profileSection.style.marginTop = '10px';
        
        // 프로필 아바타 스타일
        const profileAvatar = this.container.querySelector('.profile-avatar');
        profileAvatar.style.width = '60px';
        profileAvatar.style.height = '60px';
        profileAvatar.style.borderRadius = '50%';
        profileAvatar.style.overflow = 'hidden';
        profileAvatar.style.marginRight = '15px';
        profileAvatar.style.border = '2px solid gray'; // 검정색 테두리 추가
        
        // 프로필 이미지 스타일 - NULL 체크 추가
        const profileImage = this.container.querySelector('.profile-avatar-image');
        if (profileImage) {
            profileImage.style.width = '100%';
            profileImage.style.height = '100%';
            profileImage.style.objectFit = 'cover';
        }
        
        // 플레이스홀더 스타일도 확인
        const placeholder = this.container.querySelector('.profile-avatar-placeholder');
        if (placeholder) {
            placeholder.style.width = '100%';
            placeholder.style.height = '100%';
            placeholder.style.backgroundColor = '#e0e0e0';
        }
        
        // 이름 스타일
        const profileName = this.container.querySelector('.profile-name');
        if (profileName) {
            profileName.style.margin = '0';
            profileName.style.fontSize = '16px';
            profileName.style.fontWeight = 'bold';
        }
        
        // 콘텐츠 스타일
        const content = this.container.querySelector('.settings-content');
        content.style.flex = '1';
        content.style.overflowY = 'auto';
        content.style.padding = '20px';
        
        // 섹션 스타일
        const sections = this.container.querySelectorAll('.settings-section');
        sections.forEach(section => {
            section.style.marginBottom = '20px';
        });
        
        // 항목 스타일
        const items = this.container.querySelectorAll('.settings-item');
        items.forEach(item => {
            item.style.display = 'flex';
            item.style.justifyContent = 'space-between';
            item.style.alignItems = 'center';
            item.style.padding = '15px 0';
            item.style.borderBottom = '1px solid #eee';
        });
        
        // 항목 라벨 스타일
        const labels = this.container.querySelectorAll('.settings-item-label');
        labels.forEach(label => {
            label.style.fontSize = '15px';
            label.style.fontWeight = '500';
        });
        
        // 버튼 스타일
        const buttons = this.container.querySelectorAll('.settings-button');
        buttons.forEach(button => {
            button.style.padding = '8px 12px';
            button.style.borderRadius = '4px';
            button.style.border = '1px solid #ddd';
            button.style.background = '#f8f8f8';
            button.style.cursor = 'pointer';
            button.style.fontSize = '13px';
        });
        
        // 위험 버튼 스타일
        const dangerButtons = this.container.querySelectorAll('.settings-button.danger');
        dangerButtons.forEach(button => {
            button.style.color = 'white';
            button.style.background = '#ff5252';
            button.style.border = '1px solid #ff5252';
        });
        
        // 닫기 버튼에 이벤트 리스너 추가
        closeButton.addEventListener('click', this.hide.bind(this));
        
        // 버튼들에 이벤트 리스너 추가
        this.addButtonEventListeners();
        
        // 지도 컨테이너에 설정 페이지 요소 추가
        const mapContainer = document.querySelector('.map-container');
        if (mapContainer) {
            mapContainer.appendChild(this.container);
            this.initialized = true;
        } else {
            console.error('지도 컨테이너를 찾을 수 없어 설정 페이지를 추가할 수 없습니다.');
        }
    }
    
    /**
     * 버튼 이벤트 리스너 추가
     */
    addButtonEventListeners() {
        if (!this.container) return;
        
        // 모든 설정 버튼 가져오기
        const buttons = this.container.querySelectorAll('.settings-button');
        
        // 각 버튼에 이벤트 리스너 추가
        buttons.forEach(button => {
            const action = button.getAttribute('data-action');
            
            button.addEventListener('click', () => {
                switch(action) {
                    case 'edit-region':
                        this.handleEditRegion();
                        break;
                    case 'edit-score-types':
                        this.handleEditScoreTypes();
                        break;
                    case 'view-favorites':
                        this.handleViewFavorites();
                        break;
                    case 'delete-account':
                        this.handleDeleteAccount();
                        break;
                }
            });
        });
    }
    
    /**
     * 내 점수 목록 수정 처리 - ScoreTypeComponent 표시
     */
    handleEditScoreTypes() {
        console.log('내 점수 목록 수정 클릭');
        
        // ScoreTypeComponent가 정의되어 있는지 확인
        if (typeof scoreTypeComponent !== 'undefined') {
            this.hide(); // 설정 페이지 숨기기
            scoreTypeComponent.show(); // 바로 점수 유형 페이지 표시
        } else {
            console.error('ScoreTypeComponent를 찾을 수 없습니다.');
            alert('점수 유형 관리 기능을 로드할 수 없습니다.');
        }
    }
    
    /**
     * 내가 찜한 매물 목록 조회 처리
     */
    handleViewFavorites() {
        console.log('내가 찜한 매물 목록 조회 클릭');
        
        // 설정 페이지 숨기기
        this.hide();
        
        // 찜한 매물 목록 컴포넌트 표시
        if (typeof favoriteEstatesComponent !== 'undefined') {
            favoriteEstatesComponent.show();
        } else {
            console.error('FavoriteEstatesComponent를 찾을 수 없습니다.');
            alert('찜한 매물 목록 기능을 로드할 수 없습니다.');
        }
    }
    
    /**
     * 회원 탈퇴 처리
     */
    handleDeleteAccount() {
        console.log('회원 탈퇴 클릭');
        
        if (confirm('정말 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
            alert('회원 탈퇴 API는 아직 준비 중입니다.');
        }
    }
    
    /**
     * 설정 페이지 표시
     */
    show() {
        if (!this.container) return;
        
        // 컨테이너 표시
        this.container.style.display = 'flex';
        
        // 애니메이션 효과를 위해 약간의 지연 후 트랜스폼 적용
        setTimeout(() => {
            this.container.style.transform = 'translateY(0)';
        }, 10);
    }
    
    /**
     * 설정 페이지 숨김
     */
    hide() {
        if (!this.container) return;
        
        // 트랜스폼 애니메이션 적용
        this.container.style.transform = 'translateY(100%)';
        
        // 애니메이션 완료 후 숨김 처리
        setTimeout(() => {
            this.container.style.display = 'none';
        }, 300); // CSS transition 시간과 일치시킴
    }
    
    /**
     * 설정 페이지 업데이트
     */
    updateContent() {
        if (!this.container) return;
        
        // 로그인 상태 확인
        const isLoggedIn = window.authTokens && window.authTokens.isLoggedIn;
        const userImageUrl = isLoggedIn && window.authTokens.imageUrl;
        
        // 로그인 상태에 따라 사용자 이름 업데이트
        const userNameDisplay = isLoggedIn
            ? `${window.authTokens.userId || 'User'}님, 안녕하세요.`
            : '게스트님, 안녕하세요.';
            
        const profileName = this.container.querySelector('.profile-name');
        if (profileName) {
            profileName.textContent = userNameDisplay;
        }
        
        // 프로필 이미지 또는 회색 배경 업데이트
        const profileAvatar = this.container.querySelector('.profile-avatar');
        if (profileAvatar) {
            // 기존 내용 초기화
            profileAvatar.innerHTML = '';
            
            if (userImageUrl) {
                // 이미지가 있는 경우
                profileAvatar.innerHTML = `<img src="${userImageUrl}" alt="프로필" class="profile-avatar-image">`;
                
                // 이미지 스타일 적용
                const profileImage = profileAvatar.querySelector('.profile-avatar-image');
                profileImage.style.width = '100%';
                profileImage.style.height = '100%';
                profileImage.style.objectFit = 'cover';
            } else {
                // 이미지가 없는 경우 회색 배경
                const placeholder = document.createElement('div');
                placeholder.className = 'profile-avatar-placeholder';
                placeholder.style.width = '100%';
                placeholder.style.height = '100%';
                placeholder.style.backgroundColor = '#e0e0e0';
                
                profileAvatar.appendChild(placeholder);
            }
            
            // 테두리 추가
            profileAvatar.style.border = '2px solid #000';
        }
        
        // 프로필 버튼도 업데이트
        if (this.profileButton) {
            // 버튼 내용 초기화
            this.profileButton.innerHTML = '';
            
            if (userImageUrl) {
                // 이미지가 있는 경우
                this.profileButton.innerHTML = `<img src="${userImageUrl}" alt="프로필" class="profile-image">`;
                
                // 이미지 스타일 적용
                const profileImage = this.profileButton.querySelector('.profile-image');
                profileImage.style.width = '100%';
                profileImage.style.height = '100%';
                profileImage.style.objectFit = 'cover';
            } else {
                // 이미지가 없는 경우 회색 배경
                const placeholder = document.createElement('div');
                placeholder.className = 'profile-placeholder';
                placeholder.style.width = '100%';
                placeholder.style.height = '100%';
                placeholder.style.backgroundColor = '#e0e0e0';
                
                this.profileButton.appendChild(placeholder);
            }
        }
    }
}

// 싱글톤 인스턴스 생성
const settingsComponent = new SettingsComponent();