SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CarteraIF";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CarteraIF" (
    IN p_IdModulo 					NVARCHAR(40),
    IN p_IdUsuario 					BIGINT,
    IN p_IdEmpresa 					BIGINT,
    IN p_FechaDesde 				DATE,
    IN p_FechaHasta 				DATE,
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
			,CAR."IdEmisor"
			,CAR."FechaVencimientoInstrumento"
			,CAR."IdTipoReajuste"
			,CAR."FechaEmisionInstrumento"
			,CAR."Nemotecnico"
			,CAR."Cantidad" AS "CantidadOriginal"
			,CAR."ValorRescate" AS "ValorRescateOriginal"
			,CAR."INGCantidad" AS "Cantidad"
			,CAR."INGValorRescate" AS "ValorRescate"
			,CAR."IdMonedaInstrumento"
			--------------------------------------------------------------------------------------------
			,CAR."TasaCompra"
			,CAR."PrecioCompra"
			,CAR."MontoCompra" AS "MontoCompraMonedaSistema"
			,CAR."MontoCompra" AS "MontoCompraMonedaAdicional"
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
	WHERE CAR."IdClasificacionTipoInstrumento" = 'IF'
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
			,"IdEmisor"
			,"FechaVencimientoInstrumento"
			,"IdTipoReajuste"
			,"FechaEmisionInstrumento"
			,"Nemotecnico"	
			,"CantidadOriginal"
			,"ValorRescateOriginal"
			,"Cantidad"  AS "CantidadCartera"
			,"ValorRescate" AS "ValorRescateCartera"
			,"IdMonedaInstrumento"
			--------------------------------------------------------------------------------------------
			,"TasaCompra"
			,"PrecioCompra"
			,"MontoCompraMonedaSistema"
			,"MontoCompraMonedaAdicional"
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
				,SUM(GAR."INGValorRescate") AS "ValorRescateGarantia"
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
				,SUM(SUB."INGValorRescate") AS "ValorRescateAPlazo"
		FROM :v_Cartera CAR
		INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente" SUB ON CAR."FolioCartera" = SUB."FolioCarteraRelacionada"	
		WHERE SUB."IdEstado" = 'VIG'
		  AND CAR."IdTipoInstrumento" IN ('SIM','PRT','EMP','PAC','RRV','RRF')
		GROUP BY SUB."IdSubyacente",CAR."FolioCartera"
	)
	SELECT   C."IdCliente"
			,C."IdPortafolio"
			,C."FolioCartera"
			,C."FechaOperacion"
			,C."FechaLiquidacion"
			,C."IdBoveda"
			,C."IdTipoInstrumento"			
			,C."IdInstrumento"
			,C."IdEmisor"
			,C."FechaVencimientoInstrumento"
			,C."IdTipoReajuste"
			,C."FechaEmisionInstrumento"
			,C."Nemotecnico"
			,C."CantidadOriginal"
			,C."ValorRescateOriginal"
			,C."CantidadCartera"
			,C."ValorRescateCartera"			
			,G."CantidadGarantia"
			,G."ValorRescateGarantia"
			,P."CantidadAPlazo"
			,P."ValorRescateAPlazo"
			,C."IdMonedaInstrumento"
			--------------------------------------------------------------------------------------------
			,C."TasaCompra"
			,C."PrecioCompra"
			,C."MontoCompraMonedaSistema"
			,C."MontoCompraMonedaAdicional"
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
	FULL OUTER JOIN OperacionesAPlazoAsociadoACartera P ON P."FolioCartera" = C."FolioCartera";	
	
	
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
				,GAR."IdEmisor"
				,GAR."FechaVencimientoInstrumento"
				,GAR."IdTipoReajuste"
				,GAR."FechaEmisionInstrumento"
				,GAR."Nemotecnico"
				,CAR."INGCantidad" AS "CantidadOriginal"
				,CAR."INGValorRescate" AS "ValorRescateOriginal"
				,GAR."INGCantidad" AS "Cantidad"
				,GAR."INGValorRescate" AS "ValorRescate"
				,GAR."IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,CAR."INGTasaCompra" AS "TasaCompra"
				,CAR."INGPrecioCompra" AS "PrecioCompra"
				,CAR."EGRMonto" AS "MontoCompraMonedaSistema"
				,CAR."EGRMontoMonedaAdicional" AS "MontoCompraMonedaAdicional"
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
		INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" CAR ON CAR."FolioCartera" = GAR."FolioCarteraOrigen"
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
		WHERE GAR."IdClasificacionTipoInstrumento" = 'IF'
		  AND GAR."IdEstado" = 'VIG'
		  AND GAR."IdEmpresa" = :p_IdEmpresa
		  AND GAR."INGFechaOperacion" <= :p_FechaHasta
	)
	SELECT   "IdCliente","IdPortafolio","FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento"
			,"IdInstrumento","IdEmisor","FechaVencimientoInstrumento","IdTipoReajuste","FechaEmisionInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal","ValorRescateOriginal","TasaCompra","PrecioCompra"
			,"MontoCompraMonedaSistema","MontoCompraMonedaAdicional","TasaMercado","PrecioMercado","TieneCorteDeCupon","FechaPagoCorteCupon"
			,SUM("Cantidad") AS "Cantidad"
			,SUM("ValorRescate") AS "ValorRescate"
			,SUM("MontoMercadoMonedaSistema") AS "MontoMercadoMonedaSistema"
			,SUM("MontoMercadoMonedaAdicional") AS "MontoMercadoMonedaAdicional"
			,SUM("MontoInteresCorridoMercadoActualMonedaSistema") AS "MontoInteresCorridoMercadoActualMonedaSistema"
			,SUM("MontoInteresCorridoMercadoActualMonedaAdicional") AS "MontoInteresCorridoMercadoActualMonedaAdicional"			
			,SUM("CuponesPagadosMonedaSistema") AS "CuponesPagadosMonedaSistema"
	FROM Garantia
	GROUP BY "IdCliente","IdPortafolio","FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento","IdInstrumento"
			,"IdEmisor","FechaVencimientoInstrumento","IdTipoReajuste","FechaEmisionInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal","ValorRescateOriginal","TasaCompra","PrecioCompra"
			,"MontoCompraMonedaSistema","MontoCompraMonedaAdicional","TasaMercado","PrecioMercado","TieneCorteDeCupon","FechaPagoCorteCupon";

	
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
				,PLZ."IdEmisor"
				,PLZ."FechaVencimientoInstrumento"
				,PLZ."IdTipoReajuste"
				,PLZ."FechaEmisionInstrumento"
				,PLZ."Nemotecnico"
				,CAR."INGCantidad" AS "CantidadOriginal"
				,CAR."INGValorRescate" AS "ValorRescateOriginal"
				,PLZ."INGCantidad" AS "Cantidad"
				,PLZ."INGValorRescate" AS "ValorRescate"
				,PLZ."IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,CAR."INGTasaCompra" AS "TasaCompra" 
				,CAR."INGPrecioCompra" AS "PrecioCompra"
				,CAR."EGRMonto" AS "MontoCompraMonedaSistema"
				,CAR."EGRMontoMonedaAdicional" AS "MontoCompraMonedaAdicional"
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
					SELECT C."IdCartera",C."FolioCartera",C."IdCliente",C."IdPortafolio"
					FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" C
					INNER JOIN :p_Datos D ON D."IdPortafolio" = C."IdPortafolio"
					WHERE C."IdEmpresa" = :p_IdEmpresa
					  AND C."IdTipoInstrumento" IN ('SIM','PRT','EMP','PAC','RRV','RRF')
					  AND C."IdEstado" = 'LIQ'
					EXCEPT 
					SELECT  "IdCartera","FolioCartera","IdCliente","IdPortafolio"
					FROM :v_Cartera
					WHERE "IdTipoInstrumento" IN ('SIM','PRT','EMP','PAC','RRV','RRF')
				 	) AS CEX ON CEX."IdCartera" = CAR."IdCartera"
		INNER JOIN :p_Datos DATOS ON DATOS."IdPortafolio" = CAR."IdPortafolio"
		WHERE PLZ."IdClasificacionTipoInstrumento" = 'IF'
		  AND PLZ."IdEstado" = 'VIG'
		  AND PLZ."INGFechaOperacion" <= :p_FechaHasta
	)
	SELECT   "IdCliente","IdPortafolio","FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento"
			,"IdInstrumento","IdEmisor","FechaVencimientoInstrumento","IdTipoReajuste","FechaEmisionInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal","ValorRescateOriginal","TasaCompra","PrecioCompra"
			,"MontoCompraMonedaSistema","MontoCompraMonedaAdicional","TasaMercado","PrecioMercado","TieneCorteDeCupon","FechaPagoCorteCupon"
			,SUM("Cantidad") AS "Cantidad"
			,SUM("ValorRescate") AS "ValorRescate"
			,SUM("MontoMercadoMonedaSistema") AS "MontoMercadoMonedaSistema"
			,SUM("MontoMercadoMonedaAdicional") AS "MontoMercadoMonedaAdicional"
			,SUM("MontoInteresCorridoMercadoActualMonedaSistema") AS "MontoInteresCorridoMercadoActualMonedaSistema"
			,SUM("MontoInteresCorridoMercadoActualMonedaAdicional") AS "MontoInteresCorridoMercadoActualMonedaAdicional"			
			,SUM("CuponesPagadosMonedaSistema") AS "CuponesPagadosMonedaSistema"
	FROM APlazo
	GROUP BY "IdCliente","IdPortafolio","FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento","IdInstrumento"
			,"IdEmisor","FechaVencimientoInstrumento","Nemotecnico","IdTipoReajuste","FechaEmisionInstrumento","IdMonedaInstrumento","CantidadOriginal","ValorRescateOriginal","TasaCompra","PrecioCompra"
			,"MontoCompraMonedaSistema","MontoCompraMonedaAdicional","TasaMercado","PrecioMercado","TieneCorteDeCupon","FechaPagoCorteCupon";
	  
			
	--INFORMACION RETORNADA A LA CARTOLA
	SELECT  "IdCliente","IdPortafolio"
			--------------------------------------------------------------------------------------------
			,MOV."FolioCartera"
			,CAST(CAST(MOV."FechaOperacion" AS DATE) AS NVARCHAR(10)) AS "FechaOperacion"
			,CAST(CAST(MOV."FechaLiquidacion" AS DATE) AS NVARCHAR(10)) AS "FechaLiquidacion"
			--------------------------------------------------------------------------------------------
			,MOV."IdBoveda"
			,MOV."IdTipoInstrumento"
			,TINS."Nombre" AS "NombreTipoInstrumento"
			,MOV."IdInstrumento"
			,MOV."IdEmisor"
			,COALESCE(EMI."NombreCompleto",EMI."NombreCorto") AS "Emisor"
			,MOV."Nemotecnico"
			,TO_VARCHAR(MOV."FechaVencimientoInstrumento" , 'DD-MM-YYYY') AS "FechaVencimiento"
			,TO_VARCHAR(MOV."FechaEmisionInstrumento" , 'DD-MM-YYYY') AS "FechaEmision"
			,CAST(CAST(MOV."FechaPagoCorteCupon" AS DATE) AS NVARCHAR(10)) AS "FechaPagoCorteCupon"
			,CONCAT(CAST(YEAR ("FechaVencimiento") - YEAR (TO_DATE (:p_FechaHasta, 'YYYY-MM-DD')) AS NVARCHAR(5)),'/00') AS "PlazoResidual"
			,RUB."Descripcion" as "Rubro"
			,MOV."IdTipoReajuste"			
			,INS."RazonSocial"
			,CASE WHEN INS."TieneCupon" = 'N' THEN 'N/A' ELSE INS."TieneCupon" END "TieneCupon"
			,MOV."CuponesPagadosMonedaSistema"
			,COALESCE(ICR."ValorClasificacionRiesgo",'') AS "ValorClasificacionRiesgo"
			--------------------------------------------------------------------------------------------
			,MOV."IdMonedaInstrumento"
			,CASE WHEN INS."CorteMinimo" > 0 THEN (MOV."CantidadOriginal" / INS."CorteMinimo") ELSE MOV."CantidadOriginal" END AS "CantidadContable"			
			,MOV."CantidadOriginal",IFNULL(MOV."CantidadCartera",0) AS "CantidadCartera",IFNULL(MOV."CantidadGarantia",0) AS "CantidadGarantia",IFNULL(MOV."CantidadAPlazo",0) AS "CantidadAPlazo" 
			,(MOV."CantidadOriginal" + MOV."CantidadCartera" + MOV."CantidadGarantia" + MOV."CantidadAPlazo") AS "CantidadTotal"
			,MOV."ValorRescateOriginal",IFNULL(MOV."ValorRescateCartera",0) AS "ValorRescateCartera",IFNULL(MOV."ValorRescateGarantia",0) AS "ValorRescateGarantia",IFNULL(MOV."ValorRescateAPlazo",0) AS "ValorRescateAPlazo" 
			,(MOV."ValorRescateOriginal" + IFNULL(MOV."ValorRescateCartera",0) + IFNULL(MOV."ValorRescateGarantia",0) + IFNULL(MOV."ValorRescateAPlazo",0)) AS "ValorRescateTotal" 
			--------------------------------------------------------------------------------------------
			,MOV."TasaCompra",MOV."PrecioCompra",MOV."MontoCompraMonedaSistema"
			,MOV."TasaMercado",MOV."PrecioMercado",MOV."MontoMercadoMonedaSistema",MOV."MontoMercadoMonedaAdicional"
			,MOV."MontoInteresCorridoMercadoActualMonedaSistema",MOV."MontoInteresCorridoMercadoActualMonedaAdicional"	
			---------------------------------------------------------------------------------------------
			,CASE WHEN MOV."IdMonedaInstrumento" = MS."PLA.MonedaSistema" THEN MS."PLA.MonedaSistema"
				  WHEN MOV."IdMonedaInstrumento" != MS."PLA.MonedaSistema" AND :p_FiltroSoloMonedaSistema = 'S' THEN MS."PLA.MonedaSistema"
				  WHEN MOV."IdMonedaInstrumento" != MS."PLA.MonedaSistema" AND :p_FiltroSoloMonedaSistema = 'N' THEN MS."PLA.MonedaAdicional"				  
			 END "Moneda"
			,CASE WHEN MOV."IdMonedaInstrumento" = MS."PLA.MonedaSistema" THEN MOV."MontoCompraMonedaSistema"
				  WHEN MOV."IdMonedaInstrumento" != MS."PLA.MonedaSistema" AND :p_FiltroSoloMonedaSistema = 'S' THEN MOV."MontoCompraMonedaSistema"
				  WHEN MOV."IdMonedaInstrumento" != MS."PLA.MonedaSistema" AND :p_FiltroSoloMonedaSistema = 'N' THEN 0  
			 END "MontoCompra"
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
				,"FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento","IdInstrumento","IdEmisor","Nemotecnico","FechaVencimientoInstrumento"			
				,"IdTipoReajuste","FechaEmisionInstrumento","IdMonedaInstrumento","CantidadOriginal","CantidadCartera","CantidadGarantia","CantidadAPlazo"
				,"ValorRescateOriginal","ValorRescateCartera","ValorRescateGarantia","ValorRescateAPlazo"
				--------------------------------------------------------------------------------------------
				,"TasaCompra","PrecioCompra","MontoCompraMonedaSistema","MontoCompraMonedaAdicional"
				,"TasaMercado","PrecioMercado","MontoMercadoMonedaSistema","MontoMercadoMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,"MontoInteresCorridoMercadoActualMonedaSistema","MontoInteresCorridoMercadoActualMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,"TieneCorteDeCupon","FechaPagoCorteCupon","CuponesPagadosMonedaSistema" 
		FROM :V_MovimientosVigentesEnCartera
		UNION ALL 
		SELECT  "IdCliente","IdPortafolio"
				--------------------------------------------------------------------------------------------
				,"FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento","IdInstrumento","IdEmisor","Nemotecnico","FechaVencimientoInstrumento"			
				,"IdTipoReajuste","FechaEmisionInstrumento","IdMonedaInstrumento","CantidadOriginal",0,"Cantidad",0,"ValorRescateOriginal",0,"ValorRescate",0
				--------------------------------------------------------------------------------------------
				,"TasaCompra","PrecioCompra","MontoCompraMonedaSistema","MontoCompraMonedaAdicional"
				,"TasaMercado","PrecioMercado","MontoMercadoMonedaSistema","MontoMercadoMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,"MontoInteresCorridoMercadoActualMonedaSistema","MontoInteresCorridoMercadoActualMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,"TieneCorteDeCupon","FechaPagoCorteCupon","CuponesPagadosMonedaSistema"  
		FROM :V_MovimientosVigentesEnGarantia 
		UNION ALL
		SELECT  "IdCliente","IdPortafolio"
				--------------------------------------------------------------------------------------------
				,"FolioCartera","FechaOperacion","FechaLiquidacion","IdBoveda","IdTipoInstrumento","IdInstrumento","IdEmisor","Nemotecnico","FechaVencimientoInstrumento"
				,"IdTipoReajuste","FechaEmisionInstrumento","IdMonedaInstrumento","CantidadOriginal",0,0,"Cantidad","ValorRescateOriginal",0,0,"ValorRescate"
				--------------------------------------------------------------------------------------------
				,"TasaCompra","PrecioCompra","MontoCompraMonedaSistema","MontoCompraMonedaAdicional"
				,"TasaMercado","PrecioMercado","MontoMercadoMonedaSistema","MontoMercadoMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,"MontoInteresCorridoMercadoActualMonedaSistema","MontoInteresCorridoMercadoActualMonedaAdicional"
				---------------------------------------------------------------------------------------------
				,"TieneCorteDeCupon","FechaPagoCorteCupon","CuponesPagadosMonedaSistema"  
		FROM :V_MovimientosVigentesAPlazo
	) AS MOV
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = MOV."IdInstrumento"	
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TINS ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" AS TICR ON TICR."IdTipoInstrumento" = INS."IdTipoInstrumento" AND TICR."EntidadClasificadoraRiesgoPrincipal" = 'S'
	LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = INS."IdInstrumento" AND ICR."IdEntidadClasificadoraRiesgo" = TICR."IdEntidadClasificadoraRiesgo"
	INNER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMI ON EMI."IdEmisor" = MOV."IdEmisor"
	LEFT JOIN "BSCL.Plataforma.BD::INS.Rubro" RUB ON RUB."CodigoRubro" = INS."CodigoRubro"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" BOV ON BOV."IdBoveda" = MOV."IdBoveda"
	FULL OUTER JOIN :v_MonedasDelSistema AS MS ON 1=1;	
END;
GO