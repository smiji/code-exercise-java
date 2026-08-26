package com.tpx.urlshort.service.alias.snowflake;

import com.tpx.urlshort.exception.IllegalParametersException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class SnowflakeIdGenerator {

    /**
     * The combination of timestamp difference ( (from a reference value - current )
     * in sec)
     * + the workstation id + dataCenterId + a generating sequence number
     * ensure the uniqueness at each server level
     * <p>
     * Why this complicated logic is required..
     * -> There is no need for using any external system for uniqueness
     * -> Scalable
     * -> uniqueness is guaranteed at the server level
     * -> Standard approach used in the industry
     */

    private static final Logger logger = LoggerFactory.getLogger(SnowflakeIdGenerator.class);
    // Bit distribution to fit exactly under 62^7 (Max 41 bits total)
    private static final long DATA_CENTER_ID_BITS = 2L;
    private static final long WORKER_ID_BITS = 3L;
    private static final long SEQUENCE_BITS = 5L;
    private static final long MAX_DATA_CENTER_ID = (1L << DATA_CENTER_ID_BITS) - 1; // 3
    private static final long MAX_WORKER_ID = (1L << WORKER_ID_BITS) - 1; // 7
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1; // 31
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = DATA_CENTER_ID_SHIFT + DATA_CENTER_ID_BITS;
    private final long workerId;
    private final long dataCenterId;
    private final long timestampBaseReference;
    private final Lock lock = new ReentrantLock();
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(@Value("${snowflake.worker-id:0}") long workerId,
            @Value("${snowflake.datacenter-id:0}") long dataCenterId,
            @Value("${snowflake.epoch-reference:1767225600}") long timestampBaseReference) {

        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalParametersException("Worker ID must be between 0 and " + MAX_WORKER_ID);
        }
        if (dataCenterId < 0 || dataCenterId > MAX_DATA_CENTER_ID) {
            throw new IllegalParametersException("Data Center ID must be between 0 and " + MAX_DATA_CENTER_ID);
        }

        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
        this.timestampBaseReference = timestampBaseReference;
    }

    public long getNextId() {

        try {
            logger.debug("Next Id requested");
            lock.lock();

            // calculate the current timestamp..
            long currentTimeStamp = getCurrentTimestamp();

            // the current timestamp is less than previous , that means the clock is reset
            if (currentTimeStamp < lastTimestamp) {
                String message = String.format("Clock moved backwards. Refusing to generate id for  %s seconds.",
                        (lastTimestamp - currentTimeStamp));
                logger.error(message);
                throw new IllegalStateException(message);
            }

            // if sequence are generate at the same time, increment it
            logger.debug("Calculating current timestamp and sequence");
            if (currentTimeStamp == lastTimestamp) {
                // if the sequence reached the max seq, reset it to 0 and get a new timestamp in
                // future
                sequence = (sequence + 1) & MAX_SEQUENCE;
                if (sequence == 0L) {
                    // Sequence exhausted for this second, block until the clock ticks forward
                    currentTimeStamp = waitAndGetNextTimestamp(currentTimeStamp);
                }
            } else {
                // if timestamp is greater use 0
                sequence = 0L;
            }
            logger.debug("current timestamp and sequence {} , {} ", currentTimeStamp, sequence);
            lastTimestamp = currentTimeStamp;

            // Compound 41-bit ID calculation
            long nextIdGenerated = ((currentTimeStamp - timestampBaseReference) << TIMESTAMP_SHIFT)
                    | (dataCenterId << DATA_CENTER_ID_SHIFT) | (workerId << WORKER_ID_SHIFT) | sequence;
            logger.debug("nextIdGenerated {}", nextIdGenerated);
            return nextIdGenerated;

        } finally {
            lock.unlock();
        }

    }

    long getCurrentTimestamp() {
        return Instant.now().getEpochSecond(); // Extracted in seconds
    }

    long waitAndGetNextTimestamp(long lastTimestamp) {
        long timestamp = getCurrentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentTimestamp();
        }
        return timestamp;
    }

}
