UPDATE "BSCL.Custodia.BD::CUS.EventoCapital"
SET "IdEstado"                  =   :p_IdEstado
WHERE
    "IdEmpresa"             = :p_IdEmpresa           AND
    "IdEventoCapital"       = :p_IdEventoCapital     AND
    "IdTipoEventoCapital"   = :p_IdTipoEventoCapital AND
    "Secuencia"             = :p_Secuencia;