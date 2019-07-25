/* Copyright (C) 2019 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.parsing;

import java.util.ArrayList;
import java.util.List;
import com.intel.crashreport.core.ParsableEvent;

import android.content.res.AssetManager;

public class ParserDirector {

	private final List<ParserBuilder> builders = new ArrayList<ParserBuilder>();
	private final List<ParserBuilder> postBuilders = new ArrayList<ParserBuilder>();
	private final List<EventParser> parsers = new ArrayList<EventParser>();
	private final List<EventParser> postParsers = new ArrayList<EventParser>();

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
		PostProcessBuilder myPostBuilder = new PostProcessBuilder(aManager);
		postBuilders.add(myPostBuilder);
	}

	public int getParserCount(){
		return parsers.size() + postParsers.size();
	}

	public void generateParsers() {
		for (ParserBuilder curBuilder : builders) {
			for (EventParser genParser : curBuilder.getParsers()) {
				if (genParser != null) {
					parsers.add(genParser);
				}
			}

		}
		for (ParserBuilder curBuilder : postBuilders) {
			for (EventParser genParser : curBuilder.getParsers()) {
				if (genParser != null) {
					postParsers.add(genParser);
				}
			}
		}
	}

	public boolean parseEvent(ParsableEvent aEvent) {
		boolean result = false;

		for (EventParser curParser : parsers) {
			if (curParser == null)
				continue;
			if (curParser.isEventEligible(aEvent)) {
				if (curParser.parseEvent(aEvent)) {
					//only one successful parsing is allowed
					result = true;
					break;
				}
			}
		}
		//exec post processing on event
		for (EventParser curParser : postParsers) {
			if (curParser == null)
				continue;
			if (curParser.isEventEligible(aEvent)) {
				if (curParser.parseEvent(aEvent)) {
					//any number of post-processing is allowed
					result = true;
					continue;
				}
			}
		}
		return result;
	}
}
