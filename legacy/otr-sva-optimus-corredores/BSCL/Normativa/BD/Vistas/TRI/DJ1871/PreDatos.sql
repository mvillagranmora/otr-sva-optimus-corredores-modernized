SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1871.PreDatos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1871.PreDatos" (
    "ID",
    "IdDocumento",
    "VboUsuario",
    "VboUsuarioDescripcion",
    "Observaciones",
    "FuenteDatos",
    ----
    "Numero",
    "RutCliente",
    "IdCliente",
    "FechaOperacion",
    "AnoAhorro",
    --Ahorros
    "AhorrosTrabajador",
    "AhorrosDeposito",
    "AhorrosCotizaciones",
    --Retiros
    "RetirosTrabajador",
    "RetirosDeposito",
    "RetirosCotizaciones",
    "ComplementoIPC"
) AS
SELECT
    a."ID",
    a."IdDocumento",
    a."VboUsuario",
    COALESCE(b."Descripcion",'') 				AS	"VboUsuarioDescripcion",
    COALESCE(VAL."CodigoValidacion", a."Observaciones") AS "Observaciones",
    a."FuenteDatos",
    ----
    a."Numero",
    a."RutCliente",
    a."IdCliente",
    a."FechaOperacion",
    a."AnoAhorro",
    --Ahorros
    COALESCE(a."AhorrosTrabajadorActualizado",a."AhorrosTrabajador") AS "AhorrosTrabajador",
    COALESCE(a."AhorrosDepositoActualizado",a."AhorrosDeposito") AS "AhorrosDeposito",
    COALESCE(a."AhorrosCotizacionesActualizado",a."AhorrosCotizaciones") AS "AhorrosCotizaciones",
    --Retiros
    COALESCE(a."RetirosTrabajadorActualizado",a."RetirosTrabajador") AS "RetirosTrabajador",
    COALESCE(a."RetirosDepositoActualizado",a."RetirosDeposito") AS "RetirosDeposito",
    COALESCE(a."RetirosCotizacionesActualizado",a."RetirosCotizaciones") AS "RetirosCotizaciones",
    a."EjecutadoComplementoIPC" AS "ComplementoIPC"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1871" a
left join "BSCL.Normativa.BD::DOC.Diccionario" b
    on b."Tipo"='VistoBueno' and a."VboUsuario"=b."Codigo"
LEFT JOIN (
           	SELECT "CodigoValidacion", "IdDocumento" ,"RutCliente","IdCliente", "AnoAhorro","EjecutadoComplementoIPC"
			FROM (
			    SELECT 
			        R."CodigoValidacion", 
			        DT."IdDocumento",
			        DT."RutCliente", 
			        DT."IdCliente",
			        DT."AnoAhorro",
			        DT."EjecutadoComplementoIPC",
			        ROW_NUMBER() OVER (
			            PARTITION BY DT."IdCliente" 
			            ORDER BY R."ID" ASC
			        ) AS rn
			    FROM "BSCL.Normativa.BD::TRI.ResumenValidacion" AS R
			    INNER JOIN "BSCL.Normativa.BD::TRI.DatosDJ1871" AS DT ON DT."IdDocumento" = R."IdDocumento"
			    INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" AS DV ON DV."IdResumenValidacion" = R."ID" AND DV."IdDetalleDocumento" = DT."ID" 
			) AS t
			WHERE t.rn = 1
        ) AS VAL ON  a."IdDocumento" = VAL."IdDocumento" AND a."RutCliente" = VAL."RutCliente" 
        	AND a."IdCliente" = VAL."IdCliente" AND a."AnoAhorro" = VAL."AnoAhorro" AND a."EjecutadoComplementoIPC" = VAL."EjecutadoComplementoIPC"
with read only;

grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1871.PreDatos" to bcs_desarrollador with grant option;
