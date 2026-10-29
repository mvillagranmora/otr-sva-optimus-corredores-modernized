SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaCuentaInversion_PatrimonioCajaFechaLiquidacion"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaCuentaInversion_PatrimonioCajaFechaLiquidacion" AS

        Select WST."IdStack" AS "IDSTACK", 
        		"IdCliente" || ':' || "IdPortafolio" || ':' || "FechaPatrimonio"  AS TABLE_KEY,
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
				"Nemotecnico" AS "NEMOTECNICO",
				"MontoPatrimonio" AS "MONTOPATRIMONIO",
				"IdMoneda" AS "IDMONEDA",
				"IdMonedaSistema" AS "IDMONEDASISTEMA",
				"Vigencia" AS "VIGENCIA",
				"MontoPatrimonioAfectoComision" AS "MONTOPATRIMONIOAFECTOCOMISION",
				"PorcentajeCobroComision" AS "PORCENTAJECOBROCOMISION",
				"MontoComision" AS "MONTOCOMISION",
				"IdEstadoComision" AS "IDESTADOCOMISION"

        from "BSCL.CuentaInversion.BD.Vistas::ConsultarPatrimonioCajaFechaLiquidacionAnalitica"
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'CuentaInversion_PatrimonioCajaFechaLiquidacion' AND WST."Key" = ("IdCliente" || ':' || "IdPortafolio" || ':' || "FechaPatrimonio")

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaCuentaInversion_PatrimonioCajaFechaLiquidacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
