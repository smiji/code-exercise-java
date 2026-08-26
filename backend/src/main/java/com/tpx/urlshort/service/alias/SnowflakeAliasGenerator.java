package com.tpx.urlshort.service.alias;

import com.tpx.urlshort.dto.UrlRequestDTO;
import com.tpx.urlshort.service.alias.snowflake.SnowflakeIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SnowflakeAliasGenerator implements AliasGenerator {

    private static final Logger logger = LoggerFactory.getLogger(SnowflakeAliasGenerator.class);
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final Base62Encoder encoder;

    public SnowflakeAliasGenerator(SnowflakeIdGenerator snowflakeIdGenerator, Base62Encoder encoder) {
        this.snowflakeIdGenerator = snowflakeIdGenerator;
        this.encoder = encoder;

    }

    @Override
    public String generate(UrlRequestDTO requestDTO) {
        long nextId = snowflakeIdGenerator.getNextId();
        logger.debug("The id -{}- is generated for the  url -{}-", nextId, requestDTO.fullUrl());
        String finalAlias = encoder.encode(nextId);
        logger.debug("The final alias -{}- is generated for the  url -{}-", finalAlias, requestDTO.fullUrl());
        return finalAlias;
    }

    @Override
    public boolean supports(UrlRequestDTO requestDTO) {
        logger.debug("Inside supports");
        return requestDTO == null || requestDTO.customAlias() == null || requestDTO.customAlias().isBlank();
    }
}
