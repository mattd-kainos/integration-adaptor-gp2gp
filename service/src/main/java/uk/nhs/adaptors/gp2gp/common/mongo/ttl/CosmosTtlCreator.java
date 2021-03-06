package uk.nhs.adaptors.gp2gp.common.mongo.ttl;

import java.time.Duration;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.core.index.IndexOperations;

@Slf4j
public class CosmosTtlCreator extends TtlCreator {
    private static final String INDEX_FIELD_KEY = "_ts";

    public CosmosTtlCreator(IndexOperations indexOperations, Duration duration) {
        super(indexOperations, duration);
    }

    public void create(Class<? extends TimeToLive> clazz) {
        if (ttlIndexHasChanged()) {
            LOGGER.info("TTL value has changed for {} - dropping index and creating new one using value {}",
                clazz.getSimpleName(), getDuration());
            String indexName = findTtlIndex().map(IndexInfo::getName).orElseThrow();
            getIndexOperations().dropIndex(indexName);
        }
        getIndexOperations().ensureIndex(new Index()
            .expire(getDuration())
            .on(INDEX_FIELD_KEY, Sort.Direction.ASC)
        );
    }

    @Override
    protected Optional<IndexInfo> findTtlIndex() {
        return getIndexOperations().getIndexInfo().stream()
            .filter(this::isTtlIndex)
            .findFirst();
    }

    private boolean isTtlIndex(IndexInfo index) {
        return index.getIndexFields()
            .stream()
            .anyMatch(field -> INDEX_FIELD_KEY.equals(field.getKey()));
    }
}
