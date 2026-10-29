INSERT INTO  "BSCL.APV.BD::APV.EstadoTransicion"
( 
	
	  "IdEstadoTransicion" 	--	BIGINT  	 	NOT NULL ,
	 ,"Tipo"				--	NVARCHAR(40)    NOT NULL ,
	 ,"IdRegistro"			--	BIGINT			NOT NULL ,
	 ,"FechaTransicion" 	--	DATE			NOT NULL , 
	 ,"EstadoAnterior" 		--	NVARCHAR(3) 	NOT NULL , 
	 ,"EstadoNuevo"  		--	NVARCHAR(3) 	NOT NULL , 
	 ,"Observacion"  		--	NVARCHAR(520) 	NOT NULL , 
	 ,"Usuario"  			--	NVARCHAR(100) 	NOT NULL ,
)   
values (
	 "BSCL.APV.BD.Secuencia::APV.IdEstadoTransicion".NEXTVAL --:IdEstadoTransicion
	,:Tipo
    ,:IdRegistro
  	,:FechaTransicion
    ,:EstadoAnterior 			
    ,:EstadoNuevo 						
	,:Observacion 					
	,:Usuario 		
);