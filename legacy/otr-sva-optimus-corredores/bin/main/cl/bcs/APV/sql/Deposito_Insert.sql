INSERT INTO  "BSCL.APV.BD::REC.Recaudacion"
( 
	"IdRecaudacion"			--BIGINT 			NOT NULL,
	,"IdEmpresa"			--BIGINT			NOT NULL,
	,"CodigoArchivo"		--NVARCHAR(10),
	,"FolioCartola"			--BIGINT,
	,"LineaCartola"			--BIGINT,
	,"Sucursal"				--NVARCHAR(200)		NOT NULL,
	,"FechaDeposito"		--DATE 				NOT NULL,
	,"CuentaCorriente"		--BIGINT			NOT NULL,
	,"MontoDeposito"		--DECIMAL(26,8)		NOT NULL,
	,"ConceptoRecaudacion"	--NVARCHAR(300),
	,"Origen"				--NVARCHAR(100),
	,"Observacion"			--NVARCHAR(200),
	,"NumeroDocumento"		--BIGINT			NOT NULL,
	,"IdTipoDeposito"		--BIGINT			NOT NULL,
	,"IdBanco"				--BIGINT			NOT NULL,
	,"IdRegimenTributario"	--BIGINT,
	,"IdCarga"				--BIGINT			NOT NULL,
	,"IdEstado"				--BIGINT			NOT NULL,
)   
values (
     :IdRecaudacion
  	,:IdEmpresa
  	,:CodigoArchivo
    ,:FolioCartola 			
    ,:LineaCartola 						
	,:Sucursal 					
	,:FechaDeposito 				
	,:CuentaCorriente 							
	,:MontoDeposito					
	,:ConceptoRecaudacion							
	,:Origen 							
	,:Observacion							
	,:NumeroDocumento 					
	,:IdTipoDeposito 					
	,:IdBanco						
	,:IdRegimenTributario					
	,:IdCarga
	,:IdEstado
);
