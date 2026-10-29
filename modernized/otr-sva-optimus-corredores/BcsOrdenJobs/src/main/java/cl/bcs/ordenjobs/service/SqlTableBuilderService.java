package cl.bcs.ordenjobs.service;

import cl.bcs.ordenjobs.domain.CampoSql;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SqlTableBuilderService {

    // B-ORD-01: replica de Campo.getCreateTableString — "Create table" casing exacto
    public static String crearTabla(List<CampoSql> campos, String tableName) {
        String sql = "Create table " + tableName + "(";
        for (int i = 0; i < campos.size(); i++) {
            sql += "\"" + campos.get(i).nombre() + "\"" + campos.get(i).tipo();
            if (i < campos.size() - 1) sql += ",\n";
        }
        sql += ");";
        return sql;
    }

    // B-ORD-02: replica de Campo.getDropTableString — CANARY: "drop" minúscula
    public static String eliminarTabla(String tableName) {
        return "drop table " + tableName;
    }

    // B-ORD-03: replica de Campo.getInsertBatchString(ArrayList<Campo>, String)
    public static String insertarPorLista(List<CampoSql> campos, String tableName) {
        String sql1 = "INSERT INTO " + tableName + "(";
        String sql2 = "VALUES (";
        for (int i = 0; i < campos.size(); i++) {
            sql1 += "\"" + campos.get(i).nombre() + "\"";
            sql2 += ":" + campos.get(i).nombre();
            if (i < campos.size() - 1) {
                sql1 += ",";
                sql2 += ",";
            }
        }
        sql1 += ")\n";
        sql2 += ")";
        return sql1 + sql2;
    }

    // B-ORD-04: replica de Campo.getInsertBatchString(Class, String)
    // Comportamiento pinned: campos con prefijo "_" se omiten; coma basada en índice
    // → bug de coma final cuando el último campo en getFields() tiene prefijo "_"
    public static String insertarPorClase(Class<?> clazz, String tableName) {
        var fields = clazz.getFields();
        String sql1 = "INSERT INTO " + tableName + "(";
        String sql2 = "VALUES (";
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().startsWith("_")) continue;
            sql1 += "\"" + fields[i].getName() + "\"";
            sql2 += ":" + fields[i].getName();
            if (i < fields.length - 1) {
                sql1 += ",";
                sql2 += ",";
            }
        }
        sql1 += ")\n";
        sql2 += ")";
        return sql1 + sql2;
    }
}
