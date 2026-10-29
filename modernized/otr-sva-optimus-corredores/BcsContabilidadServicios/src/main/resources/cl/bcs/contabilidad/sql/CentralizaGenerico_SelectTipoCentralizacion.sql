SELECT
"IdTipoCentralizacion"
,"Nombre"
,"TipoEnvio"
FROM 
"BSCL.Contabilidad.BD::ITZ.CentralizacionTipo"
WHERE 
"IdTipoCentralizacion" IN (:IdTipoCentralizacion);