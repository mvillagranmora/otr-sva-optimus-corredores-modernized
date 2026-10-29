SELECT
"IdEstado",
"IdPortafolio",
"Proposito"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 
WHERE "IdPortafolio" IN(:IdPortafolios);
