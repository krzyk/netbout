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
package com.netbout.spi.client;

import com.netbout.spi.Profile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The profile of identity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class RestProfile implements Profile {

    /**
     * Rest client.
     */
    private final transient RestClient client;

    /**
     * Public ctor.
     * @param clnt Rest client
     */
    public RestProfile(final RestClient clnt) {
        this.client = clnt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Locale locale() {
        final String lang = this.client
            .get("reading locale of identity")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath("/page/identity/locale")
            .xpath("/page/identity/locale/text()")
            .get(0);
        return new Locale(lang);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLocale(final Locale locale) {
        throw new UnsupportedOperationException(
            "Profile#setLocale() is not implemented yet"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL photo() {
        final String href = this.client
            .get("reading photo of identity")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath("/page/identity/photo[.!='']")
            .xpath("/page/identity/photo/text()")
            .get(0);
        try {
            return new URL(href);
        } catch (java.net.MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPhoto(final URL photo) {
        throw new UnsupportedOperationException(
            "Profile#setPhoto() is not implemented yet"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> aliases() {
        final List<String> names = this.client
            .get("reading aliases of identity")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath("/page/identity/aliases")
            .xpath("/page/identity/aliases/alias/text()");
        return Collections.unmodifiableSet(new HashSet<String>(names));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void alias(final String alias) {
        throw new UnsupportedOperationException(
            "Profile#alias() is not implemented yet"
        );
    }

}
