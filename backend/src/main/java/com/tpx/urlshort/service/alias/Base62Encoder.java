package com.tpx.urlshort.service.alias;

import com.tpx.urlshort.exception.IllegalParametersException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * To generate a 7 length long alphabet
 */
@Component
public class Base62Encoder {

    private static final Logger logger = LoggerFactory.getLogger(Base62Encoder.class);
    private static final char[] ALPHA_NUMERICS_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
            .toCharArray();
    private static final Integer ALPHA_NUMERICS_LENGTH = ALPHA_NUMERICS_CHARS.length;
    private static final int FIXED_SIZE_OF_SHORTENED_URL = 7;
    private static final long TOKEN_THRESHOLD = 3521614606207L; // 62^7 - 1

    public String encode(long number) {

        logger.debug("Encoding started for {}", number);

        if (number < 2) {
            String message = String.format("Invalid input too less to consider %d", number);
            logger.error(message);
            throw new IllegalParametersException(message);
        }

        if (number > TOKEN_THRESHOLD) {
            String message = String.format("Value %d exceeds maximum capacity for %d chars ( %d )", number,
                    FIXED_SIZE_OF_SHORTENED_URL, TOKEN_THRESHOLD);
            logger.error(message);
            throw new IllegalParametersException(message);

        }

        char[] numCharHolder = new char[FIXED_SIZE_OF_SHORTENED_URL];
        int placer = FIXED_SIZE_OF_SHORTENED_URL;

        while (number > 0) {
            int index = (int) (number % ALPHA_NUMERICS_LENGTH);
            numCharHolder[--placer] = ALPHA_NUMERICS_CHARS[index];
            number = number / ALPHA_NUMERICS_LENGTH;
        }

        // To keep the generated shortened url fixed length
        while (placer > 0) {
            // filler will be 'a',
            numCharHolder[--placer] = ALPHA_NUMERICS_CHARS[10];
        }

        String finalEncodedValue = new String(numCharHolder);
        logger.debug("Encoded completed for  {} , final value is {} ", number, finalEncodedValue);
        return finalEncodedValue;
    }

}
