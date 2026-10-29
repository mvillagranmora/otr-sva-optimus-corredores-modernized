SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_AporteReinversionAgrupado"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_AporteReinversionAgrupado" AS

        SELECT WST."IdStack" AS "IDSTACK", 
        		TO_VARCHAR("IdSolicitudAporteRescate") AS TABLE_KEY,
        		"Action" AS ACTION,
                "IdSolicitudAporteRescate" AS "IDSOLICITUDAPORTERESCATE",
				"IdAgrupador" AS "IDAGRUPADOR",
				"IdSerieFondo" AS "IDSERIEFONDO",
				"IdMoneda" AS "IDMONEDA",
				"Monto" AS "MONTO",
				"MontoMonedaSistema" AS "MONTOMONEDASISTEMA",
				"NumeroCuotas" AS "NUMEROCUOTAS",
				"FechaCertificacion" AS "FECHACERTIFICACION",
				"FechaLiquidacion" AS "FECHALIQUIDACION",
				"FechaValorCuota" AS "FECHAVALORCUOTA",
				"FechaAceptacion" AS "FECHAACEPTACION",
				"FechaEmision" AS "FECHAEMISION",
				"ValorCuota" AS "VALORCUOTA",
				"IdRegimenTributario" AS "IDREGIMENTRIBUTARIO",
				"IdCliente" AS "IDCLIENTE",
				"IdPortafolio" AS "IDPORTAFOLIO",
				"IdTipoMovimientoFondoMutuo" AS "IDTIPOMOVIMIENTOFONDOMUTUO",
				"IdEmpresa" AS "IDEMPRESA",
				"TipoSolicitud" AS "TIPOSOLICITUD",
				"FolioContratoOperacion" AS "FOLIOCONTRATOOPERACION",
				"TipoContratoOperacion" AS "TIPOCONTRATOOPERACION",
				"Estado" AS "ESTADO",
				"DocumentoIdentidad" AS "DOCUMENTOIDENTIDAD"

        FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarAporteReinversionAgrupadoAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'FondoMutuo_AporteReinversionAgrupado' AND WST."Key" = "IdSolicitudAporteRescate"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_AporteReinversionAgrupado" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
