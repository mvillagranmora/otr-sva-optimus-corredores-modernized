			SELECT 	COALESCE (CAST(TO_VARCHAR(CURRENT_TIMESTAMP, 'YYYY-MM') AS VARCHAR(100)),''   )  AS COD_PER,  
					COALESCE (OPE."IdOperacion" , 0    )  AS SEC_COMPROM,
					COALESCE (CAST('' AS VARCHAR(100)),''   )  AS RUT_CORR,
					CASE WHEN OPE."IdTipoOperacion" = 'COMPAC' THEN 'C'
						WHEN OPE."IdTipoOperacion" = 'VENPAC' THEN 'V'
						WHEN OPE."IdTipoOperacion" = 'COMTER' THEN 'C'
						WHEN OPE."IdTipoOperacion" = 'VENTER' THEN 'V'
						WHEN OPE."IdTipoOperacion" = 'COMFWD' THEN 'C'
						WHEN OPE."IdTipoOperacion" = 'VENFWD' THEN 'V'
						ELSE '' END AS TIPO_COMPROM,
					COALESCE (CAST("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST(IDENT."DocumentoIdentidad" AS VARCHAR(11))) AS VARCHAR(100)),''   )  AS RUT_CLI,  
					COALESCE (CAST(POR."CodigoPortafolio" AS VARCHAR(100)), '')  AS SEC_RUT_CLI,  
					COALESCE (TO_VARCHAR(OPE."FechaLiquidacion" , 'YYYY-MM'),''   )  AS FEC_LIQUID,  
					COALESCE (TO_VARCHAR(OPE."FechaOperacion", 'YYYY-MM'),''   )  AS FEC_OPERAC,  COALESCE (0 , 0    )  AS CAN_LIN,  
					LEFT(COALESCE (CAST(MES."Codigo" AS VARCHAR(4)),''   ),4)  AS COD_MESA,  
					CASE WHEN OPE."IdEstado" = 'PEN' THEN 'I'
						WHEN OPE."IdEstado" = 'VIG' THEN 'E'
						WHEN OPE."IdEstado" = 'ANU' THEN 'A'
						WHEN OPE."IdEstado" = 'LIQ' THEN 'T'
						ELSE 'T' END AS ESTADO,
					COALESCE (TO_VARCHAR(OPE."FechaVencimiento" , 'YYYY-MM'),''   )  AS FEC_VCTO_COM, 
					COALESCE (OPE."MontoOperacionInicial" , 0 )  AS MONTO_CAPITAL,
					COALESCE (OPE."MontoOperacionInicialMoneda" , 0    )  AS VALOR_PTE,					
					COALESCE (OPE."MontoOperacionFinal" , 0   )  AS MONTO_COMPROM,
					COALESCE (OPE."MontoOperacionInicial" , 0 )  AS MONTO_INI_AJUS,
					COALESCE (OPE."PlazoEnDias" , 0    )  AS PLAZO,
					COALESCE (OPE."Tasa" , 0    )  AS TASA_COMPROM,
					replace(CAST(OPE."IdTipoReajuste" AS VARCHAR(2)), 'CL' , 'NR')  AS TIPO_REAJ_COMP,  
					COALESCE (TO_VARCHAR("FechaOperacion", 'YYYY-MM-DD HH:MI:SS') ,'1900-01-01'   )  AS HORA,  
					COALESCE (OPE."MontoOperacionInicial" , 0    )  AS CANTIDAD_CAPITAL_H,
					COALESCE (OPE."MontoOperacionFinal" , 0    )  AS CANTIDAD_COMPROM_H,
					COALESCE (OPE."Tasa" , 0 )  AS TASA_COMPROM_H,
					COALESCE (OPE."MontoOperacionInicial" , 0    )  AS MONTO_CAPITAL_H,
					COALESCE (OPE."MontoOperacionFinal" , 0    )  AS MONTO_COMPROM_H,
					CASE WHEN CL."Codigo" = 'CN' THEN '' ELSE CL."Codigo" END AS IND_LIQUID,
					COALESCE (CAST(OPE."FechaCondicionLiquidacion" AS VARCHAR(10)),'1900-01-01'   )  AS FEC_IND_LIQUID  
					FROM "BSCL.Mesa.BD::MES.Operacion" OPE 
					INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON OPE."IdCliente" = CLI."IdCliente" 
					INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona" 
					INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad" 
					INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdCliente" = CLI."IdCliente" AND por."IdPortafolio"  = OPE."IdPortafolio" 
					LEFT JOIN "BSCL.SocioNegocio.BD::EJE.Ejecutivo" EJE ON EJE."IdEmpleadoEjecutivo" = POR."IdEmpleadoEjecutivo" 
					LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON MES."IdMesa" = OPE."IdMesaOrigen" 
					LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CL ON  CL."IdCondicionDeLiquidacion" = OPE."IdCondicionLiquidacion" 