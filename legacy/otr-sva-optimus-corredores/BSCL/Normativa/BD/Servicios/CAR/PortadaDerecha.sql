SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.PortadaDerecha";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.PortadaDerecha" (
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
        "notaTitulo" NVARCHAR(1024)  ,
        "notaInst" NVARCHAR(1024)   );

    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "notaTitulo"  ,
        'TEXTO' AS "notaInst"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "notaTitulo"  ,
        'TEXTO' AS "notaInst"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "notaTitulo"  ,
        'TEXTO' AS "notaInst"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "notaTitulo"  ,
        'TEXTO' AS "notaInst"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "notaTitulo"  ,
        'TEXTO' AS "notaInst"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "notaTitulo"  ,
        'TEXTO' AS "notaInst"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "notaTitulo"  ,
        'TEXTO' AS "notaInst"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "notaTitulo"  ,
        'TEXTO' AS "notaInst"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "notaTitulo"  ,
        'TEXTO' AS "notaInst"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "notaTitulo"  ,
        'TEXTO' AS "notaInst"   
    FROM :p_Datos AS p;

    SELECT * FROM #temp_data;
    DROP TABLE #temp_data;
END;