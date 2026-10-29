SET SCHEMA "BSCL";
GO

DROP PROCEDURE "BSCL.Custodia.BD.Servicios::ConsultarCustodiaMovimientos";
GO

CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::ConsultarCustodiaMovimientos" (
	
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa							BIGINT,
    IN p_IdUsuario							BIGINT,
    IN p_IdModulo							NVARCHAR(3),
	
    --VARIABLES DEL PROCESO "BSCL.Custodia.BD.Servicios::ConsultarCustodiaMovimientos";
    IN p_FechaInicio						DATE,
	IN p_FechaTermino						DATE,
	IN p_VariablesProceso					TABLE(
												"IdCliente"							BIGINT,
												"IdPortafolio"						BIGINT,
												"FolioCartera" 						BIGINT,
												"IdClasificacionTipoInstrumento" 	NVARCHAR(3),
												"IdTipoInstrumento" 				NVARCHAR(3),
												"IdInstrumento" 					BIGINT
											),
	IN p_IdProductoAgrupador				BIGINT DEFAULT NULL,
	
    --VARIABLES DE SALIDA OBLIGATORIAS
    OUT p_out_Tabla TABLE (
    	 "IdCartera" 							BIGINT
    	,"FechaRegistro" 						TIMESTAMP
    	,"FechaOperacion" 						DATE
    	,"FechaLiquidacion" 					DATE
    	,"IdTipoOperacion"						NVARCHAR(6)    	
    	,"TipoOperacion" 						NVARCHAR(100)
    	,"Accion"								NVARCHAR(15)
    	,"IdCliente" 							BIGINT
    	,"IdPortafolio" 						BIGINT
    	,"CodigoPortafolio" 					NVARCHAR(100)
    	,"NombreCliente" 						NVARCHAR(100)
    	,"DocumentoIdentidad" 					NVARCHAR(40)
    	,"IdConcepto" 							NVARCHAR(6)
    	,"NombreConcepto" 						NVARCHAR(100)
    	,"IdClasificacionTipoInstrumento" 		NVARCHAR(3)
    	,"IdTipoInstrumento" 					NVARCHAR(3)
    	,"IdInstrumento" 						BIGINT
    	,"Nemotecnico" 							NVARCHAR(40)    	
    	,"IdEmisor" 							NVARCHAR(40)
    	,"FechaVencimientoInstr" 				DATE
    	,"IdTipoReajuste" 						NVARCHAR(3)
    	,"NemoSVS" 								NVARCHAR(40)
    	,"IdMonedaInstr" 						NVARCHAR(3)
    	,"IdBoveda" 							NVARCHAR(3)
    	,"Precio"	 							DECIMAL(26,8)
    	,"Tasa"		 							DECIMAL(26,8)
    	,"Monto"	 							DECIMAL(26,8)
    	,"PrecioMercado"						DECIMAL(26,8)
		,"TasaMercado"							DECIMAL(26,8)
		,"MontoMercadoMonedaLocal"				DECIMAL(26,8)
		,"MontoMercadoMonedaInstrumento"		DECIMAL(26,8)
		,"MontoMercadoMonedaAdicional"			DECIMAL(26,8)
    	,"ValorRescate"							DECIMAL(26,8)
    	,"Saldo" 								DECIMAL(26,8)
    	,"Cantidad" 							DECIMAL(26,8)
    	,"CantidadNeg" 							DECIMAL(26,8)
    	,"FolioCartera"							BIGINT
    	,"FolioComprobante"						BIGINT
    	,"FechaComprobante" 					DATE
    	,"IdTipoComprobante"					NVARCHAR(6)
    	,"TipoRegistro" 						NVARCHAR(1)  -- 1 = saldoInicial de la consulta     2=Ingreso  3=Retiro
		,"NroFila" 								BIGINT
		,"ISIN"									NVARCHAR(40)
		,"IdCuentaCustodioPortafolio"			NVARCHAR(40)		
		,"NumeroCuentaBoveda"					NVARCHAR(40)
		,"IdTipoCustodia"						NVARCHAR(40)
		,"NombreTipoCustodia"					NVARCHAR(100)
		,"Producto"								NVARCHAR(100)
		,"IdEstado"                             NVARCHAR(10)
	) DEFAULT EMPTY
)

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"

