SELECT "ListaCorreos","TipoProceso" FROM "BSCL.Plataforma.BD::EVT.ListaCorreo"
WHERE "TipoProceso"=:TipoProceso AND "NombreLista"=:NombreLista;