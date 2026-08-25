package com.tpx.urlshort.service.alias;

import com.tpx.urlshort.dto.UrlRequestDTO;
import com.tpx.urlshort.exception.ConfigMissingException;
import com.tpx.urlshort.exception.IllegalParametersException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AliasResolver {

    private static final Logger logger = LoggerFactory.getLogger(AliasResolver.class);
    private final List<AliasGenerator> generators;

    public AliasResolver(List<AliasGenerator> generators) {
        this.generators = generators;
    }

    public String resolveAndGenerate(UrlRequestDTO urlRequestDTO) {

        logger.debug("Inside the resolveAndGenerate, ");
        if (urlRequestDTO == null || urlRequestDTO.fullUrl() == null || urlRequestDTO.fullUrl().isEmpty()) {
            logger.error("Invalid parameters , request or actual url cannot be null or empty ");
            throw new IllegalParametersException("Invalid parameters , request or actual url cannot be null or empty");
        }
        return generators
                .stream()
                .filter(gen -> gen.supports(urlRequestDTO))
                .findFirst()
                .map(gen -> gen.generate(urlRequestDTO))
                .orElseThrow(() -> new ConfigMissingException("No suitable alias generator found!"));
    }

}
