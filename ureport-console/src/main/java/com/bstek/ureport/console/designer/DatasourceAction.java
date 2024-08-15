package com.bstek.ureport.console.designer;

import com.bstek.ureport.Utils;
import com.bstek.ureport.build.Context;
import com.bstek.ureport.console.BaseAction;
import com.bstek.ureport.console.exception.ReportDesignException;
import com.bstek.ureport.definition.dataset.Field;
import com.bstek.ureport.definition.datasource.BuildinDatasource;
import com.bstek.ureport.definition.datasource.DataType;
import com.bstek.ureport.expression.ExpressionUtils;
import com.bstek.ureport.expression.model.Expression;
import com.bstek.ureport.expression.model.data.ExpressionData;
import com.bstek.ureport.expression.model.data.ObjectExpressionData;
import com.bstek.ureport.utils.ProcedureUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jacky.gao
 * @since 2017年2月6日
 */
@RestController
@RequestMapping("ureport/datasource")
public class DatasourceAction extends BaseAction {

    @Resource
    ApplicationContext applicationContext;

    @RequestMapping("loadBuildinDatasources")
    public List<String> loadBuildinDatasources() {
        List<String> datasources = new ArrayList<>();

        Collection<BuildinDatasource> values = applicationContext.getBeansOfType(BuildinDatasource.class).values();

        for (BuildinDatasource datasource : values) {
            datasources.add(datasource.name());
        }
        return datasources;
    }

    @RequestMapping("loadMethods")
    public List<String> loadMethods(String beanId) {
        Object obj = applicationContext.getBean(beanId);
        Class<?> clazz = obj.getClass();
        Method[] methods = clazz.getMethods();
        List<String> result = new ArrayList<String>();
        for (Method method : methods) {
            Class<?>[] types = method.getParameterTypes();
            if (types.length != 3) {
                continue;
            }
            Class<?> typeClass1 = types[0]; // dsName
            Class<?> typeClass2 = types[1]; // datasetName
            Class<?> typeClass3 = types[2]; // parameters
            if (!String.class.isAssignableFrom(typeClass1)) {
                continue;
            }
            if (!String.class.isAssignableFrom(typeClass2)) {
                continue;
            }
            if (!Map.class.isAssignableFrom(typeClass3)) {
                continue;
            }
            result.add(method.getName());
        }
        return result;
    }

    /**
     *
     * 获得bean的方法，以及其返回对象记录的className
     * 这样就不用手动输入返回对象vo了
     *
     * deepls 新增接口， react版使用
     *
     * @param beanId
     * @return
     */
    @RequestMapping("loadMethodsAndClazz")
    public List<Map<String, String>> loadMethodsAndClazz(String beanId) {
        Object obj = applicationContext.getBean(beanId);
        Class<?> clazz = obj.getClass();
        Method[] methods = clazz.getMethods();
        List<Map<String,String>> result = new ArrayList<>();
        for (Method method : methods) {
            Class<?>[] types = method.getParameterTypes();
            if (types.length != 3) {
                continue;
            }
            Class<?> typeClass1 = types[0]; // dsName
            Class<?> typeClass2 = types[1]; // datasetName
            Class<?> typeClass3 = types[2]; // parameters
            if (!String.class.isAssignableFrom(typeClass1)) {
                continue;
            }
            if (!String.class.isAssignableFrom(typeClass2)) {
                continue;
            }
            if (!Map.class.isAssignableFrom(typeClass3)) {
                continue;
            }
            Type genericReturnType = method.getGenericReturnType();

            String typeName = genericReturnType.getTypeName();

            //  java.util.List<com.bstek.ureport.console.designer.DemoSpringDatasourceBean$DemoUser>
            typeName = typeName.replace("java.util.List<","").replace(">","");

            HashMap<String, String> item = new HashMap<>();
            item.put("method", method.getName());
            item.put("clazz", typeName);
            result.add(item);
        }
        return result;
    }

