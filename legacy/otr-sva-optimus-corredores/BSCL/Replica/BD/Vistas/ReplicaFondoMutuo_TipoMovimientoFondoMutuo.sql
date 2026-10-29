SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_TipoMovimientoFondoMutuo"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_TipoMovimientoFondoMutuo" AS

        SELECT WST."IdStack" AS "IDSTACK", 
        		"IdTipoMovimientoFondoMutuo"  AS TABLE_KEY,
        		"Action" AS ACTION,
               	"IdTipoMovimientoFondoMutuo" AS "IDTIPOMOVIMIENTOFONDOMUTUO",
				"Descripcion" AS "DESCRIPCION",
				"IdTipoOperacion" AS "IDTIPOOPERACION"

        from "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoMovimientoFondoMutuoAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'FondoMutuo_TipoMovimientoFondoMutuo' AND WST."Key" = "IdTipoMovimientoFondoMutuo"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_TipoMovimientoFondoMutuo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
