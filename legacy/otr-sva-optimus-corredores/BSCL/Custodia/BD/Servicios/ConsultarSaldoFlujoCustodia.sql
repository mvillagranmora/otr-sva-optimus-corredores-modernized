SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::ConsultarSaldoFlujoCustodia";
--  Proposito: Recuperar los saldos desde: 			
--													 INCORPORADO	CHEKEADO	ITEM_ORIGEN		ITEM_CALCULO
			--1.1 Cartera								OK				OK		CUSTODIA		INFORMACION_CARTERA
			--1.2 Transito								OK				OK				
				--1.2.1 Transito (Ingreso - Retiro)		OK						CUSTODIA		INFORMACION_TRANSITO_INGRESO -/- INFORMACION_TRANSITO_RETIRO
			--1.3 Garantia								OK						GARANTIA		INFORMACION_GARANTIA
			--1.4 Subyacente
				--1.4.1 Simultanea (Compra - Venta)		OK						SIMULTANEA		SIMULTANEA_COMPRA -/- SIMULTANEA_VENTA
				--1.4.2 Prestamo						OK						PRESTAMO		PRESTAMO
				--1.4.3 Emprestito						OK						PRESTAMO		EMPRESTITO
				--1.4.4 Pactos							OK						PACTOS			COMPRA_PACTO -/- VENTA_PACTO
			--1.5 Cartera Reservada
				--1.8.1 Pactos (Compra - Venta)			OK				OK		RESERVA			RESERVA_COMPRA_PACTOS -/- RESERVA_VENTA_PACTO
				--1.8.2 Termino	(Compra - Venta)		OK				OK		RESERVA			RESERVA_COMPRA_TERMINO -/- RESERVA_VENTA_TERMINO
			--1.6 Ordenes (Compra - Venta)
				--1.6.1 Pendientes de Autorizacion		OK				OK		ORDEN			ORDENES_COMPRA_PENDIENTES -/- ORDENES_VENTA_PENDIENTES
				--1.6.2 Ingresadas						OK				OK		ORDEN			ORDENES_COMPRA_INGRESADAS -/- ORDENES_VENTA_INGRESADAS
			--1.7 Asignaciones							OK						ASIGNACION		ASIGNACIONES_COMPRA_PENDIENTE -/- ASIGNACIONES_VENTA_PENDIENTE
			--1.8 Movmientos Facturacion				OK						FACTURACION		MOVIMIENTOS_COMPRA_FACTURACION -/- MOVIMIENTOS_VENTA_FACTURACION
			--1.9 Evento Capital						
				--1.9.1 Subscripciones					OK						EVENTOCAPITAL	SUBSCRIPCIONES
				--1.9.2	Sorteo de Letras				OK						EVENTOCAPITAL	SORTEO_LETRAS
			--1.10 Fondos Mutuos (Aportes - Rescates)	OK						FONDOSMUTUOS	APORTE_FONDOS_MUTUOS -/- RESCATE_FONDOS_MUTUOS
--  Autor:  NRM 
--  Fecha Creacion: 30-11-2016
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::ConsultarSaldoFlujoCustodia" ( 
    IN p_IdEmpresa				BIGINT,
    IN p_FechaSaldo				NVARCHAR(10),
    IN p_KeyFlujoCustodia		BIGINT,
    IN p_ValorizarFlujo			NVARCHAR(1)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL" AS
BEGIN
	DECLARE v_FechaMinima			DATE	:= NULL;
	DECLARE v_FechaMaxima			DATE	:= NULL;
	
	SELECT MIN("Fecha"),MAX("Fecha") INTO v_FechaMinima, v_FechaMaxima FROM "BSCL.Custodia.BD::CUS.FlujoCustodia_Fechas";
	v_FlujoCustodia_MatrizUso = SELECT * FROM "BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso" WHERE "IdUsoFlujoCustodia" IS NOT NULL;
	
		
	---------------------------------------------
	-- 1.1 CARTERA
	---------------------------------------------
	INSERT INTO "BSCL.Custodia.BD::CUS.DatosFlujoCustodia"(
		"IdDatosFlujoCustodia","KeyFlujoCustodia","IdUsoFlujoCustodia","UsoTipoOperacion","BusquedaEspecifica","IdTipoInstrumentoEspecifico",
		"ItemCalculo","ItemOrigen","FechaEjecucion","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdEmisor","Nemotecnico",
		"NemoSVS","IdMonedaInstrumento","IdTipoReajuste","FechaEmisionInstrumento","FechaVencimientoInstrumento","CodigoIFRS","EsFungible","TipoCarteraCorredor",
		"IdBoveda","IdBolsa","IdTipoOperacion","IdMesa","FechaLiquidacion","Cantidad","ValorRescate","PrecioCompra","TasaCompra","MontoATasaCompra","PrecioMercado",
		"TasaMercado","MontoATasaMercado","IdCliente","IdPortafolio","FechaLiquidacionProceso","IdRegistro","FolioRegistro","IdConcepto","RequiereValorizacion",
		"ValorizarFlujo"
	)
	SELECT  "BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia".NEXTVAL,:p_KeyFlujoCustodia,MZU."IdUsoFlujoCustodia",MZU."UsoTipoOperacion",
			MZU."BusquedaEspecifica",MZU."IdTipoInstrumentoEspecifico",I."ItemCalculo",I."ItemOrigen",:p_FechaSaldo,
			car."IdClasificacionTipoInstrumento",
			car."IdTipoInstrumento",
			car."IdInstrumento",
			car."IdEmisor",
			car."Nemotecnico",
			car."NemoSVS",
			car."IdMonedaInstrumento",
			car."IdTipoReajuste",
			car."FechaEmisionInstrumento",
			car."FechaVencimientoInstrumento",
			car."CodigoIFRS",
			car."EsFungible",
			car."TipoCarteraCorredor",
			car."IdBoveda",
			car."IdBolsa",				
			car."IdTipoOperacion",
			car."IdMesa",
			car."INGFechaLiquidacion" AS "FechaLiquidacion",
			car."INGCantidad" AS "Cantidad",
			car."INGValorRescate" AS "ValorRescate",
			car."INGPrecioCompra" AS "PrecioCompra",
			car."INGTasaCompra" AS "TasaCompra",
			car."INGMontoCompraActualMonedaInstrumento" as "MontoATasaCompra",
---------------------------------------------------------------------------------------------------------------
			car."INGPrecioMercadoActual" as "PrecioMercado",
			car."INGTasaMercadoActual" as "TasaMercado",
			car."INGMontoMercadoActualMonedaInstrumento" as "MontoATasaMercado",
---------------------------------------------------------------------------------------------------------------				
			car."IdCliente",
			car."IdPortafolio",				
			(CASE WHEN car."FechaLiquidacion" >= :v_FechaMinima THEN car."FechaLiquidacion" ELSE :v_FechaMinima END) AS "FechaLiquidacionProceso",				
			car."IdCartera" as "IdRegistro",
			car."FolioCartera" as "FolioRegistro",
			car."IdConcepto",
			'S' AS "RequiereValorizacion", --CASE WHEN car."IdClasificacionTipoInstrumento" = 'RV' THEN 'N' ELSE 'S' END AS "RequiereValorizacion",
			:p_ValorizarFlujo
		FROM "BSCL.Custodia.BD::CUS.Cartera" AS car
	   INNER JOIN "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" as CMC
	   		   ON CMC."IdTipoOperacion" = car."IdTipoOperacion"
	   		  AND CMC."PoseeInstrumentoSubyacente" = 'N'
	   INNER JOIN :v_FlujoCustodia_MatrizUso as MZU 
	   		   ON ( (MZU."IdCliente" IS NULL OR car."IdCliente" = MZU."IdCliente") AND
	   		   		(MZU."IdPortafolio" IS NULL OR car."IdPortafolio" = MZU."IdPortafolio")	AND
	   		   		(MZU."IdClasificacionTipoInstrumento" IS NULL OR car."IdClasificacionTipoInstrumento" = MZU."IdClasificacionTipoInstrumento") AND
	   		   		(MZU."IdTipoInstrumento" IS NULL OR car."IdTipoInstrumento" = MZU."IdTipoInstrumento") AND
	   		   		(MZU."IdInstrumento" IS NULL OR car."IdInstrumento" = MZU."IdInstrumento") AND
	   		   		(MZU."IdEmisor" IS NULL OR car."IdEmisor" = MZU."IdEmisor") AND
	   		   		(MZU."FechaVencimiento" IS NULL OR car."FechaVencimientoInstrumento" = MZU."FechaVencimiento") AND
	   		   		(MZU."IdTipoReajuste" IS NULL OR car."IdTipoReajuste" = MZU."IdTipoReajuste") AND 
	   		   		(MZU."EsFungible" IS NULL OR car."EsFungible" = MZU."EsFungible") AND
	   		   		(MZU."IdBoveda" IS NULL OR car."IdBoveda" = MZU."IdBoveda")
	   		   	  ),
			 "BSCL.Custodia.BD::CUS.FlujoCustodia_Items" AS I
	   WHERE I."ItemCalculo"			= 'INFORMACION_CARTERA'   
		 AND car."IdEstado"				= 'VIG'
		 AND car."IdEmpresa"			= :p_IdEmpresa
		 AND car."INGFechaLiquidacion" <=:v_FechaMaxima;
	
	
	---------------------------------------------
	-- 1.2 TRANSITO
	---------------------------------------------
	INSERT INTO "BSCL.Custodia.BD::CUS.DatosFlujoCustodia"(
		"IdDatosFlujoCustodia","KeyFlujoCustodia","IdUsoFlujoCustodia","UsoTipoOperacion","BusquedaEspecifica","IdTipoInstrumentoEspecifico",
		"ItemCalculo","ItemOrigen","FechaEjecucion","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdEmisor","Nemotecnico",
		"NemoSVS","IdMonedaInstrumento","IdTipoReajuste","FechaEmisionInstrumento","FechaVencimientoInstrumento","CodigoIFRS","EsFungible","TipoCarteraCorredor",
		"IdBoveda","IdBolsa","IdTipoOperacion","IdMesa","FechaLiquidacion","Cantidad","ValorRescate","PrecioCompra","TasaCompra","MontoATasaCompra","PrecioMercado",
		"TasaMercado","MontoATasaMercado","IdCliente","IdPortafolio","FechaLiquidacionProceso","IdRegistro","FolioRegistro","IdConcepto","RequiereValorizacion",
		"ValorizarFlujo"
	)
	SELECT  "BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia".NEXTVAL,:p_KeyFlujoCustodia,MZU."IdUsoFlujoCustodia",MZU."UsoTipoOperacion",
			MZU."BusquedaEspecifica",MZU."IdTipoInstrumentoEspecifico",I."ItemCalculo",I."ItemOrigen",:p_FechaSaldo,
			tra."IdClasificacionTipoInstrumento",
			tra."IdTipoInstrumento",
			tra."IdInstrumento",
			tra."IdEmisor",
			tra."Nemotecnico",
			tra."NemoSVS",
			tra."IdMonedaInstrumento",
			tra."IdTipoReajuste",
			tra."FechaEmisionInstrumento",
			tra."FechaVencimientoInstrumento",
			tra."CodigoIFRS",
			tra."EsFungible",
			'' AS "TipoCarteraCorredor",
			tra."IdBoveda",
			tra."IdBolsa",
			tra."IdTipoOperacion",
			tra."IdMesa",
			tra."FechaLiquidacion",
			tra."Cantidad",
			tra."ValorRescate",
			tra."PrecioCompra" AS "PrecioCompra",
			tra."TasaCompra" AS "TasaCompra",
			tra."MontoCompraMonedaInstrumento" as "MontoATasaCompra",
