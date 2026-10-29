SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarteraUI";
GO

CREATE VIEW "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarteraUI" 
AS 

	SELECT * 
	FROM 
		(	SELECT  
				"RANKG", "IdCartera", "FolioCartera", "IdEmpresa", "NombreEmpresa", "FechaOperacion", "IdCliente", "IdPortafolio", "DocumentoIdentidad", "RazonSocial", "CodigoPortafolio", 
				"DescripcionPortafolio", "IdEstado", "NombreEstado", "IdMonedaSistema", "MonedaSistema", "CodigoIFRS", "IdConcepto", "NombreConcepto", "IdTipoOperacion", 
				"IdTipoComprobante", "FolioComprobante", "FechaComprobante", "IdMesa", "IdBolsa", "NombreBolsa", "IdSucursal", "IdAgente", "IdCondicionLiquidacion", 
				"NombreCondicionLiquidacion", "FechaLiquidacion", "IdClasificacionTipoInstrumento", "ClasificacionTipoInstrumento", "IdTipoInstrumento", "TipoInstrumento", 
				"IdInstrumento", "RazonSocialInstrumento", "ISIN", "Nemotecnico", "IdEmisor", "NemoSVS", "IdMonedaEmision", "IdTipoReajuste", "NombreTipoReajuste", "IdMonedaInstrReajuste", 
				"NombreMonedaInstrReajuste", "FechaEmisionInstrumento", "FechaVencimientoInstrumento", "EsFungible", "IdBoveda", "NombreBoveda", "NumeroCuentaBoveda", "IdTipoCustodia", 
				"NombreTipoCustodia", "IdMonedaInstrumento", "MonedaInstrumento", 
				CASE WHEN "IdClasificacionTipoInstrumento"='IF' THEN "ValorRescate" ELSE "Cantidad" END AS "Cantidad", 
				"CorteMinimo", "ValorRescate", "PrecioCompra", "MontoCompra", "INGFechaOperacion", 
				IFNULL(LAG("EGRFechaLiquidacion") over (partition by "IdEmpresa", "FolioCartera" order by "EGRFechaLiquidacion" ), "INGFechaLiquidacion") as "INGFechaLiquidacion",
				
				CASE WHEN "IdClasificacionTipoInstrumento"='IF' THEN "INGValorRescate" ELSE "INGCantidad" END AS "INGCantidad",
				
				"TasaCompra", "INGTasaCompra", "INGValorRescate", "INGPrecioCompra", "INGPrecioPromedioCompra", "INGMontoPrecioPromedioCompra", "INGMontoATasaCompraActual", 
				"DiferenciaInversion", "INGMontoCompraActualMonedaInstrumento", "INGFuenteTasaPrecioMercadoActual", "INGPrecioMercadoActual", "Rentabilidad", "RentabilidadReal", "INGTasaMercadoActual", 
				"INGMontoATasaMercadoActual", "INGMontoMercadoActualMonedaInstrumento", "INGFechaPagoCorteCupon", "INGInteresesPagoCorteCupon", "INGMontoCorteCupon", "INGMontoCapitalCorteCupon", "INGMontoInteresCorteCupon",
				"INGMontoInteresCompraActual","INGMontoReajusteCompraActual","InteresCorridoMonedaInstrumento","ReajusteCorridoMonedaInstrumento",
				-----------------------------------------------------------------------------------------------------------------------
				"EGRFechaOperacion", "EGRFechaLiquidacion", "EGRIdTipoComprobante", "EGRFolioComprobante", "EGRFechaComprobante", "EGRIdConcepto", "EGRNombreConcepto", 
				CASE WHEN "IdClasificacionTipoInstrumento"='IF' THEN "EGRValorRescate" ELSE "EGRCantidad" END AS "EGRCantidad", 
				"EGRValorRescate", "EGRPrecio", "EGRTasa", "EGRMonto", "EGRMontoMonedaInstrumento","TipoOpcion","INGMontoCompraProximoMonedaInstrumento","INGMontoMercadoProximoMonedaInstrumento", "INGFechaValorizacionCartera", "FechaRespaldo"
				,"RegimenTributario","FechaOrigenCompra","CostoUF"
			FROM (
				SELECT 
				RANK() over (partition by car."IdEmpresa", car."FolioCartera", CASE car."EGRFechaLiquidacion"  WHEN '0001-01-01' THEN '2500-12-31' ELSE car."EGRFechaLiquidacion" END order by CAR."IdCartera" desc) as "RANKG"
				, car."IdCartera"
				, car."FolioCartera"
				, car."IdEmpresa"
				, Empresa."Nombre" AS "NombreEmpresa"
				, car."FechaOperacion"
				, car."IdCliente"
				, car."IdPortafolio"
				, cli."DocumentoIdentidad" AS "DocumentoIdentidad"
				, cli."NombreCompleto" AS "RazonSocial"
				, Port."CodigoPortafolio"
				, Port."DescripcionPortafolio"
				, car."IdEstado"
				, Estado."Nombre" AS "NombreEstado"
				, car."IdMonedaSistema"
				, Mon1."Nombre" AS "MonedaSistema"
				, car."CodigoIFRS"
				, car."IdConcepto"
				, Concepto."Nombre" as "NombreConcepto"
				, car."IdTipoOperacion"
				, car."IdTipoComprobante"
				, car."FolioComprobante"
				, car."FechaComprobante"
				, car."IdMesa"
				, car."IdBolsa"
				, Bolsa."Nombre" AS "NombreBolsa"
				, car."IdSucursal"
				, car."IdAgente"
				, car."IdCondicionLiquidacion"
				, CondicionLiquidacion."Nombre" AS "NombreCondicionLiquidacion"
				, car."FechaLiquidacion"
				, car."IdClasificacionTipoInstrumento"
				, ClasInst."Nombre" as "ClasificacionTipoInstrumento"
				, car."IdTipoInstrumento"
				, TipoInstrumento."Descripcion" AS "TipoInstrumento"
				, car."IdInstrumento"
				, Instrumento."RazonSocial" AS "RazonSocialInstrumento"
				, car."ISIN"
				,case 
					when car."IdTipoInstrumento" IN ('SIM','PRT','EMP','PAC','RRV','RRF') then
						(select "Nemotecnico" 
						from (
								select "Nemotecnico", row_number() over(order by (SELECT 1 from dummy) ) as "NroFila" 
								from "BSCL.Custodia.BD::CUS.Subyacente" as miSub
								where car."FolioCartera" = miSub."FolioCarteraRelacionada"
							 ) as Suby
						where Suby."NroFila" = 1
						)
					when car."IdClasificacionTipoInstrumento" = 'IF' THEN car."NemoSVS"
					else car."Nemotecnico"
				end as "Nemotecnico"
				
				, car."IdEmisor"
				, car."NemoSVS"
				, CASE WHEN IFNULL(car."IdMonedaEmision",'') <> '' THEN car."IdMonedaEmision" 
					   WHEN IFNULL(car."IdTipoReajuste",'') <> '' THEN car."IdTipoReajuste" 
				       ELSE car."IdMonedaInstrumento"
				  END AS "IdMonedaEmision"
				       
				, car."IdTipoReajuste"
				, TipoReajuste."Nombre" AS "NombreTipoReajuste"
				, case when ifnull(car."IdTipoReajuste",'') <> '' then car."IdTipoReajuste"
					   when ifnull(car."IdMonedaInstrumento",'') <> '' then car."IdMonedaInstrumento" 
					   when ifnull(car."IdMonedaPrincipal",'') <> '' then car."IdMonedaPrincipal" 
					   else ''
				  end as "IdMonedaInstrReajuste"
				, case when ifnull(car."IdMonedaInstrumento",'') <> '' then Mon2."Nombre" else ifnull(TipoReajuste."Nombre",'') end as "NombreMonedaInstrReajuste"
				, car."FechaEmisionInstrumento"
				, case 
					when car."IdTipoInstrumento" IN ('SIM','PRT','EMP','PAC','RRV','RRF') then car."FechaVencimientoOperacion" 
					else car."FechaVencimientoInstrumento"
				  end as "FechaVencimientoInstrumento"
				, car."EsFungible"
				
				---------------------------------------------------------
				, CASE WHEN COALESCE(CCPI."IdCuentaCustodioPortafolio",0) > 0 THEN CCPI."IdBoveda" ELSE car."IdBoveda" END "IdBoveda"
				, CASE WHEN COALESCE(CCPI."IdCuentaCustodioPortafolio",0) > 0 THEN CCPI."NombreBoveda" ELSE Boveda."Nombre" END AS "NombreBoveda"
				, COALESCE(CCPI."Codigo",'') AS "NumeroCuentaBoveda"
				, CCPI."IdTipoCustodia"
				, CCPI."NombreTipoCustodia"
				---------------------------------------------------------
				
				, car."IdMonedaInstrumento"
				, Mon2."Nombre" AS "MonedaInstrumento"
				, CASE WHEN car."IdClasificacionTipoInstrumento"='IF' THEN car."ValorRescate" ELSE car."Cantidad" END AS "Cantidad"
				, car."CorteMinimo"
				, car."ValorRescate"
				, car."PrecioCompra"
				, car."MontoCompra"
				, car."INGFechaOperacion"
				, car."INGFechaLiquidacion"
				, CASE WHEN car."IdClasificacionTipoInstrumento"='IF' THEN car."INGValorRescate" ELSE car."INGCantidad" END AS "INGCantidad"
				, car."TasaCompra"
				, car."INGTasaCompra"
				, car."INGValorRescate"
				, car."INGPrecioCompra"
				, car."INGPrecioPromedioCompra"
				, car."INGMontoPrecioPromedioCompra"
				, car."INGMontoATasaCompraActual"
				, car."INGFechaValorizacionCartera"
				
				--, car."INGMontoCompraActualMonedaInstrumento"
				, car."INGMontoCompraProximoMonedaInstrumento"
				                                        
				-- , car."INGMontoMercadoActualMonedaInstrumento"
		 		 , car."INGMontoMercadoProximoMonedaInstrumento"
				
				--(Diferencia = Valor Mercado - Valor compra
--				, (CASE car."IdClasificacionTipoInstrumento" WHEN 'RV' THEN (CASE WHEN COALESCE(PREMER."PrecioCierre",0 )=0 THEN car."INGMontoATasaMercadoActual" ELSE COALESCE(PREMER."PrecioCierre",0 ) * "INGCantidad" END)  
--															 ELSE car."INGMontoATasaMercadoActual" END) - car."INGMontoATasaCompraActual"
				, (CASE car."IdClasificacionTipoInstrumento" 
						WHEN 'RV' THEN (CASE WHEN COALESCE(car."INGMontoATasaMercadoActual",0 )=0 THEN COALESCE(PREMER."PrecioCierre",0)*"INGCantidad" ELSE car."INGMontoATasaMercadoActual" END)  
						ELSE car."INGMontoATasaMercadoActual" 
				   END) - car."INGMontoATasaCompraActual" 
					as "DiferenciaInversion"
		
				, car."INGMontoCompraActualMonedaInstrumento"
				, car."INGFuenteTasaPrecioMercadoActual"
				, car."INGPrecioMercadoActual"--(CASE car."IdClasificacionTipoInstrumento" WHEN 'RV' THEN COALESCE(car."INGPrecioMercadoActual", PREMER."PrecioCierre",0)  ELSE car."INGPrecioMercadoActual" END ) 
						--AS "INGPrecioMercadoActual"
				, car."INGTasaMercadoActual"
				, car."INGMontoATasaMercadoActual"
				,(((CASE car."IdClasificacionTipoInstrumento" WHEN 'RV' THEN (CASE WHEN COALESCE(PREMER."PrecioCierre",0 )=0 THEN car."INGMontoMercadoActualMonedaInstrumento" ELSE COALESCE(PREMER."PrecioCierre",0 ) * "INGCantidad" END)  
															 ELSE car."INGMontoMercadoActualMonedaInstrumento" END)
			         - car."INGMontoCompraActualMonedaInstrumento")	/  (CASE WHEN car."INGMontoCompraActualMonedaInstrumento" >0
																				 THEN car."INGMontoCompraActualMonedaInstrumento"ELSE 1 END))*100	
				   AS "Rentabilidad"
				, car."MontoCompraFactor" AS "RentabilidadReal"   
				,car."INGMontoMercadoActualMonedaInstrumento"
						
				/*
				 * 
				 * ESTE PROCESO DE CALCULO EN EL PROCESO DE VALORIZACION 
--				, (CASE car."IdClasificacionTipoInstrumento" WHEN 'RV' THEN (CASE WHEN COALESCE(PREMER."PrecioCierre",0 )=0 THEN car."INGMontoMercadoActualMonedaInstrumento" ELSE COALESCE(PREMER."PrecioCierre",0 ) * "INGCantidad" END)  
--															 ELSE car."INGMontoMercadoActualMonedaInstrumento" END)  
				, (CASE car."IdClasificacionTipoInstrumento" WHEN 'RV' THEN (CASE WHEN COALESCE(car."INGMontoMercadoActualMonedaInstrumento",0 )>0 THEN car."INGMontoMercadoActualMonedaInstrumento" ELSE COALESCE(PREMER."PrecioCierre",0 )* car."INGCantidad" END)  
															 ELSE car."INGMontoMercadoActualMonedaInstrumento" END)  
					AS "INGMontoMercadoActualMonedaInstrumento"
				*/	
					
				, car."INGFechaPagoCorteCupon"
				, car."INGInteresesPagoCorteCupon"
				, car."INGMontoCorteCupon"
				, car."INGMontoCapitalCorteCupon"
				, car."INGMontoInteresCorteCupon"
				, CASE car."EGRFechaOperacion"  WHEN '0001-01-01' THEN '2500-12-31' ELSE car."EGRFechaOperacion" END AS "EGRFechaOperacion" 
				, CASE car."EGRFechaLiquidacion"  WHEN '0001-01-01' THEN '2500-12-31' ELSE car."EGRFechaLiquidacion" END AS "EGRFechaLiquidacion" 
				, car."EGRIdTipoComprobante"
				, car."EGRFolioComprobante"
				, car."EGRFechaComprobante"
				, car."EGRIdConcepto"
				, ConceptoEGR."Nombre" as "EGRNombreConcepto"
				, CASE WHEN car."IdClasificacionTipoInstrumento"='IF' THEN car."EGRValorRescate" ELSE car."EGRCantidad" END AS "EGRCantidad"
				, car."EGRValorRescate"
				, car."EGRPrecio"
				, car."EGRTasa"
				, car."EGRMonto"
				, car."EGRMontoMonedaInstrumento"
				, car."FechaRespaldo"
				
				, car."INGMontoInteresCompraProximoMonedaInstrumento" AS  "INGMontoInteresCompraActual" 
				, car."INGMontoReajusteCompraAnteriorMonedaInstrumento" AS "INGMontoReajusteCompraActual"
				, CASE WHEN car."IdTipoOperacion" IN ('SIM','PAC','RRV','RRF') THEN car."INGMontoInteresCorridoCompraActualMonedaInstrumento" ELSE car."INGMontoInteresCorteCuponMonedaInstrumento" END AS "InteresCorridoMonedaInstrumento"
				, CASE WHEN car."IdTipoOperacion" IN ('SIM','PAC','RRV','RRF') THEN car."INGMontoReajusteCorridoCompraActualMonedaInstrumento" ELSE car."INGMontoReajusteCorteCuponMonedaInstrumento" END AS "ReajusteCorridoMonedaInstrumento"
				, Instrumento."EsPutEsCall" AS "TipoOpcion"
				, case
					when car."PlazoEnDiasOperacion" > 0 then car."PlazoEnDiasOperacion"
					when car."FechaVencimientoOperacion" > TO_DATE('0001-01-01','YYYY-MM-DD') then DAYS_BETWEEN(car."FechaOperacion", car."FechaVencimientoOperacion")
					else 0
					end as "PlazoEnDiasOperacion"
				, car."INGDurationCompraActual"
				, car."INGPrecioMercadoProximo"
				, car."INGTasaMercadoProximo"
				, car."TipoCarteraCorredor"
				, COALESCE (REG."Descripcion",'') AS "RegimenTributario"
				, car."FechaOrigenCompra"
				, car."INGCostoInversionEnFactorAjuste" as "CostoUF"
			FROM "BSCL.Custodia.BD::HIS.Cartera" AS car 
			
				LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as Port ON Port."IdPortafolio" = car."IdPortafolio" 
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as Cli ON Cli."IdCliente" = Port."IdCliente" 
				LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" as Boveda ON car."IdBoveda" = Boveda."IdBoveda" 
				LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoReajuste" as TipoReajuste ON car."IdTipoReajuste" = TipoReajuste."IdTipoReajuste" 
				LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" as Bolsa ON car."IdBolsa" = Bolsa."IdBolsa" 
				LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as Concepto ON car."IdConcepto" = Concepto."IdConcepto" 
				LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as ConceptoEGR ON car."EGRIdConcepto" = ConceptoEGR."IdConcepto" 
				LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" as CondicionLiquidacion ON car."IdCondicionLiquidacion" = CondicionLiquidacion."IdCondicionDeLiquidacion" 
				LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" as Empresa ON car."IdEmpresa" = Empresa."IdEmpresa" 
				LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as Estado ON car."IdEstado" = Estado."IdEstado" 
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as Instrumento ON car."IdInstrumento" = Instrumento."IdInstrumento"
				LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento ON TipoInstrumento."IdTipoInstrumento" = car."IdTipoInstrumento" 
				LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as ClasInst ON ClasInst."IdClasificacionTipoInstrumento" = car."IdClasificacionTipoInstrumento" 
				LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon1 ON car."IdMonedaSistema" = Mon1."IdMoneda" 
				LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon2 ON car."IdMonedaInstrumento" = Mon2."IdMoneda" 
				LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" as CCPI ON CCPI."IdCuentaCustodioPortafolio" = car."IdCuentaCustodioPortafolio"
				LEFT JOIN (
							---Precio Mercado actual (monedad instrumento) x Instrumento
							SELECT PRE."IdInstrumento",PRE."PrecioCierre",PRE."Fecha",PRE."IdEmpresa"	
							FROM
								( 	
												
									SELECT row_number() over (partition by prec."IdInstrumento",SIS."IdEmpresa" order by prec."IdInstrumento",prec."Fecha" desc) AS row_num
									,prec."IdInstrumento",prec."Fecha",	prec."IdBolsa",prec."PrecioCierre",SIS."IdEmpresa"
									FROM "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" prec
									INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" SIS 
									ON  ( SIS."CodigoParametro" = 'PLA.FechaSistema' AND TO_DATE(prec."Fecha") <= TO_DATE(SIS."Valor")) 
									 where prec."IdBolsa" = 'BCS'
							) PRE	
							WHERE PRE.row_num = 1 AND PRE."PrecioCierre" > 0 
						)PREMER
				ON (car."IdInstrumento"=PREMER."IdInstrumento"  AND CAR."IdEmpresa"= PREMER."IdEmpresa")			
				LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = car."IdRegimenFondoMutuo"		
			)
			where "RANKG" = 1 
		)
		
		
	ORDER BY "FolioCartera"
	WITH READ ONLY;

GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarteraUI"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
