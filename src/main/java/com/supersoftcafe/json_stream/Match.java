package com.supersoftcafe.json_stream;

/* A single match found in the stream provided to handlers.
 * The caller almost certainly will re-use <emp>Match</emp>
 * instances, so do not retain a reference.
 */
public interface Match<T> {
    Path getPath();
    T getContent();

    void stop();
}
