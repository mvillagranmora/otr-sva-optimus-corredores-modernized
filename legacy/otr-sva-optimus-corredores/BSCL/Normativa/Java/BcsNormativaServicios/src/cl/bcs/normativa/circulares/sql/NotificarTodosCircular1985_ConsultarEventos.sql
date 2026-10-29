SELECT 
     "IdEvento"
    ,"IdCliente"
FROM "BSCL.Normativa.BD.Vistas::CIR.UIConsultarEventosCircular1985"
WHERE TO_DATE("FechaEjecucion") >= TO_DATE(:p_FechaDesde)
  AND TO_DATE("FechaEjecucion") <= TO_DATE(:p_FechaHasta)
  AND (:p_IdCliente = 0 OR "IdCliente" = :p_IdCliente)