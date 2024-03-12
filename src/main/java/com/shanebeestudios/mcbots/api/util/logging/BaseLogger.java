package com.shanebeestudios.mcbots.api.util.logging;

public abstract class BaseLogger {

    public abstract void log(String prefix, String message);

    public abstract void error(String error);

    public abstract void error(Exception e);

    public abstract void info(String format, Object... objects);

    public abstract void info(String... message);

    public abstract void info();

    public abstract void warn(String... warning);

    public abstract void warn();

    public abstract void chat(String... chat);

    public abstract void critical(Exception e);

}
