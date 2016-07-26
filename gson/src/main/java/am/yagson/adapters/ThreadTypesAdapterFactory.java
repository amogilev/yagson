package am.yagson.adapters;

import com.google.gson.Gson;
import com.google.gson.SimpleTypeAdapter;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides type adapters for {@link Thread} and {@link ThreadGroup}.
 */
public class ThreadTypesAdapterFactory implements TypeAdapterFactory {

    private static final char SEPARATOR_CHAR = '.';

    // do not use '/' as escape char as it is double-escaped by writer
    private static final char ESCAPE_CHAR = '_';

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Class<? super T> rawType = typeToken.getRawType();
        if (ThreadGroup.class.isAssignableFrom(rawType)) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            TypeAdapter<T> result = (TypeAdapter)new ThreadGroupAdapter();
            return result;
        } else if (Thread.class.isAssignableFrom(rawType)) {
            // TODO
        }
        return null;
    }

    private static class ThreadGroupAdapter extends SimpleTypeAdapter<ThreadGroup> {

        /**
         * The backup thread group returned if no exact match for the read path is found
         */
        ThreadGroup BACKUP_GROUP = null;

        @Override
        public void write(JsonWriter out, ThreadGroup tg) throws IOException {
            if (tg == null) {
                out.nullValue();
                return;
            }

            out.value(getThreadGroupsPath(tg));
        }

        @Override
        public ThreadGroup read(JsonReader in) throws IOException {
            String path = in.nextString();
            List<String> groupNames = split(path, SEPARATOR_CHAR, ESCAPE_CHAR);
            ThreadGroup matchedGroup = matchThreadGroup(groupNames);
            return matchedGroup == null ? BACKUP_GROUP : matchedGroup;
        }
    }

    private static ThreadGroup matchThreadGroup(List<String> groupNames) {
        ThreadGroup rootGroup = getSystemThreadGroup();

        if (groupNames.size() <= 0 || !rootGroup.getName().equals(groupNames.get(0))) {
            return null;
        }

        ThreadGroup curMatchedGroup = rootGroup;
        for (int i = 1; i < groupNames.size() && curMatchedGroup != null; i++) {
            String groupName = groupNames.get(i);
            ThreadGroup[] childGroups = new ThreadGroup[curMatchedGroup.activeGroupCount()];
            rootGroup.enumerate(childGroups, false);

            curMatchedGroup = findNamedIn(groupName, childGroups);
        }

        return curMatchedGroup;
    }

    private static ThreadGroup findNamedIn(String groupName, ThreadGroup[] groups) {
        for (ThreadGroup group : groups) {
            if (group != null && groupName.equals(group.getName())) {
                return group;
            }
        }
        return null;
    }

    private static ThreadGroup getSystemThreadGroup() {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        while (tg.getParent() != null) {
            tg = tg.getParent();
        }
        return tg;
    }

    private static String getThreadGroupsPath(ThreadGroup tg) {
        List<ThreadGroup> groups = new ArrayList<ThreadGroup>(4);
        while (tg != null) {
            groups.add(tg);
            tg = tg.getParent();
        }
        List<String> groupNamesReversed = new ArrayList<String>(groups.size());
        for (int i = groups.size() - 1; i >= 0; i--) {
            groupNamesReversed.add(groups.get(i).getName());
        }
        return join(groupNamesReversed, SEPARATOR_CHAR, ESCAPE_CHAR);
    }

    private static String join(Iterable<String> parts, char separatorChar, char escapeChar) {
        StringBuilder sb = new StringBuilder();
        char[] charsToEscape = escapeChar == separatorChar ? new char[]{escapeChar} : new char[]{escapeChar, separatorChar};
        for (String part : parts) {
            appendEscaped(sb, part, escapeChar, charsToEscape);
            sb.append(separatorChar);
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private static List<String> split(String str, char separatorChar, char escapeChar) {
        List<String> result = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();

        boolean escaped = false;
        for (char ch : str.toCharArray()) {
            if (escaped) {
                escaped = false;
            } else if (ch == escapeChar) {
                escaped = true;
                continue;
            } else if (ch == separatorChar) {
                result.add(sb.toString());
                sb.setLength(0);
                continue;
            }
            sb.append(ch);
        }
        result.add(sb.toString());

        return result;
    }

    private static void appendEscaped(StringBuilder sb, String str, char escapeChar, char...charsToEscape) {
        for (char ch : str.toCharArray()) {
            if (isInArray(ch, charsToEscape)) {
                sb.append(escapeChar);
            }
            sb.append(ch);
        }
    }

    private static boolean isInArray(char ch, char[] charArr) {
        for (char element : charArr) {
            if (ch == element) {
                return true;
            }
        }
        return false;
    }
}
