SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarAsignacionUI";
GO

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarAsignacionUI" AS
SELECT
	ASIG."IdAsignacion"
   ,ASIG."IdTransaccion"
   ,ASIG."IdOrden"
   ,ASIG."FechaAsignacion"
   ,CASE "ClasificacionTipoInstrumento_$8"."IdClasificacionTipoInstrumento" WHEN 'IF' THEN ASIG."ValorRescateAsignado" 
	ELSE ASIG."CantidadAsignada" END AS "CantidadAsignada"
   ,ASIG."MontoAsignado"
   ,ASIG."MontoAsignadoLocal"
   ,ASIG."MontoAsignadoLiquidacion"
   ,ASIG."EsAPlazo"
   ,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(ASIG."EsAPlazo") AS "EsAPlazoUI"
   ,ASIG."IdAsignacionRef"
   ,COALESCE("Asignacion_PLAZO"."MontoAsignado",0) as "MontoAsignadoPlazo"
   ,COALESCE("Transaccion_PLAZO"."Precio",0) as "PrecioAsignadoPlazo"
   ,ASIG."IdMovimientoComprobante"
   ,ASIG."IdEstadoAsignacion" AS "IdEstadoAsignacion"
   ,"ESTASIGNACION"."Nombre" AS "EstadoAsignacion"
   ,"Instrumento_$13"."IdMoneda" AS "IdMonedaInstrumento"
   ,TRAN."IdMoneda" AS "IdMonedaOperacion"
   ,TRAN."IdTipoReajuste" AS "IdTipoReajuste"
   ,"Orden_$3"."IdMonedaLiquidacionOrden" AS "IdMonedaLiquidacionOrden"
   ,"Orden_$3"."IdEmpresa" AS "IdEmpresa"
   ,"Orden_$3"."IdFormaCustodia" AS "IdFormaCustodia"
   ,FORMACUS."Nombre" AS "NombreFormaCustodia"
   ,TRAN."IndicadorOrdenDirecta"
   ,TRAN."IdEmisor" AS "IdEmisor"
   ,TRAN."EsVentaCorta" AS "EsVentaCorta"
   ,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(TRAN."EsVentaCorta") AS "EsVentaCortaUI"
   ,TRAN."Precio" AS "PrecioAsignacion"
   ,TRAN."Tasa" AS "Tasa"
   ,"Orden_$3"."TasaVentaCorta" AS "TasaVentaCorta"
   ,CASE
   		WHEN TRAN."EsVentaCorta" = 'S' THEN "Orden_$3"."PlazoVentaCorta"
   		WHEN TRAN."IdTipoInstrumento" = 'SIM' THEN TRAN."Plazo" 
         WHEN TRAN."IdClasificacionTipoInstrumento" = 'IF' THEN TRAN."Plazo" 
   		ELSE 0 
   	END AS "Plazo"
   ,TRAN."IdCondicionDeLiquidacion" AS "IdCondicionDeLiquidacion"
   ,"CondicionDeLiquidacion_$12"."Codigo" AS "CodigoCondicionDeLiquidacion"
   ,"CondicionDeLiquidacion_$12"."Nombre" AS "NombreCondicionDeLiquidacion"
   ,TRAN."FechaLiquidacion" AS "FechaLiquidacion"
   ,TRAN."Folio" AS "FolioTransaccion"
   ,TRAN."FechaOperacion" AS "FechaOperacionTransaccion"
   ,TRAN."IdCorredorContraparte" AS "IdCorredorContraparte"
   ,"Orden_$3"."IdLibroOrdenes" AS "IdLibroOrdenes"
   ,"Orden_$3"."Folio" AS "FolioOrden"
   ,"LibroOrden"."NombreLibroOrdenes" AS "NombreLibroOrdenes"
   ,CASE WHEN TRAN."EsVentaCorta" = 'S' THEN  "Orden_$3"."FechaVencimiento" ELSE TRAN."FechaVencimiento" END AS "FechaVencimiento"
   ,"TipoOperacion_$9"."IdTipoOperacion"  AS "IdTipoOperacion" 
   ,"TipoOperacion_$9"."AccionOperacion" AS "CodigoTipoOperacionAccion"  --CAMBIAR POR EL DE ABAJO
   ,"TipoOperacion_$9"."AccionOperacion" AS "AccionTipoOperacion"
   ,"TipoOperacion_$9"."AccionOrden" AS "AccionOrden"
   ,CASE 
      WHEN (TRAN."EsVentaCorta" = 'S' AND TRAN."IdClasificacionTipoInstrumento" = 'RV') THEN 'INTERMEDIACION VENTA CORTA RENTA VARIABLE'
      WHEN (TRAN."EsVentaCorta" = 'S' AND TRAN."IdClasificacionTipoInstrumento" = 'RF') THEN 'INTERMEDIACION VENTA CORTA RENTA FIJA'
      ELSE "TipoOperacion_$9"."Nombre"
   END AS "NombreTipoOperacion"
   ,"Orden_$3"."IdTipoCanalIngreso" AS "IdTipoCanalIngreso"
   ,"TipoCanalIngreso_$11"."Nombre" AS "NombreTipoCanalIngreso"
   ,"TipoProducto_$10"."IdTipoProducto" AS "IdTipoProducto"
   ,TRAN."IdInstrumento" AS "IdInstrumento"
   	--MEZCLADO
   ,CASE WHEN "TipoInstrumento_$7"."IdTipoInstrumento" = 'SIM' THEN TRAN."IdInstrumentoSubyacente" ELSE TRAN."IdInstrumento" END AS "IdInstrumentoUI"
   ,CASE WHEN "TipoInstrumento_$7"."IdTipoInstrumento" = 'SIM' THEN "Instrumento_ASOC"."Nemotecnico" ELSE TRAN."Nemotecnico" END AS "InstrumentoUI"
   	--SUBYACENTE
   ,TRAN."IdInstrumentoSubyacente" AS "IdInstrumentoSubyacente"
   ,"Instrumento_ASOC"."Nemotecnico" AS "InstrumentoSubyacente"
   ,"TipoInstrumento_ASOC"."IdTipoInstrumento" AS "IdTipoInstrumentoSubyacente"
   ,"TipoInstrumento_ASOC"."Nombre" AS "NombreTipoInstrumentoSubyacente"
   ,"TipoInstrumento_ASOC"."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumentoSubyacente"
   ,"ClasificacionTipoInstrumento_ASOC"."Nombre" AS "NombreClasificacionTipoInstrumentoSubyacente"
   ,"ConsultarPortafolio_$0"."CodigoPortafolio"
   ,TRAN."IdBolsa" AS "IdBolsa"
   ,"Bolsa_$4"."Nombre" AS "NombreBolsa"
   ,"Orden_$3"."FechaOperacion" AS "FechaOperacionOrden"
   ,TRAN."Nemotecnico" AS "Instrumento"
   ,"TipoInstrumento_$7"."IdTipoInstrumento" AS "IdTipoInstrumento"
   ,"TipoInstrumento_$7"."Nombre" AS "NombreTipoInstrumento"
   ,"ClasificacionTipoInstrumento_$8"."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
   ,"ClasificacionTipoInstrumento_$8"."Nombre" AS "NombreClasificacionTipoInstrumento"
   ,"Orden_$3"."IdCliente" AS "IdCliente"
   ,"ConsultarCliente_$5"."DocumentoIdentidadAdicional" AS "DocumentoIdentidadAdicional"
   ,"ConsultarCliente_$5"."DocumentoIdentidad" AS "DocumentoIdentidad"
   ,"ConsultarCliente_$5"."RazonSocial" AS "RazonSocial"
   ,"ConsultarCliente_$5"."NombreCompleto" AS "NombreCompleto"
   ,"Orden_$3"."IdPortafolioCliente" AS "IdPortafolio"
   ,ASIG."SecAsignacionMigracion"
   ,"Orden_$3"."IdEmpleadoPortafolio"
   ,"Orden_$3"."IdEmpleadoDigitador"
   ,"Orden_$3"."IdTipoCanalDistribucion"
   ,"CanalDistribucion"."Nombre" AS "NombreTipoCanalDistribucion"
   ,"EmpleadoPortafolio"."CodigoEmpleado" as "CodigoEmpleadoPortafolio"
   ,"EmpleadoPortafolio"."NombreSucursal" as "NombreSucursalEmpleadoPortafolio"
   ,"EmpleadoDigitador"."CodigoEmpleado" as "CodigoEmpleadoDigitador"
   ,ASIG."FolioCartera" as "FolioCartera"
   ,COALESCE(ESTADOPLAFAC."Nombre",ESTASIGNACION."Nombre") AS "NombreEstadoFacturacion"
   ,ASIG."CodigoIFRS"
   ,"FormaDePagoOrden"."IdMedioDePago"
   ,COALESCE("MedioPago"."Nombre",'SIN DEFINIR') AS "NombreMedioDePago"
   	--OPCIONES CL
   ,"Instrumento_$6"."ModalidadLiquidacion" AS "IdModalidadLiquidacion"
   ,CASE WHEN "Instrumento_$6"."ModalidadLiquidacion" = 'P' THEN 'FISICA' WHEN "Instrumento_$6"."ModalidadLiquidacion" = 'C' THEN 'FINANCIERA' ELSE '' END AS "ModalidadLiquidacion"
   ,"Instrumento_$6"."ModalidadEjercicio" AS "IdModalidadEjercicio"
   ,CASE WHEN "Instrumento_$6"."ModalidadEjercicio" = 'A' THEN 'AUTOMATICA' WHEN "Instrumento_$6"."ModalidadEjercicio" = 'M' THEN 'MANUAL' ELSE '' END AS "ModalidadEjercicio"
   ,"Instrumento_$6"."EsPutEsCall" AS "TipoOpcion"
   ,CASE WHEN "Instrumento_$6"."EsPutEsCall" = 'CALL' THEN 'C' WHEN "Instrumento_$6"."EsPutEsCall" = 'PUT' THEN 'P' ELSE '' END AS "IdTipoOpcion"
   ,"Instrumento_$6"."EstiloOpcion" AS "IdEstiloOpcion"
   ,CASE WHEN "Instrumento_$6"."EstiloOpcion" = 'A' THEN 'AMERICANA' WHEN "Instrumento_$6"."EstiloOpcion" = 'E' THEN 'EUROPEA' ELSE '' END AS "EstiloOpcion"
   ,"Instrumento_$6"."PrecioStrike"
   ,"Instrumento_$6"."Tamano"
   
   --,CCP."Codigo" as "CodigoBoveda"
   ,BOV."Nombre" AS "NombreBoveda"
   ,BOV."Codigo" AS "CodigoBoveda"
   
   ,CCP."IdCuentaCustodioPortafolio"
   ,CASE WHEN coalesce(CCP."IdPersonaBancoCustodio",0)=0 THEN 'N' ELSE 'S' END AS "IndicadorBancoCustodio"
   ,CCP."CodigoBancoCustodio" as "CodigoBancoCustodio"
   
   ,CCP."CodigoBancoLiquidador" as "CodigoBancoLiquidador"
   ,CCP."NombreModeloCustodia" as "NombreModeloCustodia"
   ,CCP."NombreTipoCustodia" as "NombreTipoCustodia"
   ,CCP."NombreCompletoBancoCustodio" as "NombreCompletoBancoCustodio"
   ,CCP."NombreCompletoBancoLiquidador" as "NombreCompletoBancoLiquidador"
   ,MatrizAgrupador."Producto" AS "Producto"
   ,COALESCE(TIPOLIQ."TipoLiquidacion",'') AS "TipoLiquidacion"
   ,MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") AS "IdProductoAgrupador"
   ,ASIG."TipoCambio" AS "TipoCambio"
