package com.tpx.urlshort.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record UrlRequestDTO(
                @NotBlank(message = "The full URL is required") @URL(message = "Please provide a valid URL format") String fullUrl,
                @Size(min = 3, max = 50, message = "Custom alias must be between 3 and 50 characters") @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Custom alias can contain only alphanumeric characters") String customAlias) {
}