---------------------------------------------------------------------------------------------------------------				
			0 AS "PrecioMercado",
			0 AS "TasaMercado",
			0 as "MontoATasaMercado",
---------------------------------------------------------------------------------------------------------------				
			tra."IdCliente",
			tra."IdPortafolio",
			(CASE WHEN tra."FechaLiquidacion" >= :v_FechaMinima THEN tra."FechaLiquidacion" ELSE :v_FechaMinima END) AS "FechaLiquidacionProceso",
			tra."IdMovimientoCarteraEnTransito" as "IdRegistro",
			0 as "FolioRegistro",
			tra."IdConcepto",
			'S' AS "RequiereValorizacion",
			:p_ValorizarFlujo
		FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS tra
	   INNER JOIN "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" AS CONFIG 
	   		   ON CONFIG."IdTipoOperacion" = tra."IdTipoOperacion"
	   INNER JOIN :v_FlujoCustodia_MatrizUso as MZU 
	   		   ON ( (MZU."IdCliente" IS NULL OR tra."IdCliente" = MZU."IdCliente") AND
	   		   		(MZU."IdPortafolio" IS NULL OR tra."IdPortafolio" = MZU."IdPortafolio") AND
	   		   		(MZU."IdClasificacionTipoInstrumento" IS NULL OR tra."IdClasificacionTipoInstrumento" = MZU."IdClasificacionTipoInstrumento") AND
	   		   		(MZU."IdTipoInstrumento" IS NULL OR tra."IdTipoInstrumento" = MZU."IdTipoInstrumento") AND
	   		   		(MZU."IdInstrumento" IS NULL OR tra."IdInstrumento" = MZU."IdInstrumento") AND
	   		   		(MZU."IdEmisor" IS NULL OR tra."IdEmisor" = MZU."IdEmisor") AND
	   		   		(MZU."FechaVencimiento" IS NULL OR tra."FechaVencimientoInstrumento" = MZU."FechaVencimiento") AND
	   		   		(MZU."IdTipoReajuste" IS NULL OR tra."IdTipoReajuste" = MZU."IdTipoReajuste") AND 
	   		   		(MZU."EsFungible" IS NULL OR tra."EsFungible" = MZU."EsFungible") AND
	   		   		(MZU."IdBoveda" IS NULL OR tra."IdBoveda" = MZU."IdBoveda")
	   		      ),
			  "BSCL.Custodia.BD::CUS.FlujoCustodia_Items" AS I
		WHERE I."ItemCalculo" = CASE WHEN CONFIG."IdAccionOperacion" = 'ING' THEN 'INFORMACION_TRANSITO_INGRESO'
		 							 WHEN CONFIG."IdAccionOperacion" = 'RET' THEN 'INFORMACION_TRANSITO_RETIRO'
	 						     END
		  AND tra."IdEmpresa" = :p_IdEmpresa
		  AND tra."IdEstado" = 'PEN'
		  AND tra."FechaLiquidacion" <=:v_FechaMaxima;
	
	
	---------------------------------------------
	-- 1.3 GARANTIA
	---------------------------------------------
	INSERT INTO "BSCL.Custodia.BD::CUS.DatosFlujoCustodia"(
		"IdDatosFlujoCustodia","KeyFlujoCustodia","IdUsoFlujoCustodia","UsoTipoOperacion","BusquedaEspecifica","IdTipoInstrumentoEspecifico",
		"ItemCalculo","ItemOrigen","FechaEjecucion","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdEmisor","Nemotecnico",
		"NemoSVS","IdMonedaInstrumento","IdTipoReajuste","FechaEmisionInstrumento","FechaVencimientoInstrumento","CodigoIFRS","EsFungible","TipoCarteraCorredor",
		"IdBoveda","IdBolsa","IdTipoOperacion","IdMesa","FechaLiquidacion","Cantidad","ValorRescate","PrecioCompra","TasaCompra","MontoATasaCompra","PrecioMercado",
		"TasaMercado","MontoATasaMercado","IdCliente","IdPortafolio","FechaLiquidacionProceso","IdRegistro","FolioRegistro","IdConcepto","RequiereValorizacion",
		"ValorizarFlujo"
	)
	SELECT  "BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia".NEXTVAL,:p_KeyFlujoCustodia,MZU."IdUsoFlujoCustodia",MZU."UsoTipoOperacion",
			MZU."BusquedaEspecifica",MZU."IdTipoInstrumentoEspecifico",I."ItemCalculo",I."ItemOrigen",:p_FechaSaldo,
			gar."IdClasificacionTipoInstrumento",
			gar."IdTipoInstrumento",
			gar."IdInstrumento",
			gar."IdEmisor",
			gar."Nemotecnico",
			gar."NemoSVS",
			gar."IdMonedaInstrumento",
			gar."IdTipoReajuste",
			gar."FechaEmisionInstrumento",
			gar."FechaVencimientoInstrumento",
			gar."CodigoIFRS",
			gar."EsFungible",
			'' AS "TipoCarteraCorredor",
			car."IdBoveda",
			car."IdBolsa",
			car."IdTipoOperacion",
			car."IdMesa",
			gar."INGFechaLiquidacion" as "FechaLiquidacion",
			gar."INGCantidad" as "Cantidad",
			gar."INGValorRescate" as "ValorRescate",
			gar."INGPrecioGarantia" as "PrecioCompra",
			gar."INGTasaGarantia" as "TasaCompra",
			gar."INGMontoCompraActualMonedaInstrumento" as "MontoATasaCompra",
---------------------------------------------------------------------------------------------------------------				
			gar."INGPrecioMercadoActual" as "PrecioMercado",
			gar."INGTasaMercadoActual" as "TasaMercado",
			gar."INGMontoMercadoActualMonedaInstrumento" as "MontoATasaMercado",
