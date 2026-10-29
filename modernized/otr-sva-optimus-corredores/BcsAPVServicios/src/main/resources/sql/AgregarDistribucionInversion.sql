INSERT INTO  "BSCL.APV.BD::SCS.DistribucionInversion"
( 
	 
     "IdDistribucionInversion"		--BIGINT			NOT NULL
    ,"IdContratoSAAPV"				--BIGINT			NOT NULL		
	,"FolioContratoSAAPV" 			--NVARCHAR(50)	NOT NULL
	,"IdInstrumento"				--BIGINT			NOT NULL
	,"Nemotecnico"					--NVARCHAR(40)	NOT NULL
	,"MontoPesos" 					--DECIMAL(26,8)	NOT NULL
	,"Porcentaje"	 				--DECIMAL(26,8)	NOT NULL
	,"IdEstado"						--NVARCHAR(3)		NOT NULL
)   
values (
    "BSCL.APV.BD.Secuencias::SCS.IdDistribucionInversion".NEXTVAL
    ,:IdContratoSAAPV
	,:FolioContratoSAAPV
  	,:IdInstrumento
    ,:Nemotecnico			
    ,:MontoPesos 						
	,:Porcentaje
	,:IdEstado
);
