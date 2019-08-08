package io.burt.jmespath.vertx;

import io.burt.jmespath.BaseRuntime;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.RuntimeConfiguration;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.*;

import static io.burt.jmespath.JmesPathType.*;

public class VertxRuntime extends BaseRuntime<Object> {
    public VertxRuntime() {
        this(RuntimeConfiguration.defaultConfiguration());
    }

    public VertxRuntime(RuntimeConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Object parseString(String str) {
        return Json.decodeValue(str);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> toList(Object value) {
        if (value instanceof JsonArray) {
            final JsonArray ja = (JsonArray)value;
            return new AbstractList<Object>() {

                @Override
                public int size() {
                    return ja.size();
                }

                @Override
                public Object get(int i) {
                    return ja.getValue(i);
                }
            };
        }
        if (value instanceof JsonObject) {
            final JsonObject jo=(JsonObject)value;
            return new AbstractList<Object>() {
                // Keep a cache of the ordered list if random access comes up
                transient List<Object> l=null;

                @Override
                public int size() {
                    return jo.size();
                }

                @Override
                public Object get(int pos) {
                    if (l==null) {
                        l=new ArrayList<Object>(size());
                        Iterator it = iterator();
                        while (it.hasNext()) {
                            l.add(it.next());
                        }
                    }
                    return l.get(pos);
                }

                @Override
                public Iterator<Object> iterator() {
                    final Iterator<Map.Entry<String, Object>> entries = jo.iterator();
                    return new Iterator<Object>() {
                        @Override
                        public boolean hasNext() {
                            return entries.hasNext();
                        }

                        @Override
                        public Object next() {
                            return entries.next().getValue();
                        }

                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
            };
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public String toString(Object value) {
        if (value instanceof String) {
            return (String)value;
        } else if (value instanceof JsonArray || value instanceof JsonObject) {
            return value.toString();
        } else {
            return String.valueOf(value);
        }
    }

    @Override
    public Number toNumber(Object value) {
        return value instanceof Number?(Number)value:null;
    }

    /**
     * Returns true when the argument is truthy.
     *
     * All values are truthy, except the following, as per the JMESPath
     * specification: <code>false</code>, <code>null</code>, empty lists, empty
     * objects, empty strings.
     */
    @Override
    public boolean isTruthy(Object value) {
        if (value instanceof Boolean) {
            return (Boolean)value;
        } else if (value instanceof JsonArray) {
            return ((JsonArray)value).size() > 0;
        } else if (value instanceof JsonObject) {
            return ((JsonObject)value).size() > 0;
        } else if (value instanceof String) {
            return ((String)value).length() > 0;
        }
        return value != null;
    }

    @Override
    public JmesPathType typeOf(Object value) {
        if (value == null) {
            return NULL;
        } else if (value instanceof Boolean) {
            return BOOLEAN;
        } else if (value instanceof Number) {
            return NUMBER;
        } else if (value instanceof JsonObject) {
            return OBJECT;
        } else if (value instanceof JsonArray) {
            return ARRAY;
        } else if (value instanceof String) {
            return STRING;
        } else {
            throw new IllegalStateException(String.format("Unknown node type encountered: %s", value.getClass().getName()));
        }
    }

    @Override
    public Object getProperty(Object value, Object name) {
        if ((value instanceof JsonObject) && (name instanceof String)) {
            return ((JsonObject)value).getValue((String)name);
        } else {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Object> getPropertyNames(Object value) {
        if (value instanceof JsonObject) {
            JsonObject jo=(JsonObject)value;
            return (Collection)jo.getMap().keySet();
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public Object createNull() {
        return null;
    }

    @Override
    public Object createArray(Collection<Object> elements) {
        JsonArray ja = new JsonArray();
        Iterator it = elements.iterator();
        while (it.hasNext()) {
            ja.add(it.next());
        }
        return ja;
    }

    @Override
    public Object createString(String str) {
        return str;
    }

    @Override
    public Object createBoolean(boolean b) {
        return Boolean.valueOf(b);
    }

    @Override
    public Object createObject(Map<Object, Object> obj) {
        JsonObject jo = new JsonObject();
        Iterator<Map.Entry<Object,Object>> it = obj.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, Object> entry = it.next();
            jo.put((String)entry.getKey(), entry.getValue());
        }
        return jo;
    }

    @Override
    public Object createNumber(double n) {
        return Double.valueOf(n);
    }

    @Override
    public Object createNumber(long n) {
        return Long.valueOf(n);
    }

    @Override
    public int compare(Object value1, Object value2) {
        JmesPathType type1 = typeOf(value1);
        JmesPathType type2 = typeOf(value2);
        if (type1 == type2) {
            switch (type1) {
                case NULL:
                    return 0;
                case BOOLEAN:
                    return isTruthy(value1) == isTruthy(value2) ? 0 : -1;
                case NUMBER:
                    double d1 = toNumber(value1).doubleValue();
                    double d2 = toNumber(value2).doubleValue();
                    return Double.compare(d1, d2);
                case STRING:
                    return ((String)value1).compareTo((String)value2);
                case ARRAY:
                case OBJECT:
                    return value1.equals(value2) ? 0 : -1;
                default:
                    throw new IllegalStateException(String.format("Unknown node type encountered: %s", value1.getClass().getName()));
            }
        } else {
            return -1;
        }
    }
}
