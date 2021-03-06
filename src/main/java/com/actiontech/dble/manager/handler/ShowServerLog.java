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
package com.actiontech.dble.manager.handler;

import com.actiontech.dble.backend.mysql.PacketUtil;
import com.actiontech.dble.config.ErrorCode;
import com.actiontech.dble.config.Fields;
import com.actiontech.dble.config.model.SystemConfig;
import com.actiontech.dble.manager.ManagerConnection;
import com.actiontech.dble.net.mysql.EOFPacket;
import com.actiontech.dble.net.mysql.FieldPacket;
import com.actiontech.dble.net.mysql.ResultSetHeaderPacket;
import com.actiontech.dble.net.mysql.RowDataPacket;
import com.actiontech.dble.route.parser.ManagerParseShow;
import com.actiontech.dble.util.CircularArrayList;
import com.actiontech.dble.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class ShowServerLog {
    private ShowServerLog() {
    }

    private static final int FIELD_COUNT = 1;
    private static final ResultSetHeaderPacket HEADER = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] FIELDS = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket EOF = new EOFPacket();
    public static final String DEFAULT_LOGFILE = "dble.log";
    private static final Logger LOGGER = LoggerFactory.getLogger(ShowServerLog.class);

    static {
        int i = 0;
        byte packetId = 0;
        HEADER.setPacketId(++packetId);

        FIELDS[i] = PacketUtil.getField("LOG", Fields.FIELD_TYPE_VAR_STRING);
        FIELDS[i].setPacketId(++packetId);

        EOF.setPacketId(++packetId);
    }

    private static File getLogFile(String logFile) {

        String daasHome = SystemConfig.getHomePath();
        File file = new File(daasHome, "logs" + File.separator + logFile);
        return file;
    }

    public static void handle(String stmt, ManagerConnection c) {

        Map<String, String> condPairMap = getCondPair(stmt);
        if (condPairMap == null) {
            c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
            return;
        }

        ByteBuffer buffer = c.allocate();

        // write header
        buffer = HEADER.write(buffer, c, true);

        // write fields
        for (FieldPacket field : FIELDS) {
            buffer = field.write(buffer, c, true);
        }

        // write eof
        buffer = EOF.write(buffer, c, true);

        // write rows

        byte packetId = EOF.getPacketId();
        PackageBufINf bufInf = null;

        if (condPairMap.isEmpty()) {
            bufInf = showLogSum(c, buffer, packetId);
        } else {
            String logFile = condPairMap.get("file");
            if (logFile == null) {
                logFile = DEFAULT_LOGFILE;
            }
            String limitStr = condPairMap.get("limit");
            limitStr = (limitStr != null) ? limitStr : "0," + 100000;
            String[] limtArry = limitStr.split("\\s|,");
            int start = Integer.parseInt(limtArry[0]);
            int page = Integer.parseInt(limtArry[1]);
            int end = start + page;
            String key = condPairMap.get("key");
            String regex = condPairMap.get("regex");
            bufInf = showLogRange(c, buffer, packetId, key, regex, start, end,
                    logFile);

        }

        packetId = bufInf.getPacketId();
        buffer = bufInf.getBuffer();

        EOFPacket lastEof = new EOFPacket();
        lastEof.setPacketId(++packetId);
        buffer = lastEof.write(buffer, c, true);

        // write buffer
        c.write(buffer);
    }

    public static PackageBufINf showLogRange(ManagerConnection c,
                                             ByteBuffer buffer, byte packetId, String key, String regex,
                                             int start, int end, String logFile) {
        PackageBufINf bufINf = new PackageBufINf();
        Pattern pattern = null;
        if (regex != null) {
            pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        }
        if (key != null) {
            key = key.toLowerCase();
        }
        File file = getLogFile(logFile);
        BufferedReader br = null;
        int curLine = 0;
        try {
            br = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = br.readLine()) != null) {
                curLine++;
                if (curLine >= start && curLine <= end && (
                        (pattern != null && pattern.matcher(line).find()) ||
                                (pattern == null && key == null) ||
                                (key != null && line.toLowerCase().contains(key)))) {
                    RowDataPacket row = new RowDataPacket(FIELD_COUNT);
                    row.add(StringUtil.encode(curLine + "->" + line,
                            c.getCharset()));
                    row.setPacketId(++packetId);
                    buffer = row.write(buffer, c, true);
                }
            }
            bufINf.setBuffer(buffer);
            bufINf.setPacketId(packetId);
            return bufINf;

        } catch (Exception e) {
            LOGGER.error("showLogRangeError", e);
            RowDataPacket row = new RowDataPacket(FIELD_COUNT);
            row.add(StringUtil.encode(e.toString(), c.getCharset()));
            row.setPacketId(++packetId);
            buffer = row.write(buffer, c, true);
            bufINf.setBuffer(buffer);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    LOGGER.error("showLogRangeError", e);
                }
            }

        }
        bufINf.setPacketId(packetId);
        return bufINf;
    }

    private static PackageBufINf showLogSum(ManagerConnection c,
                                            ByteBuffer buffer, byte packetId) {
        PackageBufINf bufINf = new PackageBufINf();
        File[] logFiles = new File(SystemConfig.getHomePath(), "logs").listFiles();
        StringBuilder fileNames = new StringBuilder();
        if (logFiles != null) {
            for (File f : logFiles) {
                if (f.isFile()) {
                    fileNames.append("  ");
                    fileNames.append(f.getName());
                }
            }
        }

        File file = getLogFile(DEFAULT_LOGFILE);
        BufferedReader br = null;
        int totalLines = 0;
        CircularArrayList<String> queue = new CircularArrayList<>(50);
        try {
            br = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = br.readLine()) != null) {
                totalLines++;
                if (queue.size() == queue.capacity()) {
                    queue.remove(0);
                }
                queue.add(line);

            }

            RowDataPacket row = new RowDataPacket(FIELD_COUNT);
            row.add(StringUtil.encode("files in log dir:" + totalLines + fileNames, c.getCharset()));
            row.setPacketId(++packetId);
            buffer = row.write(buffer, c, true);
            row = new RowDataPacket(FIELD_COUNT);
            row.add(StringUtil.encode("Total lines " + totalLines + " ,tail " +
                    queue.size() + " line is following:", c.getCharset()));
            row.setPacketId(++packetId);
            buffer = row.write(buffer, c, true);
            int size = queue.size() - 1;
            for (int i = size; i >= 0; i--) {
                String data = queue.get(i);
                row = new RowDataPacket(FIELD_COUNT);
                row.add(StringUtil.encode(data, c.getCharset()));
                row.setPacketId(++packetId);
                buffer = row.write(buffer, c, true);
            }
            bufINf.setBuffer(buffer);
            bufINf.setPacketId(packetId);
            return bufINf;

        } catch (Exception e) {
            LOGGER.error("showLogSumError", e);
            RowDataPacket row = new RowDataPacket(FIELD_COUNT);
            row.add(StringUtil.encode(e.toString(), c.getCharset()));
            row.setPacketId(++packetId);
            buffer = row.write(buffer, c, true);
            bufINf.setBuffer(buffer);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    LOGGER.error("showLogSumError", e);
                }
            }

        }
        bufINf.setPacketId(packetId);
        return bufINf;
    }


    /**
     * @param sql
     * @return
     */
    public static Map<String, String> getCondPair(String sql) {
        try {
            HashMap<String, String> map = new HashMap<>();
            int offset = ManagerParseShow.trim(0, sql);
            //filter whitespace of sql
            char c1 = sql.charAt(offset);
            char c2 = sql.charAt(++offset);
            char c3 = sql.charAt(++offset);
            if ((c1 == 'L' || c1 == 'l') &&
                    (c2 == 'o' || c2 == 'O') &&
                    (c3 == 'g' || c3 == 'G')) {
                offset = ManagerParseShow.trim(++offset, sql);
                char c4 = sql.charAt(offset);
                char c5 = sql.charAt(++offset);
                offset++;
                if (c4 == c5 && c5 == '@') {
                    offset = ManagerParseShow.trim(offset, sql);
                    while (offset < sql.length()) {
                        switch (sql.charAt(offset)) {
                            case 'f':
                            case 'F':
                                offset = checkFcond(offset, sql, map);
                                break;
                            case 'l':
                            case 'L':
                                offset = checkLcond(offset, sql, map);
                                break;
                            case 'k':
                            case 'K':
                                offset = checkKcond(offset, sql, map);
                                break;
                            case 'R':
                            case 'r':
                                offset = checkRcond(offset, sql, map);
                                break;
                            default:
                                return null;
                        }
                        offset = ManagerParseShow.trim(offset, sql);
                    }
                }
            }
            return map;
        } catch (Exception e) {
            //ignore error
        }
        return null;
    }


    public static int checkFcond(int offset, String sql, Map<String, String> map) throws Exception {
        if (map.get("file") != null) {
            throw new Exception();
        }
        char c1 = sql.charAt(++offset);
        char c2 = sql.charAt(++offset);
        char c3 = sql.charAt(++offset);
        if ((c1 == 'I' | c1 == 'i') && (c2 == 'l' || c2 == 'L') && (c3 == 'e' || c3 == 'E')) {
            offset = ManagerParseShow.trim(++offset, sql);
            if (sql.charAt(offset) == '=') {
                offset = ManagerParseShow.trim(++offset, sql);
                int start = offset;
                for (; offset < sql.length(); offset++) {
                    if (sql.charAt(offset) == ' ') {
                        break;
                    }
                }
                map.put("file", sql.substring(start, offset));
                return offset;
            }
        }
        throw new Exception();
    }

    public static int checkLcond(int offset, String sql, Map<String, String> map) throws Exception {
        if (map.get("limit") != null) {
            throw new Exception();
        }
        char c1 = sql.charAt(++offset);
        char c2 = sql.charAt(++offset);
        char c3 = sql.charAt(++offset);
        char c4 = sql.charAt(++offset);
        if ((c1 == 'I' | c1 == 'i') && (c2 == 'm' || c2 == 'M') && (c3 == 'i' || c3 == 'I') && (c4 == 'T' || c4 == 't')) {
            offset = ManagerParseShow.trim(++offset, sql);
            if (sql.charAt(offset) == '=') {
                offset = ManagerParseShow.trim(++offset, sql);
                int start = offset;
                for (; offset < sql.length(); offset++) {
                    if (sql.charAt(offset) == ' ') {
                        break;
                    }
                }
                map.put("limit", sql.substring(start, offset));
                return offset;
            }
        }
        throw new Exception();
    }

    public static int checkKcond(int offset, String sql, Map<String, String> map) throws Exception {
        boolean quotationMarks = false;
        int start = 0;
        if (map.get("key") != null) {
            throw new Exception();
        }
        char c1 = sql.charAt(++offset);
        char c2 = sql.charAt(++offset);
        if ((c1 == 'E' | c1 == 'e') && (c2 == 'Y' || c2 == 'y')) {
            offset = ManagerParseShow.trim(++offset, sql);
            if (sql.charAt(offset) == '=') {
                offset = ManagerParseShow.trim(++offset, sql);
                if (sql.charAt(offset++) == '\'') {
                    quotationMarks = true;
                }
                if (quotationMarks) {
                    start = offset;
                    for (; offset < sql.length(); offset++) {
                        if (sql.charAt(offset) == '\'') {
                            break;
                        }
                    }
                } else {
                    start = offset - 1;
                    for (; offset < sql.length(); offset++) {
                        if (sql.charAt(offset) == ' ') {
                            break;
                        }
                    }
                }
                map.put("key", sql.substring(start, offset));
                return ++offset;
            }
        }
        throw new Exception();
    }

    public static int checkRcond(int offset, String sql, Map<String, String> map) throws Exception {
        if (map.get("regex") != null) {
            throw new Exception();
        }
        char c1 = sql.charAt(++offset);
        char c2 = sql.charAt(++offset);
        char c3 = sql.charAt(++offset);
        char c4 = sql.charAt(++offset);
        if ((c1 == 'E' | c1 == 'e') && (c2 == 'g' || c2 == 'G') && (c3 == 'e' || c3 == 'E') && (c4 == 'X' || c4 == 'x')) {
            offset = ManagerParseShow.trim(++offset, sql);
            if (sql.charAt(offset) == '=') {
                offset = ManagerParseShow.trim(++offset, sql);
                int start = offset;
                for (; offset < sql.length(); offset++) {
                    if (sql.charAt(offset) == ' ') {
                        break;
                    }
                }
                map.put("regex", sql.substring(start, offset));
                return offset;
            }
        }
        throw new Exception();
    }
}

