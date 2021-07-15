package base.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static base.util.Util.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class UtilTest {

    @Test
    @DisplayName("stripLegacyFormattingCodes() correct")
    public void stripLegacyFormattingCodesCorrect() {
        assertThat(stripLegacyFormattingCodes(null), nullValue());
        assertThat(stripLegacyFormattingCodes(""), is(""));
        assertThat(stripLegacyFormattingCodes("   "), is("   "));
        assertThat(stripLegacyFormattingCodes("A"), is("A"));
        assertThat(stripLegacyFormattingCodes("AB"), is("AB"));
        assertThat(stripLegacyFormattingCodes("Hello, world!"), is("Hello, world!"));
        assertThat(stripLegacyFormattingCodes("§2Hello, world!"), is("Hello, world!"));
        assertThat(stripLegacyFormattingCodes("§2§lHello, world!"), is("Hello, world!"));
        assertThat(stripLegacyFormattingCodes("§2§lHello, §rworld!"), is("Hello, world!"));
        assertThat(stripLegacyFormattingCodes("§2§lHello, §rworld!§4"), is("Hello, world!"));
        assertThat(stripLegacyFormattingCodes("§2§lHello, §rworld!§4§"), is("Hello, world!§"));
    }

}
