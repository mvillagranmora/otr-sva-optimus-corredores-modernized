SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroConvenioDetalle"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroConvenioDetalle" AS  
-- Esta vista se utiliza como base para realizar los calculos de comision y derecho por convenio
SELECT MOV."IdComprobante"
	  ,MOV."IdMovimientoComprobante"
	  ,MOV."IdCliente"
	  ,MOV."IdPortafolio"
	  ,CL."CodigoCobroClasificacion"
      ,MOVCOB."MontoCobro"
 FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS FACT
INNER JOIN "BSCL.Plataforma.BD::COM.ContratoComision" AS C ON --( C."IdContratoComision" = FACT."IdContratoComision" )
    ( C."IdCliente" 						= FACT."IdCliente"
  AND C."IdClasificacionTipoInstrumento" 	= FACT."IdClasificacionTipoInstrumento"
  AND C."IdTipoContrato"				= 'CONV' 
  AND C."IdEstado"              = 'ACT' )
INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MOV ON ( MOV."IdComprobante" = FACT."IdComprobante" )
INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" AS MOVCOB ON ( MOVCOB."IdMovimientoComprobante" = MOV."IdMovimientoComprobante" )
INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" AS CO ON ( CO."IdCobro" = MOVCOB."IdCobro" )
INNER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" AS CL ON ( CL."IdCobroClasificacion" = CO."IdCobroClasificacion" );

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroConvenioDetalle" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
