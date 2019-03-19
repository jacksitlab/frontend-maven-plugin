package com.github.eirslett.maven.plugins.frontend.lib;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class LernaExecutor {

    private final ProcessExecutor executor;
    private final Logger logger;


    public LernaExecutor(LernaExecutorConfig config, List<String> arguments,
            Map<String, String> additionalEnvironment) {
        this.logger = LoggerFactory.getLogger(getClass());
        final String lerna = config.getLernaLocalInstallDirectoryPath() + "/lerna";
        final String lerna2 = config.getLernaLocalInstallDirectoryPathAlt() + "/lerna";
        List<String> localPaths = new ArrayList<>();
        localPaths.add(config.getYarnPath().getParent());
        localPaths.add(config.getNodePath().getParent());
        localPaths.add(config.getLernaLocalInstallDirectoryPath());
        localPaths.add(config.getLernaLocalInstallDirectoryPathAlt());
         for (String p : localPaths) {
            this.logger.info("add {} to PATH", p);
        }
        File f = new File(lerna);
        File f2 = new File(lerna2);
        if (f.exists()) {   //local executable exists
            executor = new ProcessExecutor(config.getWorkingDirectory(), localPaths, Utils.prepend(lerna, arguments),
                    config.getPlatform(), additionalEnvironment);
        } 
        else if (f2.exists()) {   //local executable exists
            executor = new ProcessExecutor(config.getWorkingDirectory(), localPaths, Utils.prepend(lerna2, arguments),
                    config.getPlatform(), additionalEnvironment);
        } 
       
        else // try global command
        {
            executor = new ProcessExecutor(config.getWorkingDirectory(), localPaths, Utils.prepend("lerna", arguments),
                    config.getPlatform(), additionalEnvironment);
        }
    }

    public String executeAndGetResult(final Logger logger) throws ProcessExecutionException {
        return executor.executeAndGetResult(logger);
    }

    public int executeAndRedirectOutput(final Logger logger) throws ProcessExecutionException {
        return executor.executeAndRedirectOutput(logger);
    }
}
