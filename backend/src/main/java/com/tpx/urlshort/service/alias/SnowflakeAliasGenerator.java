package com.tpx.urlshort.service.alias;

import com.tpx.urlshort.dto.UrlRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SnowflakeAliasGenerator implements AliasGenerator {

    private static final Logger logger = LoggerFactory.getLogger(SnowflakeAliasGenerator.class);

    @Override
    public String generate(UrlRequestDTO requestDTO) {
        //TODO -implement the generator
        return "";
    }

    @Override
    public boolean supports(UrlRequestDTO requestDTO) {
        logger.debug("Inside supports");
        return requestDTO == null || requestDTO.customAlias() == null || requestDTO.customAlias().isBlank();
    }
}
