package app.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountResponse {
    private long count;

    public static UnreadCountResponse of(long count) {
        return new UnreadCountResponse(count);
    }
}