---------------------------------------------------------------------------------------------------------------				
			gar."IdCliente",
			gar."IdPortafolio",		
			(CASE WHEN gar."INGFechaLiquidacion" >= :v_FechaMinima THEN gar."INGFechaLiquidacion" ELSE :v_FechaMinima END) AS "FechaLiquidacionProceso",
			gar."IdGarantia" as "IdRegistro",
			gar."FolioGarantia" as "FolioRegistro",
			gar."IdConcepto",
			'S' AS "RequiereValorizacion", --CASE WHEN gar."IdClasificacionTipoInstrumento" = 'RV' THEN 'N' ELSE 'S' END AS "RequiereValorizacion",
			:p_ValorizarFlujo
		FROM "BSCL.Custodia.BD::GTA.Garantia" AS gar
	   INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS car ON car."IdCartera" = gar."IdCarteraOrigen"
	   INNER JOIN :v_FlujoCustodia_MatrizUso as MZU 
	   		   ON ( (MZU."IdCliente" IS NULL OR gar."IdCliente" = MZU."IdCliente") AND
	   		   		(MZU."IdPortafolio" IS NULL OR gar."IdPortafolio" = MZU."IdPortafolio") AND
	   		   		(MZU."IdClasificacionTipoInstrumento" IS NULL OR gar."IdClasificacionTipoInstrumento" = MZU."IdClasificacionTipoInstrumento") AND
	   		   		(MZU."IdTipoInstrumento" IS NULL OR gar."IdTipoInstrumento" = MZU."IdTipoInstrumento") AND
	   		   		(MZU."IdInstrumento" IS NULL OR gar."IdInstrumento" = MZU."IdInstrumento") AND
	   		   		(MZU."IdEmisor" IS NULL OR gar."IdEmisor" = MZU."IdEmisor") AND
	   		   		(MZU."FechaVencimiento" IS NULL OR gar."FechaVencimientoInstrumento" = MZU."FechaVencimiento") AND
	   		   		(MZU."IdTipoReajuste" IS NULL OR gar."IdTipoReajuste" = MZU."IdTipoReajuste") AND
	   		   		(MZU."EsFungible" IS NULL OR gar."EsFungible" = MZU."EsFungible")
	   		   	  ),
			  "BSCL.Custodia.BD::CUS.FlujoCustodia_Items" AS I
		WHERE I."ItemCalculo" = 'INFORMACION_GARANTIA'
		  AND gar."IdEstado" = 'VIG'
		  AND gar."IdOrigenGarantia" = 'CUS'
		  AND gar."IdEmpresa" = :p_IdEmpresa			  
		  AND gar."INGFechaLiquidacion" <=:v_FechaMaxima;
	
	--------------------------------------------------------------------
	-- 1.4 SUBYACENTE
	--------------------------------------------------------------------
	INSERT INTO "BSCL.Custodia.BD::CUS.DatosFlujoCustodia"(
		"IdDatosFlujoCustodia","KeyFlujoCustodia","IdUsoFlujoCustodia","UsoTipoOperacion","BusquedaEspecifica","IdTipoInstrumentoEspecifico",
		"ItemCalculo","ItemOrigen","FechaEjecucion","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdEmisor","Nemotecnico",
		"NemoSVS","IdMonedaInstrumento","IdTipoReajuste","FechaEmisionInstrumento","FechaVencimientoInstrumento","CodigoIFRS","EsFungible","TipoCarteraCorredor",
		"IdBoveda","IdBolsa","IdTipoOperacion","IdMesa","FechaLiquidacion","Cantidad","ValorRescate","PrecioCompra","TasaCompra","MontoATasaCompra","PrecioMercado",
		"TasaMercado","MontoATasaMercado","IdCliente","IdPortafolio","FechaLiquidacionProceso","IdRegistro","FolioRegistro","IdConcepto","RequiereValorizacion",
		"ValorizarFlujo"
	)
	SELECT  "BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia".NEXTVAL,:p_KeyFlujoCustodia,MZU."IdUsoFlujoCustodia",MZU."UsoTipoOperacion",
			MZU."BusquedaEspecifica",MZU."IdTipoInstrumentoEspecifico",I."ItemCalculo",I."ItemOrigen",:p_FechaSaldo,
		    sub."IdClasificacionTipoInstrumento",
			sub."IdTipoInstrumento",
			sub."IdInstrumento",
			sub."IdEmisor",
			sub."Nemotecnico",
			sub."NemoSVS",
			sub."IdMonedaInstrumento",
			sub."IdTipoReajuste",
			sub."FechaEmisionInstrumento",
			sub."FechaVencimientoInstrumento",
			car."CodigoIFRS",
			sub."EsFungible",
			'' AS "TipoCarteraCorredor",
			car."IdBoveda",
			car."IdBolsa",
			car."IdTipoOperacion",
			car."IdMesa",
			car."FechaLiquidacion",
			
			sub."INGCantidad" as "Cantidad",
			sub."INGValorRescate" as "ValorRescate",
			sub."INGPrecioSubyacente" as "PrecioCompra",
			sub."INGTasaSubyacente" as "TasaCompra",
			sub."INGMontoCompraActualMonedaInstrumento" as "MontoATasaCompra", 
---------------------------------------------------------------------------------------------------------------				
			sub."INGPrecioMercadoActual" as "PrecioMercado",
			sub."INGTasaMercadoActual" as "TasaMercado",
			sub."INGMontoMercadoActualMonedaInstrumento" as "MontoATasaMercado",
---------------------------------------------------------------------------------------------------------------				
			car."IdCliente",
			car."IdPortafolio",
			(CASE WHEN car."FechaLiquidacion" >= :v_FechaMinima THEN car."FechaLiquidacion" 
				  ELSE :v_FechaMinima
			  END) AS "FechaLiquidacionProceso",				
			sub."IdSubyacente" as "IdRegistro",
			sub."FolioSubyacente" as "FolioRegistro",
			car."IdConcepto",
			'S' AS "RequiereValorizacion", --CASE WHEN sub."IdClasificacionTipoInstrumento" = 'RV' THEN 'N' ELSE 'S' END AS "RequiereValorizacion",
			:p_ValorizarFlujo
		FROM "BSCL.Custodia.BD::CUS.Subyacente" AS sub
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS car
			   ON (car."FolioCartera" = sub."FolioCarteraRelacionada")
		INNER JOIN :v_FlujoCustodia_MatrizUso as MZU
	   		   ON (	(MZU."IdCliente" IS NULL OR car."IdCliente" = MZU."IdCliente") AND
	   		   		(MZU."IdPortafolio" IS NULL OR car."IdPortafolio" = MZU."IdPortafolio") AND
	   		   		(MZU."IdClasificacionTipoInstrumento" IS NULL OR sub."IdClasificacionTipoInstrumento" = MZU."IdClasificacionTipoInstrumento") AND
	   		   		(MZU."IdTipoInstrumento" IS NULL OR sub."IdTipoInstrumento" = MZU."IdTipoInstrumento") AND
	   		   		(MZU."IdInstrumento" IS NULL OR sub."IdInstrumento" = MZU."IdInstrumento") AND
	   		   		(MZU."IdEmisor" IS NULL OR sub."IdEmisor" = MZU."IdEmisor") AND
	   		   		(MZU."FechaVencimiento" IS NULL OR sub."FechaVencimientoInstrumento" = MZU."FechaVencimiento") AND
	   		   		(MZU."IdTipoReajuste" IS NULL OR sub."IdTipoReajuste" = MZU."IdTipoReajuste") AND
	   		   		(MZU."EsFungible" IS NULL OR sub."EsFungible" = MZU."EsFungible") AND
	   		   		(MZU."IdBoveda" IS NULL OR car."IdBoveda" = MZU."IdBoveda")
	   		   	  ),
			 "BSCL.Custodia.BD::CUS.FlujoCustodia_Items" AS I
		WHERE I."ItemCalculo" =  CASE WHEN car."IdTipoOperacion" = 'COMSIM' THEN 'SIMULTANEA_COMPRA'		--1.4.1
		 							  WHEN car."IdTipoOperacion" = 'VENSIM' THEN 'SIMULTANEA_VENTA'			--1.4.1
		 							  WHEN car."IdTipoOperacion" = 'PRESTA' THEN 'PRESTAMO' 				--1.4.2
		 							  WHEN car."IdTipoOperacion" = 'EMPRES' THEN 'EMPRESTITO'				--1.4.3
		 							  WHEN car."IdTipoOperacion" = 'COMPAC' THEN 'COMPRA_PACTO'				--1.4.4
		 							  WHEN car."IdTipoOperacion" = 'VENPAC' THEN 'VENTA_PACTO'				--1.4.4
	 						      END
		  AND car."IdEmpresa" = :p_IdEmpresa
		  AND car."IdTipoOperacion" IN ('COMSIM','VENSIM','PRESTA','EMPRES','COMPAC','VENPAC')
		  AND sub."IdEstado" = 'VIG'
		  AND car."FechaLiquidacion" <=:v_FechaMaxima;
	
	
	---------------------------------------------
	-- 1.5 CARTERA RESERVADA
	---------------------------------------------
	INSERT INTO "BSCL.Custodia.BD::CUS.DatosFlujoCustodia"(
		"IdDatosFlujoCustodia","KeyFlujoCustodia","IdUsoFlujoCustodia","UsoTipoOperacion","BusquedaEspecifica","IdTipoInstrumentoEspecifico",
		"ItemCalculo","ItemOrigen","FechaEjecucion","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdEmisor","Nemotecnico",
		"NemoSVS","IdMonedaInstrumento","IdTipoReajuste","FechaEmisionInstrumento","FechaVencimientoInstrumento","CodigoIFRS","EsFungible","TipoCarteraCorredor",
		"IdBoveda","IdBolsa","IdTipoOperacion","IdMesa","FechaLiquidacion","Cantidad","ValorRescate","PrecioCompra","TasaCompra","MontoATasaCompra","PrecioMercado",
		"TasaMercado","MontoATasaMercado","IdCliente","IdPortafolio","FechaLiquidacionProceso","IdRegistro","FolioRegistro","IdConcepto","RequiereValorizacion",
		"ValorizarFlujo"
	)
	SELECT "BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia".NEXTVAL,:p_KeyFlujoCustodia,MZU."IdUsoFlujoCustodia",MZU."UsoTipoOperacion",
		   MZU."BusquedaEspecifica",MZU."IdTipoInstrumentoEspecifico",I."ItemCalculo",I."ItemOrigen",:p_FechaSaldo,
		   RSV."IdClasificacionTipoInstrumento",
		   RSV."IdTipoInstrumento",
		   RSV."IdInstrumento",
		   RSV."IdEmisor",
		   RSV."Nemotecnico",
		   RSV."NemoSVS",
		   RSV."IdMonedaInstrumento",
		   RSV."IdTipoReajuste",
		   RSV."FechaEmisionInstrumento",
		   RSV."FechaVencimientoInstrumento",
		   RSV."CodigoIFRS",
		   RSV."EsFungible",
		   '' AS "TipoCarteraCorredor",
		   RSV."IdBoveda",
		   RSV."IdBolsa",
		   RSV."IdTipoOperacion",
		   RSV."IdMesa",
		   RSV."INGFechaLiquidacion" as "FechaLiquidacion",
		   RSV."INGCantidad" as "Cantidad",
		   RSV."INGValorRescate" as "ValorRescate",
		   RSV."INGPrecioCompra",
		   RSV."INGTasaCompra",
		   RSV."INGMontoATasaCompraActual" as "MontoATasaCompra",
