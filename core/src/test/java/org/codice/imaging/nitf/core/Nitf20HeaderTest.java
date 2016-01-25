/**
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
 **/
package org.codice.imaging.nitf.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.codice.imaging.nitf.core.common.FileReader;
import org.codice.imaging.nitf.core.common.FileType;
import org.codice.imaging.nitf.core.common.NitfInputStreamReader;
import org.codice.imaging.nitf.core.common.NitfReader;
import org.codice.imaging.nitf.core.image.ImageCategory;
import org.codice.imaging.nitf.core.image.ImageCompression;
import org.codice.imaging.nitf.core.image.ImageCoordinatesRepresentation;
import org.codice.imaging.nitf.core.image.ImageMode;
import org.codice.imaging.nitf.core.image.ImageRepresentation;
import org.codice.imaging.nitf.core.image.NitfImageBand;
import org.codice.imaging.nitf.core.image.NitfImageSegmentHeader;
import org.codice.imaging.nitf.core.image.PixelJustification;
import org.codice.imaging.nitf.core.image.PixelValueType;
import org.codice.imaging.nitf.core.label.LabelSegmentHeader;
import org.codice.imaging.nitf.core.security.FileSecurityMetadata;
import org.codice.imaging.nitf.core.security.SecurityClassification;
import org.codice.imaging.nitf.core.security.SecurityMetadata;
import org.codice.imaging.nitf.core.symbol.SymbolColour;
import org.codice.imaging.nitf.core.symbol.SymbolSegmentHeader;
import org.codice.imaging.nitf.core.symbol.SymbolType;
import org.codice.imaging.nitf.core.text.TextDataExtractionParseStrategy;
import org.codice.imaging.nitf.core.text.TextFormat;
import org.codice.imaging.nitf.core.text.TextSegmentHeader;
import org.junit.Before;
import org.junit.Test;

public class Nitf20HeaderTest {

    private SimpleDateFormat formatter = null;

