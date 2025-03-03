package io.github.wasabithumb.xpdy.endpoint;

import io.github.wasabithumb.xpdy.misc.collections.ClassMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.Map;

@ApiStatus.Internal
public final class EndpointInjector {

    private final ClassMap<Object, Object> map = ClassMap.create();

    //

    public <T> void register(@NotNull Class<T> clazz, @NotNull T value) {
        this.map.put(clazz, value);
    }

    public <T> @Nullable T match(@NotNull Class<T> clazz) {
        Object exact = this.map.get(clazz);
        if (exact != null) return clazz.cast(exact);

        for (Map.Entry<Class<?>, Object> entry : this.map.entrySet()) {
            if (clazz.isAssignableFrom(entry.getKey()))
                return clazz.cast(entry.getValue());
        }

        return null;
    }

    public <E extends Endpoints> @NotNull E inject(@NotNull Class<E> clazz) {
        E ret = clazz.cast(this.construct(clazz));
        this.handleFields(ret);
        return ret;
    }

    //

    private Object construct(@NotNull Class<?> clazz) {
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            throw new IllegalArgumentException("Cannot construct abstract class " + clazz.getName());
        }

        Constructor<?> con = null;

        for (Constructor<?> candidate : clazz.getDeclaredConstructors()) {
            if (!candidate.isAnnotationPresent(EndpointInject.class)) continue;
            if (con != null) {
                throw new IllegalStateException("Endpoint class " + clazz.getName() + " has multiple constructors " +
                        "annotated with @EndpointInject");
            }
            con = candidate;
        }

        if (con == null) {
            try {
                con = clazz.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Endpoint class " + clazz.getName() + " has no primary constructor", e);
            }
        }

        try {
            con.setAccessible(true);
        } catch (InaccessibleObjectException | SecurityException ignored) { }

        Parameter[] paramSpecs = con.getParameters();
        int paramCount = paramSpecs.length;
        Object[] params = new Object[paramCount];

        for (int i=0; i < paramCount; i++) {
            Parameter spec = paramSpecs[i];
            Class<?> type = spec.getType();
            Object value = this.match(type);
            if (value == null) {
                throw new IllegalStateException("Cannot inject constructor parameter \"" + spec.getName() +
                        "\" for endpoint class " + clazz.getName() + " (type " +
                        type.getName() + " is not registered)");
            }
            params[i] = value;
        }

        try {
            return con.newInstance(params);
        } catch (InvocationTargetException | ExceptionInInitializerError e) {
            throw new IllegalStateException("Primary constructor for endpoint class " + clazz.getName() +
                    " raised an exception", e);
        } catch (ReflectiveOperationException | SecurityException e) {
            throw new AssertionError("Unexpected reflect error", e);
        }
    }

    private void handleFields(Endpoints o) {
        Class<?> head = o.getClass();
        do {
            this.handleFields(head, o);
            head = head.getSuperclass();
        } while (head != null && Endpoints.class.isAssignableFrom(head));
    }

    private void handleFields(Class<?> cls, Endpoints o) {
        for (Field f : cls.getDeclaredFields()) {
            if (!f.isAnnotationPresent(EndpointInject.class)) continue;
            try {
                f.setAccessible(true);
            } catch (InaccessibleObjectException | SecurityException ignored) { }
            this.handleField(cls, o, f);
        }
    }

    private void handleField(Class<?> cls, Endpoints o, Field f) {
        Object value = this.match(f.getType());
        if (value == null) {
            throw new IllegalStateException("Cannot inject field \"" + f.getName() + "\" for endpoint class " +
                    cls.getName() + " (type " + f.getType().getName() + " is not registered)");
        }

        try {
            f.set(o, value);
        } catch (IllegalAccessException | SecurityException e) {
            throw new IllegalStateException("Unable to inject field \"" + f.getName() + "\" for endpoint class " +
                    cls.getName() + " (VM restriction)");
        } catch (Exception e) {
            throw new AssertionError("Unable to inject field \"" + f.getName() + "\" for endpoint class " +
                    cls.getName() + " (expectation failed)", e);
        }
    }

}
