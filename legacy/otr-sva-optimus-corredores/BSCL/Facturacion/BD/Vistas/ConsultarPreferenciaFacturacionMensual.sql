SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarPreferenciaFacturacionMensual";
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarPreferenciaFacturacionMensual" AS 
SELECT Cmbte."IdComprobante",
       Cmbte."FolioComprobante",
       Cmbte."IdTipoComprobante",
       Cmbte."FechaComprobante",
       PlaConc."Nombre" AS "NombreConcepto",
       Cmbte."IdCliente",
       Cmbte."IdPortafolio",
       Portafolio."CodigoPortafolio",
       Cmbte."NombreLargoCliente" AS "NombreCliente",
       Cmbte."DocumentoIdentidad",
       Cmbte."Monto",
       Cmbte."MontoComprobante",
       Cmbte."MontoLiquidacion",
       Cmbte."IdCondicionLiquidacion"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS Cmbte
  INNER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS PlaConc
    ON PlaConc."IdConcepto" = Cmbte."IdConcepto"
   AND PlaConc."IdEstado" = 'VIG'
   INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS  Portafolio
    ON Portafolio."IdPortafolio" = Cmbte."IdPortafolio"    
WHERE Cmbte."MontoCobro" = 0
  AND Cmbte."IdEstadoComprobante" = 'ENV'
  AND Cmbte."IdTipoComprobante" IN ('FAE', 'FAC')
  AND Cmbte."ModPrefFact" = 'MODPREFMENS'
  AND Cmbte."IdEstadoModPrefFact" = 'VIG'
MINUS
SELECT Cmbte."IdComprobante",
       Cmbte."FolioComprobante",
       Cmbte."IdTipoComprobante",
       Cmbte."FechaComprobante",
       PlaConc."Nombre" AS "NombreConcepto",
       Cmbte."IdCliente",
       Cmbte."IdPortafolio",
       Portafolio."CodigoPortafolio",
       Cmbte."NombreLargoCliente" AS "NombreCliente",
       Cmbte."DocumentoIdentidad",
       Cmbte."Monto",
       Cmbte."MontoComprobante",
       Cmbte."MontoLiquidacion",
       Cmbte."IdCondicionLiquidacion"       
FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS Cmbte
  INNER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS PlaConc
    ON PlaConc."IdConcepto" = Cmbte."IdConcepto"
   AND PlaConc."IdEstado" = 'VIG'
   INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS  Portafolio
    ON Portafolio."IdPortafolio" = Cmbte."IdPortafolio" 
   INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS CmbteAsocANC
      On CmbteAsocANC."IdComprobante" = Cmbte."IdComprobanteReferencia"
     AND CmbteAsocANC."IdEstadoComprobante" <> 'ANU' 
WHERE Cmbte."MontoCobro" = 0
  AND Cmbte."IdEstadoComprobante" = 'ENV'
  AND Cmbte."IdTipoComprobante" IN ('FAE', 'FAC')
  AND Cmbte."ModPrefFact" = 'MODPREFMENS'
  AND Cmbte."IdEstadoModPrefFact" = 'VIG';
  
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarPreferenciaFacturacionMensual" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