---------------------------------------------------------------------------------------------------------------			   
		   RSV."INGPrecioMercadoActual" as "PrecioMercado",
		   RSV."INGTasaMercadoActual" as "TasaMercado",
		   RSV."INGMontoATasaMercadoActual" as "MontoATasaMercado",
---------------------------------------------------------------------------------------------------------------			   
		   RSV."IdCliente",
		   RSV."IdPortafolio",
		   (CASE WHEN RSV."INGFechaLiquidacion" >= :v_FechaMinima THEN RSV."INGFechaLiquidacion" ELSE :v_FechaMinima END) AS "FechaLiquidacionProceso",	
		   RSV."IdRegistro",
		   RSV."FolioRegistro",
		   RSV."IdConcepto",
		   'S' AS "RequiereValorizacion", --CASE WHEN RSV."IdClasificacionTipoInstrumento" = 'RV' THEN 'N' ELSE 'S' END AS "RequiereValorizacion",
		   :p_ValorizarFlujo
	  FROM ( --Las reservas estan guardando "IngMontoValorizado" expresado en la moneda de sistema
	  		SELECT R."IdClasificacionTipoInstrumento",R."IdTipoInstrumento",R."IdInstrumento",R."IdEmisor",C."Nemotecnico",
	  			   C."NemoSVS",C."IdMonedaSistema" AS "IdMonedaInstrumento",C."IdTipoReajuste",C."FechaEmisionInstrumento",C."FechaVencimientoInstrumento",
				   R."CodigoIFRS",R."EsFungible",R."IdBoveda",R."IdBolsa",O."IdTipoOperacion",O."IdMesaOrigen" AS "IdMesa",
				   CASE WHEN O."IdTipoOperacion" IN ('COMTER','VENTER') THEN O."FechaCondicionLiquidacion"
				   		WHEN O."IdTipoOperacion" IN ('COMPAC','VENPAC') THEN O."FechaCreacion"
				    END AS "INGFechaLiquidacion",
				   R."IngCantidad" AS "INGCantidad",R."IngValorRescate" AS "INGValorRescate",R."Precio" AS "INGPrecioCompra",R."Tasa" AS "INGTasaCompra",
				   R."IngMontoValorizado" AS "INGMontoATasaCompraActual",0 AS "INGPrecioMercadoActual",0 AS "INGTasaMercadoActual",R."IdConcepto",
				   0 AS "INGMontoATasaMercadoActual",O."IdCliente",O."IdPortafolio",R."IdReserva" AS "IdRegistro",O."IdOperacion" AS "FolioRegistro"					   
			  FROM "BSCL.Custodia.BD::CUS.Reserva" as R
			 INNER JOIN "BSCL.Mesa.BD::MES.Operacion" as O ON O."IdOperacion" = R."IdOperacion"
			 INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as C ON C."FolioCartera" = R."FolioCarteraOrigen"
			 WHERE R."IdEstado" = 'VIG'
			   AND C."IdEstado" = 'VIG'
			   AND C."IdEmpresa" = :p_IdEmpresa
	  	   ) AS RSV
	 INNER JOIN :v_FlujoCustodia_MatrizUso as MZU 
	   		   ON (	(MZU."IdCliente" IS NULL OR RSV."IdCliente" = MZU."IdCliente") AND
	   		   		(MZU."IdPortafolio" IS NULL OR RSV."IdPortafolio" = MZU."IdPortafolio") AND
	   		   		(MZU."IdClasificacionTipoInstrumento" IS NULL OR RSV."IdClasificacionTipoInstrumento" = MZU."IdClasificacionTipoInstrumento") AND
	   		   		(MZU."IdTipoInstrumento" IS NULL OR RSV."IdTipoInstrumento" = MZU."IdTipoInstrumento") AND
	   		   		(MZU."IdInstrumento" IS NULL OR RSV."IdInstrumento" = MZU."IdInstrumento") AND
	   		   		(MZU."IdEmisor" IS NULL OR RSV."IdEmisor" = MZU."IdEmisor") AND
	   		   		(MZU."IdTipoReajuste" IS NULL OR RSV."IdTipoReajuste" = MZU."IdTipoReajuste") AND
	   		   		(MZU."EsFungible" IS NULL OR RSV."EsFungible" = MZU."EsFungible")
	   		      ),
		   "BSCL.Custodia.BD::CUS.FlujoCustodia_Items" AS I
	 WHERE I."ItemCalculo" = CASE WHEN RSV."IdTipoOperacion" = 'COMPAC' THEN 'RESERVA_COMPRA_PACTOS' 
	 							  WHEN RSV."IdTipoOperacion" = 'VENPAC' THEN 'RESERVA_VENTA_PACTO'
	 							  WHEN RSV."IdTipoOperacion" = 'COMTER' THEN 'RESERVA_COMPRA_TERMINO' 
	 							  WHEN RSV."IdTipoOperacion" = 'VENTER' THEN 'RESERVA_VENTA_TERMINO'
	 						  END
	   AND RSV."IdTipoOperacion" IN ('COMPAC','VENPAC','COMTER','VENTER')
	   AND RSV."INGFechaLiquidacion" <=:v_FechaMaxima;
	

	---------------------------------------------
	-- 1.6.1 ORDENES PENDIENTES DE AUTORIZACION
	---------------------------------------------
	INSERT INTO "BSCL.Custodia.BD::CUS.DatosFlujoCustodia"(
		"IdDatosFlujoCustodia","KeyFlujoCustodia","IdUsoFlujoCustodia","UsoTipoOperacion","BusquedaEspecifica","IdTipoInstrumentoEspecifico",
		"ItemCalculo","ItemOrigen","FechaEjecucion","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdEmisor","Nemotecnico",
		"NemoSVS","IdMonedaInstrumento","IdTipoReajuste","FechaEmisionInstrumento","FechaVencimientoInstrumento","CodigoIFRS","EsFungible","TipoCarteraCorredor",
		"IdBoveda","IdBolsa","IdTipoOperacion","IdMesa","FechaLiquidacion","Cantidad","ValorRescate","PrecioCompra","TasaCompra","MontoATasaCompra","PrecioMercado",
		"TasaMercado","MontoATasaMercado","IdCliente","IdPortafolio","FechaLiquidacionProceso","IdRegistro","FolioRegistro","IdConcepto","RequiereValorizacion",
		"ValorizarFlujo"
	)
	SELECT  "BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia".NEXTVAL,:p_KeyFlujoCustodia,MZU."IdUsoFlujoCustodia",MZU."UsoTipoOperacion",
			MZU."BusquedaEspecifica",MZU."IdTipoInstrumentoEspecifico",I."ItemCalculo",I."ItemOrigen",:p_FechaSaldo,
		    OI."IdClasificacionTipoInstrumento",
			OI."IdTipoInstrumento",
			OI."IdInstrumento",
			OI."IdEmisor",
			OI."Nemotecnico",
			'' AS "NemoSVS",
			OI."IdMonedaOrden" AS "IdMonedaInstrumento",
			OI."IdTipoReajuste",
			OI."FechaEmision" AS "FechaEmisionInstrumento",
			'0001-01-01' AS "FechaVencimientoInstrumento",
			'' AS "CodigoIFRS",
			OI."EsFungible",
			'' AS "TipoCarteraCorredor",
			'' AS "IdBoveda",
			OI."IdBolsa" as "IdBolsa",
			OI."IdTipoOperacion",
			0 AS "IdMesa",
			OI."FechaLiquidacion",
			OI."Cantidad",
			OI."ValorRescate",
			OI."PrecioOrden" AS "PrecioCompra",
			0 AS "TasaCompra",
			OI."MontoOrden" AS "MontoATasaCompra",
---------------------------------------------------------------------------------------------------------------				
			0 AS "PrecioMercado",
			0 AS "TasaMercado",
			0 AS "MontoATasaMercado",
