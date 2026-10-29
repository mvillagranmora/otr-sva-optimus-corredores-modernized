set schema bscl;
DROP table #fondos;
create local temporary table #fondos(
												"IdInterno" 				BIGINT,
		    									"IdFondo"					NVARCHAR(40),
		    									"IdEmisor"					NVARCHAR(40),
		    									"Nemotecnico"				NVARCHAR(40),
		    									"NombreFondo"				NVARCHAR(100),
		    									"IdTipoFondo"				NVARCHAR(40),
		    									"IdRegimenTributario"		NVARCHAR(40),
		    									"IdPaisOrigen"				BIGINT,
		    									"CodigoSVS"					NVARCHAR(40),
		    									"MoneyMarket"   			NVARCHAR(1),
		    									"IdCategoriaInversionista"	NVARCHAR(40),
		    									"Vigencia"					NVARCHAR(1),
		    									"FondoExtranjero"			NVARCHAR(1),
		    									"IdMoneda"					NVARCHAR(3),
		    									"IdTipoInstrumento"			NVARCHAR(3),
		    									"IdFamiliaFondo"			NVARCHAR(40)
);

DELETE FROM  #fondos;
INSERT INTO #fondos VALUES(0,'PRUEBA_1','AGF','PRUEBA','NombreFondoA','1','APV57',0,'CODSVS','N','ARRIESGADO','S','n','CLP','FFM', '');


select * from #fondos;



call "BSCL.FondoMutuo.BD.Servicios::ModificarFondo" (
	p_IdEmpresa	 => 1,
	p_Fondo => #fondos,
	p_out_Resultado => ?
);


SELECT * FROM "BSCL.FondoMutuo.BD::FMM.Fondo";
