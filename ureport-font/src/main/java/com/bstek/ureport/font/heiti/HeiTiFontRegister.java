package com.bstek.ureport.font.heiti;

import com.bstek.ureport.export.pdf.font.FontRegister;
import org.springframework.stereotype.Component;

/**
 * @author Jacky.gao
 * @since 2014年5月7日
 */
@Component
public class HeiTiFontRegister implements FontRegister {

	public String getFontName() {
		return "黑体";
	}

	public String getFontPath() {
		return "com/bstek/ureport/font/heiti/SIMHEI.TTF";
	}
}