---------------------------------------------------------------------------------------------------------------				
			OI."IdCliente",
			OI."IdPortafolio",
			(CASE WHEN OI."FechaLiquidacion" >= :v_FechaMinima THEN OI."FechaLiquidacion" ELSE :v_FechaMinima END) AS "FechaLiquidacionProceso",				
			OI."IdOrden" as "IdRegistro",
			0 as "FolioRegistro",
			OI."IdConcepto",
			'S' AS "RequiereValorizacion", --CASE WHEN OI."IdClasificacionTipoInstrumento" = 'RV' THEN 'N' ELSE 'S' END AS "RequiereValorizacion",
			:p_ValorizarFlujo
	  FROM "BSCL.Orden.BD.Vistas::Flujos.ConsultarOrdenPendienteAutorizacionFlujoCustodia" as OI
	 INNER JOIN :v_FlujoCustodia_MatrizUso as MZU 
	   		   ON (	(MZU."IdCliente" IS NULL OR OI."IdCliente" = MZU."IdCliente") AND
	   		   		(MZU."IdPortafolio" IS NULL OR OI."IdPortafolio" = MZU."IdPortafolio") AND
	   		   		(MZU."IdClasificacionTipoInstrumento" IS NULL OR OI."IdClasificacionTipoInstrumento" = MZU."IdClasificacionTipoInstrumento") AND
	   		   		(MZU."IdTipoInstrumento" IS NULL OR OI."IdTipoInstrumento" = MZU."IdTipoInstrumento") AND
	   		   		(MZU."IdInstrumento" IS NULL OR OI."IdInstrumento" = MZU."IdInstrumento") AND
	   		   		(MZU."IdEmisor" IS NULL OR OI."IdEmisor" = MZU."IdEmisor") AND
	   		   		(MZU."IdTipoReajuste" IS NULL OR OI."IdTipoReajuste" = MZU."IdTipoReajuste") AND
	   		   		(MZU."EsFungible" IS NULL OR OI."EsFungible" = MZU."EsFungible")
	   		   	  ),
		   "BSCL.Custodia.BD::CUS.FlujoCustodia_Items" AS I
	 WHERE I."ItemCalculo" = CASE WHEN OI."Accion" = 'COMPRA' THEN 'ORDENES_COMPRA_PENDIENTES' 
	 							  WHEN OI."Accion" = 'VENTA' THEN 'ORDENES_VENTA_PENDIENTES' 
	 						  END
	   AND OI."FechaLiquidacion" <=:v_FechaMaxima
	   AND OI."IdEmpresa" = :p_IdEmpresa;
				   
 	
	---------------------------------------------
	-- 1.6.2 ORDENES INGRESADAS
	---------------------------------------------
	INSERT INTO "BSCL.Custodia.BD::CUS.DatosFlujoCustodia"(
		"IdDatosFlujoCustodia","KeyFlujoCustodia","IdUsoFlujoCustodia","UsoTipoOperacion","BusquedaEspecifica","IdTipoInstrumentoEspecifico",
		"ItemCalculo","ItemOrigen","FechaEjecucion","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdEmisor","Nemotecnico",
		"NemoSVS","IdMonedaInstrumento","IdTipoReajuste","FechaEmisionInstrumento","FechaVencimientoInstrumento","CodigoIFRS","EsFungible","TipoCarteraCorredor",
		"IdBoveda","IdBolsa","IdTipoOperacion","IdMesa","FechaLiquidacion","Cantidad","ValorRescate","PrecioCompra","TasaCompra","MontoATasaCompra","PrecioMercado",
		"TasaMercado","MontoATasaMercado","IdCliente","IdPortafolio","FechaLiquidacionProceso","IdRegistro","FolioRegistro","IdConcepto","RequiereValorizacion",
		"ValorizarFlujo"
	)
	SELECT  "BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia".NEXTVAL,:p_KeyFlujoCustodia,MZU."IdUsoFlujoCustodia",MZU."UsoTipoOperacion",
			MZU."BusquedaEspecifica",MZU."IdTipoInstrumentoEspecifico",I."ItemCalculo",I."ItemOrigen",:p_FechaSaldo,
		    OI."IdClasificacionTipoInstrumento",
			OI."IdTipoInstrumento",
			OI."IdInstrumento",
			OI."IdEmisor",
			OI."Nemotecnico",
			'' AS "NemoSVS",
			OI."IdMonedaOrden" AS "IdMonedaInstrumento",
			OI."IdTipoReajuste",
			OI."FechaEmision" AS "FechaEmisionInstrumento",
			'0001-01-01' AS "FechaVencimientoInstrumento",
			'' AS "CodigoIFRS",
			OI."EsFungible",
			'' AS "TipoCarteraCorredor",
			'' AS "IdBoveda",
			OI."IdBolsa" as "IdBolsa",
			OI."IdTipoOperacion",
			0 AS "IdMesa",
			OI."FechaLiquidacion",
			OI."CantidadDisponible" AS"Cantidad",
			OI."ValorRescateDisponible" AS "ValorRescate",
			OI."PrecioOrden" AS "PrecioCompra",
			0 AS "TasaCompra",
			OI."MontoDisponible" AS "MontoATasaCompra",
---------------------------------------------------------------------------------------------------------------				
			0 AS "PrecioMercado",
			0 AS "TasaMercado",
			0 AS "MontoATasaMercado",
---------------------------------------------------------------------------------------------------------------				
			OI."IdCliente",
			OI."IdPortafolio",
			(CASE WHEN OI."FechaLiquidacion" >= :v_FechaMinima THEN OI."FechaLiquidacion" ELSE :v_FechaMinima END) AS "FechaLiquidacionProceso",				
			OI."IdOrden" as "IdRegistro",
			0 as "FolioRegistro",
			OI."IdConcepto",
			'S' AS "RequiereValorizacion", --CASE WHEN OI."IdClasificacionTipoInstrumento" = 'RV' THEN 'N' ELSE 'S' END AS "RequiereValorizacion",
			:p_ValorizarFlujo
	  FROM "BSCL.Orden.BD.Vistas::Flujos.ConsultarOrdenIngresadaFlujoCustodia" as OI
	 INNER JOIN :v_FlujoCustodia_MatrizUso as MZU 
	   		   ON (	(MZU."IdCliente" IS NULL OR OI."IdCliente" = MZU."IdCliente") AND
	   		   		(MZU."IdPortafolio" IS NULL OR OI."IdPortafolio" = MZU."IdPortafolio") AND
	   		   		(MZU."IdClasificacionTipoInstrumento" IS NULL OR OI."IdClasificacionTipoInstrumento" = MZU."IdClasificacionTipoInstrumento") AND
	   		   		(MZU."IdTipoInstrumento" IS NULL OR OI."IdTipoInstrumento" = MZU."IdTipoInstrumento") AND
	   		   		(MZU."IdInstrumento" IS NULL OR OI."IdInstrumento" = MZU."IdInstrumento") AND
	   		   		(MZU."IdEmisor" IS NULL OR OI."IdEmisor" = MZU."IdEmisor") AND
	   		   		(MZU."IdTipoReajuste" IS NULL OR OI."IdTipoReajuste" = MZU."IdTipoReajuste") AND
	   		   		(MZU."EsFungible" IS NULL OR OI."EsFungible" = MZU."EsFungible")
	   		      ),
		   "BSCL.Custodia.BD::CUS.FlujoCustodia_Items" AS I
	 WHERE I."ItemCalculo" = CASE WHEN OI."Accion" = 'COMPRA' THEN 'ORDENES_COMPRA_INGRESADAS' 
	 							  WHEN OI."Accion" = 'VENTA' THEN 'ORDENES_VENTA_INGRESADAS' 
	 						  END
	   AND OI."FechaLiquidacion" <=:v_FechaMaxima
	   AND OI."IdEmpresa" = :p_IdEmpresa
	   AND OI."CantidadDisponible" > 0;


	---------------------------------------------
	-- 1.7 ASIGNACIONES NO ENVIAS A FACTURAR
	---------------------------------------------
	INSERT INTO "BSCL.Custodia.BD::CUS.DatosFlujoCustodia"(
		"IdDatosFlujoCustodia","KeyFlujoCustodia","IdUsoFlujoCustodia","UsoTipoOperacion","BusquedaEspecifica","IdTipoInstrumentoEspecifico",
		"ItemCalculo","ItemOrigen","FechaEjecucion","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdEmisor","Nemotecnico",
		"NemoSVS","IdMonedaInstrumento","IdTipoReajuste","FechaEmisionInstrumento","FechaVencimientoInstrumento","CodigoIFRS","EsFungible","TipoCarteraCorredor",
		"IdBoveda","IdBolsa","IdTipoOperacion","IdMesa","FechaLiquidacion","Cantidad","ValorRescate","PrecioCompra","TasaCompra","MontoATasaCompra","PrecioMercado",
		"TasaMercado","MontoATasaMercado","IdCliente","IdPortafolio","FechaLiquidacionProceso","IdRegistro","FolioRegistro","IdConcepto","RequiereValorizacion",
		"ValorizarFlujo"
	)
	SELECT  "BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia".NEXTVAL,:p_KeyFlujoCustodia,MZU."IdUsoFlujoCustodia",MZU."UsoTipoOperacion",
			MZU."BusquedaEspecifica",MZU."IdTipoInstrumentoEspecifico",I."ItemCalculo",I."ItemOrigen",:p_FechaSaldo,
		    ASIG."IdClasificacionTipoInstrumento",
			ASIG."IdTipoInstrumento",
			ASIG."IdInstrumento",
			ASIG."IdEmisor",
			ASIG."Nemotecnico",
			'' AS "NemoSVS",
			ASIG."IdMonedaAsignacion" AS "IdMonedaInstrumento",
			ASIG."IdTipoReajuste",
			ASIG."FechaEmision" AS "FechaEmisionInstrumento",
			'0001-01-01' AS "FechaVencimientoInstrumento",
			'' AS "CodigoIFRS",
			ASIG."EsFungible",
			'' AS "TipoCarteraCorredor",
			'' AS "IdBoveda",
			ASIG."IdBolsa" as "IdBolsa",
			ASIG."IdTipoOperacion",
			0 AS "IdMesa",
			ASIG."FechaLiquidacion",
			ASIG."CantidadAsignada" AS "Cantidad",
			ASIG."ValorRescateAsignado" AS "ValorRescate",
			0 AS "PrecioCompra",						---	revisar
			0 AS "TasaCompra",						---	revisar
			ASIG."MontoAsignado" AS "MontoATasaCompra",
---------------------------------------------------------------------------------------------------------------				
			0 AS "PrecioMercado",
			0 AS "TasaMercado",
			0 AS "MontoATasaMercado",