FROM  "BSCL.Orden.BD::ORD.Asignacion" AS ASIG
LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" AS "Asignacion_PLAZO" ON  ("Asignacion_PLAZO"."IdAsignacion" = ASIG."IdAsignacionRef" AND ASIG."EsAPlazo" = 'N' )
LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" AS "Transaccion_PLAZO" ON  ( "Asignacion_PLAZO"."IdTransaccion" = "Transaccion_PLAZO"."IdTransaccion" )
LEFT JOIN "BSCL.Orden.BD::ORD.EstadoOrden" AS "EstadoOrden_$1" ON  (  ( "EstadoOrden_$1"."IdEstado" = ASIG."IdEstadoAsignacion" )  AND  ( "EstadoOrden_$1"."NombreEntidad" = 'ASIGNACION' )  )  
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "ESTASIGNACION" ON "ESTASIGNACION"."IdEstado" = "EstadoOrden_$1"."IdEstado"
LEFT JOIN "BSCL.Orden.BD::ORD.Orden" AS "Orden_$3" ON  ( "Orden_$3"."IdOrden" = ASIG."IdOrden" )
LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" AS TRAN ON  ( TRAN."IdTransaccion" = ASIG."IdTransaccion" )
LEFT JOIN "BSCL.Orden.BD::ORD.TipoFormaCustodiaOrden" AS FORMACUS ON FORMACUS."IdTipoFormaCustodiaOrden" = "Orden_$3"."IdFormaCustodia"
LEFT JOIN "BSCL.Orden.BD::ORD.TipoCanalDistribucion" "CanalDistribucion" on "CanalDistribucion"."IdTipoCanalDistribucion" = "Orden_$3"."IdTipoCanalDistribucion"
--empleado portafolio
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS "EmpleadoPortafolio" ON "EmpleadoPortafolio"."IdEmpleado" = "Orden_$3"."IdEmpleadoPortafolio"
--empleado digitador
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS "EmpleadoDigitador" ON "EmpleadoDigitador"."IdEmpleado" = "Orden_$3"."IdEmpleadoDigitador"
LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" AS "Bolsa_$4" ON  ( "Bolsa_$4"."IdBolsa" = TRAN."IdBolsa" )
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "ConsultarPortafolio_$0" ON  ( "Orden_$3"."IdPortafolioCliente" = "ConsultarPortafolio_$0"."IdPortafolio" )
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarCliente_$5" ON  ( "ConsultarCliente_$5"."IdCliente" = "ConsultarPortafolio_$0"."IdCliente" )
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_$6" ON  ( "Instrumento_$6"."IdInstrumento" = TRAN."IdInstrumento" )
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumento_$7" ON  ( "TipoInstrumento_$7"."IdTipoInstrumento" = "Instrumento_$6"."IdTipoInstrumento" )
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS "ClasificacionTipoInstrumento_$8" ON  ( "ClasificacionTipoInstrumento_$8"."IdClasificacionTipoInstrumento" = "TipoInstrumento_$7"."IdClasificacionTipoInstrumento" )
--SUBYACENTE
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_ASOC" ON  ( "Instrumento_ASOC"."IdInstrumento" = TRAN."IdInstrumentoSubyacente" )
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumento_ASOC" ON  ( "TipoInstrumento_ASOC"."IdTipoInstrumento" = "Instrumento_ASOC"."IdTipoInstrumento" )
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS "ClasificacionTipoInstrumento_ASOC" ON  ( "ClasificacionTipoInstrumento_ASOC"."IdClasificacionTipoInstrumento" = "TipoInstrumento_ASOC"."IdClasificacionTipoInstrumento" )
LEFT JOIN "BSCL.Orden.BD::ORD.FormaDePagoOrden" AS "FormaDePagoOrden" ON "FormaDePagoOrden"."IdOrden" = ASIG."IdOrden"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion_$9" ON  ( "TipoOperacion_$9"."IdTipoOperacion" = TRAN."IdTipoOperacion" )
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" AS "TipoProducto_$10" ON  ( "TipoProducto_$10"."IdTipoProducto" = "TipoOperacion_$9"."IdTipoProducto" )
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS "CondicionDeLiquidacion_$12" ON  ( "CondicionDeLiquidacion_$12"."IdCondicionDeLiquidacion" = TRAN."IdCondicionDeLiquidacion" )
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_$13" ON  ( "Instrumento_$13"."IdInstrumento" = TRAN."IdInstrumento" )
LEFT JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" AS "LibroOrden" ON "LibroOrden"."IdLibroOrdenes" = "Orden_$3"."IdLibroOrdenes"
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" AS "TipoCanalIngreso_$11" ON  ( "TipoCanalIngreso_$11"."IdTipoCanalIngreso" = "Orden_$3"."IdTipoCanalIngreso" )
LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" mov on asig."IdMovimientoComprobante" = mov."IdMovimientoComprobante"
LEFT JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS ESTADOFACTURACION     ON ESTADOFACTURACION."IdEstado" = mov."IdEstadoFacturacion" AND ESTADOFACTURACION."NombreEntidad" = 'FAC.MovimientoComprobante'
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTADOPLAFAC ON ESTADOPLAFAC."IdEstado" = ESTADOFACTURACION."IdEstado"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" CCP ON (CCP."IdCuentaCustodioPortafolio" = "Orden_$3"."IdCuentaCustodioPortafolio")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" BOV ON (BOV."IdBoveda" = "Orden_$3"."IdBoveda")
LEFT JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS "MedioPago" ON "MedioPago"."IdMedioDePago" = "FormaDePagoOrden"."IdMedioDePago"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = "ConsultarPortafolio_$0"."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.Facturacion.BD::FAC.TipoLiquidacionComisionFactura" AS TIPOLIQ ON TIPOLIQ."IdTipoLiquidacionComisionFactura" = "Orden_$3"."IdTipoLiquidacionComisionFactura"
WHERE "ClasificacionTipoInstrumento_$8"."IdClasificacionTipoInstrumento" != 'RE';
GO

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarAsignacionUI" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO