SET SCHEMA "BSCL";
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFMParidadTC";

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFMParidadTC" AS

  SELECT
    WST."IdStack" AS IDSTACK,"Action" AS ACTION,
    Fecha || CodMoneda AS TABLE_KEY,
    Fecha, --DATETIME
    CodMoneda, --INT
    ParidadMercado, --DECIMAL
    ParidadSBIF, --DECIMAL
    TCMercado, --DECIMAL
    UsuarioUltMod, --DECIMAL
    FecUltMod, --DATETIME
    PrecioCompra, --DECIMAL
    PrecioVenta, --DECIMAL
    PrecioPromedio --DECIMAL
  FROM "BSCL.Replica.BD.Vistas::Sebra.FMParidadTC" PAR
  LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'FMParidadTC' AND WST."Key" = Fecha || CodMoneda
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFMParidadTC" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
