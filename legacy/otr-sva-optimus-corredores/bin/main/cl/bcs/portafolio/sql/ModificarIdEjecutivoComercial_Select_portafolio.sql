SELECT PORT."IdPortafolio" , PORT."CodigoPortafolio",PORT."IdEmpleadoEjecutivoComercial", PORT."IdCliente"   
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS PORT 
WHERE "IdPortafolio" = :idPortafolio;