    /**
     * spring数据源，返回一个类的字段
     * @param clazz
     * @return
     */
    @RequestMapping("buildClass")
    public List<Field> buildClass(String clazz) {
        List<Field> result = new ArrayList<>();
        try {
            Class<?> targetClass = Class.forName(clazz);
            PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(targetClass);
            for (PropertyDescriptor pd : propertyDescriptors) {
                String name = pd.getName();
                if ("class".equals(name)) {
                    continue;
                }
                result.add(new Field(name));
            }
            // 字段排序 ， 从短到长，从小到大
            result.sort((o1, o2) -> {
                String n1 = o1.getName();
                String n2 = o2.getName();
                if(n1.length() == n2.length()){
                    return  n1.compareTo(n2);
                }
                return n1.length() - n2.length();
            });

            return result;
        } catch (Exception ex) {
            throw new ReportDesignException(ex);
        }
    }


    @RequestMapping("buildDatabaseTables")
    public List<Map<String, String>> buildDatabaseTables(String type,
                                                         String username,
                                                         String password ,
                                                         String driver,
                                                         String url,
                                                         String name) throws ServletException {
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = buildConnection(type,username,password,driver, url, name);
            DatabaseMetaData metaData = conn.getMetaData();
             url = metaData.getURL();
            String schema = null;
            if (url.toLowerCase().contains("oracle")) {
                schema = metaData.getUserName();
            }
            List<Map<String, String>> tables = new ArrayList<Map<String, String>>();
            rs = metaData.getTables(null, schema, "%", new String[]{"TABLE", "VIEW"});
            while (rs.next()) {
                Map<String, String> table = new HashMap<String, String>();
                table.put("name", rs.getString("TABLE_NAME"));
                table.put("type", rs.getString("TABLE_TYPE"));
                tables.add(table);
            }
            return tables;
        } catch (Exception ex) {
            throw new ServletException(ex);
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeConnection(conn);
        }
    }

    @RequestMapping("buildFields")
    public List<Field> buildFields(String sql, String parameters, String type,
                                   String username,
                                   String password ,
                                   String driver,
                                   String url,
                                   String name) {
        Connection conn = null;
        final List<Field> fields = new ArrayList<>();
        try {
            conn = buildConnection(type,username,password,driver,url,name);
            Map<String, Object> map = buildParameters(parameters);
            sql = parseSql(sql, map);
            if (ProcedureUtils.isProcedure(sql)) {
                List<Field> fieldsList = ProcedureUtils.procedureColumnsQuery(sql, map, conn);
                fields.addAll(fieldsList);
            } else {
                DataSource dataSource = new SingleConnectionDataSource(conn, false);
                NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(dataSource);
                PreparedStatementCreator statementCreator = getPreparedStatementCreator(sql, new MapSqlParameterSource(map));
                jdbc.getJdbcOperations().execute(statementCreator, new PreparedStatementCallback<Object>() {
                    @Override
                    public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                        ResultSet rs = null;
                        try {
                            rs = ps.executeQuery();
                            ResultSetMetaData metadata = rs.getMetaData();
                            int columnCount = metadata.getColumnCount();
                            for (int i = 0; i < columnCount; i++) {
                                String columnName = metadata.getColumnLabel(i + 1);
                                fields.add(new Field(columnName));
                            }
                            return null;
                        } finally {
                            JdbcUtils.closeResultSet(rs);
                        }
                    }
                });
            }
            return fields;
        } catch (Exception ex) {
            throw new ReportDesignException(ex);
        } finally {
            JdbcUtils.closeConnection(conn);
        }
    }

    protected PreparedStatementCreator getPreparedStatementCreator(String sql, SqlParameterSource paramSource) {
        ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
        String sqlToUse = NamedParameterUtils.substituteNamedParameters(parsedSql, paramSource);
        Object[] params = NamedParameterUtils.buildValueArray(parsedSql, paramSource, null);
        List<SqlParameter> declaredParameters = NamedParameterUtils.buildSqlParameterList(parsedSql, paramSource);
        PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(sqlToUse, declaredParameters);
        return pscf.newPreparedStatementCreator(params);
    }

    @RequestMapping("previewData")
    public DataResult previewData(String sql, String parameters, HttpServletRequest req,

                                  String type,
                                  String username,
                                  String password ,
                                  String driver,
                                  String url,
                                  String name) throws ServletException, IOException {
        String oldSql = req.getParameter("sql");
        System.out.println(oldSql);
        String newSql = decode(sql);

        Map<String, Object> map = buildParameters(parameters);
        sql = parseSql(sql, map);
        Connection conn = null;
        try {
            conn = buildConnection(type,username,password,driver,url,name);
            List<Map<String, Object>> list = null;
            if (ProcedureUtils.isProcedure(sql)) {
                list = ProcedureUtils.procedureQuery(sql, map, conn);
            } else {
                DataSource dataSource = new SingleConnectionDataSource(conn, false);
                NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(dataSource);
                list = jdbc.queryForList(sql, map);
            }
            int size = list.size();
            int currentTotal = size;
            if (currentTotal > 500) {
                currentTotal = 500;
            }
            List<Map<String, Object>> ls = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < currentTotal; i++) {
                ls.add(list.get(i));
            }
            DataResult result = new DataResult();
            List<String> fields = new ArrayList<String>();
            if (size > 0) {
                Map<String, Object> item = list.get(0);
                for (String f : item.keySet()) {
                    fields.add(f);
                }
            }
            result.setFields(fields);
            result.setCurrentTotal(currentTotal);
            result.setData(ls);
            result.setTotal(size);
            return result;
        } catch (Exception ex) {
            throw new ServletException(ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String parseSql(String sql, Map<String, Object> parameters) {
        sql = sql.trim();
        Context context = new Context(applicationContext, parameters);
        if (sql.startsWith(ExpressionUtils.EXPR_PREFIX) && sql.endsWith(ExpressionUtils.EXPR_SUFFIX)) {
            sql = sql.substring(2, sql.length() - 1);
            Expression expr = ExpressionUtils.parseExpression(sql);
            sql = executeSqlExpr(expr, context);
            return sql;
        } else {
            String sqlForUse = sql;
            Pattern pattern = Pattern.compile("\\$\\{.*?\\}");
            Matcher matcher = pattern.matcher(sqlForUse);
            while (matcher.find()) {
                String substr = matcher.group();
                String sqlExpr = substr.substring(2, substr.length() - 1);
                Expression expr = ExpressionUtils.parseExpression(sqlExpr);
                String result = executeSqlExpr(expr, context);
                sqlForUse = sqlForUse.replace(substr, result);
            }
            Utils.logToConsole("DESIGN SQL:" + sqlForUse);
            return sqlForUse;
        }
    }

    private String executeSqlExpr(Expression sqlExpr, Context context) {
        String sqlForUse = null;
        ExpressionData<?> exprData = sqlExpr.execute(null, null, context);
        if (exprData instanceof ObjectExpressionData) {
            ObjectExpressionData data = (ObjectExpressionData) exprData;
            Object obj = data.getData();
            if (obj != null) {
                String s = obj.toString();
                s = s.replaceAll("\\\\", "");
                sqlForUse = s;
            }
        }
        return sqlForUse;
    }

    @RequestMapping("testConnection")
    public Map<String, Object> testConnection(String username, String password, String driver, String url) throws ServletException, IOException {

        Connection conn = null;
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
            map.put("result", true);
        } catch (Exception ex) {
            map.put("error", ex.toString());
            map.put("result", false);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildParameters(String parameters) throws IOException {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(parameters)) {
            return map;
        }
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> list = mapper.readValue(parameters, ArrayList.class);
        for (Map<String, Object> param : list) {
            String name = param.get("name").toString();
            DataType type = DataType.valueOf(param.get("type").toString());
            String defaultValue = (String) param.get("defaultValue");
            if (defaultValue == null || defaultValue.equals("")) {
                switch (type) {
                    case Boolean:
                        map.put(name, false);
                    case Date:
                        map.put(name, new Date());
                    case Float:
                        map.put(name, new Float(0));
                    case Integer:
                        map.put(name, 0);
                    case String:
                        if (defaultValue != null && defaultValue.equals("")) {
                            map.put(name, "");
                        } else {
                            map.put(name, "null");
                        }
                        break;
                    case List:
                        map.put(name, new ArrayList<>());
                }
            } else {
                map.put(name, type.parse(defaultValue));
            }
        }
        return map;
    }

    private Connection buildConnection( String type,
                                        String username,
                                        String password ,
                                        String driver,
                                        String url,
                                        String name
                                        ) throws Exception {
        if (type.equals("jdbc")) {
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url, username, password);
            return conn;
        } else {
            Connection conn = Utils.getBuildinConnection(name);
            if (conn == null) {
                throw new ReportDesignException("Buildin datasource [" + name + "] not exist.");
            }
            return conn;
        }
    }


}
