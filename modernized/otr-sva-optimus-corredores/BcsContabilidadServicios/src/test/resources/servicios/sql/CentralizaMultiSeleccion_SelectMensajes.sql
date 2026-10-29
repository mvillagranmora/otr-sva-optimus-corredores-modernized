SELECT
"IdInterno"
,"IdSesion"
,"Codigo"
,"CodigoDato1"
,"ValorDato1"
,"CodigoDato2"
,"ValorDato2"
,"Resultado"
FROM "BSCL.Contabilidad.BD::GBL.CentralizaGenerico_OUT_TablaCodigosMensajes" 
WHERE "IdSesion" IN (:IdTipoCentralizacion)