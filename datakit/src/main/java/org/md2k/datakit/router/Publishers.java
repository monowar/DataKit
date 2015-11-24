package org.md2k.datakit.router;

import android.os.Messenger;
import android.util.SparseArray;

import org.md2k.datakit.logger2.DatabaseLogger;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.status.StatusCodes;
import org.md2k.utilities.Report.Log;

/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p/>
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p/>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class Publishers {
    private static final String TAG = Publishers.class.getSimpleName();
    SparseArray<Publisher> publishers;
    private static Publishers instance = null;

    public static Publishers getInstance() {
        if (instance == null)
            instance = new Publishers();
        return instance;
    }

    Publishers() {
        publishers = new SparseArray<>();
    }
    public int addPublisher(int ds_id) {
        if (ds_id == -1) return StatusCodes.DATASOURCE_INVALID;
        if (publishers.indexOfKey(ds_id) < 0) {
            publishers.put(ds_id, new Publisher(ds_id));
        }
        publishers.get(ds_id).setActive(true);
        return StatusCodes.SUCCESS;
    }
    public int addSubscriber(int ds_id) {
        if (ds_id == -1) return StatusCodes.DATASOURCE_INVALID;
        if (publishers.indexOfKey(ds_id) < 0) {
            publishers.put(ds_id, new Publisher(ds_id));
            publishers.get(ds_id).setActive(false);
        }
        return StatusCodes.SUCCESS;
    }

    public int addPublisher(int ds_id, DatabaseLogger databaseLogger) {
        int status=addPublisher(ds_id);
        if(status==StatusCodes.SUCCESS)
            publishers.get(ds_id).setDatabaseSubscriber(new DatabaseSubscriber(databaseLogger));
        return StatusCodes.SUCCESS;
    }

    public void receivedData(int ds_id, DataType dataType) {
        publishers.get(ds_id).receivedData(dataType);
    }

    public int remove(int ds_id) {
        if(!isExist(ds_id))
            return StatusCodes.DATASOURCE_NOT_EXIST;
        publishers.get(ds_id).setDatabaseSubscriber(null);
        publishers.get(ds_id).setActive(false);
        return StatusCodes.SUCCESS;
    }
    public void setActive(int ds_id, boolean active){
        publishers.get(ds_id).setActive(active);
    }

    public boolean isExist(int ds_id) {
        return publishers.indexOfKey(ds_id) >= 0;
    }

    public int subscribe(int ds_id, Messenger reply) {
        int status=addSubscriber(ds_id);
        if(status==StatusCodes.SUCCESS)
            status= publishers.get(ds_id).add(new MessageSubscriber(reply));
        return status;
    }

    public int unsubscribe(int ds_id, Messenger reply) {
        if(!isExist(ds_id))
            return StatusCodes.DATASOURCE_NOT_EXIST;
        return publishers.get(ds_id).remove(new MessageSubscriber(reply));
    }

    public void close() {
        Log.d(TAG, "close: publishers size=" + publishers.size());
        for (int i = 0; i < publishers.size(); i++) {
            int key = publishers.keyAt(i);
            publishers.get(key).close();
        }
        publishers.clear();
        publishers = null;
        instance = null;
    }
}
