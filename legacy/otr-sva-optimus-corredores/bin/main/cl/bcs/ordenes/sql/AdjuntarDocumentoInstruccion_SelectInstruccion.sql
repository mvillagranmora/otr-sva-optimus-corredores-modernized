SELECT INS."IdInstruccion" 
FROM "BSCL.Orden.BD::ORD.Instruccion" INS
WHERE INS."IdInstruccion" = (:IdInstruccion);