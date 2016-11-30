package com.ivan.xinput.natives;

import java.nio.ByteBuffer;

/**
 * Native methods for XInput 1.3.
 *
 * @author Ivan "StrikerX3" Oliveira
 */
public final class XInputNatives {
    private static final boolean LOADED;
    private static Throwable loadError;
    static {
        boolean loaded = false;
        try {
            NativeLibraryHelper.load("lib/native", "XInputDevice", XInputNatives.class.getClassLoader());
            loaded = true;
        } catch (final Throwable e) {
            loadError = e;
        }
        LOADED = loaded;
    }

    private XInputNatives() {}

    /**
     * Determines whether the native libraries were loaded successfully.
     *
     * @return <code>true</code> if the native libraries were loaded, <code>false</code> otherwise
     */
    public static boolean isLoaded() {
        return LOADED;
    }

    /**
     * Retrieves the exception thrown when attempting to load the native library.
     *
     * @return the exception thrown when attempting to load the native library
     */
    public static Throwable getLoadError() {
        return loadError;
    }

    // https://msdn.microsoft.com/en-us/library/windows/desktop/microsoft.directx_sdk.reference.xinputgetstate(v=vs.85).aspx
    public static native int pollDevice(int playerNum, ByteBuffer data);

    // https://msdn.microsoft.com/en-us/library/windows/desktop/microsoft.directx_sdk.reference.xinputsetstate(v=vs.85).aspx
    public static native int setVibration(int playerNum, int leftMotor, int rightMotor);
}
