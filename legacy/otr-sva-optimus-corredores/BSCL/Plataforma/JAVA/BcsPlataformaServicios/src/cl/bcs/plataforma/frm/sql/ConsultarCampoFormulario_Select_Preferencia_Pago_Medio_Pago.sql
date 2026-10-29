
SELECT
"IdMedioDePago"
,"Nombre"
FROM "BSCL.Plataforma.BD::PAG.MedioDePago"
WHERE "IdMedioDePago" in (:IdsMedioDePago);