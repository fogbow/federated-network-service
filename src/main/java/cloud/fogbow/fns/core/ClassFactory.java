package cloud.fogbow.fns.core;

import cloud.fogbow.common.exceptions.FatalErrorException;
import cloud.fogbow.fns.constants.Messages;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;

// Each package has to have its own ClassFactory

public class ClassFactory {
    private static final Logger LOGGER = Logger.getLogger(ClassFactory.class);

    public Object createInstance(String className, String parameter1, String parameter2)
            throws FatalErrorException {

        Object pluginInstance = null;

        Class<?> classpath;
        Constructor<?> constructor;

        try {
            classpath = Class.forName(className);
            constructor = classpath.getConstructor(String.class, String.class);
            pluginInstance = constructor.newInstance(parameter1, parameter2);
        } catch (ClassNotFoundException e) {
            throw new FatalErrorException(String.format(Messages.Exception.UNABLE_TO_FIND_CLASS_S, className));
        } catch (Exception e) {
            throw new FatalErrorException(e.getMessage(), e);
        }

        return pluginInstance;
    }

    public Object createInstance(String className, String parameter) throws FatalErrorException {

        Object pluginInstance = null;

        Class<?> classpath;
        Constructor<?> constructor;

        try {
            classpath = Class.forName(className);
            constructor = classpath.getConstructor(String.class);
            pluginInstance = constructor.newInstance(parameter);
        } catch (ClassNotFoundException e) {
            throw new FatalErrorException(String.format(Messages.Exception.UNABLE_TO_FIND_CLASS_S, className));
        } catch (Exception e) {
            throw new FatalErrorException(e.getMessage(), e);
        }

        return pluginInstance;
    }

    public Object createInstance(String className) throws FatalErrorException {

        Object pluginInstance = null;

        Class<?> classpath;
        Constructor<?> constructor;

        try {
            classpath = Class.forName(className);
            constructor = classpath.getConstructor();
            pluginInstance = constructor.newInstance();
        } catch (ClassNotFoundException e) {
            throw new FatalErrorException(String.format(Messages.Exception.UNABLE_TO_FIND_CLASS_S, className));
        } catch (Exception e) {
            throw new FatalErrorException(e.getMessage(), e);
        }

        return pluginInstance;
    }
}
