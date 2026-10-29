SET SCHEMA "BSCL"; 
GO

DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarTransaccionReporteUI"; 
GO

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarTransaccionReporteUI"  
AS SELECT 
"Transaccion_$0"."IdEmpresa" 
, "Transaccion_$0"."IdTransaccion" 
, "Transaccion_$0"."Folio" as "FolioTransaccion"
, "Transaccion_$0"."FechaSistemaCreacion" 
, "Transaccion_$0"."HoraSistemaCreacion" 
, TO_DATE(TO_VARCHAR("Transaccion_$0"."FechaOperacion",'YYYY-MM-DD')) AS "FechaOperacion" 
, "Transaccion_$0"."FechaOperacion" AS "FechaOperacionTransaccion"
, "Transaccion_$0"."FechaLiquidacion" 
, "Transaccion_$0"."FechaEmision" 
, "Transaccion_$0"."FechaModificacionOperacion"  
, "Transaccion_$0"."IdEmisor" 
, "Transaccion_$0"."EsVentaCorta"
, "BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("Transaccion_$0"."EsVentaCorta") as "EsVentaCortaUI"
, "Transaccion_$0"."FechaVencimiento" 
, "Transaccion_$0"."IdSistemaBursatil" 
, "Bursatil"."NombreSistemaBursatil" 
, "IndicadorOrdenDirecta"
, "Transaccion_$0"."EsCuentaPropia" 
, "Transaccion_$0"."EsFungible" 
, "BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("Transaccion_$0"."EsFungible") AS "EsFungibleUI"
, "Transaccion_$0"."EsAPlazo" 
, "BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("Transaccion_$0"."EsAPlazo") AS "EsAPlazoUI"
, "Transaccion_$0"."CodigoOperadorContraparte" 
, "Transaccion_$0"."CodigoOperadorCorredor" 

, CASE "TipoOperacion_$8"."AccionOrden" WHEN 'COMPRA' THEN "Transaccion_$0"."CodigoOperadorCorredor" ELSE "Transaccion_$0"."CodigoOperadorContraparte" END AS "CodigoOperadorComprador"
, CASE "TipoOperacion_$8"."AccionOrden" WHEN 'VENTA' THEN "Transaccion_$0"."CodigoOperadorCorredor" ELSE "Transaccion_$0"."CodigoOperadorContraparte" END AS "CodigoOperadorVendedor"



, CASE 
	--WHEN "Transaccion_$0"."IdClasificacionTipoInstrumento" = 'IF' AND "Transaccion_$0"."EsFungible" = 'S' THEN "Transaccion_$0"."ValorRescate"
	WHEN "Transaccion_$0"."IdClasificacionTipoInstrumento" = 'IF' THEN "Transaccion_$0"."ValorRescate"
	ELSE "Transaccion_$0"."Cantidad"
	END AS "CantidadTransaccion"

, "Transaccion_$0"."Monto"	 AS "MontoTransaccion" 
, "Transaccion_$0"."Precio" as "PrecioTransaccion"
, "Transaccion_$0"."Tasa" 
, "Transaccion_$0"."Plazo"
, "Transaccion_$0"."Reservado"
, "Transaccion_$0"."Bloqueado"
, "Transaccion_$0"."IdTipoReajuste"
, "Transaccion_$0"."IdComprobanteTesoreria"
, "Transaccion_$0"."EsBilateral"
, "BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("Transaccion_$0"."EsBilateral") AS "EsBilateralUI"
, "Transaccion_$0"."IdCamara" -- RV/PM/PH/SM


--MEZCLADO
, CASE 
	WHEN "TipoInstrumento_$6"."IdClasificacionTipoInstrumento" = 'RE' THEN "Transaccion_$0"."IdInstrumentoSubyacente" 
	ELSE "Transaccion_$0"."IdInstrumento" 
	END AS "IdInstrumentoUI"
, CASE 
	WHEN "TipoInstrumento_$6"."IdClasificacionTipoInstrumento" = 'RE' THEN "Instrumento_ASOC"."Nemotecnico" 
	ELSE "Transaccion_$0"."Nemotecnico" 
	END AS "InstrumentoUI"

