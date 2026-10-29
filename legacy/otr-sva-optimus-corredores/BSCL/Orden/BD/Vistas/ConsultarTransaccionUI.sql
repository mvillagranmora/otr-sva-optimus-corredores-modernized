SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarTransaccionUI"; 

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarTransaccionUI"  
AS SELECT 
"Transaccion"."IdEmpresa" 
, "Transaccion"."IdTransaccion" 
, "Transaccion"."Folio" as "FolioTransaccion"
, "Transaccion"."FechaSistemaCreacion" 
, "Transaccion"."HoraSistemaCreacion" 
, TO_DATE(TO_VARCHAR("Transaccion"."FechaOperacion",'YYYY-MM-DD')) AS "FechaOperacion" 
, "Transaccion"."FechaOperacion" AS "FechaOperacionTransaccion"
, "Transaccion"."FechaLiquidacion" 
, "Transaccion"."FechaEmision" 
, "Transaccion"."FechaModificacionOperacion"  

, CASE 
    WHEN "Transaccion"."IdTipoInstrumento" = 'SIM' THEN "Instrumento_ASOC"."IdEmisor" 
  ELSE "Transaccion"."IdEmisor"
  END "IdEmisor"


, "Transaccion"."EsVentaCorta"
, "BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("Transaccion"."EsVentaCorta") as "EsVentaCortaUI"
, "Transaccion"."FechaVencimiento" 
, "Transaccion"."IdSistemaBursatil" 
, "Bursatil"."NombreSistemaBursatil"
, "Transaccion"."IndicadorOrdenDirecta"
, "Transaccion"."EsCuentaPropia" 
, "Transaccion"."EsFungible" 
, "BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("Transaccion"."EsFungible") AS "EsFungibleUI"
, "Transaccion"."EsAPlazo" 
, "BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("Transaccion"."EsAPlazo") AS "EsAPlazoUI"
, "Transaccion"."CodigoOperadorContraparte" 
, "Transaccion"."CodigoOperadorCorredor"
, CASE "TipoOperacion_$8"."AccionOrden" WHEN 'COMPRA' THEN "Transaccion"."CodigoOperadorCorredor" ELSE "Transaccion"."CodigoOperadorContraparte" END AS "CodigoOperadorComprador"
, CASE "TipoOperacion_$8"."AccionOrden" WHEN 'VENTA' THEN "Transaccion"."CodigoOperadorCorredor" ELSE "Transaccion"."CodigoOperadorContraparte" END AS "CodigoOperadorVendedor"

, CASE 
	WHEN "Transaccion"."IdClasificacionTipoInstrumento" = 'IF' THEN "Transaccion"."ValorRescate"
	ELSE "Transaccion"."Cantidad"
	END AS "CantidadTransaccion"

, "Transaccion"."Monto"	 AS "MontoTransaccion" 
, "Transaccion"."Precio" as "PrecioTransaccion"
, "Transaccion"."Tasa" 
, "Transaccion"."Plazo"
, "Transaccion"."Reservado"
, "Transaccion"."Bloqueado"
, "Transaccion"."IdTipoReajuste"
, "Transaccion"."IdComprobanteTesoreria"
, "Transaccion"."EsBilateral"
, "BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("Transaccion"."EsBilateral") AS "EsBilateralUI"
, "Transaccion"."IdCamara" -- RV/PM/PH/SM


--MEZCLADO
, CASE 
	WHEN "TipoInstrumento_$6"."IdTipoInstrumento" = 'SIM' THEN "Transaccion"."IdInstrumentoSubyacente" 
	WHEN "TipoInstrumento_$6"."IdClasificacionTipoInstrumento" = 'RE' THEN "Transaccion"."IdInstrumentoSubyacente" 
	ELSE "Transaccion"."IdInstrumento" 
	END AS "IdInstrumentoUI"
