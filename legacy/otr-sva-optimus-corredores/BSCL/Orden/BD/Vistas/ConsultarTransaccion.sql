SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarTransaccion"; 
GO
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarTransaccion"  
AS SELECT "Transaccion"."IdEmpresa" 
, "Transaccion"."IdTransaccion" 
, "Transaccion"."Folio" as "FolioTransaccion"
, "Transaccion"."FechaSistemaCreacion" 
, "Transaccion"."HoraSistemaCreacion" 
, TO_DATE(TO_VARCHAR("Transaccion"."FechaOperacion",'YYYY-MM-DD')) AS "FechaOperacion" 
, "Transaccion"."FechaOperacion" AS "FechaOperacionTransaccion"
, "Transaccion"."FechaLiquidacion" 
, "Transaccion"."FechaEmision" 
, "Transaccion"."FechaModificacionOperacion"  
, "Transaccion"."IdEmisor" 
, "Transaccion"."EsVentaCorta"
, "BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("Transaccion"."EsVentaCorta") as "EsVentaCortaUI"
, "Transaccion"."FechaVencimiento" 
, "Transaccion"."IdSistemaBursatil" 
, "Transaccion"."IndicadorOrdenDirecta" 
, "Transaccion"."EsCuentaPropia" 
, "Transaccion"."EsFungible" 
, "BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("Transaccion"."EsFungible") AS "EsFungibleUI"
, "Transaccion"."EsAPlazo" 
, "BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("Transaccion"."EsAPlazo") AS "EsAPlazoUI"
, "Transaccion"."CodigoOperadorContraparte" 
, "Transaccion"."CodigoOperadorCorredor" 
, "Transaccion"."Cantidad" AS "CantidadTransaccion" 
, "Transaccion"."Monto"	 AS "MontoTransaccion" 
, "Transaccion"."Precio" as "PrecioTransaccion"
, "Transaccion"."Tasa" 
, "Transaccion"."Plazo"
, "Transaccion"."Reservado"
, "Transaccion"."Bloqueado"
, "Transaccion"."IdTipoReajuste"
, "Transaccion"."IdComprobanteTesoreria"
, "Transaccion"."EsBilateral"
, "Transaccion"."IdCamara" -- RV/PM/PH/SM


--MEZCLADO
, CASE 
	WHEN "TipoInstrumento"."IdTipoInstrumento" = 'SIM' THEN "Transaccion"."IdInstrumentoSubyacente" 
	WHEN "TipoInstrumento"."IdClasificacionTipoInstrumento" = 'RE' THEN "Transaccion"."IdInstrumentoSubyacente" 
	ELSE "Transaccion"."IdInstrumento" 
	END AS "IdInstrumentoUI"
, CASE 
	WHEN "TipoInstrumento"."IdTipoInstrumento" = 'SIM' THEN "Instrumento_ASOC"."Nemotecnico"
	WHEN "TipoInstrumento"."IdClasificacionTipoInstrumento" = 'RE' THEN "Instrumento_ASOC"."Nemotecnico"
	ELSE "Instrumento"."Nemotecnico" 
	END AS "InstrumentoUI"

--SUBYACENTE
, "Transaccion"."IdInstrumentoSubyacente" 
, "Instrumento_ASOC"."Nemotecnico" AS "InstrumentoSubyacente"
, "TipoInstrumento_ASOC"."IdTipoInstrumento" AS "IdTipoInstrumentoSubyacente" 
, "TipoInstrumento_ASOC"."Nombre" AS "NombreTipoInstrumentoSubyacente" 
, "ClasificacionTipoInstrumento_ASOC"."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumentoSubyacente" 
, "ClasificacionTipoInstrumento_ASOC"."Nombre" AS "NombreClasificacionTipoInstrumentoSubyacente" 
, "Transaccion"."IdMonedaSubyacente" AS "IdMonedaInstrumentoSubyacente"

