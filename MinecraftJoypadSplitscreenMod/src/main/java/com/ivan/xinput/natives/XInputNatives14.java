package com.ivan.xinput.natives;

import java.nio.ByteBuffer;

/**
 * Native methods for XInput 1.4.
 *
 * @author Ivan "StrikerX3" Oliveira
 */
public final class XInputNatives14 {
    private static final boolean LOADED;
    private static Throwable loadError;
    static {
        boolean loaded = false;
        try {
            NativeLibraryHelper.load("lib/native", "XInputDevice14", XInputNatives14.class.getClassLoader());
            loaded = true;
        } catch (final Throwable e) {
            loadError = e;
        }
        LOADED = loaded;
    }

    private XInputNatives14() {}

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

    // https://msdn.microsoft.com/en-us/library/windows/desktop/microsoft.directx_sdk.reference.xinputenable(v=vs.85).aspx
    public static native void setEnabled(boolean enabled);

    // https://msdn.microsoft.com/en-us/library/windows/desktop/microsoft.directx_sdk.reference.xinputgetcapabilities(v=vs.85).aspx
    public static native int getCapabilities(int playerNum, int flags, ByteBuffer data);

    // https://msdn.microsoft.com/en-us/library/windows/desktop/microsoft.directx_sdk.reference.xinputgetbatteryinformation(v=vs.85).aspx
    public static native int getBatteryInformation(int playerNum, int deviceType, ByteBuffer data);

    // https://msdn.microsoft.com/en-us/library/windows/desktop/microsoft.directx_sdk.reference.xinputgetkeystroke(v=vs.85).aspx
    public static native int getKeystroke(int playerNum, ByteBuffer data);
}
