WITH asignaciones AS ( 
	SELECT
		"Orden_$3"."IdEmpresa" AS "IdEmpresa"
		,ASIG."FechaAsignacion"
		,ASIG."IdOrden"
		,TRAN."Folio" AS "FolioTransaccion"
		,TRAN."FechaOperacion" AS "FechaOperacionTransaccion"
		,"ClasificacionTipoInstrumento_$8"."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
		,CASE 
			WHEN (TRAN."EsVentaCorta" = 'S' AND TRAN."IdClasificacionTipoInstrumento" = 'RV') THEN 'INTERMEDIACION VENTA CORTA RENTA VARIABLE'
			WHEN (TRAN."EsVentaCorta" = 'S' AND TRAN."IdClasificacionTipoInstrumento" = 'RF') THEN 'INTERMEDIACION VENTA CORTA RENTA FIJA'
			ELSE "TipoOperacion_$9"."Nombre"
		END AS "NombreTipoOperacion" 
		,CASE WHEN "TipoInstrumento_$7"."IdTipoInstrumento" = 'SIM' THEN "Instrumento_ASOC"."Nemotecnico" ELSE TRAN."Nemotecnico" END AS "InstrumentoUI"
		,TRAN."Precio" AS "PrecioAsignacion"
		,ASIG."MontoAsignado"
		,CAST(ROUND(ASIG."MontoAsignado" * ASIG."TipoCambio", 0) AS BIGINT) AS "MontoCLP"
		,ASIG."TipoCambio" as "TipoCambio"
		,TRAN."Tasa" AS "Tasa"
		,TRAN."IdBolsa" AS "IdBolsa"
		,TRAN."FechaLiquidacion" AS "FechaLiquidacion"
		,CASE WHEN TRAN."EsVentaCorta" = 'S' THEN  "Orden_$3"."FechaVencimiento" ELSE TRAN."FechaVencimiento" END AS "FechaVencimiento"
		,TRAN."IdEmisor" AS "IdEmisor"
		,"CondicionDeLiquidacion_$12"."Codigo" AS "CodigoCondicionDeLiquidacion"
		,"ESTASIGNACION"."Nombre" AS "EstadoAsignacion"
		,CASE "ClasificacionTipoInstrumento_$8"."IdClasificacionTipoInstrumento" WHEN 'IF' THEN ASIG."ValorRescateAsignado"  ELSE ASIG."CantidadAsignada" END AS "CantidadAsignada"
		,COALESCE(ESTADOPLAFAC."Nombre",ESTASIGNACION."Nombre") AS "NombreEstadoFacturacion"
		,TRAN."IdTipoReajuste" AS "IdTipoReajuste"
		,TRAN."IdMoneda" AS "IdMonedaOperacion"
		,"TipoInstrumento_$7"."IdTipoInstrumento" AS "IdTipoInstrumento"
		,"LibroOrden"."NombreLibroOrdenes" AS "NombreLibroOrdenes"
		,BOV."Nombre" AS "NombreBoveda"
		,BOV."Codigo" AS "CodigoBoveda"
		,CASE WHEN coalesce(CCP."IdPersonaBancoCustodio",0)=0 THEN 'N' ELSE 'S' END AS "IndicadorBancoCustodio"
		,CCP."CodigoBancoCustodio" as "CodigoBancoCustodio"
		,CCP."CodigoBancoLiquidador" as "CodigoBancoLiquidador"
		,FORMACUS."Nombre" AS "NombreFormaCustodia"
		,"EmpleadoPortafolio"."CodigoEmpleado" as "CodigoEmpleadoPortafolio"
		,"EmpleadoDigitador"."CodigoEmpleado" as "CodigoEmpleadoDigitador"
		,COALESCE("Asignacion_PLAZO"."MontoAsignado",0) as "MontoAsignadoPlazo"
		,"IDENT"."DocumentoIdentidad" AS "DocumentoIdentidad"
		,"PersonaNombreCompleto"."NombreCompleto" AS "NombreCompleto"
		,"Orden_$3"."IdPortafolioCliente" AS "IdPortafolio"
		,"ConsultarPortafolio_$0"."CodigoPortafolio"
		,"TipoCanalIngreso_$11"."Nombre" AS "NombreTipoCanalIngreso"
		,TRAN."IndicadorOrdenDirecta"
		,ASIG."EsAPlazo"
		,TRAN."EsVentaCorta"
		,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(ASIG."EsAPlazo") AS "EsAPlazoUI"
		,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(TRAN."EsVentaCorta") AS "EsVentaCortaUI"
		,"Orden_$3"."Folio" AS "FolioOrden"
		,"Orden_$3"."FechaOperacion" AS "FechaOperacionOrden"
		,ASIG."IdAsignacion"
		,ASIG."CodigoIFRS"
		,COALESCE("MedioPago"."Nombre",'SIN DEFINIR') AS "NombreMedioDePago"
		,CASE WHEN "Instrumento_$6"."ModalidadLiquidacion" = 'P' THEN 'FISICA' WHEN "Instrumento_$6"."ModalidadLiquidacion" = 'C' THEN 'FINANCIERA' ELSE '' END AS "ModalidadLiquidacion"
		,CASE WHEN "Instrumento_$6"."ModalidadEjercicio" = 'A' THEN 'AUTOMATICA' WHEN "Instrumento_$6"."ModalidadEjercicio" = 'M' THEN 'MANUAL' ELSE '' END AS "ModalidadEjercicio"
		,"Instrumento_$6"."EsPutEsCall" AS "TipoOpcion"
		,CASE WHEN "Instrumento_$6"."EstiloOpcion" = 'A' THEN 'AMERICANA' WHEN "Instrumento_$6"."EstiloOpcion" = 'E' THEN 'EUROPEA' ELSE '' END AS "EstiloOpcion"
		,"Instrumento_$6"."Tamano"
		,"Instrumento_$6"."IdMoneda" AS "IdMonedaInstrumento"
		,"Orden_$3"."IdMonedaLiquidacionOrden"
		--filtros
		,TRAN."IdInstrumentoSubyacente"
		,"TipoOperacion_$9"."AccionOrden" AS "AccionOrden"
		,CASE WHEN "TipoInstrumento_$7"."IdTipoInstrumento" = 'SIM' THEN TRAN."IdInstrumentoSubyacente" ELSE TRAN."IdInstrumento" END AS "IdInstrumentoUI"
		,TRAN."IdCondicionDeLiquidacion" AS "IdCondicionDeLiquidacion"
		,"Orden_$3"."IdLibroOrdenes" AS "IdLibroOrdenes"
		,MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") AS "IdProductoAgrupador"
		,ASIG."IdEstadoAsignacion" AS "IdEstadoAsignacion"
		,"ConsultarPortafolio_$0"."Alias" AS "RazonSocialPortafolio"
		,"Cliente_$5"."IdCliente"
		,COALESCE(TIPOLIQ."TipoLiquidacion",'') AS "TipoLiquidacion"
		,COALESCE("DetalleTipoIndicador_1"."DetalleIndicador",'') AS "ModoFacturacion"
		,"Orden_$3"."TasaVentaCorta" AS "TasaVentaCorta"
		,TRAN."CodigoOperadorCorredor" AS "CodigoOperador"
		,COALESCE(ESTADOPLAFAC."IdEstado",ESTASIGNACION."IdEstado") AS "IdEstadoFacturacion"
		,COALESCE(ASIG."IdOperacionSpot",0) AS "FolioSpot"
		,COALESCE("Instrumento_Negociacion"."Nemotecnico",'') AS "InstrumentoNegociacion"
		,COALESCE(SPOT."Estado",'') AS "EstadoSpot"
		,ASIG."IdAsignacionMasiva" AS "IdAsignacionMasiva"
		,TIPOASIG."Nombre" AS "NombreTipoAsignacion"
		,ASIG."AplicaComisionAsignacionMasiva" AS "AplicaComisionAsignacionMasiva"
		,TRAN."MarcaAsignacion" AS "MarcaAsignacion"
	FROM  "BSCL.Orden.BD::ORD.Asignacion" AS ASIG
	LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" AS TRAN ON  ( TRAN."IdTransaccion" = ASIG."IdTransaccion" )
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_$6" ON  ( "Instrumento_$6"."IdInstrumento" = TRAN."IdInstrumento" )
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumento_$7" ON  ( "TipoInstrumento_$7"."IdTipoInstrumento" = "Instrumento_$6"."IdTipoInstrumento" )
	LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS "ClasificacionTipoInstrumento_$8" ON  ( "ClasificacionTipoInstrumento_$8"."IdClasificacionTipoInstrumento" = "TipoInstrumento_$7"."IdClasificacionTipoInstrumento" )
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_ASOC" ON  ( "Instrumento_ASOC"."IdInstrumento" = TRAN."IdInstrumentoSubyacente" )	
	LEFT JOIN "BSCL.Orden.BD::ORD.Orden" AS "Orden_$3" ON  ( "Orden_$3"."IdOrden" = ASIG."IdOrden" )	
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_Negociacion" ON  ( "Instrumento_Negociacion"."IdInstrumento" = "Orden_$3"."IdInstrumentoNegociacion" )		
	LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS "CondicionDeLiquidacion_$12" ON  ( "CondicionDeLiquidacion_$12"."IdCondicionDeLiquidacion" = TRAN."IdCondicionDeLiquidacion" )
	LEFT JOIN "BSCL.Orden.BD::ORD.EstadoOrden" AS "EstadoOrden_$1" ON  (  ( "EstadoOrden_$1"."IdEstado" = ASIG."IdEstadoAsignacion" )  AND  ( "EstadoOrden_$1"."NombreEntidad" = 'ASIGNACION' )  )  
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "ESTASIGNACION" ON "ESTASIGNACION"."IdEstado" = "EstadoOrden_$1"."IdEstado"
	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" mov on asig."IdMovimientoComprobante" = mov."IdMovimientoComprobante"
	LEFT JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS ESTADOFACTURACION     ON ESTADOFACTURACION."IdEstado" = mov."IdEstadoFacturacion" AND ESTADOFACTURACION."NombreEntidad" = 'FAC.MovimientoComprobante'
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTADOPLAFAC ON ESTADOPLAFAC."IdEstado" = ESTADOFACTURACION."IdEstado"
	LEFT JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" AS "LibroOrden" ON "LibroOrden"."IdLibroOrdenes" = "Orden_$3"."IdLibroOrdenes"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" BOV ON (BOV."IdBoveda" = "Orden_$3"."IdBoveda")
	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" CCP ON (CCP."IdCuentaCustodioPortafolio" = "Orden_$3"."IdCuentaCustodioPortafolio")
	LEFT JOIN "BSCL.Orden.BD::ORD.TipoFormaCustodiaOrden" AS FORMACUS ON FORMACUS."IdTipoFormaCustodiaOrden" = "Orden_$3"."IdFormaCustodia"
	LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS "EmpleadoPortafolio" ON "EmpleadoPortafolio"."IdEmpleado" = "Orden_$3"."IdEmpleadoPortafolio"
	LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS "EmpleadoDigitador" ON "EmpleadoDigitador"."IdEmpleado" = "Orden_$3"."IdEmpleadoDigitador"
	LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" AS "Asignacion_PLAZO" ON  ("Asignacion_PLAZO"."IdAsignacion" = ASIG."IdAsignacionRef" AND ASIG."EsAPlazo" = 'N' )
	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "ConsultarPortafolio_$0" ON  ( "Orden_$3"."IdPortafolioCliente" = "ConsultarPortafolio_$0"."IdPortafolio" )
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "Cliente_$5" ON  ( "Cliente_$5"."IdCliente" = "ConsultarPortafolio_$0"."IdCliente" )
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS "Persona" ON  ( "Persona"."IdPersona" = "Cliente_$5"."IdPersona" )
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "IDENT" ON  ( "Persona"."IdDocumentoIdentidad" = "IDENT"."IdDocumentoIdentidad" )
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" AS "PersonaNombreCompleto" ON  ( "Persona"."IdPersona" = "PersonaNombreCompleto"."IdPersona" )
	LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" AS "TipoCanalIngreso_$11" ON  ( "TipoCanalIngreso_$11"."IdTipoCanalIngreso" = "Orden_$3"."IdTipoCanalIngreso" )
	LEFT JOIN "BSCL.Orden.BD::ORD.FormaDePagoOrden" AS "FormaDePagoOrden" ON "FormaDePagoOrden"."IdOrden" = ASIG."IdOrden"
	LEFT JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS "MedioPago" ON "MedioPago"."IdMedioDePago" = "FormaDePagoOrden"."IdMedioDePago"
	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = "ConsultarPortafolio_$0"."IdMatrizAgrupadorAPV"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion_$9" ON  ( "TipoOperacion_$9"."IdTipoOperacion" = TRAN."IdTipoOperacion" )
	LEFT JOIN "BSCL.Facturacion.BD::FAC.TipoLiquidacionComisionFactura" AS TIPOLIQ ON TIPOLIQ."IdTipoLiquidacionComisionFactura" = "Orden_$3"."IdTipoLiquidacionComisionFactura"
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" AS "DetalleTipoIndicador_1" ON "DetalleTipoIndicador_1"."CodigoTipoIndicador" = 'FAC_MODFAC' AND "DetalleTipoIndicador_1"."Codigo" = "Orden_$3"."CodigoModoFacturacionPortafolio" and "Orden_$3"."IdEmpresa" = "DetalleTipoIndicador_1"."IdEmpresa"
	LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" AS SPOT ON (SPOT."IdOperacionSpot" = ASIG."IdOperacionSpot")
	LEFT JOIN "BSCL.Orden.BD::ORD.TipoAsignacion" AS TIPOASIG ON TIPOASIG."IdTipoAsignacion" = ASIG."IdTipoAsignacion"
	WHERE "ClasificacionTipoInstrumento_$8"."IdClasificacionTipoInstrumento" != 'RE' AND ASIG."IdEstadoAsignacion" != 'ANU'
)
SELECT 
	asignacion."IdEmpresa",
	asignacion."FechaAsignacion",
	asignacion."IdOrden",
	asignacion."FolioTransaccion",
	asignacion."FechaOperacionTransaccion",
	asignacion."IdClasificacionTipoInstrumento",
	asignacion."NombreTipoOperacion",
	asignacion."CodigoOperador",
	asignacion."InstrumentoUI",
	asignacion."PrecioAsignacion",
	asignacion."MontoAsignado",
	asignacion."MontoCLP",
	asignacion."TipoCambio",
	asignacion."Tasa",
	asignacion."IdBolsa",
	asignacion."FechaLiquidacion",
	asignacion."FechaVencimiento",
	asignacion."IdEmisor",
	asignacion."CodigoCondicionDeLiquidacion",
	asignacion."EstadoAsignacion",
	asignacion."CantidadAsignada",
	asignacion."NombreEstadoFacturacion",
	asignacion."IdTipoReajuste",
	asignacion."IdMonedaOperacion",
	asignacion."IdTipoInstrumento",
	asignacion."NombreLibroOrdenes",
	asignacion."NombreBoveda",
	asignacion."CodigoBoveda",
	asignacion."IndicadorBancoCustodio",
	asignacion."CodigoBancoCustodio",
	asignacion."CodigoBancoLiquidador",
	asignacion."NombreFormaCustodia",
	asignacion."CodigoEmpleadoPortafolio",
	asignacion."CodigoEmpleadoDigitador",
	asignacion."MontoAsignadoPlazo",
	asignacion."DocumentoIdentidad",
	asignacion."NombreCompleto",
	asignacion."IdPortafolio",
	asignacion."CodigoPortafolio",
	asignacion."NombreTipoCanalIngreso",
	asignacion."IndicadorOrdenDirecta",
	asignacion."FolioOrden",
	asignacion."FechaOperacionOrden",
	asignacion."IdAsignacion",
	asignacion."CodigoIFRS",
	asignacion."NombreMedioDePago",
	asignacion."ModalidadLiquidacion",
	asignacion."ModalidadEjercicio",
	asignacion."TipoOpcion",
	asignacion."EstiloOpcion",
	asignacion."Tamano",
	asignacion."IdMonedaInstrumento",
	asignacion."IdMonedaLiquidacionOrden"
	--filtros
	,asignacion."IdInstrumentoSubyacente"
	,asignacion."AccionOrden"
	,asignacion."IdInstrumentoUI"
	,asignacion."IdCondicionDeLiquidacion"
	,asignacion."IdLibroOrdenes"
	,asignacion."IdProductoAgrupador"
	,asignacion."IdEstadoAsignacion"
	,asignacion."RazonSocialPortafolio"
	,asignacion."EsAPlazo"
	,asignacion."EsVentaCorta"
	,asignacion."EsAPlazoUI"
	,asignacion."EsVentaCortaUI"
	,asignacion."TipoLiquidacion"
	,asignacion."ModoFacturacion"
	,asignacion."TasaVentaCorta"
	,asignacion."IdEstadoFacturacion"
	,asignacion."FolioSpot"
	,asignacion."InstrumentoNegociacion" 
	,asignacion."EstadoSpot"
	,asignacion."IdAsignacionMasiva"
	,asignacion."NombreTipoAsignacion"
	,asignacion."AplicaComisionAsignacionMasiva"
	,asignacion."MarcaAsignacion"
