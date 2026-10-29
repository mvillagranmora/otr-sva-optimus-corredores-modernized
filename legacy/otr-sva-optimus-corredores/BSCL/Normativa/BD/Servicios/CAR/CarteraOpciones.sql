SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CarteraOpciones";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CarteraOpciones" (
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
			,CAR."INGCantidad" AS "CantidadCartera"
			,CAR."IdMonedaInstrumento"
			--------------------------------------------------------------------------------------------
			,CAR."INGTasaCompra" AS "TasaCompra"
			,CAR."INGPrecioCompra" AS "PrecioCompra"
			,CAR."INGPrecioPromedioCompra" AS "PrecioPromedioCompra"
			,CAR."INGMontoATasaCompraActual" AS "MontoCompraMonedaSistema"
			,CAR."INGMontoCompraActualMonedaInstrumento" AS "MontoCompraMonedaInstrumento"
			,CAR."INGMontoCompraActualMonedaAdicional" AS "MontoCompraMonedaAdicional"
			
			,CAR."INGTasaMercadoActual" AS "TasaMercado"
			,CAR."INGPrecioMercadoActual" as "PrecioMercado"
			,CAR."INGMontoATasaMercadoActual" AS "MontoMercadoMonedaSistema"
			,CAR."INGMontoMercadoActualMonedaInstrumento" AS "MontoMercadoMonedaInstrumento"
			,CAR."INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
	FROM :p_Datos DATOS
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" CAR ON DATOS."IdPortafolio" = CAR."IdPortafolio"
	WHERE CAR."IdClasificacionTipoInstrumento" = 'DV'
		AND "CAR"."IdTipoInstrumento" = 'OPC'
	  AND CAR."IdEstado" = 'VIG'
	  AND CAR."IdEmpresa" = :p_IdEmpresa
	  AND CAR."INGFechaOperacion" <= :p_FechaHasta;

	  
	--INFORMACION RETORNADA A LA CARTOLA
	v_MovimientosRV = 
	SELECT  "IdCliente","IdPortafolio"
			--------------------------------------------------------------------------------------------
			,MOV."FolioCartera"
			,CAST(CAST(MOV."FechaOperacion" AS DATE) AS NVARCHAR(10)) AS "FechaOperacion"
			,CAST(CAST(MOV."FechaLiquidacion" AS DATE) AS NVARCHAR(10)) AS "FechaLiquidacion"
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
			,CAST(CAST(INS."FechaVencimiento" AS DATE) AS NVARCHAR(10)) AS "FechaVencimiento"
			,CAST(CAST(INS."FechaEmision" AS DATE) AS NVARCHAR(10)) AS "FechaEmision"		
			,CONCAT(CAST(YEAR (INS."FechaVencimiento") - YEAR (TO_DATE (:p_FechaHasta, 'YYYY-MM-DD')) AS NVARCHAR(5)),'/00') AS "PlazoResidual"			
			,RUB."Descripcion" as "Rubro"			
			,INS."IdTipoReajuste"
			,INS."IdEmisor"
			,INS."RazonSocial"
			,INS."CorteMinimo"
			,INS."Tasa" AS "TasaEmision"		
			,COALESCE(ICR."ValorClasificacionRiesgo",'') AS "ValorClasificacionRiesgo"
			,CASE WHEN INS."EsPutEsCall" = 'C' THEN 'CALL' ELSE 'PUT' END AS "TipoOpcion"
			,INSREL."Nemotecnico" AS "NemotecnicoInstrumentoRelacionado"
			,INS."PrecioStrike"
			--------------------------------------------------------------------------------------------
			,MOV."CantidadOriginal",MOV."CantidadCartera",0 as "CantidadGarantia",0 as "CantidadAPlazo",0 as "CantidadReporte",0 as "CantidadPrestamo"		
			--------------------------------------------------------------------------------------------
			,MOV."TasaCompra",MOV."PrecioCompra",MOV."PrecioPromedioCompra",(MOV."CantidadOriginal" * MOV."PrecioPromedioCompra") AS "MontoCompraAPrecioPromedio"
			,MOV."MontoCompraMonedaSistema",MOV."MontoCompraMonedaInstrumento",MOV."MontoCompraMonedaAdicional"
			,MOV."TasaMercado",MOV."PrecioMercado",MOV."MontoMercadoMonedaSistema",MOV."MontoMercadoMonedaInstrumento",MOV."MontoMercadoMonedaAdicional"			
			---------------------------------------------------------------------------------------------
			,CASE WHEN MOV."IdMonedaInstrumento" = MS."PLA.MonedaSistema" THEN MS."PLA.MonedaSistema"
				  WHEN MOV."IdMonedaInstrumento" != MS."PLA.MonedaSistema" AND :p_FiltroSoloMonedaSistema = 'S' THEN MS."PLA.MonedaSistema"
				  WHEN MOV."IdMonedaInstrumento" != MS."PLA.MonedaSistema" AND :p_FiltroSoloMonedaSistema = 'N' THEN MS."PLA.MonedaAdicional"				  
			 END "Moneda"
			,CASE WHEN MOV."IdMonedaInstrumento" = MS."PLA.MonedaSistema" THEN MOV."MontoCompraMonedaSistema"
				  WHEN MOV."IdMonedaInstrumento" != MS."PLA.MonedaSistema" AND :p_FiltroSoloMonedaSistema = 'S' THEN MOV."MontoCompraMonedaSistema"
				  WHEN MOV."IdMonedaInstrumento" != MS."PLA.MonedaSistema" AND :p_FiltroSoloMonedaSistema = 'N' THEN MOV."MontoCompraMonedaAdicional"  
			 END "MontoCompra"
			,CASE WHEN MOV."IdMonedaInstrumento" = MS."PLA.MonedaSistema" THEN MOV."MontoMercadoMonedaSistema"
				  WHEN MOV."IdMonedaInstrumento" != MS."PLA.MonedaSistema" AND :p_FiltroSoloMonedaSistema = 'S' THEN MOV."MontoMercadoMonedaSistema"
				  WHEN MOV."IdMonedaInstrumento" != MS."PLA.MonedaSistema" AND :p_FiltroSoloMonedaSistema = 'N' THEN MOV."MontoMercadoMonedaAdicional"  
			 END "MontoMercado"
	FROM :v_Cartera AS MOV
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = MOV."IdInstrumento"
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TINS ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INSREL ON INSREL."IdInstrumento" = INS."IdInstrumentoRelacionado"
	LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = INS."IdInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::INS.Rubro" RUB ON RUB."CodigoRubro" = INS."CodigoRubro"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" BOV ON BOV."IdBoveda" = MOV."IdBoveda"
	FULL OUTER JOIN :v_MonedasDelSistema AS MS ON 1=1;
	
		
	SELECT 	"IdCliente","IdPortafolio"
			,"FolioCartera",TO_VARCHAR("FechaOperacion",'DD-MM-YYYY') AS "FechaOperacion",TO_VARCHAR("FechaLiquidacion",'DD-MM-YYYY') AS "FechaLiquidacion"
			--------------------------------------------------------------------------------------------
			,"IdBoveda","CodigoBoveda","NombreBoveda","Ubicacion","UbicacionSolicitada","IdClasificacionTipoInstrumento","IdTipoInstrumento"
			,"NombreTipoInstrumento","IdInstrumento","Nemotecnico",TO_VARCHAR("FechaVencimiento",'DD-MM-YYYY') AS "FechaVencimiento",TO_VARCHAR("FechaEmision",'DD-MM-YYYY') AS "FechaEmision","PlazoResidual","Rubro"
			,"IdTipoReajuste","IdEmisor","RazonSocial","CorteMinimo","TasaEmision","ValorClasificacionRiesgo"
			,"TipoOpcion","NemotecnicoInstrumentoRelacionado","PrecioStrike"
			--------------------------------------------------------------------------------------------
			,"CantidadOriginal","CantidadCartera","CantidadGarantia","CantidadAPlazo","CantidadReporte","CantidadPrestamo"		
			--------------------------------------------------------------------------------------------
			,"TasaCompra","PrecioCompra","PrecioPromedioCompra","MontoCompraAPrecioPromedio"
			,"MontoCompraMonedaSistema","MontoCompraMonedaInstrumento","MontoCompraMonedaAdicional"
			,"TasaMercado","PrecioMercado","MontoMercadoMonedaSistema","MontoMercadoMonedaInstrumento","MontoMercadoMonedaAdicional"
			---------------------------------------------------------------------------------------------
			,"Moneda","MontoCompra","MontoMercado"
	FROM :v_MovimientosRV
	WHERE "Ubicacion" = "UbicacionSolicitada"
	ORDER BY "IdPortafolio","Moneda"; 
	
END;
GO
