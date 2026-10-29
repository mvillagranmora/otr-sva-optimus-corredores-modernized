INSERT INTO  "BSCL.APV.BD::REC.Empleador"
( 
	"IdRecaudacionEmpleador"	--BIGINT 		NOT NULL
	,"IdRecaudacion"			--BIGINT 		NOT NULL
	,"CodigoArchivo"			--NVARCHAR(100)
	,"IdEmpresa"				--BIGINT		NOT NULL
	,"IdPersona"				--BIGINT		NOT NULL
	,"FechaIngreso"				--DATE 			NOT NULL
	,"PeriodoPago"				--NVARCHAR(7)	NOT NULL
	,"NumeroHojas"				--BIGINT		NOT NULL
	,"TipoAfiliado"				--NVARCHAR(3)
	,"TipoPago"					--NVARCHAR(50)	NOT NULL
	,"Insistencias"				--NVARCHAR(50)
	,"MontoDepositoConvenido"	--DECIMAL(26,8)	NOT NULL
	,"MontoDepositoAPV"			--DECIMAL(26,8)	NOT NULL
	,"Reajustes"				--DECIMAL(26,8)	NOT NULL
	,"Intereses"				--DECIMAL(26,8)	NOT NULL
	,"Origen"					--NVARCHAR(100)	NOT NULL
	,"Observaciones"			--NVARCHAR(200)
	,"IdEstado"					--BIGINT		NOT NULL
)   
values (
	:IdRecaudacionEmpleador
    ,:IdRecaudacion
    ,:CodigoArchivo
  	,:IdEmpresa
    ,:IdPersona 			
    ,:FechaIngreso 						
	,:PeriodoPago 					
	,:NumeroHojas 				
	,:TipoAfiliado 							
	,:TipoPago					
	,:Insistencias							
	,:MontoDepositoConvenido 							
	,:MontoDepositoAPV							
	,:Reajustes 					
	,:Intereses 					
	,:Origen						
	,:Observaciones					
	,:IdEstado		
);
