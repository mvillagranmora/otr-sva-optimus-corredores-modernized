SELECT   
    "IdPortafolio", 
    "CodigoPortafolio",  
    "DescripcionPortafolio",
    "Proposito", 
    "IdCliente", 
    "IdEstado",
    "Estado", 
    "EsCuentaPropia",
    "IdEmpresaCuentaPropia",
    "IdMatrizAgrupadorAPV",
    "TienePropositoAPV"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio"
WHERE "IdPortafolio" IN (:IdPortafolio);