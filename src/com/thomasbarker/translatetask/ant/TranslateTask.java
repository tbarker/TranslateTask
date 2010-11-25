package com.thomasbarker.translatetask.ant;

import java.io.File;
import java.util.Arrays;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.thomasbarker.translatetask.GoogleTranslate;
import com.thomasbarker.translatetask.TranslateProperties;
import com.thomasbarker.translatetask.TranslateProperties.PropEncoding;

public final class TranslateTask extends Task {

	private String   googleApiKey;
	private String   sourceLanguage;
	private String[] targetLanguages;
	private String[] skippedLanguages = new String[0];

	private String magicProperty;

	private PropEncoding targetEncoding = PropEncoding.ASCII;

	private File	targetPropsDir;
    private FileSet	fileset;

	@Override
	public void execute() throws BuildException {

		// Scan for source files to translate
		DirectoryScanner ds = fileset.getDirectoryScanner();
		ds.scan();

		// Gather up source files
		File[] sourcePropsFiles = new File[ ds.getIncludedFiles().length ];
		for( int i = 0; i != sourcePropsFiles.length; i++ ) {
			sourcePropsFiles[ i ]
				= new File( this.fileset.getDir().toURI().resolve( ds.getIncludedFiles()[ i ] ) );
		}

		TranslateProperties.go(
			this.googleApiKey,
			sourceLanguage,
			targetLanguages,
			skippedLanguages,
			sourcePropsFiles,
			targetPropsDir,
			targetEncoding,
			magicProperty
		);

	}

	public void addConfiguredFileset ( FileSet fileset ) {
        this.fileset = fileset;
    }
	public void setDest( File dest ) {
		this.targetPropsDir = dest;
	}
	public final void setMagicProperty( String magicProperty ) {
		this.magicProperty = magicProperty;
	}
	public void setKey( String googleApiKey ) {
		this.googleApiKey = googleApiKey;
	}
	public void setFrom( String sourceLanguage ) {
		this.sourceLanguage = sourceLanguage;
	}
	public void setTo( String targetLanguages ) {
		// If we're not given languages, use all of them.
		this.targetLanguages = ( null != targetLanguages
			? targetLanguages.split( "," )
			: GoogleTranslate.ALL_TRANSLATABLE_LANGUAGES
		);
	}
	public void setSkip( String skippedLanguages ) {
		this.skippedLanguages = skippedLanguages.split( "," );
		Arrays.sort( this.skippedLanguages  );
	}
	public final void setTargetEncoding( PropEncoding targetEncoding ) {
		this.targetEncoding = targetEncoding;
	}

}
