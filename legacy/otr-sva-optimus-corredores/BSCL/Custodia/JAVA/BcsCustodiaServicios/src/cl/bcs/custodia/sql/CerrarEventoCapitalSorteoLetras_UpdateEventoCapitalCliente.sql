UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
SET "IdEstado"                  =   :p_IdEstado
WHERE
    "IdEventoCapital"       = :p_IdEventoCapital     AND
    "IdEventoCapitalCliente"= :p_IdEventoCapitalCliente AND
    "IdTipoEventoCapital"   = :p_IdTipoEventoCapital AND
    "Secuencia"             = :p_Secuencia;