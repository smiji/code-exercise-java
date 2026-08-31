package com.tpx.urlshort.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpx.urlshort.dto.UrlRequestDTO;
import com.tpx.urlshort.dto.UrlResponseDTO;
import com.tpx.urlshort.exception.AliasAlreadyPresentException;
import com.tpx.urlshort.exception.ItemNotFoundException;
import com.tpx.urlshort.service.UrlService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlController.class)
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UrlService urlService;

    @Test
    void shortenUrl_shouldReturn201AndBody() throws Exception {
        String fullUrl = "https://example.com/some/path";
        String alias = "mycustomalias";
        String expectedShortUrl = "http://localhost:8080/mycustomalias";
        UrlRequestDTO request = new UrlRequestDTO(fullUrl, alias);
        UrlResponseDTO response = new UrlResponseDTO(expectedShortUrl, fullUrl);
        Mockito.when(urlService.shortenAndPersistURL(request)).thenReturn(response);
        mockMvc.perform(post("/api/v1/shorten").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortUrl").value(expectedShortUrl))
                .andExpect(jsonPath("$.actualUrl").value(fullUrl));
    }

    @Test
    void shortenUrl_whenAliasTaken_shouldReturn400() throws Exception {
        String fullUrl = "https://example.com/some/path";
        String alias = "mycustomalias";
        UrlRequestDTO request = new UrlRequestDTO(fullUrl, alias);
        Mockito.when(urlService.shortenAndPersistURL(request))
                .thenThrow(new AliasAlreadyPresentException("The alias -mycustomalias already exist"));

        mockMvc.perform(post("/api/v1/shorten").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Invalid input or alias already taken"))
                .andExpect(jsonPath("$.message").value("The alias -mycustomalias already exist"));
    }

    @Test
    void shortenUrl_whenCustomAliasHasSpecialCharacters_shouldReturn400() throws Exception {
        String fullUrl = "https://example.com/some/path";
        UrlRequestDTO request = new UrlRequestDTO(fullUrl, "my alias!");

        mockMvc.perform(post("/api/v1/shorten").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void redirectToFullUrl_shouldReturn302AndLocationHeader() throws Exception {
        String shortUrl = "http://localhost:8080/mycustomalias";
        String actualUrl = "https://example.com/some/path";
        String alias = "mycustomalias";
        UrlResponseDTO response = new UrlResponseDTO(shortUrl, actualUrl);
        Mockito.when(urlService.findByAlias(alias)).thenReturn(response);
        mockMvc.perform(get("/api/v1/mycustomalias")).andExpect(status().isFound())
                .andExpect(header().string("Location", actualUrl));
    }

    @Test
    void redirectToFullUrl_whenAliasMissing_shouldReturn404() throws Exception {
        Mockito.when(urlService.findByAlias("missing-alias"))
                .thenThrow(new ItemNotFoundException("No details found for the alias missing-alias"));
        mockMvc.perform(get("/api/v1/missing-alias")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404)).andExpect(jsonPath("$.error").value("Alias not found"))
                .andExpect(jsonPath("$.message").value("No details found for the alias missing-alias"));
    }

    @Test
    void deleteUrl_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/to-delete")).andExpect(status().isNoContent());
        Mockito.verify(urlService).delete("to-delete");
    }

    @Test
    void deleteUrl_whenAliasMissing_shouldReturn404() throws Exception {

        Mockito.doThrow(new ItemNotFoundException("No details found for the alias not-here")).when(urlService)
                .delete("not-here");
        mockMvc.perform(delete("/api/v1/not-here"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Alias not found"))
                .andExpect(jsonPath("$.message").value("No details found for the alias not-here"));
    }

    @Test
    void listAllUrls_shouldReturn200AndPagedBody() throws Exception {
        List<UrlResponseDTO> content = List.of(
                new UrlResponseDTO("http://localhost:8080/xy", "https://site1.com"),
                new UrlResponseDTO("http://localhost:8080/ab", "https://site2.com"));
        Page<UrlResponseDTO> page = new PageImpl<>(content, PageRequest.of(0, 10), 2);
        Mockito.when(urlService.getAll(Mockito.any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/urls").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].shortUrl").value("http://localhost:8080/xy"))
                .andExpect(jsonPath("$.content[0].actualUrl").value("https://site1.com"))
                .andExpect(jsonPath("$.content[1].shortUrl").value("http://localhost:8080/ab"))
                .andExpect(jsonPath("$.content[1].actualUrl").value("https://site2.com"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }
}
