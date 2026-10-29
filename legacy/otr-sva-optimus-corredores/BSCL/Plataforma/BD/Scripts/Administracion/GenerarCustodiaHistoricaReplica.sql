--select distinct "FechaRespaldo" from "BSCL"."BSCL.Custodia.BD::HIS.Cartera" order by "FechaRespaldo";
SELECT "FechaCustodia",count(1) from BSCL."BSCL.Custodia.BD::HIS.CustodiaFechaLiquidacion" GROUP BY "FechaCustodia";
SELECT "FechaCustodia",count(1) from BSCL."BSCL.Custodia.BD::HIS.CustodiaFechaOperacion" GROUP BY "FechaCustodia";

INSERT INTO BSCL."BSCL.Custodia.BD::HIS.CustodiaFechaLiquidacion"
select
		 cast("IdEmpresa" as bigint)				 			as "IdEmpresa"
		,cast("TipoFechaConsulta" as nvarchar(3)) 				as "TipoFechaConsulta"
		,cast("FechaCustodia" as nvarchar(10)) 					as "FechaCustodia"
		,CASE WHEN "IdClasificacionTipoInstrumento" = '' THEN NULL ELSE cast("IdClasificacionTipoInstrumento" as nvarchar(3)) END as "IdClasificacionTipoInstrumento" 
		,cast("NombreClasificacionTipoInstrumento" as nvarchar(100)) as "NombreClasificacionTipoInstrumento"
		,CASE WHEN "IdTipoInstrumento" = '' THEN NULL ELSE cast("IdTipoInstrumento" as nvarchar(3)) END	as "IdTipoInstrumento" 
		,cast("NombreTipoInstrumento" as nvarchar(100)) 		as "NombreTipoInstrumento" 
		,cast("ISIN" as nvarchar(40)) 							as "ISIN"
		,CASE WHEN "IdInstrumento" = 0 THEN NULL ELSE cast("IdInstrumento" as bigint) END as "IdInstrumento" 
		,cast("Nemotecnico" as nvarchar(40)) 					as "Nemotecnico" 
		,CASE WHEN "IdEmisor" = '' THEN NULL ELSE cast("IdEmisor" as nvarchar(40)) END	as "IdEmisor" 
		,cast("EsFungible" as nvarchar(1)) 						as "EsFungible" 
		,CASE WHEN "IdCliente" = 0 THEN NULL ELSE cast("IdCliente" as bigint) END as "IdCliente" 
		,CASE WHEN "IdPortafolio" = 0 THEN NULL ELSE cast("IdPortafolio" as bigint) END as "IdPortafolio" 
		,cast("DocumentoIdentidad" as nvarchar(40)) 			as "DocumentoIdentidad" 
		,cast("NombreCliente" as nvarchar(302)) 				as "NombreCliente" 
		,cast("CodigoPortafolio" as nvarchar(100)) 				as "CodigoPortafolio" 
		,CASE WHEN "IdBoveda" = '' THEN NULL ELSE cast("IdBoveda" as nvarchar(3)) END as "IdBoveda" 
		,cast("NumeroCuentaBoveda" as nvarchar(40)) 			as "NumeroCuentaBoveda"
		,cast("Relacion" as nvarchar(100)) 						as "Relacion" 
		,cast("DescripcionRelacion" as nvarchar(100)) 			as "DescripcionRelacion" 
	--CUSTODIA
		,cast("CantidadCustodia" as decimal(26,8)) 				as "CantidadCustodia" 
		,cast("PPPCustodia" as decimal(26,8)) 					as "PPPCustodia" 
		,cast("ValorPPPCustodia" as decimal(26,8)) 				as "ValorPPPCustodia" 
		,cast("MontoPPPCustodia" as decimal(26,8))	 			as "MontoPPPCustodia" 
		,cast("PrecioMercadoCustodia" as decimal(26,8)) 		as "PrecioMercadoCustodia"
		--,cast("PrecioCompraCustodia" as decimal(26,8)) 			as "PrecioCompraCustodia"
		,cast("Rentabilidad" as decimal(26,8)) 					as "Rentabilidad"
		,cast("ValorMercadoCustodia" as decimal(26,8)) 			as "ValorMercadoCustodia"
		,cast("MontoMercadoCustodia" as decimal(26,8)) 			as "MontoMercadoCustodia"
		,cast("DiferenciaMercadoCustodia" as decimal(26,8)) 	as "DiferenciaMercadoCustodia"
		,CASE WHEN "IdMonedaInstrReajuste" <> '' THEN cast("IdMonedaInstrReajuste" as nvarchar(3)) else '' END as "IdMonedaReajuste"
		,cast("EsPagaderoEn" as nvarchar(3)) 					as "EsPagaderoEn"
	--SIMULTANEA COMPRA
		,cast("CantidadSimulCom" as decimal(26,8)) 				as "CantidadSimulCom" 
		,cast("PPPSimulCom" as decimal(26,8)) 					as "PPPSimulCom" 
		,cast("ValorPPPSimulCom" as decimal(26,8)) 				as "ValorPPPSimulCom" 
		,cast("PrecioMercadoSimulCom" as decimal(26,8)) 		as "PrecioMercadoSimulCom"
		,cast("ValorMercadoSimulCom" as decimal(26,8)) 			as "ValorMercadoSimulCom"
		,cast("DiferenciaMercadoSimulCom" as decimal(26,8)) 	as "DiferenciaMercadoSimulCom"
	--SIMULTANEA VENTA
		,cast("CantidadSimulVen" as decimal(26,8)) 				as "CantidadSimulVen" 
		,cast("PPPSimulVen" as decimal(26,8)) 					as "PPPSimulVen" 
		,cast("ValorPPPSimulVen" as decimal(26,8)) 				as "ValorPPPSimulVen" 
		,cast("PrecioMercadoSimulVen" as decimal(26,8)) 		as "PrecioMercadoSimulVen"
		,cast("ValorMercadoSimulVen" as decimal(26,8)) 			as "ValorMercadoSimulVen"
		,cast("DiferenciaMercadoSimulVen" as decimal(26,8)) 	as "DiferenciaMercadoSimulVen"
	--PRESTAMO 
		,cast("CantidadPrestamo" as decimal(26,8)) 				as "CantidadPrestamo" 
		,cast("PPPPrestamo" as decimal(26,8)) 					as "PPPPrestamo" 
		,cast("ValorPPPPrestamo" as decimal(26,8)) 				as "ValorPPPPrestamo" 
		,cast("PrecioMercadoPrestamo" as decimal(26,8)) 		as "PrecioMercadoPrestamo"
		,cast("ValorMercadoPrestamo" as decimal(26,8)) 			as "ValorMercadoPrestamo"
		,cast("DiferenciaMercadoPrestamo" as decimal(26,8)) 	as "DiferenciaMercadoPrestamo"
	--EMPRESTITO
		,cast("CantidadEmpresti" as decimal(26,8)) 				as "CantidadEmpresti" 
		,cast("PPPEmpresti" as decimal(26,8)) 					as "PPPEmpresti" 
		,cast("ValorPPPEmpresti" as decimal(26,8)) 				as "ValorPPPEmpresti" 
		,cast("PrecioMercadoEmpresti" as decimal(26,8)) 		as "PrecioMercadoEmpresti"
		,cast("ValorMercadoEmpresti" as decimal(26,8)) 			as "ValorMercadoEmpresti"
		,cast("DiferenciaMercadoEmpresti" as decimal(26,8)) 	as "DiferenciaMercadoEmpresti"
	--GARANTIA
		,cast("CantidadGarantia" as decimal(26,8)) 				as "CantidadGarantia" 
		,cast("PPPGarantia" as decimal(26,8)) 					as "PPPGarantia" 
		,cast("ValorPPPGarantia" as decimal(26,8)) 				as "ValorPPPGarantia" 
		,cast("PrecioMercadoGarantia" as decimal(26,8)) 		as "PrecioMercadoGarantia"
		,cast("ValorMercadoGarantia" as decimal(26,8)) 			as "ValorMercadoGarantia"
		,cast("DiferenciaMercadoGarantia" as decimal(26,8)) 	as "DiferenciaMercadoGarantia"
	--INGRESO
		,cast("CantidadTransIng" as decimal(26,8)) 				as "CantidadTransIng" 
	--EGRESO
		,cast("CantidadTransEgr" as decimal(26,8)) 				as "CantidadTransEgr" 
	--TOTALIZADORES
		,cast("TotalPosicion" as decimal(26,8)) 				as "TotalPosicion"    
		,cast("TotalPosicionPresunta" as decimal(26,8)) 		as "TotalPosicionPresunta"
	-- PACTOS VENTA		
		,CAST("CantidadPactosV" as decimal(26,8))				AS "CantidadPactosV"
		,CAST("PPPPactosV" as decimal(26,8))					AS "PPPPactosV"
		,CAST("ValorPPPPactosV" as decimal(26,8))				AS "ValorPPPPactosV"
		,CAST("PrecioMercadoPactosV" as decimal(26,8))			AS "PrecioMercadoPactosV"
		,CAST("ValorMercadoPactosV" as decimal(26,8))			AS "ValorMercadoPactosV"
		,CAST("DiferenciaMercadoPactosV" as decimal(26,8))		AS "DiferenciaMercadoPactosV"
	-- PACTOS COMPRA
		,CAST("CantidadPactosC" as decimal(26,8))				AS "CantidadPactosC"
		,CAST("PPPPactosC" as decimal(26,8))					AS "PPPPactosC"
		,CAST("ValorPPPPactosC" as decimal(26,8))				AS "ValorPPPPactosC"
		,CAST("PrecioMercadoPactosC" as decimal(26,8))			AS "PrecioMercadoPactosC"
		,CAST("ValorMercadoPactosC" as decimal(26,8))			AS "ValorMercadoPactosC"
		,CAST("DiferenciaMercadoPactosC" as decimal(26,8))		AS "DiferenciaMercadoPactosC"
		,"IdMatrizAgrupador"									AS "IdMatrizAgrupador"
		,"EsADC"												AS "EsADC"
	 from "BSCL.Custodia.BD.Vistas::ConsultarCustodiaPortafolioAnaliticaHis" WITH PARAMETERS(
		'PLACEHOLDER' = ('$$p_idempresa$$','1'),
		'PLACEHOLDER' = ('$$p_fechaconsulta$$','2018-10-31'),
		'PLACEHOLDER' = ('$$p_tipofechaconsulta$$','LIQ')
	 );
	 
	 INSERT INTO BSCL."BSCL.Custodia.BD::HIS.CustodiaFechaOperacion"
