package uk.nhs.adaptors.gp2gp.common.mongo.ttl;

import java.time.Duration;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.core.index.IndexOperations;

@Slf4j
public class MongoTtlCreator extends TtlCreator {
    public static final String TTL_INDEX_NAME = "TTL";
    private static final String FIELD_KEY = "updatedAt";

    public MongoTtlCreator(IndexOperations indexOperations, Duration duration) {
        super(indexOperations, duration);
    }

    public void create(Class<? extends TimeToLive> clazz) {
        if (ttlIndexHasChanged()) {
            LOGGER.info("TTL value has changed for {} - dropping index and creating new one using value {}",
                clazz.getSimpleName(), getDuration());
            getIndexOperations().dropIndex(TTL_INDEX_NAME);
        }
        getIndexOperations().ensureIndex(new Index()
            .expire(getDuration())
            .named(TTL_INDEX_NAME)
            .on(FIELD_KEY, Sort.Direction.ASC)
        );
    }

    @Override
    protected Optional<IndexInfo> findTtlIndex() {
        return getIndexOperations().getIndexInfo().stream()
            .filter(index -> TTL_INDEX_NAME.equals(index.getName()))
            .findFirst();
    }
}
