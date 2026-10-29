SELECT "POR_IdPortafolio" as "IdInterno"
							,CASE 
								WHEN "REQ_IdCondicionOperativa" = 'CONTCONDGRAPACTO' and ("PAR_IdTipoOperacion" = 'COMTER' or "PAR_IdTipoOperacion"= 'VENTER'  or "PAR_IdTipoOperacion"='') THEN CAST(1 AS BIGINT)
								WHEN "REQ_IdCondicionOperativa" = 'CONTCONDGRAPACTO' and ("PAR_IdTipoOperacion" = 'COMPAC' or "PAR_IdTipoOperacion"= 'VENPAC') and "IND_ValorIndicador" = 'S' THEN CAST(1 AS BIGINT)
								WHEN "REQ_IdCondicionOperativa" = 'PERSONABLOQUEADA' and "CLI_IdEstadoPersona" = 'ACT' THEN CAST(1 AS BIGINT)
								WHEN "REQ_IdCondicionOperativa" = 'CLIENTEBLOQUEADO' and "CLI_IdEstado" = 'ACT' THEN CAST(1 AS BIGINT)
								WHEN "REQ_IdCondicionOperativa" = 'PORTBLOQ' and "POR_IdEstado" = 'ACT' THEN CAST(1 AS BIGINT)
								WHEN "REQ_IdCondicionOperativa" = 'SALDOFLUJOCAJA' and ("PRE_OperaCuentaInversion"='N' or "PAR_IdTipoOperacion"= 'COMTER' or "PAR_IdTipoOperacion"= 'COMPAC'  or "PAR_IdTipoOperacion"='') THEN CAST(1 AS BIGINT)
								WHEN "REQ_IdCondicionOperativa" = 'SALDOFLUJOCAJA' and "PRE_OperaCuentaInversion"='S' and ("PAR_MontoOperacion" <= "FCA_Saldo" or ("PAR_MontoOperacion" >= "FCA_Saldo" and "PRE_SobregiroCuentaInversion"='S')) and ("PAR_IdTipoOperacion"= 'VENTER' or "PAR_IdTipoOperacion"= 'VENPAC') THEN CAST(1 AS BIGINT)
								WHEN "REQ_IdCondicionOperativa" = 'FECHAINVALIDA' and "PAR_FechaOperacion" = :FechaSistema THEN CAST(1 AS BIGINT)
								WHEN "REQ_IdCondicionOperativa" = 'PRODUCTOINVALIDO' AND "PRO_IdTipoProducto" = 'MERF' 	AND ("PRO_IdTipoOperacion" = "PAR_IdTipoOperacion" OR "PRO_IdTipoOperacion" = 0 ) THEN CAST(1 AS BIGINT)
								ELSE 
									case 
										when "REQ_IdTipoComportamiento" = 'RECHAZO' then CAST(4 AS BIGINT)
										when "REQ_IdTipoComportamiento" = 'AUTORIZACION' then CAST(3 AS BIGINT)
										when "REQ_IdTipoComportamiento" = 'ADVERTENCIA' then CAST(2 AS BIGINT)
										else CAST(4 AS BIGINT)
									end
								END 								AS "IdResultado"
							,CASE 
								WHEN "REQ_IdCondicionOperativa" = 'CONTCONDGRAPACTO' 	THEN 'CLIENTE_SIN_CONTRATO_COND_GRAL_PACTOS'
								WHEN "REQ_IdCondicionOperativa" = 'PERSONABLOQUEADA'	THEN 'PERSONA_NO_ACTIVA'
								WHEN "REQ_IdCondicionOperativa" = 'CLIENTEBLOQUEADO' 	THEN 'CLIENTE_BLOQUEADO'
								WHEN "REQ_IdCondicionOperativa" = 'PORTBLOQ' 			THEN 'PORTAFOLIO_BLOQUEADO'
								WHEN "REQ_IdCondicionOperativa" = 'SALDOFLUJOCAJA' 		THEN 'FLUJO_CAJA_INSUFICIENTE_PORTAFOLIO' 
								WHEN "REQ_IdCondicionOperativa" = 'FECHAINVALIDA' 		THEN 'FECHA_OPERACION_DISTINTA_FECHA_PROCESO'
								WHEN "REQ_IdCondicionOperativa" = 'PRODUCTOINVALIDO' 	THEN 'PRODUCTO_INVALIDO'
								ELSE '' END								AS "Codigo"
				  			
							,CASE 
								WHEN "REQ_IdCondicionOperativa" = 'CONTCONDGRAPACTO'  THEN 'DocumentoIdentidad'
								WHEN "REQ_IdCondicionOperativa" = 'PERSONABLOQUEADA' THEN 'NombrePersona'
								WHEN "REQ_IdCondicionOperativa" = 'PORTBLOQ' THEN 'IdPortafolio'
								WHEN "REQ_IdCondicionOperativa" = 'SALDOFLUJOCAJA' THEN 'IdMonedaLiquidacionOrden' 
								ELSE '' END 								AS "NombreParametro1"
								
							,CASE 
								WHEN "REQ_IdCondicionOperativa" = 'CONTCONDGRAPACTO'  THEN coalesce("CLI_DocumentoIdentidad",'')
								WHEN "REQ_IdCondicionOperativa" = 'PERSONABLOQUEADA' THEN coalesce("CLI_NombreCompleto",'')
								WHEN "REQ_IdCondicionOperativa" = 'CLIENTEBLOQUEADO' THEN coalesce("CLI_DocumentoIdentidad",'')
								WHEN "REQ_IdCondicionOperativa" = 'PORTBLOQ' THEN CONCAT("POR_CodigoPortafolio",CONCAT(' ',"POR_Proposito"))
								WHEN "REQ_IdCondicionOperativa" = 'SALDOFLUJOCAJA' THEN coalesce("PAR_IdMoneda",'')
								WHEN "REQ_IdCondicionOperativa" = 'PRODUCTOINVALIDO'  THEN coalesce("CLI_DocumentoIdentidad",'')
								ELSE '' END AS "ValorParametro1"
								
							,CASE 
								WHEN "REQ_IdCondicionOperativa" = 'SALDOFLUJOCAJA' THEN 'FlujoCajaPortafolio' 
								ELSE '' END 								AS "NombreParametro2"
								
							,CASE 
								WHEN "REQ_IdCondicionOperativa" = 'SALDOFLUJOCAJA' THEN CONCAT("POR_CodigoPortafolio",CONCAT(' ',"POR_Proposito"))
								ELSE '' END AS "ValorParametro2"
							
							
							
						FROM #CumpleRequisitosClienteIngresoOperacionRF_ClientesVal_Temp;