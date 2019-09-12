package com.shiny.joypadmod.mappings;

public class DefaultAxisMappings {


    public int LSx() {
        return 0;
    }

    public int LSy() {
        return 1;
    }

    public int RSx() {
        return 2;
    }

    public int RSy() {
        return 3;
    }

    public int LT() {
        return 4;
    }

    public int RT() {
        return 5;
    }

    public class LWJGLAxisMappings extends DefaultAxisMappings {

        @Override
        public int LSx() {
            return 1;
        }

        @Override
        public int LSy() {
            return 0;
        }

        @Override
        public int RSx() {
            return 3;
        }

        @Override
        public int RSy() {
            return 2;
        }

    }

}
