SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::MRF.CarteraPropiaDisponibleMesaRF";
GO
CREATE PROCEDURE
--  Proposito: Informe cartera Cuenta Propia en Garantia mesa RF 
--  Autor: projo
--  Fecha Creacion: 04/07/2016
"BSCL.Mesa.CrystalReports.Servicios::MRF.CarteraPropiaDisponibleMesaRF" ( 
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
			v_Montos =
				SELECT
					"FolioCartera"
					,SUM (CASE WHEN "IdClasificacionTipoInstrumento"= 'IF'
					 	  THEN  "ValorRescate"
					 	  ELSE  "INGCantidad"
					 END) AS "INGCantidad"
					--,SUM("INGCantidad") as "INGCantidad"
					,SUM("INGTasaCompra") as "INGTasaCompra"
					,SUM("INGMontoInteresCompraProximo") as "INGMontoInteresCompraProximo"
					,SUM("INGMontoReajusteCompraAnterior") as "INGMontoReajusteCompraProximo" --Existe Reajuste al dia de ayer y el acumulado, se deja reajuste al día de ayer						
					,SUM("INGMontoATasaCompraActual") as "INGMontoATasaCompraActual"
					,SUM("INGMontoATasaCompraProximo") as "INGMontoATasaCompraProximo"
					,SUM("INGMontoATasaMercadoActual") as "INGMontoATasaMercadoActual"
					,SUM("INGMontoATasaMercadoAnterior") as "INGMontoATasaMercadoProximo"
					--,SUM("INGMontoCompraActualMonedaInstrumento") AS "INGMontoMonedaInstrumentoActual"
					
					,SUM("INGMontoCompraActualMonedaInstrumento") AS "INGMontoCompraActualMonedaInstrumento"
					,SUM("INGMontoMercadoActualMonedaInstrumento") AS "INGMontoMercadoActualMonedaInstrumento"
					
					,SUM("INGMontoCompraProximoMonedaInstrumento") AS "INGMontoCompraProximoMonedaInstrumento"
					,SUM("INGMontoMercadoProximoMonedaInstrumento") AS "INGMontoMercadoProximoMonedaInstrumento"					
					
				--FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"
				FROM "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarteraUI"
				WHERE "IdCliente"=p_IdCliente 
				  and "IdPortafolio"=p_IdPortafolio 
				  and "IdEstado"='VIG' 
				  and "IdClasificacionTipoInstrumento" in ('RF','IF')
				  AND "EGRCantidad" = 0
				  --AND (("INGFechaLiquidacion" <= p_Fecha AND "EGRCantidad" = 0) OR
				  --	   ("INGFechaLiquidacion" <= p_Fecha AND "EGRCantidad" <> 0 AND "EGRFechaLiquidacion" > p_Fecha))
				  and "IdTipoInstrumento" != 'PAC'
				  AND "FechaRespaldo" = p_Fecha
				GROUP BY "FolioCartera";
	
			v_Datos =
				SELECT 
					ROW_NUMBER() over (PARTITION BY "FolioCartera"	ORDER BY "FolioCartera" DESC) as "Id"
					,"IdCartera"
					,"FolioCartera"
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
					,"INGPrecioCompra"
					,"INGTasaMercadoActual"
					,"INGTasaCompra"					
					,"INGPrecioMercadoProximo"
					,"INGTasaMercadoProximo"
			    --FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"
				FROM "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarteraUI"
				WHERE "IdCliente"=p_IdCliente 
				  and "IdPortafolio"=p_IdPortafolio 
				  and "IdEstado"='VIG' 
				  and "IdClasificacionTipoInstrumento" in ('RF','IF')
				 AND "EGRCantidad" = 0
				  --AND (("INGFechaLiquidacion" <= p_Fecha AND "EGRCantidad" = 0) OR
				  --	   ("INGFechaLiquidacion" <= p_Fecha AND "EGRCantidad" <> 0 AND "EGRFechaLiquidacion" > p_Fecha))
				  and "IdTipoInstrumento" != 'PAC'
				  AND "FechaRespaldo" = p_Fecha;
		ELSE
			v_Montos =
				SELECT
					"FolioCartera"
					,SUM (CASE WHEN "IdClasificacionTipoInstrumento"= 'IF'
					 	  THEN  "ValorRescate"
					 	  ELSE  "INGCantidad"
					 END) AS "INGCantidad"
					--,SUM("INGCantidad") as "INGCantidad"   --IF la cantidad es el valor rescate
					,SUM("INGTasaCompra") as "INGTasaCompra"
					,SUM("INGMontoInteresCompraProximo") as "INGMontoInteresCompraProximo"
					,SUM("INGMontoReajusteCompraAnterior") as "INGMontoReajusteCompraProximo" --Existe Reajuste al dia de ayer y el acumulado, se deja reajuste al día de ayer						
					,SUM("INGMontoATasaCompraActual") as "INGMontoATasaCompraActual"
					,SUM("INGMontoATasaCompraProximo") as "INGMontoATasaCompraProximo"
					,SUM("INGMontoATasaMercadoActual") as "INGMontoATasaMercadoActual"
					,SUM("INGMontoATasaMercadoAnterior") as "INGMontoATasaMercadoProximo"
					--,SUM("INGMontoCompraActualMonedaInstrumento") AS "INGMontoMonedaInstrumentoActual"
					
					,SUM("INGMontoCompraActualMonedaInstrumento") AS "INGMontoCompraActualMonedaInstrumento"
					,SUM("INGMontoMercadoActualMonedaInstrumento") AS "INGMontoMercadoActualMonedaInstrumento"
					
					,SUM("INGMontoCompraProximoMonedaInstrumento") AS "INGMontoCompraProximoMonedaInstrumento"
					,SUM("INGMontoMercadoProximoMonedaInstrumento") AS "INGMontoMercadoProximoMonedaInstrumento"							
				
				FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"
				WHERE "IdCliente"=p_IdCliente 
				  and "IdPortafolio"=p_IdPortafolio 
				  and "IdEstado"='VIG' 
				  and "IdClasificacionTipoInstrumento" in ('RF','IF')
				  AND "EGRCantidad" = 0
				  --AND (("INGFechaLiquidacion" <= p_Fecha AND "EGRCantidad" = 0) OR
					--   ("INGFechaLiquidacion" <= p_Fecha AND "EGRCantidad" <> 0 AND "EGRFechaLiquidacion" > p_Fecha))
				  and "IdTipoInstrumento" != 'PAC'
				  AND "FechaOperacion" <= p_Fecha
				GROUP BY "FolioCartera";
	
			v_Datos =
				SELECT 
					ROW_NUMBER() over (PARTITION BY "FolioCartera"	ORDER BY "FolioCartera" DESC) as "Id"
					,"IdCartera"
					,"FolioCartera"
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
					,"INGPrecioCompra"
					,"INGTasaMercadoActual"
					,"INGTasaCompra"
					,"INGPrecioMercadoProximo"
					,"INGTasaMercadoProximo"
			    FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"
				WHERE "IdCliente"=p_IdCliente 
				  and "IdPortafolio"=p_IdPortafolio 
				  and "IdEstado"='VIG' 
				  and "IdClasificacionTipoInstrumento" in ('RF','IF')
				 AND "EGRCantidad" = 0
				 -- AND (("INGFechaLiquidacion" <= p_Fecha AND "EGRCantidad" = 0) OR
				  --	   ("INGFechaLiquidacion" <= p_Fecha AND "EGRCantidad" <> 0 AND "EGRFechaLiquidacion" > p_Fecha))
				  and "IdTipoInstrumento" != 'PAC'
				  AND "FechaOperacion" <= p_Fecha;
		END IF;
		
		
		--select * from :v_Datos;
		--select * from :v_Montos;
		v_DatosFin = SELECT 
						dat."FolioCartera"
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
						,CASE WHEN p_TipoValorizacion = 'C' THEN "INGPrecioCompra" ELSE "INGPrecioMercadoActual" END AS "INGPrecioMercadoActual"
						,CASE WHEN p_TipoValorizacion = 'C' THEN dat."INGTasaCompra" ELSE "INGTasaMercadoActual" END AS "INGTasaMercadoActual"
						,CASE WHEN p_TipoValorizacion = 'C' THEN "INGPrecioCompra" ELSE "INGPrecioMercadoProximo" END AS "INGPrecioMercadoProximo"
						,CASE WHEN p_TipoValorizacion = 'C' THEN dat."INGTasaCompra" ELSE "INGTasaMercadoProximo" END AS "INGTasaMercadoProximo"
						,mon."INGCantidad"
						,mon."INGTasaCompra"
						
						,CASE WHEN p_TipoValorizacion = 'C'
							THEN mon."INGMontoCompraActualMonedaInstrumento"
							ELSE mon."INGMontoMercadoActualMonedaInstrumento"
						END AS "MontoActualMonedaInstrumento"
						
						,CASE WHEN p_TipoValorizacion = 'C'
							THEN mon."INGMontoCompraProximoMonedaInstrumento"
							ELSE mon."INGMontoMercadoProximoMonedaInstrumento"				
						END AS "MontoProximoMonedaInstrumento" 
									
						,mon."INGMontoInteresCompraProximo"
						,mon."INGMontoReajusteCompraProximo"
						--,mon."INGMontoMonedaInstrumentoActual"
						
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
					INNER JOIN :v_Montos mon ON (dat."Id"=1 AND mon."FolioCartera" = dat."FolioCartera");
		
			SELECT COUNT(*) INTO v_count FROM :v_Datos;
			IF (v_count = 0) THEN
				v_DatosFin = SELECT 0 as "FolioCartera"
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
								,0  AS "INGTasaCompra"								
								
								,0  AS "MontoActualMonedaInstrumento"
								,0  AS "MontoProximoMonedaInstrumento"								
											
								,0  as "INGMontoInteresCompraProximo"
								,0  as "INGMontoReajusteCompraProximo"
								
								,0  as "INGMontoATasaActual"
								,0  as "INGMontoATasaProximo"
								
								--,0  AS "INGMontoMonedaInstrumentoActual"
						FROM DUMMY;
			END IF;
			SELECT * FROM :v_DatosFin;
		
	END;
END;
