/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi.cpa;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.CharEncoding;

/**
 * Usefull instruments for CPA helpers.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class CpaUtils {

    /**
     * It's a utility class.
     */
    private CpaUtils() {
        // empty
    }

    /**
     * Convert HTTP body to the map of arguments.
     * @param body The body
     * @return Map of arguments found there
     */
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    public static Map<String, String> decodeBody(final String body) {
        final Map<String, String> args = new HashMap<String, String>();
        for (String pair : body.split("&")) {
            final String[] parts = pair.split("=", 2);
            String value;
            if (parts.length == 1) {
                value = "";
            } else {
                value = CpaUtils.decode(parts[1]);
            }
            args.put(CpaUtils.decode(parts[0]), value);
        }
        return args;
    }

    /**
     * URL decode simple text.
     * @param text The text to decode
     * @return Decoded
     */
    private static String decode(final String text) {
        try {
            return URLDecoder.decode(text, CharEncoding.UTF_8);
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
