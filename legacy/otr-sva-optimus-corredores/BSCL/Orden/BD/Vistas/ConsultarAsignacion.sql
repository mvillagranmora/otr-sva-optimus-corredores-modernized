SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarAsignacion";
GO
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarAsignacion"
AS SELECT ASIG."IdAsignacion" 
, ASIG."IdTransaccion" 
, TRAN."IdTransaccionOd" 
, ASIG."IdOrden"
, ASIG."FechaAsignacion"
, ASIG."CantidadAsignada" 
, ASIG."MontoAsignado" 
, ASIG."MontoAsignadoLocal" 
, ASIG."MontoAsignadoLiquidacion" 
, ASIG."ValorRescateAsignado"
, ASIG."EsAPlazo"
, "BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(ASIG."EsAPlazo") AS "EsAPlazoUI" 
, ASIG."IdAsignacionRef"
, COALESCE("Asignacion_PLAZO"."MontoAsignado",0) AS "MontoAsignadoPlazo"
, COALESCE("Transaccion_PLAZO"."Precio",0) AS "PrecioAsignadoPlazo"
, ASIG."IdMovimientoComprobante"
, ASIG."IdEstadoAsignacion" AS "IdEstadoAsignacion" 
, "ESTASIGNACION"."Nombre" AS "EstadoAsignacion" 
, "Instrumento_$13"."IdMoneda" AS "IdMonedaInstrumento" 
, TRAN."IdMonedaSubyacente" AS "IdMonedaInstrumentoSubyacente"
, TRAN."IdMoneda" AS "IdMonedaOperacion" 
, TRAN."IdTipoReajuste" AS "IdTipoReajuste" 
, "Orden_$3"."IdMonedaLiquidacionOrden" AS "IdMonedaLiquidacionOrden" 
, "Orden_$3"."IdFormaCustodia" AS "IdFormaCustodia"
, "Orden_$3"."IdEmpresa" AS "IdEmpresa"
, TRAN."IndicadorOrdenDirecta" 
, TRAN."IdEmisor" AS "IdEmisor"
, TRAN."FechaEmision" AS "FechaEmision"
, TRAN."EsCuentaPropia" AS "EsCuentaPropia"
, TRAN."EsFungible" AS "EsFungible"

, ASIG."FechaParidad" AS "FechaParidad"
, ASIG."Paridad" AS "ValorParidad"
, ASIG."FechaTipoCambio" AS "FechaTipoCambio"
, ASIG."TipoCambio" AS "ValorTipoCambio"
  
, TRAN."EsVentaCorta" AS "EsVentaCorta"
, "BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(TRAN."EsVentaCorta") AS "EsVentaCortaUI" 
, TRAN."Precio" AS "PrecioAsignacion" 
, CASE TRAN."EsVentaCorta" WHEN 'S' THEN "Orden_$3"."TasaVentaCorta" ELSE TRAN."Tasa" END AS "Tasa"
, CASE WHEN TRAN."EsVentaCorta" = 'S' THEN "Orden_$3"."PlazoVentaCorta" WHEN TRAN."IdTipoInstrumento" = 'SIM' THEN TRAN."Plazo" WHEN TRAN."IdClasificacionTipoInstrumento" = 'RE' THEN TRAN."Plazo" ELSE 0 END AS "Plazo"      
, TRAN."IdCondicionDeLiquidacion" AS "IdCondicionDeLiquidacion" 
, "CondicionDeLiquidacion_$12"."Codigo" AS "CodigoCondicionDeLiquidacion" 
, "CondicionDeLiquidacion_$12"."Nombre" AS "NombreCondicionDeLiquidacion" 
, TRAN."FechaLiquidacion" AS "FechaLiquidacion" 
, TRAN."Folio" AS "FolioTransaccion" 
, TRAN."FechaOperacion" AS "FechaOperacionTransaccion" 
, TRAN."IdCorredorContraparte" AS "IdCorredorContraparte" 
, "Orden_$3"."IdLibroOrdenes" AS "IdLibroOrdenes"
, "Orden_$3"."Glosa" AS "GlosaOrden"
, "Orden_$3"."Folio" AS "FolioOrden"
, "LibroOrden"."NombreLibroOrdenes" AS "NombreLibroOrdenes" 
, CASE 	WHEN TRAN."EsVentaCorta" = 'S' THEN  "Orden_$3"."FechaVencimiento"
		ELSE TRAN."FechaVencimiento" END AS "FechaVencimiento" 
