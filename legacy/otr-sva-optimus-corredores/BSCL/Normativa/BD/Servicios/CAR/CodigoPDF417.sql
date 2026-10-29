SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CodigoPDF417";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CodigoPDF417" (
    IN p_IdModulo NVARCHAR(40),
    IN p_IdUsuario BIGINT,
    IN p_IdEmpresa BIGINT,
    IN p_FechaDesde DATE,
    IN p_FechaHasta DATE,
    IN p_Datos TABLE ("IdCliente" BIGINT, "IdPortafolio" BIGINT)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
--READS SQL DATA
AS
BEGIN
    CREATE LOCAL TEMPORARY TABLE #temp_data (
        "IdCliente" BIGINT,
        "IdPortafolio" BIGINT,
        "fecha" TIMESTAMP  ,
        "retirosClp" DECIMAL(18,6)  ,
        "abonosClp" DECIMAL(18,6)  ,
        "saldoInicial" DECIMAL(18,6)  ,
        "saldoFinal" DECIMAL(18,6)  ,
        "item" NVARCHAR(1024)  ,
        "cargo" DECIMAL(18,6)  ,
        "monto" DECIMAL(18,6)  ,
        "saldoFinalMov" DECIMAL(18,6)  ,
        "saldoInicialMov" DECIMAL(18,6)  ,
        "totalRv" DECIMAL(18,6)  ,
        "totalRf" DECIMAL(18,6)  ,
        "totalDer" DECIMAL(18,6)  ,
        "totalFm" DECIMAL(18,6)  ,
        "totalOp" DECIMAL(18,6)  ,
        "totalCaja" DECIMAL(18,6)  ,
        "pais" NVARCHAR(1024)  ,
        "complemento" NVARCHAR(1024)  ,
        "ciudadCodBarra" NVARCHAR(1024)  ,
        "direccionCodBarra" NVARCHAR(1024)  ,
        "nombre" NVARCHAR(1024)  ,
        "cuenta" NVARCHAR(1024)  ,
        "comunaCodBarra" NVARCHAR(1024)  ,
        "numeroCodBarra" NVARCHAR(1024)  ,
        "rut" NVARCHAR(1024)   );

    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        '2016-06-17' AS "fecha"  ,
        90785.39 AS "retirosClp"  ,
        4613.68 AS "abonosClp"  ,
        92653.53 AS "saldoInicial"  ,
        95982.83 AS "saldoFinal"  ,
        'TEXTO' AS "item"  ,
        66106.50 AS "cargo"  ,
        16164.48 AS "monto"  ,
        15774.96 AS "saldoFinalMov"  ,
        96513.39 AS "saldoInicialMov"  ,
        39829.07 AS "totalRv"  ,
        87189.01 AS "totalRf"  ,
        76312.30 AS "totalDer"  ,
        83861.45 AS "totalFm"  ,
        40255.15 AS "totalOp"  ,
        26164.30 AS "totalCaja"  ,
        'TEXTO' AS "pais"  ,
        'TEXTO' AS "complemento"  ,
        'TEXTO' AS "ciudadCodBarra"  ,
        'TEXTO' AS "direccionCodBarra"  ,
        'TEXTO' AS "nombre"  ,
        '511127_' AS "cuenta"  ,
        'TEXTO' AS "comunaCodBarra"  ,
        'TEXTO' AS "numeroCodBarra"  ,
        '9819078-3' AS "rut"   
    FROM :p_Datos AS p;

    SELECT * FROM #temp_data;
    DROP TABLE #temp_data;
END;
GO