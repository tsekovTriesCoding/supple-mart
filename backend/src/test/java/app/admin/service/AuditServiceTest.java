package app.admin.service;

import app.admin.dto.AuditHistoryResponse;
import app.admin.dto.AuditRevision;
import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private AuditReader auditReader;

    @InjectMocks
    private AuditService auditService;

    private UUID testProductId;
    private UUID testOrderId;

    @BeforeEach
    void setUp() {
        testProductId = UUID.randomUUID();
        testOrderId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Get Product Audit History Tests")
    class GetProductAuditHistoryTests {

        @Test
        @DisplayName("Should return empty history when no revisions exist")
        void shouldReturnEmptyHistoryWhenNoRevisions() {
            try (MockedStatic<AuditReaderFactory> factory = mockStatic(AuditReaderFactory.class)) {
                factory.when(() -> AuditReaderFactory.get(entityManager)).thenReturn(auditReader);

                when(auditReader.getRevisions(app.product.model.Product.class, testProductId))
                        .thenReturn(Collections.emptyList());

                AuditHistoryResponse response = auditService.getProductAuditHistory(testProductId);

                assertThat(response).isNotNull();
                assertThat(response.getEntityType()).isEqualTo("Product");
                assertThat(response.getEntityId()).isEqualTo(testProductId.toString());
                assertThat(response.getTotalRevisions()).isZero();
                assertThat(response.getRevisions()).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("Get Order Audit History Tests")
    class GetOrderAuditHistoryTests {

        @Test
        @DisplayName("Should return empty history when no revisions exist")
        void shouldReturnEmptyHistoryWhenNoRevisions() {
            try (MockedStatic<AuditReaderFactory> factory = mockStatic(AuditReaderFactory.class)) {
                factory.when(() -> AuditReaderFactory.get(entityManager)).thenReturn(auditReader);

                when(auditReader.getRevisions(app.order.model.Order.class, testOrderId))
                        .thenReturn(Collections.emptyList());

                AuditHistoryResponse response = auditService.getOrderAuditHistory(testOrderId);

                assertThat(response).isNotNull();
                assertThat(response.getEntityType()).isEqualTo("Order");
                assertThat(response.getEntityId()).isEqualTo(testOrderId.toString());
                assertThat(response.getTotalRevisions()).isZero();
                assertThat(response.getRevisions()).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("Get Recent Audit Activity Tests")
    class GetRecentAuditActivityTests {

        @Test
        @DisplayName("Should return empty list when exceptions occur")
        void shouldReturnEmptyListWhenExceptionsOccur() {
            try (MockedStatic<AuditReaderFactory> factory = mockStatic(AuditReaderFactory.class)) {
                factory.when(() -> AuditReaderFactory.get(entityManager)).thenReturn(auditReader);

                when(auditReader.createQuery()).thenThrow(new RuntimeException("Database error"));

                List<AuditRevision> result = auditService.getRecentAuditActivity(10);

                assertThat(result).isNotNull();
                assertThat(result).isEmpty();
            }
        }
    }
}
