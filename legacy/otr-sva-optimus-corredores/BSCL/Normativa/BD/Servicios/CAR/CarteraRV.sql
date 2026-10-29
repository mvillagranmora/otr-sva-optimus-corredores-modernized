SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CarteraRV";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CarteraRV" (
    IN p_IdModulo 					NVARCHAR(40),
    IN p_IdUsuario 					BIGINT,
    IN p_IdEmpresa 					BIGINT,
    IN p_FechaDesde 				DATE,
    IN p_FechaHasta 				DATE,
    IN p_FiltroUbicacionBoveda		NVARCHAR(500) DEFAULT NULL,
    IN p_FiltroSoloMonedaSistema	NVARCHAR(1) DEFAULT 'S',
    IN p_Datos 						TABLE ("IdCliente" BIGINT, "IdPortafolio" BIGINT)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL" READS SQL DATA
AS
BEGIN
	
	v_MonedasDelSistema = 
	SELECT  "PLA.MonedaSistema",
			CASE WHEN "PLA.MonedaAdicional" IS NULL OR "PLA.MonedaAdicional" = '' THEN 'USD' ELSE "PLA.MonedaAdicional" END "PLA.MonedaAdicional" 
	  FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionPivot"
	 WHERE "IdEmpresa" = :p_IdEmpresa;
	
	v_Cartera =
	SELECT 	 CAR."IdCliente"
			,CAR."IdPortafolio"
			,CAR."IdCartera"
			,CAR."FolioCartera"
			,CAR."INGFechaOperacion" AS "FechaOperacion"
			,CAR."INGFechaLiquidacion" as "FechaLiquidacion"
			,CAR."IdBoveda"
			,CAR."IdClasificacionTipoInstrumento"
			,CAR."IdTipoInstrumento"
			,CAR."IdInstrumento"
			,CAR."Nemotecnico"
			,CAR."Cantidad" AS "CantidadOriginal"
			,CAR."INGCantidad" AS "Cantidad"
			,CAR."IdMonedaInstrumento"
			--------------------------------------------------------------------------------------------
			,CAR."TasaCompra"
			,CAR."PrecioCompra"
			,CAR."INGPrecioPromedioCompra" AS "PrecioPromedioCompra"
			,(ROUND(CAR."INGPrecioPromedioCompra",2)*CAR."Cantidad") AS "MontoPromedioCompraMonedaSistema"
			,((CAR."INGMontoPrecioPromedioMonedaAdicional"*CAR."Cantidad")/CAR."INGCantidad") AS "MontoPromedioCompraMonedaAdicional" 
			--,CAR."MontoCompra" AS "MontoCompraMonedaSistema"
			,CAR."INGTasaMercadoActual" AS "TasaMercado"			
			,CAR."INGPrecioMercadoActual" as "PrecioMercado"			
			,(ROUND(CAR."INGPrecioMercadoActual",2)*CAR."Cantidad") AS "MontoMercadoMonedaSistema"  --,CAR."INGMontoATasaMercadoActual" AS "MontoMercadoMonedaSistema"
			,((CAR."INGMontoMercadoActualMonedaAdicional"*CAR."Cantidad")/CAR."INGCantidad") AS "MontoMercadoMonedaAdicional" --,CAR."INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
	FROM :p_Datos DATOS
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" CAR ON DATOS."IdPortafolio" = CAR."IdPortafolio"
	WHERE CAR."IdClasificacionTipoInstrumento" = 'RV'
	  AND CAR."IdEmpresa" = :p_IdEmpresa
	  AND CAR."IdEstado" = 'VIG'
	  AND CAR."FechaOperacion" <= :p_FechaHasta
	  AND CAR."EGRCantidad" = 0;
	  
	
	  
	V_MovimientosVigentesEnCartera =  	  
	WITH 
	Cartera AS (
		SELECT 	 
			 "IdCliente"
			,"IdPortafolio"
			,"FolioCartera"
			,"FechaOperacion"
			,"FechaLiquidacion"
			,"IdBoveda"
			,"IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento"
			,"IdInstrumento"
			,"Nemotecnico"	
			,"CantidadOriginal"
			,"Cantidad"  AS "CantidadCartera"
			,"IdMonedaInstrumento"
			--------------------------------------------------------------------------------------------
			,"PrecioPromedioCompra"
			,"MontoPromedioCompraMonedaSistema"
			,"MontoPromedioCompraMonedaAdicional"
			,"TasaMercado"
			,"PrecioMercado"
			,"MontoMercadoMonedaSistema"
			,"MontoMercadoMonedaAdicional"
		FROM :v_Cartera
		WHERE "IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
	),
	GarantiaAsociadaACartera AS (
		SELECT   CAR."FolioCartera"
				,SUM(GAR."INGCantidad") AS "CantidadGarantia"
		FROM :v_Cartera CAR
		INNER JOIN "BSCL.Custodia.BD.Vistas::GTA.ConsultarTablaGarantia" GAR 
				ON GAR."FolioCarteraOrigen" = CAR."FolioCartera"	
		WHERE GAR."IdEstado" = 'VIG'
		GROUP BY CAR."FolioCartera"
	),
	OperacionesAPlazoAsociadoACartera AS (
		SELECT   CAR."FolioCartera"
				,SUM(SUB."INGCantidad") AS "CantidadAPlazo"
		FROM :v_Cartera CAR
		INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"	
		WHERE SUB."IdEstado" = 'VIG'
		  AND CAR."IdTipoInstrumento" IN ('SIM','PAC')
		GROUP BY CAR."FolioCartera"
	),
	ReporteAsociadoACartera AS (
		SELECT   CAR."FolioCartera"
				,SUM(SUB."INGCantidad") AS "CantidadReporte"
		FROM :v_Cartera CAR
		INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"	
		WHERE SUB."IdEstado" = 'VIG'
		  AND CAR."IdTipoInstrumento" IN ('RRV','RRF')
		GROUP BY CAR."FolioCartera"
	),
	PrestamoEmprestitoAsociadoACartera AS (
		SELECT   CAR."FolioCartera"
				,SUM(SUB."INGCantidad") AS "CantidadPrestamo"
		FROM :v_Cartera CAR
		INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"	
		WHERE SUB."IdEstado" = 'VIG'
		  AND CAR."IdTipoInstrumento" IN ('PRT','EMP')
		GROUP BY CAR."FolioCartera"
	)
	SELECT   C."IdCliente"
			,C."IdPortafolio"
			,C."IdClasificacionTipoInstrumento"
			,C."IdTipoInstrumento"
			,C."IdBoveda"
			,C."IdInstrumento"
			,C."Nemotecnico"
			,IFNULL(SUM(C."CantidadOriginal"),0) AS "CantidadOriginal"
			,IFNULL(SUM(C."CantidadCartera"),0) AS "CantidadCartera"
			,IFNULL(SUM(G."CantidadGarantia"),0) AS "CantidadGarantia"
			,IFNULL(SUM(O."CantidadAPlazo"),0) AS "CantidadAPlazo"
			,IFNULL(SUM(R."CantidadReporte"),0) AS "CantidadReporte"
			,IFNULL(SUM(P."CantidadPrestamo"),0) AS "CantidadPrestamo"
			,C."IdMonedaInstrumento"
			--------------------------------------------------------------------------------------------
			,C."PrecioPromedioCompra"
			,IFNULL(SUM(C."MontoPromedioCompraMonedaSistema"),0) AS "MontoPromedioCompraMonedaSistema"
			,IFNULL(SUM(C."MontoPromedioCompraMonedaAdicional"),0) AS "MontoPromedioCompraMonedaAdicional"
			,C."TasaMercado"
			,C."PrecioMercado"
			,IFNULL(SUM(C."MontoMercadoMonedaSistema"),0) AS "MontoMercadoMonedaSistema"
			,IFNULL(SUM(C."MontoMercadoMonedaAdicional"),0) AS "MontoMercadoMonedaAdicional"
	FROM Cartera AS C
	FULL OUTER JOIN GarantiaAsociadaACartera G ON G."FolioCartera" = C."FolioCartera"
	FULL OUTER JOIN OperacionesAPlazoAsociadoACartera O ON O."FolioCartera" = C."FolioCartera"
	FULL OUTER JOIN ReporteAsociadoACartera R ON R."FolioCartera" = C."FolioCartera"
	FULL OUTER JOIN PrestamoEmprestitoAsociadoACartera P ON P."FolioCartera" = C."FolioCartera"
	GROUP BY C."IdCliente",C."IdPortafolio",C."IdClasificacionTipoInstrumento",C."IdTipoInstrumento",C."IdBoveda",C."IdInstrumento",C."Nemotecnico"
			,C."IdMonedaInstrumento",C."PrecioPromedioCompra",C."TasaMercado",C."PrecioMercado";
			
			
	V_MovimientosVigentesEnGarantia =
	WITH 
	Garantia AS (
		SELECT 	 CAR."IdCliente"
				,CAR."IdPortafolio"
				,CAR."FolioCartera"
				,GAR."INGFechaOperacion" AS "FechaOperacion"
				,GAR."INGFechaLiquidacion" as "FechaLiquidacion"
				,CAR."IdBoveda"
				,GAR."IdClasificacionTipoInstrumento"
				,GAR."IdTipoInstrumento"
				,GAR."IdInstrumento"
				,GAR."Nemotecnico"
				,CAR."Cantidad" AS "CantidadOriginal"
				,GAR."INGCantidad" AS "Cantidad"
				,GAR."IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,CAR."INGTasaCompra" AS "TasaCompra"
				,CAR."INGPrecioCompra" AS "PrecioCompra"
				,CAR."INGPrecioPromedioCompra" AS "PrecioPromedioCompra"
				,GAR."MontoPrecioPromedioCompraOperacionOrigen" AS "MontoPromedioCompraMonedaSistema"
				,GAR."MontoPrecioPromedioMonedaAdicionalOperacionOrigen" AS "MontoPromedioCompraMonedaAdicional"				
				,GAR."INGTasaMercadoActual" AS "TasaMercado"
				,GAR."INGPrecioMercadoActual" as "PrecioMercado"
				,GAR."INGMontoATasaMercadoActual" AS "MontoMercadoMonedaSistema"
				,GAR."INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
		FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarTablaGarantia" AS GAR
		INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" AS CAR ON CAR."FolioCartera" = GAR."FolioCarteraOrigen"
		INNER JOIN (
					SELECT C."FolioCartera",C."IdCliente",C."IdPortafolio"
					FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" C
					INNER JOIN :p_Datos D ON D."IdPortafolio" = C."IdPortafolio"
					WHERE C."IdEstado" = 'LIQ'
					EXCEPT 
					SELECT "FolioCartera","IdCliente","IdPortafolio"
					FROM :v_Cartera				
				 	) AS CEX ON CEX."FolioCartera" = CAR."FolioCartera"
		INNER JOIN :p_Datos DATOS ON DATOS."IdPortafolio" = CAR."IdPortafolio"	
		WHERE GAR."IdClasificacionTipoInstrumento" = 'RV'
		  AND GAR."IdEstado" = 'VIG'
		  AND GAR."IdEmpresa" = :p_IdEmpresa
		  AND GAR."INGFechaOperacion" <= :p_FechaHasta
	)
	SELECT   "IdCliente","IdPortafolio","IdBoveda","IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal","TasaCompra","PrecioCompra"
			,"PrecioPromedioCompra"
			,IFNULL(SUM("MontoPromedioCompraMonedaSistema"),0) AS "MontoPromedioCompraMonedaSistema"
			,IFNULL(SUM("MontoPromedioCompraMonedaAdicional"),0) AS "MontoPromedioCompraMonedaAdicional"
			,"TasaMercado","PrecioMercado"
			,IFNULL(SUM("Cantidad"),0) AS "Cantidad"
			,IFNULL(SUM("MontoMercadoMonedaSistema"),0) AS "MontoMercadoMonedaSistema"
			,IFNULL(SUM("MontoMercadoMonedaAdicional"),0) AS "MontoMercadoMonedaAdicional"
	FROM Garantia
	GROUP BY "IdCliente","IdPortafolio","IdBoveda","IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal","TasaCompra","PrecioCompra","PrecioPromedioCompra"
			,"TasaMercado","PrecioMercado";


	
	V_MovimientosVigentesAPlazo =
	WITH 
	APlazo AS (
		SELECT 	 CAR."IdCliente"
				,CAR."IdPortafolio"
				,CAR."FolioCartera"
				,PLZ."INGFechaOperacion" AS "FechaOperacion"
				,CAR."INGFechaLiquidacion" as "FechaLiquidacion"
				,CAR."IdBoveda"
				,PLZ."IdClasificacionTipoInstrumento"
				,PLZ."IdTipoInstrumento"
				,PLZ."IdInstrumento"
				,PLZ."Nemotecnico"
				,CAR."INGCantidad" AS "CantidadOriginal"
				,PLZ."INGCantidad" AS "Cantidad"
				,PLZ."IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,CAR."INGTasaCompra" AS "TasaCompra" 
				,CAR."INGPrecioCompra" AS "PrecioCompra"
				,CAR."INGPrecioPromedioCompra" AS "PrecioPromedioCompra"
				,CAR."INGMontoATasaCompraActual" AS "MontoCompraMonedaSistema"
				,0 AS "MontoPromedioCompraMonedaSistema"
				,0 AS "MontoPromedioCompraMonedaAdicional"
				,PLZ."INGTasaMercadoActual" AS "TasaMercado"
				,PLZ."INGPrecioMercadoActual" as "PrecioMercado"
				,PLZ."INGMontoATasaMercadoActual" AS "MontoMercadoMonedaSistema"
				,PLZ."INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente" PLZ
		INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" CAR ON CAR."FolioCartera" = PLZ."FolioCarteraRelacionada"
		INNER JOIN (
					SELECT C."IdCartera",C."FolioCartera",C."IdCliente",C."IdPortafolio"
					FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" C
					INNER JOIN :p_Datos D ON D."IdPortafolio" = C."IdPortafolio"
					WHERE C."IdEmpresa" = :p_IdEmpresa
					  AND C."IdTipoInstrumento" IN ('SIM','PRT','EMP','PAC')
					  AND C."IdEstado" = 'LIQ'
					EXCEPT 
					SELECT  "IdCartera","FolioCartera","IdCliente","IdPortafolio"
					FROM :v_Cartera
					WHERE "IdTipoInstrumento" IN ('SIM','PRT','EMP','PAC')
				 	) AS CEX ON CEX."IdCartera" = CAR."IdCartera"
		INNER JOIN :p_Datos DATOS ON DATOS."IdPortafolio" = CAR."IdPortafolio"
		WHERE PLZ."IdClasificacionTipoInstrumento" = 'RV'
		  AND PLZ."IdEstado" = 'VIG'
		  AND PLZ."INGFechaOperacion" <= :p_FechaHasta
	)
	SELECT   "IdCliente","IdPortafolio","IdBoveda","IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal","TasaCompra","PrecioCompra"
			,"PrecioPromedioCompra"
			,IFNULL(SUM("MontoPromedioCompraMonedaSistema"),0) AS "MontoPromedioCompraMonedaSistema"
			,IFNULL(SUM("MontoPromedioCompraMonedaAdicional"),0) AS "MontoPromedioCompraMonedaAdicional"
			,"TasaMercado","PrecioMercado"
			,IFNULL(SUM("Cantidad"),0) AS "Cantidad"
			,IFNULL(SUM("MontoMercadoMonedaSistema"),0) AS "MontoMercadoMonedaSistema"
			,IFNULL(SUM("MontoMercadoMonedaAdicional"),0) AS "MontoMercadoMonedaAdicional"
	FROM APlazo
	GROUP BY "IdCliente","IdPortafolio","IdBoveda","IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal","TasaCompra","PrecioCompra"
			,"PrecioPromedioCompra","TasaMercado","PrecioMercado";
	  
	
	V_MovimientosVigentesPrestamosEmprestitos =
	WITH 
	APlazo AS (
		SELECT 	 CAR."IdCliente"
				,CAR."IdPortafolio"
				,CAR."FolioCartera"
				,PLZ."INGFechaOperacion" AS "FechaOperacion"
				,CAR."INGFechaLiquidacion" as "FechaLiquidacion"
				,CAR."IdBoveda"
				,PLZ."IdClasificacionTipoInstrumento"
				,PLZ."IdTipoInstrumento"
				,PLZ."IdInstrumento"
				,PLZ."Nemotecnico"
				,CAR."INGCantidad" AS "CantidadOriginal"
				,PLZ."Cantidad"
				,PLZ."IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,CAR."INGTasaCompra" AS "TasaCompra" 
				,CAR."INGPrecioCompra" AS "PrecioCompra"
				,CAR."INGPrecioPromedioCompra" AS "PrecioPromedioCompra"
				,CAR."INGMontoATasaCompraActual" AS "MontoCompraMonedaSistema"
				,0 AS "MontoPromedioCompraMonedaSistema"
				,0 AS "MontoPromedioCompraMonedaAdicional"
				,PLZ."INGTasaMercadoActual" AS "TasaMercado"
				,PLZ."INGPrecioMercadoActual" as "PrecioMercado"
				,PLZ."INGMontoATasaMercadoActual" AS "MontoMercadoMonedaSistema"
				,PLZ."INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente" PLZ
		INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" CAR ON CAR."FolioCartera" = PLZ."FolioCarteraRelacionada"
		INNER JOIN (
					SELECT C."IdCartera",C."FolioCartera",C."IdCliente",C."IdPortafolio"
					FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" C
					INNER JOIN :p_Datos D ON D."IdPortafolio" = C."IdPortafolio"
					WHERE C."IdEmpresa" = :p_IdEmpresa
					  AND C."IdTipoInstrumento" IN ('PRT','EMP')
					  AND C."IdEstado" = 'LIQ'
					EXCEPT 
					SELECT  "IdCartera","FolioCartera","IdCliente","IdPortafolio"
					FROM :v_Cartera
					WHERE "IdTipoInstrumento" IN ('PRT','EMP')
				 	) AS CEX ON CEX."IdCartera" = CAR."IdCartera"
		INNER JOIN :p_Datos DATOS ON DATOS."IdPortafolio" = CAR."IdPortafolio"
		WHERE PLZ."IdClasificacionTipoInstrumento" = 'RV'
		  AND PLZ."IdEstado" = 'VIG'
		  AND PLZ."INGFechaOperacion" <= :p_FechaHasta
	)
	SELECT   "IdCliente","IdPortafolio","IdBoveda","IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal","TasaCompra","PrecioCompra"
			,"PrecioPromedioCompra"
			,IFNULL(SUM("MontoPromedioCompraMonedaSistema"),0) AS "MontoPromedioCompraMonedaSistema"
			,IFNULL(SUM("MontoPromedioCompraMonedaAdicional"),0) AS "MontoPromedioCompraMonedaAdicional"
			,"TasaMercado","PrecioMercado"
			,IFNULL(SUM("Cantidad"),0) AS "Cantidad"
			,IFNULL(SUM("MontoMercadoMonedaSistema"),0) AS "MontoMercadoMonedaSistema"
			,IFNULL(SUM("MontoMercadoMonedaAdicional"),0) AS "MontoMercadoMonedaAdicional"
	FROM APlazo
	GROUP BY "IdCliente","IdPortafolio","IdBoveda","IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal","TasaCompra","PrecioCompra"
			,"PrecioPromedioCompra","TasaMercado","PrecioMercado";		
			
	V_MovimientosVigentesEnReporte =
	WITH 
	Reporte AS (
		SELECT 	 CAR."IdCliente"
				,CAR."IdPortafolio"
				,CAR."FolioCartera"
				,REP."INGFechaOperacion" AS "FechaOperacion"
				,CAR."INGFechaLiquidacion" as "FechaLiquidacion"
				,CAR."IdBoveda"
				,REP."IdClasificacionTipoInstrumento"
				,REP."IdTipoInstrumento"
				,REP."IdInstrumento"
				,REP."Nemotecnico"
				,CAR."INGCantidad" AS "CantidadOriginal"
				,REP."Cantidad"
				,REP."IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,CAR."INGTasaCompra" AS "TasaCompra"
				,CAR."INGPrecioCompra" AS "PrecioCompra"
				,CAR."INGPrecioPromedioCompra" AS "PrecioPromedioCompra"				
				,CAR."INGMontoATasaCompraActual" AS "MontoCompraMonedaSistema"
				,0 AS "MontoPromedioCompraMonedaSistema"
				,0 AS "MontoPromedioCompraMonedaAdicional"
				,REP."INGTasaMercadoActual" AS "TasaMercado"
				,REP."INGPrecioMercadoActual" as "PrecioMercado"
				,REP."INGMontoATasaMercadoActual" AS "MontoMercadoMonedaSistema"
				,REP."INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente" REP
		INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" CAR ON CAR."FolioCartera" = REP."FolioCarteraRelacionada"
		INNER JOIN (
					SELECT C."IdCartera",C."IdCliente",C."IdPortafolio"
					FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" C
					INNER JOIN :p_Datos D ON D."IdPortafolio" = C."IdPortafolio"
					WHERE C."IdEmpresa" = :p_IdEmpresa
					  AND C."IdTipoInstrumento" IN ('RRV','RRF')
					  AND C."IdEstado" = 'LIQ'
					EXCEPT 
					SELECT "IdCartera","IdCliente","IdPortafolio"
					FROM :v_Cartera
					WHERE "IdTipoInstrumento" IN ('RRV','RRF')
				 	) AS CEX ON CEX."IdCartera" = CAR."IdCartera"
		INNER JOIN :p_Datos DATOS ON DATOS."IdPortafolio" = CAR."IdPortafolio"
		WHERE REP."IdClasificacionTipoInstrumento" = 'RV'
		  AND REP."IdEstado" = 'VIG'
		  AND REP."INGFechaOperacion" <= :p_FechaHasta
	)
	SELECT   "IdCliente","IdPortafolio","IdBoveda","IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal","TasaCompra","PrecioCompra"
			,"PrecioPromedioCompra"
			,IFNULL(SUM("MontoPromedioCompraMonedaSistema"),0) AS "MontoPromedioCompraMonedaSistema"
			,IFNULL(SUM("MontoPromedioCompraMonedaAdicional"),0) AS "MontoPromedioCompraMonedaAdicional"
			,"TasaMercado","PrecioMercado"
			,IFNULL(SUM("Cantidad"),0) AS "Cantidad"
			,IFNULL(SUM("MontoMercadoMonedaSistema"),0) AS "MontoMercadoMonedaSistema"
			,IFNULL(SUM("MontoMercadoMonedaAdicional"),0) AS "MontoMercadoMonedaAdicional"
	FROM Reporte
	GROUP BY "IdCliente","IdPortafolio","IdBoveda","IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal","TasaCompra","PrecioCompra","PrecioPromedioCompra"
			,"TasaMercado","PrecioMercado";  
	

	
	--INFORMACION RETORNADA A LA CARTOLA
	v_MovimientosRV = 
	SELECT  "IdCliente","IdPortafolio"
			--------------------------------------------------------------------------------------------
			,MOV."IdBoveda"
			,BOV."Codigo" AS "CodigoBoveda"
			,BOV."Nombre" AS "NombreBoveda"
			,BOV."Ubicacion"
			,CASE WHEN COALESCE(:p_FiltroUbicacionBoveda,'') = '' THEN BOV."Ubicacion" ELSE :p_FiltroUbicacionBoveda END AS "UbicacionSolicitada"
			,MOV."IdClasificacionTipoInstrumento"
			,MOV."IdTipoInstrumento"
			,TINS."Nombre" AS "NombreTipoInstrumento"
			,MOV."IdInstrumento"
			,MOV."Nemotecnico"
			,TO_VARCHAR(INS."FechaVencimiento", 'DD-MM-YYYY') AS "FechaVencimiento"
			,TO_VARCHAR(INS."FechaEmision", 'DD-MM-YYYY') AS "FechaEmision"
			,CONCAT(CAST(YEAR ("FechaVencimiento") - YEAR (TO_DATE (:p_FechaHasta, 'YYYY-MM-DD')) AS NVARCHAR(5)),'/00') AS "PlazoResidual"			
			,RUB."Descripcion" as "Rubro"			
			,INS."IdTipoReajuste"
			,INS."IdEmisor"
			,INS."RazonSocial"
			,INS."CorteMinimo"
			,INS."Tasa" AS "TasaEmision"		
			,COALESCE(ICR."ValorClasificacionRiesgo",'') AS "ValorClasificacionRiesgo"
			--------------------------------------------------------------------------------------------
			,MOV."CantidadOriginal",MOV."CantidadCartera",MOV."CantidadGarantia",MOV."CantidadAPlazo",MOV."CantidadReporte",MOV."CantidadPrestamo"
			,(COALESCE(MOV."CantidadOriginal",0) - COALESCE(MOV."CantidadGarantia",0) - COALESCE(MOV."CantidadAPlazo",0) - COALESCE(MOV."CantidadReporte",0) - COALESCE(MOV."CantidadPrestamo",0)) AS "CantidadDisponible"
			--------------------------------------------------------------------------------------------
			,MOV."PrecioPromedioCompra",MOV."MontoPromedioCompraMonedaSistema",MOV."MontoPromedioCompraMonedaAdicional"
			,MOV."TasaMercado",MOV."PrecioMercado",MOV."MontoMercadoMonedaSistema",MOV."MontoMercadoMonedaAdicional"
			---------------------------------------------------------------------------------------------
			,CASE WHEN MOV."IdMonedaInstrumento" = MS."PLA.MonedaSistema" THEN MS."PLA.MonedaSistema"
				  WHEN MOV."IdMonedaInstrumento" != MS."PLA.MonedaSistema" AND :p_FiltroSoloMonedaSistema = 'S' THEN MS."PLA.MonedaSistema"
				  WHEN MOV."IdMonedaInstrumento" != MS."PLA.MonedaSistema" AND :p_FiltroSoloMonedaSistema = 'N' THEN MS."PLA.MonedaAdicional"				  
			 END "Moneda"
			,CASE WHEN MOV."IdMonedaInstrumento" = MS."PLA.MonedaSistema" THEN MOV."MontoPromedioCompraMonedaSistema"
				  WHEN MOV."IdMonedaInstrumento" != MS."PLA.MonedaSistema" AND :p_FiltroSoloMonedaSistema = 'S' THEN MOV."MontoPromedioCompraMonedaSistema"
				  WHEN MOV."IdMonedaInstrumento" != MS."PLA.MonedaSistema" AND :p_FiltroSoloMonedaSistema = 'N' THEN MOV."MontoPromedioCompraMonedaAdicional"  
			 END "MontoPromedio"
			,CASE WHEN MOV."IdMonedaInstrumento" = MS."PLA.MonedaSistema" THEN MOV."MontoMercadoMonedaSistema"
				  WHEN MOV."IdMonedaInstrumento" != MS."PLA.MonedaSistema" AND :p_FiltroSoloMonedaSistema = 'S' THEN MOV."MontoMercadoMonedaSistema"
				  WHEN MOV."IdMonedaInstrumento" != MS."PLA.MonedaSistema" AND :p_FiltroSoloMonedaSistema = 'N' THEN MOV."MontoMercadoMonedaAdicional"  
			 END "MontoMercado"
			---------------------------------------------------------------------------------------------
			,(MOV."MontoPromedioCompraMonedaSistema" - "MontoMercadoMonedaSistema") AS "GananciaPerdida"
			,CASE WHEN "MontoMercadoMonedaSistema" <> 0 THEN ((MOV."MontoPromedioCompraMonedaSistema" - "MontoMercadoMonedaSistema")/"MontoMercadoMonedaSistema") 
			  END AS "Rentabilidad"
	FROM (
		SELECT  "IdCliente","IdPortafolio"
				--------------------------------------------------------------------------------------------
				,"IdBoveda","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento"
				,"Nemotecnico","CantidadOriginal","CantidadCartera","CantidadGarantia","CantidadAPlazo","CantidadReporte","CantidadPrestamo"
				,"IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,"PrecioPromedioCompra","MontoPromedioCompraMonedaSistema","MontoPromedioCompraMonedaAdicional"
				,"TasaMercado","PrecioMercado","MontoMercadoMonedaSistema","MontoMercadoMonedaAdicional"
		FROM :V_MovimientosVigentesEnCartera
		UNION ALL 
		SELECT  "IdCliente","IdPortafolio"
				--------------------------------------------------------------------------------------------
				,"IdBoveda","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento"
				,"Nemotecnico","CantidadOriginal",0,"Cantidad",0,0,0,"IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,"PrecioPromedioCompra","MontoPromedioCompraMonedaSistema","MontoPromedioCompraMonedaAdicional"
				,"TasaMercado","PrecioMercado","MontoMercadoMonedaSistema","MontoMercadoMonedaAdicional"
		FROM :V_MovimientosVigentesEnGarantia
		UNION ALL
		SELECT  "IdCliente","IdPortafolio"
				--------------------------------------------------------------------------------------------
				,"IdBoveda","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento"
				,"Nemotecnico","CantidadOriginal",0,0,"Cantidad",0,0,"IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,"PrecioPromedioCompra","MontoPromedioCompraMonedaSistema","MontoPromedioCompraMonedaAdicional"
				,"TasaMercado","PrecioMercado","MontoMercadoMonedaSistema","MontoMercadoMonedaAdicional"
		FROM :V_MovimientosVigentesAPlazo		
		UNION ALL
		SELECT  "IdCliente","IdPortafolio"
				--------------------------------------------------------------------------------------------
				,"IdBoveda","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento"
				,"Nemotecnico","CantidadOriginal",0,0,0,"Cantidad",0,"IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,"PrecioPromedioCompra","MontoPromedioCompraMonedaSistema","MontoPromedioCompraMonedaAdicional"
				,"TasaMercado","PrecioMercado","MontoMercadoMonedaSistema","MontoMercadoMonedaAdicional"
		FROM :V_MovimientosVigentesEnReporte
		UNION ALL
		SELECT  "IdCliente","IdPortafolio"
				--------------------------------------------------------------------------------------------
				,"IdBoveda","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento"
				,"Nemotecnico","CantidadOriginal",0,0,"Cantidad",0,"Cantidad","IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,"PrecioPromedioCompra","MontoPromedioCompraMonedaSistema","MontoPromedioCompraMonedaAdicional"
				,"TasaMercado","PrecioMercado","MontoMercadoMonedaSistema","MontoMercadoMonedaAdicional"
		FROM :V_MovimientosVigentesPrestamosEmprestitos
	) AS MOV
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = MOV."IdInstrumento"
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TINS ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" AS TICR ON TICR."IdTipoInstrumento" = INS."IdTipoInstrumento" AND TICR."EntidadClasificadoraRiesgoPrincipal" = 'S'
	LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = INS."IdInstrumento" AND ICR."IdEntidadClasificadoraRiesgo" = TICR."IdEntidadClasificadoraRiesgo"
	LEFT JOIN "BSCL.Plataforma.BD::INS.Rubro" RUB ON RUB."CodigoRubro" = INS."CodigoRubro"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" BOV ON BOV."IdBoveda" = MOV."IdBoveda"
	FULL OUTER JOIN :v_MonedasDelSistema AS MS ON 1=1;
	
	
	SELECT 	"IdCliente","IdPortafolio"
			--------------------------------------------------------------------------------------------
			,"IdBoveda","CodigoBoveda","NombreBoveda","Ubicacion","UbicacionSolicitada",a."IdClasificacionTipoInstrumento","IdTipoInstrumento"
			,"NombreTipoInstrumento",mercado."Nombre" AS "descMercado" 
			,"IdInstrumento","Nemotecnico","FechaVencimiento","FechaEmision","PlazoResidual","Rubro"
			,"IdTipoReajuste","IdEmisor","RazonSocial","CorteMinimo","TasaEmision","ValorClasificacionRiesgo"
			--------------------------------------------------------------------------------------------
			,"CantidadOriginal","CantidadCartera","CantidadGarantia","CantidadAPlazo","CantidadReporte","CantidadPrestamo","CantidadDisponible"
			--------------------------------------------------------------------------------------------
			,"PrecioPromedioCompra","MontoPromedioCompraMonedaSistema","MontoPromedioCompraMonedaAdicional"
			,"TasaMercado","PrecioMercado","MontoMercadoMonedaSistema","MontoMercadoMonedaAdicional"
			---------------------------------------------------------------------------------------------
			,"Moneda","MontoPromedio","MontoMercado"
			---------------------------------------------------------------------------------------------
			,"GananciaPerdida","Rentabilidad" 
	FROM :v_MovimientosRV AS a
	LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS mercado ON mercado."IdClasificacionTipoInstrumento" = a."IdClasificacionTipoInstrumento"
	WHERE "Ubicacion" = "UbicacionSolicitada"
	ORDER BY "IdPortafolio","Moneda","Nemotecnico"; 
	
END;
GO