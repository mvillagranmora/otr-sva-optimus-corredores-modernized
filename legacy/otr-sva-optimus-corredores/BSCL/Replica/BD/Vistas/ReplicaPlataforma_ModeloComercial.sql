SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_ModeloComercial"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_ModeloComercial" AS

        SELECT WST."IdStack" AS "IDSTACK", 
        		"IdPerfilComercialBase"  AS TABLE_KEY,
        		"Action" AS ACTION,
        		"IdPerfilComercialBase" AS "IDPERFILCOMERCIALBASE",
				"DescripcionPerfilComercialBase" AS "DESCRIPCIONPERFILCOMERCIALBASE",
				"IdCategoriaInversionista" AS "IDCATEGORIAINVERSIONISTA",
				"IdPerfilProductos" AS "IDPERFILPRODUCTOS",
				"IdPerfilCobros" AS "IDPERFILCOBROS",
				"IdPerfilLimites" AS "IDPERFILLIMITES",
				"IdPerfilCondicionesOperativas" AS "IDPERFILCONDICIONESOPERATIVAS",
				"FechaCreacion" AS "FECHACREACION",
				"FechaModificacion" AS "FECHAMODIFICACION",
				"IdEstado" AS "IDESTADO",
				"IdEmpresa" AS "IDEMPRESA",
				"PorDefecto" AS "PORDEFECTO",
				"IdModeloFacturacion" AS "IDMODELOFACTURACION"

        from "BSCL.Plataforma.BD.Vistas::COM.ConsultarModeloComercialAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_ModeloComercial' AND WST."Key" = "IdPerfilComercialBase"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_ModeloComercial" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
