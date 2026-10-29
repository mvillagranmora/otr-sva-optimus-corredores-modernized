SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::EVC.ConsultarSimultaneaVigentes";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::EVC.ConsultarSimultaneaVigentes" AS

WITH MovimientoComprobanteUnico AS
(   -- Las comprobantes de facturas de simultaneas solo consideran 1 detalle (en sebra no, por lo que se aplica max para que no se dupliquen registros)
    SELECT "IdComprobante","NemotecnicoSubyacente",  MAX("IdEmpresa") AS "IdEmpresa", MAX("IdMovimientoComprobante") AS "IdMovimientoComprobante"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple"
    GROUP BY "IdComprobante", "NemotecnicoSubyacente"
)

SELECT MovNormal."IdMovimientoComprobante",MovNormal."IdInstrumentoSubyacente"
    FROM "BSCL.Custodia.BD::CUS.Cartera" as Car
     INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub ON ultSub."FolioCarteraRelacionada" = car."FolioCartera"
     INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" as Sub ON (Sub."IdSubyacente" = ultSub."IdSubyacente" )
     LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" as MovNormal on 
        MovNormal."IdEmpresa" = Car."IdEmpresa" and 
        MovNormal."IdComprobante" = Car."FolioComprobante" 
        --MovNormal."NemotecnicoSubyacente" = Sub."Nemotecnico" 
     -- Las comprobantes de facturas de simultaneas solo consideran 1 detalle (en sebra no, por lo que se aplica max para que no se dupliquen registros)
     INNER JOIN MovimientoComprobanteUnico AS MCU ON (MCU."IdEmpresa" = MovNormal."IdEmpresa" AND MCU."IdMovimientoComprobante" = MovNormal."IdMovimientoComprobante" AND MCU."NemotecnicoSubyacente" = MovNormal."NemotecnicoSubyacente")
     WHERE Car."IdTipoInstrumento" IN ('SIM') 
     AND Car."IdEstado" = 'VIG'
UNION ALL
SELECT MovPlazo."IdMovimientoComprobante",MovPlazo."IdInstrumentoSubyacente"
    FROM "BSCL.Custodia.BD::CUS.Cartera" as Car
     INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub ON ultSub."FolioCarteraRelacionada" = car."FolioCartera"
     INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" as Sub ON (Sub."IdSubyacente" = ultSub."IdSubyacente" )
     LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" as MovNormal on 
        MovNormal."IdEmpresa" = Car."IdEmpresa" and 
        MovNormal."IdComprobante" = Car."FolioComprobante" 
        --MovNormal."NemotecnicoSubyacente" = Sub."Nemotecnico" 
     -- Las comprobantes de facturas de simultaneas solo consideran 1 detalle (en sebra no, por lo que se aplica max para que no se dupliquen registros)
     INNER JOIN MovimientoComprobanteUnico AS MCU ON (MCU."IdEmpresa" = MovNormal."IdEmpresa" AND MCU."IdMovimientoComprobante" = MovNormal."IdMovimientoComprobante" AND MCU."NemotecnicoSubyacente" = MovNormal."NemotecnicoSubyacente")
     LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" as MovPlazo on 
        MovPlazo."IdEmpresa" = Car."IdEmpresa" and 
        MovPlazo."IdMovimientoComprobante" = MovNormal."IdMovimientoRelacionado" and 
        MovPlazo."NemotecnicoSubyacente" = Sub."Nemotecnico"
     WHERE Car."IdTipoInstrumento" IN ('SIM') 
     AND Car."IdEstado" = 'VIG'