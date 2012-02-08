/**
 * Copyright (c) 2009-2011, netBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code occasionally and without intent to use it, please report this
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
package com.netbout.db.helper;

import com.netbout.db.BoutRowMocker;
import com.netbout.db.IdentityRowMocker;
import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import com.rexsl.test.XhtmlConverter;
import com.rexsl.test.XhtmlMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * Test case of {@link StatsFarm}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class StatsFarmTest {

    /**
     * Farm to work with.
     */
    private final transient StatsFarm farm = new StatsFarm();

    /**
     * Find aliases of some identity.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testSummaryRendering() throws Exception {
        final Long bout = new BoutRowMocker().mock();
        final Identity identity =
            new IdentityMocker().namedAs(new IdentityRowMocker().mock()).mock();
        this.farm.init(identity);
        final String xml = this.farm.renderStageXml(
            bout, identity.name(), identity.name(), ""
        );
        MatcherAssert.assertThat(
            XhtmlConverter.the(xml),
            XhtmlMatchers.hasXPath("/data/text")
        );
    }

    /**
     * Render XSL.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testRenderingOfXslStylesheet() throws Exception {
        final Long bout = new BoutRowMocker().mock();
        final Identity identity =
            new IdentityMocker().namedAs(new IdentityRowMocker().mock()).mock();
        this.farm.init(identity);
        final String xsl = this.farm.renderStageXsl(bout, identity.name());
        MatcherAssert.assertThat(
            XhtmlConverter.the(xsl),
            XhtmlMatchers.hasXPath("/xsl:stylesheet")
        );
    }

}