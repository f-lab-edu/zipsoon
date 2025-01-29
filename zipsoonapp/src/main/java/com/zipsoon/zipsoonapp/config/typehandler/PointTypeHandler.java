package com.zipsoon.zipsoonapp.config.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.postgis.PGgeometry;
import org.postgis.Point;

import java.sql.*;

@MappedTypes(Point.class)
public class PointTypeHandler extends BaseTypeHandler<Point> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Point parameter, JdbcType jdbcType)
            throws SQLException {
        String pointWkt = String.format("POINT(%f %f)", parameter.getX(), parameter.getY());
        ps.setObject(i, pointWkt, Types.OTHER);
    }

    @Override
    public Point getNullableResult(ResultSet rs, String columnName) throws SQLException {
        PGgeometry geometry = (PGgeometry) rs.getObject(columnName);
        return geometry != null ? convertToPoint(geometry) : null;
    }

    @Override
    public Point getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        PGgeometry geometry = (PGgeometry) rs.getObject(columnIndex);
        return geometry != null ? convertToPoint(geometry) : null;
    }

    @Override
    public Point getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        PGgeometry geometry = (PGgeometry) cs.getObject(columnIndex);
        return geometry != null ? convertToPoint(geometry) : null;
    }

    private Point convertToPoint(PGgeometry geometry) {
        org.postgis.Point pgPoint = (org.postgis.Point) geometry.getGeometry();
        return new Point(pgPoint.x, pgPoint.y);
    }
}