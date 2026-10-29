SELECT "IdPerfilComercialBase", "IdPerfilCobros"
FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilComercialBase"
WHERE "IdPerfilComercialBase" = :p_IdPerfilComercialBase
  AND "IdPerfilCobros" = 'APV_EXENTO'
  AND "IdEstado" = 'ACT';