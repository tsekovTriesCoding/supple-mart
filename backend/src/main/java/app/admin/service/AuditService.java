package app.admin.service;

import app.admin.dto.AuditHistoryResponse;
import app.admin.dto.AuditRevision;
import app.order.model.Order;
import app.product.model.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public AuditHistoryResponse getProductAuditHistory(UUID productId) {
        return getEntityAuditHistory(Product.class, productId, "Product");
    }

    @Transactional(readOnly = true)
    public AuditHistoryResponse getOrderAuditHistory(UUID orderId) {
        return getEntityAuditHistory(Order.class, orderId, "Order");
    }

    @Transactional(readOnly = true)
    public List<AuditRevision> getRecentAuditActivity(int limit) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        List<AuditRevision> allRevisions = new ArrayList<>();

        try {
            List<Object[]> productRevisions = getRecentRevisionsForEntity(auditReader, Product.class, limit);
            for (Object[] revision : productRevisions) {
                allRevisions.add(mapToAuditRevision(revision, "Product"));
            }
        } catch (Exception e) {
            log.warn("Could not fetch product audit history: {}", e.getMessage());
        }

        try {
            List<Object[]> orderRevisions = getRecentRevisionsForEntity(auditReader, Order.class, limit);
            for (Object[] revision : orderRevisions) {
                allRevisions.add(mapToAuditRevision(revision, "Order"));
            }
        } catch (Exception e) {
            log.warn("Could not fetch order audit history: {}", e.getMessage());
        }

        allRevisions.sort((a, b) -> b.getRevisionDate().compareTo(a.getRevisionDate()));
        
        return allRevisions.stream()
                .limit(limit)
                .toList();
    }

    @SuppressWarnings("unchecked")
    private <T> AuditHistoryResponse getEntityAuditHistory(Class<T> entityClass, UUID entityId, String entityType) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        
        List<Number> revisionNumbers = auditReader.getRevisions(entityClass, entityId);
        List<AuditRevision> revisions = new ArrayList<>();
        
        for (Number revisionNumber : revisionNumbers) {
            try {
                T entity = auditReader.find(entityClass, entityId, revisionNumber);
                Date revisionDate = auditReader.getRevisionDate(revisionNumber);

                AuditQuery query = auditReader.createQuery()
                        .forRevisionsOfEntity(entityClass, false, true)
                        .add(AuditEntity.id().eq(entityId))
                        .add(AuditEntity.revisionNumber().eq(revisionNumber));
                
                List<Object[]> results = query.getResultList();
                RevisionType revisionType = results.isEmpty() ? RevisionType.MOD : (RevisionType) results.get(0)[2];
                
                // Extract modified by from entity if available
                String modifiedBy = extractModifiedBy(entity);
                
                revisions.add(AuditRevision.builder()
                        .revisionNumber(revisionNumber.longValue())
                        .revisionDate(LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(revisionDate.getTime()), 
                                ZoneId.systemDefault()))
                        .revisionType(revisionType.name())
                        .modifiedBy(modifiedBy)
                        .entityData(mapEntityToSimpleData(entity))
                        .build());
            } catch (Exception e) {
                log.error("Error fetching revision {} for {} {}: {}", 
                        revisionNumber, entityType, entityId, e.getMessage());
            }
        }

        revisions.sort((a, b) -> b.getRevisionNumber().compareTo(a.getRevisionNumber()));
        
        return AuditHistoryResponse.builder()
                .entityType(entityType)
                .entityId(entityId.toString())
                .totalRevisions(revisions.size())
                .revisions(revisions)
                .build();
    }

    @SuppressWarnings("unchecked")
    private <T> List<Object[]> getRecentRevisionsForEntity(AuditReader auditReader, Class<T> entityClass, int limit) {
        AuditQuery query = auditReader.createQuery()
                .forRevisionsOfEntity(entityClass, false, true)
                .addOrder(AuditEntity.revisionNumber().desc())
                .setMaxResults(limit);
        
        return query.getResultList();
    }

    private AuditRevision mapToAuditRevision(Object[] revision, String entityType) {
        Object entity = revision[0];
        Object revisionEntity = revision[1];
        RevisionType revisionType = (RevisionType) revision[2];

        Long timestamp = extractRevisionTimestamp(revisionEntity);
        LocalDateTime revisionDate = timestamp != null 
                ? LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                : LocalDateTime.now();
        
        Long revisionNumber = extractRevisionNumber(revisionEntity);
        String modifiedBy = extractModifiedBy(entity);
        
        return AuditRevision.builder()
                .revisionNumber(revisionNumber)
                .revisionDate(revisionDate)
                .revisionType(revisionType.name())
                .modifiedBy(modifiedBy)
                .entityData(mapEntityToSimpleData(entity))
                .build();
    }

    private Long extractRevisionTimestamp(Object revisionEntity) {
        try {
            var method = revisionEntity.getClass().getMethod("getTimestamp");
            return (Long) method.invoke(revisionEntity);
        } catch (Exception e) {
            return null;
        }
    }

    private Long extractRevisionNumber(Object revisionEntity) {
        try {
            var method = revisionEntity.getClass().getMethod("getId");
            Object id = method.invoke(revisionEntity);
            return id instanceof Number ? ((Number) id).longValue() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String extractModifiedBy(Object entity) {
        try {
            var method = entity.getClass().getMethod("getLastModifiedBy");
            Object result = method.invoke(entity);
            return result != null ? result.toString() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    private Map<String, Object> mapEntityToSimpleData(Object entity) {
        Map<String, Object> data = new HashMap<>();
        
        if (entity instanceof Product product) {
            data.put("id", product.getId());
            data.put("name", product.getName());
            data.put("price", product.getPrice());
            data.put("stockQuantity", product.getStockQuantity());
            data.put("category", product.getCategory());
            data.put("isActive", product.isActive());
            data.put("lastModifiedBy", product.getLastModifiedBy());
        } else if (entity instanceof Order order) {
            data.put("id", order.getId());
            data.put("orderNumber", order.getOrderNumber());
            data.put("status", order.getStatus());
            data.put("totalAmount", order.getTotalAmount());
            data.put("lastModifiedBy", order.getLastModifiedBy());
        }
        
        return data;
    }
}
