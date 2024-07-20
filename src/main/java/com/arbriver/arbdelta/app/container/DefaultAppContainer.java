package com.arbriver.arbdelta.app.container;

import com.arbriver.arbdelta.app.service.MatchOrchestrator;
import org.springframework.stereotype.Component;


@Component
public class DefaultAppContainer {
    private final MatchOrchestrator orchestrator;

    public DefaultAppContainer(MatchOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    public void start() {
        orchestrator.orchestrate();
    }
}
