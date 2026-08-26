package com.tpx.urlshort.service.alias;

import com.tpx.urlshort.dto.UrlRequestDTO;
import com.tpx.urlshort.exception.ConfigMissingException;
import com.tpx.urlshort.exception.IllegalParametersException;
import com.tpx.urlshort.service.alias.snowflake.SnowflakeIdGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

class AliasResolverTest {

    SnowflakeIdGenerator snowflakeIdGeneratorMock = Mockito.mock(SnowflakeIdGenerator.class);
    Base62Encoder base62EncoderMock = Mockito.mock(Base62Encoder.class);

    @Test
    void testCustomGenerator() {
        AliasGenerator customGenerator = new CustomAliasGenerator();
        AliasGenerator snowFlakeIdGenerator = new SnowflakeAliasGenerator(snowflakeIdGeneratorMock, base62EncoderMock);
        AliasResolver aliasResolver = new AliasResolver(List.of(customGenerator, snowFlakeIdGenerator));
        UrlRequestDTO urlRequestDTO = new UrlRequestDTO("my-long-url", "mlu");
        String finalAlias = aliasResolver.resolveAndGenerate(urlRequestDTO);
        Assertions.assertEquals("mlu", finalAlias);
    }


    @Test
    void testCustomGenerator_when_both_missing() {
        AliasGenerator customGenerator = new CustomAliasGenerator();
        AliasGenerator snowFlakeIdGenerator = new SnowflakeAliasGenerator(snowflakeIdGeneratorMock, base62EncoderMock);
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
        AliasGenerator snowFlakeIdGenerator = new SnowflakeAliasGenerator(snowflakeIdGeneratorMock, base62EncoderMock);
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
        AliasGenerator snowFlakeIdGenerator = new SnowflakeAliasGenerator(snowflakeIdGeneratorMock, base62EncoderMock);
        long uniqueNumber = 564654654L;
        String expectedAlias = "helowld";
        //handle the mock for the snowFlake and the base62 encoder
        Mockito.when(snowflakeIdGeneratorMock.getNextId()).thenReturn(uniqueNumber);
        Mockito.when(base62EncoderMock.encode(uniqueNumber)).thenReturn(expectedAlias);

        AliasResolver aliasResolver = new AliasResolver(List.of(customGenerator, snowFlakeIdGenerator));
        UrlRequestDTO urlRequestDTO = new UrlRequestDTO("my-long-url", null);

        String finalAlias = aliasResolver.resolveAndGenerate(urlRequestDTO);
        Assertions.assertEquals(expectedAlias, finalAlias);
    }


}