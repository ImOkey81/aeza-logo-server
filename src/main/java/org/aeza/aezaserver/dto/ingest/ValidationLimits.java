package org.aeza.aezaserver.dto.ingest;

public final class ValidationLimits {
    public static final int LEVEL_MAX = 32;
    public static final int HOST_MAX = 2048;
    public static final int SERVICE_MAX = 2048;
    public static final int SOURCE_TYPE_MAX = 255;
    public static final int SOURCE_PATH_MAX = 4096;
    public static final int AGENT_ID_MAX = 255;

    private ValidationLimits() {
    }
}
