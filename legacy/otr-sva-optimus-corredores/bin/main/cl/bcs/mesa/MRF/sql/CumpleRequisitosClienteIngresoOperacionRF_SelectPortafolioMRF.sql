SELECT pr.*, CAST(0 AS DECIMAL (27,8)) as "FCA_Saldo", COALESCE("IdTipoProducto",'MERF') as "PRO_IdTipoProducto", COALESCE(det."IdTipoOperacion",'1') AS "PRO_IdTipoOperacion"
								 FROM #CumpleRequisitosClienteIngresoOperacionRF_PortafoliosPreferencia_Temp 								pr
								 INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio"  				por	on por."IdPortafolio"			= pr."POR_IdPortafolio"
								 INNER JOIN "BSCL.Plataforma.BD::COM.PerfilProductos" 			pp 	on por."IdPerfilProductosExtra" = pp."IdPerfilProductos"
								 INNER JOIN "BSCL.Plataforma.BD::COM.DetallePerfilProductos"	det on det."IdPerfilProductos" 		= pp."IdPerfilProductos" 
								 WHERE 
								 	 "PAR_FechaLiquidacion" 	!= ''
								 AND "REQ_IdCondicionOperativa" = 'PRODUCTOINVALIDO' 
								 AND  det."IdEstado" 			= 'ACT' 
								 AND "IdTipoProducto" 			= 'MERF'
							UNION 
							
							SELECT pr.*, CAST(0 AS DECIMAL (27 , 8)) as "FCA_Saldo", COALESCE("IdTipoProducto",'MERF') as "PRO_IdTipoProducto", COALESCE(det."IdTipoOperacion",'1') AS "PRO_IdTipoOperacion"
								 FROM #CumpleRequisitosClienteIngresoOperacionRF_PortafoliosPreferencia_Temp							pr
								 INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio"  				por	on por."IdPortafolio"	= pr."POR_IdPortafolio"
								 INNER JOIN "BSCL.Plataforma.BD::COM.PerfilProductos" 			pp 	on por."IdPerfilProductos" = pp."IdPerfilProductos"
								 INNER JOIN "BSCL.Plataforma.BD::COM.DetallePerfilProductos"	det on det."IdPerfilProductos" = pp."IdPerfilProductos"-- AND  det."IdEstado" = 'ACT' AND "IdTipoProducto" = 'MERF'
								 WHERE 
								 	 "PAR_FechaLiquidacion" 	!= ''
								 AND "REQ_IdCondicionOperativa" = 'PRODUCTOINVALIDO'
 								 AND  det."IdEstado" 			= 'ACT' 
 								 AND "IdTipoProducto" 			= 'MERF';