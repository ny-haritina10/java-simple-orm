package mg.itu.base;

import java.sql.*;
import java.util.*;
import java.lang.reflect.Array;

import mg.itu.db.Database;
import mg.itu.helpers.Utils;

public abstract class BaseModel<T> {

    @SuppressWarnings("unchecked")
    public T[] getAll(Class<T> clazz, Connection con, String tableName)
        throws Exception
    {
        Vector<T> list= new Vector<T>();

        boolean valid = true;
        Statement state = null;
        ResultSet result = null;

        try {
            if(con == null) {
                con = Database.getConnection();
                valid = false;
            }

            String sql = "SELECT * FROM " + (tableName != null ? tableName : this.getClass().getSimpleName() + " ORDER BY id");
            state = con.createStatement();
            result = state.executeQuery(sql);

            while(result.next()) {
                T item = mapRow(result);
                list.add(item);
            }
        } 
        catch (Exception e) 
        { e.printStackTrace(); }
        finally {
            try {
                if (state != null) state.close(); 
                if (result != null ) result.close(); 
                if (valid == false || con !=null) con.close(); 
            } 
            catch (Exception e) 
            { e.printStackTrace(); }
        }

        T[] items = (T[]) Array.newInstance(clazz, list.size());
        list.toArray(items);

        return items;
    }

    public T[] getAll(Class<T> clazz, Connection con) 
        throws Exception 
    {
        return getAll(clazz, con, null);
    }

    public T getById(int id, Class<T> clazz, Connection con, String tableName) 
        throws Exception 
    {
        T item = null;
        boolean valid = true;
        PreparedStatement state = null;
        ResultSet result = null;

        try {
            if(con == null) {
                con = Database.getConnection();
                valid = false;
            }

            String actualTableName = tableName != null ? tableName : this.getClass().getSimpleName();
            String sql = "SELECT * FROM " + actualTableName + " WHERE id = ?";

            state = con.prepareStatement(sql);
            state.setInt(1, id);

            result = state.executeQuery();

            if(result.next()) 
            { item = mapRow(result); }
        } 
        catch (Exception e) 
        { e.printStackTrace(); } 
        finally {
            try {
                if (state != null) state.close(); 
                if (result != null) result.close(); 
                if (!valid || con != null) con.close(); 
            } 
            catch (Exception e) 
            { e.printStackTrace(); }
        }

        return item;
    }

    public T getById(int id, Class<T> clazz, Connection con) 
        throws Exception 
    {
        return getById(id, clazz, con, null);
    }

    public Integer getMaxId(Connection con, String tableName) 
        throws Exception 
    {
        Integer maxId = null;
        boolean valid = true;
        Statement state = null;
        ResultSet result = null;
    
        try {
            if (con == null) {
                con = Database.getConnection();
                valid = false;
            }
    
            String actualTableName = tableName != null ? tableName : this.getClass().getSimpleName();
            String sql = "SELECT MAX(id) AS max_id FROM " + actualTableName;
    
            state = con.createStatement();
            result = state.executeQuery(sql);
    
            if (result.next()) {
                maxId = result.getInt("max_id");
            }
        } 

        catch (Exception e) 
        { e.printStackTrace(); } 

        finally {
            try {
                if (state != null) state.close();
                if (result != null) result.close();
                if (!valid || con != null) con.close();
            } 
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    
        return maxId;
    }    

    public boolean save(Connection con, String tableName) 
        throws Exception 
    {
        boolean valid = true;
        PreparedStatement state = null;
        ResultSet generatedKeys = null;
        
        try {
            if(con == null) {
                con = Database.getConnection();
                valid = false;
            }
            
            String actualTableName = tableName != null ? tableName : this.getClass().getSimpleName();
            Map<String, Object> fields = getFieldsMap();
            
            StringBuilder columns = new StringBuilder();
            StringBuilder values = new StringBuilder();
            List<Object> parameterValues = new ArrayList<>();
            
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                if (entry.getKey().equals("id")) continue; // Skip ID for insert
                
                if (columns.length() > 0) {
                    columns.append(", ");
                    values.append(", ");
                }
                columns.append(entry.getKey());
                values.append("?");
                parameterValues.add(entry.getValue());
            }
            
            String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", 
                actualTableName, columns.toString(), values.toString());
            
            state = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            for (int i = 0; i < parameterValues.size(); i++) {
                Utils.setParameter(state, i + 1, parameterValues.get(i));
            }
            
            int result = state.executeUpdate();
            
            if (result > 0) {
                generatedKeys = state.getGeneratedKeys();
                if (generatedKeys.next()) {
                    setId(generatedKeys.getInt(1));
                }
                return true;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (state != null) state.close();
                if (generatedKeys != null) generatedKeys.close();
                if (!valid || con != null) con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean update(Connection con, String tableName) 
        throws Exception 
    {
        boolean valid = true;
        PreparedStatement state = null;
        
        try {
            if(con == null) {
                con = Database.getConnection();
                valid = false;
            }
            
            String actualTableName = tableName != null ? tableName : this.getClass().getSimpleName();
            Map<String, Object> fields = getFieldsMap();
            
            StringBuilder setClause = new StringBuilder();
            List<Object> parameterValues = new ArrayList<>();
            
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                if (entry.getKey().equals("id")) continue;
                
                if (setClause.length() > 0) {
                    setClause.append(", ");
                }
                setClause.append(entry.getKey()).append(" = ?");
                parameterValues.add(entry.getValue());
            }
            
            String sql = String.format("UPDATE %s SET %s WHERE id = ?", 
                actualTableName, setClause.toString());
            
            state = con.prepareStatement(sql);
            
            for (int i = 0; i < parameterValues.size(); i++) {
                Utils.setParameter(state, i + 1, parameterValues.get(i));
            }
            state.setInt(parameterValues.size() + 1, getId());
            
            return state.executeUpdate() > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (state != null) state.close();
                if (!valid || con != null) con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean delete(Connection con, String tableName) 
        throws Exception 
    {
        boolean valid = true;
        PreparedStatement state = null;
        
        try {
            if(con == null) {
                con = Database.getConnection();
                valid = false;
            }
            
            String actualTableName = tableName != null ? tableName : this.getClass().getSimpleName();
            String sql = "DELETE FROM " + actualTableName + " WHERE id = ?";
            
            state = con.prepareStatement(sql);
            state.setInt(1, getId());
            
            return state.executeUpdate() > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (state != null) state.close();
                if (!valid || con != null) con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Abstract methods that need to be implemented by child classes
    protected abstract T mapRow(ResultSet result) throws Exception;
    protected abstract Map<String, Object> getFieldsMap();
    protected abstract void setId(int id);

    public abstract int getId();
}