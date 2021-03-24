/*
 *  Copyright (c) 2021 Vulcan Inc. All rights reserved.
 *  Licensed under the Apache 2.0 license. See LICENSE file in the project root for full license information.
 */

package com.vulcan.vmlci.orca.helpers;

/**
 * Exception raised during the loading of a data file.
 */
public class DataFileLoadException extends Exception {
    /**
     * Constructs a new exception with the specified detail message and cause.  <p>Note that the detail
     * message associated with {@code cause} is <i>not</i> automatically incorporated in this
     * exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link
     *                #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()} method).
     *                (A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent
     *                or unknown.)
     * @since 1.4
     */
    public DataFileLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
