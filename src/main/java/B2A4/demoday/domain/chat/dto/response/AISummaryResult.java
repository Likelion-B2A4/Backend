package B2A4.demoday.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AISummaryResult {
    private String symptomSummary;
    private String diagnosisSummary;
}