--SUBYACENTE
, "Transaccion_$0"."IdInstrumentoSubyacente" 
, "Instrumento_ASOC"."Nemotecnico" AS "InstrumentoSubyacente"
, "TipoInstrumento_ASOC"."IdTipoInstrumento" AS "IdTipoInstrumentoSubyacente" 
, "TipoInstrumento_ASOC"."Nombre" AS "NombreTipoInstrumentoSubyacente" 
, "ClasificacionTipoInstrumento_ASOC"."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumentoSubyacente" 
, "ClasificacionTipoInstrumento_ASOC"."Nombre" AS "NombreClasificacionTipoInstrumentoSubyacente" 
, "Transaccion_$0"."IdMonedaSubyacente" AS "IdMonedaInstrumentoSubyacente"

--REAL
, "Transaccion_$0"."IdInstrumento" 
, "Instrumento_$5"."Nemotecnico" AS "Instrumento" 
, "Instrumento_$5"."Tamano" 
, "TipoInstrumento_$6"."IdTipoInstrumento" AS "IdTipoInstrumento" 
, "TipoInstrumento_$6"."Nombre" AS "NombreTipoInstrumento" 
, "ClasificacionTipoInstrumento_$7"."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento" 
, "ClasificacionTipoInstrumento_$7"."Codigo" AS "CodigoClasificacionTipoInstrumento" 
, "ClasificacionTipoInstrumento_$7"."Nombre" AS "NombreClasificacionTipoInstrumento"
, "Transaccion_$0"."IdMoneda" AS "IdMonedaInstrumento" 
, "Transaccion_$0"."IdMoneda" AS "IdMonedaTransaccion" 
, "Transaccion_$0"."ValorRescate"  as "ValorRescateTransaccion" 
, "Transaccion_$0"."IdEstado"  as "IdEstadoTransaccion"
, "EST"."Nombre" AS "EstadoTransaccion" 
, "Transaccion_$0"."IdTransaccionPlazo"
, "Transaccion_$0"."IdTransaccionOd" 
, "Transaccion_$0"."IdEmpleado" 
, "Transaccion_$0"."IdTipoOperacion" 
, "TipoOperacion_$8"."Nombre" AS "NombreTipoOperacion" 
, "TipoOperacion_$8"."AccionOperacion" AS "AccionTipoOperacion" 
, "TipoOperacion_$8"."AccionOrden" AS "AccionOrden" 
, "TipoOperacion_$8"."IdTipoProducto" AS "IdTipoProducto" 
, "Transaccion_$0"."IdCondicionDeLiquidacion" 
, "CondicionDeLiquidacion_$2"."Codigo" AS "CodigoCondicionDeLiquidacion" 
, "CondicionDeLiquidacion_$2"."Nombre" AS "NombreCondicionDeLiquidacion" 
, "Transaccion_$0"."FechaParidad" 
, "Transaccion_$0"."ValorParidad" 
, "Transaccion_$0"."FechaTipoCambio" 
, "Transaccion_$0"."ValorTipoCambio" 
, "Transaccion_$0"."IdCorredorContraparte" 
, cor."NombreCompleto" as "NombreCorredorContraparte"
, "Transaccion_$0"."IdUsuarioReserva" 
, EMPRES."CodigoEmpleado" as "CodigoEmpleadoReserva"
, "Transaccion_$0"."FechaIngresoOferta" 
, "Transaccion_$0"."IdBolsa" 
, "Transaccion_$0"."IdLibroOrdenes"
, "LibroOrden"."NombreLibroOrdenes"
, "LibroOrden"."IdTipoLibroOrden"
, "Bolsa_$1"."Nombre" AS "NombreBolsa" 
, "Bolsa_$1"."IdPais" AS "IdPaisBolsa" 
, CASE 
	WHEN "Transaccion_$0"."IdClasificacionTipoInstrumento" = 'IF' AND "Transaccion_$0"."EsFungible" = 'S' THEN "ConsultarDisponibleTransaccion_$3"."ValorRescateDisponible" 
	ELSE "ConsultarDisponibleTransaccion_$3"."CantidadDisponible" 
	END AS "CantidadDisponibleTransaccion"
, CASE 
	WHEN "Transaccion_$0"."IdClasificacionTipoInstrumento" = 'IF' AND "Transaccion_$0"."EsFungible" = 'S' THEN "ConsultarDisponibleTransaccion_$3"."ValorRescateAsignado" 
	ELSE "ConsultarDisponibleTransaccion_$3"."CantidadAsignada" 
	END AS "CantidadAsignadaTransaccion"

