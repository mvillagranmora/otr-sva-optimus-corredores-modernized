UPDATE
	"BSCL.Normativa.BD::TRI.DatosDJ1871"
SET
	"RetirosDeposito" = :p_RetirosDeposito
   ,"RetirosDepositoActualizado" = :p_RetirosDeposito
WHERE
	"ID" = :p_ID
AND "Numero" = :p_Numero
AND "IdCliente" = :p_IdCliente;