select
		 cast("IdEmpresa" as bigint)				 			as "IdEmpresa"
		,cast("TipoFechaConsulta" as nvarchar(3)) 				as "TipoFechaConsulta"
		,cast("FechaCustodia" as nvarchar(10)) 					as "FechaCustodia"
		,CASE WHEN "IdClasificacionTipoInstrumento" = '' THEN NULL ELSE cast("IdClasificacionTipoInstrumento" as nvarchar(3)) END as "IdClasificacionTipoInstrumento" 
		,cast("NombreClasificacionTipoInstrumento" as nvarchar(100)) as "NombreClasificacionTipoInstrumento"
		,CASE WHEN "IdTipoInstrumento" = '' THEN NULL ELSE cast("IdTipoInstrumento" as nvarchar(3)) END	as "IdTipoInstrumento" 
		,cast("NombreTipoInstrumento" as nvarchar(100)) 		as "NombreTipoInstrumento" 
		,cast("ISIN" as nvarchar(40)) 							as "ISIN"
		,CASE WHEN "IdInstrumento" = 0 THEN NULL ELSE cast("IdInstrumento" as bigint) END as "IdInstrumento" 
		,cast("Nemotecnico" as nvarchar(40)) 					as "Nemotecnico" 
		,CASE WHEN "IdEmisor" = '' THEN NULL ELSE cast("IdEmisor" as nvarchar(40)) END	as "IdEmisor" 
		,cast("EsFungible" as nvarchar(1)) 						as "EsFungible" 
		,CASE WHEN "IdCliente" = 0 THEN NULL ELSE cast("IdCliente" as bigint) END as "IdCliente" 
		,CASE WHEN "IdPortafolio" = 0 THEN NULL ELSE cast("IdPortafolio" as bigint) END as "IdPortafolio" 
		,cast("DocumentoIdentidad" as nvarchar(40)) 			as "DocumentoIdentidad" 
		,cast("NombreCliente" as nvarchar(302)) 				as "NombreCliente" 
		,cast("CodigoPortafolio" as nvarchar(100)) 				as "CodigoPortafolio" 
		,CASE WHEN "IdBoveda" = '' THEN NULL ELSE cast("IdBoveda" as nvarchar(3)) END as "IdBoveda" 
		,cast("NumeroCuentaBoveda" as nvarchar(40)) 			as "NumeroCuentaBoveda"
		,cast("Relacion" as nvarchar(100)) 						as "Relacion" 
		,cast("DescripcionRelacion" as nvarchar(100)) 			as "DescripcionRelacion" 
	--CUSTODIA
		,cast("CantidadCustodia" as decimal(26,8)) 				as "CantidadCustodia" 
		,cast("PPPCustodia" as decimal(26,8)) 					as "PPPCustodia" 
		,cast("ValorPPPCustodia" as decimal(26,8)) 				as "ValorPPPCustodia" 
		,cast("MontoPPPCustodia" as decimal(26,8))	 			as "MontoPPPCustodia" 
		,cast("PrecioMercadoCustodia" as decimal(26,8)) 		as "PrecioMercadoCustodia"
		--,cast("PrecioCompraCustodia" as decimal(26,8)) 			as "PrecioCompraCustodia"
		,cast("Rentabilidad" as decimal(26,8)) 					as "Rentabilidad"
		,cast("ValorMercadoCustodia" as decimal(26,8)) 			as "ValorMercadoCustodia"
		,cast("MontoMercadoCustodia" as decimal(26,8)) 			as "MontoMercadoCustodia"
		,cast("DiferenciaMercadoCustodia" as decimal(26,8)) 	as "DiferenciaMercadoCustodia"
		,CASE WHEN "IdMonedaInstrReajuste" <> '' THEN cast("IdMonedaInstrReajuste" as nvarchar(3)) else '' END as "IdMonedaReajuste"
		,cast("EsPagaderoEn" as nvarchar(3)) 					as "EsPagaderoEn"
	--SIMULTANEA COMPRA
		,cast("CantidadSimulCom" as decimal(26,8)) 				as "CantidadSimulCom" 
		,cast("PPPSimulCom" as decimal(26,8)) 					as "PPPSimulCom" 
		,cast("ValorPPPSimulCom" as decimal(26,8)) 				as "ValorPPPSimulCom" 
		,cast("PrecioMercadoSimulCom" as decimal(26,8)) 		as "PrecioMercadoSimulCom"
		,cast("ValorMercadoSimulCom" as decimal(26,8)) 			as "ValorMercadoSimulCom"
		,cast("DiferenciaMercadoSimulCom" as decimal(26,8)) 	as "DiferenciaMercadoSimulCom"
	--SIMULTANEA VENTA
		,cast("CantidadSimulVen" as decimal(26,8)) 				as "CantidadSimulVen" 
		,cast("PPPSimulVen" as decimal(26,8)) 					as "PPPSimulVen" 
		,cast("ValorPPPSimulVen" as decimal(26,8)) 				as "ValorPPPSimulVen" 
		,cast("PrecioMercadoSimulVen" as decimal(26,8)) 		as "PrecioMercadoSimulVen"
		,cast("ValorMercadoSimulVen" as decimal(26,8)) 			as "ValorMercadoSimulVen"
		,cast("DiferenciaMercadoSimulVen" as decimal(26,8)) 	as "DiferenciaMercadoSimulVen"
	--PRESTAMO 
		,cast("CantidadPrestamo" as decimal(26,8)) 				as "CantidadPrestamo" 
		,cast("PPPPrestamo" as decimal(26,8)) 					as "PPPPrestamo" 
		,cast("ValorPPPPrestamo" as decimal(26,8)) 				as "ValorPPPPrestamo" 
		,cast("PrecioMercadoPrestamo" as decimal(26,8)) 		as "PrecioMercadoPrestamo"
		,cast("ValorMercadoPrestamo" as decimal(26,8)) 			as "ValorMercadoPrestamo"
		,cast("DiferenciaMercadoPrestamo" as decimal(26,8)) 	as "DiferenciaMercadoPrestamo"
	--EMPRESTITO
		,cast("CantidadEmpresti" as decimal(26,8)) 				as "CantidadEmpresti" 
		,cast("PPPEmpresti" as decimal(26,8)) 					as "PPPEmpresti" 
		,cast("ValorPPPEmpresti" as decimal(26,8)) 				as "ValorPPPEmpresti" 
		,cast("PrecioMercadoEmpresti" as decimal(26,8)) 		as "PrecioMercadoEmpresti"
		,cast("ValorMercadoEmpresti" as decimal(26,8)) 			as "ValorMercadoEmpresti"
		,cast("DiferenciaMercadoEmpresti" as decimal(26,8)) 	as "DiferenciaMercadoEmpresti"
	--GARANTIA
		,cast("CantidadGarantia" as decimal(26,8)) 				as "CantidadGarantia" 
		,cast("PPPGarantia" as decimal(26,8)) 					as "PPPGarantia" 
		,cast("ValorPPPGarantia" as decimal(26,8)) 				as "ValorPPPGarantia" 
		,cast("PrecioMercadoGarantia" as decimal(26,8)) 		as "PrecioMercadoGarantia"
		,cast("ValorMercadoGarantia" as decimal(26,8)) 			as "ValorMercadoGarantia"
		,cast("DiferenciaMercadoGarantia" as decimal(26,8)) 	as "DiferenciaMercadoGarantia"
	--INGRESO
		,cast("CantidadTransIng" as decimal(26,8)) 				as "CantidadTransIng" 
	--EGRESO
		,cast("CantidadTransEgr" as decimal(26,8)) 				as "CantidadTransEgr" 
	--TOTALIZADORES
		,cast("TotalPosicion" as decimal(26,8)) 				as "TotalPosicion"    
		,cast("TotalPosicionPresunta" as decimal(26,8)) 		as "TotalPosicionPresunta"
	-- PACTOS VENTA		
		,CAST("CantidadPactosV" as decimal(26,8))				AS "CantidadPactosV"
		,CAST("PPPPactosV" as decimal(26,8))					AS "PPPPactosV"
		,CAST("ValorPPPPactosV" as decimal(26,8))				AS "ValorPPPPactosV"
		,CAST("PrecioMercadoPactosV" as decimal(26,8))			AS "PrecioMercadoPactosV"
		,CAST("ValorMercadoPactosV" as decimal(26,8))			AS "ValorMercadoPactosV"
		,CAST("DiferenciaMercadoPactosV" as decimal(26,8))		AS "DiferenciaMercadoPactosV"
	-- PACTOS COMPRA
		,CAST("CantidadPactosC" as decimal(26,8))				AS "CantidadPactosC"
		,CAST("PPPPactosC" as decimal(26,8))					AS "PPPPactosC"
		,CAST("ValorPPPPactosC" as decimal(26,8))				AS "ValorPPPPactosC"
		,CAST("PrecioMercadoPactosC" as decimal(26,8))			AS "PrecioMercadoPactosC"
		,CAST("ValorMercadoPactosC" as decimal(26,8))			AS "ValorMercadoPactosC"
		,CAST("DiferenciaMercadoPactosC" as decimal(26,8))		AS "DiferenciaMercadoPactosC"
		,"IdMatrizAgrupador"									AS "IdMatrizAgrupador"
		,"EsADC"												AS "EsADC"
	 from "BSCL.Custodia.BD.Vistas::ConsultarCustodiaPortafolioAnaliticaHis" WITH PARAMETERS(
		'PLACEHOLDER' = ('$$p_idempresa$$','1'),
		'PLACEHOLDER' = ('$$p_fechaconsulta$$','2018-10-31'),
		'PLACEHOLDER' = ('$$p_tipofechaconsulta$$','ADQ')
	 );


