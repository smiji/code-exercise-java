package com.tpx.urlshort.service;

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

    private final UrlRepository urlRepositoryMock = Mockito.mock(UrlRepository.class);
    private final AliasResolver aliasResolverMock = Mockito.mock(AliasResolver.class);

    @Test
    void testShortenAndPersists() {
        String finalAlias = "my-final-alias";

        UrlRequestDTO urlRequestDTO = new UrlRequestDTO("my-full-url", "alias");
        UrlDetails savedUrlDetails = new UrlDetails(1L, "my-full-url", finalAlias);

        // mock handling
        Mockito.when(aliasResolverMock.resolveAndGenerate(Mockito.any(UrlRequestDTO.class))).thenReturn(finalAlias);
        Mockito.when(urlRepositoryMock.saveAndFlush(Mockito.any(UrlDetails.class))).thenReturn(savedUrlDetails);

        UrlService urlService = new UrlService(urlRepositoryMock, aliasResolverMock);
        UrlResponseDTO urlResponseDTO = urlService.shortenAndPersistURL(urlRequestDTO);
        Assertions.assertEquals(finalAlias, urlResponseDTO.shortUrl());
    }

    @Test
    void testShortenAndPersists_for_exception() {
        String finalAlias = "my-final-alias";
        String exceptionMessage = "The alias -my-final-alias already exist";
        UrlRequestDTO urlRequestDTO = new UrlRequestDTO("my-full-url", "alias");
        // mock handling
        Mockito.when(aliasResolverMock.resolveAndGenerate(Mockito.any(UrlRequestDTO.class))).thenReturn(finalAlias);
        Mockito.when(urlRepositoryMock.saveAndFlush(Mockito.any(UrlDetails.class)))
                .thenThrow(new DataIntegrityViolationException("Already present"));
        UrlService urlService = new UrlService(urlRepositoryMock, aliasResolverMock);
        AliasAlreadyPresentException aliasAlreadyPresentException = Assertions.assertThrows(
                AliasAlreadyPresentException.class,
                () -> urlService.shortenAndPersistURL(urlRequestDTO));
        Assertions.assertEquals(exceptionMessage, aliasAlreadyPresentException.getMessage());

    }

    @Test
    void testFindByAlias() {
        // Implement the second API
        String finalAlias = "my-final-alias";
        String actualUrl = "my-full-url";
        UrlDetails savedUrlDetails = new UrlDetails(1L, "my-full-url", finalAlias);
        Optional<UrlDetails> savedUrlDetailsOptional = Optional.of(savedUrlDetails);

        // handle mock
        Mockito.when(urlRepositoryMock.findByShortUrl(finalAlias)).thenReturn(savedUrlDetailsOptional);

        UrlService urlService = new UrlService(urlRepositoryMock, aliasResolverMock);
        UrlResponseDTO byAlias = urlService.findByAlias(finalAlias);
        Assertions.assertEquals(actualUrl, byAlias.actualUrl());
    }

    @Test
    void testFindByAlias_input_null() {
        String finalAlias = null;
        String expectedErrorMessage = "Invalid parameter alias - null";
        UrlService urlService = new UrlService(urlRepositoryMock, aliasResolverMock);
        IllegalParametersException illegalParametersException = Assertions.assertThrows(
                IllegalParametersException.class,
                () -> urlService.findByAlias(finalAlias));
        Assertions.assertEquals(expectedErrorMessage, illegalParametersException.getMessage());
    }

    @Test
    void testFindByAlias_return_empty() {
        // Implement the second API
        String finalAlias = "my-final-alias";
        String expectedErrorMessage = "No details found for the alias my-final-alias";
        Optional<UrlDetails> savedUrlDetailsOptional = Optional.empty();

        // handle mock
        Mockito.when(urlRepositoryMock.findByShortUrl(finalAlias)).thenReturn(savedUrlDetailsOptional);
        UrlService urlService = new UrlService(urlRepositoryMock, aliasResolverMock);
        ItemNotFoundException itemNotFoundException = Assertions.assertThrows(ItemNotFoundException.class,
                () -> urlService.findByAlias(finalAlias));

        Assertions.assertEquals(expectedErrorMessage, itemNotFoundException.getMessage());
    }

    @Test
    void testDelete_aliasExists() {
        String alias = "my-final-alias";
        UrlDetails savedUrlDetails = new UrlDetails(1L, "my-full-url", alias);

        Mockito.when(urlRepositoryMock.findByShortUrl(alias)).thenReturn(Optional.of(savedUrlDetails));

        UrlService urlService = new UrlService(urlRepositoryMock, aliasResolverMock);
        urlService.delete(alias);

        Mockito.verify(urlRepositoryMock).deleteById(1L);
    }

    @Test
    void testDelete_aliasMissing_shouldNotThrow() {
        String alias = "missing-alias";

        Mockito.when(urlRepositoryMock.findByShortUrl(alias)).thenReturn(Optional.empty());

        UrlService urlService = new UrlService(urlRepositoryMock, aliasResolverMock);

        Assertions.assertDoesNotThrow(() -> urlService.delete(alias));
        Mockito.verify(urlRepositoryMock, Mockito.never()).deleteById(Mockito.anyLong());
    }

    @Test
    void testDelete_aliasNull_shouldThrowIllegalParametersException() {
        String expectedErrorMessage = "Invalid parameter alias - null";

        UrlService urlService = new UrlService(urlRepositoryMock, aliasResolverMock);
        IllegalParametersException illegalParametersException = Assertions.assertThrows(
                IllegalParametersException.class,
                () -> urlService.delete(null));

        Assertions.assertEquals(expectedErrorMessage, illegalParametersException.getMessage());

    }

}