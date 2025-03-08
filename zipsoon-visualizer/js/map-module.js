/**
 * 지도 모듈
 * OpenStreetMap을 사용하여 지도 기능을 구현합니다.
 */

/**
 * 맵 뷰포트 정보를 저장하는 클래스
 */
class ViewportInfo {
    constructor() {
        this.swLng = 0;
        this.swLat = 0;
        this.neLng = 0;
        this.neLat = 0;
        this.nwLng = 0;
        this.nwLat = 0;
        this.seLng = 0;
        this.seLat = 0;
        this.zoom = 0;
        this.center = { lat: 0, lng: 0 };
    }
    
    /**
     * 뷰포트 정보 업데이트
     * @param {L.LatLngBounds} bounds - 사각형 경계
     * @param {Number} zoom - 줌 레벨
     * @param {L.LatLng} center - 중심 좌표
     */
    update(bounds, zoom, center) {
        const sw = bounds.getSouthWest();
        const ne = bounds.getNorthEast();
        
        this.swLng = sw.lng;
        this.swLat = sw.lat;
        this.neLng = ne.lng;
        this.neLat = ne.lat;
        this.nwLng = sw.lng;
        this.nwLat = ne.lat;
        this.seLng = ne.lng;
        this.seLat = sw.lat;
        this.zoom = zoom;
        this.center = { lat: center.lat, lng: center.lng };
        
        // 콘솔에 로그 출력
        this.logToConsole();
    }
    
    /**
     * 콘솔에 뷰포트 정보 출력
     */
    logToConsole() {
        console.debug('ViewportInfo:', this.toJSON());
    }
    
    /**
     * 데이터 객체 반환
     * @returns {Object} 뷰포트 데이터 객체
     */
    toJSON() {
        return {
            sw: { lng: this.swLng, lat: this.swLat },
            ne: { lng: this.neLng, lat: this.neLat },
            nw: { lng: this.nwLng, lat: this.nwLat },
            se: { lng: this.seLng, lat: this.seLat },
            zoom: this.zoom,
            center: this.center
        };
    }
}

class MapModule {
    constructor() {
        this.map = null;
        this.markers = [];
        // 서울시 중심 좌표 (서울시청)
        this.defaultCenter = [37.5664056, 126.9778222];
        this.defaultZoom = 15;
        this.boundaryRectangle = null;
        this.zoomControl = null;
        this.centerSquare = null;
        this.cornerMarkers = [];
        this.shadeOverlays = [];
        // 뷰포트 정보 객체
        this.viewportInfo = new ViewportInfo();
    }

