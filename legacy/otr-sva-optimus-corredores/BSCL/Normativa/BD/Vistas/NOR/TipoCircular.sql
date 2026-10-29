

SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.TipoCircular"; 
 		
CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.TipoCircular" AS

SELECT
	NOR."IdNormativa" as "IdTipoCircular",
 	NOR."NombreNormativa" as "NombreCircular",
 	NOR."IdEstado",
 	NOR."MetodoRelacionado",
 	NOR."EntidadDestino" 	 
FROM "BSCL.Normativa.BD::NOR.Normativa" AS NOR 
ORDER BY "IdNormativa", "EntidadDestino" ;


GRANT SELECT ON "BSCL.Normativa.BD::NOR.TipoCircular" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
 

