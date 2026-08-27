package com.tpx.urlshort.service.alias;

import com.tpx.urlshort.dto.UrlRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class CustomAliasGenerator implements AliasGenerator {

    
    @Override
    public String generate(UrlRequestDTO requestDTO) {
        if (requestDTO.customAlias() == null || requestDTO.customAlias().isEmpty()) {
            throw new IllegalArgumentException("Custom alias cannot be null or empty");
        }
        return requestDTO.customAlias().trim();
    }

    @Override
    public boolean supports(UrlRequestDTO requestDTO) {
        return requestDTO.customAlias() != null && !requestDTO.customAlias().isBlank();
    }
}
