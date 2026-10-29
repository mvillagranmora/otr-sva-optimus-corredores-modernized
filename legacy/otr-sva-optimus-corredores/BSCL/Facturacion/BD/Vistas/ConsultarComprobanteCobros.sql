SET SCHEMA "BSCL"; 
GO  
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros"; 
GO 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros" As 

SELECT CmbteCobro."IdComprobanteCobro", 
       CmbteCobro."IdCobro", 
       Cobro."NombreCobro", 
       Case 
          When Cobro."FormaCobro" = 'V' Then 'Variable'
       Else 'Fijo'
       End As "FormaCobro" , 
       Cobro."IdCobroClasificacion", 
       CobroClasificacion."CodigoCobroClasificacion", 
       CmbteCobro."IdComprobante", 
       --Operacion
       CmbteCobro."IdMonedaCobro",
       Coalesce("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(CmbteCobro."MontoCobro", FormCmbteCob."MontoDecimales", FormCmbteCob."MontoTrucaRedondea"), CmbteCobro."MontoCobro") As "MontoCobro",
       --Local
       CmbteCobro."IdMonedaComprobanteCobro" AS "IdMonedaLocalCobro",              
       Moneda."Codigo" AS "CodigoMonedaLocalCobro", 
       Moneda."Nombre" AS "NombreMonedaLocalCobro", 
       Moneda."Decimales" AS "DecimalesMonedaLocalCobro",
       Coalesce("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(CmbteCobro."MontoComprobanteCobro", FormCmbteLocCob."MontoDecimales", FormCmbteLocCob."MontoTrucaRedondea"), CmbteCobro."MontoComprobanteCobro") As "MontoLocalCobro",
       --Liquidacion
       CmbteCobro."IdMonedaLiquidacionCobro",
       MonedaLiq."Codigo" AS "CodigoMonedaLiquidacionCobro", 
       MonedaLiq."Nombre" AS "NombreMonedaLiquidacionCobro", 
       Coalesce("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(CmbteCobro."MontoLiquidacionCobro", FormCmbteLiqCob."MontoDecimales", FormCmbteLiqCob."MontoTrucaRedondea"), CmbteCobro."MontoLiquidacionCobro") As "MontoLiquidacionCobro",
       CmbteCobro."Porcentaje", 
       CmbteCobro."FechaSistema", 
       CmbteCobro."FechaIngresoRegistro", 
       CmbteCobro."IdEstado",       
       EstadoPla."Nombre" AS "NombreEstado",
       Cobro."EsRecalculable" 
FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro" As CmbteCobro 
  Left Outer Join "BSCL.Plataforma.BD::COM.Cobro" As Cobro
    On Cobro."IdCobro" = CmbteCobro."IdCobro"
  Left Outer Join "BSCL.Plataforma.BD::COM.CobroClasificacion" As CobroClasificacion 
    On CobroClasificacion."IdCobroClasificacion" = Cobro."IdCobroClasificacion"
  Left Outer Join "BSCL.Facturacion.BD::FAC.EstadoFacturacion" As EstadoFacturacion
    On EstadoFacturacion."IdEstado" = CmbteCobro."IdEstado"
  Left Outer Join "BSCL.Plataforma.BD::PLA.Estado" As EstadoPla
    On EstadoPla."IdEstado" = EstadoFacturacion."IdEstado"
  Left Outer Join "BSCL.Plataforma.BD::PLA.Moneda" As Moneda 
    On Moneda."IdMoneda" = CmbteCobro."IdMonedaComprobanteCobro"
  Left Outer Join "BSCL.Plataforma.BD::PLA.Moneda" As MonedaLiq 
    On MonedaLiq."IdMoneda" = CmbteCobro."IdMonedaLiquidacionCobro"  
  LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" As FormCmbteCob
    On FormCmbteCob."IdMoneda" = "IdMonedaCobro"  
   And FormCmbteCob."IdTipoOperacion" = ''
   And FormCmbteCob."IdTipoInstrumento" = ''
   And FormCmbteCob."IdUsoMonedaFormato" = 'GEN'
  LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" As FormCmbteLocCob 
    On FormCmbteLocCob."IdMoneda" = "IdMonedaComprobanteCobro"
   And FormCmbteLocCob."IdTipoOperacion" = ''
   And FormCmbteLocCob."IdTipoInstrumento" = ''
   And FormCmbteLocCob."IdUsoMonedaFormato" = 'GEN'   
  LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" As FormCmbteLiqCob 
    On FormCmbteLiqCob."IdMoneda" = "IdMonedaLiquidacionCobro"
   And FormCmbteLiqCob."IdTipoOperacion" = ''
   And FormCmbteLiqCob."IdTipoInstrumento" = ''
   And FormCmbteLiqCob."IdUsoMonedaFormato" = 'GEN'
WHERE Upper(EstadoFacturacion."NombreEntidad") = Upper('FAC.ComprobanteCobro');
GO 
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO 