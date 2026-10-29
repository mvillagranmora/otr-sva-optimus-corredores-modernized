SELECT DISTINCT car."IdCliente"
FROM "BSCL.Custodia.BD::CUS.Cartera" car WHERE car."IdEmpresa" =:p_IdEmpresa 
AND car."IdInstrumento" = :p_IdInstrumento 
AND car."IdEstado" = 'VIG'
AND car."IdCliente"=:p_IdClientePrestamista