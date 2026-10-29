SELECT
	"ID"
   ,"Numero"
   ,"RutCliente"
   ,"IdCliente"
   ,"FechaOperacion"
   ,"AnoAhorro"
   ,"AhorrosDeposito"
   ,"AhorrosDepositoActualizado"
   ,"RetirosDeposito"
   ,"RetirosDepositoActualizado"
FROM
	"BSCL.Normativa.BD::TRI.DatosDJ1871"
WHERE
	"AnoAhorro" = :p_AnoAhorro;