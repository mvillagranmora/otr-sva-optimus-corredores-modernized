SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CarteraPactos";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CarteraPactos" (
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
        "operacion" NVARCHAR(1024)  ,
        "moneda" NVARCHAR(1024)  ,
        "SEC_COMPROM" DECIMAL(18,6)  ,
        "plazo" DECIMAL(18,6)  ,
        "TASA_COMPROM" DECIMAL(18,6)  ,
        "operacionContado" NVARCHAR(1024)  ,
        "fechaContado" TIMESTAMP  ,
        "montoContratadoContado" DECIMAL(18,6)  ,
        "equivClpContado" DECIMAL(18,6)  ,
        "operacionPlazo" NVARCHAR(1024)  ,
        "fechaPlazo" TIMESTAMP  ,
        "montoContratadoPlazo" DECIMAL(18,6)  ,
        "equivClpPlazo" DECIMAL(18,6)  ,
        "VALOR_PTE" DECIMAL(18,6)  ,
        "equivClpValPresen" DECIMAL(18,6)  ,
        "equivClpCompra" DECIMAL(18,6)  ,
        "equivClpVenta" DECIMAL(18,6)  ,
        "equivPresenClpCompra" DECIMAL(18,6)  ,
        "equivPresenClpVenta" DECIMAL(18,6)  ,
        "CantVenta" BIGINT  ,
        "cantCompra" BIGINT   );

    INSERT INTO #temp_data
    SELECT
        CAR."IdCliente",
        CAR."IdPortafolio",
        CAR."NombreTipoOperacion" AS "operacion"  ,
        CAR."IdMonedaInstrumento" AS "moneda"  ,
        CAR."IdOperacion" AS "SEC_COMPROM"  ,
        CAR."PlazoEnDiasOperacion" AS "plazo"  ,
        CAR."TasaCompra" AS "TASA_COMPROM"  ,
        'TEXTO' AS "operacionContado"  ,
        '2016-09-10' AS "fechaContado"  ,
        CAR."MontoCompra" AS "montoContratadoContado"  ,
        CAR."MontoCompraMonedaInstrumento" AS "equivClpContado"  ,
        CAR."TipoInstrumento" AS "operacionPlazo"  ,
        '2016-02-08' AS "fechaPlazo"  ,
        96672.62 AS "montoContratadoPlazo"  ,
        34862.22 AS "equivClpPlazo"  ,
        22975.14 AS "VALOR_PTE"  ,
        39290.64 AS "equivClpValPresen"  ,
        28240.71 AS "equivClpCompra"  ,
        35054.80 AS "equivClpVenta"  ,
        73773.61 AS "equivPresenClpCompra"  ,
        61870.31 AS "equivPresenClpVenta"  ,
        CAR."Cantidad" AS "CantVenta"  ,
        CAR."Cantidad" AS "cantCompra"   
        
     	FROM
     	:p_Datos D
     	JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldos" CAR ON D."IdPortafolio" = CAR."IdPortafolio" --"BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CAR
		JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS 
		ON INS."IdTipoInstrumento" = CAR."IdTipoInstrumento" AND CAR."IdTipoInstrumento" = 'PAC'
		AND INS."IdClasificacionTipoInstrumento" = 'RF' 
		AND INS."Nemotecnico" = CAR."Nemotecnico"
		WHERE CAR."IdEstado" = 'VIG';
   
   
   
    SELECT * FROM #temp_data;
    DROP TABLE #temp_data;
END;