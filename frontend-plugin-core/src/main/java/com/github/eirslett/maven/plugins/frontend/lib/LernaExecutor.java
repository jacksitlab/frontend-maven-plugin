package com.github.eirslett.maven.plugins.frontend.lib;

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
        final String lerna = config.getLernaPath().getAbsolutePath();
        List<String> localPaths = new ArrayList<>();
        localPaths.add(config.getYarnPath().getParent());
        localPaths.add(config.getNodePath().getParent());
        for(String p:localPaths) {
            this.logger.info("add {} to PATH",p);
        }
        executor = new ProcessExecutor(config.getWorkingDirectory(), localPaths,
            Utils.prepend("lerna", arguments), config.getPlatform(), additionalEnvironment);
    }

    public String executeAndGetResult(final Logger logger) throws ProcessExecutionException {
        return executor.executeAndGetResult(logger);
    }

    public int executeAndRedirectOutput(final Logger logger) throws ProcessExecutionException {
        return executor.executeAndRedirectOutput(logger);
    }
}
