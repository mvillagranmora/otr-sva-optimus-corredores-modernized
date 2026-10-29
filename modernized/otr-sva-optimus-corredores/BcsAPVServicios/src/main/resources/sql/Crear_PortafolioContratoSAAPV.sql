INSERT INTO "BSCL.APV.BD::SCS.PortafolioContratoSAAPV"
(
 "IdPortafolioContratoSAAPV"
,"IdPortafolio"
,"IdContratoSAAPV"
,"IdEstado"
,"OrigenAhorro"
,"MontoPesos" 				
,"MontoUF" 					
,"PorcentajeRegimenImponible"
,"Pago" 						
,"PeriodoInicio" 			
,"PeriodoTermino" 			
)	
VALUES(
 "BSCL.APV.BD.Secuencia::SCS.IdPortafolioContratoSAAPV".NEXTVAL
,:IdPortafolio
,:IdContratoSAAPV
,:IdEstado
,:OrigenAhorro
,:MontoPesos 				
,:MontoUF			
,:PorcentajeRegimenImponible
,:Pago 						
,:PeriodoInicio 					
,:PeriodoTermino 	
);