, CASE 
	WHEN "TipoInstrumento_$6"."IdTipoInstrumento" = 'SIM' THEN "Instrumento_ASOC"."Nemotecnico" 
	WHEN "TipoInstrumento_$6"."IdClasificacionTipoInstrumento" = 'RE' THEN "Instrumento_ASOC"."Nemotecnico" 
	WHEN "TipoInstrumento_$6"."IdClasificacionTipoInstrumento" = 'IF' THEN "Transaccion"."NemotecnicoSVS"
	ELSE "Transaccion"."Nemotecnico"
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
, "Instrumento_$5"."Nemotecnico" AS "Instrumento" 
, "Instrumento_$5"."Tamano" 
, "TipoInstrumento_$6"."IdTipoInstrumento" AS "IdTipoInstrumento" 
, "TipoInstrumento_$6"."Nombre" AS "NombreTipoInstrumento" 
, "ClasificacionTipoInstrumento_$7"."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento" 
, "ClasificacionTipoInstrumento_$7"."Codigo" AS "CodigoClasificacionTipoInstrumento" 
, "ClasificacionTipoInstrumento_$7"."Nombre" AS "NombreClasificacionTipoInstrumento"
, "Transaccion"."IdMoneda" AS "IdMonedaInstrumento" 
, "Transaccion"."IdMonedaEmision" AS "IdMonedaEmision" 
, "Transaccion"."IdMoneda" AS "IdMonedaTransaccion" 
, "Transaccion"."ValorRescate"  as "ValorRescateTransaccion" 
, "Transaccion"."IdEstado"  as "IdEstadoTransaccion"
, "EST"."Nombre" AS "EstadoTransaccion" 
, "Transaccion"."IdTransaccionPlazo"
, "Transaccion"."IdTransaccionOd" 
, "Transaccion"."IdEmpleado" 
, "Transaccion"."IdTipoOperacion"  

,CASE 
	WHEN ("Transaccion"."EsVentaCorta" = 'S' AND "Transaccion"."IdClasificacionTipoInstrumento" = 'RV') THEN 'INTERMEDIACION VENTA CORTA RENTA VARIABLE'
	WHEN ("Transaccion"."EsVentaCorta" = 'S' AND "Transaccion"."IdClasificacionTipoInstrumento" = 'RF') THEN 'INTERMEDIACION VENTA CORTA RENTA FIJA'
	ELSE "TipoOperacion_$8"."Nombre"
END AS "NombreTipoOperacion" 

, "TipoOperacion_$8"."AccionOperacion" AS "AccionTipoOperacion" 
, "TipoOperacion_$8"."AccionOrden" AS "AccionOrden" 
, "TipoOperacion_$8"."IdTipoProducto" AS "IdTipoProducto" 
, "Transaccion"."IdCondicionDeLiquidacion" 
, "CondicionDeLiquidacion_$2"."Codigo" AS "CodigoCondicionDeLiquidacion" 
, "CondicionDeLiquidacion_$2"."Nombre" AS "NombreCondicionDeLiquidacion" 
, "Transaccion"."FechaParidad" 
, "Transaccion"."ValorParidad" 
, "Transaccion"."FechaTipoCambio" 
, "Transaccion"."ValorTipoCambio" 
, "Transaccion"."IdCorredorContraparte" 
, cor."NombreCompleto" as "NombreCorredorContraparte"
, "Transaccion"."IdUsuarioReserva" 
, CASE "Transaccion"."Reservado" WHEN 'N' THEN '' ELSE USU."CodigoUsuario" END as "CodigoEmpleadoReserva"
, "Transaccion"."FechaIngresoOferta" 
, "Transaccion"."IdBolsa" 
, "Transaccion"."IdLibroOrdenes"
, "LibroOrden"."NombreLibroOrdenes"
, "LibroOrden"."IdTipoLibroOrden"
, "Bolsa_$1"."Nombre" AS "NombreBolsa" 
, "Bolsa_$1"."IdPais" AS "IdPaisBolsa" 
, CASE 
	WHEN "Transaccion"."IdClasificacionTipoInstrumento" = 'IF' THEN "ConsultarDisponibleTransaccion_$3"."ValorRescateDisponible" 
	ELSE "ConsultarDisponibleTransaccion_$3"."CantidadDisponible" 
	END AS "CantidadDisponibleTransaccion"
, CASE 
	WHEN "Transaccion"."IdClasificacionTipoInstrumento" = 'IF' THEN "ConsultarDisponibleTransaccion_$3"."ValorRescateAsignado" 
	ELSE "ConsultarDisponibleTransaccion_$3"."CantidadAsignada" 
	END AS "CantidadAsignadaTransaccion"

