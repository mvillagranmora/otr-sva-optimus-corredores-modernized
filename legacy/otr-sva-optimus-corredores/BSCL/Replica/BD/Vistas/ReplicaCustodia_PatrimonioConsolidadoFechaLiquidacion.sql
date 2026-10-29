SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaCustodia_PatrimonioConsolidadoFechaLiquidacion"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaCustodia_PatrimonioConsolidadoFechaLiquidacion" AS

        SELECT
        	WST."IdStack" AS "IDSTACK", 
        	"IdCliente" || ':' || "IdPortafolio" || ':' || "FechaPatrimonio" || ':' || "IdInstrumento" || ':' || "IdRegimenTributario" AS TABLE_KEY,
			"Action" AS ACTION,
			"IdCliente" AS "IDCLIENTE",
			"IdPortafolio" AS "IDPORTAFOLIO",
			"FechaPatrimonio" AS "FECHAPATRIMONIO",
			"DocumentoIdentidad" AS "DOCUMENTOIDENTIDAD",
			"NombreCompleto" AS "NOMBRECOMPLETO",
			"TipoAhorro" AS "TIPOAHORRO",
			"IdMatrizAgrupadorAPV" AS "IDMATRIZAGRUPADORAPV",
			"CodigoPortafolio" AS "CODIGOPORTAFOLIO",
			"Proposito" AS "PROPOSITO",
			"PlanAPV" AS "PLANAPV",
			"CategoriaAPV" AS "CATEGORIAAPV",
			"IdRegimenTributario" AS "IDREGIMENTRIBUTARIO",
			"Fondo" AS "FONDO",
			"Serie" AS "SERIE",
			"IdInstrumento" AS "IDINSTRUMENTO",
			"Nemotecnico" AS "NEMOTECNICO",
			"IdClasificacionTipoInstrumento" AS "IDCLASIFICACIONTIPOINSTRUMENTO",
			"CantidadPatrimonio" AS "CANTIDADPATRIMONIO",
			"PrecioMercadoCustodia" AS "PRECIOMERCADOCUSTODIA",
			"ValorCuota" AS "VALORCUOTA",
			"MontoPatrimonio" AS "MONTOPATRIMONIO",
			"IdMoneda" AS "IDMONEDA",
			"IdMonedaSistema" AS "IDMONEDASISTEMA",
			"Vigencia" AS "VIGENCIA",
			"FechaFuente" AS "FECHAFUENTE",
			"MontoComision" AS "MONTOCOMISION",
			"IdEstadoComision" AS "IDESTADOCOMISION",
			"PorcentajeCobroComision" AS "PORCENTAJECOBROCOMISION",
			"MontoPatrimonioAfectoComision" AS "MONTOPATRIMONIOAFECTOCOMISION"
        FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarPatrimonioConsolidadoFechaLiquidacionAnalitica"
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Custodia_PatrimonioConsolidadoFechaLiquidacion' AND WST."Key" = ("IdCliente" || ':' || "IdPortafolio" || ':' || "FechaPatrimonio" || ':' || "IdInstrumento" || ':' || "IdRegimenTributario")
        --and 1=2
	 	--where "FechaPatrimonio" in ('2020-05-30','2020-05-31','2020-07-06')
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaCustodia_PatrimonioConsolidadoFechaLiquidacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
