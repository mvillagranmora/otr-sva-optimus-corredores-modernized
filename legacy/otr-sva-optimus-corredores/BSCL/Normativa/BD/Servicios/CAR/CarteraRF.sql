SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CarteraRF";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CarteraRF" (
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
DEFAULT SCHEMA "BSCL"
--READS SQL DATA
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
			,CAR."IdTipoInstrumento"
			,CAR."IdInstrumento"
			,CAR."Nemotecnico"
			,CAR."Cantidad" AS "CantidadOriginal"
			,CAR."INGCantidad" AS "Cantidad"
			,CAR."IdMonedaInstrumento"
			--------------------------------------------------------------------------------------------
			,CAR."TasaCompra"
			,CAR."PrecioCompra"
			,CAR."MontoCompra" AS "MontoCompraMonedaSistema"
			,CAR."INGTasaMercadoActual" AS "TasaMercado"
			,CAR."INGPrecioMercadoActual" as "PrecioMercado"
			,CAR."INGMontoATasaMercadoActual" AS "MontoMercadoMonedaSistema"
			,CAR."INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
			---------------------------------------------------------------------------------------------
			,CAR."INGMontoInteresCorridoMercadoActual" AS "MontoInteresCorridoMercadoActualMonedaSistema" 
			,CAR."INGMontoInteresCorridoMercadoActualMonedaAdicional" AS "MontoInteresCorridoMercadoActualMonedaAdicional"
			---------------------------------------------------------------------------------------------
			,CAR."TieneCorteDeCupon"
			,CAR."INGFechaPagoCorteCupon" AS "FechaPagoCorteCupon" 
			,CASE WHEN CAR."INGFechaPagoCorteCupon" <= :p_FechaHasta THEN CAR."INGMontoCapitalCorteCupon" ELSE 0 END as "CuponesPagadosMonedaSistema"
	FROM :p_Datos DATOS
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" CAR ON DATOS."IdPortafolio" = CAR."IdPortafolio"
	WHERE CAR."IdClasificacionTipoInstrumento" = 'RF'
	  AND CAR."IdEstado" = 'VIG'
	  AND CAR."IdEmpresa" = :p_IdEmpresa
	  AND CAR."INGFechaOperacion" <= :p_FechaHasta;
	  
	  
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
			,"IdTipoInstrumento"
			,"IdInstrumento"
			,"Nemotecnico"	
			,"CantidadOriginal"
			,"Cantidad"  AS "CantidadCartera"
			,"IdMonedaInstrumento"
			--------------------------------------------------------------------------------------------
			,"TasaCompra"
			,"PrecioCompra"
			,"MontoCompraMonedaSistema"
			,"TasaMercado"
			,"PrecioMercado"
			,"MontoMercadoMonedaSistema"
			,"MontoMercadoMonedaAdicional"
			---------------------------------------------------------------------------------------------
			,"MontoInteresCorridoMercadoActualMonedaSistema"
			,"MontoInteresCorridoMercadoActualMonedaAdicional"
			---------------------------------------------------------------------------------------------
			,"TieneCorteDeCupon"
			,"FechaPagoCorteCupon"			
			,"CuponesPagadosMonedaSistema"
		FROM :v_Cartera
		WHERE "IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
	),
	GarantiaAsociadaACartera AS (
		SELECT   GAR."IdGarantia"
				,CAR."FolioCartera"
				,SUM(GAR."INGCantidad") AS "CantidadGarantia"
		FROM :v_Cartera CAR
		INNER JOIN "BSCL.Custodia.BD.Vistas::GTA.ConsultarTablaGarantia" GAR 
				ON GAR."FolioCarteraOrigen" = CAR."FolioCartera"	
		WHERE GAR."IdEstado" = 'VIG'
		GROUP BY GAR."IdGarantia",CAR."FolioCartera"
	),
	OperacionesAPlazoAsociadoACartera AS (
		SELECT   SUB."IdSubyacente"
				,CAR."FolioCartera"
				,SUM(SUB."INGCantidad") AS "CantidadAPlazo"
		FROM :v_Cartera CAR
		INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente" SUB ON CAR."FolioCartera" = SUB."FolioCarteraRelacionada"	
		WHERE SUB."IdEstado" = 'VIG'
		  AND CAR."IdTipoInstrumento" IN ('SIM','PRT','EMP','PAC')
		GROUP BY SUB."IdSubyacente",CAR."FolioCartera"
	),
	ReporteAsociadoACartera AS (
		SELECT   SUB."IdSubyacente"
				,CAR."FolioCartera"
				,SUM(SUB."INGCantidad") AS "CantidadReporte"
		FROM :v_Cartera CAR
		INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente" SUB ON CAR."FolioCartera" = SUB."FolioCarteraRelacionada"	
		WHERE SUB."IdEstado" = 'VIG'
		  AND CAR."IdTipoInstrumento" IN ('RRV','RRF')
		GROUP BY SUB."IdSubyacente",CAR."FolioCartera"
	)
	SELECT   C."IdCliente"
			,C."IdPortafolio"
			,C."FolioCartera"
			,C."FechaOperacion"
			,C."FechaLiquidacion"
			,C."IdTipoInstrumento"
			,C."IdBoveda"
			,C."IdInstrumento"
			,C."Nemotecnico"
			,C."CantidadOriginal"
			,C."CantidadCartera"
			,G."CantidadGarantia"
			,P."CantidadAPlazo"
			,R."CantidadReporte"
			,C."IdMonedaInstrumento"
			--------------------------------------------------------------------------------------------
			,C."TasaCompra"
			,C."PrecioCompra"
			,C."MontoCompraMonedaSistema"
			,C."TasaMercado"
			,C."PrecioMercado"
			,C."MontoMercadoMonedaSistema"
			,C."MontoMercadoMonedaAdicional"
			---------------------------------------------------------------------------------------------
			,C."MontoInteresCorridoMercadoActualMonedaSistema"
			,C."MontoInteresCorridoMercadoActualMonedaAdicional"
			---------------------------------------------------------------------------------------------
			,C."TieneCorteDeCupon"
			,C."FechaPagoCorteCupon"			
			,C."CuponesPagadosMonedaSistema"
	FROM Cartera AS C
	FULL OUTER JOIN GarantiaAsociadaACartera G ON G."FolioCartera" = C."FolioCartera"
	FULL OUTER JOIN OperacionesAPlazoAsociadoACartera P ON P."FolioCartera" = C."FolioCartera"
	FULL OUTER JOIN ReporteAsociadoACartera R ON R."FolioCartera" = C."FolioCartera";
	
	
	V_MovimientosVigentesEnGarantia =
	WITH 
	Garantia AS (
		SELECT 	 CAR."IdCliente"
				,CAR."IdPortafolio"
				,CAR."FolioCartera"
				,GAR."INGFechaOperacion" AS "FechaOperacion"
				,GAR."INGFechaLiquidacion" as "FechaLiquidacion"
				,CAR."IdBoveda"
				,GAR."IdTipoInstrumento"
				,GAR."IdInstrumento"
				,GAR."Nemotecnico"
				,CAR."INGCantidad" AS "CantidadOriginal"
				,GAR."INGCantidad" AS "Cantidad"
				,GAR."IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,CAR."INGTasaCompra" AS "TasaCompra"
				,CAR."INGPrecioCompra" AS "PrecioCompra"
				,CAR."INGMontoATasaCompraActual" AS "MontoCompraMonedaSistema"
				,GAR."INGTasaMercadoActual" AS "TasaMercado"
				,GAR."INGPrecioMercadoActual" as "PrecioMercado"
				,GAR."INGMontoATasaMercadoActual" AS "MontoMercadoMonedaSistema"
				,GAR."INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,GAR."INGMontoInteresCorridoMercadoActual" AS "MontoInteresCorridoMercadoActualMonedaSistema"
				,GAR."INGMontoInteresCorridoMercadoActualMonedaAdicional" AS "MontoInteresCorridoMercadoActualMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,GAR."TieneCorteDeCupon"
				,GAR."INGFechaPagoCorteCupon" AS "FechaPagoCorteCupon"
				,CASE WHEN GAR."INGFechaPagoCorteCupon" <= :p_FechaHasta THEN GAR."INGMontoCapitalCorteCupon" ELSE 0 END as "CuponesPagadosMonedaSistema"
		FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarTablaGarantia" AS GAR
		INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" AS CAR ON CAR."FolioCartera" = GAR."FolioCarteraOrigen"
		INNER JOIN (
					SELECT C."FolioCartera",C."IdCliente",C."IdPortafolio"
					FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" C
					INNER JOIN :p_Datos D ON D."IdPortafolio" = C."IdPortafolio"
					WHERE C."IdEstado" = 'LIQ'
					EXCEPT 
					SELECT  "FolioCartera","IdCliente","IdPortafolio"
					FROM :v_Cartera				
				 	) AS CEX ON CEX."FolioCartera" = CAR."FolioCartera"
		INNER JOIN :p_Datos DATOS ON DATOS."IdPortafolio" = CAR."IdPortafolio"	
		WHERE GAR."IdClasificacionTipoInstrumento" = 'RF'
		  AND GAR."IdEstado" = 'VIG'
		  AND GAR."IdEmpresa" = :p_IdEmpresa
		  AND GAR."INGFechaOperacion" <= :p_FechaHasta
	)
	SELECT   "IdCliente","IdPortafolio","FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento"
			,"IdInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal","TasaCompra","PrecioCompra"
			,"MontoCompraMonedaSistema","TasaMercado","PrecioMercado","TieneCorteDeCupon","FechaPagoCorteCupon"
			,SUM("Cantidad") AS "Cantidad"
			,SUM("MontoMercadoMonedaSistema") AS "MontoMercadoMonedaSistema"
			,SUM("MontoMercadoMonedaAdicional") AS "MontoMercadoMonedaAdicional"
			,SUM("MontoInteresCorridoMercadoActualMonedaSistema") AS "MontoInteresCorridoMercadoActualMonedaSistema"
			,SUM("MontoInteresCorridoMercadoActualMonedaAdicional") AS "MontoInteresCorridoMercadoActualMonedaAdicional"			
			,SUM("CuponesPagadosMonedaSistema") AS "CuponesPagadosMonedaSistema"
	FROM Garantia
	GROUP BY "IdCliente","IdPortafolio","FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento","IdInstrumento"
			,"Nemotecnico","IdMonedaInstrumento","CantidadOriginal","TasaCompra","PrecioCompra","MontoCompraMonedaSistema"
			,"TasaMercado","PrecioMercado","TieneCorteDeCupon","FechaPagoCorteCupon";

	
	V_MovimientosVigentesAPlazo =
	WITH 
	APlazo AS (
		SELECT 	 CAR."IdCliente"
				,CAR."IdPortafolio"
				,CAR."FolioCartera"
				,PLZ."INGFechaOperacion" AS "FechaOperacion"
				,CAR."INGFechaLiquidacion" as "FechaLiquidacion"
				,CAR."IdBoveda"
				,PLZ."IdTipoInstrumento"
				,PLZ."IdInstrumento"
				,PLZ."Nemotecnico"
				,CAR."INGCantidad" AS "CantidadOriginal"
				,PLZ."Cantidad"
				,PLZ."IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,CAR."INGTasaCompra" AS "TasaCompra" 
				,CAR."INGPrecioCompra" AS "PrecioCompra"
				,CAR."INGMontoATasaCompraActual" AS "MontoCompraMonedaSistema"
				,PLZ."INGTasaMercadoActual" AS "TasaMercado"
				,PLZ."INGPrecioMercadoActual" as "PrecioMercado"
				,PLZ."INGMontoATasaMercadoActual" AS "MontoMercadoMonedaSistema"
				,PLZ."INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,PLZ."INGMontoInteresCorridoMercadoActual" AS "MontoInteresCorridoMercadoActualMonedaSistema"
				,PLZ."INGMontoInteresCorridoMercadoActualMonedaAdicional" AS "MontoInteresCorridoMercadoActualMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,PLZ."TieneCorteDeCupon"
				,PLZ."INGFechaPagoCorteCupon" AS "FechaPagoCorteCupon"
				,CASE WHEN PLZ."INGFechaPagoCorteCupon" <= :p_FechaHasta THEN PLZ."INGMontoCapitalCorteCupon" ELSE 0 END as "CuponesPagadosMonedaSistema"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente" PLZ
		INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" CAR ON CAR."FolioCartera" = PLZ."FolioCarteraRelacionada"
		INNER JOIN (
					SELECT C."IdCartera",C."IdCliente",C."IdPortafolio"
					FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" C
					INNER JOIN :p_Datos D ON D."IdPortafolio" = C."IdPortafolio"
					WHERE C."IdEmpresa" = :p_IdEmpresa
					  AND C."IdTipoInstrumento" IN ('SIM','PRT','EMP','PAC')
					  AND C."IdEstado" = 'LIQ'
					EXCEPT 
					SELECT  "IdCartera","IdCliente","IdPortafolio"
					FROM :v_Cartera
					WHERE "IdTipoInstrumento" IN ('SIM','PRT','EMP','PAC')
				 	) AS CEX ON CEX."IdCartera" = CAR."IdCartera"
		INNER JOIN :p_Datos DATOS ON DATOS."IdPortafolio" = CAR."IdPortafolio"
		WHERE PLZ."IdClasificacionTipoInstrumento" = 'RF'
		  AND PLZ."IdEstado" = 'VIG'
		  AND PLZ."INGFechaOperacion" <= :p_FechaHasta
	)
	SELECT   "IdCliente","IdPortafolio","FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento"
			,"IdInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal","TasaCompra","PrecioCompra"
			,"MontoCompraMonedaSistema","TasaMercado","PrecioMercado","TieneCorteDeCupon","FechaPagoCorteCupon"
			,SUM("Cantidad") AS "Cantidad"
			,SUM("MontoMercadoMonedaSistema") AS "MontoMercadoMonedaSistema"
			,SUM("MontoMercadoMonedaAdicional") AS "MontoMercadoMonedaAdicional"
			,SUM("MontoInteresCorridoMercadoActualMonedaSistema") AS "MontoInteresCorridoMercadoActualMonedaSistema"
			,SUM("MontoInteresCorridoMercadoActualMonedaAdicional") AS "MontoInteresCorridoMercadoActualMonedaAdicional"			
			,SUM("CuponesPagadosMonedaSistema") AS "CuponesPagadosMonedaSistema"
	FROM APlazo
	GROUP BY "IdCliente","IdPortafolio","FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento","IdInstrumento"
			,"Nemotecnico","IdMonedaInstrumento","CantidadOriginal","TasaCompra","PrecioCompra","MontoCompraMonedaSistema"
			,"TasaMercado","PrecioMercado","TieneCorteDeCupon","FechaPagoCorteCupon";
	  
	V_MovimientosVigentesEnReporte =
	WITH 
	Reporte AS (
		SELECT 	 CAR."IdCliente"
				,CAR."IdPortafolio"
				,CAR."FolioCartera"
				,REP."INGFechaOperacion" AS "FechaOperacion"
				,CAR."INGFechaLiquidacion" as "FechaLiquidacion"
				,CAR."IdBoveda"
				,REP."IdTipoInstrumento"
				,REP."IdInstrumento"
				,REP."Nemotecnico"
				,CAR."INGCantidad" AS "CantidadOriginal"
				,REP."Cantidad"
				,REP."IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,CAR."INGTasaCompra" AS "TasaCompra"
				,CAR."INGPrecioCompra" AS "PrecioCompra"
				,CAR."INGMontoATasaCompraActual" AS "MontoCompraMonedaSistema"
				,REP."INGTasaMercadoActual" AS "TasaMercado"
				,REP."INGPrecioMercadoActual" as "PrecioMercado"
				,REP."INGMontoATasaMercadoActual" AS "MontoMercadoMonedaSistema"
				,REP."INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,REP."INGMontoInteresCorridoMercadoActual" AS "MontoInteresCorridoMercadoActualMonedaSistema"
				,REP."INGMontoInteresCorridoMercadoActualMonedaAdicional" AS "MontoInteresCorridoMercadoActualMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,REP."TieneCorteDeCupon"
				,REP."INGFechaPagoCorteCupon" AS "FechaPagoCorteCupon"
				,CASE WHEN REP."INGFechaPagoCorteCupon" <= :p_FechaHasta THEN REP."INGMontoCapitalCorteCupon" ELSE 0 END as "CuponesPagadosMonedaSistema"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente" REP
		INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" CAR ON CAR."FolioCartera" = REP."FolioCarteraRelacionada"
		INNER JOIN (
					SELECT C."IdCartera",C."FolioCartera",C."IdCliente",C."IdPortafolio"
					FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" C
					INNER JOIN :p_Datos D ON D."IdPortafolio" = C."IdPortafolio"
					WHERE C."IdEmpresa" = :p_IdEmpresa
					  AND C."IdTipoInstrumento" IN ('RRV','RRF')
					  AND C."IdEstado" = 'LIQ'
					EXCEPT 
					SELECT "IdCartera","FolioCartera","IdCliente","IdPortafolio"
					FROM :v_Cartera
					WHERE "IdTipoInstrumento" IN ('RRV','RRF')
				 	) AS CEX ON CEX."IdCartera" = CAR."IdCartera"
		INNER JOIN :p_Datos DATOS ON DATOS."IdPortafolio" = CAR."IdPortafolio"
		WHERE REP."IdClasificacionTipoInstrumento" = 'RF'
		  AND REP."IdEstado" = 'VIG'
		  AND REP."INGFechaOperacion" <= :p_FechaHasta
	)
	SELECT   "IdCliente","IdPortafolio","FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento"
			,"IdInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal","TasaCompra","PrecioCompra"
			,"MontoCompraMonedaSistema","TasaMercado","PrecioMercado","TieneCorteDeCupon","FechaPagoCorteCupon"
			,SUM("Cantidad") AS "Cantidad"
			,SUM("MontoMercadoMonedaSistema") AS "MontoMercadoMonedaSistema"
			,SUM("MontoMercadoMonedaAdicional") AS "MontoMercadoMonedaAdicional"
			,SUM("MontoInteresCorridoMercadoActualMonedaSistema") AS "MontoInteresCorridoMercadoActualMonedaSistema"
			,SUM("MontoInteresCorridoMercadoActualMonedaAdicional") AS "MontoInteresCorridoMercadoActualMonedaAdicional"			
			,SUM("CuponesPagadosMonedaSistema") AS "CuponesPagadosMonedaSistema"
	FROM Reporte
	GROUP BY "IdCliente","IdPortafolio","FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento","IdInstrumento"
			,"Nemotecnico","IdMonedaInstrumento","CantidadOriginal","TasaCompra","PrecioCompra","MontoCompraMonedaSistema"
			,"TasaMercado","PrecioMercado","TieneCorteDeCupon","FechaPagoCorteCupon";  

			
	--INFORMACION RETORNADA A LA CARTOLA
	v_MovimientosRF = 
	SELECT  "IdCliente","IdPortafolio"
			--------------------------------------------------------------------------------------------
			,MOV."FolioCartera"
			--,CAST(CAST(MOV."FechaOperacion" AS DATE) AS NVARCHAR(10)) AS "FechaOperacion"
			--,CAST(CAST(MOV."FechaLiquidacion" AS DATE) AS NVARCHAR(10)) AS "FechaLiquidacion"
			,TO_VARCHAR(MOV."FechaOperacion", 'DD-MM-YYYY') AS "FechaOperacion"
			,TO_VARCHAR(MOV."FechaLiquidacion", 'DD-MM-YYYY') AS "FechaLiquidacion"
			--------------------------------------------------------------------------------------------
			,MOV."IdBoveda"
			,BOV."Codigo" AS "CodigoBoveda"
			,BOV."Nombre" AS "NombreBoveda"
			,BOV."Ubicacion"
			,CASE WHEN COALESCE(:p_FiltroUbicacionBoveda,'') = '' THEN BOV."Ubicacion" ELSE :p_FiltroUbicacionBoveda END AS "UbicacionSolicitada"
			,MOV."IdTipoInstrumento"
			,TINS."Nombre" AS "NombreTipoInstrumento"
			,MOV."IdInstrumento"
			,MOV."Nemotecnico"
			,TO_VARCHAR(INS."FechaVencimiento", 'DD-MM-YYYY') AS "FechaVencimiento"
			,TO_VARCHAR(INS."FechaEmision", 'DD-MM-YYYY') AS "FechaEmision"
			,TO_VARCHAR(MOV."FechaPagoCorteCupon", 'DD-MM-YYYY') AS "FechaPagoCorteCupon"			
			,CONCAT(CAST(YEAR ("FechaVencimiento") - YEAR (TO_DATE (:p_FechaHasta, 'YYYY-MM-DD')) AS NVARCHAR(5)),'/00') AS "PlazoResidual"			
			,RUB."Descripcion" as "Rubro"			
			,INS."IdTipoReajuste"
			,INS."IdEmisor"
			,INS."RazonSocial"
			,INS."CorteMinimo"
			,CASE WHEN INS."TieneCupon" = 'N' THEN 'N/A' ELSE INS."TieneCupon" END "TieneCupon"
			,COALESCE(INS."Tasa",0) AS "TasaEmision"
			,MOV."CuponesPagadosMonedaSistema"
			,COALESCE(ICR."ValorClasificacionRiesgo",'') AS "ValorClasificacionRiesgo"
			--------------------------------------------------------------------------------------------
			,MOV."IdMonedaInstrumento"
			,CASE WHEN INS."CorteMinimo" > 0 THEN (MOV."CantidadOriginal" / INS."CorteMinimo") ELSE MOV."CantidadOriginal" END AS "CantidadContable"			
			,MOV."CantidadOriginal",MOV."CantidadCartera",MOV."CantidadGarantia",MOV."CantidadAPlazo",MOV."CantidadReporte"		
			--------------------------------------------------------------------------------------------
			,MOV."TasaCompra",MOV."PrecioCompra",MOV."MontoCompraMonedaSistema"
			,MOV."TasaMercado",MOV."PrecioMercado",MOV."MontoMercadoMonedaSistema",MOV."MontoMercadoMonedaAdicional"
			,MOV."MontoInteresCorridoMercadoActualMonedaSistema",MOV."MontoInteresCorridoMercadoActualMonedaAdicional"	
			---------------------------------------------------------------------------------------------
			,CASE WHEN MOV."IdMonedaInstrumento" = MS."PLA.MonedaSistema" THEN MS."PLA.MonedaSistema"
				  WHEN MOV."IdMonedaInstrumento" != MS."PLA.MonedaSistema" AND :p_FiltroSoloMonedaSistema = 'S' THEN MS."PLA.MonedaSistema"
				  WHEN MOV."IdMonedaInstrumento" != MS."PLA.MonedaSistema" AND :p_FiltroSoloMonedaSistema = 'N' THEN MS."PLA.MonedaAdicional"				  
			 END "Moneda"
			,CASE WHEN MOV."IdMonedaInstrumento" = MS."PLA.MonedaSistema" THEN MOV."MontoMercadoMonedaSistema"
				  WHEN MOV."IdMonedaInstrumento" != MS."PLA.MonedaSistema" AND :p_FiltroSoloMonedaSistema = 'S' THEN MOV."MontoMercadoMonedaSistema"
				  WHEN MOV."IdMonedaInstrumento" != MS."PLA.MonedaSistema" AND :p_FiltroSoloMonedaSistema = 'N' THEN MOV."MontoMercadoMonedaAdicional"  
			 END "MontoMercado"
			,CASE WHEN MOV."IdMonedaInstrumento" = MS."PLA.MonedaSistema" THEN MOV."MontoInteresCorridoMercadoActualMonedaSistema"
				  WHEN MOV."IdMonedaInstrumento" != MS."PLA.MonedaSistema" AND :p_FiltroSoloMonedaSistema = 'S' THEN MOV."MontoInteresCorridoMercadoActualMonedaSistema"
				  WHEN MOV."IdMonedaInstrumento" != MS."PLA.MonedaSistema" AND :p_FiltroSoloMonedaSistema = 'N' THEN MOV."MontoInteresCorridoMercadoActualMonedaAdicional"  
			 END "MontoInteresCorrido"
			---------------------------------------------------------------------------------------------
			,CASE WHEN "MontoMercadoMonedaSistema" <> 0 THEN ((MOV."MontoCompraMonedaSistema" - "MontoMercadoMonedaSistema")/"MontoMercadoMonedaSistema") 
			  END AS "Rentabilidad" 
	FROM (
		SELECT  "IdCliente","IdPortafolio"
				--------------------------------------------------------------------------------------------
				,"FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento","IdInstrumento","Nemotecnico"			
				,"CantidadOriginal","CantidadCartera","CantidadGarantia","CantidadAPlazo","CantidadReporte","IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,"TasaCompra","PrecioCompra","MontoCompraMonedaSistema"
				,"TasaMercado","PrecioMercado","MontoMercadoMonedaSistema","MontoMercadoMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,"MontoInteresCorridoMercadoActualMonedaSistema","MontoInteresCorridoMercadoActualMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,"TieneCorteDeCupon","FechaPagoCorteCupon","CuponesPagadosMonedaSistema" 
		FROM :V_MovimientosVigentesEnCartera
		UNION ALL 
		SELECT  "IdCliente","IdPortafolio"
				--------------------------------------------------------------------------------------------
				,"FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento","IdInstrumento","Nemotecnico"			
				,"CantidadOriginal",0,"Cantidad",0,0,"IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,"TasaCompra","PrecioCompra","MontoCompraMonedaSistema"
				,"TasaMercado","PrecioMercado","MontoMercadoMonedaSistema","MontoMercadoMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,"MontoInteresCorridoMercadoActualMonedaSistema","MontoInteresCorridoMercadoActualMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,"TieneCorteDeCupon","FechaPagoCorteCupon","CuponesPagadosMonedaSistema"  
		FROM :V_MovimientosVigentesEnGarantia 
		UNION ALL
		SELECT  "IdCliente","IdPortafolio"
				--------------------------------------------------------------------------------------------
				,"FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento","IdInstrumento","Nemotecnico"			
				,"CantidadOriginal",0,0,"Cantidad",0,"IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,"TasaCompra","PrecioCompra","MontoCompraMonedaSistema"
				,"TasaMercado","PrecioMercado","MontoMercadoMonedaSistema","MontoMercadoMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,"MontoInteresCorridoMercadoActualMonedaSistema","MontoInteresCorridoMercadoActualMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,"TieneCorteDeCupon","FechaPagoCorteCupon","CuponesPagadosMonedaSistema"  
		FROM :V_MovimientosVigentesAPlazo 
		UNION ALL
		SELECT  "IdCliente","IdPortafolio"
				--------------------------------------------------------------------------------------------
				,"FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento","IdInstrumento","Nemotecnico"			
				,"CantidadOriginal",0,0,0,"Cantidad","IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,"TasaCompra","PrecioCompra","MontoCompraMonedaSistema"
				,"TasaMercado","PrecioMercado","MontoMercadoMonedaSistema","MontoMercadoMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,"MontoInteresCorridoMercadoActualMonedaSistema","MontoInteresCorridoMercadoActualMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,"TieneCorteDeCupon","FechaPagoCorteCupon","CuponesPagadosMonedaSistema"  
		FROM :V_MovimientosVigentesEnReporte
	) AS MOV
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = MOV."IdInstrumento"
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TINS ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" AS TICR ON TICR."IdTipoInstrumento" = INS."IdTipoInstrumento" AND TICR."EntidadClasificadoraRiesgoPrincipal" = 'S'
	LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = INS."IdInstrumento" AND ICR."IdEntidadClasificadoraRiesgo" = TICR."IdEntidadClasificadoraRiesgo"	
	LEFT JOIN "BSCL.Plataforma.BD::INS.Rubro" RUB ON RUB."CodigoRubro" = INS."CodigoRubro"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" BOV ON BOV."IdBoveda" = MOV."IdBoveda"
	FULL OUTER JOIN :v_MonedasDelSistema AS MS ON 1=1;
	
	
	
	
	SELECT 	"IdCliente","IdPortafolio"
			--------------------------------------------------------------------------------------------
			,"FolioCartera","FechaOperacion","FechaLiquidacion"
			--------------------------------------------------------------------------------------------
			,"IdBoveda","IdBoveda","CodigoBoveda","NombreBoveda","Ubicacion","UbicacionSolicitada","IdTipoInstrumento","NombreTipoInstrumento"
			,"IdInstrumento","Nemotecnico","FechaVencimiento","FechaEmision","FechaPagoCorteCupon","PlazoResidual","Rubro","IdTipoReajuste","IdEmisor"
			,"RazonSocial","CorteMinimo","TieneCupon","TasaEmision","CuponesPagadosMonedaSistema","ValorClasificacionRiesgo"
			--------------------------------------------------------------------------------------------
			,"IdMonedaInstrumento","CantidadContable","CantidadOriginal","CantidadCartera","CantidadGarantia","CantidadAPlazo","CantidadReporte"		
			--------------------------------------------------------------------------------------------
			,"TasaCompra","PrecioCompra","MontoCompraMonedaSistema","TasaMercado","PrecioMercado","MontoMercadoMonedaSistema","MontoMercadoMonedaAdicional"
			,"MontoInteresCorridoMercadoActualMonedaSistema","MontoInteresCorridoMercadoActualMonedaAdicional"	
			---------------------------------------------------------------------------------------------
			,"Moneda","MontoMercado","MontoInteresCorrido"
			---------------------------------------------------------------------------------------------
			,"Rentabilidad" 
	FROM :v_MovimientosRF
	WHERE "Ubicacion" = "UbicacionSolicitada"
	ORDER BY "IdPortafolio","Moneda"; 
END;
GO