commit;

drop table BSCL."BSCL.Custodia.BD::HIS.CustodiaFechaLiquidacion";
CREATE COLUMN TABLE BSCL."BSCL.Custodia.BD::HIS.CustodiaFechaLiquidacion"(
"IdEmpresa" BIGINT,
"TipoFechaConsulta" NVARCHAR(3),
"FechaCustodia" CHAR(10),
"IdClasificacionTipoInstrumento" NVARCHAR(3),
"NombreClasificacionTipoInstrumento" NVARCHAR(100),
"IdTipoInstrumento" NVARCHAR(3),
"NombreTipoInstrumento" NVARCHAR(100),
"ISIN" NVARCHAR(40),
"IdInstrumento" BIGINT,
"Nemotecnico" NVARCHAR(40),
"IdEmisor" NVARCHAR(40),
"EsFungible" NVARCHAR(1),
"IdCliente" BIGINT,
"IdPortafolio" BIGINT,
"DocumentoIdentidad" NVARCHAR(40),
"NombreCliente" NVARCHAR(302),
"CodigoPortafolio" NVARCHAR(100),
"IdBoveda" NVARCHAR(3),
"NumeroCuentaBoveda" NVARCHAR(40),
"Relacion" NVARCHAR(100),
"DescripcionRelacion" NVARCHAR(100),
"CantidadCustodia" DECIMAL(26,8),
"PPPCustodia" DECIMAL(26,8),
"ValorPPPCustodia" DECIMAL(26,8),
"MontoPPPCustodia" DECIMAL(26,8),
"PrecioMercadoCustodia" DECIMAL(26,8),
"Rentabilidad" DECIMAL(26,8),
"ValorMercadoCustodia" DECIMAL(26,8),
"MontoMercadoCustodia" DECIMAL(26,8),
"DiferenciaMercadoCustodia" DECIMAL(26,8),
"IdMonedaReajuste" NVARCHAR(3),
"EsPagaderoEn" NVARCHAR(3),
"CantidadSimulCom" DECIMAL(26,8),
"PPPSimulCom" DECIMAL(26,8),
"ValorPPPSimulCom" DECIMAL(26,8),
"PrecioMercadoSimulCom" DECIMAL(26,8),
"ValorMercadoSimulCom" DECIMAL(26,8),
"DiferenciaMercadoSimulCom" DECIMAL(26,8),
"CantidadSimulVen" DECIMAL(26,8),
"PPPSimulVen" DECIMAL(26,8),
"ValorPPPSimulVen" DECIMAL(26,8),
"PrecioMercadoSimulVen" DECIMAL(26,8),
"ValorMercadoSimulVen" DECIMAL(26,8),
"DiferenciaMercadoSimulVen" DECIMAL(26,8),
"CantidadPrestamo" DECIMAL(26,8),
"PPPPrestamo" DECIMAL(26,8),
"ValorPPPPrestamo" DECIMAL(26,8),
"PrecioMercadoPrestamo" DECIMAL(26,8),
"ValorMercadoPrestamo" DECIMAL(26,8),
"DiferenciaMercadoPrestamo" DECIMAL(26,8),
"CantidadEmpresti" DECIMAL(26,8),
"PPPEmpresti" DECIMAL(26,8),
"ValorPPPEmpresti" DECIMAL(26,8),
"PrecioMercadoEmpresti" DECIMAL(26,8),
"ValorMercadoEmpresti" DECIMAL(26,8),
"DiferenciaMercadoEmpresti" DECIMAL(26,8),
"CantidadGarantia" DECIMAL(26,8),
"PPPGarantia" DECIMAL(26,8),
"ValorPPPGarantia" DECIMAL(26,8),
"PrecioMercadoGarantia" DECIMAL(26,8),
"ValorMercadoGarantia" DECIMAL(26,8),
"DiferenciaMercadoGarantia" DECIMAL(26,8),
"CantidadTransIng" DECIMAL(26,8),
"CantidadTransEgr" DECIMAL(26,8),
"TotalPosicion" DECIMAL(26,8),
"TotalPosicionPresunta" DECIMAL(26,8),
"CantidadPactosV" DECIMAL(26,8),
"PPPPactosV" DECIMAL(26,8),
"ValorPPPPactosV" DECIMAL(26,8),
"PrecioMercadoPactosV" DECIMAL(26,8),
"ValorMercadoPactosV" DECIMAL(26,8),
"DiferenciaMercadoPactosV" DECIMAL(26,8),
"CantidadPactosC" DECIMAL(26,8),
"PPPPactosC" DECIMAL(26,8),
"ValorPPPPactosC" DOUBLE,
"PrecioMercadoPactosC" DECIMAL(26,8),
"ValorMercadoPactosC" DECIMAL(26,8),
"DiferenciaMercadoPactosC" DECIMAL(26,8),
"IdMatrizAgrupador" BIGINT,
"EsADC" NVARCHAR(1),
"FechaFuentePrecioMercadoCustodia" NVARCHAR(10));

