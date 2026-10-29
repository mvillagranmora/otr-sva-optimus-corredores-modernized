SELECT "IdCuentaCustodioPortafolio","Codigo","IdBoveda","IdPortafolio" 
FROM "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio"
WHERE "IdBoveda" IN (:IdBoveda) AND "Codigo" IN (:Codigo) AND "IdPortafolio" IN (:IdPortafolio)