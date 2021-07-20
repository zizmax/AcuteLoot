package base.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;

import static base.util.Util.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    @DisplayName("substituteVariables() correct")
    public void substituteVariablesCorrect() {
        assertThrows(NullPointerException.class, () -> substituteVariables(null, null));
        assertThrows(NullPointerException.class, () -> substituteVariables("", null));
        assertThrows(NullPointerException.class, () -> substituteVariables(null, new HashMap<>()));

        final LinkedHashMap<String, String> empty = new LinkedHashMap<String, String>(){{
            put("0", "");
        }};
        assertThat(substituteVariables("", new HashMap<>()), is(empty));

        final LinkedHashMap<String, String> noVars = new LinkedHashMap<String, String>(){{
            put("0", "Hello, world! How are you today?");
        }};
        assertThat(substituteVariables("Hello, world! How are you today?", new HashMap<>()), is(noVars));

        final LinkedHashMap<String, String> oneVar = new LinkedHashMap<String, String>(){{
            put("0", "Hello, ");
            put("[target]", "world");
            put("2", "! How are you today?");
        }};
        final HashMap<String, String> oneVarMap = new HashMap<String, String>() {{
            put("[target]", "world");
        }};
        assertThat(substituteVariables("Hello, [target]! How are you today?", oneVarMap), is(oneVar));

        final LinkedHashMap<String, String> manyVars = new LinkedHashMap<String, String>(){{
            put("[title]", "[MOD]");
            put("[killed]", "zizmax");
            put("2", " was slain by ");
            put("[killer]", "dizigma");
            put("4", " using ");
            put("[item]", "Sorcerous Blade of Desires");
        }};
        final HashMap<String, String> manyVarsMap = new HashMap<String, String>() {{
            put("[title]", "[MOD]");
            put("[killed]", "zizmax");
            put("[killer]", "dizigma");
            put("[item]", "Sorcerous Blade of Desires");
        }};
        assertThat(substituteVariables("[title][killed] was slain by [killer] using [item]", manyVarsMap), is(manyVars));
    }

}
