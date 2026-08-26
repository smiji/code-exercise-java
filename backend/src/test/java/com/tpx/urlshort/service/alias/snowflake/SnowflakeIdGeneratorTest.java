package com.tpx.urlshort.service.alias.snowflake;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

class SnowflakeIdGeneratorTest {

    private static final long EPOCH_SECONDS = 1767225600L;

    @Test
    void testGetCurrentTimestamp() {
        SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(1L, 1L, EPOCH_SECONDS);
        long currentTimestamp = snowflakeIdGenerator.getCurrentTimestamp();
        Assertions.assertTrue(currentTimestamp > 0L);
    }

    @Test
    void testWaitAndGetNextTimestamp() {
        SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(1L, 1L, EPOCH_SECONDS);
        long epochCurrentSecond = Instant.now().getEpochSecond();
        long newTimestamp = snowflakeIdGenerator.waitAndGetNextTimestamp(epochCurrentSecond);
        Assertions.assertTrue(epochCurrentSecond < newTimestamp);
    }

    @Test
    void testGetNextId(){
        SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(1L, 1L, EPOCH_SECONDS);
        long first = snowflakeIdGenerator.getNextId();
        long second = snowflakeIdGenerator.getNextId();
        System.out.println(first);
        System.out.println(second);
        Assertions.assertNotEquals(first, second);
    }

}