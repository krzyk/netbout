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
package com.netbout.rest.meta;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Html PAR.
 *
 * <p>The class is mutable and NOT thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class HtmlPar extends AbstractPar {

    /**
     * Public ctor.
     */
    @SuppressWarnings("unchecked")
    public HtmlPar() {
        super(
            ArrayUtils.toMap(
                new Object[][] {
                    // @checkstyle MultipleStringLiterals (5 lines)
                    {"\\[(.+?)\\]\\((http://.+?)\\)", "<a href='$2'>$1</a>"},
                    {"\\*+(.+?)\\*+", "<b>$1</b>"},
                    {"`(.+?)`", "<span class='tt'>$1</span>"},
                    {"_+(.+?)_+", "<i>$1</i>"},
                }
        ));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String pack() {
        String out;
        if (this.isPre()) {
            out = String.format(
                "<p class='fixed'>%s</p>",
                this.getText()
            );
        } else if (this.isBullets()) {
            out = String.format(
                "<ul><li>%s</li></ul>",
                StringUtils.join(this.getText().split("\n"), "</li><li>")
            );
        } else {
            out = String.format("<p>%s</p>", this.getText());
        }
        return out;
    }

}
