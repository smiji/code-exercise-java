package com.tpx.urlshort.service;

import com.tpx.urlshort.domain.UrlDetails;
import com.tpx.urlshort.dto.UrlRequestDTO;
import com.tpx.urlshort.dto.UrlResponseDTO;
import com.tpx.urlshort.exception.AliasAlreadyPresentException;
import com.tpx.urlshort.exception.IllegalParametersException;
import com.tpx.urlshort.exception.ItemNotFoundException;
import com.tpx.urlshort.repository.UrlRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UrlServiceIT {



    @Autowired
    private UrlService urlService;

    @Autowired
    private UrlRepository urlRepository;

    @Value("${app.base-url:http://localhost:8080/}")
    String appBaseUrl;

    @BeforeEach
    void cleanUp() {
        urlRepository.deleteAll();
    }

    @Test
    void shortenAndPersistURL_shouldSaveAndReturnMappedResponse() {
        String actualUrl = "https://example.com/some/path";
        String customAlias = "my-custom-alias";
        UrlRequestDTO request = new UrlRequestDTO(actualUrl, customAlias);
        UrlResponseDTO response = urlService.shortenAndPersistURL(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(appBaseUrl+customAlias, response.shortUrl());
        Assertions.assertEquals(actualUrl, response.actualUrl());

        Optional<UrlDetails> persisted = urlRepository.findByShortUrl(customAlias);
        Assertions.assertTrue(persisted.isPresent());
        Assertions.assertEquals(actualUrl, persisted.get().getActualUrl());
    }

    @Test
    void shortenAndPersistURL_whenAliasAlreadyExists_shouldThrowAliasAlreadyPresentException() {
        UrlDetails existing = new UrlDetails();
        String actualUrl = "https://already.com";
        String shortUrl = "dup-alias";
        String newUrlToShorten = "https://example.com/new";
        String expectedErrorMessage = "The alias -dup-alias already exist";

        existing.setActualUrl(actualUrl);
        existing.setShortUrl(shortUrl);

        urlRepository.saveAndFlush(existing);
        UrlRequestDTO request = new UrlRequestDTO(newUrlToShorten, shortUrl);
        AliasAlreadyPresentException aliasAlreadyPresentException = Assertions
                .assertThrows(AliasAlreadyPresentException.class, () -> urlService.shortenAndPersistURL(request));
        Assertions.assertEquals(expectedErrorMessage, aliasAlreadyPresentException.getMessage());
    }

    @Test
    void findByAlias_shouldReturnResponseWhenAliasExists() {
        UrlDetails existing = new UrlDetails();

        String actualUrl = "https://lookup.com";
        String shortUrl = "lookup-alias";

        existing.setActualUrl(actualUrl);
        existing.setShortUrl(shortUrl);
        urlRepository.saveAndFlush(existing);

        UrlResponseDTO response = urlService.findByAlias(shortUrl);

        Assertions.assertEquals(appBaseUrl+shortUrl, response.shortUrl());
        Assertions.assertEquals(actualUrl, response.actualUrl());
    }

    @Test
    void findByAlias_whenAliasDoesNotExist_shouldThrowItemNotFoundException() {
        String expectedMessage = "No details found for the alias missing-alias";
        ItemNotFoundException itemNotFoundException = Assertions.assertThrows(ItemNotFoundException.class,
                () -> urlService.findByAlias("missing-alias"));
        Assertions.assertEquals(expectedMessage, itemNotFoundException.getMessage());
    }

    @Test
    void findByAlias_whenAliasIsNull_shouldThrowIllegalParametersException() {
        String expectedMessage = "Invalid parameter alias - null";
        IllegalParametersException illegalParametersException = Assertions
                .assertThrows(IllegalParametersException.class, () -> urlService.findByAlias(null));
        Assertions.assertEquals(expectedMessage, illegalParametersException.getMessage());
    }

    @Test
    void delete_shouldRemoveEntryWhenAliasExists() {

        String actualUrl = "https://delete-me.com";
        String shortUrl = "to-delete";

        UrlDetails existing = new UrlDetails();
        existing.setActualUrl(actualUrl);
        existing.setShortUrl(shortUrl);
        urlRepository.saveAndFlush(existing);

        urlService.delete(shortUrl);

        Assertions.assertFalse(urlRepository.findByShortUrl(shortUrl).isPresent());
    }

    @Test
    void delete_whenAliasDoesNotExist_noException() {
        Assertions.assertThrows(ItemNotFoundException.class,()->urlService.delete("not-here"));
    }

    @Test
    void testGetAll() {
        Pageable pageable = PageRequest.of(0, 3);
        urlRepository.saveAll(prepareDataForPagination());

        Page<UrlResponseDTO> page0 = urlService.getAll(pageable);
        Assertions.assertEquals(3, page0.getSize());
        Assertions.assertEquals(3, page0.getContent().size());

        // createdAt desc: latest timestamp first; then shortUrl asc when createdAt ties
        Assertions.assertEquals(appBaseUrl+"xy", page0.getContent().get(0).shortUrl());
        Assertions.assertEquals(appBaseUrl+"pq", page0.getContent().get(1).shortUrl());
        Assertions.assertEquals(appBaseUrl+"AB", page0.getContent().get(2).shortUrl());

        UrlDetails urlDetails = new UrlDetails();
        urlDetails.setActualUrl("http://abcd/123243");
        urlDetails.setShortUrl("zzz");
        urlDetails.setCreatedAt(LocalDateTime.of(2026, 8, 26, 12, 0, 0));
        urlDetails.setUpdatedAt(urlDetails.getCreatedAt());

        // add one more and see that is coming at the beginning
        urlRepository.save(urlDetails);

        Page<UrlResponseDTO> page1 = urlService.getAll(pageable);
        Assertions.assertEquals(3, page1.getSize());
        Assertions.assertEquals(appBaseUrl+"zzz", page1.getContent().get(0).shortUrl());
        Assertions.assertEquals(appBaseUrl+"xy", page1.getContent().get(1).shortUrl());
        Assertions.assertEquals(appBaseUrl+"pq", page1.getContent().get(2).shortUrl());

    }

    private List<UrlDetails> prepareDataForPagination() {
        // for pagination test
        // set data
        UrlDetails urlDetails1 = new UrlDetails();
        urlDetails1.setActualUrl("http://abcd/123243");
        urlDetails1.setShortUrl("AB");
        urlDetails1.setCreatedAt(LocalDateTime.of(2026, 8, 26, 11, 55, 0));
        urlDetails1.setUpdatedAt(urlDetails1.getCreatedAt());

        UrlDetails urlDetails2 = new UrlDetails();
        urlDetails2.setActualUrl("http://pqrst/123243");
        urlDetails2.setShortUrl("pq");
        urlDetails2.setCreatedAt(LocalDateTime.of(2026, 8, 26, 11, 58, 0));
        urlDetails2.setUpdatedAt(urlDetails2.getCreatedAt());

        UrlDetails urlDetails3 = new UrlDetails();
        urlDetails3.setActualUrl("http://pqrst12345/123243");
        urlDetails3.setShortUrl("xy");
        urlDetails3.setCreatedAt(LocalDateTime.of(2026, 8, 26, 11, 59, 0));
        urlDetails3.setUpdatedAt(urlDetails3.getCreatedAt());

        UrlDetails urlDetails4 = new UrlDetails();
        urlDetails4.setActualUrl("http://ZZZpqrst12345/123243");
        urlDetails4.setShortUrl("ZZ12");
        urlDetails4.setCreatedAt(LocalDateTime.of(2026, 8, 26, 11, 55, 0));
        urlDetails4.setUpdatedAt(urlDetails4.getCreatedAt());

        return List.of(urlDetails1, urlDetails2, urlDetails3, urlDetails4);
    }
}