drop table BSCL."BSCL.Custodia.BD::HIS.CustodiaFechaOperacion";
CREATE COLUMN TABLE BSCL."BSCL.Custodia.BD::HIS.CustodiaFechaOperacion"(
"IdEmpresa" BIGINT,
"TipoFechaConsulta" NVARCHAR(3),
"FechaCustodia" CHAR(10),
"IdClasificacionTipoInstrumento" NVARCHAR(3),
"NombreClasificacionTipoInstrumento" NVARCHAR(100),
"IdTipoInstrumento" NVARCHAR(3),
"NombreTipoInstrumento" NVARCHAR(100),
"ISIN" NVARCHAR(40),
"IdInstrumento" BIGINT,
"Nemotecnico" NVARCHAR(40),
"IdEmisor" NVARCHAR(40),
"EsFungible" NVARCHAR(1),
"IdCliente" BIGINT,
"IdPortafolio" BIGINT,
"DocumentoIdentidad" NVARCHAR(40),
"NombreCliente" NVARCHAR(302),
"CodigoPortafolio" NVARCHAR(100),
"IdBoveda" NVARCHAR(3),
"NumeroCuentaBoveda" NVARCHAR(40),
"Relacion" NVARCHAR(100),
"DescripcionRelacion" NVARCHAR(100),
"CantidadCustodia" DECIMAL(26,8),
"PPPCustodia" DECIMAL(26,8),
"ValorPPPCustodia" DECIMAL(26,8),
"MontoPPPCustodia" DECIMAL(26,8),
"PrecioMercadoCustodia" DECIMAL(26,8),
"Rentabilidad" DECIMAL(26,8),
"ValorMercadoCustodia" DECIMAL(26,8),
"MontoMercadoCustodia" DECIMAL(26,8),
"DiferenciaMercadoCustodia" DECIMAL(26,8),
"IdMonedaReajuste" NVARCHAR(3),
"EsPagaderoEn" NVARCHAR(3),
"CantidadSimulCom" DECIMAL(26,8),
"PPPSimulCom" DECIMAL(26,8),
"ValorPPPSimulCom" DECIMAL(26,8),
"PrecioMercadoSimulCom" DECIMAL(26,8),
"ValorMercadoSimulCom" DECIMAL(26,8),
"DiferenciaMercadoSimulCom" DECIMAL(26,8),
"CantidadSimulVen" DECIMAL(26,8),
"PPPSimulVen" DECIMAL(26,8),
"ValorPPPSimulVen" DECIMAL(26,8),
"PrecioMercadoSimulVen" DECIMAL(26,8),
"ValorMercadoSimulVen" DECIMAL(26,8),
"DiferenciaMercadoSimulVen" DECIMAL(26,8),
"CantidadPrestamo" DECIMAL(26,8),
"PPPPrestamo" DECIMAL(26,8),
"ValorPPPPrestamo" DECIMAL(26,8),
"PrecioMercadoPrestamo" DECIMAL(26,8),
"ValorMercadoPrestamo" DECIMAL(26,8),
"DiferenciaMercadoPrestamo" DECIMAL(26,8),
"CantidadEmpresti" DECIMAL(26,8),
"PPPEmpresti" DECIMAL(26,8),
"ValorPPPEmpresti" DECIMAL(26,8),
"PrecioMercadoEmpresti" DECIMAL(26,8),
"ValorMercadoEmpresti" DECIMAL(26,8),
"DiferenciaMercadoEmpresti" DECIMAL(26,8),
"CantidadGarantia" DECIMAL(26,8),
"PPPGarantia" DECIMAL(26,8),
"ValorPPPGarantia" DECIMAL(26,8),
"PrecioMercadoGarantia" DECIMAL(26,8),
"ValorMercadoGarantia" DECIMAL(26,8),
"DiferenciaMercadoGarantia" DECIMAL(26,8),
"CantidadTransIng" DECIMAL(26,8),
"CantidadTransEgr" DECIMAL(26,8),
"TotalPosicion" DECIMAL(26,8),
"TotalPosicionPresunta" DECIMAL(26,8),
"CantidadPactosV" DECIMAL(26,8),
"PPPPactosV" DECIMAL(26,8),
"ValorPPPPactosV" DECIMAL(26,8),
"PrecioMercadoPactosV" DECIMAL(26,8),
"ValorMercadoPactosV" DECIMAL(26,8),
"DiferenciaMercadoPactosV" DECIMAL(26,8),
"CantidadPactosC" DECIMAL(26,8),
"PPPPactosC" DECIMAL(26,8),
"ValorPPPPactosC" DOUBLE,
"PrecioMercadoPactosC" DECIMAL(26,8),
"ValorMercadoPactosC" DECIMAL(26,8),
"DiferenciaMercadoPactosC" DECIMAL(26,8),
"IdMatrizAgrupador" BIGINT,
"EsADC" NVARCHAR(1),
"FechaFuentePrecioMercadoCustodia" NVARCHAR(10));