    @Before
    public void beforeTest() {
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Test
    public void testCompliantHeaderReadInputStream() throws IOException, ParseException {
        final String simpleNitf20File = "/JitcNitf20Samples/U_1114A.NTF";

        assertNotNull("Test file missing", getClass().getResource(simpleNitf20File));

        InputStream is = getClass().getResourceAsStream(simpleNitf20File);
        TextDataExtractionParseStrategy parseStrategy = new TextDataExtractionParseStrategy();
        NitfReader reader = new NitfInputStreamReader(new BufferedInputStream(is));
        NitfFileParser.parse(reader, parseStrategy);
        checkCompliantReadResults(parseStrategy);
        is.close();
    }

    @Test
    public void testCompliantHeaderReadFile() throws IOException, ParseException {
        final String simpleNitf20File = "/JitcNitf20Samples/U_1114A.NTF";

        assertNotNull("Test file missing", getClass().getResource(simpleNitf20File));

        File resourceFile = new File(getClass().getResource(simpleNitf20File).getFile());
        TextDataExtractionParseStrategy parseStrategy = new TextDataExtractionParseStrategy();
        NitfReader reader = new FileReader(resourceFile);
        NitfFileParser.parse(reader, parseStrategy);
        checkCompliantReadResults(parseStrategy);
        assertEquals("A", parseStrategy.getTextSegmentData().get(0));
    }

    private void checkCompliantReadResults(SlottedNitfParseStrategy parseStrategy) throws ParseException {
        NitfFileHeader header = parseStrategy.getNitfHeader();
        assertEquals(FileType.NITF_TWO_ZERO, header.getFileType());
        assertEquals(1, header.getComplexityLevel());
        assertEquals("", header.getStandardType());
        assertEquals("U21H00N1", header.getOriginatingStationId());
        assertEquals("1994-04-03 19:16:36", formatter.format(header.getFileDateTime().toDate()));
        assertEquals("checks the handling of an NITF file w/ only a text file.", header.getFileTitle());
        FileSecurityMetadata securityMetadata = header.getFileSecurityMetadata();
        assertUnclasAndEmpty(securityMetadata);
        assertEquals("999998", securityMetadata.getDowngradeDateOrSpecialCase());
        assertEquals("This  file   will not need a downgrade.", securityMetadata.getDowngradeEvent());

        assertEquals("00001", header.getFileSecurityMetadata().getFileCopyNumber());
        assertEquals("00001", header.getFileSecurityMetadata().getFileNumberOfCopies());
        assertEquals("JITC Fort Huachuca, AZ", header.getOriginatorsName());
        assertEquals("(602) 538-5458", header.getOriginatorsPhoneNumber());
        assertEquals(0, parseStrategy.getImageSegmentHeaders().size());
        assertEquals(0, parseStrategy.getGraphicSegmentHeaders().size());
        assertEquals(1, parseStrategy.getTextSegmentHeaders().size());
        assertEquals(0, parseStrategy.getDataExtensionSegmentHeaders().size());

        TextSegmentHeader textSegment = parseStrategy.getTextSegmentHeaders().get(0);
        assertNotNull(textSegment);
        assertEquals("0000000001", textSegment.getIdentifier());
        assertEquals(0, textSegment.getAttachmentLevel());
        assertEquals("1993-03-27 23:55:36", formatter.format(textSegment.getTextDateTime().toDate()));
        assertEquals("This is the title of unclassified text file #1 in NITF  file   U21H00N1.", textSegment.getTextTitle());
        SecurityMetadata textSecurityMetadata = textSegment.getSecurityMetadata();
        assertUnclasAndEmpty(textSecurityMetadata);
        assertEquals("", textSecurityMetadata.getSecurityControlNumber());
        assertEquals("999998", textSecurityMetadata.getDowngradeDateOrSpecialCase());
        assertEquals("This text will never need downgrading.", textSecurityMetadata.getDowngradeEvent());
        assertEquals(TextFormat.BASICCHARACTERSET, textSegment.getTextFormat());
    }

    @Test
    public void testU1123() throws IOException, ParseException {
        final String nitf20File = "/JitcNitf20Samples/U_1123A.NTF";

        assertNotNull("Test file missing", getClass().getResource(nitf20File));

        InputStream is = getClass().getResourceAsStream(nitf20File);
        AllDataExtractionParseStrategy parseStrategy = new AllDataExtractionParseStrategy();
        NitfReader reader = new NitfInputStreamReader(new BufferedInputStream(is));
        NitfFileParser.parse(reader, parseStrategy);
        NitfFileHeader nitfFileHeader = parseStrategy.getNitfHeader();
        assertEquals(FileType.NITF_TWO_ZERO, nitfFileHeader.getFileType());
        assertEquals(1, nitfFileHeader.getComplexityLevel());
        assertEquals("", nitfFileHeader.getStandardType());
        assertEquals("U217G0J1", nitfFileHeader.getOriginatingStationId());
        assertEquals("1992-11-03 13:52:26", formatter.format(nitfFileHeader.getFileDateTime().toDate()));
        assertEquals("This NITF message contains 5 images, 4 symbols, 4 labels and 1 text.", nitfFileHeader.getFileTitle());
        FileSecurityMetadata securityMetadata = nitfFileHeader.getFileSecurityMetadata();
        assertUnclasAndEmpty(securityMetadata);
        assertEquals("999998", securityMetadata.getDowngradeDateOrSpecialCase());
        assertEquals("This message will not need a downgrade.", securityMetadata.getDowngradeEvent());

        assertEquals("00001", nitfFileHeader.getFileSecurityMetadata().getFileCopyNumber());
        assertEquals("00001", nitfFileHeader.getFileSecurityMetadata().getFileNumberOfCopies());
        assertEquals("JITC", nitfFileHeader.getOriginatorsName());
        assertEquals("(602) 538-5458", nitfFileHeader.getOriginatorsPhoneNumber());
        assertEquals(5, parseStrategy.getImageSegmentHeaders().size());
        assertEquals(0, parseStrategy.getGraphicSegmentHeaders().size());
        assertEquals(4, parseStrategy.getSymbolSegmentHeaders().size());
        assertEquals(4, parseStrategy.getLabelSegmentHeaders().size());
        assertEquals(1, parseStrategy.getTextSegmentHeaders().size());
        assertEquals(0, parseStrategy.getDataExtensionSegmentHeaders().size());

        NitfImageSegmentHeader imageSegment1 = parseStrategy.getImageSegmentHeaders().get(0);
        assertNotNull(imageSegment1);
        assertEquals("Missing ID", imageSegment1.getIdentifier());
        assertEquals("1993-03-25 15:25:59",  formatter.format(imageSegment1.getImageDateTime().toDate()));
        assertEquals("- BASE IMAGE -", imageSegment1.getImageIdentifier2());
        assertEquals("          ", imageSegment1.getImageTargetId().getBasicEncyclopediaNumber());
        assertEquals("     ", imageSegment1.getImageTargetId().getOSuffix());
        assertEquals("  ", imageSegment1.getImageTargetId().getCountryCode());
        assertUnclasAndEmpty(imageSegment1.getSecurityMetadata());
        assertEquals("Unknown", imageSegment1.getImageSource());
        assertEquals(1024L, imageSegment1.getNumberOfRows());
        assertEquals(1024L, imageSegment1.getNumberOfColumns());
        assertEquals(PixelValueType.INTEGER, imageSegment1.getPixelValueType());
        assertEquals(ImageRepresentation.MONOCHROME, imageSegment1.getImageRepresentation());
        assertEquals(ImageCategory.VISUAL, imageSegment1.getImageCategory());
        assertEquals(8, imageSegment1.getActualBitsPerPixelPerBand());
        assertEquals(PixelJustification.RIGHT, imageSegment1.getPixelJustification());
        assertEquals(ImageCoordinatesRepresentation.NONE, imageSegment1.getImageCoordinatesRepresentation());
        assertEquals(0, imageSegment1.getImageComments().size());
        assertEquals(ImageCompression.NOTCOMPRESSED, imageSegment1.getImageCompression());
        assertEquals(1, imageSegment1.getNumBands());
        NitfImageBand band1 = imageSegment1.getImageBand(1);
        assertNotNull(band1);
        assertEquals("", band1.getImageRepresentation());
        assertEquals("", band1.getSubCategory());
        assertEquals(0, band1.getNumLUTs());
        assertEquals(0, band1.getNumLUTEntries());
        assertEquals(ImageMode.BLOCKINTERLEVE, imageSegment1.getImageMode());
        assertEquals(1, imageSegment1.getNumberOfBlocksPerRow());
        assertEquals(1, imageSegment1.getNumberOfBlocksPerColumn());
        assertEquals(1024, imageSegment1.getNumberOfPixelsPerBlockHorizontal());
        assertEquals(1024, imageSegment1.getNumberOfPixelsPerBlockVertical());
        assertEquals(8, imageSegment1.getNumberOfBitsPerPixelPerBand());
        assertEquals(1, imageSegment1.getImageDisplayLevel());
        assertEquals(0, imageSegment1.getAttachmentLevel());
        assertEquals(0, imageSegment1.getImageLocationRow());
        assertEquals(0, imageSegment1.getImageLocationColumn());
        assertEquals("1.0 ", imageSegment1.getImageMagnification());
        assertEquals(1.0, imageSegment1.getImageMagnificationAsDouble(), 0.00000001);

        NitfImageSegmentHeader imageSegment2 = parseStrategy.getImageSegmentHeaders().get(1);
        assertNotNull(imageSegment2);
        assertEquals("Missing ID", imageSegment2.getIdentifier());
        assertEquals("1993-03-25 15:25:59",  formatter.format(imageSegment2.getImageDateTime().toDate()));
        assertEquals("- GROUP 3 -", imageSegment2.getImageIdentifier2());
        assertEquals("          ", imageSegment2.getImageTargetId().getBasicEncyclopediaNumber());
        assertEquals("     ", imageSegment2.getImageTargetId().getOSuffix());
        assertEquals("  ", imageSegment2.getImageTargetId().getCountryCode());
        assertUnclasAndEmpty(imageSegment2.getSecurityMetadata());
        assertEquals("123456789012345678901234567890123456789012", imageSegment2.getImageSource());
        assertEquals(64L, imageSegment2.getNumberOfRows());
        assertEquals(64L, imageSegment2.getNumberOfColumns());
        assertEquals(PixelValueType.INTEGER, imageSegment2.getPixelValueType());
        assertEquals(ImageRepresentation.MONOCHROME, imageSegment2.getImageRepresentation());
        assertEquals(ImageCategory.VISUAL, imageSegment2.getImageCategory());
        assertEquals(1, imageSegment2.getActualBitsPerPixelPerBand());
        assertEquals(PixelJustification.RIGHT, imageSegment2.getPixelJustification());
        assertEquals(ImageCoordinatesRepresentation.NONE, imageSegment2.getImageCoordinatesRepresentation());
        assertEquals(0, imageSegment2.getImageComments().size());
        assertEquals(ImageCompression.BILEVEL, imageSegment2.getImageCompression());
        assertEquals(1, imageSegment2.getNumBands());
        band1 = imageSegment2.getImageBand(1);
        assertNotNull(band1);
        assertEquals("", band1.getImageRepresentation());
        assertEquals("", band1.getSubCategory());
        assertEquals(0, band1.getNumLUTs());
        assertEquals(0, band1.getNumLUTEntries());
        assertEquals(ImageMode.BLOCKINTERLEVE, imageSegment2.getImageMode());
        assertEquals(1, imageSegment2.getNumberOfBlocksPerRow());
        assertEquals(1, imageSegment2.getNumberOfBlocksPerColumn());
        assertEquals(64, imageSegment2.getNumberOfPixelsPerBlockHorizontal());
        assertEquals(64, imageSegment2.getNumberOfPixelsPerBlockVertical());
        assertEquals(1, imageSegment2.getNumberOfBitsPerPixelPerBand());
        assertEquals(8, imageSegment2.getImageDisplayLevel());
        assertEquals(2, imageSegment2.getAttachmentLevel());
        assertEquals(150, imageSegment2.getImageLocationRow());
        assertEquals(0, imageSegment2.getImageLocationColumn());
        assertEquals("1.0 ", imageSegment2.getImageMagnification());

        NitfImageSegmentHeader imageSegment3 = parseStrategy.getImageSegmentHeaders().get(2);
        assertNotNull(imageSegment3);
        assertEquals("0000000003", imageSegment3.getIdentifier());
        assertEquals("1989-01-01 12:00:00",  formatter.format(imageSegment3.getImageDateTime().toDate()));
        assertEquals("This is an unclassified image in an unclassified NITF message called U227C2J0", imageSegment3.getImageIdentifier2());
        assertEquals("          ", imageSegment3.getImageTargetId().getBasicEncyclopediaNumber());
        assertEquals("     ", imageSegment3.getImageTargetId().getOSuffix());
        assertEquals("  ", imageSegment3.getImageTargetId().getCountryCode());
        assertUnclasAndEmpty(imageSegment3.getSecurityMetadata());
        assertEquals("", imageSegment3.getImageSource());
        assertEquals(64L, imageSegment3.getNumberOfRows());
        assertEquals(64L, imageSegment3.getNumberOfColumns());
        assertEquals(PixelValueType.INTEGER, imageSegment3.getPixelValueType());
        assertEquals(ImageRepresentation.MONOCHROME, imageSegment3.getImageRepresentation());
        assertEquals(ImageCategory.VISUAL, imageSegment3.getImageCategory());
        assertEquals(8, imageSegment3.getActualBitsPerPixelPerBand());
        assertEquals(PixelJustification.RIGHT, imageSegment3.getPixelJustification());
        assertEquals(ImageCoordinatesRepresentation.NONE, imageSegment3.getImageCoordinatesRepresentation());
        assertEquals(9, imageSegment3.getImageComments().size());
        assertEquals("This is image comment 1 for the unclassified image #3 from test message.", imageSegment3.getImageComments().get(0));
        assertEquals("This is image comment 2 for the unclassified image #3 from test message.", imageSegment3.getImageComments().get(1));
        assertEquals("This is image comment 3 for the unclassified image #3 from test message.", imageSegment3.getImageComments().get(2));
        assertEquals("This is image comment 4 for the unclassified image #3 from test message.", imageSegment3.getImageComments().get(3));
        assertEquals("This is image comment 5 for the unclassified image #3 from test message.", imageSegment3.getImageComments().get(4));
        assertEquals("This is image comment 6 for the unclassified image #3 from test message.", imageSegment3.getImageComments().get(5));
        assertEquals("This is image comment 7 for the unclassified image #3 from test message.", imageSegment3.getImageComments().get(6));
        assertEquals("This is image comment 8 for the unclassified image #3 from test message.", imageSegment3.getImageComments().get(7));
        assertEquals("This is image comment 9 for the unclassified image #3 from test message.", imageSegment3.getImageComments().get(8));
        assertEquals(ImageCompression.NOTCOMPRESSED, imageSegment3.getImageCompression());
        assertEquals(1, imageSegment3.getNumBands());
        band1 = imageSegment3.getImageBand(1);
        assertNotNull(band1);
        assertEquals("", band1.getImageRepresentation());
        assertEquals("", band1.getSubCategory());
        assertEquals(0, band1.getNumLUTs());
        assertEquals(0, band1.getNumLUTEntries());
        assertEquals(ImageMode.BLOCKINTERLEVE, imageSegment3.getImageMode());
        assertEquals(1, imageSegment3.getNumberOfBlocksPerRow());
        assertEquals(1, imageSegment3.getNumberOfBlocksPerColumn());
        assertEquals(64, imageSegment3.getNumberOfPixelsPerBlockHorizontal());
        assertEquals(64, imageSegment3.getNumberOfPixelsPerBlockVertical());
        assertEquals(8, imageSegment3.getNumberOfBitsPerPixelPerBand());
        assertEquals(12, imageSegment3.getImageDisplayLevel());
        assertEquals(11, imageSegment3.getAttachmentLevel());
        assertEquals(10, imageSegment3.getImageLocationRow());
        assertEquals(10, imageSegment3.getImageLocationColumn());
        assertEquals("1.0 ", imageSegment3.getImageMagnification());

        NitfImageSegmentHeader imageSegment4 = parseStrategy.getImageSegmentHeaders().get(3);
        assertNotNull(imageSegment4);
        assertEquals("0000000001", imageSegment4.getIdentifier());
        assertEquals("1989-01-01 12:00:00",  formatter.format(imageSegment4.getImageDateTime().toDate()));
        assertEquals("This is an unclassified image in an unclassified NITF file.", imageSegment4.getImageIdentifier2());
        assertEquals("          ", imageSegment4.getImageTargetId().getBasicEncyclopediaNumber());
        assertEquals("     ", imageSegment4.getImageTargetId().getOSuffix());
        assertEquals("  ", imageSegment4.getImageTargetId().getCountryCode());
        assertUnclasAndEmpty(imageSegment4.getSecurityMetadata());
        assertEquals("", imageSegment4.getImageSource());
        assertEquals(191L, imageSegment4.getNumberOfRows());
        assertEquals(231L, imageSegment4.getNumberOfColumns());
        assertEquals(PixelValueType.INTEGER, imageSegment4.getPixelValueType());
        assertEquals(ImageRepresentation.MONOCHROME, imageSegment4.getImageRepresentation());
        assertEquals(ImageCategory.VISUAL, imageSegment4.getImageCategory());
        assertEquals(8, imageSegment4.getActualBitsPerPixelPerBand());
        assertEquals(PixelJustification.RIGHT, imageSegment4.getPixelJustification());
        assertEquals(ImageCoordinatesRepresentation.NONE, imageSegment4.getImageCoordinatesRepresentation());
        assertEquals(0, imageSegment4.getImageComments().size());
        assertEquals(ImageCompression.JPEG, imageSegment4.getImageCompression());
        assertEquals(1, imageSegment4.getNumBands());
        band1 = imageSegment4.getImageBand(1);
        assertNotNull(band1);
        assertEquals("", band1.getImageRepresentation());
        assertEquals("", band1.getSubCategory());
        assertEquals(0, band1.getNumLUTs());
        assertEquals(0, band1.getNumLUTEntries());
        assertEquals(ImageMode.BLOCKINTERLEVE, imageSegment4.getImageMode());
        assertEquals(1, imageSegment4.getNumberOfBlocksPerRow());
        assertEquals(1, imageSegment4.getNumberOfBlocksPerColumn());
        assertEquals(231, imageSegment4.getNumberOfPixelsPerBlockHorizontal());
        assertEquals(191, imageSegment4.getNumberOfPixelsPerBlockVertical());
        assertEquals(8, imageSegment4.getNumberOfBitsPerPixelPerBand());
        assertEquals(5, imageSegment4.getImageDisplayLevel());
        assertEquals(1, imageSegment4.getAttachmentLevel());
        assertEquals(40, imageSegment4.getImageLocationRow());
        assertEquals(220, imageSegment4.getImageLocationColumn());
        assertEquals("1.0 ", imageSegment4.getImageMagnification());

        NitfImageSegmentHeader imageSegment5 = parseStrategy.getImageSegmentHeaders().get(4);
        assertNotNull(imageSegment5);
        assertEquals("0000000001", imageSegment5.getIdentifier());
        assertEquals("1989-01-01 12:00:00",  formatter.format(imageSegment5.getImageDateTime().toDate()));
        assertEquals("This is an unclassified image in an unclassified NITF file Q3.", imageSegment5.getImageIdentifier2());
        assertEquals("          ", imageSegment5.getImageTargetId().getBasicEncyclopediaNumber());
        assertEquals("     ", imageSegment5.getImageTargetId().getOSuffix());
        assertEquals("  ", imageSegment5.getImageTargetId().getCountryCode());
        assertUnclasAndEmpty(imageSegment5.getSecurityMetadata());
        assertEquals("", imageSegment5.getImageSource());
        assertEquals(73L, imageSegment5.getNumberOfRows());
        assertEquals(181L, imageSegment5.getNumberOfColumns());
        assertEquals(PixelValueType.INTEGER, imageSegment5.getPixelValueType());
        assertEquals(ImageRepresentation.MONOCHROME, imageSegment5.getImageRepresentation());
        assertEquals(ImageCategory.VISUAL, imageSegment5.getImageCategory());
        assertEquals(8, imageSegment5.getActualBitsPerPixelPerBand());
        assertEquals(PixelJustification.RIGHT, imageSegment5.getPixelJustification());
        assertEquals(ImageCoordinatesRepresentation.NONE, imageSegment5.getImageCoordinatesRepresentation());
        assertEquals(9, imageSegment5.getImageComments().size());
        assertEquals("This is image comment #1 for the unclassified image #1 from test message Q3.", imageSegment5.getImageComments().get(0));
        assertEquals("This is image comment #9 for the unclassified image #1 from test message Q3.", imageSegment5.getImageComments().get(8));
        assertEquals(ImageCompression.JPEG, imageSegment5.getImageCompression());
        assertEquals(1, imageSegment5.getNumBands());
        band1 = imageSegment5.getImageBand(1);
        assertNotNull(band1);
        assertEquals("", band1.getImageRepresentation());
        assertEquals("", band1.getSubCategory());
        assertEquals(0, band1.getNumLUTs());
        assertEquals(0, band1.getNumLUTEntries());
        assertEquals(ImageMode.BLOCKINTERLEVE, imageSegment5.getImageMode());
        assertEquals(1, imageSegment5.getNumberOfBlocksPerRow());
        assertEquals(1, imageSegment5.getNumberOfBlocksPerColumn());
        assertEquals(181, imageSegment5.getNumberOfPixelsPerBlockHorizontal());
        assertEquals(73, imageSegment5.getNumberOfPixelsPerBlockVertical());
        assertEquals(8, imageSegment5.getNumberOfBitsPerPixelPerBand());
        assertEquals(2, imageSegment5.getImageDisplayLevel());
        assertEquals(1, imageSegment5.getAttachmentLevel());
        assertEquals(65, imageSegment5.getImageLocationRow());
        assertEquals(30, imageSegment5.getImageLocationColumn());
        assertEquals("1.0 ", imageSegment5.getImageMagnification());

        SymbolSegmentHeader symbolSegment1 = parseStrategy.getSymbolSegmentHeaders().get(0);
        assertNotNull(symbolSegment1);
        assertEquals("0000000001", symbolSegment1.getIdentifier());
        assertEquals("Unclassified Symbol.", symbolSegment1.getSymbolName());
        assertUnclasAndEmpty(symbolSegment1.getSecurityMetadata());
        assertEquals("999998", symbolSegment1.getSecurityMetadata().getDowngradeDateOrSpecialCase());
        assertEquals("This symbol will never need downgrading.", symbolSegment1.getSecurityMetadata().getDowngradeEvent());
        assertEquals(SymbolType.BITMAP, symbolSegment1.getSymbolType());
        assertEquals(SymbolColour.ZERO_TRANSPARENT_ONE_BLACK, symbolSegment1.getSymbolColour());
        assertEquals(7, symbolSegment1.getNumberOfLinesPerSymbol());
        assertEquals(7, symbolSegment1.getNumberOfPixelsPerLine());
        assertEquals(0, symbolSegment1.getLineWidth());
        assertEquals(1, symbolSegment1.getNumberOfBitsPerPixel());
        assertEquals(3, symbolSegment1.getSymbolDisplayLevel());
        assertEquals(1, symbolSegment1.getAttachmentLevel());
        assertEquals(62, symbolSegment1.getSymbolLocationRow());
        assertEquals(170, symbolSegment1.getSymbolLocationColumn());
        assertEquals(0, symbolSegment1.getSymbolLocation2Row());
        assertEquals(0, symbolSegment1.getSymbolLocation2Column());
        assertEquals("000000", symbolSegment1.getSymbolNumber());
        assertEquals(0, symbolSegment1.getSymbolRotation());
        assertEquals(7, parseStrategy.getSymbolSegmentData().get(0).length);

        SymbolSegmentHeader symbolSegment2 = parseStrategy.getSymbolSegmentHeaders().get(1);
        assertNotNull(symbolSegment2);
        assertEquals("0000000002", symbolSegment2.getIdentifier());
        assertEquals("Unclassified Symbol.", symbolSegment2.getSymbolName());
        assertUnclasAndEmpty(symbolSegment2.getSecurityMetadata());
        assertEquals("999998", symbolSegment2.getSecurityMetadata().getDowngradeDateOrSpecialCase());
        assertEquals("This symbol will never need downgrading.", symbolSegment2.getSecurityMetadata().getDowngradeEvent());
        assertEquals(SymbolType.BITMAP, symbolSegment2.getSymbolType());
        assertEquals(SymbolColour.ZERO_TRANSPARENT_ONE_BLACK, symbolSegment2.getSymbolColour());
        assertEquals(18, symbolSegment2.getNumberOfLinesPerSymbol());
        assertEquals(35, symbolSegment2.getNumberOfPixelsPerLine());
        assertEquals(0, symbolSegment2.getLineWidth());
        assertEquals(1, symbolSegment2.getNumberOfBitsPerPixel());
        assertEquals(7, symbolSegment2.getSymbolDisplayLevel());
        assertEquals(5, symbolSegment2.getAttachmentLevel());
        assertEquals(100, symbolSegment2.getSymbolLocationRow());
        assertEquals(100, symbolSegment2.getSymbolLocationColumn());
        assertEquals(0, symbolSegment2.getSymbolLocation2Row());
        assertEquals(0, symbolSegment2.getSymbolLocation2Column());
        assertEquals("000000", symbolSegment2.getSymbolNumber());
        assertEquals(0, symbolSegment2.getSymbolRotation());
        assertEquals(79, parseStrategy.getSymbolSegmentData().get(1).length);

        SymbolSegmentHeader symbolSegment3 = parseStrategy.getSymbolSegmentHeaders().get(2);
        assertNotNull(symbolSegment3);
        assertEquals("0000000003", symbolSegment3.getIdentifier());
        assertEquals("Unclassified Symbol.", symbolSegment3.getSymbolName());
        assertUnclasAndEmpty(symbolSegment3.getSecurityMetadata());
        assertEquals("999998", symbolSegment3.getSecurityMetadata().getDowngradeDateOrSpecialCase());
        assertEquals("This symbol will never need downgrading.", symbolSegment3.getSecurityMetadata().getDowngradeEvent());
        assertEquals(SymbolType.BITMAP, symbolSegment3.getSymbolType());
        assertEquals(SymbolColour.ZERO_TRANSPARENT_ONE_BLACK, symbolSegment3.getSymbolColour());
        assertEquals(18, symbolSegment3.getNumberOfLinesPerSymbol());
        assertEquals(35, symbolSegment3.getNumberOfPixelsPerLine());
        assertEquals(0, symbolSegment3.getLineWidth());
        assertEquals(1, symbolSegment3.getNumberOfBitsPerPixel());
        assertEquals(9, symbolSegment3.getSymbolDisplayLevel());
        assertEquals(8, symbolSegment3.getAttachmentLevel());
        assertEquals(25, symbolSegment3.getSymbolLocationRow());
        assertEquals(25, symbolSegment3.getSymbolLocationColumn());
        assertEquals(0, symbolSegment3.getSymbolLocation2Row());
        assertEquals(0, symbolSegment3.getSymbolLocation2Column());
        assertEquals("000000", symbolSegment3.getSymbolNumber());
        assertEquals(0, symbolSegment3.getSymbolRotation());
        assertEquals(79, parseStrategy.getSymbolSegmentData().get(2).length);

        SymbolSegmentHeader symbolSegment4 = parseStrategy.getSymbolSegmentHeaders().get(3);
        assertNotNull(symbolSegment4);
        assertEquals("0000000004", symbolSegment4.getIdentifier());
        assertEquals("Unclassified Symbol.", symbolSegment4.getSymbolName());
        assertUnclasAndEmpty(symbolSegment4.getSecurityMetadata());
        assertEquals("999998", symbolSegment4.getSecurityMetadata().getDowngradeDateOrSpecialCase());
        assertEquals("This symbol will never need downgrading.", symbolSegment4.getSecurityMetadata().getDowngradeEvent());
        assertEquals(SymbolType.BITMAP, symbolSegment4.getSymbolType());
        assertEquals(SymbolColour.ZERO_TRANSPARENT_ONE_BLACK, symbolSegment4.getSymbolColour());
        assertEquals(17, symbolSegment4.getNumberOfLinesPerSymbol());
        assertEquals(35, symbolSegment4.getNumberOfPixelsPerLine());
        assertEquals(0, symbolSegment4.getLineWidth());
        assertEquals(1, symbolSegment4.getNumberOfBitsPerPixel());
        assertEquals(11, symbolSegment4.getSymbolDisplayLevel());
        assertEquals(1, symbolSegment4.getAttachmentLevel());
        assertEquals(400, symbolSegment4.getSymbolLocationRow());
        assertEquals(400, symbolSegment4.getSymbolLocationColumn());
        assertEquals(0, symbolSegment4.getSymbolLocation2Row());
        assertEquals(0, symbolSegment4.getSymbolLocation2Column());
        assertEquals("000000", symbolSegment4.getSymbolNumber());
        assertEquals(0, symbolSegment4.getSymbolRotation());
        assertEquals(75, parseStrategy.getSymbolSegmentData().get(3).length);

        LabelSegmentHeader labelSegment1 = parseStrategy.getLabelSegmentHeaders().get(0);
        assertNotNull(labelSegment1);
        assertEquals("0000000001", labelSegment1.getIdentifier());
        assertUnclasAndEmpty(labelSegment1.getSecurityMetadata());
        assertEquals("999998", labelSegment1.getSecurityMetadata().getDowngradeDateOrSpecialCase());
        assertEquals("This label will never need downgrading.", labelSegment1.getSecurityMetadata().getDowngradeEvent());
        assertEquals(20, labelSegment1.getLabelLocationRow());
        assertEquals(160, labelSegment1.getLabelLocationColumn());
        assertEquals(0, labelSegment1.getLabelCellWidth());
        assertEquals(0, labelSegment1.getLabelCellHeight());
        assertEquals(4, labelSegment1.getLabelDisplayLevel());
        assertEquals(2, labelSegment1.getAttachmentLevel());
        // TODO: make this reflect the Table VIII representation.
        assertEquals(1, labelSegment1.getLabelTextColour().getRed());
        assertEquals(1, labelSegment1.getLabelTextColour().getGreen());
        assertEquals(1, labelSegment1.getLabelTextColour().getBlue());
        assertEquals(0, labelSegment1.getLabelBackgroundColour().getRed());
        assertEquals(0, labelSegment1.getLabelBackgroundColour().getGreen());
        assertEquals(0, labelSegment1.getLabelBackgroundColour().getBlue());
        assertEquals("Label 3", parseStrategy.getLabelSegmentData().get(0));

        LabelSegmentHeader labelSegment2 = parseStrategy.getLabelSegmentHeaders().get(1);
        assertNotNull(labelSegment2);
        assertEquals("0000000002", labelSegment2.getIdentifier());
        assertUnclasAndEmpty(labelSegment2.getSecurityMetadata());
        assertEquals("999998", labelSegment2.getSecurityMetadata().getDowngradeDateOrSpecialCase());
        assertEquals("This label will never need downgrading.", labelSegment2.getSecurityMetadata().getDowngradeEvent());
        assertEquals(100, labelSegment2.getLabelLocationRow());
        assertEquals(100, labelSegment2.getLabelLocationColumn());
        assertEquals(0, labelSegment2.getLabelCellWidth());
        assertEquals(0, labelSegment2.getLabelCellHeight());
        assertEquals(6, labelSegment2.getLabelDisplayLevel());
        assertEquals(5, labelSegment2.getAttachmentLevel());
        // TODO: make this reflect the Table VIII representation.
        assertEquals(1, labelSegment2.getLabelTextColour().getRed());
        assertEquals(1, labelSegment2.getLabelTextColour().getGreen());
        assertEquals(1, labelSegment2.getLabelTextColour().getBlue());
        assertEquals(0, labelSegment2.getLabelBackgroundColour().getRed());
        assertEquals(0, labelSegment2.getLabelBackgroundColour().getGreen());
        assertEquals(0, labelSegment2.getLabelBackgroundColour().getBlue());
        assertEquals("Label 2", parseStrategy.getLabelSegmentData().get(1));

        LabelSegmentHeader labelSegment3 = parseStrategy.getLabelSegmentHeaders().get(2);
        assertNotNull(labelSegment3);
        assertEquals("0000000003", labelSegment3.getIdentifier());
        assertUnclasAndEmpty(labelSegment3.getSecurityMetadata());
        assertEquals("999998", labelSegment3.getSecurityMetadata().getDowngradeDateOrSpecialCase());
        assertEquals("This label will never need downgrading.", labelSegment3.getSecurityMetadata().getDowngradeEvent());
        assertEquals(-20, labelSegment3.getLabelLocationRow());
        assertEquals(-20, labelSegment3.getLabelLocationColumn());
        assertEquals(0, labelSegment3.getLabelCellWidth());
        assertEquals(0, labelSegment3.getLabelCellHeight());
        assertEquals(10, labelSegment3.getLabelDisplayLevel());
        assertEquals(9, labelSegment3.getAttachmentLevel());
        // TODO: make this reflect the Table VIII representation.
        assertEquals(1, labelSegment3.getLabelTextColour().getRed());
        assertEquals(1, labelSegment3.getLabelTextColour().getGreen());
        assertEquals(1, labelSegment3.getLabelTextColour().getBlue());
        assertEquals(0, labelSegment3.getLabelBackgroundColour().getRed());
        assertEquals(0, labelSegment3.getLabelBackgroundColour().getGreen());
        assertEquals(0, labelSegment3.getLabelBackgroundColour().getBlue());
        assertEquals("Label 4", parseStrategy.getLabelSegmentData().get(2));

        LabelSegmentHeader labelSegment4 = parseStrategy.getLabelSegmentHeaders().get(3);
        assertNotNull(labelSegment4);
        assertEquals("0000000004", labelSegment4.getIdentifier());
        assertUnclasAndEmpty(labelSegment4.getSecurityMetadata());
        assertEquals("999998", labelSegment4.getSecurityMetadata().getDowngradeDateOrSpecialCase());
        assertEquals("This label will never need downgrading.", labelSegment4.getSecurityMetadata().getDowngradeEvent());
        assertEquals(0, labelSegment4.getLabelLocationRow());
        assertEquals(25, labelSegment4.getLabelLocationColumn());
        assertEquals(0, labelSegment4.getLabelCellWidth());
        assertEquals(0, labelSegment4.getLabelCellHeight());
        assertEquals(13, labelSegment4.getLabelDisplayLevel());
        assertEquals(11, labelSegment4.getAttachmentLevel());
        // TODO: make this reflect the Table VIII representation.
        assertEquals(1, labelSegment4.getLabelTextColour().getRed());
        assertEquals(1, labelSegment4.getLabelTextColour().getGreen());
        assertEquals(1, labelSegment4.getLabelTextColour().getBlue());
        assertEquals(0, labelSegment4.getLabelBackgroundColour().getRed());
        assertEquals(0, labelSegment4.getLabelBackgroundColour().getGreen());
        assertEquals(0, labelSegment4.getLabelBackgroundColour().getBlue());
        assertEquals("Label 1", parseStrategy.getLabelSegmentData().get(3));

        TextSegmentHeader textSegment = parseStrategy.getTextSegmentHeaders().get(0);
        assertNotNull(textSegment);
        assertEquals("0000000001", textSegment.getIdentifier());
        assertEquals(0, textSegment.getAttachmentLevel());
        assertEquals("1990-06-07 21:11:36", formatter.format(textSegment.getTextDateTime().toDate()));
        assertEquals("This is the title of unclassified text file #1 in NITF message JR1_B.", textSegment.getTextTitle());
        SecurityMetadata textSecurityMetadata = textSegment.getSecurityMetadata();
        assertUnclasAndEmpty(textSecurityMetadata);
        assertEquals("999998", textSecurityMetadata.getDowngradeDateOrSpecialCase());
        assertEquals("This text will never need downgrading.", textSecurityMetadata.getDowngradeEvent());
        assertEquals(TextFormat.BASICCHARACTERSET, textSegment.getTextFormat());
        assertEquals("123456\r\n", parseStrategy.getTextSegmentData().get(0));

        is.close();
    }

    void assertUnclasAndEmpty(SecurityMetadata securityMetadata) {
        assertNotNull(securityMetadata);
        assertEquals(SecurityClassification.UNCLASSIFIED, securityMetadata.getSecurityClassification());
        assertNull(securityMetadata.getSecurityClassificationSystem());
        assertEquals("", securityMetadata.getCodewords());
        assertEquals("", securityMetadata.getControlAndHandling());
        assertEquals("", securityMetadata.getReleaseInstructions());
        assertNull(securityMetadata.getDeclassificationType());
        assertNull(securityMetadata.getDeclassificationDate());
        assertNull(securityMetadata.getDeclassificationExemption());
        assertNull(securityMetadata.getDowngrade());
        assertNull(securityMetadata.getDowngradeDate());
        assertNull(securityMetadata.getClassificationText());
        assertNull(securityMetadata.getClassificationAuthorityType());
        assertEquals("", securityMetadata.getClassificationAuthority());
        assertNull(securityMetadata.getClassificationReason());
        assertEquals("", securityMetadata.getSecurityControlNumber());
    }
}