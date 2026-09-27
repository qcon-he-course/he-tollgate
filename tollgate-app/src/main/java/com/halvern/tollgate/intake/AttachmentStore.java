/*
 * Tollgate - Abnormal Load Permit System
 * Copyright (c) 2017 Pellowe Tarn Systems Ltd. All rights reserved.
 * Developed under contract PT-HX-0412 for Halvern Crossings plc.
 *
 * $Id: AttachmentStore.java 1092 2017-10-10 16:55:30Z koduya $
 */
package com.halvern.tollgate.intake;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import com.halvern.tollgate.platform.ErrorCodes;
import com.halvern.tollgate.platform.TollgateException;

/**
 * Stores attachment files on the file system under the configured directory, one folder per application.
 * Implements BR-13.
 *
 * @author K. Oduya
 * @since 1.0
 */
@Component
public class AttachmentStore {

    private static final Logger log = LoggerFactory.getLogger(AttachmentStore.class);

    @Value("${tollgate.attachments.dir}")
    private String baseDir;

    /**
     * Save.
     *
     * @param reference the application reference
     * @param in the file content
     * @return the storage path relative to the attachments directory
     * @throws TollgateException if the file cannot be written
     */
    public String save(String reference, InputStream in) throws TollgateException {
        File dir = new File(baseDir, reference);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new TollgateException(ErrorCodes.ATTACHMENT_STORE_FAILED, "Cannot create " + dir);
        }
        String name = UUID.randomUUID().toString() + ".pdf";
        File file = new File(dir, name);
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            FileCopyUtils.copy(in, out);
        } catch (IOException e) {
            throw new TollgateException(ErrorCodes.ATTACHMENT_STORE_FAILED, "Cannot write " + file, e);
        }
        log.info("Stored attachment " + file.getAbsolutePath());
        return reference + "/" + name;
    }

    /**
     * Open.
     *
     * @param storagePath the storage path returned by save
     * @return a stream over the file content; the caller must close it
     * @throws TollgateException if the file is missing
     */
    public InputStream open(String storagePath) throws TollgateException {
        File file = new File(baseDir, storagePath);
        try {
            return new FileInputStream(file);
        } catch (IOException e) {
            throw new TollgateException(ErrorCodes.ATTACHMENT_NOT_FOUND, "Missing attachment " + file, e);
        }
    }
}
