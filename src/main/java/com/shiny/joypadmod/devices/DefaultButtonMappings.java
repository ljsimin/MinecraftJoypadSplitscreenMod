package com.shiny.joypadmod.devices;

public class DefaultButtonMappings {

    public int A() {
        return 0;
    }

    public int B() {
        return 1;
    }

    public int X() {
        return 2;
    }

    public int Y() {
        return 3;
    }

    public int Back() {
        return 4;
    }

    public int Start() {
        return 5;
    }

    public int LB() {
        return 6;
    }

    public int RB() {
        return 7;
    }

    public int LS() {
        return 8;
    }

    public int RS() {
        return 9;
    }


    public class LWJGLButtonMappings extends DefaultButtonMappings {
        @Override
        public int Back() {
            return 6;
        }

        @Override
        public int LB() {
            return 4;
        }

        @Override
        public int RB() {
            return 5;
        }

        @Override
        public int Start() {
            return 7;
        }

    }
}

