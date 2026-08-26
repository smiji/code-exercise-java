package com.tpx.urlshort.service.alias;

import com.tpx.urlshort.exception.IllegalParametersException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Base62EncoderTest {

    @Test
    void testEncode() {
        Base62Encoder base62Encoder = new Base62Encoder();
        String encodeFirst = base62Encoder.encode(123);
        String expectedValue = "aaaaa1Z";
        Assertions.assertEquals(expectedValue, encodeFirst);

        //This is expected to be deterministic
        String encodeSecond = base62Encoder.encode(123);
        Assertions.assertEquals(encodeFirst, encodeSecond);

        //its length must be 7
        Assertions.assertEquals(7, encodeFirst.length());
        Assertions.assertEquals(7, encodeSecond.length());
    }

    @Test
    void testEncodeWhenNumber_negative() {
        Base62Encoder base62Encoder = new Base62Encoder();
        String expectedError = "Invalid input too less to consider -123";
        IllegalParametersException illegalParametersException = Assertions.assertThrows(IllegalParametersException.class, () -> base62Encoder.encode(-123));
        Assertions.assertEquals(expectedError, illegalParametersException.getMessage());
    }

    @Test
    void testEncode_more_than_upper_threshold() {
        Base62Encoder base62Encoder = new Base62Encoder();
        long input = 3521614606208L;
        String expectedError = "Value 3521614606208 exceeds maximum capacity for 7 chars ( 3521614606207 )";
        IllegalParametersException illegalParametersException = Assertions.assertThrows(IllegalParametersException.class, () -> base62Encoder.encode(input));
        Assertions.assertEquals(expectedError, illegalParametersException.getMessage());
    }
}