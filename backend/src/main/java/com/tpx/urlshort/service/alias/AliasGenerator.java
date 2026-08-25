package com.tpx.urlshort.service.alias;

import com.tpx.urlshort.dto.UrlRequestDTO;

public interface AliasGenerator {

    String generate(UrlRequestDTO requestDTO);

    boolean supports(UrlRequestDTO requestDTO);

}
