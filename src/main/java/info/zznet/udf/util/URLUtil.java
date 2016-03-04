package info.zznet.udf.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class URLUtil {
	public static String getRegList(String adid, String reg) {
		StringBuilder sb = new StringBuilder();
		String[] regs = reg.split(",");
		for (String r : regs) {
			String[] regSplit = r.split("\\{\\}");
			if (regSplit.length == 2 && regSplit[1].equals(adid)) {
				sb.append(regSplit[0] + ",");
			}
		}
		if (sb.length() > 0) {
			return sb.substring(0, sb.length() - 1);
		}
		return null;
	}
	public static String getKeyword(String url,String reg) throws Exception{
		Pattern queryPatternkeyword1 = Pattern.compile(reg);
		Matcher matcherkeword = queryPatternkeyword1.matcher(url);
		String keywords=null;
		if (matcherkeword.find()) {
			try {
				String keywordUTF8="",keywordGB2312="";
				String[] tmp=null;
				keywords = matcherkeword.group();
				keywordUTF8 = URLDecoder.decode(keywords, "utf-8");
				keywordGB2312 = URLDecoder.decode(keywords, "GB2312");
				if(Charset.forName("GB2312").newEncoder().canEncode(keywordGB2312)){
					tmp = keywordGB2312.split("=", 2);
				}else{
					tmp = keywordUTF8.split("=", 2);
				}
				if (tmp.length == 2) {
					tmp[1] = tmp[1].replace("\t", "").replace("\r\n", "");
					return tmp[1];
				} else
					return null;
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				//System.out.println("refer url keyword encoder error:"+url);
				return keywords.split("=",2)[1];
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				return keywords.split("=",2)[1];
			}catch(Exception e){
				
			}
		}
		return null;
	}
	public static String[] splitReg(String reg){
		if(reg!=null&&!reg.equals("")){
			return reg.split("\\{\\}");
		}else{
			return null;
		}
	}

	public static String getKeywordForRegs(String url, String regs) throws Exception {
		String keyword = "";
		for (String r : URLUtil.splitReg(regs)) {
			keyword = URLUtil.getKeyword(url, r);
			if (keyword != null && !keyword.equals(""))
				return keyword;
		}
		return null;
	}
	public static String pickArgsToURL(String url,String argReg){
		if(url==null||url.equals("")) return null;
		String reg=argReg;
		String ltr=null;
		Pattern queryPatternkeyword1 = Pattern.compile(reg);
		Matcher matcherkeword = queryPatternkeyword1.matcher(url);
		String ltrURL="";
		try {
			if (matcherkeword.find()) {
				ltr = matcherkeword.group();
				if (ltr == null || ltr.trim().equals(""))return null;
				ltrURL = ltr.split("=")[1];
				if (ltrURL != null && !ltrURL.equals("")) {
					return URLDecoder.decode(ltrURL, "UTF-8");
				} else {
					return null;
				}
			} else {
				return null;
			}
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			//System.out.println("refer url keyword encoder error:"+url);
			return "";
		}catch(UnsupportedEncodingException ue){
			//System.out.println("refer url keyword encoder error:"+url);
			return "";
		}catch(ArrayIndexOutOfBoundsException ae){
			//System.out.println("refer url keyword encoder error:"+url);
			return "";
		}catch (IllegalArgumentException ie) {
			// TODO Auto-generated catch block
			//System.out.println("refer url keyword encoder error:"+url);
			return "";
		}
	}
}
