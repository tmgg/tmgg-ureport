package com.bstek.ureport.font.fangsong;

import com.bstek.ureport.export.pdf.font.FontRegister;
import org.springframework.stereotype.Component;

/**
 * @author Jacky.gao
 * @since 2014年5月7日
 */
@Component
public class FangSongFontRegister implements FontRegister {

	public String getFontName() {
		return "仿宋";
	}

	public String getFontPath() {
		return "com/bstek/ureport/font/fangsong/SIMFANG.TTF";
	}
}
