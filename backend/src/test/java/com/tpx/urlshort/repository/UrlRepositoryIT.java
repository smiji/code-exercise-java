package com.tpx.urlshort.repository;

import com.tpx.urlshort.domain.UrlDetails;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

/**
 * This test class is created for future reason
 * if someone is creating any api  using complex custom sql
 */
@DataJpaTest
//to stop defaulting to H2
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UrlRepositoryIT {


    @Autowired
    UrlRepository urlRepository;

    @Test
    void testSave() {
        UrlDetails urlDetails = new UrlDetails();
        urlDetails.setActualUrl("abcd");
        urlDetails.setShortUrl("ab");
        UrlDetails persistedUrlDetails = urlRepository.save(urlDetails);
        Assertions.assertNotNull(persistedUrlDetails.getId());
        Assertions.assertNotNull(persistedUrlDetails.getCreatedAt());
        Assertions.assertNotNull(persistedUrlDetails.getUpdatedAt());
        Assertions.assertEquals("abcd", persistedUrlDetails.getActualUrl());
        Assertions.assertEquals("ab", persistedUrlDetails.getShortUrl());
    }

    @Test
    void testGetById() {
        UrlDetails urlDetails = new UrlDetails();
        urlDetails.setActualUrl("abcd");
        urlDetails.setShortUrl("ab");
        UrlDetails persistedUrlDetails = urlRepository.save(urlDetails);
        Assertions.assertNotNull(persistedUrlDetails.getId());
        Assertions.assertNotNull(persistedUrlDetails.getCreatedAt());
        Assertions.assertNotNull(persistedUrlDetails.getUpdatedAt());
        Assertions.assertEquals("abcd", persistedUrlDetails.getActualUrl());
        Assertions.assertEquals("ab", persistedUrlDetails.getShortUrl());
        Optional<UrlDetails> resultById = urlRepository.findById(persistedUrlDetails.getId());
        if (resultById.isEmpty()) {
            Assertions.fail("Expected persisted value , but found none");
        }
        UrlDetails urlDetailsFound = resultById.get();
        Assertions.assertEquals("abcd", urlDetailsFound.getActualUrl());
        Assertions.assertEquals("ab", urlDetailsFound.getShortUrl());
        Assertions.assertNotNull(persistedUrlDetails.getId());
    }

    @Test
    void testDelete() {
        UrlDetails urlDetails = new UrlDetails();
        urlDetails.setActualUrl("abcd");
        urlDetails.setShortUrl("ab");
        UrlDetails persistedUrlDetails = urlRepository.save(urlDetails);
        Assertions.assertNotNull(persistedUrlDetails.getId());
        Assertions.assertNotNull(persistedUrlDetails.getCreatedAt());
        Assertions.assertNotNull(persistedUrlDetails.getUpdatedAt());
        Assertions.assertEquals("abcd", persistedUrlDetails.getActualUrl());
        Assertions.assertEquals("ab", persistedUrlDetails.getShortUrl());
        //start deleting
        urlRepository.deleteById(persistedUrlDetails.getId());
        Optional<UrlDetails> resultById = urlRepository.findById(persistedUrlDetails.getId());
        Assertions.assertTrue(resultById.isEmpty());
    }

    @Test
    void testGetAll_paginated() {
        List<UrlDetails> urlDetailsLst = getUrlDetails();
        urlRepository.saveAll(urlDetailsLst);
        Pageable pageable = PageRequest.of(0, 2);
        Page<UrlDetails> allResp = urlRepository.findAllByOrderByCreatedAtDescShortUrlAsc(pageable);
        Assertions.assertNotNull(allResp);
        Assertions.assertEquals(2, allResp.getContent().size());
        Assertions.assertEquals(2, allResp.getTotalPages());
        Assertions.assertEquals(4, allResp.getTotalElements());
    }

    @Test
    void testFindByShortUrl() {
        UrlDetails urlDetails = new UrlDetails();
        urlDetails.setActualUrl("abcd");
        urlDetails.setShortUrl("ab");
        UrlDetails persistedUrlDetails = urlRepository.save(urlDetails);

        //Optional<UrlDetails> findByShortUrl(String shortUrl);
        //boolean existsByShortUrl(String shortUrl);
        Optional<UrlDetails> byShortUrl = urlRepository.findByShortUrl(persistedUrlDetails.getShortUrl());
        Assertions.assertFalse(byShortUrl.isEmpty());
        UrlDetails urlDetailsBySearch = byShortUrl.get();
        Assertions.assertNotNull(urlDetailsBySearch);
        Assertions.assertEquals("abcd", urlDetailsBySearch.getActualUrl());
    }

    @Test
    void testExistsByShortUrl() {
        UrlDetails urlDetails = new UrlDetails();
        urlDetails.setActualUrl("abcd");
        urlDetails.setShortUrl("ab");
        UrlDetails persistedUrlDetails = urlRepository.save(urlDetails);
        Assertions.assertTrue(urlRepository.existsByShortUrl(persistedUrlDetails.getShortUrl()));
    }

    //for pagination test
    List<UrlDetails> getUrlDetails() {
        //set data
        UrlDetails urlDetails1 = new UrlDetails();
        urlDetails1.setActualUrl("http://abcd/123243");
        urlDetails1.setShortUrl("ab");

        UrlDetails urlDetails2 = new UrlDetails();
        urlDetails2.setActualUrl("http://pqrst/123243");
        urlDetails2.setShortUrl("pq");

        UrlDetails urlDetails3 = new UrlDetails();
        urlDetails3.setActualUrl("http://pqrst12345/123243");
        urlDetails3.setShortUrl("pq12");

        UrlDetails urlDetails4 = new UrlDetails();
        urlDetails4.setActualUrl("http://ZZZpqrst12345/123243");
        urlDetails4.setShortUrl("ZZ12");
        return List.of(urlDetails1, urlDetails2, urlDetails3, urlDetails4);
    }
}