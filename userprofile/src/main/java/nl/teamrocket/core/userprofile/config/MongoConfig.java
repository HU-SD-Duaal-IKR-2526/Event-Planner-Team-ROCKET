package nl.teamrocket.core.userprofile.config;

import com.mongodb.WriteConcern;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;

/**
 * MongoDB config for User/Profile module.
 * Write concern: MAJORITY for causal consistency (read-your-writes).
 * Data distribution doc §4.2.2.
 */
@Configuration
public class MongoConfig {
    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory factory, MongoConverter converter) {
        var template = new MongoTemplate(factory, converter);
        template.setWriteConcern(WriteConcern.MAJORITY);
        return template;
    }
}
