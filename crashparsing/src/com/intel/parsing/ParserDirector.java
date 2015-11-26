/* INTEL CONFIDENTIAL
 * Copyright 2015 Intel Corporation
 *
 * The source code contained or described herein and all documents
 * related to the source code ("Material") are owned by Intel
 * Corporation or its suppliers or licensors. Title to the Material
 * remains with Intel Corporation or its suppliers and
 * licensors. The Material contains trade secrets and proprietary
 * and confidential information of Intel or its suppliers and
 * licensors. The Material is protected by worldwide copyright and
 * trade secret laws and treaty provisions. No part of the Material
 * may be used, copied, reproduced, modified, published, uploaded,
 * posted, transmitted, distributed, or disclosed in any way without
 * Intel's prior express written permission.
 *
 * No license under any patent, copyright, trade secret or other
 * intellectual property right is granted to or conferred upon you
 * by disclosure or delivery of the Materials, either expressly, by
 * implication, inducement, estoppel or otherwise. Any license under
 * such intellectual property rights must be express and approved by
 * Intel in writing.
 */

package com.intel.parsing;

import java.util.ArrayList;
import java.util.List;

import android.content.res.AssetManager;

public class ParserDirector {

	private final List<ParserBuilder> builders = new ArrayList<ParserBuilder>();
	private final List<EventParser> parsers = new ArrayList<EventParser>();

	public ParserDirector() {
	}

	public void initParserWithManager(AssetManager aManager){
		initBuilders(aManager);
		generateParsers();
	}

	public void initBuilders(AssetManager aManager){
		//load appropriate builders
		JsonBuilder myBuilder = new JsonBuilder(aManager);
		builders.add(myBuilder);
		//legacy builder should added at the end
		builders.add(new LegacyBuilder());
	}

	public int getParserCount(){
		return parsers.size();
	}

	public void generateParsers() {
		for (ParserBuilder curBuilder : builders) {
			while (curBuilder.hasNextParser()) {
				EventParser genParser = curBuilder.getNextParser();
				if (genParser != null) {
					parsers.add(genParser);
				}
			}
		}
	}

	public boolean parseEvent(ParsableEvent aEvent) {
		for (EventParser curParser : parsers) {
			if (curParser == null)
				continue;
			if (curParser.isEventEligible(aEvent)) {
				if (curParser.parseEvent(aEvent)) {
					//only one successful parsing is allowed
					return true;
				}
			}
		}
		//no parser found the event eligible or parse it successfully => false
		return false;
	}
}
