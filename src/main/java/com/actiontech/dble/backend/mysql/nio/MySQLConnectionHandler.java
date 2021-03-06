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
import com.actiontech.dble.backend.mysql.ByteUtil;
import com.actiontech.dble.backend.mysql.nio.handler.LoadDataResponseHandler;
import com.actiontech.dble.backend.mysql.nio.handler.ResponseHandler;
import com.actiontech.dble.net.handler.BackendAsyncHandler;
import com.actiontech.dble.net.mysql.EOFPacket;
import com.actiontech.dble.net.mysql.ErrorPacket;
import com.actiontech.dble.net.mysql.OkPacket;
import com.actiontech.dble.net.mysql.RequestFilePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * life cycle: from connection establish to close <br/>
 *
 * @author mycat
 */
public class MySQLConnectionHandler extends BackendAsyncHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MySQLConnectionHandler.class);
    private static final int RESULT_STATUS_INIT = 0;
    private static final int RESULT_STATUS_HEADER = 1;
    private static final int RESULT_STATUS_FIELD_EOF = 2;

    private final MySQLConnection source;
    private volatile int resultStatus;
    private volatile byte[] header;
    private volatile List<byte[]> fields;

    /**
     * life cycle: one SQL execution
     */
    private volatile ResponseHandler responseHandler;

    public MySQLConnectionHandler(MySQLConnection source) {
        this.source = source;
        this.resultStatus = RESULT_STATUS_INIT;
    }

    public void connectionError(Throwable e) {
        if (responseHandler != null) {
            responseHandler.connectionError(e, source);
        }

    }

    @Override
    public void handle(byte[] data) {
        if (source.isComplexQuery()) {
            offerData(data, DbleServer.getInstance().getComplexQueryExecutor());
        } else {
            offerData(data, source.getProcessor().getExecutor());
        }
    }

    @Override
    protected void offerDataError() {
        resultStatus = RESULT_STATUS_INIT;
        throw new RuntimeException("offer data error!");
    }

    @Override
    protected void handleData(byte[] data) {
        switch (resultStatus) {
            case RESULT_STATUS_INIT:
                switch (data[4]) {
                    case OkPacket.FIELD_COUNT:
                        handleOkPacket(data);
                        break;
                    case ErrorPacket.FIELD_COUNT:
                        handleErrorPacket(data);
                        break;
                    case RequestFilePacket.FIELD_COUNT:
                        handleRequestPacket(data);
                        break;
                    default:
                        resultStatus = RESULT_STATUS_HEADER;
                        header = data;
                        fields = new ArrayList<>((int) ByteUtil.readLength(data,
                                4));
                }
                break;
            case RESULT_STATUS_HEADER:
                switch (data[4]) {
                    case ErrorPacket.FIELD_COUNT:
                        resultStatus = RESULT_STATUS_INIT;
                        handleErrorPacket(data);
                        break;
                    case EOFPacket.FIELD_COUNT:
                        resultStatus = RESULT_STATUS_FIELD_EOF;
                        handleFieldEofPacket(data);
                        break;
                    default:
                        fields.add(data);
                }
                break;
            case RESULT_STATUS_FIELD_EOF:
                switch (data[4]) {
                    case ErrorPacket.FIELD_COUNT:
                        resultStatus = RESULT_STATUS_INIT;
                        handleErrorPacket(data);
                        break;
                    case EOFPacket.FIELD_COUNT:
                        resultStatus = RESULT_STATUS_INIT;
                        handleRowEofPacket(data);
                        break;
                    default:
                        handleRowPacket(data);
                }
                break;
            default:
                throw new RuntimeException("unknown status!");
        }
    }

    public void setResponseHandler(ResponseHandler responseHandler) {
        // logger.info("set response handler "+responseHandler);
        // if (this.responseHandler != null && responseHandler != null) {
        // throw new RuntimeException("reset agani!");
        // }
        this.responseHandler = responseHandler;
    }

    /**
     * execute OK Packet
     */
    private void handleOkPacket(byte[] data) {
        ResponseHandler respHand = responseHandler;
        if (respHand != null) {
            respHand.okResponse(data, source);
        }
    }

    /**
     * execute ERROR packet
     */
    private void handleErrorPacket(byte[] data) {
        ResponseHandler respHand = responseHandler;
        if (respHand != null) {
            respHand.errorResponse(data, source);
        } else {
            closeNoHandler();
        }
    }

    /**
     * execute load request Packet:load data file
     */
    private void handleRequestPacket(byte[] data) {
        ResponseHandler respHand = responseHandler;
        if (respHand != null && respHand instanceof LoadDataResponseHandler) {
            ((LoadDataResponseHandler) respHand).requestDataResponse(data,
                    source);
        } else {
            closeNoHandler();
        }
    }

    /**
     * execute FieldEof Packet
     */
    private void handleFieldEofPacket(byte[] data) {
        ResponseHandler respHand = responseHandler;
        if (respHand != null) {
            respHand.fieldEofResponse(header, fields, null, data, false, source);
        } else {
            closeNoHandler();
        }
    }

    /**
     * execute Row Packet
     */
    private void handleRowPacket(byte[] data) {
        ResponseHandler respHand = responseHandler;
        if (respHand != null) {
            respHand.rowResponse(data, null, false, source);
        } else {
            closeNoHandler();

        }
    }

    private void closeNoHandler() {
        if (!source.isClosedOrQuit()) {
            source.close("no handler");
            LOGGER.warn("no handler bind in this con " + this + " client:" + source);
        }
    }

    /**
     * execute RowEof Packet
     */
    private void handleRowEofPacket(byte[] data) {
        if (responseHandler != null) {
            responseHandler.rowEofResponse(data, false, source);
        } else {
            closeNoHandler();
        }
    }

    @Override
    protected void handleDataError(Exception e) {
        LOGGER.warn(this.source.toString() + " handle data error:", e);
        dataQueue.clear();
        ResponseHandler handler = this.responseHandler;
        if (handler != null)
            handler.connectionError(e, this.source);
    }
}
