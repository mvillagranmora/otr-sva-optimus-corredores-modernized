SELECT "IdPortafolio", "IdCliente", "IdEstado", "IdEstadoAutorizacion" 
FROM "BSCL.Portafolio.BD::POR.Portafolio" 
WHERE "IdPortafolio" = :IdPortafolio 
AND "Proposito" = 'PROPOSITOAPV';
