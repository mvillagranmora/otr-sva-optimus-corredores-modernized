SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.GraficoFM";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.GraficoFM" (
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
        "Fecha" TIMESTAMP  ,
        "Patrimonio_Final" DECIMAL(18,6)   );

    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        '2016-06-10' AS "Fecha"  ,
        55802.99 AS "Patrimonio_Final"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        '2016-04-23' AS "Fecha"  ,
        12218.06 AS "Patrimonio_Final"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        '2016-12-05' AS "Fecha"  ,
        44213.55 AS "Patrimonio_Final"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        '2016-04-18' AS "Fecha"  ,
        31676.97 AS "Patrimonio_Final"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        '2016-06-05' AS "Fecha"  ,
        37548.30 AS "Patrimonio_Final"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        '2016-02-20' AS "Fecha"  ,
        8091.14 AS "Patrimonio_Final"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        '2016-11-02' AS "Fecha"  ,
        85654.69 AS "Patrimonio_Final"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        '2016-10-04' AS "Fecha"  ,
        11434.52 AS "Patrimonio_Final"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        '2016-02-09' AS "Fecha"  ,
        2101.44 AS "Patrimonio_Final"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        '2016-10-12' AS "Fecha"  ,
        47603.21 AS "Patrimonio_Final"   
    FROM :p_Datos AS p;

    SELECT * FROM #temp_data;
    DROP TABLE #temp_data;
END;
