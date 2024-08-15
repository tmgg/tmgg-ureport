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
package com.bstek.ureport.console.chart;

import com.bstek.ureport.cache.CacheUtils;
import com.bstek.ureport.chart.ChartData;
import com.bstek.ureport.console.BaseAction;
import com.bstek.ureport.utils.UnitUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jacky.gao
 * @since 2017年6月30日
 */
@RestController
@RequestMapping("ureport/chart")
public class ChartAction extends BaseAction {


    @RequestMapping("storeData")
    public void storeData(String _chartId, String _base64Data, Integer _width, Integer _height) {
        ChartData chartData = CacheUtils.getChartData(_chartId);
        if (chartData == null) {
            return;
        }
        String prefix = "data:image/png;base64,";
        if (_base64Data != null) {
            if (_base64Data.startsWith(prefix)) {
                _base64Data = _base64Data.substring(prefix.length(), _base64Data.length());
            }
        }
        chartData.setBase64Data(_base64Data);
        chartData.setHeight(UnitUtils.pixelToPoint(_height));
        chartData.setWidth(UnitUtils.pixelToPoint(_width));
    }

}