---------------------------------------------------------------------------------------------------------------				
			ASIG."IdCliente",
			ASIG."IdPortafolio",
			(CASE WHEN ASIG."FechaLiquidacion" >= :v_FechaMinima THEN ASIG."FechaLiquidacion" ELSE :v_FechaMinima END) AS "FechaLiquidacionProceso",				
			ASIG."IdOrden" as "IdRegistro",
			0 as "FolioRegistro",
			ASIG."IdConcepto",
			'S' AS "RequiereValorizacion", --CASE WHEN ASIG."IdClasificacionTipoInstrumento" = 'RV' THEN 'N' ELSE 'S' END AS "RequiereValorizacion",
			:p_ValorizarFlujo
	  FROM "BSCL.Orden.BD.Vistas::Flujos.ConsultarAsignacionNoEnviadaFacturarFlujoCustodia" as ASIG
	 INNER JOIN :v_FlujoCustodia_MatrizUso as MZU 
	   		   ON (	(MZU."IdCliente" IS NULL OR ASIG."IdCliente" = MZU."IdCliente") AND
	   		   		(MZU."IdPortafolio" IS NULL OR ASIG."IdPortafolio" = MZU."IdPortafolio") AND
	   		   		(MZU."IdClasificacionTipoInstrumento" IS NULL OR ASIG."IdClasificacionTipoInstrumento" = MZU."IdClasificacionTipoInstrumento") AND
	   		   		(MZU."IdTipoInstrumento" IS NULL OR ASIG."IdTipoInstrumento" = MZU."IdTipoInstrumento") AND
	   		   		(MZU."IdInstrumento" IS NULL OR ASIG."IdInstrumento" = MZU."IdInstrumento") AND
	   		   		(MZU."IdEmisor" IS NULL OR ASIG."IdEmisor" = MZU."IdEmisor") AND
	   		   		(MZU."IdTipoReajuste" IS NULL OR ASIG."IdTipoReajuste" = MZU."IdTipoReajuste") AND
	   		   		(MZU."EsFungible" IS NULL OR ASIG."EsFungible" = MZU."EsFungible")
	   		   	  ),
		   "BSCL.Custodia.BD::CUS.FlujoCustodia_Items" AS I
	 WHERE I."ItemCalculo" = CASE WHEN ASIG."Accion" = 'COMPRA' THEN 'ASIGNACIONES_COMPRA_PENDIENTE' 
	 							  WHEN ASIG."Accion" = 'VENTA' THEN 'ASIGNACIONES_VENTA_PENDIENTE' 
	 						  END
	   AND ASIG."FechaLiquidacion" <=:v_FechaMaxima
	   AND ASIG."IdEmpresa" = :p_IdEmpresa;					   
				   

	---------------------------------------------
	-- 1.8 MOVIMIENTOS FACTURACION
	---------------------------------------------
	INSERT INTO "BSCL.Custodia.BD::CUS.DatosFlujoCustodia"(
		"IdDatosFlujoCustodia","KeyFlujoCustodia","IdUsoFlujoCustodia","UsoTipoOperacion","BusquedaEspecifica","IdTipoInstrumentoEspecifico",
		"ItemCalculo","ItemOrigen","FechaEjecucion","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdEmisor","Nemotecnico",
		"NemoSVS","IdMonedaInstrumento","IdTipoReajuste","FechaEmisionInstrumento","FechaVencimientoInstrumento","CodigoIFRS","EsFungible","TipoCarteraCorredor",
		"IdBoveda","IdBolsa","IdTipoOperacion","IdMesa","FechaLiquidacion","Cantidad","ValorRescate","PrecioCompra","TasaCompra","MontoATasaCompra","PrecioMercado",
		"TasaMercado","MontoATasaMercado","IdCliente","IdPortafolio","FechaLiquidacionProceso","IdRegistro","FolioRegistro","IdConcepto","RequiereValorizacion",
		"ValorizarFlujo"
	)
	SELECT  "BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia".NEXTVAL,:p_KeyFlujoCustodia,MZU."IdUsoFlujoCustodia",MZU."UsoTipoOperacion",
			MZU."BusquedaEspecifica",MZU."IdTipoInstrumentoEspecifico",I."ItemCalculo",I."ItemOrigen",:p_FechaSaldo,
		    MOV."IdClasificacionTipoInstrumento",
			MOV."IdTipoInstrumento",
			MOV."IdInstrumento",
			MOV."IdEmisor",
			MOV."Nemotecnico",
			MOV."NemoSVS",
			MOV."IdMoneda" AS "IdMonedaInstrumento",
			MOV."IdTipoReajuste",
			MOV."FechaEmision" AS "FechaEmisionInstrumento",
			MOV."FechaVencimiento" AS "FechaVencimientoInstrumento",
			MOV."CodigoIFRS",
			MOV."EsFungible",
			'' AS "TipoCarteraCorredor",
			MOV."IdBoveda",
			MOV."IdBolsa",
			MOV."IdTipoOperacion",
			0 AS "IdMesa",
			MOV."FechaLiquidacion",
			MOV."Cantidad",
			MOV."ValorRescate",
			MOV."Precio" AS "PrecioCompra",
			MOV."Tasa" AS "TasaCompra",
			MOV."Monto" AS "MontoATasaCompra",
---------------------------------------------------------------------------------------------------------------				
			0 AS "PrecioMercado",
			0 AS "TasaMercado",
			0 AS "MontoATasaMercado",
---------------------------------------------------------------------------------------------------------------				
			MOV."IdCliente",
			MOV."IdPortafolio",
			(CASE WHEN MOV."FechaLiquidacion" >= :v_FechaMinima THEN MOV."FechaLiquidacion" ELSE :v_FechaMinima END) AS "FechaLiquidacionProceso",				
			MOV."IdMovimientoComprobante" as "IdRegistro",
			0 as "FolioRegistro",
			MOV."IdConceptoMovimiento" as "IdConcepto",
			'S' AS "RequiereValorizacion", --CASE WHEN MOV."IdClasificacionTipoInstrumento" = 'RV' THEN 'N' ELSE 'S' END AS "RequiereValorizacion",
			:p_ValorizarFlujo
	  FROM "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteFlujoCustodia" as MOV
	 INNER JOIN :v_FlujoCustodia_MatrizUso as MZU 
   		     ON (	(MZU."IdCliente" IS NULL OR MOV."IdCliente" = MZU."IdCliente") AND
   		     		(MZU."IdPortafolio" IS NULL OR MOV."IdPortafolio" = MZU."IdPortafolio") AND
   		     		(MZU."IdClasificacionTipoInstrumento" IS NULL OR MOV."IdClasificacionTipoInstrumento" = MZU."IdClasificacionTipoInstrumento") AND
   		     		(MZU."IdTipoInstrumento" IS NULL OR MOV."IdTipoInstrumento" = MZU."IdTipoInstrumento") AND
   		     		(MZU."IdInstrumento" IS NULL OR MOV."IdInstrumento" = MZU."IdInstrumento") AND
   		     		(MZU."IdEmisor" IS NULL OR MOV."IdEmisor" = MZU."IdEmisor") AND
   		     		(MZU."IdTipoReajuste" IS NULL OR MOV."IdTipoReajuste" = MZU."IdTipoReajuste") AND
   		     		(MZU."EsFungible" IS NULL OR MOV."EsFungible" = MZU."EsFungible")
   		      	),
		   "BSCL.Custodia.BD::CUS.FlujoCustodia_Items" AS I
	 WHERE I."ItemCalculo" = CASE WHEN MOV."AccionTipoOperacion" = 'COMPRA' THEN 'MOVIMIENTOS_COMPRA_FACTURACION'
	 							  WHEN MOV."AccionTipoOperacion" = 'VENTA' THEN 'MOVIMIENTOS_VENTA_FACTURACION'
	 						  END
	   AND MOV."FechaLiquidacion" <=:v_FechaMaxima
	   AND MOV."IdEmpresa" = :p_IdEmpresa;
	

	---------------------------------------------
	-- 1.9 EVENTOS DE CAPITAL
	---------------------------------------------
	INSERT INTO "BSCL.Custodia.BD::CUS.DatosFlujoCustodia"(
		"IdDatosFlujoCustodia","KeyFlujoCustodia","IdUsoFlujoCustodia","UsoTipoOperacion","BusquedaEspecifica","IdTipoInstrumentoEspecifico",
		"ItemCalculo","ItemOrigen","FechaEjecucion","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdEmisor","Nemotecnico",
		"NemoSVS","IdMonedaInstrumento","IdTipoReajuste","FechaEmisionInstrumento","FechaVencimientoInstrumento","CodigoIFRS","EsFungible","TipoCarteraCorredor",
		"IdBoveda","IdBolsa","IdTipoOperacion","IdMesa","FechaLiquidacion","Cantidad","ValorRescate","PrecioCompra","TasaCompra","MontoATasaCompra","PrecioMercado",
		"TasaMercado","MontoATasaMercado","IdCliente","IdPortafolio","FechaLiquidacionProceso","IdRegistro","FolioRegistro","RequiereValorizacion","ValorizarFlujo"
	)
	SELECT  "BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia".NEXTVAL,:p_KeyFlujoCustodia,MZU."IdUsoFlujoCustodia",MZU."UsoTipoOperacion",
			MZU."BusquedaEspecifica",MZU."IdTipoInstrumentoEspecifico",I."ItemCalculo",I."ItemOrigen",:p_FechaSaldo,
			EVC."IdClasificacionTipoInstrumento",
			EVC."IdTipoInstrumento",
			CASE WHEN EVC."IdTipoEventoCapital" = 'SL' THEN EVC."IdInstrumento" ELSE EVC."IdInstrumentoNuevo" END AS "IdInstrumento",			
			'' AS "IdEmisor",
			EVC."NemoNuevo" AS "Nemotecnico",
			'' AS "NemoSVS",
			EVC."IdMoneda" AS "IdMonedaInstrumento",
			'' AS "IdTipoReajuste",
			'0001-01-01' AS "FechaEmisionInstrumento",
			'0001-01-01' AS "FechaVencimientoInstrumento",
			'' AS "CodigoIFRS",
			'' AS "EsFungible",
			'' AS "TipoCarteraCorredor",
			EVC."IdBoveda",
			'' as "IdBolsa",
			'' AS "IdTipoOperacion",
			0 AS "IdMesa",
			EVC."FechaLiquidacion",
			EVC."Cantidad",
			0 AS "ValorRescate",
			EVC."Precio" AS "PrecioCompra",
			0 AS "TasaCompra",
			EVC."Monto" AS "MontoATasaCompra",
