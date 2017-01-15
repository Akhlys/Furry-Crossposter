/*
 * (C) Copyright 2015 Boni Garcia (http://bonigarcia.github.io/)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package io.github.bonigarcia.wdm;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Generic manager.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public abstract class BrowserManager {

	protected static final Logger log = LoggerFactory
			.getLogger(BrowserManager.class);

	private static final String SEPARATOR = "/";
	private static final Architecture DEFAULT_ARCH = Architecture
			.valueOf("x" + System.getProperty("sun.arch.data.model"));
	private static final String MY_OS_NAME = getOsName();
	private static final String VERSION_PROPERTY = "wdm.driverVersion";

	public abstract List<URL> getDrivers() throws Exception;

	protected abstract String getExportParameter();

	protected abstract String getDriverVersion();

	protected abstract List<String> getDriverName();

	protected abstract URL getDriverUrl() throws MalformedURLException;

	protected String versionToDownload;

	public void setup() {
		setup(DEFAULT_ARCH, DriverVersion.NOT_SPECIFIED.name());
	}

	public void setup(String version) {
		setup(DEFAULT_ARCH, version);
	}

	public void setup(Architecture arch) {
		setup(arch, DriverVersion.NOT_SPECIFIED.name());
	}

	public void setup(Architecture arch, String version) {
		try {

			// Honor property if available (even when version is present)
			String driverVersion = getDriverVersion();
			if (!driverVersion.equalsIgnoreCase(DriverVersion.LATEST.name())
					|| version.equals(DriverVersion.NOT_SPECIFIED.name())) {
				version = driverVersion;
			}

			this.getClass().newInstance().manage(arch, version);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public void manage(Architecture arch, DriverVersion version) {
		manage(arch, version.name());
	}

	public String forceCache(String repository) {
		String driverInCache = null;
		for (String driverName : getDriverName()) {
			log.trace("Checking if {} exists in cache {}", driverName,
					repository);

			Collection<File> listFiles = FileUtils
					.listFiles(new File(repository), null, true);
			Object[] array = listFiles.toArray();
			Arrays.sort(array, Collections.reverseOrder());

			for (Object f : array) {
				driverInCache = f.toString();
				log.trace("Checking {}", driverInCache);
				if (driverInCache.contains(driverName)) {
					log.info("Found {} in cache: {} ", driverName,
							driverInCache);
					break;
				} else {
					driverInCache = null;
				}
			}

			if (driverInCache == null) {
				log.trace("{} do not exist in cache {}", driverName,
						repository);
			} else {
				break;
			}
		}
		return driverInCache;
	}

	public String existsDriverInCache(String repository, String driverVersion,
			Architecture arch) {

		String driverInCache = null;
		for (String driverName : getDriverName()) {
			log.trace("Checking if {} {} ({} bits) exists in cache {}",
					driverName, driverVersion, arch, repository);

			Iterator<File> iterateFiles = FileUtils
					.iterateFiles(new File(repository), null, true);

			while (iterateFiles.hasNext()) {
				driverInCache = iterateFiles.next().toString();

				// Exception for phantomjs
				boolean architecture = driverName.equals("phantomjs")
						|| driverInCache.contains(arch.toString());
				log.trace("Checking {}", driverInCache);

				if (driverInCache.contains(driverVersion)
						&& driverInCache.contains(driverName) && architecture) {
					log.debug("Found {} {} ({} bits) in cache: {} ",
							driverVersion, driverName, arch, driverInCache);
					break;
				} else {
					driverInCache = null;
				}
			}

			if (driverInCache == null) {
				log.trace("{} {} ({} bits) do not exist in cache {}",
						driverVersion, driverName, arch, repository);
			} else {
				break;
			}
		}
		return driverInCache;
	}

	public void manage(Architecture arch, String version) {
		try {
			boolean getLatest = version == null || version.isEmpty()
					|| version.equalsIgnoreCase(DriverVersion.LATEST.name())
					|| version.equalsIgnoreCase(
							DriverVersion.NOT_SPECIFIED.name());

			boolean forceCache = WdmConfig.getBoolean("wdm.forceCache");
			String driverInCache = null;
			if (forceCache) {
				driverInCache = forceCache(Downloader.getTargetPath());
			} else if (!getLatest) {
				versionToDownload = version;
				driverInCache = existsDriverInCache(Downloader.getTargetPath(),
						version, arch);
			}

			if (driverInCache != null) {
				System.setProperty(VERSION_PROPERTY, version);
				exportDriver(getExportParameter(), driverInCache);

			} else {

				// Get the complete list of URLs
				List<URL> urls = getDrivers();
				if (!urls.isEmpty()) {
					List<URL> candidateUrls;
					boolean continueSearchingVersion;

					do {
						// Get the latest or concrete version or Edge (the only
						// version that can be downloaded is the latest)
						if (getLatest || getDriverName()
								.contains("MicrosoftWebDriver")) {
							candidateUrls = getLatest(urls, getDriverName());
						} else {
							candidateUrls = getVersion(urls, getDriverName(),
									version);
						}
						if (versionToDownload == null) {
							break;
						}

						log.trace("All URLs: {}", urls);
						log.trace("Candidate URLs: {}", candidateUrls);

						
						// Filter by architecture and OS
						candidateUrls = filter(candidateUrls, arch);

						// Find out if driver version has been found or not
						continueSearchingVersion = candidateUrls.isEmpty()
								&& getLatest;
						if (continueSearchingVersion) {
							log.debug("No valid binary found for {} {}",
									getDriverName(), versionToDownload);
							urls = removeFromList(urls, versionToDownload);
							versionToDownload = null;
						}

					} while (continueSearchingVersion);

					if (candidateUrls.isEmpty()) {
						String versionStr = getLatest ? "(latest version)"
								: version;
						String errMessage = getDriverName() + " " + versionStr
								+ " for " + MY_OS_NAME + arch.toString()
								+ " not found in " + getDriverUrl();
						log.error(errMessage);
						throw new RuntimeException(errMessage);
					}

					for (URL url : candidateUrls) {
						String export = candidateUrls.contains(url)
								? getExportParameter() : null;
						System.setProperty(VERSION_PROPERTY, versionToDownload);
						Downloader.download(url, versionToDownload, export,
								getDriverName());
					}
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<URL> filter(List<URL> list, Architecture arch) {

		log.trace("{} {} - URLs before filtering: {}", getDriverName(),
				versionToDownload, list);

		List<URL> out = new ArrayList<URL>();

		// Round #1 : Filter by OS
		for (URL url : list) {
			for (OperativeSystem os : OperativeSystem.values()) {
				if (((MY_OS_NAME.contains(os.name())
						&& url.getFile().toLowerCase().contains(os.name()))
						|| getDriverName().equals("IEDriverServer")
						|| (IS_OS_MAC
								&& url.getFile().toLowerCase().contains("osx")))
						&& !out.contains(url)) {
					out.add(url);
				}
			}
		}

		log.trace("{} {} - URLs after filtering by OS ({}): {}",
				getDriverName(), versionToDownload, MY_OS_NAME, out);

		// Round #2 : Filter by architecture (32/64 bits)
		if (out.size() > 1 && arch != null) {
			for (URL url : list) {
				// Exception: 32 bits (sometimes referred as x86)
				if (arch == Architecture.x32 && url.getFile().contains("x86")) {
					continue;
				}

				if (!url.getFile().contains(arch.toString())) {
					out.remove(url);
				}
			}
		}

		log.trace("{} {} - URLs after filtering by architecture ({}): {}",
				getDriverName(), versionToDownload, arch, out);

		return out;
	}

	public List<URL> removeFromList(List<URL> list, String version) {
		List<URL> out = new ArrayList<URL>(list);
		for (URL url : list) {
			if (url.getFile().contains(version)) {
				out.remove(url);
			}
		}
		return out;
	}

	public List<URL> getVersion(List<URL> list, List<String> match,
			String version) {
		List<URL> out = new ArrayList<URL>();
		for (String s : match) {
			Collections.reverse(list);
			for (URL url : list) {
				if (url.getFile().contains(s) && url.getFile().contains(version)
						&& !url.getFile().contains("-symbols")) {
					out.add(url);
				}
			}
		}
		versionToDownload = version;
		log.debug("Using {} {}", match, version);
		return out;
	}

	public List<URL> getLatest(List<URL> list, List<String> match) {
		log.trace("Checking the lastest version of {}", match);
		log.trace("Input URL list {}", list);
		List<URL> out = new ArrayList<URL>();
		Collections.reverse(list);
		for (URL url : list) {
			for (String s : match) {
				if (url.getFile().contains(s)) {
					log.trace("URL {} match with {}", url, s);
					String currentVersion;
					if (getDriverName().contains("phantomjs")) {
						String file = url.getFile();
						file = url.getFile().substring(
								file.lastIndexOf(SEPARATOR), file.length());
						final int matchIndex = file.indexOf(s);
						currentVersion = file.substring(
								matchIndex + s.length() + 1, file.length());
						final int dashIndex = currentVersion.indexOf('-');
						currentVersion = currentVersion.substring(0, dashIndex);

					} else if (getDriverName().contains("wires")
							|| getDriverName().contains("geckodriver")) {
						currentVersion = url.getFile().substring(
								url.getFile().indexOf("-") + 1,
								url.getFile().lastIndexOf("-"));

					} else if (getDriverName().contains("operadriver")) {
						currentVersion = url.getFile().substring(
								url.getFile().indexOf(SEPARATOR + "v") + 2,
								url.getFile().lastIndexOf(SEPARATOR));

					} else {
						currentVersion = url.getFile().substring(
								url.getFile().indexOf(SEPARATOR) + 1,
								url.getFile().lastIndexOf(SEPARATOR));
					}

					if (getDriverName().contains("MicrosoftWebDriver")) {
						out.add(url);
						break;
					}

					if (versionToDownload == null) {
						versionToDownload = currentVersion;
					}
					if (versionCompare(currentVersion, versionToDownload) > 0) {
						versionToDownload = currentVersion;
						out.clear();
					}
					if (url.getFile().contains(versionToDownload)) {
						out.add(url);
					}
					if (versionToDownload.startsWith("v")) {
						versionToDownload = versionToDownload.substring(1);
					}
				}
			}
		}

		log.info("Latest version of {} is {}", match, versionToDownload);
		return out;
	}

	public Integer versionCompare(String str1, String str2) {
		String[] vals1 = str1.split("\\.");
		String[] vals2 = str2.split("\\.");
		int i = 0;
		while (i < vals1.length && i < vals2.length
				&& vals1[i].equals(vals2[i])) {
			i++;
		}
		if (i < vals1.length && i < vals2.length) {
			String v2 = vals2[i];
			if (v2.startsWith("v")) {
				v2 = v2.substring(1);
			}
			int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(v2));
			return Integer.signum(diff);
		} else {
			return Integer.signum(vals1.length - vals2.length);
		}
	}

	public List<URL> getDriversFromXml(URL driverUrl, List<String> driverBinary)
			throws Exception {
		log.info("Reading {} to seek {}", driverUrl, getDriverName());

		List<URL> urls = new ArrayList<URL>();

		int retries = 1;
		int maxRetries = WdmConfig.getInt("wdm.seekErrorRetries");
		do {
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(driverUrl.openStream()));
				Document xml = loadXML(reader);

				XPath xPath = XPathFactory.newInstance().newXPath();
				NodeList nodes = (NodeList) xPath.evaluate("//Contents/Key",
						xml.getDocumentElement(), XPathConstants.NODESET);

				for (int i = 0; i < nodes.getLength(); ++i) {
					Element e = (Element) nodes.item(i);
					String version = e.getChildNodes().item(0).getNodeValue();
					urls.add(new URL(driverUrl + version));
				}
				reader.close();
				break;
			} catch (Throwable e) {
				log.warn("[{}/{}] Exception reading {} to seek {}: {} {}",
						retries, maxRetries, driverUrl, getDriverName(),
						e.getClass().getName(), e.getMessage(), e);
				retries++;
				if (retries > maxRetries) {
					throw e;
				}
			}
		} while (true);

		return urls;
	}

	public Document loadXML(Reader reader) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(reader);
		return builder.parse(is);
	}

	public String getDownloadedVersion() {
		return System.getProperty(VERSION_PROPERTY);
	}

	private static String getOsName() {
		String os = System.getProperty("os.name").toLowerCase();

		if (SystemUtils.IS_OS_WINDOWS) {
			os = "win";
		} else if (SystemUtils.IS_OS_LINUX) {
			os = "linux";
		} else if (SystemUtils.IS_OS_MAC) {
			os = "mac";
		}
		return os;
	}

	protected static void exportDriver(String variableName,
			String variableValue) {
		log.info("Exporting {} as {}", variableName, variableValue);
		System.setProperty(variableName, variableValue);
	}

	protected InputStream openGitHubConnection(URL driverUrl)
			throws IOException {
		URLConnection conn = driverUrl.openConnection();
		conn.setRequestProperty("User-Agent", "Mozilla/5.0");
		conn.addRequestProperty("Connection", "keep-alive");

		String gitHubTokenName = WdmConfig.getString("wdm.gitHubTokenName");
		String gitHubTokenSecret = WdmConfig.getString("wdm.gitHubTokenSecret");
		if (!isNullOrEmpty(gitHubTokenName)
				&& !isNullOrEmpty(gitHubTokenSecret)) {
			String userpass = gitHubTokenName + ":" + gitHubTokenSecret;
			String basicAuth = "Basic "
					+ new String(new Base64().encode(userpass.getBytes()));
			conn.setRequestProperty("Authorization", basicAuth);
		}
		conn.connect();

		return conn.getInputStream();
	}

}
