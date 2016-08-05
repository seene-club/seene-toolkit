package org.seeneclub.toolkit;


// Seene-ToolKit Constants
public interface STK {
	public static final int WORK_WIDTH = 240;
	public static final int WORK_HEIGHT = 240;
	public static final float INIT_DEPTH = 0.4f;
	public static final String DEFAULT_CAPTION = "#synthetic (created with https://github.com/seene-club/seene-toolkit)";
	public static final String DEFAULT_AVATAR = "https://seene.co/images/b503611e.AvatarDefault@2x.png";
	
	public static final int NUMBER_OF_DOWNLOAD_THREADS = 4;
	public static final int UNDO_RINGBUFFER_SIZE = 15;
	
	public static final String AWS_INSTANCE_IP_ADDRESS = "54.243.113.182";
	
	public static final int CALCULATION_METHOD_STK_PRESERVE = 1;
	public static final int CALCULATION_METHOD_GOOGLE_RANGELINEAR = 2;
	
	public static final String SEENE_MODEL = "scene.oemodel";
	public static final String SEENE_TEXTURE = "poster.jpg";
	
	public static final String XMP_ORIGINAL_JPG = "poster_original.jpg";
	public static final String XMP_DEPTH_PNG = "poster_depth.png";
	public static final String XMP_COMBINED_JPG = "poster_xmp.jpg";
	
	public static final int POPUP_POSITION_GENERATE_XMP = 3;
	public static final int POPUP_POSITION_LOAD_TEXTURE_POSTER = 5;
	public static final int POPUP_POSITION_LOAD_TEXTURE_POSTER_ORIGINAL = 6;
	public static final int POPUP_POSITION_LOAD_TEXTURE_XMP = 7;
	public static final int POPUP_POSITION_LOAD_MODEL_OEMODEL = 8;
	public static final int POPUP_POSITION_LOAD_MODEL_PNG = 9;
	public static final int POPUP_POSITION_LOAD_MODEL_XMP = 10;
	
	public static final String CONFIG_API_ID_HINT = "<insert Seene API ID here>";
	public static final String CONFIG_AUTH_CODE_HINT = "<insert Seene Authorization Code here>";
	
	public static final String API_CLIENT_ID = "d802e04485019c1c472d976ef2f2723a888f06332ff397dd73c68f738684b3ed";
	public static final String API_CLIENT_PUBLIC_KEY = "f 6xrfpew~as3ua{gsrcw3yrcrcu#2zg  f dpp5 6 qg'h'qkp1 vav4ws7{d#";
	public static final String API_REDIRECT = "urn:ietf:wg:oauth:2.0:oob";
	public static final String API_CLIENT_KEY = "sclient";
	
	public static final String AUTHORIZE_URL = "https://api.seene.co/oauth/authorize?client_id=" + API_CLIENT_ID + "&redirect_uri=" + API_REDIRECT + "&response_type=code&scope=public+write";
	
	public static final String LINE_FEED = "\r\n";
}