--REAL
, "Transaccion"."IdInstrumento" 
, "Instrumento"."Nemotecnico" AS "Instrumento" 
, "Instrumento"."Tamano" 
, "TipoInstrumento"."IdTipoInstrumento" AS "IdTipoInstrumento" 
, "TipoInstrumento"."Nombre" AS "NombreTipoInstrumento" 
, "ClasificacionTipoInstrumento_$7"."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento" 
, "ClasificacionTipoInstrumento_$7"."Codigo" AS "CodigoClasificacionTipoInstrumento" 
, "ClasificacionTipoInstrumento_$7"."Nombre" AS "NombreClasificacionTipoInstrumento"
, "Transaccion"."IdMoneda" AS "IdMonedaInstrumento"  --DESCONTINUAR, MONEDA TRANSACCION ES MAS CLARO
, "Transaccion"."IdMoneda" AS "IdMonedaTransaccion" 
, "Transaccion"."ValorRescate"  as "ValorRescateTransaccion" 
, "Transaccion"."IdEstado"  as "IdEstadoTransaccion"
, "EST"."Nombre" AS "EstadoTransaccion" 
, "Transaccion"."IdTransaccionPlazo"
, "Transaccion"."IdTransaccionOd" 
, "Transaccion"."IdEmpleado" 
, "Transaccion"."IdTipoOperacion" 
, "TipoOperacion"."Nombre" AS "NombreTipoOperacion" 
, "TipoOperacion"."AccionOperacion" AS "AccionTipoOperacion" 
, "TipoOperacion"."AccionOperacion" AS "AccionOrden" 
, "TipoOperacion"."IdTipoProducto" AS "IdTipoProducto" 
, "Transaccion"."IdCondicionDeLiquidacion" 
, "CondicionDeLiquidacion"."Codigo" AS "CodigoCondicionDeLiquidacion" 
, "CondicionDeLiquidacion"."Nombre" AS "NombreCondicionDeLiquidacion" 
, "Transaccion"."FechaParidad" 
, "Transaccion"."ValorParidad" 
, "Transaccion"."FechaTipoCambio" 
, "Transaccion"."ValorTipoCambio"
, "Transaccion"."IdCorredorLocal"
, "Transaccion"."IdCorredorContraparte" 
, cor."NombreCompleto" as "NombreCorredorContraparte"
, "Transaccion"."IdUsuarioReserva" 
, "Transaccion"."FechaIngresoOferta" 
, "Transaccion"."IdBolsa" 
, "Transaccion"."IdLibroOrdenes"
, "LibroOrden"."NombreLibroOrdenes"
, "LibroOrden"."IdTipoLibroOrden"
, "Bolsa"."Nombre" AS "NombreBolsa" 
, "Bolsa"."IdPais" AS "IdPaisBolsa" 
, "ConsultarDisponibleTransaccion"."CantidadDisponible"  as "CantidadDisponibleTransaccion"
, "ConsultarDisponibleTransaccion"."CantidadAsignada"  as "CantidadAsignadaTransaccion"
, "ConsultarDisponibleTransaccion"."ValorRescateDisponible"  as "ValorRescateDisponibleTransaccion"
, "ConsultarDisponibleTransaccion"."ValorRescateAsignado"  as "ValorRescateAsignadoTransaccion"
, "ConsultarDisponibleTransaccion"."MontoDisponible"   as "MontoDisponibleTransaccion"
, "ConsultarDisponibleTransaccion"."MontoAsignado"  as "MontoAsignadoTransaccion"
, "Transaccion"."IdCarga"
, "Transaccion"."NemotecnicoSVS"
, "Transaccion"."MarcaNegociacion"
, "Transaccion"."IndicadorDCV"
, COALESCE(itr."CorteMinimo",0) AS "CorteMinimo"
 
,"Transaccion"."TipoCambioCorresponsal"
,"Transaccion"."IdCorredorCorresponsal"
,"Transaccion"."MontoLiquidacionCorresponsal"
,"Transaccion"."IdMonedaLiquidacionCorresponsal"

,"Transaccion"."Intereses"		  			-- PERU para RF
,"Transaccion"."Reajustes"  				-- PERU para RF
,"Transaccion"."BloqueoLiquidacion"			-- Permite bloquear la liquidación(Bilateral/Clearing)
,"Transaccion"."FechaPago"                  -- Fecha liquidacion original, ya que la Fecha de Liquidacion puede ser editada en tesoreria(Gestion de Contrapartes)
,"Transaccion"."MontoRetencion"             -- PERU monto retenido por cavali
,"Transaccion"."MarcaIGK"                   -- PERU indica si transaccion fue confirmada por cavali a través de archivo IGK
,"Instrumento_NEG"."Nemotecnico" as "InstrumentoNegociacion"
FROM "BSCL.Orden.BD::ORD.Transaccion" AS "Transaccion"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" cor on cor."IdCorredora" = "Transaccion"."IdCorredorContraparte"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" itr on "Transaccion"."IdInstrumento" = itr."IdInstrumento" and "Transaccion"."IdTipoReajuste" = itr."IdTipoReajuste"
LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" AS "Bolsa" ON  ( "Bolsa"."IdBolsa" = "Transaccion"."IdBolsa" )
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS "CondicionDeLiquidacion" ON  ( "CondicionDeLiquidacion"."IdCondicionDeLiquidacion" = "Transaccion"."IdCondicionDeLiquidacion" )
LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarDisponibleTransaccion" AS "ConsultarDisponibleTransaccion" ON  ( "ConsultarDisponibleTransaccion"."IdTransaccion" = "Transaccion"."IdTransaccion" )
LEFT JOIN "BSCL.Orden.BD::ORD.EstadoOrden" AS "EstadoOrden" ON  (  ( "EstadoOrden"."IdEstado" = "Transaccion"."IdEstado" )  AND  ( "EstadoOrden"."NombreEntidad" = 'TRANSACCION' )  )
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "EST" ON ( "EST"."IdEstado" = "EstadoOrden"."IdEstado")
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento" ON  ( "Instrumento"."IdInstrumento" = "Transaccion"."IdInstrumento" )
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumento" ON  ( "TipoInstrumento"."IdTipoInstrumento" = "Instrumento"."IdTipoInstrumento" )
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS "ClasificacionTipoInstrumento_$7" ON  ( "ClasificacionTipoInstrumento_$7"."IdClasificacionTipoInstrumento" = "TipoInstrumento"."IdClasificacionTipoInstrumento" )
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion" ON  ( "TipoOperacion"."IdTipoOperacion" = "Transaccion"."IdTipoOperacion" )

--SUBYACENTE
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_ASOC" ON  ( "Instrumento_ASOC"."IdInstrumento" = "Transaccion"."IdInstrumentoSubyacente" )
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumento_ASOC" ON  ( "TipoInstrumento_ASOC"."IdTipoInstrumento" = "Instrumento_ASOC"."IdTipoInstrumento" )
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS "ClasificacionTipoInstrumento_ASOC" ON  ( "ClasificacionTipoInstrumento_ASOC"."IdClasificacionTipoInstrumento" = "TipoInstrumento_ASOC"."IdClasificacionTipoInstrumento" ) 
--NEGOCIADO
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_NEG" ON  ( "Instrumento_NEG"."IdInstrumento" = "Transaccion"."IdInstrumentoNegociacion" )

LEFT JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" AS "LibroOrden" ON "LibroOrden"."IdLibroOrdenes" = "Transaccion"."IdLibroOrdenes";
GO
GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarTransaccion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO