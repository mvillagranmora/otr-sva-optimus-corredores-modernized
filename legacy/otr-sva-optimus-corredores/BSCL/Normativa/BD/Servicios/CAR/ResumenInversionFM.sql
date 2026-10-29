SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.ResumenInversionFM";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.ResumenInversionFM" (
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
        "Item" NVARCHAR(1024)  ,
        "Monto" DECIMAL(18,6)  ,
        "Moneda" NVARCHAR(1024)  ,
        "Render" BIGINT   );

    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "Item"  ,
        87253.98 AS "Monto"  ,
        'TEXTO' AS "Moneda"  ,
        754633 AS "Render"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "Item"  ,
        60585.77 AS "Monto"  ,
        'TEXTO' AS "Moneda"  ,
        824866 AS "Render"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "Item"  ,
        27368.50 AS "Monto"  ,
        'TEXTO' AS "Moneda"  ,
        608969 AS "Render"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "Item"  ,
        47581.14 AS "Monto"  ,
        'TEXTO' AS "Moneda"  ,
        693103 AS "Render"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "Item"  ,
        17113.77 AS "Monto"  ,
        'TEXTO' AS "Moneda"  ,
        446117 AS "Render"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "Item"  ,
        55983.20 AS "Monto"  ,
        'TEXTO' AS "Moneda"  ,
        904324 AS "Render"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "Item"  ,
        80116.57 AS "Monto"  ,
        'TEXTO' AS "Moneda"  ,
        319147 AS "Render"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "Item"  ,
        55877.02 AS "Monto"  ,
        'TEXTO' AS "Moneda"  ,
        50674 AS "Render"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "Item"  ,
        71699.72 AS "Monto"  ,
        'TEXTO' AS "Moneda"  ,
        38102 AS "Render"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "Item"  ,
        60863.53 AS "Monto"  ,
        'TEXTO' AS "Moneda"  ,
        229023 AS "Render"   
    FROM :p_Datos AS p;

    SELECT * FROM #temp_data;
    DROP TABLE #temp_data;
END;
