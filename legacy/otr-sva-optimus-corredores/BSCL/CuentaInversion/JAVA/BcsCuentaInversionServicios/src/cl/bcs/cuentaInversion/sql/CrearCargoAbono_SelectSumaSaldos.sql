SELECT  POR."NumeroCargoAbono" as "IdInterno"
	   ,FC."Saldo" - SUM(POR."Monto") over (partition by POR."IdPortafolio",POR."IdMoneda" order by POR."NumeroCargoAbono" asc 
				 	                     		 rows between unbounded preceding and current row) as "SaldoFinal"
FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_IN_CargoAbonoSumaSaldos" as FC
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_IN_CargoAbonoAGenerar" as POR 
ON POR."IdPortafolio" 					= FC."IdPortafolio" 
AND POR."IdMoneda" 						= FC."IdMoneda"
AND POR."FechaCondicionDeLiquidacion"  	= FC."FechaSaldo"
AND POR."IdTipoMovimiento"				= 'MOVCAR'