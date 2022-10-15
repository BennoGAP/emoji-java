package com.vdurmont.emoji;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test that checks emoji json.
 * <p>
 *     Currently contains checks for:
 *     <ul>
 *         <li>Unicode emoji presents in json</li>
 *         <li>Right fitzpatric flag for emoji</li>
 *     </ul>
 *
 * <p>
 *     The test data is taken from: <a href="http://unicode.org/Public/emoji/4.0/emoji-test.txt">Unicode test data</a>
 *     related to unicode 9.0
 */

public class EmojiJsonTest {

    public static Collection<String> emojis() throws IOException {
        final InputStream is = EmojiJsonTest.class.getClassLoader().getResourceAsStream("emoji-test.txt");
        return EmojiTestDataReader.getEmojiList(is);
    }

    private static final int[] FITZPATRIC_CODEPOINTS = new int[]{
            EmojiTestDataReader.convertFromCodepoint("1F3FB"),
            EmojiTestDataReader.convertFromCodepoint("1F3FC"),
            EmojiTestDataReader.convertFromCodepoint("1F3FD"),
            EmojiTestDataReader.convertFromCodepoint("1F3FE"),
            EmojiTestDataReader.convertFromCodepoint("1F3FF")
    };


    // public String emoji;

    @ParameterizedTest
    @MethodSource("emojis")
    @DisplayName("checkEmojiExisting")
    public void checkEmojiExisting(String emoji) {
        var str = string2Unicode(emoji);
        System.out.println("[" + str + "]");

        assertTrue(EmojiManager.isEmoji(emoji), String.format("Asserting for emoji: %s", emoji));
    }

    @Test
    public void checkEmojiExisting() {
        System.out.println("\\u263a\\ufe0f");
    }

    @ParameterizedTest
    @MethodSource("emojis")
    public void checkEmojiFitzpatricFlag(String emoji) {
        final int len = emoji.toCharArray().length;
        boolean shouldContainFitzpatric = false;
        int codepoint;
        for (int i = 0; i < len; i++) {
            codepoint = emoji.codePointAt(i);
            shouldContainFitzpatric = Arrays.binarySearch(FITZPATRIC_CODEPOINTS, codepoint) >= 0;
            if (shouldContainFitzpatric) {
                break;
            }
        }

        if (shouldContainFitzpatric) {
            EmojiParser.parseFromUnicode(emoji, unicodeCandidate -> {
                if (unicodeCandidate.hasFitzpatrick()) {
                    assertTrue(unicodeCandidate.getEmoji().supportsFitzpatrick(), "Asserting emoji contains fitzpatric: " + emoji + " " + unicodeCandidate.getEmoji());
                }
                return "";
            });
        }
    }

    private static class EmojiTestDataReader {
        static List<String> getEmojiList(final InputStream emojiFileStream) throws IOException {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(emojiFileStream));
            final List<String> result = new LinkedList<>();

            String line = reader.readLine();
            String [] lineSplit;
            while (line != null) {
                if (!line.startsWith("#") && !line.startsWith(" ") && !line.startsWith("\n") &&
                        line.length() != 0) {
                    lineSplit = line.split(";");
                    result.add(convertToEmoji(lineSplit[0].trim()));
                }
                line = reader.readLine();
            }
            return result;
        }

        private static String convertToEmoji(final String input) {
            String[] emojiCodepoints = input.split(" ");
            StringBuilder sb = new StringBuilder();
            for (String emojiCodepoint : emojiCodepoints) {
                int codePoint = convertFromCodepoint(emojiCodepoint);
                sb.append(Character.toChars(codePoint));
            }
            return sb.toString();
        }

        static int convertFromCodepoint(String emojiCodepointAsString) {
            return Integer.parseInt(emojiCodepointAsString, 16);
        }

    }

    @ParameterizedTest
    @MethodSource("emojis")
    public void checkInverseParse(String emoji) {
        assertEquals(emoji, EmojiParser.parseToUnicode(EmojiParser.parseToHtmlDecimal(emoji, EmojiParser.FitzpatrickAction.IGNORE)));

        assertEquals(emoji, EmojiParser.parseToUnicode(EmojiParser.parseToHtmlHexadecimal(emoji, EmojiParser.FitzpatrickAction.IGNORE)));

        assertEquals(emoji, EmojiParser.parseToUnicode(EmojiParser.parseToAliases(emoji, EmojiParser.FitzpatrickAction.IGNORE)));
    }

    /**
     * 字符串转换unicode
     */
    public static String string2Unicode(String string) {
        StringBuilder unicode = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            // 取出每一个字符
            char c = string.charAt(i);
            if (c<0x20 || c>0x7E) {
                // 转换为unicode
                String tmp = Integer.toHexString(c);
                if (tmp.length() >= 4) {
                    unicode.append("\\u").append(Integer.toHexString(c));
                } else if (tmp.length() == 3){
                    unicode.append("\\u0").append(Integer.toHexString(c));
                } else if (tmp.length() == 2){
                    unicode.append("\\u00").append(Integer.toHexString(c));
                } else {
                    unicode.append("\\u000").append(Integer.toHexString(c));
                }
            } else {
                unicode.append(c);
            }
        }
        return unicode.toString();
    }

}
