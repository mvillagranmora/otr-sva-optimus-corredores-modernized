package cl.bcs.replica.domain;

import java.sql.Timestamp;

public class Workstack {
    public String Key;
    public String Table;
    public String Action;
    public int Status;
    public Timestamp Stamp;
    public String Parameters;

    public static Workstack nuevo(String tabla, String parametros) {
        Workstack w = new Workstack();
        w.Key = "";
        w.Table = tabla;
        w.Action = "M";
        w.Parameters = parametros;
        w.Status = 0;
        return w;
    }
}
