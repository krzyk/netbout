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
package com.netbout.rest;

import com.netbout.spi.Entry;
import com.netbout.spi.Identity;
import com.netbout.spi.User;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link Cryptor}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class CryptorTest {

    /**
     * Encryption + decryption.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testEncryptionDecryption() throws Exception {
        final String uname = "Alex Doe";
        final String iname = "\u041F\u0435\u0442\u0440 I";
        final Identity identity = Mockito.mock(Identity.class);
        Mockito.doReturn(iname).when(identity).name();
        final User user = Mockito.mock(User.class);
        Mockito.doReturn(user).when(identity).user();
        Mockito.doReturn(uname).when(user).name();
        final Entry entry = Mockito.mock(Entry.class);
        Mockito.doReturn(user).when(entry).user(uname);
        Mockito.doReturn(identity).when(user).identity(iname);
        final String hash = new Cryptor(entry).encrypt(identity);
        MatcherAssert.assertThat(
            hash.matches("[\\w=\\+\\./]+"),
            Matchers.describedAs(hash, Matchers.is(true))
        );
        final Identity discovered = new Cryptor(entry).decrypt(hash);
        MatcherAssert.assertThat(discovered, Matchers.equalTo(identity));
    }

}