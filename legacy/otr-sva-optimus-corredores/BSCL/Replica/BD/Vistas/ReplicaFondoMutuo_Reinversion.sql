SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_Reinversion"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_Reinversion" AS

        SELECT WST."IdStack" AS "IDSTACK", 
        		TO_VARCHAR("IdSolicitudAporteRescate")  AS TABLE_KEY,
        		"Action" AS ACTION,
                "IdSolicitudAporteRescate" AS "IDSOLICITUDAPORTERESCATE",
				"FolioContrato" AS "FOLIOCONTRATO",
				"FechaRegistro" AS "FECHAREGISTRO",
				"FechaCurse" AS "FECHACURSE",
				"FechaCertificacion" AS "FECHACERTIFICACION",
				"FechaLiquidacion" AS "FECHALIQUIDACION",
				"Canal" AS "CANAL",
				"IdTipoMovimientoFondoMutuo" AS "IDTIPOMOVIMIENTOFONDOMUTUO",
				"TipoSolicitud" AS "TIPOSOLICITUD",
				"Estado" AS "ESTADO",
				"EstadoLiquidacion" AS "ESTADOLIQUIDACION",
				"FolioCartaLiquidacion" AS "FOLIOCARTALIQUIDACION",
				"IdEmisor" AS "IDEMISOR",
				"IdFondo" AS "IDFONDO",
				"NombreFondo" AS "NOMBREFONDO",
				"IdSerieFondo" AS "IDSERIEFONDO",
				"NombreSerieFondo" AS "NOMBRESERIEFONDO",
				"DocumentoIdentidad" AS "DOCUMENTOIDENTIDAD",
				"NombreCompleto" AS "NOMBRECOMPLETO",
				"IdPortafolio" AS "IDPORTAFOLIO",
				"IdRegimenTributario" AS "IDREGIMENTRIBUTARIO",
				"IdMonedaFondo" AS "IDMONEDAFONDO",
				"ValorCuota" AS "VALORCUOTA",
				"NumeroCuotas" AS "NUMEROCUOTAS",
				"Monto" AS "MONTO",
				"IdMoneda" AS "IDMONEDA",
				"TipoCambio" AS "TIPOCAMBIO",
				"MontoMonedaOrigen" AS "MONTOMONEDAORIGEN",
				"CostoUF" AS "COSTOUF",
				"MontoMonedaSistema" AS "MONTOMONEDASISTEMA",
				"FormaDePago" AS "FORMADEPAGO",
				"Spread" AS "SPREAD",
				"RentabilidadReal" AS "RENTABILIDADREAL",
				"IdSerieFondoDestino" AS "IDSERIEFONDODESTINO",
				"IdEmisorDestino" AS "IDEMISORDESTINO",
				"Switch" AS "SWITCH",
				"Producto" AS "PRODUCTO",
				"IdIngresoEgreso" AS "IDINGRESOEGRESO",
				"IdEstadoTesoreriaSpot" AS "IDESTADOTESORERIASPOT",
				"FechaLiquidacionTesoreria" AS "FECHALIQUIDACIONTESORERIA",
				"IdOperacionSpot" AS "IDOPERACIONSPOT",
				"IdEstadoSpot" AS "IDESTADOSPOT",
				"IdTipoOperacionSpot" AS "IDTIPOOPERACIONSPOT",
				"FolioOmnibus" AS "FOLIOOMNIBUS",
				"IdAgrupador" AS "IDAGRUPADOR",
				"IdMatrizAgrupador" AS "IDMATRIZAGRUPADOR",
				"EsADC" AS "ESADC"

        from "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarReinversionAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'FondoMutuo_Reinversion' AND WST."Key" = "IdSolicitudAporteRescate"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_Reinversion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
