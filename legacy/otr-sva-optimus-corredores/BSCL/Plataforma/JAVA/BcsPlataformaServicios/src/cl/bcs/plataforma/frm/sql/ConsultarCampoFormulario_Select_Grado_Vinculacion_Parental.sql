SELECT
	TO_NVARCHAR("IdGradoVinculacionParental") AS "IdGradoVinculacionParental"
	,"Nombre"
FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarGradoVinculacionParental"
WHERE TO_NVARCHAR("IdGradoVinculacionParental") in (:IdsGradoVinculacionParental);