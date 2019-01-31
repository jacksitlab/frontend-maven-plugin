package com.github.eirslett.maven.plugins.frontend.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

final class LernaExecutor {

    private final ProcessExecutor executor;

    public LernaExecutor(LernaExecutorConfig config, List<String> arguments,
        Map<String, String> additionalEnvironment) {
        final String lerna = config.getLernaPath().getAbsolutePath();
        List<String> localPaths = new ArrayList<>();
        localPaths.add(config.getYarnPath().getParent());
        localPaths.add(config.getNodePath().getParent());
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
