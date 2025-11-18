package B2A4.demoday.global.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 메모리 기반 상태 관리자 */
@Slf4j
@Component
public class WebSocketSessionManager {

    // 각 채팅방별 접속 상태 저장
    // chatRoomId - true/false
    private final Map<Long, Boolean> patientConnected = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> hospitalConnected = new ConcurrentHashMap<>();

    // 의사의 대기 세션 저장
    // doctorId - WebSocket sessionId
    private final Map<Long, String> doctorStandbySessionMap = new ConcurrentHashMap<>();

    public boolean isPatientConnected(Long roomId) {
        return patientConnected.getOrDefault(roomId, false);
    }

    public boolean isHospitalConnected(Long roomId) {
        return hospitalConnected.getOrDefault(roomId, false);
    }

    // 채팅방 경로 구독 (STOMP SUBSCRIBE)
    public void connect(Long roomId, String userType) {
        if (roomId == null || userType == null) return;

        switch (userType.toLowerCase()) {
            case "patient" -> patientConnected.put(roomId, true);
            case "hospital" -> hospitalConnected.put(roomId, true);
        }

        log.info("{} connected to room {}", userType, roomId);
        log.debug("상태 → patient={}, hospital={}",
                patientConnected.getOrDefault(roomId, false),
                hospitalConnected.getOrDefault(roomId, false));
    }

    // 채팅방 구독 해제 (STOMP UNSUBSCRIBE)
    public void disconnect(Long roomId, String userType) {
        if (roomId == null || userType == null) return;

        switch (userType.toLowerCase()) {
            case "patient" -> patientConnected.put(roomId, false);
            case "hospital" -> hospitalConnected.put(roomId, false);
        }

        log.info("{} disconnected from room {}", userType, roomId);
        log.debug("상태 → patient={}, hospital={}",
                patientConnected.getOrDefault(roomId, false),
                hospitalConnected.getOrDefault(roomId, false));
    }

    // 메시지 전송 전에 둘다 접속중인지 확인
    public boolean bothConnected(Long roomId) {
        boolean patient = patientConnected.getOrDefault(roomId, false);
        boolean hospital = hospitalConnected.getOrDefault(roomId, false);
        boolean result = patient && hospital;

        log.info("bothConnected(roomId={}) → patient={}, hospital={}, result={}",
                roomId, patient, hospital, result);

        return result;
    }

    // 전체 해제 및 메모리 정리
    public void disconnectAll(Long roomId) {
        patientConnected.remove(roomId);  // put(false) 대신 remove()
        hospitalConnected.remove(roomId);
        log.info("Room {} all connections cleared and removed from memory", roomId);
    }

    // 의사의 대기 경로 구독
    public void connectDoctor(Long doctorId, String sessionId) {
        if (doctorId == null || sessionId == null) return;
        
        doctorStandbySessionMap.put(doctorId, sessionId);
        log.info("의사 대기 세션 등록 → doctorId={}, sessionId={}", doctorId, sessionId);
    }

    // 의사의 대기 구독 해제
    public void disconnectDoctor(Long doctorId) {
        if (doctorId == null) return;
        
        String removedSessionId = doctorStandbySessionMap.remove(doctorId);
        log.info("의사 대기 세션 해제 → doctorId={}, sessionId={}", doctorId, removedSessionId);
    }

    // 의사가 대기중인지
    public boolean isDoctorStandby(Long doctorId) {
        return doctorStandbySessionMap.containsKey(doctorId);
    }

    // 대기중인 의사의 세션 ID
    public String getDoctorSessionId(Long doctorId) {
        return doctorStandbySessionMap.get(doctorId);
    }
}
