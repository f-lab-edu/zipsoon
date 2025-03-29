package com.zipsoon.batch.performance;

import com.zipsoon.batch.infrastructure.mapper.source.SourceMapper;
import com.zipsoon.batch.infrastructure.repository.source.SourceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 테스트용 SourceRepository 구현체
 * PostGIS 확장 함수를 직접 사용하지 않고 테스트 환경에서도 동작하도록 간소화
 */
@Slf4j
public class TestSourceRepository extends SourceRepository {
    private final JdbcTemplate jdbcTemplate;

    public TestSourceRepository(SourceMapper sourceMapper, JdbcTemplate jdbcTemplate) {
        super(sourceMapper);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void executeDDL(String sql) {
        // SQL 실행 직접 수행
        try {
            jdbcTemplate.execute(sql);
            log.info("SQL 실행 성공: {}", sql.length() > 50 ? sql.substring(0, 50) + "..." : sql);
        } catch (Exception e) {
            log.error("SQL 실행 실패: {}", e.getMessage(), e);
            throw new RuntimeException("SQL 실행 실패", e);
        }
    }

    @Override
    public void addLocationColumn(String tableName) {
        // 테스트 환경에서는 location 컬럼을 추가하지 않음 (무시)
        log.info("테스트 환경에서 location 컬럼 추가 생략: {}", tableName);
    }

    @Override
    public int updateLocationCoordinates(String tableName) {
        // 테스트 환경에서는 좌표 업데이트를 시뮬레이션만 함
        log.info("테스트 환경에서 location 좌표 업데이트 시뮬레이션");
        return 1; // 성공한 것처럼 반환
    }

    @Override
    public void dropTable(String tableName) {
        // 테이블 삭제
        try {
            String sql = "DROP TABLE IF EXISTS " + tableName;
            jdbcTemplate.execute(sql);
            log.info("테이블 삭제 완료: {}", tableName);
        } catch (Exception e) {
            log.error("테이블 삭제 실패: {}", e.getMessage(), e);
            throw new RuntimeException("테이블 삭제 실패", e);
        }
    }
    
    /**
     * 테스트 초기화 - 필요한 경우 여기에 초기화 코드 추가
     */
    public void initialize() {
        log.info("테스트 저장소 초기화 완료");
    }
    
    /**
     * 테스트용 간소화된 테이블 스키마 생성
     */
    public void createSimplifiedParkTable() {
        try {
            dropTable("parks");
            
            // 원본 스키마와 정확히 일치하는 테이블 생성 - PRIMARY KEY 없음, NULL 허용
            String createTableSql = 
                "CREATE TABLE IF NOT EXISTS parks (" +
                "  \"관리번호\" varchar(255) NULL," +         // PRIMARY KEY 제거, NULL 허용
                "  \"공원명\" varchar(255) NULL," +           // NOT NULL 제거
                "  \"공원구분\" varchar(255) NULL," +         // NULL 명시
                "  \"소재지도로명주소\" varchar(255) NULL," +
                "  \"소재지지번주소\" varchar(255) NULL," +
                "  \"위도\" DOUBLE PRECISION NULL," +
                "  \"경도\" DOUBLE PRECISION NULL," +
                "  \"공원면적\" DOUBLE PRECISION NULL," +
                "  \"공원보유시설(운동시설)\" TEXT NULL," +
                "  \"공원보유시설(유희시설)\" TEXT NULL," +
                "  \"공원보유시설(편익시설)\" TEXT NULL," +
                "  \"공원보유시설(교양시설)\" TEXT NULL," +
                "  \"공원보유시설(기타시설)\" TEXT NULL," +
                "  \"지정고시일\" DATE NULL," +
                "  \"관리기관명\" varchar(255) NULL," +
                "  \"전화번호\" varchar(50) NULL," +
                "  \"데이터기준일자\" DATE NULL," +
                "  \"제공기관코드\" varchar(50) NULL," +
                "  \"제공기관명\" varchar(255) NULL" +
                ")";
            
            jdbcTemplate.execute(createTableSql);
            log.info("테스트용 공원 테이블 생성 완료");
        } catch (Exception e) {
            log.error("테스트용 공원 테이블 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("테스트용 공원 테이블 생성 실패", e);
        }
    }
}