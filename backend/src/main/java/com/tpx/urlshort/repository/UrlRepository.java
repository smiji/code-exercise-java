package com.tpx.urlshort.repository;

import com.tpx.urlshort.domain.UrlDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<UrlDetails, Long> {

    Optional<UrlDetails> findByShortUrl(String shortUrl);

    boolean existsByShortUrl(String shortUrl);
}
