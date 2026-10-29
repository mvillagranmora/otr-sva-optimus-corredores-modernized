
-- Obtiene los movimientos (comprobante forma de pago) que afectaron el saldo para ontener el saldo disponible y calcular el saldo simulado
				
WITH ComprobanteAfectaSaldo AS (
	SELECT 
	      row_number () over (order by CFP."FechaAfectaSaldo") as "RowNum"
		 ,CFP."IdComprobanteFormaDePago"
		 ,CFP."IdComprobante"
		 ,CFP."IdTipoMovimiento"
		 ,CFP."NombreMedioDePago"
		 ,CFP."NumeroDocumentoCorredora"
		 ,CFP."IdMoneda"
		 ,CFP."Monto"
		 ,(CASE WHEN CFP."IdTipoMovimiento" = 'MOVING' THEN CFP."Monto" ELSE -1 * CFP."Monto" END) AS "MontoConSigno"
		 ,CFP."IdEstado"
		 ,EST."Nombre" AS "NombreEstado"	 -- ?????????????? ******* ?????????
		 ,COM."IdConcepto"
	 	 ,CON."Nombre" AS "NombreConcepto"
		 ,CAST(CFP."FechaAfectaSaldo" AS DATE) AS "FechaAfectaSaldo"
	 	 ,CFP."FechaAfectaSaldo" AS "FechaHoraAfectaSaldo"
	 	 ,COM."FechaLiquidacion"
	 	 ,COM."NombrePersona" AS "NombreSocio"
		 ,COM."DocumentoIdentidad" AS "DocumentoIdentidadSocio"
	 	 ,CFP."SaldoDisponible"
	 	 ,CFP."SaldoContable"
		 ,COM."IdPortafolioTitularPago" AS "IdPortafolio"
		 ,TPN."NombreTipoSocioDeNegocio" AS "TipoSocioNegocio"
		 ,POR."CodigoPortafolio" AS "CodigoPortafolio"
		 ,concat(concat(CBAN."NombreCuentaBancaria", ' - '), CBAN."NumeroCuentaBancaria") AS "Alias"
		 ,COM."Glosa" AS "Comentario"
	FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP
	INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM  ON (COM."IdComprobante" = CFP."IdComprobante")	
	LEFT JOIN  "BSCL.Plataforma.BD::PLA.Estado"     AS EST  ON (EST."IdEstado" = CFP."IdEstado")   -- ?????????????? ******* ?????????
	LEFT JOIN  "BSCL.Plataforma.BD::PLA.Concepto"   AS CON  ON (CON."IdConcepto" = COM."IdConcepto")
	LEFT JOIN  "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CBAN ON (CBAN."IdCuentaBanco" = CFP."IdCuentaBancariaCorredora")
	LEFT JOIN  "BSCL.Plataforma.BD::BAN.Banco"      AS BAN  ON (BAN."IdBanco" = CBAN."IdBanco")
	LEFT JOIN  "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (POR."IdPortafolio" = COM."IdPortafolioTitularPago")
	LEFT JOIN  "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio" AS TPN ON (TPN."IdTipoSocioDeNegocio" = COM."IdTipoPersonaTitularPago")
	WHERE 
	BAN."IdBanco" 		  			= :IdBancoCorredora
	AND   COM."IdEmpresa"                   = :IdEmpresa
	AND   (CFP."FechaAfectaSaldo" >= :FechaConsultaDesde AND  CFP."FechaAfectaSaldo" <= :FechaConsultaHasta)
	AND   (CFP."IdEstado" <> 'MOD' AND CFP."IdEstado" <> 'ANU')

	
)


SELECT
		 "IdComprobante"
		,"IdComprobanteFormaDePago"
		,"NombreSocio"
		,"DocumentoIdentidadSocio"
		,"IdConcepto"
		,"NombreConcepto"		
		,"NombreMedioDePago"
		,"IdMoneda"
		,(CASE WHEN "IdTipoMovimiento" = 'MOVING' THEN "Monto" ELSE 0 END) AS "MontoIngreso"
		,(CASE WHEN "IdTipoMovimiento" = 'MOVEGR' THEN "Monto" ELSE 0 END) AS "MontoEgreso"
		,"SaldoDisponible"
		,"SaldoContable"		
		,COALESCE(SUM("MontoConSigno") Over (ORDER BY "FechaHoraAfectaSaldo"  ROWS BETWEEN Unbounded Preceding AND CURRENT ROW),0) as "SaldoSimulado"
		,"FechaAfectaSaldo"
		,"FechaLiquidacion"	 	
		,"IdEstado"
		,"NombreEstado" --------------- ??????????? ----------------
		,COALESCE("NumeroDocumentoCorredora", 0) AS "NumeroDocumentoCorredora"  ----- ????
		,"TipoSocioNegocio"
		,"IdPortafolio"
		,"CodigoPortafolio"
		,"Alias"
		,"Comentario"
FROM ComprobanteAfectaSaldo
ORDER BY "Alias";	

																													  
																													  