, "TipoOperacion_$9"."IdTipoOperacion" AS "IdTipoOperacion" 
, "TipoOperacion_$9"."AccionOperacion" AS "CodigoTipoOperacionAccion"  --CAMBIAR POR EL DE ABAJO
, "TipoOperacion_$9"."AccionOperacion" AS "AccionTipoOperacion"
, "TipoOperacion_$9"."AccionOrden" AS "AccionOrden"
, "TipoOperacion_$9"."Nombre" AS "NombreTipoOperacion" 
, "Orden_$3"."IdTipoCanalIngreso" AS "IdTipoCanalIngreso" 
, "TipoCanalIngreso_$11"."Nombre" AS "NombreTipoCanalIngreso" 
, "TipoProducto_$10"."IdTipoProducto" AS "IdTipoProducto" 
, TRAN."IdInstrumento" AS "IdInstrumento"
--, "Instrumento_$13"."ValorClasificacionRiesgo" AS "ValorClasificacionRiesgo" -- se quita, pues ahora la clasificación de riesgo se debe obtener desde "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" 
--MEZCLADO
, CASE WHEN "TipoInstrumento_$7"."IdTipoInstrumento" = 'SIM' THEN TRAN."IdInstrumentoSubyacente" ELSE TRAN."IdInstrumento" END AS "IdInstrumentoUI"
, CASE WHEN "TipoInstrumento_$7"."IdTipoInstrumento" = 'SIM' THEN "Instrumento_ASOC"."Nemotecnico" ELSE TRAN."Nemotecnico" END AS "InstrumentoUI"


--SUBYACENTE
, TRAN."IdInstrumentoSubyacente" AS "IdInstrumentoSubyacente"
, "Instrumento_ASOC"."Nemotecnico" AS "InstrumentoSubyacente"  
, "TipoInstrumento_ASOC"."IdTipoInstrumento" AS "IdTipoInstrumentoSubyacente" 
, "TipoInstrumento_ASOC"."Nombre" AS "NombreTipoInstrumentoSubyacente"
, "TipoInstrumento_ASOC"."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumentoSubyacente"
, "ClasificacionTipoInstrumento_ASOC"."Nombre" AS "NombreClasificacionTipoInstrumentoSubyacente"

, "ConsultarPortafolio_$0"."CodigoPortafolio" 
, TRAN."IdBolsa" AS "IdBolsa" 
, "Bolsa_$4"."Nombre" AS "NombreBolsa" 
, "Orden_$3"."FechaOperacion" AS "FechaOperacionOrden" 
, TRAN."Nemotecnico" AS "Instrumento" 
, "TipoInstrumento_$7"."IdTipoInstrumento" AS "IdTipoInstrumento" 
, "TipoInstrumento_$7"."Nombre" AS "NombreTipoInstrumento" 
, "ClasificacionTipoInstrumento_$8"."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento" 
, "ClasificacionTipoInstrumento_$8"."Nombre" AS "NombreClasificacionTipoInstrumento" 
, "Orden_$3"."IdCliente" AS "IdCliente" 
, "ConsultarCliente_$5"."DocumentoIdentidad" AS "DocumentoIdentidad" 
, "ConsultarCliente_$5"."RazonSocial" AS "RazonSocial" 
, "ConsultarCliente_$5"."NombreCompleto" AS "NombreCompleto" 
, "Orden_$3"."IdPortafolioCliente" AS "IdPortafolio"
, ASIG."SecAsignacionMigracion"

, "Orden_$3"."IdEmpleadoPortafolio" 
, "Orden_$3"."IdEmpleadoDigitador"
, "Orden_$3"."IdTipoCanalDistribucion"
, "CanalDistribucion"."Nombre" AS "NombreTipoCanalDistribucion"

