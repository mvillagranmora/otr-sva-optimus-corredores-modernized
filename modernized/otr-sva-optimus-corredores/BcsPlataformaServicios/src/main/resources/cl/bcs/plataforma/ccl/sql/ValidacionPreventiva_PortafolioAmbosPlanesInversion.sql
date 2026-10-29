SELECT DISTINCT con. "DocumentoIdentidad" AS "Valor1",
				po120."IdCliente", 
				po120."IdMatrizAgrupadorAPV" AS "Valor5",
				po120."IdPortafolio" AS "Valor6",
				po70."IdMatrizAgrupadorAPV" AS "Valor7",
				po70."IdPortafolio" AS "Valor8"
 FROM "BSCL.Portafolio.BD::POR.Portafolio" po120 
 INNER JOIN   (SELECT "IdCliente", "IdMatrizAgrupadorAPV" ,"IdPortafolio" FROM "BSCL.Portafolio.BD::POR.Portafolio" 
                      WHERE  "IdMatrizAgrupadorAPV" IN (7,8,9,10,11,12,20) 
                      )      po70
        ON po70."IdCliente" = po120."IdCliente"
 INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" con 
        ON con."IdCliente" = po70."IdCliente"
 WHERE  po120."IdMatrizAgrupadorAPV" IN (1,2,3,4,5,6,19)
 ORDER BY 1,2
