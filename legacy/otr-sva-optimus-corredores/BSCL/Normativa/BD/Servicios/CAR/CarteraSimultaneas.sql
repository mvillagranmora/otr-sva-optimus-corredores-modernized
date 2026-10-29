SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CarteraSimultaneas";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CarteraSimultaneas" (
    IN p_IdModulo NVARCHAR(40),
    IN p_IdUsuario BIGINT,
    IN p_IdEmpresa BIGINT,
    IN p_FechaDesde DATE,
    IN p_FechaHasta DATE,
    IN p_Datos TABLE ("IdCliente" BIGINT, "IdPortafolio" BIGINT)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
--READS SQL DATA
AS
BEGIN

	v_MonedasDelSistema = 
	SELECT  "IdEmpresa",
			"PLA.MonedaSistema",
			CASE WHEN "PLA.MonedaAdicional" IS NULL OR "PLA.MonedaAdicional" = '' THEN 'USD' ELSE "PLA.MonedaAdicional" END "PLA.MonedaAdicional" 
	  FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionPivot"
	 WHERE "IdEmpresa" = :p_IdEmpresa;
	
	
	v_Simultaneas = 
	SELECT 	 CAR."IdCliente"
			,CAR."IdPortafolio"
			,CAR."IdCartera"
			,CAR."FolioCartera"
			,CAR."IdTipoOperacion"						
			,TOPER."Nombre" AS "NombreOperacion"
			,TOPER."Accion" AS "AccionAlContado"
			,CASE WHEN TOPER."Accion" = 'COMPRA' THEN 'VENTA' ELSE 'COMPRA' END AS "AccionAPlazo"
			,CASE WHEN TOPER."Accion" = 'COMPRA' THEN 'Reportante' ELSE 'Reportado' END AS "TipoPosicion"
			,CAR."FolioComprobante"
			--------------------------------------------------------------------------------------------
			,SUB."IdSubyacente"
			,TO_VARCHAR(SUB."INGFechaOperacion",'DD-MM-YYYY') AS "FechaOperacion"
			,SUB."IdTipoInstrumento"
			,SUB."IdInstrumento"
			,SUB."Nemotecnico"
			,INS."RazonSocial"
			,SUB."INGCantidad" AS "Cantidad"
			,SUB."INGPrecioSubyacente" AS "PrecioContado"
			,SUB."INGTasaSubyacente" AS "Tasa"
			,SUB."INGMontoATasaCompraActual" AS "MontoATasaCompraActual"	  
			,SUB."INGMontoCompraActualMonedaInstrumento" AS "MontoCompraActualMonedaInstrumento"
			,SUB."INGMontoCompraActualMonedaAdicional" AS "MontoCompraActualMonedaAdicional"			
			,SUB."INGPrecioMercadoActual" AS "PrecioMercado"				 	  			
			,SUB."INGTasaMercadoActual" AS "TasaMercado"
			,SUB."INGMontoATasaMercadoActual" AS "MontoMercadoMonedaSistema"
			,SUB."INGMontoMercadoActualMonedaInstrumento" AS "MontoMercadoMonedaInstrumento"
			,SUB."INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
			--------------------------------------------------------------------------------------------
			,TO_VARCHAR(CAR."FechaVencimientoOperacion",'DD-MM-YYYY') AS "FechaVencimientoOperacion"
			,CAR."PlazoEnDiasOperacion"
			,CAR."PrecioAPlazo"							 	 			
			,CAR."MontoAPlazo" AS "MontoAPlazoMonedaSistema"
			,CAR."MontoAPlazoMonedaInstrumento" 			 	 			
			,CAR."MontoAPlazoMonedaAdicional"
			--------------------------------------------------------------------------------------------
			,CASE WHEN SUB."IdMonedaInstrumento" = MON."PLA.MonedaSistema" THEN MON."PLA.MonedaSistema" ELSE MON."PLA.MonedaAdicional" END "Moneda"
			,CASE WHEN SUB."IdMonedaInstrumento" = MON."PLA.MonedaSistema" THEN SUB."INGMontoATasaCompraActual" ELSE SUB."INGMontoCompraActualMonedaAdicional" END "MontoAContado"			
			,CASE WHEN SUB."IdMonedaInstrumento" = MON."PLA.MonedaSistema" THEN SUB."INGMontoATasaMercadoActual" ELSE SUB."INGMontoMercadoActualMonedaAdicional" END "MontoMercado"			
			,CASE WHEN SUB."IdMonedaInstrumento" = MON."PLA.MonedaSistema" THEN CAR."MontoAPlazo" ELSE CAR."MontoAPlazoMonedaAdicional" END "MontoAPlazo"
			--------------------------------------------------------------------------------------------
			,CASE WHEN SUB."INGMontoATasaMercadoActual" <> 0 THEN ((SUB."INGMontoATasaCompraActual" - SUB."INGMontoATasaMercadoActual")/SUB."INGMontoATasaMercadoActual") 
			  END AS "Rentabilidad" 
	FROM :p_Datos DATOS
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" CAR ON DATOS."IdPortafolio" = CAR."IdPortafolio"
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = SUB."IdInstrumento"
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TOPER ON TOPER."IdTipoOperacion" = CAR."IdTipoOperacion"
	INNER JOIN :v_MonedasDelSistema MON ON MON."IdEmpresa" = CAR."IdEmpresa"
	WHERE CAR."IdTipoInstrumento" IN ('SIM','RRV','RRF')
	  AND CAR."IdEstado" = 'VIG'
	  AND CAR."IdEmpresa" = :p_IdEmpresa;
	  
	  
	SELECT 	 S."IdCliente"
			,S."IdPortafolio"
			,S."IdCartera"
			,S."FolioCartera"
			,S."IdTipoOperacion"						
			,S."NombreOperacion"
			,S."AccionAlContado"
			,S."AccionAPlazo"
			,S."FolioComprobante"
			--------------------------------------------------------------------------------------------
			,S."IdSubyacente"
			,S."FechaOperacion"
			,S."IdTipoInstrumento"
			,S."IdInstrumento"
			,S."Nemotecnico"
			,S."RazonSocial"
			,S."Cantidad"
			,S."PrecioContado"
			,S."Tasa"
			,S."MontoATasaCompraActual"	  
			,S."MontoCompraActualMonedaInstrumento"
			,S."MontoCompraActualMonedaAdicional"			
			,S."PrecioMercado"				 	  			
			,S."TasaMercado"
			,S."MontoMercadoMonedaSistema"
			,S."MontoMercadoMonedaInstrumento"
			,S."MontoMercadoMonedaAdicional"
			--------------------------------------------------------------------------------------------
			,S."FechaVencimientoOperacion"
			,S."PlazoEnDiasOperacion"
			,S."PrecioAPlazo"							 	 			
			,S."MontoAPlazoMonedaSistema"
			,S."MontoAPlazoMonedaInstrumento" 			 	 			
			,S."MontoAPlazoMonedaAdicional"
			--------------------------------------------------------------------------------------------
			,S."Moneda"
			,S."MontoAContado"			
			,S."MontoMercado"			
			,S."MontoAPlazo"
			--------------------------------------------------------------------------------------------
			,S."Rentabilidad" 
			,TC."Cantidad" AS "CantidadCompraTotalAPlazo"
			,TC."Monto" AS "MontoCompraTotalAPlazo"
			,TV."Cantidad" AS "CantidadVentaTotalAPlazo"
			,TV."Monto" AS "MontoVentaTotalAPlazo"
	FROM :v_Simultaneas AS S,
		(SELECT IFNULL(COUNT("Cantidad"),0) AS "Cantidad",IFNULL(SUM("MontoAPlazo"),0) AS "Monto" FROM :v_Simultaneas WHERE "AccionAPlazo" = 'COMPRA') AS TC,
		(SELECT IFNULL(COUNT("Cantidad"),0) AS "Cantidad",IFNULL(SUM("MontoAPlazo"),0) AS "Monto" FROM :v_Simultaneas WHERE "AccionAPlazo" = 'VENTA') AS TV;


END;
GO