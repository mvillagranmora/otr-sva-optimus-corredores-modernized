SET SCHEMA "BSCL";

DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferencia";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferencia" AS
WITH DetalleSinCheque AS (
    SELECT 
        NTD."IdNominaTransferencia", 
        COUNT(NTD."IdNominaTransferenciaDetalle") - 
        SUM(CASE WHEN CFP."IdChequera" > 0 THEN 1 ELSE 0 END) AS "DetalleSinCheque"
    FROM "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" AS NTD
    INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP 
        ON CFP."IdComprobanteFormaDePago" = NTD."IdComprobanteFormaDePago"
    WHERE CFP."IdMedioDePago" = 'CHE'
    GROUP BY NTD."IdNominaTransferencia"
)
SELECT   NominaTransferencia."IdNominaTransferencia"
		,NominaTransferencia."IdEmpresa"
		,NominaTransferencia."IdComprobantePartidaEgreso"
		,ComprobanteEgreso."IdTipoMovimiento"
		,NominaTransferencia."FechaMovimiento"
		,NominaTransferencia."FechaIngresoRegistro"
		,NominaTransferencia."FechaLiquidacion"
		,NominaTransferencia."FechaRecepcion"
		,NominaTransferencia."IdMedioDePago"
		,MedioDePago."TipoMedioDePago"
		,MedioDePago."Nombre" AS "NombreMedioDePago"
		,NominaTransferencia."IdModalidadDePago"
		,ModalidadDePago."Nombre" AS "NombreModalidadDePago"
		,NominaTransferencia."IdTipoCuentaBanco"
		,NominaTransferencia."IdCuentaBanco"
		,CuentaBancaria."NombreCuentaBancariaFull"
		,CuentaBancaria."IdBanco"
		,CuentaBancaria."NombreBanco"
		,CuentaBancaria."NumeroCuentaBancaria"
		,NominaTransferencia."IdMoneda"
		,Moneda."Nombre" AS "NombreMoneda"
		,NominaTransferencia."Monto"
		,NominaTransferencia."UsaTalonarioCheque"
		,NominaTransferencia."EsTransferencia"
		,NominaTransferencia."IdFormatoArchivo"
		,NominaTransferencia."NombreArchivo"
		,NominaTransferencia."EsNomina"
		,NominaTransferencia."IdFormatoNomina"			
		,NominaTransferencia."IdUsuario"
		,NominaTransferencia."IdEstado"
		,EstadoTesoreria."NombreEstado"
		,COALESCE(DetalleSinCheque."DetalleSinCheque",0) AS "DetalleSinCheque"		
		-- Datos del formasto del archivo de transferencia a generar
		,FormatoArchivo."RutaBase"
		,FormatoArchivo."RutaTesoreria"
		,COALESCE(CartaInstruccion."TemplateCartaInstruccion",'CAPABA') AS "TemplateCartaInstruccion"
 FROM "BSCL.Tesoreria.BD::TES.NominaTransferencia" AS NominaTransferencia
 INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS ComprobanteEgreso 
 	ON ComprobanteEgreso."IdComprobante" = NominaTransferencia."IdComprobantePartidaEgreso"
 INNER JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS MedioDePago 
 	ON MedioDePago."IdMedioDePago" = NominaTransferencia."IdMedioDePago"
 INNER JOIN "BSCL.Plataforma.BD::PAG.ModalidadDePago" AS ModalidadDePago 
 	ON ModalidadDePago."IdModalidadDePago" = NominaTransferencia."IdModalidadDePago"
 INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" AS CuentaBancaria 
 	ON CuentaBancaria."IdCuentaBanco" = NominaTransferencia."IdCuentaBanco"
 INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarEstadosTesoreria" AS EstadoTesoreria 
 	ON EstadoTesoreria."IdEstado" = NominaTransferencia."IdEstado" 
 	AND EstadoTesoreria."NombreEntidad" = 'TES.NominaTransferencia'
 INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS Moneda 
 	ON Moneda."IdMoneda" = NominaTransferencia."IdMoneda" 
 LEFT JOIN "BSCL.Tesoreria.BD::TES.CartasInstruccionPagoBanco" AS CartaInstruccion 
 	ON (CartaInstruccion."IdBanco" = CuentaBancaria."IdBanco")
 LEFT JOIN DetalleSinCheque
    ON DetalleSinCheque."IdNominaTransferencia" = NominaTransferencia."IdNominaTransferencia"
 LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivo" AS FormatoArchivo 
 	ON (FormatoArchivo."IdBanco" = CuentaBancaria."IdBanco"
	AND FormatoArchivo."IdMoneda" = NominaTransferencia."IdMoneda"
	AND FormatoArchivo."IdMedioDePago" = NominaTransferencia."IdMedioDePago"
	AND FormatoArchivo."IdEmpresa" = NominaTransferencia."IdEmpresa")
WITH READ ONLY;

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferencia" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
