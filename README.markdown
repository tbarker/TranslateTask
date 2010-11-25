Google Translate Task
======================

A simple ant task and runnable JAR for translating Java property files using Google Translate.  Just run 'ant dist' to get started.

You'll need a Google Translate API key from the Google API Console.

Remember that machine translated text is rarely "quite right", so you should flag this up to your user.  (And give Google Translate credit as per their T&Cs.)  Translate Task can add a magic property to generated output to distinguish it.


Useage
------

'''''''''''''''''''''''''''''''''''''''

    <taskdef name="translate" classname="com.thomasbarker.translatetask.ant.TranslateTask" classpath="dist/translate-task.jar" />
    <translate from="en" to="de" key="${translate.googleapikey}"
        dest="test" targetEncoding="XML" magicProperty="magic.autotranslate">
        <fileset dir="test">
            <include name="**/test.properties" />
        </fileset>
    </translate>

'''''''''''''''''''''''''''''''''''''''

Alternatively...

'''''''''''''''''''''''''''''''''''''''

$ java -jar translate-task.jar -h
usage: translate-tool
 -e,--encoding <arg>                Output encoding, either ASCII or XML.
 -f,--sourcePropsFile <arg>         Source properties files
 -h                                 Print this message
 -k,--googleApiKey <arg>            Google Translate API Key
 -m,--magicProperty <arg>           Magic property key to mark
                                    autotranslated output
 -o,--targetPropsDir <arg>          Target director for output files
 -s,--sourceLanguage <ISO 639-1>    Source langauge
    --skipped <ISO 639-1>           Skip languages
 -t,--targetLanguages <ISO 639-1>   Target languages.  Omit to translate


'''''''''''''''''''''''''''''''''''''''


Future Work
-----------

+ Handle GNUText PO file
+ Rotating Google API keys (slightly evil)
+ Multiple source languages
+ Translate static content

