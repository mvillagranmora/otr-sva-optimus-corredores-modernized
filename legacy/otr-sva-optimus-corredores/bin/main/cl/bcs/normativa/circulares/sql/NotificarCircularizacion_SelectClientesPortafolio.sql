SELECT POR."IdCliente" AS "IdCliente", POR."IdPortafolio" AS "IdPortafolio"
FROM  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
WHERE POR."IdCliente"=:IdCliente AND POR."IdEstado"='ACT';