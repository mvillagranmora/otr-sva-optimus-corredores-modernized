SELECT
"IdTipoAutorizado"
,"NombreTipoAutorizado"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarTipoAutorizado"
WHERE 
"IdEmpresa" = :IdsEmpresa AND
"IdTipoAutorizado" in (:IdsTipoAutorizado);