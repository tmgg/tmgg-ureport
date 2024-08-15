/*******************************************************************************
 * Copyright 2017 Bstek
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.bstek.ureport.parser.impl.value;

import com.bstek.ureport.definition.value.Slash;
import com.bstek.ureport.definition.value.SlashValue;
import com.bstek.ureport.definition.value.Value;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;


public class SlashValueParser extends ValueParser {
    @Override
    public Value parse(Element element) {
        SlashValue value = new SlashValue();
        List<Slash> slashes = new ArrayList<Slash>();
        value.setSlashes(slashes);
        for (Object obj : element.elements()) {
            if (obj == null || !(obj instanceof Element)) {
                continue;
            }
            Element ele = (Element) obj;
            if (ele.getName().equals("slash")) {
                Slash slash = new Slash();
                slashes.add(slash);
                slash.setDegree(Integer.valueOf(ele.attributeValue("degree")));
                slash.setX(Integer.valueOf(ele.attributeValue("x")));
                slash.setY(Integer.valueOf(ele.attributeValue("y")));
                slash.setText(ele.attributeValue("text"));
            } else if (ele.getName().equals("base64-data")) {
                String prefix = "data:image/png;base64,";
                String base64Data = ele.getText();
                if (base64Data.startsWith(prefix)) {
                    base64Data = base64Data.substring(prefix.length(), base64Data.length());
                }
                value.setBase64Data(base64Data);
            }
        }
        return value;
    }

    @Override
    public void convert(Object o, Element el) {
        SlashValue value = (SlashValue) o;

        for (Slash slash : value.getSlashes()) {
            Element child = DocumentHelper.createElement("slash");
            child.addAttribute("degree", String.valueOf(slash.getDegree()));
            child.addAttribute("x", String.valueOf(slash.getX()));
            child.addAttribute("y", String.valueOf(slash.getY()));
            child.addAttribute("text", slash.getText());

            el.add(child);
        }

		String base64 = value.getBase64Data();
		if(base64 != null){
			Element child = DocumentHelper.createElement("base64-data");
			child.setText(base64);
			el.add(child);
		}
    }
}
