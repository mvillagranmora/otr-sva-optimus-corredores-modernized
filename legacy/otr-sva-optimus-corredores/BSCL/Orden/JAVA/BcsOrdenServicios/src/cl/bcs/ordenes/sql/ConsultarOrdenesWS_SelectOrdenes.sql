-- Se reemplazo la vista "BSCL.Orden.BD.Vistas::ConsultarOrdenesUI" por lógica en el .sql
WITH Ordenes AS  (
	SELECT
		ORD."IdOrden"
	   ,ORD."Folio" as "FolioOrden"
	   ,ORD."FechaRecepcion"
	   ,ORD."FechaIngresoRegistro"
	   ,ORD."FechaIngresoRegistroCorta"
	   ,ORD."FechaSolicitudAnulacion"
	   ,ORD."FechaAnulacion"
	   ,ORD."FechaOperacion" AS "FechaOperacionOrden"
	   ,ORD."FechaVigencia"
	   ,ORD."FechaVencimiento"
	   ,ORD."FechaLiquidacion"
	   ,ORD."MontoLiquidacion"
	   ,ORD."FechaAceptacion"
	   ,ORD."FechaAutorizacion"
	   ,ORD."IdTipoVigenciaOrden"
	   ,ORD."Glosa" as "GlosaOrden"
	   ,CASE WHEN ORD."OrdenVigente" = 'S' AND COALESCE(estAsig."IdEstadoAsignacion",'SAS') != 'SAS' THEN 
	   		CASE WHEN "ConsultarDisponibleOrden_$4"."CantidadAsignada" = ORD."Cantidad" THEN 
	   			'EJE'
	   		ELSE 
	   			'EJP'
	   		END
	    ELSE 
	    	ORD."IdEstadoOrden"
	   END AS "IdEstadoOrden"
	   ,"ESTORDEN"."Nombre" AS "EstadoOrden"
	   ,(IFNULL(ORD."Cantidad",0) - IFNULL(ORDREF."Cantidad",0))  AS "CantidadOrden"
	   ,(IFNULL(ORD."MontoOrden",0) - IFNULL(ORDREF."MontoOrden",0)) AS "MontoOrden"
	   ,CASE
	   		WHEN ORD."IdTipoInstrumento" = 'SIM' THEN ORD."PlazoSimultanea"
			WHEN ORD."EsVentaCorta" = 'S' THEN ORD."PlazoVentaCorta"
			WHEN ORD."IdClasificacionTipoInstrumento" = 'IF' THEN ORD."PlazoIF"
			WHEN ORD."IdClasificacionTipoInstrumento" = 'RE' THEN ORD."PlazoReporte"
			ELSE 0
		END AS "Plazo"
	   ,CASE
	   		WHEN ORD."IdTipoInstrumento" = 'SIM' THEN ORD."TasaSimultanea"
			WHEN ORD."EsVentaCorta" = 'S' THEN ORD."TasaVentaCorta"
			WHEN ORD."IdClasificacionTipoInstrumento" = 'RF' THEN ORD."TasaRFIF"
			WHEN ORD."IdClasificacionTipoInstrumento" = 'IF' THEN ORD."TasaRFIF"
			WHEN ORD."IdClasificacionTipoInstrumento" = 'RE' THEN ORD."TasaReporte"
			ELSE 0
		END AS "Tasa"
	   ,ORD."IdFormaCustodia"
	   ,"visi"."Nombre" as "PorcentajeVisibilidad"  
	   ,"visi"."IdPorcentajeVisibilidad"
	   ,ORD."ValorRescate"
	   ,ORD."IdTipoReajuste"
	   ,ORD."PrecioOrden"
	   ,ORD."PrecioMercado"
	   ,ORD."EsVentaCorta"
	   ,ORD."IdTipoPrecio"
	   ,ORD."IdTipoCanalDistribucion"
	   ,ORD."EspecificaBolsa"
	   ,ORD."IdTipoTasa"
	   ,ORD."IdCondicionDeLiquidacion"
	   ,ORD."IdTipoOperacion"
	   ,ORD."IdCliente"
	   ,"PersonaNombreCompleto"."NombreCompleto"
	   ,"DocumentoIdentidad"."DocumentoIdentidad"
	   ,ORD."IdPortafolioCliente" as "IdPortafolio"
	   ,"ConsultarPortafolio_$0"."CodigoPortafolio"
	   ,ORD."IdTipoCanalIngreso"
	   ,ORD."IdLibroOrdenes"
	   ,ORD."IdEmisor"
	   ,ORD."IdBolsa"
	   ,CASE
       	  WHEN ORD."IdTipoInstrumento" = 'SIM' THEN "Instrumento_ASOC"."Nemotecnico"
       	  ELSE "Instrumento_$8"."Nemotecnico"
       END AS "Instrumento"
	   ,"TipoInstrumento_$9"."IdTipoInstrumento" AS "IdTipoInstrumento"
	   ,"TipoInstrumento_$9"."Nombre" AS "NombreTipoInstrumento"
	   ,"ClasificacionTipoInstrumento_$10"."Nombre" AS "NombreClasificacionTipoInstrumento"
	   ,ORD."IdInstrumentoSubyacente" as "IdInstrumentoSubyacente"
	   ,"Instrumento_ASOC"."Nemotecnico" AS "InstrumentoSubyacente"
	   ,estAsig."IdEstadoAsignacion"
	   ,"ESTASIGNACION"."Nombre" AS "EstadoOrdenAsignacion"
	   ,ORD."IdMonedaOrden" AS "IdMonedaOrden"
	   ,ORD."IdMonedaLiquidacionOrden" AS "IdMonedaLiquidacionOrden"
	   ,"TipoOperacion_$12"."Nombre" AS "NombreTipoOperacion"
	   ,"ConsultarDisponibleOrden_$4"."CantidadAsignada" AS "CantidadAsignadaOrden"
	   ,ORD."TipoCambio"
	   ,ORD."IdEmpleadoPortafolio"
	   ,ORD."IdEmpleadoDigitador"
	   ,ORD."CodigoClaseOrden"
	   ,COALESCE(MATRIZ."IdMatrizAgrupador",0) AS "IdMatrizAgrupador"
	   ,"Instrumento_Negociacion"."Nemotecnico" AS "InstrumentoNegociacion"
	   ,ORD."GeneraSpot"
	   ,TCI."Nombre" AS "Canal"
	   ,RANK() OVER (PARTITION BY EVT."IdOrden" ORDER BY "FechaEventoBitacora" DESC, EVT."IdEventoBitacora" DESC) AS "Rank"
	
	FROM "BSCL.Orden.BD::ORD.Orden" AS ORD
	LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarEstadoAsignacionOrden" as estAsig on estAsig."IdOrden" = ORD."IdOrden"
	LEFT JOIN "BSCL.Orden.BD::ORD.PorcentajeVisibilidad" AS "visi" ON "visi"."IdPorcentajeVisibilidad" = ORD."IdPorcentajeVisibilidad"
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS "ConsultarPortafolio_$0" ON ORD."IdPortafolioCliente" = "ConsultarPortafolio_$0"."IdPortafolio"
	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMatriz" MATRIZ ON MATRIZ."IdPortafolio" = "ConsultarPortafolio_$0"."IdPortafolio"
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "ConsultarCliente_$2" ON "ConsultarCliente_$2"."IdCliente" = "ConsultarPortafolio_$0"."IdCliente"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS "Persona" ON "Persona"."IdPersona" = "ConsultarCliente_$2"."IdPersona"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DocumentoIdentidad" ON "DocumentoIdentidad"."IdDocumentoIdentidad" = "Persona"."IdDocumentoIdentidad"
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" AS "PersonaNombreCompleto" ON "PersonaNombreCompleto"."IdPersona" = "ConsultarCliente_$2"."IdPersona"
	LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarDisponibleOrden" AS "ConsultarDisponibleOrden_$4" ON "ConsultarDisponibleOrden_$4"."IdOrden" = ORD."IdOrden"
	LEFT JOIN "BSCL.Orden.BD::ORD.EstadoOrden" AS "EstadoOrden_$5"
		ON  ( "EstadoOrden_$5"."IdEstado" = CASE WHEN ORD."OrdenVigente" = 'S' AND COALESCE(estAsig."IdEstadoAsignacion",'SAS') != 'SAS' THEN 
			CASE WHEN "ConsultarDisponibleOrden_$4"."CantidadAsignada" = ORD."Cantidad" THEN  'EJE' ELSE  'EJP' END
				 ELSE ORD."IdEstadoOrden"END)  
		AND  ( "EstadoOrden_$5"."NombreEntidad" = 'ORDEN' )
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "ESTORDEN" ON "ESTORDEN"."IdEstado" = "EstadoOrden_$5"."IdEstado"
	LEFT JOIN "BSCL.Orden.BD::ORD.EstadoOrden" AS "EstadoOrden_$6" ON  ( "EstadoOrden_$6"."IdEstado" = estAsig."IdEstadoAsignacion" )  AND  ( "EstadoOrden_$6"."NombreEntidad" = 'ORDEN.ASIGNACION' )
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "ESTASIGNACION" ON "ESTASIGNACION"."IdEstado" = "EstadoOrden_$6"."IdEstado"
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_$8" ON  ( "Instrumento_$8"."IdInstrumento" = ORD."IdInstrumento" )
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumento_$9" ON  ( "TipoInstrumento_$9"."IdTipoInstrumento" = "Instrumento_$8"."IdTipoInstrumento" )
	LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS "ClasificacionTipoInstrumento_$10" ON  ( "ClasificacionTipoInstrumento_$10"."IdClasificacionTipoInstrumento" = "TipoInstrumento_$9"."IdClasificacionTipoInstrumento" )
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_ASOC" ON  "Instrumento_ASOC"."IdInstrumento" = ORD."IdInstrumentoSubyacente"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion_$12" ON  ( "TipoOperacion_$12"."IdTipoOperacion" = ORD."IdTipoOperacion" )
	LEFT JOIN "BSCL.Orden.BD::ORD.Orden" AS ORDREF ON ORDREF."IdOrdenReferencia" = ORD."IdOrden"
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_Negociacion" ON  "Instrumento_Negociacion"."IdInstrumento" = ORD."IdInstrumentoNegociacion"
	LEFT JOIN "BSCL.Orden.BD::ORD.EventoBitacora" EVT ON EVT."IdOrden" = ORD."IdOrden"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" AS TCI ON TCI."IdTipoCanalIngreso"= ORD."IdTipoCanalIngreso" 
	WHERE ORD."IdClasificacionTipoInstrumento" != 'RE'
)
SELECT 
	ORD."IdOrden",
	ORD."FolioOrden",
	ORD."FechaRecepcion",
	ORD."FechaIngresoRegistro",
	ORD."FechaIngresoRegistroCorta",
	ORD."IdCliente",
	ORD."NombreCompleto",
	ORD."IdPortafolio",
	ORD."CodigoPortafolio",
	ORD."NombreClasificacionTipoInstrumento",
	ORD."IdTipoInstrumento",
	ORD."NombreTipoInstrumento",
	ORD."Instrumento",
	ORD."DocumentoIdentidad",
	ORD."IdTipoOperacion",
	ORD."NombreTipoOperacion",
	ORD."IdEstadoOrden",
	ORD."EstadoOrden",
	ORD."IdEstadoAsignacion",
	ORD."EstadoOrdenAsignacion",
	ORD."IdEmisor",
	ORD."EspecificaBolsa",
	ORD."IdBolsa",
	ORD."CantidadOrden",
	ORD."CantidadAsignadaOrden",
	ORD."ValorRescate",
	ORD."PrecioOrden",
	ORD."Plazo",
	ORD."Tasa",
	ORD."IdTipoPrecio",
	ORD."IdTipoTasa",
	ORD."PrecioMercado",
	ORD."IdTipoReajuste",
	ORD."IdCondicionDeLiquidacion",
	ORD."FechaLiquidacion",
	ORD."IdMonedaLiquidacionOrden",
	ORD."MontoLiquidacion",
	ORD."IdMonedaOrden",
	ORD."MontoOrden",
	ORD."TipoCambio",
	ORD."FechaVencimiento",
	ORD."IdTipoVigenciaOrden",
	ORD."FechaVigencia",
	ORD."FechaOperacionOrden",
	ORD."FechaSolicitudAnulacion",
	ORD."FechaAnulacion",
	ORD."FechaAceptacion",
	ORD."FechaAutorizacion",
	ORD."GlosaOrden",
	ORD."IdTipoCanalIngreso",
	ORD."IdLibroOrdenes",
	ORD."EsVentaCorta",
	ORD."IdTipoCanalDistribucion",
	ORD."IdInstrumentoSubyacente",
	ORD."InstrumentoSubyacente",
	ORD."PorcentajeVisibilidad",
	ORD."IdPorcentajeVisibilidad",
	ORD."IdFormaCustodia",
	ORD."IdEmpleadoPortafolio",
	ORD."IdEmpleadoDigitador",
	ORD."CodigoClaseOrden",
	ORD."IdMatrizAgrupador",
	ORD."InstrumentoNegociacion",
    ORD."GeneraSpot",
    ORD."Canal"
FROM Ordenes AS ORD
WHERE ORD."Rank" = 1
	AND :p_Filtros;