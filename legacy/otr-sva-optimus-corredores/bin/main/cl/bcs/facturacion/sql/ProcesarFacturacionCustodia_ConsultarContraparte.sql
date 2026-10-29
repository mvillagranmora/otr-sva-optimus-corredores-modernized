SELECT 
	ENTIDAD."IdContraparte" AS "IdContraparteGarantia",

	CONTR."NombreCompleto" AS "RazonSocialContraparteGarantia",	
	ENTIDAD."IdEntidad"
	
FROM "BSCL.Custodia.BD::GTA.EntidadDestinoGarantia" ENTIDAD 
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte" AS CONTR ON CONTR."IdContraparte" = ENTIDAD."IdContraparte";



