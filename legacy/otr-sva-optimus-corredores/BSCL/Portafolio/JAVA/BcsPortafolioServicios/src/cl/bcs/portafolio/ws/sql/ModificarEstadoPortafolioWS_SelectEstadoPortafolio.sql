SELECT 
"IdEstado"
,"NombreEntidad" 
FROM "BSCL.Portafolio.BD::POR.EstadoPortafolio"
WHERE "NombreEntidad" = 'PORTAFOLIO'
AND "IdEstado" = :IdEstado;