    /**
     * 지도 초기화
     */
    initialize() {
        // 서울 지역을 제한하는 경계 설정
        // [남서, 북동] 좌표 쌍으로 경계 지정
        // 서울시 대략적 경계: 남서(37.413294, 126.734086), 북동(37.715133, 127.269311)
        const seoulBounds = L.latLngBounds(
            L.latLng(37.413294, 126.734086), // 남서
            L.latLng(37.715133, 127.269311)  // 북동
        );
        
        // Leaflet 지도 초기화 (maxBounds로 이동 제한)
        this.map = L.map('map', {
            center: this.defaultCenter,
            zoom: this.defaultZoom,
            maxBounds: seoulBounds,
            maxBoundsViscosity: 1.0, // 경계에서 튕기는 효과(1.0은 완전 제한)
            minZoom: 11 // 최소 줌 레벨 설정 (너무 넓게 보는 것 방지)
        });
        
        // OpenStreetMap 타일 레이어 추가
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
            maxZoom: 19,
            bounds: seoulBounds // 타일 로딩도 이 경계 내로 제한
        }).addTo(this.map);

        // 지도 로드 완료 후 이벤트
        this.map.on('load', () => {
            console.log('지도가 로드되었습니다.');
        });

        // 지도 클릭 이벤트
        this.map.on('click', (e) => {
            console.log('지도 클릭 좌표:', e.latlng);
        });
        
        // 서울 경계를 시각적으로 표시 (선택 사항)
        this.boundaryRectangle = L.rectangle(seoulBounds, {
            color: "#ff7800",
            weight: 1,
            fillOpacity: 0.05
        }).addTo(this.map);
        
        // Leaflet 내장 좌표 컨트롤 추가
        L.control.scale({position: 'bottomright', imperial: false}).addTo(this.map);
        
        // 중앙 사각형 생성
        this.createCenterSquare();
        
        // Leaflet 내장 좌표 표시 컨트롤 추가
        this.addCoordinatesControl();
        
        // 지도 이벤트 설정 (이동 및 줌 변경 시 정보 업데이트)
        this.map.on('moveend', this.updateCoordinatesInfo.bind(this));
        this.map.on('zoomend', this.updateCoordinatesInfo.bind(this));
        this.map.on('resize', this.updateCoordinatesInfo.bind(this));
    }
    
    /**
     * 좌표 마커 배열 초기화
     */
    addCoordinatesControl() {
        // 좌표 마커를 저장할 배열 초기화
        this.cornerMarkers = [];
        // 줌 표시용 컨트롤만 추가
        this.zoomControl = L.control({position: 'bottomleft'});
        this.zoomControl.onAdd = () => {
            this.zoomContainer = L.DomUtil.create('div', 'coordinates-overlay');
            return this.zoomContainer;
        };
        this.zoomControl.addTo(this.map);
    }
    
    /**
     * 중앙에 정방형 사각형 생성
     */
    createCenterSquare() {
        // 지도 중심 좌표
        const center = this.map.getCenter();
        
        // 처음에는 임의의 경계로 사각형 생성
        const initialBounds = L.latLngBounds(
            [center.lat - 0.001, center.lng - 0.001],
            [center.lat + 0.001, center.lng + 0.001]
        );
        
        // 사각형 생성 및 추가 - 내부 투명하게
        this.centerSquare = L.rectangle(initialBounds, {
            className: 'center-square',
            weight: 2,
            fill: false,
            color: '#ff0000'
        }).addTo(this.map);
        
        // 바깥 영역에 회색 오버레이 추가
        this.createShadeOverlay();
        
        // 초기 사이즈 설정
        this.adjustSquareSize();
    }
    
    /**
     * 중심 좌표에서 정방형 사각형 경계 계산
     * @param {L.LatLng} center - 중심 좌표
     * @param {Number} size - 사각형 크기 (미터)
     * @returns {L.LatLngBounds} - 계산된 경계
     */
    calculateSquareBounds(center, size) {
        // 지구 반경 (미터)
        const earthRadius = 6378137;
        
        // 라디안으로 변환
        const lat = center.lat * Math.PI / 180;
        
        // 위도에 따른 경도 보정 계수
        const latAdjustment = Math.cos(lat);
        
        // 미터당 위도 변화량
        const metersPerLat = 1 / ((2 * Math.PI / 360) * earthRadius);
        
        // 미터당 경도 변화량 (위도에 따라 다름)
        const metersPerLng = metersPerLat / latAdjustment;
        
        // 사각형 크기의 절반
        const halfSize = size / 2;
        
        // 위도, 경도 변화량 계산
        const latChange = halfSize * metersPerLat;
        const lngChange = halfSize * metersPerLng;
        
        // 사각형 경계 계산
        return L.latLngBounds(
            [center.lat - latChange, center.lng - lngChange], // 남서
            [center.lat + latChange, center.lng + lngChange]  // 북동
        );
    }
    
    /**
     * 화면 너비에 맞게 사각형 크기 조정 (정방형으로)
     */
    adjustSquareSize() {
        if (!this.map || !this.centerSquare) return;
        
        // 지도의 픽셀 크기 구하기
        const mapSize = this.map.getSize();
        const mapWidth = mapSize.x;
        
        // 사각형이 화면 가로의 75%가 되도록 계산
        const targetPixelSize = mapWidth * 0.75;
        
        // 지도 중심점
        const center = this.map.getCenter();
        
        // 지도 경계 구하기
        const mapBounds = this.map.getBounds();
        const mapNorthEast = mapBounds.getNorthEast();
        const mapSouthWest = mapBounds.getSouthWest();
        
        // 현재 지도의 실제 너비와 높이 (경도/위도 단위)
        const mapWidthInDegrees = mapBounds.getEast() - mapBounds.getWest();
        const mapHeightInDegrees = mapBounds.getNorth() - mapBounds.getSouth();
        
        // 픽셀당 경도 단위 계산
        const lngPerPixel = mapWidthInDegrees / mapSize.x;
        
        // 픽셀당 위도 단위 계산
        const latPerPixel = mapHeightInDegrees / mapSize.y;
        
        // 정방형 사각형을 위한 계산
        // 타겟 픽셀 크기에 해당하는 경도/위도 변화량
        const halfLngDelta = (targetPixelSize * lngPerPixel) / 2;
        const halfLatDelta = (targetPixelSize * latPerPixel) / 2;
        
        // 사각형 경계 계산 (정방형 - 픽셀 단위로 동일한 크기)
        const squareBounds = L.latLngBounds(
            [center.lat - halfLatDelta, center.lng - halfLngDelta],
            [center.lat + halfLatDelta, center.lng + halfLngDelta]
        );
        
        // 사각형 경계 업데이트
        this.centerSquare.setBounds(squareBounds);
        
        return squareBounds;
    }
    
    /**
     * 좌표 정보 업데이트
     */
    updateCoordinatesInfo() {
        // 지도의 현재 경계 가져오기
        const bounds = this.map.getBounds();
        const zoom = this.map.getZoom();
        const center = this.map.getCenter();
        
        // 중앙 사각형 경계 업데이트
        if (this.centerSquare) {
            // 화면 가로의 75%가 되도록 사각형 크기 조정
            const squareBounds = this.adjustSquareSize();
            
            // 중앙 사각형의 경계 좌표
            const sw = squareBounds.getSouthWest();
            const ne = squareBounds.getNorthEast();
            const nw = L.latLng(ne.lat, sw.lng);
            const se = L.latLng(sw.lat, ne.lng);
            
            // 기존 마커 모두 제거
            this.removeCornerMarkers();
            
            // 꼭짓점에 마커 추가 (위치 조정)
            this.addCornerMarker(sw, 'swLng: ' + sw.lng.toFixed(6) + '<br>swLat: ' + sw.lat.toFixed(6), 'bottom');
            this.addCornerMarker(ne, 'neLng: ' + ne.lng.toFixed(6) + '<br>neLat: ' + ne.lat.toFixed(6), 'top');
            this.addCornerMarker(nw, 'nwLng: ' + nw.lng.toFixed(6) + '<br>nwLat: ' + nw.lat.toFixed(6), 'top');
            this.addCornerMarker(se, 'seLng: ' + se.lng.toFixed(6) + '<br>seLat: ' + se.lat.toFixed(6), 'bottom');
            
            // 사각형 바깥 중앙 상단에 줌 표시
            // 사각형 위쪽 중앙 바깥으로 위치 조정
            const centerTop = L.latLng(ne.lat + (ne.lat - center.lat) * 0.2, center.lng);
            this.addCornerMarker(centerTop, `zoom: ${zoom}`, 'top');
            
            // 바깥 영역 음영 업데이트
            this.updateShadeOverlay(squareBounds);
            
            // 뷰포트 정보 업데이트 및 콘솔에 출력
            this.viewportInfo.update(squareBounds, zoom, center);
            
            // 이벤트 발생 (다른 컴포넌트에서 구독할 수 있음)
            this.triggerViewportChanged();
        }
    }
    
    /**
     * 뷰포트 변경 이벤트 발생
     */
    triggerViewportChanged() {
        // 커스텀 이벤트 생성 및 디스패치
        const event = new CustomEvent('viewportChanged', {
            detail: this.viewportInfo.toJSON()
        });
        document.dispatchEvent(event);
    }
    
    /**
     * 뷰포트 정보 가져오기
     * @returns {Object} 뷰포트 정보
     */
    getViewportInfo() {
        return this.viewportInfo.toJSON();
    }
    
    /**
     * 사각형 바깥을 어둡게 하는 오버레이 생성
     */
    createShadeOverlay() {
        // 4개의 영역 생성: 위, 오른쪽, 아래, 왼쪽
        this.shadeOverlays = [];
    }
    
    /**
     * 사각형 바깥 영역 음영 업데이트
     * @param {L.LatLngBounds} squareBounds - 중앙 사각형 경계
     */
    updateShadeOverlay(squareBounds) {
        // 기존 오버레이 제거
        this.shadeOverlays.forEach(overlay => {
            if (overlay) overlay.remove();
        });
        this.shadeOverlays = [];
        
        // 지도 전체 경계
        const mapBounds = this.map.getBounds();
        const sw = squareBounds.getSouthWest();
        const ne = squareBounds.getNorthEast();
        
        // 상단 영역
        const topOverlay = L.rectangle(
            L.latLngBounds(
                L.latLng(ne.lat, mapBounds.getWest()),
                L.latLng(mapBounds.getNorth(), mapBounds.getEast())
            ), {
                color: 'none',
                fillColor: '#000',
                fillOpacity: 0.3,
                interactive: false
            }
        ).addTo(this.map);
        this.shadeOverlays.push(topOverlay);
        
        // 오른쪽 영역
        const rightOverlay = L.rectangle(
            L.latLngBounds(
                L.latLng(sw.lat, ne.lng),
                L.latLng(ne.lat, mapBounds.getEast())
            ), {
                color: 'none',
                fillColor: '#000',
                fillOpacity: 0.3,
                interactive: false
            }
        ).addTo(this.map);
        this.shadeOverlays.push(rightOverlay);
        
        // 하단 영역
        const bottomOverlay = L.rectangle(
            L.latLngBounds(
                L.latLng(mapBounds.getSouth(), mapBounds.getWest()),
                L.latLng(sw.lat, mapBounds.getEast())
            ), {
                color: 'none',
                fillColor: '#000',
                fillOpacity: 0.3,
                interactive: false
            }
        ).addTo(this.map);
        this.shadeOverlays.push(bottomOverlay);
        
        // 왼쪽 영역
        const leftOverlay = L.rectangle(
            L.latLngBounds(
                L.latLng(sw.lat, mapBounds.getWest()),
                L.latLng(ne.lat, sw.lng)
            ), {
                color: 'none',
                fillColor: '#000',
                fillOpacity: 0.3,
                interactive: false
            }
        ).addTo(this.map);
        this.shadeOverlays.push(leftOverlay);
    }
    
    /**
     * 좌표 마커 추가
     * @param {L.LatLng} position - 마커 위치
     * @param {String} text - 표시할 텍스트
     * @param {String} position - 마커 위치 ('top' 또는 'bottom')
     */
    addCornerMarker(position, text, placement = 'top') {
        // 마커의 위치에 따라 앵커 포인트 조정
        // top: 마커의 중앙 하단이 좌표 위치에 오도록
        // bottom: 마커의 중앙 상단이 좌표 위치에 오도록
        const iconWidth = 120;
        const iconHeight = 40;
        
        const anchorX = iconWidth / 2;  // 수평 중앙
        const anchorY = placement === 'top' ? iconHeight : 0;  // top이면 하단, bottom이면 상단
        
        // 줌일 경우 다른 스타일 적용
        const isZoom = text.includes('zoom');
        const className = isZoom 
            ? `corner-label corner-label-${placement} zoom-label` 
            : `corner-label corner-label-${placement}`;
        
        const icon = L.divIcon({
            className: className,
            html: text,
            iconSize: [iconWidth, iconHeight],
            iconAnchor: [anchorX, anchorY]
        });
        
        const marker = L.marker(position, {
            icon: icon,
            interactive: false
        }).addTo(this.map);
        
        this.cornerMarkers.push(marker);
    }
    
    /**
     * 모든 꼭짓점 마커 제거
     */
    removeCornerMarkers() {
        this.cornerMarkers.forEach(marker => marker.remove());
        this.cornerMarkers = [];
    }

    /**
     * 마커 추가
     * @param {Array} position - [위도, 경도] 형식의 배열
     * @param {Object} estateData - 매물 데이터
     * @param {Object} options - 마커 옵션
     * @returns {Object} 생성된 마커 객체
     */
    addMarker(position, estateData = {}, options = {}) {
        const marker = L.marker(position, options).addTo(this.map);
        marker.estateData = estateData;
        
        // 마커 클릭 이벤트 추가
        marker.on('click', (e) => {
            // 커스텀 이벤트 발생 - 매물 클릭
            const event = new CustomEvent('estateMarkerClicked', {
                detail: estateData
            });
            document.dispatchEvent(event);
        });
        
        this.markers.push(marker);
        return marker;
    }

    /**
     * 모든 마커 제거
     */
    clearMarkers() {
        this.markers.forEach(marker => marker.remove());
        this.markers = [];
    }

    /**
     * 지정된 위치로 지도 이동
     * @param {Array} position - [위도, 경도] 형식의 배열
     * @param {Number} zoom - 확대 레벨
     */
    setCenter(position, zoom = this.defaultZoom) {
        this.map.setView(position, zoom);
    }

    /**
     * 지도 확대/축소 레벨 설정
     * @param {Number} zoom - 확대 레벨
     */
    setZoom(zoom) {
        this.map.setZoom(zoom);
    }
}

// 싱글톤으로 지도 모듈 내보내기
const mapModule = new MapModule();