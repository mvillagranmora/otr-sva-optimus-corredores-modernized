SELECT CPPP."IdPreferenciaPagoPortafolio", 
       CPPP."IdPortafolio", 
       CPPP."IdTipoOperacion", 
       CPPP."IdMoneda" 
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" CPPP
WHERE CPPP."IdPortafolio" IN (:idsPortafolio)
AND CPPP."PorDefecto" = 'S'
AND CPPP."IdEstado" = 'ACT';
