package net.kencochrane.raven.marshaller.json;

import com.fasterxml.jackson.core.JsonGenerator;
import net.kencochrane.raven.event.interfaces.StackTraceInterface;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class StackTraceInterfaceBinding implements InterfaceBinding<StackTraceInterface> {
    private static final String FRAMES_PARAMETER = "frames";
    private static final String FILENAME_PARAMETER = "filename";
    private static final String FUNCTION_PARAMETER = "function";
    private static final String MODULE_PARAMETER = "module";
    private static final String LINE_NO_PARAMETER = "lineno";
    private static final String ABSOLUTE_PATH_PARAMETER = "abs_path";
    private static final String CONTEXT_LINE_PARAMETER = "context_line";
    private static final String PRE_CONTEXT_PARAMETER = "pre_context";
    private static final String POST_CONTEXT_PARAMETER = "post_context";
    private static final String IN_APP_PARAMETER = "in_app";
    private static final String VARIABLES_PARAMETER = "vars";
    private final Set<String> notInAppFrames;

    public StackTraceInterfaceBinding() {
        notInAppFrames = new HashSet<String>();
        notInAppFrames.add("com.sun.");
        notInAppFrames.add("java.");
        notInAppFrames.add("javax.");
        notInAppFrames.add("org.omg.");
        notInAppFrames.add("sun.");
        notInAppFrames.add("junit.");
        notInAppFrames.add("com.intellij.rt.");
    }

    public StackTraceInterfaceBinding(Set<String> notInAppFrames) {
        // Makes a copy to avoid an external modification.
        this.notInAppFrames = new HashSet<String>(notInAppFrames);
    }

    /**
     * Writes a single frame based on a {@code StackTraceElement}.
     *
     * @param stackTraceElement current frame in the stackTrace.
     */
    private void writeFrame(JsonGenerator generator, StackTraceElement stackTraceElement) throws IOException {
        generator.writeStartObject();
        // Do not display the file name (irrelevant) as it replaces the module in the sentry interface.
        //generator.writeStringField(FILENAME_PARAMETER, stackTraceElement.getFileName());
        generator.writeStringField(MODULE_PARAMETER, stackTraceElement.getClassName());
        generator.writeBooleanField(IN_APP_PARAMETER, isFrameInApp(stackTraceElement));
        generator.writeStringField(FUNCTION_PARAMETER, stackTraceElement.getMethodName());
        generator.writeNumberField(LINE_NO_PARAMETER, stackTraceElement.getLineNumber());
        generator.writeEndObject();
    }

    private boolean isFrameInApp(StackTraceElement stackTraceElement) {
        //TODO: A set is absolutely not performant here, a Trie could be a better solution.
        for (String notInAppFrame : notInAppFrames) {
            if (stackTraceElement.getClassName().startsWith(notInAppFrame)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void writeInterface(JsonGenerator generator, StackTraceInterface stackTraceInterface) throws IOException {
        StackTraceElement[] stackTrace = stackTraceInterface.getStackTrace();

        generator.writeStartObject();
        generator.writeArrayFieldStart(FRAMES_PARAMETER);

        // Go through the stackTrace frames from the first call to the last
        for (int i = stackTrace.length - 1; i >= 0; i--) {
            writeFrame(generator, stackTrace[i]);
        }

        generator.writeEndArray();
        generator.writeEndObject();
    }
}
