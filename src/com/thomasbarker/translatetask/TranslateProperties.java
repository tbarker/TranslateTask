package com.thomasbarker.translatetask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class TranslateProperties {

	private static Log logger = LogFactory.getLog( TranslateProperties.class );

	public static void go(
		String googleApiKey,

		String   sourceLanguage,
		String[] targetLanguages,
		String[] skippedLanguages,

		File[] sourcePropsFiles,
		File   targetPropsDir,
		PropEncoding targetEncoding,

		String magicProperty
	)
	{
		for( File sourcePropsFile : sourcePropsFiles ) {
			try {
				// Let's fetch our source messages
				Properties sourceProps = new Properties();
				InputStream sourceStream = sourcePropsFile.toURI().toURL().openStream();
				if( sourcePropsFile.getName().endsWith( ".xml" ) ) {
					sourceProps.loadFromXML( sourceStream );
				} else {
					sourceProps.load( sourceStream );
				}

				logger.info( String.format(
					"Starting translation of %s from %s.", sourcePropsFile.getName(), sourceLanguage ) );
				
				// Try to find a memory file in the build directory
				Properties memoryProps = new Properties();
				File memoryFile = new File(
				targetPropsDir.toURI().resolve( sourcePropsFile.getName() + ".translatetask.memory" ) );
				if( memoryFile.exists() ) {
					memoryProps.load( memoryFile.toURI().toURL().openStream() );
				}

				// Then translate into each target language
				for( String lang : targetLanguages ) {

					// Skip the target and skipped languages
					if( 0 < Arrays.binarySearch( skippedLanguages, lang )
						|| lang.equals( sourceLanguage ) )
					{
						continue;
					}

					Properties targetProps = new Properties();

					// Don't start from scratch if there are existing translations
					File targetFile = new File( targetPropsDir.toURI().resolve( deriveFileName( sourcePropsFile, lang, targetEncoding ) ) );
					if( targetFile.exists() ) {
						if( targetEncoding.equals( PropEncoding.XML ) ) {
							targetProps.loadFromXML( targetFile.toURI().toURL().openStream() );
						} else {
							targetProps.load( new FileReader( targetFile ) );
						}
					}

					// Find entirely missing translations
					Properties missingProps = new Properties();
					for( Entry<Object, Object> entry : sourceProps.entrySet() ) {
						if( !targetProps.containsKey( entry.getKey() ) ) {
							missingProps.put( entry.getKey(), entry.getValue() );
						}
					}

					// Add ones that have changed (refers to memory file)
					for( Entry<Object, Object> entry : memoryProps.entrySet() ) {
						if( sourceProps.containsKey( entry.getKey() )
								&& !entry.getValue().equals(
									sourceProps.get( entry.getKey() ) ) )
						{
							missingProps.put( entry.getKey() , sourceProps.get( entry.getKey() ) );
						}
					}

					// Collate raw text strings for Google
					List<String> messages = new ArrayList<String>();
					for( Entry<Object, Object> entry : missingProps.entrySet() ) {
						messages.add( entry.getValue().toString() );
						logger.info( String.format( "Translating: '%s'", entry.getValue().toString( ) ) );
					}

					// Push through Google
					List<String> translated = GoogleTranslate.translate(
						googleApiKey,
						messages,
						new Locale( lang ),
						new Locale( sourceLanguage )
					);

					// Add the new translations
					int pos = missingProps.size();
					for( Entry<Object, Object> key : missingProps.entrySet() ) {
						targetProps.put( key.getKey(), translated.get( --pos ) );
					}

					// Prune deleted messages from target language
					for( Iterator<Object> i = targetProps.keySet().iterator(); i.hasNext(); ) {
						if( !sourceProps.containsKey( i.next() ) ) {
							i.remove();
						}
					}

					// Add a magic marker if needed
					if( null != magicProperty ) {
						targetProps.put( magicProperty, "" );
					}

					// And write the target out
					FileOutputStream out = new FileOutputStream( targetFile );
					String comment = "Auto translated by Google.";
					if( targetEncoding.equals( PropEncoding.ASCII ) ) {
						targetProps.store( out, comment );
					} else {
						targetProps.storeToXML( out, comment );
					}

					// Replace the memory file (if any) with the source props
					memoryFile.createNewFile();
					sourceProps.store(
						new FileOutputStream( memoryFile ),
						"TranslateTask Memory File"
					);

				}

			} catch ( MalformedURLException mue ) {
				throw new RuntimeException( mue );
			} catch ( IOException ioe ) {
				throw new RuntimeException( ioe );
			}
		}
	}

	public static enum PropEncoding {
		ASCII, XML
	}

	private static String deriveFileName( File originFile, String targetLanguage, PropEncoding targetEncoding ) {
		String end = targetEncoding.equals( PropEncoding.ASCII ) ? "properties" : "xml";
		String[] fileNameParts = originFile.getName().split( "(_[a-z][a-z])?(_[a-z][a-z])?\\.") ;
		return fileNameParts[0] + "_" + targetLanguage + "." + end;
	}
}