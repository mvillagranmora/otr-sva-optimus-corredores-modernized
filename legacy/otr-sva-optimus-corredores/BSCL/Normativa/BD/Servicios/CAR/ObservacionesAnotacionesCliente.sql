SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.ObservacionesAnotacionesCliente";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.ObservacionesAnotacionesCliente" (
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
        "dan_titulo" NVARCHAR(1024)  ,
        "dan_nota" NVARCHAR(1024)   );

    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "dan_titulo"  ,
        'TEXTO' AS "dan_nota"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "dan_titulo"  ,
        'TEXTO' AS "dan_nota"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "dan_titulo"  ,
        'TEXTO' AS "dan_nota"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "dan_titulo"  ,
        'TEXTO' AS "dan_nota"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "dan_titulo"  ,
        'TEXTO' AS "dan_nota"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "dan_titulo"  ,
        'TEXTO' AS "dan_nota"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "dan_titulo"  ,
        'TEXTO' AS "dan_nota"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "dan_titulo"  ,
        'TEXTO' AS "dan_nota"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "dan_titulo"  ,
        'TEXTO' AS "dan_nota"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "dan_titulo"  ,
        'TEXTO' AS "dan_nota"   
    FROM :p_Datos AS p;

    SELECT * FROM #temp_data;
    DROP TABLE #temp_data;
END;