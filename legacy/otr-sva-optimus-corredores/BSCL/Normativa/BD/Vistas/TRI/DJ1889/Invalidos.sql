SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1889.Invalidos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1889.Invalidos" (
    "CodigoValidacion",
    "ID",
    "IdDocumento",
    "VboUsuario",
    "Observaciones",
    "FuenteDatos",
    ----
    "Numero",
    "RutCliente",
    "IdCliente",
    "FechaOperacion",
    "TipoAhorro",
    ---
    "MontoNominal",
    --
    "RentabilidadPositiva",
    "RentabilidadNegativa",
    --Actualizada ($)
    "Factor",
    "RentabilidadActualizadaPositiva",
    "RentabilidadActualizadaNegativa",
    --
    "NumeroCertificado",
    "ComplementoIPC"
) AS
SELECT
    b."CodigoValidacion", a.*
FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1889.PreDatos" a
INNER JOIN "BSCL.Normativa.BD::TRI.ResumenValidacion" b ON (b."IdDocumento" = a."IdDocumento")
INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" c ON (c."IdResumenValidacion" = b."ID")
WHERE a."ID" = c."IdDetalleDocumento"
with read only;
    
grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1889.Invalidos" to bcs_desarrollador with grant option;
