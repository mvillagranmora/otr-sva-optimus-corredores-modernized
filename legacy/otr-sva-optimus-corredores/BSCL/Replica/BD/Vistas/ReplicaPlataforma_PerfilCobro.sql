SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_PerfilCobro"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_PerfilCobro" AS

        SELECT WST."IdStack" AS "IDSTACK", 
        		TO_VARCHAR("IdDetallePerfilCobros")  AS TABLE_KEY,
        		"Action" AS ACTION,
               	"IdPerfilCobros" AS "IDPERFILCOBROS",
				"DescripcionPerfilCobros" AS "DESCRIPCIONPERFILCOBROS",
				"IdEstado" AS "IDESTADO",
				"IdValorCobro" AS "IDVALORCOBRO",
				"IdBolsa" AS "IDBOLSA",
				"IdTipoProducto" AS "IDTIPOPRODUCTO",
				"IdTipoOperacion" AS "IDTIPOOPERACION",
				"IdCanalIngreso" AS "IDCANALINGRESO",
				"IdMoneda" AS "IDMONEDA",
				"IdEstadoPerfilCobro" AS "IDESTADOPERFILCOBRO",
				"FechaCreacion" AS "FECHACREACION",
				"FechaIngreso" AS "FECHAINGRESO",
				"FechaModificacion" AS "FECHAMODIFICACION",
				"FechaCreacionPerfilCobro" AS "FECHACREACIONPERFILCOBRO",
				"FechaModificacionPerfilCobro" AS "FECHAMODIFICACIONPERFILCOBRO",
				"FechaIngresoPerfilCobro" AS "FECHAINGRESOPERFILCOBRO",
				"IdEmpresa" AS "IDEMPRESA",
				"IdDetallePerfilCobros" AS "IDDETALLEPERFILCOBROS",
				"DiasPlazo" AS "DIASPLAZO",
				"Prioridad" AS "PRIORIDAD"
 
        from "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilCobroAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_PerfilCobro' AND WST."Key" = "IdDetallePerfilCobros"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_PerfilCobro" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
