/*
  This is a part of SubNet Scout source.
  SubNet Scout is a fork of Angry IP Scanner (https://angryip.org), licensed under GPLv2.
  Original copyright © Anton Keks and contributors. See NOTICE file for details.
 */
package net.azib.ipscan.config;

import java.util.jar.JarFile;
import java.util.logging.Level;

/**
 * Class with accessors to version information of the program.
 *
 * @author Anton Keks
 */
public class Version {
	public static final String NAME = "SubNet Scout";
	
	public static final String COPYLEFT = "© 2026 SubNet Scout contributors. Based on Angry IP Scanner, © Anton Keks and contributors";
	
	public static final String OWN_HOST = "subnetscout.example.com"; // TODO: заменить на реальный домен

	public static final String WEBSITE = "https://" + OWN_HOST;

	public static final String FAQ_URL = WEBSITE + "/faq/";

	public static final String PRIVACY_URL = WEBSITE + "/privacy.html";

	public static final String FULL_LICENSE_URL = "https://www.gnu.org/licenses/gpl-2.0.html";

	public static final String PLUGINS_URL = WEBSITE + "/plugins/";
	
	public static final String DOWNLOAD_URL = WEBSITE + "/download/";

	public static final String ISSUES_URL = WEBSITE + "/issues/";

	public static final String IP_LOCATE_URL = WEBSITE + "/iplocate";

	public static final String LATEST_VERSION_URL = WEBSITE + "/SUBNETSCOUT.VERSION";

	// Google Analytics отключена в форке — используйте свои ключи, если нужна телеметрия,
	// и обязательно раскройте это в privacy policy при публикации в Store.
	public static final String GA_ID = "";
	public static final String GA_SECRET = "";

	private static String version;
	private static String buildDate;
	
	/**
	 * @return version of currently running Angry IP Scanner (retrieved from the jar file)
	 */
	public static String getVersion() {
		if (version == null) {
			loadVersionFromJar();
		}
		return version;
	}
	
	/**
	 * @return build date of currently running Angry IP Scanner  (retrieved from the jar file)
	 */
	public static String getBuildDate() {
		if (buildDate == null) {
			loadVersionFromJar();
		}
		return buildDate;
	}

	private static void loadVersionFromJar() {
		try {
			var path = Version.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			if (path.endsWith(".jar") || path.endsWith(".exe")) {
				var jarFile = new JarFile(path);
				var attrs = jarFile.getManifest().getMainAttributes();
				version = attrs.getValue("Version");
				buildDate = attrs.getValue("Build-Date");
				return;
			}
		}
		catch (Exception e) {
			LoggerFactory.getLogger().log(Level.WARNING, "Cannot obtain version", e);
		}
		version = "current";
		buildDate = "today";
	}
	
	public static String getFullName() {
		return NAME + " " + getVersion();
	}
}
