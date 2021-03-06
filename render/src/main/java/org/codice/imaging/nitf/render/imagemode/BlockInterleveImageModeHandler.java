/*
 * Copyright (c) Codice Foundation
 *
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 *
 */
package org.codice.imaging.nitf.render.imagemode;

import java.awt.image.DataBuffer;
import java.io.IOException;
import org.codice.imaging.nitf.core.image.ImageMode;
import org.codice.imaging.nitf.core.image.ImageSegment;
import org.codice.imaging.nitf.render.imagerep.ImageRepresentationHandler;

/**
 * ImageModeHandler for Block Interleve mode.
 */
class BlockInterleveImageModeHandler extends SharedImageModeHandler implements ImageModeHandler {

    BlockInterleveImageModeHandler(final ImageRepresentationHandler imageRepresentationHandler) {
        super(imageRepresentationHandler);
    }

    @Override
    protected ImageMode getSupportedImageMode() {
        return ImageMode.BLOCKINTERLEVE;
    }


    @Override
    protected String getHandlerName() {
        return "BlockInterleveImageModeHandler";
    }

    @Override
    protected void readBlock(final ImageBlock block, final ImageSegment imageSegment) {

        final DataBuffer data = block.getDataBuffer();

        try {
            for (int bandIndex = 0; bandIndex < imageSegment.getNumBands(); bandIndex++) {
                for (int row = 0; row < block.getHeight(); row++) {
                    for (int column = 0; column < block.getWidth(); column++) {
                        int i = row * block.getWidth() + column;
                        imageRepresentationHandler.renderPixelBand(data, i, imageSegment.getData(), bandIndex);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
