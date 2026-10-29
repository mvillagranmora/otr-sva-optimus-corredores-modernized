SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_PerfilProducto"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_PerfilProducto" AS

        SELECT WST."IdStack" AS "IDSTACK", 
        		"IdDetallePerfilProductos"  AS TABLE_KEY,
        		"Action" AS ACTION,
				"IdPerfilProductos" AS "IDPERFILPRODUCTOS",
				"DescripcionPerfilProductos" AS "DESCRIPCIONPERFILPRODUCTOS",
				"IdCategoriaInversionista" AS "IDCATEGORIAINVERSIONISTA",
				"IdEstado" AS "IDESTADO",
				"IdTipoProducto" AS "IDTIPOPRODUCTO",
				"IdTipoOperacion" AS "IDTIPOOPERACION",
				"IdCanalIngreso" AS "IDCANALINGRESO",
				"IdMoneda" AS "IDMONEDA",
				"FechaIngreso" AS "FECHAINGRESO",
				"FechaCreacion" AS "FECHACREACION",
				"FechaModificacion" AS "FECHAMODIFICACION",
				"IdDetallePerfilProductos" AS "IDDETALLEPERFILPRODUCTOS",
				"IdEmpresa" AS "IDEMPRESA"

        from "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilProductoAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_PerfilProducto' AND WST."Key" = "IdDetallePerfilProductos"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_PerfilProducto" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
