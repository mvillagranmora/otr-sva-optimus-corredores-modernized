SET SCHEMA "BSCL";
GO

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CarteraFM";
GO

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CarteraFM" (
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
        "alert" NVARCHAR(1024)  ,
        "nemo" NVARCHAR(1024)  ,
        "emisor" NVARCHAR(1024)  ,
        "serie" NVARCHAR(1024)  ,
        "RAZON_SOCIAL" NVARCHAR(1024)  ,
        "CAN_CUSTODIA" DECIMAL(18,6)  ,
        "garantia" DECIMAL(18,6)  ,
        "valorCuotaAporte" DECIMAL(18,6)  ,
        "valorCuotaActual" DECIMAL(18,6)  ,
        "COD_IMP_RES" NVARCHAR(1024)  ,
        "IND_DIFERIDO" NVARCHAR(1024)  ,
        "NEMO_ASOC" NVARCHAR(1024)  ,
        "TASA_CAMBIO" DECIMAL(18,6)  ,
        "BLOQUEO_FONDO" DECIMAL(18,6)  ,
        "TIR_CONTAB" DECIMAL(18,6)  ,
        "TIPO_FAM" NVARCHAR(1024)  ,
        "categorizacion" NVARCHAR(1024)  ,
        "MONTO_VALORIZ" DECIMAL(18,6)  ,
        "valorActual" DECIMAL(18,6)  ,
        "RENTABILIDAD" DECIMAL(18,6)   );
        
        
insert into #temp_data
	select 
		CAR."IdCliente" 				as "IdCliente",
		CAR."IdPortafolio" 				as "IdPortafolio",
		'' 								as "alert",
		CAR."Nemotecnico" 				as "nemo",
		CAR."IdEmisor" 					as "emisor",
		INS."IdTipoInstrumento" 		as "serie",
		pers."RazonSocial"				as "RAZON_SOCIAL",
		CAR."INGCantidad"				as "CAN_CUSTODIA",
		0 								as "garantia",
		IFNULL(CAR."INGPrecioPromedioCompra",0)	as "valorCuotaAporte",
		--IFNULL(CUO."ValorCuota",0)		as "valorCuotaActual",
		IFNULL(CAR."INGPrecioMercadoActual",0)		as "valorCuotaActual",
		''								as "COD_IMP_RES",
		''								as "IND_DIFERIDO",
		CAR."Nemotecnico" 				as "NEMO_ASOC",
		0								as "TASA_CAMBIO",
		0			 					as "BLOQUEO_FONDO",
		0			 					as "TIR_CONTAB",
		''								as "TIPO_FAM",
		''		 						as "categorizacion",
		--CAR."INGPrecioMercadoActual" 	as "MONTO_VALORIZ",
		--CAST(IFNULL(CAR."Cantidad",0) * IFNULL(CUO."ValorCuota",0) AS BIGINT)  	as "MONTO_VALORIZ",
		--CAST(IFNULL(CAR."INGCantidad",0) * IFNULL(CAR."INGPrecioMercadoActual",0) AS BIGINT)  	as "MONTO_VALORIZ",
		FLOOR(CAR."INGMontoATasaMercadoActual") AS "MONTO_VALORIZ",
		ifnull(INS."Precio",0)			as "valorActual",
		0				 				as "RENTABILIDAD"

	FROM 
		:p_Datos D
		JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldos" CAR ON D."IdPortafolio" = CAR."IdPortafolio" --"BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CAR
		JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS ON INS."IdInstrumento" = CAR."IdInstrumento"  AND INS."IdClasificacionTipoInstrumento" = 'FM'  AND  CAR."IdClasificacionTipoInstrumento" = 'FM' AND INS."Nemotecnico" = CAR."Nemotecnico"
--		LEFT JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuota" CUO ON CAR."Nemotecnico" = CUO."IdSerieFondo"
		LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" emisor ON emisor."IdEmisor" = CAR."IdEmisor"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" pers ON pers."IdPersona" = emisor."IdPersona"

	WHERE CAR."IdEstado" = 'VIG'
;
   

    SELECT * FROM #temp_data;
    DROP TABLE #temp_data;
END;
GO