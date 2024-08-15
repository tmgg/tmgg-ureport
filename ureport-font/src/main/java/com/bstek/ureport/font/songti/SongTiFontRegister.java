package com.bstek.ureport.font.songti;

import com.bstek.ureport.export.pdf.font.FontRegister;
import org.springframework.stereotype.Component;

/**
 * @author Jacky.gao
 * @since 2014年5月7日
 */
@Component
public class SongTiFontRegister implements FontRegister {

	public String getFontName() {
		return "宋体";
	}

	public String getFontPath() {
		return "com/bstek/ureport/font/songti/SIMSUN.TTC";
	}
}