, "EmpleadoPortafolio"."CodigoEmpleado" AS "CodigoEmpleadoPortafolio"	
, "EmpleadoPortafolio"."NombreCompleto" AS "NombreEmpleadoPortafolio"	
, "EmpleadoPortafolio"."IdSucursal" AS "IdSucursalEmpleadoPortafolio"	
, "SucursalEmpleadoPortafolio"."NombreSucursal" AS "NombreSucursalEmpleadoPortafolio"								
, "EmpleadoDigitador"."CodigoEmpleado" AS "CodigoEmpleadoDigitador" 
, ASIG."FolioCartera" AS "FolioCartera" 	
, COALESCE("Instrumento_Negociacion"."Nemotecnico",'') AS "InstrumentoNegociacion"
, ASIG."IdAsignacionMasiva" AS "IdAsignacionMasiva"
, "TipoAsignacion"."Nombre" AS "TipoAsignacion"
, TRAN."MarcaAsignacion" AS "MarcaAsignacion"
FROM  "BSCL.Orden.BD::ORD.Asignacion" AS ASIG
LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" AS "Asignacion_PLAZO" ON  ("Asignacion_PLAZO"."IdAsignacion" = ASIG."IdAsignacionRef" AND ASIG."EsAPlazo" = 'N' )  
LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" AS "Transaccion_PLAZO" ON  ( "Asignacion_PLAZO"."IdTransaccion" = "Transaccion_PLAZO"."IdTransaccion" )  

LEFT JOIN "BSCL.Orden.BD::ORD.EstadoOrden" AS "EstadoOrden_$1" ON  (  ( "EstadoOrden_$1"."IdEstado" = ASIG."IdEstadoAsignacion" )  AND  ( "EstadoOrden_$1"."NombreEntidad" = 'ASIGNACION' )  )  
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "ESTASIGNACION" ON "ESTASIGNACION"."IdEstado" = "EstadoOrden_$1"."IdEstado"

LEFT JOIN "BSCL.Orden.BD::ORD.Orden" AS "Orden_$3" ON  ( "Orden_$3"."IdOrden" = ASIG."IdOrden" )  
LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" AS TRAN ON  ( TRAN."IdTransaccion" = ASIG."IdTransaccion" )  
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_Negociacion" ON  ( "Instrumento_Negociacion"."IdInstrumento" = "Orden_$3"."IdInstrumentoNegociacion" )		

LEFT JOIN "BSCL.Orden.BD::ORD.TipoCanalDistribucion" "CanalDistribucion" on "CanalDistribucion"."IdTipoCanalDistribucion" = "Orden_$3"."IdTipoCanalDistribucion"

--empleado portafolio
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS "EmpleadoPortafolio" ON "EmpleadoPortafolio"."IdEmpleado" = "Orden_$3"."IdEmpleadoPortafolio"

--empleado digitador
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS "EmpleadoDigitador" ON "EmpleadoDigitador"."IdEmpleado" = "Orden_$3"."IdEmpleadoDigitador"

LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSucursal" AS "SucursalEmpleadoPortafolio" ON "SucursalEmpleadoPortafolio"."IdSucursal" = "EmpleadoPortafolio"."IdSucursal"

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


LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion_$9" ON  ( "TipoOperacion_$9"."IdTipoOperacion" = TRAN."IdTipoOperacion" )  
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" AS "TipoProducto_$10" ON  ( "TipoProducto_$10"."IdTipoProducto" = "TipoOperacion_$9"."IdTipoProducto" )  
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS "CondicionDeLiquidacion_$12" ON  ( "CondicionDeLiquidacion_$12"."IdCondicionDeLiquidacion" = TRAN."IdCondicionDeLiquidacion" ) 
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_$13" ON  ( "Instrumento_$13"."IdInstrumento" = TRAN."IdInstrumento" ) 

LEFT JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" AS "LibroOrden" ON "LibroOrden"."IdLibroOrdenes" = "Orden_$3"."IdLibroOrdenes"
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" AS "TipoCanalIngreso_$11" ON  ( "TipoCanalIngreso_$11"."IdTipoCanalIngreso" = "Orden_$3"."IdTipoCanalIngreso" )

LEFT JOIN "BSCL.Orden.BD::ORD.TipoAsignacion" AS "TipoAsignacion" ON "TipoAsignacion"."IdTipoAsignacion" = ASIG."IdTipoAsignacion";

GO
GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarAsignacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO
