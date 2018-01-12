package com.nld.starpos.wxtrade.local.db.bean;

public class ScanParams {


	private String tagname;//参数名称
	private String tagval;//参数值
	public ScanParams(){}
	public ScanParams(String tagname, String tagval){
		this.tagname = tagname;
		this.tagval = tagval;
	}
	public String getTagname() {
		return tagname==null?"":tagname;
	}
	public void setTagname(String tagname) {
		if (tagname != null) {
			this.tagname = tagname.trim();
		}
	}
	public String getTagval() {
		return tagval==null?"":tagval;
	}
	public void setTagval(String tagval) {
		if (tagval != null) {
			this.tagval = tagval.trim();
		}
	}
}