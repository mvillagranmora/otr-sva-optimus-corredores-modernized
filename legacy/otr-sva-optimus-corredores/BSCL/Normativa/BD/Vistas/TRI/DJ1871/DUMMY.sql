SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1871.DUMMY";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1871.DUMMY" (
    "IdEmpresa",
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
    "IdEmpresa",
    "RutCliente",
    "IdCliente",
    TO_DATE("FechaOperacion") AS "FechaOperacion",
    "AnoAhorro",
    --Ahorros
    0 AS "AhorrosTrabajador",
    CASE WHEN "IdCliente" = 1 THEN SUM("AhorrosDeposito") +5000
    ELSE SUM("AhorrosDeposito") END AS "AhorrosDeposito",
    0 AS "AhorrosCotizaciones",
    --Retiros
    0 AS "RetirosTrabajador",
    SUM("RetirosDeposito") AS "RetirosDeposito",
    0 AS "RetirosCotizaciones",
    "ComplementoIPC" AS "ComplementoIPC"
FROM (
    SELECT 
        "IdEmpresa",
        "DocumentoIdentidad" AS "RutCliente",
        "IdCliente",
        YEAR("FechaCertificacion")||'-12-31' AS "FechaOperacion",
        YEAR("FechaCertificacion") AS "AnoAhorro",
        --Ahorros
        "Abono" AS "AhorrosDeposito",
        --Retiros
        "Cargo" AS "RetirosDeposito",
        'N' AS "ComplementoIPC"
    FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos"
    WHERE "IdConcepto" IN ('APORCI','RTROCI','INGAPV','EGRAPV','ABOAPV','CARAPV','ITRAPV','ETRAPV')
    AND "IdEstado" = 'CRT'
    AND "RegimenMatrizAgrupador" = 'APV-A'
)
GROUP BY "IdEmpresa","RutCliente","IdCliente","FechaOperacion","AnoAhorro","ComplementoIPC"
WITH read only;

grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1871.DUMMY" to bcs_desarrollador with grant option;
