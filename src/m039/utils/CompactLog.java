/*
* Copyright (C) 2011 Mozgin Dmitry
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package m039.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

/**
 * Describe class CompactLog here.
 *
 *
 * Created: Wed May 25 14:21:23 2011
 *
 * @author <a href="mailto:flam44@gmail.com">Mozgin Dmitry</a>
 * @version 1.0
 */
public class CompactLog {
    private static Pattern	stPattern = Pattern.compile("^(.*)\\<(\\d+)\\>$", Pattern.DOTALL);

    private List<String>	mContent;
	
    public CompactLog() {
	mContent	= new ArrayList<String>();
    }
    
    /**
     * Adds a new string to the log. 
     *
     * @param str a string to add
     * @param newLine if add newline to the string
     * @return 'this' to construct a chain of methods
     */
    public CompactLog append(String str, boolean newLine) {
	String lastInserted;
	String strToInsert;

	if (!newLine) {
	    newLine = str.endsWith("\n");
	}

	strToInsert = str.substring(0, str.lastIndexOf("\n"));
	    
	if (mContent.size() > 0) {
	    lastInserted = mContent.get(mContent.size() - 1);

	    if (lastInserted.indexOf(strToInsert) == 0) {
		Matcher m = stPattern.matcher(lastInserted);
		    
		if (m.find()) {
		    // The last insertion has a number count
		    int count = Integer.parseInt(m.group(2));
		    strToInsert = m.group(1) + "<" + (count + 1) + ">";
		} else {
		    // The last inserted string hasn't a number count
		    strToInsert += " <2>";
		}

		mContent.remove(mContent.size() - 1);
	    }
	}

	if (newLine) {
	    strToInsert += '\n';
	}

	mContent.add(strToInsert);

	return this;
    }

    public CompactLog appendln(String str) {
	return append(str, true);
    }

    public CompactLog append(String str) {
	return append(str, false);
    }

    public String toString() {
	StringBuilder sb = new StringBuilder();
	
	for (String s: mContent) {
	    sb.append(s);
	}

	return sb.toString();
    }
}
