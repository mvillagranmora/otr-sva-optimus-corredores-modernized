
	-- Obtiene información de las cuentas bancarias de la corredora de acuerdo a los parámetros de entrada. 

	 SELECT  
	 		 CB."IdCuentaBanco"
			,CB."IdBanco"
			,BAN."NombreBanco"
			,CB."NumeroCuentaBancaria"
			,CB."NombreCuentaBancaria" AS "NombreCuentaBanco"
			,CB."IdMoneda"
			,MON."Nombre" AS "NombreMoneda"
			
	FROM "BSCL.Tesoreria.BD::GBL.ConsultarSaldoCuentaBanco_CuentasAConsultar" as DC
	LEFT JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" as CB 
	 		 ON CB."IdCuentaBanco" 	= COALESCE(DC."IdCuentaBanco",CB."IdCuentaBanco")
	 		AND CB."IdBanco"  		= COALESCE(DC."IdBanco",CB."IdBanco")
	 		AND CB."IdMoneda"		= COALESCE(DC."IdMoneda",CB."IdMoneda")		 		 
	 		 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON (MON."IdMoneda" = CB."IdMoneda")
	LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco"  AS BAN ON (BAN."IdBanco"  = CB."IdBanco")

	WHERE CB."IdEmpresa" = :IdEmpresa;
		 