package com.vdurmont.emoji;


import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;


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


    public String emoji;

    @Test
    public void checkEmojiExisting() {
        assertTrue(EmojiManager.isEmoji(emoji), "Asserting for emoji: " + emoji);
    }

    @Test
    public void checkEmojiFitzpatricFlag() {
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

    @Test
    public void checkInverseParse() {
        assertEquals(emoji, EmojiParser.parseToUnicode(EmojiParser.parseToHtmlDecimal(emoji, EmojiParser.FitzpatrickAction.IGNORE)));

        assertEquals(emoji, EmojiParser.parseToUnicode(EmojiParser.parseToHtmlHexadecimal(emoji, EmojiParser.FitzpatrickAction.IGNORE)));

        assertEquals(emoji, EmojiParser.parseToUnicode(EmojiParser.parseToAliases(emoji, EmojiParser.FitzpatrickAction.IGNORE)));
    }
}
