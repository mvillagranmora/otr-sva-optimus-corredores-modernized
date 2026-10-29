SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_PrecioInstrumento"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_PrecioInstrumento" AS
		
		SELECT 
			WST."IdStack" AS "IDSTACK","Action" AS ACTION, 
			"IdInstrumento" || "Fecha"  AS TABLE_KEY,
			"IdInstrumento" AS "IDINSTRUMENTO", --BIGINT
			"Nemotecnico"  AS "NEMOTECNICO", -- VARCHAR(40)
			"RazonSocial"  AS "RAZONSOCIAL", -- VARCHAR(100)
			"Moneda" AS "MONEDA",				
			"Precio" AS "PRECIO",	
			TO_CHAR("Fecha",'YYYY-MM-DD') AS "FECHA",	 --CHAR(10)
			"Codigo_Fuente" AS "CODIGO_FUENTE"
			
		FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarPrecioInstrumentoAnalitica"
		
		LEFT  JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_PrecioInstrumento' AND WST."Key" = "IdInstrumento" || "Fecha"
	

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_PrecioInstrumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
