package com.tpx.urlshort.controller;

import com.tpx.urlshort.dto.UrlRequestDTO;
import com.tpx.urlshort.dto.UrlResponseDTO;
import com.tpx.urlshort.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "URL Shortener API", description = "Simple RESTful API for shortening URLs.")
@Validated
public class UrlController {

    private static final Logger logger = LoggerFactory.getLogger(UrlController.class);


    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/shorten")
    @Operation(summary = "Shorten a URL")
    @ApiResponse(responseCode = "201", description = "URL successfully shortened")
    @ApiResponse(responseCode = "400", description = "Invalid input or alias already taken")
    public ResponseEntity<UrlResponseDTO> shortenUrl(@Valid @RequestBody UrlRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(urlService.shortenAndPersistURL(request));
    }

    @GetMapping("/{alias}")
    @Operation(summary = "Redirect to full URL")
    @ApiResponse(responseCode = "302", description = "Redirect to the original URL")
    @ApiResponse(responseCode = "404", description = "Alias not found")
    public ResponseEntity<Void> redirectToFullUrl(@PathVariable @NotBlank(message = "Alias cannot be empty") @Size(min = 3, max = 50, message = "Alias must be between 3 and 50 characters") String alias) {
        String destinationUrl = urlService.findByAlias(alias).actualUrl();
        logger.info("{} redirecting to the url {} ", alias, destinationUrl);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(destinationUrl)).build();
    }

    @DeleteMapping("/{alias}")
    @Operation(summary = "Delete a shortened URL")
    @ApiResponse(responseCode = "204", description = "Successfully deleted")
    @ApiResponse(responseCode = "404", description = "Alias not found")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUrl(@PathVariable @NotBlank(message = "Alias cannot be empty") @Size(min = 3, max = 50, message = "Alias must be between 3 and 50 characters") String alias) {
        urlService.delete(alias);
    }


    @GetMapping("/urls")
    @Operation(summary = "List all shortened URLs")
    @ApiResponse(responseCode = "200", description = "A list of shortened URLs")
    public ResponseEntity<Page<UrlResponseDTO>> listAllUrls(@ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(urlService.getAll(pageable));
    }

}
