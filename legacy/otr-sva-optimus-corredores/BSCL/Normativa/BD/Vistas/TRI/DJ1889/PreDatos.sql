SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1889.PreDatos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1889.PreDatos" (
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
    d."ID",
    d."IdDocumento",
    d."VboUsuario",
    COALESCE(VAL."CodigoValidacion", d."Observaciones") AS "Observaciones",
    d."FuenteDatos",
    ----
    d."Numero",
    d."RutCliente",
    d."IdCliente",
    d."FechaOperacion",
    d."TipoAhorro",
    ---
    d."MontoNominal",
    --
    d."RentabilidadPositiva",
    d."RentabilidadNegativa",
    --Actualizada ($)
    d."Factor",
    d."RentabilidadActualizadaPositiva",
    d."RentabilidadActualizadaNegativa",
    --
    d."NumeroCertificado",
    d."EjecutadoComplementoIPC"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1889" d
LEFT JOIN (
           	SELECT "CodigoValidacion", "IdDocumento" ,"RutCliente","IdCliente", "TipoAhorro","FechaOperacion"
			FROM (
			    SELECT 
			        R."CodigoValidacion", 
			        DT."IdDocumento",
			        DT."RutCliente", 
			        DT."IdCliente",
			        DT."TipoAhorro",
			        DT."FechaOperacion",
			        ROW_NUMBER() OVER (
			            PARTITION BY DT."IdCliente" 
			            ORDER BY R."ID" ASC
			        ) AS rn
			    FROM "BSCL.Normativa.BD::TRI.ResumenValidacion" AS R
			    INNER JOIN "BSCL.Normativa.BD::TRI.DatosDJ1889" AS DT ON DT."IdDocumento" = R."IdDocumento"
			    INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" AS DV ON DV."IdResumenValidacion" = R."ID" AND DV."IdDetalleDocumento" = DT."ID" 
			) AS t
			WHERE t.rn = 1
        ) AS VAL ON  d."IdDocumento" = VAL."IdDocumento" AND d."RutCliente" = VAL."RutCliente" 
        	AND d."IdCliente" = VAL."IdCliente" AND d."TipoAhorro" = VAL."TipoAhorro" AND YEAR(d."FechaOperacion") = YEAR(VAL."FechaOperacion")
with read only;

grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1889.PreDatos" to bcs_desarrollador with grant option;
