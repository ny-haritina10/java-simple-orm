package mg.itu.helpers;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import mg.itu.base.BaseModel;

public class Utils {
    
    public static void setParameter(PreparedStatement statement, int index, Object value) 
        throws SQLException 
    {
        if (value == null) 
        { statement.setNull(index, Types.NULL); } 
        
        else if (value instanceof String) 
        { statement.setString(index, (String) value); } 
        
        else if (value instanceof Integer) 
        { statement.setInt(index, (Integer) value); } 
        
        else if (value instanceof Double) 
        { statement.setDouble(index, (Double) value); } 
        
        else if (value instanceof java.sql.Date) 
        { statement.setDate(index, (java.sql.Date) value); } 
        
        else if (value instanceof Timestamp) 
        { statement.setTimestamp(index, (Timestamp) value); } 
        
        else if (value instanceof Boolean) 
        { statement.setBoolean(index, (Boolean) value); } 
        
        else if (value instanceof BaseModel) 
        { statement.setInt(index, ((BaseModel<?>) value).getId()); }
    }
}