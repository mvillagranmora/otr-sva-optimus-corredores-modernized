SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarNormativa";

CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarNormativa" ( 
 "IdNormativa",
 "NombreNormativa",
 "IdEstado",
 "MetodoRelacionado",
 "EntidadDestino",
 "UI_Emision",
 "PermiteSolicitar"
 ) AS SELECT
	NOR."IdNormativa",
 	NOR."NombreNormativa",
 	NOR."IdEstado",
 	NOR."MetodoRelacionado",
 	NOR."EntidadDestino",
 	NOR."UI_Emision",
 	NOR."PermiteSolicitar"
FROM "BSCL.Normativa.BD::NOR.Normativa" AS NOR 
ORDER BY "IdNormativa", "EntidadDestino" 
WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.ConsultarNormativa"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
