SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSUCURSAL"; 

-- select  *  from "BSCL.Replica.BD.Vistas::ReplicaSUCURSAL"
CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSUCURSAL" AS

        Select WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
        	SUC."SUC_ID"  AS TABLE_KEY,
            SUC."SUC_ID",
			SUC."FIL_ID",
			SUC."COM_ID",
			SUC."SUC_NOMBRE",
			SUC."PAR_SUC_ZONA",
			SUC."SUC_DIRECCION",
			SUC."SUC_TELEFONO",
			SUC."SUC_FAX",
			SUC."SUC_IND_HABILITADO"
			
        FROM  "BSCL.Replica.BD.Vistas::Sebra.SUCURSAL" SUC
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'SUCURSAL' AND WST."Key" = SUC."SUC_ID"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSUCURSAL" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
