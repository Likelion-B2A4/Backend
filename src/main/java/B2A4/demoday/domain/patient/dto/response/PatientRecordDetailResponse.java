package B2A4.demoday.domain.patient.dto.response;

import B2A4.demoday.domain.chat.entity.ChatRoom;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PatientRecordDetailResponse {

    private Long chatRoomId;

    private Long hospitalId;
    private String hospitalName;

    private Long doctorId;
    private String doctorName;
    private String specialty;
    private String doctorImageUrl;

    private String status;
    private String startedAt;
    private String finishedAt;

    private String symptomSummary;
    private String diagnosisSummary;

    public static PatientRecordDetailResponse from(ChatRoom room) {
        var doctor = room.getDoctor();
        var hospital = doctor.getHospital();

        return PatientRecordDetailResponse.builder()
                .chatRoomId(room.getId())
                .hospitalId(hospital.getId())
                .hospitalName(hospital.getName())
                .doctorId(doctor.getId())
                .doctorName(doctor.getName())
                .specialty(doctor.getSpecialty())
                .doctorImageUrl(doctor.getImageUrl())
                .status(room.getStatus())
                .startedAt(room.getStartedAt().toString())
                .finishedAt(room.getFinishedAt().toString())
                .symptomSummary(room.getSymptomSummary())
                .diagnosisSummary(room.getDiagnosisSummary())
                .build();
    }
}