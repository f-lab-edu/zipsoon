class ERDConnector {
  constructor() {
    this.erdContainer = document.getElementById('erd-container');
    this.highlightedTables = [];
  }

  // ERD 다이어그램 초기화
  initialize() {
    setTimeout(() => {
      this.bindTableElements();
    }, 1000);
  }

  // 테이블 요소에 DOM 참조 바인딩
  bindTableElements() {
    console.log('bindTableElements 호출됨 - 시간:', new Date().toISOString());
    
    const svg = this.erdContainer.querySelector('svg');
    if (!svg) {
      console.error('ERD SVG 요소를 찾을 수 없습니다.');
      return;
    }

    this.tableElements = {};
    this.entityIdMap = {}; // 원래 테이블명과 ID 매핑을 저장

    // 모든 g 요소 중 id가 'entity-'로 시작하는 요소 찾기
    const entities = svg.querySelectorAll('g[id^="entity-"]');
    console.log(`발견된 엔티티 요소 수: ${entities.length}`);

    entities.forEach(entity => {
      // 엔티티 ID 확인 (디버깅)
      console.log('발견된 엔티티 ID:', entity.id);
      
      // ID에서 테이블 이름 추출 (entity-TABLE-uuid 형식)
      const match = entity.id.match(/entity-(.*?)-[\w\d-]+$/);
      if (match) {
        // 실제 테이블 이름을 찾기 위해 텍스트 라벨 확인
        const textElements = entity.querySelectorAll('text.entityLabel');
        let tableName = '';
        
        if (textElements.length > 0) {
          // 첫 번째 텍스트 요소가 테이블 이름을 가지고 있음
          tableName = textElements[0].textContent.trim();
          console.log('텍스트 요소에서 찾은 테이블명:', tableName);
        } else {
          // 텍스트 요소를 찾지 못한 경우 ID에서 가져온 이름 사용
          tableName = match[1];
          console.log('ID에서 추출한 테이블명:', tableName);
        }
        
        // 원래 테이블명 저장 (app_user, estate_snapshot 등)
        const originalTableNames = ['app_user', 'estate_snapshot', 'score_type', 'estate_score'];
        
        // 각 원래 테이블명에 대해 현재 엔티티가 해당 테이블을 나타내는지 확인
        for (const origName of originalTableNames) {
          // 언더바 제거한 이름과 일치하는지 확인
          const noUnderscoreName = origName.replace(/_/g, '');
          if (match[1].toLowerCase() === noUnderscoreName.toLowerCase()) {
            this.tableElements[origName] = entity;
            this.entityIdMap[origName] = entity.id;
            console.log(`테이블 매핑 완료: ${origName} -> ${entity.id}`);
            break;
          }
        }
      }
    });

    console.log('바인딩된 테이블:', Object.keys(this.tableElements));
    console.log('테이블 ID 매핑:', this.entityIdMap);
  }

  // 테이블 강조 표시
  highlightTables(tables) {
    console.log('highlightTables 호출됨, 테이블:', tables);
    
    // 강조 표시 초기화
    this.resetHighlights();

    // 테이블이 배열이 아니면 배열로 변환
    if (!Array.isArray(tables)) {
      tables = [tables];
    }

    this.highlightedTables = tables;
    
    console.log('사용 가능한 테이블 요소:', Object.keys(this.tableElements));
    console.log('사용 가능한 매핑:', this.entityIdMap);

    // 각 테이블 강조 표시
    tables.forEach(tableName => {
      console.log(`테이블 ${tableName} 강조 시도 중...`);
      const element = this.tableElements[tableName];

      if (element) {
        console.log(`테이블 ${tableName} 요소를 찾음:`, element.id);
        // g 요소에 직접 스타일 적용
        element.setAttribute('style', 'stroke: #ff0000; stroke-width: 1px;');
      } else {
        console.warn(`테이블 요소를 찾을 수 없습니다: ${tableName}`);
        // ID에서 언더스코어를 제거한 이름으로 시도
        const noUnderscoreName = tableName.replace(/_/g, '');
        console.log(`언더스코어 제거 후 테이블명 시도: ${noUnderscoreName}`);
        
        // SVG에서 직접 찾기 시도
        const svg = this.erdContainer.querySelector('svg');
        const entities = svg.querySelectorAll('g[id^="entity-"]');
        entities.forEach(entity => {
          if (entity.id.includes(noUnderscoreName.toLowerCase())) {
            console.log(`이름이 포함된 엔티티 발견: ${entity.id}`);
            entity.setAttribute('style', 'stroke: #ff0000; stroke-width: 1px;');
          }
        });
      }
    });
  }

  // 강조 표시 초기화
  resetHighlights() {
    console.log('resetHighlights 호출됨, 강조된 테이블:', this.highlightedTables);
    
    if (!this.highlightedTables || !Array.isArray(this.highlightedTables)) {
      console.log('강조된 테이블이 없거나 유효하지 않음');
      this.highlightedTables = [];
      return;
    }
    
    this.highlightedTables.forEach(tableName => {
      const element = this.tableElements[tableName];

      if (element) {
        console.log(`테이블 ${tableName} 강조 해제`);
        // 스타일 제거
        element.removeAttribute('style');
      } else {
        console.warn(`강조 해제 실패: 테이블 ${tableName} 요소를 찾을 수 없음`);
        
        // 모든 강조 표시된 엔티티 찾아서 스타일 초기화
        const svg = this.erdContainer.querySelector('svg');
        if (svg) {
          // style 속성이 있는 모든 엔티티 선택
          const styledEntities = svg.querySelectorAll('g[id^="entity-"][style]');
          console.log(`스타일이 적용된 엔티티 수: ${styledEntities.length}`);
          styledEntities.forEach(entity => {
            entity.removeAttribute('style');
          });
        }
      }
    });

    this.highlightedTables = [];
  }
}

// 전역 인스턴스 생성
const erdConnector = new ERDConnector();