package io.github.wasabithumb.xpdy.endpoint;

import io.github.wasabithumb.xpdy.endpoint.methods.*;
import io.github.wasabithumb.xpdy.misc.HTTPVerb;
import io.github.wasabithumb.xpdy.misc.MimeType;
import io.github.wasabithumb.xpdy.misc.MimeTypes;
import io.github.wasabithumb.xpdy.misc.URIPath;
import io.github.wasabithumb.xpdy.payload.response.Response;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;

public sealed abstract class EndpointMeta {

    /**
     * Computes the endpoint meta for a given (possibly) endpoint method
     * @return The meta, or null if method has no endpoint annotations
     * @throws IllegalStateException Method has multiple endpoint annotations
     */
    public static @Nullable EndpointMeta of(@NotNull Method method) throws IllegalStateException {
        EndpointMeta meta = null;

        Get get = method.getAnnotation(Get.class);
        if (get != null) {
            meta = new ForGet(get);
        }

        Post post = method.getAnnotation(Post.class);
        if (post != null) {
            if (meta != null) raiseMultiple(method);
            meta = new ForPost(post);
        }

        Put put = method.getAnnotation(Put.class);
        if (put != null) {
            if (meta != null) raiseMultiple(method);
            meta = new ForPut(put);
        }

        Patch patch = method.getAnnotation(Patch.class);
        if (patch != null) {
            if (meta != null) raiseMultiple(method);
            meta = new ForPatch(patch);
        }

        Delete delete = method.getAnnotation(Delete.class);
        if (delete != null) {
            if (meta != null) raiseMultiple(method);
            meta = new ForDelete(delete);
        }

        if (meta != null) {
            method.trySetAccessible();
            Class<?> type = method.getReturnType();
            if (Void.TYPE.equals(type) || Void.class.equals(type)) {
                meta.isVoid = true;
            } else if (!type.isAssignableFrom(Response.class)) {
                throw new IllegalStateException("Endpoint method \"" + method.getName() + "\" in class " +
                        method.getDeclaringClass().getName() + " has illegal return type (must be void or Response)");
            }
        }

        return meta;
    }

    @Contract("_ -> fail")
    private static void raiseMultiple(@NotNull Method method) throws IllegalStateException {
        throw new IllegalStateException("Method \"" + method.getName() + "\" in class " +
                method.getDeclaringClass().getName() + " has multiple endpoint annotations"
        );
    }

    //

    private boolean isVoid = false;

    //

    public abstract @NotNull HTTPVerb verb();

    public @NotNull String verbString() {
        return this.verb().name();
    }

    public abstract @NotNull @URIPath String path();

    public abstract @NotNull @MimeType String inType(@NotNull @MimeType String defaultType);

    public abstract @NotNull @MimeType String outType(@NotNull @MimeType String defaultType);

    public abstract boolean enforceOutType();

    public final boolean isVoid() {
        return this.isVoid;
    }

    //

    @Override
    public int hashCode() {
        return Objects.hash(this.verb(), this.path());
    }

    @Override
    @Contract("null -> false")
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof EndpointMeta other) {
            return this.verb().equals(other.verb()) &&
                    this.path().equals(other.path());
        }
        return super.equals(obj);
    }

    @Override
    public @NotNull String toString() {
        return "EndpointMeta[verb = " + this.verbString() + ", path = " + this.path() + "]";
    }

    //

    private static sealed abstract class For<A extends Annotation> extends EndpointMeta {

        protected final A handle;
        protected For(@NotNull A handle) {
            this.handle = handle;
        }

        //

        @Contract(pure = true)
        protected abstract @NotNull @MimeType String in();

        @Contract(pure = true)
        protected abstract @NotNull @MimeType String out();

        //

        @Override
        public @NotNull @MimeType String inType(@NotNull @MimeType String defaultType) {
            return !this.in().isEmpty() ? this.in() : defaultType;
        }

        @Override
        public boolean enforceOutType() {
            return !this.out().isEmpty();
        }

        @Override
        public @NotNull @MimeType String outType(@NotNull @MimeType String defaultType) {
            return this.enforceOutType() ? this.out() : defaultType;
        }

    }

    //

    private static final class ForGet extends For<Get> {

        private ForGet(@NotNull Get handle) {
            super(handle);
        }

        @Override
        public @NotNull HTTPVerb verb() {
            return HTTPVerb.GET;
        }

        @Override
        public @NotNull @URIPath String path() {
            return this.handle.value();
        }

        @Override
        protected @NotNull @MimeType String in() {
            return MimeTypes.URLENCODED;
        }

        @Override
        protected @NotNull @MimeType String out() {
            return this.handle.out();
        }

    }

    private static final class ForPost extends For<Post> {

        private ForPost(@NotNull Post handle) {
            super(handle);
        }

        @Override
        public @NotNull HTTPVerb verb() {
            return HTTPVerb.POST;
        }

        @Override
        public @NotNull @URIPath String path() {
            return this.handle.value();
        }

        @Override
        protected @NotNull @MimeType String in() {
            return this.handle.in();
        }

        @Override
        protected @NotNull @MimeType String out() {
            return this.handle.out();
        }

    }

    private static final class ForPut extends For<Put> {

        private ForPut(@NotNull Put handle) {
            super(handle);
        }

        @Override
        public @NotNull HTTPVerb verb() {
            return HTTPVerb.PUT;
        }

        @Override
        public @NotNull @URIPath String path() {
            return this.handle.value();
        }

        @Override
        protected @NotNull @MimeType String in() {
            return this.handle.in();
        }

        @Override
        protected @NotNull @MimeType String out() {
            return this.handle.out();
        }

    }

    private static final class ForPatch extends For<Patch> {

        private ForPatch(@NotNull Patch handle) {
            super(handle);
        }

        @Override
        public @NotNull HTTPVerb verb() {
            return HTTPVerb.PATCH;
        }

        @Override
        public @NotNull @URIPath String path() {
            return this.handle.value();
        }

        @Override
        protected @NotNull @MimeType String in() {
            return this.handle.in();
        }

        @Override
        protected @NotNull @MimeType String out() {
            return this.handle.out();
        }

    }

    private static final class ForDelete extends For<Delete> {

        private ForDelete(@NotNull Delete handle) {
            super(handle);
        }

        @Override
        public @NotNull HTTPVerb verb() {
            return HTTPVerb.DELETE;
        }

        @Override
        public @NotNull @URIPath String path() {
            return this.handle.value();
        }

        @Override
        protected @NotNull @MimeType String in() {
            return this.handle.in();
        }

        @Override
        protected @NotNull @MimeType String out() {
            return this.handle.out();
        }

    }

}
