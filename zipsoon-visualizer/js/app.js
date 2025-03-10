/**
 * 메인 애플리케이션 진입점
 * 각 모듈을 초기화하고 연결합니다.
 */

// 사용자 인증 관련 전역 변수
window.authTokens = {
    accessToken: null,
    refreshToken: null,
    userId: null,
    isLoggedIn: false
};

$(() => {
    // OpenStreetMap 초기화
    mapModule.initialize();

    // ERD 커넥터 전역 변수 설정 (초기화는 mermaid 렌더링 후에 함)
    window.erdConnector = erdConnector;

    // 인터랙션 모듈 초기화
    window.interactionModule = interactionModule;
    interactionModule.loadInteractions(INTERACTIONS);

    // viewportChanged 이벤트 구독 (다른 컴포넌트에서 사용할 수 있음)
    document.addEventListener('viewportChanged', (e) => {
        // 이 이벤트를 사용하여 다른 컴포넌트에 뷰포트 변경 알림
        // console.log('Viewport Changed:', e.detail);

        // 현재는 콘솔에서 직접 맵모듈에 접근하여 뷰포트 정보 확인 가능
        // console.log(mapModule.getViewportInfo());
    });

    // 디버깅을 위해 전역 접근 추가
    window.mapModule = mapModule;
    window.settingsComponent = settingsComponent;
    window.favoriteEstatesComponent = favoriteEstatesComponent;

    console.log('지도 초기화가 완료되었습니다.');
    console.log('콘솔에서 확인: 지도를 움직이면 ViewportInfo가 자동으로 출력됩니다.');

    // Initialize Mermaid
    mermaid.initialize({
        startOnLoad: true,
        theme: 'default'
    });

    // Define the ERD diagram using Mermaid syntax
    const erdDiagram = `
    erDiagram

        estate {
            bigint id PK
            varchar platform_type
            varchar platform_id
            jsonb raw_data
            varchar estate_name
            varchar estate_type
            varchar trade_type
            numeric price
            numeric rent_price
            numeric area_meter
            numeric area_pyeong
            geometry location
            varchar address
            varchar[] image_urls
            varchar[] tags
            varchar dong_code
            timestamp created_at
        }

        estate_score {
            bigint id PK
            bigint estate_id FK
            int score_type_id FK
            numeric raw_score
            numeric normalized_score
            timestamp created_at
        }


        score_type {
            int id PK
            varchar name
            text description
            boolean active
            timestamp created_at
        }


        user_disabled_score_type {
            bigint user_id PK,FK
            int score_type_id PK,FK
            timestamp created_at
        }


        app_user {
            bigint id PK
            varchar email
            varchar name
            varchar image_url
            varchar role
            timestamp created_at
            timestamp updated_at
        }
        
        user_favorite_estate {
            bigint id PK
            bigint user_id FK
            bigint estate_id FK
            timestamp created_at
        }

        estate ||--o{ estate_score : has
        score_type ||--o{ estate_score : has
        app_user ||--o{ user_favorite_estate : favorites
        estate ||--o{ user_favorite_estate : favorited_by
        app_user ||--o{ user_disabled_score_type : disables
        score_type ||--o{ user_disabled_score_type : disabled_by
    `;

    // Insert the Mermaid diagram into the ERD container
    const erdContainer = document.getElementById('erd-container');
    erdContainer.innerHTML = `<pre class="mermaid">${erdDiagram}</pre>`;

    // mermaid 렌더링 완료 후 ERDConnector 초기화하도록 수정
    mermaid.init(undefined, '.mermaid').then(() => {
      console.log('Mermaid 다이어그램 렌더링 완료');
      window.erdConnector = erdConnector;
      erdConnector.initialize();
    });

    $('#login-button').on('click', function() {
        // 랜덤 UUID 생성
        const uuid = generateShortUUID();
        interactionModule.triggerInteraction('login', {
            email: `user-${uuid}@example.com`
        });
    });

    $('#signup-button').on('click', function() {
        // 랜덤 UUID 생성
        const uuid = generateShortUUID();
        interactionModule.triggerInteraction('signup', {
            email: `user-${uuid}@example.com`,
            name: `User ${uuid}`
        });
    });
    
    // 짧은 UUID 생성 함수
    function generateShortUUID() {
        return Math.random().toString(36).substring(2, 14);
    }
    
    // 로그인 토글 이벤트 리스너 추가
    $('#login-toggle').on('change', function() {
        if ($(this).is(':checked')) {
            // 토글이 ON으로 변경되면 회원가입 후 로그인 실행
            handleLoginProcess();
            // 토글 라벨 업데이트
            $('.toggle-label').text('로그인 ON');
        } else {
            // 토글이 OFF로 변경되면 로그아웃 처리
            handleLogout();
            // 토글 라벨 업데이트
            $('.toggle-label').text('로그인 OFF');
            
            // 프로필 정보 업데이트 (항상 최신 상태 유지)
            if (window.settingsComponent) {
                window.settingsComponent.updateContent();
            }
        }
    });
    
    // 로그인 프로세스 처리 함수
    function handleLoginProcess() {
        console.log('로그인 프로세스 시작');
        
        // 랜덤 UUID 생성
        const uuid = generateShortUUID();
        const userEmail = `user-${uuid}@example.com`;
        const userName = `User ${uuid}`;
        
        console.log(`회원가입 시도: ${userEmail}, ${userName}`);
        
        // 회원가입 실행
        interactionModule.triggerInteraction('signup', {
            email: userEmail,
            name: userName
        })
        .then(signupData => {
            // 회원가입 응답 로깅
            console.log('회원가입 응답:', signupData);
            
            // 응답이 있으면 로그인 진행 (refreshToken 확인을 제거하고 응답 자체 확인)
            if (signupData) {
                console.log(`로그인 시도: ${userEmail}`);
                // 회원가입 성공 후 로그인 실행
                return interactionModule.triggerInteraction('login', {
                    email: userEmail
                });
            }
            return null; // 명시적으로 null 반환하여 체인 계속 진행
        })
        .then(loginData => {
            // 로그인 응답 로깅
            console.log('로그인 응답:', loginData);
            
            // 로그인 응답이 없는 경우 (회원가입에서 null 반환)
            if (!loginData) {
                console.log('로그인 데이터가 없어 로그인 프로세스를 중단합니다.');
                return;
            }
            
            // 필요한 토큰이 모두 있는지 확인
            if (!loginData.accessToken || !loginData.refreshToken) {
                console.log('토큰 정보가 없어 로그인 프로세스를 중단합니다.');
                return;
            }
            
            // 토큰 저장 (AuthToken 응답 형식에 맞춤)
            window.authTokens.accessToken = loginData.accessToken;
            window.authTokens.refreshToken = loginData.refreshToken;
            window.authTokens.expiresAt = loginData.expiresAt;
            window.authTokens.userId = userName; // 사용자 이름 저장
            window.authTokens.isLoggedIn = true;
            
            console.log('로그인 성공:', window.authTokens);
            
            // 설정 컴포넌트가 있다면 프로필 정보 업데이트
            if (window.settingsComponent) {
                window.settingsComponent.updateContent();
            }
        })
        .catch(error => {
            console.error('로그인 프로세스 오류:', error);
            // 오류 발생 시 토글 OFF로 되돌림
            $('#login-toggle').prop('checked', false);
            $('.toggle-label').text('로그인 OFF');
            // 알림 메시지 표시
            alert('로그인 프로세스에 실패했습니다: ' + error.message);
        });
    }
    
    // 로그아웃 처리 함수
    function handleLogout() {
        console.log('로그아웃 처리');
        
        // 토큰 정보 초기화
        window.authTokens.accessToken = null;
        window.authTokens.refreshToken = null;
        window.authTokens.userId = null;
        window.authTokens.isLoggedIn = false;
        
        console.log('로그아웃 완료');
        
        // 설정 컴포넌트가 있다면 프로필 정보 업데이트
        if (window.settingsComponent) {
            window.settingsComponent.updateContent();
        }
    }
});