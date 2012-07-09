package org.openstack.atlas.common.crypto;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * CryptoUtil driver
 * @author avishayb@radware.com
 *
 */
public class CryptoTool {
	public static void main(String [] args) throws Exception {
		final CommandLineParser parser = new PosixParser();
		final Options options = getOptions();
		CommandLine cmd = null;
		try {
			cmd = parser.parse( options, args);			
		} catch(ParseException e) {
			System.err.println("Failed to parse command line: " + e.getMessage());
			showUsage(options);	
			System.exit(1);
		}

		if(cmd.hasOption(HELP) || args.length == 0) {
			showUsage(options);	
			System.exit(0);
		}
		if(cmd.hasOption(ENC)) {
			final String valueToEncrypt = cmd.getOptionValue(ENC); 
			final String encryptedValue = CryptoUtil.encrypt(valueToEncrypt);
			System.out.println("Encrypted value:" + encryptedValue);
			System.exit(0);
		}
		
		if(cmd.hasOption(DEC)) {
			final String valueToDecrypt = cmd.getOptionValue(DEC); 
			final String decypteddValue = CryptoUtil.decrypt(valueToDecrypt);
			System.out.println("Decrypted value:" + decypteddValue);
			System.exit(0);
		}
		System.exit(0);
	}

	private static void showUsage(final Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "atlas-crypto-driver", options );
	}
	
	@SuppressWarnings("static-access")
	private static Options getOptions() {
		Options options = new Options();
		Option encrypt   = OptionBuilder.withArgName( "string_to_encrypt" )
                .hasArg()
                .withDescription(  "encrypt the input" )
                .create( ENC );
		options.addOption(encrypt);
		
		Option decrypt   = OptionBuilder.withArgName( "string_to_decrypt" )
                .hasArg()
                .withDescription(  "decrypt the input" )
                .create( DEC );
		options.addOption(encrypt);
		options.addOption(decrypt);
		options.addOption(new Option( HELP, "print this message" ));
		return options;
	}
	
	private static final String ENC = "enc";
	private static final String DEC = "dec";
	private static final String HELP = "help";

}
