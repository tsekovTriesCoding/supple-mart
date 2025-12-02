package app.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductPageResponse {
    private List<AdminProductResponse> content;
    private int currentPage;
    private int pageSize;
    private int totalPages;
    private long totalElements;
}

