package com.tpx.urlshort.mapper;

import com.tpx.urlshort.domain.UrlDetails;
import com.tpx.urlshort.dto.UrlRequestDTO;
import com.tpx.urlshort.dto.UrlResponseDTO;

public class DTOMapper {

    public static UrlDetails mapToResponse(UrlRequestDTO requestDTO, String shortUrl) {
        UrlDetails urlDetails = new UrlDetails();
        urlDetails.setActualUrl(requestDTO.fullUrl());
        urlDetails.setShortUrl(shortUrl);
        return urlDetails;
    }

    public static UrlResponseDTO mapToResponse(UrlDetails urlDetails, String domain) {
        return new UrlResponseDTO(domain + urlDetails.getShortUrl(), urlDetails.getActualUrl());
    }
}
