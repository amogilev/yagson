package am.yagson.refs;

import java.io.IOException;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;

public interface ReferencesReadContext {

    /**
     * Registers an object corresponding to the path built by previous {@link #beforeObjectRead()} call.
     *
     * @param value the object created by de-serializers, optionally without fields/contents yet
     * @param isSimple whether the object is known to not contain any circular dependencies, and so
     *    there will be no references to it
     */
    void registerObject(Object value, boolean isSimple);

    <T> T doRead(JsonReader reader, TypeAdapter<T> typeAdapter, String pathElement) throws IOException;

    /**
     * Returns whether the specified string is a reference string, known in this context.
     */
    boolean isReferenceString(String str);

    /**
     * Returns the object corresponding to the specified reference string. If the reference is known, but
     * no actual object exist yet, returns {@code null} and sets the placeholder, which MUST be consumed
     * using {@link #consumeLastPlaceholderIfAny()} before any further call.
     *
     * @throws JsonSyntaxException if no referenced object is known for the specified reference
     * @throws IOException if read failed
     *
     * @return the object for the reference; or {@code null} if there is a placeholder set for this reference
     */
    <T> T getReferencedObject(String reference) throws IOException;

    /**
     * Consumes and returns the placeholder set by the last call of {@link #getReferencedObject(String)}
     */
    <T> ReferencePlaceholder<T> consumeLastPlaceholderIfAny();

    /**
     * Returns the policy implemented by this context.
     */
    ReferencesPolicy getPolicy();

}