FROM "BSCL.Orden.BD::GBL.ConsultarAsignaciones_Proceso_Paso" p 
INNER JOIN asignaciones asignacion  ON p."IdPortafolio" = asignacion."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS insSub  ON  insSub."IdInstrumento" = asignacion."IdInstrumentoSubyacente"
WHERE asignacion."IdEmpresa" = :p_IdEmpresa
AND (asignacion."IdCliente" = :p_IdCliente OR 1= :var_IdCliente)
AND (asignacion."IdClasificacionTipoInstrumento" = :p_IdClasificacionTipoInstrumento OR 1= :var_IdClasificacionTipoInstrumento)
AND (asignacion."IdTipoInstrumento" = :p_IdTipoInstrumento OR 1= :var_IdTipoInstrumento)
AND (asignacion."AccionOrden" = :p_AccionOrden OR 1= :var_AccionOrden)
AND (asignacion."IdInstrumentoUI" = :p_IdInstrumentoUI OR 1= :var_IdInstrumentoUI)
AND (asignacion."FechaAsignacion" BETWEEN :p_FechaAsignacionI AND :p_FechaAsignacionF)
AND (asignacion."IdCondicionDeLiquidacion" = :p_IdCondicionDeLiquidacion OR 1= :var_IdCondicionDeLiquidacion)
AND (asignacion."IdLibroOrdenes" = :p_IdLibroOrdenes OR 1= :var_IdLibroOrdenes)
AND (asignacion."IdBolsa" = :p_IdBolsa OR 1= :var_IdBolsa)
AND (asignacion."IdProductoAgrupador" = :p_IdProductoAgrupador OR 1= :var_IdProductoAgrupador)
AND (asignacion."IdEstadoAsignacion" != 'ANU' AND (asignacion."IdEstadoAsignacion" = :p_IdEstadoAsignacion OR 1= :var_IdEstadoAsignacion))
AND (asignacion."IdMonedaOperacion" = :p_IdMonedaOperacion OR 1= :varIdMonedaOperacion)
AND (asignacion."MarcaAsignacion" = :p_MarcaAsignacion OR 1 = :var_MarcaAsignacion)
AND (asignacion."CodigoOperador" = :p_CodigoOperador OR 1 = :var_CodigoOperador)
