/**
 * <h1>Endpoint Annotations</h1>
 * <ul>
 *     <li>{@link io.github.wasabithumb.xpdy.endpoint.methods.Get Get} for HTTP <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/GET">GET</a></li>
 *     <li>{@link io.github.wasabithumb.xpdy.endpoint.methods.Post Post} for HTTP <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/POST">POST</a></li>
 *     <li>{@link io.github.wasabithumb.xpdy.endpoint.methods.Put Put} for HTTP <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/PUT">PUT</a></li>
 *     <li>{@link io.github.wasabithumb.xpdy.endpoint.methods.Patch Patch} for HTTP <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/PATCH">PATCH</a></li>
 *     <li>{@link io.github.wasabithumb.xpdy.endpoint.methods.Delete Delete} for HTTP <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/DELETE">DELETE</a></li>
 * </ul>
 *
 * <h2>Notes</h2>
 * <ul>
 *     <li>
 *         HTTP <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/HEAD">HEAD</a> and
 *         <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/OPTIONS">OPTIONS</a> are not supported;
 *         these are automatically served using global configuration
 *     </li>
 *     <li>
 *         HTTP <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/CONNECT">CONNECT</a> is not supported;
 *         out of scope
 *     </li>
 *     <li>
 *         HTTP <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/TRACE">TRACE</a> is not supported;
 *         cumbersome to implement
 *     </li>
 * </ul>
 */
package io.github.wasabithumb.xpdy.endpoint.methods;