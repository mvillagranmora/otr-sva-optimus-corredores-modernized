UPSERT "BSCL.Normativa.BD::TRI.ResumenGeneracion"
("ID", "IdProceso", "IdDocumento", "IdUsuario", "Fecha", "Glosa", "TotalRegistrosGenerados", "NombreArchivo", "UltimoCorrelativo")
VALUES (
  (SELECT MAX("ID") + 1 FROM "BSCL.Normativa.BD::TRI.ResumenGeneracion"),
  (SELECT MAX("IdProceso") + 1 FROM "BSCL.Normativa.BD::TRI.ResumenGeneracion"),
  (
    SELECT "IdDocumento"
    FROM "BSCL.Normativa.BD::DOC.Documento"
    WHERE "Esquema" = :p_Esquema
      AND "Version" = :p_Version
  ),
  :p_IdUsuario,
  :p_Fecha,
  :p_Glosa,
  :p_Total,
  :p_NombreArchivo,
  0
)
WITH PRIMARY KEY;
