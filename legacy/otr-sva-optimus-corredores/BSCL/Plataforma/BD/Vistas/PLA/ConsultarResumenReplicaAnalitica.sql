SET SCHEMA "BSCL"; 
 
DROP VIEW bscl."BSCL.Plataforma.BD.Vistas::PLA.ConsultarResumenReplicaAnalitica"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarResumenReplicaAnalitica" AS
 Select 		  "Tipo",
                  "Nombre",
                  "Estado",
                  "Detalle",
                  TO_VARCHAR("FechaSistema",'YYYY-MM-DD') AS "FechaSistema",
                  TO_VARCHAR("UltimaActualizacion",'YYYY-MM-DD HH24:MI:SS') AS "UltimaActualizacion",
			      TO_VARCHAR("FechaReal",'YYYY-MM-DD') AS "FechaReal"

        from "BSCL.Plataforma.BD::PLA.ResumenReplica";
        

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarResumenReplicaAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
