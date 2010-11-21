package com.thomasbarker.translateant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class GoogleTranslate {

	static List<String> translate( String apiKey, List<String> messages, Locale to, Locale from ) {
		List<String> translated = new ArrayList<String>();
		Stack<String> messageStack = new Stack<String>();
		messageStack.addAll(cover( messages ) );
		while( !messageStack.isEmpty() ) {
			translated.addAll( uncover ( fromGoogleTranslateJSON(
				callGoogle(
					apiKey,
					from.getLanguage(),
					to.getLanguage(),
					messageStack
					)
				)
			) );
		}

		return translated;
	}

	private static List<String> cover( List<String> messages ) {
		ArrayList<String> results = new ArrayList<String>();
		for( String message : messages ) {
			results.add( message.replaceAll( "(?:[^'])\\{([^\\}]+)(?:[^'])\\}", "<div class=\"magicKtoKen\" style=\"$1\" />" ) );
		}
		return results;
	}

	private static List<String> uncover( List<String> translations ) {
		ArrayList<String> results = new ArrayList<String>();
		for( String translation : translations ) {
			results.add( translation.replaceAll("<div class=\"magicKtoKen\" style=\"([^\"]+)\" />", "{$1}" ) );
		}

		return results;
	}

	private static List<String> fromGoogleTranslateJSON( String raw ) {
		List<String> texts = new ArrayList<String>();
		try {
			JSONObject json = new JSONObject( raw );
			JSONArray jsonTexts = json.getJSONObject( "data" ).getJSONArray( "translations" );
			for( int i = 0; i != jsonTexts.length(); i++ ) {
				String text = jsonTexts.getJSONObject( i ).getString( "translatedText" );
				texts.add( text );
			}
			return texts;
		} catch ( JSONException je ) {
			throw new RuntimeException( je );
		}
	}

	private static String callGoogle( String apiKey, String from, String to, Stack<String> texts ) {

		StringBuilder url = new StringBuilder( "https://www.googleapis.com/language/translate/v2" );
		try {
			// Basic parameteres
			url.append( "?format=html" );
			url.append( "&key=" + apiKey );
			url.append( "&source=" + from );
			url.append( "&target=" + to );

			// Assemble the batch to translate
			while( !texts.isEmpty() &&
					3 + url.length() + URLEncoder.encode( texts.peek(), "UTF-8" ).length()
					< 2048
				)
			{
				url.append( "&q=" + URLEncoder.encode( texts.pop(), "UTF-8" ) );
			}

			// Request translation
			URL googleTranslate = new URL( url.toString() );

			// Collect translations
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							googleTranslate.openStream(),
							"UTF-8"
							)
					);
			StringBuilder data = new StringBuilder();
			String inputLine;
			while ( ( inputLine = in.readLine() ) != null ) {
		    	data.append( inputLine );
			}
			in.close();

			return data.toString();

		} catch ( MalformedURLException mue ) {
			throw new RuntimeException( mue );
		} catch ( UnsupportedEncodingException uee)  {
			throw new RuntimeException( uee );
		} catch ( IOException ioe ) {
			throw new RuntimeException( ioe );
		}
	}

}
