SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CarteraFwd";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CarteraFwd" (
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
        "tipoOper" NVARCHAR(1024)  ,
        "SEC_OPER" DECIMAL(18,6)  ,
        "GLOSA" NVARCHAR(1024)  ,
        "TIPO_LIQUID" NVARCHAR(1024)  ,
        "FEC_OPER" NVARCHAR(1024)  ,
        "FEC_VCTO" NVARCHAR(1024)  ,
        "moneda" NVARCHAR(1024)  ,
        "PRECIO_PESOS" DECIMAL(18,6)  ,
        "monedaRecibe" NVARCHAR(1024)  ,
        "montoRecibe" DECIMAL(18,6)  ,
        "montoEquivRecibe" DECIMAL(18,6)  ,
        "monedaEntrega" NVARCHAR(1024)  ,
        "montoEntrega" DECIMAL(18,6)  ,
        "montoEquivEntrega" DECIMAL(18,6)  ,
        "resultado" DECIMAL(18,6)  ,
        "rentabilidad" DECIMAL(18,6)   );

    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "tipoOper"  ,
        69769.64 AS "SEC_OPER"  ,
        'TEXTO' AS "GLOSA"  ,
        'TEXTO' AS "TIPO_LIQUID"  ,
        'TEXTO' AS "FEC_OPER"  ,
        'TEXTO' AS "FEC_VCTO"  ,
        'TEXTO' AS "moneda"  ,
        53235.96 AS "PRECIO_PESOS"  ,
        'TEXTO' AS "monedaRecibe"  ,
        22879.99 AS "montoRecibe"  ,
        87456.36 AS "montoEquivRecibe"  ,
        'TEXTO' AS "monedaEntrega"  ,
        15210.76 AS "montoEntrega"  ,
        4901.28 AS "montoEquivEntrega"  ,
        48537.81 AS "resultado"  ,
        75658.47 AS "rentabilidad"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "tipoOper"  ,
        5886.61 AS "SEC_OPER"  ,
        'TEXTO' AS "GLOSA"  ,
        'TEXTO' AS "TIPO_LIQUID"  ,
        'TEXTO' AS "FEC_OPER"  ,
        'TEXTO' AS "FEC_VCTO"  ,
        'TEXTO' AS "moneda"  ,
        58409.30 AS "PRECIO_PESOS"  ,
        'TEXTO' AS "monedaRecibe"  ,
        89599.07 AS "montoRecibe"  ,
        47168.36 AS "montoEquivRecibe"  ,
        'TEXTO' AS "monedaEntrega"  ,
        22310.49 AS "montoEntrega"  ,
        68717.80 AS "montoEquivEntrega"  ,
        59878.67 AS "resultado"  ,
        39904.76 AS "rentabilidad"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "tipoOper"  ,
        55725.62 AS "SEC_OPER"  ,
        'TEXTO' AS "GLOSA"  ,
        'TEXTO' AS "TIPO_LIQUID"  ,
        'TEXTO' AS "FEC_OPER"  ,
        'TEXTO' AS "FEC_VCTO"  ,
        'TEXTO' AS "moneda"  ,
        65806.18 AS "PRECIO_PESOS"  ,
        'TEXTO' AS "monedaRecibe"  ,
        56693.23 AS "montoRecibe"  ,
        90769.48 AS "montoEquivRecibe"  ,
        'TEXTO' AS "monedaEntrega"  ,
        11280.49 AS "montoEntrega"  ,
        31653.23 AS "montoEquivEntrega"  ,
        74593.47 AS "resultado"  ,
        46909.78 AS "rentabilidad"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "tipoOper"  ,
        7496.68 AS "SEC_OPER"  ,
        'TEXTO' AS "GLOSA"  ,
        'TEXTO' AS "TIPO_LIQUID"  ,
        'TEXTO' AS "FEC_OPER"  ,
        'TEXTO' AS "FEC_VCTO"  ,
        'TEXTO' AS "moneda"  ,
        60459.29 AS "PRECIO_PESOS"  ,
        'TEXTO' AS "monedaRecibe"  ,
        78470.86 AS "montoRecibe"  ,
        76635.27 AS "montoEquivRecibe"  ,
        'TEXTO' AS "monedaEntrega"  ,
        94648.76 AS "montoEntrega"  ,
        81667.84 AS "montoEquivEntrega"  ,
        68175.52 AS "resultado"  ,
        40588.49 AS "rentabilidad"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "tipoOper"  ,
        67608.63 AS "SEC_OPER"  ,
        'TEXTO' AS "GLOSA"  ,
        'TEXTO' AS "TIPO_LIQUID"  ,
        'TEXTO' AS "FEC_OPER"  ,
        'TEXTO' AS "FEC_VCTO"  ,
        'TEXTO' AS "moneda"  ,
        68766.38 AS "PRECIO_PESOS"  ,
        'TEXTO' AS "monedaRecibe"  ,
        22886.53 AS "montoRecibe"  ,
        54240.55 AS "montoEquivRecibe"  ,
        'TEXTO' AS "monedaEntrega"  ,
        21733.11 AS "montoEntrega"  ,
        62114.92 AS "montoEquivEntrega"  ,
        21174.28 AS "resultado"  ,
        9598.32 AS "rentabilidad"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "tipoOper"  ,
        98493.38 AS "SEC_OPER"  ,
        'TEXTO' AS "GLOSA"  ,
        'TEXTO' AS "TIPO_LIQUID"  ,
        'TEXTO' AS "FEC_OPER"  ,
        'TEXTO' AS "FEC_VCTO"  ,
        'TEXTO' AS "moneda"  ,
        44162.85 AS "PRECIO_PESOS"  ,
        'TEXTO' AS "monedaRecibe"  ,
        5986.37 AS "montoRecibe"  ,
        50168.81 AS "montoEquivRecibe"  ,
        'TEXTO' AS "monedaEntrega"  ,
        24759.23 AS "montoEntrega"  ,
        47370.89 AS "montoEquivEntrega"  ,
        67674.33 AS "resultado"  ,
        79729.59 AS "rentabilidad"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "tipoOper"  ,
        21958.19 AS "SEC_OPER"  ,
        'TEXTO' AS "GLOSA"  ,
        'TEXTO' AS "TIPO_LIQUID"  ,
        'TEXTO' AS "FEC_OPER"  ,
        'TEXTO' AS "FEC_VCTO"  ,
        'TEXTO' AS "moneda"  ,
        56954.65 AS "PRECIO_PESOS"  ,
        'TEXTO' AS "monedaRecibe"  ,
        10657.93 AS "montoRecibe"  ,
        73300.51 AS "montoEquivRecibe"  ,
        'TEXTO' AS "monedaEntrega"  ,
        92731.38 AS "montoEntrega"  ,
        64209.91 AS "montoEquivEntrega"  ,
        62397.10 AS "resultado"  ,
        90902.49 AS "rentabilidad"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "tipoOper"  ,
        27202.73 AS "SEC_OPER"  ,
        'TEXTO' AS "GLOSA"  ,
        'TEXTO' AS "TIPO_LIQUID"  ,
        'TEXTO' AS "FEC_OPER"  ,
        'TEXTO' AS "FEC_VCTO"  ,
        'TEXTO' AS "moneda"  ,
        61667.05 AS "PRECIO_PESOS"  ,
        'TEXTO' AS "monedaRecibe"  ,
        7008.49 AS "montoRecibe"  ,
        64223.34 AS "montoEquivRecibe"  ,
        'TEXTO' AS "monedaEntrega"  ,
        86567.39 AS "montoEntrega"  ,
        26653.11 AS "montoEquivEntrega"  ,
        95124.42 AS "resultado"  ,
        79188.67 AS "rentabilidad"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "tipoOper"  ,
        39387.48 AS "SEC_OPER"  ,
        'TEXTO' AS "GLOSA"  ,
        'TEXTO' AS "TIPO_LIQUID"  ,
        'TEXTO' AS "FEC_OPER"  ,
        'TEXTO' AS "FEC_VCTO"  ,
        'TEXTO' AS "moneda"  ,
        25014.62 AS "PRECIO_PESOS"  ,
        'TEXTO' AS "monedaRecibe"  ,
        36446.33 AS "montoRecibe"  ,
        33657.14 AS "montoEquivRecibe"  ,
        'TEXTO' AS "monedaEntrega"  ,
        64759.80 AS "montoEntrega"  ,
        85773.22 AS "montoEquivEntrega"  ,
        40893.44 AS "resultado"  ,
        40025.43 AS "rentabilidad"   
    FROM :p_Datos AS p;
    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        'TEXTO' AS "tipoOper"  ,
        8390.43 AS "SEC_OPER"  ,
        'TEXTO' AS "GLOSA"  ,
        'TEXTO' AS "TIPO_LIQUID"  ,
        'TEXTO' AS "FEC_OPER"  ,
        'TEXTO' AS "FEC_VCTO"  ,
        'TEXTO' AS "moneda"  ,
        34348.00 AS "PRECIO_PESOS"  ,
        'TEXTO' AS "monedaRecibe"  ,
        43288.93 AS "montoRecibe"  ,
        65436.78 AS "montoEquivRecibe"  ,
        'TEXTO' AS "monedaEntrega"  ,
        15574.66 AS "montoEntrega"  ,
        36684.87 AS "montoEquivEntrega"  ,
        21161.83 AS "resultado"  ,
        46540.42 AS "rentabilidad"   
    FROM :p_Datos AS p;

    SELECT * FROM #temp_data;
    DROP TABLE #temp_data;
END;