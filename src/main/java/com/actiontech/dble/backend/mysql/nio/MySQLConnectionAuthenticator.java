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
package com.actiontech.dble.backend.mysql.nio;

import com.actiontech.dble.DbleServer;
import com.actiontech.dble.backend.mysql.CharsetUtil;
import com.actiontech.dble.backend.mysql.SecurityUtil;
import com.actiontech.dble.backend.mysql.nio.handler.ResponseHandler;
import com.actiontech.dble.config.Capabilities;
import com.actiontech.dble.net.ConnectionException;
import com.actiontech.dble.net.NIOHandler;
import com.actiontech.dble.net.mysql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MySQLConnectionAuthenticator
 *
 * @author mycat
 */
public class MySQLConnectionAuthenticator implements NIOHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MySQLConnectionAuthenticator.class);
    private final MySQLConnection source;
    private final ResponseHandler listener;

    public MySQLConnectionAuthenticator(MySQLConnection source,
                                        ResponseHandler listener) {
        this.source = source;
        this.listener = listener;
    }

    public void connectionError(MySQLConnection c, Throwable e) {
        if (listener != null) {
            listener.connectionError(e, c);
        }
    }

    @Override
    public void handle(byte[] data) {
        try {
            switch (data[4]) {
                case OkPacket.FIELD_COUNT:
                    HandshakePacket packet = source.getHandshake();
                    if (packet == null) {
                        processHandShakePacket(data);
                        // send auth packet
                        source.authenticate();
                        break;
                    }
                    // execute auth response
                    source.setHandler(new MySQLConnectionHandler(source));
                    source.setAuthenticated(true);
                    boolean clientCompress = Capabilities.CLIENT_COMPRESS == (Capabilities.CLIENT_COMPRESS & packet.getServerCapabilities());
                    boolean usingCompress = DbleServer.getInstance().getConfig().getSystem().getUseCompression() == 1;
                    if (clientCompress && usingCompress) {
                        source.setSupportCompress(true);
                    }
                    if (listener != null) {
                        listener.connectionAcquired(source);
                    }
                    break;
                case ErrorPacket.FIELD_COUNT:
                    ErrorPacket err = new ErrorPacket();
                    err.read(data);
                    String errMsg = new String(err.getMessage());
                    LOGGER.warn("can't connect to mysql server ,errmsg:" + errMsg + " " + source);
                    //source.close(errMsg);
                    throw new ConnectionException(err.getErrno(), errMsg);

                case EOFPacket.FIELD_COUNT:
                    auth323(data[3]);
                    break;
                default:
                    packet = source.getHandshake();
                    if (packet == null) {
                        processHandShakePacket(data);
                        // send auth packet
                        source.authenticate();
                        break;
                    } else {
                        throw new RuntimeException("Unknown Packet!");
                    }

            }

        } catch (RuntimeException e) {
            if (listener != null) {
                listener.connectionError(e, source);
                return;
            }
            throw e;
        }
    }

    private void processHandShakePacket(byte[] data) {
        HandshakePacket packet = new HandshakePacket();
        packet.read(data);
        source.setHandshake(packet);
        source.setThreadId(packet.getThreadId());

        int charsetIndex = (packet.getServerCharsetIndex() & 0xff);
        String charset = CharsetUtil.getCharset(charsetIndex);
        if (charset != null) {
            source.setCharset(charset);
        } else {
            throw new RuntimeException("Unknown charsetIndex:" + charsetIndex);
        }
    }

    private void auth323(byte packetId) {
        // send 323 auth packet
        Reply323Packet r323 = new Reply323Packet();
        r323.setPacketId(++packetId);
        String pass = source.getPassword();
        if (pass != null && pass.length() > 0) {
            byte[] seed = source.getHandshake().getSeed();
            r323.setSeed(SecurityUtil.scramble323(pass, new String(seed)).getBytes());
        }
        r323.write(source);
    }

}
