package com.nld.cloudpos.payment.socket;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;


public class ErrInfoHandler extends DefaultHandler {

	private String Tag_error_info = "error-info";
	private String Tag_item = "item";
	private String Tag_type = "type";
	private String Tag_errcode = "errcode";
	private String Tag_tip_info = "tip-info";
	
	private StringBuilder sb = new StringBuilder();
	private ErrInfo mErrInfo = null;
	private List<ErrInfo> errInfos = null;
	
	public List<ErrInfo> getErrInfos (){
		return errInfos;
	}
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		sb.append(ch, start, length);  
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		String value = sb.toString();
		if (Tag_item.equals(localName)) {
			errInfos.add(mErrInfo);
		}
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		sb.setLength(0);  
		if(Tag_error_info.equals(localName)) {  
			errInfos = new ArrayList<ErrInfo>();
		} else if (Tag_item.equals(localName)) {
			mErrInfo = new ErrInfo();
			mErrInfo.setType(attributes.getValue(Tag_type));
			mErrInfo.setErrcode(attributes.getValue(Tag_errcode));
			mErrInfo.setTip_info(attributes.getValue(Tag_tip_info));
		}
	}

}
