SET SCHEMA "BSCL";
	
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::ConsultarCustodiaPortafolioAnalitica";

CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::ConsultarCustodiaPortafolioAnalitica" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa                		BIGINT,
    IN p_FechaConsulta 					NVARCHAR(10)	DEFAULT '0001-01-01',
	IN p_TipoFechaConsulta				NVARCHAR(3)		DEFAULT 'LIQ', -- Consultar por LIQ: Fecha Liquidación  ADQ: Fecha Adquisicion
	
	OUT p_out_tbl   table(
					"IdEmpresa" 								BIGINT,
					"TipoFechaConsulta" 						NVARCHAR(3),
					"FechaCustodia" 							NVARCHAR(10),
					"IdClasificacionTipoInstrumento" 			NVARCHAR(3),
					"NombreClasificacionTipoInstrumento"		NVARCHAR(100),
					"IdTipoInstrumento" 						NVARCHAR(3),
					"NombreTipoInstrumento" 					NVARCHAR(100),
					"ISIN" 										NVARCHAR(40),
					"IdInstrumento" 							BIGINT,
					"Nemotecnico" 								NVARCHAR(40),
					"IdEmisor" 									NVARCHAR(40),
					"EsFungible" 								NVARCHAR(1),
					"IdCliente"									BIGINT,
					"IdPortafolio" 								BIGINT,
					"DocumentoIdentidad" 						NVARCHAR(40),
					"NombreCliente" 							NVARCHAR(500),
					"CodigoPortafolio" 							NVARCHAR(100),
					"IdBoveda" 									NVARCHAR(3),
					"NumeroCuentaBoveda"						NVARCHAR(40),
					"Relacion" 									NVARCHAR(20),
					"DescripcionRelacion" 						NVARCHAR(100),
					"CantidadCustodia" 							DECIMAL(28,8),
					"PPPCustodia" 								DECIMAL(26,8),
					"ValorPPPCustodia" 							DECIMAL(26,8),
					"MontoPPPCustodia" 							DECIMAL(26,8),
					"PrecioMercadoCustodia" 					DECIMAL(26,8),
					"Rentabilidad" 								DECIMAL(26,8),
					"ValorMercadoCustodia" 						DECIMAL(26,8),
					"MontoMercadoCustodia" 						DECIMAL(26,8),
					"DiferenciaMercadoCustodia" 				DECIMAL(26,8),
					"IdMonedaReajuste" 							NVARCHAR(3),
					"EsPagaderoEn" 								NVARCHAR(3),
					"CantidadSimulCom" 							DECIMAL(26,8),
					"PPPSimulCom" 								DECIMAL(26,8),
					"ValorPPPSimulCom" 							DECIMAL(26,8),
					"PrecioMercadoSimulCom" 					DECIMAL(26,8),
					"ValorMercadoSimulCom" 						DECIMAL(26,8),
					"DiferenciaMercadoSimulCom" 				DECIMAL(26,8),
					"CantidadSimulVen" 							DECIMAL(26,8),
					"PPPSimulVen" 								DECIMAL(26,8),
					"ValorPPPSimulVen" 							DECIMAL(26,8),
					"PrecioMercadoSimulVen" 					DECIMAL(26,8),
					"ValorMercadoSimulVen" 						DECIMAL(26,8),
					"DiferenciaMercadoSimulVen" 				DECIMAL(26,8),
					"CantidadPrestamo"							DECIMAL(26,8),
					"PPPPrestamo" 								DECIMAL(26,8),
					"ValorPPPPrestamo" 							DECIMAL(26,8),
					"PrecioMercadoPrestamo" 					DECIMAL(26,8),
					"ValorMercadoPrestamo" 						DECIMAL(26,8),
					"DiferenciaMercadoPrestamo" 				DECIMAL(26,8),
					"CantidadEmpresti" 							DECIMAL(26,8),
					"PPPEmpresti" 								DECIMAL(26,8),
					"ValorPPPEmpresti" 							DECIMAL(26,8),
					"PrecioMercadoEmpresti" 					DECIMAL(26,8),
					"ValorMercadoEmpresti" 						DECIMAL(26,8),
					"DiferenciaMercadoEmpresti" 				DECIMAL(26,8),
					"CantidadGarantia" 							DECIMAL(26,8),
					"PPPGarantia" 								DECIMAL(26,8),
					"ValorPPPGarantia" 							DECIMAL(26,8),
					"PrecioMercadoGarantia" 					DECIMAL(26,8),
					"ValorMercadoGarantia" 						DECIMAL(26,8),
					"DiferenciaMercadoGarantia" 				DECIMAL(26,8),
					"CantidadPactosV" 							DECIMAL(26,8),
					"PPPPactosV" 								DECIMAL(26,8),
					"ValorPPPPactosV" 							DECIMAL(26,8),
					"PrecioMercadoPactosV" 						DECIMAL(26,8),
					"ValorMercadoPactosV" 						DECIMAL(26,8),
					"DiferenciaMercadoPactosV" 					DECIMAL(26,8),
					"CantidadPactosC" 							DECIMAL(26,8),
					"PPPPactosC" 								DECIMAL(26,8),
					"ValorPPPPactosC" 							DECIMAL(26,8),
					"PrecioMercadoPactosC" 						DECIMAL(26,8),
					"ValorMercadoPactosC" 						DECIMAL(26,8),
					"DiferenciaMercadoPactosC" 					DECIMAL(26,8),
					"CantidadTransIng" 							DECIMAL(26,8),
					"CantidadTransEgr" 							DECIMAL(26,8),
					"TotalPosicion" 							DECIMAL(26,8),
					"TotalPosicionPresunta" 					DECIMAL(26,8),
					"IdMatrizAgrupador"							BIGINT,
					"EsADC"										NVARCHAR(1),
					"FechaFuentePrecioMercadoCustodia"			NVARCHAR(10),
					"IdRegimenFondoMutuo"						NVARCHAR(40)
	)	 DEFAULT empty
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
READS SQL DATA WITH RESULT VIEW "BSCL.Custodia.BD.Vistas::ConsultarCustodiaPortafolioAnalitica" AS 
BEGIN
	
	DECLARE v_IdPersonaCuentaPropia             BIGINT;
	DECLARE v_IdClienteCuentaPropia             BIGINT;
	DECLARE v_IdPortafolio						BIGINT := 0;
	DECLARE v_FechaConsulta 					NVARCHAR(10) := '2300-12-31';
	DECLARE v_IdBovedaDefecto 					NVARCHAR(3) = 'DCV';
	DECLARE v_IdMonedaSistema 					NVARCHAR(3) = '';
	
	DECLARE v_tblInst TABLE (
   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		 ,"NombreClasificacionTipoInstrumento" 	NVARCHAR(100)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
  		,"NombreTipoInstrumento" 				NVARCHAR(100)
  		,"ISIN"									NVARCHAR(40)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40) 
   		,"IdEmisor" 							NVARCHAR(40)
   		,"EsFungible" 							NVARCHAR(1)
   		,"IdCliente" 							BIGINT
   		,"IdPortafolio" 						BIGINT
   		,"CodigoPortafolio"						NVARCHAR(100)
   		,"EsADC"		 						NVARCHAR(1)
   		,"IdMatrizAgrupador"					BIGINT
   		,"NombreCliente" 						NVARCHAR(302)
   		,"DocumentoIdentidad"					NVARCHAR(40)
   		,"IdBoveda" 							NVARCHAR(3)
   		,"IdCuentaCustodioPortafolio"			BIGINT
   		,"NumeroCuentaBoveda"					NVARCHAR(40)		
   		,"Relacion" 							NVARCHAR(4)
   		,"Tamano"								BIGINT
   		,"PrecioBase"							DECIMAL(26,8)
   		,"PrecioMercadoCustodia"				DECIMAL(26,8)
   		,"FechaFuentePrecioMercadoCustodia"		DATE
	);
	
	DECLARE v_tblCustodia TABLE (
   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"IdEmisor" 							NVARCHAR(40)
   		,"EsFungible" 							NVARCHAR(1)
   		,"IdCliente" 							BIGINT
   		,"IdPortafolio" 						BIGINT
   		,"IdBoveda" 							NVARCHAR(3)
   		,"IdCuentaCustodioPortafolio"			BIGINT
   		,"NumeroCuentaBoveda"					NVARCHAR(40)
   		,"Cantidad" 							DECIMAL(26,8)   		
   		,"ValorCompra"							DECIMAL(26,8) --RV.
   		,"MontoCompra"							DECIMAL(26,8) --RV.      		
   		,"INGPrecioMercadoActual"				DECIMAL(26,8) --RV. 
		,"INGTasaMercadoActual"					DECIMAL(26,8) --RF,IF
   		,"ValorMercado"							DECIMAL(26,8) --suma INGMontoMercadoActualMonedaInstrumento
   		,"MontoMercado"							DECIMAL(26,8) --suma INGMontoATasaMercadoActual
   		,"ValorPPP"								DECIMAL(26,8) --RV,FM: suma INGMontoPrecioPromedioMonedaInstrumento, IF,RF:suma INGMontoCompraActualMonedaInstrumento
   		,"IdMonedaReajuste"						NVARCHAR(3) 
		,"IdMonedaEmision"						NVARCHAR(3)
		,"IdRegimenFondoMutuo"					NVARCHAR(40)
	)DEFAULT SELECT
	 	'' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "IdEmisor", '' as "EsFungible", 
	 	0 as "IdCliente", 0 as "IdPortafolio",'' as "IdBoveda",0 as "IdCuentaCustodioPortafolio",'' AS "NumeroCuentaBoveda", 0 as "Cantidad",
	 	0 AS "ValorCompra",0 AS "MontoCompra",0 as "INGPrecioMercadoActual",0 as "INGTasaMercadoActual",0 as "ValorMercado",0 as "MontoMercado",
	 	0 AS "ValorPPP", '' as "IdMonedaReajuste",'' as  "IdMonedaEmision", '' AS "IdRegimenFondoMutuo"	
	FROM DUMMY WHERE 1=2;
	
	DECLARE v_tblVentaPacto TABLE (
   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"IdEmisor" 							NVARCHAR(40)
   		,"EsFungible" 							NVARCHAR(1)
   		,"IdCliente" 							BIGINT
   		,"IdPortafolio" 						BIGINT
   		,"IdBoveda" 							NVARCHAR(3)
   		,"IdCuentaCustodioPortafolio"			BIGINT
   		,"NumeroCuentaBoveda"					NVARCHAR(40)
   		,"Cantidad" 							DECIMAL(26,8)   		
   		,"ValorCompra"							DECIMAL(26,8) --RV.
   		,"MontoCompra"							DECIMAL(26,8) --RV.      		
   		,"INGPrecioMercadoActual"				DECIMAL(26,8) --RV. 
		,"INGTasaMercadoActual"					DECIMAL(26,8) --RF,IF
   		,"ValorMercado"							DECIMAL(26,8) --suma INGMontoMercadoActualMonedaInstrumento
   		,"MontoMercado"							DECIMAL(26,8) --suma INGMontoATasaMercadoActual
   		,"ValorPPP"								DECIMAL(26,8) --RV,FM: suma INGMontoPrecioPromedioMonedaInstrumento, IF,RF:suma INGMontoCompraActualMonedaInstrumento
   		,"IdMonedaReajuste"						NVARCHAR(3) 
		,"IdMonedaEmision"						NVARCHAR(3)
	)DEFAULT SELECT
	 	'' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "IdEmisor", '' as "EsFungible", 
	 	0 as "IdCliente", 0 as "IdPortafolio",'' as "IdBoveda",0 as "IdCuentaCustodioPortafolio",'' AS "NumeroCuentaBoveda", 0 as "Cantidad",
	 	0 AS "ValorCompra",0 AS "MontoCompra",0 as "INGPrecioMercadoActual",0 as "INGTasaMercadoActual",0 as "ValorMercado",0 as "MontoMercado",
	 	0 AS "ValorPPP", '' as "IdMonedaReajuste",'' as  "IdMonedaEmision"
	FROM DUMMY WHERE 1=2;
	
	DECLARE v_tblCompraPacto TABLE (
   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"IdEmisor" 							NVARCHAR(40)
   		,"EsFungible" 							NVARCHAR(1)
   		,"IdCliente" 							BIGINT
   		,"IdPortafolio" 						BIGINT
   		,"IdBoveda" 							NVARCHAR(3)
   		,"IdCuentaCustodioPortafolio"			BIGINT
   		,"NumeroCuentaBoveda"					NVARCHAR(40)
   		,"Cantidad" 							DECIMAL(26,8)   		
   		,"ValorCompra"							DECIMAL(26,8) --RV.
   		,"MontoCompra"							DECIMAL(26,8) --RV.      		
   		,"INGPrecioMercadoActual"				DECIMAL(26,8) --RV. 
		,"INGTasaMercadoActual"					DECIMAL(26,8) --RF,IF
   		,"ValorMercado"							DECIMAL(26,8) --suma INGMontoMercadoActualMonedaInstrumento
   		,"MontoMercado"							DECIMAL(26,8) --suma INGMontoATasaMercadoActual
   		,"ValorPPP"								DECIMAL(26,8) --RV,FM: suma INGMontoPrecioPromedioMonedaInstrumento, IF,RF:suma INGMontoCompraActualMonedaInstrumento
   		,"IdMonedaReajuste"						NVARCHAR(3) 
		,"IdMonedaEmision"						NVARCHAR(3)
	)DEFAULT SELECT
	 	'' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "IdEmisor", '' as "EsFungible", 
	 	0 as "IdCliente", 0 as "IdPortafolio",'' as "IdBoveda",0 as "IdCuentaCustodioPortafolio",'' AS "NumeroCuentaBoveda", 0 as "Cantidad",
	 	0 AS "ValorCompra",0 AS "MontoCompra",0 as "INGPrecioMercadoActual",0 as "INGTasaMercadoActual",0 as "ValorMercado",0 as "MontoMercado",
	 	0 AS "ValorPPP", '' as "IdMonedaReajuste",'' as  "IdMonedaEmision"
	FROM DUMMY WHERE 1=2;
	
	DECLARE v_tblSimulC TABLE (
   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"IdEmisor" 							NVARCHAR(40)
   		,"EsFungible" 							NVARCHAR(1)
   		,"IdCliente" 							BIGINT
   		,"IdPortafolio" 						BIGINT
   		,"IdBoveda" 							NVARCHAR(3)
   		,"IdCuentaCustodioPortafolio"			BIGINT
   		,"NumeroCuentaBoveda"					NVARCHAR(40)
   		,"Cantidad" 							DECIMAL(26,8)   		
   		,"ValorCompra"							DECIMAL(26,8) --RV.
   		,"MontoCompra"							DECIMAL(26,8) --RV.      		
   		,"INGPrecioMercadoActual"				DECIMAL(26,8) --RV. 
		,"INGTasaMercadoActual"					DECIMAL(26,8) --RF,IF
   		,"ValorMercado"							DECIMAL(26,8) --suma INGMontoMercadoActualMonedaInstrumento
   		,"MontoMercado"							DECIMAL(26,8) --suma INGMontoATasaMercadoActual
   		,"ValorPPP"								DECIMAL(26,8) --RV,FM: suma INGMontoPrecioPromedioMonedaInstrumento, IF,RF:suma INGMontoCompraActualMonedaInstrumento
   		,"IdMonedaReajuste"				NVARCHAR(3) 
		,"IdMonedaEmision"							NVARCHAR(3)
	)DEFAULT SELECT
	 	'' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "IdEmisor", '' as "EsFungible", 
	 	0 as "IdCliente", 0 as "IdPortafolio",'' as "IdBoveda",0 as "IdCuentaCustodioPortafolio",'' AS "NumeroCuentaBoveda", 0 as "Cantidad",
	 	0 AS "ValorCompra",0 AS "MontoCompra",0 as "INGPrecioMercadoActual",0 as "INGTasaMercadoActual",0 as "ValorMercado",0 as "MontoMercado",
	 	0 AS "ValorPPP", '' as "IdMonedaReajuste",'' as  "IdMonedaEmision"
	FROM DUMMY WHERE 1=2;
	
	DECLARE v_tblSimulV TABLE (
   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"IdEmisor" 							NVARCHAR(40)
   		,"EsFungible" 							NVARCHAR(1)
   		,"IdCliente" 							BIGINT
   		,"IdPortafolio" 						BIGINT
   		,"IdBoveda" 							NVARCHAR(3)
   		,"IdCuentaCustodioPortafolio"			BIGINT
   		,"NumeroCuentaBoveda"					NVARCHAR(40)
   		,"Cantidad" 							DECIMAL(26,8)   		
   		,"ValorCompra"							DECIMAL(26,8) --RV.
   		,"MontoCompra"							DECIMAL(26,8) --RV.      		
   		,"INGPrecioMercadoActual"				DECIMAL(26,8) --RV. 
		,"INGTasaMercadoActual"					DECIMAL(26,8) --RF,IF
   		,"ValorMercado"							DECIMAL(26,8) --suma INGMontoMercadoActualMonedaInstrumento
   		,"MontoMercado"							DECIMAL(26,8) --suma INGMontoATasaMercadoActual
   		,"ValorPPP"								DECIMAL(26,8) --RV,FM: suma INGMontoPrecioPromedioMonedaInstrumento, IF,RF:suma INGMontoCompraActualMonedaInstrumento
   		,"IdMonedaReajuste"				NVARCHAR(3) 
		,"IdMonedaEmision"							NVARCHAR(3)
	)DEFAULT SELECT
	 	'' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "IdEmisor", '' as "EsFungible", 
	 	0 as "IdCliente", 0 as "IdPortafolio",'' as "IdBoveda",0 as "IdCuentaCustodioPortafolio",'' AS "NumeroCuentaBoveda", 0 as "Cantidad",
	 	0 AS "ValorCompra",0 AS "MontoCompra",0 as "INGPrecioMercadoActual",0 as "INGTasaMercadoActual",0 as "ValorMercado",0 as "MontoMercado",
	 	0 AS "ValorPPP", '' as "IdMonedaReajuste",'' as  "IdMonedaEmision"
	FROM DUMMY WHERE 1=2;
	
	DECLARE v_tblPrestamos TABLE (
   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"IdEmisor" 							NVARCHAR(40)
   		,"EsFungible" 							NVARCHAR(1)
   		,"IdCliente" 							BIGINT
   		,"IdPortafolio" 						BIGINT
   		,"IdBoveda" 							NVARCHAR(3)
   		,"IdCuentaCustodioPortafolio"			BIGINT
   		,"NumeroCuentaBoveda"					NVARCHAR(40)
   		,"Cantidad" 							DECIMAL(26,8)   		
   		,"ValorCompra"							DECIMAL(26,8) --RV.
   		,"MontoCompra"							DECIMAL(26,8) --RV.      		
   		,"INGPrecioMercadoActual"				DECIMAL(26,8) --RV. 
		,"INGTasaMercadoActual"					DECIMAL(26,8) --RF,IF
   		,"ValorMercado"							DECIMAL(26,8) --suma INGMontoMercadoActualMonedaInstrumento
   		,"MontoMercado"							DECIMAL(26,8) --suma INGMontoATasaMercadoActual
   		,"ValorPPP"								DECIMAL(26,8) --RV,FM: suma INGMontoPrecioPromedioMonedaInstrumento, IF,RF:suma INGMontoCompraActualMonedaInstrumento
   		,"IdMonedaReajuste"				NVARCHAR(3) 
		,"IdMonedaEmision"							NVARCHAR(3)
	)DEFAULT SELECT
	 	'' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "IdEmisor", '' as "EsFungible", 
	 	0 as "IdCliente", 0 as "IdPortafolio",'' as "IdBoveda",0 as "IdCuentaCustodioPortafolio",'' AS "NumeroCuentaBoveda", 0 as "Cantidad",
	 	0 AS "ValorCompra",0 AS "MontoCompra",0 as "INGPrecioMercadoActual",0 as "INGTasaMercadoActual",0 as "ValorMercado",0 as "MontoMercado",
	 	0 AS "ValorPPP", '' as "IdMonedaReajuste",'' as  "IdMonedaEmision"
	FROM DUMMY WHERE 1=2;
	
	DECLARE v_tblEmprestitos TABLE (
   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"IdEmisor" 							NVARCHAR(40)
   		,"EsFungible" 							NVARCHAR(1)
   		,"IdCliente" 							BIGINT
   		,"IdPortafolio" 						BIGINT
   		,"IdBoveda" 							NVARCHAR(3)
   		,"IdCuentaCustodioPortafolio"			BIGINT
   		,"NumeroCuentaBoveda"					NVARCHAR(40)
   		,"Cantidad" 							DECIMAL(26,8)   		
   		,"ValorCompra"							DECIMAL(26,8) --RV.
   		,"MontoCompra"							DECIMAL(26,8) --RV.      		
   		,"INGPrecioMercadoActual"				DECIMAL(26,8) --RV. 
		,"INGTasaMercadoActual"					DECIMAL(26,8) --RF,IF
   		,"ValorMercado"							DECIMAL(26,8) --suma INGMontoMercadoActualMonedaInstrumento
   		,"MontoMercado"							DECIMAL(26,8) --suma INGMontoATasaMercadoActual
   		,"ValorPPP"								DECIMAL(26,8) --RV,FM: suma INGMontoPrecioPromedioMonedaInstrumento, IF,RF:suma INGMontoCompraActualMonedaInstrumento
   		,"IdMonedaReajuste"				NVARCHAR(3) 
		,"IdMonedaEmision"							NVARCHAR(3)
	)DEFAULT SELECT
	 	'' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "IdEmisor", '' as "EsFungible", 
	 	0 as "IdCliente", 0 as "IdPortafolio",'' as "IdBoveda",0 as "IdCuentaCustodioPortafolio",'' AS "NumeroCuentaBoveda", 0 as "Cantidad",
	 	0 AS "ValorCompra",0 AS "MontoCompra",0 as "INGPrecioMercadoActual",0 as "INGTasaMercadoActual",0 as "ValorMercado",0 as "MontoMercado",
	 	0 AS "ValorPPP", '' as "IdMonedaReajuste",'' as  "IdMonedaEmision"
	FROM DUMMY WHERE 1=2;
	
	DECLARE v_tblGarantias TABLE (
   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"IdEmisor" 							NVARCHAR(40)
   		,"EsFungible" 							NVARCHAR(1)
   		,"IdCliente" 							BIGINT
   		,"IdPortafolio" 						BIGINT
   		,"IdBoveda" 							NVARCHAR(3)
   		,"IdCuentaCustodioPortafolio"			BIGINT
   		,"NumeroCuentaBoveda"					NVARCHAR(40)
   		,"Cantidad" 							DECIMAL(26,8)   		
   		,"ValorCompra"							DECIMAL(26,8) --RV.
   		,"MontoCompra"							DECIMAL(26,8) --RV.      		
   		,"INGPrecioMercadoActual"				DECIMAL(26,8) --RV. 
		,"INGTasaMercadoActual"					DECIMAL(26,8) --RF,IF
   		,"ValorMercado"							DECIMAL(26,8) --suma INGMontoMercadoActualMonedaInstrumento
   		,"MontoMercado"							DECIMAL(26,8) --suma INGMontoATasaMercadoActual
   		,"ValorPPP"								DECIMAL(26,8) --RV,FM: suma INGMontoPrecioPromedioMonedaInstrumento, IF,RF:suma INGMontoCompraActualMonedaInstrumento
   		,"IdMonedaReajuste"				NVARCHAR(3) 
		,"IdMonedaEmision"							NVARCHAR(3)
	)DEFAULT SELECT
	 	'' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "IdEmisor", '' as "EsFungible", 
	 	0 as "IdCliente", 0 as "IdPortafolio",'' as "IdBoveda",0 as "IdCuentaCustodioPortafolio",'' AS "NumeroCuentaBoveda", 0 as "Cantidad",
	 	0 AS "ValorCompra",0 AS "MontoCompra",0 as "INGPrecioMercadoActual",0 as "INGTasaMercadoActual",0 as "ValorMercado",0 as "MontoMercado",
	 	0 AS "ValorPPP", '' as "IdMonedaReajuste",'' as  "IdMonedaEmision"
	FROM DUMMY WHERE 1=2;
	
	DECLARE v_tblTrasnIng TABLE (
   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"IdEmisor" 							NVARCHAR(40)
   		,"EsFungible" 							NVARCHAR(1)
   		,"IdCliente" 							BIGINT
   		,"IdPortafolio" 						BIGINT
   		,"IdBoveda" 							NVARCHAR(3)
   		,"IdCuentaCustodioPortafolio"			BIGINT
   		,"NumeroCuentaBoveda"					NVARCHAR(40)
   		,"CantidadCustodia"						DECIMAL(26,8)   	
   		,"CantidadNominal"						DECIMAL(26,8)   		
   		,"ValorCompra"							DECIMAL(26,8) --RV.
   		,"MontoCompra"							DECIMAL(26,8) --RV.      		
   		,"INGPrecioMercadoActual"				DECIMAL(26,8) --RV. 
		,"INGTasaMercadoActual"					DECIMAL(26,8) --RF,IF
   		,"ValorMercado"							DECIMAL(26,8) --suma INGMontoMercadoActualMonedaInstrumento
   		,"MontoMercado"							DECIMAL(26,8) --suma INGMontoATasaMercadoActual
   		,"ValorPPP"								DECIMAL(26,8) --RV,FM: suma INGMontoPrecioPromedioMonedaInstrumento, IF,RF:suma INGMontoCompraActualMonedaInstrumento
   		,"IdMonedaReajuste"				NVARCHAR(3) 
		,"IdMonedaEmision"							NVARCHAR(3)
	)DEFAULT SELECT
	 	'' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "IdEmisor", '' as "EsFungible", 
	 	0 as "IdCliente", 0 as "IdPortafolio",'' as "IdBoveda",0 as "IdCuentaCustodioPortafolio",'' AS "NumeroCuentaBoveda", 0 as "CantidadCustodia",0 as "CantidadNominal",
	 	0 AS "ValorCompra",0 AS "MontoCompra",0 as "INGPrecioMercadoActual",0 as "INGTasaMercadoActual",0 as "ValorMercado",0 as "MontoMercado",
	 	0 AS "ValorPPP",'' as "IdMonedaReajuste",'' as  "IdMonedaEmision"
	FROM DUMMY WHERE 1=2;
	 
	DECLARE v_tblTrasnEgr TABLE (   		 
		"IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"IdEmisor" 							NVARCHAR(40)
   		,"EsFungible" 							NVARCHAR(1)
   		,"IdCliente" 							BIGINT
   		,"IdPortafolio" 						BIGINT
   		,"IdBoveda" 							NVARCHAR(3)
   		,"IdCuentaCustodioPortafolio"			BIGINT
   		,"NumeroCuentaBoveda"					NVARCHAR(40)
   		,"Cantidad" 							DECIMAL(26,8)   		
   		,"ValorCompra"							DECIMAL(26,8) --RV.
   		,"MontoCompra"							DECIMAL(26,8) --RV.      		
   		,"INGPrecioMercadoActual"				DECIMAL(26,8) --RV. 
		,"INGTasaMercadoActual"					DECIMAL(26,8) --RF,IF
   		,"ValorMercado"							DECIMAL(26,8) --suma INGMontoMercadoActualMonedaInstrumento
   		,"MontoMercado"							DECIMAL(26,8) --suma INGMontoATasaMercadoActual
   		,"ValorPPP"								DECIMAL(26,8) --RV,FM: suma INGMontoPrecioPromedioMonedaInstrumento, IF,RF:suma INGMontoCompraActualMonedaInstrumento
   		,"IdMonedaReajuste"				NVARCHAR(3) 
		,"IdMonedaEmision"							NVARCHAR(3)
	)DEFAULT SELECT
	 	'' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "IdEmisor", '' as "EsFungible", 
	 	0 as "IdCliente", 0 as "IdPortafolio",'' as "IdBoveda",0 as "IdCuentaCustodioPortafolio",'' AS "NumeroCuentaBoveda", 0 as "Cantidad",
	 	0 AS "ValorCompra",0 AS "MontoCompra",0 as "INGPrecioMercadoActual",0 as "INGTasaMercadoActual",0 as "ValorMercado",0 as "MontoMercado",
	 	0 AS "ValorPPP",'' as "IdMonedaReajuste",'' as  "IdMonedaEmision"	
	FROM DUMMY WHERE 1=2;
	
	IF p_FechaConsulta = '0001-01-01' THEN
		SELECT top 1 "Valor" INTO v_FechaConsulta FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema' AND "IdEmpresa" = p_IdEmpresa;
	ELSE
		v_FechaConsulta := p_FechaConsulta;
	END IF;
	
	SELECT mda."Valor"
	INTO v_IdMonedaSistema
	FROM "BSCL.Plataforma.BD::PLA.Configuracion" mda 
	WHERE mda."CodigoParametro" = 'PLA.MonedaSistema' AND mda."IdEmpresa" = p_IdEmpresa;
	
	-- Obtiene los datos de la corredora(para cuenta propia)	
	SELECT CLI."IdPersona", CLI."IdCliente" 
	INTO v_IdPersonaCuentaPropia, v_IdClienteCuentaPropia 
	FROM "BSCL.Plataforma.BD::PLA.Empresa" AS EMP
	LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC on SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI on CLI."IdCliente" = SOC."IdCliente"	
	WHERE EMP."IdEmpresa" = :p_IdEmpresa;
	
	--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- 
	-- custodia
	---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	v_tblCustodia =
		SELECT
				CAR."IdClasificacionTipoInstrumento"
			   ,CAR."IdTipoInstrumento"
			   ,CAR."IdInstrumento"
			   ,Case when CAR."IdClasificacionTipoInstrumento" = 'IF' then CAR."NemoSVS" else CAR."Nemotecnico" end as "Nemotecnico"
			   ,CASE WHEN coalesce(CAR."IdEmisor",inst3."IdEmisor",'') <> '' THEN coalesce(CAR."IdEmisor",inst3."IdEmisor",'') ELSE inst3."IdEmisor" END AS "IdEmisor"
			   ,case when CAR."IdClasificacionTipoInstrumento" = 'IF' then CAR."EsFungible" else 'N' end as "EsFungible"
			   ,CAR."IdCliente"
			   ,CAR."IdPortafolio"
			   ,COALESCE(CAR."IdBoveda",:v_IdBovedaDefecto) AS "IdBoveda" 
			   ,COALESCE(CCPI."IdCuentaCustodioPortafolio",0) AS "IdCuentaCustodioPortafolio"
			   ,COALESCE(CCPI."Codigo",'') AS "NumeroCuentaBoveda"
			   ,cast(IFNULL(case when CAR."IdClasificacionTipoInstrumento" = 'IF' then CAR."INGValorRescate" else CAR."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
			   ,CAR."INGMontoCompraActualMonedaInstrumento" as "ValorCompra"		
			   ,CAR."INGMontoATasaCompraActual" as "MontoCompra"					
			   ,CAR."INGPrecioMercadoActual" as "INGPrecioMercadoActual"			
			   ,CAR."INGTasaMercadoActual" as "INGTasaMercadoActual" 			
			   ,CAR."INGMontoMercadoActualMonedaInstrumento" as "ValorMercado"
			   ,CAR."INGMontoATasaMercadoActual" as "MontoMercado"
			   ,0 AS "ValorPPP"
		   	   ,CASE
			   		WHEN COALESCE(CAR."IdTipoReajuste",inst3."IdTipoReajuste", '') <> '' THEN COALESCE(CAR."IdTipoReajuste",inst3."IdTipoReajuste", '')
			   		WHEN ifnull(CAR."IdMonedaPrincipal",'') <> '' THEN CAR."IdMonedaPrincipal"
			   		ELSE COALESCE(CAR."IdMonedaInstrumento",inst3."IdMoneda",'')
				END AS "IdMonedaReajuste"
			   ,CASE
			   		WHEN IFNULL(CAR."IdMonedaEmision",'') <> '' THEN CAR."IdMonedaEmision"
			   		WHEN IFNULL(CAR."IdTipoReajuste",'') <> '' THEN CAR."IdTipoReajuste"
			   		ELSE inst3."IdMoneda"
				END AS "IdMonedaEmision"
			   ,CASE WHEN (CAR."IdClasificacionTipoInstrumento" = 'FM') THEN CAR."IdRegimenFondoMutuo" ELSE '' END AS "IdRegimenFondoMutuo"
			FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
			--LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = CAR."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = CAR."IdInstrumento"
			WHERE 
				CAR."IdEmpresa" = p_IdEmpresa
			AND	CAR."IdPortafolio" = CASE WHEN v_IdPortafolio > 0 THEN v_IdPortafolio ELSE CAR."IdPortafolio" END
			AND	CAR."IdTipoInstrumento" NOT IN ('PRT','EMP','SIM','PAC','REP','RRV','RRF')
			AND
			(
				   (p_TipoFechaConsulta = 'LIQ' AND (   -- Filtra por Fecha Liquidacion
								  v_FechaConsulta BETWEEN CAR."INGFechaLiquidacion" and case when IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') end
								  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
							AND	  v_FechaConsulta < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') end	   
				   ))
				OR (p_TipoFechaConsulta = 'ADQ' AND (   -- Filtra por Fecha Adquisición
								  v_FechaConsulta BETWEEN CAR."INGFechaOperacion" and case when IFNULL(CAR."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(CAR."EGRFechaOperacion",'0001-01-01') end
								  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
							AND	  v_FechaConsulta < case when IFNULL("EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(CAR."EGRFechaOperacion",'0001-01-01') end	
				))
			)
			AND CAR."IdEstado" = 'VIG';
		
		
		
		-- Resumen
		v_tblCustodia =
		SELECT
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,cast(SUM("Cantidad") as DECIMAL(26,8)) as "Cantidad"
		   ,SUM("ValorCompra") as "ValorCompra"
		   ,SUM("MontoCompra") as "MontoCompra"
		   ,sum(coalesce("INGPrecioMercadoActual",0))/count(1) as "INGPrecioMercadoActual"
		   ,max(coalesce("INGTasaMercadoActual",0)) as "INGTasaMercadoActual"
		   ,sum(coalesce("ValorMercado",0)) as "ValorMercado"
		   ,sum(coalesce("MontoMercado",0)) as "MontoMercado"
		   ,SUM("ValorCompra") AS "ValorPPP"
		   ,"IdMonedaReajuste" as "IdMonedaReajuste"
		   ,"IdMonedaEmision"   AS "IdMonedaEmision"
		   ,"IdRegimenFondoMutuo" AS  "IdRegimenFondoMutuo"
		FROM :v_tblCustodia
		GROUP BY
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision"
		   ,"IdRegimenFondoMutuo";
	
	-- fin custodia
	
	
	---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	-- Ventas con Pacto
	---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		v_tblVentaPacto =
		SELECT
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,"Cantidad"
		   ,"ValorCompra"
		   ,"MontoCompra"
		   ,"INGPrecioMercadoActual"
		   ,"INGTasaMercadoActual"
		   ,"ValorMercado"
		   ,"MontoMercado"
		   ,0 AS "ValorPPP"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision"
		FROM
		(
			SELECT
				RANK() over (partition by  PAC."FolioCartera" order by PAC."IdCartera" desc) as RANK
			   ,sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
			   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
			   ,sby."IdInstrumento" as "IdInstrumento"
			   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
			   ,sby."IdEmisor" as "IdEmisor"
			   ,case when sby."IdClasificacionTipoInstrumento"= 'IF' then sby."EsFungible" else 'N' end as "EsFungible"
			   ,PAC."IdCliente" as "IdCliente"
			   ,PAC."IdPortafolio" as "IdPortafolio"
			   ,COALESCE(sby."IdBoveda",:v_IdBovedaDefecto) AS "IdBoveda" 
			   ,COALESCE(CCPI."IdCuentaCustodioPortafolio",0) AS "IdCuentaCustodioPortafolio"
			   ,COALESCE(CCPI."Codigo",'') AS "NumeroCuentaBoveda"			   
			   ,cast(IFNULL(case when sby."IdClasificacionTipoInstrumento" = 'IF'  then sby."INGValorRescate" else sby."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
			   ,sby."INGMontoSubyacenteActualMonedaInstrumento" as "ValorCompra" 	
			   ,sby."INGMontoATasaSubyacenteActual" as "MontoCompra" 				
			   ,sby."INGPrecioMercadoActual" as "INGPrecioMercadoActual" 			
			   ,sby."INGTasaMercadoActual" as "INGTasaMercadoActual" 				
			   ,sby."INGMontoMercadoActualMonedaInstrumento" as "ValorMercado"
			   ,sby."INGMontoATasaMercadoActual" as "MontoMercado"
			   ,CASE
			   		WHEN ifnull(sby."IdMonedaInstrumento",'') <> '' THEN sby."IdMonedaInstrumento"
			   		WHEN ifnull(sby."IdTipoReajusteSubyacente",'') <> '' THEN sby."IdTipoReajusteSubyacente"
			   		ELSE ifnull(sby."IdMonedaSistema",'')
			   	END AS "IdMonedaReajuste"
			   ,CASE
			   		WHEN IFNULL(sby."IdMonedaEmision",'') <> '' THEN sby."IdMonedaEmision"
			   		WHEN IFNULL(sby."IdTipoReajusteSubyacente",'') <> '' THEN sby."IdTipoReajusteSubyacente"
			   		ELSE sby."IdMonedaInstrumento" 
				END AS "IdMonedaEmision"
			FROM "BSCL.Custodia.BD::CUS.Cartera" AS PAC
			INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (PAC."FolioCartera" = ultSub."FolioCarteraRelacionada")
			INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on (sby."IdSubyacente" = ultSub."IdSubyacente")
			--LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdCliente" =  PAC."IdCliente")
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PAC."IdPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = PAC."IdCuentaCustodioPortafolio"
			WHERE 
				PAC."IdEmpresa" = p_IdEmpresa
			AND
			(
				   (p_TipoFechaConsulta = 'LIQ' AND (  -- Filtra por Fecha Liquidacion // 16-05-2017 Para prestamos, se modifica pa q siempre busque sobre FechaOperacion		   
							  v_FechaConsulta BETWEEN PAC."INGFechaLiquidacion" and case when IFNULL(PAC."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PAC."EGRFechaLiquidacion",'0001-01-01') end
							  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
						AND	  v_FechaConsulta < case when IFNULL(PAC."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PAC."EGRFechaLiquidacion",'0001-01-01') end
				   ))
				OR (p_TipoFechaConsulta = 'ADQ' AND ( -- Filtra por Fecha Adquisicion
							  v_FechaConsulta BETWEEN PAC."INGFechaOperacion" and case when IFNULL(PAC."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PAC."EGRFechaOperacion",'0001-01-01') end
							  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
						AND	  v_FechaConsulta < case when IFNULL(PAC."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PAC."EGRFechaOperacion",'0001-01-01') end
				)) 
			)
			AND PAC."IdTipoInstrumento" = 'PAC'
			AND PAC."IdTipoOperacion" = 'COMPAC'
		)
		WHERE RANK = 1;
		
		
		-- Resumen
		v_tblVentaPacto =
		SELECT
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,cast(SUM("Cantidad") as DECIMAL(26,8)) as "Cantidad"
		   ,SUM("ValorCompra") as "ValorCompra"
		   ,SUM("MontoCompra") as "MontoCompra"
		   ,sum(coalesce("INGPrecioMercadoActual",0))/count(1) as "INGPrecioMercadoActual"
		   ,max(coalesce("INGTasaMercadoActual",0)) as "INGTasaMercadoActual"
		   ,sum(coalesce("ValorMercado",0)) as "ValorMercado"
		   ,sum(coalesce("MontoMercado",0)) as "MontoMercado"
		   ,SUM("ValorCompra") AS "ValorPPP"
		   ,"IdMonedaReajuste" as "IdMonedaReajuste"
		   ,"IdMonedaEmision"   AS "IdMonedaEmision"	
		FROM :v_tblVentaPacto
		GROUP BY
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision";
	--Ventas con Pacto
	
	
	---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	-- Compras con Pacto
	---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	
		v_tblCompraPacto =
		SELECT
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,"Cantidad"
		   ,"ValorCompra"
		   ,"MontoCompra"
		   ,"INGPrecioMercadoActual"
		   ,"INGTasaMercadoActual"
		   ,"ValorMercado"
		   ,"MontoMercado"
		   ,0 AS "ValorPPP"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision"
		FROM
		( 
			SELECT
				RANK() over (partition by  PAC."FolioCartera" order by PAC."IdCartera" desc) as RANK
			   ,sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
			   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
			   ,sby."IdInstrumento" as "IdInstrumento"
			   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
			   ,sby."IdEmisor" as "IdEmisor"
			   ,case when sby."IdClasificacionTipoInstrumento"= 'IF' then sby."EsFungible" else 'N' end as "EsFungible"
			   ,PAC."IdCliente" as "IdCliente"
			   ,PAC."IdPortafolio" as "IdPortafolio"			   
			   ,sby."IdBoveda" AS "IdBoveda" 
			   ,COALESCE(CCPI."IdCuentaCustodioPortafolio",0) AS "IdCuentaCustodioPortafolio"
			   ,COALESCE(CCPI."Codigo",'') AS "NumeroCuentaBoveda"
			   ,cast(IFNULL(case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."INGValorRescate" else sby."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
			   ,sby."INGMontoSubyacenteActualMonedaInstrumento" as "ValorCompra"		
			   ,sby."INGMontoATasaSubyacenteActual" as "MontoCompra"					
			   ,sby."INGPrecioMercadoActual" as "INGPrecioMercadoActual" 				
			   ,sby."INGTasaMercadoActual" as "INGTasaMercadoActual" 					
			   ,sby."INGMontoMercadoActualMonedaInstrumento" as "ValorMercado"
			   ,sby."INGMontoATasaMercadoActual" as "MontoMercado"
			   ,CASE
			   		WHEN ifnull(sby."IdMonedaInstrumento",'') <> '' THEN sby."IdMonedaInstrumento"
			   		WHEN ifnull(sby."IdTipoReajusteSubyacente",'') <> '' THEN sby."IdTipoReajusteSubyacente"
			   		ELSE ifnull(sby."IdMonedaSistema",'')
				END AS "IdMonedaReajuste"
			   ,CASE
			   		WHEN IFNULL(sby."IdMonedaEmision",'') <> '' THEN sby."IdMonedaEmision"
			   		WHEN IFNULL(sby."IdTipoReajusteSubyacente",'') <> '' THEN sby."IdTipoReajusteSubyacente"
			   		ELSE sby."IdMonedaInstrumento"
				END AS "IdMonedaEmision"
			FROM "BSCL.Custodia.BD::CUS.Cartera" AS PAC
			INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (PAC."FolioCartera" = ultSub."FolioCarteraRelacionada")
			INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on (sby."IdSubyacente" = ultSub."IdSubyacente")
			--LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = PAC."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PAC."IdPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = PAC."IdCuentaCustodioPortafolio"
			WHERE
				PAC."IdEmpresa" = p_IdEmpresa
			AND
			(
				   (p_TipoFechaConsulta = 'LIQ' AND (  -- Filtra por Fecha Liquidacion // 16-05-2017 Para prestamos, se modifica pa q siempre busque sobre FechaOperacion		   
							  v_FechaConsulta BETWEEN PAC."INGFechaLiquidacion" and case when IFNULL(PAC."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PAC."EGRFechaLiquidacion",'0001-01-01') end
							  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
						AND	  v_FechaConsulta < case when IFNULL(PAC."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PAC."EGRFechaLiquidacion",'0001-01-01') end
				   ))
				OR (p_TipoFechaConsulta = 'ADQ' AND ( -- Filtra por Fecha Adquisicion
							  v_FechaConsulta BETWEEN PAC."INGFechaOperacion" and case when IFNULL(PAC."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PAC."EGRFechaOperacion",'0001-01-01') end
							  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
						AND	  v_FechaConsulta < case when IFNULL(PAC."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PAC."EGRFechaOperacion",'0001-01-01') end
				)) 
			)
			AND PAC."IdTipoInstrumento" = 'PAC'
			AND PAC."IdTipoOperacion" = 'VENPAC'
		)
		WHERE RANK = 1;
		
		
		-- Resumen
		v_tblCompraPacto =
		SELECT
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,cast(SUM("Cantidad") as DECIMAL(26,8)) as "Cantidad"
		   ,SUM("ValorCompra") as "ValorCompra"
		   ,SUM("MontoCompra") as "MontoCompra"
		   ,sum(coalesce("INGPrecioMercadoActual",0))/count(1) as "INGPrecioMercadoActual"
		   ,max(coalesce("INGTasaMercadoActual",0)) as "INGTasaMercadoActual"
		   ,sum(coalesce("ValorMercado",0)) as "ValorMercado"
		   ,sum(coalesce("MontoMercado",0)) as "MontoMercado"
		   ,SUM("ValorCompra") AS "ValorPPP"
		   ,"IdMonedaReajuste" as "IdMonedaReajuste"
		   ,"IdMonedaEmision"   AS "IdMonedaEmision"
		FROM :v_tblCompraPacto
		GROUP BY
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision";
	

	--Compras con Pacto
	
	
	---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	-- Simultaneas de compra
	---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		v_tblSimulC =
		SELECT
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,"Cantidad"
		   ,"ValorCompra"
		   ,"MontoCompra"
		   ,"INGPrecioMercadoActual"
		   ,"INGTasaMercadoActual"
		   ,"ValorMercado"
		   ,"MontoMercado"
		   ,0 AS "ValorPPP"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision"
		FROM
		(
			SELECT
				RANK() over (partition by  SIM."FolioCartera" order by SIM."IdCartera" desc) as RANK
			   ,SIM."suby_IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento", SIM."suby_IdTipoInstrumento" as "IdTipoInstrumento", SIM."suby_IdInstrumento" as "IdInstrumento"
			   ,case when "suby_IdClasificacionTipoInstrumento" = 'IF' then SIM."suby_NemoSVS" else SIM."suby_Nemotecnico" end as "Nemotecnico"
			   ,SIM."suby_IdEmisor" as "IdEmisor"
			   ,case when "suby_IdClasificacionTipoInstrumento" = 'IF' then SIM."suby_EsFungible" else 'N' end as "EsFungible"
			   ,SIM."IdCliente"
			   ,SIM."IdPortafolio"
			   ,SIM."suby_IdBoveda" AS "IdBoveda" 
			   ,COALESCE(CCPI."IdCuentaCustodioPortafolio",0) AS "IdCuentaCustodioPortafolio"
			   ,COALESCE(CCPI."Codigo",'') AS "NumeroCuentaBoveda"			   
			   ,cast(IFNULL(case when "suby_IdClasificacionTipoInstrumento" = 'IF' then SIM."INGValorRescate" else SIM."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
			   ,SIM."suby_INGMontoATasaCompraActualMonedaInstrumento" as "ValorCompra"	
			   ,SIM."suby_INGMontoATasaCompraActual" as "MontoCompra"					
			   ,SIM."suby_INGPrecioMercadoActual" as "INGPrecioMercadoActual"			
			   ,SIM."suby_INGTasaMercadoActual" as "INGTasaMercadoActual"				
			   ,SIM."suby_INGMontoMercadoActualMonedaInstrumento" as "ValorMercado"
			   ,SIM."suby_INGMontoATasaMercadoActual" as "MontoMercado"
			   ,CASE
			   		WHEN ifnull(SIM."suby_IdMonedaInstrumento",'') <> '' THEN SIM."suby_IdMonedaInstrumento"
			   		WHEN ifnull(SIM."suby_IdTipoReajusteSubyacente",'') <> '' THEN SIM."suby_IdTipoReajusteSubyacente"
			   		ELSE ifnull(SIM."IdMonedaSistema",'')
				END AS "IdMonedaReajuste"
			   ,CASE
			   		WHEN IFNULL(SIM."suby_EsPagaderoEn",'') <> '' THEN IFNULL(SIM."suby_EsPagaderoEn",'') 
			   		WHEN ifnull(SIM."suby_IdMonedaInstrumento",'') <> '' THEN SIM."suby_IdMonedaInstrumento"
			   		WHEN ifnull(SIM."suby_IdTipoReajusteSubyacente",'') <> '' THEN SIM."suby_IdTipoReajusteSubyacente"
					ELSE ifnull(SIM."IdMonedaSistema",'')
				END AS "IdMonedaEmision"
			FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" AS SIM
			--LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdCliente" = SIM."IdCliente")
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = SIM."IdPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = SIM."IdCuentaCustodioPortafolio"
			WHERE
				SIM."IdEmpresa" = p_IdEmpresa
			AND	SIM."IdPortafolio" = case when v_IdPortafolio > 0 then v_IdPortafolio else SIM."IdPortafolio" END
			AND	SIM."IdTipoOperacion" = 'VENSIM'  --old: COMSIM
			AND
			(
				   (p_TipoFechaConsulta = 'LIQ' AND ( -- Filtra por Fecha Liquidacion
							  v_FechaConsulta BETWEEN SIM."INGFechaLiquidacion" and case when IFNULL(SIM."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(SIM."EGRFechaLiquidacion",'0001-01-01') end
							  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
						AND	  v_FechaConsulta < case when IFNULL(SIM."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(SIM."EGRFechaLiquidacion",'0001-01-01') end				   
				   ))
				OR (p_TipoFechaConsulta = 'ADQ' AND (-- Filtra por Fecha Adquisicion
							  v_FechaConsulta BETWEEN SIM."INGFechaOperacion" and case when IFNULL(SIM."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(SIM."EGRFechaOperacion",'0001-01-01') end
							  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
						AND	  v_FechaConsulta < case when IFNULL(SIM."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(SIM."EGRFechaOperacion",'0001-01-01') end
				)) 
			)
			AND SIM."IdEstado" NOT IN ('ANU', 'VCD','ANT')
		)
		WHERE RANK = 1;
		
		
		-- Resumen
		v_tblSimulC =
		SELECT
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,cast(SUM("Cantidad") as DECIMAL(26,8)) as "Cantidad"
		   ,SUM("ValorCompra") as "ValorCompra"
		   ,SUM("MontoCompra") as "MontoCompra"
		   ,sum(coalesce("INGPrecioMercadoActual",0))/count(1) as "INGPrecioMercadoActual"
		   ,max(coalesce("INGTasaMercadoActual",0)) as "INGTasaMercadoActual"
		   ,sum(coalesce("ValorMercado",0)) as "ValorMercado"
		   ,sum(coalesce("MontoMercado",0)) as "MontoMercado"
		   ,SUM("ValorCompra") AS "ValorPPP"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision"
		FROM :v_tblSimulC
		GROUP BY
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision";
	
	--Simultaneas de compra
	
	
	---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	-- Simultaneas de venta
	---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		v_tblSimulV =
		SELECT
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,"Cantidad"
		   ,"ValorCompra"
		   ,"MontoCompra"
		   ,"INGPrecioMercadoActual"
		   ,"INGTasaMercadoActual"
		   ,"ValorMercado"
		   ,"MontoMercado"
		   ,0 AS "ValorPPP"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision"
		FROM
		(
			SELECT
				RANK() over (partition by  SIM."FolioCartera" order by SIM."IdCartera" desc) as RANK
			   ,SIM."suby_IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento", SIM."suby_IdTipoInstrumento" as "IdTipoInstrumento", SIM."suby_IdInstrumento" as "IdInstrumento"
			   ,case when "suby_IdClasificacionTipoInstrumento" = 'IF' then "suby_NemoSVS" else SIM."suby_Nemotecnico" end as "Nemotecnico"
			   ,SIM."suby_IdEmisor" as "IdEmisor"
			   ,case when "suby_IdClasificacionTipoInstrumento" = 'IF' then SIM."suby_EsFungible" else 'N' end as "EsFungible"
			   ,SIM."IdCliente"
			   ,SIM."IdPortafolio"
			   ,SIM."suby_IdBoveda" AS "IdBoveda"
			   ,COALESCE(CCPI."IdCuentaCustodioPortafolio",0) AS "IdCuentaCustodioPortafolio"
			   ,COALESCE(CCPI."Codigo",'') AS "NumeroCuentaBoveda"	
			   ,cast(IFNULL(case when "suby_IdClasificacionTipoInstrumento" = 'IF'  then SIM."INGValorRescate" else SIM."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
			   ,SIM."suby_INGMontoATasaCompraActualMonedaInstrumento" as "ValorCompra"	
			   ,SIM."suby_INGMontoATasaCompraActual" as "MontoCompra"					
			   ,SIM."suby_INGPrecioMercadoActual" as "INGPrecioMercadoActual" 			
			   ,SIM."suby_INGTasaMercadoActual" as "INGTasaMercadoActual"	 			
			   ,SIM."suby_INGMontoMercadoActualMonedaInstrumento" as "ValorMercado"
			   ,SIM."suby_INGMontoATasaMercadoActual" as "MontoMercado"
			   ,CASE
			   		WHEN ifnull(SIM."suby_IdMonedaInstrumento",'') <> '' THEN SIM."suby_IdMonedaInstrumento"
			   		WHEN ifnull(SIM."suby_IdTipoReajusteSubyacente",'') <> '' THEN SIM."suby_IdTipoReajusteSubyacente"
					ELSE ifnull(SIM."IdMonedaSistema",'')
				END AS "IdMonedaReajuste"
			   ,CASE
			   		WHEN IFNULL(SIM."suby_EsPagaderoEn",'') <> '' THEN IFNULL(SIM."suby_EsPagaderoEn",'') 
			   		WHEN ifnull(SIM."suby_IdMonedaInstrumento",'') <> '' THEN SIM."suby_IdMonedaInstrumento"
			   		WHEN ifnull(SIM."suby_IdTipoReajusteSubyacente",'') <> '' THEN SIM."suby_IdTipoReajusteSubyacente"
					ELSE ifnull(SIM."IdMonedaSistema",'')
				END AS "IdMonedaEmision"
			FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" AS SIM
			--LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = SIM."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = SIM."IdPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = SIM."IdCuentaCustodioPortafolio"
			WHERE
				SIM."IdEmpresa" = p_IdEmpresa
			AND	SIM."IdPortafolio" = case when v_IdPortafolio > 0 then v_IdPortafolio else SIM."IdPortafolio" END
			AND	SIM."IdTipoOperacion" = 'COMSIM'  -- old: VENSIM
			AND SIM."IdEstado" NOT IN ('ANU', 'VCD','ANT')
			AND
			(
				   (p_TipoFechaConsulta = 'LIQ' AND ( -- Filtra por Fecha Liquidacion
							  v_FechaConsulta BETWEEN SIM."INGFechaLiquidacion" and case when IFNULL(SIM."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(SIM."EGRFechaLiquidacion",'0001-01-01') end
							  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
						AND	  v_FechaConsulta < case when IFNULL(SIM."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(SIM."EGRFechaLiquidacion",'0001-01-01') end				   
				   ))
				OR (p_TipoFechaConsulta = 'ADQ' AND (-- Filtra por Fecha Adquisicion
							  v_FechaConsulta BETWEEN SIM."INGFechaOperacion" and case when IFNULL(SIM."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(SIM."EGRFechaOperacion",'0001-01-01') end
							  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
						AND	  v_FechaConsulta < case when IFNULL(SIM."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(SIM."EGRFechaOperacion",'0001-01-01') end
				)) 
			)
		)
		WHERE RANK = 1;
		
		
		-- Resumen
		v_tblSimulV =
		SELECT
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,cast(SUM("Cantidad") as DECIMAL(26,8)) as "Cantidad"
		   ,SUM("ValorCompra") as "ValorCompra"
		   ,SUM("MontoCompra") as "MontoCompra"
		   ,sum(coalesce("INGPrecioMercadoActual",0))/count(1) as "INGPrecioMercadoActual"
		   ,max(coalesce("INGTasaMercadoActual",0)) as "INGTasaMercadoActual"
		   ,sum(coalesce("ValorMercado",0)) as "ValorMercado"
		   ,sum(coalesce("MontoMercado",0)) as "MontoMercado"
		    ,SUM("ValorCompra") AS "ValorPPP"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision"
		FROM :v_tblSimulV
		GROUP BY
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision";
	
	--Simultaneas de venta
	
	
	---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	-- Prestamos
	---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	
		v_tblPrestamos =
		SELECT
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,"Cantidad"
		   ,"ValorCompra"
		   ,"MontoCompra"
		   ,"INGPrecioMercadoActual"
		   ,"INGTasaMercadoActual"
		   ,"ValorMercado"
		   ,"MontoMercado"
		   ,0 AS "ValorPPP"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision"
		FROM
		(
			SELECT
				RANK() over (partition by  PRE."FolioCartera" order by PRE."IdCartera" desc) as RANK
			   ,sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
			   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
			   ,sby."IdInstrumento" as "IdInstrumento"
			   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
			   ,sby."IdEmisor" as "IdEmisor"
			   ,case when sby."IdClasificacionTipoInstrumento"= 'IF' then sby."EsFungible" else 'N' end as "EsFungible"
			   ,PRE."IdCliente"
			   ,PRE."IdPortafolio"
			   ,COALESCE(sby."IdBoveda",'DCV') AS "IdBoveda"
			   ,COALESCE(CCPI."IdCuentaCustodioPortafolio",0) AS "IdCuentaCustodioPortafolio"
			   ,COALESCE(CCPI."Codigo",'') AS "NumeroCuentaBoveda"
			   ,cast(IFNULL(case when sby."IdClasificacionTipoInstrumento" = 'IF' then PRE."INGValorRescate" else PRE."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
			   ,sby."INGMontoSubyacenteActualMonedaInstrumento" as "ValorCompra" 	
			   ,sby."INGMontoATasaSubyacenteActual" as "MontoCompra"			
			   ,sby."INGPrecioMercadoActual" as "INGPrecioMercadoActual"			
			   ,sby."INGTasaMercadoActual" as "INGTasaMercadoActual"				
			   ,sby."INGMontoMercadoActualMonedaInstrumento" as "ValorMercado"
			   ,sby."INGMontoATasaMercadoActual" as "MontoMercado"
			   ,CASE
			   		WHEN ifnull(sby."IdMonedaInstrumento",'') <> '' THEN sby."IdMonedaInstrumento"
			   		WHEN ifnull(sby."IdTipoReajusteSubyacente",'') <> '' THEN sby."IdTipoReajusteSubyacente"
			   		ELSE ifnull(sby."IdMonedaSistema",'')
				END AS "IdMonedaReajuste"
			   ,CASE
			   		WHEN IFNULL(sby."IdMonedaEmision",'') <> '' THEN sby."IdMonedaEmision"
			   		WHEN IFNULL(sby."IdTipoReajusteSubyacente",'') <> '' THEN sby."IdTipoReajusteSubyacente"
			   		ELSE sby."IdMonedaInstrumento"
				END AS "IdMonedaEmision"
			FROM "BSCL.Custodia.BD::CUS.Cartera" AS PRE
			INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (PRE."FolioCartera" = ultSub."FolioCarteraRelacionada")
			INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on (sby."IdSubyacente" = ultSub."IdSubyacente")
			--LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = PRE."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PRE."IdPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = PRE."IdCuentaCustodioPortafolio"
			WHERE
				PRE."IdEmpresa" = p_IdEmpresa
			AND	PRE."IdPortafolio" = case when v_IdPortafolio > 0 then v_IdPortafolio else PRE."IdPortafolio" END
			AND PRE."IdEstado" NOT IN ('ANU', 'VCD','ANT')
			AND
			(
				   (p_TipoFechaConsulta = 'LIQ' AND (  -- Filtra por Fecha Liquidacion // 16-05-2017 Para prestamos, se modifica pa q siempre busque sobre FechaOperacion		   
							  v_FechaConsulta BETWEEN PRE."INGFechaOperacion" and case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
							  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
						AND	  v_FechaConsulta < case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
				   ))
				OR (p_TipoFechaConsulta = 'ADQ' AND ( -- Filtra por Fecha Adquisicion
							  v_FechaConsulta BETWEEN PRE."INGFechaOperacion" and case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
							  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
						AND	  v_FechaConsulta < case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
				)) 
			)
			AND PRE."IdTipoInstrumento" = 'PRT'
		)
		WHERE RANK = 1;
		
		
		-- Resumen
		v_tblPrestamos = 
		SELECT
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,cast(SUM("Cantidad") as DECIMAL(26,8)) as "Cantidad"
		   ,SUM("ValorCompra") as "ValorCompra"
		   ,SUM("MontoCompra") as "MontoCompra"
		   ,sum(coalesce("INGPrecioMercadoActual",0))/count(1) as "INGPrecioMercadoActual"
		   ,max(coalesce("INGTasaMercadoActual",0)) as "INGTasaMercadoActual"
		   ,sum(coalesce("ValorMercado",0)) as "ValorMercado"
		   ,sum(coalesce("MontoMercado",0)) as "MontoMercado"
		   ,SUM("ValorCompra") AS "ValorPPP"
		   ,"IdMonedaReajuste" as "IdMonedaReajuste"
		   ,"IdMonedaEmision"
		FROM :v_tblPrestamos
		GROUP BY
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision";
	
	--Prestamos
	
	
	---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	-- Emprestitos
	---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	-- Detalle
	
		v_tblEmprestitos =
		SELECT
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,"Cantidad"
		   ,"ValorCompra"
		   ,"MontoCompra"
		   ,"INGPrecioMercadoActual"
		   ,"INGTasaMercadoActual"
		   ,"ValorMercado"
		   ,"MontoMercado"
		   ,0 AS "ValorPPP"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision"
		FROM
		( 
			SELECT
				RANK() over (partition by  EMP."FolioCartera" order by EMP."IdCartera" desc) as RANK
			   ,sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
			   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
			   ,sby."IdInstrumento" as "IdInstrumento"
			   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
			   ,sby."IdEmisor" as "IdEmisor"
			   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."EsFungible" else 'N' end as "EsFungible"
			   ,EMP."IdCliente"
			   ,EMP."IdPortafolio"
			   ,CASE
			   		WHEN ifnull(EMP."IdBoveda",'') <> '' THEN EMP."IdBoveda"
			   		WHEN ifnull(orig."IdBoveda",'') <> '' THEN orig."IdBoveda"
			   		ELSE :v_IdBovedaDefecto
			   	END AS "IdBoveda"
			   ,COALESCE(CCPI."IdCuentaCustodioPortafolio",0) AS "IdCuentaCustodioPortafolio"
			   ,COALESCE(CCPI."Codigo",'') AS "NumeroCuentaBoveda"
			   ,cast(IFNULL(case when sby."IdClasificacionTipoInstrumento" = 'IF' then EMP."INGValorRescate" else EMP."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
			   ,sby."INGMontoSubyacenteActualMonedaInstrumento" as "ValorCompra"	
			   ,sby."INGMontoATasaSubyacenteActual" as "MontoCompra" 			
			   ,sby."INGPrecioMercadoActual" as "INGPrecioMercadoActual" 			
			   ,sby."INGTasaMercadoActual"as "INGTasaMercadoActual" 				
			   ,sby."INGMontoMercadoActualMonedaInstrumento" as "ValorMercado"
			   ,sby."INGMontoATasaMercadoActual" as "MontoMercado"
			   ,CASE
			   		WHEN ifnull(sby."IdMonedaInstrumento",'') <> '' THEN sby."IdMonedaInstrumento"
			   		WHEN ifnull(sby."IdTipoReajusteSubyacente",'') <> '' THEN sby."IdTipoReajusteSubyacente"
			   		ELSE ifnull(sby."IdMonedaSistema",'')
			   	END AS "IdMonedaReajuste"
			   ,sby."IdMonedaEmision"
			FROM "BSCL.Custodia.BD::CUS.Cartera" AS EMP
			INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (EMP."FolioCartera" = ultSub."FolioCarteraRelacionada")
			INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoCartera" as ultPrt on (EMP."FolioCarteraRelacionada" = ultPrt."FolioCartera")
			INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as prt on (prt."IdCartera" = ultPrt."IdCartera")
			LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on sby."IdSubyacente" = ultSub."IdSubyacente"
			LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" as orig on (orig."IdCartera" = sby."IdCarteraRetirada")
			--LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdCliente" = EMP."IdCliente")
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = EMP."IdPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = EMP."IdCuentaCustodioPortafolio"
			WHERE
				EMP."IdEmpresa" = p_IdEmpresa
			AND	EMP."IdPortafolio" = case when v_IdPortafolio > 0 then v_IdPortafolio else EMP."IdPortafolio" END
			AND EMP."IdEstado" NOT IN ('ANU', 'VCD','ANT')
			AND
			(
				   (p_TipoFechaConsulta = 'LIQ' AND (-- Filtra por Fecha Liquidacion // 16-05-2017 Para prestamos, se modifica pa q siempre busque sobre FechaOperacion
							  v_FechaConsulta BETWEEN EMP."INGFechaOperacion" and case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end
							  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
						AND	  v_FechaConsulta < case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end
				   ))
				OR (p_TipoFechaConsulta = 'ADQ' AND (-- Filtra por Fecha Adquisición
							  v_FechaConsulta BETWEEN EMP."INGFechaOperacion" and case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end
							  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
						AND	  v_FechaConsulta < case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end
				))
			)
			AND EMP."IdTipoInstrumento" = 'EMP'
		)
		WHERE RANK = 1;
		
		
		-- Resumen
		v_tblEmprestitos =
		SELECT
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,cast(SUM("Cantidad") as DECIMAL(26,8)) as "Cantidad"
		   ,SUM("ValorCompra") as "ValorCompra"
		   ,SUM("MontoCompra") as "MontoCompra"
		   ,sum(coalesce("INGPrecioMercadoActual",0))/count(1) as "INGPrecioMercadoActual"
		   ,max(coalesce("INGTasaMercadoActual",0)) as "INGTasaMercadoActual"
		   ,sum(coalesce("ValorMercado",0)) as "ValorMercado"
		   ,sum(coalesce("MontoMercado",0)) as "MontoMercado"
		   ,SUM("ValorCompra") AS "ValorPPP"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision"
		FROM :v_tblEmprestitos
		GROUP BY
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision";
	
	--Emprestitos
	
	
	---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	-- Garantias
	---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	-- Detalle
	
		v_tblGarantias =
		SELECT
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,"Cantidad"
		   ,"ValorCompra"
		   ,"MontoCompra"
		   ,"INGPrecioMercadoActual"
		   ,"INGTasaMercadoActual"
		   ,"ValorMercado"
		   ,"MontoMercado"
		   ,0 AS "ValorPPP"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision"
		FROM
		(
			SELECT
				RANK() over (partition by  GAR."FolioGarantia" order by GAR."IdGarantia" desc) as RANK
			   ,GAR."IdClasificacionTipoInstrumento"
			   ,GAR."IdTipoInstrumento"
			   ,GAR."IdInstrumento"
			   ,case when GAR."IdClasificacionTipoInstrumento" = 'IF' then GAR."NemoSVS" else GAR."Nemotecnico" end as "Nemotecnico"
			   ,CASE WHEN IFNULL(GAR."IdEmisor",'') <> '' THEN IFNULL(GAR."IdEmisor",'') ELSE inst4."IdEmisor" END AS "IdEmisor"
			   ,case when GAR."IdClasificacionTipoInstrumento" = 'IF' then GAR."EsFungible" else 'N' end as "EsFungible"
			   ,GAR."IdCliente"
			   ,GAR."IdPortafolio"
			   ,CAR."IdBoveda" AS "IdBoveda"
			   ,COALESCE(CCPI."IdCuentaCustodioPortafolio",0) AS "IdCuentaCustodioPortafolio"
			   ,COALESCE(CCPI."Codigo",'') AS "NumeroCuentaBoveda"			   
			   ,cast(IFNULL(case when GAR."IdClasificacionTipoInstrumento" = 'IF' then GAR."INGValorRescate" else GAR."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
			   ,GAR."INGMontoGarantiaActualMonedaInstrumento" as "ValorCompra" 
			   ,GAR."INGMontoATasaGarantiaActual" as "MontoCompra"				
			   ,GAR."INGPrecioMercadoActual" as "INGPrecioMercadoActual" 		
			   ,GAR."INGTasaMercadoActual"as "INGTasaMercadoActual" 			
			   ,GAR."INGMontoMercadoActualMonedaInstrumento" as "ValorMercado"
			   ,GAR."INGMontoATasaMercadoActual" as "MontoMercado"
			   ,CASE
			   		WHEN IFNULL(GAR."IdTipoReajuste",'')<>'' THEN IFNULL(GAR."IdTipoReajuste",'')
			   		WHEN IFNULL(inst4."IdTipoReajuste",'')<>'' THEN IFNULL(inst4."IdTipoReajuste",'')
					WHEN IFNULL(GAR."IdMonedaInstrumento",'')<>'' THEN IFNULL(GAR."IdMonedaInstrumento",'')			   		
			   		ELSE IFNULL(inst4."IdMoneda",'')
				END as "IdMonedaReajuste"
			    ,CASE 
			   		WHEN IFNULL(GAR."IdMonedaEmision",'')<>'' THEN IFNULL(GAR."IdMonedaEmision",'')
			   		WHEN IFNULL(GAR."IdTipoReajuste",'')<>'' THEN IFNULL(GAR."IdTipoReajuste",'')
			   		WHEN IFNULL(GAR."IdMonedaInstrumento",'')<>'' THEN IFNULL(GAR."IdMonedaInstrumento",'')
			   		WHEN IFNULL(inst4."IdTipoReajuste",'')<>'' THEN IFNULL(inst4."IdTipoReajuste",'')
			   		ELSE IFNULL(inst4."IdMoneda",'')
				END AS "IdMonedaEmision"
			FROM "BSCL.Custodia.BD::GTA.Garantia" AS GAR
			INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as CAR ON CAR."IdCartera" = GAR."IdCarteraOrigen"
			--LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = GAR."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = GAR."IdPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = GAR."IdInstrumento"
			WHERE
				GAR."IdEmpresa" = p_IdEmpresa
			AND GAR."IdPortafolio" = case when v_IdPortafolio > 0 then v_IdPortafolio else GAR."IdPortafolio" END
			AND GAR."IdEstado" = 'VIG'
			AND
			(
				(p_TipoFechaConsulta = 'LIQ' AND (   -- Filtra por Fecha Liquidacion
								  v_FechaConsulta BETWEEN GAR."INGFechaLiquidacion" and case when IFNULL(GAR."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(GAR."EGRFechaLiquidacion",'0001-01-01') end
								  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
							AND	  v_FechaConsulta < case when IFNULL(GAR."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(GAR."EGRFechaLiquidacion",'0001-01-01') end	   
				))
				OR (p_TipoFechaConsulta = 'ADQ' AND (   -- Filtra por Fecha Adquisición
								  v_FechaConsulta BETWEEN GAR."INGFechaOperacion" and case when IFNULL(GAR."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(GAR."EGRFechaOperacion",'0001-01-01') end
								  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
							AND	  v_FechaConsulta < case when IFNULL(GAR."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(GAR."EGRFechaOperacion",'0001-01-01') end	
				)) 
			)
		)
		WHERE RANK = 1;
		
		
		-- Resumen
		v_tblGarantias =
		SELECT
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,cast(SUM("Cantidad") as DECIMAL(26,8)) as "Cantidad"
		   ,SUM("ValorCompra") as "ValorCompra"
		   ,SUM("MontoCompra") as "MontoCompra"
		   ,sum(coalesce("INGPrecioMercadoActual",0))/count(1) as "INGPrecioMercadoActual"
		   ,max(coalesce("INGTasaMercadoActual",0)) as "INGTasaMercadoActual"
		   ,sum(coalesce("ValorMercado",0)) as "ValorMercado"
		   ,sum(coalesce("MontoMercado",0)) as "MontoMercado"
		   ,SUM("ValorCompra") AS "ValorPPP"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision"
		FROM :v_tblGarantias
		GROUP BY
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision";
	
	--garantias
	
	
	---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	-- Movimiento en transito, ingreso
	---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	-- Detalle
		
		v_tblTrasnIng =
		SELECT
				MOV."IdClasificacionTipoInstrumento"
			   ,MOV."IdTipoInstrumento"
			   ,MOV."IdInstrumento"	AS "IdInstrumento"
			   ,CASE WHEN MOV."IdClasificacionTipoInstrumento" = 'IF' THEN MOV."NemoSVS" ELSE MOV."Nemotecnico" END AS "Nemotecnico"
			   ,CASE WHEN IFNULL(MOV."IdEmisor",'') <> '' THEN IFNULL(MOV."IdEmisor",'') ELSE inst4."IdEmisor" END AS "IdEmisor"
			   ,case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."EsFungible" else 'N' end as "EsFungible"
			   ,MOV."IdCliente"
			   ,MOV."IdPortafolio"
			   ,MOV."IdBoveda" AS "IdBoveda"			   
			   ,COALESCE(CCPI."IdCuentaCustodioPortafolio",0) AS "IdCuentaCustodioPortafolio"
			   ,COALESCE(CCPI."Codigo",'') AS "NumeroCuentaBoveda"
			   ,CASE WHEN MOV."EsNominal" = 'N' THEN 
					cast(IFNULL(case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."ValorRescate" else MOV."Cantidad" end,0) as DECIMAL(26,8)) ELSE 0 END as "CantidadCustodia"
  			   ,CASE WHEN MOV."EsNominal" = 'S' THEN 
  			   		cast(IFNULL(case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."ValorRescate" else MOV."Cantidad" end,0) as DECIMAL(26,8)) ELSE 0 END as "CantidadNominal"
			   ,0 AS "ValorCompra"
			   ,0 AS "MontoCompra"
			   ,0 AS "INGPrecioMercadoActual"
			   ,0 AS "INGTasaMercadoActual"
			   ,0 AS "ValorMercado"
			   ,0 AS "MontoMercado"
			   ,0 AS "ValorPPP"
			   ,CASE
			   		WHEN ifnull(MOV."IdTipoReajuste",'') <> '' THEN MOV."IdTipoReajuste"
			   		WHEN ifnull(MOV."IdMonedaInstrumento",'') <> '' THEN MOV."IdMonedaInstrumento"
			   		WHEN ifnull(MOV."IdMonedaSistema",'') <> '' THEN MOV."IdMonedaSistema"
			   		WHEN ifnull(inst4."IdTipoReajuste",'')<> '' THEN ifnull(inst4."IdTipoReajuste",'')
			   		ELSE inst4."IdMoneda"
			   	END AS "IdMonedaReajuste"
			   ,CASE
			   		WHEN ifnull(MOV."IdMonedaEmision",'') <> '' THEN MOV."IdMonedaEmision"
			   		WHEN ifnull(MOV."IdTipoReajuste",'') <> '' THEN MOV."IdTipoReajuste"
			   		WHEN ifnull(MOV."IdMonedaInstrumento",'') <> '' THEN MOV."IdMonedaInstrumento"
			   		WHEN ifnull(MOV."IdMonedaSistema",'') <> '' THEN MOV."IdMonedaSistema"
			   		WHEN ifnull(inst4."IdTipoReajuste",'')<> '' THEN ifnull(inst4."IdTipoReajuste",'')
			   		ELSE inst4."IdMoneda"
			   	END AS "IdMonedaEmision"
			FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
 			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = MOV."IdCuentaCustodioPortafolio"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = MOV."IdInstrumento"
			WHERE
				MOV."IdEmpresa" = p_IdEmpresa
			AND	MOV."IdPortafolio" = CASE WHEN v_IdPortafolio > 0 THEN v_IdPortafolio ELSE MOV."IdPortafolio" END
			AND	MOV."IdEstado" = 'PEN'
			AND IFNULL(TipoOperacion."AccionCustodia",'') <> '' 
			AND TipoOperacion."AccionCustodia" = 'INGRESO'
			AND	  
			(
				(v_FechaConsulta >= MOV."FechaOperacion" AND v_FechaConsulta <= MOV."FechaCertificacionRegistro" AND MOV."FechaCertificacionRegistro" >= '0001-01-02')
				OR 
				(v_FechaConsulta >= MOV."FechaOperacion" AND MOV."FechaCertificacionRegistro" = '0001-01-01' )
			)
			AND MOV."IdMovimientoCarteraEnTransitoRelacionado" = 0;
		
		
		-- Resumen
		v_tblTrasnIng =
		SELECT
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,cast(SUM("CantidadCustodia") as DECIMAL(26,8)) as "CantidadCustodia"
		   ,cast(SUM("CantidadNominal") as DECIMAL(26,8)) as "CantidadNominal"
		   ,0 as "ValorCompra"
		   ,0 as "MontoCompra"
		   ,0 as "INGPrecioMercadoActual"
		   ,0 as "INGTasaMercadoActual"
		   ,0 as "ValorMercado"
		   ,0 as "MontoMercado"
		   ,0 as "ValorPPP"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision"
		FROM :v_tblTrasnIng
		GROUP BY
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision";
	
	--Movimiento en transito, ingreso
	
	
	---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	-- Movimiento en transito, egreso
	---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	-- Detalle
	
		v_tblTrasnEgr =
		SELECT
				MOV."IdClasificacionTipoInstrumento"
			   ,MOV."IdTipoInstrumento"
			   ,MOV."IdInstrumento"	AS "IdInstrumento"
			   ,CASE WHEN MOV."IdClasificacionTipoInstrumento" = 'IF' THEN MOV."NemoSVS" ELSE MOV."Nemotecnico" END AS "Nemotecnico"
			   ,CASE WHEN IFNULL(MOV."IdEmisor",'') <> '' THEN IFNULL(MOV."IdEmisor",'') ELSE inst4."IdEmisor" END AS "IdEmisor"
			   ,case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."EsFungible" else 'N' end as "EsFungible"
			   ,MOV."IdCliente"
			   ,MOV."IdPortafolio"
			   ,MOV."IdBoveda" AS "IdBoveda"
			   ,COALESCE(CCPI."IdCuentaCustodioPortafolio",0) AS "IdCuentaCustodioPortafolio"
			   ,COALESCE(CCPI."Codigo",'') AS "NumeroCuentaBoveda"
			   ,cast(IFNULL(case when MOV."IdClasificacionTipoInstrumento" = 'IF'  then MOV."ValorRescate" else MOV."Cantidad" end,0) * -1 as DECIMAL(26,8)) as "Cantidad"
			   ,0 AS "ValorCompra"
			   ,0 AS "MontoCompra"
			   ,0 AS "INGPrecioMercadoActual"
			   ,0 AS "INGTasaMercadoActual"
			   ,0 AS "ValorMercado"
			   ,0 AS "MontoMercado"
			   ,0 AS "ValorPPP"
			   ,CASE
			   		WHEN ifnull(MOV."IdTipoReajuste",'') <> '' THEN MOV."IdTipoReajuste"
			   		WHEN ifnull(MOV."IdMonedaInstrumento",'') <> '' THEN MOV."IdMonedaInstrumento"
			   		WHEN ifnull(MOV."IdMonedaSistema",'') <> '' THEN MOV."IdMonedaSistema"
			   		WHEN ifnull(inst4."IdTipoReajuste",'')<> '' THEN ifnull(inst4."IdTipoReajuste",'')
			   		ELSE inst4."IdMoneda"
			   	END AS "IdMonedaReajuste"
			   ,CASE
			   		WHEN ifnull(MOV."IdMonedaEmision",'') <> '' THEN MOV."IdMonedaEmision"
			   		WHEN ifnull(MOV."IdTipoReajuste",'') <> '' THEN MOV."IdTipoReajuste"
			   		WHEN ifnull(MOV."IdMonedaInstrumento",'') <> '' THEN MOV."IdMonedaInstrumento"
			   		WHEN ifnull(MOV."IdMonedaSistema",'') <> '' THEN MOV."IdMonedaSistema"
			   		WHEN ifnull(inst4."IdTipoReajuste",'')<> '' THEN ifnull(inst4."IdTipoReajuste",'')
			   		ELSE inst4."IdMoneda"
			   	END AS "IdMonedaEmision"
			FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
 			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
			--LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = MOV."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = MOV."IdCuentaCustodioPortafolio"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = MOV."IdInstrumento"
			WHERE
				MOV."IdEmpresa" = p_IdEmpresa
			AND	MOV."IdPortafolio" = case when v_IdPortafolio > 0 then v_IdPortafolio else MOV."IdPortafolio" END
			AND	MOV."IdEstado" = 'PEN'
			AND IFNULL(TipoOperacion."AccionCustodia",'') <> ''
			AND
			(
				TipoOperacion."AccionCustodia" = 'RETIRO' AND MOV."EsNominal" = 'N'
			)
			AND
			(
				(v_FechaConsulta >= MOV."FechaOperacion" AND v_FechaConsulta <= MOV."FechaCertificacionRegistro" AND MOV."FechaCertificacionRegistro" >= '0001-01-02')
				OR
				(v_FechaConsulta >= MOV."FechaOperacion" AND MOV."FechaCertificacionRegistro" = '0001-01-01' )
			)
			AND MOV."IdMovimientoCarteraEnTransitoRelacionado" = 0;
		
		
		-- Resumen
		v_tblTrasnEgr =
		SELECT
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,cast(SUM("Cantidad") as DECIMAL(26,8)) as "Cantidad"
		   ,0 as "ValorCompra"
		   ,0 as "MontoCompra"
		   ,0 as "INGPrecioMercadoActual"
		   ,0 as "INGTasaMercadoActual"
		   ,0 as "ValorMercado"
		   ,0 as "MontoMercado"
		   ,0 as "ValorPPP"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision"
		FROM :v_tblTrasnEgr
		GROUP BY
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision";
	
	--Movimiento en transito, ingreso	
	
	---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	-- Instrumentos de los cuales el portafolio tiene custodia
	---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	p_precios_fm = SELECT PRE."IdInstrumento",PRE."ValorCuota",PRE."FechaCierre",PRE."FechaConsulta"
				FROM
					( 	
									
						SELECT row_number() over (partition by s."IdInstrumento" order by s."IdInstrumento",b."FechaCierre" desc) AS row_num
						,s."IdInstrumento",b."FechaCierre",b."ValorCuota",:v_FechaConsulta as "FechaConsulta"
						FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" s
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico" b on s."IdSerieFondo" = b."IdSerieFondo" and "EsUltimaCargaDiaria" = 'S'
						where b."FechaCierre" <= :v_FechaConsulta
				) PRE	
				WHERE PRE.row_num = 1 ;
	
	v_tblInst =
	SELECT DISTINCT
		a."IdClasificacionTipoInstrumento"
		,CTINS."Nombre" AS "NombreClasificacionTipoInstrumento"
		,a."IdTipoInstrumento"
		,TINS."Nombre" AS "NombreTipoInstrumento"
		,INS."ISIN"
		,a."IdInstrumento"
		,a."Nemotecnico"
		,a."IdEmisor"
		,a."EsFungible"
		,a."IdCliente"
		,a."IdPortafolio"
		,POR."CodigoPortafolio"
		,PORMATRIZ."EsADC"
		,PORMATRIZ."IdMatrizAgrupador"
		,PERNOM."NombreCompleto" as "NombreCliente"
		,IDENT."DocumentoIdentidad"
		,a."IdBoveda"
		,a."IdCuentaCustodioPortafolio"
		,a."NumeroCuentaBoveda"
		,CASE
		WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO'
		WHEN COALESCE(IND."ValorIndicador",'N') = 'S' THEN 'REL'
		ELSE 'TER' END AS "Relacion",
		CASE WHEN COALESCE(ins."Tamano",0) = 0 THEN 1 ELSE ins."Tamano" END AS "Tamano",
		ins."Precio" AS "PrecioBase",
	   	CASE 
	   		WHEN a."IdClasificacionTipoInstrumento"  = 'FM' THEN PFM."ValorCuota" 
	   		WHEN a."IdClasificacionTipoInstrumento" = 'RV' and PRECIO_BCS."Precio" > 0 THEN PRECIO_BCS."Precio"
	   		ELSE 0 END as "PrecioMercadoCustodia",
		CASE 
	   		WHEN a."IdClasificacionTipoInstrumento"  = 'FM' THEN PFM."FechaCierre" 
	   		WHEN a."IdClasificacionTipoInstrumento" = 'RV' and  PRECIO_BCS."Precio" > 0 THEN PRECIO_BCS."FechaSistema"
	   		ELSE '0001-01-01' END
	   as "FechaFuentePrecioMercadoCustodia"
	FROM
	(
		SELECT DISTINCT "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","IdEmisor","EsFungible", "IdCliente", "IdPortafolio", "IdBoveda", "IdCuentaCustodioPortafolio","NumeroCuentaBoveda" FROM :v_tblCustodia UNION ALL
		SELECT DISTINCT "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","IdEmisor","EsFungible", "IdCliente", "IdPortafolio", "IdBoveda", "IdCuentaCustodioPortafolio","NumeroCuentaBoveda" FROM :v_tblVentaPacto UNION ALL 
		SELECT DISTINCT "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","IdEmisor","EsFungible", "IdCliente", "IdPortafolio", "IdBoveda", "IdCuentaCustodioPortafolio","NumeroCuentaBoveda" FROM :v_tblCompraPacto UNION ALL
		SELECT DISTINCT "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","IdEmisor","EsFungible", "IdCliente", "IdPortafolio", "IdBoveda", "IdCuentaCustodioPortafolio","NumeroCuentaBoveda" FROM :v_tblSimulC UNION ALL
		SELECT DISTINCT "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","IdEmisor","EsFungible", "IdCliente", "IdPortafolio", "IdBoveda", "IdCuentaCustodioPortafolio","NumeroCuentaBoveda" FROM :v_tblSimulV UNION ALL
		SELECT DISTINCT "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","IdEmisor","EsFungible", "IdCliente", "IdPortafolio", "IdBoveda", "IdCuentaCustodioPortafolio","NumeroCuentaBoveda" FROM :v_tblPrestamos UNION ALL
		SELECT DISTINCT "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","IdEmisor","EsFungible", "IdCliente", "IdPortafolio", "IdBoveda", "IdCuentaCustodioPortafolio","NumeroCuentaBoveda" FROM :v_tblEmprestitos UNION ALL
		SELECT DISTINCT "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","IdEmisor","EsFungible", "IdCliente", "IdPortafolio", "IdBoveda", "IdCuentaCustodioPortafolio","NumeroCuentaBoveda" FROM :v_tblTrasnIng UNION ALL
		SELECT DISTINCT "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","IdEmisor","EsFungible", "IdCliente", "IdPortafolio", "IdBoveda", "IdCuentaCustodioPortafolio","NumeroCuentaBoveda" FROM :v_tblTrasnEgr UNION ALL
		SELECT DISTINCT "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","IdEmisor","EsFungible", "IdCliente", "IdPortafolio", "IdBoveda", "IdCuentaCustodioPortafolio","NumeroCuentaBoveda" FROM :v_tblGarantias
		
	) AS a
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = a."IdTipoInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = a."IdInstrumento"
	LEFT JOIN :p_precios_FM PFM ON PFM."IdInstrumento" = ins."IdInstrumento" and :v_FechaConsulta = PFM."FechaConsulta"
	LEFT JOIN "BSCL.Plataforma.BD::INS.RegistroPrecioBolsaInstrumento" AS PRECIO_BCS ON PRECIO_BCS."IdEmpresa" = 1 AND INS."IdInstrumento" = PRECIO_BCS."IdInstrumento" and PRECIO_BCS."FechaSistema" = :v_FechaConsulta
	LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CTINS ON CTINS."IdClasificacionTipoInstrumento" = TINS."IdClasificacionTipoInstrumento"
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdPortafolio" = a."IdPortafolio"
	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMatriz" AS PORMATRIZ ON POR."IdPortafolio" = PORMATRIZ."IdPortafolio"
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI  ON CLI."IdCliente" = a."IdCliente"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER  ON PER."IdPersona" = CLI."IdPersona"
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" AS PERNOM  ON PER."IdPersona" = PERNOM."IdPersona"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT  ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
	LEFT JOIN (
		SELECT "IdEntidad" AS "IdCliente","ValorIndicador" 
		FROM "BSCL.Plataforma.BD::IND.TipoIndicador"  TPO
		LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IND ON IND."IdTipoIndicador" = TPO."IdTipoIndicador"
		WHERE TPO."IdTipoEntidad" = 'CLIENTE' AND TPO."CodigoTipoIndicador"  = 'RELCORR'
	) AS IND ON IND."IdCliente" = CLI."IdCliente";
	

	/*select 'inst',* from :v_tblInst;
	SELECT  'cus',* FROM :v_tblCustodia;
	SELECT  'vpc',* FROM :v_tblVentaPacto; 
	SELECT  'ccp',* FROM :v_tblCompraPacto;
	SELECT  'simc',* FROM :v_tblSimulC;
	SELECT  'simv',* FROM :v_tblSimulV;
	SELECT  'prt',* FROM :v_tblPrestamos;
	SELECT  'emp',* FROM :v_tblEmprestitos;
	SELECT  'egr',* FROM :v_tblTrasnEgr;
	SELECT  'ing',* FROM :v_tblTrasnIng;
	SELECT  'gta',* FROM :v_tblGarantias;*/
	
	---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	-- SALIDA
	---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	
	
	v_SalidaPrevia = 
	SELECT
		:p_IdEmpresa 			as "IdEmpresa",
		:p_TipoFechaConsulta	as "TipoFechaConsulta",
		:v_FechaConsulta		as "FechaCustodia",
		ins."IdClasificacionTipoInstrumento", 
		ins."NombreClasificacionTipoInstrumento",
		ins."IdTipoInstrumento", 
		ins."NombreTipoInstrumento", 
		ins."ISIN",
		ins."IdInstrumento",
		ins."Nemotecnico",
		ins."IdEmisor" AS "IdEmisor",
		ins."EsFungible", 
		ins."IdCliente", 
		ins."IdPortafolio", 
		ins."DocumentoIdentidad",
		ins."NombreCliente",
		ins."CodigoPortafolio",
		ins."EsADC",
		ins."IdMatrizAgrupador",
		ins."IdBoveda",
		ins."NumeroCuentaBoveda",
		ins."Relacion",
		ins."Relacion" as "DescripcionRelacion",
		ins."Tamano" AS "Tamano",
		SUM(COALESCE(cus."Cantidad",0)+COALESCE(vpc."Cantidad",0)+COALESCE(cpc."Cantidad",0)+COALESCE(simc."Cantidad",0)+COALESCE(simv."Cantidad",0)+COALESCE(prt."Cantidad",0)+COALESCE(emp."Cantidad",0)+COALESCE(gar."Cantidad",0)) AS "CantidadTotal",
		SUM(COALESCE(cus."ValorCompra",0)+COALESCE(vpc."ValorCompra",0)+COALESCE(cpc."ValorCompra",0)+COALESCE(simc."ValorCompra",0)+COALESCE(simv."ValorCompra",0)+COALESCE(prt."ValorCompra",0)+COALESCE(emp."ValorCompra",0)+COALESCE(gar."ValorCompra",0)) AS "ValorCompra",
		SUM(COALESCE(cus."MontoCompra",0)+COALESCE(vpc."MontoCompra",0)+COALESCE(cpc."MontoCompra",0)+COALESCE(simc."MontoCompra",0)+COALESCE(simv."MontoCompra",0)+COALESCE(prt."MontoCompra",0)+COALESCE(emp."MontoCompra",0)+COALESCE(gar."MontoCompra",0)) AS "MontoCompra",
		
		SUM(COALESCE(cus."ValorMercado",0)+COALESCE(vpc."ValorMercado",0)+COALESCE(cpc."ValorMercado",0)+COALESCE(simc."ValorMercado",0)+COALESCE(simv."ValorMercado",0)+COALESCE(prt."ValorMercado",0)+COALESCE(emp."ValorMercado",0)+COALESCE(gar."ValorMercado",0)) AS "ValorTotalMercado",
		SUM(COALESCE(cus."MontoMercado",0)+COALESCE(vpc."MontoMercado",0)+COALESCE(cpc."MontoMercado",0)+COALESCE(simc."MontoMercado",0)+COALESCE(simv."MontoMercado",0)+COALESCE(prt."MontoMercado",0)+COALESCE(emp."MontoMercado",0)+COALESCE(gar."MontoMercado",0)) AS "MontoTotalMercado",
				
		sum(ifnull( cus."Cantidad",0)) + sum(ifnull( mti."CantidadNominal",0))	as "CantidadCustodia",
		sum(ifnull( cus."ValorCompra",0))  	as "ValorPPPCustodia",
		sum(ifnull( cus."MontoCompra",0))  	as "MontoPPPCustodia",
		ins."PrecioMercadoCustodia" AS "PrecioMercadoCustodia",
		CASE WHEN ins."IdClasificacionTipoInstrumento" = 'RV' THEN
			ins."PrecioMercadoCustodia" * sum(ifnull( cus."Cantidad",0)) 
			ELSE sum(ifnull( cus."ValorMercado",0))
		END AS "ValorMercadoCustodia",
		sum(ifnull( cus."MontoMercado",0))  as "MontoMercadoCustodia",
		
		sum(ifnull( simc."Cantidad",0))  						as "CantidadSimulCom",
		sum(ifnull( simc."ValorCompra",0))  						as "ValorPPPSimulCom",
		sum(ifnull( simc."MontoCompra",0))  						as "MontoPPPSimulCom",
		sum(ifnull( simc."INGPrecioMercadoActual",0))/count(1)	as "PrecioMercadoSimulCom",
		max(coalesce(simc."INGTasaMercadoActual",0)) 				AS "TasaMercadoSimulCom",
		sum(ifnull( simc."ValorMercado",0))						as "ValorMercadoSimulCom",
		sum(ifnull( simc."MontoMercado",0)) 					as "MontoMercadoSimulCom",
		
		sum(ifnull( simv."Cantidad",0))  	as "CantidadSimulVen",
		sum(ifnull( simv."ValorCompra",0))  	as "ValorPPPSimulVen",
		sum(ifnull( simv."MontoCompra",0))  	as "MontoPPPSimulVen",
		sum(ifnull( simv."INGPrecioMercadoActual",0))/count(1)	as "PrecioMercadoSimulVen",
		max(coalesce(simv."INGTasaMercadoActual",0)) 				AS "TasaMercadoSimulVen",
		sum(ifnull( simv."ValorMercado",0)) as "ValorMercadoSimulVen",
		sum(ifnull( simv."MontoMercado",0)) as "MontoMercadoSimulVen",
		
		sum(ifnull( prt."Cantidad",0))  	as "CantidadPrestamo",
		sum(ifnull( prt."ValorCompra",0))  	as "ValorPPPPrestamo",
		sum(ifnull( prt."MontoCompra",0))  	as "MontoPPPPrestamo",
		sum(ifnull( prt."INGPrecioMercadoActual",0))/count(1)	as "PrecioMercadoPrestamo",
		max(coalesce(prt."INGTasaMercadoActual",0))				AS "TasaMercadoPrestamo",
		sum(ifnull( prt."ValorMercado",0)) 	as "ValorMercadoPrestamo",
		sum(ifnull( prt."MontoMercado",0)) 	as "MontoMercadoPrestamo",
		
		sum(ifnull( emp."Cantidad",0))  	as "CantidadEmpresti",
		sum(ifnull( emp."ValorCompra",0))  	as "ValorPPPEmpresti",
		sum(ifnull( emp."MontoCompra",0))  	as "MontoPPPEmpresti",
		sum(ifnull( emp."INGPrecioMercadoActual",0))/count(1)	as "PrecioMercadoEmpresti",
		max(coalesce(emp."INGTasaMercadoActual",0)) 				AS "TasaMercadoEmpresti",
		sum(ifnull( emp."ValorMercado",0)) 	as "ValorMercadoEmpresti",
		sum(ifnull( emp."MontoMercado",0)) 	as "MontoMercadoEmpresti",
		
		sum(ifnull( gar."Cantidad",0))  	as "CantidadGarantia",
		sum(ifnull( gar."ValorCompra",0))  	as "ValorPPPGarantia",
		sum(ifnull( gar."MontoCompra",0))  	as "MontoPPPGarantia",
		sum(ifnull( gar."INGPrecioMercadoActual",0))/count(1)	as "PrecioMercadoGarantia",
		max(coalesce(gar."INGTasaMercadoActual",0)) 				AS "TasaMercadoGarantia",
		sum(ifnull( gar."ValorMercado",0)) 	as "ValorMercadoGarantia",
		sum(ifnull( gar."MontoMercado",0)) 	as "MontoMercadoGarantia",
		
		sum(ifnull( vpc."Cantidad",0))  	as "CantidadPactosV",
		sum(ifnull( vpc."ValorCompra",0))  	as "ValorPPPPactosV",
		sum(ifnull( vpc."MontoCompra",0))  	as "MontoPPPPactosV",
		sum(ifnull( vpc."INGPrecioMercadoActual",0))/count(1)	as "PrecioMercadoPactosV",
		max(coalesce(vpc."INGTasaMercadoActual",0)) 				AS "TasaMercadoPactosV",
		sum(ifnull( vpc."ValorMercado",0)) 	as "ValorMercadoPactosV",
		sum(ifnull( vpc."MontoMercado",0)) 	as "MontoMercadoPactosV",
		
		sum(ifnull( cpc."Cantidad",0))  	as "CantidadPactosC",
		sum(ifnull( cpc."ValorCompra",0))  	as "ValorPPPPactosC",
		sum(ifnull( cpc."MontoCompra",0))  	as "MontoPPPPactosC",
		sum(ifnull( cpc."INGPrecioMercadoActual",0))/count(1)	as "PrecioMercadoPactosC",
		max(coalesce(cpc."INGTasaMercadoActual",0)) 				AS "TasaMercadoPactosC",
		sum(ifnull( cpc."ValorMercado",0)) 	as "ValorMercadoPactosC",
		sum(ifnull( cpc."MontoMercado",0)) 	as "MontoMercadoPactosC",
		
		sum(ifnull( mti."CantidadCustodia",0))  	as "CantidadTransIng",
		sum(ifnull( mti."ValorCompra",0))  	as "ValorPPPTransIng",
		sum(ifnull( mti."MontoCompra",0))  	as "MontoPPPTransIng",
		sum(ifnull( mti."INGPrecioMercadoActual",0))/count(1)	as "PrecioMercadoTransIng",
		max(coalesce(mti."INGTasaMercadoActual",0)) 				AS "TasaMercadoTransIng",
		sum(ifnull( mti."ValorMercado",0)) 	as "ValorMercadoTransIng",
		sum(ifnull( mti."MontoMercado",0)) 	as "MontoMercadoTransIng",
		
		sum(ifnull( mte."Cantidad",0))  	as "CantidadTransEgr",
		sum(ifnull( mte."ValorCompra",0))  	as "ValorPPPTransEgr",
		sum(ifnull( mte."MontoCompra",0))  	as "MontoPPPTransEgr",
		sum(ifnull( mte."INGPrecioMercadoActual",0))/count(1)	as "PrecioMercadoTransEgr",
		max(coalesce(mte."INGTasaMercadoActual",0)) 				AS "TasaMercadoTransEgr",
		sum(ifnull( mte."ValorMercado",0)) 	as "ValorMercadoTransEgr",
		sum(ifnull( mte."MontoMercado",0)) 	as "MontoMercadoTransEgr",
				
		CASE WHEN coalesce(cus."IdMonedaReajuste",'') <> '' THEN cus."IdMonedaReajuste"
			WHEN coalesce(vpc."IdMonedaReajuste",'') <> '' THEN vpc."IdMonedaReajuste"
			WHEN coalesce(cpc."IdMonedaReajuste",'') <> '' THEN cpc."IdMonedaReajuste"
			WHEN coalesce(simc."IdMonedaReajuste",'') <> '' THEN simc."IdMonedaReajuste"
			WHEN coalesce(simv."IdMonedaReajuste",'') <> '' THEN simv."IdMonedaReajuste"
			WHEN coalesce(prt."IdMonedaReajuste",'') <> '' THEN prt."IdMonedaReajuste"
			WHEN coalesce(emp."IdMonedaReajuste",'') <> '' THEN emp."IdMonedaReajuste"
			WHEN coalesce(gar."IdMonedaReajuste",'') <> '' THEN gar."IdMonedaReajuste"
			WHEN coalesce(mti."IdMonedaReajuste",'') <> '' THEN mti."IdMonedaReajuste"
			WHEN coalesce(mte."IdMonedaReajuste",'') <> '' THEN mte."IdMonedaReajuste"
			ELSE v_IdMonedaSistema
		END AS "IdMonedaReajuste",
		
		CASE WHEN coalesce(cus."IdMonedaEmision",'') <> '' THEN cus."IdMonedaEmision"
			WHEN coalesce(vpc."IdMonedaEmision",'') <> '' THEN vpc."IdMonedaEmision"
			WHEN coalesce(cpc."IdMonedaEmision",'') <> '' THEN cpc."IdMonedaEmision"
			WHEN coalesce(simc."IdMonedaEmision",'') <> '' THEN simc."IdMonedaEmision"
			WHEN coalesce(simv."IdMonedaEmision",'') <> '' THEN simv."IdMonedaEmision"
			WHEN coalesce(prt."IdMonedaEmision",'') <> '' THEN prt."IdMonedaEmision"
			WHEN coalesce(emp."IdMonedaEmision",'') <> '' THEN emp."IdMonedaEmision"
			WHEN coalesce(gar."IdMonedaEmision",'') <> '' THEN gar."IdMonedaEmision"
			WHEN coalesce(mti."IdMonedaEmision",'') <> '' THEN mti."IdMonedaEmision"
			WHEN coalesce(mte."IdMonedaEmision",'') <> '' THEN mte."IdMonedaEmision"
			ELSE v_IdMonedaSistema
		END AS "IdMonedaEmision",
		ins."FechaFuentePrecioMercadoCustodia",
		CASE WHEN cus."IdClasificacionTipoInstrumento" = 'FM' THEN "IdRegimenFondoMutuo" ELSE '' END AS "IdRegimenFondoMutuo",
		CASE WHEN ins."IdClasificacionTipoInstrumento" = 'RV' 
			 AND SUM(COALESCE(cus."MontoCompra",0)+COALESCE(mti."MontoCompra",0)) > 0 
			 AND ((sum(ifnull( cus."Cantidad",0)) - sum(ifnull( mti."CantidadNominal",0)) + sum(ifnull( mte."Cantidad",0))) * ins."PrecioMercadoCustodia") > 0
		THEN 
				(((sum(ifnull( cus."Cantidad",0)) - sum(ifnull( mti."CantidadNominal",0)) + sum(ifnull( mte."Cantidad",0))) * ins."PrecioMercadoCustodia") - 
				SUM(COALESCE(cus."MontoCompra",0)-COALESCE(mti."MontoCompra",0))) / SUM(COALESCE(cus."MontoCompra",0)+COALESCE(mti."MontoCompra",0)) * 100
		ELSE
			 	TO_DECIMAL(0,26,8)
			 END AS "Rentabilidad"
	FROM :v_tblInst as ins
	--INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" as inst2 on ins."IdInstrumento" = inst2."IdInstrumento"
	--LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = inst2."IdTipoInstrumento"
	--LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CTINS ON CTINS."IdClasificacionTipoInstrumento" = TINS."IdClasificacionTipoInstrumento"
	LEFT JOIN :v_tblCustodia AS cus on cus."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
	                                  and cus."IdTipoInstrumento" = ins."IdTipoInstrumento"
	                                  and cus."IdInstrumento" = ins."IdInstrumento"
	                                  and cus."Nemotecnico" = ins."Nemotecnico"
	                                  and cus."EsFungible" = ins."EsFungible"
	                                  and cus."IdCliente" = ins."IdCliente"
	                                  and cus."IdPortafolio" = ins."IdPortafolio"
	                                  and cus."IdBoveda" = ins."IdBoveda"
	                                  AND cus."IdCuentaCustodioPortafolio" = ins."IdCuentaCustodioPortafolio"
	LEFT JOIN :v_tblVentaPacto as vpc on vpc."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
	                                  and vpc."IdTipoInstrumento" = ins."IdTipoInstrumento"
	                                  and vpc."IdInstrumento" = ins."IdInstrumento"
	                                  and vpc."EsFungible" = ins."EsFungible"
									  and vpc."IdCliente" = ins."IdCliente"
	                                  and vpc."IdCliente" = ins."IdCliente"
	                                  and vpc."IdPortafolio" = ins."IdPortafolio"
	                                  and vpc."IdBoveda" = ins."IdBoveda"
	                                  AND vpc."IdCuentaCustodioPortafolio" = ins."IdCuentaCustodioPortafolio"
	LEFT JOIN :v_tblCompraPacto as cpc on cpc."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
	                                  and cpc."IdTipoInstrumento" = ins."IdTipoInstrumento"
	                                  and cpc."IdInstrumento" = ins."IdInstrumento"
	                                  and cpc."EsFungible" = ins."EsFungible"
									  and cpc."IdCliente" = ins."IdCliente"
	                                  and cpc."IdCliente" = ins."IdCliente"
	                                  and cpc."IdPortafolio" = ins."IdPortafolio"
	                                  and cpc."IdBoveda" = ins."IdBoveda"
	                                  AND cpc."IdCuentaCustodioPortafolio" = ins."IdCuentaCustodioPortafolio"
	LEFT JOIN :v_tblSimulC as simc     on simc."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
	                                  and simc."IdTipoInstrumento" = ins."IdTipoInstrumento"
	                                  and simc."IdInstrumento" = ins."IdInstrumento"
	                                  and simc."Nemotecnico" = ins."Nemotecnico"
	                                  and simc."EsFungible" = ins."EsFungible"
	                                  and simc."IdCliente" = ins."IdCliente"
	                                  and simc."IdPortafolio" = ins."IdPortafolio"
	                                  and simc."IdBoveda" = ins."IdBoveda"
	                                  AND simc."IdCuentaCustodioPortafolio" = ins."IdCuentaCustodioPortafolio"
	LEFT JOIN :v_tblSimulV as simv     on simv."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
	                                  and simv."IdTipoInstrumento" = ins."IdTipoInstrumento"
	                                  and simv."IdInstrumento" = ins."IdInstrumento"
	                                  and simv."Nemotecnico" = ins."Nemotecnico"
	                                  and simv."EsFungible" = ins."EsFungible"
	                                  and simv."IdCliente" = ins."IdCliente"
	                                  and simv."IdPortafolio" = ins."IdPortafolio"
	                                  and simv."IdBoveda" = ins."IdBoveda"
	                                  AND simv."IdCuentaCustodioPortafolio" = ins."IdCuentaCustodioPortafolio"
	LEFT JOIN :v_tblPrestamos as prt   on prt."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
	                                  and prt."IdTipoInstrumento" = ins."IdTipoInstrumento"
	                                  and prt."IdInstrumento" = ins."IdInstrumento"
	                                  and prt."Nemotecnico" = ins."Nemotecnico"
	                                  and prt."EsFungible" = ins."EsFungible"
	                                  and prt."IdCliente" = ins."IdCliente"
	                                  and prt."IdPortafolio" = ins."IdPortafolio"
	                                  and prt."IdBoveda" = ins."IdBoveda"
	                                  AND prt."IdCuentaCustodioPortafolio" = ins."IdCuentaCustodioPortafolio"
	LEFT JOIN :v_tblEmprestitos as emp on emp."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
	                                  and emp."IdTipoInstrumento" = ins."IdTipoInstrumento"
	                                  and emp."IdInstrumento" = ins."IdInstrumento"
	                                  and emp."Nemotecnico" = ins."Nemotecnico"
	                                  and emp."EsFungible" = ins."EsFungible"
	                                  and emp."IdCliente" = ins."IdCliente"
	                                  and emp."IdPortafolio" = ins."IdPortafolio"
	                                  and emp."IdBoveda" = ins."IdBoveda"
	                                  AND emp."IdCuentaCustodioPortafolio" = ins."IdCuentaCustodioPortafolio"
	LEFT JOIN :v_tblGarantias as gar   on gar."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
	                                  and gar."IdTipoInstrumento" = ins."IdTipoInstrumento"
	                                  and gar."IdInstrumento" = ins."IdInstrumento"
	                                  and gar."IdCliente" = ins."IdCliente"
	                                  and gar."Nemotecnico" = ins."Nemotecnico"
	                                  and gar."EsFungible" = ins."EsFungible"
	                                  and gar."IdPortafolio" = ins."IdPortafolio"
	                                  and gar."IdBoveda" = ins."IdBoveda"
	                                  AND gar."IdCuentaCustodioPortafolio" = ins."IdCuentaCustodioPortafolio"
	LEFT JOIN :v_tblTrasnIng as mti    on mti."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
	                                  and mti."IdTipoInstrumento" = ins."IdTipoInstrumento"
	                                  and mti."IdInstrumento" = ins."IdInstrumento"
	                                  and mti."Nemotecnico" = ins."Nemotecnico"
	                                  and mti."EsFungible" = ins."EsFungible"
									  and mti."IdCliente" = ins."IdCliente"
	                                  and mti."IdPortafolio" = ins."IdPortafolio"
	                                  and mti."IdBoveda" = ins."IdBoveda"
	                                  AND mti."IdCuentaCustodioPortafolio" = ins."IdCuentaCustodioPortafolio"
	LEFT JOIN :v_tblTrasnEgr as mte    on mte."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
	                                  and mte."IdTipoInstrumento" = ins."IdTipoInstrumento"
	                                  and mte."IdInstrumento" = ins."IdInstrumento"
	                                  and mte."EsFungible" = ins."EsFungible"
									  and mte."IdCliente" = ins."IdCliente"
	                                  and mte."IdPortafolio" = ins."IdPortafolio"
	                                  and mte."IdBoveda" = ins."IdBoveda"
	                                  AND mte."IdCuentaCustodioPortafolio" = ins."IdCuentaCustodioPortafolio"
	
    GROUP BY 	ins."IdClasificacionTipoInstrumento",
    			ins."NombreClasificacionTipoInstrumento",
    			ins."IdTipoInstrumento",
    			ins."NombreTipoInstrumento",
    			ins."ISIN",
    			ins."IdInstrumento",
    			ins."Nemotecnico",
    			ins."IdEmisor",
    			ins."EsFungible",
    			ins."IdCliente",
    			ins."IdPortafolio",
    			ins."DocumentoIdentidad",
    			ins."NombreCliente",
    			ins."CodigoPortafolio",
    			ins."EsADC",
    			ins."IdMatrizAgrupador",
    			ins."IdBoveda",
    			ins."NumeroCuentaBoveda",
    			ins."Relacion",
    			ins."Tamano",
    			INS."PrecioMercadoCustodia",
    			INS."FechaFuentePrecioMercadoCustodia",
				CASE WHEN coalesce(cus."IdMonedaReajuste",'') <> '' THEN cus."IdMonedaReajuste"
				WHEN coalesce(vpc."IdMonedaReajuste",'') <> '' THEN vpc."IdMonedaReajuste"
				WHEN coalesce(cpc."IdMonedaReajuste",'') <> '' THEN cpc."IdMonedaReajuste"
				WHEN coalesce(simc."IdMonedaReajuste",'') <> '' THEN simc."IdMonedaReajuste"
				WHEN coalesce(simv."IdMonedaReajuste",'') <> '' THEN simv."IdMonedaReajuste"
				WHEN coalesce(prt."IdMonedaReajuste",'') <> '' THEN prt."IdMonedaReajuste"
				WHEN coalesce(emp."IdMonedaReajuste",'') <> '' THEN emp."IdMonedaReajuste"
				WHEN coalesce(gar."IdMonedaReajuste",'') <> '' THEN gar."IdMonedaReajuste"
				WHEN coalesce(mti."IdMonedaReajuste",'') <> '' THEN mti."IdMonedaReajuste"
				WHEN coalesce(mte."IdMonedaReajuste",'') <> '' THEN mte."IdMonedaReajuste"
				ELSE v_IdMonedaSistema
			END 
			,CASE WHEN coalesce(cus."IdMonedaEmision",'') <> '' THEN cus."IdMonedaEmision"
				WHEN coalesce(vpc."IdMonedaEmision",'') <> '' THEN vpc."IdMonedaEmision"
				WHEN coalesce(cpc."IdMonedaEmision",'') <> '' THEN cpc."IdMonedaEmision"
				WHEN coalesce(simc."IdMonedaEmision",'') <> '' THEN simc."IdMonedaEmision"
				WHEN coalesce(simv."IdMonedaEmision",'') <> '' THEN simv."IdMonedaEmision"
				WHEN coalesce(prt."IdMonedaEmision",'') <> '' THEN prt."IdMonedaEmision"
				WHEN coalesce(emp."IdMonedaEmision",'') <> '' THEN emp."IdMonedaEmision"
				WHEN coalesce(gar."IdMonedaEmision",'') <> '' THEN gar."IdMonedaEmision"
				WHEN coalesce(mti."IdMonedaEmision",'') <> '' THEN mti."IdMonedaEmision"
				WHEN coalesce(mte."IdMonedaEmision",'') <> '' THEN mte."IdMonedaEmision"
				ELSE v_IdMonedaSistema
			END
			,CASE WHEN cus."IdClasificacionTipoInstrumento" = 'FM' THEN "IdRegimenFondoMutuo" ELSE '' END;
	
	--select 'v_SalidaPrevia',* from :v_SalidaPrevia;
	
	
		
	p_out_tbl =
	SELECT
		a."IdEmpresa"
	   ,a."TipoFechaConsulta"
	   ,a."FechaCustodia"
	   ,a."IdClasificacionTipoInstrumento"
	   ,a."NombreClasificacionTipoInstrumento"
	   ,a."IdTipoInstrumento"
	   ,a."NombreTipoInstrumento"
	   ,a."ISIN"
	   ,a."IdInstrumento"
	   ,a."Nemotecnico"
	   ,a."IdEmisor"
	   ,a."EsFungible"
	   ,a."IdCliente"
	   ,a."IdPortafolio"
	   ,a."DocumentoIdentidad"
	   ,a."NombreCliente"
	   ,a."CodigoPortafolio"
	   ,a."IdBoveda"
	   ,a."NumeroCuentaBoveda"
	   ,a."Relacion"
	   ,a."DescripcionRelacion"	  
	-- CUSTODIA
	   ,a."CantidadCustodia"
	   ,CASE WHEN a."IdClasificacionTipoInstrumento" <> 'RV' AND a."IdClasificacionTipoInstrumento" <> 'FM' 
		THEN TO_DECIMAL(0) ELSE (a."ValorCompra" / CASE WHEN a."CantidadTotal" > 0 THEN (a."CantidadTotal"*a."Tamano") ELSE 1 END) END 					AS "PPPCustodia"
	   ,CASE WHEN a."IdClasificacionTipoInstrumento" <> 'RV' AND a."IdClasificacionTipoInstrumento" <> 'FM' THEN TO_DECIMAL(0) ELSE a."ValorCompra" END AS "ValorPPPCustodia"
	   ,CASE WHEN a."IdClasificacionTipoInstrumento" <> 'RV' AND a."IdClasificacionTipoInstrumento" <> 'FM' THEN TO_DECIMAL(0) ELSE a."MontoCompra" END	AS "MontoPPPCustodia"
	   ,"PrecioMercadoCustodia"
	   ,CASE WHEN a."IdClasificacionTipoInstrumento" <> 'RV' THEN 
				CASE  WHEN "ValorTotalMercado">0 AND "ValorCompra">0 THEN
					(("ValorTotalMercado" - "ValorCompra")/ "ValorCompra") * 100
				ELSE 
					TO_DECIMAL(0,26,8)
				END
		 	ELSE 
				a."Rentabilidad"
		END AS "Rentabilidad"
	   ,"ValorMercadoCustodia"
	   ,"MontoMercadoCustodia"
	   ,("ValorMercadoCustodia" - "CantidadCustodia"*("ValorCompra" / CASE WHEN "CantidadTotal" > 0 THEN ("CantidadTotal"*a."Tamano") ELSE 1 END))			AS "DiferenciaMercadoCustodia"	--(Diferencia = Valor Mercado - Valor compra
	   ,a."IdMonedaReajuste"
	   ,COALESCE(a."IdMonedaEmision",'') as "EsPagaderoEn"	   
	-- SIMULTANEA COMPRA
	   ,"CantidadSimulCom"
	   ,CASE WHEN "IdClasificacionTipoInstrumento" <> 'RV' AND "IdClasificacionTipoInstrumento" <> 'FM' THEN TO_DECIMAL(0,26,8) ELSE ("ValorPPPSimulCom" / CASE WHEN "CantidadSimulCom" > 0 THEN "CantidadSimulCom" ELSE 1 END) END AS "PPPSimulCom"
	   ,CASE WHEN "IdClasificacionTipoInstrumento" <> 'RV' AND "IdClasificacionTipoInstrumento" <> 'FM' THEN TO_DECIMAL(0,26,8) ELSE "ValorPPPSimulCom" END 													AS "ValorPPPSimulCom"
	   ,"PrecioMercadoSimulCom"
	   ,"ValorMercadoSimulCom"
	   ,("ValorMercadoSimulCom" - "CantidadSimulCom"*("ValorCompra" / CASE WHEN "CantidadTotal" > 0 THEN ("CantidadTotal"*a."Tamano") ELSE 1 END))			AS "DiferenciaMercadoSimulCom"
	-- SIMULTANEA VENTA
	   ,"CantidadSimulVen"
	   ,CASE WHEN "IdClasificacionTipoInstrumento" <> 'RV' AND "IdClasificacionTipoInstrumento" <> 'FM' THEN TO_DECIMAL(0,26,8) ELSE ("ValorPPPSimulVen" / CASE WHEN "CantidadSimulVen" > 0 THEN "CantidadSimulVen" ELSE 1 END) END AS "PPPSimulVen"
	   ,CASE WHEN "IdClasificacionTipoInstrumento" <> 'RV' AND "IdClasificacionTipoInstrumento" <> 'FM' THEN TO_DECIMAL(0,26,8) ELSE "ValorPPPSimulVen" END 													AS "ValorPPPSimulVen" 
	   ,"PrecioMercadoSimulVen"
	   ,"ValorMercadoSimulVen"
	   ,("ValorMercadoSimulVen" - "CantidadSimulVen"*("ValorCompra" / CASE WHEN "CantidadTotal" > 0 THEN ("CantidadTotal"*a."Tamano") ELSE 1 END))			AS "DiferenciaMercadoSimulVen"
	-- PRESTAMO
	   ,"CantidadPrestamo"
	   ,CASE WHEN "IdClasificacionTipoInstrumento" <> 'RV' AND "IdClasificacionTipoInstrumento" <> 'FM' THEN TO_DECIMAL(0) ELSE ("ValorPPPPrestamo" / CASE WHEN "CantidadPrestamo" > 0 THEN "CantidadPrestamo" ELSE 1 END) END AS "PPPPrestamo"
	   ,CASE WHEN "IdClasificacionTipoInstrumento" <> 'RV' AND "IdClasificacionTipoInstrumento" <> 'FM' THEN TO_DECIMAL(0) ELSE "ValorPPPPrestamo" END 													AS "ValorPPPPrestamo" 
	   ,"PrecioMercadoPrestamo"
	   ,"ValorMercadoPrestamo"
	   ,("ValorMercadoPrestamo" - "CantidadPrestamo"*("ValorCompra" / CASE WHEN "CantidadTotal" > 0 THEN ("CantidadTotal"*a."Tamano") ELSE 1 END))			AS "DiferenciaMercadoPrestamo"
	-- EMPRESTITO
	   ,"CantidadEmpresti"
	   ,CASE WHEN "IdClasificacionTipoInstrumento" <> 'RV' AND "IdClasificacionTipoInstrumento" <> 'FM' THEN TO_DECIMAL(0,26,8) ELSE ("ValorPPPEmpresti" / CASE WHEN "CantidadEmpresti" > 0 THEN "CantidadEmpresti" ELSE 1 END) END AS "PPPEmpresti"
	   ,CASE WHEN "IdClasificacionTipoInstrumento" <> 'RV' AND "IdClasificacionTipoInstrumento" <> 'FM' THEN TO_DECIMAL(0,26,8) ELSE "ValorPPPEmpresti" END 													AS "ValorPPPEmpresti"
	   ,"PrecioMercadoEmpresti"
	   ,"ValorMercadoEmpresti"
	   ,("ValorMercadoEmpresti" - "CantidadEmpresti"*("ValorCompra" / CASE WHEN "CantidadTotal" > 0 THEN ("CantidadTotal"*a."Tamano") ELSE 1 END))			AS "DiferenciaMercadoEmpresti"
	-- GARANTIA
	   ,"CantidadGarantia"
	   ,CASE WHEN "IdClasificacionTipoInstrumento" <> 'RV' AND "IdClasificacionTipoInstrumento" <> 'FM' THEN TO_DECIMAL(0) ELSE ("ValorPPPGarantia" / CASE WHEN "CantidadGarantia" > 0 THEN "CantidadGarantia" ELSE 1 END) END AS "PPPGarantia"
	   ,CASE WHEN "IdClasificacionTipoInstrumento" <> 'RV' AND "IdClasificacionTipoInstrumento" <> 'FM' THEN TO_DECIMAL(0) ELSE "ValorPPPGarantia" END 													AS "ValorPPPGarantia" 
	   ,"PrecioMercadoGarantia"
	   ,"ValorMercadoGarantia"
	   ,("ValorMercadoGarantia" - "CantidadGarantia"*("ValorCompra" / CASE WHEN "CantidadTotal" > 0 THEN ("CantidadTotal"*a."Tamano") ELSE 1 END))			AS "DiferenciaMercadoGarantia"
	-- VENTAS CON PACTO
	   ,"CantidadPactosV"
	   ,CASE WHEN "IdClasificacionTipoInstrumento" <> 'RV' AND "IdClasificacionTipoInstrumento" <> 'FM' THEN TO_DECIMAL(0,26,8) ELSE ("ValorPPPPactosV" / CASE WHEN "CantidadPactosV" > 0 THEN "CantidadPactosV" ELSE 1 END) END AS "PPPPactosV"
	   ,CASE WHEN "IdClasificacionTipoInstrumento" <> 'RV' AND "IdClasificacionTipoInstrumento" <> 'FM' THEN TO_DECIMAL(0,26,8) ELSE "ValorPPPPactosV" END 													AS "ValorPPPPactosV" 
	   ,"PrecioMercadoPactosV"
	   ,"ValorMercadoPactosV"
	   ,("ValorMercadoPactosV" - "CantidadPactosV"*("ValorCompra" / CASE WHEN "CantidadTotal" > 0 THEN ("CantidadTotal"*a."Tamano") ELSE 1 END))				AS "DiferenciaMercadoPactosV"
	-- COMPRAS CON PACTO
	   ,"CantidadPactosC"
	   ,CASE WHEN "IdClasificacionTipoInstrumento" <> 'RV' AND "IdClasificacionTipoInstrumento" <> 'FM' THEN TO_DECIMAL(0,26,8) ELSE ("ValorPPPPactosC" / CASE WHEN "CantidadPactosC" > 0 THEN "CantidadPactosC" ELSE 1 END)	END AS "PPPPactosC"
	   ,CASE WHEN "IdClasificacionTipoInstrumento" <> 'RV' AND "IdClasificacionTipoInstrumento" <> 'FM' THEN TO_DECIMAL(0,26,8) ELSE "ValorPPPPactosC" END 													AS "ValorPPPPactosC"
	   ,"PrecioMercadoPactosC"
	   ,"ValorMercadoPactosC"
	   ,("ValorMercadoPactosC" - "CantidadPactosC"*("ValorCompra" / CASE WHEN "CantidadTotal" > 0 THEN ("CantidadTotal"*a."Tamano") ELSE 1 END))				AS "DiferenciaMercadoPactosC"
	-- TRANSITO INGRESO
	   ,"CantidadTransIng"
	-- TRANSITO INGRESO
	   ,"CantidadTransEgr"
	-- TOTALIZADOR: POSICION
	   ,COALESCE("CantidadCustodia",0) + COALESCE("CantidadGarantia",0) + COALESCE("CantidadSimulCom",0) - ABS(COALESCE("CantidadSimulVen",0)) + COALESCE("CantidadPactosV",0) AS "TotalPosicion"
	-- TOTALIZADOR: POSICION PRESUNTA
	   ,COALESCE("CantidadCustodia",0) - COALESCE("CantidadPrestamo",0) + COALESCE("CantidadGarantia",0)  + COALESCE("CantidadTransIng",0) - ABS(COALESCE("CantidadSimulVen",0)) + ABS(COALESCE("CantidadTransEgr",0)) AS "TotalPosicionPresunta"
	   ,a."IdMatrizAgrupador"
	   ,a."EsADC"
	   ,a."FechaFuentePrecioMercadoCustodia"
	-- FFMM
	   ,a."IdRegimenFondoMutuo"
	FROM :v_SalidaPrevia a WHERE "IdTipoInstrumento" != 'FWD';
	
END;
