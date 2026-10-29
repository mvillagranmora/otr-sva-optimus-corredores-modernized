SELECT "IdCamaraDeCompensacion", "Codigo", "Nombre", COALESCE("IdPersona",0) AS "IdPersona"
	, "IdPaisCamara", "IdEstadoCamara", "DocumentoIdentidad", "RazonSocial"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCamaraDeCompensacion"
WHERE "IdPaisCamara"   = :IdPaisCamara
	AND   "IdEstadoCamara" = 'ACT';
	
--SELECT  * FROM "BSCL.Plataforma.BD::CAM.GrupoCamaraDeCompensacion"