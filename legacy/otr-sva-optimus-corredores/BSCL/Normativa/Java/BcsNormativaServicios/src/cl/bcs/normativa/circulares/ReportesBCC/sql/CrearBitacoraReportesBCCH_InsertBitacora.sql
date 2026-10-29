INSERT INTO "BSCL.Normativa.BD::NOR.BitacoraInterfaz"
("IdDocumento", "Esquema", "Version", "FolioOrigen", "TipoOrigen", 
 "FechaOrigen", "IDReferencia", "FechaEvento", "IdEstado", "Glosa", 
 "Codigo", "IdUsuario")
VALUES (
    :IdDocumento,
    :Esquema,
    :Version,
    :FolioOrigen,
    :TipoOrigen,
    :FechaOrigen,
    :IDReferencia,
    CURRENT_TIMESTAMP,
    :IdEstado,
    :Glosa,
    :Codigo,
    :IdUsuario
);