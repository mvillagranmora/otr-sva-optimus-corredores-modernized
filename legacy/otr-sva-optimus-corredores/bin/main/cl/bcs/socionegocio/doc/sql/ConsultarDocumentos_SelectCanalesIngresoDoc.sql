


SELECT Cnl."IdTipoCanalIngreso",   Cnl."IdTipoDocumento" , TCI."Nombre" 
FROM "BSCL.Plataforma.BD::DOC.CanalesDocumento" Cnl
INNER JOIN  "BSCL.Plataforma.BD::DOC.TipoDocumento" TPO ON Cnl."IdTipoDocumento" = TPO."IdTipoDocumento"
INNER JOIN 	"BSCL.Plataforma.BD::PLA.TipoCanalIngreso" TCI ON Cnl."IdTipoCanalIngreso" = TCI."IdTipoCanalIngreso" 
WHERE TPO."IdTipoDocumento" IN (:IdtipoDocumento)
ORDER BY Cnl."IdTipoDocumento";