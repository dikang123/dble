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
package com.actiontech.dble.net.handler;

import com.actiontech.dble.backend.mysql.MySQLMessage;
import com.actiontech.dble.config.ErrorCode;
import com.actiontech.dble.net.FrontendConnection;
import com.actiontech.dble.net.NIOHandler;
import com.actiontech.dble.net.mysql.MySQLPacket;
import com.actiontech.dble.server.NonBlockingSession;
import com.actiontech.dble.statistic.CommandCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * FrontendCommandHandler
 *
 * @author mycat
 */
public class FrontendCommandHandler implements NIOHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(NonBlockingSession.class);
    protected final ConcurrentLinkedQueue<byte[]> dataQueue = new ConcurrentLinkedQueue<>();
    protected final AtomicBoolean handleStatus;
    protected final FrontendConnection source;
    protected final CommandCount commands;

    public FrontendCommandHandler(FrontendConnection source) {
        this.source = source;
        this.commands = source.getProcessor().getCommands();
        this.handleStatus = new AtomicBoolean(false);
    }

    @Override
    public void handle(byte[] data) {
        if (source.getLoadDataInfileHandler() != null && source.getLoadDataInfileHandler().isStartLoadData()) {
            MySQLMessage mm = new MySQLMessage(data);
            int packetLength = mm.readUB3();
            if (packetLength + 4 == data.length) {
                source.loadDataInfileData(data);
            }
            return;
        }

        if (dataQueue.offer(data)) {
            handleQueue();
        } else {
            throw new RuntimeException("add data to queue error.");
        }
    }

    protected void handleData(byte[] data) {
        switch (data[4]) {
            case MySQLPacket.COM_INIT_DB:
                commands.doInitDB();
                source.initDB(data);
                break;
            case MySQLPacket.COM_QUERY:
                commands.doQuery();
                source.query(data);
                break;
            case MySQLPacket.COM_PING:
                commands.doPing();
                source.ping();
                break;
            case MySQLPacket.COM_QUIT:
                commands.doQuit();
                source.close("quit cmd");
                break;
            case MySQLPacket.COM_PROCESS_KILL:
                commands.doKill();
                source.kill(data);
                break;
            case MySQLPacket.COM_STMT_PREPARE:
                commands.doStmtPrepare();
                source.stmtPrepare(data);
                break;
            case MySQLPacket.COM_STMT_SEND_LONG_DATA:
                commands.doStmtSendLongData();
                source.stmtSendLongData(data);
                break;
            case MySQLPacket.COM_STMT_RESET:
                commands.doStmtReset();
                source.stmtReset(data);
                break;
            case MySQLPacket.COM_STMT_EXECUTE:
                commands.doStmtExecute();
                source.stmtExecute(data);
                break;
            case MySQLPacket.COM_STMT_CLOSE:
                commands.doStmtClose();
                source.stmtClose(data);
                break;
            case MySQLPacket.COM_HEARTBEAT:
                commands.doHeartbeat();
                source.heartbeat(data);
                break;
            default:
                commands.doOther();
                source.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unknown command");
        }
    }

    private void handleQueue() {
        if (this.handleStatus.compareAndSet(false, true)) {
            this.source.getProcessor().getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] data = null;
                        while ((data = dataQueue.poll()) != null) {
                            handleData(data);
                        }
                    } catch (Exception e) {
                        LOGGER.warn("maybe occur a bug,", e);
                        source.writeErrMessage(ErrorCode.ER_UNKNOWN_ERROR, e.toString());
                        dataQueue.clear();
                    } finally {
                        handleStatus.set(false);
                        if (dataQueue.size() > 0) {
                            handleQueue();
                        }
                    }
                }
            });
        }
    }
}
