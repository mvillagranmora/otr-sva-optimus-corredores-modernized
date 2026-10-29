SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CarteraPrestamos";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CarteraPrestamos" (
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
        "tipo_operacion" NVARCHAR(1024)  ,
        "nemo" NVARCHAR(1024)  ,
        "cantidad" DECIMAL(18,6)  ,
        "tasa" DECIMAL(18,6)  ,
        "operacion" NVARCHAR(1024)  ,
        "f_registro" TIMESTAMP  ,
        "f_vencimiento" TIMESTAMP  ,
        "precio" DECIMAL(18,6)  ,
        "monto" DECIMAL(18,6)  ,
        "prima_premio" DECIMAL(18,6)  ,
        "precio_cierre" DECIMAL(18,6)  ,
        "plazo" BIGINT  ,
        "monto_cierre" DECIMAL(18,6)  ,
        "resultado" DECIMAL(18,6)   );

    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "tipo_operacion"  ,
        'TEXTO' AS "nemo"  ,
        63698.74 AS "cantidad"  ,
        40376.77 AS "tasa"  ,
        'TEXTO' AS "operacion"  ,
        '2016-11-21' AS "f_registro"  ,
        '2016-08-17' AS "f_vencimiento"  ,
        68291.27 AS "precio"  ,
        61087.73 AS "monto"  ,
        78514.54 AS "prima_premio"  ,
        91711.11 AS "precio_cierre"  ,
        58948 AS "plazo"  ,
        98226.90 AS "monto_cierre"  ,
        11609.92 AS "resultado"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "tipo_operacion"  ,
        'TEXTO' AS "nemo"  ,
        71545.28 AS "cantidad"  ,
        84183.47 AS "tasa"  ,
        'TEXTO' AS "operacion"  ,
        '2016-11-08' AS "f_registro"  ,
        '2016-03-14' AS "f_vencimiento"  ,
        61212.29 AS "precio"  ,
        48703.97 AS "monto"  ,
        65320.79 AS "prima_premio"  ,
        8340.11 AS "precio_cierre"  ,
        759086 AS "plazo"  ,
        29148.48 AS "monto_cierre"  ,
        3686.73 AS "resultado"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO1' AS "tipo_operacion"  ,
        'TEXTO2' AS "nemo"  ,
        3 AS "cantidad"  ,
        4 AS "tasa"  ,
        'TEXTO5' AS "operacion"  ,
        '2016-12-26' AS "f_registro"  ,
        '2016-10-27' AS "f_vencimiento"  ,
        8 AS "precio"  ,
        9 AS "monto"  ,
        10 AS "prima_premio"  ,
        11 AS "precio_cierre"  ,
        12 AS "plazo"  ,
        13 AS "monto_cierre"  ,
        14 AS "resultado"   
    FROM :p_Datos AS p;
    

    SELECT * FROM #temp_data;
    DROP TABLE #temp_data;
END;
