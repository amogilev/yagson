package am.yagson.refs.impl;

import am.yagson.refs.HashReferencePlaceholder;
import am.yagson.refs.PlaceholderUse;
import am.yagson.refs.ReferencePlaceholder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility functions related to {@link am.yagson.refs.ReferencePlaceholder}
 */
public class PlaceholderUtils {

    /**
     * Finds hash placeholders in the specified list of placeholders and resolve them, either
     * noe (if no non-hash placeholders are present), or after all non-hash placeholders are resolved.
     *
     * @param instance the instance which contains the field placeholders
     * @param placeholders all field placeholders of the instance object, either hash or non-hash
     * @throws IOException
     */
    public static <T> void applyOrDeferHashPlaceholders(final T instance,
            final Set<ReferencePlaceholder> placeholders) throws IOException {
        final List<HashReferencePlaceholder> hashPlaceholders = new ArrayList<HashReferencePlaceholder>(placeholders.size());
        Iterator<ReferencePlaceholder> ip = placeholders.iterator();
        while (ip.hasNext()) {
            ReferencePlaceholder p = ip.next();
            if (p instanceof HashReferencePlaceholder) {
                hashPlaceholders.add((HashReferencePlaceholder)p);
                ip.remove();
            }
        }
        if (hashPlaceholders.isEmpty()) {
            return;
        }
        if (placeholders.isEmpty()) {
            // no non-hash placeholders, apply it now
            int hashCode = instance.hashCode();
            for (HashReferencePlaceholder hp : hashPlaceholders) {
                hp.applyActualObject(hashCode);
            }
        } else {
            // apply hash when all non-hash references are resolved
            for (ReferencePlaceholder p : placeholders) {
                final AtomicBoolean isResolved = new AtomicBoolean();
                p.registerUse(new PlaceholderUse<Object>() {
                    public void applyActualObject(Object actualObject) throws IOException {
                        if (isResolved.get()) {
                            return;
                        }
                        for (ReferencePlaceholder checkedPlaceholder : placeholders) {
                            if (checkedPlaceholder.getActualObject() == null) {
                                // not all placeholders are resolved yet
                                return;
                            }
                        }
                        isResolved.set(true);
                        int hashCode = instance.hashCode();
                        for (HashReferencePlaceholder hp : hashPlaceholders) {
                            hp.applyActualObject(hashCode);
                        }
                    }
                });
            }
        }
    }
}
