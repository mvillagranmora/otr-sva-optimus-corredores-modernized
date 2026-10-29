Select "IdPortafolio","IdInstrumento","Nemotecnico", SUM("INGCantidad") as "CustodiaDisponible" 
FROM "BSCL.Custodia.BD::CUS.Cartera" 
Where "IdPortafolio" IN (:IdPortafolio) AND "IdEstado" ='VIG' AND "IdInstrumento" IN (:IdInstrumento) Group By "IdPortafolio","IdInstrumento","Nemotecnico";