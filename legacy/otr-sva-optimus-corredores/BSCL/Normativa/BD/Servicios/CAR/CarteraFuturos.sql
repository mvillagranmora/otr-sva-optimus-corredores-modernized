SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CarteraFuturos";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CarteraFuturos" (
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
        "NEMO" NVARCHAR(1024)  ,
        "TIPO_OPERAC" NVARCHAR(1024)  ,
        "CANTIDAD" DECIMAL(18,6)  ,
        "FEC_TRANS" NVARCHAR(1024)  ,
        "FEC_VCTO_FU" TIMESTAMP  ,
        "FEC_VCTO_FUTURO" NVARCHAR(1024)  ,
        "MONTO" DECIMAL(18,6)  ,
        "VAL_MERCADO" DECIMAL(18,6)  ,
        "RESULTADO_DIARIO" DECIMAL(18,6)  ,
        "RESULTADO_ACUM" DECIMAL(18,6)  ,
        "RUT_CLI" NVARCHAR(1024)  ,
        "SEC_RUT_CLI" DECIMAL(18,6)  ,
        "SEC_MOVTO" DECIMAL(18,6)  ,
        "FOLIO_TRANS" NVARCHAR(1024)  ,
        "FEC_LIQUID" TIMESTAMP  ,
        "IND_LIQUID" NVARCHAR(1024)  ,
        "PRECIO" DECIMAL(18,6)  ,
        "TASA" DECIMAL(18,6)  ,
        "COD_CORR_CONTRA" DECIMAL(18,6)  ,
        "COD_AGENTE" NVARCHAR(1024)  ,
        "COD_SUC" NVARCHAR(1024)  ,
        "IND_BOLSA" NVARCHAR(1024)  ,
        "TIPO_COMP_ADJ" NVARCHAR(1024)  ,
        "FOLIO_COMP_ADJ" DECIMAL(18,6)  ,
        "PRE_BASE" DECIMAL(18,6)  ,
        "PRE_CIE_OFI" DECIMAL(18,6)  ,
        "FEC_FIJ_CIE" NVARCHAR(1024)  ,
        "MAR_EXI" DECIMAL(18,6)  ,
        "DISPONIBLE" DECIMAL(18,6)  ,
        "FOLIO_CONT_ANUL" DECIMAL(18,6)  ,
        "FECHA_CONT_ANUL" NVARCHAR(1024)  ,
        "MERCADO" NVARCHAR(1024)  ,
        "DESC_MERCADO" NVARCHAR(1024)  ,
        "TIPO_INST" NVARCHAR(1024)  ,
        "TASA_AYER" DECIMAL(18,6)  ,
        "PRE_BASE_AYER" DECIMAL(18,6)  ,
        "FEC_PRECIO_BASE" NVARCHAR(1024)  ,
        "FEC_ANTICIPO" NVARCHAR(1024)  ,
        "FEC_NETEO" NVARCHAR(1024)  ,
        "SEC_NETEO" DECIMAL(18,6)  ,
        "SEC_MOVTO_DEST" DECIMAL(18,6)  ,
        "MONTO_AYER" DECIMAL(18,6)  ,
        "SEC_CONTRATO" DECIMAL(18,6)  ,
        "TIPOMERCADO" NVARCHAR(1024)   );

    INSERT INTO #temp_data
    SELECT
        CAR."IdCliente",
        CAR."IdPortafolio",
        CAR."Nemotecnico" AS "NEMO"  ,
        CAR."NombreTipoOperacion" AS "TIPO_OPERAC"  ,
        CAR."Cantidad" AS "CANTIDAD"  ,
        CAR."FechaOperacion" AS "FEC_TRANS"  ,
        INS."FechaVencimiento" AS "FEC_VCTO_FU"  ,
        INS."FechaVencimiento" AS "FEC_VCTO_FUTURO"  , --string
        0 AS "MONTO"  ,
        CAR."INGPrecioMercadoActual" AS "VAL_MERCADO"  ,
        98361.02 AS "RESULTADO_DIARIO"  ,
        45506.55 AS "RESULTADO_ACUM"  ,
        'TEXTO' AS "RUT_CLI"  ,
        89950.33 AS "SEC_RUT_CLI"  ,
        52181.87 AS "SEC_MOVTO"  ,
        'TEXTO' AS "FOLIO_TRANS"  ,
        '2016-03-21' AS "FEC_LIQUID"  ,
        'TEXTO' AS "IND_LIQUID"  ,
        CAR."PrecioCompra" AS "PRECIO"  ,
        CAR."TasaCompra" AS "TASA"  ,
        46158.68 AS "COD_CORR_CONTRA"  ,
        CAR."IdAgente" AS "COD_AGENTE"  ,
        CAR."IdSucursal" AS "COD_SUC"  ,
        '' AS "IND_BOLSA"  ,
        '' AS "TIPO_COMP_ADJ"  ,
        0 AS "FOLIO_COMP_ADJ"  ,
        CAR."PrecioCompra" AS "PRE_BASE"  ,
        CAR."INGPrecioMercadoActual" AS "PRE_CIE_OFI"  ,
        CAR."INGFechaPrecioTasaMercadoActual" AS "FEC_FIJ_CIE"  ,
        0 AS "MAR_EXI"  ,
        35638.48 AS "DISPONIBLE"  ,
        0 AS "FOLIO_CONT_ANUL"  ,
        '' AS "FECHA_CONT_ANUL"  ,
        '' AS "MERCADO"  ,
        '' AS "DESC_MERCADO"  ,
        '' AS "TIPO_INST"  ,
        CAR."INGTasaMercadoAnterior" AS "TASA_AYER"  ,
        CAR."INGPrecioMercadoAnterior" AS "PRE_BASE_AYER"  ,
        CAR."INGFechaPrecioTasaMercadoAnterior" AS "FEC_PRECIO_BASE"  ,
        'TEXTO' AS "FEC_ANTICIPO"  ,
        'TEXTO' AS "FEC_NETEO"  ,
        19689.91 AS "SEC_NETEO"  ,
        8567.02 AS "SEC_MOVTO_DEST"  ,
        4186.35 AS "MONTO_AYER"  ,
        38626.37 AS "SEC_CONTRATO"  ,
        'TEXTO' AS "TIPOMERCADO"   
     	FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldos" CAR --"BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CAR
		JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS 
		ON INS."IdTipoInstrumento" = CAR."IdTipoInstrumento"
		AND CAR."IdTipoInstrumento" IN ('FDO','FIP','FBC','FIC','FUF')
		AND INS."IdClasificacionTipoInstrumento" = 'RF' 
		AND INS."Nemotecnico" = CAR."Nemotecnico"
		LEFT JOIN "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiasSaldos" GTA ON GTA."IdPortafolio" = CAR."IdPortafolio" AND GTA."IdCliente" = CAR."IdCliente" AND CAR."Nemotecnico" = GTA."NemoGarantizado"
		WHERE CAR."IdEstado" = 'VIG';
    

    SELECT * FROM #temp_data;
    DROP TABLE #temp_data;
END;