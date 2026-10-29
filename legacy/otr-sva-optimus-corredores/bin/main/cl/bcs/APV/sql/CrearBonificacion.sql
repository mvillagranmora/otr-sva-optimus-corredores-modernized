INSERT INTO "BSCL.APV.BD::INV.Bonificacion"
( 
	 	"IdEmpresa"				   	  		--BIGINT			NOT NULL
		,"IdBonificacion"					--BIGINT			NOT NULL
	/*Registro tipo 1*/
		,"IdEntidadRecaudadora"				--BIGINT			NOT NULL
		,"FechaGeneracion"					--SECONDDATE		NOT NULL
	/*Registro tipo 2*/
		,"TipoRegistro"						--NVARCHAR(40)		NOT NULL /*ANTERIOR o ACTUAL*/
		,"AnoAhorro"						--BIGINT			NOT NULL
		,"AnoProceso"						--BIGINT			NOT NULL
		,"FolioEgreso"						--BIGINT			NOT NULL
		,"MontoPagadoNeto"					--DECIMAL(26,8)		NOT NULL
		,"MontoSaldoNeto"				 	--DECIMAL(26,8)		NOT NULL
		,"MontoPagadoReajustado"			--DECIMAL(26,8)		NOT NULL
		,"FechaPago"						--DATE				NOT NULL
	/*Registro tipo 3*/
		,"IdCliente"					 	--BIGINT			NOT NULL
		,"MontoBonificacionAfiliadoNeto" 	--DECIMAL(26,8)		NOT NULL
		,"PA1"								--DECIMAL(3,2)		NOT NULL
		,"PA2"								--DECIMAL(3,2)		NOT NULL
		,"PA3"								--DECIMAL(3,2)		NOT NULL
		,"MontoAfiliadoNeto"   				--DECIMAL(26,8)		NOT NULL
		,"MontoAfiliadoSaldoNeto"		    --DECIMAL(26,8)		NOT NULL
		,"MontoReajustadoPagadoAfiliado"	--DECIMAL(26,8)		NOT NULL
		,"MontoCalculado"
		,"FechaCalculado"
		,"IdEstado"							--BIGINT			NOT NULL
		,"CodigoArchivo"					--NVARCHAR(200)		NOT NULL
)   
values (
     :IdEmpresa
  	,"BSCL.APV.BD.Secuencias::INV.IdBonificacion".NEXTVAL
  	,:IdEntidadRecaudadora
    ,:FechaGeneracion 			
    ,:TipoRegistro 						
	,:AnoAhorro 					
	,:AnoProceso 				
	,:FolioEgreso 							
	,:MontoPagadoNeto					
	,:MontoSaldoNeto							
	,:MontoPagadoReajustado 							
	,:FechaPago							
	,:IdCliente 					
	,:MontoBonificacionAfiliadoNeto 					
	,:PA1						
	,:PA2
	,:PA3
	,:MontoAfiliadoNeto
	,:MontoAfiliadoSaldoNeto
	,:MontoReajustadoPagadoAfiliado
	,0
	,'1900-01-01 00:00:00.0'
	,:IdEstado
	,:CodigoArchivo
);
