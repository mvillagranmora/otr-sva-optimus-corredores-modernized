INSERT INTO  "BSCL.APV.BD::REC.Cliente"
( 
	 "IdRecaudacionCliente"		--BIGINT 		NOT NULL
	,"IdRecaudacion"			--BIGINT
	,"IdRecaudacionEmpleador"	--BIGINT 		
	,"IdEmpresa"				--BIGINT		NOT NULL
	,"IdCliente"				--BIGINT		NOT NULL
	,"FechaIngreso"				--DATE 			NOT NULL
	,"TipoAfiliado"				--NVARCHAR(3)	NOT NULL
	,"IdRegimenTriburario"		--NVARCHAR(50)	NOT NULL
	,"MontoDepositoConvenido"	--DECIMAL(26,8)	NOT NULL
	,"MontoDepositoAPV"			--DECIMAL(26,8)	NOT NULL
	,"Origen"					--NVARCHAR(100)	NOT NULL
	,"IdEstado"					--BIGINT		NOT NULL
)   
values (
	 :IdRecaudacionCliente
	,:IdRecaudacion
    ,:IdRecaudacionEmpleador
  	,:IdEmpresa
    ,:IdCliente 			
    ,:FechaIngreso 						
	,:TipoAfiliado 					
	,:IdRegimenTriburario 									
	,:MontoDepositoConvenido 							
	,:MontoDepositoAPV										
	,:Origen						
	,:IdEstado		
);

