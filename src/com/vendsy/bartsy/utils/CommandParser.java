/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vendsy.bartsy.utils;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class parses XML feeds from stackoverflow.com.
 * Given an InputStream representation of a feed, it returns a List of entries,
 * where each list element represents a single entry (post) in the XML feed.
 */
public class CommandParser {
    private static final String ns = null;

    // This class represents a single command in the XML feed.
    // It includes the data members "opcode," "arguments"
    public static class BartsyCommand {
        public String opcode;
        public ArrayList<String> arguments;

        private BartsyCommand(String opcode, ArrayList<String> arguments) {
            this.opcode = opcode;
            this.arguments = arguments;
        }
    }

    // Parses the contents of a command. 

    public BartsyCommand parse(InputStream in) throws XmlPullParserException, IOException {

    	XmlPullParser parser;
    	
    	try {
            parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
        } finally {
            in.close();
        }

        parser.require(XmlPullParser.START_TAG, ns, "command");

//        String title = null;
//        String summary = null;
//        String link = null;
        
        BartsyCommand bartsyCommand = new BartsyCommand("", new ArrayList<String>());
        
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            parser.require(XmlPullParser.START_TAG, ns, name);
            String title = readText(parser);
            parser.require(XmlPullParser.END_TAG, ns, name);
            if (name.equals("opcode")) {
                bartsyCommand.opcode = title;
            } else if (name.equals("argument")) {
                bartsyCommand.arguments.add(title);
            } else {
                skip(parser);
            }
        }
        return bartsyCommand;
    }


    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
    // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
    // finds the matching END_TAG (as indicated by the value of "depth" being 0).
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
            case XmlPullParser.END_TAG:
                    depth--;
                    break;
            case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
