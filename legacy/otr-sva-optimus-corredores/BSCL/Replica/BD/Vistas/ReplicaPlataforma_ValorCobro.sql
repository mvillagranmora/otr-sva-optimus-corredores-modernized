SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_ValorCobro"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_ValorCobro" AS

        SELECT WST."IdStack" AS "IDSTACK", 
        		"IdValorCobro"  AS TABLE_KEY,
        		"Action" AS ACTION,
        		"IdValorCobro" AS "IDVALORCOBRO",
				"DescripcionValorCobro" AS "DESCRIPCIONVALORCOBRO",
				"IdTipoProducto" AS "IDTIPOPRODUCTO",
				"IdCobro" AS "IDCOBRO",
				"IdMoneda" AS "IDMONEDA",
				"IdEstado" AS "IDESTADO",
				"FechaCreacion" AS "FECHACREACION",
				"ValorCobro" AS "VALORCOBRO",
				"ValorMinimo" AS "VALORMINIMO",
				"IdEmpresa" AS "IDEMPRESA"

        from "BSCL.Plataforma.BD.Vistas::COM.ConsultarValorCobroAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_ValorCobro' AND WST."Key" = "IdValorCobro"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_ValorCobro" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
