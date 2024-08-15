package com.bstek.ureport.definition;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Paper implements Serializable{
	private static final long serialVersionUID = -6153150083492704136L;
	private int leftMargin=90;
	private int rightMargin=90;
	private int topMargin=72;
	private int bottomMargin=72;
	private PaperType paperType;
	private PagingMode pagingMode;
	private int fixRows;
	private int width;
	private int height;
	private Orientation orientation;
	private HtmlReportAlign htmlReportAlign=HtmlReportAlign.left;
	private String bgImage;
	private boolean columnEnabled;
	private int columnCount=2;
	private int columnMargin=5;
	private int htmlIntervalRefreshValue=0;

}
