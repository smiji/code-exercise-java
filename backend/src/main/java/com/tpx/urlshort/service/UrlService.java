package com.tpx.urlshort.service;

import com.tpx.urlshort.cache.redis.UrlCacheService;
import com.tpx.urlshort.domain.UrlDetails;
import com.tpx.urlshort.dto.UrlRequestDTO;
import com.tpx.urlshort.dto.UrlResponseDTO;
import com.tpx.urlshort.exception.AliasAlreadyPresentException;
import com.tpx.urlshort.exception.IllegalParametersException;
import com.tpx.urlshort.exception.ItemNotFoundException;
import com.tpx.urlshort.mapper.DTOMapper;
import com.tpx.urlshort.repository.UrlRepository;
import com.tpx.urlshort.service.alias.AliasResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UrlService {

    private static final Logger logger = LoggerFactory.getLogger(UrlService.class);
    private final UrlRepository urlRepository;
    private final AliasResolver aliasResolver;
    private final UrlCacheService urlCacheService;

    @Value("${app.base-url:http://localhost:8080/}")
    String appBaseUrl;

    public UrlService(UrlRepository urlRepository, AliasResolver resolver, UrlCacheService urlCacheService) {
        this.urlRepository = urlRepository;
        this.aliasResolver = resolver;
        this.urlCacheService = urlCacheService;
    }

    // for shortening the url
    public UrlResponseDTO shortenAndPersistURL(UrlRequestDTO urlRequestDTO) {
        String finalAlias = aliasResolver.resolveAndGenerate(urlRequestDTO);
        try {
            logger.info("final alias - {} generated for the url - {}", finalAlias, urlRequestDTO.fullUrl());
            UrlDetails savedDetails = urlRepository.saveAndFlush(DTOMapper.mapToResponse(urlRequestDTO, finalAlias));
            try {
                urlCacheService.put(finalAlias, savedDetails.getActualUrl());
            } catch (RuntimeException e) {
                logger.warn("Cache write skipped for alias '{}': {}", finalAlias, e.getMessage());
            }
            return DTOMapper.mapToResponse(savedDetails, appBaseUrl);
        } catch (DataAccessException e) {
            String message = String.format("The alias -%s already exist", finalAlias);
            logger.error("The alias -{} already  exist , reason: {}", finalAlias, e.getMessage());
            throw new AliasAlreadyPresentException(message);
        }
    }

    public UrlResponseDTO findByAlias(String aliasName) {
        try {
            Optional<String> cachedActualUrl = urlCacheService.get(aliasName);
            if (cachedActualUrl.isPresent()) {
                return new UrlResponseDTO(appBaseUrl + aliasName, cachedActualUrl.get());
            }
        } catch (RuntimeException e) {
            logger.warn("Cache read skipped for alias '{}': {}", aliasName, e.getMessage());
        }

        Optional<UrlDetails> urlDetailsByShortUrl = findByAliasCommon(aliasName);
        if (urlDetailsByShortUrl.isEmpty()) {
            String message = String.format("No details found for the alias %s", aliasName);
            logger.error(message);
            throw new ItemNotFoundException(message);
        }

        UrlDetails urlDetails = urlDetailsByShortUrl.get();
        try {
            urlCacheService.put(aliasName, urlDetails.getActualUrl());
        } catch (RuntimeException e) {
            logger.warn("Cache save skipped for alias '{}': {}", aliasName, e.getMessage());
        }
        return DTOMapper.mapToResponse(urlDetails, appBaseUrl);
    }

    public Page<UrlResponseDTO> getAll(Pageable pageable) {
        return urlRepository.findAllByOrderByCreatedAtDescShortUrlAsc(pageable)
                .map(urlDetails -> DTOMapper.mapToResponse(urlDetails, appBaseUrl));
    }

    public void delete(String alias) {
        Optional<UrlDetails> urlDetailsByAlias = findByAliasCommon(alias);
        if (urlDetailsByAlias.isEmpty()) {
            String message = String.format("No details found for the alias %s", alias);
            logger.debug(message);
            throw new ItemNotFoundException(message);
        }
        urlRepository.deleteById(urlDetailsByAlias.get().getId());
        try {
            urlCacheService.evict(alias);
        } catch (RuntimeException e) {
            logger.warn("Cache eviction skipped for alias '{}': {}", alias, e.getMessage());
        }
    }

    Optional<UrlDetails> findByAliasCommon(String aliasName) {
        if (aliasName == null || aliasName.isEmpty()) {
            String message = String.format("Invalid parameter alias - %s", aliasName);
            logger.error(message);
            throw new IllegalParametersException(message);
        }
        return urlRepository.findByShortUrl(aliasName);
    }
}
