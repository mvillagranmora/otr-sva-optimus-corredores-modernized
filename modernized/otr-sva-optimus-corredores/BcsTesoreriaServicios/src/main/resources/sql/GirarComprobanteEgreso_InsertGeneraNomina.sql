INSERT INTO "BSCL.Tesoreria.BD::TES.NominaTransferencia"
				(
					  "IdNominaTransferencia"
					 ,"IdEmpresa"
					 ,"IdComprobantePartidaEgreso"
					 ,"FechaMovimiento"
					 ,"FechaIngresoRegistro"
					 ,"FechaLiquidacion"
					 ,"FechaRecepcion"				-- Fecha en que el banco recibio con exito la nomina
					 ,"IdMedioDePago"
					 ,"IdModalidadDePago"
					 ,"IdCuentaBanco"
					 ,"IdTipoCuentaBanco"
					 ,"IdMoneda"
					 ,"Monto"
					 ,"NombreArchivo"
					 ,"IdFormatoArchivo"
					 ,"IdFormatoNomina"
					 ,"EsNomina"
					 ,"EsTransferencia"
					 ,"IdUsuario"
					 ,"IdEstado"
				     ,"UsaTalonarioCheque"
				)
				VALUES
				(
					  :v_IdNominaTransferencia
					 ,:p_IdEmpresa
					 ,:v_IdComprobantePartidaEgreso
					 ,:p_FechaMovimiento
					 ,CURRENT_UTCTIMESTAMP	 
					 ,:p_out_FechaLiquidacion
					 ,'0001-01-01'
					 ,:p_IdMedioDePago
					 ,:p_IdModalidadDePago
					 ,:p_IdCuentaBancariaCorredora
					 ,:v_IdTipoCuentaBancariaCorredora
					 ,:p_out_IdMoneda
					 ,:p_Monto
					 -- Estos campos se deben borrar(deprecados)
					 ,''	   -- NombreArchivo
					 ,1		   -- IdFormatoArchivo
					 ,1		   -- IdFormatoNomina
					 ,'S'      -- GeneraNomina
					 ,'N'      -- GeneraTransferencia
					 ,:p_IdUsuario
					 ,'LIQ'
					 ,:p_UsaTalonarioCheque
				);