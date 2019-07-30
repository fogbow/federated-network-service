package cloud.fogbow.fns.utils;

import cloud.fogbow.common.exceptions.UnexpectedException;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

public class BashScriptRunner {
    private static final String LINE_BREAK = System.getProperty("line.separator");

    public Output runtimeRun(String... command) throws UnexpectedException {
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            int exitCode = p.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            Iterator<String> outputLines = reader.lines().iterator();
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(
                    p.getErrorStream()));
            Iterator<String> outputLines2 = reader2.lines().iterator();
            return new Output(exitCode, StringUtils.join(outputLines, LINE_BREAK), StringUtils.join(outputLines2, LINE_BREAK));
        } catch (InterruptedException|IOException e) {
            throw new UnexpectedException("", e);
        }
    }

    @Deprecated
    public Output run(String... command) throws UnexpectedException {
        return run(null, command);
    }

    private Output run(File outputFile, String... command) throws UnexpectedException {
        ProcessBuilder pb = new ProcessBuilder(command);

        if (outputFile != null) {
            pb.redirectOutput(outputFile);
        }

        try {
            Process p = pb.start();
            int exitCode = p.waitFor();
            return new Output(exitCode, null, null);
        } catch (IOException|InterruptedException e) {
            throw new UnexpectedException(e.getMessage(), e);
        }
    }

    public class Output {
        private int exitCode;
        private String content;
        private String error;

        public Output(int exitCode, String content, String error) {
            this.exitCode = exitCode;
            this.content = content;
            this.error = error;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getContent() {
            return content;
        }

        public String getError() {
            return error;
        }
    }
}