---------------------------------------------------------------------------------------------------------------				
			0 AS "PrecioMercado",
			0 AS "TasaMercado",
			0 AS "MontoATasaMercado",
---------------------------------------------------------------------------------------------------------------				
			EVC."IdCliente",
			EVC."IdPortafolio",
			(CASE WHEN EVC."FechaLiquidacion" >= :v_FechaMinima THEN EVC."FechaLiquidacion" ELSE :v_FechaMinima END) AS "FechaLiquidacionProceso",				
			EVC."IdRegistro",
			EVC."FolioRegistro",
			'S' AS "RequiereValorizacion", --CASE WHEN EVC."IdClasificacionTipoInstrumento" = 'RV' THEN 'N' ELSE 'S' END AS "RequiereValorizacion",
			:p_ValorizarFlujo
	  FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteFlujoCustodia" AS EVC
	 INNER JOIN :v_FlujoCustodia_MatrizUso as MZU 
   		     ON (	(MZU."IdCliente" IS NULL OR EVC."IdCliente" = MZU."IdCliente") AND
   		     		(MZU."IdPortafolio" IS NULL OR EVC."IdPortafolio" = MZU."IdPortafolio") AND
   		     		(MZU."IdClasificacionTipoInstrumento" IS NULL OR EVC."IdClasificacionTipoInstrumento" = MZU."IdClasificacionTipoInstrumento") AND
   		     		(MZU."IdTipoInstrumento" IS NULL OR EVC."IdTipoInstrumento" = MZU."IdTipoInstrumento") AND
   		     		(MZU."IdInstrumento" IS NULL OR EVC."IdInstrumento" = MZU."IdInstrumento")
   		     	),
		   "BSCL.Custodia.BD::CUS.FlujoCustodia_Items" AS I
	 WHERE I."ItemCalculo" = CASE WHEN EVC."IdTipoEventoCapital" = 'SU' THEN 'SUBSCRIPCIONES'	--1.9.1
	 							  WHEN EVC."IdTipoEventoCapital" = 'SL' THEN 'SORTEO_LETRAS'	--1.9.2
	 						  END
	   AND EVC."IdTipoEventoCapital" IN ('SU','SL')
	   AND EVC."IdEstado" IN ('ING','VIG','NOT')
	   AND EVC."FechaLiquidacion" <=:v_FechaMaxima
	   AND EVC."IdEmpresa" = :p_IdEmpresa;


	---------------------------------------------
	-- 1.10 FONDOS MUTUOS
	---------------------------------------------
	INSERT INTO "BSCL.Custodia.BD::CUS.DatosFlujoCustodia"(
		"IdDatosFlujoCustodia","KeyFlujoCustodia","IdUsoFlujoCustodia","UsoTipoOperacion","BusquedaEspecifica","IdTipoInstrumentoEspecifico",
		"ItemCalculo","ItemOrigen","FechaEjecucion","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","IdEmisor","Nemotecnico","NemoSVS",
		"IdMonedaInstrumento","IdTipoReajuste","FechaEmisionInstrumento","FechaVencimientoInstrumento","CodigoIFRS","EsFungible","TipoCarteraCorredor","IdBoveda",
		"IdBolsa","IdTipoOperacion","IdMesa","FechaLiquidacion","Cantidad","ValorRescate","PrecioCompra","TasaCompra","MontoATasaCompra","PrecioMercado","TasaMercado",
		"MontoATasaMercado","IdCliente","IdPortafolio","FechaLiquidacionProceso","IdRegistro","FolioRegistro","IdConcepto","RequiereValorizacion","ValorizarFlujo"
	)
	SELECT  "BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia".NEXTVAL,:p_KeyFlujoCustodia,MZU."IdUsoFlujoCustodia",MZU."UsoTipoOperacion",
			MZU."BusquedaEspecifica",MZU."IdTipoInstrumentoEspecifico",I."ItemCalculo",I."ItemOrigen",:p_FechaSaldo,
			FFMM."IdClasificacionInstrumento" as "IdClasificacionTipoInstrumento",
	        FFMM."IdTipoInstrumento",
	        FFMM."IdInstrumento",
	        FFMM."IdEmisor",
	        FFMM."Nemotecnico",
	        FFMM."NemoSVS",
	        FFMM."IdMonedaInstrumento",
	        FFMM."IdTipoReajuste",
	        FFMM."FechaEmisionInstrumento",	
	        FFMM."FechaVencimientoInstrumento",
	        FFMM."CodigoIFRS",
	        FFMM."EsFungible",
	        '' AS "TipoCarteraCorredor",
	        FFMM."IdBoveda",
	        FFMM."IdBolsa",
	        FFMM."IdTipoOperacion",	
	        0 as "IdMesa",
	        FFMM."FechaLiquidacion",
	        FFMM."Cantidad",	
	        FFMM."ValorRescate",
	        FFMM."PrecioCompra",	
	        FFMM."TasaCompra",
	        FFMM."MontoATasaCompra",
---------------------------------------------------------------------------------------------------------------		        
	        0 as "PrecioMercado",	
	        0 as "TasaMercado",
	        0 as "MontoATasaMercado",
