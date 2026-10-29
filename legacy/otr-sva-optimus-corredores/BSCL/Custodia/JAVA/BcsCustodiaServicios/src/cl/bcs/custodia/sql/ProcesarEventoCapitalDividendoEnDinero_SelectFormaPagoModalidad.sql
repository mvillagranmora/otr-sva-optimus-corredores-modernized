  SELECT Eccf."IdEventoCapitalClienteFormaDePago" , Ecc."IdEventoCapital" ,Ecc."IdEventoCapitalCliente"  
   FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"  AS Ecc
   INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"			AS Eccf 
   ON Ecc."IdEventoCapitalCliente" = Eccf."IdEventoCapitalCliente" 
   LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" AS Ec 
   ON Ec."IdEventoCapital"= Ecc."IdEventoCapital" 
   WHERE Ecc."IdEventoCapital" = :IdEventoCapital