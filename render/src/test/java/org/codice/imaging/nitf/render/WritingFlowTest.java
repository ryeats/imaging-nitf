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
package org.codice.imaging.nitf.render;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;

import org.codice.imaging.nitf.render.flow.NitfParserInputFlow;
import org.codice.imaging.nitf.render.flow.NitfWriterFlow;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests that the flow API for writing NITFs works as intended.
 */
public class WritingFlowTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(WritingFlowTest.class);
    private static final String INPUT_FILE_NAME = "ns3038a.nsf";
    private static final String DIRECTORY_NAME = "JitcNitf21Samples";

    private static final String TEST_FILE_TITLE = "test file title";
    private static final String TEST_ORIGINATOR_NAME = "test originator";
    private static final String TEST_STATIION_ID = "_OSTAID";

    private File outputFile;

    @Before
    public void setUp() {
        String outputFileName = "target" + File.separator + "output-" + INPUT_FILE_NAME;
        this.outputFile = new File(outputFileName);
    }

    @Test
    public void testNS3038A() throws IOException, ParseException {
        String inputFileName = "/" + DIRECTORY_NAME + "/" + INPUT_FILE_NAME;
        LOGGER.info("================================== Testing :" + inputFileName);
        assertNotNull("Test file missing: " + inputFileName, getClass().getResource(inputFileName));
        modifyNitf(inputFileName);
        verifyResult();
    }

    public void modifyNitf(final String inputFileName) throws
            IOException, ParseException {

        new NitfParserInputFlow()
                .inputStream(getClass().getResourceAsStream(inputFileName))
                .imageData()
                .fileHeader(
                    header -> {
                        header.setFileTitle(TEST_FILE_TITLE);
                        header.setOriginatorsName(TEST_ORIGINATOR_NAME);
                        header.setOriginatingStationId(TEST_STATIION_ID);
                    }
                )
                .dataSource(datasource ->
                        new NitfWriterFlow()
                            .file(outputFile)
                            .write(datasource)
                );
    }

    private void verifyResult() throws ParseException, FileNotFoundException {
        new NitfParserInputFlow()
                .file(outputFile)
                .headerOnly()
                .fileHeader(
                    header -> {
                        assertThat(header.getFileTitle(), is(TEST_FILE_TITLE));
                        assertThat(header.getOriginatorsName(), is(TEST_ORIGINATOR_NAME));
                        assertThat(header.getOriginatingStationId(), is(TEST_STATIION_ID));
                    }
                );
    }

}