SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosPeru";
GO
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosPeru" AS 

SELECT
	   MvtoCmbteCobro."IdMovimientoComprobanteCobro" ,
	   MvtoCmbteCobro."IdCobro" ,
	   Cobro."Descripcion" AS "NombreCobro" ,
	   Cobro."IdTipoCobro" AS "FormaCobro" ,
	   Cobro."Otros_CheckAllIn" AS "ModificarValor",
	   Cobro."Otros_CheckPermiteAumentarValor" AS "AumentarValor",
	   Cobro."Otros_CheckPermiteDisminuirValor" AS "DisminuirValor",
	   TipoCobro."NombreTipoCobro" AS "NombreFormaCobro",
	   Cobro."IdClaseCobro" AS "IdCobroClasificacion",
	   ClaseCobro."NombreClaseCobro" AS "CodigoCobroClasificacion",
	   ClaseCobro."IdClaseCobroBase" AS "DescripcionCobroClasificacion",
	   
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
	   'N' AS "EsRecalculable" 
  FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" As MvtoCmbteCobro 
      INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro" As Cobro On Cobro."CodigoDefinicionCobro" = MvtoCmbteCobro."IdCobro"
      LEFT JOIN "BSCL.Plataforma.BD::COM_2.TipoCobro" As TipoCobro On TipoCobro."IdTipoCobro" = Cobro."IdTipoCobro"
      LEFT JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro" As ClaseCobro On ClaseCobro."IdClaseCobro" = Cobro."IdClaseCobro"
      
      LEFT JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" As EstadoFact On EstadoFact."IdEstado" = MvtoCmbteCobro."IdEstado" AND Upper(EstadoFact."NombreEntidad") = Upper('FAC.MovimientoComprobanteCobro')
      LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" As Estado On Estado."IdEstado" = EstadoFact."IdEstado"
      LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" As MonedaOperacion On MonedaOperacion."IdMoneda" = MvtoCmbteCobro."IdMonedaCobro" 
      LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" As MonedaSistema On MonedaSistema."IdMoneda" = MvtoCmbteCobro."IdMonedaMovimientoComprobanteCobro" 
      LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" As MonedaLiq On MonedaLiq."IdMoneda" = MvtoCmbteCobro."IdMonedaLiquidacionCobro"
      --WHERE MvtoCmbteCobro."IdMovimientoComprobante" = 3977
  WITH READ ONLY;
  
GO  
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosPeru" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO
