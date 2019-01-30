package com.github.eirslett.maven.plugins.frontend.lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LernaInstaller {

    public static final String INSTALL_PATH = "/node/lerna";

    public static final String DEFAULT_LERNA_DOWNLOAD_ROOT =
        "https://github.com/lerna/lerna/releases/download/";

    private static final Object LOCK = new Object();

    private static final String LERNA_ROOT_DIRECTORY = "dist";

    private String lernaVersion, lernaDownloadRoot, userName, password;

    private final Logger logger;

    private final InstallConfig config;

    private final ArchiveExtractor archiveExtractor;

    private final FileDownloader fileDownloader;

    LernaInstaller(InstallConfig config, ArchiveExtractor archiveExtractor, FileDownloader fileDownloader) {
        logger = LoggerFactory.getLogger(getClass());
        this.config = config;
        this.archiveExtractor = archiveExtractor;
        this.fileDownloader = fileDownloader;
    }

    public LernaInstaller setLernaVersion(String lernaVersion) {
        this.lernaVersion = lernaVersion;
        return this;
    }

    public LernaInstaller setLernaDownloadRoot(String lernaDownloadRoot) {
        this.lernaDownloadRoot = lernaDownloadRoot;
        return this;
    }

    public LernaInstaller setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public LernaInstaller setPassword(String password) {
        this.password = password;
        return this;
    }

    public void install() throws InstallationException {
        // use static lock object for a synchronized block
        synchronized (LOCK) {
            if (lernaDownloadRoot == null || lernaDownloadRoot.isEmpty()) {
                lernaDownloadRoot = DEFAULT_LERNA_DOWNLOAD_ROOT;
            }
            if (!lernaIsAlreadyInstalled()) {
                if (!lernaVersion.startsWith("v")) {
                    throw new InstallationException("Lerna version has to start with prefix 'v'.");
                }
                installLerna();
            }
        }
    }

    private boolean lernaIsAlreadyInstalled() {
        try {
            LernaExecutorConfig executorConfig = new InstallLernaExecutorConfig(config);
            File nodeFile = executorConfig.getLernaPath();
            if (nodeFile.exists()) {
                final String version =
                    new LernaExecutor(executorConfig, Arrays.asList("--version"), null).executeAndGetResult(logger).trim();

                if (version.equals(lernaVersion.replaceFirst("^v", ""))) {
                    logger.info("Lerna {} is already installed.", version);
                    return true;
                } else {
                    logger.info("Lerna {} was installed, but we need version {}", version, lernaVersion);
                    return false;
                }
            } else {
                return false;
            }
        } catch (ProcessExecutionException e) {
            return false;
        }
    }

    private void installLerna() throws InstallationException {
        try {
            logger.info("Installing Lerna version {}", lernaVersion);
            String downloadUrl = lernaDownloadRoot + lernaVersion;
            String extension = "tar.gz";
            String fileending = "/lerna-" + lernaVersion + "." + extension;

            downloadUrl += fileending;

            CacheDescriptor cacheDescriptor = new CacheDescriptor("lerna", lernaVersion, extension);

            File archive = config.getCacheResolver().resolve(cacheDescriptor);

            downloadFileIfMissing(downloadUrl, archive, userName, password);

            File installDirectory = getInstallDirectory();

            // We need to delete the existing lerna directory first so we clean out any old files, and
            // so we can rename the package directory below.
            try {
                if (installDirectory.isDirectory()) {
                    FileUtils.deleteDirectory(installDirectory);
                }
            } catch (IOException e) {
                logger.warn("Failed to delete existing Lerna installation.");
            }

            extractFile(archive, installDirectory);

            ensureCorrectLernaRootDirectory(installDirectory, lernaVersion);

            logger.info("Installed Lerna locally.");
        } catch (DownloadException e) {
            throw new InstallationException("Could not download Lerna", e);
        } catch (ArchiveExtractionException | IOException e) {
            throw new InstallationException("Could not extract the Lerna archive", e);
        }
    }

    private File getInstallDirectory() {
        File installDirectory = new File(config.getInstallDirectory(), INSTALL_PATH);
        if (!installDirectory.exists()) {
            logger.debug("Creating install directory {}", installDirectory);
            installDirectory.mkdirs();
        }
        return installDirectory;
    }

    private void extractFile(File archive, File destinationDirectory) throws ArchiveExtractionException {
        logger.info("Unpacking {} into {}", archive, destinationDirectory);
        archiveExtractor.extract(archive.getPath(), destinationDirectory.getPath());
    }

    private void ensureCorrectLernaRootDirectory(File installDirectory, String lernaVersion) throws IOException {
        File lernaRootDirectory = new File(installDirectory, LERNA_ROOT_DIRECTORY);
        if (!lernaRootDirectory.exists()) {
            logger.debug("Lerna root directory not found, checking for lerna-{}", lernaVersion);
            // Handle renaming Lerna 1.X root to LERNA_ROOT_DIRECTORY
            File lernaOneXDirectory = new File(installDirectory, "lerna-" + lernaVersion);
            if (lernaOneXDirectory.isDirectory()) {
                if (!lernaOneXDirectory.renameTo(lernaRootDirectory)) {
                    throw new IOException("Could not rename versioned lerna root directory to " + LERNA_ROOT_DIRECTORY);
                }
            } else {
                throw new FileNotFoundException("Could not find lerna distribution directory during extract");
            }
        }
    }

    private void downloadFileIfMissing(String downloadUrl, File destination, String userName, String password)
        throws DownloadException {
        if (!destination.exists()) {
            downloadFile(downloadUrl, destination, userName, password);
        }
    }

    private void downloadFile(String downloadUrl, File destination, String userName, String password)
        throws DownloadException {
        logger.info("Downloading {} to {}", downloadUrl, destination);
        fileDownloader.download(downloadUrl, destination.getPath(), userName, password);
    }
}
