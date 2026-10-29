SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.ProximosEventosCapital";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.ProximosEventosCapital" (
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
        "nemo" NVARCHAR(1024)  ,
        "descripcion" NVARCHAR(1024)  ,
        "monto" DECIMAL(18,6)  ,
        "fecha_cierre" TIMESTAMP  ,
        "fecha_pago" TIMESTAMP  ,
        "eventoCapital" NVARCHAR(1024)  ,
        "agrupador" NVARCHAR(1024)   );

    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "nemo"  ,
        'TEXTO' AS "descripcion"  ,
        71254.37 AS "monto"  ,
        '2016-04-07' AS "fecha_cierre"  ,
        '2016-11-01' AS "fecha_pago"  ,
        'TEXTO' AS "eventoCapital"  ,
        'TEXTO' AS "agrupador"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "nemo"  ,
        'TEXTO' AS "descripcion"  ,
        79749.32 AS "monto"  ,
        '2016-04-22' AS "fecha_cierre"  ,
        '2016-06-01' AS "fecha_pago"  ,
        'TEXTO' AS "eventoCapital"  ,
        'TEXTO' AS "agrupador"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "nemo"  ,
        'TEXTO' AS "descripcion"  ,
        42670.40 AS "monto"  ,
        '2016-04-13' AS "fecha_cierre"  ,
        '2016-02-08' AS "fecha_pago"  ,
        'TEXTO' AS "eventoCapital"  ,
        'TEXTO' AS "agrupador"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "nemo"  ,
        'TEXTO' AS "descripcion"  ,
        44402.71 AS "monto"  ,
        '2016-04-09' AS "fecha_cierre"  ,
        '2016-12-11' AS "fecha_pago"  ,
        'TEXTO' AS "eventoCapital"  ,
        'TEXTO' AS "agrupador"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "nemo"  ,
        'TEXTO' AS "descripcion"  ,
        90228.14 AS "monto"  ,
        '2016-07-11' AS "fecha_cierre"  ,
        '2016-04-15' AS "fecha_pago"  ,
        'TEXTO' AS "eventoCapital"  ,
        'TEXTO' AS "agrupador"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "nemo"  ,
        'TEXTO' AS "descripcion"  ,
        51352.80 AS "monto"  ,
        '2016-12-22' AS "fecha_cierre"  ,
        '2016-05-21' AS "fecha_pago"  ,
        'TEXTO' AS "eventoCapital"  ,
        'TEXTO' AS "agrupador"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "nemo"  ,
        'TEXTO' AS "descripcion"  ,
        54555.77 AS "monto"  ,
        '2016-07-04' AS "fecha_cierre"  ,
        '2016-03-15' AS "fecha_pago"  ,
        'TEXTO' AS "eventoCapital"  ,
        'TEXTO' AS "agrupador"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "nemo"  ,
        'TEXTO' AS "descripcion"  ,
        87084.60 AS "monto"  ,
        '2016-07-22' AS "fecha_cierre"  ,
        '2016-12-07' AS "fecha_pago"  ,
        'TEXTO' AS "eventoCapital"  ,
        'TEXTO' AS "agrupador"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "nemo"  ,
        'TEXTO' AS "descripcion"  ,
        41338.43 AS "monto"  ,
        '2016-04-05' AS "fecha_cierre"  ,
        '2016-12-04' AS "fecha_pago"  ,
        'TEXTO' AS "eventoCapital"  ,
        'TEXTO' AS "agrupador"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "nemo"  ,
        'TEXTO' AS "descripcion"  ,
        6603.44 AS "monto"  ,
        '2016-03-13' AS "fecha_cierre"  ,
        '2016-08-01' AS "fecha_pago"  ,
        'TEXTO' AS "eventoCapital"  ,
        'TEXTO' AS "agrupador"   
    FROM :p_Datos AS p;

    SELECT * FROM #temp_data;
    DROP TABLE #temp_data;
END;