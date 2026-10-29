SELECT 
"IdControlFusion"
,"IdEstado"
FROM "BSCL.Custodia.BD::HIS.ControlFusionCartera"
WHERE "IdCliente" = :IdCliente 
AND "IdPortafolio" = :IdPortafolio
AND "FechaOperacion" = :FechaOperacion
AND "IdEstado" = 'PEN'
ORDER BY "IdControlFusion" DESC
LIMIT 1;