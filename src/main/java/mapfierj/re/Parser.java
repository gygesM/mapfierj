package mapfierj.re;

import mapfierj.MappingException;
import mapfierj.TypeConverterAdapter;
import org.reflections.Reflections;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Parser implements UnrealConverter {

    private Set<TypeConverterAdapter> adapters = new HashSet<>();

    public Parser() {
        final Reflections reflections = new Reflections(BASE_PACKAGE);
        reflections.getSubTypesOf(TypeConverterAdapter.class)
                .parallelStream().forEach((c -> adapters.add(InstanceCreator.create(c))));
    }

    public void scanPackages(String... packages) {
        for (String item : packages) {
            if (item != null && !item.trim().isEmpty()) {
                final Reflections reflections = new Reflections(item);
                reflections.getSubTypesOf(TypeConverterAdapter.class)
                        .parallelStream().forEach((c -> adapters.add(InstanceCreator.create(c))));
            }
        }
    }

    public void register(TypeConverterAdapter adapter) {
        adapters.add(adapter);
    }

    public Transaction set(Object o) {
        return new Transaction(o);
    }

    public class Transaction {

        private Object o;
        private Set<TypeConverterAdapter> validConverter = new HashSet<>();

        public Transaction(Object o) {
            this.o = o;
        }

        @SuppressWarnings("unchecked")
        public <T> T convertTo(Class<T> c) throws MappingException {
            adapters.parallelStream().filter(adapter -> {
                final Type types[] = (((ParameterizedType)
                        adapter.getClass().getGenericSuperclass()).getActualTypeArguments());
                return Arrays.asList(types).parallelStream()
                        .anyMatch(type -> type.toString().equals(o.getClass().toString()));
            }).filter(adapter -> {
                final Type types[] = (((ParameterizedType)
                        adapter.getClass().getGenericSuperclass()).getActualTypeArguments());
                return Arrays.asList(types).parallelStream()
                        .anyMatch(type -> type.toString().equals(c.toString()));
            }).forEach(adapter -> validConverter.add(adapter));

            if (validConverter.size() > 0) {
                while (validConverter.iterator().hasNext()) {
                    try {
                        return (T) validConverter.iterator().next().convert(o);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                throw new MappingException("Unable to convert!");
            } else {
                throw new MappingException("No converter match for your object!");
            }
        }

        @SuppressWarnings("unchecked")
        public <T> T convertBy(TypeConverterAdapter adapter) throws MappingException {
            try {
                return (T) adapter.convert(o);
            } catch (Exception e) {
                throw new MappingException("Unable to convert!");
            }
        }
    }
}