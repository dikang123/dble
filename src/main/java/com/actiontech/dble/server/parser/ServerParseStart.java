/*
 * Copyright (c) 2013, OpenCloudDB/MyCAT and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software;Designed and Developed mainly by many Chinese
 * opensource volunteers. you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 2 only, as published by the
 * Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Any questions about this component can be directed to it's project Web address
 * https://code.google.com/p/opencloudb/.
 *
 */
package com.actiontech.dble.server.parser;

import com.actiontech.dble.route.parser.util.ParseUtil;

/**
 * @author mycat
 */
public final class ServerParseStart {
    private ServerParseStart() {
    }

    public static final int OTHER = -1;
    public static final int TRANSACTION = 1;
    public static final int READCHARCS = 2;

    public static int parse(String stmt, int offset) {
        int i = offset;
        for (; i < stmt.length(); i++) {
            switch (stmt.charAt(i)) {
                case ' ':
                    continue;
                case '/':
                case '#':
                    i = ParseUtil.comment(stmt, i);
                    continue;
                case 'T':
                case 't':
                    return transactionCheck(stmt, i);
                default:
                    return OTHER;
            }
        }
        return OTHER;
    }

    // START TRANSACTION
    /*
    static int transactionCheck(String stmt, int offset) {
        if (stmt.length() > offset + "ransaction".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            char c7 = stmt.charAt(++offset);
            char c8 = stmt.charAt(++offset);
            char c9 = stmt.charAt(++offset);
            char c10 = stmt.charAt(++offset);
            if ((c1 == 'R' || c1 == 'r') && (c2 == 'A' || c2 == 'a') && (c3 == 'N' || c3 == 'n')
        && (c4 == 'S' || c4 == 's') && (c5 == 'A' || c5 == 'a') && (c6 == 'C' || c6 == 'c')
        && (c7 == 'T' || c7 == 't') && (c8 == 'I' || c8 == 'i') && (c9 == 'O' || c9 == 'o')
        && (c10 == 'N' || c10 == 'n')
        && (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
                return TRANSACTION;
            }
        }
        return OTHER;
    }
    */
    // transaction characteristic check
    private static int transactionCheck(String stmt, int offset) {
        int tmpOff;
        tmpOff = skipTrans(stmt, offset);
        if (tmpOff < 0) {
            return OTHER;
        }

        if (stmt.length() == ++tmpOff) {
            return TRANSACTION;
        } else {
            return readCharcsCheck(stmt, tmpOff);
        }
    }

    private static int skipTrans(String stmt, int offset) {
        if (stmt.length() > offset + "ransaction".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            char c7 = stmt.charAt(++offset);
            char c8 = stmt.charAt(++offset);
            char c9 = stmt.charAt(++offset);
            char c10 = stmt.charAt(++offset);
            if ((c1 == 'R' || c1 == 'r') && (c2 == 'A' || c2 == 'a') && (c3 == 'N' || c3 == 'n') &&
                    (c4 == 'S' || c4 == 's') && (c5 == 'A' || c5 == 'a') && (c6 == 'C' || c6 == 'c') &&
                    (c7 == 'T' || c7 == 't') && (c8 == 'I' || c8 == 'i') && (c9 == 'O' || c9 == 'o') &&
                    (c10 == 'N' || c10 == 'n')) {
                return offset;
            }
        }

        return -1;
    }

    static int readCharcsCheck(String stmt, int offset) {
        do {
            char c = stmt.charAt(offset);
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n')
                continue;
            break;
        } while (stmt.length() > ++offset);

        if (stmt.length() == offset) {
            return TRANSACTION;
        } else if (stmt.length() > offset + "ead ".length()) {
            char c0 = stmt.charAt(offset);
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            if ((c0 == 'R' || c0 == 'r') && (c1 == 'E' || c1 == 'e') && (c2 == 'A' || c2 == 'a') &&
                    (c3 == 'D' || c3 == 'd') && (c4 == ' ' || c4 == '\t' || c4 == '\r' || c4 == '\n')) {
                return READCHARCS;
            }
        }

        return OTHER;
    }
}
