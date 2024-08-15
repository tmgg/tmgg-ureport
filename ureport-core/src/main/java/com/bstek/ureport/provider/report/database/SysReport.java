package com.bstek.ureport.provider.report.database;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
public class SysReport {

    String id;

    String file;


    String content;

    Date updateTime;
}
