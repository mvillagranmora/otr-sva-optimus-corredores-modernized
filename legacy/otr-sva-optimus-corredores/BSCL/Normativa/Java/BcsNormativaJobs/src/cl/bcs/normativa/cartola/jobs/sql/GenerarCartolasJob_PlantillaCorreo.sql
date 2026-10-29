SELECT
	"Template" AS "Plantilla"
	,"Asunto"
FROM "BSCL.Plataforma.BD::EVT.Plantilla"
WHERE "Nombre"=:plantilla;