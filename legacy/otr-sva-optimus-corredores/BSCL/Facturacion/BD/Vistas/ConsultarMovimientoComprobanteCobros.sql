SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobros";
GO
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobros" AS 

SELECT
	   MvtoCmbteCobro."IdMovimientoComprobanteCobro" ,
	   MvtoCmbteCobro."IdCobro" ,
	   Cobro."NombreCobro" ,
	   Cobro."FormaCobro" ,
	   (CASE
	   	  WHEN Cobro."FormaCobro" = 'V' THEN 'Variable'
	   	  WHEN Cobro."FormaCobro" = 'F' THEN 'Fijo'
	   END) AS "NombreFormaCobro",
	   Cobro."IdCobroClasificacion",
	   CobroClasificacion."CodigoCobroClasificacion", 
	   CobroClasificacion."DescripcionCobroClasificacion",
	   MvtoCmbteCobro."IdMovimientoComprobante" ,
	   MvtoCmbteCobro."Monto",
	   MvtoCmbteCobro."MontoLiquidacion",
	   MvtoCmbteCobro."MontoMovimientoComprobante",

	   --Operación
	   MvtoCmbteCobro."IdMonedaCobro",
	   MonedaOperacion."Codigo" AS "CodigoMonedaOperacionCobro" ,
	   MonedaOperacion."Nombre" AS "NombreMonedaOperacionCobro" ,
	   MonedaOperacion."Decimales" AS "DecimalesMonedaOperacionCobro" ,
	   MvtoCmbteCobro."MontoCobro",

	   --Local/Sistema
       MvtoCmbteCobro."IdMonedaMovimientoComprobanteCobro",
	   MonedaSistema."Codigo" AS "CodigoMonedaSistemaCobro" ,
	   MonedaSistema."Nombre" AS "NombreMonedaSistemaCobro" ,
	   MonedaSistema."Decimales" AS "DecimalesMonedaSistemaCobro" ,
	   MvtoCmbteCobro."MontoMovimientoComprobanteCobro",

       --Liquidacion       
	   MvtoCmbteCobro."IdMonedaLiquidacionCobro",
	   MonedaLiq."Codigo" AS "CodigoMonedaLiquidacionCobro" ,
	   MonedaLiq."Nombre" AS "NombreMonedaLiquidacionCobro" ,
	   MonedaLiq."Decimales" AS "DecimalesMonedaLiquCobro" ,
	   MvtoCmbteCobro."MontoLiquidacionCobro",
	   
	   MvtoCmbteCobro."Porcentaje" ,
	   MvtoCmbteCobro."FechaSistema" ,
	   MvtoCmbteCobro."FechaIngresoRegistro" ,
	   MvtoCmbteCobro."IdEstado" ,
	   Estado."Nombre" AS "NombreEstado",
	   Cobro."EsRecalculable" 
  FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" As MvtoCmbteCobro 
      INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" As Cobro 
        On Cobro."IdCobro" = MvtoCmbteCobro."IdCobro"
      INNER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" As CobroClasificacion 
        On Cobro."IdCobroClasificacion" = CobroClasificacion."IdCobroClasificacion"
      LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" As EstadoFact 
        On EstadoFact."IdEstado" = MvtoCmbteCobro."IdEstado"
      LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" As Estado 
        On Estado."IdEstado" = EstadoFact."IdEstado"
      LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" As MonedaOperacion 
        On MonedaOperacion."IdMoneda" = MvtoCmbteCobro."IdMonedaCobro" 
      LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" As MonedaSistema
        On MonedaSistema."IdMoneda" = MvtoCmbteCobro."IdMonedaMovimientoComprobanteCobro" 
      LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" As MonedaLiq 
        On MonedaLiq."IdMoneda" = MvtoCmbteCobro."IdMonedaLiquidacionCobro"
        
  WHERE Upper(EstadoFact."NombreEntidad") = Upper('FAC.MovimientoComprobanteCobro')  
  WITH READ ONLY;
  
GO  
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobros" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
  
GO