SELECT 
		 "IdEmpresa"				   	  	
		,"IdBonificacion"					
	/*Registro tipo 1*/
		,"IdEntidadRecaudadora"				
		,"FechaGeneracion"					
	/*Registro tipo 2*/
		,"TipoRegistro"						
		,"AnoAhorro"						
		,"AnoProceso"						
		,"FolioEgreso"						
		,"MontoPagadoNeto"					
		,"MontoSaldoNeto"				 	
		,"MontoPagadoReajustado"			
		,"FechaPago"						
	/*Registro tipo 3*/
		,"IdCliente"					 	
		,"MontoBonificacionAfiliadoNeto" 	
		,"OrigenAhorro"						
		,"PorcentajeAhorro"					
		,"MontoAfiliadoNeto"   				
		,"MontoAfiliadoSaldoNeto"		    
		,"MontoReajustadoPagadoAfiliado"	
		,"IdEstado"							
 FROM "BSCL.APV.BD::INV.Bonificacion" 
 WHERE "IdBonificacion" IN (:IdsBonificacion)