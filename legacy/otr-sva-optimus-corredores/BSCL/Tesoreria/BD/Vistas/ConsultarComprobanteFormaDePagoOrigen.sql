SET SCHEMA "BSCL";

DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePagoOrigen";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePagoOrigen" AS


SELECT 
	 COM."IdComprobante"	
	 ,COM."FechaLiquidacion"
	 ,COM."IdEmpresa"
	 ,COM."IdUsuario"
	 ,COM."IdPersona"
	 ,COM."IdTipoPersona"
	 ,COM."NombrePersona"
	 ,COM."IdTipoDocumentoIdentidad"
	 ,COM."DocumentoIdentidad"	 
	 ,COM."IdTipoPersonaTitularPago"
	 ,COM."IdPersonaTitularPago"
	 ,COM."NombrePersonaTitularPago"
	 ,COM."IdPortafolioTitularPago"
	 ,COM."IdMoneda"
	 ,INGEGR."IdTipoOperacion"
	 ,INGEGR."IdOperacion"
	 ,INGEGR."Monto" AS "MontoOperacion"
	 ,INGEGR."IdMoneda" AS "IdMonedaOperacion"
	 ,INGEGR."IdEmpleadoDigitador"
	 ,INGEGR."IdSucursalEmpleadoDigitador"
	 ,INGEGR."IdModulo"
	 ,INGEGR."IdTipoComprobanteOrigen"
	 ,INGEGR."IdComprobanteOrigen"
	 ,COMPP."IdComprobanteFormaDePago"
	 ,COMPP."Monto" AS "MontoComprobanteFormaDePago"
	 ,COMPP."IdMoneda" AS "IdMonedaFormaDePago"
	 ,COMPP."IdMedioDePago"
	 ,COMPP."NombreMedioDePago" 
	 ,COMPP."IdModalidadDePago"
	 ,COMPP."NombreModalidadDePago"
	 ,COMPP."IdEstado"
	FROM "BSCL.Tesoreria.BD::TES.Comprobante" COM
	INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" COMPP
	ON (COMPP."IdComprobante"=COM."IdComprobante")
	INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" COMDE
	ON (COMDE."IdComprobante"=COMPP."IdComprobante")
	INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" ORIG
	ON (COMDE."IdComprobanteDetalle"=ORIG."IdComprobanteDetalle")
	INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" INGEGR
	ON (ORIG."IdIngresoEgreso"=INGEGR."IdIngresoEgreso")
WITH READ ONLY;

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePagoOrigen" TO  BCS_DESARROLLADOR WITH GRANT OPTION;