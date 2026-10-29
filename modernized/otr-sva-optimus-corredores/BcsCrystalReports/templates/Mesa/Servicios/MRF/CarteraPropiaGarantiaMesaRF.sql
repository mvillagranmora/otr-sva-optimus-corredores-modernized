SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::MRF.CarteraPropiaGarantiaMesaRF";
GO
CREATE PROCEDURE
--  Proposito: Informe cartera Cuenta Propia en Garantia mesa RF 
--  Autor: projo
--  Fecha Creacion: 04/07/2016
"BSCL.Mesa.CrystalReports.Servicios::MRF.CarteraPropiaGarantiaMesaRF" ( 
    --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa          	          	BIGINT      -- identificador de la empresa de la operacion
    ,IN p_IdUsuario	                        BIGINT      -- usuario que ingresa la operacion
    ,IN p_IdModulo                          NVARCHAR(3) -- modulo de la operacion 
	,IN p_Fecha								DATE		 --fecha de consulta
	,IN p_TipoValorizacion					NVARCHAR(1) default 'M'--'C' 'M'
	,IN p_IdCliente							INT
	,IN p_IdPortafolio						INT
    
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	DECLARE v_count INT;
	DECLARE v_FechaSistema	NVARCHAR(10);
	
	BEGIN
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		IF (:p_Fecha < TO_DATE(:v_FechaSistema,'YYYY-MM-DD')) THEN
			v_Montos = SELECT "FolioCarteraOrigen"
						,SUM("INGCantidad") as "INGCantidad"
						,SUM("INGMontoATasaGarantiaActual") as "INGMontoATasaGarantiaActual"
						
						,SUM("INGMontoInteresCompraProximo") as "INGMontoInteresCompraProximo"
						,SUM("INGMontoReajusteCompraProximo") as "INGMontoReajusteCompraProximo"
						
						,SUM("INGMontoATasaCompraActual") as "INGMontoATasaCompraActual"
						,SUM("INGMontoATasaCompraProximo") as "INGMontoATasaCompraProximo"
						,SUM("INGMontoATasaMercadoActual") as "INGMontoATasaMercadoActual"
						,SUM("INGMontoATasaMercadoProximo") as "INGMontoATasaMercadoProximo"
					--FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias"
					FROM "BSCL.Custodia.BD.Vistas::HIS.ConsultarGarantias"
					WHERE "IdCliente"=p_IdCliente 
					  and "IdPortafolio"=p_IdPortafolio
					  and "IdEstado"='VIG' 
					  and "IdClasificacionTipoInstrumento" in ('RF','IF')
					  AND "EGRCantidad" = 0
					  --AND (("INGFechaLiquidacion" <= p_Fecha AND "EGRCantidad" = 0) OR
						--   ("INGFechaLiquidacion" <= p_Fecha AND "EGRCantidad" <> 0 AND "EGRFechaLiquidacion" > p_Fecha))
					  AND "FechaRespaldo" = p_Fecha
					GROUP BY "FolioCarteraOrigen";
	
			v_Datos = SELECT
							ROW_NUMBER() over (PARTITION BY "FolioCarteraOrigen"	ORDER BY "FolioCarteraOrigen" DESC) as "Id"
							,"IdGarantia"
							,"FolioGarantia"
							,"FolioCarteraOrigen"
							,"FechaLiquidacion"
							,"IdEstado"
							,"IdInstrumento"
							,"IdMonedaInstrumento"
							,"Nemotecnico"
							,"CodigoIFRS"
							,"IdEmisor"
							,"EsFungible"
							,"FechaVencimientoInstrumento"
							,"FechaVencimientoOperacion"
							,"PlazoEnDiasOperacion"
							,"FechaOperacion"
							,"INGDurationCompraActual"
							,"INGPrecioMercadoActual"
							,"PrecioCompraOperacionOrigen"
							,"INGTasaMercadoActual"
							,"TasaCompraOperacionOrigen"
							,"INGPrecioMercadoProximo"
							,"INGTasaMercadoProximo"
							
					--FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias"
					FROM "BSCL.Custodia.BD.Vistas::HIS.ConsultarGarantias"
					WHERE "IdCliente"=p_IdCliente 
					  and "IdPortafolio"=p_IdPortafolio 
					  and "IdEstado"='VIG' 
					  and "IdClasificacionTipoInstrumento" in ('RF','IF')
					 AND "EGRCantidad" = 0
					  --and "FechaLiquidacion"= p_Fecha
					  --AND (("INGFechaLiquidacion" <= p_Fecha AND "EGRCantidad" = 0) OR
						--   ("INGFechaLiquidacion" <= p_Fecha AND "EGRCantidad" <> 0 AND "EGRFechaLiquidacion" > p_Fecha))
					  AND "FechaRespaldo" = p_Fecha;
		ELSE
			v_Montos = SELECT "FolioCarteraOrigen"
						,SUM("INGCantidad") as "INGCantidad"
						,SUM("INGMontoATasaGarantiaActual") as "INGMontoATasaGarantiaActual"
						
						,SUM("INGMontoInteresCompraProximo") as "INGMontoInteresCompraProximo"
						,SUM("INGMontoReajusteCompraProximo") as "INGMontoReajusteCompraProximo"
						
						,SUM("INGMontoATasaCompraActual") as "INGMontoATasaCompraActual"
						,SUM("INGMontoATasaCompraProximo") as "INGMontoATasaCompraProximo"
						,SUM("INGMontoATasaMercadoActual") as "INGMontoATasaMercadoActual"
						,SUM("INGMontoATasaMercadoProximo") as "INGMontoATasaMercadoProximo"
						
					FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias"
					WHERE "IdCliente"=p_IdCliente 
					  and "IdPortafolio"=p_IdPortafolio
					  and "IdEstado"='VIG' 
					  and "IdClasificacionTipoInstrumento" in ('RF','IF')
					  AND "EGRCantidad" = 0
					  --AND (("INGFechaLiquidacion" <= p_Fecha AND "EGRCantidad" = 0) OR
					--	   ("INGFechaLiquidacion" <= p_Fecha AND "EGRCantidad" <> 0 AND "EGRFechaLiquidacion" > p_Fecha))
					  AND "FechaOperacion" <= p_Fecha
					GROUP BY "FolioCarteraOrigen";
	
			v_Datos = SELECT
							ROW_NUMBER() over (PARTITION BY "FolioCarteraOrigen"	ORDER BY "FolioCarteraOrigen" DESC) as "Id"
							,"IdGarantia"
							,"FolioGarantia"
							,"FolioCarteraOrigen"
							,"FechaLiquidacion"
							,"IdEstado"
							,"IdInstrumento"
							,"IdMonedaInstrumento"
							,"Nemotecnico"
							,"CodigoIFRS"
							,"IdEmisor"
							,"EsFungible"
							,"FechaVencimientoInstrumento"
							,"FechaVencimientoOperacion"
							,"PlazoEnDiasOperacion"
							,"FechaOperacion"
							,"INGDurationCompraActual"
							,"INGPrecioMercadoActual"
							,"PrecioCompraOperacionOrigen"
							,"INGTasaMercadoActual"
							,"TasaCompraOperacionOrigen"
							,"INGPrecioMercadoProximo"
							,"INGTasaMercadoProximo"							
					FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias"
					WHERE "IdCliente"=p_IdCliente 
					  and "IdPortafolio"=p_IdPortafolio 
					  and "IdEstado"='VIG' 
					  and "IdClasificacionTipoInstrumento" in ('RF','IF')
					  AND "EGRCantidad" = 0
					  --and "FechaLiquidacion"= p_Fecha
					  --AND (("INGFechaLiquidacion" <= p_Fecha AND "EGRCantidad" = 0) OR
						--   ("INGFechaLiquidacion" <= p_Fecha AND "EGRCantidad" <> 0 AND "EGRFechaLiquidacion" > p_Fecha))
					  AND "FechaOperacion" <= p_Fecha;
		END IF;
		
		--select * from :v_Datos;
		--select * from :v_Montos;
		v_DatosFin = SELECT 
						dat."FolioCarteraOrigen"
						,to_char("FechaLiquidacion", 'DD-MM-YYYY') as "FechaLiquidacion"
						,"IdEstado"
						,CASE 
							WHEN p_TipoValorizacion = 'C' THEN 'C'
							ELSE 'M'
						 END AS "TipoValorizacion"
						,"IdInstrumento"
						,"IdMonedaInstrumento"
						,"Nemotecnico"
						,"CodigoIFRS"
						,"IdEmisor"
						,"EsFungible"
						,to_char("FechaVencimientoInstrumento", 'DD-MM-YYYY') as "FechaVencimientoInstrumento"
						,to_char("FechaVencimientoOperacion", 'DD-MM-YYYY') as "FechaVencimientoOperacion"
						,"PlazoEnDiasOperacion"
						,to_char("FechaOperacion", 'DD-MM-YYYY') as "FechaOperacion"
						,"INGDurationCompraActual"
						,CASE WHEN p_TipoValorizacion = 'C' THEN "PrecioCompraOperacionOrigen" ELSE "INGPrecioMercadoActual" END AS "INGPrecioMercadoActual"
						,CASE WHEN p_TipoValorizacion = 'C' THEN "TasaCompraOperacionOrigen" ELSE "INGTasaMercadoActual" END AS "INGTasaMercadoActual"
						,CASE WHEN p_TipoValorizacion = 'C' THEN "PrecioCompraOperacionOrigen" ELSE "INGPrecioMercadoProximo" END AS "INGPrecioMercadoProximo"
						,CASE WHEN p_TipoValorizacion = 'C' THEN "TasaCompraOperacionOrigen" ELSE "INGTasaMercadoProximo" END AS "INGTasaMercadoProximo"
						,mon."INGCantidad"
						,mon."INGMontoATasaGarantiaActual"
									
						,mon."INGMontoInteresCompraProximo"
						,mon."INGMontoReajusteCompraProximo"
						
						,CASE 
							WHEN p_TipoValorizacion = 'C' THEN mon."INGMontoATasaCompraActual"
							--WHEN p_TipoValorizacion = 'M' THEN 
							ELSE mon."INGMontoATasaMercadoActual"
						 END AS "INGMontoATasaActual"
						,CASE 
							WHEN p_TipoValorizacion = 'C' THEN mon."INGMontoATasaCompraProximo"
							--WHEN p_TipoValorizacion = 'M' THEN 
							ELSE mon."INGMontoATasaMercadoProximo"
						 END AS "INGMontoATasaProximo"
						 
						 
					FROM :v_Datos dat 
						INNER JOIN :v_Montos mon ON (dat."Id"=1 AND mon."FolioCarteraOrigen" = dat."FolioCarteraOrigen");
						
			SELECT COUNT(*) INTO v_count FROM :v_Datos;
			IF (v_count = 0) THEN
				v_DatosFin = SELECT 0 as "FolioCarteraOrigen"
								,to_char('0001-01-01', 'DD-MM-YYYY') as "FechaLiquidacion"
								,'' as "IdEstado"
								,'' as "TipoValorizacion"
								,0  as "IdInstrumento"
								,'' as "IdMonedaInstrumento"
								,'' as "Nemotecnico"
								,'' as "CodigoIFRS"
								,'' as "IdEmisor"
								,'' as "EsFungible"
								,to_char('0001-01-01', 'DD-MM-YYYY') as "FechaVencimientoInstrumento"
								,to_char('0001-01-01', 'DD-MM-YYYY') as "FechaVencimientoOperacion"
								,0  as "PlazoEnDiasOperacion"
								,to_char('0001-01-01', 'DD-MM-YYYY') as "FechaOperacion"
								,0  as "INGDurationCompraActual"
								,0  as "INGPrecioMercadoActual"
								,0  as "INGTasaMercadoActual"
								,0  as "INGPrecioMercadoProximo"
								,0  as "INGTasaMercadoProximo"
								,0  as "INGCantidad"
								,0  AS "INGMontoATasaGarantiaActual"
											
								,0  as "INGMontoInteresCompraProximo"
								,0  as "INGMontoReajusteCompraProximo"
								
								,0  as "INGMontoATasaActual"
								,0  as "INGMontoATasaProximo"
								
						FROM DUMMY;
			END IF;
			SELECT * FROM :v_DatosFin;
		
	END;
END;
