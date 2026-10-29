UPDATE "BSCL.Plataforma.BD::AUT.AutorizadoEtapa"
SET 
"IdUsuario" = :IdUsuario    
,"IdRol" = :IdRol             
,"IdUsuarioReemplaza" = :IdUsuarioReemplaza
,"ReemplazaDesde" = :ReemplazaDesde    
,"ReemplazaHasta" = :ReemplazaHasta      
,"IdEstado" = :IdEstado
,"EsAutorizacionPorRol" = :EsAutorizacionPorRol
WHERE "IdAutorizadoEtapa" = :IdAutorizadoEtapa;			
