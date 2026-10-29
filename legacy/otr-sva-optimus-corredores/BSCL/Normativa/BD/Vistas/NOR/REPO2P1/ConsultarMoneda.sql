SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO2P1.ConsultarMoneda";

CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO2P1.ConsultarMoneda" (
	"Codigo"
	,"Descripcion"
	,"Orden" 
) AS
	SELECT
	 "Codigo"
	,"Descripcion"
	,"Orden" 
	FROM  "BSCL.Normativa.BD.Vistas::DOC.ConsultarDiccionario" 
	WHERE "Tipo" = 'REPO2P1MONEDAISO' AND  "Activo"='S'
	ORDER BY "Orden" 

WITH read ONLY; 

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.REPO2P1.ConsultarMoneda" TO bcs_desarrollador WITH GRANT OPTION;