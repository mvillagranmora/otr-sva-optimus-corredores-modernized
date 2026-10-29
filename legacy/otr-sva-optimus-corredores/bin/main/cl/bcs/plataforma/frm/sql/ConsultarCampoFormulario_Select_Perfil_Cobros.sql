SELECT
"IdPerfilCobros", 
"PerfilCobros"
FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilCobros"
WHERE "IdPerfilCobros" in (:IdsPerfilCobros);