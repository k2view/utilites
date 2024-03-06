package com.k2view.broadway.actors.masking;

import com.k2view.broadway.Broadway;
import com.k2view.broadway.exception.BroadwayException;
import com.k2view.broadway.model.*;
import com.k2view.fabric.common.io.IoCommand;
import com.k2view.fabric.common.io.IoProvider;
import com.k2view.fabric.common.io.IoSession;
import com.k2view.fabric.common.io.IoSessionDelegate;
import com.k2view.fabric.common.io.basic.BasicIoProvider;

import java.util.*;
import java.util.stream.Stream;

public class MaskingTestUtils {
    public static final String JDBC_SQLITE = "jdbc:sqlite:";

    public static Context getMockContext() throws Exception {
        return getMockContext(new Masking());
    }

    public static Context getMockContext(Actor actor) throws Exception {
        return new DefaultContext() {
            @Override
            public Broadway broadway() {
                return new Broadway() {
                    @Override
                    public Flow deserializeFlow(String json) throws BroadwayException {
                        return null;
                    }

                    @Override
                    public Flow createFlow(String flowName) throws BroadwayException {
                        return new Flow() {
                            @Override
                            public Data act(Data flowArgs, ExecutionContext executionContext) throws BroadwayException {
                                return null;
                            }

                            @Override
                            public Data act(Data flowArgs, Context context) throws BroadwayException {
                                Data output = Data.create();
                                try {
                                    actor.action(flowArgs, output, context);
                                    return output;
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            @Override
                            public void abort(boolean force) throws BroadwayException {

                            }

                            @Override
                            public String getFlowName() {
                                return null;
                            }

                            @Override
                            public Model model() throws BroadwayException {
                                return new Model() {
                                    @Override
                                    public Stream<Connection> outputs() {
                                        return Stream.empty();
                                    }

                                    @Override
                                    public Stream<Connection> inputs() {
                                        return Stream.empty();
                                    }

                                    @Override
                                    public Stream<Level> levels() {
                                        return Stream.empty();
                                    }

                                    @Override
                                    public Stream<Connection> connections() {
                                        return Stream.empty();
                                    }

                                    @Override
                                    public String description() {
                                        return "";
                                    }

                                    @Override
                                    public String[] tags() {
                                        return new String[0];
                                    }
                                };
                            }

                            @Override
                            public void close() {

                            }
                        };
                    }
                };
            }

            IoSession sqliteSession;
            @Override
            public IoProvider ioProvider() {
                return new IoProvider() {
                    @Override
                    public IoSession createSession(String s, Map<String, Object> map) throws Exception {
                        if (JDBC_SQLITE.equals(s)) {
                            if (sqliteSession == null) {
                                sqliteSession = getMaskingSqliteSession();
                            }
                            return sqliteSession;
                        } else if ("fabric".equals(s)) {
                            Map<String, Map<String, Object>> commands = new HashMap<String, Map<String, Object>>() {{
                                put("set scope;", new HashMap<String, Object>() {{
                                    put("value", "test_scope");
                                }});
                                put("set execution_id;", new HashMap<String, Object>() {{
                                    put("value", "test_execution_id");
                                }});
                            }};
                            return getMockFabricSession(commands);
                        } else {
                            return null;
                        }
                    }

                    @Override
                    public <T extends IoProvider> T unwrap(Class<T> aClass) {
                        return null;
                    }

                };
            }
        };
    }

    public static IoSession getMockFabricSession(Map<String, Map<String, Object>> commands) {
        HashMap<String, IoCommand.Statement> statements = new HashMap<>();
        commands.keySet().forEach(command -> statements.put(command, getPrepareStatement(commands.get(command))));

        return new IoSessionDelegate(null) {
            @Override
            public Statement prepareStatement(String command) throws Exception {
                return statements.get(command);
            }
        };
    }

    private static IoCommand.Statement getPrepareStatement(Map<String, Object> results) {
        return objects -> new IoCommand.Result() {
            public Iterator<IoCommand.Row> iterator() {
                List<IoCommand.Row> rows = new ArrayList<>();
                results.keySet().forEach(key -> rows.add(getRow(results)));
                return rows.iterator();
            }
        };
    }

    public static IoCommand.Row getRow(Map<String, Object> values) {
        return new IoCommand.Row() {

            @Override
            public Object get(Object key) {
                return values.get(key);
            }

            @Override
            public int size() {
                return values.size();
            }

            @Override
            public boolean isEmpty() {
                return values.isEmpty();
            }

            @Override
            public boolean containsKey(Object key) {
                return values.containsKey(key);
            }

            @Override
            public boolean containsValue(Object value) {
                return values.containsValue(value);
            }

            @Override
            public Object put(String key, Object value) {
                return values.put(key, value);
            }

            @Override
            public Object remove(Object key) {
                return values.remove(key);
            }

            @Override
            public void putAll(Map<? extends String, ?> m) {
                values.putAll(m);
            }

            @Override
            public void clear() {
                values.clear();
            }

            @Override
            public Set<String> keySet() {
                return values.keySet();
            }

            @Override
            public Collection<Object> values() {
                return values.values();
            }

            @Override
            public Set<Entry<String, Object>> entrySet() {
                return values.entrySet();
            }
        };
    }


    public static IoSession getMaskingSqliteSession() throws Exception {
        IoSession session = getSqliteWithKeyspaceSession("k2masking");

        session.prepareStatement("CREATE TABLE \"k2masking\".\"masking_cache\" " +
                "(" +
                "\"environment\" TEXT, " +
                "\"execution_id\" TEXT, " +
                "\"masking_id\" TEXT, " +
                "\"instance_id\" TEXT, " +
                "\"clone_id\" TEXT, " +
                "\"original_value_hash\" TEXT, " +
                "\"masked_value\" TEXT, " +
                "PRIMARY KEY(\"execution_id\", \"masking_id\", \"instance_id\", \"clone_id\", \"original_value_hash\")" +
                ");").execute();

        session.prepareStatement("CREATE TABLE 'k2masking'.'uniqueness' " +
                "(" +
                "'environment' TEXT, " +
                "'execution_id' TEXT, " +
                "'masking_id' TEXT, " +
                "'masked_value' TEXT, " +
                "PRIMARY KEY ('execution_id', 'masking_id', 'masked_value')" +
                ");").execute();

        return session;
    }

    public static IoSession getSqliteWithKeyspaceSession(String keyspaceName) throws Exception {
        IoProvider sf = new BasicIoProvider();
        IoSession session = sf.createSession("jdbc:sqlite:");
        session.prepareStatement("attach database '' as " + keyspaceName).execute();
        return session;
    }

    public static Context getTestContextForFlowExecution(Data outputData) {
        return new DefaultContext() {
            @Override
            public Broadway broadway() {
                return new Broadway() {
                    @Override
                    public Flow deserializeFlow(String json) throws BroadwayException {
                        return null;
                    }

                    @Override
                    public Flow createFlow(String flowName) throws BroadwayException {
                        return new FlowImp() {
                            @Override
                            public Data act(Data flowArgs, Context context) throws BroadwayException {
                                return outputData;
                            }

                            @Override
                            public Data act() throws BroadwayException {
                                return outputData;
                            }

                            @Override
                            public Data act(Data flowArgs, ExecutionContext executionContext) throws BroadwayException {
                                return outputData;
                            }
                        };
                    }
                };
            }
        };
    }

}
