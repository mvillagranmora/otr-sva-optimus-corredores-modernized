SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.ItemDerivados";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.ItemDerivados" (
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
        "grupo" NVARCHAR(1024)  ,
        "total" DECIMAL(18,6)  ,
        "item" NVARCHAR(1024)  ,
        "monto" DECIMAL(18,6)  ,
        "totPorcActivo" DECIMAL(18,6)   );

    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "grupo"  ,
        54644.81 AS "total"  ,
        'TEXTO' AS "item"  ,
        14618.32 AS "monto"  ,
        6726.79 AS "totPorcActivo"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "grupo"  ,
        6776.81 AS "total"  ,
        'TEXTO' AS "item"  ,
        25308.97 AS "monto"  ,
        54908.84 AS "totPorcActivo"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "grupo"  ,
        81683.66 AS "total"  ,
        'TEXTO' AS "item"  ,
        84076.02 AS "monto"  ,
        4709.47 AS "totPorcActivo"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "grupo"  ,
        70610.69 AS "total"  ,
        'TEXTO' AS "item"  ,
        20281.52 AS "monto"  ,
        17218.98 AS "totPorcActivo"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "grupo"  ,
        25860.72 AS "total"  ,
        'TEXTO' AS "item"  ,
        92061.94 AS "monto"  ,
        28982.75 AS "totPorcActivo"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "grupo"  ,
        48804.90 AS "total"  ,
        'TEXTO' AS "item"  ,
        24779.72 AS "monto"  ,
        83865.48 AS "totPorcActivo"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "grupo"  ,
        54242.35 AS "total"  ,
        'TEXTO' AS "item"  ,
        83235.18 AS "monto"  ,
        35879.60 AS "totPorcActivo"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "grupo"  ,
        46188.56 AS "total"  ,
        'TEXTO' AS "item"  ,
        38919.28 AS "monto"  ,
        40370.78 AS "totPorcActivo"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "grupo"  ,
        78981.47 AS "total"  ,
        'TEXTO' AS "item"  ,
        77904.18 AS "monto"  ,
        61165.39 AS "totPorcActivo"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "grupo"  ,
        50062.53 AS "total"  ,
        'TEXTO' AS "item"  ,
        88556.46 AS "monto"  ,
        24309.08 AS "totPorcActivo"   
    FROM :p_Datos AS p;

    SELECT * FROM #temp_data;
    DROP TABLE #temp_data;
END;