, "ConsultarDisponibleTransaccion_$3"."ValorRescateDisponible"  as "ValorRescateDisponibleTransaccion"
, "ConsultarDisponibleTransaccion_$3"."ValorRescateAsignado"  as "ValorRescateAsignadoTransaccion"
, "ConsultarDisponibleTransaccion_$3"."MontoDisponible"   as "MontoDisponibleTransaccion"
, "ConsultarDisponibleTransaccion_$3"."MontoAsignado"  as "MontoAsignadoTransaccion"
, "Transaccion"."IdCarga"
, "Transaccion"."NemotecnicoSVS"
, UPPER("Transaccion"."MarcaNegociacion") "MarcaNegociacion"
, "Transaccion"."IndicadorDCV"
, COALESCE(itr."CorteMinimo",0) AS "CorteMinimo"
 
,"Transaccion"."TipoCambioCorresponsal"
,"Transaccion"."IdCorredorCorresponsal"
,"Corresponsal"."CodigoCorredor" AS "CorredorCorresponsal"
,"Transaccion"."MontoLiquidacionCorresponsal"
,"Transaccion"."IdMonedaLiquidacionCorresponsal"
,"Transaccion"."GastosCorresponsal"

,"Transaccion"."Intereses"		  			-- PERU para RF
,"Transaccion"."Reajustes"  				-- PERU para RF
,"Transaccion"."BloqueoLiquidacion"			-- Permite bloquear la liquidación(Bilateral/Clearing)
,CASE ("Transaccion"."BloqueoLiquidacion")
	WHEN 'S' THEN 'SI'
	WHEN 'N' THEN 'NO'
END AS "BloqueoLiquidacionUI"
,"Transaccion"."FechaPago"  				-- Fecha liquidacion original, ya que la Fecha de Liquidacion puede ser editada en tesoreria(Gestion de Contrapartes)
,"Transaccion"."MontoRetencion"             -- PERU monto retenido por CAVALI
,"Transaccion"."MarcaIGK"                   -- PERU indica si la transacción fue confirmada por CAVALI para liquidación(a través de archivo IGK)
,CASE ("Transaccion"."MarcaIGK")
	WHEN 'S' THEN 'SI'
	WHEN 'N' THEN 'NO'
END AS "MarcaIGKUI"
--,CASE "Instrumento_$5"."IdTipoInstrumento" WHEN 'OPC' THEN "Instrumento_$5"."PrecioStrike" ELSE NULL END AS "PrecioStrike"
,"Instrumento_$5"."PrecioStrike"

--OPCIONES CL
,"Instrumento_$5"."ModalidadLiquidacion" AS "IdModalidadLiquidacion"
,CASE WHEN "Instrumento_$5"."ModalidadLiquidacion" = 'P' THEN 'FISICA' WHEN "Instrumento_$5"."ModalidadLiquidacion" = 'C' THEN 'FINANCIERA' ELSE '' END AS "ModalidadLiquidacion"
,"Instrumento_$5"."ModalidadEjercicio" AS "IdModalidadEjercicio"
,CASE WHEN "Instrumento_$5"."ModalidadEjercicio" = 'A' THEN 'AUTOMATICA' WHEN "Instrumento_$5"."ModalidadEjercicio" = 'M' THEN 'MANUAL' ELSE '' END AS "ModalidadEjercicio"
,"Instrumento_$5"."EsPutEsCall" AS "TipoOpcion"
,CASE WHEN "Instrumento_$5"."EsPutEsCall" = 'CALL' THEN 'C' WHEN "Instrumento_$5"."EsPutEsCall" = 'PUT' THEN 'P' ELSE '' END AS "IdTipoOpcion"
,"Instrumento_$5"."EstiloOpcion" AS "IdEstiloOpcion"
,CASE WHEN "Instrumento_$5"."EstiloOpcion" = 'A' THEN 'AMERICANA' WHEN "Instrumento_$5"."EstiloOpcion" = 'E' THEN 'EUROPEA' ELSE '' END AS "EstiloOpcion"
,"Instrumento_$5"."FechaVencimiento" AS "FechaVencimientoOpciones"
,"Transaccion"."IdBoveda"
,CASE WHEN IFNULL("Bolsa_$1"."EsExtranjera",'N') = 'S' THEN 'EXTERIOR' ELSE 'LOCAL' END AS "IdModeloCustodia"


,"Transaccion"."IdGrupoReserva"
, cor."NombreCorto" as "NombreCortoContraparte"
,"Instrumento_NEG"."Nemotecnico" AS "InstrumentoNegociacionTran"
,"Instrumento_NEG"."IdInstrumento" AS "IdInstrumentoNegociacion"

