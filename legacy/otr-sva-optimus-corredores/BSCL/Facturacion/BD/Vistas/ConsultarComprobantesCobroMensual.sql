SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobantesCobroMensual"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobantesCobroMensual" AS
 
-- Comprobantes con facturaciones de cobros mensuales por contrato
((SELECT DISTINCT
	 Cmbte."IdComprobante"
	,Cmbte."FolioComprobante"
	,Cmbte."IdTipoComprobante"
	,Cmbte."FechaComprobante"
	,Cmbte."IdTipoInstrumento"
	,Cmbte."IdClasificacionTipoInstrumento"
	,Cmbte."IdConcepto"
	,PlaConc."Nombre" 				AS "NombreConcepto"
	,Cmbte."IdCliente"
	,Cmbte."IdPortafolio"
	,Portafolio."CodigoPortafolio"
	,Cmbte."NombreLargoCliente" 	AS "NombreCliente"
	,Cmbte."DocumentoIdentidad"
	,Cmbte."IdMoneda"
	,Cmbte."Monto"
	,Cmbte."MontoComprobante"
	,Cmbte."MontoLiquidacion"
	,Cmbte."IdCondicionLiquidacion"
	,Cont."IdContratoComision"
	,Cont."Nombre"					AS "NombreContrato"
	,Cmbte."ModPrefFact"			AS "CodigoModoFacturacionComprobante"
	,Ind."DetalleIndicador" 		AS "DescripcionModoFacturacionComprobante"
	,Cmbte."IdEmpresa"
	,Ccm."IdComprobante"			AS "IdComprobanteCobroMensual"
	,CASE WHEN Cmcms."IdEstadoComprobante" = 'ANU' OR Cmcms."IdComprobanteReferencia" > 0 THEN 0 ELSE Ccm."IdComprobanteComisionMensual" END AS "IdComprobanteComisionMensual" 
	,Cmbte."IdComprobanteReferencia"						AS "IdComprobanteReferencia"
	,CASE 
    WHEN Cmbte."IdComprobanteReferencia" > 0
    THEN 'NO DISPONIBLE'
    WHEN CCM."IdMovimientoComprobanteComisionMensual" IS NOT NULL AND fmc."IdEstadoFacturacion" = 'ING'
    THEN 'PENDIENTE'
    WHEN Cmcms."IdComprobante" IS NULL OR (COALESCE(Cmcms."IdEstadoComprobante", '') = 'ANU' AND COALESCE(fmc."IdEstadoFacturacion", '') = 'ANU') OR Cmcms."IdComprobanteReferencia" > 0
    THEN 'NO FACTURADA'
    ELSE 'FACTURADA'
	END AS "Estado"
	,Cont."IdTipoContrato"
 FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS Cmbte 
 LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS CmCdt ON 
 		( CmCdt."IdComprobanteReferencia" = Cmbte."IdComprobante"  
 		AND CmCdt."IdTipoComprobante" IN ('NCE','NCR') )
 INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS Portafolio ON Portafolio."IdPortafolio" = Cmbte."IdPortafolio"
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS PlaConc ON PlaConc."IdConcepto" = Cmbte."IdConcepto"
 LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" AS Ind ON Ind."Codigo" = Cmbte."ModPrefFact" AND Ind."CodigoTipoIndicador" = 'FAC_MODFAC'
 LEFT JOIN "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensualDetalle" AS Ccm ON Ccm."IdComprobante" = Cmbte."IdComprobante"
 LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS Cmcms ON Cmcms."IdComprobante" = Ccm."IdComprobanteComisionMensual" 
 LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS fmc ON fmc."IdMovimientoComprobante" = Ccm."IdMovimientoComprobanteComisionMensual"
 INNER JOIN ( SELECT Cont."IdContratoComision", Cont."Nombre", Cont."IdMoneda", Cont."IdCliente", Cont."IdPortafolio", Cont."IdClasificacionTipoInstrumento", ContDet."IdConcepto" ,Cont."IdSegmentoCliente", Cont."CodigoArea", Cont."IdTipoContrato", Cont."IdFormatoCobro"
		  	  FROM "BSCL.Plataforma.BD::COM.ContratoComision" AS Cont
             		INNER JOIN "BSCL.Plataforma.BD::COM.DetalleContratoComision" AS ContDet ON ( ContDet."IdContratoComision" = Cont."IdContratoComision"  )
	          WHERE Cont."IdTipoContrato" = 'CONT' AND Cont."IdEstado" = 'ACT' ) AS Cont ON ( Cont."IdMoneda" = Cmbte."IdMoneda" AND Cont."IdCliente" = Cmbte."IdCliente" AND ( Cont."IdFormatoCobro" = 'AGRRUT' OR Cmbte."IdPortafolio" = Cont."IdPortafolio" ) AND 
                   													Cont."IdClasificacionTipoInstrumento" = Cmbte."IdClasificacionTipoInstrumento"  )
 INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON (Cmbte."IdCliente" = CLI."IdCliente" AND Cont."IdSegmentoCliente" = CLI."IdSegmentoCliente" )
 WHERE Cmbte."IdEstadoComprobante" 				IN ('FAC','ENV')
  AND Cmbte."IdTipoComprobante" 				IN ('FAE','FAC')
  AND Cmbte."IdTipoLiquidacionComisionFactura" 	= 'MEN'
  AND Cmbte."ModPrefFact"						= 'MODPREFCONT')

 UNION ALL 
 -- Comprobantes con facturaciones de cobros mensuales por convenio
(SELECT DISTINCT
	 Cmbte."IdComprobante"
	,Cmbte."FolioComprobante"
	,Cmbte."IdTipoComprobante"
	,Cmbte."FechaComprobante"
	,Cmbte."IdTipoInstrumento"
	,Cmbte."IdClasificacionTipoInstrumento"
	,Cmbte."IdConcepto"
	,PlaConc."Nombre" 				AS "NombreConcepto"
	,Cmbte."IdCliente"
	,Cmbte."IdPortafolio"
	,Portafolio."CodigoPortafolio"
	,Cmbte."NombreLargoCliente" 	AS "NombreCliente"
	,Cmbte."DocumentoIdentidad"
	,Cmbte."IdMoneda"
	,Cmbte."Monto"
	,Cmbte."MontoComprobante"
	,Cmbte."MontoLiquidacion"
	,Cmbte."IdCondicionLiquidacion"
	,Cont."IdContratoComision"
	,Cont."Nombre"					AS "NombreContrato"
	,Cmbte."ModPrefFact"			AS "CodigoModoFacturacionComprobante"
	,Ind."DetalleIndicador" 		AS "DescripcionModoFacturacionComprobante"
	,Cmbte."IdEmpresa"
	,Ccm."IdComprobante"			AS "IdComprobanteCobroMensual"
	,Ccm."IdComprobanteComisionMensual"
	,CAST(0 AS BIGINT)				AS "IdComprobanteReferencia"
	,CASE WHEN CmCdt."IdComprobanteReferencia" > 0 THEN 'NO DISPONIBLE'
		  WHEN Ccm."IdComprobante" IS NULL THEN 'NO FACTURADA' ELSE 'FACTURADA' 
	 END AS "Estado"
	,Cont."IdTipoContrato"
 FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS Cmbte 
 LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS CmCdt ON 
 		( CmCdt."IdComprobanteReferencia" = Cmbte."IdComprobante" 
 		AND CmCdt."IdTipoComprobante" IN ('NCE','NCR') )
INNER JOIN "BSCL.Plataforma.BD::COM.ContratoComision" AS Cont ON -- probando
      	( Cont."IdCliente" 						= Cmbte."IdCliente" 
      AND Cont."IdPortafolio" = Cmbte."IdPortafolio"
  	  AND Cont."IdClasificacionTipoInstrumento" = Cmbte."IdClasificacionTipoInstrumento" 
  	  AND Cont."IdTipoContrato"					= 'CONV'
	  AND Cont."IdEstado" = 'ACT' )
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS PlaConc ON PlaConc."IdConcepto" = Cmbte."IdConcepto"
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS Portafolio ON Portafolio."IdPortafolio" = Cmbte."IdPortafolio"
 LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" AS Ind ON Ind."Codigo" = Cmbte."ModPrefFact" AND Ind."CodigoTipoIndicador" = 'FAC_MODFAC'
 LEFT JOIN "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensualDetalle" AS Ccm ON Ccm."IdComprobante" = Cmbte."IdComprobante"
WHERE Cmbte."IdEstadoComprobante" 				IN ('FAC','ENV')
  AND Cmbte."IdTipoComprobante" 				IN ('FAE','FAC')
  AND Cmbte."IdTipoLiquidacionComisionFactura" 	= 'MEN'
  AND Cmbte."ModPrefFact"						= 'MODPREFCONV') 
  
UNION ALL 
 -- Comprobantes de notas de creditos con facturaciones de cobros mensuales por convenio o contrato
(SELECT DISTINCT
	 Cmbte."IdComprobante"
	,Cmbte."FolioComprobante"
	,Cmbte."IdTipoComprobante"
	,Cmbte."FechaComprobante"
	,Cmbte."IdTipoInstrumento"
	,Cmbte."IdClasificacionTipoInstrumento"
	,Cmbte."IdConcepto"
	,PlaConc."Nombre" 				AS "NombreConcepto"
	,Cmbte."IdCliente"
	,Cmbte."IdPortafolio"
	,Portafolio."CodigoPortafolio"
	,Cmbte."NombreLargoCliente" 	AS "NombreCliente"
	,Cmbte."DocumentoIdentidad"
	,Cmbte."IdMoneda"
	,Cmbte."Monto"
	,Cmbte."MontoComprobante"
	,Cmbte."MontoLiquidacion"
	,Cmbte."IdCondicionLiquidacion"
	,CAST(0 AS BIGINT)				AS "IdContratoComision"
	,''								AS "NombreContrato"
	,Cmbte."ModPrefFact"			AS "CodigoModoFacturacionComprobante"
	,Ind."DetalleIndicador" 		AS "DescripcionModoFacturacionComprobante"
	,Cmbte."IdEmpresa"
	,CAST(0 AS BIGINT)				AS "IdComprobanteCobroMensual"
	,CAST(0 AS BIGINT)				AS "IdComprobanteComisionMensual"
	,Cmbte."IdComprobanteReferencia"
	,'NO DISPONIBLE' 				AS "Estado"	
	,Cont."IdTipoContrato"
 FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS Cmbte 
 LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS CmMens ON CmMens."IdComprobanteReferencia" = Cmbte."IdComprobante"
 INNER JOIN "BSCL.Plataforma.BD::COM.ContratoComision" AS Cont ON
      	( Cont."IdCliente" 						= Cmbte."IdCliente"
      AND ( Cont."IdFormatoCobro" = 'AGRRUT' OR Cont."IdPortafolio" = Cmbte."IdPortafolio" )
  	  AND Cont."IdClasificacionTipoInstrumento" = Cmbte."IdClasificacionTipoInstrumento"
	  AND Cont."IdEstado" = 'ACT')
 LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS PlaConc ON PlaConc."IdConcepto" = Cmbte."IdConcepto"
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS Portafolio ON Portafolio."IdPortafolio" = Cmbte."IdPortafolio"
 LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" AS Ind ON Ind."Codigo" = Cmbte."ModPrefFact" AND Ind."CodigoTipoIndicador" = 'FAC_MODFAC'
WHERE Cmbte."IdEstadoComprobante" 				IN ('FAC','ENV')
  AND Cmbte."IdTipoComprobante" 				IN ('NCE','NCR')
  AND CmMens."ModPrefFact" 						IN ('MODPREFCONV','MODPREFCONT')  
  AND CmMens."IdTipoLiquidacionComisionFactura" = 'MEN')
ORDER BY Cmbte."IdComprobante" DESC);

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarComprobantesCobroMensual" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
