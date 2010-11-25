package com.thomasbarker.translatetask.main;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.thomasbarker.translatetask.GoogleTranslate;
import com.thomasbarker.translatetask.TranslateProperties;
import com.thomasbarker.translatetask.TranslateProperties.PropEncoding;

public final class RunTranslateTool {

	@SuppressWarnings("static-access")
	public static void main( String[] args ) {

		Options options = new Options();
		options.addOption( "k", "googleApiKey", true, "Google Translate API Key" );
		options.addOption(
			OptionBuilder
				.withLongOpt( "sourceLanguage" )
				.withArgName( "ISO 639-1" )
				.hasArg()
				.isRequired( true )
				.withDescription( "Source langauge" )
			.create( "s" )
		);		
		options.addOption(
			OptionBuilder
				.withLongOpt( "targetLanguages" )
				.withArgName( "ISO 639-1" )
				.hasArgs()
				.isRequired( false )
				.withDescription( "Target languages.  Omit to translate to all supported languages." )
			.create( "t" )
		);
		options.addOption(
			OptionBuilder
				.withLongOpt( "skipped" )
				.withArgName( "ISO 639-1" )
				.hasArgs()
				.isRequired( false )
				.withDescription( "Skip languages " )
			.create()
		);
		options.addOption(
			OptionBuilder
				.withLongOpt( "sourcePropsFile" )
				.hasArg()
				.isRequired( true )
				.withDescription( "Source properties files" )
				.withType( File.class )
			.create( "f" )
		);
		options.addOption(
			OptionBuilder
				.withLongOpt( "targetPropsDir" )
				.hasArg()
				.isRequired( true )
				.withDescription( "Target director for output files" )
				.withType( File.class )
			.create( "o" )
		);
		options.addOption( "e", "encoding", true, "Output encoding, either ASCII or XML." );
		options.addOption( "m", "magicProperty", true, "Magic property key to mark autotranslated output" );
		options.addOption( "h", false, "Print this message" );

		// Create the parser
		CommandLineParser parser = new PosixParser();
		try {
			// Parse the command line arguments
			CommandLine cmd = parser.parse( options, args );

			String	 googleApiKey = cmd.getOptionValue( "k" );
			String   sourceLanguage = cmd.getOptionValue( "s" );
			String[] targetLanguages = cmd.getOptionValues( "t" );
			String[] skippedLanguages = cmd.getOptionValues( "skipped" );
			File	 sourcePropsFile = (File) cmd.getParsedOptionValue( "f" );
			File	 targetPropsDir = (File) cmd.getParsedOptionValue( "o" );
			String	 targetEncoding = cmd.getOptionValue( "e" );
			String	 magicProperty = cmd.getOptionValue( "m" );

			// Handle defaults
			if( null == skippedLanguages ) { skippedLanguages = new String[0]; };
			if( null == targetLanguages ) { targetLanguages = GoogleTranslate.ALL_TRANSLATABLE_LANGUAGES; };

			// Do the translation
			TranslateProperties.go(
				googleApiKey,
				sourceLanguage,
				targetLanguages,
				skippedLanguages,
				new File[] { sourcePropsFile },
				targetPropsDir,
				(null == targetEncoding ? PropEncoding.ASCII : PropEncoding.valueOf( targetEncoding ) ),
				magicProperty
			);

		} catch ( ParseException pe ) {
			// Generate the help statement
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "translate-tool", options );
			System.exit( 0 );
		}
		
	}

}