,"Transaccion"."MontoRaiz" AS "MontoRaiz" 
,"Transaccion"."PrecioRaiz" AS "PrecioRaiz"
,"Transaccion"."IdMonedaRaiz" AS "MonedaRaiz"
,"Transaccion"."MotivoReserva" AS "MotivoReserva"

FROM "BSCL.Orden.BD::ORD.Transaccion" AS "Transaccion"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USU on USU."TipoEntidad" = 'EMP' AND USU."IdUsuario" = "Transaccion"."IdUsuarioReserva"
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMPRES on EMPRES."IdEmpleado" = USU."IdEntidad"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" cor on cor."IdCorredora" = "Transaccion"."IdCorredorContraparte"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" itr on "Transaccion"."IdInstrumento" = itr."IdInstrumento" and "Transaccion"."IdTipoReajuste" = itr."IdTipoReajuste"
LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" AS "Bolsa_$1" ON  ( "Bolsa_$1"."IdBolsa" = "Transaccion"."IdBolsa" )
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS "CondicionDeLiquidacion_$2" ON  ( "CondicionDeLiquidacion_$2"."IdCondicionDeLiquidacion" = "Transaccion"."IdCondicionDeLiquidacion" )
LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarDisponibleTransaccion" AS "ConsultarDisponibleTransaccion_$3" ON  ( "ConsultarDisponibleTransaccion_$3"."IdTransaccion" = "Transaccion"."IdTransaccion" )
LEFT JOIN "BSCL.Orden.BD::ORD.EstadoOrden" AS "EstadoOrden_$4" ON  (  ( "EstadoOrden_$4"."IdEstado" = "Transaccion"."IdEstado" )  AND  ( "EstadoOrden_$4"."NombreEntidad" = 'TRANSACCION' )  )
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "EST" ON ( "EST"."IdEstado" = "EstadoOrden_$4"."IdEstado")
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_$5" ON  ( "Instrumento_$5"."IdInstrumento" = "Transaccion"."IdInstrumento" )
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_NEG" ON  "Instrumento_NEG"."IdInstrumento" = "Transaccion"."IdInstrumentoNegociacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumento_$6" ON  ( "TipoInstrumento_$6"."IdTipoInstrumento" = "Instrumento_$5"."IdTipoInstrumento" )
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS "ClasificacionTipoInstrumento_$7" ON  ( "ClasificacionTipoInstrumento_$7"."IdClasificacionTipoInstrumento" = "TipoInstrumento_$6"."IdClasificacionTipoInstrumento" )
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion_$8" ON  ( "TipoOperacion_$8"."IdTipoOperacion" = "Transaccion"."IdTipoOperacion" )
LEFT JOIN "BSCL.Orden.BD::ORD.SistemaBursatil" AS "Bursatil" ON "Bursatil"."IdSistemaBursatil" = "Transaccion"."IdSistemaBursatil" 
	AND ("Bursatil"."IdClasificacionTipoInstrumento" = "TipoInstrumento_$6"."IdClasificacionTipoInstrumento" OR "Bursatil"."IdClasificacionTipoInstrumento" IS NULL)
	AND ("Bursatil"."IdEmpresa" = "Transaccion"."IdEmpresa")

--SUBYACENTE
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_ASOC" ON  ( "Instrumento_ASOC"."IdInstrumento" = "Transaccion"."IdInstrumentoSubyacente" )
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumento_ASOC" ON  ( "TipoInstrumento_ASOC"."IdTipoInstrumento" = "Instrumento_ASOC"."IdTipoInstrumento" )
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS "ClasificacionTipoInstrumento_ASOC" ON  ( "ClasificacionTipoInstrumento_ASOC"."IdClasificacionTipoInstrumento" = "TipoInstrumento_ASOC"."IdClasificacionTipoInstrumento" )
LEFT JOIN "BSCL.Orden.BD::ORD.CorredorCorresponsal" AS "Corresponsal" ON ("Corresponsal"."IdCorredorCorresponsal" = "Transaccion"."IdCorredorCorresponsal")
LEFT JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" AS "LibroOrden" ON "LibroOrden"."IdLibroOrdenes" = "Transaccion"."IdLibroOrdenes"
WHERE "Transaccion"."EsAPlazo" = 'N' AND "Transaccion"."IdClasificacionTipoInstrumento" != 'RE'
AND "Transaccion"."IdEstado" <> 'EPR';

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarTransaccionUI" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
