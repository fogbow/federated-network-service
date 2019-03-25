package cloud.fogbow.fns.core;

import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.processors.ClosedProcessor;
import cloud.fogbow.fns.core.processors.OpenProcessor;
import org.apache.log4j.Logger;

public class ProcessorThreadsController {
    private static final Logger LOGGER = Logger.getLogger(ProcessorThreadsController.class);

    private static final Long DEFAULT_SLEEP_TIME = 1000L;

    private final static String OPEN_PROCESSOR_THREAD_NAME = "fns-open-proc";
    private final static String CLOSED_PROCESSOR_THREAD_NAME = "fns-closed-proc";

    private final Thread openProcessorThread;
    private final Thread closedProcessorThread;

    public ProcessorThreadsController() {
        OpenProcessor openProcessor = new OpenProcessor(DEFAULT_SLEEP_TIME);
        ClosedProcessor closedProcessor = new ClosedProcessor(DEFAULT_SLEEP_TIME);

        this.openProcessorThread = new Thread(openProcessor, OPEN_PROCESSOR_THREAD_NAME);
        this.closedProcessorThread = new Thread(closedProcessor, CLOSED_PROCESSOR_THREAD_NAME);
    }

    /**
     * This method starts all FNS processors, if you defined a new FNS operation and this
     * operation require a new thread to run, you should start this thread at this method.
     */
    public void startFnsThreads() {
        LOGGER.info(Messages.Info.STARTING_THREADS);
        this.openProcessorThread.start();
        this.closedProcessorThread.start();
    }
}
