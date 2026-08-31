package com.tpx.urlshort.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpx.urlshort.cache.redis.UrlCacheService;
import com.tpx.urlshort.domain.UrlDetails;
import com.tpx.urlshort.dto.UrlRequestDTO;
import com.tpx.urlshort.repository.UrlRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UrlControllerIT {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UrlRepository urlRepository;

  @MockBean
  private UrlCacheService urlCacheService;

  @BeforeEach
  void cleanUp() {
    urlRepository.deleteAll();
    // Mock cache to always return empty (cache miss) - forces DB lookup
    when(urlCacheService.get(anyString())).thenReturn(Optional.empty());
  }

  @Test
  void shortenUrl_shouldReturn201AndPersist() throws Exception {
    UrlRequestDTO request = new UrlRequestDTO("https://example.com/some/path", "mycustomalias");

    mockMvc.perform(post("/api/v1/shorten")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/mycustomalias"))
        .andExpect(jsonPath("$.actualUrl").value("https://example.com/some/path"));

    Assertions.assertTrue(urlRepository.findByShortUrl("mycustomalias").isPresent());
  }

  @Test
  void shortenUrl_whenCustomAliasHasSpecialCharacters_shouldReturn400() throws Exception {
    UrlRequestDTO request = new UrlRequestDTO("https://example.com/some/path", "my alias!");

    mockMvc.perform(post("/api/v1/shorten")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shortenUrl_whenAliasAlreadyExists_shouldReturn400() throws Exception {
    UrlDetails existing = new UrlDetails();
    existing.setActualUrl("https://already.com");
    existing.setShortUrl("dupalias");
    urlRepository.saveAndFlush(existing);

    UrlRequestDTO request = new UrlRequestDTO("https://example.com/new", "dupalias");

    mockMvc.perform(post("/api/v1/shorten")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Invalid input or alias already taken"));
  }

  @Test
  void redirectToFullUrl_shouldReturn302AndLocation() throws Exception {
    UrlDetails existing = new UrlDetails();
    existing.setActualUrl("https://lookup.com/path");
    existing.setShortUrl("lookup-alias");
    urlRepository.saveAndFlush(existing);

    mockMvc.perform(get("/api/v1/lookup-alias"))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", "https://lookup.com/path"));
  }

  @Test
  void redirectToFullUrl_whenAliasMissing_shouldReturn404() throws Exception {
    mockMvc.perform(get("/api/v1/missing-alias"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Alias not found"));
  }

  @Test
  void deleteUrl_shouldReturn204AndRemoveEntry() throws Exception {
    UrlDetails existing = new UrlDetails();
    existing.setActualUrl("https://delete-me.com");
    existing.setShortUrl("to-delete");
    urlRepository.saveAndFlush(existing);

    mockMvc.perform(delete("/api/v1/to-delete"))
        .andExpect(status().isNoContent());

    Assertions.assertTrue(urlRepository.findByShortUrl("to-delete").isEmpty());
  }

  @Test
  void deleteUrl_whenAliasMissing_shouldReturn404() throws Exception {
    mockMvc.perform(delete("/api/v1/not-here"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Alias not found"));
  }

  @Test
  void listAllUrls_shouldReturn200AndPagedContent() throws Exception {
    UrlDetails first = new UrlDetails();
    first.setActualUrl("https://site1.com");
    first.setShortUrl("AB");
    first.setCreatedAt(LocalDateTime.of(2026, 8, 26, 11, 55, 0));
    first.setUpdatedAt(first.getCreatedAt());

    UrlDetails second = new UrlDetails();
    second.setActualUrl("https://site2.com");
    second.setShortUrl("pq");
    second.setCreatedAt(LocalDateTime.of(2026, 8, 26, 11, 58, 0));
    second.setUpdatedAt(second.getCreatedAt());

    UrlDetails third = new UrlDetails();
    third.setActualUrl("https://site3.com");
    third.setShortUrl("xy");
    third.setCreatedAt(LocalDateTime.of(2026, 8, 26, 11, 59, 0));
    third.setUpdatedAt(third.getCreatedAt());

    urlRepository.saveAll(java.util.List.of(first, second, third));

    mockMvc.perform(get("/api/v1/urls")
        .param("page", "0")
        .param("size", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].shortUrl").value("http://localhost:8080/xy"))
        .andExpect(jsonPath("$.content[0].actualUrl").value("https://site3.com"))
        .andExpect(jsonPath("$.content[1].shortUrl").value("http://localhost:8080/pq"))
        .andExpect(jsonPath("$.content[1].actualUrl").value("https://site2.com"))
        .andExpect(jsonPath("$.totalElements").value(3));
  }
}
