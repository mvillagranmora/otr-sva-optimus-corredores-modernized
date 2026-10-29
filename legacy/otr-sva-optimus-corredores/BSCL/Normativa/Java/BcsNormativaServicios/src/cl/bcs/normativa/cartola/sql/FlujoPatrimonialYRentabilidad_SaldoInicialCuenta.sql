   SELECT "IdMoneda","Monto","FechaMovimiento"
   FROM  "BSCL.CuentaInversion.BD.Vistas::ConsultarMovimientosCuentaInversion" 
   WHERE "IdCliente"      = :p_IdCliente
   AND   "IdPortafolio"   = :p_Portafolio
   AND   "FechaIngresoRegistro" =:p_FechaCreacion