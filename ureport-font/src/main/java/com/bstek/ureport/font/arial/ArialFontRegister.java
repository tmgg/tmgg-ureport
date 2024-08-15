package com.bstek.ureport.font.arial;

import com.bstek.ureport.export.pdf.font.FontRegister;
import org.springframework.stereotype.Component;


/**
 * @author Jacky.gao
 * @since 2014年5月7日
 */
@Component
public class ArialFontRegister implements FontRegister {

	public String getFontName() {
		return "Arial";
	}

	public String getFontPath() {
		return "com/bstek/ureport/font/arial/ARIAL.TTF";
	}
}
