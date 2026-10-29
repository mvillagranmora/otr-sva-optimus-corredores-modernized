SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::DOC.ConsultarPlantillaContratoDocumento";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::DOC.ConsultarPlantillaContratoDocumento" AS 

SELECT  "PCD"."IdPlantillaContratoDocumento" AS "IdPlantillaContratoDocumento",
		"PCD"."PlantillaContrato" AS "PlantillaContrato",
		"PCD"."Estado" AS "Estado",
		"PCD"."FechaVigencia" AS "FechaVigencia",
		"PCD"."FechaVencimiento" AS "FechaVencimiento",
		"TDO"."IdTipoDocumento" AS "IdTipoDocumento"
FROM "BSCL.Plataforma.BD::DOC.PlantillaContratoDocumento" "PCD"
JOIN "BSCL.Plataforma.BD::DOC.TipoDocumento" "TDO" ON ("PCD"."IdTipoDocumento" = "TDO"."IdTipoDocumento");

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::DOC.ConsultarPlantillaContratoDocumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

