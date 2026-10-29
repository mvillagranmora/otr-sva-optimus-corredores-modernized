SELECT
"IdTipoCuentaCustodia", 
"NombreTipoCuentaCustodia"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarTipoCuentaCustodia"
WHERE "IdTipoCuentaCustodia" in (:IdsTipoCuentaCustodia);