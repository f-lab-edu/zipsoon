# Zipsoon (집순)
> 집 순위를 손쉽게 매겨봅시다! 집순

Zipsoon은 사용자의 설정에 따라 부동산 매물에 점수를 매겨주는, 집 구하기 서비스입니다.

<br><br>

## 0. 빠르게 구경하세요!

- [↗️ 웹앱 구경하기](https://shiny-goldfish-wgw9rqjqw9435x54-5500.app.github.dev/)
- [↗️ swagger 구경하기](https://shiny-goldfish-wgw9rqjqw9435x54-8080.app.github.dev/swagger-ui/index.html)

❗️위 링크는 codespace에 의해 임시 제공됩니다. 비용이나 보안 문제로 codespace가 닫힐 경우 접근할 수 없습니다.

<br><br>

## 1. 화면

<table>
  <tr>
    <th>메인 화면</th>
    <th>매물 검색 결과</th>
    <th>매물 상세 정보</th>
    <th>사용자 설정</th>
  </tr>
  <tr valign="top">
    <td>
      <img src="/assets/images/guest-screen.png" alt="메인 화면(게스트)"><br>
      • 게스트: 매물 정보만 제공
    </td>
    <td>
      <img src="/assets/images/user-screen.png" alt="메인 화면(회원)"><br>
      • 사용자: 매물 점수 제공<br>
      > 매물의 종합 점수와, 상위 3개 상세 점수를 제공<br>
      > 각 상세 점수는 고유한 점수 계산 방식을 따름
    </td>
    <td>
      <img src="/assets/images/detail-screen.png" alt="매물 상세보기 화면"><br>
      • 매물 선택: 종합 평점, 상세 정보 제공<br>
      > 매물의 상세 정보와, 모든 상세 점수를 제공
      > 마음에 드는 매물을 찜할 수 있음
    </td>
    <td>
      <img src="/assets/images/settings-screen.png" alt="유저 정보 화면"><br>
      • 사용자 설정: 상세 점수 개인화<br>
      > 사용자는 특정 점수 계산 방식을 포함/제외시킬 수 있음
    </td>
  </tr>
</table>

<img src="/assets/images/blueprint.png" alt="기획 화면">

<br><br>

## 2. 아키텍처 및 ERD
```mermaid
flowchart LR
    subgraph TOP[" "]
        direction LR

        subgraph BATCH["SpringBatch"]
            direction TB
            SourceJob["1\. SourceJob<br>(필요 데이터 수집)"]
            EstateJob["2\. EstateJob<br>(부동산 매물 정보 수집)"]
            ScoreJob["3\. ScoreJob<br>(부동산 매물별 점수 계산)"]
            NormalizeJob["4\. NormalizeJob<br>(0-10점으로 정규화)"]
            
            SourceJob --> EstateJob
            EstateJob --> ScoreJob
            ScoreJob --> NormalizeJob
        end

        subgraph MIDDLE[" "]
            style MIDDLE stroke-width:0px
            direction TB
            subgraph SOURCE["외부 자원"]
                direction LR
                NaverLand["네이버 부동산<br>(웹 자원)"]
                PublicData["외부 데이터 정보<br>*행정구역코드, 공원 정보 등<br>(csv 파일)"]
                NaverLand ~~~ PublicData
            end


            subgraph DB["PostgreSQL"]
                EstateTable[(estate)]
                EstateScoreTable[(estate_score)]
                AppUserTable[(app_user)]
            end

            
            SOURCE ~~~ DB

        end


        subgraph API["SpringBoot"]
            direction TB
            ViewportSearch["지도 뷰포트 검색<br>.../estates/map"]
            DetailView["매물 상세 조회<br>.../estates/{id}"]
            ScoreFilter["점수 타입 활성화/비활성화<br>.../estates/score-types"]
            Favorite["매물 찜하기<br>.../estates/{id}/favorite"]

            ViewportSearch ~~~ DetailView
            DetailView ~~~ ScoreFilter
            ScoreFilter ~~~ Favorite
        end

        BATCH["SpringBatch"] <-.-> MIDDLE <-.-> API["SpringBoot"]
    end
```
<img src="/assets/images/ERD.png" alt="ERD"><br>
