UPDATE "BSCL.SocioNegocio.BD::PER.PaisResidenciaFiscal" 
SET "IdPais"=:IdPais
    ,"FechaInicioTributacionPais" =:FechaInicioTributacionPais    
    ,"FechaModificacion" = CURRENT_UTCTIMESTAMP
    ,"IdUsuarioModificador" =:IdUsuarioModificador
    ,"IdEstado" =:IdEstado
WHERE "IdPaisResidenciaFiscal" = :IdPaisResidenciaFiscal AND "IdEmpresa" = :IdEmpresa;