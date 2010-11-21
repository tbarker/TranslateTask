package com.thomasbarker.translateant;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;


public final class TranslateTask extends MatchingTask {

	private String GoogleApiKey;
	private String sourceLanguage;
	private String targetLanguages;

	private String magicProperty;

	private File dest;
    private File dir;

	@Override
	public void execute() throws BuildException {

		fileset.setDir( getProject().getBaseDir() );
		DirectoryScanner ds = getDirectoryScanner( dir );
		ds.scan();

		for( String propsFileName : ds.getIncludedFiles() ) {
			try {
				// Let's fetch our source messages
				Properties sourceProps = new Properties();
				sourceProps.load( fileset.getDir( getProject() ).toURI().resolve( propsFileName ).toURL().openStream() );
	
				// Then translate into each target language
				for( String langString : this.targetLanguages.split( "," ) ) {
	
					Properties targetProps = new Properties();
					
					// Don't start from scratch if there are existing translations
					File targetFile = new File( dest.toURI().resolve( deriveFileName(propsFileName, langString ) ) );
					if( targetFile.exists() ) {
						log( String.format( "We already have some %s translations.", langString ) );
						targetProps.load( new FileReader( targetFile ) );
					}
	
					// Find missing translations
					Properties missingProps = new Properties();
					for( Entry<Object, Object> entry : sourceProps.entrySet() ) {
						if( !targetProps.containsKey( entry.getKey() ) ) {
							missingProps.put( entry.getKey(), entry.getValue() );
						}
					}

					// Collate raw text strings for Google
					List<String> messages = new ArrayList<String>();
					for( Entry<Object, Object> entry : missingProps.entrySet() ) {
						messages.add( (String) entry.getValue().toString() );
					}

					// Push through Google
					List<String> translated = GoogleTranslate.translate(
						this.GoogleApiKey,
						messages,
						new Locale( langString ),
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
					if( null != this.magicProperty ) {
						targetProps.put( this.magicProperty, "" );
					}

					// And write the target out
					FileOutputStream out = new FileOutputStream( targetFile );
					targetProps.store( out, "Auto translated by Google." );
				}

			} catch ( MalformedURLException mue ) {
				throw new RuntimeException( mue );
			} catch ( IOException ioe ) {
				throw new RuntimeException( ioe );
			}
		}
	}

	private String deriveFileName( String originFileName, String targetLanguage ) {
		String[] fileNameParts = originFileName.split( "(_[a-z][a-z])?\\." );
		return fileNameParts[0] + "_" + targetLanguage + ".properties";
	}

    public void setDir (File dir) {
        this.dir = dir;
    }
	public void setDest( File dest ) {
		this.dest = dest;
	}
	public final void setMagicProperty( String magicProperty ) {
		this.magicProperty = magicProperty;
	}
	public void setGoogleApiKey( String googleApiKey ) {
		GoogleApiKey = googleApiKey;
	}
	public void setSourceLanguage( String sourceLanguage ) {
		this.sourceLanguage = sourceLanguage;
	}
	public void setTargetLanguages( String targetLanguages ) {
		this.targetLanguages = targetLanguages;
	}

}
