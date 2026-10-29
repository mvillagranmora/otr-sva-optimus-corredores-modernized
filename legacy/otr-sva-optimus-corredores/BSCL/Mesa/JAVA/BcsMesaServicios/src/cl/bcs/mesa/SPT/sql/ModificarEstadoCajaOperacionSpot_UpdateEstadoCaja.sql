UPDATE "BSCL.Mesa.BD::SPT.OperacionSpot"
 SET "EstadoCaja" = :NuevoEstado
WHERE "IdOperacionSpot" = :IdOperacion;