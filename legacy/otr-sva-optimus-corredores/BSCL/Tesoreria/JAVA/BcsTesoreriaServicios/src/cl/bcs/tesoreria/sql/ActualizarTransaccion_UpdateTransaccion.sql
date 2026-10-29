UPDATE "BSCL.Orden.BD::ORD.Transaccion"
SET "IdCamara"=:IdCamara, "EsBilateral" =:EsBilateral 
WHERE "IdTransaccion"=:IdTransaccion;