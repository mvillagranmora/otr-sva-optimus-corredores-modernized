SELECT
"IdTipoBanco", 
"NombreTipoBanco"
FROM "BSCL.Plataforma.BD.Vistas::BAN.ConsultarTipoBanco"
WHERE "IdTipoBanco" in (:IdsTipoBanco);