READS SQL DATA AS
BEGIN
	
	DECLARE v_FechaConsulta 	DATE;
	
	SELECT  ADD_DAYS(p_FechaInicio, -1)
	INTO v_FechaConsulta
	FROM DUMMY;
	
	p_out_tabla =
	SELECT
		0 as "IdCartera"
	   ,'0001-01-01' as "FechaRegistro"
	   ,'0001-01-01' as "FechaOperacion"
	   ,'0001-01-01' as "FechaLiquidacion"
	   ,'' AS "IdTipoOperacion"
	   ,'' as "TipoOperacion"
	   ,'' AS "Accion"
	   ,00 as "IdCliente"
	   ,00 as "IdPortafolio"
	   ,'' as "CodigoPortafolio"
	   ,'' as "NombreCliente"
	   ,'' as "DocumentoIdentidad"
	   ,'' as "IdConcepto"
	   ,'' as "NombreConcepto"
	   ,'' as "IdClasificacionTipoInstrumento"
	   ,'' as "IdTipoInstrumento"
	   ,00 as "IdInstrumento"
	   ,'' as "Nemotecnico"
	   ,'' as "IdEmisor"
	   ,'0001-01-01' as "FechaVencimientoInstr"
	   ,'' as "IdTipoReajuste"
	   ,'' as "NemoSVS"
	   ,'' as "IdMonedaInstr"
	   ,'' as "IdBoveda"
	   ,cast(0 as DECIMAL(26,8)) as "Precio"
	   ,cast(0 as DECIMAL(26,8)) as "Tasa"
	   ,cast(0 as DECIMAL(26,8)) as "Monto"
	   ,cast(0 as DECIMAL(26,8)) as "PrecioMercado"
	   ,cast(0 as DECIMAL(26,8)) as "TasaMercado"
	   ,cast(0 as DECIMAL(26,8)) as "MontoMercadoMonedaLocal"
	   ,cast(0 as DECIMAL(26,8)) as "MontoMercadoMonedaInstrumento"
	   ,cast(0 as DECIMAL(26,8)) as "MontoMercadoMonedaAdicional"
	   ,cast(0 as DECIMAL(26,8)) as "ValorRescate"
	   ,cast(0 as DECIMAL(26,8)) as "Saldo"
	   ,cast(0 as DECIMAL(26,8)) as "Cantidad"
	   ,cast(0 as DECIMAL(26,8)) as "CantidadNeg"
	   ,0 as "FolioCartera"
	   ,0 as "FolioComprobante"
	   ,'0001-01-01' as "FechaComprobante"
	   ,'' as "IdTipoComprobante"
	   ,'' as "TipoRegistro"
	   ,0 as "NroFila"
	   ,'' as "ISIN"
	   ,0 as "IdCuentaCustodioPortafolio"
	   ,'' as "NumeroCuentaBoveda"
	   ,'CUS' as "IdTipoCustodia"
	   ,'CUSTODIA' as "NombreTipoCustodia"
	   ,'' as "Producto"
	   ,'' as "IdEstado" --AE 1
	FROM DUMMY
	WHERE 1 = 2;
	
	
	v_Ingresos =
	WITH
	CarteraOriginal AS
	(
		SELECT
			RANK() over (partition by car."FolioCartera" order by CAR."IdCartera" asc) as "RANK"
		   ,car."IdCartera" as "IdCartera"
		   ,car."FechaIngresoRegistro" as "FechaRegistro"
		   ,car."FechaOperacion" as "FechaOperacion"
		   ,car."INGFechaLiquidacion" as "FechaLiquidacion"
		   ,car."IdTipoOperacion"
		   ,CASE
		   		WHEN car."IdTipoInstrumento" = 'PRT' AND car."IdTipoPrestamo" = 'PRTVTC' THEN car."Nemotecnico"
		   		WHEN car."IdTipoInstrumento" = 'PRT' THEN CONCAT(TipOper."Nombre" , CONCAT(' ', car."Nemotecnico")) 
		   		ELSE TipOper."Nombre" 
		   	END AS "TipoOperacion"
		   ,'INGRESO' as "Accion"
		   ,car."IdCliente" as "IdCliente"
		   ,car."IdPortafolio" as "IdPortafolio"
		   ,car."IdConcepto" as "IdConcepto"
		   ,car."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
		   ,COALESCE(sby."IdTipoInstrumento", car."IdTipoInstrumento") AS "IdTipoInstrumento"
		   ,CASE 
		   		WHEN car."IdTipoInstrumento" = 'PRT' THEN sby."IdInstrumento" 
		   		ELSE car."IdInstrumento"
		   	END AS "IdInstrumento"
		   ,CASE
		   		WHEN car."IdClasificacionTipoInstrumento" = 'IF' THEN car."NemoSVS"
		   		WHEN car."IdTipoInstrumento" = 'PRT' THEN sby."Nemotecnico" 
		   		ELSE car."Nemotecnico"
			END AS "Nemotecnico"
		   ,car."IdEmisor" as "IdEmisor"
		   ,car."FechaVencimientoInstrumento" as "FechaVencimientoInstr"
		   ,car."IdTipoReajuste" as "IdTipoReajuste"
		   ,car."NemoSVS" as "NemoSVS"
		   ,car."IdMonedaInstrumento" as "IdMonedaInstr"
		   ,car."IdBoveda" as "IdBoveda"
		   ,car."PrecioCompra" as "Precio"
		   ,car."TasaCompra" as "Tasa"
		   ,car."MontoCompra"as "Monto"
		   ,car."INGPrecioMercadoActual" AS "PrecioMercado"
		   ,car."INGTasaMercadoActual" AS "TasaMercado"
		   ,car."INGMontoATasaMercadoActual" AS "MontoMercadoMonedaLocal"
		   ,car."INGMontoMercadoActualMonedaInstrumento" AS "MontoMercadoMonedaInstrumento"
		   ,car."INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
		   ,car."ValorRescate" as "ValorRescate"
		   ,cast(0 as DECIMAL(26,8)) as "Saldo"
		   ,case when car."IdClasificacionTipoInstrumento" = 'IF' AND car."EsFungible" = 'S' then car."INGValorRescate" else car."INGCantidad" end as "Cantidad"
		   ,case when car."IdClasificacionTipoInstrumento" = 'IF' AND car."EsFungible" = 'S' then car."INGValorRescate" else car."INGCantidad" end as "CantidadNeg"
		   ,car."FolioCartera"
		   ,car."FolioComprobante"
		   ,car."FechaComprobante"
		   ,car."IdTipoComprobante"
		   ,'2' as "TipoRegistro"
		   ,0 as "NroFila"
		   ,car."ISIN"
		   ,car."IdCuentaCustodioPortafolio"
		   ,'' as "NumeroCuentaBoveda"
		   ,'CUS' as "IdTipoCustodia"
		   ,'CUSTODIA' as "NombreTipoCustodia"
		   ,ProductoAgrupador."NombreProductoAgrupador" AS "Producto"
		   ,CAR."IdEstado" as "IdEstado" --AE 2
		FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
			LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (CAR."FolioCartera" = ultSub."FolioCarteraRelacionada")
			LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on (sby."IdSubyacente" = ultSub."IdSubyacente")
		
		INNER JOIN :p_VariablesProceso AS VP ON
		(
				CAR."IdCliente" 		= case when VP."IdCliente" > 0 then VP."IdCliente" else car."IdCliente" END
			AND CAR."IdPortafolio" 		= case when VP."IdPortafolio" > 0 then VP."IdPortafolio" else car."IdPortafolio" END
			AND CAR."FolioCartera"		= case when VP."FolioCartera" > 0 then VP."FolioCartera" else car."FolioCartera" END
			AND CAR."IdClasificacionTipoInstrumento" = case when VP."IdClasificacionTipoInstrumento" <> '' then VP."IdClasificacionTipoInstrumento" else car."IdClasificacionTipoInstrumento" END
			AND COALESCE(sby."IdTipoInstrumento", car."IdTipoInstrumento") = CASE WHEN VP."IdTipoInstrumento" <> '' then VP."IdTipoInstrumento" ELSE COALESCE(sby."IdTipoInstrumento", car."IdTipoInstrumento") END
			AND COALESCE(sby."IdInstrumento",car."IdInstrumento") 	= case when VP."IdInstrumento" > 0 then VP."IdInstrumento" else COALESCE(sby."IdInstrumento",car."IdInstrumento") end
		)
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" As TipOper ON TipOper."IdTipoOperacion" = car."IdTipoOperacion"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
		WHERE
			CAR."IdEmpresa" 			= p_IdEmpresa
--		AND CAR."IdTipoInstrumento" 	NOT IN ('PRT','EMP','SIM','RRV','RRF')
		AND CAR."IdTipoInstrumento" 	NOT IN ('SIM','RRV','RRF','PAC')
		AND CAR."INGFechaLiquidacion" 	>= p_FechaInicio
		AND CAR."INGFechaLiquidacion" 	<= p_FechaTermino
		AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
	)
	SELECT
		"IdCartera","FechaRegistro","FechaOperacion","FechaLiquidacion","IdTipoOperacion","TipoOperacion","Accion","IdCliente","IdPortafolio","IdConcepto","IdClasificacionTipoInstrumento"
	   ,"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor" ,"FechaVencimientoInstr","IdTipoReajuste","NemoSVS" ,"IdMonedaInstr", "IdBoveda" ,"Precio","Tasa","Monto","PrecioMercado"
	   ,"TasaMercado","MontoMercadoMonedaLocal"	,"MontoMercadoMonedaInstrumento","MontoMercadoMonedaAdicional","ValorRescate","Saldo","Cantidad","CantidadNeg","FolioCartera","FolioComprobante"
	   ,"FechaComprobante","IdTipoComprobante","TipoRegistro","NroFila","ISIN","IdCuentaCustodioPortafolio","NumeroCuentaBoveda","IdTipoCustodia","NombreTipoCustodia","Producto","IdEstado"
	FROM CarteraOriginal
	WHERE "RANK" = 1;	
	
	
	v_Anulaciones =
	WITH
	ingresos AS
	(
		SELECT
--			RANK() over (partition by car."FolioCartera" order by CAR."IdCartera" asc) as "RANK"
		    car."IdCartera" as "IdCartera"
		   ,ADD_SECONDS(CAR."FechaIngresoRegistro",3) as "FechaRegistro"
		   ,car."FechaOperacion" as "FechaOperacion"
		   ,car."INGFechaLiquidacion" as "FechaLiquidacion"
		   ,CASE WHEN IFNULL(car."EGRIdTipoOperacion",'') <> '' THEN IFNULL(car."EGRIdTipoOperacion",'') ELSE car."IdTipoOperacion" END AS "IdTipoOperacion"
		   ,CASE
		   		WHEN car."IdTipoInstrumento" = 'PRT' AND car."IdTipoPrestamo" = 'PRTVTC' THEN car."Nemotecnico"
		   		WHEN car."IdTipoInstrumento" = 'PRT' THEN CONCAT(TipOper."Nombre" , CONCAT(' ', car."Nemotecnico")) 
		   		ELSE TipOper."Nombre" 
		   	END AS "TipoOperacion"
		   ,'ANULACION' as "Accion"
		   ,car."IdCliente" as "IdCliente"
		   ,car."IdPortafolio" as "IdPortafolio"
		   ,CASE WHEN IFNULL(car."EGRIdConcepto",'') <> '' THEN IFNULL(car."EGRIdConcepto",'') ELSE car."EGRIdConcepto" END as "IdConcepto"
		   ,car."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
		   ,COALESCE(sby."IdTipoInstrumento", car."IdTipoInstrumento") AS "IdTipoInstrumento"
		   ,CASE 
		   		WHEN car."IdTipoInstrumento" = 'PRT' THEN sby."IdInstrumento" 
		   		ELSE car."IdInstrumento"
		   	END AS "IdInstrumento"
		   ,CASE
		   		WHEN car."IdClasificacionTipoInstrumento" = 'IF' THEN car."NemoSVS"
		   		WHEN car."IdTipoInstrumento" = 'PRT' THEN sby."Nemotecnico" 
		   		ELSE car."Nemotecnico"
			END AS "Nemotecnico"
		   ,car."IdEmisor" as "IdEmisor"
		   ,car."FechaVencimientoInstrumento" as "FechaVencimientoInstr"
		   ,car."IdTipoReajuste" as "IdTipoReajuste"
		   ,car."NemoSVS" as "NemoSVS"
		   ,car."IdMonedaInstrumento" as "IdMonedaInstr"
		   ,car."IdBoveda" as "IdBoveda"
		   ,car."PrecioCompra" as "Precio"
		   ,car."TasaCompra" as "Tasa"
		   ,car."MontoCompra"as "Monto"
		   ,car."INGPrecioMercadoActual" AS "PrecioMercado"
		   ,car."INGTasaMercadoActual" AS "TasaMercado"
		   ,car."INGMontoATasaMercadoActual" AS "MontoMercadoMonedaLocal"
		   ,car."INGMontoMercadoActualMonedaInstrumento" AS "MontoMercadoMonedaInstrumento"
		   ,car."INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
		   ,car."ValorRescate" as "ValorRescate"
		   ,cast(0 as DECIMAL(26,8)) as "Saldo"
		   ,case when car."IdClasificacionTipoInstrumento" = 'IF' AND car."EsFungible" = 'S' then car."INGValorRescate" else car."INGCantidad" end as "Cantidad"
		   ,case when car."IdClasificacionTipoInstrumento" = 'IF' AND car."EsFungible" = 'S' then car."INGValorRescate" else car."INGCantidad" end as "CantidadNeg"
		   ,car."FolioCartera"
		   ,car."EGRFolioComprobante" AS "FolioComprobante"
		   ,car."EGRFechaComprobante" AS "FechaComprobante" 
		   ,car."EGRIdTipoComprobante" AS "IdTipoComprobante"
		   ,'4' as "TipoRegistro"
		   ,0 as "NroFila"
		   ,car."ISIN"
		   ,car."IdCuentaCustodioPortafolio"
		   ,'' as "NumeroCuentaBoveda"
		   ,'CUS' as "IdTipoCustodia"
		   ,'CUSTODIA' as "NombreTipoCustodia"
		   ,ProductoAgrupador."NombreProductoAgrupador" AS "Producto"
		   ,CAR."IdEstado" as "IdEstado" --AE 3
		FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
			LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (CAR."FolioCartera" = ultSub."FolioCarteraRelacionada")
			LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on (sby."IdSubyacente" = ultSub."IdSubyacente")
			--LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" regAnt ON regAnt."IdCartera" = LAG(car."IdCartera") OVER (PARTITION BY car."IdEmpresa", car."FolioCartera", car."IdCartera" ORDER BY car."IdEmpresa", car."FolioCartera", car."IdCartera") 
		
		INNER JOIN :p_VariablesProceso AS VP ON
		(
				CAR."IdCliente" 		= case when VP."IdCliente" > 0 then VP."IdCliente" else car."IdCliente" END
			AND CAR."IdPortafolio" 		= case when VP."IdPortafolio" > 0 then VP."IdPortafolio" else car."IdPortafolio" END
			AND CAR."FolioCartera"		= case when VP."FolioCartera" > 0 then VP."FolioCartera" else car."FolioCartera" END
			AND CAR."IdClasificacionTipoInstrumento" = case when VP."IdClasificacionTipoInstrumento" <> '' then VP."IdClasificacionTipoInstrumento" else car."IdClasificacionTipoInstrumento" END
			AND COALESCE(sby."IdTipoInstrumento", car."IdTipoInstrumento") = CASE WHEN VP."IdTipoInstrumento" <> '' then VP."IdTipoInstrumento" ELSE COALESCE(sby."IdTipoInstrumento", car."IdTipoInstrumento") END
			AND COALESCE(sby."IdInstrumento",car."IdInstrumento") 	= case when VP."IdInstrumento" > 0 then VP."IdInstrumento" else COALESCE(sby."IdInstrumento",car."IdInstrumento") end
		)
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" As TipOper ON TipOper."IdTipoOperacion" = CASE WHEN IFNULL(car."EGRIdTipoOperacion",'') <> '' THEN car."EGRIdTipoOperacion" ELSE car."IdTipoOperacion" END  
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
		WHERE
			CAR."IdEmpresa" 			= p_IdEmpresa
		AND CAR."IdEstado" 				= 'ANU' --AND CAR."INGCantidad" = CAR."EGRCantidad"
--		AND CAR."IdTipoInstrumento" 	NOT IN ('PRT','EMP','SIM','RRV','RRF')
		AND CAR."IdTipoInstrumento" 	NOT IN ('SIM','RRV','RRF')
		AND CAR."INGFechaOperacion" 	>= p_FechaInicio
		AND CAR."INGFechaOperacion" 	<= p_FechaTermino
		AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
	)
	SELECT
		"IdCartera","FechaRegistro","FechaOperacion","FechaLiquidacion","IdTipoOperacion","TipoOperacion","Accion","IdCliente","IdPortafolio","IdConcepto","IdClasificacionTipoInstrumento"
	   ,"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor" ,"FechaVencimientoInstr","IdTipoReajuste","NemoSVS" ,"IdMonedaInstr", "IdBoveda" ,"Precio","Tasa","Monto","PrecioMercado"
	   ,"TasaMercado","MontoMercadoMonedaLocal"	,"MontoMercadoMonedaInstrumento","MontoMercadoMonedaAdicional","ValorRescate","Saldo","Cantidad","CantidadNeg","FolioCartera","FolioComprobante"
	   ,"FechaComprobante","IdTipoComprobante","TipoRegistro","NroFila","ISIN","IdCuentaCustodioPortafolio","NumeroCuentaBoveda","IdTipoCustodia","NombreTipoCustodia","Producto","IdEstado"
	FROM ingresos;	
	
	
	v_Retiros =
	SELECT
		car."IdCartera" as "IdCartera"
	   ,car."EGRFechaLiquidacion" as "FechaRegistro"
	   ,car."EGRFechaOperacion" as "FechaOperacion"
	   ,car."EGRFechaLiquidacion" as "FechaLiquidacion"
	   ,car."EGRIdTipoOperacion" AS "IdTipoOperacion"
	   , CASE
		 	WHEN car."IdTipoInstrumento" = 'PRT' AND car."IdTipoPrestamo" = 'PRTVTC' THEN car."Nemotecnico"
		 	WHEN car."IdTipoInstrumento" = 'PRT' THEN CONCAT(TipOper."Nombre" , CONCAT(' ', car."Nemotecnico")) 
		 	ELSE TipOper."Nombre" 
		 END AS "TipoOperacion"

	   ,'RETIRO' as "Accion"
	   ,car."IdCliente" as "IdCliente"
	   ,car."IdPortafolio" as "IdPortafolio"
	   ,car."EGRIdConcepto" as "IdConcepto"
	   ,car."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
	   ,COALESCE(sby."IdTipoInstrumento", car."IdTipoInstrumento") AS "IdTipoInstrumento"
	   ,CASE 
		   		WHEN car."IdTipoInstrumento" = 'PRT' THEN sby."IdInstrumento" 
		   		ELSE car."IdInstrumento"
		END AS "IdInstrumento"
	   ,CASE
	   		WHEN car."IdClasificacionTipoInstrumento" = 'IF' THEN car."NemoSVS"
		   	WHEN car."IdTipoInstrumento" = 'PRT' THEN sby."Nemotecnico" 
		   	ELSE car."Nemotecnico"
		END AS "Nemotecnico"
	   ,car."IdEmisor" as "IdEmisor"
	   ,car."FechaVencimientoInstrumento" as "FechaVencimientoInstr"
	   ,car."IdTipoReajuste" as "IdTipoReajuste"
	   ,car."NemoSVS" as "NemoSVS"
	   ,car."IdMonedaInstrumento" as "IdMonedaInstr"
	   ,car."IdBoveda" as "IdBoveda"
	   ,car."EGRPrecio" as "Precio"
	   ,car."EGRTasa" as "Tasa"
	   ,car."EGRMonto"as "Monto"
	   ,car."EGRPrecioMercado" AS "PrecioMercado"
	   ,car."EGRTasaMercado" AS "TasaMercado"
	   ,car."EGRMontoATasaMercado" AS "MontoMercadoMonedaLocal"
	   ,car."EGRMontoMercadoMonedaInstrumento" AS "MontoMercadoMonedaInstrumento"
	   ,car."EGRMontoMercadoMonedaAdicional" AS "MontoMercadoMonedaAdicional"
	   ,car."EGRValorRescate" as "ValorRescate"
	   ,cast(0 as DECIMAL(26,8)) as "Saldo"
	   ,case when car."IdClasificacionTipoInstrumento" = 'IF' AND car."EsFungible" = 'S' then car."EGRValorRescate" else car."EGRCantidad" END as "Cantidad"
	   --,cast((case when car."IdClasificacionTipoInstrumento" = 'IF' AND car."EsFungible" = 'S' then car."EGRValorRescate" else car."EGRCantidad" END*CASE WHEN TipOper."AccionCustodia" = 'INGRESO' THEN -1 ELSE 1 END) as DECIMAL(26,8)) as "CantidadNeg" 						 						
	   ,cast((case when car."IdClasificacionTipoInstrumento" = 'IF' AND car."EsFungible" = 'S' then car."EGRValorRescate" else car."EGRCantidad" END*-1) as DECIMAL(26,8)) as "CantidadNeg"
	   ,car."FolioCartera" as "FolioCartera"
	   ,car."EGRFolioComprobante" AS "FolioComprobante"
	   ,car."EGRFechaComprobante" as "FechaComprobante"
	   ,car."EGRIdTipoComprobante" as "IdTipoComprobante"
	   ,'2' as "TipoRegistro"
	   ,0 as "NroFila"
	   ,car."ISIN"
	   ,car."IdCuentaCustodioPortafolio"
	   ,'' as "NumeroCuentaBoveda"
	   ,'CUS' as "IdTipoCustodia"
	   ,'CUSTODIA' as "NombreTipoCustodia"
	   ,ProductoAgrupador."NombreProductoAgrupador" AS "Producto"
	   ,CAR."IdEstado" as "IdEstado" --AE 4
	FROM "BSCL.Custodia.BD::CUS.Cartera" as CAR
			LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (CAR."FolioCartera" = ultSub."FolioCarteraRelacionada")
			LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on (sby."IdSubyacente" = ultSub."IdSubyacente")

	
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" As TipOper ON TipOper."IdTipoOperacion" = car."EGRIdTipoOperacion"
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
	LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
	INNER JOIN :p_VariablesProceso AS VP ON
	(
			CAR."IdCliente" 		= case when VP."IdCliente" > 0 then VP."IdCliente" else car."IdCliente" END
		AND CAR."IdPortafolio" 		= case when VP."IdPortafolio" > 0 then VP."IdPortafolio" else car."IdPortafolio" END
		AND CAR."FolioCartera"		= case when VP."FolioCartera" > 0 then VP."FolioCartera" else car."FolioCartera" END
		AND CAR."IdClasificacionTipoInstrumento" = case when VP."IdClasificacionTipoInstrumento" <> '' then VP."IdClasificacionTipoInstrumento" else car."IdClasificacionTipoInstrumento" END
		AND COALESCE(sby."IdTipoInstrumento", car."IdTipoInstrumento") = CASE WHEN VP."IdTipoInstrumento" <> '' then VP."IdTipoInstrumento" ELSE COALESCE(sby."IdTipoInstrumento", car."IdTipoInstrumento") END
		AND COALESCE(sby."IdInstrumento",car."IdInstrumento") 	= case when VP."IdInstrumento" > 0 then VP."IdInstrumento" else COALESCE(sby."IdInstrumento",car."IdInstrumento") end
	)
	WHERE
		car."IdEmpresa" = p_IdEmpresa