---------------------------------------------------------------------------------------------------------------		        
	        FFMM."IdCliente",
	        FFMM."IdPortafolio",
	        (CASE WHEN FFMM."FechaLiquidacion" >= :v_FechaMinima THEN FFMM."FechaLiquidacion" ELSE :v_FechaMinima END) AS "FechaLiquidacionProceso",
	        FFMM."IdRegistro",	
	        0 as "FolioRegistro",
	        FFMM."IdConcepto",
	        'S' AS "RequiereValorizacion", --CASE WHEN FFMM."IdClasificacionInstrumento" = 'RV' THEN 'N' ELSE 'S' END AS "RequiereValorizacion",
			:p_ValorizarFlujo
	  FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateFlujoCustodia" as FFMM
	 INNER JOIN :v_FlujoCustodia_MatrizUso as MZU 
   		     ON (	(MZU."IdCliente" IS NULL OR FFMM."IdCliente" = MZU."IdCliente") AND
   		     		(MZU."IdPortafolio" IS NULL OR FFMM."IdPortafolio" = MZU."IdPortafolio") AND
   		     		(MZU."IdClasificacionTipoInstrumento" IS NULL OR FFMM."IdClasificacionInstrumento" = MZU."IdClasificacionTipoInstrumento") AND
   		     		(MZU."IdTipoInstrumento" IS NULL OR FFMM."IdTipoInstrumento" = MZU."IdTipoInstrumento") AND
   		     		(MZU."IdInstrumento" IS NULL OR FFMM."IdInstrumento" = MZU."IdInstrumento") AND
   		     		(MZU."IdEmisor" IS NULL OR FFMM."IdEmisor" = MZU."IdEmisor") AND
   		     		(MZU."IdTipoReajuste" IS NULL OR FFMM."IdTipoReajuste" = MZU."IdTipoReajuste") AND
   		     		(MZU."EsFungible" IS NULL OR FFMM."EsFungible" = MZU."EsFungible")
   		     	),
		   "BSCL.Custodia.BD::CUS.FlujoCustodia_Items" AS I
	 WHERE I."ItemCalculo" = CASE WHEN FFMM."IdTipoOperacion" = 'APORFM' THEN 'APORTE_FONDOS_MUTUOS'
	 							  WHEN FFMM."IdTipoOperacion" = 'RESCFM' THEN 'RESCATE_FONDOS_MUTUOS'
	 						  END
	   AND FFMM."FechaLiquidacion" <=:v_FechaMaxima
	   AND FFMM."IdEstado"	= 'ING'
	   AND FFMM."IdEmpresa" = :p_IdEmpresa;

	
   	---------------------------------------------
	-- ACTUALIZACION MASIVA
	---------------------------------------------
	UPDATE DFC
	   SET   "Accion"					= TOPER."Accion"
	   		,"IdTipoProducto"			= TOPER."IdTipoProducto"
	   		,"FuncionOperador"			= UI."FuncionOperador"
	   		,"TipoValorizacion"			= INS."TipoValorizacion"
			,"CantidadProceso"			= ((CASE WHEN UI."FuncionOperador" = '-' THEN -1 ELSE 1 END) * DFC."Cantidad")
			,"ValorRescateProceso"		= ((CASE WHEN UI."FuncionOperador" = '-' THEN -1 ELSE 1 END) * DFC."ValorRescate")			
			,"PrecioCompraProceso"		= DFC."PrecioCompra"
			,"TasaCompraProceso"		= DFC."TasaCompra"
			,"MontoCompraProceso"		= ((CASE WHEN UI."FuncionOperador" = '-' THEN -1 ELSE 1 END) * DFC."MontoATasaCompra")
			,"PrecioMercadoProceso"		= DFC."PrecioMercado"
			,"TasaMercadoProceso"		= DFC."TasaMercado"
			,"MontoMercadoProceso"		= ((CASE WHEN UI."FuncionOperador" = '-' THEN -1 ELSE 1 END) * DFC."MontoATasaMercado")			
			,"EsCuentaPropia"			= (CASE WHEN COALESCE(PCP."IdPortafolio",0) = 0 THEN 'N' ELSE 'S' END)
			,"TipoRegistro" 			= (CASE WHEN (UI."FuncionOperador" = '') THEN 'D'
											WHEN (DFC."TipoCarteraCorredor" = 'CC') THEN 'D'
											WHEN (TOTI."IdTipoOperacion" IS NULL AND DFC."BusquedaEspecifica" = '') THEN 'D'
											WHEN (TIPG."IdTipoInstrumentoAval" IS NULL AND DFC."BusquedaEspecifica" = 'SOLO_GARANTIZABLE') THEN 'D'
											WHEN (INSUB."IdInstrumento" IS NULL AND DFC."BusquedaEspecifica" = 'SOLO_SUBYACENTE') THEN 'D'											
											ELSE "TipoRegistro"
										   END)
			,"MotivoEliminacioRegistro"	= (CASE WHEN (UI."FuncionOperador" = '') THEN 'CONFIGURACION_SIN_FUNCION_OPERADOR' 
											WHEN (DFC."TipoCarteraCorredor" = 'CC') THEN 'SE_EXCLUYE_CARTERA_COMPROMETIDA'
											WHEN (TOTI."IdTipoOperacion" IS NULL AND DFC."BusquedaEspecifica" = '') THEN 'CONFIGURACION_TIPO_OPERACION_EXCLUYE_INSTRUMENTO'
											WHEN (TIPG."IdTipoInstrumentoAval" IS NULL AND DFC."BusquedaEspecifica" = 'SOLO_GARANTIZABLE') THEN 'CONFIGURACION_GARANTIA_EXCLUYE_INSTRUMENTO'
											WHEN (INSUB."IdInstrumento" IS NULL AND DFC."BusquedaEspecifica" = 'SOLO_SUBYACENTE') THEN 'CONFIGURACION_SUBYACENTE_EXCLUYE_INSTRUMENTO'											
											ELSE ''
										   END)
	  FROM "BSCL.Custodia.BD::CUS.DatosFlujoCustodia" AS DFC
	 INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = DFC."IdInstrumento"
	 INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TOPER ON (TOPER."IdTipoOperacion" = DFC."IdTipoOperacion")
	 INNER JOIN "BSCL.Custodia.BD::CUS.ItemsFlujoCustodia" AS UI ON UI."IdUsoFlujoCustodia" = DFC."IdUsoFlujoCustodia"
	  LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia" as PCP ON PCP."IdEmpresa" = :p_IdEmpresa AND PCP."IdPortafolio" = DFC."IdPortafolio"	  
	  -- Consulta que puedo incluir en el flujo 
	  LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" as TOTI 
	  		 ON TOTI."IdTipoOperacion" = DFC."UsoTipoOperacion" AND TOTI."IdTipoInstrumento" = DFC."IdTipoInstrumento"
	  -- Consulta con que puedo garantizar
	  LEFT JOIN "BSCL.Custodia.BD.Vistas::GTA.ConsultarTipoInstrumentoParaGarantia" as TIPG 
	  		 ON TIPG."IdTipoInstrumentoGarantizado" = DFC."IdTipoInstrumentoEspecifico" AND TIPG."IdTipoInstrumentoAval" = DFC."IdTipoInstrumento"
	  		AND TIPG."IdEstadoBolsa" = 'ACT' AND TIPG."ExigeGarantia" = 'S' AND TIPG."IdEstadoTipoInstrumentoAval" = 'VIG' AND TIPG."IdEstadoAval" = 'ACT'
	  -- Consulta que puedo ingresar como subyacente
	  LEFT JOIN (SELECT ISU."IdTipoInstrumento",ISU."IdTipoInstrumentoSubyacente",ISU."IdInstrumento"
	  			   FROM "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente" AS TIS
	  			  INNER JOIN "BSCL.Plataforma.BD::INS.InstrumentoSubyacente" AS ISU
		 		 		  ON ISU."IdTipoInstrumento" = TIS."IdTipoInstrumento"
						 AND ISU."IdBolsa" = TIS."IdBolsa"
						 AND ISU."IdTipoInstrumentoSubyacente" = TIS."IdTipoInstrumentoSubyacente"
	  			) as INSUB
	  		 ON INSUB."IdTipoInstrumento" = DFC."IdTipoInstrumentoEspecifico" 
	  		AND INSUB."IdTipoInstrumentoSubyacente" = DFC."IdTipoInstrumento"
	  		AND INSUB."IdInstrumento" = DFC."IdInstrumento"
	 WHERE DFC."KeyFlujoCustodia" = :p_KeyFlujoCustodia
	   AND DFC."TipoRegistro" = 'R'
	   AND DFC."ItemCalculo" = UI."ItemCalculo"
	 WITH HINT( IGNORE_PLAN_CACHE );
	
	
	---------------------------------------------
	-- INGRESO DE DATOS PROYECTADOS
	---------------------------------------------
	INSERT INTO "BSCL.Custodia.BD::CUS.DatosFlujoCustodia"(
		   "IdDatosFlujoCustodia","KeyFlujoCustodia","IdUsoFlujoCustodia","UsoTipoOperacion","BusquedaEspecifica","IdTipoInstrumentoEspecifico","ItemCalculo",
		   "ItemOrigen","FuncionOperador","FechaEjecucion","IdCliente","IdPortafolio","IdRegistro","FolioRegistro","IdConcepto","IdClasificacionTipoInstrumento",
		   "IdTipoInstrumento","IdInstrumento","TipoValorizacion","IdEmisor","Nemotecnico","NemoSVS","IdMonedaInstrumento","IdTipoReajuste","FechaEmisionInstrumento",
		   "FechaVencimientoInstrumento","CodigoIFRS","EsFungible","IdBoveda","IdBolsa","IdTipoOperacion","Accion","IdTipoProducto","IdMesa","FechaLiquidacion",
		   "Cantidad","ValorRescate","PrecioCompra","TasaCompra","MontoATasaCompra","PrecioMercado","TasaMercado","MontoATasaMercado",		  
		   "CantidadProceso","ValorRescateProceso","PrecioCompraProceso","TasaCompraProceso","MontoCompraProceso","PrecioMercadoProceso","TasaMercadoProceso",
		   "MontoMercadoProceso","FechaLiquidacionProceso","RequiereValorizacion","TipoRegistro","ValorizarFlujo","EsCuentaPropia"
	)
	SELECT "BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia".NEXTVAL AS "IdDatosFlujoCustodia",
		   "KeyFlujoCustodia","IdUsoFlujoCustodia","UsoTipoOperacion","BusquedaEspecifica","IdTipoInstrumentoEspecifico","ItemCalculo","ItemOrigen","FuncionOperador",
		   "FechaEjecucion","IdCliente","IdPortafolio","IdRegistro","FolioRegistro","IdConcepto","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento",
		   "TipoValorizacion","IdEmisor","Nemotecnico","NemoSVS","IdMonedaInstrumento","IdTipoReajuste","FechaEmisionInstrumento","FechaVencimientoInstrumento",
		   "CodigoIFRS","EsFungible","IdBoveda","IdBolsa","IdTipoOperacion","Accion","IdTipoProducto","IdMesa","FechaLiquidacion","Cantidad","ValorRescate",
		   "PrecioCompra","TasaCompra","MontoATasaCompra","PrecioMercado","TasaMercado","MontoATasaMercado",
		   CASE WHEN "FechaLiquidacionProceso" > "Fecha" THEN 0 ELSE "CantidadProceso" END AS "CantidadProceso",
		   CASE WHEN "FechaLiquidacionProceso" > "Fecha" THEN 0 ELSE "ValorRescateProceso" END AS "ValorRescateProceso",		   
		   CASE WHEN "FechaLiquidacionProceso" > "Fecha" THEN 0 ELSE "PrecioCompraProceso" END AS "PrecioCompraProceso",
		   CASE WHEN "FechaLiquidacionProceso" > "Fecha" THEN 0 ELSE "TasaCompraProceso" END AS "TasaCompraProceso",
		   CASE WHEN "FechaLiquidacionProceso" > "Fecha" THEN 0 ELSE "MontoCompraProceso" END AS "MontoCompraProceso",
		   CASE WHEN "FechaLiquidacionProceso" > "Fecha" THEN 0 ELSE "PrecioMercadoProceso" END AS "PrecioMercadoProceso",
		   CASE WHEN "FechaLiquidacionProceso" > "Fecha" THEN 0 ELSE "TasaMercadoProceso" END AS "TasaMercadoProceso",
		   CASE WHEN "FechaLiquidacionProceso" > "Fecha" THEN 0 ELSE "MontoMercadoProceso" END AS "MontoMercadoProceso",
		   F."Fecha" as "FechaLiquidacionProceso","RequiereValorizacion",'P' AS "TipoRegistro","ValorizarFlujo","EsCuentaPropia"
      FROM "BSCL.Custodia.BD::CUS.DatosFlujoCustodia" as DFC, "BSCL.Custodia.BD::CUS.FlujoCustodia_Fechas" as F
     WHERE "KeyFlujoCustodia" = :p_KeyFlujoCustodia
       AND "TipoRegistro" = 'R'
       AND "FechaLiquidacionProceso" <> F."Fecha";
       
END;
GO