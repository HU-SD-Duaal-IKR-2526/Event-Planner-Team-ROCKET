package nl.hu.ikr.schedule.repository;

import nl.hu.ikr.schedule.domain.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, String> {
    boolean existsByMessageId(String messageId);
}

