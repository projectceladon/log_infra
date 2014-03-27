/* Phone Doctor - parsing
 *
 * Copyright (C) Intel 2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Nicolas BENOIT <nicolasx.benoit@intel.com>
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
