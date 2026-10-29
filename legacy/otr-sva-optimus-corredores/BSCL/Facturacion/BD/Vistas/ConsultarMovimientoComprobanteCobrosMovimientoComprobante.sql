SET SCHEMA "BSCL";
 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosMovimientoComprobante";

CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosMovimientoComprobante" AS 

SELECT
       MvtoCmbteCobro."IdMovimientoComprobante",
	   MvtoCmbteCobro."IdMovimientoComprobanteCobro" ,
	   MvtoCmbteCobro."IdCobro" ,
	   "Cobro_$1"."NombreCobro" ,
	   "Cobro_$1"."FormaCobro" ,
	   (CASE
	   	  WHEN "Cobro_$1"."FormaCobro" = 'V' THEN 'Variable'
	   	  WHEN "Cobro_$1"."FormaCobro" = 'F' THEN 'Fijo'
	   END) AS "NombreFormaCobro",
	   "Cobro_$1"."IdCobroClasificacion",
	   "CobroClasificacion_$5"."CodigoCobroClasificacion", 
	   "CobroClasificacion_$5"."DescripcionCobroClasificacion",
	   MvtoCmbteCobro."IdMonedaCobro" ,
	   "Moneda_$4"."Codigo" AS "CodigoMoneda" ,
	   "Moneda_$4"."Nombre" AS "NombreMoneda" ,
	   "Moneda_$4"."Decimales" AS "DecimalesMoneda" ,
	   MvtoCmbte."Monto" As "MontoMovimiento",
	   MvtoCmbte."MontoLiquidacion" As "MontoLiquidacionMovimiento",
	   MvtoCmbte."MontoMovimientoComprobante" As "MontoMovimientoComprobanteMovimiento",
	   Round(MvtoCmbteCobro."MontoCobro", 0) As "MontoCobro",
	   MvtoCmbteCobro."Porcentaje" ,
	   MvtoCmbteCobro."FechaSistema" ,
	   MvtoCmbteCobro."FechaIngresoRegistro" ,
	   MvtoCmbteCobro."IdEstado" ,
	   "Estado_$3"."Nombre" AS "NombreEstado" ,
	   "Cobro_$1"."EsRecalculable"
  FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" As MvtoCmbteCobro
     Inner Join "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MvtoCmbte 
       On MvtoCmbte."IdMovimientoComprobante" = MvtoCmbteCobro."IdMovimientoComprobante"
      LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.Cobro" AS "Cobro_$1" ON  "Cobro_$1"."IdCobro" = MvtoCmbteCobro."IdCobro"
      LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" AS "CobroClasificacion_$5" ON  "Cobro_$1"."IdCobroClasificacion" = "CobroClasificacion_$5"."IdCobroClasificacion"
      LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS "EstadoFacturacion_$2" ON "EstadoFacturacion_$2"."IdEstado" = MvtoCmbteCobro."IdEstado"
      LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado_$3" ON  "Estado_$3"."IdEstado" = "EstadoFacturacion_$2"."IdEstado"
      LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS "Moneda_$4" ON  "Moneda_$4"."IdMoneda" = MvtoCmbteCobro."IdMonedaCobro" 
  WHERE Upper("EstadoFacturacion_$2"."NombreEntidad") = Upper('FAC.MovimientoComprobanteCobro');
 
  
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosMovimientoComprobante" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
  
