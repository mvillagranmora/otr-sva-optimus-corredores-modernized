SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1871.Datos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1871.Datos" (
    "ID",
    "IdDocumento",
    "Numero",
    "RutCliente",
    "IdCliente",
    "FechaOperacion",
    "AnoAhorro",
    "AhorrosTrabajador",
    "AhorrosDeposito",
    "AhorrosCotizaciones",
    "RetirosTrabajador",
    "RetirosDeposito",
    "RetirosCotizaciones",
    "ComplementoIPC"
) AS
SELECT
    TO_BIGINT("ID") AS "ID",
    TO_BIGINT("IdDocumento") AS "IdDocumento",
    TO_BIGINT("Numero") AS "Numero",
    CAST("RutCliente" AS NVARCHAR(40)) AS "RutCliente",
    TO_BIGINT("IdCliente") AS "IdCliente",
    TO_DATE("FechaOperacion") AS "FechaOperacion",
    TO_BIGINT("AnoAhorro") AS "AnoAhorro",
    TO_DECIMAL("AhorrosTrabajador",26,8) AS "AhorrosTrabajador",
    TO_DECIMAL("AhorrosDeposito",26,8) AS "AhorrosDeposito",
    TO_DECIMAL("AhorrosCotizaciones",26,8) AS "AhorrosCotizaciones",
    TO_DECIMAL("RetirosTrabajador",26,8) AS "RetirosTrabajador",
    TO_DECIMAL("RetirosDeposito",26,8) AS "RetirosDeposito",
    TO_DECIMAL("RetirosCotizaciones",26,8) AS "RetirosCotizaciones",
	"EjecutadoComplementoIPC" AS "ComplementoIPC"
FROM (
        SELECT
            MAX("ID") AS "ID",
            "IdDocumento",
            MAX("Numero") AS "Numero",
            "RutCliente",
            "IdCliente",
            MAX(YEAR("FechaOperacion")||'-12-31') AS "FechaOperacion",
            "AnoAhorro",
            COALESCE(SUM("AhorrosTrabajadorActualizado"),SUM("AhorrosTrabajador")) AS "AhorrosTrabajador",
            COALESCE(SUM("AhorrosDepositoActualizado"),SUM("AhorrosDeposito")) AS "AhorrosDeposito",
            COALESCE(SUM("AhorrosCotizacionesActualizado"),SUM("AhorrosCotizaciones")) AS "AhorrosCotizaciones",
            COALESCE(SUM("RetirosTrabajadorActualizado"),SUM("RetirosTrabajador")) AS "RetirosTrabajador",
            COALESCE(SUM("RetirosDepositoActualizado"),SUM("RetirosDeposito")) AS "RetirosDeposito",
            COALESCE(SUM("RetirosCotizacionesActualizado"),SUM("RetirosCotizaciones")) AS "RetirosCotizaciones",
            "EjecutadoComplementoIPC" AS "EjecutadoComplementoIPC"
        FROM "BSCL.Normativa.BD::TRI.DatosDJ1871"
        GROUP BY
        "IdDocumento",
        "RutCliente",
        "IdCliente",
        "AnoAhorro",
        "EjecutadoComplementoIPC"
        )
with read only;

grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1871.Datos" to bcs_desarrollador with grant option;
