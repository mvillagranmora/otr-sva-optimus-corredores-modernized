SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.InfoCarteraFM";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.InfoCarteraFM" (
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
        "Acumulado" DECIMAL(18,6)  ,
        "Ult_Ano" DECIMAL(18,6)  ,
        "Ult_Tres_Meses" DECIMAL(18,6)  ,
        "Ult_Mes" DECIMAL(18,6)  ,
        "Un_Ano" DECIMAL(18,6)  ,
        "Dos_Ano_Antes" DECIMAL(18,6)  ,
        "Tres_Ano_Antes" DECIMAL(18,6)  ,
        "Cuatro_Ano_Antes" DECIMAL(18,6)  ,
        "Year_minus_1" NVARCHAR(1024)  ,
        "Year_minus_2" NVARCHAR(1024)  ,
        "Year_minus_3" NVARCHAR(1024)  ,
        "Year_minus_4" NVARCHAR(1024)   );

    INSERT INTO #temp_data
    SELECT
        p."IdCliente",
        p."IdPortafolio",
        0.17 AS "Acumulado"  ,
        0.73 AS "Ult_Ano"  ,
        0.83 AS "Ult_Tres_Meses"  ,
        0.86 AS "Ult_Mes"  ,
        0.38 AS "Un_Ano"  ,
        0.83 AS "Dos_Ano_Antes"  ,
        0.48 AS "Tres_Ano_Antes"  ,
        0.50 AS "Cuatro_Ano_Antes"  ,
        'TEXTO' AS "Year_minus_1"  ,
        'TEXTO' AS "Year_minus_2"  ,
        'TEXTO' AS "Year_minus_3"  ,
        'TEXTO' AS "Year_minus_4"   
    FROM :p_Datos AS p;

    SELECT * FROM #temp_data;
    DROP TABLE #temp_data;
END;
