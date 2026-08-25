package com.tpx.urlshort.service.alias;

import com.tpx.urlshort.dto.UrlRequestDTO;
import com.tpx.urlshort.exception.ConfigMissingException;
import com.tpx.urlshort.exception.IllegalParametersException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class AliasResolverTest {


    @Test
    void testCustomGenerator() {
        AliasGenerator customGenerator = new CustomAliasGenerator();
        AliasGenerator snowFlakeIdGenerator = new SnowflakeAliasGenerator();
        AliasResolver aliasResolver = new AliasResolver(List.of(customGenerator, snowFlakeIdGenerator));
        UrlRequestDTO urlRequestDTO = new UrlRequestDTO("my-long-url", "mlu");
        String finalAlias = aliasResolver.resolveAndGenerate(urlRequestDTO);
        Assertions.assertEquals("mlu", finalAlias);
    }


    @Test
    void testCustomGenerator_when_both_missing() {
        AliasGenerator customGenerator = new CustomAliasGenerator();
        AliasGenerator snowFlakeIdGenerator = new SnowflakeAliasGenerator();
        AliasResolver aliasResolver = new AliasResolver(List.of());
        UrlRequestDTO urlRequestDTO = new UrlRequestDTO("my-long-url", "mlu");
        String expectedExceptionMessage = "No suitable alias generator found!";
        ConfigMissingException configMissingException = Assertions.assertThrows(ConfigMissingException.class, () -> {
            aliasResolver.resolveAndGenerate(urlRequestDTO);
        });
        Assertions.assertEquals(expectedExceptionMessage, configMissingException.getMessage());
    }

    @Test
    void testCustomGenerator_actual_url_isNull() {
        AliasGenerator customGenerator = new CustomAliasGenerator();
        AliasGenerator snowFlakeIdGenerator = new SnowflakeAliasGenerator();
        AliasResolver aliasResolver = new AliasResolver(List.of(customGenerator, snowFlakeIdGenerator));
        UrlRequestDTO urlRequestDTO = new UrlRequestDTO(null, null);
        String expectedExceptionMessage = "Invalid parameters , request or actual url cannot be null or empty";
        IllegalParametersException illegalParametersException = Assertions.assertThrows(IllegalParametersException.class, () -> {
            aliasResolver.resolveAndGenerate(urlRequestDTO);
        });
        Assertions.assertEquals(expectedExceptionMessage, illegalParametersException.getMessage());
    }

    @Test
    void testCustomGenerator_alias_isEmpty() {
        AliasGenerator customGenerator = new CustomAliasGenerator();
        AliasGenerator snowFlakeIdGenerator = new SnowflakeAliasGenerator();
        AliasResolver aliasResolver = new AliasResolver(List.of(customGenerator, snowFlakeIdGenerator));
        UrlRequestDTO urlRequestDTO = new UrlRequestDTO("my-long-url", null);
        String expectedExceptionMessage = "No suitable alias generator found!";
        String finalAlias = aliasResolver.resolveAndGenerate(urlRequestDTO);
        //TODO Snowflak generated ID required here..
    }



}