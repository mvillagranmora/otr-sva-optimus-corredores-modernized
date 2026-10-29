SET SCHEMA "BSCL";

DROP VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarEntidadDestinoGarantia";

CREATE VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarEntidadDestinoGarantia" AS 

	SELECT 		   
		 ENT."IdEntidadDestinoGarantia"
		,ENT."TipoEntidadDestinoGarantia"  -- BOLSA | CAMARA
		,ENT."IdEntidad"                   -- IdBolsa | IdCamara
		,(CASE
			WHEN (ENT."TipoEntidadDestinoGarantia" = 'BOLSA') THEN ENT."IdEntidad" ELSE ''
		  END) AS "IdBolsa"
	    ,ENT."IdPersona"
		,ENT."IdContraparte"
		,ENT."IdEstado" AS "IdEstadoEntidadDestinoGarantia"			   
		,PER."NombreCompleto" AS "NombreEntidad"			   
			   
	FROM "BSCL.Custodia.BD::GTA.EntidadDestinoGarantia" AS ENT
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON (PER."IdPersona" = ENT."IdPersona")
  

WITH READ ONLY;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::GTA.ConsultarEntidadDestinoGarantia" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
