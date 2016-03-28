/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package com.koolearn.kooreader.formats;

import com.koolearn.klibrary.core.drm.FileEncryptionInfo;
import com.koolearn.klibrary.core.encodings.EncodingCollection;
import com.koolearn.klibrary.core.filesystem.ZLFile;
import com.koolearn.klibrary.core.image.ZLImage;
import com.koolearn.klibrary.core.resources.ZLResource;
import com.koolearn.klibrary.core.util.SystemInfo;
import com.koolearn.kooreader.book.AbstractBook;

import java.util.Collections;
import java.util.List;

public abstract class FormatPlugin {
    protected final com.koolearn.klibrary.core.util.SystemInfo SystemInfo;
    private final String myFileType;

    protected FormatPlugin(SystemInfo systemInfo, String fileType) {
        SystemInfo = systemInfo;
        myFileType = fileType;
    }

    public final String supportedFileType() {
        return myFileType;
    }

    public final String name() {
        return ZLResource.resource("format").getResource(myFileType).getValue();
    }

    public ZLFile realBookFile(ZLFile file) throws BookReadingException {
        return file;
    }

    public List<FileEncryptionInfo> readEncryptionInfos(AbstractBook book) {

        return Collections.emptyList();
    }

    public abstract void readMetainfo(AbstractBook book) throws BookReadingException;

    public abstract void readUids(AbstractBook book) throws BookReadingException;

    public abstract void detectLanguageAndEncoding(AbstractBook book) throws BookReadingException;

    public abstract ZLImage readCover(ZLFile file);

//    public abstract String readAnnotation(ZLFile file);

    /* lesser is higher: 0 for ePub/fb2, 5 for other native, 10 for external */
    public abstract int priority();

    public abstract EncodingCollection supportedEncodings();
}
