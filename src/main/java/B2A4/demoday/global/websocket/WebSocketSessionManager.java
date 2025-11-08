package B2A4.demoday.global.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketSessionManager {

    // 각 채팅방별 접속 상태 저장
    private final Map<Long, Boolean> patientConnected = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> hospitalConnected = new ConcurrentHashMap<>();

    public boolean isPatientConnected(Long roomId) {
        return patientConnected.getOrDefault(roomId, false);
    }

    public boolean isHospitalConnected(Long roomId) {
        return hospitalConnected.getOrDefault(roomId, false);
    }

    // --- 연결 ---
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

    // --- 해제 ---
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

    // --- 병원과 환자 모두 연결됐는지 확인 ---
    public boolean bothConnected(Long roomId) {
        boolean patient = patientConnected.getOrDefault(roomId, false);
        boolean hospital = hospitalConnected.getOrDefault(roomId, false);
        boolean result = patient && hospital;

        log.info("bothConnected(roomId={}) → patient={}, hospital={}, result={}",
                roomId, patient, hospital, result);

        return result;
    }

    // --- 전체 해제 ---
    public void disconnectAll(Long roomId) {
        patientConnected.put(roomId, false);
        hospitalConnected.put(roomId, false);
        log.info("Room {} all connections cleared", roomId);
    }
}