, "ConsultarDisponibleTransaccion_$3"."ValorRescateDisponible"  as "ValorRescateDisponibleTransaccion"
, "ConsultarDisponibleTransaccion_$3"."ValorRescateAsignado"  as "ValorRescateAsignadoTransaccion"
, "ConsultarDisponibleTransaccion_$3"."MontoDisponible"   as "MontoDisponibleTransaccion"
, "ConsultarDisponibleTransaccion_$3"."MontoAsignado"  as "MontoAsignadoTransaccion"
, "Transaccion_$0"."IdCarga"
, "Transaccion_$0"."NemotecnicoSVS"
, "Transaccion_$0"."MarcaNegociacion"
, "Transaccion_$0"."IndicadorDCV"
, COALESCE(itr."CorteMinimo",0) AS "CorteMinimo"
 
,"Transaccion_$0"."TipoCambioCorresponsal"
,"Transaccion_$0"."IdCorredorCorresponsal"
,"Transaccion_$0"."MontoLiquidacionCorresponsal"
,"Transaccion_$0"."IdMonedaLiquidacionCorresponsal"
 
 
FROM "BSCL.Orden.BD::ORD.Transaccion" AS "Transaccion_$0"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USU on USU."TipoEntidad" = 'EMP' AND USU."IdUsuario" = "Transaccion_$0"."IdUsuarioReserva"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" EMPRES on EMPRES."IdEmpleado" = USU."IdEntidad"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" cor on cor."IdCorredora" = "Transaccion_$0"."IdCorredorContraparte"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" itr on "Transaccion_$0"."IdInstrumento" = itr."IdInstrumento" and "Transaccion_$0"."IdTipoReajuste" = itr."IdTipoReajuste"
LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" AS "Bolsa_$1" ON  ( "Bolsa_$1"."IdBolsa" = "Transaccion_$0"."IdBolsa" )
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS "CondicionDeLiquidacion_$2" ON  ( "CondicionDeLiquidacion_$2"."IdCondicionDeLiquidacion" = "Transaccion_$0"."IdCondicionDeLiquidacion" )
LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarDisponibleTransaccion" AS "ConsultarDisponibleTransaccion_$3" ON  ( "ConsultarDisponibleTransaccion_$3"."IdTransaccion" = "Transaccion_$0"."IdTransaccion" )
LEFT JOIN "BSCL.Orden.BD::ORD.EstadoOrden" AS "EstadoOrden_$4" ON  (  ( "EstadoOrden_$4"."IdEstado" = "Transaccion_$0"."IdEstado" )  AND  ( "EstadoOrden_$4"."NombreEntidad" = 'TRANSACCION' )  )
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "EST" ON ( "EST"."IdEstado" = "EstadoOrden_$4"."IdEstado")
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_$5" ON  ( "Instrumento_$5"."IdInstrumento" = "Transaccion_$0"."IdInstrumento" )
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumento_$6" ON  ( "TipoInstrumento_$6"."IdTipoInstrumento" = "Instrumento_$5"."IdTipoInstrumento" )
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS "ClasificacionTipoInstrumento_$7" ON  ( "ClasificacionTipoInstrumento_$7"."IdClasificacionTipoInstrumento" = "TipoInstrumento_$6"."IdClasificacionTipoInstrumento" )
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion_$8" ON  ( "TipoOperacion_$8"."IdTipoOperacion" = "Transaccion_$0"."IdTipoOperacion" )
LEFT JOIN "BSCL.Orden.BD::ORD.SistemaBursatil" AS "Bursatil" ON  ( "Bursatil"."IdSistemaBursatil" = "Transaccion_$0"."IdSistemaBursatil" AND "Bursatil"."IdClasificacionTipoInstrumento" = "TipoInstrumento_$6"."IdClasificacionTipoInstrumento")

--SUBYACENTE
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_ASOC" ON  ( "Instrumento_ASOC"."IdInstrumento" = "Transaccion_$0"."IdInstrumentoSubyacente" )
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumento_ASOC" ON  ( "TipoInstrumento_ASOC"."IdTipoInstrumento" = "Instrumento_ASOC"."IdTipoInstrumento" )
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS "ClasificacionTipoInstrumento_ASOC" ON  ( "ClasificacionTipoInstrumento_ASOC"."IdClasificacionTipoInstrumento" = "TipoInstrumento_ASOC"."IdClasificacionTipoInstrumento" ) 


LEFT JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" AS "LibroOrden" ON "LibroOrden"."IdLibroOrdenes" = "Transaccion_$0"."IdLibroOrdenes"
WHERE "Transaccion_$0"."EsAPlazo" = 'N' AND "Transaccion_$0"."IdClasificacionTipoInstrumento" = 'RE';
GO

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarTransaccionReporteUI" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO