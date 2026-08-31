package com.tpx.urlshort.service;

import com.tpx.urlshort.cache.redis.UrlCacheService;
import com.tpx.urlshort.domain.UrlDetails;
import com.tpx.urlshort.dto.UrlRequestDTO;
import com.tpx.urlshort.dto.UrlResponseDTO;
import com.tpx.urlshort.exception.AliasAlreadyPresentException;
import com.tpx.urlshort.exception.IllegalParametersException;
import com.tpx.urlshort.exception.ItemNotFoundException;
import com.tpx.urlshort.repository.UrlRepository;
import com.tpx.urlshort.service.alias.AliasResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

class UrlServiceTest {

    @Test
    void testShortenAndPersists() {
        UrlRepository urlRepositoryMock = Mockito.mock(UrlRepository.class);
        AliasResolver aliasResolverMock = Mockito.mock(AliasResolver.class);
        UrlCacheService urlCacheServiceMock = Mockito.mock(UrlCacheService.class);

        String finalAlias = "my-final-alias";
        String expectedShortUrl = "http://localhost:8080/my-final-alias";

        UrlRequestDTO urlRequestDTO = new UrlRequestDTO("my-full-url", "alias");
        UrlDetails savedUrlDetails = new UrlDetails(1L, "my-full-url", finalAlias);

        // mock handling
        Mockito.when(aliasResolverMock.resolveAndGenerate(Mockito.any(UrlRequestDTO.class))).thenReturn(finalAlias);
        Mockito.when(urlRepositoryMock.saveAndFlush(Mockito.any(UrlDetails.class))).thenReturn(savedUrlDetails);

        UrlService urlService = new UrlService(urlRepositoryMock, aliasResolverMock, urlCacheServiceMock);
        urlService.appBaseUrl = "http://localhost:8080/";
        UrlResponseDTO urlResponseDTO = urlService.shortenAndPersistURL(urlRequestDTO);
        Mockito.verify(urlCacheServiceMock).put(finalAlias, "my-full-url");
        Assertions.assertEquals(expectedShortUrl, urlResponseDTO.shortUrl());
    }

    @Test
    void testShortenAndPersists_for_exception() {
        UrlRepository urlRepositoryMock = Mockito.mock(UrlRepository.class);
        AliasResolver aliasResolverMock = Mockito.mock(AliasResolver.class);
        UrlCacheService urlCacheServiceMock = Mockito.mock(UrlCacheService.class);

        String finalAlias = "my-final-alias";
        String exceptionMessage = "The alias -my-final-alias already exist";
        UrlRequestDTO urlRequestDTO = new UrlRequestDTO("my-full-url", "alias");
        // mock handling
        Mockito.when(aliasResolverMock.resolveAndGenerate(Mockito.any(UrlRequestDTO.class))).thenReturn(finalAlias);
        Mockito.when(urlRepositoryMock.saveAndFlush(Mockito.any(UrlDetails.class)))
                .thenThrow(new DataIntegrityViolationException("Already present"));
        UrlService urlService = new UrlService(urlRepositoryMock, aliasResolverMock, urlCacheServiceMock);
        urlService.appBaseUrl = "http://localhost:8080/";
        AliasAlreadyPresentException aliasAlreadyPresentException = Assertions.assertThrows(
                AliasAlreadyPresentException.class,
                () -> urlService.shortenAndPersistURL(urlRequestDTO));
        Assertions.assertEquals(exceptionMessage, aliasAlreadyPresentException.getMessage());

    }

    @Test
    void testFindByAlias() {
        UrlRepository urlRepositoryMock = Mockito.mock(UrlRepository.class);
        AliasResolver aliasResolverMock = Mockito.mock(AliasResolver.class);
        UrlCacheService urlCacheServiceMock = Mockito.mock(UrlCacheService.class);

        // Implement the second API
        String finalAlias = "my-final-alias";
        String actualUrl = "my-full-url";
        UrlDetails savedUrlDetails = new UrlDetails(1L, "my-full-url", finalAlias);
        Optional<UrlDetails> savedUrlDetailsOptional = Optional.of(savedUrlDetails);

        // handle mock
        Mockito.when(urlRepositoryMock.findByShortUrl(finalAlias)).thenReturn(savedUrlDetailsOptional);

        Mockito.when(urlCacheServiceMock.get(finalAlias)).thenReturn(Optional.empty());

        UrlService urlService = new UrlService(urlRepositoryMock, aliasResolverMock, urlCacheServiceMock);
        urlService.appBaseUrl = "http://localhost:8080/";
        UrlResponseDTO byAlias = urlService.findByAlias(finalAlias);
        Mockito.verify(urlCacheServiceMock).put(finalAlias, actualUrl);
        Assertions.assertEquals(actualUrl, byAlias.actualUrl());
    }

    @Test
    void testFindByAlias_input_null() {
        UrlRepository urlRepositoryMock = Mockito.mock(UrlRepository.class);
        AliasResolver aliasResolverMock = Mockito.mock(AliasResolver.class);
        UrlCacheService urlCacheServiceMock = Mockito.mock(UrlCacheService.class);

        String finalAlias = null;
        String expectedErrorMessage = "Invalid parameter alias - null";
        UrlService urlService = new UrlService(urlRepositoryMock, aliasResolverMock, urlCacheServiceMock);
        urlService.appBaseUrl = "http://localhost:8080/";
        IllegalParametersException illegalParametersException = Assertions.assertThrows(
                IllegalParametersException.class,
                () -> urlService.findByAlias(finalAlias));
        Assertions.assertEquals(expectedErrorMessage, illegalParametersException.getMessage());
    }

    @Test
    void testFindByAlias_return_empty() {
        UrlRepository urlRepositoryMock = Mockito.mock(UrlRepository.class);
        AliasResolver aliasResolverMock = Mockito.mock(AliasResolver.class);
        UrlCacheService urlCacheServiceMock = Mockito.mock(UrlCacheService.class);

        // Implement the second API
        String finalAlias = "my-final-alias";
        String expectedErrorMessage = "No details found for the alias my-final-alias";
        Optional<UrlDetails> savedUrlDetailsOptional = Optional.empty();

        // handle mock
        Mockito.when(urlRepositoryMock.findByShortUrl(finalAlias)).thenReturn(savedUrlDetailsOptional);
        Mockito.when(urlCacheServiceMock.get(finalAlias)).thenReturn(Optional.empty());

        UrlService urlService = new UrlService(urlRepositoryMock, aliasResolverMock, urlCacheServiceMock);
        urlService.appBaseUrl = "http://localhost:8080/";
        ItemNotFoundException itemNotFoundException = Assertions.assertThrows(ItemNotFoundException.class,
                () -> urlService.findByAlias(finalAlias));

        Assertions.assertEquals(expectedErrorMessage, itemNotFoundException.getMessage());
    }

    @Test
    void testDelete_aliasExists() {
        UrlRepository urlRepositoryMock = Mockito.mock(UrlRepository.class);
        AliasResolver aliasResolverMock = Mockito.mock(AliasResolver.class);
        UrlCacheService urlCacheServiceMock = Mockito.mock(UrlCacheService.class);

        String alias = "my-final-alias";
        UrlDetails savedUrlDetails = new UrlDetails(1L, "my-full-url", alias);

        Mockito.when(urlRepositoryMock.findByShortUrl(alias)).thenReturn(Optional.of(savedUrlDetails));

        UrlService urlService = new UrlService(urlRepositoryMock, aliasResolverMock, urlCacheServiceMock);
        urlService.appBaseUrl = "http://localhost:8080/";
        urlService.delete(alias);

        Mockito.verify(urlRepositoryMock).deleteById(1L);
        Mockito.verify(urlCacheServiceMock).evict(alias);
    }

    @Test
    void testFindByAlias_whenCacheReadFails_shouldStillUseDatabase() {
        UrlRepository urlRepositoryMock = Mockito.mock(UrlRepository.class);
        AliasResolver aliasResolverMock = Mockito.mock(AliasResolver.class);
        UrlCacheService urlCacheServiceMock = Mockito.mock(UrlCacheService.class);

        String finalAlias = "my-final-alias";
        String actualUrl = "my-full-url";
        UrlDetails savedUrlDetails = new UrlDetails(1L, actualUrl, finalAlias);

        Mockito.when(urlCacheServiceMock.get(finalAlias)).thenThrow(new RuntimeException("Redis down"));
        Mockito.when(urlRepositoryMock.findByShortUrl(finalAlias)).thenReturn(Optional.of(savedUrlDetails));

        UrlService urlService = new UrlService(urlRepositoryMock, aliasResolverMock, urlCacheServiceMock);
        urlService.appBaseUrl = "http://localhost:8080/";

        UrlResponseDTO byAlias = urlService.findByAlias(finalAlias);

        Assertions.assertEquals(actualUrl, byAlias.actualUrl());
        Mockito.verify(urlCacheServiceMock).put(finalAlias, actualUrl);
    }

    @Test
    void testShortenAndPersist_whenCacheWriteFails_shouldStillPersist() {
        UrlRepository urlRepositoryMock = Mockito.mock(UrlRepository.class);
        AliasResolver aliasResolverMock = Mockito.mock(AliasResolver.class);
        UrlCacheService urlCacheServiceMock = Mockito.mock(UrlCacheService.class);

        String finalAlias = "my-final-alias";
        String actualUrl = "my-full-url";
        UrlRequestDTO urlRequestDTO = new UrlRequestDTO(actualUrl, "alias");
        UrlDetails savedUrlDetails = new UrlDetails(1L, actualUrl, finalAlias);

        Mockito.when(aliasResolverMock.resolveAndGenerate(Mockito.any(UrlRequestDTO.class))).thenReturn(finalAlias);
        Mockito.when(urlRepositoryMock.saveAndFlush(Mockito.any(UrlDetails.class))).thenReturn(savedUrlDetails);
        Mockito.doThrow(new RuntimeException("Redis down")).when(urlCacheServiceMock).put(finalAlias, actualUrl);

        UrlService urlService = new UrlService(urlRepositoryMock, aliasResolverMock, urlCacheServiceMock);
        urlService.appBaseUrl = "http://localhost:8080/";

        UrlResponseDTO result = urlService.shortenAndPersistURL(urlRequestDTO);

        Assertions.assertEquals("http://localhost:8080/my-final-alias", result.shortUrl());
        Assertions.assertEquals(actualUrl, result.actualUrl());
    }

    @Test
    void testDelete_aliasMissing_shouldThrowItemNotFoundException() {
        UrlRepository urlRepositoryMock = Mockito.mock(UrlRepository.class);
        AliasResolver aliasResolverMock = Mockito.mock(AliasResolver.class);
        UrlCacheService urlCacheServiceMock = Mockito.mock(UrlCacheService.class);

        String alias = "missing-alias";
        String expectedErrorMessage = "No details found for the alias missing-alias";

        Mockito.when(urlRepositoryMock.findByShortUrl(alias)).thenReturn(Optional.empty());

        UrlService urlService = new UrlService(urlRepositoryMock, aliasResolverMock, urlCacheServiceMock);
        urlService.appBaseUrl = "http://localhost:8080/";

        ItemNotFoundException itemNotFoundException = Assertions.assertThrows(ItemNotFoundException.class,
                () -> urlService.delete(alias));
        Assertions.assertEquals(expectedErrorMessage, itemNotFoundException.getMessage());
        Mockito.verify(urlRepositoryMock, Mockito.never()).deleteById(Mockito.anyLong());
    }

    @Test
    void testDelete_aliasNull_shouldThrowIllegalParametersException() {
        UrlRepository urlRepositoryMock = Mockito.mock(UrlRepository.class);
        AliasResolver aliasResolverMock = Mockito.mock(AliasResolver.class);
        UrlCacheService urlCacheServiceMock = Mockito.mock(UrlCacheService.class);

        String expectedErrorMessage = "Invalid parameter alias - null";

        UrlService urlService = new UrlService(urlRepositoryMock, aliasResolverMock, urlCacheServiceMock);
        urlService.appBaseUrl = "http://localhost:8080/";

        IllegalParametersException illegalParametersException = Assertions.assertThrows(
                IllegalParametersException.class,
                () -> urlService.delete(null));

        Assertions.assertEquals(expectedErrorMessage, illegalParametersException.getMessage());

    }

}