--	AND car."IdTipoInstrumento" NOT IN ('PRT','EMP','SIM','RRV','RRF')
	AND car."IdTipoInstrumento" NOT IN ('SIM','RRV','RRF','PAC')
	AND car."EGRCantidad" <> 0
	AND car."EGRRetiroPorFusion" = 'N'
	AND car."EGRFechaLiquidacion" >= p_FechaInicio
	AND car."EGRFechaLiquidacion" <= p_FechaTermino
	AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador);
	
	
	v_RetirosGarantia =
	WITH
	Garantias AS
	(
		SELECT
			car."IdCartera" as "IdCartera"
		   ,gar."EGRFechaLiquidacion" as "FechaRegistro"
		   ,car."EGRFechaOperacion" as "FechaOperacion"
		   ,car."EGRFechaLiquidacion" as "FechaLiquidacion"
		   ,car."IdTipoOperacion"
		   ,TipOper."Nombre" AS "TipoOperacion"
		   ,car."IdCliente" as "IdCliente"
		   ,car."IdPortafolio" as "IdPortafolio"
		   ,gar."EGRIdConcepto" as "IdConcepto"
		   ,car."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
		   ,car."IdTipoInstrumento" as "IdTipoInstrumento"
		   ,car."IdInstrumento" as "IdInstrumento"
		   ,CASE
		   		WHEN car."IdClasificacionTipoInstrumento" = 'IF' THEN car."NemoSVS"
		   		ELSE car."Nemotecnico"
			END AS "Nemotecnico"
		   ,car."IdEmisor" as "IdEmisor"
		   ,car."FechaVencimientoInstrumento" as "FechaVencimientoInstr"
		   ,car."IdTipoReajuste" as "IdTipoReajuste"
		   ,car."NemoSVS" as "NemoSVS"
		   ,car."IdMonedaInstrumento" as "IdMonedaInstr"
		   ,car."IdBoveda" as "IdBoveda"
		   ,gar."EGRPrecio" as "Precio"
		   ,gar."EGRTasa" as "Tasa"
		   ,gar."EGRMonto"as "Monto"
		   ,gar."EGRPrecio" AS "PrecioMercado"
		   ,gar."EGRTasa" AS "TasaMercado"
		   ,gar."EGRMonto" AS "MontoMercadoMonedaLocal"
		   ,gar."EGRMontoMonedaInstrumento" AS "MontoMercadoMonedaInstrumento"
		   ,gar."EGRMontoMonedaAdicional" AS "MontoMercadoMonedaAdicional"
		   ,gar."EGRValorRescate" as "ValorRescate"
		   ,0 AS "Saldo"
		   ,case when car."IdClasificacionTipoInstrumento" = 'IF' AND car."EsFungible" = 'S' then gar."EGRValorRescate" else gar."EGRCantidad" END as "Cantidad"
		   --,cast((case when car."IdClasificacionTipoInstrumento" = 'IF' AND car."EsFungible" = 'S' then gar."EGRValorRescate" else gar."EGRCantidad" END*CASE WHEN TipOper."AccionCustodia" = 'INGRESO' THEN -1 ELSE 1 END) as DECIMAL(26,8)) as "CantidadNeg"
		   ,cast((case when car."IdClasificacionTipoInstrumento" = 'IF' AND car."EsFungible" = 'S' then gar."EGRValorRescate" else gar."EGRCantidad" END) as DECIMAL(26,8)) as "CantidadNeg"
		   ,car."FolioCartera" as "FolioCartera"
		   ,gar."EGRFolioComprobante" as "FolioComprobante"
		   ,gar."EGRFechaComprobante" as "FechaComprobante"
		   ,gar."EGRIdTipoComprobante" as "IdTipoComprobante"
		   ,'2' as "TipoRegistro"
		   ,0 as "NroFila"
		   ,car."ISIN"
		   ,car."IdCuentaCustodioPortafolio"
		   ,'' as "NumeroCuentaBoveda"
		   ,'CUS' as "IdTipoCustodia"
		   ,ProductoAgrupador."NombreProductoAgrupador" AS "Producto"
		  ,CAR."IdEstado" as "IdEstado" --AE 5
		FROM "BSCL.Custodia.BD::CUS.Cartera" as CAR
		INNER JOIN "BSCL.Custodia.BD::GTA.Garantia" as GAR ON GAR."IdGarantia" = CAR."EGRIdOperacion"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" As TipOper ON TipOper."IdTipoOperacion" = GAR."EGRIdTipoOperacion"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
		INNER JOIN :p_VariablesProceso AS VP ON
		(
				CAR."IdCliente" 		= case when VP."IdCliente" > 0 then VP."IdCliente" else car."IdCliente" END
			and CAR."IdPortafolio" 		= case when VP."IdPortafolio" > 0 then VP."IdPortafolio" else car."IdPortafolio" END
			and CAR."FolioCartera"		= case when VP."FolioCartera" > 0 then VP."FolioCartera" else car."FolioCartera" END
			and CAR."IdClasificacionTipoInstrumento" = case when VP."IdClasificacionTipoInstrumento" <> '' then VP."IdClasificacionTipoInstrumento" else car."IdClasificacionTipoInstrumento" END
			and CAR."IdTipoInstrumento" = case when VP."IdTipoInstrumento" <> '' then VP."IdTipoInstrumento" else car."IdTipoInstrumento" END
			and CAR."IdInstrumento" 	= case when VP."IdInstrumento" > 0 then VP."IdInstrumento" else car."IdInstrumento" end
		)
		WHERE
			car."IdEmpresa" = p_IdEmpresa
		AND car."EGRRetiroPorFusion" = 'S'
		AND car."IdTipoInstrumento" NOT IN ('PRT','EMP','SIM','RRV','RRF')
		and gar."EGRFechaLiquidacion" >= p_FechaInicio
		and gar."EGRFechaLiquidacion" <= p_FechaTermino
		AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
	)
	SELECT
		"IdCartera","FechaRegistro","FechaOperacion","FechaLiquidacion","IdTipoOperacion","TipoOperacion",'INGRESO' AS "Accion" ,"IdCliente","IdPortafolio","IdConcepto","IdClasificacionTipoInstrumento"
	   ,"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor" ,"FechaVencimientoInstr","IdTipoReajuste","NemoSVS" ,"IdMonedaInstr", "IdBoveda" ,"Precio","Tasa","Monto","PrecioMercado"
	   ,"TasaMercado","MontoMercadoMonedaLocal"	,"MontoMercadoMonedaInstrumento","MontoMercadoMonedaAdicional","ValorRescate","Saldo","Cantidad","CantidadNeg","FolioCartera","FolioComprobante"
	   ,"FechaComprobante","IdTipoComprobante","TipoRegistro","NroFila","ISIN","IdCuentaCustodioPortafolio","NumeroCuentaBoveda","IdTipoCustodia",'CUSTODIA' AS "NombreTipoCustodia","Producto","IdEstado"
	FROM Garantias;
	
	
	v_Nominales =
	SELECT
		0 as "IdCartera"
	   ,mov."FechaCertificacionRegistro" as "FechaRegistro"
	   ,mov."FechaOperacion" as "FechaOperacion"
	   ,mov."FechaCertificacionRegistro" as "FechaLiquidacion"
	   ,mov."IdTipoOperacion"
	   ,mov."IndicadorIngEgr" as "TipoOperacion"
	   ,mov."IndicadorIngEgr" as "Accion"
	   ,mov."IdCliente"
	   ,mov."IdPortafolio"
	   ,mov."CodigoPortafolio"
	   ,mov."NombreCliente"
	   ,mov."DocumentoIdentidad"
	   ,mov."IdConcepto" as "IdConcepto"
	   ,mov."NombreConcepto"
	   ,mov."IdClasificacionTipoInstrumento"
	   ,mov."IdTipoInstrumento"
	   ,mov."IdInstrumento"
	   ,mov."Nemotecnico"
	   ,mov."IdEmisor"
	   ,mov."FechaVencimientoInstrumento" as "FechaVencimientoInstr"
	   ,mov."IdTipoReajuste" as "IdTipoReajuste"
	   ,mov."NemoSVS" as "NemoSVS"
	   ,mov."IdMonedaInstrumento" as "IdMonedaInstr"
	   ,mov."IdBoveda" as "IdBoveda"
	   ,mov."PrecioCompra" as "Precio"
	   ,mov."TasaCompra" as "Tasa"
	   ,mov."MontoCompra" as "Monto"
	   ,0 AS "PrecioMercado"
	   ,0 AS "TasaMercado"
	   ,0 AS "MontoMercadoMonedaLocal"
	   ,0 AS "MontoMercadoMonedaInstrumento"
	   ,0 AS "MontoMercadoMonedaAdicional"
	   ,mov."ValorRescate" as "ValorRescate"
	   ,cast(0 as DECIMAL(26,8)) as "Saldo"
	   ,cast(case when mov."IdClasificacionTipoInstrumento" = 'IF' AND mov."EsFungible" = 'S' then mov."ValorRescate" else mov."Cantidad" END as DECIMAL(26,8)) as "Cantidad"
	   ,cast(case when mov."IdClasificacionTipoInstrumento" = 'IF' AND mov."EsFungible" = 'S' then mov."ValorRescate" else mov."Cantidad" END as DECIMAL(26,8)) as "CantidadNeg"
	   ,0 as "FolioCartera"
	   ,mov."FolioComprobante" as "FolioComprobante"
	   ,CASE WHEN IFNULL(mov."FolioComprobante",0) > 0 THEN mov."FechaOperacion" ELSE NULL END as "FechaComprobante"
	   ,CASE WHEN IFNULL(mov."FolioComprobante",0) > 0 THEN mov."IdTipoComprobante" ELSE '' END as "IdTipoComprobante"
	   ,'2' as "TipoRegistro"
	   ,0 as "NroFila"
	   ,'' AS "ISIN"
	   ,0 AS "IdCuentaCustodioPortafolio"
	   ,'' as "NumeroCuentaBoveda"
	   ,CASE WHEN ifnull(mov."EsNominal",'N') = 'S' THEN 'NOM' ELSE 'CUS' END as "IdTipoCustodia"
	   ,CASE WHEN ifnull(mov."EsNominal",'N') = 'S' THEN 'NOMINAL' ELSE 'CUSTODIA' END as "NombreTipoCustodia"
	   ,ProductoAgrupador."NombreProductoAgrupador" AS "Producto"
	   ,mov."IdEstado" as "IdEstado" --AE 6
	FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEnTransito" AS mov
	INNER JOIN :p_VariablesProceso AS VP ON
	(
			mov."IdCliente" 		= case when VP."IdCliente" > 0 then VP."IdCliente" else mov."IdCliente" END
		AND mov."IdPortafolio" 		= case when VP."IdPortafolio" > 0 then VP."IdPortafolio" else mov."IdPortafolio" END
		AND mov."IdClasificacionTipoInstrumento" = case when VP."IdClasificacionTipoInstrumento" <> '' then VP."IdClasificacionTipoInstrumento" else mov."IdClasificacionTipoInstrumento" END
		AND mov."IdTipoInstrumento" = case when VP."IdTipoInstrumento" <> '' then VP."IdTipoInstrumento" else mov."IdTipoInstrumento" END
		AND mov."IdInstrumento" 	= case when VP."IdInstrumento" > 0 then VP."IdInstrumento" else mov."IdInstrumento" END
	)
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio" AND Portafolio."IdCliente" = MOV."IdCliente"
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
	LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
	WHERE
		mov."IdEmpresa" = p_IdEmpresa
	AND mov."FechaOperacion" >= p_FechaInicio
	AND mov."FechaOperacion" <= p_FechaTermino
	AND mov."IdEstado" = 'CRT'
	AND ifnull(mov."EsNominal",'N') = 'S'
	AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador);
	
	
	--//////////////////////////////////////////////////////////////////////////////////////////////////////
	p_out_Tabla =
	SELECT
		"IdCartera","FechaRegistro","FechaOperacion","FechaLiquidacion","IdTipoOperacion","TipoOperacion","Accion","IdCliente","IdPortafolio",'' as "CodigoPortafolio",'' as "NombreCliente",'' as "DocumentoIdentidad"
	   ,"IdConcepto",'' as "NombreConcepto","IdClasificacionTipoInstrumento","IdTipoInstrumento", "IdInstrumento","Nemotecnico","IdEmisor" ,"FechaVencimientoInstr","IdTipoReajuste", "NemoSVS" ,"IdMonedaInstr","IdBoveda","Precio","Tasa"
	   ,"Monto","PrecioMercado","TasaMercado","MontoMercadoMonedaLocal", "MontoMercadoMonedaInstrumento","MontoMercadoMonedaAdicional","ValorRescate","Saldo","Cantidad","CantidadNeg", "FolioCartera"	,"FolioComprobante"
	   ,"FechaComprobante","IdTipoComprobante","TipoRegistro","NroFila","ISIN","IdCuentaCustodioPortafolio","NumeroCuentaBoveda","IdTipoCustodia","NombreTipoCustodia","Producto", "IdEstado"
	FROM :p_out_Tabla
	UNION ALL
	
	SELECT
		"IdCartera","FechaRegistro","FechaOperacion","FechaLiquidacion","IdTipoOperacion","TipoOperacion","Accion","IdCliente","IdPortafolio",'' as "CodigoPortafolio",'' as "NombreCliente",'' as "DocumentoIdentidad"
	   ,"IdConcepto",'' as "NombreConcepto","IdClasificacionTipoInstrumento","IdTipoInstrumento", "IdInstrumento","Nemotecnico","IdEmisor" ,"FechaVencimientoInstr","IdTipoReajuste", "NemoSVS" ,"IdMonedaInstr","IdBoveda","Precio","Tasa"
	   ,"Monto","PrecioMercado","TasaMercado","MontoMercadoMonedaLocal", "MontoMercadoMonedaInstrumento","MontoMercadoMonedaAdicional","ValorRescate","Saldo","Cantidad","CantidadNeg", "FolioCartera"	,"FolioComprobante"
	   ,"FechaComprobante","IdTipoComprobante","TipoRegistro","NroFila","ISIN","IdCuentaCustodioPortafolio","NumeroCuentaBoveda","IdTipoCustodia","NombreTipoCustodia","Producto", "IdEstado"
	FROM :v_Ingresos
	UNION ALL
	
	SELECT
		"IdCartera","FechaRegistro","FechaOperacion","FechaLiquidacion","IdTipoOperacion","TipoOperacion","Accion","IdCliente","IdPortafolio",'' as "CodigoPortafolio",'' as "NombreCliente",'' as "DocumentoIdentidad"
	   ,"IdConcepto",'' as "NombreConcepto","IdClasificacionTipoInstrumento","IdTipoInstrumento", "IdInstrumento","Nemotecnico","IdEmisor" ,"FechaVencimientoInstr","IdTipoReajuste", "NemoSVS" ,"IdMonedaInstr","IdBoveda","Precio","Tasa"
	   ,"Monto","PrecioMercado","TasaMercado","MontoMercadoMonedaLocal", "MontoMercadoMonedaInstrumento","MontoMercadoMonedaAdicional","ValorRescate","Saldo","Cantidad","CantidadNeg", "FolioCartera"	,"FolioComprobante"
	   ,"FechaComprobante","IdTipoComprobante","TipoRegistro","NroFila","ISIN","IdCuentaCustodioPortafolio","NumeroCuentaBoveda","IdTipoCustodia","NombreTipoCustodia","Producto", "IdEstado"
	FROM :v_Anulaciones
	UNION ALL
	
	SELECT
		"IdCartera","FechaRegistro","FechaOperacion","FechaLiquidacion","IdTipoOperacion","TipoOperacion","Accion","IdCliente","IdPortafolio",'' as "CodigoPortafolio",'' as "NombreCliente",'' as "DocumentoIdentidad"
	   ,"IdConcepto",'' as "NombreConcepto","IdClasificacionTipoInstrumento","IdTipoInstrumento", "IdInstrumento","Nemotecnico","IdEmisor" ,"FechaVencimientoInstr","IdTipoReajuste", "NemoSVS" ,"IdMonedaInstr","IdBoveda","Precio","Tasa"
	   ,"Monto","PrecioMercado","TasaMercado","MontoMercadoMonedaLocal", "MontoMercadoMonedaInstrumento","MontoMercadoMonedaAdicional","ValorRescate","Saldo","Cantidad","CantidadNeg", "FolioCartera"	,"FolioComprobante"
	   ,"FechaComprobante","IdTipoComprobante","TipoRegistro","NroFila","ISIN","IdCuentaCustodioPortafolio","NumeroCuentaBoveda","IdTipoCustodia","NombreTipoCustodia","Producto", "IdEstado"
	FROM :v_retiros
	UNION ALL
	
	SELECT
		"IdCartera","FechaRegistro","FechaOperacion","FechaLiquidacion","IdTipoOperacion","TipoOperacion","Accion","IdCliente","IdPortafolio",'' as "CodigoPortafolio",'' as "NombreCliente",'' as "DocumentoIdentidad"
	   ,"IdConcepto",'' as "NombreConcepto","IdClasificacionTipoInstrumento","IdTipoInstrumento", "IdInstrumento","Nemotecnico","IdEmisor" ,"FechaVencimientoInstr","IdTipoReajuste", "NemoSVS" ,"IdMonedaInstr","IdBoveda","Precio","Tasa"
	   ,"Monto","PrecioMercado","TasaMercado","MontoMercadoMonedaLocal", "MontoMercadoMonedaInstrumento","MontoMercadoMonedaAdicional","ValorRescate","Saldo","Cantidad","CantidadNeg", "FolioCartera"	,"FolioComprobante"
	   ,"FechaComprobante","IdTipoComprobante","TipoRegistro","NroFila","ISIN","IdCuentaCustodioPortafolio","NumeroCuentaBoveda","IdTipoCustodia","NombreTipoCustodia","Producto", "IdEstado"
	FROM :v_RetirosGarantia
	UNION ALL			
	
	SELECT
		"IdCartera","FechaRegistro","FechaOperacion","FechaLiquidacion","IdTipoOperacion","TipoOperacion","Accion","IdCliente","IdPortafolio",'' as "CodigoPortafolio",'' as "NombreCliente",'' as "DocumentoIdentidad"
	   ,"IdConcepto",'' as "NombreConcepto","IdClasificacionTipoInstrumento","IdTipoInstrumento", "IdInstrumento","Nemotecnico","IdEmisor" ,"FechaVencimientoInstr","IdTipoReajuste", "NemoSVS" ,"IdMonedaInstr","IdBoveda","Precio","Tasa"
	   ,"Monto","PrecioMercado","TasaMercado","MontoMercadoMonedaLocal", "MontoMercadoMonedaInstrumento","MontoMercadoMonedaAdicional","ValorRescate","Saldo","Cantidad","CantidadNeg", "FolioCartera"	,"FolioComprobante"
	   ,"FechaComprobante","IdTipoComprobante","TipoRegistro","NroFila","ISIN","IdCuentaCustodioPortafolio","NumeroCuentaBoveda","IdTipoCustodia","NombreTipoCustodia","Producto", "IdEstado"
	FROM :v_Nominales;
	
	
	p_out_Tabla = 
	WITH
	Informacion AS
	(
		SELECT
			ROW_NUMBER() over (order BY a."DocumentoIdentidad", a."IdPortafolio", a."FechaRegistro" desc, a."IdCartera" desc) AS ROWNUMBER
		   ,a."IdCartera"
		   ,a."FechaRegistro"
		   ,a."FechaOperacion"
		   ,a."FechaLiquidacion"
		   ,a."IdTipoOperacion"
		   ,a."TipoOperacion"
		   ,a."Accion"
		   ,a."IdCliente"
		   ,a."IdPortafolio"
		   ,Port."CodigoPortafolio"
		   ,cli."RazonSocial" as "NombreCliente"
		   ,cli."DocumentoIdentidad" as "DocumentoIdentidad"
		   ,a."IdConcepto"
		   ,Concepto."Nombre" as "NombreConcepto"
		   ,a."IdClasificacionTipoInstrumento"
		   ,a."IdTipoInstrumento"
		   ,a."IdInstrumento"
		   ,a."Nemotecnico"
		   ,a."IdEmisor"
		   ,a."FechaVencimientoInstr"
		   ,a."IdTipoReajuste"
		   ,a."NemoSVS"
		   ,a."IdMonedaInstr"
		   ,CASE WHEN COALESCE(CCP."IdCuentaCustodioPortafolio",0) > 0 THEN CCP."IdBoveda" ELSE a."IdBoveda" END "IdBoveda"
		   ,a."Precio"
		   ,a."Tasa"
		   ,a."Monto"
		   ,a."PrecioMercado"
		   ,a."TasaMercado"
		   ,a."MontoMercadoMonedaLocal"
		   ,a."MontoMercadoMonedaInstrumento"
		   ,a."MontoMercadoMonedaAdicional"
		   ,a."ValorRescate"
		   ,cast(SUM(a."CantidadNeg") over (partition by a."DocumentoIdentidad", a."IdPortafolio", a."Nemotecnico"
											order by a."DocumentoIdentidad", a."IdPortafolio", a."Nemotecnico" asc, a."FechaRegistro"  desc, a."IdCartera"
											rows between unbounded preceding and current row) as DECIMAL(26,8))as "Saldo"
		   ,a."Cantidad"
		   ,a."CantidadNeg"
		   ,a."FolioCartera"
		   ,a."FolioComprobante"
		   ,a."FechaComprobante"
		   ,a."IdTipoComprobante"
		   ,a."TipoRegistro"
		   ,ROW_NUMBER() over (order by a."DocumentoIdentidad", a."IdPortafolio", a."Nemotecnico" asc, a."FechaRegistro" desc, a."IdCartera") as "NroFila"
		   ,a."ISIN"
		   ,a."IdCuentaCustodioPortafolio"
		   ,COALESCE(CCP."Codigo",'') AS "NumeroCuentaBoveda"
		   ,a."IdTipoCustodia"
		   ,a."NombreTipoCustodia"
		   ,ProductoAgrupador."NombreProductoAgrupador" AS "Producto"
		   ,a."IdEstado" as "IdEstado"
		FROM :p_out_Tabla as a
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" as CCP ON CCP."IdCuentaCustodioPortafolio" = a."IdCuentaCustodioPortafolio"
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as Port ON Port."IdPortafolio" = a."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as Cli ON Cli."IdCliente" = Port."IdCliente"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as Concepto ON Concepto."IdConcepto" = a."IdConcepto"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Port."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
	)
	SELECT
		"IdCartera","FechaRegistro","FechaOperacion","FechaLiquidacion","IdTipoOperacion","TipoOperacion","Accion","IdCliente","IdPortafolio","CodigoPortafolio","NombreCliente","DocumentoIdentidad"
	   ,"IdConcepto","NombreConcepto","IdClasificacionTipoInstrumento","IdTipoInstrumento", "IdInstrumento","Nemotecnico","IdEmisor" ,"FechaVencimientoInstr","IdTipoReajuste", "NemoSVS" ,"IdMonedaInstr","IdBoveda","Precio","Tasa"
	   ,"Monto","PrecioMercado","TasaMercado","MontoMercadoMonedaLocal", "MontoMercadoMonedaInstrumento","MontoMercadoMonedaAdicional","ValorRescate","Saldo","Cantidad","CantidadNeg", "FolioCartera"	,"FolioComprobante"
	   ,"FechaComprobante","IdTipoComprobante","TipoRegistro","NroFila","ISIN","IdCuentaCustodioPortafolio","NumeroCuentaBoveda","IdTipoCustodia","NombreTipoCustodia","Producto", "IdEstado"
	FROM Informacion
	ORDER BY ROWNUMBER;

	
END;
GO