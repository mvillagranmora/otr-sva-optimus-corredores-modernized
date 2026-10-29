UPDATE  "BSCL.Mesa.BD::MES.Operacion" 
SET     "ConfirmaAdemdum" = :ConfirmaAdemdum,
"IdUsuarioConfirma" = :IdUsuarioConfirma
WHERE "IdOperacion" IN (:IdOperacion);