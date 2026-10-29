-- ******************************************************************** 
-- SQL de servicio No. 700.146 - ValidarTransferenciaDVP.java
 
   SELECT DISTINCT 
             CD."IdTipoOperacion"
            ,OP."FolioOperacion" AS "Folio"
            ,NT."Monto" AS "MontoNominaTransferencia"
            ,COALESCE(VOP."Monto", 0) AS "MontoCustodia"
            ,COALESCE(FP."ClaveDCV",'') AS "ClaveDCV"
            ,COALESCE(OP."Clave",'') AS "ClaveCustodia"
            ,NTD."IdEstado"
    FROM "BSCL.Tesoreria.BD::TES.Comprobante" C
   INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" CD ON CD."IdComprobante" = C."IdComprobante"
   INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" FP ON FP."IdComprobante" = CD."IdComprobante" 
   INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" NTD ON NTD."IdComprobanteFormaDePago" = FP."IdComprobanteFormaDePago"
   INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" NT ON NT."IdNominaTransferencia" = NTD."IdNominaTransferencia" 
   INNER JOIN "BSCL.Custodia.BD::CUS.OperacionesPendientes" OP ON OP."IdTipoOperacion" = CD."IdTipoOperacion" 
   INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarOperacionesPendientes" VOP ON VOP."IdTipoOperacion" = OP."IdTipoOperacion" AND VOP."FolioOperacion" = OP."FolioOperacion"   
   INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SN ON  SN."IdPersona" = C."IdPersona" AND SN."IdCliente" = VOP."IdCliente"   
        WHERE NT."IdNominaTransferencia" = :IdNominaTransferencia
          AND OP."Clave" = :ClaveDCV
          AND FP."ClaveDCV" = OP."Clave" 
          AND NT."Monto" = COALESCE(VOP."Monto", 0)
          AND FP."IdMedioDePago" = 'TDP'
          