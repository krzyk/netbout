/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code accidentally and without intent to use it, please report this
 * incident to the author by email.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.netbout.hub;

import com.jcabi.urn.URN;
import com.jcabi.urn.URNMocker;
import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case of {@link DefaultURNResolver}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class DefaultURNResolverTest {

    /**
     * DefaultURNResolver can load namespaces from Hub on start.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void loadsNamesspacesFromHub() throws Exception {
        final String namespace = "alpha";
        final URL url = new URL("http://localhost/abc");
        final List<String> names = new ArrayList<String>();
        names.add(namespace);
        final Hub hub = new HubMocker()
            // @checkstyle MultipleStringLiterals (3 lines)
            .doReturn(names, "get-all-namespaces")
            .doReturn(url.toString(), "get-namespace-template")
            .doReturn(new URNMocker().mock(), "get-namespace-owner")
            .mock();
        final URNResolver resolver = new DefaultURNResolver(hub);
        MatcherAssert.assertThat(
            resolver.authority(new URN(namespace, "nss")),
            Matchers.equalTo(url)
        );
    }

    /**
     * DefaultURNResolver can register namespaces for a given user.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void registersNamespacesForIdentity() throws Exception {
        final String namespace = "beta";
        final String url = "http://localhost/beta";
        final Hub hub = new HubMocker().mock();
        final Identity identity = new IdentityMocker().mock();
        final URNResolver resolver = new DefaultURNResolver(hub);
        resolver.register(identity, namespace, url);
        resolver.register(new IdentityMocker().mock(), "x", "http://x");
        MatcherAssert.assertThat(
            resolver.registered(identity).size(),
            Matchers.equalTo(1)
        );
    }

    /**
     * DefaultURNResolver can load multiple namespaces from Hub on start.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void loadsMultipleNamesspacesFromHub() throws Exception {
        final List<String> names = new ArrayList<String>();
        // @checkstyle MagicNumber (2 lines)
        for (int num = 0; num < 5; num += 1) {
            names.add(RandomStringUtils.randomAlphabetic(10).toLowerCase());
        }
        final String url = "http://localhost/some-path";
        final Hub hub = new HubMocker()
            // @checkstyle MultipleStringLiterals (3 lines)
            .doReturn(names, "get-all-namespaces")
            .doReturn(url, "get-namespace-template")
            .doReturn(new URNMocker().mock(), "get-namespace-owner")
            .mock();
        final URNResolver resolver = new DefaultURNResolver(hub);
        for (String namespace : names) {
            MatcherAssert.assertThat(
                resolver.authority(new URN(namespace, "")).toString(),
                Matchers.equalTo(url)
            );
        }
    }

    /**
     * DefaultURNResolver can throw exception if namespace is absent.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = Identity.UnreachableURNException.class)
    public void thowsOnAbsentNamespace() throws Exception {
        final Hub hub = new HubMocker().mock();
        new DefaultURNResolver(hub).authority(new URN("absent", ""));
    }

    /**
     * DefaultURNResolver can load namespaces lazy, not during construction.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void lazyLoadsNamesspaces() throws Exception {
        final HubMocker hmocker = new HubMocker();
        final URNResolver resolver = new DefaultURNResolver(hmocker.mock());
        final String namespace = "lazy";
        try {
            resolver.authority(new URN(namespace, ""));
            throw new AssertionError("we shouldn't reach this point");
        } catch (Identity.UnreachableURNException ex) {
            MatcherAssert.assertThat(
                ex.getMessage(),
                Matchers.containsString(namespace)
            );
        }
        final URL url = new URL("http://localhost/lazy");
        final List<String> names = new ArrayList<String>();
        names.add(namespace);
        hmocker
            // @checkstyle MultipleStringLiterals (3 lines)
            .doReturn(names, "get-all-namespaces")
            .doReturn(url.toString(), "get-namespace-template")
            .doReturn(new URNMocker().mock(), "get-namespace-owner");
        MatcherAssert.assertThat(
            resolver.authority(new URN(namespace, "")),
            Matchers.equalTo(url)
        );
        resolver.authority(new URN(namespace, ""));
        resolver.authority(new URN(namespace, "test"));
        // @checkstyle MultipleStringLiterals (1 line)
        Mockito.verify(hmocker.mock(), Mockito.times(2))
            .make("get-all-namespaces");
    }

}