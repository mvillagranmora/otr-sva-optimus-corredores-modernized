SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoDetalle"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoDetalle" AS  
-- Esta vista se utiliza como base para realizar los calculos de comision y derecho por contrato
-- por rango de MontoMaximo y monto unico o lineal a traves de las siguientes vistas:
-- BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoLineal"
-- BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoPorRango"
 SELECT FACT."IdComprobante"
	   ,FACT."MontoLiquidacion"
 	   ,CD."IdDetalleContratoComision"
	   ,C."IdContratoComision"
	   ,C."IdFormatoCobro"
	   ,CD."EsPorRangos"
   	   ,CD."IdCobro"
   	   ,CD."IdConcepto"
   FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS FACT
   INNER JOIN "BSCL.Plataforma.BD::COM.ContratoComision" AS C ON 
      ( C."IdMoneda" 						= FACT."IdMoneda"      
    AND C."IdCliente" 						= FACT."IdCliente" 
    AND C."IdClasificacionTipoInstrumento" 	= FACT."IdClasificacionTipoInstrumento"
    AND C."IdTipoContrato"					= 'CONT' )
   INNER JOIN "BSCL.Plataforma.BD::COM.DetalleContratoComision" AS CD ON 
      ( CD."IdContratoComision" = C."IdContratoComision" AND CD."IdConcepto" = FACT."IdConcepto" )
 WHERE FACT."ModPrefFact" = 'MODPREFCONT'
   AND C."IdEstado" = 'ACT';

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroContratoDetalle" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
