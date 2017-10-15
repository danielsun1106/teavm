/*
 *  Copyright 2017 Alexey Andreev.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.teavm.classlib.java.util;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public final class TFormatter implements Closeable, Flushable {
    private Locale locale;
    private Appendable out;
    private IOException ioException;
    private boolean closed;

    public TFormatter() {
        this(Locale.getDefault());
    }

    public TFormatter(Appendable a) {
        this(a, Locale.getDefault());
    }

    public TFormatter(Locale l) {
        this(new StringBuilder(), l);
    }

    public TFormatter(Appendable a, Locale l) {
        out = a;
        locale = l;
    }

    public TFormatter(PrintStream ps) {
        this(new OutputStreamWriter(ps));
    }

    public TFormatter(OutputStream os) {
        this(new OutputStreamWriter(os));
    }

    public TFormatter(OutputStream os, String csn) throws UnsupportedEncodingException {
        this(new OutputStreamWriter(os, csn));
    }

    public TFormatter(OutputStream os, String csn, Locale l) throws UnsupportedEncodingException {
        this(new OutputStreamWriter(os, csn), l);
    }

    public Locale locale() {
        requireOpen();
        return locale;
    }

    public Appendable out() {
        requireOpen();
        return out;
    }

    private void requireOpen() {
        if (out == null) {
            throw new TFormatterClosedException();
        }
    }

    @Override
    public String toString() {
        requireOpen();
        return out.toString();
    }

    @Override
    public void flush() {
        requireOpen();
        if (out instanceof Flushable) {
            try {
                ((Flushable) out).flush();
            } catch (IOException e) {
                ioException = e;
            }
        }
    }

    @Override
    public void close() {
        requireOpen();
        try {
            if (out instanceof Closeable) {
                ((Closeable) out).close();
            }
        } catch (IOException e) {
            ioException = e;
        } finally {
            out = null;
        }
    }

    public IOException ioException() {
        return ioException;
    }

    public TFormatter format(String format, Object... args) {
        return format(locale, format, args);
    }

    public TFormatter format(Locale l, String format, Object... args) {
        requireOpen();
        try {
            new FormatWriter(out, l, format, args).write();
        } catch (IOException e) {
            ioException = e;
        }
        return this;
    }

    static class FormatWriter {
        Appendable out;
        Locale locale;
        String format;
        Object[] args;
        int index;
        int currentArgumentIndex;

        FormatWriter(Appendable out, Locale locale, String format, Object[] args) {
            this.out = out;
            this.locale = locale;
            this.format = format;
            this.args = args;
        }

        void write() throws IOException {
            while (true) {
                int next = format.indexOf('%', index);
                if (next < 0) {
                    out.append(format.substring(index));
                    break;
                }
                out.append(format.substring(index, next));
                index = next + 1;
                parseFormatSpecifier();
            }
        }

        private void parseFormatSpecifier() throws IOException {
            char c = format.charAt(index);
            if (isDigit(c)) {

            }
        }

        private static boolean isDigit(char c) {
            return c >= '0' && c <= '9';
        }
    }
}
