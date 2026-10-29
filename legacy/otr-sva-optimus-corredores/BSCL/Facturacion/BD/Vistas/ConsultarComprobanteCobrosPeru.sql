SET SCHEMA "BSCL"; 
GO  
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobrosPeru"; 
GO 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobrosPeru" As 

SELECT CmbteCobro."IdComprobanteCobro", 
       CmbteCobro."IdCobro", 
       Cobro."Descripcion" AS "NombreCobro" ,
       Cobro."IdTipoCobro" AS "FormaCobro" ,
       TipoCobro."NombreTipoCobro" AS "NombreFormaCobro",
       
       Cobro."IdClaseCobro" AS "IdCobroClasificacion",
	   ClaseCobro."NombreClaseCobro" AS "CodigoCobroClasificacion",
	   ClaseCobro."IdClaseCobroBase",
       CmbteCobro."IdComprobante", 
       --Operacion
       CmbteCobro."IdMonedaCobro",
       CmbteCobro."MontoCobro" As "MontoCobro",
       --Local
       CmbteCobro."IdMonedaComprobanteCobro" AS "IdMonedaLocalCobro",              
       CmbteCobro."MontoComprobanteCobro" As "MontoLocalCobro",
       --Liquidacion
       CmbteCobro."IdMonedaLiquidacionCobro",
       MonedaLiq."Codigo" AS "CodigoMonedaLiquidacionCobro", 
       MonedaLiq."Nombre" AS "NombreMonedaLiquidacionCobro", 
       CmbteCobro."MontoLiquidacionCobro" As "MontoLiquidacionCobro",
       CmbteCobro."Porcentaje", 
       CmbteCobro."FechaSistema", 
       CmbteCobro."FechaIngresoRegistro", 
       CmbteCobro."IdEstado",       
       EstadoPla."Nombre" AS "NombreEstado",
       'S' AS "EsRecalculable" 
FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro" As CmbteCobro 

INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro" As Cobro On Cobro."CodigoDefinicionCobro" = CmbteCobro."IdCobro"
LEFT JOIN "BSCL.Plataforma.BD::COM_2.TipoCobro" As TipoCobro On TipoCobro."IdTipoCobro" = Cobro."IdTipoCobro"
LEFT JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro" As ClaseCobro On ClaseCobro."IdClaseCobro" = Cobro."IdClaseCobro"

Left Outer Join "BSCL.Facturacion.BD::FAC.EstadoFacturacion" As EstadoFacturacion On EstadoFacturacion."IdEstado" = CmbteCobro."IdEstado"
Left Outer Join "BSCL.Plataforma.BD::PLA.Estado" As EstadoPla On EstadoPla."IdEstado" = EstadoFacturacion."IdEstado"
Left Outer Join "BSCL.Plataforma.BD::PLA.Moneda" As Moneda On Moneda."IdMoneda" = CmbteCobro."IdMonedaComprobanteCobro"
Left Outer Join "BSCL.Plataforma.BD::PLA.Moneda" As MonedaLiq On MonedaLiq."IdMoneda" = CmbteCobro."IdMonedaLiquidacionCobro"  

WHERE Upper(EstadoFacturacion."NombreEntidad") = Upper('FAC.ComprobanteCobro');
GO 
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobrosPeru" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO 