package com.github.eirslett.maven.plugins.frontend.lib;

import java.io.File;

public interface LernaExecutorConfig {

    File getNodePath();

    File getYarnPath();

    File getLernaPath();

    File getWorkingDirectory();

    Platform getPlatform();

    /**
     * @return
     */

}


final class InstallLernaExecutorConfig implements LernaExecutorConfig {

    private static final String LERNA_WINDOWS =
            LernaInstaller.INSTALL_PATH.concat("lerna.cmd").replaceAll("/", "\\\\");

    private static final String LERNA_DEFAULT = LernaInstaller.INSTALL_PATH + "lerna";

    private File nodePath;

    private final InstallConfig installConfig;

    public InstallLernaExecutorConfig(InstallConfig installConfig) {
        this.installConfig = installConfig;
        nodePath = new InstallNodeExecutorConfig(installConfig).getNodePath();
    }

    @Override
    public File getNodePath() {
        return nodePath;
    }

    @Override
    public File getYarnPath() {
        String yarnExecutable = getPlatform().isWindows() ? InstallYarnExecutorConfig.YARN_WINDOWS : InstallYarnExecutorConfig.YARN_DEFAULT;
        return new File(installConfig.getInstallDirectory() + yarnExecutable);
    }
    
    @Override
    public File getLernaPath() {
        String lernaExecutable = getPlatform().isWindows() ? LERNA_WINDOWS : LERNA_DEFAULT;
        return new File(installConfig.getInstallDirectory() +"node_modules/.bin/"+ lernaExecutable);
    }

    @Override
    public File getWorkingDirectory() {
        return installConfig.getWorkingDirectory();
    }

    @Override
    public Platform getPlatform() {
        return installConfig.getPlatform();
    }

    
}
